package org.jboss.windup.config.query;

import com.thinkaurelius.titan.graphdb.query.TitanPredicate;

/**
 * Handles cases of multiple values and also properly deals with enumeration values in the Iterable.
 *
 */
public final class MultipleValueTitanPredicate implements TitanPredicate
{
    @Override
    public boolean evaluate(Object first, Object second)
    {
        if (second instanceof Iterable<?>)
        {
            boolean found = false;
            ITERABLE: for (Object element : (Iterable<?>) second)
            {
                if (element instanceof Enum && ((Enum<?>) element).name().equals(first))
                {
                    found = true;
                    break ITERABLE;
                }
                if (first.equals(element))
                {
                    found = true;
                    break ITERABLE;
                }
            }
            return found;
        }
        return false;
    }

    @Override
    public boolean isValidCondition(Object condition)
    {
        return condition != null && condition instanceof Iterable<?>;
    }

    @Override
    public boolean isValidValueType(Class<?> clazz)
    {
        return Iterable.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean hasNegation()
    {
        return false;
    }

    @Override
    public TitanPredicate negate()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isQNF()
    {
        return true;
    }
}