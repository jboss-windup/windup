package org.jboss.windup.graph.service;

import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.util.datastructures.IterablesUtil;
import com.tinkerpop.blueprints.GraphQuery;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.FramedElementInMemory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.InMemoryVertexFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.exception.NonUniqueResultException;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.FilteredIterator;
import static org.jboss.windup.util.Util.NL;

public class GraphService<T extends WindupVertexFrame> implements Service<T>
{
    private final Class<T> type;
    private final GraphContext context;

    public GraphService(GraphContext context, Class<T> type)
    {
        this.context = context;
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public static <T extends WindupVertexFrame> T refresh(GraphContext context, T frame)
    {
        return (T) context.getFramed().frame(frame.asVertex(), WindupVertexFrame.class);
    }

    @Override
    public void commit()
    {
        ExecutionStatistics.performBenchmarked("GraphService.commit", () ->
        {
            getGraphContext().getGraph().getBaseGraph().commit();
            return null;
        });
    }

    @Override
    public long count(final Iterable<?> obj)
    {
        return ExecutionStatistics.performBenchmarked("GraphService.count", () ->
        {
            GremlinPipeline<Iterable<?>, Object> pipe = new GremlinPipeline<>();
            long result = pipe.start(obj).count();
            return result;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public T createInMemory()
    {
        Class<?>[] resolvedTypes = new Class<?>[] { VertexFrame.class, InMemoryVertexFrame.class, type };
        return (T) Proxy.newProxyInstance(this.type.getClassLoader(),
                    resolvedTypes, new FramedElementInMemory<>(this.type));
    }

    /**
     * Create a new instance of the given {@link WindupVertexFrame} type. The ID is generated by the underlying graph database.
     */
    @Override
    public T create()
    {
        return ExecutionStatistics.performBenchmarked("GraphService.create", () -> context.getFramed().addVertex(null, type));
    }

    @Override
    public T addTypeToModel(final WindupVertexFrame model)
    {
        return ExecutionStatistics.performBenchmarked("GraphService.addTypeToModel", () -> GraphService.addTypeToModel(getGraphContext(), model, type));
    }

    protected FramedGraphQuery findAllQuery()
    {
        return context.getQuery().type(type);
    }

    @Override
    public Iterable<T> findAll()
    {
        return findAllQuery().vertices(type);
    }

    @Override
    public Iterable<T> findAllByProperties(final String[] keys, final String[] vals)
    {
        return ExecutionStatistics.performBenchmarked("GraphService.findAllByProperties(" + Arrays.asList(keys) + ")", () ->
        {
            FramedGraphQuery query = findAllQuery();
            for (int i = 0, j = keys.length; i < j; i++)
            {
                query = query.has(keys[i], vals[i]);
            }
            return query.vertices(type);
        });
    }

    @Override
    public Iterable<T> findAllByProperty(final String key, final Object value)
    {
        return findAllByProperty(key, value, false);
    }

    /**
     * Allows optional filtering by model type, because getVertices(prop, value, type) 
     * does not filter by model type (it only frames the vertex as that type).
     * That can be used to prevent collisions if multiple models use the same property name (e.g. "name").
     * 
     * @param filterByType  If true, the items returned from the graph are further filtered by this service's model type.
     */
    public Iterable<T> findAllByProperty(final String key, final Object value, final boolean filterByType)
    {
        return ExecutionStatistics.performBenchmarked("GraphService.findAllByProperty(" + key + ")", () -> {
            if (!filterByType)
                return context.getFramed().getVertices(key, value, type);

            final Iterable vertices = context.getFramed().getVertices(key, value, WindupVertexFrame.class);
            return new Iterable<T>()
            {
                @Override
                public Iterator<T> iterator()
                {
                    final FilteredIterator.Filter<T> filter = new ModelTypeFilter<>(GraphService.this.type);
                    return new FilteredIterator<T>(vertices.iterator(), filter);
                }
            };
        });
    }

    @Override
    public Iterable<T> findAllWithoutProperty(final String key, final Object value)
    {
        return ExecutionStatistics.performBenchmarked("GraphService.findAllWithoutProperty(" + key + ")", () -> findAllQuery().hasNot(key, value).vertices(type));
    }

    @Override
    public Iterable<T> findAllWithoutProperty(final String key)
    {
        return ExecutionStatistics.performBenchmarked("GraphService.findAllWithoutProperty(" + key + ")", () -> findAllQuery().hasNot(key).vertices(type));
    }

    @Override
    public Iterable<T> findAllByPropertyMatchingRegex(final String key, final String... regex)
    {
        return ExecutionStatistics.performBenchmarked("GraphService.findAllByPropertyMatchingRegex(" + key + ")", () ->
        {
            if (regex.length == 0)
                return IterablesUtil.emptyIterable();

            final String regexFinal;
            if (regex.length == 1)
            {
                regexFinal = regex[0];
            }
            else
            {
                StringBuilder builder = new StringBuilder();
                builder.append("\\b(");
                int i = 0;
                for (String value : regex)
                {
                    if (i > 0)
                        builder.append("|");
                    builder.append(value);
                    i++;
                }
                builder.append(")\\b");
                regexFinal = builder.toString();
            }
            return findAllQuery().has(key, Text.REGEX, regexFinal).vertices(type);
        });
    }

    /**
     * Returns the vertex with given ID framed into given interface.
     */
    @Override
    public T getById(Object id)
    {
        return context.getFramed().getVertex(id, this.type);
    }

    @Override
    public T frame(Vertex vertex)
    {
        return getGraphContext().getFramed().frame(vertex, this.getType());
    }

    @Override
    public Class<T> getType()
    {
        return this.type;
    }

    protected GraphQuery getTypedQuery()
    {
        return getGraphContext().getQuery().type(type);
    }

    /**
     * Returns what this' frame has in @TypeValue().
     */
    protected String getTypeValueForSearch()
    {
        TypeValue typeValue = this.type.getAnnotation(TypeValue.class);
        if (typeValue == null)
            throw new IllegalArgumentException("Must be annotated with '@TypeValue': " + this.type.getName());
        return typeValue.value();
    }

    @Override
    public T getUnique() throws NonUniqueResultException
    {
        Iterable<T> results = findAll();

        if (!results.iterator().hasNext())
        {
            return null;
        }

        Iterator<T> iterator = results.iterator();
        T result = iterator.next();

        if (iterator.hasNext())
        {
            throw new NonUniqueResultException("Expected unique value, but returned non-unique.");
        }

        return result;
    }

    @Override
    public T getUniqueByProperty(String property, Object value) throws NonUniqueResultException
    {
        return getUniqueByProperty(property, value, false);
    }

    public T getUniqueByProperty(String property, Object value, boolean enforceType) throws NonUniqueResultException
    {
        Iterable<T> results = findAllByProperty(property, value, enforceType);

        T result = null;
        for (WindupVertexFrame item : results)
        {
            // There can be other types using the same property name.
            if (!type.isInstance(item))
                continue;

            if (result != null)
            {
                throw new NonUniqueResultException("Expected unique value, but returned non-unique: " + property + " Conflicting models:"
                        + NL + "\t" + StringUtils.join(item.getClass().getInterfaces(), ", ") + NL + "\t\t" + item.toPrettyString()
                        + NL + "\t" + StringUtils.join(result.getClass().getInterfaces(), ", ") + NL + "\t\t" + result.toPrettyString());
            }
            result = (T) item;
        }

        return result;
    }

    protected T getUnique(GraphQuery framedQuery)
    {
        Iterable<Vertex> results = framedQuery.vertices();

        if (!results.iterator().hasNext())
        {
            return null;
        }

        Iterator<Vertex> iter = results.iterator();
        Vertex result = iter.next();

        if (iter.hasNext())
        {
            throw new NonUniqueResultException("Expected unique value, but returned non-unique.");
        }

        return frame(result);
    }

    protected GraphContext getGraphContext()
    {
        return context;
    }

    @Override
    public TitanTransaction newTransaction()
    {
        return context.getGraph().getBaseGraph().newTransaction();
    }

    /**
     * Adds the specified type to this frame, and returns a new object that implements this type.
     */
    public static <T extends WindupVertexFrame> T addTypeToModel(GraphContext graphContext, WindupVertexFrame frame, Class<T> type)
    {
        Vertex vertex = frame.asVertex();
        graphContext.getGraphTypeManager().addTypeToElement(type, vertex);
        return graphContext.getFramed().frame(vertex, type);
    }

    /**
     * Removes the specified type from the frame.
     */
    public static <T extends WindupVertexFrame> WindupVertexFrame removeTypeFromModel(GraphContext graphContext, WindupVertexFrame frame, Class<T> type)
    {
        Vertex vertex = frame.asVertex();
        graphContext.getGraphTypeManager().removeTypeFromElement(type, vertex);
        return graphContext.getFramed().frame(vertex, WindupVertexFrame.class);
    }

    @Override
    public void remove(final T model)
    {
        ExecutionStatistics.performBenchmarked("GraphService.commit", () ->
        {
            model.asVertex().remove();
            return null;
        });
    }


    /**
     * Only accepts vertices of given type and it's subtypes.
     */
    static class ModelTypeFilter<E extends VertexFrame> implements FilteredIterator.Filter<E>
    {
        Class<E> type;

        public ModelTypeFilter(Class<E> type)
        {
            this.type = type;
        }

        public boolean accept(E item)
        {
            return this.type.isInstance(item);
        }
    }

}
