/*******************************************************************************
 * Copyright (c) 2014 Institute for Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lukas Felber (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.comenthandler;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.parser.tests.rewrite.TestSourceFile;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.text.edits.TextEditGroup;

public class CommentHandlingWithRewriteTest extends CommentHandlingTest {
	private ASTRewrite newRewrite;

	public CommentHandlingWithRewriteTest(String name, List<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		IASTTranslationUnit tu = getUnit("main.cpp");
		IASTTranslationUnit otherTu = getUnit("other.cpp");

		IASTNodeSelector selector = tu.getNodeSelector(null);
		IASTNodeSelector otherSelector = otherTu.getNodeSelector(null);

		IASTNode fooBody = selector.findNode(11, 27);
		IASTNode iNode = selector.findEnclosingNode(26, 10).getParent();
		IASTNode jNode = otherSelector.findNode(20, 10);

		ASTRewrite rewrite = ASTRewrite.create(tu);
		newRewrite = rewrite.insertBefore(fooBody, iNode, jNode, new TextEditGroup("test group"));
		super.runTest();
	}

	@Override
	protected NodeCommentMap getNodeMapForFile(String fileName) throws Exception {
		if (fileName.equals("main.cpp")) {
			return getNodeMapFromRewrite(newRewrite);
		}
		return super.getNodeMapForFile(fileName);
	}

	private NodeCommentMap getNodeMapFromRewrite(ASTRewrite rewrite) throws Exception {
		Field commentMapField = rewrite.getClass().getDeclaredField("fCommentMap");
		commentMapField.setAccessible(true);
		return (NodeCommentMap) commentMapField.get(rewrite);
	}
}
