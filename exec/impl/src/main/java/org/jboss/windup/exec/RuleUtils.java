package org.jboss.windup.exec;

import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.RuleMetadataType;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;

/**
 * Utils for the Metadata. Will be likely moved to Windup Utils.
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class RuleUtils
{
    /**
     * Describes given {@link Rule} as:
     * <p>
     * <code>ID: Phase - Provider [tags ...]".</code>
     */
    public static String prettyPrintRule(Rule rule)
    {
        StringBuilder result = new StringBuilder();
        if (rule instanceof Context)
        {
            final Context context = (Context) rule;
            RuleProvider provider = (RuleProvider) context.get(RuleMetadataType.RULE_PROVIDER);

            if (provider != null && provider.getMetadata() != null)
            {
                result.append(provider.getMetadata().getPhase().getSimpleName()).append(" - ");
                result.append(provider.getMetadata().getID()).append(" - ");
            }

            if (rule.getId() != null)
                result.append(rule.getId());
        }

        return result.toString();
    }

}