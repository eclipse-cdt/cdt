/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

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
