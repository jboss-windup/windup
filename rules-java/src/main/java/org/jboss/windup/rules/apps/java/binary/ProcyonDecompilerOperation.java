package org.jboss.windup.rules.apps.java.binary;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.procyon.ProcyonConfiguration;
import org.jboss.windup.decompiler.procyon.ProcyonDecompiler;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileModelService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.model.WarArchiveModel;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Decompile the provided archive and place metadata regarding which files were decompiled into the graph.
 */
public class ProcyonDecompilerOperation extends AbstractIterationOperation<ArchiveModel>
{
    private static final String TECH_TAG = "Decompiled Java File";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.INFORMATIONAL;

    public ProcyonDecompilerOperation(String variableName)
    {
        super(variableName);
    }

    /**
     * Let the variable name to be set by the current Iteration.
     */
    public ProcyonDecompilerOperation()
    {
        super();
    }

    @Override
    public void perform(final GraphRewrite event, final EvaluationContext context, final ArchiveModel payload)
    {
        ExecutionStatistics.get().begin("ProcyonDecompilationOperation.perform");
        if (payload.getUnzippedDirectory() != null)
        {
            ProcyonDecompiler decompiler = new ProcyonDecompiler(new ProcyonConfiguration().setIncludeNested(false));
            int cores = Runtime.getRuntime().availableProcessors() / 2;
            if (cores < 1)
            {
                cores = 1;
            }
            // decompiler.setExecutorService(Executors.newFixedThreadPool(cores));
            String archivePath = ((FileModel) payload).getFilePath();
            File archive = new File(archivePath);
            File outputDir = new File(payload.getUnzippedDirectory().getFilePath());
            if (payload instanceof WarArchiveModel)
            {
                outputDir = outputDir.toPath().resolve("WEB-INF").resolve("classes").toFile();
            }

            try
            {
                DecompilationResult result = decompiler.decompileArchive(archive, outputDir);
                decompiler.close();
                Map<String, String> decompiledOutputFiles = result.getDecompiledFiles();

                FileModelService fileService = new FileModelService(event.getGraphContext());
                for (Map.Entry<String, String> decompiledEntry : decompiledOutputFiles.entrySet())
                {
                    // original source is a path inside the archive... split it up by separator, and append
                    // it to the unzipped directory
                    String[] classFilePathTokens = decompiledEntry.getKey().split("\\\\|/");

                    Path classFilePath = Paths.get(payload.getUnzippedDirectory().getFilePath());
                    for (String pathToken : classFilePathTokens)
                    {
                        classFilePath = classFilePath.resolve(pathToken);
                    }

                    String decompiledOutputFile = decompiledEntry.getValue();
                    FileModel decompiledFileModel = fileService.getUniqueByProperty(FileModel.FILE_PATH,
                                decompiledOutputFile);

                    if (decompiledFileModel == null)
                    {
                        FileModel parentFileModel = fileService.findByPath(Paths.get(decompiledOutputFile)
                                    .getParent()
                                    .toString());
                        decompiledFileModel = fileService.createByFilePath(parentFileModel, decompiledOutputFile);
                        decompiledFileModel.setParentArchive(payload);
                    }
                    ProjectModel projectModel = payload.getProjectModel();
                    decompiledFileModel.setProjectModel(projectModel);
                    projectModel.addFileModel(decompiledFileModel);

                    if (decompiledOutputFile.endsWith(".java"))
                    {
                        if (!(decompiledFileModel instanceof JavaSourceFileModel))
                        {
                            decompiledFileModel = GraphService.addTypeToModel(event.getGraphContext(),
                                        decompiledFileModel, JavaSourceFileModel.class);
                        }
                        JavaSourceFileModel decompiledSourceFileModel = (JavaSourceFileModel) decompiledFileModel;
                        TechnologyTagService techTagService = new TechnologyTagService(event.getGraphContext());
                        techTagService.addTagToFileModel(decompiledSourceFileModel, TECH_TAG, TECH_TAG_LEVEL);

                        FileModel classFileModel = fileService.getUniqueByProperty(
                                    FileModel.FILE_PATH, classFilePath.toAbsolutePath().toString());
                        if (classFileModel != null && classFileModel instanceof JavaClassFileModel)
                        {
                            JavaClassFileModel classModel = (JavaClassFileModel) classFileModel;
                            classModel.getJavaClass().setDecompiledSource(decompiledSourceFileModel);
                            decompiledSourceFileModel.setPackageName(classModel.getPackageName());
                        }
                        else
                        {
                            throw new WindupException(
                                        "Failed to find original JavaClassFileModel for decompiled Java file: "
                                                    + decompiledOutputFile + " at: " + classFilePath.toString());
                        }
                    }
                    payload.addDecompiledFileModel(decompiledFileModel);
                }
            }
            catch (final DecompilationException exc)
            {
                throw new WindupException("Error decompiling archive " + archivePath + " due to: " + exc.getMessage(),
                            exc);
            }
        }
        ExecutionStatistics.get().end("ProcyonDecompilationOperation.perform");
    }

    @Override
    public String toString()
    {
        return "DecompileWithProcyon";
    }
}