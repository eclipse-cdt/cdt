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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpHelper;


class RemoveFromSourceAction extends MoveMethodAction {

	public RemoveFromSourceAction(MoveActionGroup group, CRefactoringContext context, 
			ICPPMember member, ICPPClassType target, int visibility) {
		super(group, context, member, target, visibility);
	}
	
	
	
	@Override
	public boolean isPossible(RefactoringStatus status, IProgressMonitor pm) {
		// either definition or declaration might be null
		if (!checkDeclarationIsValid(status)) {
			return false;
		}
		
		return true;
	}
	
	
	
	@Override
	protected void runSourceChangeForDeclaration(IASTFunctionDeclarator decl, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		
		if (decl == null) {
			return;
		}
		
		final IASTSimpleDeclaration declaration = CPPVisitor.findAncestorWithType(
				decl, IASTSimpleDeclaration.class);
		final IASTTranslationUnit targetAst = declaration.getTranslationUnit();
		final ASTRewrite rwx = mc.rewriterForTranslationUnit(targetAst);
		final IASTNode remove = PullUpHelper.findRemovePoint(declaration);
		this.group.performRemove(remove, rwx, editGroup);
	}
	
	
	
	@Override
	protected void runSourceChangeForDefinition(IASTFunctionDefinition def, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		
		if (def == null) {
			return;
		}
		
		final IASTTranslationUnit targetAst = def.getTranslationUnit();
		final ASTRewrite rwx = mc.rewriterForTranslationUnit(targetAst);
		final IASTNode remove = PullUpHelper.findRemovePoint(def);
		this.group.performRemove(remove, rwx, editGroup);
	}
	
	
	
	@Override
	protected void runTargetChangeForDeclaration(IASTFunctionDeclarator decl, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		// do nothing
	}
	
	
	
	@Override
	protected void runTargetChangeForDefinition(IASTFunctionDefinition def, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		// do nothing
	}
}