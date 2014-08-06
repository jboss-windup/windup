package org.jboss.windup.reporting.util;


import com.tinkerpop.frames.Property;
import java.lang.reflect.Method;
import javax.inject.Singleton;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.jboss.windup.config.Variables;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.meta.ReportCommons;
import org.jboss.windup.reporting.meta.ann.Description;
import org.jboss.windup.reporting.meta.ann.ReportElement;
import org.jboss.windup.reporting.meta.ann.Title;
import org.jboss.windup.utils.el.IExprLangEvaluator;
import org.jboss.windup.utils.el.JuelCustomResolverEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts the report commons meta info from the given Model.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Singleton
public class ReportCommonsExtractor {
    private static final Logger log = LoggerFactory.getLogger( ReportCommonsExtractor.class );
    
    private Variables varStack;


    public ReportCommonsExtractor( Variables varStack ) {
        this.varStack = varStack;
    }
    

    /**
     * Extracts the static metadata from the model class - no EL evaluation.
     */
    public static ReportCommons extract( Class<? extends WindupVertexFrame> modelClass ){
        ReportCommons ri = new ReportCommons();
        return extract( modelClass, ri );
    }
        
    private static ReportCommons extract( Class<? extends WindupVertexFrame> modelClass, ReportCommons rc ){

        // Title
        if( rc.getTitle() == null ){
            Title annTitle = modelClass.getAnnotation( Title.class );
            if( annTitle != null )
                rc.setTitle( annTitle.value() );
        }
        
        // Description
        if( rc.getDesc() == null ){
            Description annDesc = modelClass.getAnnotation( Description.class );
            if( annDesc != null )
                rc.setDesc( annDesc.value() );
        }
        
        // Icon
        Description annIcon = modelClass.getAnnotation( Description.class );
        if( rc.getIcon() == null ){
            if( annIcon != null )
                rc.setIcon( annIcon.value() );
        }
        
        // Traits
        Method[] methods = modelClass.getMethods();
        for( Method method : methods ) {
            
            // formatter:off Only consider getters with @Property returning String, Number, or primitive.
            if( method.getAnnotation( Property.class ) == null )
                continue;
            if( ! method.getName().startsWith("get") )
                continue;
            final Class<?> retType = method.getReturnType();
            if( ! (  String.class.isAssignableFrom(retType)
                  || Number.class.isAssignableFrom(retType)
                  || retType.isPrimitive()
                //ClassUtils.isPrimitiveOrWrapper()
                //BeanUtils.isSimpleValueType()
            ))
                continue;
            
            String propName = StringUtils.uncapitalize( method.getName().substring(3) );
            
            // Only consider those with either @ReportElement or @Title.
            Title annTitle = method.getAnnotation( Title.class );
            ReportElement annRE = method.getAnnotation( ReportElement.class );
            if( annRE == null && annTitle == null )
                continue;
            
            if( annTitle != null ){
                rc.putTrait( propName, new ReportCommons( annTitle.value() ) );
            }
            
            ReportElement.Type type = annRE.type();
            // formatter:on
        }
        
        // Superclass
        if( modelClass.getSuperclass().isAssignableFrom( WindupVertexFrame.class ) )
            extract( (Class<? extends WindupVertexFrame>) modelClass.getSuperclass(), rc );
            
        return rc;
    }


    /**
     * Extracts the metadata from the model class and evaluates them as EL,
     * with a resolver looking in VarStack, and "this" pointing to the frame.
     */
    public ReportCommons extract( WindupVertexFrame frame )
    {
        ReportCommons ri = this.extract( frame.getClass() );
        return this.extract( frame, ri );
    }
        
    public ReportCommons extract( WindupVertexFrame frame, ReportCommons ri ){
        ReportCommons ri2 = new ReportCommons();
        
        // Title
        if( ri.getTitle() != null ){
            ri2.setTitle( evaluateEL(ri.getTitle(), frame) );
        }
        
        // Description
        if( ri.getDesc() != null ){
            ri2.setDesc( evaluateEL(ri.getDesc(), frame) );
        }
        
        // Icon
        if( ri.getIcon() != null ){
            ri.setIcon( evaluateEL( ri.getIcon(), frame ) );
        }
        
        return ri;
    }


    /**
     *  Evaluates EL expressions like "Datasource '${this.name} to ${this.server}".
     */
    private String evaluateEL( String el, WindupVertexFrame frame ) {
        ReportVariablesProvider prov = new ReportVariablesProvider(frame, this.varStack);
        return new JuelCustomResolverEvaluator(prov).evaluateEL( el );
    }

    

    /**
     *  Resolves EL variables, which is the first token in ${...} in EL expressions.
     */
    static class ReportVariablesProvider implements IExprLangEvaluator.IVariablesProvider
    {
        WindupVertexFrame frame;
        Variables varStack;
        // TODO: Some MigrationContext?

        public ReportVariablesProvider( WindupVertexFrame frame, Variables varStack ) {
            this.frame = frame;
            this.varStack = varStack;
        }

        public Object getVariable( String name ) {
            if("this".equals(name))
                return this.frame;
            
            // Not sure if we need the var stack in the context of resolving a model. Probably not.
            //Iterable<WindupVertexFrame> frames = this.varStack.findVariable(name);
            //if( frames != null )
            //    return frames;
            
            return null;
        }

    }

}// class
