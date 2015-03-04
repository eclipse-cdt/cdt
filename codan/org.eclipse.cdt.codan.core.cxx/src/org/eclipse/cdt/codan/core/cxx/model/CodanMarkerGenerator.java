/*******************************************************************************
 * Copyright (c) Mar 4, 2015 QNX Software Systems. All Rights Reserved.
 *
 * You must obtain a written license from and pay applicable license fees to QNX
 * Software Systems before you may reproduce, modify or distribute this software,
 * or any work that includes all or part of this software.   Free development
 * licenses are available for evaluation and non-commercial purposes.  For more
 * information visit [http://licensing.qnx.com] or email licensing@qnx.com.
 *
 * This file may contain contributions from others.  Please review this entire
 * file for other proprietary rights or license notices, as well as the QNX
 * Development Suite License Guide at [http://licensing.qnx.com/license-guide/]
 * for other information.
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.model;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemLocation;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * Default implementation of IMarkerGenerator for API's that require such thing
 * 
 * @since 3.3
 */
public class CodanMarkerGenerator implements IMarkerGenerator {
	private final String problemId;
	private final IProblemReporter reporter;

	public CodanMarkerGenerator(String problemId) {
		this.problemId = problemId;
		this.reporter = CodanRuntime.getInstance().getProblemReporter();
	}

	public CodanMarkerGenerator(String problemId, IProblemReporter reporter) {
		this.problemId = problemId;
		this.reporter = reporter;
	}

	@Deprecated
	public void addMarker(IResource file, int lineNumber, String description, int severity, String variableName) {
		addMarker(new ProblemMarkerInfo(file, lineNumber, description, severity, variableName));
	}

	@Override
	public void addMarker(ProblemMarkerInfo info) {
		reporter.reportProblem(getProblemId(info.severity), createProblemLocation(info), info.description, info.variableName);
	}

	protected String getProblemId(int severity) {
		return problemId;
	}

	protected IProblemLocation createProblemLocation(ProblemMarkerInfo info) {
		IProblemLocationFactory factory = CodanRuntime.getInstance().getProblemLocationFactory();
		if (info.file instanceof IFile)
			return factory.createProblemLocation((IFile) info.file, info.startChar, info.endChar, info.lineNumber);
		else
			return new CodanProblemLocation(info.file, info.startChar, info.endChar, info.lineNumber);
	}
}
