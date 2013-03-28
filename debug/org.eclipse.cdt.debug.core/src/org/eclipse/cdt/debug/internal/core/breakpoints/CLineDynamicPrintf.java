/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDynamicPrintf;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint2;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.icu.text.MessageFormat;

/**
 * A DynamicPrintf that prints a string when a particular line of code is reached.
 */
public class CLineDynamicPrintf extends AbstractDynamicPrintf implements ICDynamicPrintf, ICLineBreakpoint2 {

	public CLineDynamicPrintf() {
	}

	public CLineDynamicPrintf( IResource resource, Map<String, Object> attributes, boolean add ) throws CoreException {
		super( resource, attributes, add );
	}

	@Override
	public String getMarkerType() {
	    return C_LINE_DYNAMICPRINTF_MARKER;
	}
	
    @Override
    public synchronized int decrementInstallCount() throws CoreException {
        int count = super.decrementInstallCount();
        if (count == 0) {
            resetInstalledLocation();
        }
        return count;
    }

	/*(non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	@Override
	protected String getMarkerMessage() throws CoreException {
		return MessageFormat.format( BreakpointMessages.getString( "CLineDynamicPrintf.0" ), (Object[])new String[] { CDebugUtils.getBreakpointText( this, false ) } ); //$NON-NLS-1$
	}
	
    @Override
    public int getRequestedLine() throws CoreException {
        return ensureMarker().getAttribute( ICLineBreakpoint2.REQUESTED_LINE, -1 );
    }

    @Override
    public void setRequestedLine(int line) throws CoreException {
        setAttribute( ICLineBreakpoint2.REQUESTED_LINE, line );
    }

    @Override
    public int getRequestedCharStart() throws CoreException {
        return ensureMarker().getAttribute( ICLineBreakpoint2.REQUESTED_CHAR_START, -1 );
    }

    @Override
    public void setRequestedCharStart(int charStart) throws CoreException {
        setAttribute( ICLineBreakpoint2.REQUESTED_CHAR_START, charStart );
    }

    @Override
    public int getRequestedCharEnd() throws CoreException {
        return ensureMarker().getAttribute( ICLineBreakpoint2.REQUESTED_CHAR_END, -1 );
    }

    @Override
    public void setRequestedCharEnd(int charEnd) throws CoreException {
        setAttribute( ICLineBreakpoint2.REQUESTED_CHAR_END, charEnd );
    }

    @Override
    public String getRequestedSourceHandle() throws CoreException {
        return ensureMarker().getAttribute( ICLineBreakpoint2.REQUESTED_SOURCE_HANDLE, "" ); //$NON-NLS-1$
    }

    @Override
    public void setRequestedSourceHandle(String fileName) throws CoreException {
        setAttribute( ICLineBreakpoint2.REQUESTED_SOURCE_HANDLE, fileName );
    }

    @Override
    public void setInstalledLineNumber(int line) throws CoreException {
        int existingValue = ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
        if (line != existingValue) {
            setAttribute(IMarker.LINE_NUMBER, line);
            setAttribute( IMarker.MESSAGE, getMarkerMessage() );
        }
    }
    
    @Override
    public void setInstalledCharStart(int charStart) throws CoreException {
        int existingValue = ensureMarker().getAttribute(IMarker.CHAR_START, -1);
        if (charStart != existingValue) {
            setAttribute(IMarker.CHAR_START, charStart);
            setAttribute( IMarker.MESSAGE, getMarkerMessage() );
        }
    }
    
    @Override
    public void setInstalledCharEnd(int charEnd) throws CoreException {
        int existingValue = ensureMarker().getAttribute(IMarker.CHAR_END, -1);
        if (charEnd != existingValue) {
            setAttribute(IMarker.CHAR_END, charEnd);
            setAttribute( IMarker.MESSAGE, getMarkerMessage() );
        }
    }
    
    @Override
    public void resetInstalledLocation() throws CoreException {
        boolean locationReset = false;
        if (this.getMarker().getAttribute(REQUESTED_LINE) != null) {
            int line = this.getMarker().getAttribute(REQUESTED_LINE, -1);
            setAttribute(IMarker.LINE_NUMBER, line);
            locationReset = true;
        } 
        if (this.getMarker().getAttribute(REQUESTED_CHAR_START) != null) {
            int charStart = this.getMarker().getAttribute(REQUESTED_CHAR_START, -1);
            setAttribute(IMarker.CHAR_START, charStart);
            locationReset = true;
        } 
        if (this.getMarker().getAttribute(REQUESTED_CHAR_END) != null) {
            int charEnd = this.getMarker().getAttribute(REQUESTED_CHAR_END, -1);
            setAttribute(IMarker.CHAR_END, charEnd);
            locationReset = true;
        } 
        if (this.getMarker().getAttribute(REQUESTED_SOURCE_HANDLE) != null) {
            String file = this.getMarker().getAttribute(REQUESTED_SOURCE_HANDLE, ""); //$NON-NLS-1$
            setAttribute(ICBreakpoint.SOURCE_HANDLE, file);
            locationReset = true;
        }
        if (locationReset) {
            setAttribute( IMarker.MESSAGE, getMarkerMessage() );
        }        
    }

    @Override
    public void refreshMessage() throws CoreException {
        IMarker marker = ensureMarker();
        marker.setAttribute(IMarker.MESSAGE, getMarkerMessage());
    }
}
