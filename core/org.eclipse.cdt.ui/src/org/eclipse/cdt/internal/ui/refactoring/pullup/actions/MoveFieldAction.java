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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.pullup.InsertionPoint;
import org.eclipse.cdt.internal.ui.refactoring.pullup.InsertionPoint.InsertType;
import org.eclipse.cdt.internal.ui.refactoring.pullup.Messages;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpHelper;

/**
 * Action to move a single field declaration from one class to another. This action can
 * only be executed within a {@link MoveActionGroup} because it requires some post 
 * processing when being run together with further actions.
 * 
 * @author Simon Taddiken
 */
public class MoveFieldAction extends MoveMemberAction {

	/**
	 * Creates a new MoveFieldAction.
	 * 
	 * @param group The group in which this action is being executed.
	 * @param context The refactoring context.
	 * @param member Binding of the member to move
	 * @param target Binding of the target class
	 * @param visibility Target visibility
	 */
	public MoveFieldAction(MoveActionGroup group, CRefactoringContext context, 
			ICPPMember member, ICPPClassType target, int visibility) {
		super(group, context, member, target, visibility);
	}

	
	
	@Override
	public boolean isPossible(RefactoringStatus status, IProgressMonitor pm) {
		// search for field declaration with identical name
		final SubMonitor sm = SubMonitor.convert(pm, 
				this.target.getDeclaredFields().length);
		
		try {
			for (final ICPPField field : this.target.getDeclaredFields()) {
				if (field.getName().equals(this.member.getName())) {
					
					// find corresponding node to show source snippet as status context
					final IASTName name = PullUpHelper.findName(this.index, this.context, 
							field, IIndex.FIND_DEFINITIONS | IIndex.FIND_DECLARATIONS);
					final RefactoringStatusContext c = getStatusContext(name);
					status.addError(NLS.bind(Messages.PullUpRefactoring_declarationExists, 
							this.member.getName()), c);
					return false;
				}
				sm.worked(1);
			}
			return true;
		} finally {
			sm.done();
		}
	}
	
	
	
	@Override
	public void run(ModificationCollector mc, TextEditGroup editGroup, IProgressMonitor pm) 
			throws CoreException {
		
		final IASTName name = PullUpHelper.findName(this.index, this.context, this.member, 
				IIndex.FIND_DECLARATIONS_DEFINITIONS);
		final IASTTranslationUnit sourceAst = name.getTranslationUnit();
		final ASTRewrite sourceRw = mc.rewriterForTranslationUnit(sourceAst);
		
		final IASTDeclarator declarator = CPPVisitor.findAncestorWithType(name, 
				IASTDeclarator.class);
		final IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) declarator.getParent();
		
		if (declaration.getDeclarators().length == 1) {
			this.group.performRemove(declaration, sourceRw, editGroup);
		} else {
			// declarators within a declaration with multiple declarators will be 
			// removed later
			this.group.removeLater.add(declarator);
		}
		
		final ICPPNodeFactory nf = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
		final IASTSimpleDeclaration newDecl = nf.newSimpleDeclaration(
				declaration.getDeclSpecifier().copy());
		newDecl.addDeclarator(declarator.copy());
		
		final InsertionPoint insertion = InsertionPoint.calculate(this.context, this.group.labels, 
				this.visibility, declarator, null, this.targetClass, InsertType.DECLARATION);
		insertion.perform(mc, newDecl, declaration, editGroup);
	}
}
