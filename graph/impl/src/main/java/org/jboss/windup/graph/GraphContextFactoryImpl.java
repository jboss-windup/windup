package org.jboss.windup.graph;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;

public class GraphContextFactoryImpl implements GraphContextFactory
{
    private static Logger LOG = Logging.get(GraphContextFactoryImpl.class);

    private GraphApiCompositeClassLoaderProvider graphApiCompositeClassLoaderProvider;
    private Furnace furnace;
    private GraphTypeManager graphTypeManager;

    private WeakHashMap<String, GraphContext> graphMap = new WeakHashMap();

    private Furnace getFurnace()
    {
        if (furnace == null)
            this.furnace = SimpleContainer.getFurnace(GraphContextFactory.class.getClassLoader());
        return this.furnace;
    }

    private GraphApiCompositeClassLoaderProvider getGraphApiCompositeClassLoaderProvider()
    {
        if (this.graphApiCompositeClassLoaderProvider == null)
            this.graphApiCompositeClassLoaderProvider = getFurnace().getAddonRegistry().getServices(GraphApiCompositeClassLoaderProvider.class)
                        .get();
        return this.graphApiCompositeClassLoaderProvider;
    }

    private GraphTypeManager getGraphTypeManager()
    {
        if (this.graphTypeManager == null)
            this.graphTypeManager = getFurnace().getAddonRegistry().getServices(GraphTypeManager.class).get();

        return this.graphTypeManager;
    }

    @Override
    public GraphContext create()
    {
        return ExecutionStatistics.performBenchmarked(GraphContextFactory.class.getName() + ".create(Path)", () -> {
            GraphContext graphContext = new GraphContextImpl(
                    getFurnace(),
                    getGraphTypeManager(),
                    getGraphApiCompositeClassLoaderProvider(),
                    getTempGraphDirectory()).create(false);
            graphMap.put(graphContext.getGraphDirectory().toString(), graphContext);
            return graphContext;
    }
        );
    }

    @Override
    public GraphContext create(Path graphDir)
    {
        return ExecutionStatistics.performBenchmarked(GraphContextFactory.class.getName() + ".create(Path)", () -> {
            GraphContext graphContext = new GraphContextImpl(
                    getFurnace(),
                    getGraphTypeManager(),
                    getGraphApiCompositeClassLoaderProvider(),
                    graphDir).create(true);
            graphMap.put(graphContext.getGraphDirectory().toString(), graphContext);
            return graphContext;
        });
    }

    @Override
    public GraphContext load(Path graphDir)
    {
        GraphContext graphContext = new GraphContextImpl(
                    getFurnace(),
                    getGraphTypeManager(),
                    getGraphApiCompositeClassLoaderProvider(),
                    graphDir).load();
        graphMap.put(graphContext.getGraphDirectory().toString(), graphContext);
        return graphContext;
    }

    private Path getTempGraphDirectory()
    {
        return new File(FileUtils.getTempDirectory(), "windupgraph_" + RandomStringUtils.randomAlphanumeric(6))
                    .toPath();
    }

    @Override
    public void closeAll()
    {
        try
        {
            LOG.info("Checking for any previously opened graphs...");
            LOG.info("Already opened: " + graphMap.keySet());
            for (String graphName : graphMap.keySet())
            {
                LOG.info("Still open graph: " + graphName);
                GraphContext graphContext = graphMap.get(graphName);
                graphContext.close();
                LOG.info("Closed graph: " + graphName);
            }
        }
        catch (Throwable t)
        {
            LOG.log(Level.WARNING, "Failed at closing previously opened graphs due to: " + t.getMessage(), t);
        }
    }
}
