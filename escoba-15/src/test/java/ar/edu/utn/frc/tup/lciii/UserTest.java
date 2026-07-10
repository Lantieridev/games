package ar.edu.utn.frc.tup.lciii;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserTest {

    @Test
    public void testNoArgConstructor_StartsWithNullFields() {
        User user = new User();
        assertNull(user.getName());
        assertNull(user.getScore());
    }

    @Test
    public void testParameterizedConstructor_SetsNameAndScore() {
        User user = new User("Human", 5);
        assertEquals("Human", user.getName());
        assertEquals(5, user.getScore());
    }

    @Test
    public void testSettersAndGetters() {
        User user = new User();
        user.setName("APP");
        user.setScore(10);
        assertEquals("APP", user.getName());
        assertEquals(10, user.getScore());
    }
}
