/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.internal.core;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;

/**
 * The breakpoint manager manages all breakpoints set to the associated 
 * debug target.
 */
public class CBreakpointManager implements IBreakpointManagerListener, ICDIEventListener, IAdaptable {

	private class BreakpointMap {

		/**
		 * Maps CBreakpoints to CDI breakpoints.
		 */
		private HashMap fCBreakpoints;

		/**
		 * Maps CDI breakpoints to CBreakpoints.
		 */
		private HashMap fCDIBreakpoints;

		protected BreakpointMap() {
			fCBreakpoints = new HashMap( 10 );
			fCDIBreakpoints = new HashMap( 10 );
		}

		protected synchronized void put( ICBreakpoint breakpoint, ICDIBreakpoint cdiBreakpoint ) {
			fCBreakpoints.put( breakpoint, cdiBreakpoint );
			fCDIBreakpoints.put( cdiBreakpoint, breakpoint );
		}

		protected synchronized ICDIBreakpoint getCDIBreakpoint( ICBreakpoint breakpoint ) {
			return (ICDIBreakpoint)fCBreakpoints.get( breakpoint );
		}

		protected synchronized ICBreakpoint getCBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
			return (ICBreakpoint)fCDIBreakpoints.get( cdiBreakpoint );
		}

		protected synchronized void removeCBreakpoint( ICBreakpoint breakpoint ) {
			if ( breakpoint != null ) {
				ICDIBreakpoint cdiBreakpoint = (ICDIBreakpoint)fCBreakpoints.remove( breakpoint );
				if ( cdiBreakpoint != null )
					fCDIBreakpoints.remove( cdiBreakpoint );
			}
		}

