/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.exception.IllegalTypeArgumentException;
import org.jboss.windup.config.operation.iteration.IterationBuilderComplete;
import org.jboss.windup.config.operation.iteration.IterationBuilderOtherwise;
import org.jboss.windup.config.operation.iteration.IterationBuilderOver;
import org.jboss.windup.config.operation.iteration.IterationBuilderPerform;
import org.jboss.windup.config.operation.iteration.IterationBuilderVar;
import org.jboss.windup.config.operation.iteration.IterationBuilderWhen;
import org.jboss.windup.config.operation.iteration.IterationImpl;
import org.jboss.windup.config.operation.iteration.IterationPayloadManager;
import org.jboss.windup.config.operation.iteration.NamedFramesSelector;
import org.jboss.windup.config.operation.iteration.NamedIterationPayloadManager;
import org.jboss.windup.config.operation.iteration.TypedNamedFramesSelector;
import org.jboss.windup.config.operation.iteration.TypedNamedIterationPayloadManager;
import org.jboss.windup.config.selectors.FramesSelector;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.And;
import org.ocpsoft.rewrite.config.CompositeOperation;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.DefaultOperationBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public abstract class Iteration extends DefaultOperationBuilder
            implements IterationBuilderOver, IterationBuilderVar,
            IterationBuilderWhen, IterationBuilderPerform, IterationBuilderOtherwise,
            IterationBuilderComplete, CompositeOperation
{
    private Condition condition;
    private Operation operationPerform;
    private Operation operationOtherwise;

    public abstract FramesSelector getSelectionManager();

    public abstract IterationPayloadManager getPayloadManager();

    public abstract void setPayloadManager(IterationPayloadManager payloadManager);

    /**
     * Begin an {@link Iteration} over the named selection of the given type.
     */
    public static IterationBuilderOver over(Class<? extends WindupVertexFrame> sourceType, String source)
    {
        return new IterationImpl(new TypedNamedFramesSelector(sourceType, source));
    }

    /**
     * Begin an {@link Iteration} over the named selection.
     */
    public static IterationBuilderOver over(String source)
    {
        return new IterationImpl(new NamedFramesSelector(source));
    }

    @Override
    public IterationBuilderVar as(Class<? extends WindupVertexFrame> varType, String var)
    {
        setPayloadManager(new TypedNamedIterationPayloadManager(varType, var));
        return this;
    }

    @Override
    public IterationBuilderVar as(String var)
    {
        setPayloadManager(new NamedIterationPayloadManager(var));
        return this;
    }

    public IterationBuilderWhen all(Condition... condition)
    {
        this.condition = And.all(condition);
        return this;
    }

    /**
     * A condition which decides for each frame whether .perform() or otherwise() will be processed.
     */
    @Override
    public IterationBuilderWhen when(Condition condition)
    {
        this.condition = condition;
        return this;
    }

    /**
     * Will be processed for frames which comply to the condition in when().
     */
    @Override
    public IterationBuilderPerform perform(Operation operation)
    {
        this.operationPerform = operation;
        return this;
    }

    /**
     * Will be processed for frames which DO NOT comply to the condition in when().
     */
    @Override
    public IterationBuilderOtherwise otherwise(Operation operation)
    {
        this.operationOtherwise = operation;
        return this;
    }

    /**
     * Visual cap of the iteration.
     */
    @Override
    public IterationBuilderComplete endIteration()
    {
        return this;
    }

    /**
     * Called internally to actually process the Iteration.
     */
    @Override
    public void perform(Rewrite event, EvaluationContext context)
    {
        perform((GraphRewrite) event, context);
    }

    /**
     * Called internally to actually process the Iteration. Loops over the frames to iterate, and performs their
     * .perform( ... ) or .otherwise( ... ) parts.
     */
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        Variables variables = Variables.instance(event);
        variables.push();
        Iterable<WindupVertexFrame> frames = getSelectionManager().getFrames(event, context);
        for (WindupVertexFrame frame : frames)
        {
            getPayloadManager().setCurrentPayload(variables, frame);
            if (condition == null || condition.evaluate(event, context))
            {
                if (operationPerform != null)
                {
                    operationPerform.perform(event, context);
                }
            }
            else if (condition != null)
            {
                if (operationOtherwise != null)
                {
                    operationOtherwise.perform(event, context);
                }
            }
        }
        getPayloadManager().removeCurrentPayload(variables);
        variables.pop();
    }

    @Override
    public List<Operation> getOperations()
    {
        return Arrays.asList(operationPerform, operationOtherwise);
    }

    /**
     * Set the current {@link Iteration} payload.
     */
    public static void setCurrentPayload(Variables stack, String name, WindupVertexFrame frame)
                throws IllegalArgumentException
    {
        Map<String, Iterable<WindupVertexFrame>> vars = stack.peek();

        Iterable<WindupVertexFrame> existingValue = vars.get(name);
        if (!(existingValue == null || existingValue instanceof IterationPayload))
        {
            throw new IllegalArgumentException("Variable \"" + name
                        + "\" has already been assigned and cannot be used as an " + Iteration.class.getSimpleName()
                        + " variable.");
        }

        vars.put(name, new IterationPayload<WindupVertexFrame>(frame));
    }

    /**
     * Get the {@link Iteration} payload with the given name.
     * 
     * @throws IllegalArgumentException if the given variable refers to a non-payload.
     */
    @SuppressWarnings("unchecked")
    public static <FRAMETYPE extends WindupVertexFrame> FRAMETYPE getCurrentPayload(Variables stack, String name)
                throws IllegalStateException, IllegalArgumentException
    {
        Map<String, Iterable<WindupVertexFrame>> vars = stack.peek();

        Iterable<WindupVertexFrame> existingValue = vars.get(name);
        if (!(existingValue == null || existingValue instanceof IterationPayload))
        {
            throw new IllegalArgumentException("Variable \"" + name
                        + "\" is not an " + Iteration.class.getSimpleName() + " variable.");
        }

        Object object = stack.findSingletonVariable(name);
        return (FRAMETYPE) object;
    }

    /**
     * Get the {@link Iteration} payload with the given name and type.
     * 
     * @throws IllegalArgumentException if the given variable refers to a non-payload.
     */
    @SuppressWarnings("unchecked")
    public static <FRAMETYPE extends WindupVertexFrame> FRAMETYPE getCurrentPayload(Variables stack,
                Class<FRAMETYPE> type, String name) throws IllegalStateException, IllegalArgumentException
    {
        Map<String, Iterable<WindupVertexFrame>> vars = stack.peek();

        Iterable<WindupVertexFrame> existingValue = vars.get(name);
        if (!(existingValue == null || existingValue instanceof IterationPayload))
        {
            throw new IllegalArgumentException("Variable \"" + name
                        + "\" is not an " + Iteration.class.getSimpleName() + " variable.");
        }

        Object object = stack.findSingletonVariable(type, name);
        return (FRAMETYPE) object;
    }

    /**
     * Remove the current {@link Iteration} payload.
     */
    public static <FRAMETYPE extends WindupVertexFrame> FRAMETYPE removeCurrentPayload(Variables stack,
                Class<FRAMETYPE> type, String name)
                throws IllegalStateException, IllegalTypeArgumentException
    {
        FRAMETYPE payload = getCurrentPayload(stack, type, name);

        Map<String, Iterable<WindupVertexFrame>> vars = stack.peek();
        vars.remove(name);

        return payload;
    }

    /**
     * Remove the current {@link Iteration} payload.
     */
    public static <FRAMETYPE extends WindupVertexFrame> FRAMETYPE removeCurrentPayload(Variables stack, String name)
                throws IllegalStateException, IllegalTypeArgumentException
    {
        FRAMETYPE payload = getCurrentPayload(stack, name);

        Map<String, Iterable<WindupVertexFrame>> vars = stack.peek();
        vars.remove(name);

        return payload;
    }

    private static class IterationPayload<T> extends HashSet<T>
    {
        private static final long serialVersionUID = 7725055142596456025L;

        public IterationPayload(T element)
        {
            super(1);
            super.add(element);
        }

        @Override
        public boolean add(T e)
        {
            throw new UnsupportedOperationException("Iteration payloads are not modifiable.");
        }

        @Override
        public boolean remove(Object o)
        {
            throw new UnsupportedOperationException("Iteration payloads are not modifiable.");
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException("Iteration payloads are not modifiable.");
        }

        @Override
        public boolean addAll(Collection<? extends T> c)
        {
            throw new UnsupportedOperationException("Iteration payloads are not modifiable.");
        }

        @Override
        public boolean removeAll(Collection<?> c)
        {
            throw new UnsupportedOperationException("Iteration payloads are not modifiable.");
        }

        @Override
        public boolean retainAll(Collection<?> c)
        {
            throw new UnsupportedOperationException("Iteration payloads are not modifiable.");
        }

    }
}
