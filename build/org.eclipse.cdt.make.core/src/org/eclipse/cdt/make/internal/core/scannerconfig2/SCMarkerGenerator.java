/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;

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
    public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
        try {
            IMarker[] cur = file.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ONE);
            /*
             * Try to find matching markers and don't put in duplicates
             */
            if ((cur != null) && (cur.length > 0)) {
                for (int i = 0; i < cur.length; i++) {
                    int line = ((Integer) cur[i].getAttribute(IMarker.LOCATION)).intValue();
                    int sev = ((Integer) cur[i].getAttribute(IMarker.SEVERITY)).intValue();
                    String mesg = (String) cur[i].getAttribute(IMarker.MESSAGE);
                    if (line == lineNumber && sev == mapMarkerSeverity(severity) && mesg.equals(errorDesc)) {
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

    public void removeMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
        IWorkspace workspace = file.getWorkspace();
        // remove specific marker
        try {
            IMarker[] markers = file.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ONE);
            if (markers != null) {
                List exactMarkers = new ArrayList();
                for (int i = 0; i < markers.length; i++) {
                    IMarker marker = markers[i];
                    int location = ((Integer) marker.getAttribute(IMarker.LOCATION)).intValue();
                    String error = (String) marker.getAttribute(IMarker.MESSAGE);
                    int sev = ((Integer) marker.getAttribute(IMarker.SEVERITY)).intValue();
                    if (location == lineNumber &&
                            errorDesc.equals(error) &&
                            sev == severity) {
                        exactMarkers.add(marker);
                    }
                }
                if (exactMarkers.size() > 0) {
                    workspace.deleteMarkers((IMarker[]) exactMarkers.toArray(new IMarker[exactMarkers.size()]));
                }
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
