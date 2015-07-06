package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerTemplateDirective;
import org.jboss.windup.reporting.model.association.LinkableModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.jboss.windup.util.Logging;

import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * Renders linkable elements as a list of links
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class RenderFileReferenceDirective implements WindupFreeMarkerTemplateDirective
{
    private static final Logger LOG = Logging.get(RenderFileReferenceDirective.class);
    private GraphContext context;
    private SourceReportService sourceReportService;
    private JavaClassService javaClassService;

    @Override
    public String getDescription()
    {
        return "Takes the following parameters: FileModel (a " + FileModel.class.getSimpleName() + ")";
    }

    @Override
    public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
                throws TemplateException, IOException
    {
        final Writer writer = env.getOut();
        StringModel stringModel = (StringModel) params.get("model");

        SimpleScalar defaultTextModel = (SimpleScalar) params.get("text");
        String defaultText = params.get("text") == null ? null : defaultTextModel.getAsString();

        if (stringModel == null || stringModel.getWrappedObject() == null)
        {
            if (StringUtils.isNotBlank(defaultText))
                writer.append(defaultText);
            else
            {
                writer.append("unknown");
                LOG.warning("Failed to resolve name or text for " + getClass().getName() + ". " + env);
            }
            return;
        }

        Object model = stringModel.getWrappedObject();

        LayoutType layoutType = resolveLayoutType(params);
        String cssClass = resolveCssClass(params);
        
        

        if (model instanceof FileLocationModel)
        {
            processFileLocationModel(writer, cssClass, (FileLocationModel) model, defaultText);
        }
        else if (model instanceof FileModel)
        {
            processFileModel(writer, cssClass, (FileModel) model, defaultText);
        }
        else if (model instanceof JavaClassModel)
        {
            processJavaClassModel(writer, layoutType, cssClass, (JavaClassModel) model, defaultText);
        }
        else if (model instanceof LinkableModel)
        {
            processLinkableModel(writer, layoutType, cssClass, (LinkableModel) model, defaultText);
        }
        else
        {
            throw new TemplateException("Object type not permitted: " + model.getClass().getSimpleName(), env);
        }
    }


	private String resolveCssClass(Map params) {
        SimpleScalar renderStyleModel = (SimpleScalar) params.get("class");
        if (renderStyleModel != null)
        {
            return renderStyleModel.getAsString();
        }
		return "";
	}

	private LayoutType resolveLayoutType(Map params) throws TemplateException {
		LayoutType layoutType = LayoutType.HORIZONTAL;
        SimpleScalar layoutModel = (SimpleScalar) params.get("layout");
        if (layoutModel != null)
        {
            String lt = layoutModel.getAsString();
            try
            {
                layoutType = LayoutType.valueOf(lt.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                throw new TemplateException("Layout: " + lt + " is not supported.", e, null);
            }
        }
		return layoutType;
	}

    private void processFileLocationModel(Writer writer, String cssClass, FileLocationModel obj, String defaultText) throws IOException
    {
        String position = " (" + obj.getLineNumber() + ", " + obj.getColumnNumber() + ")";
        String linkText = StringUtils.isBlank(defaultText) ? getPrettyPathForFile(obj.getFile()) + position : defaultText;
        String anchor = obj.asVertex().getId().toString();

        SourceReportModel result = sourceReportService.getSourceReportForFileModel(obj.getFile());
        if (result == null)
        {
            writer.write(linkText);
        }
        renderLink(writer, cssClass, result.getReportFilename() + "#" + anchor, linkText);
    }

    private void processLinkableModel(Writer writer, LayoutType layoutType, String cssClass, LinkableModel obj, String defaultText) throws IOException
    {
        List<Link> links = new LinkedList<>();
        for (LinkModel model : obj.getLinks())
        {
            Link l = new Link(model.getLink(), model.getDescription());
            links.add(l);
        }
        renderLinks(writer, layoutType, cssClass, links);
    }

    private void processFileModel(Writer writer, String cssClass, FileModel fileModel, String defaultText) throws IOException
    {
        String linkText = StringUtils.isBlank(defaultText) ? getPrettyPathForFile(fileModel) : defaultText;

        SourceReportModel result = sourceReportService.getSourceReportForFileModel(fileModel);
        if (result == null)
        {
            writer.write(linkText);
        }
        else
        {
            renderLink(writer, cssClass, result.getReportFilename(), linkText);
        }
    }

    private void processJavaClassModel(Writer writer, LayoutType layoutType, String cssClass, JavaClassModel clz, String defaultText) throws IOException
    {
        Iterator<JavaSourceFileModel> results = javaClassService.getJavaSource(clz.getQualifiedName()).iterator();

        int i = 0;
        if (results.hasNext())
        {
            while (results.hasNext())
            {
                if (i == 0)
                {
                    String linkText = StringUtils.isBlank(defaultText) ? clz.getQualifiedName() : defaultText;
                    JavaSourceFileModel source = results.next();

                    SourceReportModel result = sourceReportService.getSourceReportForFileModel(source);
                    if (result == null)
                    {
                        writer.write(linkText);
                    }
                    else
                    {
                        renderLink(writer, cssClass, result.getReportFilename(), linkText);
                    }

                }
                else
                {
                    JavaSourceFileModel source = results.next();
                    SourceReportModel result = sourceReportService.getSourceReportForFileModel(source);

                    if (result == null)
                    {
                        writer.write(" (" + (i + 1) + ")");
                    }
                    else
                    {
                        renderLink(writer, cssClass, result.getReportFilename(), " (" + (i + 1) + ")");
                    }

                }
                i++;
            }
        }
        else
        {
            writer.write(clz.getQualifiedName());
        }
    }

    private void renderLinks(Writer writer, LayoutType layoutType, String cssClass, Iterable<Link> linkIterable) throws IOException
    {
        Iterator<Link> links = linkIterable.iterator();
        if (layoutType == LayoutType.UL)
        {
            renderAsUL(writer, links);
        }
        if (layoutType == LayoutType.LI)
        {
            renderAsLI(writer, links);
        }
        else if (layoutType == LayoutType.DL)
        {
            renderAsDL(writer, links);
        }
        else if (layoutType == LayoutType.DT)
        {
            renderAsDT(writer, links);
        }
        else
        {
            renderAsHorizontal(writer, links);
        }
    }

    private void renderLink(Writer writer, String cssClass, String href, String linkText) throws IOException
    {
    	if(cssClass == null) {
    		cssClass = "";
    	}
    	
        writer.append("<a class='"+cssClass+"' href='" + href + "'>");
        writer.append(linkText);
        writer.append("</a>");
    }

    /*
     * Wraps with UL tags
     */
    private void renderAsUL(Writer writer, Iterator<Link> links) throws IOException
    {
        if (links.hasNext())
        {
            writer.append("<ul>");
            renderAsLI(writer, links);
            writer.append("</ul>");
        }
    }

    /*
     * Renders only LI tags
     */
    private void renderAsLI(Writer writer, Iterator<Link> links) throws IOException
    {
        if (links.hasNext())
        {
            while (links.hasNext())
            {
                Link link = links.next();
                writer.append("<li>");
                renderLink(writer, link);
                writer.append("</li>");
            }
        }
    }

    /*
     * Renders the full DL
     */
    private void renderAsDL(Writer writer, Iterator<Link> links) throws IOException
    {
        if (links.hasNext())
        {
            writer.append("<dl>");
            renderAsDT(writer, links);
            writer.append("</dl>");
        }
    }

    /*
     * Renders as DT elements
     */
    private void renderAsDT(Writer writer, Iterator<Link> links) throws IOException
    {
        if (links.hasNext())
        {
            while (links.hasNext())
            {
                Link link = links.next();
                writer.append("<dt>");
                writer.append(link.getDescription());
                writer.append("</dt>");
                writer.append("<dd>");
                writer.append("<a href='" + link.getLink() + "'>Link</a>");
                writer.append("</dd>");
            }
        }
    }

    private void renderAsHorizontal(Writer writer, Iterator<Link> links) throws IOException
    {
        while (links.hasNext())
        {
            Link link = links.next();
            renderLink(writer, link);

            if (links.hasNext())
            {
                writer.append(" | ");
            }
        }
    }

    private void renderLink(Writer writer, Link link) throws IOException
    {
        writer.append("<a href='" + link.getLink() + "'>");
        writer.append(link.getDescription());
        writer.append("</a>");
    }

    private String getPrettyPathForFile(FileModel fileModel)
    {
        if (fileModel instanceof JavaClassFileModel)
        {
            JavaClassFileModel jcfm = (JavaClassFileModel) fileModel;
            return jcfm.getJavaClass().getQualifiedName();
        }
        else if (fileModel instanceof ReportResourceFileModel)
        {
            return "resources/" + fileModel.getPrettyPath();
        }
        else if (fileModel instanceof JavaSourceFileModel)
        {
            JavaSourceFileModel javaSourceModel = (JavaSourceFileModel) fileModel;
            String filename = fileModel.getFileName();
            String packageName = javaSourceModel.getPackageName();

            if (filename.endsWith(".java"))
            {
                filename = filename.substring(0, filename.length() - 5);
            }

            return packageName == null || packageName.equals("") ? filename : packageName + "." + filename;
        }
        else
        {
            return fileModel.getPrettyPathWithinProject();
        }
    }

    @Override
    public String getDirectiveName()
    {
        return "render_link";
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
        this.sourceReportService = new SourceReportService(this.context);
        this.javaClassService = new JavaClassService(this.context);
    }

    private static enum LayoutType
    {
        HORIZONTAL, UL, DL, LI, DT
    }

	public enum RenderStyleType {
		DEFAULT, LABEL
	}


    private static class Link
    {
        private final String link;
        private final String description;

        public Link(String link, String description)
        {
            this.link = link;
            this.description = description;
        }

        public String getLink()
        {
            return link;
        }

        public String getDescription()
        {
            return description;
        }
    }
}
