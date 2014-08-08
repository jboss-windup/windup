/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.windup.config.model.XmlMetaFacetModel;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationFilter;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class XmlExampleRuleProvider1 extends WindupRuleProvider
{
    final List<XmlMetaFacetModel> typeSearchResults = new ArrayList<>();
    final Set<String> xmlRootNames = new HashSet<>();
    private final Set<String> excludedXmlRootNames = new HashSet<>();

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder.begin()
        .addRule()
        .when(Query.find(XmlMetaFacetModel.class).as("xmlModels"))
        .perform(Iteration
            .over(XmlMetaFacetModel.class, "xmlModels")
            .as("xml")
            .when(new AbstractIterationFilter<XmlMetaFacetModel>(XmlMetaFacetModel.class, "xml")
            {
                @Override
                public boolean evaluate(GraphRewrite event, EvaluationContext context,
                            XmlMetaFacetModel payload)
                {
                    String rootTagName = payload.getRootTagName();
                    boolean result = !"xmlTag3".equals(rootTagName);
                    return result;
                }
            })
            .perform(new AbstractIterationOperation<XmlMetaFacetModel>(XmlMetaFacetModel.class,
                        "xml")
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context,
                            XmlMetaFacetModel xmlFacetModel)
                {
                    typeSearchResults.add(xmlFacetModel);
                    if (xmlRootNames.contains(xmlFacetModel.getRootTagName()))
                    {
                        Assert.fail("Tag found multiple times");
                    }
                    xmlRootNames.add(xmlFacetModel.getRootTagName());
                }
            })
            .otherwise(new AbstractIterationOperation<XmlMetaFacetModel>(XmlMetaFacetModel.class,
                        "xml")
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context,
                            XmlMetaFacetModel payload)
                {
                    typeSearchResults.add(payload);
                    if (excludedXmlRootNames.contains(payload.getRootTagName()))
                    {
                        Assert.fail("Tag found multiple times");
                    }
                    excludedXmlRootNames.add(payload.getRootTagName());
                }
            })
            .endIteration()
        );
        return configuration;
    }

    public List<XmlMetaFacetModel> getTypeSearchResults()
    {
        return typeSearchResults;
    }

    public Set<String> getXmlRootNames()
    {
        return xmlRootNames;
    }

    public Set<String> getExcludedXmlRootNames()
    {
        return excludedXmlRootNames;
    }

}
