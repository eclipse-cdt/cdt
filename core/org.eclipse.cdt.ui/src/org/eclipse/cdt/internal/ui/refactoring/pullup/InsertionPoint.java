/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup;

import java.util.List;
import java.util.Map;

import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite.CommentPosition;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.CPPASTAllVisitor;

/**
 * Calculates suitable insertion points for moved class members. Calculation obeys target 
 * visibility and chooses the correct target file (header or cpp). The returned
 * class can then be used to perform the insertion using 
 * {@link #perform(ModificationCollector, IASTNode, IASTNode, TextEditGroup)}
 *  
 * @author Simon Taddiken
 */
public final class InsertionPoint {

	/** Type of the member to insert */
	public static enum InsertType {
		/** Definition is being inserted */
		DEFINITION,
		/** Declaration is being inserted */
		DECLARATION
	}
	
	
	
	/**
	 * Represents the result of determining the insertion's target AST
	 * 
	 * @author Simon Taddiken
	 */
	private static class InsertionTarget {
		/** The translation unit in which the member is inserted */
		private IASTTranslationUnit targetAst;
		
		/** The class specification if member is to be inserted within it */
		private IASTCompositeTypeSpecifier targetClassDef;
		
		/** Whether a declaration must be created from the existing definition*/
		private boolean mustTransformDefinitionToDeclaration;
		
		public InsertionTarget(IASTTranslationUnit targetAst, 
				IASTCompositeTypeSpecifier targetClassDef,
				boolean mustTransformDefinitionToDeclaration) {
			super();
			this.targetAst = targetAst;
			this.targetClassDef = targetClassDef;
			this.mustTransformDefinitionToDeclaration = mustTransformDefinitionToDeclaration;
		}
		
		
		
		/**
		 * Determines whether the member is to be inserted within a class definition.
		 * @return Whether to insert within a class definition.
		 */
		public boolean isWithinClass() {
			return this.targetClassDef != null;
		}
	}

	
	
	/**
	 * Calculates an insertion point.
	 * 
	 * @param context Current refactoring context.
	 * @param labels Maps a visibility to its AST node. Needed for labels which
	 * 			are created during a refactoring but not finally inserted into the AST.
	 * @param targetVisibility The target visibility of the member to insert.
	 * @param declaration The declaration of the member to move. <code>null</code> if
	 * 			member has no declaration.
	 * @param definition The definition of the member to move. <code>null</code> if 
	 * 			member has no definition.
	 * @param targetClass Target class definition.
	 * @param type Insertion type.
	 * @return The calculated insertion point.
	 */
	public static InsertionPoint calculate(CRefactoringContext context, 
			Map<Integer, ICPPASTVisibilityLabel> labels, 
			int targetVisibility, IASTNode declaration, IASTNode definition, 
			IASTCompositeTypeSpecifier targetClass,
			InsertType type) {
		
		final InsertionPoint insertion = new InsertionPoint(context, labels, targetVisibility, 
				targetClass);
		insertion.insertionTarget = insertion.determineTarget(declaration, 
				definition, targetClass, type);
		return insertion;
	}
	
	
	
	private final Map<Integer, ICPPASTVisibilityLabel> labels;
	private final int targetVisibility;
	private final CRefactoringContext context;
	private InsertionTarget insertionTarget;
	
	
	private InsertionPoint(CRefactoringContext context, 
			Map<Integer, ICPPASTVisibilityLabel> labels, 
			int targetVisibility, 
			IASTCompositeTypeSpecifier targetClass) {
		this.context = context;
		this.labels = labels;
		this.targetVisibility = targetVisibility;
	}
	
	
	
	/**
	 * Returns whether this instance represents a location within a class definition.
	 * 
	 * @return Whether this instance represents a location within a class definition. 
	 */
	public boolean isWithinClassDefinition() {
		return this.insertionTarget.isWithinClass();
	}
	
	
	
	public boolean mustTransformDefinitionToDeclaration() {
		return this.insertionTarget.mustTransformDefinitionToDeclaration;
	}
	
	
	
