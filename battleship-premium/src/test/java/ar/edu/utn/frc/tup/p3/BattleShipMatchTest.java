package ar.edu.utn.frc.tup.p3;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BattleShipMatchTest {

    private Boolean invokeGetYesNoAnswer(String input) {
        Optional<Method> method = ReflectionUtils.findMethod(BattleShipMatch.class, "getYesNoAnswer", String.class);
        if (method.isEmpty()) {
            fail("Method getYesNoAnswer not found");
        }
        return (Boolean) ReflectionUtils.invokeMethod(method.get(), null, input);
    }

    @Test
    void testGetYesNoAnswer_LowercaseY_ReturnsTrue() {
        assertTrue(invokeGetYesNoAnswer("y"));
    }

    @Test
    void testGetYesNoAnswer_UppercaseY_ReturnsTrue() {
        assertTrue(invokeGetYesNoAnswer("Y"));
    }

    @Test
    void testGetYesNoAnswer_LowercaseN_ReturnsFalse() {
        assertFalse(invokeGetYesNoAnswer("n"));
    }

    @Test
    void testGetYesNoAnswer_UppercaseN_ReturnsFalse() {
        assertFalse(invokeGetYesNoAnswer("N"));
    }

    @Test
    void testGetYesNoAnswer_InvalidInput_ReturnsNull() {
        assertNull(invokeGetYesNoAnswer("maybe"));
    }

    @Test
    void testGetYesNoAnswer_EmptyInput_ReturnsNull() {
        assertNull(invokeGetYesNoAnswer(""));
    }
}
