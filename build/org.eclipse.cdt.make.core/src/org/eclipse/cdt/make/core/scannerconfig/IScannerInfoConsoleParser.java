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

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Parses a line of command output looking for scanner info entries.
 * 
 * @author vhirsl
 */
public interface IScannerInfoConsoleParser {
	/**
	 * One time initialization of a console parser.
	 * 
	 * @param project
     * @param workingDirectory
	 * @param collector - scanner info collector
     * @param markerGenerator
	 */
	public void startup(IProject project, IPath workingDirectory,
            			IScannerInfoCollector collector, IMarkerGenerator markerGenerator);
	
	/**
	 * Parse one line of output.
	 * 
	 * @param line
	 * @return true if scanner info entry was found in the line
	 */
	public boolean processLine(String line);
	
	/**
	 * Optional finalization of a console parser.
	 */
	public void shutdown();
}
