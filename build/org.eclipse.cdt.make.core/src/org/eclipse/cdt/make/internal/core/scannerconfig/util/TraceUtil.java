/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.util.Iterator;
import java.util.List;

/**
 * Tracebility related utility functions
 *
 * @author vhirsl
 */
public class TraceUtil {
	public static final String EOL = System.getProperty("line.separator"); //$NON-NLS-1$
	public static boolean SCANNER_CONFIG = false;
	
	public static boolean isTracing() {
		return SCANNER_CONFIG;
	}
	
	public static void outputTrace(String prefix, String msg, String postfix) {
		if (isTracing()) {
			System.out.println();
			System.out.println(prefix + ' ' + msg + ' ' + postfix);
		}
	}
	
	/**
	 * For traces of type:
	 *     Title:
	 *         Subtitle1:
	 *             item1[0]
	 *             item1[1]
	 *             ...
	 *         Subtitle2:
	 *             item2[0]
	 *             item2[1]
	 *             ...
	 * @param title
	 * @param col1
	 * @param col2
	 */
	public static void outputTrace(String title, String subtitle1, List item1, List item1new, String subtitle2, List item2) {
		if (isTracing()) {
			System.out.println();
			System.out.println(title);
			final String prefix = "  ";	//$NON-NLS-1$
			final String doublePrefix = "    ";	//$NON-NLS-1$
			System.out.println(prefix + subtitle1 + " (" + item1.size() + "):");	//$NON-NLS-1$ //$NON-NLS-2$
			int count = 0;
			for (Iterator i = item1.iterator(), j = item1new.iterator(); i.hasNext(); ) {
				System.out.println(doublePrefix + String.valueOf(++count) + "\t\'" +(String)i.next() + (j.hasNext()?"\' -> \'" + (String)j.next():"") + '\'');	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			System.out.println(prefix + subtitle2 + " (" + item2.size() + "):");	//$NON-NLS-1$ //$NON-NLS-2$
			count = 0;
			for (Iterator i = item2.iterator(); i.hasNext(); ) {
				System.out.println(doublePrefix + String.valueOf(++count) + "\t\'" + (String)i.next() + '\'');	//$NON-NLS-1$
			}
		}
	}

	/**
	 * @param string
	 * @param line
	 */
	public static void outputError(String string, String line) {
		if (isTracing()) {
			System.out.println();
			System.out.println("Error: " + string + line); //$NON-NLS-1$
		}
	}
}