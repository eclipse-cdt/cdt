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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.codan.core.cxx.externaltool.AbstractExternalToolBasedChecker;
import org.eclipse.cdt.codan.core.cxx.externaltool.ConfigurationSettings;
import org.eclipse.cdt.core.ProblemMarkerInfo;

/**
 * Checker that invokes <a href="http://cppcheck.sourceforge.net/">Cppcheck</a> when a C/C++ file is
 * saved.
 */
public class CppcheckChecker extends AbstractExternalToolBasedChecker {
	private static final String TOOL_NAME = Messages.CppcheckChecker_toolName; 
	private static final String EXECUTABLE_NAME = "cppcheck"; //$NON-NLS-1$
	private static final String DEFAULT_ARGS = "--enable=all"; //$NON-NLS-1$

	private static final String DESCRIPTION_FORMAT = "[" + TOOL_NAME + "] %s"; //$NON-NLS-1$ //$NON-NLS-2$

	private static final String ERROR_PROBLEM_ID;

	// key: severity (error, warning, etc.) - value : problem ID associated to severity
	private static final Map<Severity, String> PROBLEM_IDS = new HashMap<Severity, String>();

	static {
		ERROR_PROBLEM_ID = addProblemId(Severity.ERROR);
		addProblemId(Severity.WARNING);
		addProblemId(Severity.STYLE);
	}

	private static String addProblemId(Severity severity) {
		String problemId = "org.eclipse.cdt.codan.checkers.cppcheck." + severity; //$NON-NLS-1$
		PROBLEM_IDS.put(severity, problemId);
		return problemId;
	}

	public CppcheckChecker() {
		super(new ConfigurationSettings(TOOL_NAME, new File(EXECUTABLE_NAME), DEFAULT_ARGS));
	}

	@Override
	protected String[] getParserIDs() {
		return new String[] { "org.eclipse.cdt.codan.checkers.externaltool.CppcheckChecker" }; //$NON-NLS-1$
	}

	@Override
	public void addMarker(ProblemMarkerInfo info) {
		Severity severity = Severity.findSeverity(info.severity);
		String description = String.format(DESCRIPTION_FORMAT, info.description);
		reportProblem(PROBLEM_IDS.get(severity), createProblemLocation(info), description);
	}

	@Override
	protected String getReferenceProblemId() {
		return ERROR_PROBLEM_ID;
	}
}
