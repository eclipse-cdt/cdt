/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ken Ryall (Nokia) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=118894
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.ICGlobalVariableManager;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICDIErrorInfo;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
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
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration2;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.model.CDebugElementState;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICGlobalVariable;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.core.model.IDebuggerProcessSupport;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.cdt.debug.core.model.IPersistableRegisterGroup;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceLookupChangeListener;
import org.eclipse.cdt.debug.internal.core.CBreakpointManager;
import org.eclipse.cdt.debug.internal.core.CGlobalVariableManager;
import org.eclipse.cdt.debug.internal.core.CMemoryBlockRetrievalExtension;
import org.eclipse.cdt.debug.internal.core.CRegisterManager;
import org.eclipse.cdt.debug.internal.core.CSignalManager;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupParticipant;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionListener;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;

/**
 * Debug target for C/C++ debug model.
 */
public class CDebugTarget extends CDebugElement implements ICDebugTarget, ICDIEventListener, ILaunchListener, IExpressionListener, ISourceLookupChangeListener {

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
	private ICDITargetConfiguration fConfig;

	/**
	 * The disassembly manager for this target.
	 */
	private Disassembly fDisassembly;

	/**
	 * The module manager for this target.
	 */
	private CModuleManager fModuleManager;

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
	 * The global variable manager for this target.
	 */
	private CGlobalVariableManager fGlobalVariableManager;
	
	/**
	 * The executable binary file associated with this target.
	 */
	private IBinaryObject fBinaryFile;

	/** 
	 * The project associated with this target.
	 */
	private IProject fProject;
	
	/**
	 * Whether the target is little endian.
	 */
	private Boolean fIsLittleEndian = null;

	/**
	 * The target's preference set.
	 */
	private Preferences fPreferences = null;

	/**
	 * The address factory of this target.
	 */
	private IAddressFactory fAddressFactory;

	/**
	 * Support for the memory retrival on this target.
	 */
	private CMemoryBlockRetrievalExtension fMemoryBlockRetrieval;

	/**
	 * Constructor for CDebugTarget.
	 */
	public CDebugTarget( ILaunch launch, IProject project, ICDITarget cdiTarget, String name, IProcess debuggeeProcess, IBinaryObject file, boolean allowsTerminate, boolean allowsDisconnect) {
		super( null );
		setLaunch( launch );
		setDebugTarget( this );
		setName( name );
		setProcess( debuggeeProcess );
		setProject(project);
		setExecFile( file );
		setCDITarget( cdiTarget );
		setState( CDebugElementState.SUSPENDED );
		initializePreferences();
		setConfiguration( cdiTarget.getConfiguration() );
		setThreadList( new ArrayList( 5 ) );
		createDisassembly();
		setModuleManager( new CModuleManager( this ) );
		setSignalManager( new CSignalManager( this ) );
		setRegisterManager( new CRegisterManager( this ) );
		setBreakpointManager( new CBreakpointManager( this ) );
		setGlobalVariableManager( new CGlobalVariableManager( this ) );
		setMemoryBlockRetrieval( new CMemoryBlockRetrievalExtension( this ) );
		initialize();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener( this );
		DebugPlugin.getDefault().getExpressionManager().addExpressionListener( this );
		getCDISession().getEventManager().addEventListener( this );
	}

	protected void initialize() {
		initializeSourceLookupPath();
		ArrayList debugEvents = new ArrayList( 1 );
		debugEvents.add( createCreateEvent() );
		initializeThreads( debugEvents );
		initializeBreakpoints();
		initializeRegisters();
		initializeSourceManager();
		initializeModuleManager();
		initializeMemoryBlocks();
		getLaunch().addDebugTarget( this );
		fireEventSet( (DebugEvent[])debugEvents.toArray( new DebugEvent[debugEvents.size()] ) );
	}

	private void initializeBreakpoints() {
		getBreakpointManager().initialize();
	}

