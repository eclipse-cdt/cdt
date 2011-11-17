/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.messages.Messages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Scanner config discovery related marker generator
 *
 * @author vhirsl
 */
public class SCMarkerGenerator implements IMarkerGenerator {

	/**
	 *
	 */
	public SCMarkerGenerator() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IMarkerGenerator#addMarker(org.eclipse.core.resources.IResource, int, java.lang.String, int, java.lang.String)
	 */
	@Override
	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		ProblemMarkerInfo info = new ProblemMarkerInfo(file, lineNumber, errorDesc, severity, errorVar);
		addMarker(info);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IMarkerGenerator#addMarker(org.eclipse.cdt.core.ProblemMarkerInfo)
	 */
	@Override
	public void addMarker(final ProblemMarkerInfo problemMarkerInfo) {
		// we have to add the marker in the job or we can deadlock other
		// threads that are responding to a resource delta by doing something
		// that accesses the project description
		Job markerJob = new Job(Messages.SCMarkerGenerator_Add_Markers) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IMarker marker;
				try {
					IMarker[] cur = problemMarkerInfo.file.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ONE);
					/*
					 * Try to find matching markers and don't put in duplicates
					 */
					if ((cur != null) && (cur.length > 0)) {
						for (int i = 0; i < cur.length; i++) {
							int line = ((Integer) cur[i].getAttribute(IMarker.LINE_NUMBER)).intValue();
							int sev = ((Integer) cur[i].getAttribute(IMarker.SEVERITY)).intValue();
							String mesg = (String) cur[i].getAttribute(IMarker.MESSAGE);
							if (line == problemMarkerInfo.lineNumber && sev == mapMarkerSeverity(problemMarkerInfo.severity) && mesg.equals(problemMarkerInfo.description)) {
								return Status.OK_STATUS;
							}
						}
					}
				} catch (CoreException e) {
					return new Status(Status.ERROR, MakeCorePlugin.getUniqueIdentifier(), Messages.SCMarkerGenerator_Error_Adding_Markers, e);
				}

				try {
					marker = problemMarkerInfo.file.createMarker(ICModelMarker.C_MODEL_PROBLEM_MARKER);
					marker.setAttribute(IMarker.MESSAGE, problemMarkerInfo.description);
					marker.setAttribute(IMarker.SEVERITY, mapMarkerSeverity(problemMarkerInfo.severity));
					marker.setAttribute(IMarker.LINE_NUMBER, problemMarkerInfo.lineNumber);
					marker.setAttribute(IMarker.CHAR_START, -1);
					marker.setAttribute(IMarker.CHAR_END, -1);

					if (problemMarkerInfo.variableName != null) {
						marker.setAttribute(ICModelMarker.C_MODEL_MARKER_VARIABLE, problemMarkerInfo.variableName);
					}
					marker.setAttribute(IMarker.LOCATION, Messages.SCMarkerGenerator_Discovery_Options_Page);
				} catch (CoreException e) {
					return new Status(Status.ERROR, MakeCorePlugin.getUniqueIdentifier(), Messages.SCMarkerGenerator_Error_Adding_Markers, e);
				}

				return Status.OK_STATUS;
			}
		};

		markerJob.setRule(problemMarkerInfo.file);
		markerJob.schedule();
	}

	public void removeMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		IWorkspace workspace = file.getWorkspace();
		// remove specific marker
		try {
			IMarker[] markers = file.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ONE);
			if (markers != null) {
				List<IMarker> exactMarkers = new ArrayList<IMarker>();
				for (int i = 0; i < markers.length; i++) {
					IMarker marker = markers[i];
					int location = ((Integer) marker.getAttribute(IMarker.LINE_NUMBER)).intValue();
					String error = (String) marker.getAttribute(IMarker.MESSAGE);
					int sev = ((Integer) marker.getAttribute(IMarker.SEVERITY)).intValue();
					if (location == lineNumber &&
							errorDesc.equals(error) &&
							sev == severity) {
						exactMarkers.add(marker);
					}
				}
				if (exactMarkers.size() > 0) {
					workspace.deleteMarkers(exactMarkers.toArray(new IMarker[exactMarkers.size()]));
				}
			}
		}
		catch (CoreException e) {
			MakeCorePlugin.log(e.getStatus());
		}
	}

	int mapMarkerSeverity(int severity) {
		switch (severity) {
			case SEVERITY_ERROR_BUILD :
			case SEVERITY_ERROR_RESOURCE :
				return IMarker.SEVERITY_ERROR;
			case SEVERITY_INFO :
				return IMarker.SEVERITY_INFO;
			case SEVERITY_WARNING :
				return IMarker.SEVERITY_WARNING;
		}
		return IMarker.SEVERITY_ERROR;
	}

}
