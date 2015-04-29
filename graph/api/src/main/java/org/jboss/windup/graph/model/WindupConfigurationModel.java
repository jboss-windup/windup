package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains the global configuration for the current Windup execution.
 */
@TypeValue(WindupConfigurationModel.TYPE)
public interface WindupConfigurationModel extends WindupVertexFrame
{
    public static final String INPUT_PATH = "inputPath";

    public static final String TYPE = "BaseWindupConfiguration";

    public static final String USER_RULES_PATH = "userRulesPath";
    public static final String USER_IGNORE_PATH = "userIgnorePath";
    public static final String OFFLINE_MODE = "fetchRemoteResources";

    /**
     * The input path to scan
     */
    @Adjacency(label = INPUT_PATH, direction = Direction.OUT)
    FileModel getInputPath();

    /**
     * The input path to scan
     */
    @Adjacency(label = INPUT_PATH, direction = Direction.OUT)
    void setInputPath(FileModel inputPath);

    /**
     * The location for user provided rules directories (typically Groovy or XML Rules)
     */
    @Adjacency(label = USER_RULES_PATH, direction = Direction.OUT)
    void addUserRulesPath(ResourceModel userRulesPath);
    
    /**
     * The location for user provided ignore directory (list of ignored jar files)
     */
    @Adjacency(label = USER_IGNORE_PATH, direction = Direction.OUT)
    void addUserIgnorePath(ResourceModel userIgnorePath);

    /**
     * The location for user provided rules directories (typically Groovy or XML Rules)
     */
    @Adjacency(label = USER_RULES_PATH, direction = Direction.OUT)
    Iterable<ResourceModel> getUserRulesPaths();
    
    /**
     * The location for user provided rules directories (typically Groovy or XML Rules)
     */
    @Adjacency(label = USER_IGNORE_PATH, direction = Direction.OUT)
    Iterable<ResourceModel> getUserIgnorePaths();

    /**
     * Where to put the report and other files produced during Windup execution.
     */
    @Adjacency(label = "outputPath", direction = Direction.OUT)
    FileModel getOutputPath();

    /**
     * Where to put the report and other files produced during Windup execution.
     */
    @Adjacency(label = "outputPath", direction = Direction.OUT)
    void setOutputPath(ResourceModel outputPath);

    /**
     * Indicates whether or not to function in offline mode
     */
    @Property(OFFLINE_MODE)
    boolean isOfflineMode();

    @Property(OFFLINE_MODE)
    void setOfflineMode(boolean offline);
}
