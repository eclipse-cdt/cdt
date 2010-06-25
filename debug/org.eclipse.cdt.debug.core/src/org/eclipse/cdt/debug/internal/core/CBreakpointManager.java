/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Matthias Spycher (matthias@coware.com) - patch for bug #112008
 * Ken Ryall (Nokia) - bugs 170027, 105196
 * Ling Wang (Nokia) - bug 176081
 * Freescale Semiconductor - Address watchpoints, https://bugs.eclipse.org/bugs/show_bug.cgi?id=118299
 * QNX Software Systems - catchpoints - bug 226689
 * James Blackburn (Broadcom) - bug 314865
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core; 

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.event.ICDIBreakpointMovedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIBreakpointProblemEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIExecutableReloadedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint2;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement2;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement3;
import org.eclipse.cdt.debug.core.cdi.model.ICDIEventBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration2;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint2;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointFilterExtension;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint2;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.core.breakpoints.BreakpointProblems;
import org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;

public class CBreakpointManager implements IBreakpointsListener, IBreakpointManagerListener, ICDIEventListener, IAdaptable {

	static class BreakpointInProgess {
		
		private ICDIBreakpoint fCDIBreakpoint;

		void setCDIBreakpoint( ICDIBreakpoint b ) {
			fCDIBreakpoint = b;
		}

		ICDIBreakpoint getCDIBreakpoint() {
			return fCDIBreakpoint;
		}
	}

	class BreakpointMap {

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

		void register( ICBreakpoint breakpoint ) {
			fCBreakpoints.put( breakpoint, new BreakpointInProgess() );
		}

		void put( ICBreakpoint breakpoint, ICDIBreakpoint cdiBreakpoint ) {
			fCBreakpoints.put( breakpoint, cdiBreakpoint );
			fCDIBreakpoints.put( cdiBreakpoint, breakpoint );
		}

		Object get( ICBreakpoint breakpoint ) {
			return fCBreakpoints.get( breakpoint );
		}

		ICDIBreakpoint getCDIBreakpoint( ICBreakpoint breakpoint ) {
			Object b = fCBreakpoints.get( breakpoint );
			return ( b instanceof ICDIBreakpoint ) ? (ICDIBreakpoint)b : null;
		}

		ICBreakpoint getCBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
			ICBreakpoint breakpoint = (ICBreakpoint)fCDIBreakpoints.get( cdiBreakpoint );
			if ( breakpoint == null ) {
				ICBreakpoint[] bip = getBreakpointsInProgress();
				for ( int i = 0; i < bip.length; ++i ) {
					if ( isSameBreakpoint( bip[i], cdiBreakpoint ) ) {
						breakpoint = bip[i];
						break;
					}
				}
			}
			return breakpoint;
		}

