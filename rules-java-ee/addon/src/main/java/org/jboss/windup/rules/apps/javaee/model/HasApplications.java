package org.jboss.windup.rules.apps.javaee.model;

import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import org.jboss.windup.graph.model.BelongsToProject;
import org.jboss.windup.graph.model.ProjectModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public interface HasApplications extends BelongsToProject
{
    Iterable<ProjectModel> getApplications();

    @Override
    @JavaHandler
    Iterable<ProjectModel> getRootProjectModels();

    abstract class Impl implements HasApplications, JavaHandlerContext<Vertex>, BelongsToProject
    {

        @Override
        public Iterable<ProjectModel> getRootProjectModels()
        {
            Set<ProjectModel> projectModelSet = new HashSet<>();

            for (ProjectModel currentProjectModel : this.getApplications())
            {
                ProjectModel rootProjectModel = currentProjectModel.getRootProjectModel();
                projectModelSet.add(rootProjectModel);
            }

            return projectModelSet;
        }
    }
}
