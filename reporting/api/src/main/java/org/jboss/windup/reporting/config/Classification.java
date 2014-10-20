package org.jboss.windup.reporting.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.FileReferenceModel;
import org.jboss.windup.reporting.model.LinkModel;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Classifies a {@link FileModel} {@link Iteration} payload.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Classification extends AbstractIterationOperation<FileModel>
{
    private static final Logger log = Logger.getLogger(Classification.class.getName());

    private List<Link> links = new ArrayList<>();
    private String classificationText;
    private String description;
    private int effort;
    private boolean proprietary;

    Classification(String variable)
    {
        super(variable);
    }

    Classification()
    {
        super();
    }

    /**
     * Set the payload to the fileModel of the given instance even though the variable is not directly referencing it. This is mainly to simplify the
     * creation of the rule, when the FileModel itself is not being iterated but just a model referencing it.
     * 
     */
    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        checkVariableName(event, context);
        WindupVertexFrame payload = resolveVariable(event, getVariableName());
        if (payload instanceof FileReferenceModel)
        {
            perform(event, context, ((FileReferenceModel) payload).getFile());
        }
        else
        {
            super.perform(event, context);
        }

    }

    /**
     * Create a new classification for the given ref.
     */
    public static ClassificationBuilderOf of(String variable)
    {
        return new ClassificationBuilderOf(variable);
    }

    /**
     * Set the description of this {@link Classification}.
     */
    public Classification withDescription(String description)
    {
        this.description = description;
        return this;
    }

    /**
     * Add a {@link Link} to this {@link Classification}.
     */
    public Classification with(Link link)
    {
        this.links.add(link);
        return this;
    }

    public Classification withEffort(int effort)
    {
        this.effort = effort;
        return this;
    }

    /**
     * Classify the current {@link FileModel} as the given text.
     */
    public static Classification as(String classificationText)
    {
        Assert.notNull(classificationText, "Classification text must not be null.");
        Classification classification = new Classification();
        classification.classificationText = classificationText;
        return classification;
    }

    /**
     * Classify the current {@link FileModel} as the given text, and also make sure that this indicates the inclusion of proprietary code.
     */
    public static Classification asProprietary(String classificationText)
    {
        Classification classification = as(classificationText);
        classification.proprietary = true;
        return classification;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
    {
        /*
         * Check for duplicate classifications before we do anything. If a classification already exists, then we don't want to add another.
         */
        GraphContext graphContext = event.getGraphContext();
        GraphService<ClassificationModel> classificationService = new GraphService<ClassificationModel>(graphContext,
                    ClassificationModel.class);
        ClassificationModel classification = classificationService.getUniqueByProperty(
                    ClassificationModel.CLASSIFICATION, classificationText);

        if (classification == null)
        {
            classification = classificationService.create();
            classification.setEffort(effort);
            classification.setDescription(description);
            classification.setClassifiation(classificationText);
            classification.setContainsProprietaryCode(proprietary);
            classification.setRuleID(((Rule) context.get(Rule.class)).getId());

            GraphService<LinkModel> linkService = new GraphService<>(graphContext, LinkModel.class);
            for (Link link : links)
            {
                LinkModel linkModel = linkService.create();
                linkModel.setDescription(link.getDescription());
                linkModel.setLink(link.getLink());
                classification.addLink(linkModel);
            }
        }

        // check for duplicate adds first
        for (FileModel existingFileModel : classification.getFileModels())
        {
            if (existingFileModel.asVertex().getId().equals(payload.asVertex().getId()))
            {
                log.info("Classification already added to " + payload.getPrettyPathWithinProject() + " [" + this
                            + "] -- not adding again");
                return;
            }
        }
        classification.addFileModel(payload);
        log.info("Classification added to " + payload.getPrettyPathWithinProject() + " [" + this + "] ");
    }

    protected void setClassificationText(String classification)
    {
        this.classificationText = classification;
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("Classification.as(" + classificationText + ")");
        if (description != null && !description.trim().isEmpty())
            result.append(".withDescription(" + description + ")");
        if (effort != 0)
            result.append(".withEffort(" + effort + ")");
        if (links != null && !links.isEmpty())
            result.append(".with(" + links + ")");
        return result.toString();
    }

}
