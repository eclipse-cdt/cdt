/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core;

import org.eclipse.cdt.debug.core.IState;
import org.eclipse.cdt.debug.core.cdi.event.ICEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICThread;
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
					 			ICEventListener
{
	/**
	 * Underlying CDI target.
	 */
	private ICThread fCDIThread;

	/**
	 * Constructor for CThread.
	 * @param target
	 */
	public CThread( CDebugTarget target, ICThread cdiThread )
	{
		super( target );
		setCDIThread( cdiThread );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getStackFrames()
	 */
	public IStackFrame[] getStackFrames() throws DebugException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#hasStackFrames()
	 */
	public boolean hasStackFrames() throws DebugException
	{
		return false;
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
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getName()
	 */
	public String getName() throws DebugException
	{
		return null;
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException
	{
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
	}

	/**
	 * Sets whether this thread is terminated
	 * 
	 * @param terminated whether this thread is terminated
	 */
	protected void setTerminated( boolean terminated )
	{
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
	}

	/**
	 * Notification this thread has terminated - update state
	 * and fire a terminate event.
	 */
	protected void terminated() 
	{
		setTerminated( true );
		setRunning( false );	
		fireTerminateEvent();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IState#getCurrentState()
	 */
	public int getCurrentState()
	{
		return IState.UNKNOWN;
	}

}
