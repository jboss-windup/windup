package org.jboss.windup.ui;

import java.io.File;
import java.nio.file.Path;

import org.jboss.forge.addon.maven.resources.MavenModelResource;
import org.jboss.forge.furnace.versions.SingleVersion;

import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.event.PostStartup;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.furnace.FurnaceHolder;
import org.jboss.windup.util.PathUtil;

@Singleton
public class RulesetUpdateChecker
{

    @Inject
    Furnace furnace;

    public static final String RULESET_CORE_DIRECTORY = "migration-core";

    public void perform(@Observes PostStartup event)
    {
        boolean isDependencies = event.getAddon().getId().toString().contains("org.jboss.windup.ui:windup-ui");
        if (isDependencies)
        {
            Imported<DependencyResolver> exportedTypes = furnace.getAddonRegistry().getServices(DependencyResolver.class);
            if (!exportedTypes.isUnsatisfied() && rulesetNeedUpdate(exportedTypes.get()))
            {
                System.out.println("");
                System.out.println("Your ruleset is obsolete. Consider running windup-update-ruleset or windup-update-distribution command. Press ENTER to continue.");
                System.out.println("");
            }

        }
    }

    public static boolean rulesetNeedUpdate(DependencyResolver dependencyResolver)
    {
        List<Coordinate> resolveVersions = dependencyResolver.resolveVersions(DependencyQueryBuilder.create(CoordinateBuilder.create()
                    .setGroupId("org.jboss.windup.rules")
                    .setArtifactId("windup-rulesets")));
        int i = 0;
        Coordinate latestCoordinate;
        do
        {
            i++;
            latestCoordinate = resolveVersions.get(resolveVersions.size() - i);
        }
        while (latestCoordinate.isSnapshot());
        Path windupRulesDir = PathUtil.getWindupRulesDir();
        Imported<ResourceFactory> imported = FurnaceHolder.getFurnace().getAddonRegistry().getServices(ResourceFactory.class);
        ResourceFactory factory = imported.get();
        Path coreRulesPropertiesPath = windupRulesDir.resolve(RULESET_CORE_DIRECTORY
                    + "/META-INF/maven/org.jboss.windup.rules/windup-rulesets/pom.xml");
        File pomXml = coreRulesPropertiesPath.toFile();
        if (pomXml.exists())
        {
            MavenModelResource pom = (MavenModelResource) factory.create(pomXml);
            String installedVersion = pom.getCurrentModel().getVersion();
            String latestVersion = latestCoordinate.getVersion();
            SingleVersion installed = new SingleVersion(installedVersion);
            SingleVersion latest = new SingleVersion(latestVersion);
            return versionIsOld(installed, latest);
        }
        else
        {
            return false;
        }
    }

    private static boolean versionIsOld(SingleVersion installedVersion, SingleVersion latestVersion)
    {
        int compareTo = installedVersion.compareTo(latestVersion);
        if (compareTo >= 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