		protected synchronized void removeCDIBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
			if ( cdiBreakpoint != null ) {
				ICBreakpoint breakpoint = (ICBreakpoint)fCDIBreakpoints.remove( cdiBreakpoint );
				if ( breakpoint != null )
					fCBreakpoints.remove( breakpoint );
			}
		}

		protected ICBreakpoint[] getAllCBreakpoints() {
			Set set = fCBreakpoints.keySet();
			return (ICBreakpoint[])set.toArray( new ICBreakpoint[set.size()] );
		}

		protected ICDIBreakpoint[] getAllCDIBreakpoints() {
			Set set = fCDIBreakpoints.keySet();
			return (ICDIBreakpoint[])set.toArray( new ICDIBreakpoint[set.size()] );
		}

		protected void dispose() {
			fCBreakpoints.clear();
			fCDIBreakpoints.clear();
		}
	}

	private CDebugTarget fDebugTarget;

	private BreakpointMap fMap;
	
	private boolean fSkipBreakpoint= false;

	public CBreakpointManager( CDebugTarget target ) {
		super();
		setDebugTarget( target );
		fMap = new BreakpointMap();
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointManagerListener( this );
		getDebugTarget().getCDISession().getEventManager().addEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
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

	public CDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	private void setDebugTarget( CDebugTarget target ) {
		fDebugTarget = target;
	}

	protected ICDITarget getCDITarget() {
		return getDebugTarget().getCDITarget();
	}

	protected ICSourceLocator getCSourceLocator() {
		ISourceLocator locator = getDebugTarget().getLaunch().getSourceLocator();
		if ( locator instanceof IAdaptable )
			return (ICSourceLocator)((IAdaptable)locator).getAdapter( ICSourceLocator.class );
		return null;
	}

	public void dispose() {
		getDebugTarget().getCDISession().getEventManager().removeEventListener( this );
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointManagerListener( this );
		removeAllBreakpoints();
		getBreakpointMap().dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent)
	 */
	public void handleDebugEvents( ICDIEvent[] events ) {
		for( int i = 0; i < events.length; i++ ) {
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();
			if ( source != null && source.getTarget().equals( getDebugTarget().getCDITarget() ) ) {
				if ( event instanceof ICDICreatedEvent ) {
					if ( source instanceof ICDIBreakpoint )
						handleBreakpointCreatedEvent( (ICDIBreakpoint)source );
				}
				else if ( event instanceof ICDIDestroyedEvent ) {
					if ( source instanceof ICDIBreakpoint )
						handleBreakpointDestroyedEvent( (ICDIBreakpoint)source );
				}
				else if ( event instanceof ICDIChangedEvent ) {
					if ( source instanceof ICDIBreakpoint )
						handleBreakpointChangedEvent( (ICDIBreakpoint)source );
				}
			}
		}
	}

	public boolean isTargetBreakpoint( ICBreakpoint breakpoint ) {
		// Problem: gdb doesn't accept breakpoint if the file is specified by full path (depends on the current directory).
		// This prevents us from using gdb as a breakpoint filter. The case when two unrelated projects contain files 
		// with the same name will cause problems.
		// Current solution: the source locator is used as a breakpoint filter.
		IResource resource = breakpoint.getMarker().getResource();
		if ( breakpoint instanceof ICAddressBreakpoint )
			return supportsAddressBreakpoint( (ICAddressBreakpoint)breakpoint );
		if ( breakpoint instanceof ICLineBreakpoint ) {
			try {
				String handle = breakpoint.getSourceHandle();
				ICSourceLocator sl = getSourceLocator();
				if ( sl != null )
					return ( sl.findSourceElement( handle ) != null );
			}
			catch( CoreException e ) {
				return false;
			}
		}
		else {
			IProject project = resource.getProject();
			if ( project != null && project.exists() ) {
				ICSourceLocator sl = getSourceLocator();
				if ( sl != null )
					return sl.contains( project );
				if ( project.equals( getProject() ) )
					return true;
				return CDebugUtils.isReferencedProject( getProject(), project );
			}
		}
		return true;
	}

	public boolean isCDIRegistered( ICBreakpoint breakpoint ) {
		return (getBreakpointMap().getCDIBreakpoint( breakpoint ) != null);
	}

	public boolean supportsAddressBreakpoint( ICAddressBreakpoint breakpoint ) {
		try {
			return ( getExecFilePath().toOSString().equals( breakpoint.getSourceHandle() ) );
		}
		catch( CoreException e ) {
		}
		return false;
	}

	public IFile getCDIBreakpointFile( ICDIBreakpoint cdiBreakpoint ) {
		IBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		if ( breakpoint instanceof ICLineBreakpoint && !(breakpoint instanceof ICAddressBreakpoint) ) {
			IResource resource = ((ICLineBreakpoint)breakpoint).getMarker().getResource();
			if ( resource instanceof IFile )
				return (IFile)resource;
		}
		return null;
	}

	public ICBreakpoint getBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
		return getBreakpointMap().getCBreakpoint( cdiBreakpoint );
	}

	public IAddress getBreakpointAddress( ICBreakpoint breakpoint ) {
		if ( breakpoint != null ) {
			ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
			if ( cdiBreakpoint instanceof ICDILocationBreakpoint ) {
				try {
					ICDILocation location = ((ICDILocationBreakpoint)cdiBreakpoint).getLocation();
					if ( location != null ) {
						IAddressFactory factory = getDebugTarget().getAddressFactory();
						return factory.createAddress( location.getAddress() );
					}	
				}
				catch( CDIException e ) {
				}
			}
		}
		return fDebugTarget.getAddressFactory().getZero();
	}

	public void setBreakpoint( ICBreakpoint breakpoint ) throws DebugException {
		doSetBreakpoint( breakpoint );
	}

	protected void doSetBreakpoint( ICBreakpoint breakpoint ) throws DebugException {
		try {
			ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
			if ( cdiBreakpoint == null ) {
				if ( breakpoint instanceof ICFunctionBreakpoint )
					setFunctionBreakpoint( (ICFunctionBreakpoint)breakpoint );
				else if ( breakpoint instanceof ICAddressBreakpoint )
					setAddressBreakpoint( (ICAddressBreakpoint)breakpoint );
				else if ( breakpoint instanceof ICLineBreakpoint )
					setLineBreakpoint( (ICLineBreakpoint)breakpoint );
				else if ( breakpoint instanceof ICWatchpoint )
					setWatchpoint( (ICWatchpoint)breakpoint );
			}
		}
		catch( CoreException e ) {
			requestFailed( MessageFormat.format( InternalDebugCoreMessages.getString( "CBreakpointManager.0" ), new String[] { e.getMessage() } ), e ); //$NON-NLS-1$
		}
		catch( NumberFormatException e ) {
			requestFailed( MessageFormat.format( InternalDebugCoreMessages.getString( "CBreakpointManager.1" ), new String[] { e.getMessage() } ), e ); //$NON-NLS-1$
		}
		catch( CDIException e ) {
			targetRequestFailed( MessageFormat.format( InternalDebugCoreMessages.getString( "CBreakpointManager.2" ), new String[] { e.getMessage() } ), e ); //$NON-NLS-1$
		}
	}

	public void removeBreakpoint( final ICBreakpoint breakpoint ) throws DebugException {
		doRemoveBreakpoint( breakpoint );
	}

	protected void doRemoveBreakpoint( ICBreakpoint breakpoint ) throws DebugException {
		final ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
		if ( cdiBreakpoint != null ) {
			final ICDITarget cdiTarget = getCDITarget();
			DebugPlugin.getDefault().asyncExec( new Runnable() {				
				public void run() {
					try {
						cdiTarget.deleteBreakpoints( new ICDIBreakpoint[]{ cdiBreakpoint } );
					}
					catch( CDIException e ) {
					} 
				}
			} );			
		}
	}

	public void changeBreakpointProperties( final ICBreakpoint breakpoint, final IMarkerDelta delta ) throws DebugException {
		doChangeBreakpointProperties( breakpoint, delta );
	}

	protected void doChangeBreakpointProperties( ICBreakpoint breakpoint, IMarkerDelta delta ) throws DebugException {
		final ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
		if ( cdiBreakpoint == null )
			return;
		ICDITarget cdiTarget = getCDITarget();
		try {
			final boolean enabled = breakpoint.isEnabled();
			boolean oldEnabled = ( delta != null ) ? delta.getAttribute( IBreakpoint.ENABLED, true ) : enabled;
			int ignoreCount = breakpoint.getIgnoreCount();
			int oldIgnoreCount = ( delta != null ) ? delta.getAttribute( ICBreakpoint.IGNORE_COUNT, 0 ) : ignoreCount;
			String condition = breakpoint.getCondition();
			String oldCondition = ( delta != null ) ? delta.getAttribute( ICBreakpoint.CONDITION, "" ) : condition; //$NON-NLS-1$
			String[] newThreadIs = getThreadNames( breakpoint );
			Boolean enabled0 = null;
			ICDICondition condition0 = null;
			if ( enabled != oldEnabled && enabled != cdiBreakpoint.isEnabled() ) {
				enabled0 = ( enabled ) ? Boolean.TRUE : Boolean.FALSE;
			}
			if ( ignoreCount != oldIgnoreCount || condition.compareTo( oldCondition ) != 0 || areThreadFiltersChanged( newThreadIs, cdiBreakpoint ) ) {
				final ICDICondition cdiCondition = cdiTarget.createCondition( ignoreCount, condition, newThreadIs  );
				if ( !cdiCondition.equals( cdiBreakpoint.getCondition() ) ) {
					condition0 = cdiCondition;
				}
			}
			if ( enabled0 != null || condition0 != null ) {
				changeBreakpointPropertiesOnTarget( cdiBreakpoint, enabled0, condition0 );
			}
		}
		catch( CoreException e ) {
			requestFailed( MessageFormat.format( InternalDebugCoreMessages.getString( "CBreakpointManager.4" ), new String[] { e.getMessage() } ), e ); //$NON-NLS-1$
		}
		catch( CDIException e ) {
			requestFailed( MessageFormat.format( InternalDebugCoreMessages.getString( "CBreakpointManager.4" ), new String[] { e.getMessage() } ), e ); //$NON-NLS-1$
		}
	}

	private void changeBreakpointPropertiesOnTarget( final ICDIBreakpoint breakpoint, final Boolean enabled, final ICDICondition condition ) {
		DebugPlugin.getDefault().asyncExec( new Runnable() {				
			public void run() {
				if ( enabled != null ) {
					try {
						breakpoint.setEnabled( enabled.booleanValue() );
					}
					catch( CDIException e ) {
					}
				}
				if ( condition != null ) {
					try {
						breakpoint.setCondition( condition );
					}
					catch( CDIException e ) {
					}
				}
			}
		} );			
	}

	private void handleBreakpointCreatedEvent( final ICDIBreakpoint cdiBreakpoint ) {
		if ( cdiBreakpoint instanceof ICDIWatchpoint )
			doHandleWatchpointCreatedEvent( (ICDIWatchpoint)cdiBreakpoint );
		else if ( cdiBreakpoint instanceof ICDILocationBreakpoint )
			doHandleLocationBreakpointCreatedEvent( (ICDILocationBreakpoint)cdiBreakpoint );
		if ( !cdiBreakpoint.isTemporary() && !DebugPlugin.getDefault().getBreakpointManager().isEnabled() ) {
			try {
				cdiBreakpoint.setEnabled( false );
			}
			catch( CDIException e ) {
				// ignore
			}
		}
	}

	protected void doHandleLocationBreakpointCreatedEvent( ICDILocationBreakpoint cdiBreakpoint ) {
		if ( cdiBreakpoint.isTemporary() )
			return;
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		if ( breakpoint == null ) {
			breakpoint = createLocationBreakpoint( cdiBreakpoint );
		}
		if ( breakpoint != null ) {
			try {
				breakpoint.setTargetFilter( getDebugTarget() );
			}
			catch( CoreException e ) {
			}
			getBreakpointNotifier().breakpointInstalled( getDebugTarget(), breakpoint );
		}
	}

	protected void doHandleWatchpointCreatedEvent( ICDIWatchpoint cdiWatchpoint ) {
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiWatchpoint );
		if ( breakpoint == null ) {
			try {
				breakpoint = createWatchpoint( cdiWatchpoint );
			}
			catch( CDIException e ) {
			}
			catch( CoreException e ) {
			}
		}
		if ( breakpoint != null ) {
			try {
				breakpoint.setTargetFilter( getDebugTarget() );
			}
			catch( CoreException e ) {
			}
			getBreakpointNotifier().breakpointInstalled( getDebugTarget(), breakpoint );
		}
	}

	private void handleBreakpointDestroyedEvent( final ICDIBreakpoint cdiBreakpoint ) {
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		getBreakpointMap().removeCDIBreakpoint( cdiBreakpoint );
		if ( breakpoint != null ) {
			if ( isFilteredByTarget( breakpoint, getDebugTarget() ) ) {
				try {
					breakpoint.removeTargetFilter( getDebugTarget() );
				}
				catch( CoreException e ) {
				}
			}
			getBreakpointNotifier().breakpointsRemoved( getDebugTarget(), new IBreakpoint[] { breakpoint } );
		}
	}

	private void handleBreakpointChangedEvent( final ICDIBreakpoint cdiBreakpoint ) {
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		if ( breakpoint != null ) {
			Map map = new HashMap( 3 );
			try {
				if ( !fSkipBreakpoint && DebugPlugin.getDefault().getBreakpointManager().isEnabled() ) {
						map.put( IBreakpoint.ENABLED, new Boolean( cdiBreakpoint.isEnabled() ) );
				}
				else {
					map.put( IBreakpoint.ENABLED, new Boolean( breakpoint.isEnabled() ) );
				}
			}
			catch( CDIException e ) {
			}
			catch( CoreException e ) {
			}
			try {
				map.put( ICBreakpoint.IGNORE_COUNT, new Integer( cdiBreakpoint.getCondition().getIgnoreCount() ) );
			}
			catch( CDIException e ) {
			}
			try {
				map.put( ICBreakpoint.CONDITION, cdiBreakpoint.getCondition().getExpression() );
			}
			catch( CDIException e ) {
			}
			getBreakpointNotifier().breakpointChanged( getDebugTarget(), breakpoint, map );
		}
	}

	private void removeAllBreakpoints() {
		ICDITarget cdiTarget = getCDITarget();
		try {
			cdiTarget.deleteAllBreakpoints();
		}
		catch( CDIException e ) {
			// ignore
		}
		ICBreakpoint[] breakpoints = getBreakpointMap().getAllCBreakpoints();
		getBreakpointNotifier().breakpointsRemoved( getDebugTarget(), breakpoints );
	}

	private void setLocationBreakpointOnTarget( final ICBreakpoint breakpoint, final ICDITarget target, final ICDILocation location, final ICDICondition condition, final boolean enabled ) {
		DebugPlugin.getDefault().asyncExec( new Runnable() {				
			public void run() {
				try {
					synchronized ( getBreakpointMap() ) {
						ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
						if ( cdiBreakpoint == null ) {
							cdiBreakpoint = target.setLocationBreakpoint( ICDIBreakpoint.REGULAR, location, condition, true );
							if ( !enabled ) {
								cdiBreakpoint.setEnabled( false );
							}
							getBreakpointMap().put( breakpoint, cdiBreakpoint );
						}
					}
				}
				catch( CDIException e ) {
				} 
			}
		} );
	}

	private void setFunctionBreakpoint( ICFunctionBreakpoint breakpoint ) throws CDIException, CoreException {
		final boolean enabled = breakpoint.isEnabled();
		final ICDITarget cdiTarget = getCDITarget();
		String function = breakpoint.getFunction();
		String fileName = breakpoint.getFileName();
		int lineNumber = breakpoint.getLineNumber();
		final ICDILocation location = cdiTarget.createLocation( fileName, function, lineNumber );
		final ICDICondition condition = createCondition( breakpoint );
		setLocationBreakpointOnTarget( breakpoint, cdiTarget, location, condition, enabled );
	}

	private void setAddressBreakpoint( ICAddressBreakpoint breakpoint ) throws CDIException, CoreException, NumberFormatException {
		final boolean enabled = breakpoint.isEnabled();
		final ICDITarget cdiTarget = getCDITarget();
		String address = breakpoint.getAddress();
		if ( address.startsWith( "0x" ) ) { //$NON-NLS-1$
			final ICDILocation location = cdiTarget.createLocation( new BigInteger ( breakpoint.getAddress().substring( 2 ), 16 ) );
			final ICDICondition condition = createCondition( breakpoint );
			setLocationBreakpointOnTarget( breakpoint, cdiTarget, location, condition, enabled );
		}
	}

	private void setLineBreakpoint( ICLineBreakpoint breakpoint ) throws CDIException, CoreException {
		final boolean enabled = breakpoint.isEnabled();
		final ICDITarget cdiTarget = getCDITarget();
		String handle = breakpoint.getSourceHandle();
		IPath path = new Path( handle );
		if ( path.isValidPath( handle ) ) {
			final ICDILocation location = cdiTarget.createLocation( path.lastSegment(), null, breakpoint.getLineNumber() );
			final ICDICondition condition = createCondition( breakpoint );
			setLocationBreakpointOnTarget( breakpoint, cdiTarget, location, condition, enabled );
		}
	}

	private void setWatchpointOnTarget( final ICWatchpoint watchpoint, final ICDITarget target, final int accessType, final String expression, final ICDICondition condition, final boolean enabled ) {
		DebugPlugin.getDefault().asyncExec( new Runnable() {				
			public void run() {
				try {
					synchronized ( getBreakpointMap() ) {
						if ( getBreakpointMap().getCDIBreakpoint( watchpoint ) == null ) {
							ICDIWatchpoint cdiWatchpoint = target.setWatchpoint( ICDIBreakpoint.REGULAR, accessType, expression, condition );
							if ( !enabled ) {
								cdiWatchpoint.setEnabled( false );
							}
							getBreakpointMap().put( watchpoint, cdiWatchpoint );
						}
					}
				}
				catch( CDIException e ) {
				} 
			}
		} );
	}

	private void setWatchpoint( ICWatchpoint watchpoint ) throws CDIException, CoreException {
		final boolean enabled = watchpoint.isEnabled();
		final ICDITarget cdiTarget = getCDITarget();
		int accessType = 0;
		accessType |= (watchpoint.isWriteType()) ? ICDIWatchpoint.WRITE : 0;
		accessType |= (watchpoint.isReadType()) ? ICDIWatchpoint.READ : 0;
		final int accessType1 = accessType;
		final String expression = watchpoint.getExpression();
		final ICDICondition condition = createCondition( watchpoint );
		setWatchpointOnTarget( watchpoint, cdiTarget, accessType1, expression, condition, enabled );
	}

	protected BreakpointMap getBreakpointMap() {
		return fMap;
	}

	protected void targetRequestFailed( String message, Throwable e ) throws DebugException {
		requestFailed0( message, e, DebugException.TARGET_REQUEST_FAILED );
	}

	protected void requestFailed( String message, Throwable e ) throws DebugException {
		requestFailed0( message, e, DebugException.REQUEST_FAILED );
	}

	private void requestFailed0( String message, Throwable e, int code ) throws DebugException {
		throw new DebugException( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), code, message, e ) );
	}

	private ICLineBreakpoint createLocationBreakpoint( ICDILocationBreakpoint cdiBreakpoint ) {
		ICLineBreakpoint breakpoint = null;
		try {
			if ( !isEmpty( cdiBreakpoint.getLocation().getFile() ) ) {
				ICSourceLocator locator = getSourceLocator();
				if ( locator != null ) {
					Object sourceElement = locator.findSourceElement( cdiBreakpoint.getLocation().getFile() );
					if ( sourceElement instanceof IFile || sourceElement instanceof IStorage ) {
						String sourceHandle = ( sourceElement instanceof IFile ) ? ((IFile)sourceElement).getLocation().toOSString() : ((IStorage)sourceElement).getFullPath().toOSString();
						IResource resource = ( sourceElement instanceof IFile ) ? (IResource)sourceElement : ResourcesPlugin.getWorkspace().getRoot();
						breakpoint = createLineBreakpoint( sourceHandle, resource, cdiBreakpoint );
					}
					else if ( !isEmpty( cdiBreakpoint.getLocation().getFunction() ) ) {
						breakpoint = createFunctionBreakpoint( cdiBreakpoint );
					}
					else if ( ! cdiBreakpoint.getLocation().getAddress().equals( BigInteger.ZERO ) ) {
						breakpoint = createAddressBreakpoint( cdiBreakpoint );
					}
				}
			}
			else if ( !isEmpty( cdiBreakpoint.getLocation().getFunction() ) ) {
				breakpoint = createFunctionBreakpoint( cdiBreakpoint );
			}
			else if ( !cdiBreakpoint.getLocation().getAddress().equals( BigInteger.ZERO ) ) {
				breakpoint = createAddressBreakpoint( cdiBreakpoint );
			}
		}
		catch( CDIException e ) {
		}
		catch( CoreException e ) {
		}
		return breakpoint;
	}

	private ICLineBreakpoint createLineBreakpoint( String sourceHandle, IResource resource, ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException {
		ICLineBreakpoint breakpoint = CDIDebugModel.createLineBreakpoint( sourceHandle, 
																		  resource, 
																		  cdiBreakpoint.getLocation().getLineNumber(), 
																		  cdiBreakpoint.isEnabled(), 
																		  cdiBreakpoint.getCondition().getIgnoreCount(), 
																		  cdiBreakpoint.getCondition().getExpression(), 
																		  false );
		getBreakpointMap().put( breakpoint, cdiBreakpoint );
		((CBreakpoint)breakpoint).register( true );
		return breakpoint;
	}

	private ICFunctionBreakpoint createFunctionBreakpoint( ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException {
		IPath execFile = getExecFilePath();
		String sourceHandle = execFile.toOSString();
		ICFunctionBreakpoint breakpoint = CDIDebugModel.createFunctionBreakpoint( sourceHandle, 
																				  getProject(), 
																				  cdiBreakpoint.getLocation().getFunction(),
																				  -1,
																				  -1,
																				  -1,
																				  cdiBreakpoint.isEnabled(), 
																				  cdiBreakpoint.getCondition().getIgnoreCount(), 
																				  cdiBreakpoint.getCondition().getExpression(), 
																				  false );
		getBreakpointMap().put( breakpoint, cdiBreakpoint );
		((CBreakpoint)breakpoint).register( true );
		return breakpoint;
	}

	private ICAddressBreakpoint createAddressBreakpoint( ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException {
		IPath execFile = getExecFilePath();
		String sourceHandle = execFile.toOSString();
		IAddress address = getDebugTarget().getAddressFactory().createAddress( cdiBreakpoint.getLocation().getAddress() );
		ICAddressBreakpoint breakpoint = CDIDebugModel.createAddressBreakpoint( sourceHandle, 
																				getProject(), 
																				address, 
																				cdiBreakpoint.isEnabled(), 
																				cdiBreakpoint.getCondition().getIgnoreCount(), 
																				cdiBreakpoint.getCondition().getExpression(), 
																				false );
		getBreakpointMap().put( breakpoint, cdiBreakpoint );
		((CBreakpoint)breakpoint).register( true );
		return breakpoint;
	}

	private ICWatchpoint createWatchpoint( ICDIWatchpoint cdiWatchpoint ) throws CDIException, CoreException {
		IPath execFile = getExecFilePath();
		String sourceHandle = execFile.toOSString();
		ICWatchpoint watchpoint = CDIDebugModel.createWatchpoint( sourceHandle, 
																  getProject(), 
																  cdiWatchpoint.isWriteType(), 
																  cdiWatchpoint.isReadType(), 
																  cdiWatchpoint.getWatchExpression(), 
																  cdiWatchpoint.isEnabled(), 
																  cdiWatchpoint.getCondition().getIgnoreCount(), 
																  cdiWatchpoint.getCondition().getExpression(), 
																  false );
		getBreakpointMap().put( watchpoint, cdiWatchpoint );
		((CBreakpoint)watchpoint).register( true );
		return watchpoint;
	}

	private ICSourceLocator getSourceLocator() {
		ISourceLocator locator = getDebugTarget().getLaunch().getSourceLocator();
		return (locator instanceof IAdaptable) ? (ICSourceLocator)((IAdaptable)locator).getAdapter( ICSourceLocator.class ) : null;
	}

	private IProject getProject() {
		return getDebugTarget().getProject();
	}

	private IPath getExecFilePath() {
		return getDebugTarget().getExecFile().getPath();
	}

	private CBreakpointNotifier getBreakpointNotifier() {
		return CBreakpointNotifier.getInstance();
	}

	private boolean isEmpty( String str ) {
		return !( str != null && str.trim().length() > 0 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManagerListener#breakpointManagerEnablementChanged(boolean)
	 */
	public void breakpointManagerEnablementChanged( boolean enabled ) {
		doSkipBreakpoints( !enabled );
	}

	public void skipBreakpoints( boolean enabled ) {
		if ( fSkipBreakpoint != enabled && (DebugPlugin.getDefault().getBreakpointManager().isEnabled() || !enabled) ) {
			fSkipBreakpoint = enabled;
			doSkipBreakpoints( enabled );
		}
	}

	private void doSkipBreakpoints( boolean enabled ) {
		ICBreakpoint[] cBreakpoints = getBreakpointMap().getAllCBreakpoints();
		for ( int i = 0; i < cBreakpoints.length; ++i ) {
			try {
				if ( cBreakpoints[i].isEnabled() ) {
					ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( cBreakpoints[i] );
					if ( cdiBreakpoint != null ) {
						cdiBreakpoint.setEnabled( !enabled );
					}
				}
			}
			catch( CoreException e ) {
				// ignore
			}
			catch( CDIException e ) {
				// ignore
			}
		}
	}

	private boolean isFilteredByTarget( ICBreakpoint breakpoint, ICDebugTarget target ) {
		boolean result = false;
		try {
			ICDebugTarget[] tfs = breakpoint.getTargetFilters();
			result = Arrays.asList( tfs ).contains( target );
		}
		catch( CoreException e ) {
			// ignore
		}
		return result;
	}

	private boolean areThreadFiltersChanged( String[] newIds, ICDIBreakpoint cdiBreakpoint ) {
		try {
			String[] oldIds = cdiBreakpoint.getCondition().getThreadIds();
			if ( oldIds.length != newIds.length )
				return true;
			List list = Arrays.asList( oldIds );
			for ( int i = 0; i < newIds.length; ++i ) {
				if ( !list.contains( newIds[i] ) ) {
					return true;
				}
			}
		}
		catch( CDIException e ) {
		}
		return false;
	}

	private String[] getThreadNames( ICBreakpoint breakpoint ) {
		try {
			ICThread[] threads = breakpoint.getThreadFilters( getDebugTarget() );
			if ( threads == null )
				return new String[0];				
			String[] names = new String[threads.length];
			for ( int i = 0; i < threads.length; ++i ) {
				names[i] = threads[i].getName();
			}
			return names;
		}
		catch( DebugException e ) {
		}
		catch( CoreException e ) {
		}
		return new String[0];
	}

	private ICDICondition createCondition( ICBreakpoint breakpoint ) throws CoreException, CDIException {
		return getCDITarget().createCondition( breakpoint.getIgnoreCount(), breakpoint.getCondition(), getThreadNames( breakpoint ) );
	}
}