	/**
	 * Performs the insertion of the specified node at the location represented by this
	 * instance.
	 * @param mc ModificationCollector for retrieving {@link ASTRewrite} instance for the
	 * 			target AST.
	 * @param newNode The node to insert.
	 * @param commentSource Node from which leading/trailing comments are fetched and 
	 * 			added to the target node. May be <code>null</code>
	 * @param editGroup EditGroup for text changes.
	 */
	public void perform(ModificationCollector mc, IASTNode newNode, 
			IASTNode commentSource, TextEditGroup editGroup) {

		final ASTRewrite rw = mc.rewriterForTranslationUnit(
				this.insertionTarget.targetAst);
		
		if (commentSource != null) {
			final ASTRewrite commentSourceRw = mc.rewriterForTranslationUnit(
					commentSource.getTranslationUnit());
			this.copyCommentKind(commentSource, commentSourceRw, newNode, rw, CommentPosition.leading);
			this.copyCommentKind(commentSource, commentSourceRw, newNode, rw, CommentPosition.freestanding);
			this.copyCommentKind(commentSource, commentSourceRw, newNode, rw, CommentPosition.trailing);
		}
		
		if (this.insertionTarget.isWithinClass()) {
			ICPPASTVisibilityLabel label = this.findVisibility(
					this.insertionTarget.targetClassDef, this.targetVisibility);
			
			if (label == null) {
				label = ASTNodeFactoryFactory.getDefaultCPPNodeFactory()
						.newVisibilityLabel(this.targetVisibility);
				this.labels.put(this.targetVisibility, label);
				rw.insertBefore(this.insertionTarget.targetClassDef, null, label, 
						editGroup);
			}
			
			// insert the node after its label by inserting it before the label's
			// immediate sibling
			final IASTNode before = this.findNextSibling(label);
			rw.insertBefore(this.insertionTarget.targetClassDef, before, newNode, 
					editGroup);
			
			this.copyComments(newNode, rw, mc);
		} else {
			rw.insertBefore(this.insertionTarget.targetAst, null, 
					newNode, editGroup);
			this.copyComments(newNode, rw, mc);
		}
	}
	
	
	
