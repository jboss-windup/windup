package org.jboss.windup.reporting.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.opencsv.CSVWriter;

/**
 * RuleProvider generating optional CSV files for every application. This file will contain the main reporting information.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class ExportCSVFileRuleProvider extends AbstractRuleProvider
{
    public static final int COMMIT_INTERVAL = 750;
    public static final int LOG_INTERVAL = 250;
    private static final Logger LOG = Logging.get(ExportCSVFileRuleProvider.class);
    Map<String, CSVWriter> projectToFile;

    public ExportCSVFileRuleProvider()
    {
        super(MetadataBuilder.forProvider(ExportCSVFileRuleProvider.class)
                    .setPhase(ReportGenerationPhase.class)
                    .setHaltOnException(true));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(WindupConfigurationModel.class).withProperty(WindupConfigurationModel.CSV_MODE, true))
                .perform(
                        Iteration.over(Iteration.DEFAULT_VARIABLE_LIST_STRING).perform(
                                new ExportCSVReportOperation()).endIteration());
    }
    // @formatter:on

    private final class ExportCSVReportOperation extends AbstractIterationOperation<WindupConfigurationModel>
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel config)
        {
            projectToFile = new HashMap<>();
            InlineHintService hintService = new InlineHintService(event.getGraphContext());
            String outputFolderPath = config.getOutputPath().getFilePath() + File.separator;
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            ProjectService projectService = new ProjectService(event.getGraphContext());
            final Iterable<InlineHintModel> hints = hintService.findAll();
            final Iterable<ProjectModel> projects = projectService.findAll();
            final Iterable<ClassificationModel> classifications = classificationService.findAll();

            //try{} in case something bad happens, we need to close files
            try
            {
                for (InlineHintModel hint : hints)
                {
                    final ProjectModel parentRootProjectModel = hint.getFile().getBoundProject().getRootProjectModel();
                    String links = buildLinkString(hint.getLinks());
                    String ruleId = hint.getRuleID() != null ? hint.getRuleID() : "";
                    String title = hint.getTitle() != null ? hint.getTitle() : "";
                    String description = hint.getDescription() != null ? hint.getDescription() : "";
                    String projectNameString = "";
                    String fileName = "";
                    String filePath = "";
                    if (hint.getFile() != null)
                    {
                        if (hint.getFile().getBoundProject() != null)
                        {
                            projectNameString = hint.getFile().getBoundProject().getName();
                        }
                        fileName = hint.getFile().getFileName();
                        filePath = hint.getFile().getFilePath();
                    }
                    String[] strings = new String[] {
                                ruleId, "hint", title, description, links,
                                projectNameString,
                                fileName, filePath, String.valueOf(
                                hint.getLineNumber()), String.valueOf(hint.getEffort()) };
                    writeCsvRecordForProject(outputFolderPath, parentRootProjectModel, strings);

                }
                for (ClassificationModel classification : classifications)
                {
                    for (FileModel fileModel : classification.getFileModels())
                    {
                        final ProjectModel parentRootProjectModel = fileModel.getBoundProject().getRootProjectModel();
                        String links = buildLinkString(classification.getLinks());
                        String ruleId = classification.getRuleID() != null ? classification.getRuleID() : "";
                        String classifText = classification.getClassification() != null ? classification.getClassification() : "";
                        String description = classification.getDescription() != null ? classification.getDescription() : "";
                        String projectNameString = "";
                        String fileName = "";
                        String filePath = "";
                        if (fileModel.getBoundProject() != null)
                        {
                            projectNameString = fileModel.getBoundProject().getName();
                        }
                        fileName = fileModel.getFileName();
                        filePath = fileModel.getFilePath();
                        String[] strings = new String[] {
                                    ruleId, "classification", classifText,
                                    description, links,
                                    projectNameString, fileName, filePath, "N/A",
                                    String.valueOf(
                                                classification.getEffort()) };
                        writeCsvRecordForProject(outputFolderPath, parentRootProjectModel, strings);

                    }
                }
            }
            finally
            {
                for (CSVWriter csvWriter : projectToFile.values())
                {
                    try
                    {
                        csvWriter.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

            }

        }

        private String buildLinkString(Iterable<LinkModel> links)
        {
            StringBuilder linksString = new StringBuilder();
            for (LinkModel linkModel : links)
            {
                linksString.append("[");
                linksString.append(linkModel.getLink()).append(",");
                linksString.append(linkModel.getDescription());
                linksString.append("]");
            }
            linksString.toString();
            return linksString.toString();
        }

        private void writeCsvRecordForProject(String outputFolderPath, ProjectModel projectModel, String[] line)
        {
            if (!projectToFile.containsKey(projectModel.getName()))
            {
                CSVWriter writer = initCSVWriter(outputFolderPath + PathUtil.cleanFileName(projectModel.getName()) + ".csv");
                projectToFile.put(projectModel.getName(), writer);
            }
            projectToFile.get(projectModel.getName()).writeNext(line);

        }

        private CSVWriter initCSVWriter(String path)
        {
            try
            {
                CSVWriter writer = new CSVWriter(
                            new FileWriter(path), ',');
                String[] headerLine = new String[] { "Rule Id", "Problem type", "Title", "Description", "Links", "Application", "File Name",
                            "File Path", "Line", "Story points" };
                writer.writeNext(headerLine);
                return writer;
            }
            catch (IOException e)
            {
                System.err.println("Windup was not able to create a CSV file " + path + ". CSV Export will not be generated.");
                throw new WindupException("Unable to create file " + path, e);
            }
        }
    }
}
