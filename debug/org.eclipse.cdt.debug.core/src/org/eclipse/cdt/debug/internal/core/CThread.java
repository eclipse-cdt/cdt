/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.IInstructionStep;
import org.eclipse.cdt.debug.core.IState;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICBreakpoint;
import org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICSessionObject;
import org.eclipse.cdt.debug.core.cdi.ICSignal;
import org.eclipse.cdt.debug.core.cdi.event.ICChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDisconnectedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICResumedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICSteppingEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICSuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICTerminatedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICObject;
import org.eclipse.cdt.debug.core.cdi.model.ICStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICThread;
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
					 			IInstructionStep,
					 			ICEventListener
{
	/**
	 * Underlying CDI thread.
	 */
	private ICThread fCDIThread;

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
	private ICDebugConfiguration fConfig;	

	/**
	 * Constructor for CThread.
	 * @param target
	 */
	public CThread( CDebugTarget target, ICThread cdiThread )
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
		setRunning( !getCDIThread().isSuspended() );
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
					if ( fStackFrames.isEmpty() )
					{
						//leave fRefreshChildren == true
						//bug 6393
						// ?????
						return fStackFrames;
					}
				}
				ICStackFrame[] frames = getCDIStackFrames();
				// compute new or removed stack frames
				int offset = 0, length = frames.length;
				if ( length > fStackFrames.size() )
				{
					// compute new frames
					offset = length - fStackFrames.size();
					for ( int i = offset - 1; i >= 0; i-- )
					{
						CStackFrame newStackFrame = new CStackFrame( this, frames[i] );
						fStackFrames.add( 0, newStackFrame );
					}
					length = fStackFrames.size() - offset;
				}
				else if ( length < fStackFrames.size() )
				{
					// compute removed children
					int removed = fStackFrames.size() - length;
					for ( int i = 0; i < removed; i++ )
					{
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
						ICStackFrame newTop = frames[0];
						ICStackFrame oldTop = ((CStackFrame)fStackFrames.get( 0 ) ).getLastCDIStackFrame();
						if (!CStackFrame.equalFrame( newTop, oldTop ) )
						{
							fStackFrames = createAllStackFrames();
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
			fRefreshChildren = false;
		}
		else
		{
			return Collections.EMPTY_LIST;
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
	protected ICStackFrame[] getCDIStackFrames() throws DebugException
	{
		try
		{
			return getCDIThread().getStackFrames();
		}
		catch( CDIException e )
		{
			requestFailed( MessageFormat.format( "{0} occurred retrieving stack frames.", new String[] { e.toString() } ), e );
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception			
			return null;
		}
	}

	/**
	 * Replaces the underlying stack frame objects in the preserved frames
	 * list with the current underlying stack frames.
	 * 
	 * @param newFrames list of current underlying <code>ICStackFrame</code>s.
	 * 	Frames from this list are assigned to the underlying frames in
	 *  the <code>oldFrames</code> list.
	 * @param offset the offset in the lists at which to start replacing
	 *  the old underlying frames
	 * @param oldFrames list of preserved frames, of type <code>CStackFrame</code>
	 * @param length the number of frames to replace
	 */
	protected void updateStackFrames( ICStackFrame[] newFrames,
									  int offset,
									  List oldFrames,
									  int length ) throws DebugException
	{
		for ( int i = 0; i < length; i++ )
		{
			CStackFrame frame = (CStackFrame)oldFrames.get( offset );
			frame.setCDIStackFrame( newFrames[offset] );
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
		return computeStackFrames( fRefreshChildren );
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
		ICStackFrame[] frames = getCDIStackFrames();
		List list= new ArrayList( frames.length );
		for ( int i = 0; i < frames.length; ++i )
		{
			CStackFrame newStackFrame = new CStackFrame( this, frames[i] );
			list.add( newStackFrame );
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
		return "Thread " + getCDIThread().getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICEventListener#handleDebugEvent(ICEvent)
	 */
	public void handleDebugEvent( ICEvent event )
	{
		ICObject source = event.getSource();
		if ( source.getCDITarget().equals( getCDITarget() ) )
		{
			if ( event instanceof ICSuspendedEvent )
			{
				if ( source instanceof ICThread )
				{
					handleSuspendedEvent( (ICSuspendedEvent)event );
				}
			}
			else if ( event instanceof ICResumedEvent )
			{
				if ( source instanceof ICThread )
				{
					handleResumedEvent( (ICResumedEvent)event );
				}
			}
			else if ( event instanceof ICTerminatedEvent )
			{
				if ( source instanceof ICThread )
				{
					handleTerminatedEvent( (ICTerminatedEvent)event );
				}
			}
			else if ( event instanceof ICDisconnectedEvent )
			{
				if ( source instanceof ICThread )
				{
					handleDisconnectedEvent( (ICDisconnectedEvent)event );
				}
			}
			else if ( event instanceof ICChangedEvent )
			{
				if ( source instanceof ICThread )
				{
					handleChangedEvent( (ICChangedEvent)event );
				}
			}
			else if ( event instanceof ICSteppingEvent )
			{
				if ( source instanceof ICThread )
				{
					handleSteppingEvent( (ICSteppingEvent)event );
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume()
	{
		return isSuspended() && !getDebugTarget().isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend()
	{
		return !isSuspended();
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
		{
			return;
		}
		try
		{
			setRunning( true );
			disposeStackFrames();
			fireResumeEvent( DebugEvent.CLIENT_REQUEST );
			getCDIThread().resume();
		}
		catch( CDIException e )
		{
			setRunning( false );
			fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
			targetRequestFailed( MessageFormat.format( "{0} occurred resuming thread.", new String[] { e.toString()} ), e );
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
			setRunning( false );
			getCDIThread().suspend();
			fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
		}
		catch( CDIException e )
		{
			setRunning( true );
			fireResumeEvent( DebugEvent.CLIENT_REQUEST );
			targetRequestFailed( MessageFormat.format( "{0} occurred suspending thread.", new String[] { e.toString()} ), e );
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
		{
			return;
		}
		try
		{
			setRunning( true );
			preserveStackFrames();
			fireResumeEvent( DebugEvent.STEP_INTO );
			getCDIThread().stepInto();
		}		
		catch( CDIException e )
		{
			setRunning( false );
			fireSuspendEvent( DebugEvent.STEP_INTO );
			targetRequestFailed( MessageFormat.format( "{0} occurred stepping in thread.", new String[] { e.toString()} ), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException
	{
		if ( !canStepOver() )
		{
			return;
		}
		try
		{
			setRunning( true );
			preserveStackFrames();
			fireResumeEvent( DebugEvent.STEP_OVER );
			getCDIThread().stepInto();
		}		
		catch( CDIException e )
		{
			setRunning( false );
			fireSuspendEvent( DebugEvent.STEP_OVER );
			targetRequestFailed( MessageFormat.format( "{0} occurred stepping in thread.", new String[] { e.toString()} ), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException
	{
		if ( !canStepReturn() )
		{
			return;
		}
		try
		{
			setRunning( true );
			preserveStackFrames();
			fireResumeEvent( DebugEvent.STEP_RETURN );
			getCDIThread().stepInto();
		}		
		catch( CDIException e )
		{
			setRunning( false );
			fireSuspendEvent( DebugEvent.STEP_RETURN );
			targetRequestFailed( MessageFormat.format( "{0} occurred stepping in thread.", new String[] { e.toString()} ), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate()
	{
		return !isTerminated();
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
	protected void setCDIThread( ICThread cdiThread )
	{
		fCDIThread = cdiThread;
	}

	/**
	 * Returns the underlying CDI thread that this model object is 
	 * a proxy to.
	 * 
	 * @return the underlying CDI thread
	 */
	protected ICThread getCDIThread()
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
	protected void preserveStackFrames()
	{
		fRefreshChildren = true;
		Iterator frames = fStackFrames.iterator();
		while( frames.hasNext() )
		{
			((CStackFrame)frames.next()).setCDIStackFrame( null );
		}
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
		fStackFrames = Collections.EMPTY_LIST;
		fRefreshChildren = true;
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
		{
			return;
		}
		try
		{
			setRunning( true );
			preserveStackFrames();
			fireResumeEvent( DebugEvent.STEP_INTO );
			getCDIThread().stepIntoInstruction();
		}		
		catch( CDIException e )
		{
			setRunning( false );
			fireSuspendEvent( DebugEvent.STEP_INTO );
			targetRequestFailed( MessageFormat.format( "{0} occurred stepping in thread.", new String[] { e.toString()} ), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IInstructionStep#stepOverInstruction()
	 */
	public void stepOverInstruction() throws DebugException
	{
		if ( !canStepOverInstruction() )
		{
			return;
		}
		try
		{
			setRunning( true );
			preserveStackFrames();
			fireResumeEvent( DebugEvent.STEP_OVER );
			getCDIThread().stepOverInstruction();
		}		
		catch( CDIException e )
		{
			setRunning( false );
			fireSuspendEvent( DebugEvent.STEP_OVER );
			targetRequestFailed( MessageFormat.format( "{0} occurred stepping in thread.", new String[] { e.toString()} ), e );
		}
	}

	private void handleSuspendedEvent( ICSuspendedEvent event )
	{
		setRunning( false );
		setCurrentStateId( IState.SUSPENDED );
		ICSessionObject reason = event.getReason();
		setCurrentStateInfo( reason );
		if ( reason instanceof ICEndSteppingRange )
		{
			handleEndSteppingRange( (ICEndSteppingRange)reason );
		}
		else if ( reason instanceof ICBreakpoint )
		{
			handleBreakpointHit( (ICBreakpoint)reason );
		}
		else if ( reason instanceof ICSignal )
		{
			handleSuspendedBySignal( (ICSignal)reason );
		}
	}

	private void handleResumedEvent( ICResumedEvent event )
	{
		setRunning( true );
		setCurrentStateId( IState.RUNNING );
		setCurrentStateInfo( null );
		fireResumeEvent( DebugEvent.UNSPECIFIED );
	}
	
	private void handleEndSteppingRange( ICEndSteppingRange endSteppingRange )
	{
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleBreakpointHit( ICBreakpoint breakpoint )
	{
		fireSuspendEvent( DebugEvent.BREAKPOINT );
	}
	
	private void handleSuspendedBySignal( ICSignal signal )
	{
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleTerminatedEvent( ICTerminatedEvent event )
	{
		setCurrentStateId( IState.TERMINATED );
		setCurrentStateInfo( null );
		terminated();
	}

	private void handleDisconnectedEvent( ICDisconnectedEvent event )
	{
		setCurrentStateId( IState.TERMINATED );
		setCurrentStateInfo( null );
		terminated();
	}

	private void handleChangedEvent( ICChangedEvent event )
	{
	}

	private void handleSteppingEvent( ICSteppingEvent event )
	{
		setCurrentStateId( IState.STEPPING );
		setCurrentStateInfo( null );
	}

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
		if ( !canStepReturn() )
		{
			return;
		}
	}
}
