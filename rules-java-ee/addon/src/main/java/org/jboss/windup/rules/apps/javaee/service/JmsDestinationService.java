package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationType;

import java.util.Set;

/**
 * Contains methods for querying, updating, and deleting {@link JmsDestinationModel}
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class JmsDestinationService extends GraphService<JmsDestinationModel>
{
    private final JNDIResourceService jndiResourceService;

    /**
     * Creates a new {@link JmsDestinationService} instance.
     */
    public JmsDestinationService(GraphContext context)
    {
        super(context, JmsDestinationModel.class);
        this.jndiResourceService = new JNDIResourceService(context);
    }

    /**
     * Creates a new instance with the given name, or converts an existing instance at this location if one already exists
     */
    public JmsDestinationModel createUnique(Set<ProjectModel> applications, String jndiName, JmsDestinationType destinationType)
    {
        JmsDestinationModel model = createUnique(applications, jndiName);
        model.setDestinationType(destinationType);

        return model;
    }

    public JmsDestinationModel createUnique(Set<ProjectModel> applications, String jndiName)
    {
        JmsDestinationModel model = null;

        JNDIResourceModel jndiRef = jndiResourceService.createUnique(applications, jndiName);
        if (jndiRef instanceof JmsDestinationModel)
        {
            model = (JmsDestinationModel) jndiRef;
        }
        else
        {
            model = this.addTypeToModel(jndiRef);
        }

        return model;
    }
}