	private void copyComments(final IASTNode root, final ASTRewrite target, final ModificationCollector mc) {
		root.accept(new CPPASTAllVisitor() {
			@Override
			public int visitAll(IASTNode node) {
				if (node == root) {
					// root node is handled extra in #perform
					return PROCESS_CONTINUE;
				}
				final IASTNode origin = node.getOriginalNode();
				if (origin != node) {
					if (origin.getTranslationUnit() != null) {
						final ASTRewrite rw = mc.rewriterForTranslationUnit(
								origin.getTranslationUnit());
						copyCommentKind(origin, rw, node, target, CommentPosition.leading);
						copyCommentKind(origin, rw, node, target, CommentPosition.trailing);
						copyCommentKind(origin, rw, node, target, CommentPosition.freestanding);
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}
	
	
	
	private void copyCommentKind(IASTNode sourceNode, ASTRewrite source, 
			IASTNode targetNode, ASTRewrite target, 
			CommentPosition pos) {
		
		final IASTNode targetOrigin = targetNode.getOriginalNode();
		if (source == target && targetOrigin != targetNode) {
			// HACK: strangely, this is handled by 
			//		 ASTWriterVisitor.writeLeadingComments
			return;
		}
		final List<IASTComment> comments = source.getComments(sourceNode, pos);
		for (final IASTComment comment : comments) {
			target.addComment(targetNode, comment, pos);
		}
	}
	
	
	
	/**
	 * Determines the target file for the insertion. Either declaration OR definition
	 * may be <code>null</code>, but not both.
	 * 
	 * @param declaration The declaration of the node to insert, or <code>null</code>
	 * @param definition The definition of the node to insert, or <code>null</code>.
	 * @param targetClass Class definition of the target class.
	 * @param type Type of the insertion to perform.
	 * @return Information about the insertion target.
	 */
	private InsertionTarget determineTarget(
			IASTNode declaration, 
			IASTNode definition,
			IASTCompositeTypeSpecifier targetClass, InsertType type) {

		// at least one must be != null
		assert declaration != null || definition != null;
		
		final IASTCompositeTypeSpecifier defClass = this.findClassDefinition(definition);
		final IASTCompositeTypeSpecifier declClass = this.findClassDefinition(declaration);
		
		// an existing declaration should imply that it is contained within class 
		// declaration
		assert (declaration != null) ? (declClass != null) : true;
		
		final boolean hasDeclaration = declaration != null;
		
		final IASTTranslationUnit targetClassAst = targetClass.getTranslationUnit();
		
		switch (type) {
		case DECLARATION:
			// declarations are always inserted within the class definition
			return new InsertionTarget(targetClassAst, targetClass, false);
			
		case DEFINITION:
			if (definition != null && defClass != null) {
				// definition for source exists and is contained within the class 
				// definition. If target has a .cpp, insert it there
				if (targetClassAst.isHeaderUnit()) {
					// this is hopefully always the case
					final IASTTranslationUnit partner = PullUpHelper.getASTForPartnerFile(
							targetClassAst, this.context);
					
					if (partner == null) {
						// no parter (.cpp) file, insert within class declaration
						return new InsertionTarget(targetClassAst, targetClass, false);
					}
					// prefer cpp
					return new InsertionTarget(partner, null, !hasDeclaration);
				}
				// fall back to simply insert into target class declaration
				return new InsertionTarget(targetClassAst, targetClass, false);
			} else if (definition != null) {
				// definition is outside the class definition
				final IASTTranslationUnit defAst = definition.getTranslationUnit();
				if (defAst.isHeaderUnit() == targetClassAst.isHeaderUnit()) {
					return new InsertionTarget(targetClassAst, null, !hasDeclaration);
				} else {
					IASTTranslationUnit ast = PullUpHelper.getASTForPartnerFile(
							targetClassAst, this.context);
					
					ast = (ast != null) ? ast : targetClassAst;
					return new InsertionTarget(ast, null, !hasDeclaration);
				}
			} else {
				// there exists no definition in source class. insert preferably in 
				// cpp file
				IASTTranslationUnit ast = targetClassAst;
				if (ast.isHeaderUnit()) {
					ast = PullUpHelper.getASTForPartnerFile(ast, this.context);
					if (ast != null) {
						// there exists a cpp file
						return new InsertionTarget(ast, null, !hasDeclaration);
					} else {
						// no cpp file for target, insert within class definition
						return new InsertionTarget(targetClassAst, targetClass, false);
					}
				} else {
					// target ast is already a cpp file
					return new InsertionTarget(ast, null, !hasDeclaration);
				}
			}
		default:
			throw new IllegalStateException();
		}
		
	}
	
	
	
	private IASTCompositeTypeSpecifier findClassDefinition(IASTNode node) {
		if (node == null) {
			return null;
		}
		return CPPVisitor.findAncestorWithType(node, IASTCompositeTypeSpecifier.class);
	}
	
	
	
	/**
	 * Finds the first label for the specified visibility in the provided target class.
	 * Before actually searching the class, the label cache {@link #visibilityMap} is 
	 * consulted to check if the label to look for has been added artificially.
	 * 
	 * @param target Class in which to search the label
	 * @param visibility The visibility of the label to search
	 * @return The found {@link ICPPASTVisibilityLabel} or <code>null</code> if none was
	 * 			found.
	 */
	private ICPPASTVisibilityLabel findVisibility(IASTCompositeTypeSpecifier target, 
			final int visibility) {
		
		if (this.labels.containsKey(visibility)) {
			return this.labels.get(visibility);
		}
		final Container<ICPPASTVisibilityLabel> result = 
					new Container<ICPPASTVisibilityLabel>();
		target.accept(new ASTVisitor() {
			{
				this.shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof ICPPASTVisibilityLabel) {
					final ICPPASTVisibilityLabel label = (ICPPASTVisibilityLabel) declaration;
					if (label.getVisibility() == visibility) {
						result.setObject(label);
						return PROCESS_ABORT;
					}
				}
				return PROCESS_CONTINUE;
			}
		});
		return result.getObject();
	}
	
	
	
	
	/**
	 * Finds the next sibling of the provided node. That is the node which immediately 
	 * follows the provided one in its parent's list of children. If the node has no 
	 * parent or is the last child of its parent, <code>null</code> is returned.
	 * 
	 * @param base A node
	 * @return The next sibling of that node.
	 */
	protected IASTNode findNextSibling(IASTNode base) {
		if (base == null || base.getParent() == null) {
			return null;
		}
		final IASTNode parent = base.getParent();
		for (int i = 0; i < parent.getChildren().length; ++i) {
			if (parent.getChildren()[i] == base && parent.getChildren().length > i + 1) {
				return parent.getChildren()[i + 1];
			}
		}
		return null;
	}
}
