package utils;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class textextractor {
    private final AutoDetectParser parser = new AutoDetectParser();

    public ExtractionResult extract(List<Path> files) {
        StringBuilder merged = new StringBuilder();
        List<FileStatus> statuses = new ArrayList<>();

        for (Path p : files) {
            String name = p.getFileName().toString();
            try (InputStream is = Files.newInputStream(p)) {
                BodyContentHandler handler = new BodyContentHandler(-1);
                Metadata metadata = new Metadata();
                ParseContext context = new ParseContext();

                parser.parse(is, handler, metadata, context);
                String text = handler.toString();

                if (!text.isBlank()) {
                    if (merged.length() > 0) {
                        merged.append("\n\n--- FILE: ").append(name).append(" ---\n\n");
                    }
                    merged.append(text);
                }
                statuses.add(FileStatus.ok(name, Files.size(p)));
            } catch (Exception ex) {
                statuses.add(FileStatus.error(name, ex.getMessage()));
            }
        }
        return new ExtractionResult(merged.toString(), statuses);
    }

    public record ExtractionResult(String text, List<FileStatus> files) {}

    public static final class FileStatus {
        public final String name;
        public final Long sizeBytes;
        public final String error;

        private FileStatus(String name, Long sizeBytes, String error) {
            this.name = name;
            this.sizeBytes = sizeBytes;
            this.error = error;
        }

        public static FileStatus ok(String name, long size) {
            return new FileStatus(name, size, null);
        }

        public static FileStatus error(String name, String error) {
            return new FileStatus(name, null, error);
        }

        public boolean succeeded() {
            return error == null;
        }
    }
}
