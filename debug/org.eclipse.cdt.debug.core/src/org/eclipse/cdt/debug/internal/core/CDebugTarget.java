/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.core.IFormattedMemoryRetrieval;
import org.eclipse.cdt.debug.core.IRestart;
import org.eclipse.cdt.debug.core.IState;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDisconnectedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDILoadedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIRestartedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDISteppingEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 1, 2002
 */
public class CDebugTarget extends CDebugElement
						  implements IDebugTarget,
						  			 ICDIEventListener,
						  			 IRestart,
						  			 IFormattedMemoryRetrieval,
						  			 IState,
						  			 ILaunchListener
{
	/**
	 * Threads contained in this debug target. When a thread
	 * starts it is added to the list. When a thread ends it
	 * is removed from the list.
	 */
	private ArrayList fThreads;

	/**
	 * Associated system process, or <code>null</code> if not available.
	 */
	private IProcess fProcess;

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
	 * Whether the target should be resumed on startup
	 */
	private boolean fResumeOnStartup = false; 
	
	/**
	 * The launch this target is contained in
	 */
	private ILaunch fLaunch;	

	/**
	 * The debug configuration of this session
	 */
	private ICDIDebugConfiguration fConfig;	

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
	 * Constructor for CDebugTarget.
	 * @param target
	 */
	public CDebugTarget( ILaunch launch, 
						 ICDITarget cdiTarget, 
						 String name,
						 IProcess process,
						 boolean allowsTerminate,
						 boolean allowsDisconnect )
	{
		super( null );
		setLaunch( launch );
		setDebugTarget( this );
		setName( name );
		setProcess( process );
		setCDITarget( cdiTarget );
		fConfig = cdiTarget.getSession().getConfiguration();
		fSupportsTerminate = allowsTerminate & fConfig.supportsTerminate();
		fSupportsDisconnect = allowsDisconnect & fConfig.supportsDisconnect();
		setThreadList( new ArrayList( 5 ) );
		initialize();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener( this );
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
		getLaunch().addDebugTarget( this );
		fireCreationEvent();
	}

	/**
	 * Adds all of the pre-existing threads to this debug target.  
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
			internalError( e );
		}
		for ( int i = 0; i < threads.length; ++i )
			createRunningThread( threads[i] );

		if ( isResumeOnStartup() )
		{
			setSuspended( false );
		}
	}

	/**
	 * Installs all C/C++ breakpoints that currently exist in
	 * the breakpoint manager.
	 * 
	 */
	protected void initializeBreakpoints()
	{
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		manager.addBreakpointListener( this );
		IBreakpoint[] bps = (IBreakpoint[])manager.getBreakpoints( CDebugModel.getPluginIdentifier() );
		for ( int i = 0; i < bps.length; i++ )
		{
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getProcess()
	 */
	public IProcess getProcess()
	{
		return fProcess;
	}

	/**
	 * Sets the process associated with this debug target,
	 * possibly <code>null</code>. Set on creation.
	 * 
	 * @param process the system process associated with the
	 * 	underlying CDI target, or <code>null</code> if no process is
	 * 	associated with this debug target (for example, a core dump debugging).
	 */
	protected void setProcess( IProcess process )
	{
		fProcess = process;
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
		return false;
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
		return isSuspended() && isAvailable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend()
	{
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
		{
			return;
		}
		try 
		{
			setSuspended( false );
			getCDITarget().resume();
			resumeThreads();
		} 
		catch( CDIException e ) 
		{
			setSuspended( true );
			targetRequestFailed( MessageFormat.format( "{0} occurred resuming target.", new String[] { e.toString() } ), e );
		}	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException
	{
		if ( isSuspended() )
		{
			return;
		}
		try
		{
			setSuspended( true );
			getCDITarget().suspend();
			suspendThreads();
		}
		catch( CDIException e )
		{
			setSuspended( false );
			targetRequestFailed( MessageFormat.format( "{0} occurred suspending target.", new String[] { e.toString()} ), e );
		}
	}

	/**
	 * Notifies threads that they have been suspended.
	 * 
	 */
	protected void suspendThreads()
	{
	}

	/**
	 * Notifies threads that they have been resumed
	 */
	protected void resumeThreads()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(IBreakpoint)
	 */
	public void breakpointAdded( IBreakpoint breakpoint )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointRemoved( IBreakpoint breakpoint, IMarkerDelta delta )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointChanged( IBreakpoint breakpoint, IMarkerDelta delta )
	{
	}

	/**
	 * Returns whether this debug target supports disconnecting.
	 * 
	 * @return whether this debug target supports disconnecting
	 */
	protected boolean supportsDisconnect()
	{
		return fConfig.supportsDisconnect();
	}
	
	/**
	 * Returns whether this debug target supports termination.
	 * 
	 * @return whether this debug target supports termination
	 */
	protected boolean supportsTerminate()
	{
		return fConfig.supportsTerminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#canDisconnect()
	 */
	public boolean canDisconnect()
	{
		return supportsDisconnect() && !isDisconnected();
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
			disconnected();
		}
		catch( CDIException e )
		{
			targetRequestFailed( MessageFormat.format( "{0} ocurred disconnecting from target.", new String[] { e.toString()} ), e );
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
		if ( adapter.equals( ICDITarget.class ) )
			return fCDITarget;
		if ( adapter.equals( IState.class ) )
			return this;
		return super.getAdapter( adapter );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvent(ICDIEvent)
	 */
	public void handleDebugEvent( ICDIEvent event )
	{
		ICDIObject source = event.getSource();
		if ( source.getTarget().equals( this ) )
		{
			if ( event instanceof ICDICreatedEvent )
			{
				if ( source instanceof ICDIThread )
				{
					handleThreadCreatedEvent( (ICDICreatedEvent)event );
				}
			}
			else if ( event instanceof ICDISuspendedEvent )
			{
				if ( source instanceof ICDITarget )
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
				if ( source instanceof ICDITarget )
				{
					handleTerminatedEvent( (ICDIDestroyedEvent)event );
				}
				else if ( source instanceof ICDIThread )
				{
					handleThreadTerminatedEvent( (ICDIDestroyedEvent)event );
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
			}
			else if ( event instanceof ICDILoadedEvent )
			{
				if ( source instanceof ICDITarget )
				{
					handleLoadedEvent( (ICDILoadedEvent)event );
				}
			}
			else if ( event instanceof ICDIRestartedEvent )
			{
				if ( source instanceof ICDITarget )
				{
					handleRestartedEvent( (ICDIRestartedEvent)event );
				}
			}
			else if ( event instanceof ICDISteppingEvent )
			{
				if ( source instanceof ICDITarget )
				{
					handleSteppingEvent( (ICDISteppingEvent)event );
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IRestart#canRestart()
	 */
	public boolean canRestart()
	{
		return fConfig.supportsRestart() && isSuspended() && isAvailable();
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
			getCDITarget().restart();
			restarted();
		}
		catch( CDIException e )
		{
			targetRequestFailed( MessageFormat.format( "{0} ocurred restarting the target.", new String[] { e.toString()} ), e );
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
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryRetrieval#getFormattedMemoryBlock(long, int, int, int, int, char)
	 */
	public IFormattedMemoryBlock getFormattedMemoryBlock( long startAddress,
														  int format,
														  int wordSize,
														  int numberOfRows,
														  int numberOfColumns,
														  char paddingChar ) throws DebugException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryRetrieval#getFormattedMemoryBlock(long, int, int, int, int)
	 */
	public IFormattedMemoryBlock getFormattedMemoryBlock( long startAddress,
														  int format,
														  int wordSize,
														  int numberOfRows,
														  int numberOfColumns ) throws DebugException
	{
		return null;
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
			setTerminated( true );
			setDisconnected( true );
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
		removeAllThreads();
		getCDISession().getEventManager().removeEventListener( this );
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener( this );
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener( this );
		removeAllBreakpoints();
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
	 * Removes all breakpoints from this target.
	 * 
	 */
	protected void removeAllBreakpoints() 
	{
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
		CThread thread = null;
		thread = new CThread( this, cdiThread );
		if ( isDisconnected() )
		{
			return null;
		}
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
		CThread thread = null;
		thread = new CThread( this, cdiThread );
		if ( isDisconnected() )
		{
			return null;
		}
		thread.setRunning( true );
		getThreadList().add( thread );
		thread.fireCreationEvent();
		return thread;
	}

	/**
	 * Sets whether this target should be resumed on startup.
	 * 
	 * @param resume whether this target should be resumed on startup
	 */
	private void setResumeOnStartup( boolean resume )
	{
		fResumeOnStartup = resume;
	}
	
	/**
	 * Returns whether this target should be resumed on startup.
	 * 
	 * @return whether this target should be resumed on startup
	 */
	protected boolean isResumeOnStartup()
	{
		return fResumeOnStartup;
	}

	private void handleSuspendedEvent( ICDISuspendedEvent event )
	{
		setSuspended( true );
		setCurrentStateId( IState.SUSPENDED );
		ICDISessionObject reason = event.getReason();
		setCurrentStateInfo( reason );
		if ( reason instanceof ICDIEndSteppingRange )
		{
			handleEndSteppingRange( (ICDIEndSteppingRange)reason );
		}
		else if ( reason instanceof ICDIBreakpoint )
		{
			handleBreakpointHit( (ICDIBreakpoint)reason );
		}
		else if ( reason instanceof ICDISignal )
		{
			handleSuspendedBySignal( (ICDISignal)reason );
		}
	}

	private void handleResumedEvent( ICDIResumedEvent event )
	{
		setSuspended( false );
		setCurrentStateId( IState.RUNNING );
		setCurrentStateInfo( null );
		fireResumeEvent( DebugEvent.UNSPECIFIED );
	}
	
	private void handleEndSteppingRange( ICDIEndSteppingRange endSteppingRange )
	{
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleBreakpointHit( ICDIBreakpoint breakpoint )
	{
		fireSuspendEvent( DebugEvent.BREAKPOINT );
	}
	
	private void handleSuspendedBySignal( ICDISignal signal )
	{
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleExitedEvent( ICDIExitedEvent event )
	{
		setCurrentStateId( IState.EXITED );
		setCurrentStateInfo( event.getExitInfo() );
		fireChangeEvent( DebugEvent.STATE );
	}

	private void handleTerminatedEvent( ICDIDestroyedEvent event )
	{
		setCurrentStateId( IState.TERMINATED );
		setCurrentStateInfo( null );
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

	private void handleLoadedEvent( ICDILoadedEvent event )
	{
	}

	private void handleRestartedEvent( ICDIRestartedEvent event )
	{
	}

	private void handleSteppingEvent( ICDISteppingEvent event )
	{
		setCurrentStateId( IState.STEPPING );
		setCurrentStateInfo( null );
	}

	private void handleThreadCreatedEvent( ICDICreatedEvent event )
	{
		ICDIThread cdiThread = (ICDIThread)event.getSource();
		CThread thread= findThread( cdiThread );
		if ( thread == null ) 
		{
			createThread( cdiThread );
		} 
		else 
		{
			thread.disposeStackFrames();
			thread.fireChangeEvent( DebugEvent.CONTENT );
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
}
