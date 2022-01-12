/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mike Kucera (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * This class contains several convenience methods mainly for debugging purposes.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("nls")
public class DebugUtil {

	private DebugUtil() { // class just contains static methods
	}

	/**
	 * Prints a trace message to stdout that gives info
	 * about the method that calls this method.
	 */
	public static void printMethodTrace() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		printMethodTrace(trace, null);
	}

	/**
	 * Prints a trace message to stdout that gives info
	 * about the method that calls this method.
	 *
	 * The output is in a format that will show up as a hyperlink in the eclipse console.
	 */
	public static void printMethodTrace(String extraMessage) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		printMethodTrace(trace, extraMessage);
	}

	private static void printMethodTrace(StackTraceElement[] trace, String extraMessage) {
		StackTraceElement caller = trace[3];

		String className = caller.getClassName();
		className = className.substring(className.lastIndexOf(".") + 1);

		String message = String.format("%s.%s(%s:%d)", className, caller.getMethodName(), caller.getFileName(),
				caller.getLineNumber());

		if (extraMessage != null)
			message += ": " + extraMessage;

		System.out.println(message);
	}

	public static String safeClassName(Object obj) {
		return obj != null ? obj.getClass().getSimpleName() : "";
	}

	public static String toStringWithClass(Object obj) {
		return obj != null ? String.valueOf(obj) + " " + obj.getClass().getSimpleName() : "null";
	}

	/**
	 * Prints the values of javabean properties to the console.
	 * This method is not recursive, it does not print nested properties.
	 *
	 * Example of usage:
	 *
	 * IResource resource = ...;
	 * DebugUtil.printObjectProperties(resource);
	 * DebugUtil.printObjectProperties(resource.getResourceAttributes());
	 * @since 5.1
	 */
	public static void printObjectProperties(Object obj) {
		try {
			System.out.println("Object: " + obj);
			BeanInfo info = Introspector.getBeanInfo(obj.getClass());

			for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
				Method getter = propertyDescriptor.getReadMethod();
				try {
					System.out.println("  " + getter.getName() + "=" + getter.invoke(obj, new Object[0]));
				} catch (Exception e) {
				}
			}
		} catch (IntrospectionException e) {
		}
	}
}
