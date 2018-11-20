/*******************************************************************************
 * Copyright (c) 2010, 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.text.edits.TextEditGroup;

final class SimilarReplacerVisitor extends SimilarFinderVisitor {
	private final IASTName name;

	private final ModificationCollector collector;

	SimilarReplacerVisitor(ExtractFunctionRefactoring refactoring, NodeContainer extractedNodes,
			ModificationCollector collector, List<IASTNode> trail, IASTName name, List<IASTNode> statements) {
		super(refactoring, extractedNodes, trail, statements);
		this.name = name;
		this.collector = collector;
	}

	@Override
	protected void foundSimilar() {
		IASTNode call = refactoring.getMethodCall(name, refactoring.nameTrail, refactoring.names, extractedNodes,
				similarContainer);
		ASTRewrite rewrite = collector.rewriterForTranslationUnit(stmtToReplace.get(0).getTranslationUnit());
		TextEditGroup editGroup = new TextEditGroup(Messages.SimilarFinderVisitor_replaceDuplicateCode);
		rewrite.replace(stmtToReplace.get(0), call, editGroup);
		if (stmtToReplace.size() > 1) {
			for (int i = 1; i < stmtToReplace.size(); ++i) {
				rewrite.remove(stmtToReplace.get(i), editGroup);
			}
		}
	}
}