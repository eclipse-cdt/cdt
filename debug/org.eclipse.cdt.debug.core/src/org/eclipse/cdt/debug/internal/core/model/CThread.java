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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.ICDISignalReceived;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDisconnectedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.model.ICDebugElementErrorStatus;
import org.eclipse.cdt.debug.core.model.IDummyStackFrame;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.core.model.IResumeWithoutSignal;
import org.eclipse.cdt.debug.core.model.IRunToLine;
import org.eclipse.cdt.debug.core.model.IState;
import org.eclipse.cdt.debug.core.model.ISwitchToFrame;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
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
					 			IResumeWithoutSignal,
					 			ISwitchToFrame,
					 			ICDIEventListener
{
	private boolean fSuspending;

	private final static int MAX_STACK_DEPTH = 100;

	/**
	 * Underlying CDI thread.
	 */
	private ICDIThread fCDIThread;

	/**
	 * Collection of stack frames
	 */
	private ArrayList fStackFrames;

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
	
	private CStackFrame fLastStackFrame = null;	
	
	private int fLastStackDepth = 0;

	private boolean fDisposed = false;

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
		fStackFrames = new ArrayList();
		setRunning( false );
//		setRunning( !getCDIThread().isSuspended() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getStackFrames()
	 */
	public IStackFrame[] getStackFrames() throws DebugException
	{
		List list = Collections.EMPTY_LIST;
		try
		{
			list = computeStackFrames();
		}
		catch( DebugException e )
		{
			setStatus( ICDebugElementErrorStatus.ERROR, e.getStatus().getMessage() );
			throw e;
		}
		return (IStackFrame[])list.toArray( new IStackFrame[list.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#hasStackFrames()
	 */
	public boolean hasStackFrames() throws DebugException
	{
		// Always return true to postpone the stack frames request
		return true;
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
				fStackFrames = new ArrayList();
			}
			else if ( refreshChildren )
			{
				if ( fStackFrames.size() > 0 )
				{
					Object frame = fStackFrames.get( fStackFrames.size() - 1 );
					if ( frame instanceof IDummyStackFrame )
					{
						fStackFrames.remove( frame );
					}
				}
				int depth = getStackDepth();
				ICDIStackFrame[] frames = ( depth != 0 ) ?
									getCDIStackFrames( 0, ( depth > getMaxStackDepth() ) ? getMaxStackDepth() - 1 : depth - 1 ) :
									new ICDIStackFrame[0];
				if ( fStackFrames.isEmpty() )
				{
					addStackFrames( frames, 0, frames.length );
				}
				else if ( depth < getLastStackDepth() )
				{
					disposeStackFrames( 0, getLastStackDepth() - depth );
					updateStackFrames( frames, 0, fStackFrames, fStackFrames.size() );
					if ( fStackFrames.size() < frames.length )
					{
						addStackFrames( frames, fStackFrames.size(), frames.length - fStackFrames.size() );
					}
				}
				else if ( depth > getLastStackDepth() )
				{
					disposeStackFrames( frames.length - depth + getLastStackDepth(), depth - getLastStackDepth() );
					addStackFrames( frames, 0, depth - getLastStackDepth() );
					updateStackFrames( frames, depth - getLastStackDepth(), fStackFrames, frames.length - depth + getLastStackDepth() );
				}
				else // depth == getLastStackDepth()
				{
					if ( depth != 0 )
					{
						// same number of frames - if top frames are in different
						// function, replace all frames
						ICDIStackFrame newTopFrame = ( frames.length > 0 ) ? frames[0] : null;
						ICDIStackFrame oldTopFrame = ( fStackFrames.size() > 0 ) ? ((CStackFrame)fStackFrames.get( 0 ) ).getLastCDIStackFrame() : null;
						if ( !CStackFrame.equalFrame( newTopFrame, oldTopFrame ) )
						{
							disposeStackFrames( 0, fStackFrames.size() );
							addStackFrames( frames, 0, frames.length );
						}
						else // we are in the same frame
						{
							updateStackFrames( frames, 0, fStackFrames, frames.length );
						}
					}
				}
				if ( depth > getMaxStackDepth() )
				{
					fStackFrames.add( new CDummyStackFrame( this ) );
				}
				setLastStackDepth( depth );
				setRefreshChildren( false );
			}
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
		return new ICDIStackFrame[0];
	}

	/**
	 * Retrieves and returns underlying stack frames between <code>lowFrame<code/> 
	 * and <code>highFrame<code/>.
	 * 
	 * @return list of <code>StackFrame</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * </ul>
	 */
	protected ICDIStackFrame[] getCDIStackFrames( int lowFrame, int highFrame ) throws DebugException
	{
		try
		{
			((CDebugTarget)getDebugTarget()).setCurrentThread( this );
			return getCDIThread().getStackFrames( lowFrame, highFrame );
		}
		catch( CDIException e )
		{
			setStatus( ICDebugElementErrorStatus.WARNING, MessageFormat.format( CoreModelMessages.getString( "CThread.0" ), new String[] { e.getMessage() } ) ); //$NON-NLS-1$
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

	protected void addStackFrames( ICDIStackFrame[] newFrames,
								   int startIndex,
								   int length )
	{
		if ( newFrames.length >= startIndex + length )
		{
			for ( int i = 0; i < length; ++i )
			{
				fStackFrames.add( i, new CStackFrame( this, newFrames[startIndex + i] ) );
			}
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
	 */
	protected List createAllStackFrames( int depth, ICDIStackFrame[] frames ) throws DebugException
	{
		List list= new ArrayList( frames.length );
		for ( int i = 0; i < frames.length; ++i )
		{
			list.add( new CStackFrame( this, frames[i] ) );
		}
		if ( depth > frames.length )
		{
			list.add( new CDummyStackFrame( this ) );
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
		return ((CDebugTarget)getDebugTarget()).getThreadBreakpoints( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(ICDIEvent)
	 */
	public void handleDebugEvents( ICDIEvent[] events )
	{
		if ( isDisposed() )
			return;
		for (int i = 0; i < events.length; i++)
		{
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();
			if ( source == null )
				continue;
			if ( source.getTarget().equals( getCDITarget() ) )
			{
				if ( event instanceof ICDISuspendedEvent )
				{
					if ( ( source instanceof ICDIThread && getCDIThread().equals( (ICDIThread)source ) ) ||
							source instanceof ICDITarget )
					{
//						if ( !(((ICDISuspendedEvent)event).getReason() instanceof ICDISharedLibraryEvent && applyDeferredBreakpoints()) )
							handleSuspendedEvent( (ICDISuspendedEvent)event );
					}
				}
				else if ( event instanceof ICDIResumedEvent )
				{
					if ( ( source instanceof ICDIThread && source.equals( getCDIThread() ) ) )
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
		if ( !isSuspended() && !isSuspending() )
			return;
		try
		{
			getCDIThread().resume();
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
						CDebugUtils.error(e1.getStatus(), CThread.this);
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
	public void stepInto() throws DebugException {
		if ( !canStepInto() )
			return;
		try {
			if ( !isInstructionsteppingEnabled() ) {
				getCDIThread().stepInto();
			}
			else {
				getCDIThread().stepIntoInstruction();
			}
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		if ( !canStepOver() )
			return;
		try {
			if ( !isInstructionsteppingEnabled() ) {
				getCDIThread().stepOver();
			}
			else {
				getCDIThread().stepOverInstruction();
			}
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		if ( !canStepReturn() )
			return;
		try {
			getCDIThread().stepReturn();
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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
			CStackFrame frame = (CStackFrame)(((IAdaptable)it.next()).getAdapter( CStackFrame.class ));
			if ( frame != null )
			{
				frame.preserve();
			}
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
			Object obj = it.next();
			if ( obj instanceof CStackFrame ) {
				((CStackFrame)obj).dispose();
			}
		}
		fStackFrames.clear();
		setLastStackDepth( 0 );
		resetStatus();
		setRefreshChildren( true );
	}

	protected void disposeStackFrames( int index, int length )
	{
		List removeList = new ArrayList( length );
		Iterator it = fStackFrames.iterator();
		int counter = 0;
		while( it.hasNext() )
		{
			CStackFrame frame = (CStackFrame)(((IAdaptable)it.next()).getAdapter( CStackFrame.class ));
			if ( frame != null && counter >= index && counter < index + length )
			{
				frame.dispose();
				removeList.add( frame );
			}
			++counter;
		}
		fStackFrames.removeAll( removeList );
	}

	/**
	 * Notification this thread has terminated - update state
	 * and fire a terminate event.
	 */
	protected void terminated() 
	{
		setRunning( false );
		dispose();
		cleanup();	
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

	private void handleSuspendedEvent( ICDISuspendedEvent event )
	{
		setRunning( false );
		if ( event.getSource() instanceof ICDITarget ) 
		{
			if ( isCurrent() && getCurrentStateId() != IState.SUSPENDED )
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
				else if ( reason instanceof ICDISignalReceived )
				{
					handleSuspendedBySignal( (ICDISignalReceived)reason );
				}
				else
				{
//					fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
					// Temporary fix for bug 56520
					fireSuspendEvent( DebugEvent.BREAKPOINT );
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
		setLastStackFrame( null );
		int state = IState.RUNNING;
		int detail = DebugEvent.RESUME;
		if ( isCurrent() && event.getType() != ICDIResumedEvent.CONTINUE )
		{
			preserveStackFrames();
			switch( event.getType() )
			{
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
			state = IState.STEPPING;
		}
		else
		{
			setCurrent( false );
			disposeStackFrames();
			fireChangeEvent( DebugEvent.CONTENT );
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
	
	private void handleSuspendedBySignal( ICDISignalReceived signal )
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
		if ( frame == null || !(frame instanceof CStackFrame) || frame.equals( getLastStackFrame() ) )
		{
			return;
		}
		try
		{
			((CDebugTarget)getDebugTarget()).setCurrentThread( this );
			if ( getLastStackFrame() != null )
			{ 
				((CDebugTarget)getDebugTarget()).resetRegisters();
				getCDIThread().setCurrentStackFrame( ((CStackFrame)frame).getCDIStackFrame() );
			}
			setLastStackFrame( (CStackFrame)frame );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
	}
	
	private void setLastStackFrame( CStackFrame frame )
	{
		fLastStackFrame = frame;
	}
	
	private CStackFrame getLastStackFrame()
	{
		return fLastStackFrame;
	}
	
	protected int getStackDepth() throws DebugException
	{
		int depth = 0;
		try
		{
			depth = getCDIThread().getStackFrameCount();
		}
		catch( CDIException e )
		{
			setStatus( ICDebugElementErrorStatus.WARNING, MessageFormat.format( CoreModelMessages.getString( "CThread.1" ), new String[] { e.getMessage() } ) ); //$NON-NLS-1$
		}
		return depth;
	}
	
	protected int getMaxStackDepth()
	{
		return MAX_STACK_DEPTH;
	}
	
	private void setLastStackDepth( int depth )
	{
		fLastStackDepth = depth;
	}
	
	protected int getLastStackDepth()
	{
		return fLastStackDepth;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter)
	{
		if ( adapter.equals( IRunToLine.class ) )
			return this;
		if ( adapter.equals( IState.class ) )
			return this;
		return super.getAdapter(adapter);
	}
	
	protected void dispose()
	{
		fDisposed = true;
	}
	
	protected boolean isDisposed()
	{
		return fDisposed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeWithoutSignal#canResumeWithoutSignal()
	 */
	public boolean canResumeWithoutSignal()
	{
		return ( getDebugTarget() instanceof IResumeWithoutSignal && 
				 ((IResumeWithoutSignal)getDebugTarget()).canResumeWithoutSignal() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeWithoutSignal#resumeWithoutSignal()
	 */
	public void resumeWithoutSignal() throws DebugException
	{
		if ( canResumeWithoutSignal() )
		{
			((IResumeWithoutSignal)getDebugTarget()).resumeWithoutSignal();
		}
	}
/*
	private boolean applyDeferredBreakpoints()
	{
		boolean result = false;
		try
		{
			result = getLaunch().getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_DEFERRED_BREAKPOINTS, false );
		}
		catch( CoreException e )
		{
		}
		return result;
	}
*/

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

	protected void resumed( int detail, List events )
	{
		setRunning( true );
		setLastStackFrame( null );
		int state = IState.RUNNING;
		if ( isCurrent() && detail != DebugEvent.CLIENT_REQUEST && detail != DebugEvent.UNSPECIFIED )
		{
			preserveStackFrames();
			state = IState.STEPPING;
			events.add( createResumeEvent( detail ) );
		}
		else
		{
			setCurrent( false );
			disposeStackFrames();
			events.add( createChangeEvent( DebugEvent.CONTENT ) );
		}
		setCurrentStateId( state );
		setCurrentStateInfo( null );
	}

	private boolean isInstructionsteppingEnabled() {
		return ((CDebugTarget)getDebugTarget()).isInstructionSteppingEnabled();
	}
}
