package org.jboss.windup.reporting.freemarker;

import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.TechnologyTagService;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Gets all technology tags for the provided {@link FileModel} (eg, "EJB", "Web XML").
 * 
 * Example call:
 * 
 * getTechnologyTagsForFile(FileModel).
 * 
 * The method will return an Iterable containing {@link TechnologyTagModel} instances.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class GetTechnologyTagsForFile implements WindupFreeMarkerMethod
{
    @Inject
    private TechnologyTagService technologyTagService;

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (FileModel)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        FileModel fileModel = (FileModel) stringModelArg.getWrappedObject();
        return technologyTagService.findTechnologyTagsForFile(fileModel);
    }

    @Override
    public String getMethodName()
    {
        return "getTechnologyTagsForFile";
    }
}
