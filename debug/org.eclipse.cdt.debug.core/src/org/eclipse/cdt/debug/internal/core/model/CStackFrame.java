/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.core.model.IResumeWithoutSignal;
import org.eclipse.cdt.debug.core.model.IStackFrameInfo;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.ISourceLocator;
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
						 			IRestart,
						 			IResumeWithoutSignal, 
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
	 * List of visible variable (includes arguments).
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
		setCDIStackFrame( cdiFrame );
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
				fVariables.add( new CModificationVariable( this, (ICDIVariable)it.next() ) );
			}
		}
		else if ( refreshVariables() )
		{
			updateVariables();
		}
		setRefreshVariables( false );
		return fVariables;
	}

	/**
	 * Incrementally updates this stack frame's variables.
	 * 
	 */
	protected void updateVariables() throws DebugException 
	{
		List locals = getAllCDIVariables();
		int index = 0;
		while( index < fVariables.size() )
		{
			CVariable local = (CVariable)fVariables.get( index );
			ICDIVariable var = findVariable( locals, local.getOriginalCDIVariable() );
			if ( var != null )
			{
				locals.remove( var );
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
			fVariables.add( new CModificationVariable( this, (ICDIVariable)newOnes.next() ) );
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
			ISourceLocator locator = ((CDebugTarget)getDebugTarget()).getSourceLocator();
			if ( locator != null && locator instanceof IAdaptable && 
				 ((IAdaptable)locator).getAdapter( ICSourceLocator.class ) != null )
				return ((ICSourceLocator)((IAdaptable)locator).getAdapter( ICSourceLocator.class )).getLineNumber( this );
			if ( getCDIStackFrame() != null && getCDIStackFrame().getLocation() != null )
				return getCDIStackFrame().getLocation().getLineNumber();
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
		return ((CDebugTarget)getDebugTarget()).getRegisterGroups();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasRegisterGroups()
	 */
	public boolean hasRegisterGroups() throws DebugException
	{
		return ((CDebugTarget)getDebugTarget()).getRegisterGroups().length > 0;
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
		if ( canStepInto() )
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
//			((CThread)getThread()).stepToFrame( this );
			getThread().stepOver(); // for now
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
/*
			List frames = ((CThread)getThread()).computeStackFrames();
			int index = frames.indexOf( this );
			if ( index >= 0 && index < frames.size() - 1 )
			{
				IStackFrame nextFrame = (IStackFrame)frames.get( index + 1 );
				((CThread)getThread()).stepToFrame( nextFrame );
			}
*/
			getThread().stepReturn(); // for now
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
		setRefreshVariables( true );
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
		if ( frameOne == null || frameTwo == null )
			return false;
		ICDILocation loc1 = frameOne.getLocation();
		ICDILocation loc2 = frameTwo.getLocation();
		if ( loc1 == null || loc2 == null )
			return false;
		if ( loc1.getFile() != null && loc1.getFile().length() > 0 &&
			 loc2.getFile() != null && loc2.getFile().length() > 0 &&
			 loc1.getFile().equals( loc2.getFile() ) )
			 
		{
			if ( loc1.getFunction() != null && loc1.getFunction().length() > 0 &&
				 loc2.getFunction() != null && loc2.getFunction().length() > 0 &&
				 loc1.getFunction().equals( loc2.getFunction() ) )
				return true;
		}
		if ( ( loc1.getFile() == null || loc1.getFile().length() < 1 ) &&
			 ( loc2.getFile() == null || loc2.getFile().length() < 1 ) )
			 
		{
			if ( loc1.getFunction() != null && loc1.getFunction().length() > 0 &&
				 loc2.getFunction() != null && loc2.getFunction().length() > 0 &&
				 loc1.getFunction().equals( loc2.getFunction() ) )
				return true;
		}
		if ( ( loc1.getFile() == null || loc1.getFile().length() < 1 ) &&
			 ( loc2.getFile() == null || loc2.getFile().length() < 1 ) &&
			 ( loc1.getFunction() == null || loc1.getFunction().length() < 1 ) &&
			 ( loc2.getFunction() == null || loc2.getFunction().length() < 1 ) )
		{
			if ( loc1.getAddress() == loc2.getAddress() )
				return true;
		}
		return  false;
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
		if ( adapter == CStackFrame.class )
		{
			return this;
		}
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
			targetRequestFailed( e.toString(), null );
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
			targetRequestFailed( e.toString(), null );
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
		fVariables = null;
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

	/*
	 * @see org.eclipse.cdt.debug.core.IStackFrameInfo#getArguments()
	 */
	public IVariable[] getArguments()
	{
		ArrayList list = new ArrayList();
		IVariable[] vars = new IVariable[0];
		try
		{
			vars = getVariables();
		}
		catch( DebugException e )
		{
			CDebugCorePlugin.log( e );
		}
		for ( int i = 0; i < vars.length; ++i )
		{
			if ( vars[i] instanceof CVariable &&
				 ((CVariable)vars[i]).getOriginalCDIVariable() instanceof ICDIArgument )
			{
				 list.add( vars[i] );
			}
		}
		return (IVariable[])list.toArray( new IVariable[list.size()] );
	}
	
	protected synchronized void preserve()
	{
		preserveVariables();
	}

	private void preserveVariables()
	{
		if ( fVariables == null )
			return;
		try
		{
			Iterator it = fVariables.iterator();
			while( it.hasNext() )
			{
				((CVariable)it.next()).setChanged( false );
			}
		}
		catch( DebugException e )
		{
			CDebugCorePlugin.log( e );
		}
	}
	
	protected ICDIVariable findVariable( List list, ICDIVariable var )
	{
		Iterator it = list.iterator();
		while( it.hasNext() )
		{
			ICDIVariable newVar = (ICDIVariable)it.next();
			if ( newVar.equals( var ) )
				return newVar;
		}
		return null;
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
	
	private void setRefreshVariables( boolean refresh )
	{
		fRefreshVariables = refresh;
	}
	
	private boolean refreshVariables()
	{
		return fRefreshVariables;
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
}
