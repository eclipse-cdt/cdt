/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core;

import java.util.HashMap;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;

/**
 * Enter type comment.
 * 
 * @since Nov 3, 2003
 */
public class CBreakpointManager implements ICDIEventListener, IAdaptable
{
	public class BreakpointMap
	{
		/**
		 * Maps CBreakpoints to CDI breakpoints.
		 */
		private HashMap fCBreakpoints;

		/**
		 * Maps CDI breakpoints to CBreakpoints.
		 */
		private HashMap fCDIBreakpoints;

		protected BreakpointMap()
		{
			fCBreakpoints = new HashMap( 10 );
			fCDIBreakpoints = new HashMap( 20 );
		}

		protected synchronized void put( ICBreakpoint breakpoint, ICDIBreakpoint[] cdiBreakpoints )
		{
		}

		protected synchronized ICDIBreakpoint[] getCDIBreakpoints( ICBreakpoint breakpoint )
		{
			return null;
		}

		protected synchronized ICBreakpoint getCBreakpoint( ICDIBreakpoint cdiBreakpoint )
		{
			return null;
		}

		protected void removeCBreakpoint( ICBreakpoint breakpoint )
		{
		}

		protected void removeCDIBreakpoint( ICBreakpoint breakpoin, ICDIBreakpoint cdiBreakpoint )
		{
		}

		protected ICBreakpoint[] getAllCBreakpoints()
		{
			return null;
		}

		protected ICDIBreakpoint[] getAllCDIBreakpoints()
		{
			return null;
		}

		protected void dispose()
		{
		}
	}
	
	private CDebugTarget fDebugTarget;
	private BreakpointMap fMap;

	public CBreakpointManager( CDebugTarget target )
	{
		super();
		setDebugTarget( target );
		fMap = new BreakpointMap();
		getDebugTarget().getCDISession().getEventManager().addEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( CBreakpointManager.class.equals( adapter ) )
			return this;
		if ( CDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		if ( ICDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		if ( IDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		return null;
	}

	public CDebugTarget getDebugTarget()
	{
		return fDebugTarget;
	}

	private void setDebugTarget( CDebugTarget target )
	{
		fDebugTarget = target;
	}

	protected ICDIBreakpointManager getCDIBreakpointManager()
	{
		return getDebugTarget().getCDISession().getBreakpointManager();
	}

	protected ICSourceLocator getCSourceLocator()
	{
		ISourceLocator locator = getDebugTarget().getLaunch().getSourceLocator();
		if ( locator instanceof IAdaptable ) 
			return (ICSourceLocator)((IAdaptable)locator).getAdapter( ICSourceLocator.class );
		return null;
	}

	public void dispose()
	{
		getDebugTarget().getCDISession().getEventManager().removeEventListener( this );
		removeAllBreakpoints();
		fMap.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvent(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent)
	 */
	public void handleDebugEvent( ICDIEvent event )
	{
		ICDIObject source = event.getSource();
		if ( source != null && source.getTarget().equals( getDebugTarget().getCDITarget() ) )
		{
			if ( event instanceof ICDICreatedEvent )
			{
				if ( source instanceof ICDILocationBreakpoint )
					handleLocationBreakpointCreatedEvent( (ICDILocationBreakpoint)source );
				else if ( source instanceof ICDIWatchpoint )
					handleWatchpointCreatedEvent( (ICDIWatchpoint)source );
			}
		}
		else if ( event instanceof ICDIDestroyedEvent )
		{
			if ( source instanceof ICDIBreakpoint )
				handleBreakpointDestroyedEvent( (ICDIBreakpoint)source );
		}
		else if ( event instanceof ICDIChangedEvent )
		{
			if ( source instanceof ICDIBreakpoint )
				handleBreakpointChangedEvent( (ICDIBreakpoint)source );
		}
	}

	public void setBreakpoint( ICBreakpoint breakpoint, boolean defer ) throws DebugException
	{
	}

	public void removeBreakpoint( ICBreakpoint breakpoint ) throws DebugException
	{
		ICDIBreakpoint[] cdiBreakpoints = fMap.getCDIBreakpoints( breakpoint );
		if ( cdiBreakpoints.length == 0 )
			return;

		MultiStatus ms = new MultiStatus( CDebugCorePlugin.getUniqueIdentifier(),
										  DebugException.TARGET_REQUEST_FAILED, 
										  "Delete breakpoints failed.",
										  null );
		ICDIBreakpointManager bm = getCDIBreakpointManager();
		for ( int i = 0; i < cdiBreakpoints.length; ++i )
		{
			try
			{
				bm.deleteBreakpoints( new ICDIBreakpoint[] { cdiBreakpoints[i] } );
			}
			catch( CDIException e )
			{
				IStatus status = new Status( IStatus.ERROR, 
											 CDebugCorePlugin.getUniqueIdentifier(), 
											 DebugException.TARGET_REQUEST_FAILED, 
											 e.getMessage(),
											 e );
				ms.addAll( status );
			}
		}
		if ( ms.getSeverity() > IStatus.OK )
			throw new DebugException( ms );
	}

	public void changeBreakpointProperties( ICBreakpoint breakpoint, IMarkerDelta delta ) throws DebugException
	{
	}

	private void handleLocationBreakpointCreatedEvent( ICDILocationBreakpoint cdiBreakpoint )
	{
	}

	private void handleWatchpointCreatedEvent( ICDIWatchpoint cdiWatchpoint )
	{
	}

	private void handleBreakpointDestroyedEvent( ICDIBreakpoint cdiBreakpoint )
	{
		ICBreakpoint breakpoint = fMap.getCBreakpoint( cdiBreakpoint );
		if ( breakpoint != null )
		{
			fMap.removeCDIBreakpoint( breakpoint, cdiBreakpoint );
			try
			{
				((CBreakpoint)breakpoint).decrementInstallCount();
			}
			catch( CoreException e )
			{
				CDebugCorePlugin.log( e.getStatus() );
			}
		}
	}

	private void handleBreakpointChangedEvent( ICDIBreakpoint cdiBreakpoint )
	{
	}

	private void removeAllBreakpoints()
	{
		ICDIBreakpoint[] cdiBreakpoints = fMap.getAllCDIBreakpoints();
		ICDIBreakpointManager bm = getCDIBreakpointManager();
		if ( cdiBreakpoints.length > 0 )
		{
			try
			{
				bm.deleteBreakpoints( cdiBreakpoints );
			}
			catch( CDIException e )
			{
				CDebugCorePlugin.log( e.getMessage() );
			}
			ICBreakpoint[] breakpoints = fMap.getAllCBreakpoints();
			for ( int i = 0; i < breakpoints.length; ++i )
			{
				try
				{
					((CBreakpoint)breakpoints[i]).decrementInstallCount();
				}
				catch( CoreException e )
				{
					CDebugCorePlugin.log( e.getMessage() );
				}
			}
		}
	}
}
