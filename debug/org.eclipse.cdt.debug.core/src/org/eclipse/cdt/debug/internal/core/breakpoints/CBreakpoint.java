/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICBreakpoint;
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

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 21, 2002
 */
public abstract class CBreakpoint extends Breakpoint
								  implements ICBreakpoint, 
								  			 IDebugEventSetListener
{
	/**
	 * Constructor for CBreakpoint.
	 */
	public CBreakpoint()
	{
	}

	/**
	 * Constructor for CBreakpoint.
	 */
	public CBreakpoint( final IResource resource, final String markerType, final Map attributes, final boolean add ) throws DebugException
	{
		IWorkspaceRunnable wr= new IWorkspaceRunnable() 
									{
										public void run( IProgressMonitor monitor ) throws CoreException 
										{
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IBreakpoint#getModelIdentifier()
	 */
	public String getModelIdentifier()
	{
		return CDebugModel.getPluginIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#isInstalled()
	 */
	public boolean isInstalled() throws CoreException
	{
		return ensureMarker().getAttribute( INSTALL_COUNT, 0 ) > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#getCondition()
	 */
	public String getCondition() throws CoreException
	{
		return ensureMarker().getAttribute( CONDITION, "" );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#setCondition(String)
	 */
	public void setCondition( String condition ) throws CoreException
	{
		setAttribute( CONDITION, condition );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#getIgnoreCount()
	 */
	public int getIgnoreCount() throws CoreException
	{
		return ensureMarker().getAttribute( IGNORE_COUNT, 0 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#setIgnoreCount(int)
	 */
	public void setIgnoreCount( int ignoreCount ) throws CoreException
	{
		setAttribute( IGNORE_COUNT, ignoreCount );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#getThreadId()
	 */
	public String getThreadId() throws CoreException
	{
		return ensureMarker().getAttribute( THREAD_ID, null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#setThreadId(String)
	 */
	public void setThreadId( String threadId ) throws CoreException
	{
		setAttribute( THREAD_ID, threadId );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents( DebugEvent[] events )
	{
	}

	/**
	 * Execute the given workspace runnable
	 */
	protected void run( IWorkspaceRunnable wr ) throws DebugException
	{
		try
		{
			ResourcesPlugin.getWorkspace().run( wr, null );
		}
		catch ( CoreException e )
		{
			throw new DebugException( e.getStatus() );
		}
	}

	/**
	 * Add this breakpoint to the breakpoint manager,
	 * or sets it as unregistered.
	 */
	protected void register( boolean register ) throws CoreException
	{
		if ( register )
		{
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint( this );
		}
		else
		{
			setRegistered( false );
		}
	}

	protected String getMarkerMessage() throws CoreException
	{
		return null;
	}

	/**
	 * Increments the install count of this breakpoint
	 */
	public void incrementInstallCount() throws CoreException
	{
		int count = getInstallCount();
		setAttribute( INSTALL_COUNT, count + 1 );
	}

	/**
	 * Returns the <code>INSTALL_COUNT</code> attribute of this breakpoint
	 * or 0 if the attribute is not set.
	 */
	public int getInstallCount() throws CoreException
	{
		return ensureMarker().getAttribute( INSTALL_COUNT, 0 );
	}

	/**
	 * Decrements the install count of this breakpoint.
	 */
	public void decrementInstallCount() throws CoreException
	{
		int count = getInstallCount();
		if ( count > 0 )
		{
			setAttribute( INSTALL_COUNT, count - 1 );
		}
	}
}
