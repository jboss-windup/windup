package org.jboss.windup.reporting;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.freemarker.FreeMarkerOperation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class FreeMarkerOperationRuleProvider extends WindupRuleProvider
{

    @Inject
    private Furnace furnace;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_RENDERING;
    }

    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .perform(
                                FreeMarkerOperation.create(furnace, "/reports/templates/FreeMarkerOperationTest.ftl",
                                            getOutputFilename())
                    );
    }

    public String getOutputFilename()
    {
        return "testapplicationreport.html";
    }
}
