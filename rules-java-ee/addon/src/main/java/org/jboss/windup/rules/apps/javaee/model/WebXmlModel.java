package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.syncleus.ferma.annotations.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents the data from a Java EE web.xml file.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(WebXmlModel.TYPE)
public interface WebXmlModel extends XmlFileModel
{
    public static final String TYPE = "WebXmlModel";
    public static final String DISPLAY_NAME = "displayName";
    public static final String SPECIFICATION_VERSION = "specificationVersion";
    public static final String WEB_XML_TO_ENVIRONMENT_REFERENCE = "webXmlToEnvironmentReference";
    public static final String WEB_XML_TO_RESOURCE_REFERENCE = "webXmlToResourceReference";

    /**
     * Gets the EE Specification version specified by this web.xml file
     */
    @Property(SPECIFICATION_VERSION)
    public String getSpecificationVersion();

    /**
     * Sets the EE Specification version specified by this web.xml file
     */
    @Property(SPECIFICATION_VERSION)
    public void setSpecificationVersion(String version);

    /**
     * Gets the web.xml display-name property
     */
    @Property(DISPLAY_NAME)
    public String getDisplayName();

    /**
     * Gets the web.xml display-name property
     */
    @Property(DISPLAY_NAME)
    public void setDisplayName(String displayName);


    /**
     * Maintains a list of {@link EnvironmentReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = WEB_XML_TO_ENVIRONMENT_REFERENCE, direction = Direction.OUT)
    public Iterable<EnvironmentReferenceModel> getEnvironmentReferences();

    /**
     * Maintains a list of {@link EnvironmentReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = WEB_XML_TO_ENVIRONMENT_REFERENCE, direction = Direction.OUT)
    public void addEnvironmentReference(EnvironmentReferenceModel environmentReference);

    /**
     * Maintains a list of {@link WebXmlResourceReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = WEB_XML_TO_RESOURCE_REFERENCE, direction = Direction.OUT)
    public Iterable<WebXmlResourceReferenceModel> getResourceReferences();

    /**
     * Maintains a list of {@link WebXmlResourceReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = WEB_XML_TO_RESOURCE_REFERENCE, direction = Direction.OUT)
    public void addEnvironmentReference(WebXmlResourceReferenceModel environmentReference);

}
