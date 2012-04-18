/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.examples.checkers.cppcheck;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.resources.IFile;

/**
 * Parses the output of Cppcheck.
 */
public class CppcheckOutputParser implements IErrorParser {
	// sample line to parse:
	//
	// [/src/HelloWorld.cpp:19]: (style) The scope of the variable 'i' can be reduced
	// ----------1--------- -2    --3--  ------------------4-------------------------
	//
	// groups:
	// 1: file path and name
	// 2: line where problem was found
	// 3: problem severity
	// 4: problem description
	private static Pattern pattern = Pattern.compile("\\[(.*):(\\d+)\\]:\\s*\\((.*)\\)\\s*(.*)"); //$NON-NLS-1$

	@Override
	public boolean processLine(String line, ErrorParserManager eoParser) {
		Matcher matcher = pattern.matcher(line);
		if (!matcher.matches()) {
			return false;
		}
		IFile fileName = eoParser.findFileName(matcher.group(1));
		if (fileName != null) {
			int lineNumber = Integer.parseInt(matcher.group(2));
			String description = matcher.group(4);
			int severity = Severity.findSeverityCode(matcher.group(3));
			ProblemMarkerInfo info = new ProblemMarkerInfo(fileName, lineNumber, description, severity, null);
			eoParser.addProblemMarker(info);
			return true;
		}
		return false;
	}
}
