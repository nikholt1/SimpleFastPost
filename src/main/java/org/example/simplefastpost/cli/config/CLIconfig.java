package org.example.simplefastpost.cli.config;

import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CLIconfig {

    public String getDBUrl() throws Exception {
        Path dataDir = getDataDir();
        Files.createDirectories(dataDir);

        Path db = dataDir.resolve("SimpleFastPostDB.db");
        String dbUrl = "jdbc:sqlite:" + db.toAbsolutePath();


        return dbUrl;
    }

    public Path getDataDir() {
        String env = System.getenv("SFP_DATA_DIR");
        if (env != null && !env.isBlank()) {
            return Paths.get(env);
        }

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return Paths.get(System.getenv("LOCALAPPDATA"), "SimpleFastPost");
        }
        if (os.contains("mac")) {
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "SimpleFastPost");
        }

        // Default for Linux/others
        return Paths.get(System.getProperty("user.home"), ".local", "share", "SimpleFastPost");
    }
}
