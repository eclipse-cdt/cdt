/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.ui;

import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;

public interface IBuildConsoleManager {
	IConsole getConsole(IProject project);
	IDocument getConsoleDocument(IProject project);
	void addConsoleListener(IBuildConsoleListener listener);
	void removeConsoleListener(IBuildConsoleListener listener);
}
