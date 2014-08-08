package org.jboss.windup.ext.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonFilter;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.loader.WindupRuleProviderLoader;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.util.FurnaceCompositeClassLoader;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.furnace.FurnaceClasspathScanner;

public class GroovyWindupRuleProviderLoader implements WindupRuleProviderLoader
{
    public static final String CURRENT_WINDUP_SCRIPT = "CURRENT_WINDUP_SCRIPT";
    @Inject
    private FurnaceClasspathScanner scanner;
    @Inject
    private Furnace furnace;
    @Inject
    private GraphContext graphContext;

    @Inject
    private Imported<GroovyConfigMethod> methods;

    @Override
    @SuppressWarnings("unchecked")
    public List<WindupRuleProvider> getProviders()
    {
        final List<WindupRuleProvider> ruleProviders = new ArrayList<WindupRuleProvider>();

        Binding binding = new Binding();
        binding.setVariable("windupRuleProviderBuilders", ruleProviders);
        binding.setVariable("supportFunctions", new HashMap<>());
        binding.setVariable("graphContext", graphContext);

        GroovyConfigContext configContext = new GroovyConfigContext()
        {

            @Override
            public void addRuleProvider(WindupRuleProvider provider)
            {
                ruleProviders.add(provider);
            }

            @Override
            public GraphContext getGraphContext()
            {
                return graphContext;
            }
        };

        for (GroovyConfigMethod method : methods)
        {
            binding.setVariable(method.getName(configContext), method.getClosure(configContext));
        }

        CompilerConfiguration config = new CompilerConfiguration();
        // config.addCompilationCustomizers(new ImportCustomizer());
        ClassLoader loader = getCompositeClassloader();
        GroovyShell shell = new GroovyShell(loader, binding, config);

        try (InputStream supportFuncsIS = getClass().getResourceAsStream(
                    "/org/jboss/windup/addon/groovy/WindupGroovySupportFunctions.groovy"))
        {
            InputStreamReader isr = new InputStreamReader(supportFuncsIS);
            shell.evaluate(isr);
        }
        catch (Exception e)
        {
            throw new WindupException("Failed to load support functions due to: " + e.getMessage(), e);
        }

        Map<String, ?> supportFunctions = (Map<String, ?>) binding.getVariable("supportFunctions");
        for (Map.Entry<String, ?> supportFunctionEntry : supportFunctions.entrySet())
        {
            binding.setVariable(supportFunctionEntry.getKey(), supportFunctionEntry.getValue());
        }
        binding.setVariable("supportFunctions", null);

        for (URL resource : getScripts())
        {
            try (Reader reader = new InputStreamReader(resource.openStream()))
            {
                binding.setVariable(CURRENT_WINDUP_SCRIPT, resource.toExternalForm());
                shell.evaluate(reader);
            }
            catch (Exception e)
            {
                throw new WindupException("Failed to evaluate configuration: ", e);
            }
        }

        List<WindupRuleProvider> providers = (List<WindupRuleProvider>) binding
                    .getVariable("windupRuleProviderBuilders");

        return providers;
    }

    private ClassLoader getCompositeClassloader()
    {
        List<ClassLoader> loaders = new ArrayList<>();
        AddonFilter filter = new AddonFilter()
        {
            @Override
            public boolean accept(Addon addon)
            {
                // TODO this should only accept addons that depend on windup-config-groovy or whatever we call that
                return true;
            }
        };

        for (Addon addon : furnace.getAddonRegistry().getAddons(filter))
        {
            loaders.add(addon.getClassLoader());
        }

        return new FurnaceCompositeClassLoader(getClass().getClassLoader(), loaders);
    }

    private Iterable<URL> getScripts()
    {
        Iterable<URL> scripts = scanner.scan("windup.groovy");
        return scripts;
    }

}
