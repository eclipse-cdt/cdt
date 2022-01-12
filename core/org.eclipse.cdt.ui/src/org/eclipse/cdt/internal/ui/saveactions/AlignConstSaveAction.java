/*******************************************************************************
 * Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.saveactions;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.actions.AlignConstAction;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Save action to align all const specifiers in a given translation unit
 * according to the settings in the workspace preferences (left or right to
 * type).
 *
 */
public class AlignConstSaveAction {

	public void perform(ITranslationUnit tu, IProgressMonitor monitor) {
		alignConstInActiveEditor(tu, monitor);
	}

	private void alignConstInActiveEditor(ITranslationUnit translationUnit, IProgressMonitor monitor) {
		try {
			IASTTranslationUnit ast = translationUnit.getAST(null, ITranslationUnit.AST_SKIP_ALL_HEADERS);
			if (ast != null) {
				AlignConstAction.rewriteMisalignedConstSpecifiers(ast, monitor);
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}
}
