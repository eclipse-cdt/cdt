/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.rewrite;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTLiteralNode;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTRewriteAnalyzer;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.text.edits.TextEditGroup;

/**
 * Infrastructure for modifying code by describing changes to AST nodes. The AST rewriter collects
 * descriptions of modifications to nodes and translates these descriptions into text edits that can
 * then be applied to the original source. This is all done without actually modifying the original
 * AST. The rewrite infrastructure tries to generate minimal text changes, preserve existing
 * comments and indentation, and follow code formatter settings. A {@link IASTComment} can be
 * removed from or added to a node.
 * <p>
 * The initial implementation does not support nodes that implement
 * {@link IASTPreprocessorStatement} or {@link IASTProblem}.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is no guarantee that this API will work or that it will remain the same.
 * Please do not use this API without consulting with the CDT team.
 * </p>
 * 
 * @since 5.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class ASTRewrite {
	/**
	 * Defines the positions of the comment.
	 * 
	 * @since 5.3
	 */
	public enum CommentPosition {
		/**
		 * Comments before a statement, declaration, or definition
		 */
		leading,
		/**
		 * Comments right after the AST node on the same line
		 */
		trailing,
		/**
		 * Comments before a closing brace such as they occur in namespace-, class- and
		 * method-definitions or at the end of a file
		 */
		freestanding
	}
	
	/**
	 * Creates a rewriter for a translation unit.
	 */
	public static ASTRewrite create(IASTTranslationUnit node) {
		NodeCommentMap commentMap = ASTCommenter.getCommentedNodeMap(node);
		return new ASTRewrite(node, new ASTModificationStore(), null, commentMap);
	}

	private final IASTNode fRoot;
	private final ASTModificationStore fModificationStore;
	private final ASTModification fParentMod;
	private final NodeCommentMap fCommentMap;
	
	private enum Operation {
		insertBefore,
		replace,
		remove
	}

	private ASTRewrite(IASTNode root, ASTModificationStore modStore, ASTModification parentMod,
			NodeCommentMap commentMap) {
		fRoot= root;
		fModificationStore= modStore;
		fParentMod= parentMod;
		fCommentMap = commentMap;
	}

	/**
	 * Creates and returns a node for a source string that is to be inserted into
	 * the output document.
	 * The string will be inserted without being reformatted beyond correcting
	 * the indentation level. 
	 * 
	 * @param code the string to be inserted; lines should not have extra indentation
	 * @return a synthetic node representing the literal code.
	 * @throws IllegalArgumentException if the code is null.
	 */
	public final IASTNode createLiteralNode(String code) {
		return new ASTLiteralNode(code);
	}

	/**
	 * Removes the given node in this rewriter. The ast is not modified, the rewriter
	 * just records the removal.
	 *
	 * @param node the node being removed
	 * @param editGroup the edit group in which to collect the corresponding
	 *     text edits, or <code>null</code> 
	 * @throws IllegalArgumentException if the node is null, the node is not
	 *     part of this rewriter's AST.
	 */
	public final void remove(IASTNode node, TextEditGroup editGroup) {
		checkBelongsToAST(node);
		checkSupportedNode(node, Operation.remove);
		ASTModification mod= new ASTModification(ModificationKind.REPLACE, node, null, editGroup);
		fModificationStore.storeModification(fParentMod, mod);
	}

	/**
	 * Replaces the given node in this rewriter. The ast is not modified, the rewriter
	 * just records the replacement.
	 * The replacement node can be part of a translation-unit or it is a synthetic 
	 * (newly created) node.
	 *
	 * @param node the node being replaced
	 * @param replacement the node replacing the given one
	 * @param editGroup the edit group in which to collect the corresponding text edits,
	 *     or <code>null</code> 
	 * @return a rewriter for further rewriting the replacement node.
	 * @throws IllegalArgumentException if the node or the replacement is null, or if the node is
	 * 	   not part of this rewriter's AST
	 */
	public final ASTRewrite replace(IASTNode node, IASTNode replacement, TextEditGroup editGroup) {
		if (replacement == null) {
			throw new IllegalArgumentException();
		}
		checkBelongsToAST(node);
		checkSupportedNode(node, Operation.replace);
		checkSupportedNode(replacement, Operation.replace);
		ASTModification mod= new ASTModification(ModificationKind.REPLACE, node, replacement, editGroup);
		fModificationStore.storeModification(fParentMod, mod);
		return new ASTRewrite(replacement, fModificationStore, mod, fCommentMap);
	}

	/**
	 * Inserts the given node in this rewriter. The ast is not modified, the rewriter
	 * just records the insertion.
	 * The new node can be part of a translation-unit or it is a synthetic 
	 * (newly created) node.
	 * @param parent the parent the new node is added to.
	 * @param insertionPoint the node before which the insertion shall be done, or <code>null</code>
	 *     for inserting after the last child.
	 * @param newNode the node being inserted 
	 * @param editGroup the edit group in which to collect the corresponding
	 *     text edits, or <code>null</code> 
	 * @return a rewriter for further rewriting the inserted node.
	 * @throws IllegalArgumentException if the parent or the newNode is null, or if the parent is
	 *     not part of this rewriter's AST, or the insertionPoint is not a child of the parent.
	 */
	public final ASTRewrite insertBefore(IASTNode parent, IASTNode insertionPoint, IASTNode newNode,
			TextEditGroup editGroup) {
		if (parent != fRoot) {
			checkBelongsToAST(parent);
		}
		if (newNode == null) {
			throw new IllegalArgumentException();
		}
		checkSupportedNode(parent, Operation.insertBefore);
		checkSupportedNode(insertionPoint, Operation.insertBefore);
		checkSupportedNode(newNode, Operation.insertBefore);

		ASTModification mod;
		if (insertionPoint == null) {
			mod= new ASTModification(ModificationKind.APPEND_CHILD, parent, newNode, editGroup);
		} else {
			if (insertionPoint.getParent() != parent) {
				throw new IllegalArgumentException();
			}
			mod= new ASTModification(ModificationKind.INSERT_BEFORE, insertionPoint, newNode, editGroup);
		}
		fModificationStore.storeModification(fParentMod, mod);
		return new ASTRewrite(newNode, fModificationStore, mod, fCommentMap);
	}
	
	/**
	 * Converts all modifications recorded by this rewriter into the change object required by
	 * the refactoring framework.
	 * <p>
	 * Calling this methods does not discard the modifications on record. Subsequence modifications 
	 * are added to the ones already on record. If this method is called again later,
	 * the resulting text edit object will accurately reflect the net cumulative affect of all those
	 * changes.
	 * </p>
	 * 
	 * @return Change object describing the changes to the document corresponding to the changes
	 *     recorded by this rewriter
	 * @since 5.0
	 */
	public Change rewriteAST() {
		if (!(fRoot instanceof IASTTranslationUnit)) {
			throw new IllegalArgumentException("This API can only be used for the root rewrite object."); //$NON-NLS-1$
		}
		return ASTRewriteAnalyzer.rewriteAST((IASTTranslationUnit) fRoot, fModificationStore, fCommentMap);
	}

	private void checkBelongsToAST(IASTNode node) {
		while (node != null) {
			node= node.getParent();
			if (node == fRoot) {
				return;
			}
		}
		throw new IllegalArgumentException();
	}
	
	private void checkSupportedNode(IASTNode node, Operation op) {
		if (node instanceof IASTComment) {
			if (op != Operation.remove) {
				throw new IllegalArgumentException("Rewriting comments is not yet supported"); //$NON-NLS-1$
			}
		}
		if (node instanceof IASTPreprocessorStatement) {
			throw new IllegalArgumentException("Rewriting preprocessor statements is not yet supported"); //$NON-NLS-1$
		}
		if (node instanceof IASTProblem) {
			throw new IllegalArgumentException("Rewriting problem nodes is not supported"); //$NON-NLS-1$
		}		
	}

	/**
	 * Assigns the comment to the node.
	 * 
	 * @param node the node.
	 * @param comment the comment to be attached to the node at the given position.
	 * @param pos the position of the comment.
	 * @since 5.3
	 */
	public void addComment(IASTNode node, IASTComment comment, CommentPosition pos) {
		switch (pos) {
		case leading:
			fCommentMap.addLeadingCommentToNode(node, comment);
			break;
		case trailing:
			fCommentMap.addTrailingCommentToNode(node, comment);
			break;
		case freestanding:
			fCommentMap.addFreestandingCommentToNode(node, comment);
			break;
		}
	}

	/**
	 * Returns comments for the given node.
	 * 
	 * @param node the node
	 * @param pos the position of the comments
	 * @return All comments assigned to the node at this position
	 * @since 5.3
	 */
	public List<IASTComment> getComments(IASTNode node, CommentPosition pos) {
		switch (pos) {
		case leading:
			return fCommentMap.getLeadingCommentsForNode(node);
		case trailing:
			return fCommentMap.getTrailingCommentsForNode(node);
		case freestanding:
			return fCommentMap.getFreestandingCommentsForNode(node);
		}
		return fCommentMap.getLeadingCommentsForNode(node);
	}
}
