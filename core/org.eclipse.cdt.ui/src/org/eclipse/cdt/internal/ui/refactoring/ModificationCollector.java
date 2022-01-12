/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.ui.refactoring.changes.CCompositeChange;
import org.eclipse.cdt.internal.ui.refactoring.changes.CreateFileChange;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

/**
 * A ModificationCollector can be passed through a refactoring and manages the rewriters
 * and additional changes a refactoring can create.
 *
 * @author Mirko Stocker
 */
public class ModificationCollector {
	private final IResourceChangeDescriptionFactory deltaFactory;

	// Each translation unit can have only one ASTRewrite.
	private final Map<IASTTranslationUnit, ASTRewrite> rewriters = new HashMap<>();

	private Collection<CreateFileChange> changes;

	public ModificationCollector() {
		this(null);
	}

	public ModificationCollector(IResourceChangeDescriptionFactory deltaFactory) {
		this.deltaFactory = deltaFactory;
	}

	public ASTRewrite rewriterForTranslationUnit(IASTTranslationUnit ast) {
		if (!rewriters.containsKey(ast)) {
			rewriters.put(ast, ASTRewrite.create(ast));
			if (deltaFactory != null)
				deltaFactory.change((IFile) ast.getOriginatingTranslationUnit().getResource());
		}
		return rewriters.get(ast);
	}

	// Creating new files doesn't concern the rewriter, the refactorings can add them here as needed.
	public void addFileChange(CreateFileChange change) {
		if (changes == null) {
			changes = new ArrayList<>();
		}
		changes.add(change);
		if (deltaFactory != null)
			deltaFactory.create(change.getModifiedResource());
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
	 *
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
