/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.IInstructionStep;
import org.eclipse.cdt.debug.core.IRestart;
import org.eclipse.cdt.debug.core.IState;
import org.eclipse.cdt.debug.core.ISwitchToFrame;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDisconnectedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
//import org.eclipse.cdt.debug.core.cdi.event.ICDISteppingEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

/**
 * 
 * A thread in a C/C++ debug target.
 * 
 * @since Aug 2, 2002
 */
public class CThread extends CDebugElement 
					 implements IThread,
					 			IState,
					 			IRestart,
					 			IInstructionStep,
					 			ISwitchToFrame,
					 			ICDIEventListener
{
	/**
	 * Underlying CDI thread.
	 */
	private ICDIThread fCDIThread;

	/**
	 * Collection of stack frames
	 */
	private List fStackFrames;

	/**
	 * Whether running.
	 */
	private boolean fRunning;

	/**
	 * Whether children need to be refreshed. Set to
	 * <code>true</code> when stack frames are re-used
	 * on the next suspend.
	 */
	private boolean fRefreshChildren = true;

	/**
	 * The current state identifier.
	 */
	private int fCurrentStateId = IState.UNKNOWN;
	
	/**
	 * The current state info.
	 */
	private Object fCurrentStateInfo = null;

	/**
	 * The debug configuration of this session.
	 */
	private ICDIConfiguration fConfig;	

	/**
	 * Whether this thread is current.
	 */
	private boolean fIsCurrent = false;	

	/**
	 * Constructor for CThread.
	 * @param target
	 */
	public CThread( CDebugTarget target, ICDIThread cdiThread )
	{
		super( target );
		setCDIThread( cdiThread );
		fConfig = getCDISession().getConfiguration();
		initialize();
		getCDISession().getEventManager().addEventListener( this );
	}

	/**
	 * Thread initialization:<ul>
	 * <li>Sets terminated state to <code>false</code></li>
	 * <li>Determines suspended state from underlying thread</li> 
	 * <li>Sets this threads stack frames to an empty collection</li>
	 * </ul>
	 */
	protected void initialize()
	{
		fStackFrames = Collections.EMPTY_LIST;
		setRunning( false );
//		setRunning( !getCDIThread().isSuspended() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getStackFrames()
	 */
	public IStackFrame[] getStackFrames() throws DebugException
	{
		List list = computeStackFrames();
		return (IStackFrame[])list.toArray( new IStackFrame[list.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#hasStackFrames()
	 */
	public boolean hasStackFrames() throws DebugException
	{
		try
		{
			return computeStackFrames().size() > 0;
		}
		catch( DebugException e )
		{
			// do not throw an exception if the thread resumed while determining
			// whether stack frames are present
		}
		return false;
	}

	/**
	 * @see computeStackFrames()
	 * 
	 * @param refreshChildren whether or not this method should request new stack
	 *        frames from the target
	 */	
	protected synchronized List computeStackFrames( boolean refreshChildren ) throws DebugException
	{
		if ( isSuspended() )
		{
			if ( isTerminated() )
			{
				fStackFrames = Collections.EMPTY_LIST;
			}
			else if ( refreshChildren )
			{
				if ( fStackFrames.isEmpty() )
				{
					fStackFrames = createAllStackFrames();
					setRefreshChildren( false );
					return fStackFrames;
				}
				ICDIStackFrame[] frames = getCDIStackFrames();
				// compute new or removed stack frames
				int offset = 0, length = frames.length;
				if ( length > fStackFrames.size() )
				{
					// compute new frames
					offset = length - fStackFrames.size();
					for ( int i = offset - 1; i >= 0; i-- )
					{
						fStackFrames.add( 0, new CStackFrame( this, frames[i] ) );
					}
					length = fStackFrames.size() - offset;
				}
				else if ( length < fStackFrames.size() )
				{
					// compute removed children
					int removed = fStackFrames.size() - length;
					for ( int i = 0; i < removed; i++ )
					{
						((CStackFrame)fStackFrames.get( 0 )).dispose();
						fStackFrames.remove( 0 );
					}
				}
				else
				{
					if ( frames.length == 0 )
					{
						fStackFrames = Collections.EMPTY_LIST;
					}
					else
					{
						// same number of frames - if top frames are in different
						// method, replace all frames
						ICDIStackFrame newTop = frames[0];
						ICDIStackFrame oldTop = ((CStackFrame)fStackFrames.get( 0 ) ).getLastCDIStackFrame();
						if ( !CStackFrame.equalFrame( newTop, oldTop ) )
						{
							disposeStackFrames();
							fStackFrames = new ArrayList( frames.length );
							for ( int i = 0; i < frames.length; ++i )
							{
								fStackFrames.add( new CStackFrame( this, frames[i] ) );
							}
							offset = fStackFrames.size();
						}
					}
				}
				// update preserved frames
				if ( offset < fStackFrames.size() )
				{
					updateStackFrames( frames, offset, fStackFrames, length );
				}
			}
			setRefreshChildren( false );
		}
		return fStackFrames;
	}
	
	/**
	 * Retrieves and returns all underlying stack frames
	 * 
	 * @return list of <code>StackFrame</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * </ul>
	 */
	protected ICDIStackFrame[] getCDIStackFrames() throws DebugException
	{
		try
		{
			return getCDIThread().getStackFrames();
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
		return new ICDIStackFrame[0];
	}

	/**
	 * Replaces the underlying stack frame objects in the preserved frames
	 * list with the current underlying stack frames.
	 * 
	 * @param newFrames list of current underlying <code>ICDIStackFrame</code>s.
	 * 	Frames from this list are assigned to the underlying frames in
	 *  the <code>oldFrames</code> list.
	 * @param offset the offset in the lists at which to start replacing
	 *  the old underlying frames
	 * @param oldFrames list of preserved frames, of type <code>CStackFrame</code>
	 * @param length the number of frames to replace
	 */
	protected void updateStackFrames( ICDIStackFrame[] newFrames,
									  int offset,
									  List oldFrames,
									  int length ) throws DebugException
	{
		for ( int i = 0; i < length; i++ )
		{
			CStackFrame frame = (CStackFrame)oldFrames.get( offset );
			frame.setCDIStackFrame( newFrames[offset] );
			frame.fireChangeEvent( DebugEvent.STATE );
			offset++;
		}
	}

	/**
	 * Returns this thread's current stack frames as a list, computing
	 * them if required. Returns an empty collection if this thread is
	 * not currently suspended, or this thread is terminated. This
	 * method should be used internally to get the current stack frames,
	 * instead of calling <code>#getStackFrames()</code>, which makes a
	 * copy of the current list.
	 * <p>
	 * Before a thread is resumed a call must be made to one of:<ul>
	 * <li><code>preserveStackFrames()</code></li>
	 * <li><code>disposeStackFrames()</code></li>
	 * </ul>
	 * If stack frames are disposed before a thread is resumed, stack frames
	 * are completely re-computed on the next call to this method. If stack
	 * frames are to be preserved, this method will attempt to re-use any stack
	 * frame objects which represent the same stack frame as on the previous
	 * suspend. Stack frames are cached until a subsequent call to preserve
	 * or dispose stack frames.
	 * </p>
	 * 
	 * @return list of <code>IStackFrame</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * </ul>
	 */	
	public List computeStackFrames() throws DebugException
	{
		return computeStackFrames( refreshChildren() );
	}
	
	/**
	 * @see CThread#computeStackFrames()
	 * 
	 * This method differs from computeStackFrames() in that it
	 * always requests new stack frames from the target. As this is
	 * an expensive operation, this method should only be used
	 * by clients who know for certain that the stack frames
	 * on the target have changed.
	 */
	public List computeNewStackFrames() throws DebugException
	{
		return computeStackFrames( true );
	}

	/**
	 * Helper method for <code>#computeStackFrames()</code> to create all
	 * underlying stack frames.
	 * 
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	protected List createAllStackFrames() throws DebugException
	{
		ICDIStackFrame[] frames = getCDIStackFrames();
		List list= new ArrayList( frames.length );
		for ( int i = 0; i < frames.length; ++i )
		{
			list.add( new CStackFrame( this, frames[i] ) );
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getPriority()
	 */
	public int getPriority() throws DebugException
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getTopStackFrame()
	 */
	public IStackFrame getTopStackFrame() throws DebugException
	{
		List c = computeStackFrames();
		return ( c.isEmpty() ) ? null : (IStackFrame)c.get( 0 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getName()
	 */
	public String getName() throws DebugException
	{
		return getCDIThread().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvent(ICDIEvent)
	 */
	public void handleDebugEvent( ICDIEvent event )
	{
		ICDIObject source = event.getSource();
		if ( source == null )
			return;
		if ( source.getTarget().equals( getCDITarget() ) )
		{
			if ( event instanceof ICDISuspendedEvent )
			{
				if ( ( source instanceof ICDIThread && getCDIThread().equals( (ICDIThread)source ) ) ||
					   source instanceof ICDITarget )
				{
					handleSuspendedEvent( (ICDISuspendedEvent)event );
				}
			}
			else if ( event instanceof ICDIResumedEvent )
			{
				if ( ( source instanceof ICDIThread && source.equals( getCDIThread() ) ) ||
					   source instanceof ICDITarget )
				{
					handleResumedEvent( (ICDIResumedEvent)event );
				}
			}
			else if ( event instanceof ICDIDestroyedEvent )
			{
				if ( source instanceof ICDIThread )
				{
					handleTerminatedEvent( (ICDIDestroyedEvent)event );
				}
			}
			else if ( event instanceof ICDIDisconnectedEvent )
			{
				if ( source instanceof ICDIThread )
				{
					handleDisconnectedEvent( (ICDIDisconnectedEvent)event );
				}
			}
			else if ( event instanceof ICDIChangedEvent )
			{
				if ( source instanceof ICDIThread )
				{
					handleChangedEvent( (ICDIChangedEvent)event );
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume()
	{
		return fConfig.supportsResume() && isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend()
	{
		return fConfig.supportsSuspend() && !isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended()
	{
		return !fRunning && !isTerminated();
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
			getCDIThread().resume();
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
		{
			return;
		}
		try
		{
			getCDIThread().suspend();
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.toString(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto()
	{
		return canStep();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver()
	{
		return canStep();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn()
	{
		return canStep();
	}

	/**
	 * Returns whether this thread is in a valid state to
	 * step.
	 * 
	 * @return whether this thread is in a valid state to
	 * step
	 */
	protected boolean canStep()
	{
		try
		{
			return fConfig.supportsStepping() && isSuspended() && getTopStackFrame() != null;
		}
		catch( DebugException e )
		{
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping()
	{
		return getCurrentStateId() == IState.STEPPING; // ????
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException
	{
		if ( !canStepInto() )
			return;
		try
		{
			getCDIThread().stepInto();
		}		
		catch( CDIException e )
		{
			targetRequestFailed( e.toString(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException
	{
		if ( !canStepOver() )
			return;
		try
		{
			getCDIThread().stepOver();
		}		
		catch( CDIException e )
		{
			targetRequestFailed( e.toString(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException
	{
		if ( !canStepReturn() )
			return;
		try
		{
			getCDIThread().stepReturn();
		}		
		catch( CDIException e )
		{
			targetRequestFailed( e.toString(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate()
	{
		return getDebugTarget().canTerminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated()
	{
		return getDebugTarget().isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException
	{
		getDebugTarget().terminate();
	}

	/**
	 * Sets the underlying CDI thread that this model object is 
	 * a proxy to.
	 * 
	 * @param thread the underlying CDI thread
	 */
	protected void setCDIThread( ICDIThread cdiThread )
	{
		fCDIThread = cdiThread;
	}

	/**
	 * Returns the underlying CDI thread that this model object is 
	 * a proxy to.
	 * 
	 * @return the underlying CDI thread
	 */
	protected ICDIThread getCDIThread()
	{
		return fCDIThread;
	}

	/**
	 * Sets whether this thread is currently executing.
	 * 
	 * @param running whether this thread is executing
	 */
	protected void setRunning( boolean running ) 
	{
		fRunning = running;
	}

	/**
	 * Preserves stack frames to be used on the next suspend event.
	 * Iterates through all current stack frames, setting their
	 * state as invalid. This method should be called before this thread
	 * is resumed, when stack frames are to be re-used when it later
	 * suspends.
	 * 
	 * @see computeStackFrames()
	 */
	protected synchronized void preserveStackFrames()
	{
		Iterator it = fStackFrames.iterator();
		while( it.hasNext() )
		{
			((CStackFrame)it.next()).preserve();
		}
		setRefreshChildren( true );
	}

	/**
	 * Disposes stack frames, to be completely re-computed on
	 * the next suspend event. This method should be called before
	 * this thread is resumed when stack frames are not to be re-used
	 * on the next suspend.
	 * 
	 */
	protected synchronized void disposeStackFrames() 
	{
		Iterator it = fStackFrames.iterator();
		while( it.hasNext() )
		{
			((CStackFrame)it.next()).dispose();
		}
		fStackFrames.clear();
		setRefreshChildren( true );
	}

	/**
	 * Notification this thread has terminated - update state
	 * and fire a terminate event.
	 */
	protected void terminated() 
	{
		setRunning( false );
		cleanup();	
		fireTerminateEvent();
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IInstructionStep#canStepIntoInstruction()
	 */
	public boolean canStepIntoInstruction()
	{
		return canStepInto();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IInstructionStep#canStepOverInstruction()
	 */
	public boolean canStepOverInstruction()
	{
		return canStepOver();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IInstructionStep#stepIntoInstruction()
	 */
	public void stepIntoInstruction() throws DebugException
	{
		if ( !canStepIntoInstruction() )
			return;
		try
		{
			getCDIThread().stepIntoInstruction();
		}		
		catch( CDIException e )
		{
			targetRequestFailed( e.toString(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IInstructionStep#stepOverInstruction()
	 */
	public void stepOverInstruction() throws DebugException
	{
		if ( !canStepOverInstruction() )
			return;
		try
		{
			getCDIThread().stepOverInstruction();
		}		
		catch( CDIException e )
		{
			targetRequestFailed( e.toString(), e );
		}
	}

	private void handleSuspendedEvent( ICDISuspendedEvent event )
	{
		setRunning( false );
		if ( event.getSource() instanceof ICDITarget ) 
		{
			if ( isCurrent() )
			{
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
				else
				{
					fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
				}
			}
			return;
		}
		setCurrentStateId( IState.SUSPENDED );
		setCurrentStateInfo( null );
	}

	private void handleResumedEvent( ICDIResumedEvent event )
	{
		setRunning( true );
		int state = IState.RUNNING;
		int detail = DebugEvent.UNSPECIFIED;
		if ( isCurrent() )
		{
			switch( event.getType() )
			{
				case ICDIResumedEvent.CONTINUE:
					detail = DebugEvent.CLIENT_REQUEST;
					state = IState.RUNNING;
					disposeStackFrames();
					break;
				case ICDIResumedEvent.STEP_INTO:
				case ICDIResumedEvent.STEP_INTO_INSTRUCTION:
					detail = DebugEvent.STEP_INTO;
					state = IState.STEPPING;
					preserveStackFrames();
					break;
				case ICDIResumedEvent.STEP_OVER:
				case ICDIResumedEvent.STEP_OVER_INSTRUCTION:
					detail = DebugEvent.STEP_OVER;
					state = IState.STEPPING;
					preserveStackFrames();
					break;
				case ICDIResumedEvent.STEP_RETURN:
					detail = DebugEvent.STEP_RETURN;
					state = IState.STEPPING;
					preserveStackFrames();
					break;
			}
		}
		else
		{
			disposeStackFrames();
			detail = DebugEvent.CLIENT_REQUEST;
		}
		setCurrentStateId( state );
		setCurrentStateInfo( null );
		fireResumeEvent( detail );
	}
	
	private void handleEndSteppingRange( ICDIEndSteppingRange endSteppingRange )
	{
		fireSuspendEvent( DebugEvent.STEP_END );
	}

	private void handleBreakpointHit( ICDIBreakpoint breakpoint )
	{
		fireSuspendEvent( DebugEvent.BREAKPOINT );
	}
	
	private void handleSuspendedBySignal( ICDISignal signal )
	{
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleTerminatedEvent( ICDIDestroyedEvent event )
	{
/*
		setCurrentStateId( IState.TERMINATED );
		setCurrentStateInfo( null );
		terminated();
*/
	}

	private void handleDisconnectedEvent( ICDIDisconnectedEvent event )
	{
		setCurrentStateId( IState.TERMINATED );
		setCurrentStateInfo( null );
		terminated();
	}

	private void handleChangedEvent( ICDIChangedEvent event )
	{
	}

	//private void handleSteppingEvent( ICDISteppingEvent event )
	//{
	//	setCurrentStateId( IState.STEPPING );
	//	setCurrentStateInfo( null );
	//}

	/** 
	 * Cleans up the internal state of this thread.
	 * 
	 */
	protected void cleanup()
	{
		getCDISession().getEventManager().removeEventListener( this );
		disposeStackFrames();
	}

	/**
	 * Steps until the specified stack frame is the top frame. Provides
	 * ability to step over/return in the non-top stack frame.
	 * This method is synchronized, such that the step request
	 * begins before a background evaluation can be performed.
	 * 
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * </ul>
	 */
	protected synchronized void stepToFrame( IStackFrame frame ) throws DebugException
	{
	}
	
	private void setRefreshChildren( boolean refresh )
	{
		fRefreshChildren = refresh;
	}
	
	private boolean refreshChildren()
	{
		return fRefreshChildren;
	} 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IRestart#canRestart()
	 */
	public boolean canRestart()
	{
		return getDebugTarget() instanceof IRestart && ((IRestart)getDebugTarget()).canRestart();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IRestart#restart()
	 */
	public void restart() throws DebugException
	{
		if ( canRestart() )
		{
			((IRestart)getDebugTarget()).restart();
		}
	}
	
	protected boolean isCurrent()
	{
		return fIsCurrent;
	}
	
	protected void setCurrent( boolean current )
	{
		fIsCurrent = current;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ISwitchToFrame#switchToFrame(IStackFrame)
	 */
	public void switchToFrame( IStackFrame frame ) throws DebugException
	{
		if ( frame == null && !(frame instanceof CStackFrame) )
		{
			return;
		}
		try
		{
			getCDIThread().setCurrentStackFrame( ((CStackFrame)frame).getCDIStackFrame() );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
	}
}
