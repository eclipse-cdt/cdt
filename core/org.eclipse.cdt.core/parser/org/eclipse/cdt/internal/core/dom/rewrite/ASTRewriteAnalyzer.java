/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.rewrite.changegenerator.ChangeGenerator;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;

public class ASTRewriteAnalyzer {
	private static ICTextFileChangeFactory sFileChangeFactory;

	public static Change rewriteAST(IASTTranslationUnit root, ASTModificationStore modificationStore,
			NodeCommentMap commentMap) {
		ChangeGenerator rewriter = new ChangeGenerator(modificationStore, commentMap);
		rewriter.generateChange(root);
		return rewriter.getChange();
	}

	public static void setCTextFileChangeFactory(ICTextFileChangeFactory factory) {
		sFileChangeFactory = factory;
	}

	public static TextFileChange createCTextFileChange(IFile file) {
		if (sFileChangeFactory == null) {
			return new TextFileChange(file.getName(), file);
		}
		return sFileChangeFactory.createCTextFileChange(file);
	}
}
