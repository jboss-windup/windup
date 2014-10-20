package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(FileLocationModel.TYPE)
public interface FileLocationModel extends FileReferenceModel
{

    String TYPE = "fileLocationModel";
    String LINE_NUMBER = "lineNumber";
    String LENGTH = "length";
    String COLUMN_NUMBER = "startPosition";
    String RULE_ID = "ruleID";

    /**
     * Set the line number at which this {@link InlineHintModel} should appear in the designated {@link FileModel}.
     */
    @Property(LINE_NUMBER)
    public void setLineNumber(int lineNumber);

    /**
     * Get the line number at which this {@link InlineHintModel} should appear in the designated {@link FileModel}.
     */
    @Property(LINE_NUMBER)
    public int getLineNumber();

    /**
     * Set the column number at which this {@link InlineHintModel} should appear in the designated {@link FileModel}.
     */
    @Property(COLUMN_NUMBER)
    public void setColumnNumber(int startPosition);

    /**
     * Get the column number at which this {@link InlineHintModel} should appear in the designated {@link FileModel}.
     */
    @Property(COLUMN_NUMBER)
    public int getColumnNumber();

    /**
     * Set the length of content for which this {@link InlineHintModel} should cover in the designated {@link FileModel}
     * .
     */
    @Property(LENGTH)
    public void setLength(int length);

    /**
     * Get the length of content for which this {@link InlineHintModel} should cover in the designated {@link FileModel}
     * .
     */
    @Property(LENGTH)
    public int getLength();

    /**
     * Set the ID of the rule that triggered this particular blacklist entry
     */
    @Property(RULE_ID)
    public void setRuleID(String ruleID);

    /**
     * Get the ID of the rule that triggered this particular blacklist entry
     */
    @Property(RULE_ID)
    public String getRuleID();
}
