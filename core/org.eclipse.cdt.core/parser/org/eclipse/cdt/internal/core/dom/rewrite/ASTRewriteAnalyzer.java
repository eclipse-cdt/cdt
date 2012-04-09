/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		sFileChangeFactory= factory;
	}

	public static TextFileChange createCTextFileChange(IFile file) {
		if (sFileChangeFactory == null) {
			return new TextFileChange(file.getName(), file);
		}
		return sFileChangeFactory.createCTextFileChange(file);
	}
}
