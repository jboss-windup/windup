package org.jboss.windup.rules.apps.java.archives.identify.test;

import java.io.File;
import java.util.List;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.windup.rules.apps.java.archives.identify.ArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.identify.LuceneArchiveIdentificationService;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class LuceneFileArchiveIdentificationServiceTest
{

    @Test
    public void testGetCoordinateFromSHA1() throws Exception
    {
        final File file = new File("target/test-nexus-data/lucene/");
        Assert.assertTrue("Test file does not exist", file.exists());
        LuceneArchiveIdentificationService ident = new LuceneArchiveIdentificationService(file);

        List<Coordinate> coordinates = ident.getCoordinates("55555555564e84315e83c6ba4a855b07ba51166b");
        Assert.assertNull("No coordinate for 55555555564e84315e83c6ba4a855b07ba51166b", coordinates);

        // Position 0
        check(ident, "000005ce9bd9867e24cdc33c06e88a65edce71db", "com.google.apis:google-api-services-genomics:jar::v1beta-rev26-1.18.0-rc");

        // Last entry
        check(ident, "ffffdf1558b62750b24bdaa33cb9a72b0cb766ce", "org.glassfish.metro:wsmc-impl:jar::2.1.1-b06");

        // A block around pivot break.
        check(ident, "4e02fd52064e84315e83c6ba4a855b07ba51166b", "org.jogamp.joal:joal:jar:natives-macosx-universal:2.1.2");
        check(ident, "4e031603849ad1e70d245855802cc388ded93461", "org.glassfish.jdbc.jdbc-ra.jdbc40:jdbc40:jar::3.0-b37");
        // Position 29723213
        check(ident, "4e031bb61df09069aeb2bffb4019e7a5034a4ee0", "junit:junit:jar::4.11");
        check(ident, "4e0334465984c00cbcf177b1702805bd4b5d6d27", "org.soitoolkit.refapps.sd:soitoolkit-refapps-sample-schemas:jar::0.6.1");
        check(ident, "4e034d862d9650df285b8ee98f7f770db6c19029", "org.apache.cxf:cxf-rt-bindings-soap:jar::2.4.8");
        //check(ident, "4e035cf698423199fbaa68a9be9fc264c758727c", "org.apache.maven.reporting:maven-reporting-impl:zip:source-release:2.0.5");
        // zip types removed from index data.

        // Some which caused issues.
        check(ident, "7ff0d167a6816aa113b1b4a8a37515701a74b288", "org.kill-bill.billing:killbill-platform-osgi-bundles-lib-slf4j-osgi:jar::0.1.0");
    }

    private static void check(ArchiveIdentificationService ident, String hash, String coordString)
    {
        List<Coordinate> coords = ident.getCoordinates(hash);
        Assert.assertNotNull("Coordinate found for " + hash, coords);
        Assert.assertFalse("Coordinate found for " + hash, coords.isEmpty());
        Assert.assertEquals(hash + " = " + coordString, coordString, coordToString(coords.get(0)));
        Assert.assertEquals(hash + " = " + coordString, coordString, coordToString(coords.get(0)));
    }


    // GROUP_ID:ARTIFACT_ID[:PACKAGING[:CLASSIFIER]]:VERSION
    private static String coordToString(Coordinate coord)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(coord.getGroupId()).append(':').append(coord.getArtifactId());
        if (coord.getPackaging() != null)
            sb.append(':').append(coord.getPackaging());
        if (coord.getClassifier() != null)
            sb.append(':').append(coord.getClassifier());
        sb.append(':').append(coord.getVersion());
        return sb.toString();
    }

}
