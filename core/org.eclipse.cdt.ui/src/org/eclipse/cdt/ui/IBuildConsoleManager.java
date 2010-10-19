/*******************************************************************************
 * Copyright (c) 2002, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Alex Collins (Broadcom Corp.) - Global console
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;

/**
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildConsoleManager {
	/**
	 * @return the console to which build output should be printed.
	 *         This may be backed by two console documents: one for the Project
	 *         and one Global
	 */
	IConsole getConsole(IProject project);
	/**
	 * @return the console associated with the specified project
	 * @since 5.3
	 */
	IConsole getProjectConsole(IProject project);
	/**
	 * @return the global console
	 * @since 5.3
	 */
	IConsole getGlobalConsole();
	/**
	 * @return the document backing the global console
	 * @since 5.3
	 */
	IDocument getGlobalConsoleDocument();
	/**
	 * @param project
	 * @return IDocument backing the console for the given project
	 */
	IDocument getConsoleDocument(IProject project);
	IProject getLastBuiltProject();
	void addConsoleListener(IBuildConsoleListener listener);
	void removeConsoleListener(IBuildConsoleListener listener);

	/**
	 * Setup the the global console at the start of the build
	 * @since 5.3
	 */
	void startGlobalConsole();
	/**
	 * Tear down the the global console at the start of the build
	 * @since 5.3
	 */
	void stopGlobalConsole();
}
