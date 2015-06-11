package org.jboss.windup.exec.configuration.options;

import java.util.Collection;

import javax.inject.Inject;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;

/**
 * Specifies the source framework or server to migrate from. This could include multiple items.
 * 
 * For example, this might be a migration from Weblogic, EJB2, and JSF 1.x to JBoss EAP 6, EJB 3, and JSF 2. Source in that case would include
 * Weblogic, EJB2, and JSF 1.x.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class SourceOption extends AbstractConfigurationOption
{
    public static final String NAME = "source";

    @Inject
    private RuleProviderRegistryCache cache;

    @Override
    public Collection<?> getAvailableValues()
    {
        return cache.getAvailableSourceTechnologies();
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Source server or framework";
    }

    @Override
    public String getDescription()
    {
        return "The source server or framework to migrate from. This could include multiple items (eg, \"WebLogic\" and \"EJB 2\")";
    }

    @Override
    public InputType getUIType()
    {
        return InputType.SELECT_MANY;
    }

    @Override
    public Class<String> getType()
    {
        return String.class;
    }

    public boolean isRequired()
    {
        return false;
    }

    @Override
    public ValidationResult validate(Object value)
    {
        return ValidationResult.SUCCESS;
    }
}
