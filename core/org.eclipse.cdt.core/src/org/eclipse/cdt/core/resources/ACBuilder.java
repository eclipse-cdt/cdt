package org.eclipse.cdt.core.resources;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import org.eclipse.cdt.core.*;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.IMarker;

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
	//public void addMarker(IFile file, int lineNumber, String errorDesc, int severity) {
	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		try {
			IMarker marker= file.createMarker(ICModelMarker.C_MODEL_PROBLEM_MARKER);
			marker.setAttribute(IMarker.LOCATION, lineNumber);
			marker.setAttribute(IMarker.MESSAGE, errorDesc);
			marker.setAttribute(IMarker.SEVERITY, severity);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.CHAR_START, -1);
			marker.setAttribute(IMarker.CHAR_END, -1);
			if(errorVar != null) {
				marker.setAttribute(ICModelMarker.C_MODEL_MARKER_VARIABLE, errorVar);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
		}
		
	}

    public abstract IPath getWorkingDirectory();
}

