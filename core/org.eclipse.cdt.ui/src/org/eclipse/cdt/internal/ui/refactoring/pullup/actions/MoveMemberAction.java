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
package org.eclipse.cdt.internal.ui.refactoring.pullup.actions;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.index.IIndex;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpPushDownBase;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpHelper;

/**
 * Abstract implementation of the logic needed to move either fields or method from one
 * class to another. Subclasses can only be run within an {@link MoveActionGroup}.
 * 
 * @author Simon Taddiken
 */
abstract class MoveMemberAction implements MoveAction {
	
	/** 
	 * Short method for retrieving default node facrtory 
	 * @return The CPP Default Node Factory.
	 */
	protected static ICPPNodeFactory nodeFactory() {
		return ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
	}
	
	
	
	/** Current refactoring context */
	protected final CRefactoringContext context;
	
	/** Binding of the member to pull up */
	protected final ICPPMember member;
	
	/** Target class to pull up to */
	protected final ICPPClassType target;
	
	/** Target class definition as AST node */
	protected final IASTCompositeTypeSpecifier targetClass;
	
	/** Source class definition as AST node */
	protected final IASTCompositeTypeSpecifier sourceClass;
	
	/** Target visibility */
	protected final int visibility;
	
	/** The action group in which this action is being executed */
	protected final MoveActionGroup group;

	/** Reference to the index */
	protected final IIndex index;
	
	
	
	public MoveMemberAction(MoveActionGroup group, CRefactoringContext context, 
			ICPPMember member, ICPPClassType target, int visibility) {
		this.context = context;
		this.member = member;
		this.target = target;
		this.visibility = visibility;
		this.group = group;
		this.index = group.getIndex();
		this.targetClass = PullUpHelper.findClass(index, context, target);
		this.sourceClass = PullUpHelper.findClass(index, context, member.getClassOwner());
		assert this.targetClass != null;
	}
	
	
	
	/**
	 * Returns the current refactoring context.
	 * @return The current refactoring context.
	 */
	protected CRefactoringContext getContext() {
		return this.context;
	}
	
	
	
	/**
	 * Creates a {@link RefactoringStatusContext} object which highlights the provided
	 * node within its file. 
	 * 
	 * @param node The node to highlight.
	 * @return The context object to use with {@link RefactoringStatus}.
	 */
	public RefactoringStatusContext getStatusContext(IASTNode node) {
		final PullUpPushDownBase<?> refactoring = 
				(PullUpPushDownBase<?>) this.context.getRefactoring();
		return refactoring.getStatusContext(node);
	}
}