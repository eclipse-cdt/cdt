/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.changes;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.text.edits.UndoEdit;

abstract class UndoTextChange extends AbstractTextChange {

	private UndoEdit fUndos;

	public UndoTextChange(String name, int changeKind, UndoEdit undos) {
		super(name, changeKind);
		fUndos= undos;
	}
	
	protected void addTextEdits(LocalTextEditProcessor editor) throws CoreException {
		editor.add(fUndos);
	}	
}

