package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.furnace.FurnaceHolder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.rules.files.DefaultFileDiscoveredEvent;
import org.jboss.windup.rules.files.FileDiscoveredListener;
import org.jboss.windup.rules.files.FileDiscoveredListenerUtil;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class RecurseDirectoryAndAddFiles extends AbstractIterationOperation<FileModel>
{
    private RecurseDirectoryAndAddFiles(String variableName)
    {
        super(variableName);
    }

    /**
     * Let the variable name to be set by the current Iteration.
     */
    public RecurseDirectoryAndAddFiles()
    {
        super();
    }

    public static RecurseDirectoryAndAddFiles startingAt(String variableName)
    {
        return new RecurseDirectoryAndAddFiles(variableName);
    }

    @Override
    public String toString()
    {
        return "RecurseDirectoryAndAddFiles";
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel resourceModel)
    {
        Iterable<FileDiscoveredListener> listeners = FurnaceHolder.getFurnace().getAddonRegistry().getServices(FileDiscoveredListener.class);

        FileService fileModelService = new FileService(event.getGraphContext());
        recurseAndAddFiles(listeners, event, fileModelService, resourceModel);
    }

    /**
     * Recurses the given folder and adds references to these files to the graph as FileModels
     */
    private void recurseAndAddFiles(Iterable<FileDiscoveredListener> listeners, GraphRewrite event, FileService fileService, FileModel file)
    {
        String filePath = file.getFilePath();
        File fileReference = new File(filePath);

        if (fileReference.isDirectory())
        {
            File[] subFiles = fileReference.listFiles();
            if (subFiles != null)
            {
                for (File reference : subFiles)
                {
                    if (FileDiscoveredListenerUtil.shouldSkip(listeners, new DefaultFileDiscoveredEvent(reference.getAbsolutePath())))
                        continue;

                    FileModel subFile = fileService.createByFilePath(file, reference.getAbsolutePath());
                    for (FileDiscoveredListener listener : listeners)
                        listener.fileModelCreated(event.getGraphContext(), subFile);
                    recurseAndAddFiles(listeners, event, fileService, subFile);
                }
            }
        }
    }
}
