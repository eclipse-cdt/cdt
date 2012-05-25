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
	
	private static final class PPRangeChecker extends ASTVisitor {
		int ppOffset;
		IASTFileLocation commentNodeLocation;
		boolean isPrePPComment = true;
		
		private PPRangeChecker(int ppOffset, IASTFileLocation commentNodeLocation) {
			super(true);
			this.ppOffset = ppOffset;
			this.commentNodeLocation = commentNodeLocation;
		}

		private int checkOffsets(IASTNode node) {
			int offset = ((ASTNode)node).getOffset();
			int status = PROCESS_CONTINUE;
			
			if (isCommentOnSameLine(node) || offset > commentNodeLocation.getNodeOffset()
					&& offset < ppOffset) {
				isPrePPComment = false;
				status = PROCESS_ABORT;
			} else if ((offset + ((ASTNode) node).getLength() < commentNodeLocation.getNodeOffset())) {
				status = PROCESS_SKIP;
			} else if (offset > ppOffset) {
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
	 * @param transUnit TranslationUnit
	 * @return NodeCommentMap
	 */
	public static NodeCommentMap getCommentedNodeMap(IASTTranslationUnit transUnit){
		NodeCommentMap commentMap = new NodeCommentMap();
		if (transUnit== null) {
			return commentMap;
		}
		IASTComment[] commentsArray = transUnit.getComments();
		if (commentsArray == null) {
			return commentMap;
		}
		ArrayList<IASTComment> comments = new ArrayList<IASTComment>(Arrays.asList(commentsArray));
		filterNonTuComments(comments);
		return addCommentsToCommentMap(transUnit, comments);
	}

	private static void filterNonTuComments(ArrayList<IASTComment> comments) {
		Iterator<IASTComment> iterator = comments.iterator();
		while (iterator.hasNext()) {
			if (!iterator.next().isPartOfTranslationUnitFile()) {
				iterator.remove();
			}
		}
	}

	private static boolean isCommentDirectlyBeforePreprocessorStatement(IASTComment comment,
			IASTPreprocessorStatement ppStatement, IASTTranslationUnit tu) {
		if (tu == null || tu.getDeclarations().length < 1) {
			return true;
		}
		IASTFileLocation commentLocation = comment.getFileLocation();
		int pPOffset = ppStatement.getFileLocation().getNodeOffset();
		if (pPOffset > commentLocation.getNodeOffset()) {
			PPRangeChecker visti = new PPRangeChecker(pPOffset, commentLocation);
			tu.accept(visti);
			return visti.isPrePPComment;
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

	private static void assignPreprocessorComments(NodeCommentMap commentMap,
			ArrayList<IASTComment> comments, IASTTranslationUnit tu) {
		IASTPreprocessorStatement[] preprocessorStatementsArray = tu.getAllPreprocessorStatements();
		if (preprocessorStatementsArray == null) {
			return;
		}
		List<IASTPreprocessorStatement> preprocessorStatements = Arrays
				.asList(preprocessorStatementsArray);

		if (preprocessorStatements.isEmpty() || comments.isEmpty()) {
			return;
		}

		Iterator<IASTPreprocessorStatement> ppIter = preprocessorStatements.iterator();
		Iterator<IASTComment> commentIter = comments.iterator();
		IASTPreprocessorStatement curPPStatement = getNextNodeInTu(ppIter);
		IASTComment curComment = getNextNodeInTu(commentIter);
		while (curPPStatement != null && curComment != null) {
			int ppLineNr = curPPStatement.getFileLocation().getStartingLineNumber();
			int commentLineNr = curComment.getFileLocation().getStartingLineNumber();
			if (commentLineNr == ppLineNr) {
				commentMap.addTrailingCommentToNode(curPPStatement, curComment);
				commentIter.remove();
				curComment = getNextNodeInTu(commentIter);
			} else if (commentLineNr > ppLineNr) {
				curPPStatement = getNextNodeInTu(ppIter);
			} else if (isCommentDirectlyBeforePreprocessorStatement(curComment, curPPStatement, tu)) {
				commentMap.addLeadingCommentToNode(curPPStatement, curComment);
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
