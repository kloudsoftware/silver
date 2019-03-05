package software.kloud.silver.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FsWriter {
    private final File file = new File(".silver");

    public void write(String message) throws IOException {
        if (file.isFile()) {
            file.delete();
            file.createNewFile();
        }

        var fout = new FileOutputStream(file);
        try (fout) {
            fout.write(message.getBytes());
            fout.flush();
        }
    }

    public String read() throws IOException {
        if(!file.exists()) {
            file.createNewFile();
        }

        var fin = new FileInputStream(file);
        try (fin) {
            return new String(fin.readAllBytes());
        }
    }
}
