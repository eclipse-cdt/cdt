/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICVariable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

/**
 * 
 * Proxy to a stack frame on the target.
 * 
 * @since Aug 7, 2002
 */
public class CStackFrame extends CDebugElement
						 implements IStackFrame, 
						 			ICEventListener
{
	/**
	 * Underlying CDI stack frame.
	 */
	private ICStackFrame fCDIStackFrame;

	/**
	 * Containing thread.
	 */
	private CThread fThread;

	/**
	 * Visible variables.
	 */
	private List fVariables;

	/**
	 * Whether the variables need refreshing
	 */
	private boolean fRefreshVariables = true;

	/**
	 * Constructor for CStackFrame.
	 * @param target
	 */
	public CStackFrame( CThread thread, ICStackFrame cdiFrame )
	{
		super( (CDebugTarget)thread.getDebugTarget() );
		fCDIStackFrame = cdiFrame;
		setThread( thread );
		getCDISession().getEventManager().addEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getThread()
	 */
	public IThread getThread()
	{
		return fThread;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException
	{
		List list = getVariables0();
		return (IVariable[])list.toArray( new IVariable[list.size()] );
	}

	protected synchronized List getVariables0() throws DebugException 
	{
		if ( fVariables == null )
		{
			fVariables = new ArrayList();
			fVariables.addAll( getCDIArguments() );
			fVariables.addAll( getCDILocalVariables() );
		}
		else if ( fRefreshVariables )
		{
			updateVariables();
		}
		fRefreshVariables = false;
		return fVariables;
	}

	/**
	 * Incrementally updates this stack frames variables.
	 * 
	 */
	protected void updateVariables() throws DebugException 
	{
	}

	/**
	 * Sets the containing thread.
	 * 
	 * @param thread the containing thread
	 */
	protected void setThread( CThread thread )
	{
		fThread = thread;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasVariables()
	 */
	public boolean hasVariables() throws DebugException
	{
		return getVariables0().size() > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getLineNumber()
	 */
	public int getLineNumber() throws DebugException
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharStart()
	 */
	public int getCharStart() throws DebugException
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharEnd()
	 */
	public int getCharEnd() throws DebugException
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getName()
	 */
	public String getName() throws DebugException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getRegisterGroups()
	 */
	public IRegisterGroup[] getRegisterGroups() throws DebugException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasRegisterGroups()
	 */
	public boolean hasRegisterGroups() throws DebugException
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICEventListener#handleDebugEvent(ICEvent)
	 */
	public void handleDebugEvent(ICEvent event)
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
	 * Returns the underlying CDI stack frame that this model object is 
	 * a proxy to.
	 * 
	 * @return the underlying CDI stack frame
	 */
	protected ICStackFrame getCDIStackFrame()
	{
		return fCDIStackFrame;
	}

	/**
	 * Sets the underlying CDI stack frame. Called by a thread
	 * when incrementally updating after a step has completed.
	 * 
	 * @param frame the underlying stack frame
	 */
	protected void setCDIStackFrame( ICStackFrame frame ) 
	{
	}

	/**
	 * The underlying stack frame that existed before the current underlying
	 * stack frame.  Used only so that equality can be checked on stack frame
	 * after the new one has been set.
	 */
	protected ICStackFrame getLastCDIStackFrame()
	{
		return null;
	}

	/**
	 * Helper method for computeStackFrames(). For the purposes of detecting if
	 * an underlying stack frame needs to be disposed, stack frames are equal if
	 * the frames are equal and the locations are equal.
	 */
	protected static boolean equalFrame( ICStackFrame frameOne, ICStackFrame frameTwo )
	{
		return false;
	}

	protected boolean exists() throws DebugException
	{
		return ((CThread)getThread()).computeStackFrames().indexOf( this ) != -1;
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter == IStackFrame.class )
		{
			return this;
		}
		if ( adapter == ICStackFrame.class )
		{
			return getCDIStackFrame();
		}
		return super.getAdapter( adapter );
	}
	
	protected void dispose()
	{
		getCDISession().getEventManager().removeEventListener( this );
	}
	
	/**
	 * Retrieves local variables in this stack frame. Returns an empty 
	 * list if there are no local variables.
	 * 
	 */
	protected List getCDILocalVariables() throws DebugException
	{
		List list = Collections.EMPTY_LIST;
		try
		{
			ICVariable[] vars = getCDIStackFrame().getLocalVariables();
			list = new ArrayList( vars.length );
			for ( int i = 0; i < vars.length; ++i )
				list.add( new CLocalVariable( this, vars[i] ) );
		}
		catch( CDIException e )
		{
			targetRequestFailed( MessageFormat.format( "{0} occurred retrieving local variables", new String[] { e.toString() } ), e );
		}
		return list;
	} 
	
	/**
	 * Retrieves arguments in this stack frame. Returns an empty list 
	 * if there are no arguments.
	 * 
	 */
	protected List getCDIArguments() throws DebugException
	{
		List list = Collections.EMPTY_LIST;
		try
		{
			ICArgument[] args = getCDIStackFrame().getArguments();
			list = new ArrayList( args.length );
			for ( int i = 0; i < args.length; ++i )
				list.add( new CLocalVariable( this, args[i] ) );
		}
		catch( CDIException e )
		{
			targetRequestFailed( MessageFormat.format( "{0} occurred retrieving arguments", new String[] { e.toString() } ), e );
		}
		return list;
	} 
}
