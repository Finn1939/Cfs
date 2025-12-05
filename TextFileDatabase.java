import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
// removed unused legacy stream imports; using NIO Files APIs instead
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Very small text-file persistence helper used by the sample app.
 * Provides simple UTF-8 line read/write operations.
 */
public class TextFileDatabase {
    /** Save lines to file (overwrites). */
    public static void saveLines(File f, List<String> lines) throws Exception {
        if (f == null) throw new IllegalArgumentException("file is null");
        File parent = f.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        // Write to a temp file and atomically move into place
        Path target = f.toPath();
        Path parentPath = target.getParent();
        if (parentPath == null) parentPath = f.getAbsoluteFile().getParentFile().toPath();
        Path tmp = Files.createTempFile(parentPath, "tmp", ".tmp");
        try (BufferedWriter bw = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8, StandardOpenOption.WRITE)) {
            for (String l : lines) {
                bw.write(l == null ? "" : l);
                bw.newLine();
            }
            bw.flush();
        }
        // Replace existing file atomically if possible
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    /** Load all lines from file. Returns empty list when file has no content. */
    public static List<String> loadLines(File f) throws Exception {
        List<String> res = new ArrayList<>();
        if (f == null || !f.exists()) return res;
        try (BufferedReader br = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                res.add(line);
            }
        }
        return res;
    }

    /** Append a single line to the file (creates file if missing). */
    public static void appendLine(File f, String line) throws Exception {
        if (f == null) throw new IllegalArgumentException("file is null");
        File parent = f.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        // Ensure file exists
        if (!f.exists()) Files.createFile(f.toPath());
        try (BufferedWriter bw = Files.newBufferedWriter(f.toPath(), StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            bw.write(line == null ? "" : line);
            bw.newLine();
            bw.flush();
        }
    }
}
