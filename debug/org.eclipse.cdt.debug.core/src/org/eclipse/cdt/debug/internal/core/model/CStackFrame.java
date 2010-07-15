/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import java.math.BigInteger;
import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIDisposable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteMoveInstructionPointer;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.model.ICGlobalVariable;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IMoveToAddress;
import org.eclipse.cdt.debug.core.model.IMoveToLine;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.core.model.IResumeAtAddress;
import org.eclipse.cdt.debug.core.model.IResumeAtLine;
import org.eclipse.cdt.debug.core.model.IResumeWithoutSignal;
import org.eclipse.cdt.debug.core.model.IRunToAddress;
import org.eclipse.cdt.debug.core.model.IRunToLine;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.core.CGlobalVariableManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * Proxy to a stack frame on the target.
 */
public class CStackFrame extends CDebugElement implements ICStackFrame, IRestart, IResumeWithoutSignal, IMoveToAddress, IMoveToLine, ICDIEventListener {

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
	 * List of watch expressions evaluating in the context of this frame.
	 */
	private List fExpressions;

	/**
	 * Need this flag to prevent evaluations on disposed frames. 
	 */
	private boolean fIsDisposed = false;

	/**
	 * Constructor for CStackFrame.
	 */
	public CStackFrame( CThread thread, ICDIStackFrame cdiFrame ) {
		super( (CDebugTarget)thread.getDebugTarget() );
		setCDIStackFrame( cdiFrame );
		setThread( thread );
		getCDISession().getEventManager().addEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getThread()
	 */
	public IThread getThread() {
		return fThread;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		if ( isDisposed() ) {
			return new IVariable[0];
		}
		ICGlobalVariable[] globals = getGlobals();
		List vars = getVariables0();
		List all = new ArrayList( globals.length + vars.size() );
		all.addAll( Arrays.asList( globals ) );
		all.addAll( vars );
		return (IVariable[])all.toArray( new IVariable[all.size()] );
	}

	protected synchronized List getVariables0() throws DebugException {
		if ( isDisposed() ) {
			return Collections.EMPTY_LIST;
		}
		CThread thread = (CThread)getThread();
		if ( thread.isSuspended() ) {
			if ( fVariables == null ) {			
				List vars = getAllCDIVariableObjects();
				fVariables = new ArrayList( vars.size() );
				Iterator it = vars.iterator();
				while( it.hasNext() ) {
					fVariables.add( CVariableFactory.createLocalVariable( this, (ICDIVariableDescriptor)it.next() ) );
				}
			}
			else if ( refreshVariables() ) {
				updateVariables();
			}
			setRefreshVariables( false );
		}
		return ( fVariables != null ) ? fVariables : Collections.EMPTY_LIST;
	}

	/**
	 * Incrementally updates this stack frame's variables.
	 */
	protected void updateVariables() throws DebugException {
		List locals = getAllCDIVariableObjects();
		Iterator<CVariable> it = fVariables.iterator();
		while (it.hasNext()) {
			CVariable var = it.next();
			ICDIVariableDescriptor varObject = findVariable(locals, var);
			if (varObject != null && !var.isDisposed())
				locals.remove(varObject);
			else {
				// ensure variable is unregistered from event listener
				var.dispose();
				it.remove();
			}
		}
		// add any new locals
		Iterator newOnes = locals.iterator();
		while( newOnes.hasNext() ) {
			fVariables.add( CVariableFactory.createLocalVariable( this, (ICDIVariableDescriptor)newOnes.next() ) );
		}
	}

	/**
	 * Sets the containing thread.
	 * 
	 * @param thread the containing thread
	 */
	protected void setThread( CThread thread ) {
		fThread = thread;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return ( isDisposed() ) ? false : (getVariables0().size() > 0 || getGlobals().length > 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getLineNumber()
	 */
	public int getLineNumber() throws DebugException {
		if ( isSuspended() ) {
			ISourceLocator locator = ((CDebugTarget)getDebugTarget()).getSourceLocator();
			if ( locator != null && locator instanceof IAdaptable && ((IAdaptable)locator).getAdapter( ICSourceLocator.class ) != null )
				return ((ICSourceLocator)((IAdaptable)locator).getAdapter( ICSourceLocator.class )).getLineNumber( this );
			
			final ICDIStackFrame cdiFrame = getCDIStackFrame();
			if ( cdiFrame != null && cdiFrame.getLocator() != null )
				return cdiFrame.getLocator().getLineNumber();
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharStart()
	 */
	public int getCharStart() throws DebugException {
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharEnd()
	 */
	public int getCharEnd() throws DebugException {
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getName()
	 */
	public String getName() throws DebugException {
		final ICDIStackFrame cdiFrame = getCDIStackFrame();
		if (cdiFrame == null) {
			return ""; //$NON-NLS-1$
		}

		ICDILocator locator = cdiFrame.getLocator();
		String func = ""; //$NON-NLS-1$
		String file = ""; //$NON-NLS-1$
		String line = ""; //$NON-NLS-1$
		if ( locator.getFunction() != null && locator.getFunction().trim().length() > 0 )
			func += locator.getFunction() + "() "; //$NON-NLS-1$
		if ( locator.getFile() != null && locator.getFile().trim().length() > 0 ) {
			file = locator.getFile();
			if ( locator.getLineNumber() != 0 ) {
				line = NumberFormat.getInstance().format( new Integer( locator.getLineNumber() ) );
			}
		}
		else {
			return func;
		}
		return MessageFormat.format( CoreModelMessages.getString( "CStackFrame.0" ), new String[]{ func, file, line } ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getRegisterGroups()
	 */
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return ( isDisposed() ) ? new IRegisterGroup[0] : ((CDebugTarget)getDebugTarget()).getRegisterGroups( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasRegisterGroups()
	 */
	public boolean hasRegisterGroups() throws DebugException {
		return ( isDisposed() ) ? false : ((CDebugTarget)getDebugTarget()).getRegisterGroups( this ).length > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents( ICDIEvent[] events ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		try {
			return exists() /*&& isTopStackFrame()*/ && getThread().canStepInto();
		}
		catch( DebugException e ) {
			logError( e );
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver() {
		try {
			return exists() && getThread().canStepOver();
		}
		catch( DebugException e ) {
			logError( e );
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		try {
			if ( !exists() ) {
				return false;
			}
			List frames = ((CThread)getThread()).computeStackFrames();
			if ( frames != null && !frames.isEmpty() ) {
				boolean bottomFrame = this.equals( frames.get( frames.size() - 1 ) );
				return !bottomFrame && getThread().canStepReturn();
			}
		}
		catch( DebugException e ) {
			logError( e );
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping() {
		return getThread().isStepping();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		if ( canStepInto() ) {
			getThread().stepInto();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		if ( canStepOver() ) {
			getThread().stepOver();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		if ( canStepReturn() ) {
			getThread().stepReturn();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return getThread().canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return getThread().canSuspend();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return getThread().isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		getThread().resume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		getThread().suspend();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		boolean exists = false;
		try {
			exists = exists();
		}
		catch( DebugException e ) {
			logError( e );
		}
		return exists && getThread().canTerminate() || getDebugTarget().canTerminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return getThread().isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		if ( getThread().canTerminate() ) {
			getThread().terminate();
		}
		else {
			getDebugTarget().terminate();
		}
	}

	/**
	 * Returns the underlying CDI stack frame that this model object is a proxy to.
	 * 
	 * @return the underlying CDI stack frame
	 */
	protected ICDIStackFrame getCDIStackFrame() {
		return fCDIStackFrame;
	}

	/**
	 * Sets the underlying CDI stack frame. Called by a thread when incrementally updating after a step has completed.
	 * 
	 * @param frame the underlying stack frame
	 */
	protected void setCDIStackFrame( ICDIStackFrame frame ) {
		if ( frame != null ) {
			fLastCDIStackFrame = frame;
		}
		else {
			fLastCDIStackFrame = fCDIStackFrame;
		}
		fCDIStackFrame = frame;
		setRefreshVariables( true );
	}

	/**
	 * The underlying stack frame that existed before the current underlying stack frame. Used only so that equality can be checked on stack frame after the new
	 * one has been set.
	 */
	protected ICDIStackFrame getLastCDIStackFrame() {
		return fLastCDIStackFrame;
	}

	/**
	 * Helper method for computeStackFrames(). For the purposes of detecting if an underlying stack frame needs to be disposed, stack frames are equal if the
	 * frames are equal and the locations are equal.
	 */
	protected static boolean equalFrame( ICDIStackFrame frameOne, ICDIStackFrame frameTwo ) {
		if ( frameOne == null || frameTwo == null )
			return false;
		ICDILocator loc1 = frameOne.getLocator();
		ICDILocator loc2 = frameTwo.getLocator();
		if ( loc1 == null || loc2 == null )
			return false;
		if ( loc1.getFile() != null && loc1.getFile().length() > 0 && loc2.getFile() != null && loc2.getFile().length() > 0 && loc1.getFile().equals( loc2.getFile() ) ) {
			if ( loc1.getFunction() != null && loc1.getFunction().length() > 0 && loc2.getFunction() != null && loc2.getFunction().length() > 0 && loc1.getFunction().equals( loc2.getFunction() ) )
				return true;
		}
		if ( (loc1.getFile() == null || loc1.getFile().length() < 1) && (loc2.getFile() == null || loc2.getFile().length() < 1) ) {
			if ( loc1.getFunction() != null && loc1.getFunction().length() > 0 && loc2.getFunction() != null && loc2.getFunction().length() > 0 && loc1.getFunction().equals( loc2.getFunction() ) )
				return true;
		}
		if ( (loc1.getFile() == null || loc1.getFile().length() < 1) && (loc2.getFile() == null || loc2.getFile().length() < 1) && (loc1.getFunction() == null || loc1.getFunction().length() < 1) && (loc2.getFunction() == null || loc2.getFunction().length() < 1) ) {
			if ( loc1.getAddress() == loc2.getAddress() )
				return true;
		}
		return false;
	}

	protected boolean exists() throws DebugException {
		return ((CThread)getThread()).computeStackFrames().indexOf( this ) != -1;
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( adapter == IRunToLine.class ) {
			return this;
		}
		if ( adapter == IRunToAddress.class ) {
			return this;
		}
		if ( adapter == IResumeAtLine.class ) {
			return this;
		}
		if ( adapter == IResumeAtAddress.class ) {
			return this;
		}
		if ( adapter == IMoveToLine.class ) {
			return this;
		}
		if ( adapter == IMoveToAddress.class ) {
			return this;
		}
		if ( adapter == CStackFrame.class ) {
			return this;
		}
		if ( adapter == ICStackFrame.class ) {
			return this;
		}
		if ( adapter == IStackFrame.class ) {
			return this;
		}
		if ( adapter == ICDIStackFrame.class ) {
			return getCDIStackFrame();
		}
		if ( adapter == IMemoryBlockRetrieval.class ) {
			return getDebugTarget().getAdapter( adapter );
		}
		return super.getAdapter( adapter );
	}

	protected void dispose() {
		setDisposed( true );
		getCDISession().getEventManager().removeEventListener( this );
		disposeAllVariables();
		disposeExpressions();

		final ICDIStackFrame cdiFrame = getCDIStackFrame();
		setCDIStackFrame(null);
		if (cdiFrame instanceof ICDIDisposable)  {
			((ICDIDisposable)cdiFrame).dispose();
		}
	}

	/**
	 * Retrieves local variables in this stack frame. Returns an empty list if there are no local variables.
	 *  
	 */
	protected List getCDILocalVariableObjects() throws DebugException {
		List list = new ArrayList();
		try {
			final ICDIStackFrame cdiFrame = getCDIStackFrame();
			if (cdiFrame != null) {
				list.addAll( Arrays.asList( cdiFrame.getLocalVariableDescriptors( ) ) );
			}
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), null );
		}
		return list;
	}

	/**
	 * Retrieves arguments in this stack frame. Returns an empty list if there are no arguments.
	 *  
	 */
	protected List getCDIArgumentObjects() throws DebugException {
		List list = new ArrayList();
		try {
			final ICDIStackFrame cdiFrame = getCDIStackFrame();
			if (cdiFrame != null) {
				list.addAll( Arrays.asList( cdiFrame.getArgumentDescriptors() ) );
			}
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), null );
		}
		return list;
	}

	protected List getAllCDIVariableObjects() throws DebugException {
		List list = new ArrayList();
		list.addAll( getCDIArgumentObjects() );
		list.addAll( getCDILocalVariableObjects() );
		return list;
	}

	protected boolean isTopStackFrame() throws DebugException {
		IStackFrame tos = getThread().getTopStackFrame();
		return tos != null && tos.equals( this );
	}

	protected void disposeAllVariables() {
		if ( fVariables == null )
			return;
		Iterator it = fVariables.iterator();
		while( it.hasNext() ) {
			((CVariable)it.next()).dispose();
		}
		fVariables.clear();
		fVariables = null;
	}

	protected void disposeExpressions() {
		if ( fExpressions != null ) {
			Iterator it = fExpressions.iterator();
			while( it.hasNext() ) {
				((CExpression)it.next()).dispose();
			}
			fExpressions.clear();
		}
		fExpressions = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICStackFrame#getAddress()
	 */
	public IAddress getAddress() {
		IAddressFactory factory = ((CDebugTarget)getDebugTarget()).getAddressFactory();
		final ICDIStackFrame cdiFrame = getCDIStackFrame();
		return cdiFrame != null ? factory.createAddress( cdiFrame.getLocator().getAddress() ) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICStackFrame#getFile()
	 */
	public String getFile() {
		final ICDIStackFrame cdiFrame = getCDIStackFrame();
		return cdiFrame != null ? cdiFrame.getLocator().getFile() : ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICStackFrame#getFunction()
	 */
	public String getFunction() {
		final ICDIStackFrame cdiFrame = getCDIStackFrame();
		return cdiFrame != null ? cdiFrame.getLocator().getFunction() : ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICStackFrame#getLevel()
	 */
	public int getLevel() {
		final ICDIStackFrame cdiFrame = getCDIStackFrame();
		return cdiFrame != null ? cdiFrame.getLevel() : -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICStackFrame#getFrameLineNumber()
	 */
	public int getFrameLineNumber() {
		final ICDIStackFrame cdiFrame = getCDIStackFrame();
		return cdiFrame != null ? cdiFrame.getLocator().getLineNumber() : -1;
	}

	protected synchronized void preserve() {
		preserveVariables();
		preserveExpressions();
	}

	private void preserveVariables() {
		if ( fVariables == null )
			return;
		Iterator it = fVariables.iterator();
		while( it.hasNext() ) {
			AbstractCVariable av = (AbstractCVariable)it.next();
			av.preserve();
		}
	}

	private void preserveExpressions() {
		if ( fExpressions == null )
			return;
		Iterator it = fExpressions.iterator();
		while( it.hasNext() ) {
			CExpression exp = (CExpression)it.next();
			exp.preserve();
		}
	}

	protected ICDIVariableDescriptor findVariable( List list, CVariable var ) {
		Iterator it = list.iterator();
		while( it.hasNext() ) {
			ICDIVariableDescriptor newVarObject = (ICDIVariableDescriptor)it.next();
			if ( var.sameVariable( newVarObject ) )
				return newVarObject;
		}
		return null;
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

	public void setRefreshVariables( boolean refresh ) {
		fRefreshVariables = refresh;
	}

	private boolean refreshVariables() {
		return fRefreshVariables;
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
	 * @see org.eclipse.cdt.debug.core.model.ICStackFrame#evaluateExpression(java.lang.String)
	 */
	public IValue evaluateExpression( String expressionText ) throws DebugException {
		if ( !isDisposed() ) {
			CExpression expression = getExpression( expressionText );
			if ( expression != null ) {
				return expression.getValue( this );
			}
		}
		return null;
	}

	private ICGlobalVariable[] getGlobals() {
		CGlobalVariableManager gvm = ((CDebugTarget)getDebugTarget()).getGlobalVariableManager();
		if ( gvm != null ) {
			return gvm.getGlobals();
		}
		return new ICGlobalVariable[0];
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		try {
			return getName();
		}
		catch( DebugException e ) {
			return e.getLocalizedMessage();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICStackFrame#evaluateExpressionToString(java.lang.String)
	 */
	public String evaluateExpressionToString( String expression ) throws DebugException {
		try {
			final ICDIStackFrame cdiFrame = getCDIStackFrame();
			if (cdiFrame != null) {
				return getCDITarget().evaluateExpressionToString( cdiFrame, expression );
			}
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), null );
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICStackFrame#canEvaluate()
	 */
	public boolean canEvaluate() {
		CDebugTarget target = ((CDebugTarget)getDebugTarget());
		return target.supportsExpressionEvaluation() && isSuspended();
	}

	protected void doStepReturn() throws DebugException {
		try {
			final ICDIStackFrame cdiFrame = getCDIStackFrame();
			if (cdiFrame != null) {
				cdiFrame.stepReturn();
			}
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), null );
		}
	}

	private synchronized CExpression getExpression( String expressionText ) throws DebugException {
		if ( isDisposed() ) {
			return null;
		}
		if ( fExpressions == null ) {
			fExpressions = new ArrayList( 5 );
		}
		CExpression expression = null;
		Iterator it = fExpressions.iterator();
		while( it.hasNext() ) {
			expression = (CExpression)it.next();
			if ( expression.getExpressionText().compareTo( expressionText ) == 0 ) {
				return expression;
			}
		}
		try {
			ICDIExpression cdiExpression = ((CDebugTarget)getDebugTarget()).getCDITarget().createExpression( expressionText );
			expression = new CExpression( this, cdiExpression, null );
			fExpressions.add( expression );
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), null );
		}
		return expression;
	}

	protected boolean isDisposed() {
		return fIsDisposed;
	}

	private synchronized void setDisposed( boolean isDisposed ) {
		fIsDisposed = isDisposed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRunToLine#canRunToLine(org.eclipse.core.resources.IFile, int)
	 */
	public boolean canRunToLine( IFile file, int lineNumber ) {
		return ((CThread)getThread()).canRunToLine( file, lineNumber );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRunToLine#runToLine(org.eclipse.core.resources.IFile, int, boolean)
	 */
	public void runToLine( IFile file, int lineNumber, boolean skipBreakpoints ) throws DebugException {
		if ( !canRunToLine( file, lineNumber ) )
			return;
		((CThread)getThread()).runToLine( file, lineNumber, skipBreakpoints );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRunToLine#canRunToLine(java.lang.String, int)
	 */
	public boolean canRunToLine( String fileName, int lineNumber ) {
		return ((CThread)getThread()).canRunToLine( fileName, lineNumber );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRunToLine#runToLine(java.lang.String, int, boolean)
	 */
	public void runToLine( String fileName, int lineNumber, boolean skipBreakpoints ) throws DebugException {
		if ( !canRunToLine( fileName, lineNumber ) )
			return;
		((CThread)getThread()).runToLine( fileName, lineNumber, skipBreakpoints );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRunToAddress#canRunToAddress(org.eclipse.cdt.core.IAddress)
	 */
	public boolean canRunToAddress( IAddress address ) {
		return getThread().canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IRunToAddress#runToAddress(org.eclipse.cdt.core.IAddress, boolean)
	 */
	public void runToAddress( IAddress address, boolean skipBreakpoints ) throws DebugException {
		if ( !canRunToAddress( address ) )
			return;
		if ( skipBreakpoints ) {
			((CDebugTarget)getDebugTarget()).skipBreakpoints( true );
		}
		ICDILocation location = getCDITarget().createAddressLocation( new BigInteger( address.toString() ) );
		try {
			getCDIThread().stepUntil( location );
		}
		catch( CDIException e ) {
			if ( skipBreakpoints ) {
				((CDebugTarget)getDebugTarget()).skipBreakpoints( false );
			}
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeAtLine#canResumeAtLine(org.eclipse.core.resources.IFile, int)
	 */
	public boolean canResumeAtLine( IFile file, int lineNumber ) {
		return getThread().canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeAtLine#resumeAtLine(org.eclipse.core.resources.IFile, int)
	 */
	public void resumeAtLine( IFile file, int lineNumber ) throws DebugException {
		if ( !canResumeAtLine( file, lineNumber ) )
			return;
		resumeAtLine( file.getLocation().lastSegment(), lineNumber );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeAtLine#canResumeAtLine(java.lang.String, int)
	 */
	public boolean canResumeAtLine( String fileName, int lineNumber ) {
		return getThread().canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeAtLine#resumeAtLine(java.lang.String, int)
	 */
	public void resumeAtLine( String fileName, int lineNumber ) throws DebugException {
		if ( !canResumeAtLine( fileName, lineNumber ) )
			return;
		
		ICDILocation location = getCDITarget().createLineLocation( fileName, lineNumber );
		try {
			ICDIExecuteResume resumer = getCDIThread();
			resumer.resume(location);
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeAtAddress#canResumeAtAddress(org.eclipse.cdt.core.IAddress)
	 */
	public boolean canResumeAtAddress( IAddress address ) {
		return getThread().canResume();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IResumeAtAddress#resumeAtAddress(org.eclipse.cdt.core.IAddress)
	 */
	public void resumeAtAddress( IAddress address ) throws DebugException {
		if ( !canResumeAtAddress( address ) )
			return;
		ICDILocation location = getCDITarget().createAddressLocation( new BigInteger( address.toString() ) );
		try {
			getCDIThread().resume( location );
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IMoveToAddress#canMoveToAddress(org.eclipse.cdt.core.IAddress)
	 */
	public boolean canMoveToAddress(IAddress address) {
		return getThread().isSuspended() && (getCDIThread() instanceof ICDIExecuteMoveInstructionPointer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IMoveToAddress#moveToAddress(org.eclipse.cdt.core.IAddress)
	 */
	public void moveToAddress(IAddress address) throws DebugException {
		if ( !canMoveToAddress( address ) )
			return;
		ICDILocation location = getCDITarget().createAddressLocation( new BigInteger( address.toString() ) );
		ICDIExecuteMoveInstructionPointer mover = (ICDIExecuteMoveInstructionPointer)getCDIThread();
		try {
			mover.moveInstructionPointer( location);
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IMoveToLine#canMoveToLine(java.lang.String, int)
	 */
	public boolean canMoveToLine(String fileName, int lineNumber) {
		return getThread().isSuspended() && (getCDIThread() instanceof ICDIExecuteMoveInstructionPointer);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IMoveToLine#moveToLine(java.lang.String, int)
	 */
	public void moveToLine(String fileName, int lineNumber) throws DebugException {
		if ( !canMoveToLine( fileName, lineNumber ) )
			return;
		ICDILocation location= getCDITarget().createLineLocation( fileName, lineNumber );
		ICDIExecuteMoveInstructionPointer mover = (ICDIExecuteMoveInstructionPointer)getCDIThread();
		try {
			mover.moveInstructionPointer( location );
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), e );
		}
	}

	private ICDIThread getCDIThread() {
		return ((CThread)getThread()).getCDIThread();
	}
}
