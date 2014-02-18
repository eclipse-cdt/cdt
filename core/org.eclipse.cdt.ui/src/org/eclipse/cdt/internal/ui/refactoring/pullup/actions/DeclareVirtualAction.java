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

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.pullup.InsertionPoint;
import org.eclipse.cdt.internal.ui.refactoring.pullup.InsertionPoint.InsertType;

class DeclareVirtualAction extends MoveMethodAction {

	public DeclareVirtualAction(MoveActionGroup group, CRefactoringContext context, 
			ICPPMember member, ICPPClassType target, int visibility) {
		super(group, context, member, target, visibility);
	}
	
	
	
	@Override
	protected void runSourceChangeForDefinition(IASTFunctionDefinition def, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		// do nothing
	}
	
	
	
	@Override
	protected void runTargetChangeForDefinition(IASTFunctionDefinition def, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		// do nothing as definition remains in source class
	}
	
	
	
	@Override
	protected void runTargetChangeForDeclaration(IASTFunctionDeclarator decl, 
			ModificationCollector mc, TextEditGroup editGroup) throws CoreException {
		
		if (decl == null) {
			// there should at least be a definition existent then
			final IASTSimpleDeclaration newDecl = this.definitionToAbstractDeclaration(
					this.definition);
			
			final InsertionPoint insertion = this.calculateInsertion(InsertType.DECLARATION);
			insertion.perform(mc, newDecl, this.definition, editGroup);
		} else {
			super.runTargetChangeForDeclaration(decl, mc, editGroup);
		}
	}
	
	
	
	@Override
	protected IASTDeclaration copyDeclarationFor(
			IASTCompositeTypeSpecifier target, IASTFunctionDeclarator decl, 
			boolean makePureVirtual) {
		// always make declaration pure virtual
		return super.copyDeclarationFor(target, decl, true);
	}
	
	
	
	@Override
	protected IASTDeclaration copyDefinitionFor(IASTCompositeTypeSpecifier source,
			IASTCompositeTypeSpecifier target, InsertionPoint insertion, 
			boolean insertVirtual,
			IASTFunctionDefinition definition) {
		// always insert 'virtual'
		return super.copyDefinitionFor(source, target, insertion, true, definition);
	}
}