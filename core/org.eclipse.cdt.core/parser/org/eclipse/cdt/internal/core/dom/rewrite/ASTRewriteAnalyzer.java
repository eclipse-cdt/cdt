/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.rewrite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.rewrite.changegenerator.ChangeGenerator;
import org.eclipse.ltk.core.refactoring.Change;

public class ASTRewriteAnalyzer {

	public static Change rewriteAST(IASTTranslationUnit root, ASTModificationStore modificationStore) {
		ChangeGenerator rewriter = new ChangeGenerator(modificationStore);
		rewriter.generateChange(root);
		return rewriter.getChange();
	}
}
