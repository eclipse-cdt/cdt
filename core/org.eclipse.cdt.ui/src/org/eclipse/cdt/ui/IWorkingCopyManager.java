/*******************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;

/**
 * Interface for accessing working copies of {@code ITranslationUnit}
 * objects. The original translation unit is only given indirectly by means
 * of an {@code IEditorInput}. The life cycle is as follows:
 * <ul>
 * <li> {@code connect} creates and remembers a working copy of the
 *     translation unit which is encoded in the given editor input</li>
 * <li> {@code getWorkingCopy} returns the working copy remembered on
 *     {@code connect}</li>
 * <li> {@code disconnect} destroys the working copy remembered on
 *     {@code connect}</li>
 * </ul>
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 *
 * @see CDTUITools#getWorkingCopyManager
 */
public interface IWorkingCopyManager {
	/**
	 * Connects the given editor input to this manager. After calling
	 * this method, a working copy will be available for the translation unit encoded
	 * in the given editor input (does nothing if there is no encoded translation unit).
	 *
	 * @param input the editor input
	 * @exception CoreException if the working copy cannot be created for the
	 *     translation unit
	 */
	void connect(IEditorInput input) throws CoreException;

	/**
	 * Disconnects the given editor input from this manager. After calling
	 * this method, a working copy for the translation unit encoded
	 * in the given editor input will no longer be available. Does nothing if there
	 * is no encoded translation unit, or if there is no remembered working copy for
	 * the translation unit.
	 *
	 * @param input the editor input
	 */
	void disconnect(IEditorInput input);

	/**
	 * Returns the working copy remembered for the translation unit encoded in
	 * the given editor input.
	 *
	 * @param input the editor input
	 * @return the working copy of the translation unit, or {@code null} if the
	 *     input does not encode an editor input, or if there is no remembered working
	 *     copy for this translation unit
	 */
	IWorkingCopy getWorkingCopy(IEditorInput input);

	/**
	 * Shuts down this working copy manager. All working copies still remembered
	 * by this manager is destroyed.
	 */
	void shutdown();

	/**
	 * Returns a shared working copy for the given translation unit. If necessary, a new
	 * working copy will be created.
	 *
	 * @param tu a translation unit
	 * @param requestor call back interface for reporting problems, may be {@code null}.
	 * @param monitor a monitor to report progress
	 * @since 5.2
	 */
	IWorkingCopy getSharedWorkingCopy(ITranslationUnit tu, IProblemRequestor requestor, IProgressMonitor monitor)
			throws CModelException;

	/**
	 * Returns all shared working copies, currently available.
	 * @since 5.2
	 */
	IWorkingCopy[] getSharedWorkingCopies();

	/**
	 * Returns the shared working copy for the given translation unit, if it exists,
	 * or {@code null}, otherwise.
	 * @since 5.2
	 */
	IWorkingCopy findSharedWorkingCopy(ITranslationUnit tu);
}
