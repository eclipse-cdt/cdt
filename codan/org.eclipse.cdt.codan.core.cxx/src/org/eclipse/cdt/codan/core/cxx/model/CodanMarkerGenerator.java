/*******************************************************************************
 * Copyright (c) 2015, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Alena Laskavaia - Initial API and implementation
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

	@Override
	@Deprecated
	public void addMarker(IResource file, int lineNumber, String description, int severity, String variableName) {
		addMarker(new ProblemMarkerInfo(file, lineNumber, description, severity, variableName));
	}

	@Override
	public void addMarker(ProblemMarkerInfo info) {
		reporter.reportProblem(getProblemId(info.severity), createProblemLocation(info), info.description,
				info.variableName);
	}

	protected String getProblemId(int severity) {
		return problemId;
	}

	protected IProblemLocation createProblemLocation(ProblemMarkerInfo info) {
		IProblemLocationFactory factory = CodanRuntime.getInstance().getProblemLocationFactory();
		if (info.file instanceof IFile)
			return factory.createProblemLocation((IFile) info.file, info.startChar, info.endChar, info.lineNumber);
		return new CodanProblemLocation(info.file, info.startChar, info.endChar, info.lineNumber);
	}
}
