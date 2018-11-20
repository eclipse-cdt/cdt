/*******************************************************************************
 * Copyright (c) 2009 Freescale Semiconductor, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

/**
 * Some general purpose functions that can be useful for logging/tracing activities.
 */
public class LoggingUtils {
	/**
	 * Return a string that uniquely identifies a Java object reference, in the
	 * form "classname@id", where 'classname' is the simple or package qualified
	 * name of the object's class, and 'id' is the hash code.
	 *
	 * Why not just use obj.toString()? That method is often overriden, and so
	 * cannot be relied on for a representation that uniquely identifies the
	 * object in the VM space.
	 *
	 * @param obj
	 *            the object reference to stringify
	 * @param simpleClassName
	 *            if true, use the class's simple name, otherwise the package
	 *            qualified one
	 *
	 * @return the stringified representation of the object reference
	 */
	public static String toString(Object obj, boolean simpleClassName) {
		if (obj == null) {
			return "null"; //$NON-NLS-1$
		}
		String className = obj.getClass().getName();
		if (simpleClassName) {
			int lastDot = className.lastIndexOf('.');
			if ((lastDot >= 0) && ((lastDot + 1) < className.length())) {
				className = className.substring(lastDot + 1);
			}
		}

		String id = Integer.toHexString(System.identityHashCode(obj));

		return className + "@" + id; //$NON-NLS-1$
	}

	/**
	 * Equivalent to toString(obj, false)
	 */
	public static String toString(Object obj) {
		return toString(obj, true);
	}
}
