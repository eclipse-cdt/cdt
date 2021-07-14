package org.eclipse.cdt.core.parser.util;

public class ObjectUtil {
	public static boolean noneInstanceOf(Class<?> cls, Object... objs) {
		if (objs == null) {
			return true;
		}

		for (Object o : objs) {
			if (cls.isInstance(o)) {
				return false;
			}
		}
		return true;
	}

	public static boolean allInstanceOf(Class<?> cls, Object... objs) {
		if (objs == null) {
			return false;
		}

		for (Object o : objs) {
			if (!cls.isInstance(o)) {
				return false;
			}
		}
		return true;
	}

	public static boolean instanceOf(Object obj, Class<?> clazz, Class<?>... clazzes) {
		if (obj == null) {
			return clazzes == null;
		}
		if (clazz == null) {
			return false;
		}
		if (obj.getClass().isAssignableFrom(clazz)) {
			return true;
		}
		for (Class<?> klazz : clazzes) {
			if (obj.getClass().isAssignableFrom(klazz)) {
				return true;
			}
		}
		return false;
	}
}
