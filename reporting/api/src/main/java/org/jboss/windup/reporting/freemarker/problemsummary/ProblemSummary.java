package org.jboss.windup.reporting.freemarker.problemsummary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.model.Severity;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class ProblemSummary
{
    private final Severity severity;
    private final String ruleID;
    private final String issueName;
    private int numberFound;
    private final int effortPerIncident;
    private final Map<String, Map<FileModel, ProblemFileSummary>> descriptionToFiles = new LinkedHashMap<>();
    private final List<Link> links = new ArrayList<>();

    /**
     * Creates a new instance with the given information.
     */
    public ProblemSummary(Severity severity, String ruleID, String issueName, int numberFound, int effortPerIncident)
    {
        this.severity = severity;
        this.ruleID = ruleID;
        this.issueName = issueName;
        this.numberFound = numberFound;
        this.effortPerIncident = effortPerIncident;
    }

    /**
     * Returns the severity as a String (the String type makes integration with freemarker easier, as freemarker doesn't always retain type
     * information on enums).
     */
    public Severity getSeverity()
    {
        return severity;
    }

    /**
     * Contains the original rule id.
     */
    public String getRuleID()
    {
        return ruleID;
    }

    /**
     * Contains the issue name.
     */
    public String getIssueName()
    {
        return issueName;
    }

    /**
     * Contains the number of incidents found for this issue.
     */
    public int getNumberFound()
    {
        return numberFound;
    }

    /**
     * Sets the number found for this issue.
     */
    void setNumberFound(int numberFound)
    {
        this.numberFound = numberFound;
    }

    /**
     * Returns the effort points per incident.
     */
    public int getEffortPerIncident()
    {
        return effortPerIncident;
    }

    /**
     * Gets the descriptions found for this rule.
     */
    public Iterable<String> getDescriptions()
    {
        return descriptionToFiles.keySet();
    }

    /**
     * Gets the files associated with a particular description.
     */
    public Iterable<ProblemFileSummary> getFilesForDescription(String description)
    {
        return descriptionToFiles.get(description).values();
    }

    private Map<FileModel, ProblemFileSummary> addDescription(String description)
    {
        Map<FileModel, ProblemFileSummary> files = descriptionToFiles.get(description);
        if (files == null)
        {
            files = new LinkedHashMap<>();
            descriptionToFiles.put(description, files);
        }
        return files;
    }

    public void addLink(Link link)
    {
        this.links.add(link);
    }

    public void addLink(String label, String url)
    {
        this.links.add(Link.to(label, url));
    }


    public List<Link> getLinks()
    {
        return links;
    }



    /**
     * Adds a file with the provided description.
     */
    public void addFile(String description, FileModel fileModel)
    {
        Map<FileModel, ProblemFileSummary> files = addDescription(description);

        if (files.containsKey(fileModel))
        {
            files.get(fileModel).addOccurence();
        } else {
            files.put(fileModel, new ProblemFileSummary(fileModel, 1));
        }
    }
}
