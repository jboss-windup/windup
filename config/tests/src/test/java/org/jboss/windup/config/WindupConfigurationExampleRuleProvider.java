/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.config.graphsearch.GremlinPipelineCriterion;
import org.jboss.windup.config.graphsearch.VarSelection;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.model.JavaMethodModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class WindupConfigurationExampleRuleProvider extends WindupRuleProvider
{
    private static final Logger LOG = LoggerFactory.getLogger(WindupConfigurationExampleRuleProvider.class);

    private final List<JavaMethodModel> results = new ArrayList<>();

    private WindupConfigurationModel config;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        GremlinPipelineCriterion methodNameCriterion = new GremlinPipelineCriterion()
        {
            @Override
            public void configurePipeline(GremlinPipeline<Vertex, Vertex> pipeline)
            {
                pipeline.out("javaMethod").has("methodName", "toString");
            }
        };

        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule()

                    /*
                     * Specify a set of conditions that must be met in order for the .perform() clause of this rule to
                     * be evaluated.
                     */
                    .when(
                                /*
                                 * Select all java classes with the FQCN matching "com.example.(.*)", store the
                                 * resultant list in a parameter named "javaClasses"
                                 */
                                GraphSearchConditionBuilder
                                            .create("javaClasses")
                                            .ofType(JavaClassModel.class)
                                            .withProperty("qualifiedName", GraphSearchPropertyComparisonType.REGEX,
                                                        "com\\.example\\..*")
                                            .and(
                                                        VarSelection.query("javaClasses", "javaMethods")
                                                                    .addCriterion(methodNameCriterion)
                                            )

                    )

                    /*
                     * If all conditions of the .when() clause were satisfied, the following conditions will be
                     * evaluated
                     */
                    .perform(Iteration.over("javaMethods").as(JavaMethodModel.class, "javaMethod")
                                .perform(new AbstractIterationOperation<JavaMethodModel>(JavaMethodModel.class,
                                            "javaMethod")
                                {
                                    @Override
                                    public void perform(GraphRewrite event, EvaluationContext context,
                                                JavaMethodModel methodModel)
                                    {
                                        WindupConfigurationExampleRuleProvider.this.config = GraphService
                                                    .getConfigurationModel(event.getGraphContext());

                                        results.add(methodModel);
                                        LOG.info("Overridden " + methodModel.getMethodName() + " Method in type: "
                                                    + methodModel.getJavaClass().getQualifiedName());
                                    }
                                })
                                .endIteration()
                    );
        return configuration;
    }

    public List<JavaMethodModel> getResults()
    {
        return results;
    }

    public WindupConfigurationModel getConfig()
    {
        return config;
    }
}
