package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MvtUnitTest {

    @Test
    void findsUnitFromSymbol() {
        assertEquals(MvtUnit.MICROGRAMS, MvtUnit.fromSymbol("µg"));
        assertEquals(MvtUnit.ALFA_TE, MvtUnit.fromSymbol("alfa-TE"));
    }

    @Test
    void rejectsUnknownSymbol() {
        assertThrows(IllegalArgumentException.class, () -> MvtUnit.fromSymbol("lb"));
    }
}
