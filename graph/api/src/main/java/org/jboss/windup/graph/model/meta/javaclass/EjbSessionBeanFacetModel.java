package org.jboss.windup.graph.model.meta.javaclass;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("EJBSessionBean")
public interface EjbSessionBeanFacetModel extends JavaClassMetaModel
{

    @Label
    @Property("ejbSessionBeanName")
    public String getSessionBeanName();

    @Property("ejbSessionBeanName")
    public void setSessionBeanName(String ejbSessionBeanName);

    @Property("sessionType")
    public String getSessionType();

    @Property("sessionType")
    public void setSessionType(String sessionType);

    @Property("transactionType")
    public String getTransactionType();

    @Property("transactionType")
    public void setTransactionType(String transactionType);

    @Property("displayName")
    public String getDisplayName();

    @Property("displayName")
    public void setDisplayName(String displayName);

    @Property("ejbId")
    public String getEjbId();

    @Property("ejbId")
    public void setEjbId(String id);

    @Adjacency(label = "ejbLocal", direction = Direction.OUT)
    public void setEjbLocal(JavaClassModel ejbLocal);

    @Adjacency(label = "ejbLocal", direction = Direction.OUT)
    public JavaClassModel getEjbLocal();

    @Adjacency(label = "ejbRemote", direction = Direction.OUT)
    public void setEjbRemote(JavaClassModel ejbRemote);

    @Adjacency(label = "ejbRemote", direction = Direction.OUT)
    public JavaClassModel getEjbRemote();

    @Adjacency(label = "ejbLocalHome", direction = Direction.OUT)
    public void setEjbLocalHome(JavaClassModel ejbLocalHome);

    @Adjacency(label = "ejbLocalHome", direction = Direction.OUT)
    public JavaClassModel getEjbLocalHome();

    @Adjacency(label = "ejbHome", direction = Direction.OUT)
    public void setEjbHome(JavaClassModel ejbHome);

    @Adjacency(label = "ejbHome", direction = Direction.OUT)
    public JavaClassModel getEjbHome();

}
