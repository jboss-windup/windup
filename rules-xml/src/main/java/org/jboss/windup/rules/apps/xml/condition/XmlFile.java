package org.jboss.windup.rules.apps.xml.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.parameters.FrameContext;
import org.jboss.windup.config.parameters.FrameCreationContext;
import org.jboss.windup.config.parameters.ParameterizedGraphCondition;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.FileReferenceModel;
import org.jboss.windup.rules.apps.xml.model.DoctypeMetaModel;
import org.jboss.windup.rules.apps.xml.model.NamespaceMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.model.XmlTypeReferenceModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.xml.LocationAwareContentHandler;
import org.jboss.windup.util.xml.NamespaceMapContext;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.Parameter;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;
import org.ocpsoft.rewrite.util.Maps;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles matching on {@link XmlFileModel} objects and creating {@link XmlTypeReferenceModel} objects on the matching nodes.
 */
public class XmlFile extends ParameterizedGraphCondition
{
    private static final Logger LOG = Logging.get(XmlFile.class);

    protected static final String UNPARSEABLE_XML_CLASSIFICATION = "Unparseable XML File";
    protected static final String UNPARSEABLE_XML_DESCRIPTION = "This file could not be parsed via XPath";

    protected static final String WINDUP_NS_PREFIX = "windup";
    protected static final String WINDUP_NS_URI = "http://windup.jboss.org/windupv2functions";

    private static XPathFactory factory = XPathFactory.newInstance();
    private final XmlFileFunctionResolver xmlFileFunctionResolver;
    private final XPath xpathEngine;

    private String xpathString;
    private XPathExpression compiledXPath;
    private Map<String, String> namespaces = new HashMap<>();
    private String fileName;
    private String publicId;
    private String xpathResultMatch;

    // just to extract required parameter names
    private RegexParameterizedPatternParser xpathPattern;

    public void setXpathResultMatch(String xpathResultMatch)
    {
        this.xpathResultMatch = xpathResultMatch;
    }

    private XmlFile(String xpath)
    {
        this();
        setXpath(xpath);
    }

    XmlFile()
    {
        this.namespaces.put(WINDUP_NS_PREFIX, WINDUP_NS_URI);

        this.xpathEngine = factory.newXPath();
        final XPathFunctionResolver originalResolver = this.xpathEngine.getXPathFunctionResolver();
        xmlFileFunctionResolver = new XmlFileFunctionResolver(originalResolver);
        this.xpathEngine.setXPathFunctionResolver(xmlFileFunctionResolver);
    }

    /**
     * Create a new {@link XmlFile} {@link Condition}.
     */
    public static XmlFile matchesXpath(String xpath)
    {
        return new XmlFile(xpath);
    }

    /**
     * Create a new {@link XmlFile} that matches on the provided DTD id regular expression.
     */
    public static XmlFile withDTDPublicId(String publicIdRegex)
    {
        XmlFile xmlFile = new XmlFile();
        xmlFile.publicId = publicIdRegex;
        return xmlFile;
    }

    /**
     * Output the results using the provided variable name.
     */
    public ConditionBuilder as(String variable)
    {
        Assert.notNull(variable, "Variable name must not be null.");
        this.setOutputVariablesName(variable);
        return this;
    }

    /**
     * Scan only files that match the given file name.
     */
    public XmlFile inFile(String fileName)
    {
        this.fileName = fileName;
        return this;
    }

    /**
     * Only return results that match the given regex.
     */
    public XmlFile resultMatches(String regex)
    {
        this.xpathResultMatch = regex;
        return this;
    }

