package org.jboss.windup.reporting.rules.generation.techreport;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ApplicationProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.reporting.model.*;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.service.TagGraphService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.config.tags.TagServiceHolder;
import org.jboss.windup.graph.model.ProjectModel;

/**
 * Creates the ReportModel for Tech stats report, and the data structure the template needs.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@RuleMetadata(phase = ReportGenerationPhase.class)
public class CreateTechReportRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logger.getLogger(CreateTechReportRuleProvider.class.getName());


    private static final String TEMPLATE_PATH_PUNCH = "/reports/templates/techReport-punchCard.ftl";
    private static final String REPORT_NAME_PUNCH = "Technologies";
    private static final String REPORT_DESCRIPTION_PUNCH =
            "This report is a statistic of technologies occurences in the input applications."
            + " It shows how the technologies are distributed and is mostly useful when analysing many applications.";

    private static final String TEMPLATE_PATH_BOXES = "/reports/templates/techReport-boxes.ftl";
    private static final String REPORT_NAME_BOXES = "Technologies";
    private static final String REPORT_DESCRIPTION_BOXES =
            "This report is a statistic of technologies occurences in the input applications."
                    + " It is an overview of what techogies are found in given project or a set of projects.";



    @Inject private TagServiceHolder tagServiceHolder;

    @Override
    public void put(Object key, Object value)
    {
        super.put(key, value);
    }

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
            /// TODO: Move this to a FeedTagStructureToGraphRuleProvider.
            .addRule()
            .perform(new GraphOperation() {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context) {
                    new TagGraphService(event.getGraphContext()).feedTheWholeTagStructureToGraph(tagServiceHolder.getTagService());
                    CreateTechReportRuleProvider.this.put(TagServiceHolder.class, tagServiceHolder);
                }
            }).withId("feedTagsToGraph")

            /// TODO: Move this to a MarkApplicationProjectModelsProvider.
            .addRule()
            .perform(new GraphOperation() {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context) {
                    new ProjectService(event.getGraphContext()).getRootProjectModels()
                            .forEach(path -> GraphService.addTypeToModel(event.getGraphContext(), path, ApplicationProjectModel.class));
                }
            }).withId("markApplicationProjectModels")


            .addRule()
            .perform(new CreateTechReportPunchCardOperation()).withId("createTechReport");
    }

    private class CreateTechReportPunchCardOperation extends GraphOperation
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext evCtx)
        {
            GraphContext grCtx = event.getGraphContext();

            // Get sectors tag and rows tag references.
            TagGraphService tagGraphService = new TagGraphService(grCtx);
            TagModel sectorsTag = tagGraphService.getTagByName(TechReportModel.EDGE_TAG_SECTORS);
            TagModel rowsTag = tagGraphService.getTagByName(TechReportModel.EDGE_TAG_ROWS);
            if (null == sectorsTag)
            {
                //throw new WindupException("Tech report sectors tag, '" + TechReportModel.EDGE_TAG_SECTORS + "', not found.");
                LOG.severe("Tech report sectors tag, '" + TechReportModel.EDGE_TAG_SECTORS + "', not found. The technology report will not be rendered.");
                return;
            }
            if (null == rowsTag)
            {
                //throw new WindupException("Tech report rows tag, '" + TechReportModel.EDGE_TAG_ROWS + "', not found.");
                LOG.severe("Tech report rows tag, '" + TechReportModel.EDGE_TAG_ROWS + "', not found. The technology report will not be rendered.");
                return;
            }

            Map<String, TechReportModel> appProjectToReportMap = new HashMap<>();

            // Create the boxes report models for each app.
            for (ApplicationProjectModel appModel : new ProjectService(grCtx).getRootProjectModels()){
                final TechReportModel appTechReport = createTechReportBoxes(grCtx, appModel);
                appTechReport.setSectorsHolderTag(sectorsTag);
                appTechReport.setRowsHolderTag(rowsTag);
                appProjectToReportMap.put(((Long)appModel.asVertex().getId()).toString(), appTechReport);
            }

            // Create the global report models.
            TechReportModel reportPunch = createTechReportPunchCard(grCtx);
            reportPunch.setSectorsHolderTag(sectorsTag);
            reportPunch.setAppProjectIdToReportMap(appProjectToReportMap);

            /* In case the box report should also appear on the global level:
            TechReportModel reportBoxes = createTechReportBoxes(grCtx);
            reportBoxes.setSectorsHolderTag(sectorsTag);
            reportBoxes.setRowsHolderTag(rowsTag);
            */


            // The actual data is computed by SortTechUsageStatsMethod.
        }

        private Map<String,Integer> computeMaxCountPerTag(Map<Long, Map<String, Integer>> countsOfTagsInApps)
        {
            final HashMap<String, Integer> maxCountPerTag = new HashMap<>();
            for (Map<String, Integer> countsOfTechs : countsOfTagsInApps.values())
            {
                countsOfTechs.forEach((techName, count) -> {
                    int current = maxCountPerTag.getOrDefault(techName, 0);
                    maxCountPerTag.put(techName, Math.max(count, current));
                });
            }
            return maxCountPerTag;
        }

        private TechReportModel createTechReportPunchCard(
                GraphContext grCtx
        ){
            TechReportModel report = createTechReportBase(grCtx);
            report.setReportName(REPORT_NAME_PUNCH);
            report.setTemplatePath(TEMPLATE_PATH_PUNCH);
            report.setDescription(REPORT_DESCRIPTION_PUNCH);
            report.setReportIconClass("glyphicon glyphicon-tags");
            report.setDisplayInGlobalApplicationIndex(true);
            report.setDisplayInApplicationReportIndex(true);

            new ReportService(grCtx).setUniqueFilename(report, "techReport-punch", "html");
            return report;
        }

        private TechReportModel createTechReportBoxes(GraphContext grCtx){
            TechReportModel report = createTechReportBase(grCtx);
            report.setReportName(REPORT_NAME_BOXES);
            report.setTemplatePath(TEMPLATE_PATH_BOXES);
            report.setDescription(REPORT_DESCRIPTION_BOXES);
            report.setReportIconClass("glyphicon glyphicon-tags");
            report.setDisplayInGlobalApplicationIndex(true);
            report.setDisplayInApplicationReportIndex(false);

            new ReportService(grCtx).setUniqueFilename(report, "techReport-boxes", "html");
            return report;
        }

        private TechReportModel createTechReportBoxes(GraphContext grCtx, ApplicationProjectModel appModel)
        {
            TechReportModel report = createTechReportBase(grCtx);
            report.setProjectModel(appModel);
            report.setDisplayInGlobalApplicationIndex(false);
            report.setDisplayInApplicationReportIndex(true);
            report.setReportName(REPORT_NAME_BOXES);
            report.setTemplatePath(TEMPLATE_PATH_BOXES);
            report.setDescription(REPORT_DESCRIPTION_BOXES);
            report.setReportIconClass("glyphicon glyphicon-tags");

            // Set the filename for the report
            new ReportService(grCtx).setUniqueFilename(report, "techReport-" + appModel.getName(), "html");

            TechReportModel techReport = new GraphService<>(grCtx, TechReportModel.class).addTypeToModel(report);
            return techReport;
        }

        private TechReportModel createTechReportBase(GraphContext grCtx)
        {
            ApplicationReportService applicationReportService = new ApplicationReportService(grCtx);
            ApplicationReportModel report = applicationReportService.create();
            report.setTemplateType(TemplateType.FREEMARKER);
            report.setMainApplicationReport(false);
            report.setReportPriority(103);

            TechReportModel techReport = new GraphService<>(grCtx, TechReportModel.class).addTypeToModel(report);
            return techReport;
        }
    }


    /*
        Needs TagService, which I don't know how to get from a Freemarker method.
        TODO: Maybe kick this and use only GetTechReportPunchCardStatsMethod#computeProjectAndTagsMatrix()?
     */
    private Map<Long, Map<String, Integer>> computeProjectAndTagsMatrix(GraphContext grCtx) {
        // App -> tag name -> occurences.
        Map<Long, Map<String, Integer>> countsOfTagsInApps = new HashMap<>();
        Map<String, Integer> maxCountPerTag = new HashMap<>();

        // What sectors (column groups) and sub-sectors (columns) should be on the report. View, Connect, Store, Sustain, ...
        Tag sectorsTag = tagServiceHolder.getTagService().getTag(TechReportModel.EDGE_TAG_SECTORS);
        if (null == sectorsTag)
            throw new WindupException("Tech report hierarchy definition tag, '"+ TechReportModel.EDGE_TAG_SECTORS +"', not found.");

        // For each sector / subsector
        for (Tag tag1 : sectorsTag.getContainedTags())
        {
            for (Tag tag2 : tag1.getContainedTags())
            {
                String tagName = tag2.getName();
                Map<Long, Integer> tagCountForAllApps = getTagCountForAllApps(grCtx, tagName);
                LOG.info("Computed tag " + tagName + ":\n" + Logging.printMap(tagCountForAllApps, true));///

                // Transpose the results from getTagCountForAllApps, so that 1st level keys are the apps.
                tagCountForAllApps.forEach((projectVertexId, count) -> {
                    Map<String, Integer> appTagCounts = countsOfTagsInApps.computeIfAbsent(projectVertexId, k -> new HashMap<>());
                    appTagCounts.put(tagName, count);
                });
            }
        }

        return countsOfTagsInApps;
    }


    /**
     * @return Map of counts of given tag and subtags occurrences in all input applications.
     *         I.e. how many items tagged with any tag under subSectorTag are there in each input application.
     *         The key is the vertex ID.
     */
    static Map<Long, Integer> getTagCountForAllApps(GraphContext grCtx, String subSectorTagName)
    {
        // Get all "subtags" of this tag.
        //Set<String> subTagsNames = getSubTagNames_tagService(subSectorTagName);
        Set<String> subTagsNames = getSubTagNames_graph(grCtx, subSectorTagName);

        // Get all apps.
        Set<ApplicationProjectModel> apps = getAllApplications(grCtx);

        Map<Long, Integer> appToTechSectorCoveredTagsOccurrenceCount = new HashMap<>();

        for (ProjectModel app : apps)
        {
            int countSoFar = 0;
            // Get the TechnologyUsageStatisticsModel's for this ProjectModel
            Iterable<Vertex> statsIt = app.asVertex().getVertices(Direction.IN, TechnologyUsageStatisticsModel.PROJECT_MODEL);
            for (Vertex vStat : statsIt)
            {
                TechnologyUsageStatisticsModel stat = grCtx.getFramed().frame(vStat, TechnologyUsageStatisticsModel.class);

                // Tags of this TechUsageStat covered by this sector Tag.
                Set<String> techStatTagsCoveredByGivenTag = stat.getTags().stream().filter(name -> subTagsNames.contains(name)).collect(Collectors.toSet());
                // TODO: Optimize this when proven stable - sum the number in the stream
                //boolean covered = stat.getTags().stream().anyMatch(name -> subTagsNames.contains(name));
                if (!techStatTagsCoveredByGivenTag.isEmpty())
                    countSoFar += stat.getOccurrenceCount();
            }
            appToTechSectorCoveredTagsOccurrenceCount.put((Long)app.asVertex().getId(), countSoFar);
        }
        return appToTechSectorCoveredTagsOccurrenceCount;
    }

    private static Set<String> getSubTagNames_graph(GraphContext grCtx, String subSectorTagName)
    {
        TagGraphService tagService = new TagGraphService(grCtx);
        Set<TagModel> subTags = tagService.getDescendantTags(tagService.getTagByName(subSectorTagName));
        return subTags.stream().map(TagModel::getName).collect(Collectors.toSet());
    }


    /**
     * Returns all ApplicationProjectModels.
     */
    private static Set<ApplicationProjectModel> getAllApplications(GraphContext grCtx)
    {
        Set<ApplicationProjectModel> apps = new HashSet<>();
        Iterable<ApplicationProjectModel> appProjects = grCtx.findAll(ApplicationProjectModel.class);
        for (ApplicationProjectModel appProject : appProjects)
            apps.add(appProject);
        return apps;
    }



    /**
     * @return Map of counts of given tag occurrences in all input applications.
     *
     * TODO This is inconvenient as the template needs things grouped by app, not by technology...
     * FIXME Due to the mismatch between how the TechUsageStats was expected to work and how it works, this approach is not possible - doesn't cover the subtags.
     */
    private static Map<ProjectModel, Integer> getTagCountForAllApps_nonDeep(GraphContext grCtx, String subSectorTagName) {
        final GraphService<TechnologyUsageStatisticsModel> techUsageService = new GraphService<>(grCtx, TechnologyUsageStatisticsModel.class);
        Iterable<TechnologyUsageStatisticsModel> usageStats = techUsageService.findAllByProperty(TechnologyUsageStatisticsModel.NAME, subSectorTagName);

        Map<ProjectModel, Integer> tagsInProject = new HashMap();
        for (TechnologyUsageStatisticsModel stat : usageStats)
        {
            LOG.info("TechnologyUsageStatisticsModel: " + stat.toPrettyString());
            // Only take the root apps.
            if (!stat.getProjectModel().equals(stat.getProjectModel().getRootProjectModel()))
                continue;
            tagsInProject.put(stat.getProjectModel(), stat.getOccurrenceCount());
        }
        return tagsInProject;
    }
}
