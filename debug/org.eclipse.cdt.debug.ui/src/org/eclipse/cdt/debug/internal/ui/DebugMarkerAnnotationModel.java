/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui; 

import java.io.File;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
 
public class DebugMarkerAnnotationModel extends AbstractMarkerAnnotationModel implements IBreakpointsListener {

	private File fFile;

	public DebugMarkerAnnotationModel( File file ) {
		super();
		fFile = file;
	}

	protected IMarker[] retrieveMarkers() throws CoreException {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints( CDIDebugModel.getPluginIdentifier() );
		IMarker[] markers = new IMarker[breakpoints.length];
		for ( int i = 0; i < markers.length; ++i ) {
			markers[i] = breakpoints[i].getMarker();
		}
		return markers;
	}

	protected void deleteMarkers( IMarker[] markers ) throws CoreException {
	}

	protected void listenToMarkerChanges( boolean listen ) {
		if ( listen )
			DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener( this );
		else
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener( this );
	}

	protected boolean isAcceptable( IMarker marker ) {
		IBreakpoint b = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint( marker );
		if ( b != null ) {
			return isAcceptable( b );
		}
		return false;
	}

	protected File getFile() {
		return fFile;
	}

	public void breakpointsAdded( IBreakpoint[] breakpoints ) {
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( isAcceptable( breakpoints[i] ) ) {
				addMarkerAnnotation( breakpoints[i].getMarker() );
				fireModelChanged();
			}
		}
	}

	public void breakpointsRemoved( IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( isAcceptable( breakpoints[i] ) ) {
				removeMarkerAnnotation( breakpoints[i].getMarker() );
				fireModelChanged();
			}
		}
	}

	public void breakpointsChanged( IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( isAcceptable( breakpoints[i] ) ) {
				modifyMarkerAnnotation( breakpoints[i].getMarker() );
				fireModelChanged();
			}
		}
	}

	private boolean isAcceptable( IBreakpoint b ) {
		if ( b instanceof ICBreakpoint ) {
			try {
				String handle = ((ICBreakpoint)b).getSourceHandle();
				File file = new File( handle );
				return file.equals( getFile() );
			}
			catch( CoreException e ) {
			}
		}
		return false;
	}
}
