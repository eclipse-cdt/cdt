/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICSession;
import org.eclipse.cdt.debug.core.cdi.model.ICExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICObject;
import org.eclipse.cdt.debug.core.cdi.model.ICRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICSharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICTarget;
import org.eclipse.cdt.debug.core.cdi.model.ICThread;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CTarget extends SessionObject implements ICTarget {

	public CTarget(Session session) {
		super(session);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#disconnect()
	 */
	public void disconnect() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#evaluateExpression(ICExpression)
	 */
	public void evaluateExpression(ICExpression expression)
		throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#evaluateExpression(String)
	 */
	public ICExpression evaluateExpression(String expressionText)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#finish()
	 */
	public void finish() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getCMemoryBlock(long, long)
	 */
	public ICMemoryBlock getCMemoryBlock(long startAddress, long length)
		throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getErrorStream()
	 */
	public InputStream getErrorStream() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getGlobalVariables()
	 */
	public ICGlobalVariable[] getGlobalVariables() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getInputStream()
	 */
	public InputStream getInputStream() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getRegisterGroups()
	 */
	public ICRegisterGroup[] getRegisterGroups() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getSharedLibraries()
	 */
	public ICSharedLibrary[] getSharedLibraries() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getThread(String)
	 */
	public ICThread getThread(String id) throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#getThreads()
	 */
	public ICThread[] getThreads() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#isDisconnected()
	 */
	public boolean isDisconnected() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#isStepping()
	 */
	public boolean isStepping() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#isSuspended()
	 */
	public boolean isSuspended() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#isTerminated()
	 */
	public boolean isTerminated() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#restart()
	 */
	public void restart() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#resume()
	 */
	public void resume() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#stepInto()
	 */
	public void stepInto() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#stepIntoInstruction()
	 */
	public void stepIntoInstruction() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#stepOver()
	 */
	public void stepOver() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#stepOverInstruction()
	 */
	public void stepOverInstruction() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#suspend()
	 */
	public void suspend() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICTarget#terminate()
	 */
	public void terminate() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICObject#getCDITarget()
	 */
	public ICTarget getCDITarget() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICObject#getId()
	 */
	public String getId() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICObject#getParent()
	 */
	public ICObject getParent() {
		return null;
	}

}
