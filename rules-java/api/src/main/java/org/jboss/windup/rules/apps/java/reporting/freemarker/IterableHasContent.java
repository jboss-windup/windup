package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;

import freemarker.core.CollectionAndSequence;
import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;

/**
 * Returns whether an Iterable has values within it.
 * 
 * Called as follows:
 * 
 * getPrettyPathForFile(Iterable)
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class IterableHasContent implements WindupFreeMarkerMethod
{
    private static final String NAME = "iterableHasContent";

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        try
        {
            if (arguments.size() != 1)
            {
                throw new TemplateModelException("Error, method expects one argument (Iterable)");
            }
            return hasContent(arguments.get(0));
        }
        finally
        {
            ExecutionStatistics.get().end(NAME);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean hasContent(Object arg) throws TemplateModelException
    {
        if (arg instanceof BeanModel)
        {
            BeanModel beanModel = (BeanModel) arg;
            return ((Iterable) beanModel.getWrappedObject()).iterator().hasNext();
        }
        else if (arg instanceof SimpleSequence)
        {
            SimpleSequence simpleSequence = (SimpleSequence) arg;
            return (simpleSequence.toList().size() > 0);
        }
        else if (arg instanceof CollectionAndSequence) {
            CollectionAndSequence sequence = (CollectionAndSequence)arg;
            return (sequence.size() > 0);
        }
        else
        {
            throw new WindupException("Unrecognized type passed to: " + getMethodName() + ": "
                        + arg.getClass().getCanonicalName());
        }
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes an Iterable as a parameter and checks to see whether items exist in the Iterable.";
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        // no-op
    }

}
