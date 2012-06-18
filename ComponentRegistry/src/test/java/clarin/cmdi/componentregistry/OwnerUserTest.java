package clarin.cmdi.componentregistry;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class OwnerUserTest {

    private OwnerUser instance;

    @Before
    public void setUp() {
	instance = new OwnerUser(101);
    }

    /**
     * Test of getId method, of class OwnerUser.
     */
    @Test
    public void testGetId() {
	Number result = instance.getId();
	assertEquals(101, result);
    }

    /**
     * Test of equals method, of class OwnerUser.
     */
    @Test
    public void testEquals() {
	OwnerUser instance2 = new OwnerUser(101);
	OwnerUser instance3 = new OwnerUser(102);
	boolean result = instance.equals(instance2);
	assertTrue(result);
	result = instance.equals(instance3);
	assertFalse(result);
    }

    /**
     * Test of hashCode method, of class OwnerUser.
     */
    @Test
    public void testHashCode() {
	OwnerUser instance2 = new OwnerUser(101);
	OwnerUser instance3 = new OwnerUser(102);
	assertTrue(instance.hashCode() == instance2.hashCode());
	assertFalse(instance.hashCode() == instance3.hashCode());
    }
}
