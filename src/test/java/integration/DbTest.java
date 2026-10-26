package integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DbTest {

    @Test
    void recognizesMvtDatabaseNames() {
        assertTrue(Db.isMvtDatabase("mvt20261026"));
        assertTrue(Db.isMvtDatabase("mvt20261026101500"));
    }

    @Test
    void ignoresOtherDatabases() {
        assertFalse(Db.isMvtDatabase("mvt"));
        assertFalse(Db.isMvtDatabase("admin"));
        assertFalse(Db.isMvtDatabase("mvt_backup"));
        assertFalse(Db.isMvtDatabase("my_mvt20261026"));
    }
}
