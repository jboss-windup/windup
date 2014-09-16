package org.jboss.windup.rules.apps.xml.operation.xslt;

import org.jboss.windup.reporting.config.Classification;


/**
 * Intermediate step for constructing {@link XSLTTransformation} instances for a specified ref.
 * 
 * @author mbriskar
 */
public class XSLTTransformationOf
{
    private XSLTTransformation transformation;

    XSLTTransformationOf(String variable)
    {
        this.transformation = new XSLTTransformation(variable);
    }

    /**
     * Set the text of this {@link Classification}. E.g: "Unparsable XML file." or "Source File"
     */
    public XSLTTransformation using(String location)
    {
        this.transformation.setSourceLocation(location);
        return this.transformation;
    }
}
