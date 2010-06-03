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
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemReporterPersistent;
import org.eclipse.cdt.codan.internal.core.CheckersRegistry;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Problem reported that created eclipse markers
 */
public class CodanMarkerProblemReporter implements IProblemReporterPersistent {
	public void reportProblem(String id, IProblemLocation loc, Object... args) {
		IResource file = loc.getFile();
		int lineNumber = loc.getLineNumber();
		if (file == null)
			throw new NullPointerException("file"); //$NON-NLS-1$
		if (id == null)
			throw new NullPointerException("id"); //$NON-NLS-1$
		IProblem problem = CheckersRegistry.getInstance()
				.getResourceProfile(file).findProblem(id);
		if (problem == null)
			throw new IllegalArgumentException("Id is not registered:" + id); //$NON-NLS-1$
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
		reportProblem(id, problem.getMarkerType(), severity, file, lineNumber,
				loc.getStartingChar(), loc.getEndingChar(), message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.IProblemReporter#reportProblem(java.
	 * lang.String, org.eclipse.core.resources.IFile, int, java.lang.String)
	 */
	public void reportProblem(String id, String markerType, int severity,
			IResource file, int lineNumber, int startChar, int endChar,
			String message) {
		try {
			// Do not put in duplicates
			IMarker[] cur = file.findMarkers(markerType, false,
					IResource.DEPTH_ZERO);
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
			IMarker marker = file.createMarker(markerType);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.PROBLEM, id);
			marker.setAttribute(IMarker.CHAR_END, endChar);
			marker.setAttribute(IMarker.CHAR_START, startChar);
			marker.setAttribute("org.eclipse.cdt.core.problem", 42); //$NON-NLS-1$
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void deleteProblems(IResource file) {
		try {
			file.deleteMarkers(GENERIC_CODE_ANALYSIS_MARKER_TYPE, true,
					IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
			ce.printStackTrace();
		}
	}

	public void deleteAllProblems() {
		try {
			ResourcesPlugin
					.getWorkspace()
					.getRoot()
					.deleteMarkers(GENERIC_CODE_ANALYSIS_MARKER_TYPE, true,
							IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			CodanCorePlugin.log(e);
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
	public void deleteProblems(final IResource file, final IChecker checker) {
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IMarker[] markers = file.findMarkers(
							GENERIC_CODE_ANALYSIS_MARKER_TYPE, true,
							IResource.DEPTH_INFINITE);
					ICheckersRegistry reg = CodanRuntime.getInstance()
							.getChechersRegistry();
					for (int i = 0; i < markers.length; i++) {
						IMarker m = markers[i];
						String id = m.getAttribute(IMarker.PROBLEM, ""); //$NON-NLS-1$
						Collection<IProblem> problems = reg.getRefProblems(checker);
						for (Iterator<IProblem> iterator = problems.iterator(); iterator
								.hasNext();) {
							IProblem iProblem = iterator.next();
							if (iProblem.getId().equals(id))
								m.delete();
						}
					}
				}
			}, null, IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e) {
			CodanCorePlugin.log(e);
		}
	}
}
