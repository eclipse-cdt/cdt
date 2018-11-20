/*******************************************************************************
 * Copyright (c) 2006, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

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
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;

/**
 * Query for searching the index based on a text selection.
 */
public class CSearchTextSelectionQuery extends CSearchQuery {
	private ITranslationUnit tu;
	private ITextSelection selection;
	private String label;

	public CSearchTextSelectionQuery(ICElement[] scope, ITranslationUnit tu, ITextSelection selection, int flags) {
		super(scope, flags | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		this.tu = tu;
		this.selection = selection;
		this.label = selection.getText();
	}

	@Override
	protected IStatus runWithIndex(final IIndex index, IProgressMonitor monitor) {
		return ASTProvider.getASTProvider().runOnAST(tu, ASTProvider.WAIT_ACTIVE_ONLY, monitor, new ASTRunnable() {
			@Override
			public IStatus runOnAST(ILanguage language, IASTTranslationUnit ast) throws CoreException {
				if (ast != null) {
					IASTName searchName = ast.getNodeSelector(null).findEnclosingName(selection.getOffset(),
							selection.getLength());
					if (searchName != null) {
						try {
							CPPSemantics.pushLookupPoint(searchName);
							label = searchName.toString();
							IBinding binding = searchName.resolveBinding();
							if (!(binding instanceof IProblemBinding)) {
								if (binding != null) {
									IScope scope = null;
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
								binding = CPPTemplates.findDeclarationForSpecialization(binding);
								if (binding != null) {
									label = labelForBinding(index, binding, label);
									createMatches(index, binding);
									return Status.OK_STATUS;
								}
							}
						} finally {
							CPPSemantics.popLookupPoint();
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
