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
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
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
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

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
		int commentOffset;
		boolean isPrePPComment = true;
		
		private PPRangeChecker(boolean visitNodes, int nextPPOfset, int commentNodeOffset) {
			super(visitNodes);
			ppOffset = nextPPOfset;
			commentOffset = commentNodeOffset;
		}

		private int checkOffsets(IASTNode node) {
			int offset = ((ASTNode)node).getOffset();
			int status = ASTVisitor.PROCESS_CONTINUE;
			
			if (offset > commentOffset && offset < ppOffset) {
				isPrePPComment = false;
				status = ASTVisitor.PROCESS_ABORT;
			} else if ((offset + ((ASTNode)node).getLength() < commentOffset)) {
				status = ASTVisitor.PROCESS_SKIP;
			} else if (offset > ppOffset) {
				status = ASTVisitor.PROCESS_ABORT;
			}
			
			return status;
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
		if (transUnit == null) {
			return new NodeCommentMap();
		}
		List<IASTComment> comments = removeNotNeededComments(transUnit);		
		if (comments == null || comments.isEmpty()) {
			return new NodeCommentMap();
		}
		return addCommentsToCommentMap(transUnit, comments);
	}

	private static List<IASTComment> removeNotNeededComments(IASTTranslationUnit transUnit) {
		List<IASTComment> comments = getCommentsInWorkspace(transUnit);
		if (comments == null || comments.isEmpty()) {
			return null;
		}
		return removeAllPreprocessorComments(transUnit, comments);
	}

	private static List<IASTComment> getCommentsInWorkspace(IASTTranslationUnit tu) {
		IASTComment[] comments = tu.getComments();
		ArrayList<IASTComment> commentsInWorkspace = new ArrayList<IASTComment>();

		if (comments == null || comments.length == 0) {
			return null;
		}

		for (IASTComment comment : comments) {
			if (isInWorkspace(comment)) {
				commentsInWorkspace.add(comment);
			}
		}
		return commentsInWorkspace;
	}

	private static List<IASTComment> removeAllPreprocessorComments(IASTTranslationUnit tu,
			List<IASTComment> comments) {
		IASTPreprocessorStatement[] preprocessorStatements = tu.getAllPreprocessorStatements();
		TreeMap<Integer, String> treeOfPreProcessorLines = new TreeMap<Integer,String>();
		TreeMap<String, ArrayList<Integer>> ppOffsetForFiles = new TreeMap<String, ArrayList<Integer>>();

		for (IASTPreprocessorStatement statement : preprocessorStatements) {
			if (isInWorkspace(statement)) {
				String fileName = statement.getFileLocation().getFileName();
				treeOfPreProcessorLines.put(statement.getFileLocation().getStartingLineNumber(), fileName);
				ArrayList<Integer> offsetList = ppOffsetForFiles.get(fileName);
				if (offsetList == null) {
					offsetList = new ArrayList<Integer>();
					ppOffsetForFiles.put(fileName, offsetList);
				}
				offsetList.add(((ASTNode)statement).getOffset());
			}
		}

		ArrayList<IASTComment> commentsInCode = new ArrayList<IASTComment>();
		for (IASTComment comment : comments) {
			IASTFileLocation commentFileLocation = comment.getFileLocation();
			int comStartLineNumber = commentFileLocation.getStartingLineNumber();
			String fileName = commentFileLocation.getFileName();
			if (treeOfPreProcessorLines.containsKey(comStartLineNumber)
					&& treeOfPreProcessorLines.get(comStartLineNumber).equals(fileName)) {
				continue;
			}
			if (commentIsAtTheBeginningBeforePreprocessorStatements(comment,
					ppOffsetForFiles.get(fileName), tu)) {
				continue;
			}
			commentsInCode.add(comment);
		}
		return commentsInCode;
	}

	private static boolean commentIsAtTheBeginningBeforePreprocessorStatements(IASTComment comment, 
			ArrayList<Integer> listOfPreProcessorOffset, IASTTranslationUnit tu) {
		if (listOfPreProcessorOffset == null) {
			return false;
		}
		
		if (comment.getTranslationUnit() == null || comment.getTranslationUnit().getDeclarations().length < 1) {
			return true;
		}
		IASTDeclaration decl = comment.getTranslationUnit().getDeclarations()[0];
		String commentFileName = comment.getFileLocation().getFileName();
		boolean sameFile = decl.getFileLocation().getFileName().equals(commentFileName);
		int commentNodeOffset = ((ASTNode)comment).getOffset();
		if (sameFile) {
			if (decl.getFileLocation().getNodeOffset() < commentNodeOffset) {
				return false;
			}
		}
		Collections.sort(listOfPreProcessorOffset);
		int nextPPOfset = -1;
		for (Integer integer : listOfPreProcessorOffset) {
			if (integer > commentNodeOffset) {
				nextPPOfset = integer;
				PPRangeChecker visti = new PPRangeChecker(true, nextPPOfset, commentNodeOffset);
				tu.accept(visti);
				if (visti.isPrePPComment) {
					return true;
				}
			}
		}
			
		return false;
	}

	private static boolean isInWorkspace(IASTNode node) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		IPath nodePath = new Path(node.getContainingFilename());
		for (IProject project : projects) {
			if (project.getLocation().isPrefixOf(nodePath)) return true;
		}
		return false;
	}
	
	private static NodeCommentMap addCommentsToCommentMap(IASTTranslationUnit rootNode,
			List<IASTComment> comments){
		NodeCommentMap commentMap = new NodeCommentMap();
		CommentHandler commHandler = new CommentHandler(comments);

		IASTDeclaration[] declarations = rootNode.getDeclarations();
		for (int i = 0; i < declarations.length; i++) {
			if (isInWorkspace(declarations[i])) {
				ASTCommenterVisitor commenter = new ASTCommenterVisitor(commHandler, commentMap);
				declarations[i].accept(commenter);
				
				// Add the remaining comments to the last declaration to prevent comment loss.
				if (i == declarations.length - 1) {
					commenter.addRemainingComments(declarations[i]);
				}
			}
		}
		return commentMap;
	}	
}
