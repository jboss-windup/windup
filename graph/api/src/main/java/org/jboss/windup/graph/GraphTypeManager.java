package org.jboss.windup.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.thinkaurelius.titan.core.TitanProperty;
import com.thinkaurelius.titan.graphdb.vertices.StandardVertex;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventVertex;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.TypeResolver;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeRegistry;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@Singleton
public class GraphTypeManager implements TypeResolver, FrameInitializer
{
    private Set<Class<? extends WindupVertexFrame>> registeredTypes = new HashSet<Class<? extends WindupVertexFrame>>();
    private TypeRegistry typeRegistry = new TypeRegistry();

    public Set<Class<? extends WindupVertexFrame>> getRegisteredTypes()
    {
        return Collections.unmodifiableSet(registeredTypes);
    }

    public void addTypeToRegistry(Class<? extends WindupVertexFrame> wvf)
    {
        if (wvf.getAnnotation(TypeValue.class) != null)
        {
            // Do not attempt to add items where this is null... we use
            // *Model types with no TypeValue to function as essentially
            // "abstract" models that would never exist on their own (only as subclasses).
            typeRegistry.add(wvf);
            registeredTypes.add(wvf);
        }
    }

    /**
     * Adds the type value to the field denoting which type the element represents.
     */
    public void addTypeToElement(Class<? extends VertexFrame> kind, Element element)
    {
    	EventVertex ev = (EventVertex)element;
    	StandardVertex v = (StandardVertex)ev.getBaseVertex();
        Class<?> typeHoldingTypeField = typeRegistry.getTypeHoldingTypeField(kind);
        if (typeHoldingTypeField == null)
            return;

        TypeValue typeValueAnnotation = kind.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            return;

        String typeFieldName = typeHoldingTypeField.getAnnotation(TypeField.class).value();
        String typeValue = typeValueAnnotation.value();

        // Store the type value in a delimited list.
        v.addProperty(typeFieldName, typeValue);
        addSuperclassType(kind, element);
    }

    @SuppressWarnings("unchecked")
    private void addSuperclassType(Class<? extends VertexFrame> kind, Element element)
    {
        for (Class<?> superInterface : kind.getInterfaces())
        {
            if (WindupVertexFrame.class.isAssignableFrom(superInterface))
            {
                addTypeToElement((Class<? extends VertexFrame>) superInterface, element);
            }
        }

    }

    /**
     * Returns the classes which this edge represents, typically subclasses.
     */
    @Override
    public Class<?>[] resolveTypes(Edge e, Class<?> defaultType)
    {
        return resolve(e, defaultType);
    }

    /**
     * Returns the classes which this vertex represents, typically subclasses.
     */
    @Override
    public Class<?>[] resolveTypes(Vertex v, Class<?> defaultType)
    {
        return resolve(v, defaultType);
    }

    /**
     * Returns the classes which this vertex/edge represents, typically subclasses. Always appends
     */
    private Class<?>[] resolve(Element e, Class<?> defaultType)
    {
        // The class field holding the name of the type holding property.
        Class<?> typeHoldingTypeField = typeRegistry.getTypeHoldingTypeField(defaultType);
        if (typeHoldingTypeField != null)
        {
            // Name of the graph element property holding the type list.
            String propName = typeHoldingTypeField.getAnnotation(TypeField.class).value();
            EventVertex ev = (EventVertex)e;
        	StandardVertex v = (StandardVertex)ev.getBaseVertex();
            
            Iterable<TitanProperty> valuesAll = v.getProperties(propName);
            if (valuesAll != null)
            {
                
                List<Class<?>> resultClasses = new ArrayList<>();
                for (TitanProperty value : valuesAll)
                {
                    Class<?> type = typeRegistry.getType(typeHoldingTypeField, value.getValue().toString());
                    if (type == null)
                        continue;
                    resultClasses.add(type);
                }
                if (!resultClasses.isEmpty())
                {
                    resultClasses.add(VertexFrame.class);
                    return resultClasses.toArray(new Class<?>[resultClasses.size()]);
                }
            }
        }
        return new Class[] { defaultType, VertexFrame.class };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initElement(Class<?> kind, FramedGraph<?> framedGraph, Element element)
    {
        if (VertexFrame.class.isAssignableFrom(kind))
        {
            addTypeToElement((Class<? extends VertexFrame>) kind, element);
        }
    }
}
