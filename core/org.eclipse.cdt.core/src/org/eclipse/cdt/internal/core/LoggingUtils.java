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
package org.eclipse.cdt.internal.core;

/**
 * Some general purpose functions that can be useful for logging/tracing activities.
 * 
 * @since 5.2
 */
public class LoggingUtils {
	/**
	 * Return a string that uniquely identifies a Java object reference, in the form "classname@id", where
	 * 'classname' is the simple (unqualified) name of the object's class, and 'id' is the hash code. If the
	 * object is of an anonymous class, classname will be "<anonymous-class>".
	 * 
	 * Why not just use obj.toString()? That method is often overriden, and so cannot be relied on for a
	 * representation that uniquely identifies the object in the VM space.
	 * 
	 * @param obj
	 *            the object reference to stringify
	 * @return the stringified representation of the object reference
	 */
	public static String toString(Object obj) {
		if (obj == null) {
			return "null";  //$NON-NLS-1$
		}
		String className = obj.getClass().getSimpleName();
		if (className == null) {
			className = "<anonymous-class>"; //$NON-NLS-1$
		}

		String id = Integer.toHexString(System.identityHashCode(obj));
	
		return className + "@" + id; //$NON-NLS-1$
	}

	
}
