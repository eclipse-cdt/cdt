/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.commenthandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;

/**
 * This is the starting point of the entire comment handling  process. The creation of the
 * {@link NodeCommentMap} is based on the {@link IASTTranslationUnit}. From this translation unit
 * the comments are extracted and skipped if they belong not to the same workspace.
 * An {@link ASTCommenterVisitor} is initialized with this collection of comments. And the visit
 * process can start.
 *
 * @see NodeCommenter
 * @see NodeCommentMap
 *
 * @author Guido Zgraggen IFS
 */
public class ASTCommenter {

	private static final class PreprocessorRangeChecker extends ASTVisitor {
		int ppStmtOffset;
		IASTFileLocation commentLocation;
		boolean isPrePpStmtComment = true;

		private PreprocessorRangeChecker(int statementOffset, IASTFileLocation commentLocation) {
			super(true);
			this.ppStmtOffset = statementOffset;
			this.commentLocation = commentLocation;
		}

		private int checkOffsets(IASTNode node) {
			IASTFileLocation nodeLocation = node.getFileLocation();
			if (nodeLocation == null)
				return PROCESS_SKIP;

			int nodeEndOffset = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();

			boolean nodeInBetweenCommentAndPpStmt = nodeEndOffset > commentLocation.getNodeOffset()
					&& nodeEndOffset < ppStmtOffset;
			if (isCommentOnSameLine(node) || nodeInBetweenCommentAndPpStmt) {
				isPrePpStmtComment = false;
				return PROCESS_ABORT;
			} else if (nodeEndOffset < commentLocation.getNodeOffset()) {
				return PROCESS_SKIP;
			} else if (nodeLocation.getNodeOffset() > ppStmtOffset) {
				return PROCESS_ABORT;
			}

			return PROCESS_CONTINUE;
		}

		private boolean isCommentOnSameLine(IASTNode node) {
			IASTFileLocation fileLocation = node.getFileLocation();
			return fileLocation != null
					&& commentLocation.getStartingLineNumber() == fileLocation.getEndingLineNumber();
		}

		@Override
		public int visit(ICPPASTBaseSpecifier baseSpecifier) {
			return checkOffsets(baseSpecifier);
		}

		@Override
		public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
			return checkOffsets(namespaceDefinition);
		}

		@Override
		public int visit(ICPPASTTemplateParameter templateParameter) {
			return checkOffsets(templateParameter);
		}

		@Override
		public int visit(IASTArrayModifier arrayModifier) {
			return checkOffsets(arrayModifier);
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			return checkOffsets(declaration);
		}

