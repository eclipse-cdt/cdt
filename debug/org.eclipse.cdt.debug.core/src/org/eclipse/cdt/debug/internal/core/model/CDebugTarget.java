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

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICBreakpoint;
import org.eclipse.cdt.debug.core.ICDebugTargetType;
import org.eclipse.cdt.debug.core.ICExpressionEvaluator;
import org.eclipse.cdt.debug.core.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.ICWatchpoint;
import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.core.IFormattedMemoryRetrieval;
import org.eclipse.cdt.debug.core.IRestart;
import org.eclipse.cdt.debug.core.IRunToLine;
import org.eclipse.cdt.debug.core.IState;
import org.eclipse.cdt.debug.core.ISwitchToThread;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDIRegisterObject;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointScope;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDisconnectedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDILoadedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIRestartedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.sourcelookup.CSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
						  implements IDebugTarget,
						  			 ICDebugTargetType,
						  			 ICDIEventListener,
						  			 IRestart,
						  			 IRunToLine,
						  			 IFormattedMemoryRetrieval,
						  			 IState,
						  			 ILaunchListener,
						  			 ISwitchToThread,
						  			 IExpressionListener,
						  			 ICExpressionEvaluator
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
	 * Collection of register groups added to this target. Values are of type <code>CRegisterGroup</code>.
	 */
	private List fRegisterGroups;

	/**
	 * Collection of temporary breakpoints set at this target. Values are of type <code>ICBreakpoint</code>.
	 */
	private List fTemporaryBreakpoints;

	/**
	 * Constructor for CDebugTarget.
	 * @param target
	 */
	public CDebugTarget( ILaunch launch,
						 int targetType, 
						 ICDITarget cdiTarget, 
						 String name,
						 IProcess process,
						 IProject project,
						 boolean allowsTerminate,
						 boolean allowsDisconnect )
	{
		super( null );
		setLaunch( launch );
		setTargetType( targetType );
		setDebugTarget( this );
		setName( name );
		setProcess( process );
		setCDITarget( cdiTarget );
		setBreakpoints( new HashMap( 5 ) );
		setTemporaryBreakpoints( new ArrayList() );
		if ( getLaunch().getSourceLocator() == null )
			getLaunch().setSourceLocator( createSourceLocator( project ) );
		setConfiguration( cdiTarget.getSession().getConfiguration() );
		fSupportsTerminate = allowsTerminate & getConfiguration().supportsTerminate();
		fSupportsDisconnect = allowsDisconnect & getConfiguration().supportsDisconnect();
		setThreadList( new ArrayList( 5 ) );
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
		setSourceSearchPath();
		initializeBreakpoints();
		initializeRegisters();
		getLaunch().addDebugTarget( this );
		fireCreationEvent();
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
	protected void initializeBreakpoints()
	{
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		manager.addBreakpointListener( this );
		IBreakpoint[] bps = (IBreakpoint[])manager.getBreakpoints( CDebugModel.getPluginIdentifier() );
		for ( int i = 0; i < bps.length; i++ )
		{
			if ( bps[i] instanceof ICBreakpoint ) 
			{
				breakpointAdded( (ICBreakpoint)bps[i] );
			}
		}
	}

	protected void initializeRegisters()
	{
		fRegisterGroups = new ArrayList( 20 );
		createMainRegisterGroup();
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
		if ( !getConfiguration().supportsBreakpoints() )
			return false;
		if ( breakpoint instanceof ICBreakpoint )
		{
			ISourceLocator sl =  getSourceLocator();
			if ( sl != null && sl instanceof CSourceLocator )
			{
				return ((CSourceLocator)sl).contains( breakpoint.getMarker().getResource() );
			}
			return true;
		}
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
		try
		{
			setTerminating( true );
			getCDISession().terminate();
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.toString(), e );
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
		try 
		{
			getCDITarget().resume();
		} 
		catch( CDIException e ) 
		{
			targetRequestFailed( e.toString(), e );
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
			targetRequestFailed( e.toString(), e );
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
		if ( supportsBreakpoint( breakpoint ) )
		{
			try
			{
				if ( breakpoint instanceof ICLineBreakpoint )
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
				CDebugCorePlugin.log( e );
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
			targetRequestFailed( e.toString(), e );
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
			targetRequestFailed( e.toString(), e );
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
		removeAllRegisterGroups();
		getCDISession().getEventManager().removeEventListener( this );
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener( this );
		DebugPlugin.getDefault().getExpressionManager().removeExpressionListener( this );
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener( this );
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

	/**
	 * Removes all temporary breakpoints from this target.
	 * 
	 */
	protected void removeAllTemporaryBreakpoints()
	{
		ICDIBreakpoint[] cdiBreakpoints = (ICDIBreakpoint[])getTemporaryBreakpoints().toArray( new ICDIBreakpoint[0] );
		ICDIBreakpointManager bm = getCDISession().getBreakpointManager();
		if ( cdiBreakpoints.length > 0 )
		{
			try
			{
				bm.deleteBreakpoints( cdiBreakpoints );
				getTemporaryBreakpoints().clear();
			}
			catch( CDIException e )
			{
				logError( e );
			}
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
		removeAllTemporaryBreakpoints();
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
		else if ( reason instanceof ICDISignal )
		{
			handleSuspendedBySignal( (ICDISignal)reason );
		}
		else if ( reason instanceof ICDIWatchpointTrigger )
		{
			handleWatchpointTrigger( (ICDIWatchpointTrigger)reason );
		}
		else if ( reason instanceof ICDIWatchpointScope )
		{
			handleWatchpointScope( (ICDIWatchpointScope)reason );
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
	
	private void handleSuspendedBySignal( ICDISignal signal )
	{
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleExitedEvent( ICDIExitedEvent event )
	{
		removeAllThreads();
		setCurrentStateId( IState.EXITED );
		setCurrentStateInfo( event.getExitInfo() );
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

	private void handleLoadedEvent( ICDILoadedEvent event )
	{
	}

	private void handleRestartedEvent( ICDIRestartedEvent event )
	{
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
	 * Returns the list of temporary breakpoints installed in this debug target.
	 * 
	 * @return list of installed temporary breakpoints
	 */
	protected List getTemporaryBreakpoints()
	{
		return fTemporaryBreakpoints;
	}

	/**
	 * Uninstalles all temporary breakpoints installed in this debug target.
	 * 
	 */
	protected void deleteTemporaryBreakpoints()
	{
	}

	/**
	 * Sets the map of breakpoints installed in this debug target. 
	 * 
	 * @param breakpoints breakpoints map
	 */
	private void setBreakpoints( HashMap breakpoints )
	{
		fBreakpoints = breakpoints;
	}
	
	/**
	 * Sets the list of temporary breakpoints installed in this debug target. 
	 * 
	 * @param breakpoints breakpoints list
	 */
	private void setTemporaryBreakpoints( List breakpoints )
	{
		fTemporaryBreakpoints = breakpoints;
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
		ICDIBreakpointManager bm = getCDISession().getBreakpointManager();
		try
		{
			// FIXME: We should make sure that the parent folder where we
			// want to set the breakpoint is added to the list of source directory.
			// where the debugger looks for files.
			ICDILocation location = bm.createLocation( breakpoint.getMarker().getResource().getLocation().lastSegment(), null, breakpoint.getLineNumber() );
			ICDICondition condition = bm.createCondition( breakpoint.getIgnoreCount(), breakpoint.getCondition() );
			ICDIBreakpoint cdiBreakpoint = bm.setLocationBreakpoint( ICDIBreakpoint.REGULAR, location, condition, null );
			if ( !getBreakpoints().containsKey( breakpoint ) )
			{
				getBreakpoints().put( breakpoint, cdiBreakpoint );
				((CBreakpoint)breakpoint).incrementInstallCount();
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
	
	private void setWatchpoint( ICWatchpoint watchpoint ) throws DebugException
	{
		ICDIBreakpointManager bm = getCDISession().getBreakpointManager();
		try
		{
			ICDICondition condition = bm.createCondition( watchpoint.getIgnoreCount(), watchpoint.getCondition() );
			int accessType = 0;
			accessType |= ( watchpoint.isWriteType() ) ? ICDIWatchpoint.WRITE : 0;
			accessType |= ( watchpoint.isReadType() ) ? ICDIWatchpoint.READ : 0;
			String expression = watchpoint.getExpression();
			ICDIWatchpoint cdiWatchpoint = bm.setWatchpoint( ICDIBreakpoint.REGULAR, accessType, expression, condition );
			if ( !getBreakpoints().containsKey( watchpoint ) )
			{
				getBreakpoints().put( watchpoint, cdiWatchpoint );
				((CBreakpoint)watchpoint).incrementInstallCount();
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

	protected IRegisterGroup[] getRegisterGroups() throws DebugException
	{
		return (IRegisterGroup[])fRegisterGroups.toArray( new IRegisterGroup[fRegisterGroups.size()] );
	}

	protected IRegisterGroup[] getRegisterGroups( CStackFrame stackFrame ) throws DebugException
	{
		return (IRegisterGroup[])fRegisterGroups.toArray( new IRegisterGroup[fRegisterGroups.size()] );
	}
	
	protected void createMainRegisterGroup()
	{
		ICDIRegisterObject[] regObjects = null;
		try
		{
			regObjects = getCDITarget().getRegisterObjects();
		}
		catch( CDIException e )
		{
			CDebugCorePlugin.log( e );
		}
		if ( regObjects != null )
		{
			fRegisterGroups.add( new CRegisterGroup( this, "Main", regObjects ) );
		}
	}

	protected void removeAllRegisterGroups()
	{
		Iterator it = fRegisterGroups.iterator();
		while( it.hasNext() )
		{
			((CRegisterGroup)it.next()).dispose();
		}
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
				em.removeExpression( ((CExpression)expression).getCDIExpression() );
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
			ICDIBreakpoint bkpt = getCDISession().getBreakpointManager().
										setLocationBreakpoint( ICDIBreakpoint.REGULAR, //ICDIBreakpoint.TEMPORARY,
															   location,
															   null,
															   null );
			getTemporaryBreakpoints().add( bkpt );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IRunToLine#canRunToLine(IResource, int)
	 */
	public boolean canRunToLine( IResource resource, int lineNumber )
	{
		// check if supports run to line
		return canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IRunToLine#runToLine(IResource, int)
	 */
	public void runToLine( IResource resource, int lineNumber ) throws DebugException
	{
		if ( !canRunToLine( resource, lineNumber ) )
		{
			return;
		}
		setInternalTemporaryBreakpoint( getCDISession().getBreakpointManager().createLocation( resource.getLocation().lastSegment(), null, lineNumber ) );
		resume();
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
			getCDITarget().setCurrentThread( ((CThread)thread).getCDIThread() );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
	}

	protected ISourceLocator getSourceLocator()
	{
		return getLaunch().getSourceLocator();
	}
	
	protected ISourceLocator createSourceLocator( IProject project )
	{
		return new CSourceLocator( project );
	}
	
	protected void setSourceSearchPath()
	{
		ICDISourceManager mgr = getCDISession().getSourceManager();
		ISourceLocator locator = getLaunch().getSourceLocator();
		ArrayList list = new ArrayList();
		if ( locator != null && locator instanceof ICSourceLocator )
		{
			ICSourceLocation[] locations = ((ICSourceLocator)locator).getSourceLocations();
			for ( int i = 0; i < locations.length; ++i )
			{
				IPath[] paths = locations[i].getPaths();
				for ( int j = 0; j < paths.length; ++j )
				{
					list.add( paths[j].toOSString() );
				}
			}
		}
		try
		{
			mgr.addSourcePaths( (String[])list.toArray( new String[list.size()] ) );
		}
		catch( CDIException e )
		{
			CDebugCorePlugin.log( e );
		}
	}

	protected void resetRegisters()
	{
		Iterator it = fRegisterGroups.iterator();
		while( it.hasNext() )
		{
			((CRegisterGroup)it.next()).resetChangeFlags();
		}
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
}
