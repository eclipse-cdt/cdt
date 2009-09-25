/*******************************************************************************
 * Copyright (c) 2009 Freescale Semiconductor, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.internal;

/**
 * Some general purpose functions that can be useful for logging/tracing
 * activities. This is a duplicate of LoggingUtils in
 * org.eclipse.cdt.internal.core. The idea is for core parts of DSF (ones that
 * don't have debug in package name) to have very limited dependencies on other
 * plugins.
 * 
 * @since 2.1
 */
public class LoggingUtils {
	/**
	 * Return a string that uniquely identifies a Java object reference, in the
	 * form "classname@id", where 'classname' is the simple (package
	 * unqualified) name of the object's class, and 'id' is the hash code.
	 * 
	 * Why not just use obj.toString()? That method is often overriden, and so
	 * cannot be relied on for a representation that uniquely identifies the
	 * object in the VM space.
	 * 
	 * @param obj
	 *            the object reference to stringify
	 * @return the stringified representation of the object reference
	 */
	public static String toString(Object obj) {
		if (obj == null) {
			return "null";  //$NON-NLS-1$
		}
		String className = obj.getClass().getName();
		int lastDot = className.lastIndexOf('.');
		if ((lastDot >= 0) && ((lastDot + 1) < className.length())) {
			className = className.substring(lastDot + 1);
		}

		String id = Integer.toHexString(System.identityHashCode(obj));
	
		return className + "@" + id; //$NON-NLS-1$
	}

	/**
	 * Flatten out an array of strings into one string, in the form
	 * "{s1, s2, s3, ...}"
	 * 
	 * @param strings
	 *            the array of string
	 * @return the flattened representation
	 */
	public static String toString(String[] strings) {
		StringBuilder str = new StringBuilder("{"); //$NON-NLS-1$
		for (String s : strings) {
			str.append(s + ", "); //$NON-NLS-1$
		}
		if (strings.length > 0) {
			str.delete(str.length()-2, Integer.MAX_VALUE); // remove the trailing comma and space
		}
		str.append("}"); //$NON-NLS-1$
		return str.toString();
	}

	
}
