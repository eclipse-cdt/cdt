/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.ui;

import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.ui.IEditorInput;

/**
 * Extension interface for <code>IWorkingCopyManager</code>.
 * @since 2.1
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkingCopyManagerExtension {

	/**
	 * Sets the given working copy for the given editor input. If the given editor input
	 * is not connected to this working copy manager, this call has no effect. <p>
	 * This working copy manager does not assume the ownership of this working copy, i.e.,
	 * the given working copy is not automatically be freed when this manager is shut down.
	 *
	 * @param input the editor input
	 * @param workingCopy the working copy
	 */
	void setWorkingCopy(IEditorInput input, IMakefile workingCopy);

	/**
	 * Removes the working copy set for the given editor input. If there is no
	 * working copy set for this input or this input is not connected to this
	 * working copy manager, this call has no effect.
	 *
	 * @param input the editor input
	 */
	void removeWorkingCopy(IEditorInput input);
}
