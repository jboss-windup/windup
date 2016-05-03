package org.jboss.windup.rules.apps.java.archives.config;

import java.io.File;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitializationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.archives.identify.CompositeArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.identify.InMemoryArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.identify.LuceneArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.identify.pkg.LucenePackageToArtifactMapper;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.file.FileSuffixPredicate;
import org.jboss.windup.util.file.FileVisit;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Loads configuration/metadata for identifying archives by SHA1 hashes.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RuleMetadata(phase = InitializationPhase.class)
public class ArchiveIdentificationConfigLoadingRuleProvider extends AbstractRuleProvider
{
    private static final Logger log = Logging.get(ArchiveIdentificationConfigLoadingRuleProvider.class);

    public static final String METADATA_DIR_MARKER_LUCENE = "archive-metadata.lucene.marker";
    public static final String METADATA_DIR_MARKER_TEXT = ".archive-metadata.txt";
    public static final String PACKAGE_INDEX_DIR_MARKER = "package-archive-map.lucene.marker";

    public static final String NEXUS_INDEXER_DATA_SUBDIR = "nexus-indexer-data";

    @Inject
    private CompositeArchiveIdentificationService identifier;

    @Override
    public Configuration getConfiguration(final GraphContext grCtx)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .perform(new AddDelimitedFileIndexOperation())
            .addRule()
            .perform(new AddLuceneIndexOperation());
    }

    private class AddDelimitedFileIndexOperation extends GraphOperation
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            Visitor<File> visitor = new Visitor<File>() {
                @Override public void visit(File file) {
                    try {
                        log.info("Loading artifact identification data from [" + file.getAbsolutePath() + "]");
                        identifier.addIdentifier(new InMemoryArchiveIdentificationService().addMappingsFrom(file));
                    }
                    catch (Exception e) {
                        throw new WindupException("Failed to load identification data from file [" + file + "]", e);
                    }
                }
            };

            FileSuffixPredicate predicate = FileSuffixPredicate.fromLiteral(METADATA_DIR_MARKER_TEXT);
            FileVisit.visit(PathUtil.getUserCacheDir().resolve(NEXUS_INDEXER_DATA_SUBDIR).toFile(), predicate, visitor);
            FileVisit.visit(PathUtil.getWindupCacheDir().resolve(NEXUS_INDEXER_DATA_SUBDIR).toFile(), predicate, visitor);
        }
    }

    private class AddLuceneIndexOperation extends GraphOperation
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            {
                Visitor<File> luceneMetadataVisitor = new Visitor<File>() {
                    @Override public void visit(File file) {
                        try {
                            log.info("Loading artifact identification Lucene index from [" + file.getAbsolutePath() + "]");
                            identifier.addIdentifier(new LuceneArchiveIdentificationService(file.getParentFile()));
                        }
                        catch (Exception e) {
                            throw new WindupException("Failed to load identification data from Lucene index [" + file + "]", e);
                        }
                    }
                };

                FileSuffixPredicate predicate = FileSuffixPredicate.fromLiteral(METADATA_DIR_MARKER_LUCENE);
                FileVisit.visit(PathUtil.getUserCacheDir().resolve(NEXUS_INDEXER_DATA_SUBDIR).toFile(), predicate, luceneMetadataVisitor);
                FileVisit.visit(PathUtil.getWindupCacheDir().resolve(NEXUS_INDEXER_DATA_SUBDIR).toFile(), predicate, luceneMetadataVisitor);
            }

            {
                Visitor<File> lucenePackageMapperVisitor = new Visitor<File>() {
                    @Override public void visit(File file) {
                        try {
                            log.info("Loading artifact identification Lucene index from [" + file.getAbsolutePath() + "]");
                            identifier.addIdentifier(new LucenePackageToArtifactMapper(file.getParentFile()));
                        }
                        catch (Exception e) {
                            throw new WindupException("Failed to load identification data from Lucene index [" + file + "]", e);
                        }
                    }
                };

                FileSuffixPredicate predicate = FileSuffixPredicate.fromLiteral(PACKAGE_INDEX_DIR_MARKER);
                FileVisit.visit(PathUtil.getUserCacheDir().resolve(NEXUS_INDEXER_DATA_SUBDIR).toFile(), predicate, lucenePackageMapperVisitor);
                FileVisit.visit(PathUtil.getWindupCacheDir().resolve(NEXUS_INDEXER_DATA_SUBDIR).toFile(), predicate, lucenePackageMapperVisitor);
            }
        }
    }
}
