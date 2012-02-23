/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import static java.util.Collections.singletonList;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.codan.core.externaltool.AbstractOutputParser;
import org.eclipse.cdt.codan.core.externaltool.ConfigurationSettings;
import org.eclipse.cdt.codan.core.externaltool.InvocationParameters;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.ui.cxx.externaltool.AbstractCxxExternalToolBasedChecker;

/**
 * Checker that invokes <a href="http://cppcheck.sourceforge.net/">Cppcheck</a> when a C/C++ is
 * saved.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class CppcheckChecker extends AbstractCxxExternalToolBasedChecker {
	private static final String TOOL_NAME = "Cppcheck"; //$NON-NLS-1$
	private static final String EXECUTABLE_NAME = "cppcheck"; //$NON-NLS-1$
	private static final String DEFAULT_ARGS = ""; //$NON-NLS-1$

	private static final String DESCRIPTION_FORMAT = "[" + TOOL_NAME + "] %s"; //$NON-NLS-1$ //$NON-NLS-2$

	private static final String ERROR_PROBLEM_ID;

	// key: severity (error, warning, etc.) - value : problem ID associated to severity
	private static final Map<String, String> PROBLEM_IDS = new HashMap<String, String>();
	static {
		ERROR_PROBLEM_ID = addProblemId("error"); //$NON-NLS-1$
		addProblemId("warning"); //$NON-NLS-1$
		addProblemId("style"); //$NON-NLS-1$
	}
	private static String addProblemId(String severity) {
		String problemId = "org.eclipse.cdt.codan.checkers.cppcheck." + severity; //$NON-NLS-1$
		PROBLEM_IDS.put(severity, problemId);
		return problemId;
	}

	public CppcheckChecker() {
		super(new ConfigurationSettings(TOOL_NAME, new File(EXECUTABLE_NAME), DEFAULT_ARGS, false));
	}

	@Override
	public void reportProblem(IProblemLocation location, String description, String severity) {
		String problemId = PROBLEM_IDS.get(severity);
		if (problemId == null) {
			problemId = getReferenceProblemId();
		}
		super.reportProblem(problemId, location, String.format(DESCRIPTION_FORMAT, description));
	}

	@Override
	protected String getReferenceProblemId() {
		return ERROR_PROBLEM_ID;
	}

	@Override
	protected List<AbstractOutputParser> createParsers(InvocationParameters parameters) {
		AbstractOutputParser parser = new CppcheckOutputParser(parameters, this);
		return singletonList(parser);
	}
}
