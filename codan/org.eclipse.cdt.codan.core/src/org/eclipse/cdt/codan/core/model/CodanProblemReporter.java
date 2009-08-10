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
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class CodanProblemReporter {
	public static final String GENERIC_CODE_ANALYSIS_MARKER_TYPE = "org.eclipse.cdt.codan.core.codanProblem";

	public void reportProblem(String id, IFile file, int lineNumber,
			String message) {
		try {
			if (file == null)
				throw new NullPointerException("file");
			if (id == null)
				throw new NullPointerException("id");
			IProblem problem = CheckersRegisry.getInstance()
					.getResourceProfile(file).findProblem(id);
			if (problem == null)
				throw new IllegalArgumentException("Id is not registered");
			if (problem.isEnabled() == false)
				return; // skip
			int severity = problem.getSeverity().intValue();
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
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void deleteMarkers(IResource file) {
		try {
			file.deleteMarkers(GENERIC_CODE_ANALYSIS_MARKER_TYPE, false,
					IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
			ce.printStackTrace();
		}
	}

	public void deleteAllMarkers() {
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
}
