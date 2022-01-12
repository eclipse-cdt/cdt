/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.internal.core.model.ext.SourceRange;
import org.eclipse.cdt.internal.ui.editor.SelectionHistory;
import org.eclipse.ui.texteditor.ITextEditor;

public class StructureSelectHistoryAction extends StructureSelectionAction {

	public static final String PREFIX = "StructureSelectHistory."; //$NON-NLS-1$

	public StructureSelectHistoryAction(ResourceBundle bundle, ITextEditor editor, SelectionHistory history) {
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
