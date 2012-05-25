/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software - initial API and implementation 
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
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * This is the starting point of the entire comment handling  process. The creation of the 
 * NodeCommentMap is based on the IASTTranslationUnit. From this TranslationUnit the comments 
 * are extracted and skipped if they belong not to the same workspace. An ASTCommenterVisitor 
 * is initialized with this collection of comments. And the visit process can start. 
 * 
 * @see org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommenter
 * @see org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap
 *  
 * @author Guido Zgraggen IFS 
 */
public class ASTCommenter {
	
	private static final class PreprocessorRangeChecker extends ASTVisitor {
		int statementOffset;
		IASTFileLocation commentNodeLocation;
		boolean isPreStatementComment = true;
		
		private PreprocessorRangeChecker(int statementOffset, IASTFileLocation commentNodeLocation) {
			super(true);
			this.statementOffset = statementOffset;
			this.commentNodeLocation = commentNodeLocation;
		}

		private int checkOffsets(IASTNode node) {
			int offset = ((ASTNode)node).getOffset();
			int status = PROCESS_CONTINUE;
			
			if (isCommentOnSameLine(node) 
					|| offset > commentNodeLocation.getNodeOffset()
					&& offset < statementOffset) {
				isPreStatementComment = false;
				status = PROCESS_ABORT;
			} else if ((offset + ((ASTNode) node).getLength() < commentNodeLocation.getNodeOffset())) {
				status = PROCESS_SKIP;
			} else if (offset > statementOffset) {
				status = PROCESS_ABORT;
			}
			
			return status;
		}

		private boolean isCommentOnSameLine(IASTNode node) {
			return commentNodeLocation.getStartingLineNumber() == node.getFileLocation()
					.getEndingLineNumber();
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
	 * Creates a NodeCommentMap for the given TranslationUnit. This is the only way
	 * to get a NodeCommentMap which contains all the comments mapped against nodes.
	 * 
	 * @param tu TranslationUnit
	 * @return NodeCommentMap
	 */
	public static NodeCommentMap getCommentedNodeMap(IASTTranslationUnit tu){
		NodeCommentMap commentMap = new NodeCommentMap();
		if (tu == null) {
			return commentMap;
		}
		IASTComment[] commentsArray = tu.getComments();
		if (commentsArray == null) {
			return commentMap;
		}
		// Note that constructing a real ArrayList is required here, since in filterNonTuComments, the
		// remove-method will be invoked on the list's iterator. Calling it on the type Arrays$ArrayList (the
		// resulting type of Arrays.asList() ) would throw a UnsupportedOperationException.
		ArrayList<IASTComment> comments = new ArrayList<IASTComment>(Arrays.asList(commentsArray));
		filterNonTuComments(comments);
		return addCommentsToCommentMap(tu, comments);
	}

	/**
	 * Note that passing an ArrayList (instead of just List or Collection) is required here, since this
	 * guarantees that the call to the remove-method on the list's iterator will not result in an
	 * UnsupportedOperationException which might be the case for other Collection/List types.
	 */
	private static void filterNonTuComments(ArrayList<IASTComment> comments) {
		Iterator<IASTComment> iterator = comments.iterator();
		while (iterator.hasNext()) {
			if (!iterator.next().isPartOfTranslationUnitFile()) {
				iterator.remove();
			}
		}
	}

	private static boolean isCommentDirectlyBeforePreprocessorStatement(IASTComment comment,
			IASTPreprocessorStatement statement, IASTTranslationUnit tu) {
		if (tu == null || tu.getDeclarations().length == 0) {
			return true;
		}
		IASTFileLocation commentLocation = comment.getFileLocation();
		int preprcessorOffset = statement.getFileLocation().getNodeOffset();
		if (preprcessorOffset > commentLocation.getNodeOffset()) {
			PreprocessorRangeChecker vister = new PreprocessorRangeChecker(preprcessorOffset, commentLocation);
			tu.accept(vister);
			return vister.isPreStatementComment;
		}
		return false;
	}

	public static boolean isInWorkspace(IASTNode node) {
		return node.isPartOfTranslationUnitFile();
	}
	
	private static NodeCommentMap addCommentsToCommentMap(IASTTranslationUnit tu,
			ArrayList<IASTComment> comments){
		NodeCommentMap commentMap = new NodeCommentMap();
		CommentHandler commHandler = new CommentHandler(comments);

		assignPreprocessorComments(commentMap, comments, tu);
		ASTCommenterVisitor commenter = new ASTCommenterVisitor(commHandler, commentMap);
		tu.accept(commenter);
		return commentMap;
	}
	
	/**
	 * Note that passing an ArrayList (instead of just List or Collection) is required here, since this
	 * guarantees that the call to the remove-method on the list's iterator will not result in an
	 * UnsupportedOperationException which might be the case for other Collection/List types.
	 */
	private static void assignPreprocessorComments(NodeCommentMap commentMap,
			ArrayList<IASTComment> comments, IASTTranslationUnit tu) {
		IASTPreprocessorStatement[] preprocessorStatementsArray = tu.getAllPreprocessorStatements();
		if (preprocessorStatementsArray == null) {
			return;
		}
		List<IASTPreprocessorStatement> preprocessorStatements = Arrays.asList(preprocessorStatementsArray);

		if (preprocessorStatements.isEmpty() || comments.isEmpty()) {
			return;
		}

		Iterator<IASTPreprocessorStatement> statementsIter = preprocessorStatements.iterator();
		Iterator<IASTComment> commentIter = comments.iterator();
		IASTPreprocessorStatement curStatement = getNextNodeInTu(statementsIter);
		IASTComment curComment = getNextNodeInTu(commentIter);
		while (curStatement != null && curComment != null) {
			int statementLineNr = curStatement.getFileLocation().getStartingLineNumber();
			int commentLineNr = curComment.getFileLocation().getStartingLineNumber();
			if (commentLineNr == statementLineNr) {
				commentMap.addTrailingCommentToNode(curStatement, curComment);
				commentIter.remove();
				curComment = getNextNodeInTu(commentIter);
			} else if (commentLineNr > statementLineNr) {
				curStatement = getNextNodeInTu(statementsIter);
			} else if (isCommentDirectlyBeforePreprocessorStatement(curComment, curStatement, tu)) {
				commentMap.addLeadingCommentToNode(curStatement, curComment);
				commentIter.remove();
				curComment = getNextNodeInTu(commentIter);
			} else {
				curComment = getNextNodeInTu(commentIter);
			}
		}
	}

	private static <T extends IASTNode> T getNextNodeInTu(Iterator<T> iter) {
		if (!iter.hasNext()) {
			return null;
		}
		T next = iter.next();
		return next.isPartOfTranslationUnitFile() ? next : getNextNodeInTu(iter);
	}
}
