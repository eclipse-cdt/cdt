/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.util.StorageEditorInput;
import org.eclipse.core.resources.IStorage;

/**
 * SourceEditorInput
 */
public class SourceEditorInput extends StorageEditorInput {

	/**
	 * @param storage
	 */
	public SourceEditorInput(IStorage storage) {
		super(storage);
	}

	/*
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	@Override
	public boolean exists() {
		return false;
	}

}
