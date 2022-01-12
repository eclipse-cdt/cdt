/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Parses a line of command output looking for scanner info entries.
 *
 * @author vhirsl
 */
public interface IScannerInfoConsoleParser extends IConsoleParser {
	/**
	 * One time initialization of a console parser.
	 *
	 * @param collector - scanner info collector
	 */
	public void startup(IProject project, IPath workingDirectory, IScannerInfoCollector collector,
			IMarkerGenerator markerGenerator);

}
