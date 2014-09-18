package org.jboss.windup.rules.apps.xml.legacy;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 * 
 */
public class XmlSpringConfig extends WindupRuleProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.MIGRATION_RULES;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        context.put(RuleMetadata.CATEGORY, "Xml");
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(XmlFile.matchesXpath("/spring:flow").namespace("spring",
                                "http://www.springframework.org/schema/webflow"))
                    .perform(Classification.as("Spring Web Flow Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/spring:beans | /beans").namespace("spring",
                                "http://www.springframework.org/schema/beans"))
                    .perform(Classification.as("Spring Configuration")
                                .and(Iteration.over()
                                            .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//sca:reference/@type") .namespace( "sca", "http://www.springframework.org/schema/sca" ) )
                                            .perform(Hint. withText("SCA Implementation") .withEffort(1) ).endIteration()
                                            )
                                            
                                .and(Iteration.over()
                                            .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//tibco:service-metadata/@wsdl-location") .namespace( "tibco", "http://xsd.tns.tibco.com/2008/amf/extension/spring" ) )
                                            .perform(Hint. withText("Tibco WSDL Location") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                   .and(Iteration.over()
                                               .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.jndi.JndiObjectFactoryBean']/@class") )
                                               .perform(Hint. withText("JNDI Lookup") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                   .and(Iteration.over()
                                               .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.instrument.classloading.glassfish.ClassTransformerAdapter']/@class") )
                                               .perform(Hint. withText("Glassfish Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                   .and(Iteration.over()
                                               .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.instrument.classloading.glassfish.GlassFishClassLoaderAdapter']/@class") )
                                               .perform(Hint. withText("Glassfish Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                   .and(Iteration.over()
                                               .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.instrument.classloading.glassfish.GlassFishLoadTimeWeaver']/@class") )
                                               .perform(Hint. withText("Glassfish Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                   .and(Iteration.over()
                                               .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.jca.work.glassfish.GlassFishWorkManagerTaskExecutor']/@class") )
                                               .perform(Hint. withText("Glassfish Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                   .and(Iteration.over()
                                               .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.instrument.classloading.jboss.JBossClassLoaderAdapter']/@class") )
                                               .perform(Hint. withText("JBoss Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                   .and(Iteration.over()
                                               .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.instrument.classloading.jboss.JBossLoadTimeWeaver']/@class") )
                                               .perform(Hint. withText("JBoss Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                   .and(Iteration.over()
                                               .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.instrument.classloading.jboss.JBossTranslatorAdapter']/@class") )
                                               .perform(Hint. withText("JBoss Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                  
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.jca.work.jboss.JBossWorkManagerTaskExecutor']/@class") )
                                              .perform(Hint. withText("JBoss Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.jca.work.jboss.JBossWorkManagerUtils']/@class") )
                                              .perform(Hint. withText("JBoss Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.jdbc.support.nativejdbc.JBossNativeJdbcExtractor']/@class") )
                                              .perform(Hint. withText("JBoss Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.instrument.classloading.tomcat.TomcatInstrumentableClassLoader']/@class") )
                                              .perform(Hint. withText("Tomcat Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.scheduling.commonj.WorkManagerTaskExecutor']/@class") )
                                              .perform(Hint. withText("Weblogic and Websphere Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.instrument.classloading.weblogic.WebLogicClassLoaderAdapter']/@class") )
                                              .perform(Hint. withText("Weblogic Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.instrument.classloading.weblogic.WebLogicClassPreProcessorAdapter']/@class") )
                                              .perform(Hint. withText("Weblogic Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  ).and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.instrument.classloading.weblogic.WebLogicLoadTimeWeaver']/@class") )
                                              .perform(Hint. withText("Weblogic Specific") .withEffort(1) ) 
                                              .endIteration()     
                                    )
                                    .and(Iteration.over()
                                                .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.jdbc.support.nativejdbc.WebLogicNativeJdbcExtractor']/@class") )
                                                .perform(Hint. withText("Weblogic Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.transaction.jta.WebLogicJtaTransactionManager']/@class") )
                                              .perform(Hint. withText("Weblogic Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.jdbc.datasource.WebSphereDataSourceAdapter']/@class") )
                                              .perform(Hint. withText("Websphere Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.jdbc.support.nativejdbc.WebSphereNativeJdbcExtractor']/@class") )
                                              .perform(Hint. withText("Websphere Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.jmx.support.WebSphereMBeanServerFactoryBean']/@class") )
                                              .perform(Hint. withText("Websphere Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.transaction.jta.WebSphereUowTransactionManager']/@class") )
                                              .perform(Hint. withText("Websphere Specific") .withEffort(1) ) 
                                            .endIteration()     
                                  ).and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.springframework.transaction.jta.WebSphereUowTransactionManager$UOWActionAdapter']/@class") )
                                              .perform(Hint. withText("Websphere Specific") .withEffort(1) ) 
                                              .endIteration()     
                                    )
                                    .and(Iteration.over()
                                                .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.apache.activemq.ActiveMQConnectionFactory']/@class") )
                                                .perform(Hint. withText("Apache ActiveMQ Specific") ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.apache.activemq.pool.PooledConnectionFactory']/@class") )
                                              .perform(Hint. withText("Apache ActiveMQ Specific") ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='org.apache.activemq.command.ActiveMQQueue']/@class") )
                                              .perform(Hint. withText("Apache ActiveMQ Specific") ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//*[@class='com.ibm.mq.jms.MQQueueConnectionFactory']/@class") )
                                              .perform(Hint. withText("IBM MQ Specific") ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//property[@value='com.tibco.tibjms.naming.TibjmsInitialContextFactory']/@value") )
                                              .perform(Hint. withText("Tibco EMS Specific") ) 
                                            .endIteration()     
                                  )
                                  .and(Iteration.over()
                                              .when( XmlFile.from(Iteration.DEFAULT_SINGLE_VARIABLE_STRING) .matchesXpath("//jee:jndi-lookup/@jndi-name") .namespace( "jee", "http://www.springframework.org/schema/jee" ) )
                                              .perform(Hint. withText("Validate JNDI Lookup") .withEffort(1) ) 
                                            .endIteration()     
                                  )
                                );
        return configuration;
    }
    // @formatter:on
}