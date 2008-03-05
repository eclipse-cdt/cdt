/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;

/**
 * Query for searching the index based on a text selection.
 */
public class PDOMSearchTextSelectionQuery extends PDOMSearchQuery {

	private ITranslationUnit tu;
	private ITextSelection selection;
	
	public PDOMSearchTextSelectionQuery(ICElement[] scope, ITranslationUnit tu, ITextSelection selection, int flags) {
		super(scope, flags | IIndex.SEARCH_ACCROSS_LANGUAGE_BOUNDARIES);
		this.tu = tu;
		this.selection = selection;
	}

	@Override
	protected IStatus runWithIndex(final IIndex index, IProgressMonitor monitor) {
		return ASTProvider.getASTProvider().runOnAST(tu, ASTProvider.WAIT_YES, monitor, new ASTRunnable() {
			public IStatus runOnAST(ILanguage language, IASTTranslationUnit ast) throws CoreException {
				if (ast != null) {
					IASTName searchName= ast.getNodeSelector(null).findEnclosingName(selection.getOffset(), selection.getLength());
					if (searchName != null) {
						IBinding binding = searchName.resolveBinding();
						if (binding != null)
							createMatches(index, binding);
					}
				}
				return Status.OK_STATUS;
			}
		});
	}

	@Override
	public String getLabel() {
		return super.getLabel() + " " + selection.getText(); //$NON-NLS-1$
	}
}
