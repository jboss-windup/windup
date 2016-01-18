package org.jboss.windup.testutil.basics;

import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

import java.util.List;

/**
 * A class providing helping static methods for the tests in Windup.
 */
public class WindupTestUtilMethods
{

    public static void runOnlyRuleProviders(Iterable<? extends RuleProvider> providers, GraphContext context)
    {
        GraphRewrite event = new GraphRewrite(context);
        DefaultEvaluationContext evaluationContext = createEvalContext(event);

        for (RuleProvider provider : providers)
        {
            Configuration configuration = provider.getConfiguration(context);
            RuleSubset.create(configuration).perform(event, evaluationContext);
        }
    }

    private static DefaultEvaluationContext createEvalContext(GraphRewrite event)
    {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }
}
