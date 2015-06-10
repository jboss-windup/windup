package org.jboss.windup.rules.java;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Tests the javaclass condition along with hint-not-exists condition.
 */
@RunWith(Arquillian.class)
public class JavaClassWithoutHintTest
{
    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addAsResource("org/jboss/windup/rules/java/javaclass-withouthint.windup.xml");
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testJavaClassCondition() throws IOException, InstantiationException, IllegalAccessException
    {
        try (GraphContext context = factory.create(getDefaultPath()))
        {
            final String inputDir = "src/test/resources/org/jboss/windup/rules/java";

            final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                        "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
            pm.setName("Main Project");

            FileModel inputPathFrame = context.getFramed().addVertex(null, FileModel.class);
            inputPathFrame.setFilePath(inputDir);
            inputPathFrame.setProjectModel(pm);
            pm.addFileModel(inputPathFrame);

            pm.setRootFileModel(inputPathFrame);

            FileModel fileModel = context.getFramed().addVertex(null, FileModel.class);
            fileModel.setFilePath(inputDir + "/JavaClassTestFile1.java");
            fileModel.setProjectModel(pm);
            pm.addFileModel(fileModel);

            fileModel = context.getFramed().addVertex(null, FileModel.class);
            fileModel.setFilePath(inputDir + "/JavaClassTestFile2.java");
            fileModel.setProjectModel(pm);
            pm.addFileModel(fileModel);

            context.getGraph().getBaseGraph().commit();

            final WindupConfiguration processorConfig = new WindupConfiguration().setOutputDirectory(outputPath);
            processorConfig.setGraphContext(context);
            processorConfig.setInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));

            processor.execute(processorConfig);

            GraphService<JavaTypeReferenceModel> typeRefService = new GraphService<>(context,
                        JavaTypeReferenceModel.class);
            Iterable<JavaTypeReferenceModel> typeReferences = typeRefService.findAll();

            int count = 0;
            for (JavaTypeReferenceModel ref : typeReferences)
            {
                String sourceSnippit = ref.getResolvedSourceSnippit();
                Assert.assertTrue(sourceSnippit.contains("org.jboss.forge") || sourceSnippit.contains("org.jboss.windup.rules.java.JavaClassTestFile"));
                count++;
            }
            Assert.assertEquals(6, count);

            GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
            Iterable<InlineHintModel> hints = hintService.findAll();

            count = 0;
            for (InlineHintModel hint : hints)
            {
                if (hint.getHint().contains("Rule1"))
                    count++;
            }
            Assert.assertEquals(1, count);

            count = 0;
            for (InlineHintModel hint : hints)
            {
                if (hint.getHint().contains("Rule2"))
                {

                    if (hint.getFileLocationReference().getSourceSnippit().contains("org.jboss.forge.furnace.util.Callables"))
                    {
                        count++;
                    }
                    else
                    {
                        Assert.fail("Catch-all hint should not be registered for locations that already contain hint");
                    }
                }

            }
            Assert.assertEquals(1, count);

        }
    }

    private Path getDefaultPath()
    {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                    .resolve("windupgraph_javaclasstest_" + RandomStringUtils.randomAlphanumeric(6));
    }
}