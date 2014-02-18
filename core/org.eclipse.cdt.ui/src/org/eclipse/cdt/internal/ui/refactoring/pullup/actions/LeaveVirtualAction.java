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
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.pullup.InsertionPoint;
import org.eclipse.cdt.internal.ui.refactoring.pullup.InsertionPoint.InsertType;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpHelper;

/**
 * This action removes the existing definition/declaration from the source class and
 * inserts a pure virtual declaration instead.
 * 
 * @author Simon Taddiken
 */
class LeaveVirtualAction extends MoveMethodAction {

	private boolean declarationInserted;
	
	public LeaveVirtualAction(MoveActionGroup group, CRefactoringContext context, 
			ICPPMember member, ICPPClassType target, int visibility) {
		super(group, context, member, target, visibility);
	}
	
	
	
	@Override
	protected void runForDeclaration(IASTFunctionDeclarator decl, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		
		if (decl == null || this.declarationInserted) {
			return;
		}
		
		
		final IASTSimpleDeclaration origDeclaration = CPPVisitor.findAncestorWithType(
				decl, IASTSimpleDeclaration.class);
		
		final IASTSimpleDeclaration copyDeclaration = origDeclaration.copy();
		final IASTFunctionDeclarator copyDecl = (IASTFunctionDeclarator) copyDeclaration.getDeclarators()[0];
		final ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) copyDeclaration.getDeclSpecifier();
		declSpec.setVirtual(true);
		final ICPPNodeFactory nf = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
		copyDecl.setInitializer(nf.newEqualsInitializer(nf.newLiteralExpression(
				IASTLiteralExpression.lk_integer_constant, "0"))); //$NON-NLS-1$
		
		final IASTTranslationUnit targetAst = this.sourceClass.getTranslationUnit();
		final ASTRewrite rwx = mc.rewriterForTranslationUnit(targetAst);
		
		if (this.member.getVisibility() == this.visibility) {
			// replace, because same visibility
			rwx.replace(origDeclaration, copyDeclaration, editGroup);
		} else {
			// remove existing, insert at target visibility
			rwx.remove(origDeclaration, editGroup);
			final InsertionPoint insertion = this.calculateInsertion(InsertType.DECLARATION);
			insertion.perform(mc, copyDeclaration, decl, editGroup);
		}
		this.declarationInserted = true;
	}
	
	
	
	@Override
	protected void runForDefinition(IASTFunctionDefinition def, ModificationCollector mc,
			TextEditGroup editGroup) throws CoreException {
		
		if (def == null || this.declarationInserted) {
			return;
		}
		
		// remove definition
		final IASTTranslationUnit defAst = def.getTranslationUnit();
		ASTRewrite rwx = mc.rewriterForTranslationUnit(defAst);
		rwx.remove(PullUpHelper.findRemovePoint(def), editGroup);
	
		// create virtual declaration from definition
		final IASTSimpleDeclaration declaration = this.definitionToAbstractDeclaration(def);
		
		final InsertionPoint insertion = this.calculateInsertion(InsertType.DECLARATION);
		insertion.perform(mc, declaration, def, editGroup);
		this.declarationInserted = true;
	}
	
	
	
	@Override
	protected void runTargetChangeForDeclaration(IASTFunctionDeclarator decl, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		// no target changes
	}
	
	
	
	@Override
	protected void runTargetChangeForDefinition(IASTFunctionDefinition def, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		// no target changes			
	}
}