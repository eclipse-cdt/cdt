/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.comenthandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.tests.rewrite.RewriteBaseTest;
import org.eclipse.cdt.core.parser.tests.rewrite.TestSourceFile;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Guido Zgraggen IFS
 * 
 */
public class CommentHandlingTest extends RewriteBaseTest {

	private static final String ANY_CHAR_REGEXP = "(.*)"; //$NON-NLS-1$
	private static String separator = System.getProperty("line.separator"); //$NON-NLS-1$
	
	private static String LEADING_COMMENT_SEPARATOR = "=>leading"; //$NON-NLS-1$
	private static String TRAILING_COMMENT_SEPARATOR = "=>trailing"; //$NON-NLS-1$
	private static String FREESTANDING_COMMENT_SEPARATOR = "=>freestanding"; //$NON-NLS-1$

	public CommentHandlingTest(String name, Vector<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {

		if (fileMap.size() > 1) {
			throw new Exception("To many files for CommentHandlingTest"); //$NON-NLS-1$
		} else if (fileMap.size() == 0) {
			throw new Exception("No file for testing"); //$NON-NLS-1$
		}

		TestSourceFile file = fileMap.values().iterator().next();

		NodeCommentMap nodeMap = ASTCommenter.getCommentedNodeMap(getUnit());
		Matcher matcher = Pattern.compile(CommentHandlingTest.getSeparatingRegexp(), Pattern.MULTILINE | Pattern.DOTALL).matcher(file.getExpectedSource());

		if (!matcher.find()) {
			fail("Missing expected section. Expected result code must be of the following format:\n\"=>leading\n...\n=>trailing\n...\n=>freestanding\""); //$NON-NLS-1$
		}
		String leadingResult = matcher.group(1);
		String trailingResult = matcher.group(2);
		String freestandingResult = matcher.group(3);

		testMap(nodeMap.getLeadingMap(), leadingResult, "Leading test failed."); //$NON-NLS-1$
		testMap(nodeMap.getTrailingMap(), trailingResult, "Trailing test failed."); //$NON-NLS-1$
		testMap(nodeMap.getFreestandingMap(), freestandingResult, "Freestanding test failed."); //$NON-NLS-1$
	}

	private void testMap(HashMap<IASTNode, ArrayList<IASTComment>> map, String expectedResult, String err) {
		TreeSet<IASTNode> keyTree = new TreeSet<IASTNode>(new NodeOffsetComparator());
		keyTree.addAll(map.keySet());
		StringBuilder output = new StringBuilder();
		for (IASTNode actNode : keyTree) {
			ArrayList<IASTComment> comments = map.get(actNode);

			output.append(actNode.getRawSignature() + " = "); //$NON-NLS-1$
			boolean first = true;
			for (IASTComment actComment : comments) {
				if (!first) {
					output.append(" , "); //$NON-NLS-1$
				}
				output.append(actComment.getRawSignature());
				first = false;
			}
			output.append(separator);
		}
		assertEquals(err, expectedResult.trim(), output.toString().trim());
	}

	private static String getSeparatingRegexp() {
		return LEADING_COMMENT_SEPARATOR + ANY_CHAR_REGEXP + TRAILING_COMMENT_SEPARATOR + ANY_CHAR_REGEXP + FREESTANDING_COMMENT_SEPARATOR + ANY_CHAR_REGEXP;
	}

	// === Nested classes for testing purpose
	private final class NodeOffsetComparator implements Comparator<IASTNode> {
		public int compare(IASTNode o1, IASTNode o2) {
			int offDif = o1.getFileLocation().getNodeOffset() - o2.getFileLocation().getNodeOffset();
			if (offDif == 0) {
				return o1.getFileLocation().getNodeLength() - o2.getFileLocation().getNodeLength();
			}
			return offDif;
		}
	}


	private IASTTranslationUnit getUnit() throws CoreException {
		ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(project.getFile(fileMap.keySet().iterator().next()));
		return tu.getAST();
	}

}
