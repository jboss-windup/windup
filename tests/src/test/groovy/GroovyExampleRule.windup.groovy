import org.jboss.windup.ast.java.data.TypeReferenceLocation
import org.jboss.windup.reporting.config.Hint
import org.jboss.windup.reporting.config.Link
import org.jboss.windup.reporting.config.classification.Classification
import org.jboss.windup.rules.apps.java.condition.JavaClass
import org.jboss.windup.rules.apps.xml.condition.XmlFile

ruleSet("Example Servlet Rule")
    .addRule()
    .when(JavaClass.references("javax.servlet.annotation.WebServlet").at(TypeReferenceLocation.ANNOTATION))
    .perform(
    	Hint
    		.withText("Web Servlet")
    		.withEffort(8)
    		.with(Link.to("Example Link", "https://www.exampleservletlink.com/"))
    		.withEffort(0)
    )

    .addRule()
    .when(JavaClass.references("StringBuilder"))
    .perform(Hint.withText("This is using a StringBuilder").withEffort(8))

    .addRule()
    .when(JavaClass.references("java.net.URL"))
    .perform(Hint.withText("This is using java.net.URL").withEffort(8))

    .addRule()
    .when(JavaClass.references("URL"))
    .perform(Hint.withText("This is using URL").withEffort(8))

    .addRule()
    .when(JavaClass.references("java.io.InputStream"))
    .perform(Hint.withText("This is using java.io.InputStream").withEffort(8))

    .addRule()
    .when(JavaClass.references("InputStream"))
    .perform(Hint.withText("This is using InputStream").withEffort(8))

    .addRule()
    .when(JavaClass.references("java.io.OutputStream"))
    .perform(Hint.withText("This is using java.io.OutputStream").withEffort(8))

    .addRule()
    .when(JavaClass.references("OutputStream"))
    .perform(Hint.withText("This is using OutputStream").withEffort(8))

    .addRule()
    .when(XmlFile.matchesXpath("/w:web-app").namespace("w", "http://java.sun.com/xml/ns/javaee"))
    .perform(Hint.titled("Container").withText("This is a web descriptor").withEffort(2))

    .addRule()
    .when(JavaClass.references("javax.ejb.ActivationConfigProperty"))
    .perform(Classification.as("Classification ActivationConfigProperty").withEffort(8))

		.addRule()
		.when(JavaClass.references("weblogic.{*}"))
		.perform(Classification.as("Some weblogic junk").withEffort(8))
