package org.jboss.windup.rules.apps.java.decompiler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.DependencyVisitor;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.ignore.JavaClassIgnoreResolver;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.objectweb.asm.ClassReader;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.google.common.collect.Iterables;

/**
 * An operation doing a pre-scan of the .class file in order to check if it is possible to tell in advance if it is worth decompiling the class.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 * @author Ondrej Zizka
 */
public class ClassFilePreDecompilationScan extends AbstractIterationOperation<JavaClassFileModel>
{
    private static final Logger LOG = Logging.get(ClassFilePreDecompilationScan.class);


    private void addClassFileMetadata(GraphRewrite event, EvaluationContext context, JavaClassFileModel originalJavaClassFileModel)
    {
        Iterable<FileModel> allIncludingDupes = Iterables.concat(originalJavaClassFileModel.getDuplicates(),
                    Collections.singleton(originalJavaClassFileModel));
        try (FileInputStream fis = new FileInputStream(originalJavaClassFileModel.getFilePath()))
        {

            final ClassParser parser = new ClassParser(fis, originalJavaClassFileModel.getFilePath());
            final JavaClass bcelJavaClass = parser.parse();

            for (FileModel fileModel : allIncludingDupes)
            {
                JavaClassFileModel javaClassFileModel = (JavaClassFileModel) fileModel;

                final String packageName = bcelJavaClass.getPackageName();

                final String qualifiedName = bcelJavaClass.getClassName();

                final JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
                final JavaClassModel javaClassModel = javaClassService.create(qualifiedName);
                int majorVersion = bcelJavaClass.getMajor();
                int minorVersion = bcelJavaClass.getMinor();

                String simpleName = qualifiedName;
                if (packageName != null && !packageName.isEmpty() && simpleName != null)
                {
                    simpleName = StringUtils.substringAfterLast(simpleName, ".");
                }

                javaClassModel.setSimpleName(simpleName);
                javaClassModel.setPackageName(packageName);
                javaClassModel.setQualifiedName(qualifiedName);
                javaClassModel.setClassFile(javaClassFileModel);
                javaClassModel.setPublic(bcelJavaClass.isPublic());
                javaClassModel.setInterface(bcelJavaClass.isInterface());

                final String[] interfaceNames = bcelJavaClass.getInterfaceNames();
                if (interfaceNames != null)
                {
                    for (final String interfaceName : interfaceNames)
                    {
                        JavaClassModel interfaceModel = javaClassService.getOrCreatePhantom(interfaceName);
                        javaClassService.addInterface(javaClassModel, interfaceModel);
                    }
                }

                String superclassName = bcelJavaClass.getSuperclassName();
                if (!bcelJavaClass.isInterface() && !StringUtils.isBlank(superclassName))
                    javaClassModel.setExtends(javaClassService.getOrCreatePhantom(superclassName));

                javaClassFileModel.setMajorVersion(majorVersion);
                javaClassFileModel.setMinorVersion(minorVersion);
                javaClassFileModel.setPackageName(packageName);
                javaClassFileModel.setJavaClass(javaClassModel);
            }
        }
        catch (Exception e)
        {
            for (FileModel fileModel : allIncludingDupes)
            {
                JavaClassFileModel javaClassFileModel = (JavaClassFileModel) fileModel;
                final String message = "BCEL was unable to parse class file '" + javaClassFileModel.getFilePath() + "':\n\t" + e.getMessage();
                LOG.log(Level.WARNING, message, e);
                ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                classificationService.attachClassification(context, javaClassFileModel, JavaClassFileModel.UNPARSEABLE_CLASS_CLASSIFICATION,
                        JavaClassFileModel.UNPARSEABLE_CLASS_DESCRIPTION);
                javaClassFileModel.setParseError(message);
                javaClassFileModel.setSkipDecompilation(true);
            }
        }
    }


    private void filterClassesToDecompile(GraphRewrite event, EvaluationContext context, JavaClassFileModel originalFileModel)
    {
        Iterable<FileModel> allIncludingDupes = Iterables.concat(originalFileModel.getDuplicates(), Collections.singleton(originalFileModel));
        WindupJavaConfigurationService configurationService = new WindupJavaConfigurationService(event.getGraphContext());
        if (!configurationService.shouldScanFile(originalFileModel.getFilePath()))
        {
            for (FileModel fileModel : allIncludingDupes)
                fileModel.asVertex().setProperty(JavaClassFileModel.SKIP_DECOMPILATION, true);
            return;
        }

        // keep inner classes (we may need them for decompilation purposes)
        if (originalFileModel.getFileName().contains("$"))
            return;

        try (InputStream is = originalFileModel.asInputStream())
        {
            DependencyVisitor dependencyVisitor = new DependencyVisitor();
            ClassReader classReader = new ClassReader(is);
            classReader.accept(dependencyVisitor, 0);

            // If we should ignore any of the contained classes, skip decompilation of the whole file.
            for (String typeReference : dependencyVisitor.classes)
            {
                if (shouldIgnore(typeReference)) {
                    for (FileModel fileModel : allIncludingDupes)
                        fileModel.asVertex().setProperty(JavaClassFileModel.SKIP_DECOMPILATION, true);
                    break;
                }
            }
        }
        catch (IOException|IllegalArgumentException e)
        {
            for (FileModel fileModel : allIncludingDupes)
            {
            final String message = "ASM was unable to parse class file '" + fileModel.getFilePath() + "':\n\t" + e.getMessage();
            LOG.log(Level.WARNING, message, e);
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            classificationService.attachClassification(context, fileModel, JavaClassFileModel.UNPARSEABLE_CLASS_CLASSIFICATION,
                        JavaClassFileModel.UNPARSEABLE_CLASS_DESCRIPTION);
            fileModel.setParseError(message);
            }
        }
    }


    @Override
    public void perform(GraphRewrite event, EvaluationContext context, JavaClassFileModel fileModel)
    {
        ExecutionStatistics.get().begin("ClassFilePreDecompilationScan.perform()");
        try
        {
            addClassFileMetadata(event, context, fileModel);
            if (fileModel.getParseError() != null)
                return;

            filterClassesToDecompile(event, context, fileModel);
        }
        finally
        {
            ExecutionStatistics.get().end("ClassFilePreDecompilationScan.perform()");
        }
    }

    /**
     * This method is called on every reference that is in the .class file.
     * @param typeReference
     * @return
     */
    private boolean shouldIgnore(String typeReference)
    {
        typeReference = typeReference.replace('/', '.').replace('\\', '.');
        return JavaClassIgnoreResolver.singletonInstance().matches(typeReference);
    }


    @Override
    public String toString()
    {
        return ClassFilePreDecompilationScan.class.getSimpleName();
    }
}
