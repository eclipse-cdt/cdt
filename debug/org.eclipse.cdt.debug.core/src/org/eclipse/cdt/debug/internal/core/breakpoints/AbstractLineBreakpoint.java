/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint2;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Base class for different types of location breakponts.
 */
public abstract class AbstractLineBreakpoint extends CBreakpoint implements ICLineBreakpoint2 {

	/**
	 * Constructor for AbstractLineBreakpoint.
	 */
	public AbstractLineBreakpoint() {
		super();
	}

	/**
	 * Constructor for AbstractLineBreakpoint.
	 *
	 * @param resource
	 * @param markerType
	 * @param attributes
	 * @param add
	 * @throws CoreException
	 */
	public AbstractLineBreakpoint( IResource resource, Map<String, Object> attributes, boolean add ) throws CoreException {
		super( resource, attributes, add );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getLineNumber()
	 */
	@Override
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute( IMarker.LINE_NUMBER, -1 );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharStart()
	 */
	@Override
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_START, -1 );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharEnd()
	 */
	@Override
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_END, -1 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#getAddress()
	 */
	@Override
	public String getAddress() throws CoreException {
		return ensureMarker().getAttribute( ICLineBreakpoint.ADDRESS, "" ); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#getFileName()
	 */
	@Override
	public String getFileName() throws CoreException {
		String fileName = ensureMarker().getAttribute( ICBreakpoint.SOURCE_HANDLE, "" ); //$NON-NLS-1$
		IPath path = new Path( fileName );
		return ( path.isValidPath( fileName ) ) ? path.lastSegment() : null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#getFunction()
	 */
	@Override
	public String getFunction() throws CoreException {
		return ensureMarker().getAttribute( ICLineBreakpoint.FUNCTION, "" ); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#setAddress(java.lang.String)
	 */
	@Override
	public void setAddress( String address ) throws CoreException {
		setAttribute( ICLineBreakpoint.ADDRESS, address );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#setFunction(java.lang.String)
	 */
	@Override
	public void setFunction( String function ) throws CoreException {
		setAttribute( ICLineBreakpoint.FUNCTION, function );
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
    public synchronized int decrementInstallCount() throws CoreException {
        int count = super.decrementInstallCount();
        if (count == 0) {
            resetInstalledLocation();
        }
        return count;
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
