package org.jboss.windup.graph.typedgraph;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("MapModelValue")
public interface MapValueModel extends WindupVertexFrame
{
    @Property("property")
    String getProperty();

    @Property("property")
    void setProperty(String val);
}
