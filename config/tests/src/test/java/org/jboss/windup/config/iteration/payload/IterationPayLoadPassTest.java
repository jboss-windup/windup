package org.jboss.windup.config.iteration.payload;

import java.nio.file.Path;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.iteration.TestSimple1Model;
import org.jboss.windup.config.iteration.TestSimple2Model;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.PathService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class IterationPayLoadPassTest
{
    public static int modelCounter = 0;

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClasses(
                                TestPayloadModel.class,
                                TestSimple2Model.class,
                                IterationPayLoadPassTest.class,
                                TestIterationPayLoadNotPassProvider.class,
                                TestIterationPayLoadPassProvider.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:windup-rules-java"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    private DefaultEvaluationContext createEvalContext(GraphRewrite event)
    {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }

    @Test
    public void testPayloadPass() throws Exception
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder))
        {

            context.getFramed().addVertex(null, TestPayloadModel.class);
            context.getFramed().addVertex(null, TestPayloadModel.class);
            context.getFramed().addVertex(null, TestPayloadModel.class);

            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            WindupConfigurationModel windupCfg = context.getFramed().addVertex(null, WindupConfigurationModel.class);
            PathService fileModelService = new PathService(context);
            windupCfg.setInputPath(fileModelService.createByPath("/tmp/testpath"));

            TestIterationPayLoadPassProvider provider = new TestIterationPayLoadPassProvider();
            Configuration configuration = provider.getConfiguration(context);

            // this should call perform()
            RuleSubset.create(configuration).perform(event, evaluationContext);
            Assert.assertEquals(3, modelCounter);
            modelCounter = 0;
        }
    }

    @Test(expected = Exception.class)
    public void testPayloadNotPass() throws Exception
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder))
        {

            context.getFramed().addVertex(null, TestSimple1Model.class);
            context.getFramed().addVertex(null, TestSimple2Model.class);
            context.getFramed().addVertex(null, TestSimple2Model.class);

            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            WindupConfigurationModel windupCfg = context.getFramed().addVertex(null, WindupConfigurationModel.class);
            PathService fileModelService = new PathService(context);
            windupCfg.setInputPath(fileModelService.createByPath("/tmp/testpath"));

            TestIterationPayLoadNotPassProvider provider = new TestIterationPayLoadNotPassProvider();
            Configuration configuration = provider.getConfiguration(context);

            // this should call perform()
            RuleSubset.create(configuration).perform(event, evaluationContext);
            Assert.assertEquals(3, modelCounter);
            modelCounter = 0;
        }
    }

    public class TestIterationPayLoadPassProvider extends AbstractRuleProvider
    {
        public TestIterationPayLoadPassProvider()
        {
            super(MetadataBuilder.forProvider(TestIterationPayLoadPassProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            Configuration configuration = ConfigurationBuilder.begin()
                        .addRule()
                        .when(Query.fromType(TestPayloadModel.class).as("list_variable"))
                        .perform(Iteration
                                    .over("list_variable").as("single_var")
                                    .perform(new AbstractIterationOperation<TestPayloadModel>()
                                    {
                                        @Override
                                        public void perform(GraphRewrite event, EvaluationContext context,
                                                    TestPayloadModel model)
                                        {
                                            modelCounter++;
                                            Assert.assertNotNull(model);
                                        }
                                    })
                                    .endIteration()
                        );
            return configuration;
        }
        // @formatter:on

    }

    public class TestIterationPayLoadNotPassProvider extends AbstractRuleProvider
    {
        public TestIterationPayLoadNotPassProvider()
        {
            super(MetadataBuilder.forProvider(TestIterationPayLoadNotPassProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            Configuration configuration = ConfigurationBuilder
                        .begin()
                        .addRule()
                        .when(Query.fromType(TestSimple2Model.class).as("do_not_perform")
                                    .and(Query.fromType(TestPayloadModel.class).as("list_variable")))
                        .perform(Iteration //first iteration
                                    .over("list_variable")
                                    .as("single_var")
                                    .perform(Iteration.over("do_not_perform") //second iteration
                                                .perform(new AbstractIterationOperation<TestPayloadModel>("single_var")
                                                {
                                                    @Override
                                                    public void perform(GraphRewrite event, EvaluationContext context,
                                                                TestPayloadModel model)
                                                    {
                                                        //should access the outer iteration, not the inner one
                                                        modelCounter++;
                                                        Assert.assertNotNull(model);
                                                    }
                                                }).endIteration())

                                    .endIteration()
                        );
            return configuration;
        }
        // @formatter:on

    }

}