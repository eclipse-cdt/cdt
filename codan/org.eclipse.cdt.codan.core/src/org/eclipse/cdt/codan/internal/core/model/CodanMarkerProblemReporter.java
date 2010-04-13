/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.model;

import java.text.MessageFormat;

import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemReporterPersistent;
import org.eclipse.cdt.codan.internal.core.CheckersRegisry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class CodanMarkerProblemReporter implements IProblemReporterPersistent {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.IProblemReporter#reportProblem(java.
	 * lang.String, org.eclipse.cdt.codan.core.model.IProblemLocation,
	 * java.lang.Object[])
	 */
	public void reportProblem(String id, IProblemLocation loc, Object... args) {
		IFile file = loc.getFile();
		int lineNumber = loc.getLineNumber();
		if (file == null)
			throw new NullPointerException("file"); //$NON-NLS-1$
		if (id == null)
			throw new NullPointerException("id"); //$NON-NLS-1$
		IProblem problem = CheckersRegisry.getInstance().getResourceProfile(
				file).findProblem(id);
		if (problem == null)
			throw new IllegalArgumentException("Id is not registered"); //$NON-NLS-1$
		if (problem.isEnabled() == false)
			return; // skip
		int severity = problem.getSeverity().intValue();
		String messagePattern = problem.getMessagePattern();
		String message = id;
		if (messagePattern == null) {
			if (args != null && args.length > 0 && args[0] instanceof String)
				message = (String) args[0];
		} else {
			message = MessageFormat.format(messagePattern, args);
		}
		reportProblem(id, severity, file, lineNumber, loc.getStartingChar(),
				loc.getEndingChar(), message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.IProblemReporter#reportProblem(java.
	 * lang.String, org.eclipse.core.resources.IFile, int, java.lang.String)
	 */
	public void reportProblem(String id, int severity, IFile file,
			int lineNumber, int startChar, int endChar, String message) {
		try {
			// Do not put in duplicates
			IMarker[] cur = file.findMarkers(GENERIC_CODE_ANALYSIS_MARKER_TYPE,
					false, IResource.DEPTH_ZERO);
			if (cur != null) {
				for (IMarker element : cur) {
					int line = ((Integer) element
							.getAttribute(IMarker.LINE_NUMBER)).intValue();
					if (line == lineNumber) {
						String mesg = (String) element
								.getAttribute(IMarker.MESSAGE);
						int sev = ((Integer) element
								.getAttribute(IMarker.SEVERITY)).intValue();
						if (sev == severity && mesg.equals(message))
							return;
					}
				}
			}
			IMarker marker = file
					.createMarker(GENERIC_CODE_ANALYSIS_MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.PROBLEM, id);
			marker.setAttribute(IMarker.CHAR_END, endChar);
			marker.setAttribute(IMarker.CHAR_START, startChar);
			marker.setAttribute("org.eclipse.cdt.core.problem", 42);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void deleteProblems(IResource file) {
		try {
			file.deleteMarkers(GENERIC_CODE_ANALYSIS_MARKER_TYPE, false,
					IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
			ce.printStackTrace();
		}
	}

	public void deleteAllProblems() {
		try {
			// TODO delete contributed markers too
			ResourcesPlugin.getWorkspace().getRoot().deleteMarkers(
					GENERIC_CODE_ANALYSIS_MARKER_TYPE, false,
					IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.IProblemReporterPersistent#deleteProblems
	 * (org.eclipse.core.resources.IResource,
	 * org.eclipse.cdt.codan.core.model.IChecker)
	 */
	public void deleteProblems(IResource file, IChecker checker) {
		deleteProblems(file);
	}
}
