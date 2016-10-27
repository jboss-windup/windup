package org.jboss.windup.reporting.problemsummary;

import java.util.*;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.TagUtil;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;

/**
 * Gets information about incidents found during the analysis and provides methods for summarizing and analyzing
 * this data.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class ProblemSummaryService
{
    /**
     * Gets lists of {@link ProblemSummary} objects organized by {@link Severity}.
     */
    public static Map<Severity, List<ProblemSummary>> getProblemSummaries(GraphContext context, Set<ProjectModel> projectModels, Set<String> includeTags,
                Set<String> excludeTags)
    {
        // The key is the severity as a String
        Map<Severity, List<ProblemSummary>> results = new TreeMap<>(new Comparator<Severity>()
        {
            @Override
            public int compare(Severity severity1, Severity severity2)
            {
                return severity1.ordinal() - severity2.ordinal();
            }
        });
        Map<RuleSummaryKey, ProblemSummary> ruleToSummary = new HashMap<>();

        InlineHintService hintService = new InlineHintService(context);
        final Iterable<InlineHintModel> hints = projectModels == null ? hintService.findAll() : hintService.getHintsForProjects(projectModels);
        for (InlineHintModel hint : hints)
        {
            Set<String> tags = hint.getTags();
            if (!TagUtil.checkMatchingTags(tags, includeTags, excludeTags, false))
                continue;

            RuleSummaryKey key = new RuleSummaryKey(hint.getEffort(), hint.getRuleID(), hint.getTitle());

            ProblemSummary summary = ruleToSummary.get(key);
            if (summary == null)
            {
                summary = new ProblemSummary(UUID.randomUUID().toString(), hint.getSeverity(), hint.getRuleID(), hint.getTitle(), 1, hint.getEffort());
                for (LinkModel link : hint.getLinks())
                {
                    summary.addLink(link.getDescription(), link.getLink());
                }
                ruleToSummary.put(key, summary);
                addToResults(results, summary);
            }
            else
            {
                summary.setNumberFound(summary.getNumberFound() + 1);
            }
            summary.addFile(hint.getHint(), hint.getFile());
        }

        ClassificationService classificationService = new ClassificationService(context);
        for (ClassificationModel classification : classificationService.findAll())
        {
            Set<String> tags = classification.getTags();
            if (!TagUtil.checkMatchingTags(tags, includeTags, excludeTags, false))
                continue;

            List<FileModel> newFileModels = new ArrayList<>();
            for (FileModel file : classification.getFileModels())
            {
                if (projectModels != null)
                {
                    // make sure this one is in the project
                    if (!projectModels.contains(file.getProjectModel()))
                        continue;
                }
                newFileModels.add(file);
            }

            if (newFileModels.isEmpty())
                continue;

            RuleSummaryKey key = new RuleSummaryKey(classification.getEffort(), classification.getRuleID(), classification.getClassification());
            ProblemSummary summary = ruleToSummary.get(key);
            if (summary == null)
            {
                summary = new ProblemSummary(UUID.randomUUID().toString(), classification.getSeverity(), classification.getRuleID(),
                            classification.getClassification(),
                            0, classification.getEffort());
                for (LinkModel link : classification.getLinks())
                {
                    summary.addLink(link.getDescription(), link.getLink());
                }
                ruleToSummary.put(key, summary);
                addToResults(results, summary);
            }

            for (FileModel file : newFileModels)
                summary.addFile(classification.getDescription(), file);

            summary.setNumberFound(summary.getNumberFound() + newFileModels.size());
        }

        return results;
    }

    private static void addToResults(Map<Severity, List<ProblemSummary>> results, ProblemSummary summary)
    {
        List<ProblemSummary> list = results.get(summary.getSeverity());
        if (list == null)
        {
            list = new ArrayList<>();
            results.put(summary.getSeverity(), list);
        }
        list.add(summary);
    }
}
