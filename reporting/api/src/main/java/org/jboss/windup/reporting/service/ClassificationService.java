package org.jboss.windup.reporting.service;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.taskdefs.Length;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.TagUtil;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.EffortReportModel;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Compare;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

/**
 * Adds methods for loading and querying ClassificationModel related data.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class ClassificationService extends GraphService<ClassificationModel>
{
    public ClassificationService(GraphContext context)
    {
        super(context, ClassificationModel.class);
    }

    /**
     * Returns the total effort points in all of the {@link ClassificationModel}s associated with the provided {@link FileModel}.
     */
    public int getMigrationEffortPoints(FileModel fileModel)
    {
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<>(fileModel.asVertex());
        classificationPipeline.in(ClassificationModel.FILE_MODEL);
        classificationPipeline.has(EffortReportModel.EFFORT, Compare.GREATER_THAN, 0);
        classificationPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);

        int classificationEffort = 0;
        for (Vertex v : classificationPipeline)
        {
            Integer migrationEffort = v.getProperty(ClassificationModel.EFFORT);
            if (migrationEffort != null)
            {
                classificationEffort += migrationEffort;
            }
        }
        return classificationEffort;
    }

    /**
     * Return all {@link ClassificationModel} instances that are attached to the given {@link FileModel} instance.
     */
    public Iterable<ClassificationModel> getClassifications(FileModel model)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(model.asVertex());
        pipeline.in(ClassificationModel.FILE_MODEL);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);
        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline, ClassificationModel.class);
    }

    /**
     * Return all {@link ClassificationModel} instances that are attached to the given {@link FileModel} instance with a specific classification name.
     */
    public Iterable<ClassificationModel> getClassificationByName(FileModel model, String classificationName)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(model.asVertex());
        pipeline.in(ClassificationModel.FILE_MODEL);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);
        pipeline.has(ClassificationModel.CLASSIFICATION, classificationName);
        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline, ClassificationModel.class);
    }

    /**
     * Returns the total effort points in all of the {@link ClassificationModel}s associated with the files in this project.
     * 
     * If set to recursive, then also include the effort points from child projects.
     */
    public int getMigrationEffortPoints(ProjectModel initialProject, Set<String> includeTags, Set<String> excludeTags, boolean recursive)
    {
        final Set<Vertex> initialVertices = new HashSet<>();
        if (recursive)
        {
            for (ProjectModel projectModel1 : initialProject.getAllProjectModels())
                initialVertices.add(projectModel1.asVertex());
        }
        else
        {
            initialVertices.add(initialProject.asVertex());
        }

        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<>(getGraphContext().getGraph());
        classificationPipeline.V();
        classificationPipeline.has(EffortReportModel.EFFORT, Compare.GREATER_THAN, 0);
        classificationPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);

        classificationPipeline.as("classification");
        classificationPipeline.out(ClassificationModel.FILE_MODEL);
        classificationPipeline.out(FileModel.FILE_TO_PROJECT_MODEL);
        classificationPipeline.filter(new PipeFunction<Vertex, Boolean>()
        {
            @Override
            public Boolean compute(Vertex argument)
            {
                return initialVertices.contains(argument);
            }
        });
        classificationPipeline.back("classification");

        int classificationEffort = 0;
        for (Vertex v : classificationPipeline)
        {
            Integer migrationEffort = v.getProperty(ClassificationModel.EFFORT);
            if (migrationEffort == null)
                continue;

            ClassificationModel classificationModel = frame(v);
            // only check tags if we have some passed in
            if (!includeTags.isEmpty() || !excludeTags.isEmpty())
            {
                if (!TagUtil.checkMatchingTags(classificationModel.getTags(), includeTags, excludeTags))
                    continue;
            }
            for (FileModel fileModel : classificationModel.getFileModels())
            {
                if (initialVertices.contains(fileModel.getProjectModel().asVertex()))
                    classificationEffort += migrationEffort;
            }
        }
        return classificationEffort;
    }

    /**
     * Attach a {@link ClassificationModel} with the given classificationText and description to the provided {@link FileModel}. If an existing Model
     * exists with the provided classificationText, that one will be used instead.
     */
    public ClassificationModel attachClassification(Rule rule, FileModel fileModel, String classificationText, String description)
    {
        ClassificationModel model = getUnique(getTypedQuery().has(ClassificationModel.CLASSIFICATION, classificationText));
        if (model == null)
        {
            model = create();
            model.setClassification(classificationText);
            model.setDescription(description);
            model.setEffort(0);
            model.addFileModel(fileModel);
            model.setRuleID(rule.getId());
        }
        else
        {
            return attachClassification(model, fileModel);
        }

        return model;
    }

    /**
     * Attach a {@link ClassificationModel} with the given classificationText and description to the provided {@link FileModel}. If an existing Model
     * exists with the provided classificationText, that one will be used instead.
     */
    public ClassificationModel attachClassification(EvaluationContext context, FileModel fileModel, String classificationText, String description)
    {
        Rule rule = (Rule) context.get(Rule.class);
        return attachClassification(rule, fileModel, classificationText, description);
    }

    private boolean isClassificationLinkedToFileModel(ClassificationModel classificationModel, FileModel fileModel)
    {
        return ClassificationServiceCache.isClassificationLinkedToFileModel(classificationModel, fileModel);
    }

    /**
     * This method just attaches the {@link ClassificationModel} to the {@link Length.FileMode}. It will only do so if this link is not already
     * present.
     */
    public ClassificationModel attachClassification(ClassificationModel classificationModel, FileModel fileModel)
    {
        if (!isClassificationLinkedToFileModel(classificationModel, fileModel))
        {
            classificationModel.addFileModel(fileModel);
        }
        ClassificationServiceCache.cacheClassificationFileModel(classificationModel, fileModel, true);

        return classificationModel;
    }

    public ClassificationModel attachLink(ClassificationModel classificationModel, LinkModel linkModel)
    {
        for (LinkModel existing : classificationModel.getLinks())
        {
            if (StringUtils.equals(existing.getLink(), linkModel.getLink()))
            {
                return classificationModel;
            }
        }
        classificationModel.addLink(linkModel);
        return classificationModel;
    }
}
