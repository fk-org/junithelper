package org.junithelper.plugin.util;

import junit.framework.TestCase;

public class PrimitiveTypeUtilTest extends TestCase {

	public void test_getPrimitiveClass_A$String() throws Exception {
		String[] args = new String[] { "byte", "short", "int", "long", "char",
				"float", "double", "boolean", "void", };
		Class<?>[] expected = new Class<?>[] { byte.class, short.class,
				int.class, long.class, char.class, float.class, double.class,
				boolean.class, void.class, };
		for (int i = 0; i < args.length; i++) {
			Class<?> actual = PrimitiveTypeUtil.getPrimitiveClass(args[i]);
			assertEquals(expected[i], actual);
		}
		try {
			PrimitiveTypeUtil.getPrimitiveClass("dummy");
			fail("Expected Exception did not occurred!");
		} catch (IllegalArgumentException e) {
		}
	}

	public void test_getPrimitiveDefaultValue_A$String() throws Exception {
		String[] args = new String[] { "byte", "short", "int", "long", "char",
				"float", "double", "boolean", "void", };
		String[] expected = new String[] { "0", "0", "0", "0L", "'\u0000'",
				"0.0F", "0.0", "false", "void" };
		for (int i = 0; i < args.length; i++) {
			String actual = PrimitiveTypeUtil.getPrimitiveDefaultValue(args[i]);
			assertEquals(expected[i], actual);
		}
		try {
			PrimitiveTypeUtil.getPrimitiveDefaultValue("dummy");
			fail("Expected Exception did not occurred!");
		} catch (IllegalArgumentException e) {
		}
	}

	public void test_isPrimitive_A$String() throws Exception {
		String[] args = new String[] { "byte", "short", "int", "long", "char",
				"float", "double", "boolean", "void", "dummy" };
		boolean[] expected = new boolean[] { true, true, true, true, true,
				true, true, true, true, false };
		for (int i = 0; i < args.length; i++) {
			boolean actual = PrimitiveTypeUtil.isPrimitive(args[i]);
			assertEquals(expected[i], actual);
		}
	}

}