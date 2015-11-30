package org.jboss.windup.rules.apps.javaee.rules;

import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;
import org.jboss.windup.rules.apps.javaee.service.HibernateConfigurationFileService;
import org.jboss.windup.rules.apps.javaee.service.HibernateEntityService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of Hibernate files within the application (eg, session configuration or entity lists).
 *
 */
public class CreateHibernateReportRuleProvider extends AbstractRuleProvider
{
    public static final String TEMPLATE_HIBERNATE_REPORT = "/reports/templates/hibernate.ftl";

    public CreateHibernateReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateHibernateReportRuleProvider.class, "Create Hibernate Report")
                    .setPhase(ReportGenerationPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder applicationProjectModelsFound = Query.fromType(HibernateConfigurationFileModel.class).or(
                    Query.fromType(HibernateEntityModel.class));

        GraphOperation addReport = new GraphOperation()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context)
            {
                WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                for (FileModel inputPath : windupConfiguration.getInputPaths())
                {
                    ProjectModel projectModel = inputPath.getBoundProject();
                    if (projectModel == null)
                    {
                        throw new WindupException("Error, no project found in: " + inputPath.getFilePath());
                    }
                    createHibernateReport(event.getGraphContext(), projectModel);
                }
            }

            @Override
            public String toString()
            {
                return "CreateHibernateReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(applicationProjectModelsFound)
                    .perform(addReport);
    }

    private void createHibernateReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(400);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Hibernate");
        applicationReportModel.setReportIconClass("glyphicon hibernate-nav-logo");
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_HIBERNATE_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        HibernateConfigurationFileService hibernateConfigurationFileService = new HibernateConfigurationFileService(context);
        HibernateEntityService hibernateEntityService = new HibernateEntityService(context);
        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);

        WindupVertexListModel hibernateConfigList = listService.create();
        for (HibernateConfigurationFileModel hibernateConfig : hibernateConfigurationFileService.findAll())
        {
            hibernateConfigList.addItem(hibernateConfig);
        }

        WindupVertexListModel entityList = listService.create();
        for (HibernateEntityModel entityModel : hibernateEntityService.findAll())
        {
            entityList.addItem(entityModel);
        }

        Map<String, WindupVertexFrame> additionalData = new HashMap<>(2);
        additionalData.put("hibernateConfiguration", hibernateConfigList);
        additionalData.put("hibernateEntities", entityList);
        applicationReportModel.setRelatedResource(additionalData);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "hibernate_" + projectModel.getName(), "html");
    }
}
