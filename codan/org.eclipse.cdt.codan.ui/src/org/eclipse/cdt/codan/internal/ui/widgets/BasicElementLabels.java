/*******************************************************************************
 * Copyright (c) 2010, 2011 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.widgets;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ui.IWorkingSet;

/**
 * A label provider for basic elements like paths. The label provider will make
 * sure that the labels are correctly
 * shown in RTL environments.
 *
 * @since 3.4
 */
public class BasicElementLabels {
	// TextProcessor delimiters
	private static final String CODE_DELIMITERS = TextProcessor.getDefaultDelimiters() + "<>()?,{}+-*!%=^|&;[]~"; //$NON-NLS-1$
	private static final String FILE_PATTERN_DELIMITERS = TextProcessor.getDefaultDelimiters() + "*.?"; //$NON-NLS-1$
	private static final String URL_DELIMITERS = TextProcessor.getDefaultDelimiters() + ":@?-"; //$NON-NLS-1$

	/**
	 * Returns the label of a path.
	 *
	 * @param path
	 *        the path
	 * @param isOSPath
	 *        if <code>true</code>, the path represents an OS path, if
	 *        <code>false</code> it is a workspace path.
	 * @return the label of the path to be used in the UI.
	 */
	public static String getPathLabel(IPath path, boolean isOSPath) {
		String label;
		if (isOSPath) {
			label = path.toOSString();
		} else {
			label = path.makeRelative().toString();
		}
		return markLTR(label);
	}

	/**
	 * Returns the label of the path of a file.
	 *
	 * @param file
	 *        the file
	 * @return the label of the file path to be used in the UI.
	 */
	public static String getPathLabel(File file) {
		return markLTR(file.getAbsolutePath());
	}

	/**
	 * Returns the label for a file pattern like '*.java'
	 *
	 * @param name
	 *        the pattern
	 * @return the label of the pattern.
	 */
	public static String getFilePattern(String name) {
		return markLTR(name, FILE_PATTERN_DELIMITERS);
	}

	/**
	 * Returns the label for a URL, URI or URL part. Example is
	 * 'http://www.x.xom/s.html#1'
	 *
	 * @param name
	 *        the URL string
	 * @return the label of the URL.
	 */
	public static String getURLPart(String name) {
		return markLTR(name, URL_DELIMITERS);
	}

	/**
	 * Returns a label for a resource name.
	 *
	 * @param resource
	 *        the resource
	 * @return the label of the resource name.
	 */
	public static String getResourceName(IResource resource) {
		return markLTR(resource.getName());
	}

	/**
	 * Returns a label for a resource name.
	 *
	 * @param resourceName
	 *        the resource name
	 * @return the label of the resource name.
	 */
	public static String getResourceName(String resourceName) {
		return markLTR(resourceName);
	}

	/**
	 * Returns a label for Java code snippet used in a label. Example is 'Test
	 * test= new Test<? extends List>() { ...}'.
	 *
	 * @param string
	 *        the Java code snippet
	 * @return the label for the Java code snippet
	 */
	public static String getJavaCodeString(String string) {
		return markLTR(string, CODE_DELIMITERS);
	}

	/**
	 * Returns a label for a version name. Example is '1.4.1'
	 *
	 * @param name
	 *        the version string
	 * @return the version label
	 */
	public static String getVersionName(String name) {
		return markLTR(name);
	}

	/**
	 * Returns a label for a working set
	 *
	 * @param set
	 *        the working set
	 * @return the label of the working set
	 */
	public static String getWorkingSetLabel(IWorkingSet set) {
		return markLTR(set.getLabel());
	}

	/**
	 * It does not do anything now, but just in case we need to do the same as
	 * JDT does (see String.markLTR in JDT)
	 */
	private static String markLTR(String label) {
		return label;
	}

	private static String markLTR(String label, String delim) {
		return label;
	}
}
