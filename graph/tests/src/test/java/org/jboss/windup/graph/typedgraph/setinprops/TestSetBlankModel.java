package org.jboss.windup.graph.typedgraph.setinprops;

import java.util.Set;

import org.jboss.windup.graph.SetInProperties;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SetInPropsBlank")
public interface TestSetBlankModel extends WindupVertexFrame
{
    @SetInProperties(propertyPrefix = "")
    Set<String> getNaturalSet();

    @SetInProperties(propertyPrefix = "")
    void setNaturalSet(Set<String> set);

    @SetInProperties(propertyPrefix = "")
    void addAllNaturalSet(Set<String> set);
}
