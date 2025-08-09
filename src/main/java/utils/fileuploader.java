package utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class fileuploader {
    public static List<Path> pickFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Documents (pdf, docx, doc, txt)", "pdf", "docx", "doc", "txt"
        ));
        int res = chooser.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            return Arrays.stream(files).map(File::toPath).toList();
        }
        return List.of();
    }
}
