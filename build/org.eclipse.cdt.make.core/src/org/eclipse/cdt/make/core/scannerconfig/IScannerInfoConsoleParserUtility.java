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
 * Common work required by the scanner info console parsers
 * 
 * @author vhirsl
 */
public interface IScannerInfoConsoleParserUtility {
	public void initialize(IProject project, IPath workingDirectory, IMarkerGenerator markerGenerator);
}
