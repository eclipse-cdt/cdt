/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICGlobalVariableManager;
import org.eclipse.cdt.debug.core.ICMemoryManager;
import org.eclipse.cdt.debug.core.ICRegisterManager;
import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.cdt.debug.core.ICSignalManager;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICDIErrorInfo;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryEvent;
import org.eclipse.cdt.debug.core.cdi.ICDISignalReceived;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointScope;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDisconnectedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIRestartedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.model.CDebugElementState;
import org.eclipse.cdt.debug.core.model.IBreakpointTarget;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICExpressionEvaluator;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.core.model.IDebuggerProcessSupport;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.cdt.debug.core.model.IJumpToAddress;
import org.eclipse.cdt.debug.core.model.IJumpToLine;
import org.eclipse.cdt.debug.core.model.IRunToAddress;
import org.eclipse.cdt.debug.core.model.IRunToLine;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.core.CBreakpointManager;
import org.eclipse.cdt.debug.internal.core.CExpressionTarget;
import org.eclipse.cdt.debug.internal.core.CGlobalVariableManager;
import org.eclipse.cdt.debug.internal.core.CMemoryManager;
import org.eclipse.cdt.debug.internal.core.CRegisterManager;
import org.eclipse.cdt.debug.internal.core.CSharedLibraryManager;
import org.eclipse.cdt.debug.internal.core.CSignalManager;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IExpressionListener;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IThread;

/**
 * Debug target for C/C++ debug model.
 */
public class CDebugTarget extends CDebugElement implements ICDebugTarget, ICDIEventListener, ILaunchListener, IExpressionListener {

	/**
	 * Threads contained in this debug target. 
	 * When a thread starts it is added to the list. 
	 * When a thread ends it is removed from the list.
	 */
	private ArrayList fThreads;

	/**
	 * Associated inferrior process, or <code>null</code> if not available.
	 */
	private IProcess fDebuggeeProcess = null;

	/**
	 * The underlying CDI target.
	 */
	private ICDITarget fCDITarget;

	/**
	 * The name of this target.
	 */
	private String fName;

	/**
	 * The launch this target is contained in
	 */
	private ILaunch fLaunch;

	/**
	 * The debug configuration of this session
	 */
	private ICDIConfiguration fConfig;

	/**
	 * The memory manager for this target.
	 */
	private CMemoryManager fMemoryManager;

	/**
	 * The disassembly manager for this target.
	 */
	private Disassembly fDisassembly;

	/**
	 * The shared library manager for this target.
	 */
	private CSharedLibraryManager fSharedLibraryManager;

	/**
	 * The signal manager for this target.
	 */
	private CSignalManager fSignalManager;

	/**
	 * The register manager for this target.
	 */
	private CRegisterManager fRegisterManager;

	/**
	 * A breakpoint manager for this target.
	 */
	private CBreakpointManager fBreakpointManager;

	/**
	 * The expression evaluation target.
	 */
	private CExpressionTarget fExpressionTarget;

	/**
	 * The global variable manager for this target.
	 */
	private CGlobalVariableManager fGlobalVariableManager;

	/**
	 * The executable file associated with this target.
	 */
	private IFile fExecFile;

	/**
	 * Whether the target is little endian.
	 */
	private Boolean fIsLittleEndian = null;

	/**
	 * The target's preference set.
	 */
	private Preferences fPreferences = null;

	/**
	 * Constructor for CDebugTarget.
	 */
	public CDebugTarget( ILaunch launch, ICDITarget cdiTarget, String name, IProcess debuggeeProcess, IProcess debuggerProcess, IFile file, boolean allowsTerminate, boolean allowsDisconnect ) {
		super( null );
		setLaunch( launch );
		setDebugTarget( this );
		setName( name );
		setProcess( debuggeeProcess );
		setExecFile( file );
		setCDITarget( cdiTarget );
		setState( CDebugElementState.SUSPENDED );
		initializePreferences();
		setConfiguration( cdiTarget.getSession().getConfiguration() );
		setThreadList( new ArrayList( 5 ) );
		createDisassembly();
		setSharedLibraryManager( new CSharedLibraryManager( this ) );
		setSignalManager( new CSignalManager( this ) );
		setRegisterManager( new CRegisterManager( this ) );
		setBreakpointManager( new CBreakpointManager( this ) );
		setGlobalVariableManager( new CGlobalVariableManager( this ) );
		initialize();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener( this );
		DebugPlugin.getDefault().getExpressionManager().addExpressionListener( this );
		getCDISession().getEventManager().addEventListener( this );
	}

	protected void initialize() {
		ArrayList debugEvents = new ArrayList( 1 );
		debugEvents.add( createCreateEvent() );
		initializeThreads( debugEvents );
		initializeBreakpoints();
		initializeRegisters();
		initializeMemoryManager();
		initializeSourceManager();
		getLaunch().addDebugTarget( this );
		fireEventSet( (DebugEvent[])debugEvents.toArray( new DebugEvent[debugEvents.size()] ) );
	}

	private void initializeBreakpoints() {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener( this );
		setBreakpoints();
	}

	/**
	 * Adds all of the pre-existing threads to this debug target.
	 */
	protected void initializeThreads( List debugEvents ) {
		ICDIThread[] cdiThreads = new ICDIThread[0];
		try {
			cdiThreads = getCDITarget().getThreads();
		}
		catch( CDIException e ) {
			// ignore
		}
		for( int i = 0; i < cdiThreads.length; ++i ) {
			CThread thread = createThread( cdiThreads[i] );
			debugEvents.add( thread.createCreateEvent() );
		}
	}

