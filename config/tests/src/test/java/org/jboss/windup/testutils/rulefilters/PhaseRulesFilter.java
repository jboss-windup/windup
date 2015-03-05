package org.jboss.windup.testutils.rulefilters;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.config.phase.RulePhase;

/**
 * Filters the rules with given phases.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class PhaseRulesFilter implements RuleFilter
{
    private final Set<RulePhase> phases;

    public PhaseRulesFilter( Class<? extends RulePhase> ... phases )
    {
        this.phases = new HashSet(Arrays.asList(phases));
    }


    @Override
    public boolean accept(WindupRuleProvider provider)
    {
        return this.phases.contains( provider.getPhase() );
    }


    /**
     * Filters the rules with phase = REPORTING_*.
     *
     * @author Ondrej Zizka, ozizka at redhat.com
     */
    public static class ReportingRulesFilter extends PhaseRulesFilter
    {
        public ReportingRulesFilter()
        {
            super(ReportGenerationPhase.class, ReportRenderingPhase.class);
        }
    }

}// class
