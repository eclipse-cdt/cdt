/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

/**
 * A ModificationCollector can be passed through a refactoring and manages the rewriters
 * and additional changes a refactoring can create.
 *
 * @author Mirko Stocker
 */
public class ModificationCollector {
	// Each translation unit can have only one ASTRewrite
	private final Map<IASTTranslationUnit, ASTRewrite> rewriters =
			new HashMap<IASTTranslationUnit, ASTRewrite>();

	private Collection<CreateFileChange> changes;

	public ASTRewrite rewriterForTranslationUnit(IASTTranslationUnit ast) {
		if (!rewriters.containsKey(ast)) {
			rewriters.put(ast, ASTRewrite.create(ast));
		}
		return rewriters.get(ast);
	}

	// Creating new files doesn't concern the rewriter, the refactorings can add them here as needed.
	public void addFileChange(CreateFileChange change) {
		if (changes == null) {
			changes = new ArrayList<CreateFileChange>();
		}
		changes.add(change);
	}

	public CCompositeChange createFinalChange() {
		// Synthetic changes aren't displayed and therefore don't need a name
		CCompositeChange result = new CCompositeChange(""); //$NON-NLS-1$
		result.markAsSynthetic();

		if (changes != null) {
			for (Change change : changes) {
				addFlattened(change, result);
			}
		}

		for (ASTRewrite each : rewriters.values()) {
			Change change = each.rewriteAST();
			addFlattened(change, result);
		}

		return result;
	}

	/**
	 * If {@code change} is a CompositeChange, merges it into the {@code receiver}, otherwise
	 * adds it to the {@code receiver}.
	 * @param change The change being added.
	 * @param receiver The composite change that receives the addition.
	 */
	private void addFlattened(Change change, CompositeChange receiver) {
		if (change instanceof CompositeChange) {
			receiver.merge((CompositeChange) change);
		} else {
			receiver.add(change);
		}
	}
}