	/**
	 * Installs all C/C++ breakpoints that currently exist in the breakpoint manager.
	 */
	public void setBreakpoints() {
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] bps = manager.getBreakpoints( CDebugModel.getPluginIdentifier() );
		for( int i = 0; i < bps.length; i++ ) {
			if ( bps[i] instanceof ICBreakpoint && getBreakpointManager().isTargetBreakpoint( (ICBreakpoint)bps[i] ) && !getBreakpointManager().isCDIRegistered( (ICBreakpoint)bps[i] ) ) {
				if ( bps[i] instanceof ICAddressBreakpoint ) {
					// disable address breakpoints to prevent the debugger to insert them prematurely
					try {
						bps[i].setEnabled( false );
					}
					catch( CoreException e ) {
					}
				}
				breakpointAdded0( bps[i] );
			}
		}
	}

	protected void initializeRegisters() {
		getRegisterManager().initialize();
	}

	protected void initializeMemoryManager() {
		fMemoryManager = new CMemoryManager( this );
	}

	protected void initializeSourceManager() {
		ISourceLocator locator = getLaunch().getSourceLocator();
		if ( locator instanceof IAdaptable ) {
			ICSourceLocator clocator = (ICSourceLocator)((IAdaptable)locator).getAdapter( ICSourceLocator.class );
			if ( clocator instanceof IAdaptable ) {
				CSourceManager sm = (CSourceManager)((IAdaptable)clocator).getAdapter( CSourceManager.class );
				if ( sm != null )
					sm.setDebugTarget( this );
			}
			IResourceChangeListener listener = (IResourceChangeListener)((IAdaptable)locator).getAdapter( IResourceChangeListener.class );
			if ( listener != null )
				CCorePlugin.getWorkspace().addResourceChangeListener( listener );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getProcess()
	 */
	public IProcess getProcess() {
		return fDebuggeeProcess;
	}

	/**
	 * Sets the process associated with this debug target, possibly <code>null</code>. Set on creation.
	 * 
	 * @param process the system process associated with the underlying CDI target, 
	 * or <code>null</code> if no process is associated with this debug target 
	 * (for a core dump debugging).
	 */
	protected void setProcess( IProcess debuggeeProcess ) {
		fDebuggeeProcess = debuggeeProcess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugTarget#getThreads()
	 */
	public IThread[] getThreads() {
		List threads = getThreadList();
		return (IThread[])threads.toArray( new IThread[threads.size()] );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugTarget#hasThreads()
	 */
	public boolean hasThreads() throws DebugException {
		return getThreadList().size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugTarget#getName()
	 */
	public String getName() throws DebugException {
		return fName;
	}

	/**
	 * Sets the name of this debug target.
	 * 
	 * @param name the name of this debug target
	 */
	protected void setName( String name ) {
		fName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public boolean supportsBreakpoint( IBreakpoint breakpoint ) {
		if ( !getConfiguration().supportsBreakpoints() )
			return false;
		return (breakpoint instanceof ICBreakpoint && getBreakpointManager().isCDIRegistered( (ICBreakpoint)breakpoint ));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug.core.ILaunch)
	 */
	public void launchRemoved( ILaunch launch ) {
		if ( !isAvailable() ) {
			return;
		}
		if ( launch.equals( getLaunch() ) ) {
			// This target has been deregistered, but it hasn't been successfully terminated.
			// Update internal state to reflect that it is disconnected
			disconnected();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.core.ILaunch)
	 */
	public void launchAdded( ILaunch launch ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchChanged(org.eclipse.debug.core.ILaunch)
	 */
	public void launchChanged( ILaunch launch ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return supportsTerminate() && isAvailable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return ( getState().equals( CDebugElementState.TERMINATED ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		if ( !canTerminate() ) {
			return;
		}
		final CDebugElementState oldState = getState();
		setState( CDebugElementState.TERMINATING );
		DebugPlugin.getDefault().asyncExec( new Runnable() {

			public void run() {
				try {
					getCDITarget().terminate();
				}
				catch( CDIException e ) {
					setState( oldState );
					try {
						targetRequestFailed( e.getMessage(), e );
					}
					catch( DebugException e1 ) {
						CDebugUtils.error( e1.getStatus(), CDebugTarget.this );
					}
				}
			}
		} );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return getConfiguration().supportsResume() && isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		if ( !getConfiguration().supportsSuspend() )
			return false;
		if ( getState().equals( CDebugElementState.RESUMED ) ) {
			// only allow suspend if no threads are currently suspended
			IThread[] threads = getThreads();
			for( int i = 0; i < threads.length; i++ ) {
				if ( threads[i].isSuspended() ) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return ( getState().equals( CDebugElementState.SUSPENDED ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		if ( !canResume() )
			return;
		final CDebugElementState oldState = getState();
		setState( CDebugElementState.RESUMING );
		DebugPlugin.getDefault().asyncExec( new Runnable() {

			public void run() {
				try {
					getCDITarget().resume();
				}
				catch( CDIException e ) {
					setState( oldState );
					try {
						targetRequestFailed( e.getMessage(), e );
					}
					catch( DebugException e1 ) {
						CDebugUtils.error( e1.getStatus(), CDebugTarget.this );
					}
				}
			}
		} );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		if ( !canSuspend() )
			return;
		final CDebugElementState oldState = getState();
		setState( CDebugElementState.SUSPENDING );
		DebugPlugin.getDefault().asyncExec( new Runnable() {

			public void run() {
				try {
					getCDITarget().suspend();
				}
				catch( CDIException e ) {
					setState( oldState );
					try {
						targetRequestFailed( e.getMessage(), e );
					}
					catch( DebugException e1 ) {
						CDebugUtils.error( e1.getStatus(), CDebugTarget.this );
					}
				}
			}
		} );
	}

	protected boolean isSuspending() {
		return ( getState().equals( CDebugElementState.SUSPENDING ) );
	}

	/**
	 * Notifies threads that the target has been suspended. 
	 */
	protected void suspendThreads( ICDISuspendedEvent event ) {
		Iterator it = getThreadList().iterator();
		while( it.hasNext() ) {
			CThread thread = (CThread)it.next();
			ICDIThread suspensionThread = null;
			try {
				suspensionThread = getCDITarget().getCurrentThread();
			}
			catch( CDIException e ) {
				// ignore
			}
			thread.suspendByTarget( event.getReason(), suspensionThread );
		}
	}

	/**
	 * Refreshes the thread list.
	 */
	protected synchronized List refreshThreads() {
		ArrayList newThreads = new ArrayList( 5 );
		ArrayList list = new ArrayList( 5 );
		ArrayList debugEvents = new ArrayList( 5 );
		List oldList = (List)getThreadList().clone();
		ICDIThread[] cdiThreads = new ICDIThread[0];
		ICDIThread currentCDIThread = null;
		try {
			cdiThreads = getCDITarget().getThreads();
			currentCDIThread = getCDITarget().getCurrentThread();
		}
		catch( CDIException e ) {
			CDebugCorePlugin.log( e );
		}
		for( int i = 0; i < cdiThreads.length; ++i ) {
			CThread thread = findThread( oldList, cdiThreads[i] );
			if ( thread == null ) {
				thread = new CThread( this, cdiThreads[i] );
				newThreads.add( thread );
			}
			else {
				oldList.remove( thread );
			}
			thread.setCurrent( cdiThreads[i].equals( currentCDIThread ) );
			list.add( thread );
		}
		Iterator it = oldList.iterator();
		while( it.hasNext() ) {
			CThread thread = (CThread)it.next();
			thread.terminated();
			debugEvents.add( thread.createTerminateEvent() );
		}
		setThreadList( list );
		it = newThreads.iterator();
		while( it.hasNext() ) {
			debugEvents.add( ((CThread)it.next()).createCreateEvent() );
		}
		if ( debugEvents.size() > 0 )
			fireEventSet( (DebugEvent[])debugEvents.toArray( new DebugEvent[debugEvents.size()] ) );
		return newThreads;
	}

	/**
	 * Notifies threads that the target has been resumed.
	 */
	protected synchronized void resumeThreads( List debugEvents, int detail ) {
		Iterator it = getThreadList().iterator();
		while( it.hasNext() ) {
			((CThread)it.next()).resumedByTarget( detail, debugEvents );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointAdded( IBreakpoint breakpoint ) {
		if ( !(breakpoint instanceof ICBreakpoint) || !isAvailable() || !getBreakpointManager().isTargetBreakpoint( (ICBreakpoint)breakpoint ) )
			return;
		breakpointAdded0( breakpoint );
	}

	private void breakpointAdded0( IBreakpoint breakpoint ) {
		if ( !isAvailable() )
			return;
		if ( breakpoint instanceof ICAddressBreakpoint && !getBreakpointManager().supportsAddressBreakpoint( (ICAddressBreakpoint)breakpoint ) )
			return;
		if ( getConfiguration().supportsBreakpoints() ) {
			try {
				getBreakpointManager().setBreakpoint( (ICBreakpoint)breakpoint );
			}
			catch( DebugException e ) {
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointRemoved( IBreakpoint breakpoint, IMarkerDelta delta ) {
		if ( !(breakpoint instanceof ICBreakpoint) || !isAvailable() || !getBreakpointManager().isCDIRegistered( (ICBreakpoint)breakpoint ) )
			return;
		try {
			getBreakpointManager().removeBreakpoint( (ICBreakpoint)breakpoint );
		}
		catch( DebugException e ) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointChanged( IBreakpoint breakpoint, IMarkerDelta delta ) {
		if ( !(breakpoint instanceof ICBreakpoint) || !isAvailable() )
			return;
		ICBreakpoint b = (ICBreakpoint)breakpoint;
		boolean install = false;
		try {
			ICDebugTarget[] tfs = b.getTargetFilters();
			install = Arrays.asList( tfs ).contains( this );
		}
		catch( CoreException e1 ) {
		}
		boolean registered = getBreakpointManager().isCDIRegistered( b );
		if ( registered && !install ) {
			try {
				getBreakpointManager().removeBreakpoint( b );
			}
			catch( DebugException e ) {
			}
		}
		if ( !registered && install ) {
			try {
				getBreakpointManager().setBreakpoint( b );
			}
			catch( DebugException e ) {
			}
		}
		if ( delta != null ) {
			try {
				getBreakpointManager().changeBreakpointProperties( b, delta );
			}
			catch( DebugException e ) {
			}
		}
	}

	/**
	 * Returns whether this debug target supports disconnecting.
	 * 
	 * @return whether this debug target supports disconnecting
	 */
	protected boolean supportsDisconnect() {
		return getConfiguration().supportsDisconnect();
	}

	/**
	 * Returns whether this debug target supports termination.
	 * 
	 * @return whether this debug target supports termination
	 */
	protected boolean supportsTerminate() {
		return getConfiguration().supportsTerminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#canDisconnect()
	 */
	public boolean canDisconnect() {
		return supportsDisconnect() && isAvailable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#disconnect()
	 */
	public void disconnect() throws DebugException {
		if ( isDisconnecting() ) {
			return;
		}
		if ( !supportsDisconnect() ) {
			notSupported( CoreModelMessages.getString( "CDebugTarget.0" ) ); //$NON-NLS-1$
		}
		final CDebugElementState oldState = getState();
		setState( CDebugElementState.DISCONNECTING );
		DebugPlugin.getDefault().asyncExec( new Runnable() {

			public void run() {
				try {
					getCDITarget().disconnect();
				}
				catch( CDIException e ) {
					setState( oldState );
					try {
						targetRequestFailed( e.getMessage(), e );
					}
					catch( DebugException e1 ) {
						CDebugUtils.error( e1.getStatus(), CDebugTarget.this );
					}
				}
			}
		} );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#isDisconnected()
	 */
	public boolean isDisconnected() {
		return ( getState().equals( CDebugElementState.DISCONNECTED ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	public boolean supportsStorageRetrieval() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
	 */
	public IMemoryBlock getMemoryBlock( long startAddress, long length ) throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/**
	 * Sets the launch this target is contained in
	 * 
	 * @param launch the launch this target is contained in
	 */
	private void setLaunch( ILaunch launch ) {
		fLaunch = launch;
	}

	/**
	 * Returns the list of threads contained in this debug target.
	 * 
	 * @return list of threads
	 */
	protected ArrayList getThreadList() {
		return fThreads;
	}

	/**
	 * Sets the list of threads contained in this debug target. Set to an empty collection on creation. Threads are added and removed as they start and end. On
	 * termination this collection is set to the immutable singleton empty list.
	 * 
	 * @param threads empty list
	 */
	private void setThreadList( ArrayList threads ) {
		fThreads = threads;
	}

	private void setCDITarget( ICDITarget cdiTarget ) {
		fCDITarget = cdiTarget;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( adapter.equals( ICDebugElement.class ) )
			return this;
		if ( adapter.equals( CDebugElement.class ) )
			return this;
		if ( adapter.equals( IDebugTarget.class ) )
			return this;
		if ( adapter.equals( ICDebugTarget.class ) )
			return this;
		if ( adapter.equals( CDebugTarget.class ) )
			return this;
		if ( adapter.equals( ICDITarget.class ) )
			return fCDITarget;
		if ( adapter.equals( ICExpressionEvaluator.class ) )
			return this;
		if ( adapter.equals( ICMemoryManager.class ) )
			return getMemoryManager();
		if ( adapter.equals( IDebuggerProcessSupport.class ) )
			return this;
		if ( adapter.equals( IExecFileInfo.class ) )
			return this;
		if ( adapter.equals( IRunToLine.class ) )
			return this;
		if ( adapter.equals( IRunToAddress.class ) )
			return this;
		if ( adapter.equals( IJumpToLine.class ) )
			return this;
		if ( adapter.equals( IJumpToAddress.class ) )
			return this;
		if ( adapter.equals( IBreakpointTarget.class ) )
			return this;
		if ( adapter.equals( CBreakpointManager.class ) )
			return getBreakpointManager();
		if ( adapter.equals( ICSharedLibraryManager.class ) )
			return getSharedLibraryManager();
		if ( adapter.equals( ICSignalManager.class ) )
			return getSignalManager();
		if ( adapter.equals( ICRegisterManager.class ) )
			return getRegisterManager();
		if ( adapter.equals( CExpressionTarget.class ) )
			return getExpressionTarget();
		if ( adapter.equals( ICGlobalVariableManager.class ) )
			return getGlobalVariableManager();
		if ( adapter.equals( ICDISession.class ) )
			return getCDISession();
		return super.getAdapter( adapter );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents( ICDIEvent[] events ) {
		for( int i = 0; i < events.length; i++ ) {
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();
			if ( source == null && event instanceof ICDIDestroyedEvent ) {
				handleTerminatedEvent( (ICDIDestroyedEvent)event );
			}
			else if ( source.getTarget().equals( getCDITarget() ) ) {
				if ( event instanceof ICDICreatedEvent ) {
					if ( source instanceof ICDIThread ) {
						handleThreadCreatedEvent( (ICDICreatedEvent)event );
					}
					if ( source instanceof ICDISharedLibrary ) {
						getSharedLibraryManager().sharedLibraryLoaded( (ICDISharedLibrary)source );
					}
				}
				else if ( event instanceof ICDISuspendedEvent ) {
					if ( source instanceof ICDITarget ) {
						handleSuspendedEvent( (ICDISuspendedEvent)event );
					}
				}
				else if ( event instanceof ICDIResumedEvent ) {
					if ( source instanceof ICDITarget ) {
						handleResumedEvent( (ICDIResumedEvent)event );
					}
				}
				else if ( event instanceof ICDIExitedEvent ) {
					if ( source instanceof ICDITarget ) {
						handleExitedEvent( (ICDIExitedEvent)event );
					}
				}
				else if ( event instanceof ICDIDestroyedEvent ) {
					if ( source instanceof ICDIThread ) {
						handleThreadTerminatedEvent( (ICDIDestroyedEvent)event );
					}
					if ( source instanceof ICDISharedLibrary ) {
						getSharedLibraryManager().sharedLibraryUnloaded( (ICDISharedLibrary)source );
					}
				}
				else if ( event instanceof ICDIDisconnectedEvent ) {
					if ( source instanceof ICDITarget ) {
						handleDisconnectedEvent( (ICDIDisconnectedEvent)event );
					}
				}
				else if ( event instanceof ICDIChangedEvent ) {
					if ( source instanceof ICDITarget ) {
						handleChangedEvent( (ICDIChangedEvent)event );
					}
					if ( source instanceof ICDISharedLibrary ) {
						getSharedLibraryManager().symbolsLoaded( (ICDISharedLibrary)source );
					}
					if ( source instanceof ICDISignal ) {
						getSignalManager().signalChanged( (ICDISignal)source );
					}
				}
				else if ( event instanceof ICDIRestartedEvent ) {
					if ( source instanceof ICDITarget ) {
						handleRestartedEvent( (ICDIRestartedEvent)event );
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRestart#canRestart()
	 */
	public boolean canRestart() {
		return getConfiguration().supportsRestart() && isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRestart#restart()
	 */
	public void restart() throws DebugException {
		if ( !canRestart() ) {
			return;
		}
		final CDebugElementState oldState = getState();
		setState( CDebugElementState.RESTARTING );
		ICDILocation location = getCDISession().getBreakpointManager().createLocation( "", "main", 0 ); //$NON-NLS-1$ //$NON-NLS-2$
		setInternalTemporaryBreakpoint( location );
		DebugPlugin.getDefault().asyncExec( new Runnable() {

			public void run() {
				try {
					getCDITarget().restart();
				}
				catch( CDIException e ) {
					setState( oldState );
					try {
						targetRequestFailed( e.getMessage(), e );
					}
					catch( DebugException e1 ) {
						CDebugUtils.error( e1.getStatus(), CDebugTarget.this );
					}
				}
			}
		} );
	}

	/**
	 * Returns whether this target is available to handle client requests.
	 * 
	 * @return whether this target is available to handle client requests
	 */
	public boolean isAvailable() {
		return !(isTerminated() || isTerminating() || isDisconnected() || isDisconnecting());
	}

	/**
	 * Returns whether this target is in the process of terminating.
	 * 
	 * @return whether this target is terminating
	 */
	protected boolean isTerminating() {
		return ( getState().equals( CDebugElementState.TERMINATING ) );
	}

	/**
	 * Updates the state of this target to be terminated, if not already terminated.
	 */
	protected void terminated() {
		if ( !isTerminated() ) {
			if ( !isDisconnected() ) {
				setState( CDebugElementState.TERMINATED );
			}
			cleanup();
			fireTerminateEvent();
		}
	}

	/**
	 * Returns whether this target is in the process of terminating.
	 * 
	 * @return whether this target is terminating
	 */
	protected boolean isDisconnecting() {
		return ( getState().equals( CDebugElementState.DISCONNECTING ) );
	}

	/**
	 * Updates the state of this target for disconnection.
	 */
	protected void disconnected() {
		if ( !isDisconnected() ) {
			setState( CDebugElementState.DISCONNECTED );
			cleanup();
			fireTerminateEvent();
		}
	}

	/**
	 * Cleans up the internal state of this debug target as a result of a session ending.
	 */
	protected void cleanup() {
		resetStatus();
		removeAllThreads();
		getCDISession().getEventManager().removeEventListener( this );
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener( this );
		DebugPlugin.getDefault().getExpressionManager().removeExpressionListener( this );
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener( this );
		saveGlobalVariables();
		disposeGlobalVariableManager();
		disposeMemoryManager();
		disposeSharedLibraryManager();
		disposeSignalManager();
		disposeRegisterManager();
		disposeDisassembly();
		disposeSourceManager();
		disposeBreakpointManager();
		disposeExpresionTarget();
		removeAllExpressions();
		disposePreferences();
	}

	/**
	 * Removes all threads from this target's collection of threads, firing a terminate event for each.
	 */
	protected void removeAllThreads() {
		List threads = getThreadList();
		setThreadList( new ArrayList( 0 ) );
		ArrayList debugEvents = new ArrayList( threads.size() );
		Iterator it = threads.iterator();
		while( it.hasNext() ) {
			CThread thread = (CThread)it.next();
			thread.terminated();
			debugEvents.add( thread.createTerminateEvent() );
		}
		fireEventSet( (DebugEvent[])debugEvents.toArray( new DebugEvent[debugEvents.size()] ) );
	}

	/**
	 * Removes all expressions from this target.
	 */
	protected void removeAllExpressions() {
		IExpressionManager em = DebugPlugin.getDefault().getExpressionManager();
		IExpression[] expressions = em.getExpressions();
		for( int i = 0; i < expressions.length; ++i ) {
			if ( expressions[i] instanceof CExpression && expressions[i].getDebugTarget().equals( this ) ) {
				em.removeExpression( expressions[i] );
			}
		}
	}

	/**
	 * Creates, adds and returns a thread for the given underlying CDI thread. A creation event is fired for the thread. Returns <code>null</code> if during
	 * the creation of the thread this target is set to the disconnected state.
	 * 
	 * @param thread the underlying CDI thread
	 * @return model thread
	 */
	protected CThread createThread( ICDIThread cdiThread ) {
		CThread thread = new CThread( this, cdiThread );
		getThreadList().add( thread );
		return thread;
	}

	private void handleSuspendedEvent( ICDISuspendedEvent event ) {
		setState( CDebugElementState.SUSPENDED );
		ICDISessionObject reason = event.getReason();
		setCurrentStateInfo( reason );
		// Reset the registers that have errors.
		getRegisterManager().targetSuspended();
		getBreakpointManager().skipBreakpoints( false );
		List newThreads = refreshThreads();
		if ( event.getSource() instanceof ICDITarget ) {
			suspendThreads( event );
		}
		// We need this for debuggers that don't have notifications
		// for newly created threads.
		else if ( event.getSource() instanceof ICDIThread ) {
			CThread thread = findThread( (ICDIThread)event.getSource() );
			if ( thread != null && newThreads.contains( thread ) ) {
				ICDIEvent[] evts = new ICDIEvent[]{ event };
				thread.handleDebugEvents( evts );
			}
		}
		if ( reason instanceof ICDIEndSteppingRange ) {
			handleEndSteppingRange( (ICDIEndSteppingRange)reason );
		}
		else if ( reason instanceof ICDIBreakpointHit ) {
			handleBreakpointHit( (ICDIBreakpointHit)reason );
		}
		else if ( reason instanceof ICDISignalReceived ) {
			handleSuspendedBySignal( (ICDISignalReceived)reason );
		}
		else if ( reason instanceof ICDIWatchpointTrigger ) {
			handleWatchpointTrigger( (ICDIWatchpointTrigger)reason );
		}
		else if ( reason instanceof ICDIWatchpointScope ) {
			handleWatchpointScope( (ICDIWatchpointScope)reason );
		}
		else if ( reason instanceof ICDIErrorInfo ) {
			handleErrorInfo( (ICDIErrorInfo)reason );
		}
		else if ( reason instanceof ICDISharedLibraryEvent ) {
			handleSuspendedBySolibEvent( (ICDISharedLibraryEvent)reason );
		}
		else { // reason is not specified
			fireSuspendEvent( DebugEvent.UNSPECIFIED );
		}
	}

	private void handleResumedEvent( ICDIResumedEvent event ) {
		setState( CDebugElementState.RESUMED );
		setCurrentStateInfo( null );
		resetStatus();
		ArrayList debugEvents = new ArrayList( 10 );
		int detail = DebugEvent.UNSPECIFIED;
		switch( event.getType() ) {
			case ICDIResumedEvent.CONTINUE:
				detail = DebugEvent.CLIENT_REQUEST;
				break;
			case ICDIResumedEvent.STEP_INTO:
			case ICDIResumedEvent.STEP_INTO_INSTRUCTION:
				detail = DebugEvent.STEP_INTO;
				break;
			case ICDIResumedEvent.STEP_OVER:
			case ICDIResumedEvent.STEP_OVER_INSTRUCTION:
				detail = DebugEvent.STEP_OVER;
				break;
			case ICDIResumedEvent.STEP_RETURN:
				detail = DebugEvent.STEP_RETURN;
				break;
		}
		debugEvents.add( createResumeEvent( detail ) );
		resumeThreads( debugEvents, detail );
		fireEventSet( (DebugEvent[])debugEvents.toArray( new DebugEvent[debugEvents.size()] ) );
	}

	private void handleEndSteppingRange( ICDIEndSteppingRange endSteppingRange ) {
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleBreakpointHit( ICDIBreakpointHit breakpointHit ) {
		fireSuspendEvent( DebugEvent.BREAKPOINT );
	}

	private void handleWatchpointTrigger( ICDIWatchpointTrigger wt ) {
		fireSuspendEvent( DebugEvent.BREAKPOINT );
	}

	private void handleWatchpointScope( ICDIWatchpointScope ws ) {
		ICBreakpoint watchpoint = getBreakpointManager().getBreakpoint( ws.getWatchpoint() );
		if ( watchpoint != null ) {
			try {
				getBreakpointManager().removeBreakpoint( watchpoint );
			}
			catch( DebugException e ) {
				CDebugCorePlugin.log( e );
			}
			fireSuspendEvent( DebugEvent.BREAKPOINT );
		}
	}

	private void handleSuspendedBySignal( ICDISignalReceived signal ) {
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleErrorInfo( ICDIErrorInfo info ) {
		setStatus( ICDebugElementStatus.ERROR, (info != null) ? info.getMessage() : null );
		if ( info != null ) {
			MultiStatus status = new MultiStatus( CDebugCorePlugin.getUniqueIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, CoreModelMessages.getString( "CDebugTarget.1" ), //$NON-NLS-1$
					null );
			StringTokenizer st = new StringTokenizer( info.getDetailMessage(), "\n\r" ); //$NON-NLS-1$
			while( st.hasMoreTokens() ) {
				String token = st.nextToken();
				if ( token.length() > 200 ) {
					token = token.substring( 0, 200 );
				}
				status.add( new Status( IStatus.ERROR, status.getPlugin(), ICDebugInternalConstants.STATUS_CODE_ERROR, token, null ) );
			}
			CDebugUtils.error( status, this );
		}
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleSuspendedBySolibEvent( ICDISharedLibraryEvent solibEvent ) {
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleExitedEvent( ICDIExitedEvent event ) {
		removeAllThreads();
		setState( CDebugElementState.EXITED );
		setCurrentStateInfo( event.getReason() );
		fireChangeEvent( DebugEvent.CONTENT );
		if ( getConfiguration().terminateSessionOnExit() )
			terminated();
	}

	private void handleTerminatedEvent( ICDIDestroyedEvent event ) {
		terminated();
	}

	private void handleDisconnectedEvent( ICDIDisconnectedEvent event ) {
		disconnected();
	}

	private void handleChangedEvent( ICDIChangedEvent event ) {
	}

	private void handleRestartedEvent( ICDIRestartedEvent event ) {
	}

	private void handleThreadCreatedEvent( ICDICreatedEvent event ) {
		ICDIThread cdiThread = (ICDIThread)event.getSource();
		CThread thread = findThread( cdiThread );
		if ( thread == null ) {
			thread = createThread( cdiThread );
			thread.fireCreationEvent();
		}
	}

	private void handleThreadTerminatedEvent( ICDIDestroyedEvent event ) {
		ICDIThread cdiThread = (ICDIThread)event.getSource();
		CThread thread = findThread( cdiThread );
		if ( thread != null ) {
			getThreadList().remove( thread );
			thread.terminated();
			thread.fireTerminateEvent();
		}
	}

	/**
	 * Finds and returns the model thread for the associated CDI thread, or <code>null</code> if not found.
	 * 
	 * @param the underlying CDI thread
	 * @return the associated model thread
	 */
	public CThread findThread( ICDIThread cdiThread ) {
		List threads = getThreadList();
		for( int i = 0; i < threads.size(); i++ ) {
			CThread t = (CThread)threads.get( i );
			if ( t.getCDIThread().equals( cdiThread ) )
				return t;
		}
		return null;
	}

	public CThread findThread( List threads, ICDIThread cdiThread ) {
		for( int i = 0; i < threads.size(); i++ ) {
			CThread t = (CThread)threads.get( i );
			if ( t.getCDIThread().equals( cdiThread ) )
				return t;
		}
		return null;
	}

	/**
	 * Returns the debug configuration of this target.
	 * 
	 * @return the debug configuration of this target
	 */
	protected ICDIConfiguration getConfiguration() {
		return fConfig;
	}

	/**
	 * Sets the debug configuration of this target.
	 * 
	 * @param config the debug configuration to set
	 */
	private void setConfiguration( ICDIConfiguration config ) {
		fConfig = config;
	}

	protected boolean supportsExpressionEvaluation() {
		return getConfiguration().supportsExpressionEvaluation();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICExpressionEvaluator#evaluateExpressionToString(java.lang.String)
	 */
	public String evaluateExpressionToString( String expression ) throws DebugException {
		try {
			return getCDITarget().evaluateExpressionToString( expression );
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), null );
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICExpressionEvaluator#canEvaluate()
	 */
	public boolean canEvaluate() {
		return supportsExpressionEvaluation() && isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionListener#expressionAdded(org.eclipse.debug.core.model.IExpression)
	 */
	public void expressionAdded( IExpression expression ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionListener#expressionChanged(org.eclipse.debug.core.model.IExpression)
	 */
	public void expressionChanged( IExpression expression ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionListener#expressionRemoved(org.eclipse.debug.core.model.IExpression)
	 */
	public void expressionRemoved( IExpression expression ) {
		if ( expression instanceof CExpression && expression.getDebugTarget().equals( this ) ) {
			((CExpression)expression).dispose();
		}
	}

	public void setInternalTemporaryBreakpoint( ICDILocation location ) throws DebugException {
		try {
			getCDISession().getBreakpointManager().setLocationBreakpoint( ICDIBreakpoint.TEMPORARY, location, null, null );
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRunToLine#canRunToLine(java.lang.String, int)
	 */
	public boolean canRunToLine( String fileName, int lineNumber ) {
		// check if supports run to line
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRunToLine#runToLine(java.lang.String, int, boolean)
	 */
	public void runToLine( String fileName, int lineNumber, boolean skipBreakpoints ) throws DebugException {
		if ( !canRunToLine( fileName, lineNumber ) )
			return;
		if ( skipBreakpoints ) {
			getBreakpointManager().skipBreakpoints( true );
		}
		ICDILocation location = getCDISession().getBreakpointManager().createLocation( fileName, null, lineNumber );
		try {
			getCDITarget().runUntil( location );
		}
		catch( CDIException e ) {
			if ( skipBreakpoints ) {
				getBreakpointManager().skipBreakpoints( false );
			}
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRunToLine#canRunToLine(org.eclipse.core.resources.IFile, int)
	 */
	public boolean canRunToLine( IFile file, int lineNumber ) {
		// check if supports run to line
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRunToLine#runToLine(org.eclipse.core.resources.IFile, int, boolean)
	 */
	public void runToLine( IFile file, int lineNumber, boolean skipBreakpoints ) throws DebugException {
		if ( !canRunToLine( file, lineNumber ) )
			return;
		runToLine( file.getLocation().lastSegment(), lineNumber, skipBreakpoints );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ISwitchToThread#setCurrentThread(org.eclipse.debug.core.model.IThread)
	 */
	public void setCurrentThread( IThread thread ) throws DebugException {
		if ( !isSuspended() || !isAvailable() || !(thread instanceof CThread) )
			return;
		try {
			CThread oldThread = (CThread)getCurrentThread();
			if ( !thread.equals( oldThread ) ) {
				getCDITarget().setCurrentThread( ((CThread)thread).getCDIThread() );
				if ( oldThread != null )
					oldThread.setCurrent( false );
				((CThread)thread).setCurrent( true );
			}
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ISwitchToThread#getCurrentThread()
	 */
	public IThread getCurrentThread() throws DebugException {
		IThread[] threads = getThreads();
		for( int i = 0; i < threads.length; ++i ) {
			if ( ((CThread)threads[i]).isCurrent() )
				return threads[i];
		}
		return null;
	}

	protected ISourceLocator getSourceLocator() {
		return getLaunch().getSourceLocator();
	}

	protected void resetRegisters() {
		getRegisterManager().reset();
	}

	protected CMemoryManager getMemoryManager() {
		return fMemoryManager;
	}

	protected void disposeMemoryManager() {
		getMemoryManager().dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IExecFileInfo#isLittleEndian()
	 */
	public boolean isLittleEndian() {
		if ( fIsLittleEndian == null ) {
			fIsLittleEndian = Boolean.TRUE;
			if ( getExecFile() != null && CoreModel.getDefault().isBinary( getExecFile() ) ) {
				ICElement cFile = CCorePlugin.getDefault().getCoreModel().create( getExecFile() );
				if ( cFile instanceof IBinary ) {
					fIsLittleEndian = new Boolean( ((IBinary)cFile).isLittleEndian() );
				}
			}
		}
		return fIsLittleEndian.booleanValue();
	}

	public IFile getExecFile() {
		return fExecFile;
	}

	private void setExecFile( IFile file ) {
		fExecFile = file;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IExecFileInfo#getGlobals()
	 */
	public IGlobalVariableDescriptor[] getGlobals() throws DebugException {
		ArrayList list = new ArrayList();
		if ( getExecFile() != null && CoreModel.getDefault().isBinary( getExecFile() ) ) {
			ICElement cFile = CCorePlugin.getDefault().getCoreModel().create( getExecFile() );
			if ( cFile instanceof IParent ) {
				list.addAll( getCFileGlobals( (IParent)cFile ) );
			}
		}
		return (IGlobalVariableDescriptor[])list.toArray( new IGlobalVariableDescriptor[list.size()] );
	}

	private List getCFileGlobals( IParent file ) throws DebugException {
		ArrayList list = new ArrayList();
		try {
			ICElement[] elements = file.getChildren();
			for( int i = 0; i < elements.length; ++i ) {
				if ( elements[i] instanceof org.eclipse.cdt.core.model.IVariable ) {
					list.add( CVariableFactory.createGlobalVariableDescriptor( (org.eclipse.cdt.core.model.IVariable)elements[i] ) );
				}
				else if ( elements[i] instanceof org.eclipse.cdt.core.model.IParent ) {
					list.addAll( getCFileGlobals( (org.eclipse.cdt.core.model.IParent)elements[i] ) );
				}
			}
		}
		catch( CModelException e ) {
			requestFailed( CoreModelMessages.getString( "CDebugTarget.Unable_to_get_globals_1" ) + e.getMessage(), e ); //$NON-NLS-1$
		}
		return list;
	}

	protected void setSharedLibraryManager( CSharedLibraryManager libman ) {
		fSharedLibraryManager = libman;
	}

	protected CSharedLibraryManager getSharedLibraryManager() {
		return fSharedLibraryManager;
	}

	protected void disposeSharedLibraryManager() {
		fSharedLibraryManager.dispose();
	}

	protected void setSignalManager( CSignalManager sm ) {
		fSignalManager = sm;
	}

	protected CSignalManager getSignalManager() {
		return fSignalManager;
	}

	protected void disposeSignalManager() {
		fSignalManager.dispose();
	}

	protected void disposeRegisterManager() {
		fRegisterManager.dispose();
	}

	protected void saveGlobalVariables() {
		fGlobalVariableManager.save();
	}

	protected void disposeGlobalVariableManager() {
		fGlobalVariableManager.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRunToAddress#canRunToAddress(long)
	 */
	public boolean canRunToAddress( long address ) {
		// for now
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRunToAddress#runToAddress(long, boolean)
	 */
	public void runToAddress( long address, boolean skipBreakpoints ) throws DebugException {
		if ( !canRunToAddress( address ) )
			return;
		if ( skipBreakpoints ) {
			getBreakpointManager().skipBreakpoints( true );
		}
		ICDILocation location = getCDISession().getBreakpointManager().createLocation( address );
		try {
			getCDITarget().runUntil( location );
		}
		catch( CDIException e ) {
			if ( skipBreakpoints ) {
				getBreakpointManager().skipBreakpoints( false );
			}
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeWithoutSignal#canResumeWithoutSignal()
	 */
	public boolean canResumeWithoutSignal() {
		// Check if the configuration supports this!!!
		return ( canResume() && getCurrentStateInfo() instanceof ICDISignalReceived );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeWithoutSignal#resumeWithoutSignal()
	 */
	public void resumeWithoutSignal() throws DebugException {
		if ( !canResume() )
			return;
		final CDebugElementState oldState = getState();
		setState( CDebugElementState.RESUMING );
		DebugPlugin.getDefault().asyncExec( new Runnable() {

			public void run() {
				try {
					getCDITarget().signal();
				}
				catch( CDIException e ) {
					setState( oldState );
					try {
						targetRequestFailed( e.getMessage(), e );
					}
					catch( DebugException e1 ) {
						CDebugUtils.error( e1.getStatus(), CDebugTarget.this );
					}
				}
			}
		} );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IJumpToLine#canJumpToLine(org.eclipse.core.resources.IFile, int)
	 */
	public boolean canJumpToLine( IFile file, int lineNumber ) {
		// check if supports jump to line
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IJumpToLine#jumpToLine(org.eclipse.core.resources.IFile, int)
	 */
	public void jumpToLine( IFile file, int lineNumber ) throws DebugException {
		if ( !canJumpToLine( file, lineNumber ) )
			return;
		jumpToLine( file.getLocation().lastSegment(), lineNumber );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IJumpToLine#canJumpToLine(java.lang.String, int)
	 */
	public boolean canJumpToLine( String fileName, int lineNumber ) {
		// check if supports jump to line
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IJumpToLine#jumpToLine(java.lang.String, int)
	 */
	public void jumpToLine( String fileName, int lineNumber ) throws DebugException {
		if ( !canJumpToLine( fileName, lineNumber ) )
			return;
		ICDILocation location = getCDISession().getBreakpointManager().createLocation( fileName, null, lineNumber );
		try {
			getCDITarget().jump( location );
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IJumpToAddress#canJumpToAddress(long)
	 */
	public boolean canJumpToAddress( long address ) {
		// check if supports jump to address
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IJumpToAddress#jumpToAddress(long)
	 */
	public void jumpToAddress( long address ) throws DebugException {
		if ( !canJumpToAddress( address ) )
			return;
		ICDILocation location = getCDISession().getBreakpointManager().createLocation( address );
		try {
			getCDITarget().jump( location );
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), e );
		}
	}

	public CRegisterManager getRegisterManager() {
		return fRegisterManager;
	}

	protected void setRegisterManager( CRegisterManager registerManager ) {
		fRegisterManager = registerManager;
	}

	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return getRegisterManager().getRegisterGroups();
	}

	protected void disposeSourceManager() {
		ISourceLocator locator = getSourceLocator();
		if ( locator instanceof IAdaptable ) {
			IResourceChangeListener listener = (IResourceChangeListener)((IAdaptable)locator).getAdapter( IResourceChangeListener.class );
			if ( listener != null )
				CCorePlugin.getWorkspace().removeResourceChangeListener( listener );
		}
	}

	private void disposeExpresionTarget() {
		if ( fExpressionTarget != null ) {
			fExpressionTarget.dispose();
			fExpressionTarget = null;
		}
	}

	public IFile getCurrentBreakpointFile() {
		Object info = getCurrentStateInfo();
		if ( info instanceof ICDIBreakpointHit ) {
			ICDIBreakpoint cdiBreakpoint = ((ICDIBreakpointHit)info).getBreakpoint();
			if ( cdiBreakpoint != null )
				return getBreakpointManager().getCDIBreakpointFile( cdiBreakpoint );
		}
		return null;
	}

	protected CBreakpointManager getBreakpointManager() {
		return fBreakpointManager;
	}

	protected void setBreakpointManager( CBreakpointManager manager ) {
		fBreakpointManager = manager;
	}

	protected void disposeBreakpointManager() {
		if ( getBreakpointManager() != null )
			getBreakpointManager().dispose();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = ""; //$NON-NLS-1$
		try {
			result = getName();
		}
		catch( DebugException e ) {
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#getDisassembly()
	 */
	public IDisassembly getDisassembly() throws DebugException {
		return fDisassembly;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#getSharedLibraries()
	 */
	public ICSharedLibrary[] getSharedLibraries() throws DebugException {
		ICSharedLibraryManager slm = getSharedLibraryManager();
		if ( slm != null ) {
			return slm.getSharedLibraries();
		}
		return new ICSharedLibrary[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#getSignals()
	 */
	public ICSignal[] getSignals() throws DebugException {
		ICSignalManager sm = getSignalManager();
		if ( sm != null ) {
			return sm.getSignals();
		}
		return new ICSignal[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#hasSharedLibraries()
	 */
	public boolean hasSharedLibraries() throws DebugException {
		ICSharedLibraryManager slm = getSharedLibraryManager();
		if ( slm != null ) {
			return (slm.getSharedLibraries().length > 0);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#hasSignals()
	 */
	public boolean hasSignals() throws DebugException {
		ICSignalManager sm = getSignalManager();
		if ( sm != null ) {
			return (sm.getSignals().length > 0);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#loadSymbols()
	 */
	public void loadSymbols() throws DebugException {
		ICSharedLibraryManager slm = getSharedLibraryManager();
		if ( slm != null ) {
			slm.loadSymbolsForAll();
		}
	}

	private void createDisassembly() {
		this.fDisassembly = new Disassembly( this );
	}

	private void disposeDisassembly() {
		if ( fDisassembly != null )
			fDisassembly.dispose();
		fDisassembly = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IBreakpointTarget#getBreakpointAddress(org.eclipse.cdt.debug.core.model.ICLineBreakpoint)
	 */
	public long getBreakpointAddress( ICLineBreakpoint breakpoint ) throws DebugException {
		return (getBreakpointManager() != null) ? getBreakpointManager().getBreakpointAddress( breakpoint ) : 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IBreakpointTarget#isTargetBreakpoint(org.eclipse.cdt.debug.core.model.ICBreakpoint)
	 */
	public boolean isTargetBreakpoint( ICBreakpoint breakpoint ) {
		return (getBreakpointManager() != null) ? getBreakpointManager().isTargetBreakpoint( breakpoint ) : false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ISteppingModeTarget#enableInstructionStepping(boolean)
	 */
	public void enableInstructionStepping( boolean enabled ) {
		fPreferences.setValue( PREF_INSTRUCTION_STEPPING_MODE, enabled );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ISteppingModeTarget#isInstructionSteppingEnabled()
	 */
	public boolean isInstructionSteppingEnabled() {
		return fPreferences.getBoolean( PREF_INSTRUCTION_STEPPING_MODE );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ISteppingModeTarget#supportsInstructionStepping()
	 */
	public boolean supportsInstructionStepping() {
		return getConfiguration().supportsInstructionStepping();
	}

	private void initializePreferences() {
		fPreferences = new Preferences();
		fPreferences.setDefault( PREF_INSTRUCTION_STEPPING_MODE, false );
	}

	private void disposePreferences() {
		fPreferences = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ITargetProperties#addPropertyChangeListener(org.eclipse.core.runtime.Preferences.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener( IPropertyChangeListener listener ) {
		if ( fPreferences != null )
			fPreferences.addPropertyChangeListener( listener );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ITargetProperties#removePropertyChangeListener(org.eclipse.core.runtime.Preferences.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener( IPropertyChangeListener listener ) {
		if ( fPreferences != null )
			fPreferences.removePropertyChangeListener( listener );
	}

	protected CExpressionTarget getExpressionTarget() {
		if ( fExpressionTarget == null ) {
			fExpressionTarget = new CExpressionTarget( this );
		}
		return fExpressionTarget;
	}

	protected CGlobalVariableManager getGlobalVariableManager() {
		return fGlobalVariableManager;
	}

	private void setGlobalVariableManager( CGlobalVariableManager globalVariableManager ) {
		fGlobalVariableManager = globalVariableManager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#isPostMortem()
	 */
	public boolean isPostMortem() {
		return false;
	}
}