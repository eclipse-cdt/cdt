package org.eclipse.cdt.core.resources;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

public abstract class ACBuilder extends IncrementalProjectBuilder implements IMarkerGenerator {

	/**
	 * Constructor for ACBuilder
	 */
	public ACBuilder() {
		super();
	}

	/*
	 * callback from Output Parser
	 */
	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {

		try {
			IMarker[] cur = file.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ONE);
			/*
			 * Try to find matching markers and don't put in duplicates
			 */
			if ((cur != null) && (cur.length > 0)) {
				for (int i = 0; i < cur.length; i++) {
					if ((((Integer) cur[i].getAttribute(IMarker.LOCATION)).intValue() == lineNumber)
						&& (((Integer) cur[i].getAttribute(IMarker.SEVERITY)).intValue() == severity)
						&& (((String) cur[i].getAttribute(IMarker.MESSAGE)).equals(errorDesc))) {
						return;
					}
				}
			}

			IMarker marker = file.createMarker(ICModelMarker.C_MODEL_PROBLEM_MARKER);
			marker.setAttribute(IMarker.LOCATION, lineNumber);
			marker.setAttribute(IMarker.MESSAGE, errorDesc);
			marker.setAttribute(IMarker.SEVERITY, mapMarkerSeverity(severity));
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.CHAR_START, -1);
			marker.setAttribute(IMarker.CHAR_END, -1);
			if (errorVar != null) {
				marker.setAttribute(ICModelMarker.C_MODEL_MARKER_VARIABLE, errorVar);
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
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
