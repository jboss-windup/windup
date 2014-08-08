package org.jboss.windup.reporting.freemarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.comparator.ProjectModelByRootFileComparator;

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
public class SortProjectsByPathMethod implements WindupFreeMarkerMethod
{
    @Override
    public String getMethodName()
    {
        return "sortProjectsByPathAscending";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (Iterable<ProjectModel>)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        @SuppressWarnings("unchecked")
        Iterable<ProjectModel> projectModelIterable = (Iterable<ProjectModel>) stringModelArg.getWrappedObject();
        List<ProjectModel> projectModelList = new ArrayList<>();
        for (ProjectModel pm : projectModelIterable)
        {
            projectModelList.add(pm);
        }
        Collections.sort(projectModelList, new ProjectModelByRootFileComparator());
        return projectModelList;
    }
}
