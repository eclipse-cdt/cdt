/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David McKnight   (IBM)        - [247573][api] IClassifierConstants.TYPE_LINK == "link" is not in sync with actual usage of "symbolic link" in the code
 ********************************************************************************/

package org.eclipse.rse.services.clientserver;

/**
 * This interface defines some constants for classifiers.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IClassifierConstants {

	/**
	 * Default type.
	 */
	public static final String TYPE_DEFAULT = "file"; //$NON-NLS-1$

	/**
	 * Link type, "link".
	 *
	 * @deprecated should use TYPE_SYMBOLIC_LINK
	 */
	public static final String TYPE_LINK = "link"; //$NON-NLS-1$

	/**
	 * Symbolic link type, "symbolic link"
	 *
	 * @since 3.1
	 */
	public static final String TYPE_SYMBOLIC_LINK = "symbolic link"; //$NON-NLS-1$

	/**
	 * Java executable type, "executable(java:*)".
	 */
	public static final String TYPE_EXECUTABLE_JAVA = "executable(java:*)"; //$NON-NLS-1$

	/**
	 * Binary executable type, "executable(binary)".
	 */
	public static final String TYPE_EXECUTABLE_BINARY = "executable(binary)"; //$NON-NLS-1$

	/**
	 * Script executable type, "executable(script)".
	 */
	public static final String TYPE_EXECUTABLE_SCRIPT = "executable(script)"; //$NON-NLS-1$

	/**
	 * Match java executable type, "*executable(java:*)*".
	 */
	public static final String MATCH_EXECUTABLE_JAVA = "*executable(java:*)*"; //$NON-NLS-1$

	/**
	 * Match binary executable type, "*executable(java:*)*".
	 */
	public static final String MATCH_EXECUTABLE_BINARY = "*executable(binary)*"; //$NON-NLS-1$

	/**
	 * Match script executable type, "*executable(java:*)*".
	 */
	public static final String MATCH_EXECUTABLE_SCRIPT = "*executable(script)*"; //$NON-NLS-1$
}