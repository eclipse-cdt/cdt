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
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.ui.refactoring.AddDeclarationNodeToClassChange;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * @author Thomas Corbat
 * 
 */
public class GenerateGettersAndSettersRefactoring extends CRefactoring {

	private static final String MEMBER_DECLARATION = "MEMBER_DECLARATION"; //$NON-NLS-1$
	private final GetterAndSetterContext context = new GetterAndSetterContext();	
	
	public GenerateGettersAndSettersRefactoring(IFile file, ISelection selection, ICElement element) {
		super(file, selection, element);
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		super.checkInitialConditions(sm.newChild(6));

		initRefactoring(pm);		
		
		return initStatus;
	}

	private void initRefactoring(IProgressMonitor pm) {
		loadTranslationUnit(initStatus, pm);
		context.setUnit(unit);
		findDeclarations();
		
	}
	
	protected void findDeclarations() {

		unit.accept(new CPPASTVisitor() {

			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration fieldDeclaration = (IASTSimpleDeclaration) declaration;
					ASTNodeProperty props = fieldDeclaration.getPropertyInParent();
					if (props.getName().contains(MEMBER_DECLARATION)) {
						final IASTDeclarator[] declarators = fieldDeclaration.getDeclarators();
						if (declarators.length > 0) {
							if ((declarators[0] instanceof IASTFunctionDeclarator)) {
								context.existingFunctionDeclarations.add(fieldDeclaration);
							} else {
								if(SelectionHelper.isInSameFile(fieldDeclaration, file)){
									context.existingFields.add(fieldDeclaration);
								}
							}
						}
					}
				}
				if (declaration instanceof IASTFunctionDefinition) {
					IASTFunctionDefinition functionDefinition = (IASTFunctionDefinition) declaration;
					ASTNodeProperty props = functionDefinition.getPropertyInParent();
					if (props.getName().contains(MEMBER_DECLARATION)) {
						context.existingFunctionDefinitions.add(functionDefinition);
					}
				}
				return super.visit(declaration);
			}
		});
	}

	@Override
	protected void collectModifications(IProgressMonitor pm,ModificationCollector collector) throws CoreException, OperationCanceledException {
// egtodo		
//		ASTRewrite rewriter = collector.rewriterForTranslationUnit(unit);
		
		for(GetterSetterInsertEditProvider currentProvider : context.selectedFunctions){
// egtodo			
//			TextEditGroup editGroup = new TextEditGroup(Messages.GenerateGettersAndSettersRefactoring_Insert + currentProvider.toString());
			ICPPASTCompositeTypeSpecifier classDefinition = (ICPPASTCompositeTypeSpecifier) context.existingFunctionDeclarations.get(context.existingFunctionDeclarations.size()-1).getParent();
			AddDeclarationNodeToClassChange.createChange(classDefinition, VisibilityEnum.v_public, currentProvider.getFunction(), false, collector);
		}		
	}

	public GetterAndSetterContext getContext() {
		return context;
	}

}
