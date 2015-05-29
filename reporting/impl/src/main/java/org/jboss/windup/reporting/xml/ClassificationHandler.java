package org.jboss.windup.reporting.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Adds the provided {@link Classification} operation to the currently selected items.
 * 
 * Expected format:
 * 
 * <pre>
 * &lt;classification; classification="classification text" description="description of classification" effort="8"&gt;
 *  &lt;link href="http://www.foo.com/" description="Helpful text" /&gt;
 * &lt;/classification&gt;
 * </pre>
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = "classification", namespace = "http://windup.jboss.org/v1/xml")
public class ClassificationHandler implements ElementHandler<Classification>
{

    @Override
    public Classification processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String classificationStr = $(element).attr("title");
        if (StringUtils.isBlank(classificationStr))
        {
            throw new WindupException(
                        "Error, 'classification' element must have a non-empty 'title' attribute (eg, 'Mule ESB Transformer')");
        }
        String description = $(element).attr("description");
        String of = $(element).attr("of");
        String effortStr = $(element).attr("effort");
        String severityStr = $(element).attr("severity");

        Classification classification = (Classification) Classification.as(classificationStr);
        if (of != null)
        {
            classification.setVariableName(of);
        }
        if (StringUtils.isNotBlank(effortStr))
        {
            try
            {
                int effort = Integer.parseInt(effortStr);
                classification.withEffort(effort);
            }
            catch (NumberFormatException e)
            {
                throw new WindupException("Could not parse effort level: " + effortStr + " as an integer!");
            }
        }

        if (StringUtils.isNotBlank(severityStr))
        {
            Severity severity = Severity.valueOf(severityStr);
            classification.withSeverity(severity);
        }

        if (StringUtils.isNotBlank(description))
        {
            classification.withDescription(description);
        }

        List<Element> children = $(element).children("link").get();
        for (Element child : children)
        {
            Link link = handlerManager.processElement(child);
            classification.with(link);
        }
        return classification;
    }
}