		@Override
		public int visit(IASTDeclarator declarator) {
			return checkOffsets(declarator);
		}

		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			return checkOffsets(declSpec);
		}

		@Override
		public int visit(IASTEnumerator enumerator) {
			return checkOffsets(enumerator);
		}

		@Override
		public int visit(IASTExpression expression) {
			return checkOffsets(expression);
		}

		@Override
		public int visit(IASTInitializer initializer) {
			return checkOffsets(initializer);
		}

		@Override
		public int visit(IASTName name) {
			return checkOffsets(name);
		}

		@Override
		public int visit(IASTParameterDeclaration parameterDeclaration) {
			return checkOffsets(parameterDeclaration);
		}

		@Override
		public int visit(IASTPointerOperator ptrOperator) {
			return checkOffsets(ptrOperator);
		}

		@Override
		public int visit(IASTStatement statement) {
			return checkOffsets(statement);
		}

		@Override
		public int visit(IASTTranslationUnit tu) {
			return checkOffsets(tu);
		}

		@Override
		public int visit(IASTTypeId typeId) {
			return checkOffsets(typeId);
		}
	}

	/**
	 * Creates a NodeCommentMap for the given AST. This is the only way to get a NodeCommentMap
	 * which contains all the comments mapped against nodes.
	 *
	 * @param ast the AST
	 * @return NodeCommentMap
	 */
	public static NodeCommentMap getCommentedNodeMap(IASTTranslationUnit ast) {
		NodeCommentMap commentMap = new NodeCommentMap();
		if (ast == null) {
			return commentMap;
		}
		addCommentsToMap(ast, commentMap);
		return commentMap;
	}

	/**
	 * Adds all comments given in {@code ast} to the {@code commentMap}. Calling this twice has
	 * no effect.
	 *
	 * @param ast
	 *            the AST which contains the comments to add
	 * @param commentMap
	 *            the comment map to which the comments are added to
	 */
	public static void addCommentsToMap(IASTTranslationUnit ast, NodeCommentMap commentMap) {
		if (ast == null || commentMap.isASTCovered(ast)) {
			return;
		}
		IASTComment[] commentsArray = ast.getComments();
		List<IASTComment> comments = new ArrayList<>(commentsArray.length);
		for (IASTComment comment : commentsArray) {
			if (comment.isPartOfTranslationUnitFile()) {
				comments.add(comment);
			}
		}
		assignPreprocessorComments(commentMap, comments, ast);
		CommentHandler commentHandler = new CommentHandler(comments);
		ASTCommenterVisitor commenter = new ASTCommenterVisitor(commentHandler, commentMap);
		commentMap.setASTCovered(ast);
		ast.accept(commenter);
	}

	private static boolean isCommentDirectlyBeforePreprocessorStatement(IASTComment comment,
			IASTPreprocessorStatement statement, IASTTranslationUnit tu) {
		if (tu == null || tu.getDeclarations().length == 0) {
			return true;
		}
		IASTFileLocation commentLocation = comment.getFileLocation();
		int preprocessorOffset = statement.getFileLocation().getNodeOffset();
		if (preprocessorOffset > commentLocation.getNodeOffset()) {
			PreprocessorRangeChecker visitor = new PreprocessorRangeChecker(preprocessorOffset, commentLocation);
			tu.accept(visitor);
			return visitor.isPrePpStmtComment;
		}
		return false;
	}

	public static boolean isInWorkspace(IASTNode node) {
		return node.isPartOfTranslationUnitFile();
	}

	/**
	 * Puts leading and trailing comments to {@code commentMap} and removes them from
	 * the {@code comments} list.
	 */
	private static void assignPreprocessorComments(NodeCommentMap commentMap, List<IASTComment> comments,
			IASTTranslationUnit tu) {
		IASTPreprocessorStatement[] preprocessorStatementsArray = tu.getAllPreprocessorStatements();
		if (preprocessorStatementsArray == null) {
			return;
		}
		List<IASTPreprocessorStatement> preprocessorStatements = Arrays.asList(preprocessorStatementsArray);

		if (preprocessorStatements.isEmpty() || comments.isEmpty()) {
			return;
		}

		List<IASTComment> freestandingComments = new ArrayList<>(comments.size());
		Iterator<IASTPreprocessorStatement> statementsIter = preprocessorStatements.iterator();
		Iterator<IASTComment> commentIter = comments.iterator();
		IASTPreprocessorStatement curStatement = getNextNodeInTu(statementsIter);
		IASTComment curComment = getNextNodeInTu(commentIter);
		while (curStatement != null && curComment != null) {
			int statementLineNr = curStatement.getFileLocation().getStartingLineNumber();
			int commentLineNr = curComment.getFileLocation().getStartingLineNumber();
			if (commentLineNr == statementLineNr) {
				commentMap.addTrailingCommentToNode(curStatement, curComment);
				curComment = getNextNodeInTu(commentIter);
			} else if (commentLineNr > statementLineNr) {
				curStatement = getNextNodeInTu(statementsIter);
			} else if (isCommentDirectlyBeforePreprocessorStatement(curComment, curStatement, tu)) {
				commentMap.addLeadingCommentToNode(curStatement, curComment);
				curComment = getNextNodeInTu(commentIter);
			} else {
				freestandingComments.add(curComment);
				curComment = getNextNodeInTu(commentIter);
			}
		}
		while (curComment != null) {
			freestandingComments.add(curComment);
			curComment = getNextNodeInTu(commentIter);
		}

		if (freestandingComments.size() != comments.size()) {
			comments.clear();
			comments.addAll(freestandingComments);
		}
	}

	private static <T extends IASTNode> T getNextNodeInTu(Iterator<T> iter) {
		while (iter.hasNext()) {
			T next = iter.next();
			if (next.isPartOfTranslationUnitFile())
				return next;
		}
		return null;
	}
}
