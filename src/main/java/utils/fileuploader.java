package utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class fileuploader {
    private static final Set<String> EXTS = Set.of("pdf","docx","doc","txt");

    /** Lets user pick files AND/OR folders. Folders are expanded to matching docs. */
    public static List<Path> pickFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Documents (pdf, docx, doc, txt)", "pdf", "docx", "doc", "txt"
        ));

        int res = chooser.showOpenDialog(null);
        if (res != JFileChooser.APPROVE_OPTION) return List.of();

        List<Path> out = new ArrayList<>();
        for (File f : chooser.getSelectedFiles()) {
            if (f == null) continue;
            if (f.isDirectory()) {
                collectDocs(f.toPath(), out);
            } else if (ok(f.getName())) {
                out.add(f.toPath());
            }
        }
        // de-dupe in case both a folder and a file inside were selected
        return out.stream().distinct().toList();
    }

    private static void collectDocs(Path dir, List<Path> out) {
        try (Stream<Path> s = Files.walk(dir)) {
            s.filter(p -> Files.isRegularFile(p) && ok(p.getFileName().toString()))
                    .forEach(out::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean ok(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0) return false;
        String ext = name.substring(i + 1).toLowerCase(Locale.ROOT);
        return EXTS.contains(ext);
    }
}
