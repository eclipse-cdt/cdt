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

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

public class GLDErrorParser implements IErrorParser {

	public boolean processLine(String line, ErrorParserManager eoParser) {
		// binutils linker error:
		// 1- an error when trying to link
		// tempfile: In function `function':
		// tempfile(.text+0xhex): undefined reference to `symbol'
		// 2-
		// Something went wrong check if it is "ld" the linkeer bay cheching
		// the last letter for "ld"
		// An example might be (not all are warnings):
		// ntox86-ld: warning: libcpp.so.2, needed by C:/temp//libdisplay.so, may conflict with libcpp.so.3
		int firstColon= line.indexOf(':');
		if (firstColon != -1) {
			String buf= line.substring(0, firstColon);
			String desc= line.substring(firstColon + 1);
			int firstPara= buf.indexOf('(');
			int secondPara= buf.indexOf(')');
			if (firstPara >= 0 && secondPara >= 0) {
				String fileName = buf.substring(0, firstPara);
				String previous = eoParser.getPreviousLine();
				if (previous == null) 
					previous = ""; //$NON-NLS-1$
				int colon = previous.indexOf(':');
				if (colon != -1) {
					previous = previous.substring(colon + 1);
				}

				// The pattern is to generall we have to guard:
				// Before making this pattern a marker we do one more check
				// The fileName that we extract __must__ look like a valid file name.
				// We been having to much bad hits with patterns like
				//   /bin/sh ../libtool --mode=link gcc -version-info 0:1:0 foo.lo var.lo
				// Things like libtool that will fool the parser because of "0:1:0"
				if (!Path.EMPTY.isValidPath(fileName)) {
					return false;
				}

				desc = "*" + previous + " " + desc; //$NON-NLS-1$ //$NON-NLS-2$
				IFile file = eoParser.findFileName(fileName);
				if (file != null) {
					if (eoParser.isConflictingName(fileName)) {
						file = null;
					}
				} else {
					file = eoParser.findFilePath(fileName);
				}
				if (file == null) {
					desc = fileName + " " + desc; //$NON-NLS-1$
				}
				eoParser.generateMarker(file, 0, desc, IMarkerGenerator.SEVERITY_ERROR_RESOURCE, null);
			} else if (buf.endsWith("ld")){ //$NON-NLS-1$
				// By default treat the condition as fatal/error, unless marked as a warning
				int errorType = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
				desc = desc.trim();
				if(desc.startsWith("warning") || desc.startsWith("Warning")) { //$NON-NLS-1$ //$NON-NLS-2$
					errorType = IMarkerGenerator.SEVERITY_WARNING;
				}

				String fileName = line.substring(0, firstColon);
				IFile file = eoParser.findFileName(fileName);
				if (file != null) {
					if (eoParser.isConflictingName(fileName)) {
						file = null;
					}
				} else {
					file = eoParser.findFilePath(fileName);
				}
				if (file == null) {
					desc = fileName + " " + desc; //$NON-NLS-1$
				} 
				
				eoParser.generateMarker(file, 0, desc, errorType, null);
			}
		}
		return false;
	}
}
