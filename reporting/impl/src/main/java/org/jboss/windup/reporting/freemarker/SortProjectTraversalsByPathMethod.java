package org.jboss.windup.reporting.freemarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import freemarker.template.DefaultIterableAdapter;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.graph.model.comparator.ProjectTraversalRootFileComparator;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Takes a list of ProjectModels and orders them according to their path.
 *
 * For example, ProjectModels with this structure:
 *
 * <ul>
 * <li>/CProject</li>
 * <li>/BProject</li>
 * <li>/AProject</li>
 * </ul>
 *
 * Will be returned as:
 *
 * <ul>
 * <li>/AProject</li>
 * <li>/BProject</li>
 * <li>/CProject</li>
 * </ul>
 *
 */
public class SortProjectTraversalsByPathMethod implements WindupFreeMarkerMethod
{
    private static final String NAME = "sortProjectTraversalsByPathAscending";

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes an Iterable<" + ProjectModelTraversal.class.getSimpleName() + "> and returns them, ordered alphabetically.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (Iterable<ProjectModelTraversal>)");
        }

        DefaultIterableAdapter argModel = (DefaultIterableAdapter) arguments.get(0);
        @SuppressWarnings("unchecked")
        ///Iterable<ProjectModelTraversal> projectTraversalIterable = (Iterable<ProjectModelTraversal>) argModel.getWrappedObject();
        Iterable<ProjectModelTraversal> projectTraversalIterable = (Iterable<ProjectModelTraversal>) argModel.getAdaptedObject(ProjectModelTraversal.class);
        List<ProjectModelTraversal> projectTraversalList = new ArrayList<>();
        for (ProjectModelTraversal traversal : projectTraversalIterable)
        {
            projectTraversalList.add(traversal);
        }
        Collections.sort(projectTraversalList, new ProjectTraversalRootFileComparator());
        ExecutionStatistics.get().end(NAME);
        return projectTraversalList;
    }

    @Override
    public void setContext(GraphRewrite event)
    {
    }
}
