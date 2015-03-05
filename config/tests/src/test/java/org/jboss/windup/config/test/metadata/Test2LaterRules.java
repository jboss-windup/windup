package org.jboss.windup.config.test.metadata;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.Log;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.phase.RulePhase;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Test2LaterRules extends SingleOpRuleProvider {

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return DependentPhase.class;
    }

    @Override
    void perform(GraphRewrite event, EvaluationContext evCtx)
    {
        Log.message(MetadataAnnotationTest.class, org.ocpsoft.logging.Logger.Level.INFO, "Inside " + Test2LaterRules.class.getSimpleName());
    }

}
