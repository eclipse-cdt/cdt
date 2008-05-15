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

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

import org.eclipse.cdt.internal.ui.refactoring.AddDeclarationNodeToClassChange;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.DeclarationFinder;
import org.eclipse.cdt.internal.ui.refactoring.utils.DeclarationFinderDO;
import org.eclipse.cdt.internal.ui.refactoring.utils.ExpressionFinder;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.TranslationUnitHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * @author Guido Zgraggen IFS
 * 
 */
public class HideMethodRefactoring extends CRefactoring {

	private IASTName methodToHide;
	private IASTDeclaration methodToHideDecl;
	private DeclarationFinderDO declData;

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
		
		declData = DeclarationFinder.getDeclaration(name, getIndex());
		
		if(declData == null || declData.name == null) {
			initStatus.addFatalError(Messages.HideMethodRefactoring_NoMethodNameSelected); 
			return initStatus;
		}
		
		methodToHide = declData.name;
		sm.worked(1);
		methodToHideDecl = NodeHelper.findSimpleDeclarationInParents(methodToHide);
		if(methodToHideDecl == null) {
			initStatus.addFatalError(Messages.HideMethodRefactoring_CanOnlyHideMethods); 
			return initStatus;
		}
		if(!(methodToHideDecl.getParent() instanceof ICPPASTCompositeTypeSpecifier)) {
			methodToHideDecl = NodeHelper.findFunctionDefinition(methodToHide);
		}

		if(isProgressMonitorCanceld(sm, initStatus)) return initStatus;
		sm.worked(1);
		if(methodToHideDecl instanceof IASTFunctionDefinition) {
			IASTDeclarator declarator = ((IASTFunctionDefinition)methodToHideDecl).getDeclarator();
			if(declarator.getName().getRawSignature().equals(name.getRawSignature())) {
				if (!(declarator instanceof IASTFunctionDeclarator)) {
					initStatus.addFatalError(Messages.HideMethodRefactoring_CanOnlyHideMethods); 
					return initStatus;
				}
			}
		}else if (methodToHideDecl instanceof IASTSimpleDeclaration) {
			for(IASTDeclarator declarator : ((IASTSimpleDeclaration) methodToHideDecl).getDeclarators()) {
				if(declarator.getName().getRawSignature().equals(name.getRawSignature())) {
					if (!(declarator instanceof IASTFunctionDeclarator)) {
						initStatus.addFatalError(Messages.HideMethodRefactoring_CanOnlyHideMethods); 
						return initStatus;
					}
				}
			}			
		}else {
			initStatus.addFatalError(Messages.HideMethodRefactoring_CanOnlyHideMethods); 
			return initStatus;
		}
		
		sm.worked(1);		

		IASTCompositeTypeSpecifier classNode = NodeHelper.findEnclosingClass(methodToHide);
		if(classNode == null) {
			initStatus.addError(Messages.HideMethodRefactoring_EnclosingClassNotFound);
		}
		
		if(checkIfPrivate(classNode, methodToHideDecl)) {
			initStatus.addError(Messages.HideMethodRefactoring_IsAlreadyPrivate);
		}
		sm.done();
		return initStatus;
	}
	
	private boolean checkIfPrivate(IASTCompositeTypeSpecifier classNode, IASTDeclaration decl) {
		IASTDeclaration[] members = classNode.getMembers();
		
		int currentVisibility = ICPPASTVisibilityLabel.v_private;
		if(IASTCompositeTypeSpecifier.k_struct == classNode.getKey()) {
			currentVisibility = ICPPASTVisibilityLabel.v_public;
		}		
		for (IASTDeclaration declaration : members) {
			if(declaration instanceof ICPPASTVisibilityLabel){
				currentVisibility =((ICPPASTVisibilityLabel) declaration).getVisibility();
			}
			
			if (declaration != null) {
				if(decl == declaration) {
					break;
				}
			}
		}
		if(ICPPASTVisibilityLabel.v_private == currentVisibility) {
			return true;
		}
		return false;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		RefactoringStatus finalConditions = super.checkFinalConditions(pm);

		for(IIndexName pdomref : declData.allNamesPDom) {
			declData.filename = pdomref.getFileLocation().getFileName();

			if(pdomref instanceof PDOMName) {
				PDOMName pdomName = (PDOMName)pdomref;
				if(pdomName.isDeclaration()) {
					continue;
				}
				if(pdomName.isDefinition()) {
					continue;
				}
			}			
			
			IASTTranslationUnit transUtmp = TranslationUnitHelper.loadTranslationUnit(declData.filename);
			IASTName expName = ExpressionFinder.findExpressionInTranslationUnit(transUtmp, pdomref);
			
			IASTFunctionDeclarator funcDec = findEnclosingFunction(expName);
			IASTCompositeTypeSpecifier encClass2;
			if(funcDec == null) {
				encClass2 = NodeHelper.findEnclosingClass(expName);
			}
			else {
				encClass2 = NodeHelper.findEnclosingClass(funcDec);
			}
			
			IASTCompositeTypeSpecifier encClass = NodeHelper.findEnclosingClass(methodToHide);

			if(!NodeHelper.isSameNode(encClass, encClass2)) {
				finalConditions.addWarning(Messages.HideMethodRefactoring_HasExternalReferences);
				break;
			}
		}
		return finalConditions;	
	}

	private IASTFunctionDeclarator findEnclosingFunction(IASTNode node) throws CoreException {
		IASTCompoundStatement compStat = NodeHelper.findCompoundStatementInParent(node);
		if(compStat == null) {
			return null;
		}

		IASTNode parent = compStat.getParent();
		if(parent instanceof IASTFunctionDefinition) {
			IASTDeclarator declarator = ((IASTFunctionDefinition)parent).getDeclarator();
			IASTName declaratorName = getLastName(declarator);

			DeclarationFinderDO data = DeclarationFinder.getDeclaration(declaratorName, getIndex());

			if(data == null || data.name == null) {
				return null;
			}
			
			if(data.name.getParent() instanceof IASTFunctionDeclarator) {
				return (IASTFunctionDeclarator) data.name.getParent();
			}			
			return null;
		}else if(parent instanceof IASTTranslationUnit) {
			return null;
		}
		return findEnclosingFunction(parent);
	}

	private IASTName getLastName(IASTDeclarator declarator) {
		IASTName declaratorName = declarator.getName();
		if(declaratorName instanceof ICPPASTQualifiedName) {
			IASTName[] declaratorNames = ((ICPPASTQualifiedName)declaratorName).getNames();
			declaratorName = declaratorNames[declaratorNames.length-1];
		}
		return declaratorName;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException,	OperationCanceledException {
		ASTRewrite rewriter = collector.rewriterForTranslationUnit(declData.transUnit);
		TextEditGroup editGroup = new TextEditGroup(Messages.HideMethodRefactoring_FILE_CHANGE_TEXT+ methodToHide.getRawSignature());
		
		ICPPASTCompositeTypeSpecifier classDefinition = (ICPPASTCompositeTypeSpecifier) methodToHideDecl.getParent();
		AddDeclarationNodeToClassChange.createChange(classDefinition, VisibilityEnum.v_private, methodToHideDecl, false, collector);

		rewriter.remove(methodToHideDecl, editGroup);
	}
}
