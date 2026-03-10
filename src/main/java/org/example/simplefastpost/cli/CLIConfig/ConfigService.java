package org.example.simplefastpost.cli.CLIConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import org.sqlite.SQLiteDataSource;

import java.nio.file.*;

@Configuration
public class ConfigService {

    @Bean
    public DataSource dataSource() throws Exception {

        Path dataDir = getDataDir();
        Files.createDirectories(dataDir);

        Path db = dataDir.resolve("SimpleFastPostDB.db");

        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:" + db.toAbsolutePath());

        return ds;
    }

    private Path getDataDir() {

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
        return Paths.get(System.getProperty("user.home"), ".local", "share", "SimpleFastPost");
    }
}
