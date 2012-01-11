/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.comenthandler;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.tests.rewrite.RewriteBaseTest;
import org.eclipse.cdt.core.parser.tests.rewrite.TestSourceFile;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.core.runtime.CoreException;

/**
 * This test tests the behavior of the class ASTCommenter. It checks if the ASTCommenter assigns
 * the comments contained in an AST to the right ASTNodes.<br>
 * The source for the CommentHandling tests is located at
 * /resources/rewrite/CommentHandlingTestSource.rts.<br>
 * This file contains the source code and the expected output for all the tests.
 * Following a little example how such a test looks like:<br><br>
 * 
 * <code><pre>
 * //!NameOfTheTest - will be used as JUnit test name
 * //#org.eclipse.cdt.core.parser.tests.rewrite.comenthandler.CommentHandlingTest
 * //@NameOfASourceFile.h
 * class myTestClass
 * {
 *  //myLeadingComment
 *  void aMethod(); //myTrailingComment
 *  //myFreestandingComment
 *  //myFreestandingComment2
 * };
 * 
 * //=
 * =>leading
 * void aMethod(); = //myLeadingComment
 * 
 * =>trailing
 * void aMethod(); = //myTrailingComment
 * 
 * =>freestanding
 * void aMethod(); = //myFreestandingComment , //myFreestandingComment2
 * </pre></code>
 * 
 * The second line (//#org.eclipse.cdt...) indicates the test class (in this case this class).<br>
 * The "//=" indicates the beginning of the expected test result.<br>
 * The test result contains three sections (separated by "=>leading", "=>trailing" and
 * "=>freestanding").<br>
 * Each section contains the raw signature of the node to which a comment is assigned plus " = "
 * and the comment. If there are several comments assigned to the same node they are concatenated
 * with a " , ".
 * 
 * @author Guido Zgraggen IFS, Lukas Felber IFS
 */
public class CommentHandlingTest extends RewriteBaseTest {
	private static final String ANY_CHAR_REGEXP = "(.*)"; //$NON-NLS-1$
	private static final String SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$

	private static final String LEADING_COMMENT_SEPARATOR = "=>leading"; //$NON-NLS-1$
	private static final String TRAILING_COMMENT_SEPARATOR = "=>trailing"; //$NON-NLS-1$
	private static final String FREESTANDING_COMMENT_SEPARATOR = "=>freestanding"; //$NON-NLS-1$

	private static final String LEADING_COMMENT_TITLE = "<<<=== Leading Comment Test Section ===>>>"; //$NON-NLS-1$
	private static final String TRAILING_COMMENT_TITLE = "<<<=== Trailing Comment Test Section ===>>>"; //$NON-NLS-1$
	private static final String FREESTANDING_COMMENT_TITLE = "<<<=== Freestanding Comment Test Section ===>>>"; //$NON-NLS-1$
	
	public CommentHandlingTest(String name, List<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		if (fileMap.isEmpty()) {
			fail("No file for testing"); //$NON-NLS-1$
		}
		
		for (String fileName : fileMap.keySet()) {
			TestSourceFile file = fileMap.get(fileName);
			NodeCommentMap nodeMap = ASTCommenter.getCommentedNodeMap(getUnit(fileName));
			
			StringBuilder expectedResultBuilder = buildExpectedResult(file);
			StringBuilder actualResultBuilder = buildActualResult(nodeMap);
			
			assertEquals(expectedResultBuilder.toString(), actualResultBuilder.toString());
		}
	}

