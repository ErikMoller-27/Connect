package utils;
import utils.textextractor.ExtractionResult;

import java.nio.file.Path;
import java.util.List;

public class apptest {
    public static void main(String[] args) {
        List<Path> files = fileuploader.pickFiles();
        if (files.isEmpty()) {
            System.out.println("No files selected.");
            return;
        }
        textextractor extractor = new textextractor();
        ExtractionResult result = extractor.extract(files);

        System.out.println("Merged text length: " + result.text().length());
        result.files().forEach(f ->
                System.out.println((f.succeeded() ? "✅" : "❌") + " " + f.name +
                        (f.succeeded() ? (" (" + f.sizeBytes + " bytes)") : (" | " + f.error)))
        );
    }
}
