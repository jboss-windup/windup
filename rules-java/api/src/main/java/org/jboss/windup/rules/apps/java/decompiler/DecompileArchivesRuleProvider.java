package org.jboss.windup.rules.apps.java.decompiler;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Commit;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.operation.Log;
import org.jboss.windup.config.phase.DecompilationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.ocpsoft.logging.Logger.Level;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

public class DecompileArchivesRuleProvider extends AbstractRuleProvider
{

    public DecompileArchivesRuleProvider()
    {
        super(MetadataBuilder.forProvider(DecompileArchivesRuleProvider.class)
                    .setPhase(DecompilationPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        QueryGremlinCriterion shouldDecompileCriterion = new ShouldDecompileCriterion();
        
        return ConfigurationBuilder.begin()
        .addRule()
        .when(Query.fromType(ArchiveModel.class).piped(shouldDecompileCriterion))
        .perform(
            new ProcyonDecompilerOperation()
            .and(IterationProgress.monitoring("Decompiled archive: ", 1))
            .and(Commit.every(1))
        )
        .otherwise(Log.message(Level.WARN, "No archives to decompile."));
    }
    // @formatter:on

    /**
     * A Gremlin criterion that only passes along Vertices with Java Classes that appear to be interesting (in the
     * package list that we are interested in).
     */
    private class ShouldDecompileCriterion implements QueryGremlinCriterion
    {
        @Override
        public void query(final GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
        {
            pipeline.filter(new PipeFunction<Vertex, Boolean>()
            {
                @Override
                public Boolean compute(Vertex argument)
                {
                    ArchiveModel archive = event.getGraphContext().getFramed().frame(argument, ArchiveModel.class);
                    WindupJavaConfigurationService windupJavaConfigurationService = new WindupJavaConfigurationService(
                                event.getGraphContext());
                    for (ResourceModel fileModel : archive.getContainedResourceModels())
                    {
                        if (fileModel instanceof JavaClassFileModel)
                        {
                            JavaClassFileModel javaClassResourceModel = (JavaClassFileModel) fileModel;
                            if (windupJavaConfigurationService.shouldScanPackage(javaClassResourceModel.getPackageName()))
                            {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
    }
}