/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryModule;
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
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
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
import org.eclipse.cdt.debug.core.model.IBreakpointTarget;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugElementErrorStatus;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICDebugTargetType;
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
import org.eclipse.cdt.debug.core.model.IState;
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
 * 
 * Enter type comment.
 * 
 * @since Aug 1, 2002
 */
public class CDebugTarget extends CDebugElement
						  implements ICDebugTarget,
						  			 ICDIEventListener,
						  			 ILaunchListener,
						  			 IExpressionListener
{
	public class RunningInfo
	{
		private int fType = 0;
		private int fStackDepth;
		
		public RunningInfo( int type, int stackDepth )
		{
			fType = type;
			fStackDepth = stackDepth;
		}

		public int getType()
		{
			return fType;
		}

		public int getStackDepth()
		{
			return fStackDepth;
		}
	}

	private boolean fSuspending;

	/**
	 * The type of this target.
	 */
	private int fTargetType;

	/**
	 * Threads contained in this debug target. When a thread
	 * starts it is added to the list. When a thread ends it
	 * is removed from the list.
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
	 * Whether this target is suspended.
	 */
	private boolean fSuspended = true;

	/**
	 * Whether terminated
	 */
	private boolean fTerminated;
	
	/**
	 * Whether in the process of terminating
	 */
	private boolean fTerminating;

	/**
	 * Whether disconnected
	 */
	private boolean fDisconnected;

	/**
	 * The launch this target is contained in
	 */
	private ILaunch fLaunch;	

	/**
	 * The debug configuration of this session
	 */
	private ICDIConfiguration fConfig;	

	/**
	 * The current state identifier.
	 */
	private int fCurrentStateId = IState.UNKNOWN;
	
	/**
	 * The current state info.
	 */
	private Object fCurrentStateInfo = null;

	/**
	 * Count of the number of suspend events in this target
	 */
	private int fSuspendCount = 0;

	/**
	 * A memory manager for this target.
	 */
	private CMemoryManager fMemoryManager;

	/**
	 * A disassembly manager for this target.
	 */
	private Disassembly fDisassembly;

	/**
	 * A shared library manager for this target.
	 */
	private CSharedLibraryManager fSharedLibraryManager;

	/**
	 * A signal manager for this target.
	 */
	private CSignalManager fSignalManager;

	/**
	 * A register manager for this target.
	 */
	private CRegisterManager fRegisterManager;

	/**
	 * A breakpoint manager for this target.
	 */
	private CBreakpointManager fBreakpointManager;

	private CExpressionTarget fExpressionTarget;

	private CGlobalVariableManager fGlobalVariableManager;

	/**
	 * The suspension thread.
	 */
	private ICDIThread fSuspensionThread;

	/**
	 * The executable file.
	 */
	private IFile fExecFile;

	/**
	 * Whether the target is little endian.
	 */
	private Boolean fIsLittleEndian = null;
	
	private RunningInfo fRunningInfo = null;

	/**
	 * The target's preference set.
	 */
	private Preferences fPreferences = null;

	/**
	 * Constructor for CDebugTarget.
	 * @param target
	 */
	public CDebugTarget( ILaunch launch,
						 int targetType, 
						 ICDITarget cdiTarget, 
						 String name,
						 IProcess debuggeeProcess,
						 IProcess debuggerProcess,
						 IFile file,
						 boolean allowsTerminate,
						 boolean allowsDisconnect )
	{
		super( null );
		setLaunch( launch );
		setTargetType( targetType );
		setDebugTarget( this );
		setName( name );
		setProcess( debuggeeProcess );
		setCDITarget( cdiTarget );
		initializePreferences();
		setExecFile( file );
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

	/**
	 * Initialize state from the underlying debug session.
	 * 
	 */
	protected void initialize() 
	{
		initializeState();
		initializeBreakpoints();
		initializeRegisters();
		initializeMemoryManager();
		initializeSourceManager();
		getLaunch().addDebugTarget( this );
		fireCreationEvent();
	}

	private void initializeBreakpoints()
	{
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener( this );
		setBreakpoints();
	}

	/**
	 * Adds all of the pre-existing threads to this debug target.
	 * 
	 */
	protected void initializeState()
	{
		ICDIThread[] threads = new ICDIThread[0];
		try
		{
			threads = getCDITarget().getThreads();
		}
		catch( CDIException e )
		{
			// ignore
		}
		for ( int i = 0; i < threads.length; ++i )
			createThread( threads[i] );
		
		// Fire thread creation events.
		List list = getThreadList();
		ArrayList debugEvents = new ArrayList( list.size() );
		Iterator it = list.iterator();
		while( it.hasNext() )
		{
			debugEvents.add( ((CThread)it.next()).createCreateEvent() );
		}
		fireEventSet( (DebugEvent[])debugEvents.toArray( new DebugEvent[debugEvents.size()] ) );
	}

	/**
	 * Installs all C/C++ breakpoints that currently exist in
	 * the breakpoint manager.
	 * 
	 */
	public void setBreakpoints()
	{
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] bps = manager.getBreakpoints( CDebugModel.getPluginIdentifier() );
		for ( int i = 0; i < bps.length; i++ )
		{
			if ( bps[i] instanceof ICBreakpoint && 
				 getBreakpointManager().isTargetBreakpoint( (ICBreakpoint)bps[i] ) && 
				 !getBreakpointManager().isCDIRegistered( (ICBreakpoint)bps[i] ) ) 
			{
				if ( bps[i] instanceof ICAddressBreakpoint )
				{
					// disable address breakpoints to prevent the debugger to insert them prematurely 
					try
					{
						bps[i].setEnabled( false );
					}
					catch( CoreException e )
					{
					}
				}
				breakpointAdded0( bps[i] );
			}
		}
	}

	protected void initializeRegisters()
	{
		getRegisterManager().initialize();
	}

	protected void initializeMemoryManager()
	{
		fMemoryManager = new CMemoryManager( this );
	}

	protected void initializeSourceManager()
	{
		ISourceLocator locator = getLaunch().getSourceLocator();
		if ( locator instanceof IAdaptable )
		{
			ICSourceLocator clocator = (ICSourceLocator)((IAdaptable)locator).getAdapter( ICSourceLocator.class );
			if ( clocator instanceof IAdaptable )
			{
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
	public IProcess getProcess()
	{
		return fDebuggeeProcess;
	}

	/**
	 * Sets the process associated with this debug target,
	 * possibly <code>null</code>. Set on creation.
	 * 
	 * @param process the system process associated with the
	 * 	underlying CDI target, or <code>null</code> if no process is
	 * 	associated with this debug target (for example, a core dump debugging).
	 */
	protected void setProcess( IProcess debuggeeProcess )
	{
		fDebuggeeProcess = debuggeeProcess;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getThreads()
	 */
	public IThread[] getThreads()
	{
		List threads = getThreadList();
		return (IThread[])threads.toArray( new IThread[threads.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#hasThreads()
	 */
	public boolean hasThreads() throws DebugException
	{
		return getThreadList().size() > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getName()
	 */
	public String getName() throws DebugException
	{
		return fName;
	}

	/**
	 * Sets the name of this debug target.
	 *  
	 * @param name the name of this debug target
	 */
	protected void setName( String name ) 
	{
		fName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(IBreakpoint)
	 */
	public boolean supportsBreakpoint( IBreakpoint breakpoint )
	{
		if ( !getConfiguration().supportsBreakpoints() )
			return false;
		return ( breakpoint instanceof ICBreakpoint && getBreakpointManager().isCDIRegistered( (ICBreakpoint)breakpoint ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved( ILaunch launch )
	{
		if ( !isAvailable() )
		{
			return;
		}
		if ( launch.equals( getLaunch() ) )
		{
			// This target has been deregistered, but it hasn't successfully terminated.
			// Update internal state to reflect that it is disconnected
			disconnected();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded( ILaunch launch )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged( ILaunch launch )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate()
	{
		return supportsTerminate() && isAvailable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated()
	{
		return fTerminated;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException
	{
		if ( isTerminating() ) 
		{
			return;
		}
		setTerminating( true );
		DebugPlugin.getDefault().asyncExec( 
						new Runnable() 
							{
								public void run() 
								{
									try 
									{
										getCDITarget().terminate();
									}
									catch( CDIException e ) 
									{
										setTerminating( false );
										try 
										{
											targetRequestFailed( e.getMessage(), e );
										} 
										catch( DebugException e1 ) 
										{
											CDebugUtils.error( e1.getStatus(), CDebugTarget.this );
										}
									}
								}
							} );
	}

	/**
	 * Sets whether this debug target is terminated
	 * 
	 * @param terminated <code>true</code> if this debug
	 * 		  target is terminated, otherwise <code>false</code>
	 */
	protected void setTerminated( boolean terminated )
	{
		fTerminated = terminated;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume()
	{
		return getConfiguration().supportsResume() && isSuspended() && isAvailable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend()
	{
		if ( !getConfiguration().supportsSuspend() )
			return false; 
		if ( !isSuspended() && isAvailable() )
		{
			// only allow suspend if no threads are currently suspended
			IThread[] threads = getThreads();
			for ( int i = 0; i < threads.length; i++ )
			{
				if ( threads[i].isSuspended() )
				{
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
	public boolean isSuspended()
	{
		return fSuspended;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException
	{
		if ( !isSuspended() && !isSuspending() ) 
			return;
		try 
		{
			getCDITarget().resume();
		} 
		catch( CDIException e ) 
		{
			targetRequestFailed( e.getMessage(), e );
		}	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException
	{
		if ( isSuspended() || isSuspending() )
			return;
		
		setSuspending(true);
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() { 
				try {
					getCDITarget().suspend();
				} catch( CDIException e ) {
					try {
						targetRequestFailed( e.getMessage(), e );
					} catch (DebugException e1) {
						CDebugUtils.error(e1.getStatus(), CDebugTarget.this);
					}
					
				} finally {
					setSuspending(false);					
				}
			}
		});
	}

	protected void setSuspending( boolean value ) 
	{
		fSuspending = value;
	}

	protected boolean isSuspending() 
	{
		return fSuspending;
	}
	
	/**
	 * Notifies threads that they have been suspended.
	 * 
	 */
	protected void suspendThreads( ICDISuspendedEvent event )
	{
		Iterator it = getThreadList().iterator();
		ICDIEvent[] events = new ICDIEvent[] {event};
		while( it.hasNext() )
		{
			((CThread)it.next()).handleDebugEvents( events );
		}
	}

	/**
	 * Refreshes the thread list.
	 * 
	 */
	protected synchronized List refreshThreads()
	{
		ArrayList newThreads = new ArrayList( 5 );
		ArrayList list = new ArrayList( 5 );
		ArrayList debugEvents = new ArrayList( 5 );
		List oldList = (List)getThreadList().clone();

		ICDIThread[] cdiThreads = new ICDIThread[0];
		try
		{
			cdiThreads = getCDITarget().getThreads();
		}
		catch( CDIException e )
		{
			CDebugCorePlugin.log( e );
		}
		for ( int i = 0; i < cdiThreads.length; ++i )
		{
			CThread thread = findThread( oldList, cdiThreads[i] ); 
			if ( thread == null )
			{
				thread = new CThread( this, cdiThreads[i] );
				newThreads.add( thread );
			}
			else
			{
				oldList.remove( thread );
			}
			list.add( thread );
		}
		
		Iterator it = oldList.iterator();
		while( it.hasNext() )
		{
			CThread thread = (CThread)it.next();
			thread.terminated();
			debugEvents.add( thread.createTerminateEvent() );
		}
		setThreadList( list );
		it = newThreads.iterator();
		while( it.hasNext() )
		{
			debugEvents.add( ((CThread)it.next()).createCreateEvent() );
		}
		if ( debugEvents.size() > 0 )
			fireEventSet( (DebugEvent[])debugEvents.toArray( new DebugEvent[debugEvents.size()] ) );
		setCurrentThread();
		return newThreads;
	}

	/**
	 * Notifies threads that they have been resumed
	 */
	protected synchronized void resumeThreads( List debugEvents, int detail )
	{
		Iterator it = getThreadList().iterator();
		while( it.hasNext() )
		{
			((CThread)it.next()).resumed( detail, debugEvents );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(IBreakpoint)
	 */
	public void breakpointAdded( IBreakpoint breakpoint )
	{
		if ( !(breakpoint instanceof ICBreakpoint) || 
			 !getBreakpointManager().isTargetBreakpoint( (ICBreakpoint)breakpoint ) || 
			 !isAvailable() )
			return;
		breakpointAdded0( breakpoint );
	}

	private void breakpointAdded0( IBreakpoint breakpoint )
	{
		if ( !isAvailable() )
			return;
		if ( breakpoint instanceof ICAddressBreakpoint && !getBreakpointManager().supportsAddressBreakpoint( (ICAddressBreakpoint)breakpoint ) )
	   		return;
		if ( getConfiguration().supportsBreakpoints() )
		{
			try
			{
				getBreakpointManager().setBreakpoint( (ICBreakpoint)breakpoint );
			}
			catch( DebugException e )
			{
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointRemoved( IBreakpoint breakpoint, IMarkerDelta delta )
	{
		if ( !(breakpoint instanceof ICBreakpoint) || 
			 !getBreakpointManager().isTargetBreakpoint( (ICBreakpoint)breakpoint ) || 
			 !isAvailable() )
			return;
		try
		{
			getBreakpointManager().removeBreakpoint( (ICBreakpoint)breakpoint );
		}
		catch( DebugException e )
		{
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointChanged( IBreakpoint breakpoint, IMarkerDelta delta )
	{
		if ( !(breakpoint instanceof ICBreakpoint) || 
			 !getBreakpointManager().isTargetBreakpoint( (ICBreakpoint)breakpoint ) || 
			 !isAvailable() ||
			 delta == null )
			return;
		try
		{
			getBreakpointManager().changeBreakpointProperties( (ICBreakpoint)breakpoint, delta );
		}
		catch( DebugException e )
		{
		}
	}

	/**
	 * Returns whether this debug target supports disconnecting.
	 * 
	 * @return whether this debug target supports disconnecting
	 */
	protected boolean supportsDisconnect()
	{
		return getConfiguration().supportsDisconnect();
	}
	
	/**
	 * Returns whether this debug target supports termination.
	 * 
	 * @return whether this debug target supports termination
	 */
	protected boolean supportsTerminate()
	{
		return getConfiguration().supportsTerminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#canDisconnect()
	 */
	public boolean canDisconnect()
	{
		return supportsDisconnect() && isAvailable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#disconnect()
	 */
	public void disconnect() throws DebugException
	{
		if ( isDisconnected() )
		{
			// already done
			return;
		}

		if ( !canDisconnect() )
		{
			notSupported( CDebugCorePlugin.getResourceString("internal.core.model.CDebugTarget.Disconnect_session_unsupported") ); //$NON-NLS-1$
		}

		try
		{
			getCDITarget().disconnect();
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#isDisconnected()
	 */
	public boolean isDisconnected()
	{
		return fDisconnected;
	}

	/**
	 * Sets whether this debug target is disconnected
	 * 
	 * @param disconnected <code>true</code> if this debug
	 *  	  target is disconnected, otherwise <code>false</code>
	 */
	protected void setDisconnected( boolean disconnected )
	{
		fDisconnected = disconnected;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	public boolean supportsStorageRetrieval()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
	 */
	public IMemoryBlock getMemoryBlock( long startAddress, long length ) throws DebugException
	{
		return null;
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() 
	{
		return fLaunch;
	}

	/**
	 * Sets the launch this target is contained in
	 * 
	 * @param launch the launch this target is contained in
	 */
	private void setLaunch( ILaunch launch )
	{
		fLaunch = launch;
	}
	
	/**
	 * Returns the list of threads contained in this debug target.
	 * 
	 * @return list of threads
	 */
	protected ArrayList getThreadList()
	{
		return fThreads;
	}
	
	/**
	 * Sets the list of threads contained in this debug target.
	 * Set to an empty collection on creation. Threads are
	 * added and removed as they start and end. On termination
	 * this collection is set to the immutable singleton empty list.
	 * 
	 * @param threads empty list
	 */
	private void setThreadList( ArrayList threads )
	{
		fThreads = threads;
	}

	private void setCDITarget( ICDITarget cdiTarget )
	{
		fCDITarget = cdiTarget;
	}

	/* (non-Javadoc)
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter ) 
	{
		if ( adapter.equals( IDebugTarget.class ) )
			return this;
		if ( adapter.equals( ICDebugTarget.class ) )
			return this;
		if ( adapter.equals( CDebugTarget.class ) )
			return this;
		if ( adapter.equals( ICDITarget.class ) )
			return fCDITarget;
		if ( adapter.equals( IState.class ) )
			return this;
		if ( adapter.equals( ICExpressionEvaluator.class ) )
			return this;
		if ( adapter.equals( ICDebugTargetType.class ) )
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
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(ICDIEvent)
	 */
	public void handleDebugEvents( ICDIEvent[] events )
	{
		for (int i = 0; i < events.length; i++)
		{
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();
			if ( source == null && event instanceof ICDIDestroyedEvent )
			{
				handleTerminatedEvent( (ICDIDestroyedEvent)event );
			}
			else if ( source.getTarget().equals( getCDITarget() ) )
			{
				if ( event instanceof ICDICreatedEvent )
				{
					if ( source instanceof ICDIThread )
					{
						handleThreadCreatedEvent( (ICDICreatedEvent)event );
					}
					if ( source instanceof ICDISharedLibrary )
					{
						getSharedLibraryManager().sharedLibraryLoaded( (ICDISharedLibrary)source );
					}
				}
				else if ( event instanceof ICDISuspendedEvent )
				{
					if ( source instanceof ICDITarget || source instanceof ICDIThread )
					{
						handleSuspendedEvent( (ICDISuspendedEvent)event );
					}
				}
				else if ( event instanceof ICDIResumedEvent )
				{
					if ( source instanceof ICDITarget )
					{
						handleResumedEvent( (ICDIResumedEvent)event );
					}
				}
				else if ( event instanceof ICDIExitedEvent )
				{
					if ( source instanceof ICDITarget )
					{
						handleExitedEvent( (ICDIExitedEvent)event );
					}
				}
				else if ( event instanceof ICDIDestroyedEvent )
				{
					if ( source instanceof ICDIThread )
					{
						handleThreadTerminatedEvent( (ICDIDestroyedEvent)event );
					}
					if ( source instanceof ICDISharedLibrary )
					{
						getSharedLibraryManager().sharedLibraryUnloaded( (ICDISharedLibrary)source );
					}
				}
				else if ( event instanceof ICDIDisconnectedEvent )
				{
					if ( source instanceof ICDITarget )
					{
						handleDisconnectedEvent( (ICDIDisconnectedEvent)event );
					}
				}
				else if ( event instanceof ICDIChangedEvent )
				{
					if ( source instanceof ICDITarget )
					{
						handleChangedEvent( (ICDIChangedEvent)event );
					}
					if ( source instanceof ICDISharedLibrary )
					{
						getSharedLibraryManager().symbolsLoaded( (ICDISharedLibrary)source );
					}
					if ( source instanceof ICDISignal )
					{
						getSignalManager().signalChanged( (ICDISignal)source );
					}
				}
				else if ( event instanceof ICDIRestartedEvent )
				{
					if ( source instanceof ICDITarget )
					{
						handleRestartedEvent( (ICDIRestartedEvent)event );
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IRestart#canRestart()
	 */
	public boolean canRestart()
	{
		return getConfiguration().supportsRestart() && isSuspended() && isAvailable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IRestart#restart()
	 */
	public void restart() throws DebugException
	{
		if ( !canRestart() )
		{
			return;
		}

		try
		{
			ICDILocation location = getCDISession().getBreakpointManager().createLocation( "", "main", 0 ); //$NON-NLS-1$ //$NON-NLS-2$
			setInternalTemporaryBreakpoint( location );
			getCDITarget().restart();
			restarted();
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/**
	 * Updates the state of this target for restarting.
	 * 
	 */
	protected void restarted()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryRetrieval#getSupportedFormats()
	 */
	public int[] getSupportedFormats() throws DebugException
	{
		return null;
	}

	/**
	 * Returns whether this target is available to handle client 
	 * requests.
	 * 
	 * @return whether this target is available to handle client requests
	 */
	public boolean isAvailable()
	{
		return !( isTerminated() || isTerminating() || isDisconnected() );
	}

	/**
	 * Sets whether this target is suspended.
	 * 
	 * @param suspended whether this target is suspended
	 */
	private void setSuspended( boolean suspended )
	{
		fSuspended = suspended;
	}

	/**
	 * Returns whether this target is in the process of terminating.
	 * 
	 * @return whether this target is terminating
	 */
	protected boolean isTerminating()
	{
		return fTerminating;
	}

	/**
	 * Sets whether this target is in the process of terminating.
	 * 
	 * @param terminating whether this target is terminating
	 */
	protected void setTerminating( boolean terminating )
	{
		fTerminating = terminating;
	}

	/**
	 * Updates the state of this target to be terminated,
	 * if not already terminated.
	 */
	protected void terminated()
	{
		setTerminating( false );
		if ( !isTerminated() )
		{
			if ( !isDisconnected() )
			{
				setTerminated( true );
			}
			cleanup();
			fireTerminateEvent();
		}
	}
	
	/**
	 * Updates the state of this target for disconnection.
	 * 
	 */
	protected void disconnected()
	{
		if ( !isDisconnected() )
		{
			setDisconnected( true );
			cleanup();
			fireTerminateEvent();
		}
	}

	/** 
	 * Cleans up the internal state of this debug target as a result 
	 * of a session ending.
	 * 
	 */
	protected void cleanup()
	{
		resetStatus();
		removeAllThreads();
		getCDISession().getEventManager().removeEventListener( this );
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener( this );
		DebugPlugin.getDefault().getExpressionManager().removeExpressionListener( this );
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener( this );
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
	 * Removes all threads from this target's collection
	 * of threads, firing a terminate event for each.
	 * 
	 */
	protected void removeAllThreads()
	{
		List threads = getThreadList();
		setThreadList( new ArrayList( 0 ) );
		ArrayList debugEvents = new ArrayList( threads.size() );
		Iterator it = threads.iterator();
		while( it.hasNext() )
		{
			CThread thread = (CThread)it.next();
			thread.terminated();
			debugEvents.add( thread.createTerminateEvent() );
		}
		fireEventSet( (DebugEvent[])debugEvents.toArray( new DebugEvent[debugEvents.size()] ) );
	}

	/**
	 * Removes all expressions from this target.
	 * 
	 */
	protected void removeAllExpressions()
	{
		IExpressionManager em = DebugPlugin.getDefault().getExpressionManager();
		IExpression[] expressions = em.getExpressions();
		for ( int i = 0; i < expressions.length; ++i )
		{
			if ( expressions[i] instanceof CExpression && expressions[i].getDebugTarget().equals( this ) )
			{
				em.removeExpression( expressions[i] );
			}
		}
	}

	/**
	 * Creates, adds and returns a thread for the given underlying 
	 * CDI thread. A creation event is fired for the thread.
	 * Returns <code>null</code> if during the creation of the thread 
	 * this target is set to the disconnected state.
	 * 
	 * @param thread the underlying CDI thread
	 * @return model thread
	 */
	protected CThread createThread( ICDIThread cdiThread )
	{
		CThread thread = new CThread( this, cdiThread );
		getThreadList().add( thread );
		return thread;
	}

	/**
	 * Creates a new thread from the given CDI thread and initializes
	 * its state to "Running".
	 * 
	 * @see CDebugTarget#createThread(ICDIThread)
	 */
	protected CThread createRunningThread( ICDIThread cdiThread )
	{
		CThread thread = new CThread( this, cdiThread );
		thread.setRunning( true );
		getThreadList().add( thread );
		return thread;
	}

	private void handleSuspendedEvent( ICDISuspendedEvent event )
	{
		setSuspended( true );
		setCurrentStateId( IState.SUSPENDED );
		ICDISessionObject reason = event.getReason();
		setCurrentStateInfo( reason );
		setRunningInfo( null );
		// Reset the registers that have errors.
		getRegisterManager().targetSuspended();
		setSuspensionThread();
		getBreakpointManager().skipBreakpoints( false );
		List newThreads = refreshThreads();
		if ( event.getSource() instanceof ICDITarget )
		{
			suspendThreads( event );
		}
		// We need this for debuggers that don't have notifications 
		// for newly created threads.
		else if ( event.getSource() instanceof ICDIThread )
		{
			CThread thread = findThread( (ICDIThread)event.getSource() );
			if ( thread != null && newThreads.contains( thread ) )
			{
				ICDIEvent[] evts = new ICDIEvent[]{event};
				thread.handleDebugEvents( evts );
			}
		}
		if ( reason instanceof ICDIEndSteppingRange )
		{
			handleEndSteppingRange( (ICDIEndSteppingRange)reason );
		}
		else if ( reason instanceof ICDIBreakpointHit )
		{
			handleBreakpointHit( (ICDIBreakpointHit)reason );
		}
		else if ( reason instanceof ICDISignalReceived )
		{
			handleSuspendedBySignal( (ICDISignalReceived)reason );
		}
		else if ( reason instanceof ICDIWatchpointTrigger )
		{
			handleWatchpointTrigger( (ICDIWatchpointTrigger)reason );
		}
		else if ( reason instanceof ICDIWatchpointScope )
		{
			handleWatchpointScope( (ICDIWatchpointScope)reason );
		}
		else if ( reason instanceof ICDIErrorInfo )
		{
			handleErrorInfo( (ICDIErrorInfo)reason );
		}
		else if ( reason instanceof ICDISharedLibraryEvent )
		{
			handleSuspendedBySolibEvent( (ICDISharedLibraryEvent)reason );
		}
	}
/*
	private boolean handleInternalSuspendedEvent( ICDISuspendedEvent event )
	{
		setRetryBreakpoints( true );
		setBreakpoints();
		RunningInfo info = getRunningInfo();
		if ( info != null )
		{
			switch( info.getType() )
			{
				case ICDIResumedEvent.CONTINUE:
					return internalResume();
				case ICDIResumedEvent.STEP_INTO:
					return internalStepInto( info.getStackDepth() );
				case ICDIResumedEvent.STEP_OVER:
					return internalStepOver( info.getStackDepth() );
				case ICDIResumedEvent.STEP_RETURN:
					return internalStepReturn( info.getStackDepth() );
			}
		}
		return internalResume();
	}

	private boolean internalResume()
	{
		boolean result = false;
		try
		{
			getCDITarget().resume();
		}
		catch( CDIException e )
		{
			result = true;
		}
		return result;
	}

	private boolean internalStepInto( int oldDepth )
	{
		return internalStepOver( oldDepth );
	}

	private boolean internalStepOver( int oldDepth )
	{
		boolean result = true;
		try
		{
			CThread thread = (CThread)getCurrentThread();
			if ( thread != null )
			{
				int depth = thread.getStackDepth();
				if ( oldDepth < depth )
				{
					ICDIStackFrame[] frames = thread.getCDIStackFrames( depth - oldDepth - 1, depth - oldDepth - 1 );
					if ( frames.length == 1 )
					{
						thread.getCDIThread().setCurrentStackFrame( frames[0] );
						getCDITarget().stepReturn();
						result = false;
					}
				}
			}
		}
		catch( CDIException e )
		{
		}
		catch( DebugException e )
		{
		}
		return result;
	}

	private boolean internalStepReturn( int oldDepth )
	{
		boolean result = true;
		try
		{
			CThread thread = (CThread)getCurrentThread();
			if ( thread != null )
			{
				int depth = thread.getStackDepth();
				if ( oldDepth < depth )
				{
					ICDIStackFrame[] frames = thread.getCDIStackFrames( depth - oldDepth, depth - oldDepth );
					if ( frames.length == 1 )
					{
						thread.getCDIThread().setCurrentStackFrame( frames[0] );
						getCDITarget().stepReturn();
						result = false;
					}
				}
			}
		}
		catch( CDIException e )
		{
		}
		catch( DebugException e )
		{
		}
		return result;
	}
*/
	private void handleResumedEvent( ICDIResumedEvent event )
	{
		setSuspended( false );
		setCurrentStateId( IState.RUNNING );
		setCurrentStateInfo( null );
		resetStatus();
		ArrayList debugEvents = new ArrayList( 10 );
		int detail = DebugEvent.UNSPECIFIED;
		switch( event.getType() )
		{
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
		resumeThreads( debugEvents, detail );
		if ( getRunningInfo() == null )
			setRunningInfo( event.getType() );
		debugEvents.add( createResumeEvent( detail ) );
		fireEventSet( (DebugEvent[])debugEvents.toArray( new DebugEvent[debugEvents.size()] ) );
	}
	
	private void handleEndSteppingRange( ICDIEndSteppingRange endSteppingRange )
	{
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleBreakpointHit( ICDIBreakpointHit breakpointHit )
	{
		fireSuspendEvent( DebugEvent.BREAKPOINT );
	}

	private void handleWatchpointTrigger( ICDIWatchpointTrigger wt )
	{
		fireSuspendEvent( DebugEvent.BREAKPOINT );
	}
	
	private void handleWatchpointScope( ICDIWatchpointScope ws )
	{
		ICBreakpoint watchpoint = getBreakpointManager().getBreakpoint( ws.getWatchpoint() );
		if ( watchpoint != null )
		{
			try
			{
				getBreakpointManager().removeBreakpoint( watchpoint );
			}
			catch( DebugException e )
			{
				CDebugCorePlugin.log( e );
			}
			fireSuspendEvent( DebugEvent.BREAKPOINT );
		}
	}
	
	private void handleSuspendedBySignal( ICDISignalReceived signal )
	{
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleErrorInfo( ICDIErrorInfo info )
	{
		setStatus( ICDebugElementErrorStatus.ERROR, ( info != null ) ? info.getMessage() : null );
		if ( info != null )
		{
			MultiStatus status = new MultiStatus( CDebugCorePlugin.getUniqueIdentifier(),
												  ICDebugInternalConstants.STATUS_CODE_ERROR,
												  CDebugCorePlugin.getResourceString("internal.core.model.CDebugTarget.Execution_suspended_because_of_error"), null ); //$NON-NLS-1$
			StringTokenizer st = new StringTokenizer( info.getDetailMessage(), "\n\r" ); //$NON-NLS-1$
			while( st.hasMoreTokens() )
			{
				String token = st.nextToken();
				if ( token.length() > 200 )
				{
					token = token.substring( 0, 200 );
				}
				status.add( new Status( IStatus.ERROR, 
										status.getPlugin(), 
										ICDebugInternalConstants.STATUS_CODE_ERROR,
										token,
										null ) );
			} 
			CDebugUtils.error( status, this );
		}
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleSuspendedBySolibEvent( ICDISharedLibraryEvent solibEvent )
	{
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleExitedEvent( ICDIExitedEvent event )
	{
		removeAllThreads();
		setCurrentStateId( IState.EXITED );
		setCurrentStateInfo( event.getReason() );
		fireChangeEvent( DebugEvent.CONTENT );
		if ( getConfiguration().terminateSessionOnExit() )
			terminated();
	}

	private void handleTerminatedEvent( ICDIDestroyedEvent event )
	{
		terminated();
	}

	private void handleDisconnectedEvent( ICDIDisconnectedEvent event )
	{
		setCurrentStateId( IState.DISCONNECTED );
		setCurrentStateInfo( null );
		disconnected();
	}

	private void handleChangedEvent( ICDIChangedEvent event )
	{
	}

	private void handleRestartedEvent( ICDIRestartedEvent event )
	{
	}

	private void handleThreadCreatedEvent( ICDICreatedEvent event )
	{
		ICDIThread cdiThread = (ICDIThread)event.getSource();
		CThread thread = findThread( cdiThread );
		if ( thread == null )
		{ 
			thread = createThread( cdiThread );
			thread.fireCreationEvent();
		}
	}

	private void handleThreadTerminatedEvent( ICDIDestroyedEvent event )
	{
		ICDIThread cdiThread = (ICDIThread)event.getSource();
		CThread thread = findThread( cdiThread );
		if ( thread != null) 
		{
			getThreadList().remove( thread );
			thread.terminated();
			thread.fireTerminateEvent();
		}
	}

	/**
	 * Finds and returns the model thread for the associated CDI thread, 
	 * or <code>null</code> if not found.
	 * 
	 * @param the underlying CDI thread
	 * @return the associated model thread
	 */
	public CThread findThread( ICDIThread cdiThread )
	{
		List threads = getThreadList();
		for ( int i = 0; i < threads.size(); i++ )
		{
			CThread t = (CThread)threads.get( i );
			if ( t.getCDIThread().equals( cdiThread ) )
				return t;
		}
		return null;
	}

	public CThread findThread( List threads, ICDIThread cdiThread )
	{
		for ( int i = 0; i < threads.size(); i++ )
		{
			CThread t = (CThread)threads.get( i );
			if ( t.getCDIThread().equals( cdiThread ) )
				return t;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IState#getCurrentStateId()
	 */
	public int getCurrentStateId()
	{
		return fCurrentStateId;
	}

	/**
	 * Sets the current state identifier.
	 * 
	 * @param id the identifier
	 */
	private void setCurrentStateId( int id )
	{
		fCurrentStateId = id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IState#getCurrentStateInfo()
	 */
	public Object getCurrentStateInfo()
	{
		return fCurrentStateInfo;
	}

	/**
	 * Sets the info object of the current state.
	 * 
	 * @param id the info object
	 */
	private void setCurrentStateInfo( Object info )
	{
		fCurrentStateInfo = info;
	}

	/**
	 * Returns the number of suspend events that have occurred in this
	 * target.
	 * 
	 * @return the number of suspend events that have occurred in this
	 * target
	 */
	protected int getSuspendCount()
	{
		return fSuspendCount;
	}

	/**
	 * Increments the suspend counter for this target.
	 */
	protected void incrementSuspendCount()
	{
		fSuspendCount++;
	}

	/**
	 * Overrides the superclass method by incrementing the suspend counter.
	 * 
	 * @param detail The int detail of the event
	 * @see org.eclipse.debug.core.DebugEvent
	 */
	public void fireSuspendEvent( int detail )
	{
		incrementSuspendCount();
		super.fireSuspendEvent( detail );
	}
	
	protected void setCurrentThread()
	{
		ICDIThread currentCDIThread = null;
		try
		{
			currentCDIThread = getCDITarget().getCurrentThread();
		}
		catch( CDIException e )
		{
			CDebugCorePlugin.log( e );
		}
		Iterator it = getThreadList().iterator();
		while( it.hasNext() )
		{
			CThread thread = (CThread)it.next();
			thread.setCurrent( currentCDIThread != null && thread.getCDIThread().equals( currentCDIThread ) );
		}
		if ( currentCDIThread == null && !getThreadList().isEmpty() )
		{
			((CThread)getThreadList().get( 0 )).setCurrent( true );
		}
	}

	/**
	 * Returns the debug configuration of this target.
	 * 
	 * @return the debug configuration of this target
	 */
	protected ICDIConfiguration getConfiguration()
	{
		return fConfig;
	}
	
	/**
	 * Sets the debug configuration of this target.
	 * 
	 * @param config the debug configuration to set
	 */
	private void setConfiguration( ICDIConfiguration config )
	{
		fConfig = config;
	}

	protected boolean supportsExpressionEvaluation()
	{
		return getConfiguration().supportsExpressionEvaluation();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICExpressionEvaluator#evaluateExpressionToString(String)
	 */
	public String evaluateExpressionToString( String expression ) throws DebugException
	{
		try
		{
			return getCDITarget().evaluateExpressionToString( expression );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICExpressionEvaluator#canEvaluate()
	 */
	public boolean canEvaluate()
	{
		return supportsExpressionEvaluation() && isAvailable() && isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionListener#expressionAdded(IExpression)
	 */
	public void expressionAdded( IExpression expression )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionListener#expressionChanged(IExpression)
	 */
	public void expressionChanged( IExpression expression )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionListener#expressionRemoved(IExpression)
	 */
	public void expressionRemoved( IExpression expression )
	{
		if ( expression != null && expression.getDebugTarget().equals( this ) && expression instanceof CExpression )
		{
			ICDIExpressionManager em = getCDISession().getExpressionManager();
			try
			{
				em.destroyExpression( ((CExpression)expression).getCDIExpression() );
			}
			catch( CDIException e )
			{
				// do nothing
			}
		}
	}
	
	public void setInternalTemporaryBreakpoint( ICDILocation location ) throws DebugException
	{
		try
		{
			getCDISession().getBreakpointManager().setLocationBreakpoint( ICDIBreakpoint.TEMPORARY,
																		  location,
																		  null,
																		  null );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IRunToLine#canRunToLine(IResource, int)
	 */
	public boolean canRunToLine( String fileName, int lineNumber )
	{
		// check if supports run to line
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IRunToLine#runToLine(IResource, int)
	 */
	public void runToLine( String fileName, int lineNumber, boolean skipBreakpoints ) throws DebugException
	{
		if ( !canRunToLine( fileName, lineNumber ) )
			return;
		if ( skipBreakpoints ) {
			getBreakpointManager().skipBreakpoints( true );
		}
		ICDILocation location = getCDISession().getBreakpointManager().createLocation( fileName, null, lineNumber );
		try
		{
			getCDITarget().runUntil( location );
		}
		catch( CDIException e )
		{
			if ( skipBreakpoints ) {
				getBreakpointManager().skipBreakpoints( false );
			}
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IRunToLine#canRunToLine(IResource, int)
	 */
	public boolean canRunToLine( IFile file, int lineNumber )
	{
		// check if supports run to line
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IRunToLine#runToLine(IResource, int)
	 */
	public void runToLine( IFile file, int lineNumber, boolean skipBreakpoints ) throws DebugException
	{
		if ( !canRunToLine( file, lineNumber ) )
			return;
		runToLine( file.getLocation().lastSegment(), lineNumber, skipBreakpoints );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ISwitchToThread#setCurrentThread(IThread)
	 */
	public void setCurrentThread( IThread thread ) throws DebugException
	{
		if ( !isSuspended() || !isAvailable() || !(thread instanceof CThread) )
			return;
		try
		{
			CThread oldThread = (CThread)getCurrentThread();
			if ( !thread.equals( oldThread ) )
			{
				getCDITarget().setCurrentThread( ((CThread)thread).getCDIThread() );
				if ( oldThread != null )
					oldThread.setCurrent( false );
				((CThread)thread).setCurrent( true );
			}
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.ISwitchToThread#getCurrentThread()
	 */
	public IThread getCurrentThread() throws DebugException
	{
		IThread[] threads = getThreads();
		for ( int i = 0; i < threads.length; ++i )
		{
			if ( ((CThread)threads[i]).isCurrent() )
				return threads[i];
		}
		return null;
	}

	protected ISourceLocator getSourceLocator()
	{
		return getLaunch().getSourceLocator();
	}
	
	protected void resetRegisters()
	{
		getRegisterManager().reset();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.ICDebugTargetType#getTargetType()
	 */
	public int getTargetType()
	{
		return fTargetType;
	}
	
	private void setTargetType( int targetType )
	{
		fTargetType = targetType;
	}
	
	protected boolean isCoreDumpTarget()
	{
		return ( getTargetType() == ICDebugTargetType.TARGET_TYPE_LOCAL_CORE_DUMP );
	}
	
	protected CMemoryManager getMemoryManager()
	{
		return fMemoryManager;
	}
	
	protected void disposeMemoryManager()
	{
		getMemoryManager().dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IExecFileInfo#isLittleEndian()
	 */
	public boolean isLittleEndian()
	{
		if ( fIsLittleEndian == null )
		{
			fIsLittleEndian = Boolean.TRUE;
			if ( getExecFile() != null && CoreModel.getDefault().isBinary( getExecFile() ) )
			{
				ICElement cFile = CCorePlugin.getDefault().getCoreModel().create( getExecFile() );
				if ( cFile instanceof IBinary )
				{
					fIsLittleEndian = new Boolean( ((IBinary)cFile).isLittleEndian() );
				}
			}
		}
		return fIsLittleEndian.booleanValue();
	}
	
	public IFile getExecFile()
	{
		return fExecFile;
	}
	
	private void setExecFile( IFile file )
	{
		fExecFile = file;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IExecFileInfo#getGlobals()
	 */
	public IGlobalVariableDescriptor[] getGlobals() throws DebugException
	{
		ArrayList list = new ArrayList();
		if ( getExecFile() != null && CoreModel.getDefault().isBinary( getExecFile() ) )
		{
			ICElement cFile = CCorePlugin.getDefault().getCoreModel().create( getExecFile() );
			if ( cFile instanceof IParent )
			{
				list.addAll( getCFileGlobals( (IParent)cFile ) );
			}
		}
		return (IGlobalVariableDescriptor[])list.toArray( new IGlobalVariableDescriptor[list.size()] );
	}

	private List getCFileGlobals( IParent file ) throws DebugException
	{
		ArrayList list = new ArrayList();
		try 
		{
			ICElement[] elements = file.getChildren();
			for ( int i = 0; i < elements.length; ++i )
			{
				if ( elements[i] instanceof org.eclipse.cdt.core.model.IVariable )
				{
					list.add( createGlobalVariable( (org.eclipse.cdt.core.model.IVariable)elements[i] ) );
				}
				else if ( elements[i] instanceof org.eclipse.cdt.core.model.IParent )
				{
					list.addAll( getCFileGlobals( (org.eclipse.cdt.core.model.IParent)elements[i] ) );
				}
			}
		} 
		catch( CModelException e ) 
		{
			requestFailed( CoreModelMessages.getString( "CDebugTarget.Unable_to_get_globals_1" ) + e.getMessage(), e ); //$NON-NLS-1$
		}
		return list;
	}

	private IGlobalVariableDescriptor createGlobalVariable( final org.eclipse.cdt.core.model.IVariable var )
	{
		return new IGlobalVariableDescriptor()
				  {
				  	  public String getName()
				  	  {
				  	  	  return var.getElementName();
				  	  }
				  	  
				  	  public IPath getPath()
				  	  {
						  IPath path = new Path( "" ); //$NON-NLS-1$
						  ICElement parent = var.getParent();
						  if ( parent instanceof IBinaryModule )
						  {
							  path = ((IBinaryModule)parent).getPath();
						  }
						  return path;
				  	  }
				  };
	}
	
	protected void setSharedLibraryManager( CSharedLibraryManager libman )
	{
		fSharedLibraryManager = libman;
	}
	
	protected CSharedLibraryManager getSharedLibraryManager()
	{
		return fSharedLibraryManager;
	}

	protected void disposeSharedLibraryManager()
	{
		fSharedLibraryManager.dispose();
	}

	protected void setSignalManager( CSignalManager sm )
	{
		fSignalManager = sm;
	}
	
	protected CSignalManager getSignalManager()
	{
		return fSignalManager;
	}

	protected void disposeSignalManager()
	{
		fSignalManager.dispose();
	}

	protected void disposeRegisterManager()
	{
		fRegisterManager.dispose();
	}

	protected void disposeGlobalVariableManager() {
		fGlobalVariableManager.dispose();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.model.IRunToAddress#canRunToAddress(long)
	 */
	public boolean canRunToAddress( long address )
	{
		// for now
		return canResume();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.model.IRunToAddress#runToLine(long)
	 */
	public void runToAddress( long address, boolean skipBreakpoints ) throws DebugException
	{
		if ( !canRunToAddress( address ) )
			return;
		if ( skipBreakpoints ) {
			getBreakpointManager().skipBreakpoints( true );
		}
		ICDILocation location = getCDISession().getBreakpointManager().createLocation( address );
		try
		{
			getCDITarget().runUntil( location );
		}
		catch( CDIException e )
		{
			if ( skipBreakpoints ) {
				getBreakpointManager().skipBreakpoints( false );
			}
			targetRequestFailed( e.getMessage(), e );
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeWithoutSignal#canResumeWithoutSignal()
	 */
	public boolean canResumeWithoutSignal()
	{
		// Check if the configuration supports this!!!
		return ( isSuspended() && getCurrentStateInfo() instanceof ICDISignalReceived );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeWithoutSignal#resumeWithoutSignal()
	 */
	public void resumeWithoutSignal() throws DebugException
	{
		try
		{
			getCDITarget().signal();
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IJumpToLine#canJumpToLine(IResource, int)
	 */
	public boolean canJumpToLine( IFile file, int lineNumber )
	{
		// check if supports jump to line
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IJumpToLine#jumpToLine(IResource, int)
	 */
	public void jumpToLine( IFile file, int lineNumber ) throws DebugException
	{
		if ( !canJumpToLine( file, lineNumber ) )
			return;
		jumpToLine( file.getLocation().lastSegment(), lineNumber );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IJumpToLine#canJumpToLine(IResource, int)
	 */
	public boolean canJumpToLine( String fileName, int lineNumber )
	{
		// check if supports jump to line
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IJumpToLine#jumpToLine(IResource, int)
	 */
	public void jumpToLine( String fileName, int lineNumber ) throws DebugException
	{
		if ( !canJumpToLine( fileName, lineNumber ) )
			return;
		ICDILocation location = getCDISession().getBreakpointManager().createLocation( fileName, null, lineNumber );
		try
		{
			getCDITarget().jump( location );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IJumpToAddress#canJumpToAddress(long)
	 */
	public boolean canJumpToAddress( long address )
	{
		// check if supports jump to address
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IJumpToAddress#jumpToAddress(long)
	 */
	public void jumpToAddress( long address ) throws DebugException
	{
		if ( !canJumpToAddress( address ) )
			return;
		ICDILocation location = getCDISession().getBreakpointManager().createLocation( address );
		try
		{
			getCDITarget().jump( location );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), e );
		}
	}

	public CRegisterManager getRegisterManager()
	{
		return fRegisterManager;
	}

	protected void setRegisterManager( CRegisterManager registerManager )
	{
		fRegisterManager = registerManager;
	}

	public IRegisterGroup[] getRegisterGroups() throws DebugException
	{
		return getRegisterManager().getRegisterGroups();
	}

	protected void disposeSourceManager()
	{
		ISourceLocator locator = getSourceLocator();
		if ( locator instanceof IAdaptable )
		{
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
	protected RunningInfo getRunningInfo()
	{
		return fRunningInfo;
	}

	public IFile getCurrentBreakpointFile()
	{
		Object info = getCurrentStateInfo();
		if ( info instanceof ICDIBreakpointHit )
		{
			ICDIBreakpoint cdiBreakpoint = ((ICDIBreakpointHit)info).getBreakpoint();
			if ( cdiBreakpoint != null )
				return getBreakpointManager().getCDIBreakpointFile( cdiBreakpoint );
		}
		return null;
	}

	protected void setRunningInfo( RunningInfo info )
	{
		fRunningInfo = info;
	}

	protected void setRunningInfo( int type )
	{
		RunningInfo info = null;
		try
		{
			CThread thread = (CThread)getCurrentThread();
			if ( thread != null )
			{
				int depth = thread.getLastStackDepth();
				if ( depth > 0 )
				{
					info = new RunningInfo( type, depth ); 
				}
			}
		}
		catch( DebugException e )
		{
		}
		setRunningInfo( info );
	}

	protected CBreakpointManager getBreakpointManager()
	{
		return fBreakpointManager;
	}

	protected void setBreakpointManager( CBreakpointManager manager )
	{
		fBreakpointManager = manager;
	}

	protected void disposeBreakpointManager()
	{
		if ( getBreakpointManager() != null )
			getBreakpointManager().dispose();
	}

	protected ICDIThread getSuspensionThread()
	{
		return fSuspensionThread;
	}

	private void setSuspensionThread()
	{
		fSuspensionThread = null;
		try
		{
			fSuspensionThread = getCDITarget().getCurrentThread();
		}
		catch( CDIException e )
		{
			// ignore
		}
	}

	protected IBreakpoint[] getThreadBreakpoints( CThread thread )
	{
		List list = new ArrayList( 1 );
		if ( isSuspended() && thread != null && 
			 getSuspensionThread() != null && 
			 getSuspensionThread().equals( thread.getCDIThread() ) )
		{
			IBreakpoint bkpt = null;
			if ( getCurrentStateInfo() instanceof ICDIBreakpointHit )
				bkpt = getBreakpointManager().getBreakpoint( ((ICDIBreakpointHit)getCurrentStateInfo()).getBreakpoint() );
			else if ( getCurrentStateInfo() instanceof ICDIWatchpointTrigger )
				bkpt = getBreakpointManager().getBreakpoint( ((ICDIWatchpointTrigger)getCurrentStateInfo()).getWatchpoint() );
			if ( bkpt != null )
				list.add( bkpt );
		}
		return (IBreakpoint[])list.toArray( new IBreakpoint[list.size()]);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		String result = ""; //$NON-NLS-1$
		try
		{
			result = getName();
		}
		catch( DebugException e )
		{
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
			return ( slm.getSharedLibraries().length > 0 );
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugTarget#hasSignals()
	 */
	public boolean hasSignals() throws DebugException {
		ICSignalManager sm = getSignalManager();
		if ( sm != null ) {
			return ( sm.getSignals().length > 0 );
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
		return ( getBreakpointManager() != null ) ? getBreakpointManager().getBreakpointAddress( breakpoint ) : 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IBreakpointTarget#isTargetBreakpoint(org.eclipse.cdt.debug.core.model.ICBreakpoint)
	 */
	public boolean isTargetBreakpoint( ICBreakpoint breakpoint ) {
		return ( getBreakpointManager() != null ) ? getBreakpointManager().isTargetBreakpoint( breakpoint ) : false;
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
		if ( fPreferences!= null )
			fPreferences.addPropertyChangeListener( listener );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ITargetProperties#removePropertyChangeListener(org.eclipse.core.runtime.Preferences.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener( IPropertyChangeListener listener ) {
		if ( fPreferences!= null )
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
}
