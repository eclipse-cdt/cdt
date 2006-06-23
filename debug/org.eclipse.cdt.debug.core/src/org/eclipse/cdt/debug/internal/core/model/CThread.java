/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.ICDISignalReceived;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger;
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
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration2;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.model.CDebugElementState;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.cdt.debug.core.model.IDummyStackFrame;
import org.eclipse.cdt.debug.core.model.IJumpToAddress;
import org.eclipse.cdt.debug.core.model.IJumpToLine;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.core.model.IResumeWithoutSignal;
import org.eclipse.cdt.debug.core.model.IRunToAddress;
import org.eclipse.cdt.debug.core.model.IRunToLine;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * A thread in a C/C++ debug model.
 */
public class CThread extends CDebugElement implements ICThread, IRestart, IResumeWithoutSignal, ICDIEventListener {

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
	 * Whether children need to be refreshed. Set to <code>true</code> when stack frames are re-used on the next suspend.
	 */
	private boolean fRefreshChildren = true;

	/**
	 * The debug configuration of this session.
	 */
	private ICDITargetConfiguration fConfig;

	/**
	 * Whether this thread is current.
	 */
	private boolean fIsCurrent = false;

	/**
	 * The depth of the current stack.  
	 */
	private int fLastStackDepth = 0;

	/**
	 * Whether this thread is disposed.
	 */
	private boolean fDisposed = false;

	/**
	 * Constructor for CThread.
	 */
	public CThread( CDebugTarget target, ICDIThread cdiThread ) {
		super( target );
		setState( cdiThread.isSuspended() ? CDebugElementState.SUSPENDED : CDebugElementState.RESUMED );
		setCDIThread( cdiThread );
		fConfig = getCDITarget().getConfiguration();
		initialize();
		getCDISession().getEventManager().addEventListener( this );
	}

