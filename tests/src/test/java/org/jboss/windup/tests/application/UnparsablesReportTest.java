package org.jboss.windup.tests.application;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.reporting.rules.CreateUnparsableFilesReportRuleProvider;
import org.jboss.windup.testutil.html.TestUnparsablesUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The archive has the following unparsable items:
 *   jee-example-app-1.0.0.ear/unparsableClass.jar!/unparsable.class
 *   jee-example-app-1.0.0.ear/META-INF/maven/org.windup.example/unparsable/pom.xml
 *   jee-example-app-1.0.0.ear/META-INF/maven/org.windup.example/unparsable/pom.properties
 *   jee-example-app-1.0.0.ear/unparsable.jar
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */

@RunWith(Arquillian.class)
public class UnparsablesReportTest extends WindupArchitectureTest
{

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.windup.tests:test-util"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addClass(WindupArchitectureTest.class);
    }

    @Test
    public void testRunWindup() throws Exception
    {
        final String path = "../test-files/jee-example-app-1.0.0.ear";
        try (GraphContext context = super.createGraphContext())
        {
            super.runTest(context, path, false);
            validateUnparsablesReport(context);
        }
    }

    private void validateUnparsablesReport(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = reportService.getUniqueByProperty(
                    ReportModel.TEMPLATE_PATH,
                    CreateUnparsableFilesReportRuleProvider.TEMPLATE_UNPARSABLE);
        TestUnparsablesUtil util = new TestUnparsablesUtil();
        Path reportPath = Paths.get(reportService.getReportDirectory(), reportModel.getReportFilename());
        util.loadPage(reportPath);
        //Assert.assertTrue(util.checkUnparsableFileInReport("unparsableClass.jar",
        //        "jee-example-app-1.0.0.ear/unparsableClass.jar", "Can't load class"));
        //Assert.assertTrue(util.checkUnparsableFileInReport("unparsable.jar",
        //        "jee-example-app-1.0.0.ear/unparsable.jar", "Cannot unzip the file"));
        Assert.assertTrue(util.checkUnparsableFileInReport("NonParsable.class",
                "archives/jee-example-services.jar/com/NonParsable.class", "BCEL"));
        Assert.assertTrue(util.checkUnparsableFileInReport("NonParsable.xml",
                "archives/jee-example-services.jar/META-INF/NonParsable.xml", "Failed to parse XML entity"));
    }

}
