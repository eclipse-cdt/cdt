/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICBreakpointManager;
import org.eclipse.cdt.debug.core.ICMemoryManager;
import org.eclipse.cdt.debug.core.ICRegisterManager;
import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.cdt.debug.core.ICSignalManager;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICDIErrorInfo;
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
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
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICDebugTargetType;
import org.eclipse.cdt.debug.core.model.ICExpressionEvaluator;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.model.IDebuggerProcessSupport;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.cdt.debug.core.model.IGlobalVariable;
import org.eclipse.cdt.debug.core.model.IJumpToAddress;
import org.eclipse.cdt.debug.core.model.IJumpToLine;
import org.eclipse.cdt.debug.core.model.IRunToAddress;
import org.eclipse.cdt.debug.core.model.IRunToLine;
import org.eclipse.cdt.debug.core.model.IState;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceMode;
import org.eclipse.cdt.debug.internal.core.CDebugUtils;
import org.eclipse.cdt.debug.internal.core.CMemoryManager;
import org.eclipse.cdt.debug.internal.core.CRegisterManager;
import org.eclipse.cdt.debug.internal.core.CSharedLibraryManager;
import org.eclipse.cdt.debug.internal.core.CSignalManager;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceManager;
import org.eclipse.cdt.debug.internal.core.sourcelookup.DisassemblyManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
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
	 * Associated debugger process, or <code>null</code> if not available.
	 */
	private IProcess fDebuggerProcess = null;

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
	 * Whether terminate is supported.
	 */
	private boolean fSupportsTerminate;

	/**
	 * Whether disconnect is supported.
	 */
	private boolean fSupportsDisconnect;

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
	 * Collection of breakpoints added to this target. Values are of type <code>ICBreakpoint</code>.
	 */
	private HashMap fBreakpoints;

	/**
	 * A memory manager for this target.
	 */
	private CMemoryManager fMemoryManager;

	/**
	 * A disassembly manager for this target.
	 */
	private DisassemblyManager fDisassemblyManager;

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
	 * The suspension thread.
	 */
	private ICDIThread fSuspensionThread;	 

	/**
	 * Whether the debugger process is default.
	 */
	private boolean fIsDebuggerProcessDefault = false;

	/**
	 * The executable file.
	 */
	private IFile fExecFile;

	/**
	 * If is set to 'true' the debugger will try to set breakpoints on 
	 * the next resume or step call.
	 */
	private boolean fSetBreakpoints = true;

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
		setProcesses( debuggeeProcess, debuggerProcess );
		setCDITarget( cdiTarget );
		initializeBreakpointMap( new HashMap( 5 ) );
		setExecFile( file );
		setConfiguration( cdiTarget.getSession().getConfiguration() );
		fSupportsTerminate = allowsTerminate & getConfiguration().supportsTerminate();
		fSupportsDisconnect = allowsDisconnect & getConfiguration().supportsDisconnect();
		setThreadList( new ArrayList( 5 ) );
		setDisassemblyManager( new DisassemblyManager( this ) );
		setSharedLibraryManager( new CSharedLibraryManager( this ) );
		setSignalManager( new CSignalManager( this ) );
		setRegisterManager( new CRegisterManager( this ) );
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
	}

	/**
	 * Installs all C/C++ breakpoints that currently exist in
	 * the breakpoint manager.
	 * 
	 */
	protected void setBreakpoints()
	{
		if ( getRetryBreakpoints() )
		{
			IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
			IBreakpoint[] bps = (IBreakpoint[])manager.getBreakpoints( CDebugModel.getPluginIdentifier() );
			for ( int i = 0; i < bps.length; i++ )
			{
				if ( bps[i] instanceof ICBreakpoint && isTargetBreakpoint( bps[i] ) && findCDIBreakpoint( bps[i] ) == null ) 
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
					breakpointAdded( (ICBreakpoint)bps[i] );
				}
			}
			setRetryBreakpoints( false );
		}
	}

	private boolean isTargetBreakpoint( IBreakpoint bp )
	{
		IProject project = bp.getMarker().getResource().getProject();
		if ( project != null && project.exists() )
		{
			if ( getSourceLocator() instanceof IAdaptable )
			{
				ICSourceLocator sl = (ICSourceLocator)((IAdaptable)getSourceLocator()).getAdapter( ICSourceLocator.class );
				if ( sl != null )
				{
					return sl.contains( project );
				}
			}
			else
			{
				if ( project.equals( getExecFile().getProject() ) )
					return true;
				return CDebugUtils.isReferencedProject( getExecFile().getProject(), project );
			}
		}
		return false;
	}

	protected void initializeRegisters()
	{
		getRegisterManager().initialize();
	}

	protected void initializeMemoryManager()
	{
		fMemoryManager = new CMemoryManager( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getProcess()
	 */
	public IProcess getProcess()
	{
		return ( fIsDebuggerProcessDefault ) ? fDebuggerProcess : fDebuggeeProcess;
	}

	/**
	 * Sets the process associated with this debug target,
	 * possibly <code>null</code>. Set on creation.
	 * 
	 * @param process the system process associated with the
	 * 	underlying CDI target, or <code>null</code> if no process is
	 * 	associated with this debug target (for example, a core dump debugging).
	 */
	protected void setProcesses( IProcess debuggeeProcess, IProcess debuggerProcess )
	{
		fDebuggeeProcess = debuggeeProcess;
		fDebuggerProcess = debuggerProcess;
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
		return ( findCDIBreakpoint( breakpoint ) != null );
	}

	private boolean supportsAddressBreakpoint( ICAddressBreakpoint breakpoint )
	{
		return ( getExecFile() != null && 
				 getExecFile().getLocation().toOSString().equals( breakpoint.getMarker().getResource().getLocation().toOSString() ) );		
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
		try
		{
			setTerminating( true );
			getCDISession().terminate();
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), e );
		}
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
		if ( !isSuspended() ) 
			return;
		setBreakpoints();
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
		if ( isSuspended() )
			return;
		try
		{
			getCDITarget().suspend();
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/**
	 * Notifies threads that they have been suspended.
	 * 
	 */
	protected void suspendThreads( ICDISuspendedEvent event )
	{
		Iterator it = getThreadList().iterator();
		while( it.hasNext() )
		{
			((CThread)it.next()).handleDebugEvent( event );
		}
	}

	/**
	 * Refreshes the thread list.
	 * 
	 */
	protected synchronized List refreshThreads()
	{
		ArrayList list = new ArrayList( 5 );
		ArrayList newThreads = new ArrayList( 5 );
		try
		{
			ICDIThread[] cdiThreads = getCDITarget().getThreads();
			for ( int i = 0; i < cdiThreads.length; ++i )
			{
				CThread thread = findThread( cdiThreads[i] ); 
				if ( thread == null )
				{
					thread = new CThread( this, cdiThreads[i] );
					newThreads.add( thread );
				}
				else
				{
					getThreadList().remove( thread );
				}
				list.add( thread );
			}
			Iterator it = getThreadList().iterator();
			while( it.hasNext() )
			{
				((CThread)it.next()).terminated();
			}
			getThreadList().clear();
			setThreadList( list );
			it = newThreads.iterator();
			while( it.hasNext() )
			{
				((CThread)it.next()).fireCreationEvent();
			}
		}
		catch( CDIException e )
		{
			CDebugCorePlugin.log( e );
		}
		setCurrentThread();
		return newThreads;
	}

	/**
	 * Notifies threads that they have been resumed
	 */
	protected void resumeThreads( ICDIResumedEvent event )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(IBreakpoint)
	 */
	public void breakpointAdded( IBreakpoint breakpoint )
	{
		if ( !isAvailable() )
		{
			return;
		}
		if ( getConfiguration().supportsBreakpoints() )
		{
			try
			{
				if ( breakpoint instanceof ICFunctionBreakpoint )
				{
					setFunctionBreakpoint( (ICFunctionBreakpoint)breakpoint );
				}
				else if ( breakpoint instanceof ICAddressBreakpoint )
				{
					if ( supportsAddressBreakpoint( (ICAddressBreakpoint)breakpoint ) )
						setAddressBreakpoint( (ICAddressBreakpoint)breakpoint );
				}
				else if ( breakpoint instanceof ICLineBreakpoint )
				{
					setLineBreakpoint( (ICLineBreakpoint)breakpoint );
				}
				else if ( breakpoint instanceof ICWatchpoint )
				{
					setWatchpoint( (ICWatchpoint)breakpoint );
				}
			}
			catch( DebugException e )
			{
//				CDebugCorePlugin.log( e );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointRemoved( IBreakpoint breakpoint, IMarkerDelta delta )
	{
		try
		{
			if ( breakpoint instanceof CBreakpoint )
				removeBreakpoint( (CBreakpoint)breakpoint );
		}
		catch( DebugException e )
		{
			CDebugCorePlugin.log( e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointChanged( IBreakpoint breakpoint, IMarkerDelta delta )
	{
		try
		{
			if ( breakpoint instanceof CBreakpoint )
				changeBreakpointProperties( (CBreakpoint)breakpoint, delta );
		}
		catch( DebugException e )
		{
			CDebugCorePlugin.log( e );
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
		return supportsDisconnect() && !isDisconnected() && isSuspended();
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
			notSupported( "Session does not support \'disconnect\'" );
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
		if ( adapter.equals( ISourceMode.class ) )
		{
			if ( getSourceLocator() instanceof IAdaptable )
			{
				return ((IAdaptable)getSourceLocator()).getAdapter( ISourceMode.class );
			}
		}
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
		if ( adapter.equals( ICBreakpointManager.class ) )
			return this;
		if ( adapter.equals( DisassemblyManager.class ) )
			return getDisassemblyManager();
		if ( adapter.equals( ICSharedLibraryManager.class ) )
			return getSharedLibraryManager();
		if ( adapter.equals( ICSignalManager.class ) )
			return getSignalManager();
		if ( adapter.equals( ICRegisterManager.class ) )
			return getRegisterManager();
		return super.getAdapter( adapter );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvent(ICDIEvent)
	 */
	public void handleDebugEvent( ICDIEvent event )
	{
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
					if ( ((ICDISharedLibrary)source).areSymbolsLoaded() )
						setRetryBreakpoints( true );
				}
				if ( source instanceof ICDILocationBreakpoint )
				{
					handleLocationBreakpointCreatedEvent( (ICDILocationBreakpoint)source );
				}
				if ( source instanceof ICDIWatchpoint )
				{
					handleWatchpointCreatedEvent( (ICDIWatchpoint)source );
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
				if ( source instanceof ICDIBreakpoint )
				{
					handleBreakpointDestroyedEvent( (ICDIBreakpoint)source );
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
				if ( source instanceof ICDIBreakpoint )
				{
					handleBreakpointChangedEvent( (ICDIBreakpoint)source );
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
			ICDILocation location = getCDISession().getBreakpointManager().createLocation( "", "main", 0 );
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
			try
			{
				getCDISession().terminate();
			}
			catch( CDIException e )
			{
				logError( e );
			}
			fireChangeEvent( DebugEvent.STATE );
/*
			cleanup();
			fireTerminateEvent();
*/
		}
	}

	/** 
	 * Cleans up the internal state of this debug target as a result 
	 * of a session ending.
	 * 
	 */
	protected void cleanup()
	{
		removeAllThreads();
		getCDISession().getEventManager().removeEventListener( this );
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener( this );
		DebugPlugin.getDefault().getExpressionManager().removeExpressionListener( this );
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener( this );
		disposeMemoryManager();
		disposeSharedLibraryManager();
		disposeSignalManager();
		disposeRegisterManager();
		removeAllExpressions();
		try
		{
			removeAllBreakpoints();
		}
		catch( DebugException e )
		{
			CDebugCorePlugin.log( e );
		}
	}
	
	/**
	 * Removes all threads from this target's collection
	 * of threads, firing a terminate event for each.
	 * 
	 */
	protected void removeAllThreads()
	{
		Iterator itr = getThreadList().iterator();
		setThreadList( new ArrayList( 0 ) );
		while( itr.hasNext() )
		{
			CThread thread = (CThread)itr.next();
			thread.terminated();
		}
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
	 * Removes all breakpoints from this target.
	 * 
	 */
	protected void removeAllBreakpoints() throws DebugException
	{
		ICDIBreakpoint[] cdiBreakpoints = (ICDIBreakpoint[])getBreakpoints().values().toArray( new ICDIBreakpoint[0] );
		ICDIBreakpointManager bm = getCDISession().getBreakpointManager();
		if ( cdiBreakpoints.length > 0 )
		{
			try
			{
				bm.deleteBreakpoints( cdiBreakpoints );
			}
			catch( CDIException e )
			{
				logError( e );
			}
			try
			{
				Iterator it = getBreakpoints().keySet().iterator();
				while( it.hasNext() )
				{
					((CBreakpoint)it.next()).decrementInstallCount();
				}
				getBreakpoints().clear();
			}
			catch( CoreException ce )
			{
				logError( ce );
			}
		}
	}

	protected void removeBreakpoint( CBreakpoint breakpoint ) throws DebugException
	{
		ICDIBreakpoint cdiBreakpoint = findCDIBreakpoint( breakpoint );
		if ( cdiBreakpoint == null )
			return;
		ICDIBreakpointManager bm = getCDISession().getBreakpointManager();
		try
		{
			bm.deleteBreakpoints( new ICDIBreakpoint[] { cdiBreakpoint } );
			getBreakpoints().remove( breakpoint );
			breakpoint.decrementInstallCount();
		}
		catch( CoreException ce )
		{
			requestFailed( "Operation failed. Reason: ", ce );
		}
		catch( CDIException e )
		{
			requestFailed( "Operation failed. Reason: ", e );
		}
	}

	protected void changeBreakpointProperties( CBreakpoint breakpoint, IMarkerDelta delta ) throws DebugException
	{
		ICDIBreakpoint cdiBreakpoint = findCDIBreakpoint( breakpoint );
		if ( cdiBreakpoint == null )
			return;
		ICDIBreakpointManager bm = getCDISession().getBreakpointManager();
		try
		{
			boolean enabled = breakpoint.isEnabled();
			boolean oldEnabled = delta.getAttribute( IBreakpoint.ENABLED, true );
			int ignoreCount = breakpoint.getIgnoreCount();
			int oldIgnoreCount = delta.getAttribute( ICBreakpoint.IGNORE_COUNT, 0 );
			String condition = breakpoint.getCondition();
			String oldCondition = delta.getAttribute( ICBreakpoint.CONDITION, "" );
			if ( enabled != oldEnabled )
			{
				cdiBreakpoint.setEnabled( enabled );
			}
			if ( ignoreCount != oldIgnoreCount || !condition.equals( oldCondition ) )
			{
				ICDICondition cdiCondition = bm.createCondition( ignoreCount, condition );
				cdiBreakpoint.setCondition( cdiCondition );
			}
		}
		catch( CoreException ce )
		{
			requestFailed( "Operation failed. Reason: ", ce );
		}
		catch( CDIException e )
		{
			requestFailed( "Operation failed. Reason: ", e );
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
		thread.fireCreationEvent();
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
		thread.fireCreationEvent();
		return thread;
	}

	private void handleSuspendedEvent( ICDISuspendedEvent event )
	{
		setSuspended( true );
		setCurrentStateId( IState.SUSPENDED );
		ICDISessionObject reason = event.getReason();
		setCurrentStateInfo( reason );
		setSuspensionThread();
		List newThreads = refreshThreads();
		if ( event.getSource() instanceof ICDITarget )
		{
			suspendThreads( event );
			fireSuspendEvent( DebugEvent.UNSPECIFIED );
		}
		// We need this for debuggers that don't have notifications 
		// for newly created threads.
		else if ( event.getSource() instanceof ICDIThread )
		{
			CThread thread = findThread( (ICDIThread)event.getSource() );
			if ( thread != null && newThreads.contains( thread ) )
			{
				thread.handleDebugEvent( event );
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
	}

	private void handleResumedEvent( ICDIResumedEvent event )
	{
		setSuspended( false );
		setCurrentStateId( IState.RUNNING );
		setCurrentStateInfo( null );
		resumeThreads( event );
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
		fireResumeEvent( detail );
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
		CBreakpoint watchpoint = (CBreakpoint)findBreakpoint( ws.getWatchpoint() );
		try
		{
			removeBreakpoint( watchpoint );
		}
		catch( DebugException e )
		{
			CDebugCorePlugin.log( e );
		}
		fireSuspendEvent( DebugEvent.BREAKPOINT );
	}
	
	private void handleSuspendedBySignal( ICDISignalReceived signal )
	{
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleErrorInfo( ICDIErrorInfo info )
	{
		if ( info != null )
		{
			MultiStatus status = new MultiStatus( CDebugCorePlugin.getUniqueIdentifier(),
												  ICDebugInternalConstants.STATUS_CODE_ERROR,
												  "The execution of program is suspended because of error.",
												  null );
			StringTokenizer st = new StringTokenizer( info.getDetailMessage(), "\n\r" );
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
	}

	private void handleExitedEvent( ICDIExitedEvent event )
	{
		removeAllThreads();
		setCurrentStateId( IState.EXITED );
		setCurrentStateInfo( event.getReason() );
		fireChangeEvent( DebugEvent.CONTENT );
		try
		{
			terminate();
		}
		catch( DebugException e )
		{
			CDebugCorePlugin.log( e.getStatus() );
		}
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
			createThread( cdiThread );
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
		}
	}

	private void handleLocationBreakpointCreatedEvent( final ICDILocationBreakpoint breakpoint )
	{
		Runnable runnable = new Runnable()
								{
									public void run()
									{
										doHandleLocationBreakpointCreatedEvent( breakpoint );
									}
								};
		CDebugCorePlugin.getDefault().asyncExec( runnable );
	}

	protected void doHandleLocationBreakpointCreatedEvent( final ICDILocationBreakpoint cdiBreakpoint )
	{
		if ( cdiBreakpoint.isTemporary() || getBreakpoints().containsValue( cdiBreakpoint ) )
			return;
		try
		{
			if ( cdiBreakpoint.getLocation().getFile() != null && cdiBreakpoint.getLocation().getFile().length() > 0 )
			{
				if ( getSourceLocator() instanceof IAdaptable && ((IAdaptable)getSourceLocator()).getAdapter( ICSourceLocator.class ) != null )
				{
					Object sourceElement = ((ICSourceLocator)((IAdaptable)getSourceLocator()).getAdapter( ICSourceLocator.class )).findSourceElement( cdiBreakpoint.getLocation().getFile() );
					if ( sourceElement != null && sourceElement instanceof IFile )
					{
						createLineBreakpoint( (IFile)sourceElement, cdiBreakpoint );
					}
					else if ( cdiBreakpoint.getLocation().getAddress() > 0 )
					{
						createAddressBreakpoint( cdiBreakpoint );
					}
				}
			}
			else if ( cdiBreakpoint.getLocation().getAddress() > 0 )
			{
				createAddressBreakpoint( cdiBreakpoint );
			}
		}
		catch( CDIException e )
		{
		}
		catch( CoreException e )
		{
		}
	}

	private void createLineBreakpoint( IFile file, ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException
	{
		ICLineBreakpoint breakpoint = CDebugModel.createLineBreakpoint( file,
																		cdiBreakpoint.getLocation().getLineNumber(),
																		cdiBreakpoint.isEnabled(),
																		cdiBreakpoint.getCondition().getIgnoreCount(),
																		cdiBreakpoint.getCondition().getExpression(),
																		false );
		getBreakpoints().put( breakpoint, cdiBreakpoint );
		((CBreakpoint)breakpoint).register( true );
	}

	private void createAddressBreakpoint( ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException
	{
		ICAddressBreakpoint breakpoint = CDebugModel.createAddressBreakpoint( getExecFile(),
																			  cdiBreakpoint.getLocation().getAddress(),
																			  cdiBreakpoint.isEnabled(),
																			  cdiBreakpoint.getCondition().getIgnoreCount(),
																			  cdiBreakpoint.getCondition().getExpression(),
																			  false );
		getBreakpoints().put( breakpoint, cdiBreakpoint );
		((CBreakpoint)breakpoint).register( true );
	}

	private void handleWatchpointCreatedEvent( final ICDIWatchpoint watchpoint )
	{
		Runnable runnable = new Runnable()
								{
									public void run()
									{
										doHandleWatchpointCreatedEvent( watchpoint );
									}
 
								};
		CDebugCorePlugin.getDefault().asyncExec( runnable );
	}

	protected void doHandleWatchpointCreatedEvent( ICDIWatchpoint cdiWatchpoint )
	{
		if ( getBreakpoints().containsValue( cdiWatchpoint ) )
			return;
		try
		{
			ICWatchpoint watchpoint = CDebugModel.createWatchpoint( getExecFile().getProject(),
																	cdiWatchpoint.isWriteType(),
																	cdiWatchpoint.isReadType(),
																	cdiWatchpoint.getWatchExpression(),
																	cdiWatchpoint.isEnabled(),
														  			cdiWatchpoint.getCondition().getIgnoreCount(),
																	cdiWatchpoint.getCondition().getExpression(),
																	false );
			getBreakpoints().put( watchpoint, cdiWatchpoint );
			((CBreakpoint)watchpoint).register( true );
		}
		catch( CDIException e )
		{
		}
		catch( CoreException e )
		{
		}
	}

	private void handleBreakpointDestroyedEvent( final ICDIBreakpoint breakpoint )
	{
		Runnable runnable = new Runnable()
								{
									public void run()
									{
										doHandleBreakpointDestroyedEvent( breakpoint );
									}
 
								};
		CDebugCorePlugin.getDefault().asyncExec( runnable );
	}

	protected void doHandleBreakpointDestroyedEvent( ICDIBreakpoint cdiBreakpoint )
	{
		IBreakpoint breakpoint = findBreakpoint( cdiBreakpoint );
		if ( breakpoint != null )
		{
			try
			{
				DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
			}
			catch( CoreException e )
			{
			}
		}
	}

	private void handleBreakpointChangedEvent( final ICDIBreakpoint breakpoint )
	{
		Runnable runnable = new Runnable()
								{
									public void run()
									{
										doHandleBreakpointChangedEvent( breakpoint );
									}
 
								};
		CDebugCorePlugin.getDefault().asyncExec( runnable );
	}

	protected void doHandleBreakpointChangedEvent( ICDIBreakpoint cdiBreakpoint )
	{
		IBreakpoint breakpoint = findBreakpoint( cdiBreakpoint );
		if ( breakpoint instanceof ICBreakpoint )
		{
			try
			{
				Map attributes = breakpoint.getMarker().getAttributes();
				attributes.put( ICBreakpoint.ENABLED, new Boolean( cdiBreakpoint.isEnabled() ) );
				attributes.put( ICBreakpoint.IGNORE_COUNT, new Integer( cdiBreakpoint.getCondition().getIgnoreCount() ) );
				attributes.put( ICBreakpoint.CONDITION, cdiBreakpoint.getCondition().getExpression() );
				breakpoint.getMarker().setAttributes( attributes );
			}
			catch( CDIException e )
			{
			}
			catch( CoreException e )
			{
			}
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
	 * Returns the map of breakpoints installed in this debug target.
	 * 
	 * @return map of installed breakpoints
	 */
	protected HashMap getBreakpoints()
	{
		return fBreakpoints;
	}

	/**
	 * Sets the map of breakpoints installed in this debug target. 
	 * 
	 * @param breakpoints breakpoints map
	 */
	private void initializeBreakpointMap( HashMap breakpoints )
	{
		fBreakpoints = breakpoints;
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

	private void setLineBreakpoint( ICLineBreakpoint breakpoint ) throws DebugException
	{
		try
		{
			ICDIBreakpoint cdiBreakpoint = (ICDIBreakpoint)getBreakpoints().get( breakpoint );
			if ( cdiBreakpoint == null )
			{
				cdiBreakpoint = setLineBreakpoint0( breakpoint );
			}
			((CBreakpoint)breakpoint).incrementInstallCount();
			if ( !breakpoint.isEnabled() )
			{
				cdiBreakpoint.setEnabled( false );
			}
		}
		catch( CoreException ce )
		{
			requestFailed( "Operation failed. Reason: ", ce );
		}
		catch( CDIException e )
		{
			requestFailed( "Operation failed. Reason: ", e );
		}
	}

	private synchronized ICDIBreakpoint setLineBreakpoint0( ICLineBreakpoint breakpoint ) throws CDIException, CoreException
	{
		ICDIBreakpointManager bm = getCDISession().getBreakpointManager();
		ICDILocation location = bm.createLocation( breakpoint.getMarker().getResource().getLocation().lastSegment(), null, breakpoint.getLineNumber() );
		ICDICondition condition = bm.createCondition( breakpoint.getIgnoreCount(), breakpoint.getCondition() );
		ICDIBreakpoint cdiBreakpoint = bm.setLocationBreakpoint( ICDIBreakpoint.REGULAR, location, condition, null );
		getBreakpoints().put( breakpoint, cdiBreakpoint );
		return cdiBreakpoint;
	}
	
	private void setAddressBreakpoint( ICAddressBreakpoint breakpoint ) throws DebugException
	{
		try
		{
			ICDIBreakpoint cdiBreakpoint = (ICDIBreakpoint)getBreakpoints().get( breakpoint );
			if ( cdiBreakpoint == null )
			{
				cdiBreakpoint = setAddressBreakpoint0( breakpoint );
			}
			((CBreakpoint)breakpoint).incrementInstallCount();
			if ( !breakpoint.isEnabled() )
			{
				cdiBreakpoint.setEnabled( false );
			}
		}
		catch( CoreException ce )
		{
			requestFailed( "Operation failed. Reason: ", ce );
		}
		catch( CDIException e )
		{
			requestFailed( "Operation failed. Reason: ", e );
		}
		catch( NumberFormatException e )
		{
			requestFailed( "Operation failed. Reason: ", e );
		}
	}
 	
	private synchronized ICDIBreakpoint setAddressBreakpoint0( ICAddressBreakpoint breakpoint ) throws CDIException, CoreException
	{
		ICDIBreakpointManager bm = getCDISession().getBreakpointManager();
		ICDILocation location = bm.createLocation( Long.parseLong( breakpoint.getAddress() ) );
		ICDICondition condition = bm.createCondition( breakpoint.getIgnoreCount(), breakpoint.getCondition() );
		ICDIBreakpoint cdiBreakpoint = bm.setLocationBreakpoint( ICDIBreakpoint.REGULAR, location, condition, null );
		getBreakpoints().put( breakpoint, cdiBreakpoint );
		return cdiBreakpoint;
	}
	
	private void setFunctionBreakpoint( ICFunctionBreakpoint breakpoint ) throws DebugException
	{
		try
		{
			ICDIBreakpoint cdiBreakpoint = (ICDIBreakpoint)getBreakpoints().get( breakpoint );
			if ( cdiBreakpoint == null )
			{
				cdiBreakpoint = setFunctionBreakpoint0( breakpoint );
			}
			((CBreakpoint)breakpoint).incrementInstallCount();
			if ( !breakpoint.isEnabled() )
			{
				cdiBreakpoint.setEnabled( false );
			}
		}
		catch( CoreException ce )
		{
			requestFailed( "Operation failed. Reason: ", ce );
		}
		catch( CDIException e )
		{
			requestFailed( "Operation failed. Reason: ", e );
		}
		catch( NumberFormatException e )
		{
			requestFailed( "Operation failed. Reason: ", e );
		}
	}
 	
	private synchronized ICDIBreakpoint setFunctionBreakpoint0( ICFunctionBreakpoint breakpoint ) throws CDIException, CoreException
	{
		ICDIBreakpointManager bm = getCDISession().getBreakpointManager();
		String function = breakpoint.getFunction();
		String fileName = ( function != null && function.indexOf( "::" ) == -1  ) ? breakpoint.getFileName() : null;
		ICDILocation location = bm.createLocation( fileName, function, -1 );
		ICDICondition condition = bm.createCondition( breakpoint.getIgnoreCount(), breakpoint.getCondition() );
		ICDIBreakpoint cdiBreakpoint = bm.setLocationBreakpoint( ICDIBreakpoint.REGULAR, location, condition, null );
		getBreakpoints().put( breakpoint, cdiBreakpoint );
		return cdiBreakpoint;
	}
	
	private void setWatchpoint( ICWatchpoint watchpoint ) throws DebugException
	{
		try
		{
			ICDIWatchpoint cdiWatchpoint = (ICDIWatchpoint)getBreakpoints().get( watchpoint );
			if ( cdiWatchpoint == null )
			{
				cdiWatchpoint = setWatchpoint0( watchpoint );
			}
			((CBreakpoint)watchpoint).incrementInstallCount();
			if ( !watchpoint.isEnabled() )
			{
				cdiWatchpoint.setEnabled( false );
			}
		}
		catch( CoreException ce )
		{
			requestFailed( "Operation failed. Reason: ", ce );
		}
		catch( CDIException e )
		{
			requestFailed( "Operation failed. Reason: ", e );
		}
	}
	
	private synchronized ICDIWatchpoint setWatchpoint0( ICWatchpoint watchpoint ) throws CDIException, CoreException
	{
		ICDIBreakpointManager bm = getCDISession().getBreakpointManager();
		ICDICondition condition = bm.createCondition( watchpoint.getIgnoreCount(), watchpoint.getCondition() );
		int accessType = 0;
		accessType |= ( watchpoint.isWriteType() ) ? ICDIWatchpoint.WRITE : 0;
		accessType |= ( watchpoint.isReadType() ) ? ICDIWatchpoint.READ : 0;
		String expression = watchpoint.getExpression();
		ICDIWatchpoint cdiWatchpoint = bm.setWatchpoint( ICDIBreakpoint.REGULAR, accessType, expression, condition );
		getBreakpoints().put( watchpoint, cdiWatchpoint );
		return cdiWatchpoint;
	}
	
	private ICDIBreakpoint findCDIBreakpoint( IBreakpoint breakpoint )
	{
		return (ICDIBreakpoint)getBreakpoints().get( breakpoint );
	}
	
	private IBreakpoint findBreakpoint( ICDIBreakpoint cdiBreakpoint )
	{
		if ( cdiBreakpoint == null )
			return null;
		Iterator it = getBreakpoints().keySet().iterator();
		while( it.hasNext() )
		{
			IBreakpoint breakpoint = (IBreakpoint)it.next();
			if ( cdiBreakpoint.equals( getBreakpoints().get( breakpoint ) ) )
				return breakpoint;
		}
		return null;
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
		return isAvailable() && isSuspended();
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
	public void runToLine( String fileName, int lineNumber ) throws DebugException
	{
		if ( !canRunToLine( fileName, lineNumber ) )
			return;
		setBreakpoints();
		ICDILocation location = getCDISession().getBreakpointManager().createLocation( fileName, null, lineNumber );
		try
		{
			getCDITarget().runUntil( location );
		}
		catch( CDIException e )
		{
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
	public void runToLine( IFile file, int lineNumber ) throws DebugException
	{
		if ( !canRunToLine( file, lineNumber ) )
			return;
		runToLine( file.getLocation().lastSegment(), lineNumber );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ISwitchToThread#setCurrentThread(IThread)
	 */
	public void setCurrentThread( IThread thread ) throws DebugException
	{
		if ( !isSuspended() || !isAvailable() || thread == null || !(thread instanceof CThread) )
		{
			return;
		}
		try
		{
			CThread oldThread = (CThread)getCurrentThread();
			if ( !oldThread.equals( thread ) )
			{
				oldThread.setCurrent( false );
				getCDITarget().setCurrentThread( ((CThread)thread).getCDIThread() );
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
	
	protected int getRealSourceMode()
	{
		ISourceLocator sl = getSourceLocator();
		if ( sl != null && 
			 sl instanceof IAdaptable && 
			 ((IAdaptable)sl).getAdapter( ICSourceLocator.class ) != null &&
			 ((IAdaptable)sl).getAdapter( ICSourceLocator.class ) instanceof CSourceManager )
		{
			return ((CSourceManager)((IAdaptable)sl).getAdapter( ICSourceLocator.class )).getRealMode();
		}
		return ISourceMode.MODE_SOURCE;
	}
	
	protected CMemoryManager getMemoryManager()
	{
		return fMemoryManager;
	}
	
	private void disposeMemoryManager()
	{
		getMemoryManager().dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IDebuggerProcessSupport#isDefault()
	 */
	public boolean isDebuggerProcessDefault()
	{
		return fIsDebuggerProcessDefault;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IDebuggerProcessSupport#setDefault(boolean)
	 */
	public void setDebuggerProcessDefault( boolean value )
	{
		fIsDebuggerProcessDefault = value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IDebuggerProcessSupport#supportsDebuggerProcess()
	 */
	public boolean supportsDebuggerProcess()
	{
		return ( fDebuggerProcess != null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IExecFileInfo#isLittleEndian()
	 */
	public boolean isLittleEndian()
	{
		if ( getExecFile() != null && CoreModel.getDefault().isBinary( getExecFile() ) )
		{
			ICElement cFile = CCorePlugin.getDefault().getCoreModel().create( getExecFile() );
			if ( cFile instanceof IBinary )
			{
				((IBinary)cFile).isLittleEndian();
			}
		}
		return true;
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
	public IGlobalVariable[] getGlobals()
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
		return (IGlobalVariable[])list.toArray( new IGlobalVariable[list.size()] );
	}

	private List getCFileGlobals( IParent file )
	{
		ArrayList list = new ArrayList();
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
		return list;
	}

	private IGlobalVariable createGlobalVariable( final org.eclipse.cdt.core.model.IVariable var )
	{
		return new IGlobalVariable()
				  {
				  	  public String getName()
				  	  {
				  	  	  return var.getElementName();
				  	  }
				  	  
				  	  public IPath getPath()
				  	  {
							IPath path = new Path("");
							try
							{
								IResource res = var.getUnderlyingResource();
								if ( res != null )
								{
									path = res.getLocation();
								}
							}
							catch (CModelException e)
							{
							}
						  return path;
				  	  }
				  };
	}
	
	protected void setDisassemblyManager( DisassemblyManager dm )
	{
		fDisassemblyManager = dm;
	}
	
	protected DisassemblyManager getDisassemblyManager()
	{
		return fDisassemblyManager;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpointManager#getBreakpointAddress(IBreakpoint)
	 */
	public long getBreakpointAddress( IBreakpoint breakpoint )
	{
		ICDIBreakpoint cdiBreakpoint = findCDIBreakpoint( breakpoint );
		if ( cdiBreakpoint != null && cdiBreakpoint instanceof ICDILocationBreakpoint )
		{
			try
			{
				ICDILocation location = ((ICDILocationBreakpoint)cdiBreakpoint).getLocation();
				if ( location != null )
				{
					return location.getAddress();
				}
			}
			catch( CDIException e )
			{
			}
		}
		return 0;
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
	public void runToAddress( long address ) throws DebugException
	{
		if ( !canRunToAddress( address ) )
			return;
		setBreakpoints();
		ICDILocation location = getCDISession().getBreakpointManager().createLocation( address );
		try
		{
			getCDITarget().runUntil( location );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), e );
		}
	}
	
	private boolean getRetryBreakpoints()
	{
		return fSetBreakpoints;
	}

	public void setRetryBreakpoints( boolean retry )
	{
		fSetBreakpoints = retry;
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
		setBreakpoints();
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
		setBreakpoints();
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
				bkpt = findBreakpoint( ((ICDIBreakpointHit)getCurrentStateInfo()).getBreakpoint() );
			else if ( getCurrentStateInfo() instanceof ICDIWatchpointTrigger )
				bkpt = findBreakpoint( ((ICDIWatchpointTrigger)getCurrentStateInfo()).getWatchpoint() );
			if ( bkpt != null )
				list.add( bkpt );
		}
		return (IBreakpoint[])list.toArray( new IBreakpoint[list.size()]);
	}
}