	protected void initialize() {
		fStackFrames = new ArrayList();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getStackFrames()
	 */
	public IStackFrame[] getStackFrames() throws DebugException {
		List list = Collections.EMPTY_LIST;
		try {
			list = computeStackFrames();
		}
		catch( DebugException e ) {
			setStatus( ICDebugElementStatus.ERROR, e.getStatus().getMessage() );
			throw e;
		}
		return (IStackFrame[])list.toArray( new IStackFrame[list.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#hasStackFrames()
	 */
	public boolean hasStackFrames() throws DebugException {
		// Always return true to postpone the stack frames request
		return true;
	}

	/**
	 * @see computeStackFrames()
	 * 
	 * @param refreshChildren whether or not this method should request new stack frames from the target
	 */
	protected synchronized List computeStackFrames( boolean refreshChildren ) throws DebugException {
		if ( isSuspended() ) {
			if ( isTerminated() ) {
				fStackFrames = new ArrayList();
			}
			else if ( refreshChildren ) {
				// Remove dummy frame
				if ( fStackFrames.size() > 0 ) {
					Object frame = fStackFrames.get( fStackFrames.size() - 1 );
					if ( frame instanceof IDummyStackFrame ) {
						fStackFrames.remove( frame );
					}
				}
				// Retrieve stack frames from the backend
				int depth = getStackDepth();
				if ( depth >= getMaxStackDepth() )
					depth = getMaxStackDepth() - 1;
				ICDIStackFrame[] frames = ( depth != 0 ) ? getCDIStackFrames( 0, depth ) : new ICDIStackFrame[0];
				
				if ( fStackFrames.isEmpty() ) {
					if ( frames.length > 0 ) {
						addStackFrames( frames, 0, frames.length, false );
					}
				}
				else {
					int diff = depth - getLastStackDepth();
					int offset = ( diff > 0 ) ? frames.length - diff : 0;
					int length = ( diff > 0 ) ? diff : -diff;
					if ( !compareStackFrames( frames, fStackFrames, offset, length ) ) {
						// replace all frames
						disposeStackFrames( 0, fStackFrames.size() );
						addStackFrames( frames, 0, frames.length, false );						
					}
					if ( diff < 0 ) {
						// stepping out of the last frame
						disposeStackFrames( 0, getLastStackDepth() - depth );
						if ( frames.length > 0 ) {
							updateStackFrames( frames, 0, fStackFrames, fStackFrames.size() );
							if ( fStackFrames.size() < frames.length ) {
								addStackFrames( frames, fStackFrames.size(), frames.length - fStackFrames.size(), true );
							}
						}
					}
					else if ( diff > 0 ) {
						// stepping into a new frame
						disposeStackFrames( frames.length - depth + getLastStackDepth(), depth - getLastStackDepth() );
						addStackFrames( frames, 0, depth - getLastStackDepth(), false );
						updateStackFrames( frames, depth - getLastStackDepth(), fStackFrames, frames.length - depth + getLastStackDepth() );
					}
					else { // diff == 0
						if ( depth != 0 ) {
							// we are in the same frame
							updateStackFrames( frames, 0, fStackFrames, frames.length );
						}
					}
				}
				if ( depth > getMaxStackDepth() ) {
					fStackFrames.add( new CDummyStackFrame( this ) );
				}
				setLastStackDepth( depth );
				setRefreshChildren( false );
			}
		}
		return fStackFrames;
	}
	
	/**
	 * Compares the lists of the old and new frames.
	 * 
	 * @param newFrames the array of the new frames
	 * @param oldFrames the list of the old frames
	 * @param offset the offset in the new frames array
	 * @param length the number of frames to compare
	 * 
	 * @return <code>true</code> if all frames are same
	 */
	private boolean compareStackFrames( ICDIStackFrame[] newFrames, List oldFrames, int offset, int length ) {
		int index = offset;
		Iterator it = oldFrames.iterator();
		while( it.hasNext() && index < newFrames.length ) {
			CStackFrame frame = (CStackFrame)it.next();
			if ( !frame.getCDIStackFrame().equals( newFrames[index++] ) )
				return false;
		}
		return true;
	}

	/**
	 * Retrieves and returns all underlying stack frames
	 * 
	 * @return list of <code>StackFrame</code>
	 * @exception DebugException if this method fails. Reasons include:
	 * <ul>
	 * </ul>
	 */
	protected ICDIStackFrame[] getCDIStackFrames() throws DebugException {
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
	protected ICDIStackFrame[] getCDIStackFrames( int lowFrame, int highFrame ) throws DebugException {
		try {
			return getCDIThread().getStackFrames( lowFrame, highFrame );
		}
		catch( CDIException e ) {
			setStatus( ICDebugElementStatus.WARNING, MessageFormat.format( CoreModelMessages.getString( "CThread.0" ), new String[]{ e.getMessage() } ) ); //$NON-NLS-1$
			targetRequestFailed( e.getMessage(), null );
		}
		return new ICDIStackFrame[0];
	}

	/**
	 * Replaces the underlying stack frame objects in the preserved frames list with the current underlying stack frames.
	 * 
	 * @param newFrames list of current underlying <code>ICDIStackFrame</code>s. Frames from this list are assigned to the underlying frames in the
	 *            <code>oldFrames</code> list.
	 * @param offset the offset in the lists at which to start replacing the old underlying frames
	 * @param oldFrames list of preserved frames, of type <code>CStackFrame</code>
	 * @param length the number of frames to replace
	 */
	protected void updateStackFrames( ICDIStackFrame[] newFrames, int offset, List oldFrames, int length ) throws DebugException {
		for( int i = 0; i < length; i++ ) {
			CStackFrame frame = (CStackFrame)oldFrames.get( offset );
			frame.setCDIStackFrame( newFrames[offset] );
			offset++;
		}
	}

	protected void addStackFrames( ICDIStackFrame[] newFrames, int startIndex, int length, boolean append ) {
		if ( newFrames.length >= startIndex + length ) {
			for( int i = 0; i < length; ++i ) {
				if ( append )
					fStackFrames.add( new CStackFrame( this, newFrames[startIndex + i] ) );
				else
					fStackFrames.add( i, new CStackFrame( this, newFrames[startIndex + i] ) );
			}
		}
	}

	/**
	 * Returns this thread's current stack frames as a list, computing them if required. Returns an empty collection if this thread is not currently suspended,
	 * or this thread is terminated. This method should be used internally to get the current stack frames, instead of calling <code>#getStackFrames()</code>,
	 * which makes a copy of the current list.
	 * <p>
	 * Before a thread is resumed a call must be made to one of:
	 * <ul>
	 * <li><code>preserveStackFrames()</code></li>
	 * <li><code>disposeStackFrames()</code></li>
	 * </ul>
	 * If stack frames are disposed before a thread is resumed, stack frames are completely re-computed on the next call to this method. If stack frames are to
	 * be preserved, this method will attempt to re-use any stack frame objects which represent the same stack frame as on the previous suspend. Stack frames
	 * are cached until a subsequent call to preserve or dispose stack frames.
	 * </p>
	 * 
	 * @return list of <code>IStackFrame</code>
	 * @exception DebugException if this method fails. Reasons include:
	 * <ul>
	 * </ul>
	 */
	public List computeStackFrames() throws DebugException {
		return computeStackFrames( refreshChildren() );
	}

	/**
	 * @see CThread#computeStackFrames()
	 * 
	 * This method differs from computeStackFrames() in that it always requests new stack frames from the target. As this is an expensive operation, this method
	 * should only be used by clients who know for certain that the stack frames on the target have changed.
	 */
	public List computeNewStackFrames() throws DebugException {
		return computeStackFrames( true );
	}

	/**
	 * Helper method for <code>#computeStackFrames()</code> to create all underlying stack frames.
	 * 
	 * @exception DebugException if this method fails. Reasons include:
	 * <ul>
	 * </ul>
	 */
	protected List createAllStackFrames( int depth, ICDIStackFrame[] frames ) throws DebugException {
		List list = new ArrayList( frames.length );
		for( int i = 0; i < frames.length; ++i ) {
			list.add( new CStackFrame( this, frames[i] ) );
		}
		if ( depth > frames.length ) {
			list.add( new CDummyStackFrame( this ) );
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getPriority()
	 */
	public int getPriority() throws DebugException {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getTopStackFrame()
	 */
	public IStackFrame getTopStackFrame() throws DebugException {
		List c = computeStackFrames();
		return (c.isEmpty()) ? null : (IStackFrame)c.get( 0 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getName()
	 */
	public String getName() throws DebugException {
		return getCDIThread().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints() {
		List list = new ArrayList( 1 );
		if ( isSuspended() ) {
			IBreakpoint bkpt = null;
			if ( getCurrentStateInfo() instanceof ICDIBreakpointHit )
				bkpt = ((CDebugTarget)getDebugTarget()).getBreakpointManager().getBreakpoint( ((ICDIBreakpointHit)getCurrentStateInfo()).getBreakpoint() );
			else if ( getCurrentStateInfo() instanceof ICDIWatchpointTrigger )
				bkpt = ((CDebugTarget)getDebugTarget()).getBreakpointManager().getBreakpoint( ((ICDIWatchpointTrigger)getCurrentStateInfo()).getWatchpoint() );
			if ( bkpt != null )
				list.add( bkpt );
		}
		return (IBreakpoint[])list.toArray( new IBreakpoint[list.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents( ICDIEvent[] events ) {
		if ( isDisposed() )
			return;
		for( int i = 0; i < events.length; i++ ) {
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();
			if ( source instanceof ICDIThread && source.equals( getCDIThread() ) ) {
				if ( event instanceof ICDISuspendedEvent ) {
					handleSuspendedEvent( (ICDISuspendedEvent)event );
				}
				else if ( event instanceof ICDIResumedEvent ) {
					handleResumedEvent( (ICDIResumedEvent)event );
				}
				else if ( event instanceof ICDIDestroyedEvent ) {
					handleTerminatedEvent( (ICDIDestroyedEvent)event );
				}
				else if ( event instanceof ICDIDisconnectedEvent ) {
					handleDisconnectedEvent( (ICDIDisconnectedEvent)event );
				}
				else if ( event instanceof ICDIChangedEvent ) {
					handleChangedEvent( (ICDIChangedEvent)event );
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return ( fConfig.supportsResume() && isSuspended() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		CDebugElementState state = getState();
		return ( fConfig.supportsSuspend() && (state.equals( CDebugElementState.RESUMED ) || state.equals( CDebugElementState.STEPPED )) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return getState().equals( CDebugElementState.SUSPENDED );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		if ( !canResume() )
			return;
		CDebugElementState oldState = getState();
		setState( CDebugElementState.RESUMING );
		try {
			getCDIThread().resume( false );
		}
		catch( CDIException e ) {
			setState( oldState );
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		if ( !canSuspend() )
			return;
		CDebugElementState oldState = getState();
		setState( CDebugElementState.SUSPENDING );
		try {
			getCDIThread().suspend();
		}
		catch( CDIException e ) {
			setState( oldState );
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		return canStep();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver() {
		return canStep();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		if ( !fConfig.supportsStepping() || !canResume() ) {
			return false;
		}
		return ( fStackFrames.size() > 1 );
	}

	/**
	 * Returns whether this thread is in a valid state to step.
	 * 
	 * @return whether this thread is in a valid state to step
	 */
	protected boolean canStep() {
		if ( !fConfig.supportsStepping() || !isSuspended() ) {
			return false;
		}
		return !fStackFrames.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping() {
		return ( getState().equals( CDebugElementState.STEPPING ) ) || ( getState().equals( CDebugElementState.STEPPED ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		if ( !canStepInto() )
			return;
		CDebugElementState oldState = getState();
		setState( CDebugElementState.STEPPING );
		try {
			if ( !isInstructionsteppingEnabled() ) {
				getCDIThread().stepInto( 1 );
			}
			else {
				getCDIThread().stepIntoInstruction( 1 );
			}
		}
		catch( CDIException e ) {
			setState( oldState );
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		if ( !canStepOver() )
			return;
		CDebugElementState oldState = getState();
		setState( CDebugElementState.STEPPING );
		try {
			if ( !isInstructionsteppingEnabled() ) {
				getCDIThread().stepOver( 1 );
			}
			else {
				getCDIThread().stepOverInstruction( 1 );
			}
		}
		catch( CDIException e ) {
			setState( oldState );
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		if ( !canStepReturn() )
			return;
		IStackFrame[] frames = getStackFrames();
		if ( frames.length == 0 )
			return;
		CStackFrame f = (CStackFrame)frames[0]; 
		CDebugElementState oldState = getState();
		setState( CDebugElementState.STEPPING );
		try {
			f.doStepReturn();
		}
		catch( DebugException e ) {
			setState( oldState );
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return getDebugTarget().canTerminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return getDebugTarget().isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		getDebugTarget().terminate();
	}

	/**
	 * Sets the underlying CDI thread that this model object is a proxy to.
	 * 
	 * @param thread the underlying CDI thread
	 */
	protected void setCDIThread( ICDIThread cdiThread ) {
		fCDIThread = cdiThread;
	}

	/**
	 * Returns the underlying CDI thread that this model object is a proxy to.
	 * 
	 * @return the underlying CDI thread
	 */
	protected ICDIThread getCDIThread() {
		return fCDIThread;
	}

	/**
	 * Preserves stack frames to be used on the next suspend event. Iterates through all current stack frames, setting their state as invalid. This method
	 * should be called before this thread is resumed, when stack frames are to be re-used when it later suspends.
	 * 
	 * @see computeStackFrames()
	 */
	protected synchronized void preserveStackFrames() {
		Iterator it = fStackFrames.iterator();
		while( it.hasNext() ) {
			CStackFrame frame = (CStackFrame)(((IAdaptable)it.next()).getAdapter( CStackFrame.class ));
			if ( frame != null ) {
				frame.preserve();
			}
		}
		setRefreshChildren( true );
	}

	/**
	 * Disposes stack frames, to be completely re-computed on the next suspend event. This method should be called before this thread is resumed when stack
	 * frames are not to be re-used on the next suspend.
	 */
	protected synchronized void disposeStackFrames() {
		Iterator it = fStackFrames.iterator();
		while( it.hasNext() ) {
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

	protected void disposeStackFrames( int index, int length ) {
		List removeList = new ArrayList( length );
		Iterator it = fStackFrames.iterator();
		int counter = 0;
		while( it.hasNext() ) {
			CStackFrame frame = (CStackFrame)(((IAdaptable)it.next()).getAdapter( CStackFrame.class ));
			if ( frame != null && counter >= index && counter < index + length ) {
				frame.dispose();
				removeList.add( frame );
			}
			++counter;
		}
		fStackFrames.removeAll( removeList );
	}

	/**
	 * Notification this thread has terminated - update state and fire a terminate event.
	 */
	protected void terminated() {
		setState( CDebugElementState.TERMINATED );
		dispose();
	}

	private void handleSuspendedEvent( ICDISuspendedEvent event ) {
		if ( !(getState().equals( CDebugElementState.RESUMED ) || 
			   getState().equals( CDebugElementState.STEPPED ) ||
			   getState().equals( CDebugElementState.SUSPENDING )) )
			return;
		setState( CDebugElementState.SUSPENDED );
		ICDISessionObject reason = event.getReason();
		setCurrentStateInfo( reason );
		if ( reason instanceof ICDIEndSteppingRange ) {
			handleEndSteppingRange( (ICDIEndSteppingRange)reason );
		}
		else if ( reason instanceof ICDIBreakpoint ) {
			handleBreakpointHit( (ICDIBreakpoint)reason );
		}
		else if ( reason instanceof ICDISignalReceived ) {
			handleSuspendedBySignal( (ICDISignalReceived)reason );
		}
		else {
			// fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
			// Temporary fix for bug 56520
			fireSuspendEvent( DebugEvent.BREAKPOINT );
		}
	}

	private void handleResumedEvent( ICDIResumedEvent event ) {
		CDebugElementState state = CDebugElementState.RESUMED;
		int detail = DebugEvent.RESUME;
		if ( isCurrent() && event.getType() != ICDIResumedEvent.CONTINUE ) {
			preserveStackFrames();
			switch( event.getType() ) {
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
			state = CDebugElementState.STEPPED;
		}
		else {
			disposeStackFrames();
			fireChangeEvent( DebugEvent.CONTENT );
		}
		setCurrent( false );
		setState( state );
		setCurrentStateInfo( null );
		fireResumeEvent( detail );
	}

	private void handleEndSteppingRange( ICDIEndSteppingRange endSteppingRange ) {
		fireSuspendEvent( DebugEvent.STEP_END );
	}

	private void handleBreakpointHit( ICDIBreakpoint breakpoint ) {
		fireSuspendEvent( DebugEvent.BREAKPOINT );
	}

	private void handleSuspendedBySignal( ICDISignalReceived signal ) {
		fireSuspendEvent( DebugEvent.UNSPECIFIED );
	}

	private void handleTerminatedEvent( ICDIDestroyedEvent event ) {
		setState( CDebugElementState.TERMINATED );
		setCurrentStateInfo( null );
		terminated();
	}

	private void handleDisconnectedEvent( ICDIDisconnectedEvent event ) {
		setState( CDebugElementState.TERMINATED );
		setCurrentStateInfo( null );
		terminated();
	}

	private void handleChangedEvent( ICDIChangedEvent event ) {
	}

	/**
	 * Cleans up the internal state of this thread.
	 */
	protected void cleanup() {
		getCDISession().getEventManager().removeEventListener( this );
		disposeStackFrames();
	}

	private void setRefreshChildren( boolean refresh ) {
		fRefreshChildren = refresh;
	}

	private boolean refreshChildren() {
		return fRefreshChildren;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRestart#canRestart()
	 */
	public boolean canRestart() {
		return getDebugTarget() instanceof IRestart && ((IRestart)getDebugTarget()).canRestart();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRestart#restart()
	 */
	public void restart() throws DebugException {
		if ( canRestart() ) {
			((IRestart)getDebugTarget()).restart();
		}
	}

	protected boolean isCurrent() {
		return fIsCurrent;
	}

	protected void setCurrent( boolean current ) {
		if (!current)
		{
			if (getCDITarget().getConfiguration() instanceof ICDITargetConfiguration2 && ((ICDITargetConfiguration2)getCDITarget().getConfiguration()).supportsThreadControl())
				current = true; // When the debugger can control individual threads treat every thread is "current"
		}
		fIsCurrent = current;
	}

	protected int getStackDepth() throws DebugException {
		int depth = 0;
		try {
			depth = getCDIThread().getStackFrameCount();
		}
		catch( CDIException e ) {
			setStatus( ICDebugElementStatus.WARNING, MessageFormat.format( CoreModelMessages.getString( "CThread.1" ), new String[]{ e.getMessage() } ) ); //$NON-NLS-1$
		}
		return depth;
	}

	protected int getMaxStackDepth() {
		return MAX_STACK_DEPTH;
	}

	private void setLastStackDepth( int depth ) {
		fLastStackDepth = depth;
	}

	protected int getLastStackDepth() {
		return fLastStackDepth;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( adapter.equals( IRunToLine.class ) || 
			 adapter.equals( IRunToAddress.class ) ||
			 adapter.equals( IJumpToLine.class ) || 
			 adapter.equals( IJumpToAddress.class ) ) {
			try {
				// Alain: Put a proper fix later.
				Object obj = getTopStackFrame();
				if (obj instanceof ICStackFrame) {
					return obj;
				}
			}
			catch( DebugException e ) {
				// do nothing
			}
		}
		if ( adapter.equals( CDebugElementState.class ) )
			return this;
		if ( adapter == ICStackFrame.class ) {
			try {
				// Alain: Put a proper fix later.
				Object obj = getTopStackFrame();
				if (obj instanceof ICStackFrame) {
					return obj;
				}
			}
			catch( DebugException e ) {
				// do nothing
			}
		}
		if ( adapter == IMemoryBlockRetrieval.class ) {
			return getDebugTarget().getAdapter( adapter );
		}
		return super.getAdapter( adapter );
	}

	protected void dispose() {
		fDisposed = true;
		cleanup();
	}

	protected boolean isDisposed() {
		return fDisposed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeWithoutSignal#canResumeWithoutSignal()
	 */
	public boolean canResumeWithoutSignal() {
		return (getDebugTarget() instanceof IResumeWithoutSignal && ((IResumeWithoutSignal)getDebugTarget()).canResumeWithoutSignal());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeWithoutSignal#resumeWithoutSignal()
	 */
	public void resumeWithoutSignal() throws DebugException {
		if ( canResumeWithoutSignal() ) {
			((IResumeWithoutSignal)getDebugTarget()).resumeWithoutSignal();
		}
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

	protected void resumedByTarget( int detail, List events ) {
		if ( isCurrent() && detail != DebugEvent.CLIENT_REQUEST && detail != DebugEvent.UNSPECIFIED ) {
			setState( CDebugElementState.STEPPED );
			preserveStackFrames();
			events.add( createResumeEvent( detail ) );
		}
		else {
			setState( CDebugElementState.RESUMED );
			disposeStackFrames();
			events.add( createResumeEvent( DebugEvent.CLIENT_REQUEST ) );
		}
		setCurrent( false );
		setCurrentStateInfo( null );
	}

	protected boolean isInstructionsteppingEnabled() {
		return ((CDebugTarget)getDebugTarget()).isInstructionSteppingEnabled();
	}

	protected void suspendByTarget( ICDISessionObject reason, ICDIThread suspensionThread ) {
		setState( CDebugElementState.SUSPENDED );
		setCurrentStateInfo( null );
		if ( getCDIThread().equals( suspensionThread ) ) {
			setCurrent( true );
			setCurrentStateInfo( reason );
			if ( reason instanceof ICDIEndSteppingRange ) {
				handleEndSteppingRange( (ICDIEndSteppingRange)reason );
			}
			else if ( reason instanceof ICDIBreakpoint ) {
				handleBreakpointHit( (ICDIBreakpoint)reason );
			}
			else if ( reason instanceof ICDISignalReceived ) {
				handleSuspendedBySignal( (ICDISignalReceived)reason );
			}
			else {
				// fireSuspendEvent( DebugEvent.CLIENT_REQUEST );
				// Temporary fix for bug 56520
				fireSuspendEvent( DebugEvent.BREAKPOINT );
			}			
		}
	}
}