		void removeCDIBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
			if ( cdiBreakpoint != null ) {
				ICBreakpoint breakpoint = (ICBreakpoint)fCDIBreakpoints.remove( cdiBreakpoint );
				if ( breakpoint != null )
					fCBreakpoints.remove( breakpoint );
			}
		}

		boolean isRegistered( ICBreakpoint breakpoint ) {
			return ( fCBreakpoints.get( breakpoint ) != null );
		}

		boolean isInProgress( ICBreakpoint breakpoint ) {
			return ( fCBreakpoints.get( breakpoint ) instanceof BreakpointInProgess );
		}

		ICBreakpoint[] getAllCBreakpoints() {
			Set set = fCBreakpoints.keySet();
			return (ICBreakpoint[])set.toArray( new ICBreakpoint[set.size()] );
		}

		void dispose() {
			fCBreakpoints.clear();
			fCDIBreakpoints.clear();
		}

		private ICBreakpoint[] getBreakpointsInProgress() {
			ArrayList list = new ArrayList();
			Set set = fCBreakpoints.entrySet();
			Iterator it = set.iterator();
			while ( it.hasNext() ) {
				Map.Entry entry = (Map.Entry)it.next();
				if ( entry.getValue() instanceof BreakpointInProgess ) {
					list.add( entry.getKey() );
				}
			}
			return (ICBreakpoint[])list.toArray( new ICBreakpoint[list.size()] );
		}

		private boolean isSameBreakpoint( ICBreakpoint breakpoint, ICDIBreakpoint cdiBreakpoint ) {
			try {
				if ( breakpoint instanceof ICFunctionBreakpoint && cdiBreakpoint instanceof ICDIFunctionBreakpoint ) {
					return ( ((ICFunctionBreakpoint)breakpoint).getFunction().compareTo( ((ICDIFunctionBreakpoint)cdiBreakpoint).getLocator().getFunction() ) == 0 );
				}
				if ( breakpoint instanceof ICAddressBreakpoint && cdiBreakpoint instanceof ICDIAddressBreakpoint ) {
					IAddressFactory factory = getDebugTarget().getAddressFactory(); 
					return factory.createAddress( ((ICAddressBreakpoint)breakpoint).getAddress() ).equals( factory.createAddress( ((ICDIAddressBreakpoint)cdiBreakpoint).getLocator().getAddress() ) );
				}
				if ( breakpoint instanceof ICLineBreakpoint && cdiBreakpoint instanceof ICDILineBreakpoint ) {
					ICDILocator location = ((ICDILineBreakpoint)cdiBreakpoint).getLocator();
					String file = location.getFile();
					String sourceHandle = file;
					if ( !isEmpty( file ) ) {
						Object sourceElement = getSourceElement( file );
						if ( sourceElement instanceof IFile ) {
							sourceHandle = ((IFile)sourceElement).getLocation().toOSString();
						}
						else if ( sourceElement instanceof IStorage ) {
							sourceHandle = ((IStorage)sourceElement).getFullPath().toOSString();
						}
						else if ( sourceElement instanceof ITranslationUnit ) {
							sourceHandle = ((ITranslationUnit)sourceElement).getLocation().toOSString();
						}
						String bpSourceHandle = ((ICLineBreakpoint)breakpoint).getSourceHandle();
						if ( sourceElement instanceof LocalFileStorage ) { // see bug #112008
							try {
								bpSourceHandle = new File( bpSourceHandle ).getCanonicalPath();
							}
							catch( IOException e ) {
							}
						}
						return sourceHandle.equals( bpSourceHandle ) && location.getLineNumber() == ((ICLineBreakpoint)breakpoint).getLineNumber(); 
					}
				}
				if ( breakpoint instanceof ICWatchpoint && cdiBreakpoint instanceof ICDIWatchpoint ) {
					try {
						ICWatchpoint watchpoint = (ICWatchpoint)breakpoint;
						if ( watchpoint instanceof ICWatchpoint2  && cdiBreakpoint instanceof ICDIWatchpoint2 ) {
							ICWatchpoint2 wp2 = (ICWatchpoint2)breakpoint;
							ICDIWatchpoint2 cdiwp2 = (ICDIWatchpoint2)cdiBreakpoint;
							if ( !wp2.getMemorySpace().equals( cdiwp2.getMemorySpace() )
									|| !wp2.getRange().equals( cdiwp2.getRange() ) ) {
								return false;
							}
						}
						ICDIWatchpoint cdiWatchpoint = (ICDIWatchpoint)cdiBreakpoint;
						return ( watchpoint.getExpression().compareTo( cdiWatchpoint.getWatchExpression() ) == 0 && 
								 watchpoint.isReadType() == cdiWatchpoint.isReadType() &&
								 watchpoint.isWriteType() == cdiWatchpoint.isWriteType() );
					}
					catch( CDIException e ) {
					}
				}
				if ( breakpoint instanceof ICEventBreakpoint && cdiBreakpoint instanceof ICDIEventBreakpoint) {
					ICEventBreakpoint mevtbkpt = (ICEventBreakpoint) breakpoint;
					ICDIEventBreakpoint cdievtbkpt = (ICDIEventBreakpoint) cdiBreakpoint;
					if (!mevtbkpt.getEventType().equals(cdievtbkpt.getEventType())) return false; 
					return (mevtbkpt.getEventArgument().equals(cdievtbkpt.getExtraArgument()));
				}
			}
			catch( CoreException e ) {
			}
			return false;
		}
	}

	private CDebugTarget fDebugTarget;

	private BreakpointMap fMap;
	
	private boolean fSkipBreakpoint = false;
	
	private ArrayList fBreakpointProblems = new ArrayList();

	public CBreakpointManager( CDebugTarget target ) {
		super();
		fDebugTarget = target;
		fMap = new BreakpointMap();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	public void breakpointsAdded( IBreakpoint[] breakpoints ) {
		if ( !isTargetAvailable() )
			return;
		setBreakpointsOnTarget( breakpoints );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsRemoved( IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		if ( !isTargetAvailable() )
			return;
		ArrayList list = new ArrayList( breakpoints.length );
		synchronized( getBreakpointMap() ) {
			for ( int i = 0; i < breakpoints.length; ++i ) {
				if ( breakpoints[i] instanceof ICBreakpoint ) {
					try { // Remove any problem markers for this breakpoint
						BreakpointProblems.removeProblemsForBreakpoint((ICBreakpoint)breakpoints[i]);
					} catch (CoreException e) {}
					Object obj = getBreakpointMap().get( (ICBreakpoint)breakpoints[i] );
					ICDIBreakpoint b = null;
					if ( obj instanceof ICDIBreakpoint ) {
						b = (ICDIBreakpoint)obj;
					}
					else if ( obj instanceof BreakpointInProgess ) {
						b = ((BreakpointInProgess)obj).getCDIBreakpoint();
					}
					if ( b != null ) {
						list.add( b );
					}
				}
			}
		}
		if ( list.isEmpty() )
			return;
		final ICDIBreakpoint[] cdiBreakpoints = (ICDIBreakpoint[])list.toArray( new ICDIBreakpoint[list.size()] );
		final ICDITarget cdiTarget = getCDITarget();
		DebugPlugin.getDefault().asyncExec( new Runnable() {				
			public void run() {
				try {
					cdiTarget.deleteBreakpoints( cdiBreakpoints );
				}
				catch( CDIException e ) {
				} 
			}
		} );			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsChanged( IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		ArrayList removeList = new ArrayList( breakpoints.length );
		ArrayList installList = new ArrayList( breakpoints.length );
		synchronized ( getBreakpointMap() ) {
			for ( int i = 0; i < breakpoints.length; ++i ) {
				if ( !(breakpoints[i] instanceof ICBreakpoint) || !isTargetAvailable() )
					continue;
				ICBreakpoint b = (ICBreakpoint)breakpoints[i];
				boolean install = false;
				try {
					ICDebugTarget[] tfs = getFilterExtension(b).getTargetFilters();
					install = Arrays.asList( tfs ).contains( getDebugTarget() );
				}
				catch( CoreException e ) {
				}
				boolean registered = getBreakpointMap().isRegistered( b );
				boolean inProgress = getBreakpointMap().isInProgress( b );
				if ( registered && !inProgress && !install ) {
					removeList.add( b );
				}
				if ( !registered && install ) {
					installList.add( b );
				}
			}
		}
		breakpointsRemoved( (ICBreakpoint[])removeList.toArray( new ICBreakpoint[removeList.size()] ), new IMarkerDelta[0] );
		breakpointsAdded( (ICBreakpoint[])installList.toArray( new ICBreakpoint[removeList.size()] ) );
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( breakpoints[i] instanceof ICBreakpoint && isTargetAvailable() )
				changeBreakpointProperties( (ICBreakpoint)breakpoints[i], deltas[i] );
		}
	}

	public void breakpointManagerEnablementChanged( boolean enabled ) {
		doSkipBreakpoints( !enabled );
	}

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
				else if ( event instanceof ICDIBreakpointMovedEvent ) {
					if ( source instanceof ICDIBreakpoint )
						handleBreakpointMovedEvent( (ICDIBreakpointMovedEvent) event );
				}
				else if ( event instanceof ICDIExecutableReloadedEvent ) {
					if ( source instanceof ICDITarget )
						handleExecutableReloadedEvent( (ICDIExecutableReloadedEvent) event );
				}
				else if ( event instanceof ICDIBreakpointProblemEvent ) {
					if ( source instanceof ICDIBreakpoint )
						handleBreakpointProblemEvent( (ICDIBreakpointProblemEvent) event );
				}
			}
		}
	}

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

	public void initialize() {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener( this );
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointManagerListener( this );
		getDebugTarget().getCDISession().getEventManager().addEventListener( this );
	}

	public void dispose() {
		getDebugTarget().getCDISession().getEventManager().removeEventListener( this );
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener( this );
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointManagerListener( this );
		removeAllBreakpoints();
		getBreakpointMap().dispose();
	}

	public IAddress getBreakpointAddress( ICLineBreakpoint breakpoint ) {
		BigInteger address = null;
		synchronized ( getBreakpointMap() ) {
			ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
			if ( cdiBreakpoint instanceof ICDILocationBreakpoint ) {
				ICDILocator locator = ((ICDILocationBreakpoint)cdiBreakpoint).getLocator();
				if ( locator != null ) {
					address = locator.getAddress();
				}
			}
		}
		return ( address != null ) ? getDebugTarget().getAddressFactory().createAddress( address ) : null;
	}

	public IBreakpoint getBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
		Object b = null;
		synchronized ( getBreakpointMap() ) {
			b = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		}
		return ( b instanceof IBreakpoint ) ? (IBreakpoint)b : null; 
	}
	
	/**
	 * @return true if the breakpoint is of a temporary type, otherwise false
	 */
	private boolean isTemporary(ICDIBreakpoint cdiBreakpoint) {
		if (cdiBreakpoint instanceof ICDIBreakpoint2) {
			return (((ICDIBreakpoint2)cdiBreakpoint).getType() & ICBreakpointType.TEMPORARY) != 0;
		}
		else {
			return cdiBreakpoint.isTemporary();
		}
	}

	private void handleBreakpointCreatedEvent( ICDIBreakpoint cdiBreakpoint ) {
		if ( cdiBreakpoint instanceof ICDIWatchpoint )
			doHandleWatchpointCreatedEvent( (ICDIWatchpoint)cdiBreakpoint );
		if ( cdiBreakpoint instanceof ICDIEventBreakpoint )
			doHandleEventBreakpointCreatedEvent( (ICDIEventBreakpoint)cdiBreakpoint );
		else if ( cdiBreakpoint instanceof ICDILocationBreakpoint )
			doHandleLocationBreakpointCreatedEvent( (ICDILocationBreakpoint)cdiBreakpoint );
		try {
			if ( !isTemporary(cdiBreakpoint) && !DebugPlugin.getDefault().getBreakpointManager().isEnabled() && cdiBreakpoint.isEnabled() ) {
				changeBreakpointPropertiesOnTarget(cdiBreakpoint, false, null);
			}
		} catch (CDIException e){
		}
	}

	private void doHandleEventBreakpointCreatedEvent(ICDIEventBreakpoint cdiEventBkpt) {
		ICBreakpoint breakpoint = null;
		ICBreakpoint newBreakpoint = null;
		boolean createNewCBkpt = false;
		final BreakpointMap bkptMap = getBreakpointMap();

		synchronized( bkptMap ) {
			createNewCBkpt = (bkptMap.getCBreakpoint( cdiEventBkpt ) == null);
		}
		
		// This has to be done outside the breakpoint map lock, or a deadlock
		// can occur (according to rev 1.71). Not certain we'll use this new CDT
		// breakpoint; we need to check the map again.
		if (createNewCBkpt) {
			try {
				newBreakpoint = createEventBreakpoint( cdiEventBkpt );
			}
			catch( CDIException e ) {}
			catch( CoreException e ) {}
		}

		synchronized( bkptMap ) {
			breakpoint = bkptMap.getCBreakpoint( cdiEventBkpt );
			if ( breakpoint == null ) {
				breakpoint = newBreakpoint;
			}
			
			if ( breakpoint != null ) {
				// filter must be set up prior to adding the breakpoint to the
				// map to avoid a race condition in breakpointsChanged for the
				// "registered && !inProgress && !install" condition
				try {
			    	getFilterExtension(breakpoint).setTargetFilter( getDebugTarget() );
				}
				catch( CoreException e ) {}
				
				bkptMap.put( breakpoint, cdiEventBkpt );
			}
		}
		
		// Delete the new CDT breakpoint if we didn't end up using it
		if (newBreakpoint != null && newBreakpoint != breakpoint) {
			try {
				newBreakpoint.delete();
			} catch (CoreException e) {}
		}

		if ( breakpoint != null ) {
			try {
				((CBreakpoint)breakpoint).register( true );
			}
			catch( CoreException e ) {
			}
			getBreakpointNotifier().breakpointInstalled( getDebugTarget(), breakpoint );
			changeBreakpointProperties( breakpoint, cdiEventBkpt );
		}
		
	}

	private void doHandleLocationBreakpointCreatedEvent( ICDILocationBreakpoint cdiBreakpoint ) {
		if ( isTemporary(cdiBreakpoint) )
			return;
		ICBreakpoint breakpoint = null;
		ICBreakpoint newBreakpoint = null;
		final BreakpointMap bkptMap = getBreakpointMap();
		boolean createNewCBkpt = false;
		synchronized( bkptMap ) {
			createNewCBkpt = (bkptMap.getCBreakpoint( cdiBreakpoint ) == null);
		}
		
		// This has to be done outside the breakpoint map lock, or a deadlock
		// can occur (according to rev 1.71). Not certain we'll use this new CDT
		// breakpoint; we need to check the map again.
		if ( createNewCBkpt ) {
			newBreakpoint = createLocationBreakpoint( cdiBreakpoint );
		}
		
		synchronized( bkptMap ) {
			breakpoint = bkptMap.getCBreakpoint( cdiBreakpoint );
			if ( breakpoint == null ) {
				breakpoint = newBreakpoint;
			}

			if ( breakpoint != null ) {
				// filter must be set up prior to adding the breakpoint to the
				// map to avoid a race condition in breakpointsChanged for the
				// "registered && !inProgress && !install" condition
				try {
					getFilterExtension(breakpoint).setTargetFilter( getDebugTarget() );
				}
				catch( CoreException e ) {}
				
				bkptMap.put( breakpoint, cdiBreakpoint );
			}
		}

		// Delete the new CDT breakpoint if we didn't end up using it
		if (newBreakpoint != null && newBreakpoint != breakpoint) {
			try {
				newBreakpoint.delete();
			} catch (CoreException e) {}
		}
		
		if ( breakpoint != null ) {
			try {
				BreakpointProblems.removeProblemsForResolvedBreakpoint(breakpoint, getDebugTarget().getInternalID());
				((CBreakpoint)breakpoint).register( true );
			}
			catch( CoreException e ) {
			}
			getBreakpointNotifier().breakpointInstalled( getDebugTarget(), breakpoint );
			changeBreakpointProperties( breakpoint, cdiBreakpoint );
		}
	}

	private void doHandleWatchpointCreatedEvent( ICDIWatchpoint cdiWatchpoint ) {
		ICBreakpoint breakpoint = null;
		ICBreakpoint newBreakpoint = null;
		boolean createNewCBkpt = false;
		final BreakpointMap bkptMap = getBreakpointMap();
		
		synchronized( bkptMap ) {
			createNewCBkpt = (bkptMap.getCBreakpoint( cdiWatchpoint ) == null);
		}
		
		// This has to be done outside the breakpoint map lock, or a deadlock
		// can occur (according to rev 1.71). Not certain we'll use this new CDT
		// breakpoint; we need to check the map again.
		if (createNewCBkpt) {
			try {
				newBreakpoint = createWatchpoint( cdiWatchpoint );
			}
			catch( CDIException e ) {}
			catch( CoreException e ) {}
		}
		
		synchronized( bkptMap ) {
			breakpoint = bkptMap.getCBreakpoint( cdiWatchpoint );
			if ( breakpoint == null ) {
				breakpoint = newBreakpoint;
			}
			
			if ( breakpoint != null ) {
				// filter must be set up prior to adding the breakpoint to the
				// map to avoid a race condition in breakpointsChanged for the
				// "registered && !inProgress && !install" condition
				try {
				    getFilterExtension(breakpoint).setTargetFilter( getDebugTarget() );
				}
				catch( CoreException e ) {}

				bkptMap.put( breakpoint, cdiWatchpoint );
			}
		}
		
		// Delete the new CDT breakpoint if we didn't end up using it
		if (newBreakpoint != null && newBreakpoint != breakpoint) {
			try {
				newBreakpoint.delete();
			} catch (CoreException e) {}
		}
		

		if ( breakpoint != null ) {
			try {
				((CBreakpoint)breakpoint).register( true );
			}
			catch( CoreException e ) {
			}
			getBreakpointNotifier().breakpointInstalled( getDebugTarget(), breakpoint );
			changeBreakpointProperties( breakpoint, cdiWatchpoint );
		}
	}

	private void handleBreakpointMovedEvent( ICDIBreakpointMovedEvent movedEvent )
	{
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( (ICDIBreakpoint) movedEvent.getSource() );
		if (breakpoint != null)
		{
			try {
				int newLineNumber = movedEvent.getNewLocation().getLineNumber();
				int currLineNumber = breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, newLineNumber);
				breakpoint.getMarker().setAttribute(IMarker.LINE_NUMBER, newLineNumber);
				IMarker marker = BreakpointProblems.reportBreakpointMoved(
						breakpoint, currLineNumber, newLineNumber, getDebugTarget().getName(), getDebugTarget().getInternalID());
				if (marker != null)
					fBreakpointProblems.add(marker);
			} catch (CoreException e) {}
		}
		
	}

	private void handleExecutableReloadedEvent( ICDIExecutableReloadedEvent reloadedEvent )
	{
		ArrayList uninstalledCBplist = new ArrayList();
		
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints( CDIDebugModel.getPluginIdentifier() );
		
		for (int i = 0; i < breakpoints.length; i++) {
			if (breakpoints[i] instanceof ICBreakpoint && (getBreakpointMap().getCDIBreakpoint((ICBreakpoint) breakpoints[i]) == null))
			{
				uninstalledCBplist.add(breakpoints[i]);					
			}
		}

		setBreakpointsOnTarget((IBreakpoint[]) uninstalledCBplist.toArray(new IBreakpoint[uninstalledCBplist.size()]));
	}

	private void handleBreakpointProblemEvent( ICDIBreakpointProblemEvent problemEvent )
	{
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( problemEvent.getBreakpoint() );
		if (breakpoint != null)
		{
			try {
				IMarker marker;
				marker = BreakpointProblems.reportBreakpointProblem(breakpoint, problemEvent.getDescription(), 
						problemEvent.getSeverity(), problemEvent.getProblemType(), problemEvent.removeExisting(),
						problemEvent.removeOnly(), getDebugTarget().getName(), getDebugTarget().getInternalID());
				if (marker != null)
					fBreakpointProblems.add(marker);
			} catch (DebugException e) {}
		}
		
	}
	
	private void handleBreakpointChangedEvent( ICDIBreakpoint cdiBreakpoint ) {
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		if ( breakpoint != null ) {
			Map map = new HashMap( 3 );
			try {
				if ( !fSkipBreakpoint && DebugPlugin.getDefault().getBreakpointManager().isEnabled() ) {
						map.put( IBreakpoint.ENABLED, Boolean.valueOf( cdiBreakpoint.isEnabled() ) );
				}
				else {
					map.put( IBreakpoint.ENABLED, Boolean.valueOf( breakpoint.isEnabled() ) );
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

	private void handleBreakpointDestroyedEvent( ICDIBreakpoint cdiBreakpoint ) {
		ICBreakpoint breakpoint = null;
		synchronized( getBreakpointMap() ) {
			breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
			getBreakpointMap().removeCDIBreakpoint( cdiBreakpoint );
		}
		if ( breakpoint != null ) {
			if ( isFilteredByTarget( breakpoint, getDebugTarget() ) ) {
				try {
				    getFilterExtension(breakpoint).removeTargetFilter( getDebugTarget() );
				}
				catch( CoreException e ) {
				}
			}
			try {
				BreakpointProblems.removeProblemsForBreakpoint(breakpoint);
			} catch (CoreException e) {}
			getBreakpointNotifier().breakpointsRemoved( getDebugTarget(), new IBreakpoint[] { breakpoint } );
		}
	}

	private BreakpointMap getBreakpointMap() {
		return fMap;
	}

	private void removeAllBreakpoints() {
		// Remove all breakpoint problem markers
		for (Iterator iter = fBreakpointProblems.iterator(); iter.hasNext();) {
			IMarker marker = (IMarker) iter.next();
			try {
				marker.delete();
			} catch (CoreException e) {}
		}
		
		ArrayList installedCDIBplist = new ArrayList();
		ArrayList installedCBplist = new ArrayList();
		ICBreakpoint[] breakpoints = new ICBreakpoint[0];
		synchronized( getBreakpointMap() ) {
			breakpoints = getBreakpointMap().getAllCBreakpoints();			
			for ( int i = 0; i < breakpoints.length; ++i ) {
				if ( !getBreakpointMap().isInProgress( breakpoints[i] ) ) {
					installedCDIBplist.add( getBreakpointMap().getCDIBreakpoint( breakpoints[i] ) );
					
					installedCBplist.add(breakpoints[i]);
				}
			}
		}
		if ( installedCDIBplist.isEmpty() )
			return;
		
		final ICDIBreakpoint[] cdiBreakpoints = (ICDIBreakpoint[])installedCDIBplist.toArray( new ICDIBreakpoint[installedCDIBplist.size()] );
		final ICDITarget cdiTarget = getCDITarget();
		
		// Clean up the target filter to avoid that the CDebugTarget remains referenced by the breakpoint.
		// Note that though we're "removing" breakpoints from a debug session, the breakpoint objects in the 
		// platform aren't going anywhere. They are "global" model elements. They go away only when the user
		// deletes them. 
		CDebugTarget target = getDebugTarget();
		for (Iterator iter = installedCBplist.iterator(); iter.hasNext();) {
			ICBreakpoint breakpoint = (ICBreakpoint) iter.next();
			if ( isFilteredByTarget( breakpoint, target ) ) {
				try {
					getFilterExtension(breakpoint).removeTargetFilter( target );
				}
				catch( CoreException e ) {
					CDebugCorePlugin.log( e.getStatus() );					
				}
			}
		}
			
		DebugPlugin.getDefault().asyncExec( new Runnable() {				
			public void run() {
				try {
					cdiTarget.deleteBreakpoints( cdiBreakpoints );
				}
				catch( CDIException e ) {
				} 
			}
		} );
		
		getBreakpointNotifier().breakpointsRemoved( getDebugTarget(), (ICBreakpoint[])installedCBplist.toArray( new ICBreakpoint[installedCBplist.size()] ) );
	}

	private ICBreakpoint[] register( IBreakpoint[] breakpoints ) {
		ArrayList list = new ArrayList( breakpoints.length );
		synchronized ( getBreakpointMap() ) {
			for ( int i = 0; i < breakpoints.length; ++i ) {
				if ( breakpoints[i] instanceof ICBreakpoint && isTargetBreakpoint( (ICBreakpoint)breakpoints[i] ) && !(getBreakpointMap().isRegistered( (ICBreakpoint)breakpoints[i] )) ) {
					getBreakpointMap().register( (ICBreakpoint)breakpoints[i] );
					list.add( breakpoints[i] );
				}
			}
		}
		return (ICBreakpoint[])list.toArray( new ICBreakpoint[list.size()] );
	}

	private void setBreakpointsOnTarget( IBreakpoint[] breakpoints ) {
		final ICBreakpoint[] bkpts = register( breakpoints );
		if ( bkpts.length > 0 ) {
			DebugPlugin.getDefault().asyncExec( new Runnable() {				
				public void run() {
					setBreakpointsOnTarget0( bkpts );
				}
			} );
		}
	}

	protected void setBreakpointsOnTarget0( ICBreakpoint[] breakpoints ) {
		ICDITarget cdiTarget = getCDITarget();
		ICDIBreakpointManagement2 bpManager2 = null;
		if (cdiTarget instanceof ICDIBreakpointManagement2)
			bpManager2 = (ICDIBreakpointManagement2) cdiTarget;
		for ( int i = 0; i < breakpoints.length; ++i ) {
			try {
				ICDIBreakpoint b = null;
				int breakpointType = ICBreakpointType.REGULAR;
				ICBreakpoint icbreakpoint = breakpoints[i];
				// Bug 314865: CDI breakpoint is only created enabled if the global breakpoint disable toggle isn't set
				boolean enabled = icbreakpoint.isEnabled() && DebugPlugin.getDefault().getBreakpointManager().isEnabled();
				if (icbreakpoint instanceof ICBreakpointType) {
					breakpointType = ((ICBreakpointType) icbreakpoint).getType();
				}
				if ( icbreakpoint instanceof ICTracepoint) {
					ICTracepoint breakpoint = (ICTracepoint)icbreakpoint; 
					IMarker marker = BreakpointProblems.reportUnsupportedTracepoint(breakpoint, getDebugTarget().getName(), getDebugTarget().getInternalID());
					if (marker != null)
						fBreakpointProblems.add(marker);
				}
				else if ( icbreakpoint instanceof ICFunctionBreakpoint ) {
					ICFunctionBreakpoint breakpoint = (ICFunctionBreakpoint)icbreakpoint; 
					String function = breakpoint.getFunction();
					String fileName = breakpoint.getFileName();
					ICDIFunctionLocation location = cdiTarget.createFunctionLocation( fileName, function );
					ICDICondition condition = createCondition( breakpoint );
					IMarker marker = BreakpointProblems.reportUnresolvedBreakpoint(breakpoint, getDebugTarget().getName(), getDebugTarget().getInternalID());
					if (marker != null)
						fBreakpointProblems.add(marker);
					if (bpManager2 != null)
						b = bpManager2.setFunctionBreakpoint( breakpointType, location, condition, true, enabled );
					else
						b = cdiTarget.setFunctionBreakpoint( breakpointType, location, condition, true );								
				} else if ( icbreakpoint instanceof ICAddressBreakpoint ) {
					ICAddressBreakpoint breakpoint = (ICAddressBreakpoint)icbreakpoint; 
					String address = breakpoint.getAddress();
					ICDIAddressLocation location = cdiTarget.createAddressLocation( new BigInteger ( ( address.startsWith( "0x" ) ) ? address.substring( 2 ) : address, 16 ) ); //$NON-NLS-1$
					ICDICondition condition = createCondition( breakpoint );
					if (bpManager2 != null)
						b = bpManager2.setAddressBreakpoint( breakpointType, location, condition, true, enabled );
					else
						b = cdiTarget.setAddressBreakpoint( breakpointType, location, condition, true );					
				} else if ( icbreakpoint instanceof ICLineBreakpoint ) {
					ICLineBreakpoint breakpoint = (ICLineBreakpoint)icbreakpoint; 
					String handle = breakpoint.getSourceHandle();
					IPath path = convertPath( handle );
					ICDILineLocation location = cdiTarget.createLineLocation( path.toPortableString(), breakpoint.getLineNumber() );
					ICDICondition condition = createCondition( breakpoint );
					IMarker marker = BreakpointProblems.reportUnresolvedBreakpoint(breakpoint, getDebugTarget().getName(), getDebugTarget().getInternalID());
					if (marker != null)
						fBreakpointProblems.add(marker);
					if (bpManager2 != null)
						b = bpManager2.setLineBreakpoint( breakpointType, location, condition, true, enabled );
					else
						b = cdiTarget.setLineBreakpoint( breakpointType, location, condition, true );
				} else if ( icbreakpoint instanceof ICWatchpoint ) {
					ICWatchpoint watchpoint = (ICWatchpoint)icbreakpoint;
					int accessType = 0;
					accessType |= (watchpoint.isWriteType()) ? ICDIWatchpoint.WRITE : 0;
					accessType |= (watchpoint.isReadType()) ? ICDIWatchpoint.READ : 0;
					String expression = watchpoint.getExpression();
					ICDICondition condition = createCondition( watchpoint );
					if ( bpManager2 != null ) {
						if ( icbreakpoint instanceof ICWatchpoint2 ) {
							ICWatchpoint2 wp2 = (ICWatchpoint2)watchpoint;
							b = bpManager2.setWatchpoint( breakpointType, accessType, expression, wp2.getMemorySpace(), 
									wp2.getRange(), condition, enabled );
						} else {
							b = bpManager2.setWatchpoint( breakpointType, accessType, expression, condition, enabled );
						}
					} else {
						b = cdiTarget.setWatchpoint(breakpointType, accessType, expression, condition );
					}
				} else if (icbreakpoint instanceof ICEventBreakpoint) {
					ICEventBreakpoint eventbkpt = (ICEventBreakpoint) icbreakpoint;
					ICDICondition condition = createCondition(eventbkpt);
					if (cdiTarget instanceof ICDIBreakpointManagement3) {
						ICDIBreakpointManagement3 bpManager3 = (ICDIBreakpointManagement3) cdiTarget;
						b = bpManager3.setEventBreakpoint(eventbkpt.getEventType(), eventbkpt
								.getEventArgument(), breakpointType, condition, true, enabled);
					} else {
						throw new UnsupportedOperationException("BreakpointManager does not support this type of breapoints");
					}

				}
				if ( b != null ) {
					Object obj = getBreakpointMap().get( icbreakpoint );
					if ( obj instanceof BreakpointInProgess ) {
						((BreakpointInProgess)obj).setCDIBreakpoint( b );
					}
				}
				// Hack: see bug 105196: [CDI]: Add "enabled" flag to the "set...Breakpoint" methods
				if (bpManager2 == null && b != null && b.isEnabled() != enabled ) {
					b.setEnabled( enabled );
				}
			}
			catch( CoreException e ) {
			}
			catch( NumberFormatException e ) {
			}
			catch( CDIException e ) {
			}
		}
	}

	protected ICDITarget getCDITarget() {
		return getDebugTarget().getCDITarget();
	}

	private ICDICondition createCondition( ICBreakpoint breakpoint ) throws CoreException, CDIException {
		return getCDITarget().createCondition( breakpoint.getIgnoreCount(), breakpoint.getCondition(), getThreadNames( breakpoint ) );
	}

	private String[] getThreadNames( ICBreakpoint breakpoint ) {
		try {
			ICThread[] threads = getFilterExtension(breakpoint).getThreadFilters( getDebugTarget() );
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
	
	private ICLineBreakpoint createLocationBreakpoint( ICDILocationBreakpoint cdiBreakpoint ) {
		ICLineBreakpoint breakpoint = null;
		try {
			ICDILocator location = cdiBreakpoint.getLocator();
			String file = location.getFile();
			String sourceHandle = file;
			IResource resource = getProject();
			if (file != null && file.length() > 0) {
				Object sourceElement = getSourceElement(file);
				if ( sourceElement instanceof IFile ) {
					sourceHandle = ((IFile)sourceElement).getLocation().toOSString();
					resource = (IResource)sourceElement;
				}
				else if ( sourceElement instanceof IStorage ) {
					sourceHandle = ((IStorage)sourceElement).getFullPath().toOSString();
					resource = ResourcesPlugin.getWorkspace().getRoot();
				}
				else if ( sourceElement instanceof ITranslationUnit ) {
					ITranslationUnit translationUnit = (ITranslationUnit)sourceElement;
					sourceHandle = translationUnit.getPath().toString();
					resource = translationUnit.getResource();

					// an IExternalTranslationUnit doesn't have an IResource
					if (resource == null) {
						resource = getProject();
					}
				}
			} else {
				sourceHandle = getExecFileHandle();
			}
			if ( cdiBreakpoint instanceof ICDILineBreakpoint ) {
				breakpoint = createLineBreakpoint( sourceHandle, resource, cdiBreakpoint );
			}
			else if ( cdiBreakpoint instanceof ICDIFunctionBreakpoint ) {
				breakpoint = createFunctionBreakpoint( sourceHandle, resource,cdiBreakpoint );
			}
			else if ( cdiBreakpoint instanceof ICDIAddressBreakpoint ) {
				breakpoint = createAddressBreakpoint( sourceHandle, resource,cdiBreakpoint );
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
																		  getCdiBreakpointType(cdiBreakpoint),
																		  cdiBreakpoint.getLocator().getLineNumber(), 
																		  cdiBreakpoint.isEnabled(), 
																		  cdiBreakpoint.getCondition().getIgnoreCount(), 
																		  cdiBreakpoint.getCondition().getExpression(), 
																		  false );
//		ICDILocator locator = cdiBreakpoint.getLocator();
//		if ( locator != null ) {
//			BigInteger address = locator.getAddress();
//			if ( address != null ) {
//				breakpoint.setAddress( address.toString() );				
//			}
//		}
		return breakpoint;
	}

	/**
	 * Utility method that queries the CDI client for the breakpoint type.
	 * 
	 * @param cdiBreakpoint
	 *            the CDI breakpoint
	 * @return an ICDIBreakpointType constant
	 */
	@SuppressWarnings("deprecation")
	private int getCdiBreakpointType(ICDIBreakpoint cdiBreakpoint) {
		if (cdiBreakpoint instanceof ICDIBreakpoint2) {
			// the new way
			return ((ICDIBreakpoint2)cdiBreakpoint).getType();
		}
		else {
			// the old way
			int type = cdiBreakpoint.isHardware() ? ICBreakpointType.HARDWARE : ICBreakpointType.REGULAR;
			if (cdiBreakpoint.isTemporary()) {
				type |= ICBreakpointType.TEMPORARY;
			}
			return type;
		}
	}
	
	private ICFunctionBreakpoint createFunctionBreakpoint( String sourceHandle, IResource resource, ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException {
		ICDILocator location = cdiBreakpoint.getLocator();
		int line = location.getLineNumber();
		ICFunctionBreakpoint breakpoint = CDIDebugModel.createFunctionBreakpoint( 
				sourceHandle, 
				resource,
				getCdiBreakpointType(cdiBreakpoint),
				location.getFunction(), 
				-1, 
				-1, 
				line, 
				cdiBreakpoint.isEnabled(), 
				cdiBreakpoint.getCondition().getIgnoreCount(), 
				cdiBreakpoint.getCondition().getExpression(), 
				false);
		return breakpoint;
	}

	private ICAddressBreakpoint createAddressBreakpoint( String sourceHandle, IResource resource, ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException {
		String binary = getExecFileHandle();
		IAddress address = getDebugTarget().getAddressFactory().createAddress( cdiBreakpoint.getLocator().getAddress() );
		ICDILocator location = cdiBreakpoint.getLocator();
		int line = location.getLineNumber();
		ICAddressBreakpoint breakpoint = CDIDebugModel.createAddressBreakpoint( 
				binary, 
				sourceHandle, 
				resource, 
				ICBreakpointType.REGULAR, 
				line, 
				address, 
				cdiBreakpoint.isEnabled(), 
				cdiBreakpoint.getCondition().getIgnoreCount(), 
				cdiBreakpoint.getCondition().getExpression(), 
				false);
		return breakpoint;
	}

	private ICWatchpoint createWatchpoint( ICDIWatchpoint cdiWatchpoint ) throws CDIException, CoreException {
		String sourceHandle = getExecFileHandle();
		ICWatchpoint watchpoint = null;
		if ( cdiWatchpoint instanceof ICDIWatchpoint2 ){
			watchpoint = CDIDebugModel.createWatchpoint( sourceHandle, 
														 getProject(), 
														 cdiWatchpoint.isWriteType(), 
														 cdiWatchpoint.isReadType(), 
														 cdiWatchpoint.getWatchExpression(), 
														 ( (ICDIWatchpoint2)cdiWatchpoint ).getMemorySpace(),
														 ( (ICDIWatchpoint2)cdiWatchpoint ).getRange(),
														 cdiWatchpoint.isEnabled(), 
														 cdiWatchpoint.getCondition().getIgnoreCount(), 
														 cdiWatchpoint.getCondition().getExpression(), 
														 false);
		} else {
			watchpoint = CDIDebugModel.createWatchpoint( sourceHandle, 
														 getProject(), 
														 cdiWatchpoint.isWriteType(), 
														 cdiWatchpoint.isReadType(), 
														 cdiWatchpoint.getWatchExpression(), 
														 cdiWatchpoint.isEnabled(), 
														 cdiWatchpoint.getCondition().getIgnoreCount(), 
														 cdiWatchpoint.getCondition().getExpression(), 
														 false );
		}
		return watchpoint;
	}
	
	private ICEventBreakpoint createEventBreakpoint(ICDIEventBreakpoint cdiEventBkpt) throws CDIException,
			CoreException {

		ICEventBreakpoint eventBkpt;
		eventBkpt = CDIDebugModel.eventBreakpointExists(cdiEventBkpt.getEventType(), cdiEventBkpt
				.getExtraArgument());
		if (eventBkpt != null)
			return eventBkpt;
		eventBkpt = CDIDebugModel.createEventBreakpoint(cdiEventBkpt.getEventType(), cdiEventBkpt
				.getExtraArgument(), false);
		return eventBkpt;
	}

	private void changeBreakpointProperties( ICBreakpoint breakpoint, IMarkerDelta delta ) {
		ICDIBreakpoint cdiBreakpoint = null;
		synchronized( getBreakpointMap() ) {
			if ( !getBreakpointMap().isInProgress( breakpoint ) )
				cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
		}
		if ( cdiBreakpoint == null )
			return;
		ICDITarget cdiTarget = getCDITarget();
		try {
			boolean enabled = breakpoint.isEnabled() && DebugPlugin.getDefault().getBreakpointManager().isEnabled();
			boolean oldEnabled = ( delta != null ) ? delta.getAttribute( IBreakpoint.ENABLED, true ) : enabled;
			int ignoreCount = breakpoint.getIgnoreCount();
			int oldIgnoreCount = ( delta != null ) ? delta.getAttribute( ICBreakpoint.IGNORE_COUNT, 0 ) : ignoreCount;
			String condition = breakpoint.getCondition();
			String oldCondition = ( delta != null ) ? delta.getAttribute( ICBreakpoint.CONDITION, "" ) : condition; //$NON-NLS-1$
			String[] newThreadIs = getThreadNames( breakpoint );
			Boolean enabled0 = null;
			ICDICondition condition0 = null;
			if ( enabled != oldEnabled && enabled != cdiBreakpoint.isEnabled() ) {
				enabled0 = Boolean.valueOf( enabled );
			}
			if ( ignoreCount != oldIgnoreCount || condition.compareTo( oldCondition ) != 0 || areThreadFiltersChanged( newThreadIs, cdiBreakpoint ) ) {
				ICDICondition cdiCondition = cdiTarget.createCondition( ignoreCount, condition, newThreadIs  );
				if ( !cdiCondition.equals( cdiBreakpoint.getCondition() ) ) {
					condition0 = cdiCondition;
				}
			}
			int line = 0;
			if (breakpoint instanceof ILineBreakpoint) {
				ILineBreakpoint l = (ILineBreakpoint) breakpoint;
				line = l.getLineNumber();
			}
			int oldLine = ( delta != null ) ? delta.getAttribute( IMarker.LINE_NUMBER, 0 ) : 0; //$NON-NLS-1$
			boolean basic = oldLine>0 && oldLine != line;
			
			if (basic) {
				final ICBreakpoint[] breakpoints = new ICBreakpoint[] {breakpoint};
				breakpointsRemoved(breakpoints, null);
				handleBreakpointDestroyedEvent(cdiBreakpoint); // events has to processed before add executes
				breakpointsAdded(breakpoints);
			} else if (enabled0 != null || condition0 != null) {
				changeBreakpointPropertiesOnTarget(cdiBreakpoint, enabled0, condition0);
			}
		}
		catch( CoreException e ) {
		}
		catch( CDIException e ) {
		}
	}

	private void changeBreakpointProperties( ICBreakpoint breakpoint, ICDIBreakpoint cdiBreakpoint ) {
		Boolean enabled = null;
		try {
			boolean shouldBeEnabled = breakpoint.isEnabled() && DebugPlugin.getDefault().getBreakpointManager().isEnabled();
			if ( cdiBreakpoint.isEnabled() != shouldBeEnabled )
				enabled = shouldBeEnabled;
		}
		catch( CDIException e ) {
		}
		catch( CoreException e ) {
		}
		ICDICondition condition = null;
		try {
			ICDICondition c = createCondition( breakpoint );
			if ( !cdiBreakpoint.getCondition().equals( c ) )
				condition = c;
		}
		catch( CDIException e ) {
		}
		catch( CoreException e ) {
		}
		if ( enabled != null || condition != null )
			changeBreakpointPropertiesOnTarget( cdiBreakpoint, enabled, condition );
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

	public void setInitialBreakpoints() {
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] bps = manager.getBreakpoints( CDIDebugModel.getPluginIdentifier() );

		ICDITargetConfiguration config = getDebugTarget().getCDITarget().getConfiguration();

		if (!(config instanceof ICDITargetConfiguration2) || !((ICDITargetConfiguration2)config).supportsAddressBreaksOnStartup())
		{ // Disable address breaks of the target does not support setting them on startup
			for( int i = 0; i < bps.length; i++ ) {
				if ( bps[i] instanceof ICBreakpoint && isTargetBreakpoint( (ICBreakpoint)bps[i] ) && !getBreakpointMap().isRegistered( (ICBreakpoint)bps[i] ) ) {
					if ( bps[i] instanceof ICAddressBreakpoint ) {
						// disable address breakpoints to prevent the debugger to insert them prematurely
						try {
							bps[i].setEnabled( false );
						}
						catch( CoreException e ) {
						}
					}
				}
			}			
		}
		
		ICBreakpoint[] breakpoints = register( bps );
		setBreakpointsOnTarget0( breakpoints );
	}

	/**
	 * Checks if matching between the symbolics referenced by the breakpoint
	 * and the symbolics of the contained CDebugTarget should be done using also source handle.
	 * @param breakpoint
	 * @return true if source handle should be used
	 */
	private boolean breakpointUsesSourceMatching(ICBreakpoint breakpoint) {
		boolean result = false;
		if (breakpoint instanceof ICLineBreakpoint) {
			result = true;
			if (breakpoint instanceof ICFunctionBreakpoint) {
				// ICDIFunctionBreakpoint on function elements from binary objects can be
				// set without having a source handle. For this case of line breakpoint 
				// don't try to match breakpoints with source locator of contained CDebugTarget.
				String handle = null;
				try {
					handle = breakpoint.getSourceHandle();
				} catch (CoreException ex) {
					// ignore exception. source handle will be empty anyway.
				}
				result = (handle != null) && (handle.length() > 0);

			}
		}
		return result;
	}
	
	private boolean isTargetBreakpoint( ICBreakpoint breakpoint ) {
		if ( breakpoint instanceof ICAddressBreakpoint )
			return supportsAddressBreakpoint( (ICAddressBreakpoint)breakpoint );

		// If the breakpoint is set on a resource in this project
		// it should be enabled irrespective of what the CSourceLookupDirector thinks
		if (breakpoint.getMarker() != null) {
			IProject project = breakpoint.getMarker().getResource().getProject();
			if (getProject().equals(project))
				return true;
			if (CDebugUtils.isReferencedProject(getProject(), project))
				return true;
		}

		// Is it a line breakpoint with source handle ?
		if ( breakpointUsesSourceMatching( breakpoint ) ) {
			try {
				String handle = breakpoint.getSourceHandle();
				ISourceLocator sl = getSourceLocator();
				if ( sl instanceof ICSourceLocator )
					return ( ((ICSourceLocator)sl).findSourceElement( handle ) != null );
				else if ( sl instanceof CSourceLookupDirector ) {
					return ( ((CSourceLookupDirector)sl).contains( breakpoint ) );
				}
			}
			catch( CoreException e ) {
				CDebugCorePlugin.log(e);
			}
		} else {
			// Check the marker resource against the source containers ...
			IResource resource = breakpoint.getMarker().getResource();			
			IProject project = resource.getProject();
			if ( project != null && project.exists() ) {
				ISourceLocator sl = getSourceLocator();
				if ( sl instanceof ICSourceLocator )
					return ((ICSourceLocator)sl).contains( project );
				else if ( sl instanceof CSourceLookupDirector )
					return ((CSourceLookupDirector)sl).contains( project );
			}
		}
		// Allow unclassified breakpoints i.e. those which aren't project scoped,
		// or not resource related (e.g. watchpoints)
		return true;
	}

	public boolean supportsBreakpoint( ICBreakpoint breakpoint ) {
		boolean s = false;
		synchronized( getBreakpointMap() ) {
			s = getBreakpointMap().isRegistered( breakpoint );
		}
		return s;
	}

	/**
	 * Checks for a match between the symbolics referenced by the breakpoint
	 * and the symbolics of the contained CDebugTarget.
	 * @param breakpoint
	 * @return true if the symbolics match or if the breakpoint has no symbolics
	 */
	public boolean supportsAddressBreakpoint( ICAddressBreakpoint breakpoint ) {
		boolean sessionHasSymbols = getExecFileHandle() != null && getExecFileHandle().length() > 0;
		boolean bpHasSymbols = false;
		try {
			String module = breakpoint.getModule();
			if ( module != null && module.length() > 0 ) {
				bpHasSymbols = true;
				if ( sessionHasSymbols ) {
					return getExecFileHandle().equals( module );				
				}
			}
		}
		catch( CoreException e ) {
		}
		
		// supporting old breakpoints (> 3.0)
		try {
			String sourceHandle = breakpoint.getSourceHandle();
			if ( sourceHandle != null && sourceHandle.length() > 0 ) {
				bpHasSymbols = true;
				if ( sessionHasSymbols ) {
					return getExecFileHandle().equals( sourceHandle );
				}
			}
		}
		catch( CoreException e ) {
		}
		
		// an address breakpoint can also be set in the absence of any symbols
		return !bpHasSymbols;
	}

	public void skipBreakpoints( boolean enabled ) {
		if ( fSkipBreakpoint != enabled && (DebugPlugin.getDefault().getBreakpointManager().isEnabled() || !enabled) ) {
			fSkipBreakpoint = enabled;
			doSkipBreakpoints( enabled );
		}
	}

	public void watchpointOutOfScope( ICDIWatchpoint cdiWatchpoint ) {
		handleBreakpointDestroyedEvent( cdiWatchpoint );
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

	private IPath convertPath( String sourceHandle ) {
		IPath path = null;
		if ( Path.EMPTY.isValidPath( sourceHandle ) ) {
			ISourceLocator sl = getSourceLocator();
			if ( sl instanceof CSourceLookupDirector ) {
				path = ((CSourceLookupDirector)sl).getCompilationPath( sourceHandle );
			}
			if ( path == null ) {
				path = new Path( sourceHandle );
			}
		}
		return path;
	}

	private IProject getProject() {
		return getDebugTarget().getProject();
	}

	private String getExecFileHandle() {
		CDebugTarget target = getDebugTarget();
		if ( target != null ) {
			IBinaryObject binary = target.getExecFile();
			if ( binary != null ) {
				IPath path = binary.getPath();
				if ( path != null ) {
					return path.toOSString();
				}
			}
		}
		return null;
	}

	private ISourceLocator getSourceLocator() {
		return getDebugTarget().getLaunch().getSourceLocator();
	}

	protected Object getSourceElement( String file ) {
		Object sourceElement = null;
		ISourceLocator locator = getSourceLocator();
		if ( locator instanceof ICSourceLocator || locator instanceof CSourceLookupDirector ) {
			if ( locator instanceof ICSourceLocator )
				sourceElement = ((ICSourceLocator)locator).findSourceElement( file );
			else
				sourceElement = ((CSourceLookupDirector)locator).getSourceElement( file );
		}
		return sourceElement;
	}

	protected boolean isEmpty( String str ) {
		return !( str != null && str.trim().length() > 0 );
	}

	private boolean isTargetAvailable() {
		return getDebugTarget().getCDITarget().getConfiguration().supportsBreakpoints() && getDebugTarget().isAvailable();
	}

	private CBreakpointNotifier getBreakpointNotifier() {
		return CBreakpointNotifier.getInstance();
	}

	private boolean isFilteredByTarget( ICBreakpoint breakpoint, ICDebugTarget target ) {
		boolean result = false;
		try {
			ICDebugTarget[] tfs = getFilterExtension(breakpoint).getTargetFilters();
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
	
	private ICBreakpointFilterExtension getFilterExtension(ICBreakpoint bp) throws CoreException{
	    return (ICBreakpointFilterExtension)bp.getExtension(
	        CDIDebugModel.getPluginIdentifier(), ICBreakpointFilterExtension.class);
	}
}
