/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
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

