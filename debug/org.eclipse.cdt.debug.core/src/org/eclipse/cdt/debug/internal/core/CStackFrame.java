/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.IStackFrameInfo;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
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
						 			IStackFrameInfo, 
						 			ICDIEventListener
{
	/**
	 * Underlying CDI stack frame.
	 */
	private ICDIStackFrame fCDIStackFrame;

	/**
	 * The last (previous) CDI stack frame.
	 */
	private ICDIStackFrame fLastCDIStackFrame;

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
	public CStackFrame( CThread thread, ICDIStackFrame cdiFrame )
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
			List vars = getAllCDIVariables();
			fVariables = new ArrayList( vars.size() );
			Iterator it = vars.iterator();
			while( it.hasNext() )
			{
				fVariables.add( new CLocalVariable( this, (ICDIVariable)it.next() ) );
			}
		}
		else if ( fRefreshVariables )
		{
			updateVariables();
		}
		fRefreshVariables = false;
		return fVariables;
	}

	/**
	 * Incrementally updates this stack frame's variables.
	 * 
	 */
	protected void updateVariables() throws DebugException 
	{
		List locals = null;
		locals = getAllCDIVariables();
		int localIndex = -1;
		int index = 0;
		while( index < fVariables.size() )
		{
			CLocalVariable local = (CLocalVariable)fVariables.get( index );
			localIndex = locals.indexOf( local.getCDIVariable() );
			if ( localIndex >= 0 )
			{
				// update variable with new underling CDI LocalVariable
				locals.remove( localIndex );
				index++;
			}
			else
			{
				// remove variable
				fVariables.remove( index );
			}
		}

		// add any new locals
		Iterator newOnes = locals.iterator();
		while( newOnes.hasNext() )
		{
			fVariables.add( new CLocalVariable( this, (ICDIVariable)newOnes.next() ) );
		}
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
		if ( isSuspended() )
		{
			ICDILocation location = getCDIStackFrame().getLocation();
			if ( location != null )
			{
				return location.getLineNumber();
			}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharStart()
	 */
	public int getCharStart() throws DebugException
	{
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharEnd()
	 */
	public int getCharEnd() throws DebugException
	{
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getName()
	 */
	public String getName() throws DebugException
	{
		ICDILocation location = getCDIStackFrame().getLocation();
		String name = new String();
		if ( location.getFunction() != null )
			name += location.getFunction() + "() ";
		if ( location.getFile() != null )
			name += "at " + location.getFile() + ":" ;
		if ( location.getLineNumber() != 0 )
			name += location.getLineNumber();
			
		return name.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getRegisterGroups()
	 */
	public IRegisterGroup[] getRegisterGroups() throws DebugException
	{
		return new IRegisterGroup[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasRegisterGroups()
	 */
	public boolean hasRegisterGroups() throws DebugException
	{
		return getRegisterGroups().length > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvent(ICDIEvent)
	 */
	public void handleDebugEvent( ICDIEvent event )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto()
	{
		try
		{
			return exists() && isTopStackFrame() && getThread().canStepInto();
		}
		catch( DebugException e )
		{
			logError( e );
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver()
	{
		try
		{
			return exists() && getThread().canStepOver();
		}
		catch( DebugException e )
		{
			logError( e );
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn()
	{
		try
		{
			if ( !exists() )
			{
				return false;
			}
			List frames = ((CThread)getThread()).computeStackFrames();
			if ( frames != null && !frames.isEmpty() )
			{
				boolean bottomFrame = this.equals( frames.get( frames.size() - 1 ) );
				boolean aboveObsoleteFrame = false;
				return !bottomFrame && getThread().canStepReturn();
			}
		}
		catch( DebugException e )
		{
			logError( e );
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping()
	{
		return getThread().isStepping();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException
	{
		if ( !canStepInto() )
		{
			getThread().stepInto();
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
		if ( isTopStackFrame() )
		{
			getThread().stepOver();
		}
		else
		{
			((CThread)getThread()).stepToFrame( this );
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
		if ( isTopStackFrame() )
		{
			getThread().stepReturn();
		}
		else
		{
			List frames = ((CThread)getThread()).computeStackFrames();
			int index = frames.indexOf( this );
			if ( index >= 0 && index < frames.size() - 1 )
			{
				IStackFrame nextFrame = (IStackFrame)frames.get( index + 1 );
				((CThread)getThread()).stepToFrame( nextFrame );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume()
	{
		return getThread().canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend()
	{
		return getThread().canSuspend();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended()
	{
		return getThread().isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException
	{
		getThread().resume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException
	{
		getThread().suspend();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate()
	{
		boolean exists = false;
		try
		{
			exists = exists();
		}
		catch( DebugException e )
		{
			logError( e );
		}
		return exists && getThread().canTerminate() || getDebugTarget().canTerminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated()
	{
		return getThread().isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException
	{
		if ( getThread().canTerminate() )
		{
			getThread().terminate();
		}
		else
		{
			getDebugTarget().terminate();
		}
	}

	/**
	 * Returns the underlying CDI stack frame that this model object is 
	 * a proxy to.
	 * 
	 * @return the underlying CDI stack frame
	 */
	protected ICDIStackFrame getCDIStackFrame()
	{
		return fCDIStackFrame;
	}

	/**
	 * Sets the underlying CDI stack frame. Called by a thread
	 * when incrementally updating after a step has completed.
	 * 
	 * @param frame the underlying stack frame
	 */
	protected void setCDIStackFrame( ICDIStackFrame frame ) 
	{
		if ( frame != null )
		{
			fLastCDIStackFrame = frame;
		}
		else
		{
			fLastCDIStackFrame = fCDIStackFrame;
		}
		fCDIStackFrame = frame;
		fRefreshVariables = true;
	}

	/**
	 * The underlying stack frame that existed before the current underlying
	 * stack frame.  Used only so that equality can be checked on stack frame
	 * after the new one has been set.
	 */
	protected ICDIStackFrame getLastCDIStackFrame()
	{
		return fLastCDIStackFrame;
	}

	/**
	 * Helper method for computeStackFrames(). For the purposes of detecting if
	 * an underlying stack frame needs to be disposed, stack frames are equal if
	 * the frames are equal and the locations are equal.
	 */
	protected static boolean equalFrame( ICDIStackFrame frameOne, ICDIStackFrame frameTwo )
	{
		return ( frameOne != null && frameTwo != null && frameOne.getLocation().equals( frameTwo.getLocation() ) );
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
		if ( adapter == ICDIStackFrame.class )
		{
			return getCDIStackFrame();
		}
		if ( adapter == IStackFrameInfo.class )
		{
			return this;
		}
		return super.getAdapter( adapter );
	}
	
	protected void dispose()
	{
		getCDISession().getEventManager().removeEventListener( this );
		disposeAllVariables();
	}
	
	/**
	 * Retrieves local variables in this stack frame. Returns an empty 
	 * list if there are no local variables.
	 * 
	 */
	protected List getCDILocalVariables() throws DebugException
	{
		List list = new ArrayList();
		try
		{
			list.addAll( Arrays.asList( getCDIStackFrame().getLocalVariables() ) );
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
		List list = new ArrayList();
		try
		{
			list.addAll( Arrays.asList( getCDIStackFrame().getArguments() ) );
		}
		catch( CDIException e )
		{
			targetRequestFailed( MessageFormat.format( "{0} occurred retrieving arguments", new String[] { e.toString() } ), e );
		}
		return list;
	}
	
	protected List getAllCDIVariables() throws DebugException
	{
		List list = new ArrayList();
		list.addAll( getCDIArguments() );
		list.addAll( getCDILocalVariables() );
		return list;
	} 

	protected boolean isTopStackFrame() throws DebugException
	{
		IStackFrame tos = getThread().getTopStackFrame();
		return tos != null && tos.equals( this );
	}
	
	protected void disposeAllVariables()
	{
		if ( fVariables == null )
			return;
		Iterator it = fVariables.iterator();
		while( it.hasNext() )
		{
			((CVariable)it.next()).dispose();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IStackFrameInfo#getAddress()
	 */
	public long getAddress()
	{
		return getCDIStackFrame().getLocation().getAddress();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IStackFrameInfo#getFile()
	 */
	public String getFile()
	{
		return getCDIStackFrame().getLocation().getFile();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IStackFrameInfo#getFunction()
	 */
	public String getFunction()
	{
		return getCDIStackFrame().getLocation().getFunction();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IStackFrameInfo#getLevel()
	 */
	public int getLevel()
	{
		return getCDIStackFrame().getLevel();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IStackFrameInfo#getFrameLineNumber()
	 */
	public int getFrameLineNumber()
	{
		return getCDIStackFrame().getLocation().getLineNumber();
	}
}
