package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

public class JavaEE_EjbRules extends WindupRuleProvider
{
    @Override
    public void enhanceMetadata(Context context)
    {
        super.enhanceMetadata(context);
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder
        .begin()
        .addRule()
        .when(JavaClass.references("javax.persistence.Entity$").at(TypeReferenceLocation.TYPE))
        .perform(Classification.as("JPA Entity").withEffort(0))

        .addRule()
        .when(JavaClass.references("javax.ejb.EJBHome$").at(TypeReferenceLocation.INHERITANCE))
        .perform(Classification.as("EJB 1.x/2.x - Home Interface").withEffort(0))

        .addRule()
        .when(JavaClass.references("javax.ejb.EJBObject$").at(TypeReferenceLocation.INHERITANCE))
        .perform(Classification.as("EJB 1.x/2.x - Remote Interface").withEffort(0))

        .addRule()
        .when(JavaClass.references("javax.ejb.EntityBean$").at(TypeReferenceLocation.INHERITANCE))
        .perform(Classification.as("EJB 1.x/2.x - Entity Bean").withEffort(0) )

        .addRule()
        .when(JavaClass.references("javax.ejb.SessionBean$")
                    .at(TypeReferenceLocation.INHERITANCE))
        .perform(Classification.as("EJB 1.x/2.x - Session Bean").withEffort(0))

        .addRule()
        .when(JavaClass.references("javax.ejb.EJBLocalHome$").at(TypeReferenceLocation.INHERITANCE))
        .perform(Classification.as("EJB 2.x - Local Home").withEffort(0))

        .addRule()
        .when(JavaClass.references("javax.ejb.EJBLocalObject$").at(TypeReferenceLocation.INHERITANCE))
        .perform(Classification.as("EJB 2.x - Local Object").withEffort(0))

        .addRule()
        .when(JavaClass.references("javax.ejb.MessageDrivenBean$").at(TypeReferenceLocation.INHERITANCE))
        .perform(Classification.as("EJB 2.x - Message Driven Bean").withEffort(0))

        .addRule()
        .when(JavaClass.references("javax.ejb.MessageDriven$").at(TypeReferenceLocation.TYPE))
        .perform(Classification.as("EJB 3.x - Message Driven Bean").withEffort(2))

        .addRule()
        .when(JavaClass.references("javax.ejb.Local$").at(TypeReferenceLocation.TYPE))
        .perform(Classification.as("EJB 3.x - Local Session Bean Interface").withEffort(0))

        .addRule()
        .when(JavaClass.references("javax.ejb.Remote$").at(TypeReferenceLocation.TYPE))
        .perform(Classification.as("EJB 3.x - Remote Session Bean Interface").withEffort(2))

        .addRule()
        .when(JavaClass.references("javax.ejb.Stateless$").at(TypeReferenceLocation.TYPE))
        .perform(Classification.as("EJB 3.x - Stateless Session Bean").withEffort(0))

        .addRule()
        .when(JavaClass.references("javax.ejb.Stateful$").at(TypeReferenceLocation.TYPE))
        .perform(Classification.as("EJB 3.x - Stateful Session Bean").withEffort(0));
        return configuration;
    }
    // @formatter:on
}
