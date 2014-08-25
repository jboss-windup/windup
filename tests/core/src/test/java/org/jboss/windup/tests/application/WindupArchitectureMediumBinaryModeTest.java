package org.jboss.windup.tests.application;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.util.test.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureMediumBinaryModeTest extends WindupArchitectureTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:java-decompiler"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.ext:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
            .addBeansXML()
            .addClass(WindupArchitectureTest.class)
            .addClass(TestUtil.class)
            .addAsResource(TestUtil.Dirs.getModuleDir().resolve("src/test/groovy/GroovyExampleRule.windup.groovy").toFile())
            .addAsAddonDependencies(
                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                AddonDependencyEntry.create("org.jboss.windup.rules.apps:java-decompiler"),
                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                AddonDependencyEntry.create("org.jboss.windup.ext:windup-config-groovy"),
                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
            );
        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContext graphContext;

    @Test
    public void testRunWindupMedium() throws Exception
    {
        final String path = TestUtil.Dirs.getTestFilesDir().resolve("Windup1x-javaee-example.war").toString();
        System.out.println("Running binary mode test with: " + path);
        runTest(processor, graphContext, path, false);
    }
}
