package org.jboss.windup.graph.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(ApplicationModel.TYPE)
public interface ApplicationModel extends WindupVertexFrame
{
    String TYPE = "ApplicationModel";
    public static final String PROPERTY_APPLICATION_NAME = "applicationName";

    @Property(PROPERTY_APPLICATION_NAME)
    public void setApplicationName(String name);

    @Property(PROPERTY_APPLICATION_NAME)
    public String getApplicationName();
}
