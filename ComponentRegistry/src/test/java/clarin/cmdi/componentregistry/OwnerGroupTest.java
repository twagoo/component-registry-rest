package clarin.cmdi.componentregistry;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class OwnerGroupTest {

    private OwnerGroup instance;

    @Before
    public void setUp() {
	instance = new OwnerGroup(101);
    }

    /**
     * Test of getId method, of class OwnerGroup.
     */
    @Test
    public void testGetId() {
	Number result = instance.getId();
	assertEquals(101, result);
    }

    /**
     * Test of equals method, of class OwnerGroup.
     */
    @Test
    public void testEquals() {
	OwnerGroup instance2 = new OwnerGroup(101);
	OwnerGroup instance3 = new OwnerGroup(102);
	boolean result = instance.equals(instance2);
	assertTrue(result);
	result = instance.equals(instance3);
	assertFalse(result);
    }

    /**
     * Test of hashCode method, of class OwnerGroup.
     */
    @Test
    public void testHashCode() {
	OwnerGroup instance2 = new OwnerGroup(101);
	OwnerGroup instance3 = new OwnerGroup(102);
	assertTrue(instance.hashCode() == instance2.hashCode());
	assertFalse(instance.hashCode() == instance3.hashCode());
    }
}
