package org.jboss.windup.rules.apps.javaee.rules;


import org.jboss.forge.furnace.util.Strings;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationTypeCondition;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.service.SpringBeanService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.Set;

/**
 * Scans for classes with Spring bean related annotations, and adds Bean related metadata for these.
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = AnalyzeJavaFilesRuleProvider.class)
public class DiscoverSpringBeanMethodAnnotationsRuleProvider extends AbstractRuleProvider
{
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {

        String ruleIDPrefix = getClass().getSimpleName();
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(JavaClass.references("{*}({*})").at(TypeReferenceLocation.METHOD)
                            .annotationMatches(new AnnotationTypeCondition("org.springframework.context.annotation.Bean"))
                    )
                    .perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                    {
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                        {
                            extractAnnotationMetadata(event, payload);
                        }
                    })
                    .withId(ruleIDPrefix + "_SpringBeanMethodRule");
    }

    private String getAnnotationLiteralValue(JavaAnnotationTypeReferenceModel model, String name) {
        JavaAnnotationTypeValueModel valueModel = model.getAnnotationValues().get(name);

        if (valueModel instanceof JavaAnnotationLiteralTypeValueModel) {
            JavaAnnotationLiteralTypeValueModel literalTypeValue = (JavaAnnotationLiteralTypeValueModel) valueModel;
            return literalTypeValue.getLiteralValue();
        }
        else {
            return null;
        }
    }

    private void extractAnnotationMetadata(GraphRewrite event, JavaTypeReferenceModel javaTypeReference) {
        javaTypeReference.getFile().setGenerateSourceReport(true);

        //TODO : get the type returned by the method---> Bean Interface
        //TODO : with that interface we will seach in the next lines the first class implementing that interface

        JavaClassModel javaClass = new JavaClassService(event.getGraphContext())
                .getByName(javaTypeReference.getFile()
                    .getJavaClasses()
                    .stream()
                    .filter(JavaClassModel::isPublic)
                    .findAny()
                    .map(JavaClassModel::getQualifiedName)
                    .orElse("") //TODO : check this
                );

        String beanName = javaClass.getClassName();
        if (javaTypeReference.getAnnotations() != null && javaTypeReference.getAnnotations().size() > 0) {
            beanName = getAnnotationLiteralValue(javaTypeReference.getAnnotations().get(0), "name");
        }

        SpringBeanService sessionBeanService = new SpringBeanService(event.getGraphContext());
        SpringBeanModel springBeanModel = sessionBeanService.create();

        Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), javaTypeReference.getFile().getProjectModel());
        springBeanModel.setApplications(applications);
        springBeanModel.setSpringBeanName(beanName);
        springBeanModel.setJavaClass(javaClass);
    }

    @Override
    public String toString() {
        return "DiscoverSpringBeanAnnotatedClasses";
    }
}
