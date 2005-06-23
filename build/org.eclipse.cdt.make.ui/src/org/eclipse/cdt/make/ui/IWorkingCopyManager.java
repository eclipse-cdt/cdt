/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.IEditorInput;

import org.eclipse.cdt.make.core.makefile.IMakefile;

/**
 * Interface for accessing working copies of <code>IMakefile</code>
 * objects. The original  unit is only given indirectly by means
 * of an <code>IEditorInput</code>. The life cycle is as follows:
 * <ul>
 * <li> <code>connect</code> creates and remembers a working copy of the 
 *    unit which is encoded in the given editor input</li>
 * <li> <code>getWorkingCopy</code> returns the working copy remembered on 
 *   <code>connect</code></li>
 * <li> <code>disconnect</code> destroys the working copy remembered on 
 *   <code>connect</code></li>
 * </ul>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * 
 */
public interface IWorkingCopyManager {
	
	/**
	 * Connects the given editor input to this manager. After calling
	 * this method, a working copy will be available for the compilation unit encoded
	 * in the given editor input (does nothing if there is no encoded compilation unit).
	 *
	 * @param input the editor input
	 * @exception CoreException if the working copy cannot be created for the 
	 *    unit
	 */
	void connect(IEditorInput input) throws CoreException;
	
	/**
	 * Disconnects the given editor input from this manager. After calling
	 * this method, a working copy for the compilation unit encoded
	 * in the given editor input will no longer be available. Does nothing if there
	 * is no encoded compilation unit, or if there is no remembered working copy for
	 * the compilation unit.
	 * 
	 * @param input the editor input
	 */
	void disconnect(IEditorInput input);
	
	/**
	 * Returns the working copy remembered for the compilation unit encoded in the
	 * given editor input.
	 *
	 * @param input the editor input
	 * @return the working copy of the compilation unit, or <code>null</code> if the
	 *   input does not encode an editor input, or if there is no remembered working
	 *   copy for this compilation unit
	 */
	IMakefile getWorkingCopy(IEditorInput input);
	
	/**
	 * Shuts down this working copy manager. All working copies still remembered
	 * by this manager are destroyed.
	 */
	void shutdown();
}
