/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.util;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

/**
 * Abstract implementation of <code>IStorageEditorInput</code>.
 */
abstract public class StorageEditorInput implements IStorageEditorInput {

	/**
	 * Storage associated with this editor input
	 */
	private IStorage fStorage;

	/**
	 * Constructs an editor input on the given storage
	 */
	public StorageEditorInput(IStorage storage) {
		fStorage = storage;
	}

	/**
	 * @see IStorageEditorInput#getStorage()
	 */
	@Override
	public IStorage getStorage() {
		return fStorage;
	}

	/**
	 * Set new storage. For subclasses only.
	 * @param storage
	 */
	protected void setStorage(IStorage storage) {
		assert storage != null;
		fStorage = storage;
	}

	/**
	 * @see IEditorInput#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/**
	 * @see IEditorInput#getName()
	 */
	@Override
	public String getName() {
		return getStorage().getName();
	}

	/**
	 * @see IEditorInput#getPersistable()
	 */
	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	/**
	 * @see IEditorInput#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return getStorage().getFullPath().toOSString();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		try {
			return object instanceof IStorageEditorInput
					&& getStorage().equals(((IStorageEditorInput) object).getStorage());
		} catch (CoreException e) {
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getStorage().hashCode();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

}
