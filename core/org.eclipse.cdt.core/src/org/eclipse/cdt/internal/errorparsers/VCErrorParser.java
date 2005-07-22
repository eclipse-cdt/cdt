/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.errorparsers;

import java.io.File;
import java.util.StringTokenizer;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;

public class VCErrorParser implements IErrorParser {
	
	public boolean processLine(String line, ErrorParserManager eoParser) {
		// msdev: filname(linenumber) : error/warning error_desc
		int firstColon = line.indexOf(':');
		if (firstColon != -1) {
			/* Guard against drive in Windows platform.  */
			if (firstColon == 1) {
				try {
					String os = System.getProperty("os.name"); //$NON-NLS-1$
					if (os != null && os.startsWith("Win")) { //$NON-NLS-1$
						try {
							if (Character.isLetter(line.charAt(0))) {
								firstColon = line.indexOf(':', 2);
							}
						} catch (StringIndexOutOfBoundsException e) {
						}
					}
				} catch (SecurityException e) {
				}
			}
		}

		if (firstColon != -1) {
			String firstPart = line.substring(0, firstColon);
			StringTokenizer tok = new StringTokenizer(firstPart, "()"); //$NON-NLS-1$
			if (tok.hasMoreTokens()) {
				String fileName = tok.nextToken();
				if (tok.hasMoreTokens()) {
					// Line number can either be ### or ###,##
					String lineNumber = tok.nextToken();
					try {
				    	int firstComma = lineNumber.indexOf(',');
				    	if (firstComma != -1) {
				    		lineNumber = lineNumber.substring(0, firstComma);
				    	}
						int num = Integer.parseInt(lineNumber);
						int i = fileName.lastIndexOf(File.separatorChar);
						if (i != -1) {
							fileName = fileName.substring(i + 1);
						}
						IFile file = eoParser.findFileName(fileName);
						if (file != null || eoParser.isConflictingName(fileName)) {
							String desc = line.substring(firstColon + 1).trim();
							if (file == null) {
								desc = "*" + desc; //$NON-NLS-1$
							}
							int severity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
							if (desc.startsWith("warning") || desc.startsWith("remark")) { //$NON-NLS-1$ //$NON-NLS-2$
								severity = IMarkerGenerator.SEVERITY_WARNING;
							}
							eoParser.generateMarker(file, num, desc, severity, null);
							return true;
						}
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		return false;
	}
}