	public void start( boolean stopInMain, boolean resume ) throws DebugException {
		String stopSymbol = null;
		try {
			if ( stopInMain )
				stopSymbol = getLaunch().getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT );
		}
		catch( CoreException e ) {
			throw new DebugException( e.getStatus() );
		}
		ICDITargetConfiguration config = getConfiguration();
		if ( config.supportsBreakpoints() ) {
			getBreakpointManager().setInitialBreakpoints();
			if ( stopSymbol != null && stopSymbol.length() != 0 ) {
				stopAtSymbol( stopSymbol );
			}
		}
		if ( config.supportsResume() && resume ) {
			resume();
		}
	}

	public void start( String stopSymbol, boolean resume ) throws DebugException {
		ICDITargetConfiguration config = getConfiguration();
		if ( config.supportsBreakpoints() ) {
			getBreakpointManager().setInitialBreakpoints();
			if ( stopSymbol != null && stopSymbol.length() != 0 ) {
				stopAtSymbol( stopSymbol );
			}
		}
		if ( config.supportsResume() && resume ) {
			resume();
		}
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
		DebugEvent suspendEvent = null;
		for( int i = 0; i < cdiThreads.length; ++i ) {
			CThread thread = createThread( cdiThreads[i] );
			debugEvents.add( thread.createCreateEvent() );
			try {
				if ( cdiThreads[i].equals( getCDITarget().getCurrentThread() ) && thread.isSuspended() ) {
					// Use BREAKPOINT as a detail to force perspective switch
					suspendEvent = thread.createSuspendEvent( DebugEvent.BREAKPOINT );
				}
			}
			catch( CDIException e ) {
				// ignore
			}
		}
		if ( suspendEvent != null ) {
			debugEvents.add( suspendEvent );
		}
	}

	protected void initializeRegisters() {
		getRegisterManager().initialize();
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

	protected void initializeSourceLookupPath() {
		ISourceLocator locator = getLaunch().getSourceLocator();
		if ( locator instanceof ISourceLookupDirector ) {
			ISourceLookupParticipant[] participants = ((ISourceLookupDirector)locator).getParticipants();
			for ( int i = 0; i < participants.length; ++i ) {
				if ( participants[i] instanceof CSourceLookupParticipant ) {
					((CSourceLookupParticipant)participants[i]).addSourceLookupChangeListener( this );
				}
			}
			setSourceLookupPath( ((ISourceLookupDirector)locator).getSourceContainers() );
		}
	}

	protected void initializeModuleManager() {
		ICDISharedLibrary[] slibs = new ICDISharedLibrary[0];
		try {
			slibs = getCDITarget().getSharedLibraries();
		}
		catch( CDIException e ) {
			DebugPlugin.log( e );
		}
		ICModule[] modules = new ICModule[slibs.length + 1];
		modules[0] = CModule.createExecutable( this, getExecFile().getPath() );
		for ( int i = 0; i < slibs.length; ++i ) {
			modules[i + 1] = CModule.createSharedLibrary( this, slibs[i] );
		}
		getModuleManager().addModules( modules );
	}

	protected void initializeMemoryBlocks() {
		getMemoryBlockRetrieval().initialize();
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
		return (breakpoint instanceof ICBreakpoint && getBreakpointManager().supportsBreakpoint( (ICBreakpoint)breakpoint ));
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
		changeState( CDebugElementState.TERMINATING );
		try {
			getCDITarget().terminate();
		}
		catch( CDIException e ) {
			restoreOldState();
			targetRequestFailed( e.getMessage(), null );
		}
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
		changeState( CDebugElementState.RESUMING );
		try {
			getCDITarget().resume( false );
		}
		catch( CDIException e ) {
			restoreOldState();
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		if ( !canSuspend() )
			return;
		changeState( CDebugElementState.SUSPENDING );
		try {
			getCDITarget().suspend();
		}
		catch( CDIException e ) {
			restoreOldState();
			targetRequestFailed( e.getMessage(), null );
		}
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointRemoved( IBreakpoint breakpoint, IMarkerDelta delta ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointChanged( IBreakpoint breakpoint, IMarkerDelta delta ) {
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
		changeState( CDebugElementState.DISCONNECTING );
		try {
			getCDITarget().disconnect();
		}
		catch( CDIException e ) {
			restoreOldState();
			targetRequestFailed( e.getMessage(), null );
		}
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
		if ( adapter.equals( IDebuggerProcessSupport.class ) )
			return this;
		if ( adapter.equals( IExecFileInfo.class ) )
			return this;
		if ( adapter.equals( CBreakpointManager.class ) )
			return getBreakpointManager();
		if ( adapter.equals( CSignalManager.class ) )
			return getSignalManager();
		if ( adapter.equals( CRegisterManager.class ) )
			return getRegisterManager();
		if ( adapter.equals( ICGlobalVariableManager.class ) )
			return getGlobalVariableManager();
		if ( adapter.equals( ICDISession.class ) )
			return getCDISession();
		if ( adapter.equals( IMemoryBlockRetrievalExtension.class ) )
			return getMemoryBlockRetrieval();
		if ( adapter.equals( IMemoryBlockRetrieval.class ) )
			return getMemoryBlockRetrieval();
		if ( adapter.equals( IModuleRetrieval.class ) )
			return getModuleManager();
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
						getModuleManager().sharedLibraryLoaded( (ICDISharedLibrary)source );
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
						getModuleManager().sharedLibraryUnloaded( (ICDISharedLibrary)source );
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
						handleSymbolsLoaded( (ICDISharedLibrary)source );
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
		String mainSymbol = new String( ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT );
		try {
			mainSymbol = getLaunch().getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT );
		}
		catch( CoreException e ) {
			requestFailed( e.getMessage(), e );
		}
		ICDILocation location = getCDITarget().createFunctionLocation( "", mainSymbol ); //$NON-NLS-1$
		setInternalTemporaryBreakpoint( location );
		changeState( CDebugElementState.RESTARTING );
		try {
			getCDITarget().restart();
		}
		catch( CDIException e ) {
			restoreOldState();
			targetRequestFailed( e.getMessage(), e );
		}
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
		DebugPlugin.getDefault().getExpressionManager().removeExpressionListener( this );
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener( this );
		saveGlobalVariables();
		disposeGlobalVariableManager();
		disposeModuleManager();
		disposeSignalManager();
		saveRegisterGroups();
		disposeRegisterManager();
		saveMemoryBlocks();
		disposeMemoryBlockRetrieval();
		disposeDisassembly();
		disposeSourceManager();
		disposeSourceLookupPath();
		disposeBreakpointManager();
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
		if (event.getSource() instanceof ICDITarget) {
			if (!(this.getConfiguration() instanceof ICDITargetConfiguration2) || !((ICDITargetConfiguration2)this.getConfiguration()).supportsThreadControl())
				suspendThreads(event);
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
		
		if (!(this.getConfiguration() instanceof ICDITargetConfiguration2) || !((ICDITargetConfiguration2)this.getConfiguration()).supportsThreadControl())
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
		getBreakpointManager().watchpointOutOfScope( ws.getWatchpoint() );
		fireSuspendEvent( DebugEvent.BREAKPOINT );
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
		ICDISessionConfiguration sessionConfig = getCDISession().getConfiguration();
		if ( sessionConfig != null && sessionConfig.terminateSessionOnExit() )
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
	protected ICDITargetConfiguration getConfiguration() {
		return fConfig;
	}

	/**
	 * Sets the debug configuration of this target.
	 * 
	 * @param config the debug configuration to set
	 */
	private void setConfiguration( ICDITargetConfiguration config ) {
		fConfig = config;
	}

	protected boolean supportsExpressionEvaluation() {
		return getConfiguration().supportsExpressionEvaluation();
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
			if (location instanceof ICDIFunctionLocation) {
				getCDITarget().setFunctionBreakpoint( ICDIBreakpoint.TEMPORARY, (ICDIFunctionLocation)location, null, false );
			} else if (location instanceof ICDILineLocation) {
				getCDITarget().setLineBreakpoint( ICDIBreakpoint.TEMPORARY, (ICDILineLocation)location, null, false );
			} else if (location instanceof ICDIAddressLocation) {
				getCDITarget().setAddressBreakpoint( ICDIBreakpoint.TEMPORARY, (ICDIAddressLocation)location, null, false );
			} else {
				// ???
				targetRequestFailed("not_a_location", null); //$NON-NLS-1$
			}
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), null );
		}
	}

	protected IThread getCurrentThread() throws DebugException {
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IExecFileInfo#isLittleEndian()
	 */
	public boolean isLittleEndian() {
		if ( fIsLittleEndian == null ) {
			fIsLittleEndian = Boolean.TRUE;
			IBinaryObject file;
			file = getBinaryFile();
			if ( file != null ) {
				fIsLittleEndian = Boolean.valueOf( file.isLittleEndian() );
			}
		}
		return fIsLittleEndian.booleanValue();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IExecFileInfo#getExecFile()
	 */
	public IBinaryObject getExecFile() {
		return getBinaryFile();
	}
	
	public IBinaryObject getBinaryFile() {
		return fBinaryFile;
	}

	private void setExecFile( IBinaryObject file ) {
		fBinaryFile = file;
	}
	
	private void setProject(IProject project) {
		fProject = project;
	}
	
	public IProject getProject() {
		return fProject;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IExecFileInfo#getGlobals()
	 */
	public IGlobalVariableDescriptor[] getGlobals() throws DebugException {
		ArrayList list = new ArrayList();
		IBinaryObject file = getBinaryFile();
		if (file != null) {
			list.addAll( getCFileGlobals( file ) );
		}
		return (IGlobalVariableDescriptor[])list.toArray( new IGlobalVariableDescriptor[list.size()] );
	}

	private List getCFileGlobals( IBinaryObject file ) throws DebugException {
		ArrayList list = new ArrayList();
		ISymbol[] symbols = file.getSymbols();
		for( int i = 0; i < symbols.length; ++i ) {
			if (symbols[i].getType() == ISymbol.VARIABLE) {
				list.add( CVariableFactory.createGlobalVariableDescriptor( symbols[i] ) );
			}
		}
		return list;
	}

	protected void setModuleManager( CModuleManager mm ) {
		fModuleManager = mm;
	}

	protected CModuleManager getModuleManager() {
		return fModuleManager;
	}

	protected void disposeModuleManager() {
		fModuleManager.dispose();
		fModuleManager = null;
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

	protected void saveRegisterGroups() {
		fRegisterManager.save();
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
		changeState( CDebugElementState.RESUMING );
		try {
			getCDITarget().resume( false );
		}
		catch( CDIException e ) {
			restoreOldState();
			targetRequestFailed( e.getMessage(), e );
		}
	}

	public CRegisterManager getRegisterManager() {
		return fRegisterManager;
	}

	protected void setRegisterManager( CRegisterManager registerManager ) {
		fRegisterManager = registerManager;
	}

	public IRegisterGroup[] getRegisterGroups( CStackFrame frame ) throws DebugException {
		return getRegisterManager().getRegisterGroups( frame );
	}

	protected void disposeSourceManager() {
		ISourceLocator locator = getSourceLocator();
		if ( locator instanceof IAdaptable ) {
			IResourceChangeListener listener = (IResourceChangeListener)((IAdaptable)locator).getAdapter( IResourceChangeListener.class );
			if ( listener != null )
				CCorePlugin.getWorkspace().removeResourceChangeListener( listener );
		}
	}

	protected void disposeSourceLookupPath() {
		ISourceLocator locator = getLaunch().getSourceLocator();
		if ( locator instanceof ISourceLookupDirector ) {
			ISourceLookupParticipant[] participants = ((ISourceLookupDirector)locator).getParticipants();
			for ( int i = 0; i < participants.length; ++i ) {
				if ( participants[i] instanceof CSourceLookupParticipant ) {
					((CSourceLookupParticipant)participants[i]).removeSourceLookupChangeListener( this );
				}
			}
		}
	}

	protected void saveMemoryBlocks() {
		getMemoryBlockRetrieval().save();
	}

	protected void disposeMemoryBlockRetrieval() {
		getMemoryBlockRetrieval().dispose();
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
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#getSignals()
	 */
	public ICSignal[] getSignals() throws DebugException {
		CSignalManager sm = getSignalManager();
		if ( sm != null ) {
			return sm.getSignals();
		}
		return new ICSignal[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#hasSignals()
	 */
	public boolean hasSignals() throws DebugException {
		CSignalManager sm = getSignalManager();
		if ( sm != null ) {
			return (sm.getSignals().length > 0);
		}
		return false;
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
	public IAddress getBreakpointAddress( ICLineBreakpoint breakpoint ) throws DebugException {
		return (getBreakpointManager() != null) ? getBreakpointManager().getBreakpointAddress( breakpoint ) : getAddressFactory().getZero();
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
		return fPreferences.getBoolean( PREF_INSTRUCTION_STEPPING_MODE ) ||
			   CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_INSTRUCTION_STEP_MODE_ON );
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

	public IAddressFactory getAddressFactory() {
		if ( fAddressFactory == null ) {
			if ( getExecFile() != null && getProject() != null ) {
				IBinaryObject file;
				file = getBinaryFile();
				if (file != null) {
					fAddressFactory = file.getAddressFactory();
				}
			}
		}
		return fAddressFactory;
	}

	private CMemoryBlockRetrievalExtension getMemoryBlockRetrieval() {
		return fMemoryBlockRetrieval;
	}

	private void setMemoryBlockRetrieval( CMemoryBlockRetrievalExtension memoryBlockRetrieval ) {
		fMemoryBlockRetrieval = memoryBlockRetrieval;
	}

	private void changeState( CDebugElementState state ) {
		setState( state );
		Iterator it = getThreadList().iterator();
		while( it.hasNext() ) {
			((CThread)it.next()).setState( state );
		}
	}

	protected void restoreOldState() {
		restoreState();
		Iterator it = getThreadList().iterator();
		while( it.hasNext() ) {
			((CThread)it.next()).restoreState();
		}
	}

	private void handleSymbolsLoaded( ICDISharedLibrary library ) {
		getModuleManager().symbolsLoaded( library );
	}

	public ICGlobalVariable createGlobalVariable( IGlobalVariableDescriptor info ) throws DebugException {
		ICDIVariableDescriptor vo = null;
		try {
			vo = getCDITarget().getGlobalVariableDescriptors( info.getPath().lastSegment(), null, info.getName() );
		}
		catch( CDIException e ) {
			throw new DebugException( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), DebugException.TARGET_REQUEST_FAILED, (vo != null) ? vo.getName() + ": " + e.getMessage() : e.getMessage(), null ) ); //$NON-NLS-1$
		}
		return CVariableFactory.createGlobalVariable( this, info, vo );
	}

	public void sourceContainersChanged( ISourceLookupDirector director ) {
		setSourceLookupPath( director.getSourceContainers() );
	}

	private void setSourceLookupPath( ISourceContainer[] containers ) {
		ArrayList list = new ArrayList( containers.length );
		getSourceLookupPath( list, containers );
		try {
			getCDITarget().setSourcePaths( (String[])list.toArray( new String[list.size()] ) );
		}
		catch( CDIException e ) {
			CDebugCorePlugin.log( e );
		}
	}

	private void getSourceLookupPath( List list, ISourceContainer[] containers ) {
		for ( int i = 0; i < containers.length; ++i ) {
			if ( containers[i] instanceof ProjectSourceContainer ) {
				IProject project = ((ProjectSourceContainer)containers[i]).getProject();
				if ( project != null && project.exists() )
					list.add( project.getLocation().toPortableString() );
			}
			if ( containers[i] instanceof FolderSourceContainer ) {
				IContainer container = ((FolderSourceContainer)containers[i]).getContainer();
				if ( container != null && container.exists() )
					list.add( container.getLocation().toPortableString() );
			}
			if ( containers[i] instanceof DirectorySourceContainer ) {
				File dir = ((DirectorySourceContainer)containers[i]).getDirectory();
				if ( dir != null && dir.exists() ) {
					IPath path = new Path( dir.getAbsolutePath() );
					list.add( path.toPortableString() );
				}
			}
			if ( containers[i].isComposite() ) {
				try {
					getSourceLookupPath( list, containers[i].getSourceContainers() );
				}
				catch( CoreException e ) {
					CDebugCorePlugin.log( e.getStatus() );
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#getRegisterDescriptors()
	 */
	public IRegisterDescriptor[] getRegisterDescriptors() throws DebugException {
		return getRegisterManager().getAllRegisterDescriptors();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#addUserDefinedRegisterGroup(java.lang.String, org.eclipse.cdt.debug.core.model.IRegisterDescriptor[])
	 */
	public void addRegisterGroup( String name, IRegisterDescriptor[] descriptors ) {
		getRegisterManager().addRegisterGroup( name, descriptors );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#removeRegisterGroups(org.eclipse.debug.core.model.IRegisterGroup[])
	 */
	public void removeRegisterGroups( IRegisterGroup[] groups ) {
		getRegisterManager().removeRegisterGroups( groups );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#modifyRegisterGroup(org.eclipse.cdt.debug.core.model.IPersistableRegisterGroup, org.eclipse.cdt.debug.core.model.IRegisterDescriptor[])
	 */
	public void modifyRegisterGroup( IPersistableRegisterGroup group, IRegisterDescriptor[] descriptors ) {
		getRegisterManager().modifyRegisterGroup( group, descriptors );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#restoreDefaultRegisterGroups()
	 */
	public void restoreDefaultRegisterGroups() {
		getRegisterManager().restoreDefaults();
	}

	protected void skipBreakpoints( boolean enabled ) {
		getBreakpointManager().skipBreakpoints( enabled );
	}

	protected void stopAtSymbol( String stopSymbol ) throws DebugException {
		try {
			ICDILocation location = getCDITarget().createFunctionLocation( "", stopSymbol ); //$NON-NLS-1$ 
			setInternalTemporaryBreakpoint( location );
		}
		catch( CoreException e ) {
			boolean isTerminated = getCDITarget().isTerminated();
			if ( isTerminated ) {
				String message = MessageFormat.format( CoreModelMessages.getString( "CDebugTarget.0" ), new String[]{ stopSymbol } ); //$NON-NLS-1$
				MultiStatus status = new MultiStatus( CDebugCorePlugin.getUniqueIdentifier(), IStatus.OK, message, null );
				status.add( e.getStatus() );
				throw new DebugException( status );
			}
			String message = MessageFormat.format( CoreModelMessages.getString( "CDebugTarget.2" ), new String[]{ stopSymbol, e.getStatus().getMessage() } ); //$NON-NLS-1$
			IStatus newStatus = new Status( IStatus.WARNING, e.getStatus().getPlugin(), ICDebugInternalConstants.STATUS_CODE_QUESTION, message, null );
			if ( !CDebugUtils.question( newStatus, this ) ) {
				throw new DebugException( new Status( IStatus.OK, e.getStatus().getPlugin(), e.getStatus().getCode(), e.getStatus().getMessage(), null ) );
			}
		}
	}

	protected void stopInMain() throws DebugException {
		String mainSymbol = new String( ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT );
		try {
			mainSymbol = getLaunch().getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT );
			ICDILocation location = getCDITarget().createFunctionLocation( "", mainSymbol ); //$NON-NLS-1$ 
			setInternalTemporaryBreakpoint( location );
		}
		catch( CoreException e ) {
			String message = MessageFormat.format( CoreModelMessages.getString( "CDebugTarget.2" ), new String[]{ mainSymbol, e.getStatus().getMessage() } ); //$NON-NLS-1$
			IStatus newStatus = new Status( IStatus.WARNING, e.getStatus().getPlugin(), ICDebugInternalConstants.STATUS_CODE_QUESTION, message, null );
			if ( !CDebugUtils.question( newStatus, this ) ) {
				terminate();
				throw new DebugException( new Status( IStatus.OK, e.getStatus().getPlugin(), e.getStatus().getCode(), e.getStatus().getMessage(), null ) );
			}
		}
	}

	public boolean hasModules() throws DebugException {
		CModuleManager mm = getModuleManager();
		if ( mm != null )
			return mm.hasModules();
		return false;
	}

	public ICModule[] getModules() throws DebugException {
		CModuleManager mm = getModuleManager();
		if ( mm != null )
			return mm.getModules();
		return new ICModule[0];
	}

	public void loadSymbolsForAllModules() throws DebugException {
		CModuleManager mm = getModuleManager();
		if ( mm != null )
			mm.loadSymbolsForAllModules();
	}
}
