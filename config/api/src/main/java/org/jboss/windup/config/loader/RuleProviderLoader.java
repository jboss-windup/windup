package org.jboss.windup.config.loader;

import java.util.List;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;

/**
 * Each configuration extension will implement this interface to provide a list of WindupRuleProviders. A
 * GraphConfigurationLoader will pull in all WindupRuleProviders and sort them based upon the provider's metadata.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface RuleProviderLoader
{
    /**
     * Return all {@link AbstractRuleProvider} instances that are relevant for this loader.
     */
    List<RuleProvider> getProviders(RuleLoaderContext ruleLoaderContext);
}
