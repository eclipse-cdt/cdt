/*******************************************************************************
 * Copyright (c) 2002, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import java.net.URI;

import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * CustomBufferFactory
 */
public class CustomBufferFactory implements IBufferFactory {
	/**
	 *
	 */
	public CustomBufferFactory() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.IBufferFactory#createBuffer(org.eclipse.cdt.core.model.IOpenable)
	 */
	@Override
	public IBuffer createBuffer(IOpenable owner) {
		if (owner instanceof IWorkingCopy) {

			IWorkingCopy unit = (IWorkingCopy) owner;
			ITranslationUnit original = unit.getOriginalElement();
			IResource resource = original.getResource();
			if (resource instanceof IFile) {
				IFile fFile = (IFile) resource;
				DocumentAdapter adapter = new DocumentAdapter(owner, fFile);
				return adapter;
			}

			// URI
			URI locationUri = original.getLocationURI();
			if (locationUri != null) {
				try {
					return new DocumentAdapter(owner, locationUri);
				} catch (CoreException exc) {
					CUIPlugin.log(exc);
				}
			}

		}
		return DocumentAdapter.NULL;
	}
}
