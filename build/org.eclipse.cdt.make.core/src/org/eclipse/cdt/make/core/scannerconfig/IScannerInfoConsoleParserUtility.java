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
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * Common work required by the scanner info console parsers
 * 
 * @author vhirsl
 */
public interface IScannerInfoConsoleParserUtility {
	// Problem marker related
	public void generateMarker(IResource file, int lineNumber, String desc, int severity, String varName);
	public boolean reportProblems();
	// File path management
	public void changeMakeDirectory(String dir, int dirLevel, boolean enterDir);
	public IFile findFile(String fileName);
	public List translateRelativePaths(IFile file, String fileName, List includes);
	public String normalizePath(String path);
}
