package org.jboss.windup.exec.rulefilters;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.forge.furnace.versions.VersionRange;
import org.jboss.forge.furnace.versions.Versions;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.TechnologyReference;

/**
 * Provides filtering of the {@link RuleProvider}s by source and target.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class SourceAndTargetPredicate implements Predicate<RuleProvider>
{
    private final Map<String, VersionRange> sources;
    private final Map<String, VersionRange> targets;

    /**
     * Creates a new instance with the given set of source and target filters.
     */
    public SourceAndTargetPredicate(Collection<String> sources, Collection<String> targets)
    {
        this.sources = initSet(sources);
        this.targets = initSet(targets);
    }

    @Override
    public boolean accept(RuleProvider type)
    {
        Set<TechnologyReference> providerSources = type.getMetadata().getSourceTechnologies();
        Set<TechnologyReference> providerTargets = type.getMetadata().getTargetTechnologies();

        return (techMatches(sources, providerSources) && techMatches(targets, providerTargets));
    }

    private boolean techMatches(Map<String, VersionRange> techs, Set<TechnologyReference> technologyReferences)
    {
        if (techs.isEmpty() || technologyReferences.isEmpty())
        {
            return true;
        }

        for (TechnologyReference technologyReference : technologyReferences)
        {
            if (techs.containsKey(technologyReference.getId()))
            {
                VersionRange expectedRange = techs.get(technologyReference.getId());
                if (expectedRange == null)
                    return true;

                return VersionRangeUtil.versionRangesOverlap(expectedRange, technologyReference.getVersionRange());
            }
        }

        return false;
    }

    private Map<String, VersionRange> initSet(Collection<String> values)
    {
        if (values == null)
        {
            return Collections.emptyMap();
        }

        Map<String, VersionRange> result = new HashMap<>();
        for (String value : values)
        {
            if (value.contains(":"))
            {
                String tech = StringUtils.substringBefore(value, ":");
                String versionRangeString = StringUtils.substringAfter(value, ":");
                if (!versionRangeString.matches("^[(\\[].*[)\\]]"))
                    versionRangeString = "[" + versionRangeString + "]";

                VersionRange versionRange = Versions.parseVersionRange(versionRangeString);
                result.put(tech, versionRange);
            } else
            {
                result.put(value, null);
            }
        }
        return result;
    }


    @Override
    public String toString()
    {
        return "SourceAndTargetPredicate{" + "sources=" + sources + ", targets=" + targets + '}';
    }
}