    /**
     * Specify the name of the variables to base this query on.
     * 
     * @param fromVariable
     * @return
     */
    public static XmlFileFrom from(String fromVariable)
    {
        return new XmlFileFrom(fromVariable);
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
        if (this.xpathPattern != null)
        {
            this.xpathPattern.setParameterStore(store);
        }
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        if (this.xpathPattern == null)
        {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>(this.xpathPattern.getRequiredParameterNames());
        return result;
    }

    @Override
    protected String getVarname()
    {
        return getOutputVariablesName();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean evaluateAndPopulateValueStores(final GraphRewrite event, final EvaluationContext context,
                final FrameCreationContext frameCreationContext)
    {
        return evaluate(event, context, new EvaluationStrategy()
        {
            private LinkedHashMap<String, List<WindupVertexFrame>> variables;

            @Override
            @SuppressWarnings("rawtypes")
            public void modelMatched()
            {
                this.variables = new LinkedHashMap<String, List<WindupVertexFrame>>();
                frameCreationContext.beginNew((Map) variables);
            }

            @Override
            public void modelSubmitted(WindupVertexFrame model)
            {
                Maps.addListValue(this.variables, getVarname(), model);
            }

            @Override
            public boolean submitValue(Parameter<?> parameter, String value)
            {
                ParameterValueStore valueStore = DefaultParameterValueStore.getInstance(context);
                return valueStore.submit(event, context, parameter, value);
            }

            @Override
            public void modelSubmissionRejected()
            {
                frameCreationContext.rollback();
            }
        });
    }

    @Override
    protected boolean evaluateWithValueStore(final GraphRewrite event, final EvaluationContext context, final FrameContext frameContext)
    {
        boolean result = evaluate(event, context, new EvaluationStrategy()
        {
            @Override
            public void modelMatched()
            {
            }

            @Override
            public void modelSubmitted(WindupVertexFrame model)
            {
            }

            @Override
            public boolean submitValue(Parameter<?> parameter, String value)
            {
                ParameterValueStore valueStore = DefaultParameterValueStore.getInstance(context);
                String existingValue = valueStore.retrieve(parameter);
                if (existingValue == null)
                {
                    return valueStore.submit(event, context, parameter, value);
                }
                else
                {
                    return valueStore.isValid(event, context, parameter, value);
                }
            }

            @Override
            public void modelSubmissionRejected()
            {
            }
        });

        if (result == false)
            frameContext.reject();

        return result;
    }

    interface EvaluationStrategy
    {
        void modelMatched();

        void modelSubmitted(WindupVertexFrame model);

        boolean submitValue(Parameter<?> parameter, String value);

        void modelSubmissionRejected();
    }

    private boolean evaluate(final GraphRewrite event, final EvaluationContext context, final EvaluationStrategy evaluationStrategy)
    {
        ExecutionStatistics.get().begin("XmlFile.evaluate");
        // list will cache all the created xpath matches for this given condition running
        final List<WindupVertexFrame> resultLocations = new ArrayList<WindupVertexFrame>();
        final GraphContext graphContext = event.getGraphContext();
        GraphService<XmlFileModel> xmlResourceService = new GraphService<XmlFileModel>(graphContext,
                    XmlFileModel.class);
        Iterable<? extends WindupVertexFrame> allXmls;
        if (getInputVariablesName() == null || getInputVariablesName().equals(""))
        {
            allXmls = xmlResourceService.findAll();
        }
        else
        {
            allXmls = Variables.instance(event).findVariable(getInputVariablesName());
        }

        for (WindupVertexFrame iterated : allXmls)
        {
            final XmlFileModel xml;
            if (iterated instanceof FileReferenceModel)
            {
                xml = (XmlFileModel) ((FileReferenceModel) iterated).getFile();
            }
            else if (iterated instanceof XmlFileModel)
            {
                xml = (XmlFileModel) iterated;
            }
            else
            {
                throw new WindupException("XmlFile was called on the wrong graph type ( " + iterated.toPrettyString()
                            + ")");
            }

            if (fileName != null && !fileName.equals(""))
            {
                if (!xml.getFileName().matches(fileName))
                {
                    continue;
                }
            }
            if (publicId != null && !publicId.equals(""))
            {
                DoctypeMetaModel doctype = xml.getDoctype();
                if (doctype == null || doctype.getPublicId() == null
                            || !doctype.getPublicId().matches(publicId))
                {
                    continue;
                }
                else if (xpathString == null)
                {
                    // if the xpath is not set and therefore we have the result already
                    resultLocations.add(xml);
                }

            }
            if (xpathString != null)
            {
                String xpathStringWithParameterFunctions = XmlFileXPathTransformer.transformXPath(this.xpathString);
                LOG.fine("XmlFile compiled: " + this.xpathString + " to " + xpathStringWithParameterFunctions);

                XmlFileService xmlFileService = new XmlFileService(graphContext);
                Document document = xmlFileService.loadDocumentQuiet(xml);
                if (document != null)
                {
                    final ParameterStore store = DefaultParameterStore.getInstance(context);

                    final XmlFileParameterMatchCache paramMatchCache = new XmlFileParameterMatchCache();
                    this.xmlFileFunctionResolver.registerFunction(WINDUP_NS_URI, "startFrame", new XmlFileStartFrameXPathFunction(paramMatchCache));
                    this.xmlFileFunctionResolver.registerFunction(WINDUP_NS_URI, "evaluate", new XmlFileEvaluateXPathFunction(evaluationStrategy));
                    this.xmlFileFunctionResolver.registerFunction(WINDUP_NS_URI, "matches", new XmlFileMatchesXPathFunction(context, store,
                                paramMatchCache, event));
                    this.xmlFileFunctionResolver.registerFunction(WINDUP_NS_URI, "persist", new XmlFilePersistXPathFunction(graphContext, xml,
                                evaluationStrategy, store, paramMatchCache, resultLocations));
                }

                if (compiledXPath == null)
                {
                    NamespaceMapContext nsContext = new NamespaceMapContext(namespaces);
                    this.xpathEngine.setNamespaceContext(nsContext);
                    try
                    {
                        this.compiledXPath = xpathEngine.compile(xpathStringWithParameterFunctions);
                    }
                    catch (Exception e)
                    {
                        String message = e.getMessage();

                        // brutal hack to try to get a reasonable error message (ugly, but it seems to work)
                        if (message == null && e.getCause() != null && e.getCause().getMessage() != null)
                        {
                            message = e.getCause().getMessage();
                        }
                        LOG.severe("Condition: " + this + " failed to run, as the following xpath was uncompilable: " + xpathString
                                    + " (compiled contents: " + xpathStringWithParameterFunctions + ") due to: "
                                    + message);
                        return false;
                    }
                }

                NodeList result = XmlUtil.xpathNodeList(document, compiledXPath);
                if (result == null || result.getLength() == 0)
                {
                }
            }
        }
        Variables.instance(event).setVariable(getOutputVariablesName(), resultLocations);
        ExecutionStatistics.get().end("XmlFile.evaluate");
        return !resultLocations.isEmpty();
    }

    public XmlFile namespace(String prefix, String url)
    {
        namespaces.put(prefix, url);
        return this;
    }

    public void setXpath(String xpath)
    {
        this.xpathString = xpath;
        this.compiledXPath = null;

        if (xpath != null)
        {
            this.xpathPattern = new RegexParameterizedPatternParser(this.xpathString);
        }
    }

    public void setPublicId(String publicId)
    {
        this.publicId = publicId;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("XmlFile");
        if (getInputVariablesName() != null)
        {
            builder.append(".inputVariable(" + getInputVariablesName() + ")");
        }
        if (xpathString != null)
        {
            builder.append(".matches(" + xpathString + ")");
        }
        if (fileName != null)
        {
            builder.append(".inFile(" + fileName + ")");
        }
        if (publicId != null)
        {
            builder.append(".withDTDPublicId(" + publicId + ")");
        }
        builder.append(".as(" + getInputVariablesName() + ")");
        return builder.toString();
    }

    final class XmlFilePersistXPathFunction implements XPathFunction
    {
        private final GraphContext graphContext;
        private final XmlFileModel xml;
        private final EvaluationStrategy evaluationStrategy;
        private final ParameterStore store;
        private final XmlFileParameterMatchCache paramMatchCache;
        private final List<WindupVertexFrame> resultLocations;

        XmlFilePersistXPathFunction(GraphContext graphContext, XmlFileModel xml, EvaluationStrategy evaluationStrategy, ParameterStore store,
                    XmlFileParameterMatchCache paramMatchCache, List<WindupVertexFrame> resultLocations)
        {
            this.graphContext = graphContext;
            this.xml = xml;
            this.evaluationStrategy = evaluationStrategy;
            this.store = store;
            this.paramMatchCache = paramMatchCache;
            this.resultLocations = resultLocations;
        }

        @Override
        public Object evaluate(@SuppressWarnings("rawtypes") List args) throws XPathFunctionException
        {
            int frameIdx = ((Double) args.get(0)).intValue();
            NodeList arg1 = (NodeList) args.get(1);
            String nodeText = XmlUtil.nodeListToString(arg1);
            LOG.fine("persist(" + frameIdx + ", " + nodeText + ")");

            for (int i = 0; i < arg1.getLength(); i++)
            {
                Node node = arg1.item(i);
                if (xpathResultMatch != null)
                {
                    if (!node.toString().matches(xpathResultMatch))
                    {
                        continue;
                    }
                }
                // Everything passed for this Node. Start creating XmlTypeReferenceModel for it.
                int lineNumber = (int) node.getUserData(
                            LocationAwareContentHandler.LINE_NUMBER_KEY_NAME);
                int columnNumber = (int) node.getUserData(
                            LocationAwareContentHandler.COLUMN_NUMBER_KEY_NAME);

                GraphService<XmlTypeReferenceModel> fileLocationService = new GraphService<XmlTypeReferenceModel>(
                            graphContext,
                            XmlTypeReferenceModel.class);
                XmlTypeReferenceModel fileLocation = fileLocationService.create();
                String sourceSnippit = XmlUtil.nodeToString(node);
                fileLocation.setSourceSnippit(sourceSnippit);
                fileLocation.setLineNumber(lineNumber);
                fileLocation.setColumnNumber(columnNumber);
                fileLocation.setLength(node.toString().length());
                fileLocation.setFile(xml);
                fileLocation.setXpath(xpathString);
                GraphService<NamespaceMetaModel> metaModelService = new GraphService<NamespaceMetaModel>(
                            graphContext,
                            NamespaceMetaModel.class);
                for (Map.Entry<String, String> namespace : namespaces.entrySet())
                {
                    NamespaceMetaModel metaModel = metaModelService.create();
                    metaModel.setSchemaLocation(namespace.getKey());
                    metaModel.setSchemaLocation(namespace.getValue());
                    metaModel.addXmlResource(xml);
                    fileLocation.addNamespace(metaModel);
                }
                resultLocations.add(fileLocation);

                evaluationStrategy.modelMatched();
                for (Map.Entry<String, String> entry : paramMatchCache.getVariables(frameIdx).entrySet())
                {
                    Parameter<?> param = store.get(entry.getKey());
                    String value = entry.getValue();
                    if (!evaluationStrategy.submitValue(param, value))
                    {
                        return false;
                    }
                }

                evaluationStrategy.modelSubmitted(fileLocation);
                evaluationStrategy.modelMatched();
            }

            return true;
        }
    }
}
