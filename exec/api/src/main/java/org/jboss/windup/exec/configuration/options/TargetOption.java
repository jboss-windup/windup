package org.jboss.windup.exec.configuration.options;

import java.util.Collection;

import javax.inject.Inject;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;

/**
 * Specifies the target framework or server to migrate to. This could include multiple items.
 * 
 * For example, this might be a migration from Weblogic, EJB2, and JSF 1.x to JBoss EAP 6, EJB 3, and JSF 2. Target in that case would include JBoss
 * EAP 6, EJB 3, and JSF 2.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class TargetOption extends AbstractConfigurationOption
{
    public static final String NAME = "target";

    @Inject
    private RuleProviderRegistryCache cache;

    @Override
    public Collection<?> getAvailableValues()
    {
        return cache.getAvailableTargetTechnologies();
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Target server or framework";
    }

    @Override
    public String getDescription()
    {
        return "The target server or framework to migrate to. This could include multiple items (eg, \"JBoss EAP 6\" and \"EJB 3\")";
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