	private StringBuilder buildExpectedResult(TestSourceFile file) {
		Matcher matcher = Pattern.compile(CommentHandlingTest.getSeparatingRegexp(),
				Pattern.MULTILINE | Pattern.DOTALL).matcher(file.getExpectedSource());
		if (!matcher.find()) {
			fail("Missing expected section. Expected result code must be of the following format:\n\"=>leading\n...\n=>trailing\n...\n=>freestanding\""); //$NON-NLS-1$
		}
		StringBuilder expectedResultBuilder = new StringBuilder();
		
		String leadingResult = matcher.group(1);
		String trailingResult = matcher.group(2);
		String freestandingResult = matcher.group(3);
		
		appendLineTrimmed(expectedResultBuilder, LEADING_COMMENT_TITLE);
		appendLineTrimmed(expectedResultBuilder, leadingResult);
		appendLineTrimmed(expectedResultBuilder, TRAILING_COMMENT_TITLE);
		appendLineTrimmed(expectedResultBuilder, trailingResult);
		appendLineTrimmed(expectedResultBuilder, FREESTANDING_COMMENT_TITLE);
		appendLineTrimmed(expectedResultBuilder, freestandingResult);

		return expectedResultBuilder;
	}

	private StringBuilder buildActualResult(NodeCommentMap nodeMap) {
		StringBuilder actualResultBuilder = new StringBuilder();
		appendLineTrimmed(actualResultBuilder, LEADING_COMMENT_TITLE);
		appendLineTrimmed(actualResultBuilder, getCommentMapResult(nodeMap.getLeadingMap()));
		appendLineTrimmed(actualResultBuilder, TRAILING_COMMENT_TITLE);
		appendLineTrimmed(actualResultBuilder, getCommentMapResult(nodeMap.getTrailingMap()));
		appendLineTrimmed(actualResultBuilder, FREESTANDING_COMMENT_TITLE);
		appendLineTrimmed(actualResultBuilder, getCommentMapResult(nodeMap.getFreestandingMap()));
		return actualResultBuilder;
	}

	private String getCommentMapResult(Map<IASTNode, List<IASTComment>> map) {
		TreeSet<IASTNode> keyTree = new TreeSet<IASTNode>(new NodeOffsetComparator());
		keyTree.addAll(map.keySet());
		StringBuilder output = new StringBuilder();
		for (IASTNode actNode : keyTree) {
			List<IASTComment> comments = map.get(actNode);
			output.append(getSignature(actNode) + " = "); //$NON-NLS-1$
			boolean first = true;
			for (IASTComment actComment : comments) {
				if (!first) {
					output.append(" , "); //$NON-NLS-1$
				}
				output.append(actComment.getRawSignature());
				first = false;
			}
			output.append(SEPARATOR);
		}
		return output.toString().trim();
	}

	private String getSignature(IASTNode actNode) {
		if (actNode instanceof IASTCompositeTypeSpecifier) {
			IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) actNode;
			return comp.getName().toString();
		} else if (actNode instanceof IASTEnumerationSpecifier) {
			IASTEnumerationSpecifier comp = (IASTEnumerationSpecifier) actNode;
			return comp.getName().toString();
		}
		return actNode.getRawSignature();
	}

	private static String getSeparatingRegexp() {
		return LEADING_COMMENT_SEPARATOR + ANY_CHAR_REGEXP + TRAILING_COMMENT_SEPARATOR +
				ANY_CHAR_REGEXP + FREESTANDING_COMMENT_SEPARATOR + ANY_CHAR_REGEXP;
	}
	
	private IASTTranslationUnit getUnit(String fileName) throws CoreException {
		ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(
				project.getFile(fileName));
		return tu.getAST();
	}
	
	private final class NodeOffsetComparator implements Comparator<IASTNode> {
		@Override
		public int compare(IASTNode o1, IASTNode o2) {
			int offDif = o1.getFileLocation().getNodeOffset() - o2.getFileLocation().getNodeOffset();
			if (offDif == 0) {
				return o1.getFileLocation().getNodeLength() - o2.getFileLocation().getNodeLength();
			}
			return offDif;
		}
	}
	
	private void appendLineTrimmed(StringBuilder builderToAppendTo, String line) {
		builderToAppendTo.append(line.trim());
		builderToAppendTo.append(SEPARATOR);
	}
}
