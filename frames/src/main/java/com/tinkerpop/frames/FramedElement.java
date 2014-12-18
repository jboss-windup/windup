package com.tinkerpop.frames;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.frames.annotations.AnnotationHandler;
import com.tinkerpop.frames.modules.MethodHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

/**
 * The proxy class of a framed element.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FramedElement implements InvocationHandler {

    private final Direction direction;
    protected final FramedGraph framedGraph;
    private final Object elementID;
    
    protected Element element;
    
    private final boolean isVertex;
    private static Method hashCodeMethod;
    private static Method equalsMethod;
    private static Method toStringMethod;
    private static Method asVertexMethod;
    private static Method asEdgeMethod;

    static {
        try {
            hashCodeMethod = Object.class.getMethod("hashCode");
            equalsMethod = Object.class.getMethod("equals", new Class[]{Object.class});
            toStringMethod = Object.class.getMethod("toString");
            asVertexMethod = VertexFrame.class.getMethod("asVertex");
            asEdgeMethod = EdgeFrame.class.getMethod("asEdge");
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    /**
     * @deprecated The direction field will be dropped in the next major release
     */
    public FramedElement(final FramedGraph framedGraph, final Element element, final Direction direction) {
        if (null == framedGraph) {
            throw new IllegalArgumentException("FramedGraph can not be null");
        }

        if (null == element) {
            throw new IllegalArgumentException("Element can not be null");
        }

        this.elementID = element.getId();
        this.element = element;
        this.isVertex = element instanceof Vertex;
        
        this.framedGraph = framedGraph;
        this.direction = direction;
        
        framedGraph.addWrappedGraphReplacedListener(new FramedGraph.WrappedGraphReplacedListener() {
            @Override
            public void onWrappedGraphReplaced() {
        	synchronized (FramedElement.this) {
        	    FramedElement.this.element = null;
        	}
            }
	});
    }

    public FramedElement(final FramedGraph framedGraph, final Element element) {
        this(framedGraph, element, Direction.OUT);
    }

    public synchronized Object invoke(final Object proxy, final Method originalMethod, final Object[] arguments) {
        Method method = null;
        
        // try to find the method on one of the proxy's interfaces
        // (the passed in Method is often from a superclass or from the {@link Proxy} object itself,
        //  so we need to make sure we find the method that the user actually intended)
        for (Class<?> c : proxy.getClass().getInterfaces()) {
            for (Method interfaceMethod : c.getMethods()) {
                if (compareMethods(originalMethod, interfaceMethod)) {
                    if (interfaceMethod.getAnnotations().length > 0) {
                	method = interfaceMethod;
                    }
                    break;
                }
            }
        }
        if (method == null) {
            method = originalMethod;
        }
        
        Annotation[] annotations = method.getAnnotations();
        Map<Class<? extends Annotation>, AnnotationHandler<?>> annotationHandlers = this.framedGraph.getConfig().getAnnotationHandlers();
        Map<Class<? extends Annotation>, MethodHandler<?>> methodHandlers = this.framedGraph.getConfig().getMethodHandlers();
        for (final Annotation annotation : annotations) {
			MethodHandler methodHandler = methodHandlers.get(annotation.annotationType());
            if (methodHandler != null) {
                return methodHandler.processElement(proxy, method, arguments, annotation, this.framedGraph, getElement());
            }
        }
        for (final Annotation annotation : annotations) {
			AnnotationHandler annotationHandler = annotationHandlers.get(annotation.annotationType());
            if (annotationHandler != null) {
                return annotationHandler.processElement(annotation, method, arguments, this.framedGraph, getElement(), this.direction);
            }
        }
        
        // Now that we have checked for annotations, check if it is one of the default methods that we 
        // have builtin support for
        if (originalMethod.equals(hashCodeMethod)) {
            return getElement().hashCode();
        } else if (originalMethod.equals(equalsMethod)) {
            return this.proxyEquals(arguments[0]);
        } else if (originalMethod.equals(toStringMethod)) {
            return getElement().toString();
        } else if (originalMethod.equals(asVertexMethod) || originalMethod.equals(asEdgeMethod)) {
            return getElement();
        }
        
        if(method.getAnnotations().length == 0) {
            throw new UnhandledMethodException("The method " + method.getDeclaringClass().getName() + "." + method.getName() + " has no annotations, therefore frames cannot handle the method.");
        }
        
        throw new UnhandledMethodException("The method " + method.getDeclaringClass().getName() + "." + method.getName() + " was not annotated with any annotations that the framed graph is configured for. Please check your frame interface and/or graph configuration.");
    }

    /**
     * Returns true if the two methods have the same arguments, return types, and method names.
     */
    private boolean compareMethods(Method m1, Method m2) {
	if (!m1.getName().equals(m2.getName())) {
	    return false;
	}
	if (!m1.getReturnType().equals(m2.getReturnType())) {
	    return false;
	}
        Class<?>[] params1 = m1.getParameterTypes();
        Class<?>[] params2 = m2.getParameterTypes();
        if (params1.length == params2.length) {
            for (int i = 0; i < params1.length; i++) {
                if (params1[i] != params2[i])
                    return false;
            }
            return true;
        }
        return false;
    }
    
    private Boolean proxyEquals(final Object other) {
        if (other instanceof VertexFrame) {
            return this.getElement().equals(((VertexFrame) other).asVertex());
        } if (other instanceof EdgeFrame) {
            return this.getElement().equals(((EdgeFrame) other).asEdge());
        } else if (other instanceof Element) {
            return ElementHelper.areEqual(getElement(), other);
        } else {
            return Boolean.FALSE;
        }
    }

    public synchronized Element getElement() {
	Element loadedElement = this.element;
	if (loadedElement == null) {
	    if (isVertex) {
		loadedElement = this.framedGraph.getVertex(this.elementID);
	    } else {
		loadedElement = this.framedGraph.getEdge(this.elementID);
	    }
	    this.element = loadedElement;
	}
        return loadedElement;
    }
}
