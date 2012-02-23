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
package org.eclipse.cdt.codan.internal.checkers.externaltool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.externaltool.AbstractOutputParser;
import org.eclipse.cdt.codan.core.externaltool.IProblemDisplay;
import org.eclipse.cdt.codan.core.externaltool.InvocationParameters;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.core.resources.IFile;

/**
 * Parses the output of Cppcheck.
 *
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 1.1
 */
public class CppcheckOutputParser extends AbstractOutputParser {
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

	private final InvocationParameters parameters;
	private final IProblemDisplay problemDisplay;

	/**
	 * Constructor.
	 * @param parameters the parameters to pass to Cppcheck.
	 * @param problemDisplay displays any problems reported by Cppcheck as markers.
	 */
	public CppcheckOutputParser(InvocationParameters parameters, IProblemDisplay problemDisplay) {
		this.parameters = parameters;
		this.problemDisplay = problemDisplay;
	}

	@Override
	public boolean parse(String line) {
		Matcher matcher = pattern.matcher(line);
		if (!matcher.matches()) {
			return false;
		}
		String filePath = matcher.group(1);
		if (parameters.getActualFilePath().equals(filePath)) {
			int lineNumber = Integer.parseInt(matcher.group(2));
			String severity = matcher.group(3);
			String description = matcher.group(4);
			IProblemLocation location = createProblemLocation(lineNumber);
			problemDisplay.reportProblem(location, description, severity);
		}
		return true;
	}

	private IProblemLocation createProblemLocation(int lineNumber) {
		IProblemLocationFactory factory = CodanRuntime.getInstance().getProblemLocationFactory();
		IFile actualFile = (IFile) parameters.getActualFile();
		return factory.createProblemLocation(actualFile, -1, -1, lineNumber);
	}

	@Override
	public void reset() {
	}
}
