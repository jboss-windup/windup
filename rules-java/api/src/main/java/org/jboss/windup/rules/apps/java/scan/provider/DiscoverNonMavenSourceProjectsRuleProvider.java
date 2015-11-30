package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Finds files that were not classified as Maven archives/projects, and adds some generic project information for them.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DiscoverNonMavenSourceProjectsRuleProvider extends AbstractRuleProvider
{
    public DiscoverNonMavenSourceProjectsRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverNonMavenSourceProjectsRuleProvider.class)
                    .setPhase(DiscoverProjectStructurePhase.class)
                    .addExecuteAfter(DiscoverNonMavenArchiveProjectsRuleProvider.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext arg0)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new AddProjectInformation());
    }

    private class AddProjectInformation extends GraphOperation
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
            for (FileModel mainFileModel : configuration.getInputPaths())
            {
                ProjectService projectModelService = new ProjectService(event.getGraphContext());
                ProjectModel mainProjectModel = mainFileModel.getBoundProject();
                if (mainProjectModel == null)
                {
                    mainProjectModel = projectModelService.create();
                    mainProjectModel.setName(mainFileModel.getFileName());
                    mainProjectModel.setDescription("Source Directory");

                    mainFileModel.setBoundProject(mainProjectModel);
                    mainProjectModel.setRootOriginLocation(mainFileModel);
                    mainProjectModel.addContainedFile(mainFileModel);
                }

                addProjectToChildFiles(mainFileModel, mainProjectModel);
            }
        }

        private void addProjectToChildFiles(FileModel fileModel, ProjectModel projectModel)
        {
            for (FileModel childFile : fileModel.getFilesInDirectory())
            {
                if (childFile.getBoundProject() == null)
                {
                    projectModel.addContainedFile(childFile);
                    childFile.setBoundProject(projectModel);
                }
                else if (childFile.getBoundProject().getParentProject() == null && !childFile.getBoundProject().equals(projectModel))
                {
                    // if the child has a project, but the project doesn't have a parent, associate it with the root
                    // project
                    childFile.getBoundProject().setParentProject(projectModel);
                }
                addProjectToChildFiles(childFile, projectModel);
            }
        }

        public String toString()
        {
            return "ScanAsNonMavenProject";
        }
    }

    @Override
    public String toString()
    {
        return "AddProjectInformation";
    }
}
