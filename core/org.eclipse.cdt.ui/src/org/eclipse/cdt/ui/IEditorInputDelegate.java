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

package org.eclipse.cdt.ui;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;

/**
 * This interface allows to create flexible editor inputs.
 * 
 * @since May 21, 2003
 */
public interface IEditorInputDelegate extends IEditorInput {
	/**
	 * Returns the editor input delegate for this editor input.
	 * 
	 * @return editor input delegate
	 */
	IEditorInput getDelegate();

	/**
	 * Returns the storage associated with this editor input.
	 * 
	 * @return stirage associated with this editor input
	 * @throws CoreException on failure. Reasons include:
	 */
	IStorage getStorage() throws CoreException;
}
