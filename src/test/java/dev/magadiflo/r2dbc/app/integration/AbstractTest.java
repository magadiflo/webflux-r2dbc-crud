package dev.magadiflo.r2dbc.app.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.core.DatabaseClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@DataR2dbcTest
public abstract class AbstractTest {

    @Autowired
    private DatabaseClient databaseClient;
    private static String dataSQL;
    private static String resetTestData;

    @BeforeAll
    static void beforeAll() throws IOException {
        resetTestData = getSQL(new ClassPathResource("sql/reset_test_data.sql"));
        dataSQL = getSQL(new ClassPathResource("sql/data.sql"));
    }

    @BeforeEach
    void setUp() {
        this.executeSQL(resetTestData);
        this.executeSQL(dataSQL);
    }

    private static String getSQL(ClassPathResource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void executeSQL(String sql) {
        this.databaseClient.sql(sql)
                .fetch()
                .rowsUpdated()
                .block();
    }
}
