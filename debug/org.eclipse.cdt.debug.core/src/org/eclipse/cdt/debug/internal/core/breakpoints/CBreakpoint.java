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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * The base class for all C/C++ specific breakpoints.
 */
public abstract class CBreakpoint extends Breakpoint implements ICBreakpoint, IDebugEventSetListener {

	private Map fFilteredThreadsByTarget;

	/**
	 * Constructor for CBreakpoint.
	 */
	public CBreakpoint() {
		fFilteredThreadsByTarget = new HashMap( 10 );
	}

	/**
	 * Constructor for CBreakpoint.
	 */
	public CBreakpoint( final IResource resource, final String markerType, final Map attributes, final boolean add ) throws CoreException {
		this();
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {

			public void run( IProgressMonitor monitor ) throws CoreException {
				// create the marker
				setMarker( resource.createMarker( markerType ) );
				// set attributes
				ensureMarker().setAttributes( attributes );
				//set the marker message
				setAttribute( IMarker.MESSAGE, getMarkerMessage() );
				// add to breakpoint manager if requested
				register( add );
			}
		};
		run( wr );
	}

	public void createMarker( final IResource resource, final String markerType, final Map attributes, final boolean add ) throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run( IProgressMonitor monitor ) throws CoreException {
				// create the marker
				setMarker( resource.createMarker( markerType ) );
				// set attributes
				ensureMarker().setAttributes( attributes );
				//set the marker message
				setAttribute( IMarker.MESSAGE, getMarkerMessage() );
				// add to breakpoint manager if requested
				register( add );
			}
		};
		run( wr );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IBreakpoint#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return CDIDebugModel.getPluginIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#isInstalled()
	 */
	public boolean isInstalled() throws CoreException {
		return ensureMarker().getAttribute( INSTALL_COUNT, 0 ) > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#getCondition()
	 */
	public String getCondition() throws CoreException {
		return ensureMarker().getAttribute( CONDITION, "" ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#setCondition(String)
	 */
	public void setCondition( String condition ) throws CoreException {
		setAttribute( CONDITION, condition );
		setAttribute( IMarker.MESSAGE, getMarkerMessage() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#getIgnoreCount()
	 */
	public int getIgnoreCount() throws CoreException {
		return ensureMarker().getAttribute( IGNORE_COUNT, 0 );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#setIgnoreCount(int)
	 */
	public void setIgnoreCount( int ignoreCount ) throws CoreException {
		setAttribute( IGNORE_COUNT, ignoreCount );
		setAttribute( IMarker.MESSAGE, getMarkerMessage() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#getThreadId()
	 */
	public String getThreadId() throws CoreException {
		return ensureMarker().getAttribute( THREAD_ID, null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#setThreadId(String)
	 */
	public void setThreadId( String threadId ) throws CoreException {
		setAttribute( THREAD_ID, threadId );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#getSourceHandle()
	 */
	public String getSourceHandle() throws CoreException {
		return ensureMarker().getAttribute( SOURCE_HANDLE, null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#setSourceHandle(java.lang.String)
	 */
	public void setSourceHandle( String sourceHandle ) throws CoreException {
		setAttribute( SOURCE_HANDLE, sourceHandle );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents( DebugEvent[] events ) {
	}

	/**
	 * Execute the given workspace runnable
	 */
	protected void run( IWorkspaceRunnable wr ) throws DebugException {
		try {
			ResourcesPlugin.getWorkspace().run( wr, null );
		}
		catch( CoreException e ) {
			throw new DebugException( e.getStatus() );
		}
	}

	/**
	 * Add this breakpoint to the breakpoint manager, or sets it as
	 * unregistered.
	 */
	public void register( boolean register ) throws CoreException {
		if ( register ) {
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint( this );
		}
		/*
		 * else { setRegistered( false ); }
		 */
	}

	abstract protected String getMarkerMessage() throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#resetInstallCount()
	 */
	public synchronized void resetInstallCount() throws CoreException {
		setAttribute( INSTALL_COUNT, 0 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#incrementInstallCount()
	 */
	public synchronized int incrementInstallCount() throws CoreException {
		int count = getInstallCount();
		setAttribute( INSTALL_COUNT, ++count );
		return count;
	}

	/**
	 * Returns the <code>INSTALL_COUNT</code> attribute of this breakpoint or
	 * 0 if the attribute is not set.
	 */
	public int getInstallCount() throws CoreException {
		return ensureMarker().getAttribute( INSTALL_COUNT, 0 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#decrementInstallCount()
	 */
	public synchronized int decrementInstallCount() throws CoreException {
		int count = getInstallCount();
		if ( count > 0 ) {
			setAttribute( INSTALL_COUNT, --count );
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.Breakpoint#ensureMarker()
	 */
	protected IMarker ensureMarker() throws DebugException {
		return super.ensureMarker();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.Breakpoint#setAttribute(String, Object)
	 */
	protected void setAttribute( String attributeName, Object value ) throws CoreException {
		super.setAttribute( attributeName, value );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#isConditional()
	 */
	public boolean isConditional() throws CoreException {
		return ((getCondition() != null && getCondition().trim().length() > 0) || getIgnoreCount() > 0);
	}

	protected String getConditionText() throws CoreException {
		StringBuffer sb = new StringBuffer();
		int ignoreCount = getIgnoreCount();
		if ( ignoreCount > 0 ) {
			sb.append( MessageFormat.format( BreakpointMessages.getString( "CBreakpoint.1" ), new Integer[] { new Integer( ignoreCount ) } ) ); //$NON-NLS-1$
		}
		String condition = getCondition();
		if ( condition != null && condition.length() > 0 ) {
			sb.append( MessageFormat.format( BreakpointMessages.getString( "CBreakpoint.2" ), new String[] { condition } ) ); //$NON-NLS-1$
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#getTargetFilters()
	 */
	public ICDebugTarget[] getTargetFilters() throws CoreException {
		Set set = fFilteredThreadsByTarget.keySet();
		return (ICDebugTarget[])set.toArray( new ICDebugTarget[set.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#getThreadFilters(org.eclipse.cdt.debug.core.model.ICDebugTarget)
	 */
	public ICThread[] getThreadFilters( ICDebugTarget target ) throws CoreException {
		Set set = (Set)fFilteredThreadsByTarget.get( target );
		return ( set != null ) ? (ICThread[])set.toArray( new ICThread[set.size()] ) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#removeTargetFilter(org.eclipse.cdt.debug.core.model.ICDebugTarget)
	 */
	public void removeTargetFilter( ICDebugTarget target ) throws CoreException {
		if ( fFilteredThreadsByTarget.containsKey( target ) ) {
			fFilteredThreadsByTarget.remove( target );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#removeThreadFilters(org.eclipse.cdt.debug.core.model.ICThread[])
	 */
	public void removeThreadFilters( ICThread[] threads ) throws CoreException {
		if ( threads != null && threads.length > 0 ) {
			IDebugTarget target = threads[0].getDebugTarget();
			if ( fFilteredThreadsByTarget.containsKey( target ) ) {
				Set set = (Set)fFilteredThreadsByTarget.get( target );
				if ( set != null ) {
					set.removeAll( Arrays.asList( threads ) );
					if ( set.isEmpty() ) {
						fFilteredThreadsByTarget.remove( target );
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#setTargetFilter(org.eclipse.cdt.debug.core.model.ICDebugTarget)
	 */
	public void setTargetFilter( ICDebugTarget target ) throws CoreException {
		fFilteredThreadsByTarget.put( target, null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#setThreadFilters(org.eclipse.cdt.debug.core.model.ICThread[])
	 */
	public void setThreadFilters( ICThread[] threads ) throws CoreException {
		if ( threads != null && threads.length > 0 ) {
			fFilteredThreadsByTarget.put( threads[0].getDebugTarget(), new HashSet( Arrays.asList( threads ) ) );
		}
	}

	/**
	 * Change notification when there are no marker changes. If the marker
	 * does not exist, do not fire a change notificaiton (the marker may not
	 * exist if the associated project was closed).
	 */
	public void fireChanged() {
		if ( markerExists() ) {
			DebugPlugin.getDefault().getBreakpointManager().fireBreakpointChanged( this );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#getModule()
	 */
	public String getModule() throws CoreException {
		return ensureMarker().getAttribute( MODULE, null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICBreakpoint#setModule(java.lang.String)
	 */
	public void setModule( String module ) throws CoreException {
		setAttribute( MODULE, module );
	}
}
