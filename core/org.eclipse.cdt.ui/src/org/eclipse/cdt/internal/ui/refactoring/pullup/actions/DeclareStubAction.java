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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.pullup.InsertionPoint;
import org.eclipse.cdt.internal.ui.refactoring.pullup.InsertionPoint.InsertType;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpHelper;

/**
 * MoveAction implementation responsible for inserting method stubs at a target class.
 * This action will not remove any members from a source class. It will search for
 * the declaration and/or definition of the specified binding and then use them to
 * create a copied definition in the specified target class with an empty body.
 * 
 * @author Simon Taddiken
 */
class DeclareStubAction extends MoveMethodAction {

	/** 
	 * Stores whether definition has already been inserted. Needed if a source member
	 * has both a definition and a declaration
	 */
	private boolean definitionInserted;
	
	
	
	public DeclareStubAction(MoveActionGroup group, CRefactoringContext context, 
			ICPPMember member, ICPPClassType target, int visibility) {
		super(group, context, member, target, visibility);
	}
	
	
	
	@Override
	public boolean isPossible(RefactoringStatus status, IProgressMonitor pm) {
		// declaring stubs is always possible as by their construction
		return true;
	}
	
	
	
	@Override
	protected void runSourceChangeForDefinition(IASTFunctionDefinition def, ModificationCollector mc,
			TextEditGroup editGroup) throws CoreException {
		// do not remove definition from source class
	}
	
	
	
	@Override
	protected void runSourceChangeForDeclaration(IASTFunctionDeclarator decl, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		// do not remove declaration from source class
	}
	
	
	
	@Override
	protected void runTargetChangeForDeclaration(IASTFunctionDeclarator decl, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		if (decl == null || this.definitionInserted) {
			// member has no declaration or we already created a definition for 
			// this member
			return;
		}
		
		final IASTSimpleDeclaration declaration = 
				(IASTSimpleDeclaration) decl.getParent();
		
		// create a definition from the declaration with empty body
		final InsertionPoint insertion = this.calculateInsertion(InsertType.DEFINITION);
		final IASTFunctionDefinition stub = this.createStubFromDeclaration(
				declaration, decl, insertion);
		insertion.perform(mc, stub, decl, editGroup);
		this.definitionInserted = true;
	}
	
	
	
	@Override
	protected void runTargetChangeForDefinition(IASTFunctionDefinition def, ModificationCollector mc,
			TextEditGroup editGroup) throws CoreException {
		if (def == null || this.definitionInserted) {
			// member has no definition or we already created a definition for 
			// this member
			return;
		}
		
		// create a copy of the definition with empty body
		final InsertionPoint insertion = this.calculateInsertion(InsertType.DEFINITION);
		final IASTFunctionDefinition stub = this.createStubFromDefinition(def, insertion);
		insertion.perform(mc, stub, def, editGroup);
		this.definitionInserted = true;
	}
	
	
	
	protected IASTFunctionDefinition createStubFromDeclaration(
			IASTSimpleDeclaration decl, IASTFunctionDeclarator declarator,
			InsertionPoint insertion) {
		final ICPPNodeFactory nf = nodeFactory();
		final IASTFunctionDeclarator newDeclarator = declarator.copy();
		
		final IASTName name = PullUpHelper.prepareNameForTarget(declarator.getName(), 
				this.targetClass, insertion);
		newDeclarator.setName(name);
		if (newDeclarator instanceof ICPPASTFunctionDeclarator) {
			((ICPPASTFunctionDeclarator) newDeclarator).setPureVirtual(false);
		}
		return nf.newFunctionDefinition(decl.getDeclSpecifier().copy(), 
				newDeclarator, nf.newCompoundStatement());
	}
	
	
	
	protected IASTFunctionDefinition createStubFromDefinition(
			IASTFunctionDefinition def, InsertionPoint insertion) {
		final ICPPNodeFactory nf = nodeFactory();
		
		final IASTFunctionDefinition copy = def.copy();
		final IASTName current = def.getDeclarator().getName();
		final IASTName name = PullUpHelper.prepareNameForTarget(current, 
				this.targetClass, insertion);
		copy.getDeclarator().setName(name);
		copy.setBody(nf.newCompoundStatement());
		
		return copy;
	}
}