/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;

/**
 * Query for searching the index based on a text selection.
 */
public class PDOMSearchTextSelectionQuery extends PDOMSearchQuery {
	private ITranslationUnit tu;
	private ITextSelection selection;
	private String label;
	
	public PDOMSearchTextSelectionQuery(ICElement[] scope, ITranslationUnit tu, ITextSelection selection, int flags) {
		super(scope, flags | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		this.tu = tu;
		this.selection = selection;
		this.label= selection.getText();
	}

	@Override
	protected IStatus runWithIndex(final IIndex index, IProgressMonitor monitor) {
		return ASTProvider.getASTProvider().runOnAST(tu, ASTProvider.WAIT_ACTIVE_ONLY, monitor, new ASTRunnable() {
			public IStatus runOnAST(ILanguage language, IASTTranslationUnit ast) throws CoreException {
				if (ast != null) {
					IASTName searchName= ast.getNodeSelector(null).findEnclosingName(selection.getOffset(), selection.getLength());
					if (searchName != null) {
						label= searchName.toString();
						IBinding binding= searchName.resolveBinding();
						if (binding instanceof IProblemBinding == false) {
							if (binding != null) {
								IScope scope= null;
								try {
									scope = binding.getScope();
								} catch (DOMException e) {
								}
								if (scope != null && scope.getKind() == EScopeKind.eLocal) {
									createLocalMatches(ast, binding);
									return Status.OK_STATUS;
								}
							}
							binding = index.findBinding(searchName);
							binding= CPPTemplates.findDeclarationForSpecialization(binding);
							if (binding != null) {
								label= labelForBinding(index, binding, label);
								createMatches(index, binding);
								return Status.OK_STATUS;
							}
						}
					}
				}
				return Status.OK_STATUS;
			}
		});
	}

	@Override
	public String getResultLabel(int numMatches) {
		return getResultLabel(label, numMatches);
	}
}
