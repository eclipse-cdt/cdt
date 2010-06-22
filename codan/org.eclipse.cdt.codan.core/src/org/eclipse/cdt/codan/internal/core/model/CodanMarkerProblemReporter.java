/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.model;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.AbstractProblemReporter;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.ICodanProblemMarker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemReporterPersistent;
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
public class CodanMarkerProblemReporter extends AbstractProblemReporter
		implements IProblemReporterPersistent {
	@Override
	protected void reportProblem(ICodanProblemMarker codanProblemMarker) {
		createProblem(codanProblemMarker);
	}

	/**
	 * @param codanProblemMarker
	 */
	protected IMarker createProblem(ICodanProblemMarker codanProblemMarker) {
		try {
			// Do not put in duplicates
			IMarker[] cur = codanProblemMarker.getResource().findMarkers(
					codanProblemMarker.getProblem().getMarkerType(), false,
					IResource.DEPTH_ZERO);
			if (cur != null) {
				String message = codanProblemMarker.createMessage();
				for (IMarker element : cur) {
					int line = ((Integer) element
							.getAttribute(IMarker.LINE_NUMBER)).intValue();
					if (line == codanProblemMarker.getLocation()
							.getLineNumber()) {
						String mesg = (String) element
								.getAttribute(IMarker.MESSAGE);
						int sev = ((Integer) element
								.getAttribute(IMarker.SEVERITY)).intValue();
						if (sev == codanProblemMarker.getProblem()
								.getSeverity().intValue()
								&& mesg.equals(message))
							return element;
					}
				}
			}
			return codanProblemMarker.createMarker();
		} catch (CoreException e) {
			CodanCorePlugin.log(e);
			return null;
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
							.getCheckersRegistry();
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
