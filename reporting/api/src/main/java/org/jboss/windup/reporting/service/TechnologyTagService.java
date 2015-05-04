package org.jboss.windup.reporting.service;

import java.util.HashSet;
import java.util.Set;

import org.jboss.forge.furnace.util.Iterators;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Contains methods for finding, creating, and deleting {@link TechnologyTagModel} instances.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class TechnologyTagService extends GraphService<TechnologyTagModel>
{

    public TechnologyTagService(GraphContext context)
    {
        super(context, TechnologyTagModel.class);
    }

    /**
     * Adds the provided tag to the provided {@link ResourceModel}. If a {@link TechnologyTagModel} cannot be found with the provided name, then one will
     * be created.
     */
    public TechnologyTagModel addTagToResourceModel(ResourceModel fileModel, String tagName, TechnologyTagLevel level)
    {
        FramedGraphQuery q = getGraphContext().getQuery().type(TechnologyTagModel.class)
                    .has(TechnologyTagModel.NAME, tagName);
        TechnologyTagModel m = super.getUnique(q);
        if (m == null)
        {
            m = create();
            m.setName(tagName);
            m.setLevel(level);
        }
        m.addResourceModel(fileModel);
        return m;
    }

    /**
     * Return an {@link Iterable} containing all {@link TechnologyTagModel}s that are directly associated with the provided {@link ResourceModel}.
     */
    public Iterable<TechnologyTagModel> findTechnologyTagsForFile(ResourceModel fileModel)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(fileModel.asVertex());
        pipeline.in(TechnologyTagModel.TECH_TAG_TO_FILE_MODEL).has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, TechnologyTagModel.TYPE);
        return new FramedVertexIterable<TechnologyTagModel>(getGraphContext().getFramed(), pipeline, TechnologyTagModel.class);
    }

    /**
     * Return an {@link Iterable} containing all {@link TechnologyTagModel}s that are directly associated with the provided {@link ProjectModel}.
     */
    public Iterable<TechnologyTagModel> findTechnologyTagsForProject(ProjectModel projectModel)
    {
        Set<TechnologyTagModel> results = new HashSet<>();

        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(projectModel.asVertex());
        pipeline.out(ProjectModel.PROJECT_MODEL_TO_FILE);
        pipeline.in(TechnologyTagModel.TECH_TAG_TO_FILE_MODEL).has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, TechnologyTagModel.TYPE);

        Iterable<TechnologyTagModel> modelIterable = new FramedVertexIterable<TechnologyTagModel>(getGraphContext().getFramed(), pipeline,
                    TechnologyTagModel.class);
        results.addAll(Iterators.asSet(modelIterable));

        for (ProjectModel childProjectModel : projectModel.getChildProjects())
        {
            results.addAll(Iterators.asSet(findTechnologyTagsForProject(childProjectModel)));
        }

        return results;
    }
}
