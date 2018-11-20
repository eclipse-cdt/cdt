/*******************************************************************************
 * Copyright (c) 2014 Institute for Software.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lukas Felber (IFS) - initial API and implementation
 *    Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.comenthandler;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;
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

		IASTNode fooBody = tu.getDeclarations()[0].getChildren()[2];
		IASTNode iNode = fooBody.getChildren()[0];
		IASTNode jNode = otherTu.getDeclarations()[0];

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
