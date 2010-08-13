/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ISourceRange;

import org.eclipse.cdt.internal.core.model.ext.SourceRange;

import org.eclipse.cdt.internal.ui.editor.SelectionHistory;

public class StructureSelectHistoryAction extends StructureSelectionAction {

	public static final String PREFIX = "StructureSelectHistory."; //$NON-NLS-1$

	public StructureSelectHistoryAction(ResourceBundle bundle, ITextEditor editor,
			SelectionHistory history) {
		super(bundle, PREFIX, editor, history);
	}

	@Override
	protected ISourceRange doExpand(IASTTranslationUnit ast, SourceRange currentSourceRange) {
		if (!history.isEmpty()) {
			return history.getLast();
		}
		return null;
	}

}
