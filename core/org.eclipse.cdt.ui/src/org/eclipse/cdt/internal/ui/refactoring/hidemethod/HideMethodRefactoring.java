/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.hidemethod;

import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.ui.refactoring.AddDeclarationNodeToClassChange;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * @author Guido Zgraggen IFS
 * 
 */
public class HideMethodRefactoring extends CRefactoring {

	private IASTName methodToMove;
	private IASTSimpleDeclaration fieldToMoveDecl;

	public HideMethodRefactoring(IFile file, ISelection selection, ICElement element) {
		super(file, selection, element);
		name = Messages.HideMethodRefactoring_HIDE_METHOD;
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		super.checkInitialConditions(sm.newChild(6));
		
		if(initStatus.hasFatalError()){
			return initStatus;
		}
		
		if(isProgressMonitorCanceld(sm, initStatus)) return initStatus;
		
		IASTName name;
		Vector<IASTName> names = findAllMarkedNames();
		if(names.size() > 1) {
			name = names.lastElement();
		} else if (names.size() < 1) {
			initStatus.addFatalError(Messages.HideMethodRefactoring_NoNameSelected);  
			return initStatus;
		}else {
			name = names.get(0);
		}
		sm.worked(1);
		if(isProgressMonitorCanceld(sm, initStatus)) return initStatus;
		
		this.methodToMove = getDeclaration(name);

		if(this.methodToMove == null) {
			initStatus.addFatalError(Messages.HideMethodRefactoring_NoMethodNameSeleceted); 
			return initStatus;
		}
		sm.worked(1);
		fieldToMoveDecl = findSimpleDeclaration(this.methodToMove);

		if(isProgressMonitorCanceld(sm, initStatus)) return initStatus;
		sm.worked(1);
		for(IASTDeclarator declarator : fieldToMoveDecl.getDeclarators()) {
			if(declarator.getName().getRawSignature().equals(name.getRawSignature())) {
				if (!(declarator instanceof IASTFunctionDeclarator)) {
					initStatus.addFatalError(Messages.HideMethodRefactoring_CanOnlyHideMethods); 
					return initStatus;
				}
			}
		}
		sm.done();
		return initStatus;
	}
	
	private Vector<IASTName> findAllMarkedNames() {
		final Vector<IASTName> namesVector = new Vector<IASTName>();		
			
		unit.accept(new CPPASTVisitor(){

			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if( isInSameFileSelection(region, name) ) {
					if (!(name instanceof ICPPASTQualifiedName)) {
						namesVector.add(name);	
					}
				}				
				return super.visit(name);
			}

		});			
		return namesVector;
	}
	
	protected IASTSimpleDeclaration findSimpleDeclaration(IASTNode fieldToFoundDecl) {
		while(fieldToFoundDecl != null){
			if (fieldToFoundDecl instanceof IASTSimpleDeclaration) {
				return (IASTSimpleDeclaration) fieldToFoundDecl;
			}
			fieldToFoundDecl = fieldToFoundDecl.getParent();
		}
		return null;
	}
	
	private IASTName getDeclaration(IASTName name) {
		DeclarationFinder df = new DeclarationFinder(name);
		unit.accept(df);
		return df.getDeclaration();
	}
	
	private class DeclarationFinder extends CPPASTVisitor{

		IASTName astName;		
		IASTName declaration = null;
		
		{
			shouldVisitDeclarators = true;
		}
		
		public DeclarationFinder(IASTName name) {
			this.astName = name;
		}

		@Override
		public int visit(IASTDeclarator declarator) {
			if(declarator instanceof ICPPASTFunctionDeclarator) {
				ICPPASTFunctionDeclarator funcDec = (ICPPASTFunctionDeclarator)declarator;
				if(funcDec.getName().getRawSignature().equals(astName.getRawSignature())){
					declaration = funcDec.getName();
					return PROCESS_ABORT;
				}
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}

		public IASTName getDeclaration() {
			return declaration;
		}
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return super.checkFinalConditions(pm);
	}
	
	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException,	OperationCanceledException {
		
		ASTRewrite rewriter = collector.rewriterForTranslationUnit(unit);
		TextEditGroup editGroup = new TextEditGroup(Messages.HideMethodRefactoring_FILE_CHANGE_TEXT+ methodToMove.getRawSignature());
	
		// egtodo
//		IASTNode parent = fieldToMoveDecl.getParent();
		
		ICPPASTCompositeTypeSpecifier classDefinition = (ICPPASTCompositeTypeSpecifier) fieldToMoveDecl.getParent();
		AddDeclarationNodeToClassChange.createChange(classDefinition, VisibilityEnum.v_private, fieldToMoveDecl, false, collector);

		rewriter.remove(fieldToMoveDecl, editGroup);
	}
}
