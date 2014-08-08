package org.jboss.windup.graph.typedgraph.map;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@RunWith(Arquillian.class)
public class FrameMapHandlerTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClasses(MapMainModel.class, MapValueModel.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Test
    public void testMapHandling() throws Exception
    {
        Assert.assertNotNull(context);

        MapMainModel mainModel = context.getFramed().addVertex(null, MapMainModel.class);
        MapValueModel value1 = context.getFramed().addVertex(null, MapValueModel.class);
        value1.setProperty("value1");
        MapValueModel value2 = context.getFramed().addVertex(null, MapValueModel.class);
        value2.setProperty("value2");
        MapValueModel value3 = context.getFramed().addVertex(null, MapValueModel.class);
        value3.setProperty("value3");

        Map<String, MapValueModel> map = new HashMap<>();
        map.put("key1", value1);
        map.put("key2", value2);
        map.put("key3", value3);

        mainModel.setMap(map);

        Iterable<Vertex> vertices = context.getFramed().query()
                    .has("type", Text.CONTAINS, MapMainModel.class.getAnnotation(TypeValue.class).value())
                    .vertices();

        int numberFound = 0;
        for (Vertex v : vertices)
        {
            numberFound++;
            MapMainModel framed = (MapMainModel) context.getFramed().frame(v, WindupVertexFrame.class);

            Assert.assertTrue(framed instanceof MapMainModel);

            Map<String, MapValueModel> foundMap = framed.getMap();
            Assert.assertEquals(3, foundMap.size());

            Assert.assertEquals("value1", foundMap.get("key1").getProperty());
            Assert.assertEquals("value2", foundMap.get("key2").getProperty());
            Assert.assertEquals("value3", foundMap.get("key3").getProperty());
        }
        Assert.assertEquals(1, numberFound);
    }
}
