package org.eclipse.cdt.core.parser.tests;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.cdt.core.parser.util.ObjectUtil;

import junit.framework.TestCase;

public class ObjectUtilTest extends TestCase {

	private String s1 = "";
	private String s2 = "kjalkfjdalf";
	private String s3 = null;

	private AtomicInteger i1 = new AtomicInteger(1);
	private AtomicInteger i2 = new AtomicInteger(2);
	private AtomicInteger i3 = null;

	public void testAllInstenceOfTrueCases() {
		assertTrue(ObjectUtil.allInstanceOf(String.class, s1));
		assertTrue(ObjectUtil.allInstanceOf(String.class, s2));
		assertTrue(ObjectUtil.allInstanceOf(String.class, s1, s2));

		assertTrue(ObjectUtil.allInstanceOf(AtomicInteger.class, i1));
		assertTrue(ObjectUtil.allInstanceOf(AtomicInteger.class, i2));
		assertTrue(ObjectUtil.allInstanceOf(AtomicInteger.class, i1, i2));
	}

	public void testAllInstenceOfFalseCases() {
		assertFalse(ObjectUtil.allInstanceOf(AtomicInteger.class, s1));
		assertFalse(ObjectUtil.allInstanceOf(AtomicInteger.class, s2));
		assertFalse(ObjectUtil.allInstanceOf(AtomicInteger.class, s1, s2));

		assertFalse(ObjectUtil.allInstanceOf(String.class, i1));
		assertFalse(ObjectUtil.allInstanceOf(String.class, i2));
		assertFalse(ObjectUtil.allInstanceOf(String.class, i1, i2));

		assertFalse(ObjectUtil.allInstanceOf(String.class, s1, i2));
		assertFalse(ObjectUtil.allInstanceOf(String.class, i1, s2));
	}

	public void testAllInstenceOfEmptyCase() {
		assertTrue(ObjectUtil.allInstanceOf(String.class));
	}

	public void testAllInstenceOfNullCase() {
		assertFalse(null instanceof String);
		assertFalse(ObjectUtil.allInstanceOf(String.class, null));
		assertFalse(ObjectUtil.allInstanceOf(String.class, (String) null));
		assertFalse(ObjectUtil.allInstanceOf(String.class, (AtomicInteger) null));
		assertFalse(ObjectUtil.allInstanceOf(String.class, "test", null, "test2"));
	}

	public void testNoneInstenceOfTrueCases() {
		assertTrue(ObjectUtil.noneInstanceOf(AtomicInteger.class, s1));
		assertTrue(ObjectUtil.noneInstanceOf(AtomicInteger.class, s3));
		assertTrue(ObjectUtil.noneInstanceOf(AtomicInteger.class, s1, s2));
	}

	public void testNoneInstenceOfFalseCases() {
		assertFalse(ObjectUtil.noneInstanceOf(String.class, s1));
		assertFalse(ObjectUtil.noneInstanceOf(String.class, s2));
		assertFalse(ObjectUtil.noneInstanceOf(String.class, s1, s2));
		assertFalse(ObjectUtil.noneInstanceOf(String.class, i2, s2, i1));
	}

	public void testNoneInstenceOfEmptyCase() {
		assertTrue(ObjectUtil.allInstanceOf(String.class));
	}

	public void testNoneInstenceOfNullCase() {
		assertTrue(ObjectUtil.noneInstanceOf(String.class, null));
		assertTrue(ObjectUtil.noneInstanceOf(String.class, (String) null));
		assertTrue(ObjectUtil.noneInstanceOf(String.class, s3));
		assertTrue(ObjectUtil.noneInstanceOf(String.class, i3));
		assertTrue(ObjectUtil.noneInstanceOf(String.class, i1, null, i2));
	}

	public void testInstanceOf() {
		assertFalse(ObjectUtil.instanceOf(null, String.class));
		assertFalse(ObjectUtil.instanceOf(null, null));
		assertFalse(ObjectUtil.instanceOf(s1, null));
		assertTrue(ObjectUtil.instanceOf(s2, String.class));
		assertTrue(ObjectUtil.instanceOf(s2, String.class, Integer.class));
		assertFalse(ObjectUtil.instanceOf(i2, String.class));
		assertFalse(ObjectUtil.instanceOf(i2, String.class, Integer.class));
	}
}
