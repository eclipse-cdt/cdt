/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui; 

import java.io.File;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
 
public class DebugMarkerAnnotationModel extends AbstractMarkerAnnotationModel implements IBreakpointsListener {

	private final File fFile;

	public DebugMarkerAnnotationModel( File file ) {
		super();
		fFile = file;
	}

	@Override
	protected IMarker[] retrieveMarkers() throws CoreException {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
		IMarker[] markers = new IMarker[breakpoints.length];
		for ( int i = 0; i < markers.length; ++i ) {
			markers[i] = breakpoints[i].getMarker();
		}
		return markers;
	}

	@Override
	protected void deleteMarkers( IMarker[] markers ) throws CoreException {
	}

	@Override
	protected void listenToMarkerChanges( boolean listen ) {
		if ( listen )
			DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener( this );
		else
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener( this );
	}

	@Override
	protected boolean isAcceptable( IMarker marker ) {
		String handle = marker.getAttribute(ICBreakpoint.SOURCE_HANDLE, null);
		if (handle != null) {
			File file = new File( handle );
			return file.equals( getFile() );
		}
		return false;
	}

	protected File getFile() {
		return fFile;
	}

	@Override
	public void breakpointsAdded( IBreakpoint[] breakpoints ) {
		for ( int i = 0; i < breakpoints.length; ++i ) {
			addMarkerAnnotation( breakpoints[i].getMarker() );
		}
		fireModelChanged();
	}

	@Override
	public void breakpointsRemoved( IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		for ( int i = 0; i < breakpoints.length; ++i ) {
			removeMarkerAnnotation( breakpoints[i].getMarker() );
		}
		fireModelChanged();
	}

	@Override
	public void breakpointsChanged( IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		for ( int i = 0; i < breakpoints.length; ++i ) {
			modifyMarkerAnnotation( breakpoints[i].getMarker() );
		}
		fireModelChanged();
	}

}
