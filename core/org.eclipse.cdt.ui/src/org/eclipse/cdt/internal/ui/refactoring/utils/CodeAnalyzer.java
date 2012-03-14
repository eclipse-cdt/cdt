/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.corext.refactoring.code.flow.Selection;

public class CodeAnalyzer extends StatementAnalyzer {

	public CodeAnalyzer(ITranslationUnit cunit, Selection selection, boolean traverseSelectedNode)
			throws CoreException {
		super(cunit, selection, traverseSelectedNode);
	}

	@Override
	protected final void checkSelectedNodes() {
		super.checkSelectedNodes();
		RefactoringStatus status= getStatus();
		if (status.hasFatalError())
			return;
		IASTNode node= getFirstSelectedNode();
		if (node instanceof IASTInitializerList) {
			status.addFatalError(Messages.CodeAnalyzer_initializer_list);
		}
	}
}
