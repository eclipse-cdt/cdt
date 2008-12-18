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
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateDeclaration;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.DefinitionFinder;
import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;

/**
 * Main class of the ImplementMethodRefactoring (Source generator).
 * Checks conditions, finds insert location and generates the ImplementationNode.
 * 
 * @author Mirko Stocker, Lukas Felber
 * 
 */
public class ImplementMethodRefactoring extends CRefactoring {

	private IASTSimpleDeclaration methodDeclaration;
	private InsertLocation insertLocation;
	private ParameterHandler parameterHandler;
	private IASTDeclaration createdMethodDefinition;
	private CPPASTFunctionDeclarator createdMethodDeclarator;
	
	public ImplementMethodRefactoring(IFile file, ISelection selection, ICElement element) {
		super(file, selection, element);
		parameterHandler = new ParameterHandler(this);
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		super.checkInitialConditions(sm.newChild(6));

		methodDeclaration = SelectionHelper.findFirstSelectedDeclaration(region, unit);

		if (!NodeHelper.isMethodDeclaration(methodDeclaration)) {
			initStatus.addFatalError(Messages.ImplementMethodRefactoring_NoMethodSelected);
			return initStatus;
		}
		
		if(isProgressMonitorCanceld(sm, initStatus))return initStatus;
		sm.worked(1);
		
		if (DefinitionFinder.getDefinition(methodDeclaration, file) != null) {
			initStatus.addFatalError(Messages.ImplementMethodRefactoring_MethodHasImpl);
			return initStatus;
		}
		if(isProgressMonitorCanceld(sm, initStatus))return initStatus;
		sm.worked(1);
		sm.done();
		parameterHandler.initArgumentNames();
		sm.worked(1);
		sm.done();
		insertLocation = findInsertLocation();
		sm.worked(1);
		sm.done();
		return initStatus;
	}


	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException,	OperationCanceledException {
		IASTTranslationUnit targetUnit = insertLocation.getTargetTranslationUnit();
 		IASTNode parent = insertLocation.getPartenOfNodeToInsertBefore();
 		createFunctionDefinition(targetUnit);
		IASTNode nodeToInsertBefore = insertLocation.getNodeToInsertBefore();
		ASTRewrite translationUnitRewrite = collector.rewriterForTranslationUnit(targetUnit);
		ASTRewrite methodRewrite = translationUnitRewrite.insertBefore(parent, nodeToInsertBefore, createdMethodDefinition, null);
		
		createParameterModifications(methodRewrite);
	}
	
	private void createParameterModifications(ASTRewrite methodRewrite) {
		for(ParameterInfo actParameterInfo : parameterHandler.getParameterInfos()) {
			ASTRewrite parameterRewrite = methodRewrite.insertBefore(createdMethodDeclarator, null, actParameterInfo.getParameter(), null);
			createNewNameInsertModification(actParameterInfo, parameterRewrite);
			createRemoveDefaultValueModification(actParameterInfo, parameterRewrite);
		}
		
	}

	private void createRemoveDefaultValueModification(ParameterInfo parameterInfo, ASTRewrite parameterRewrite) {
		if(parameterInfo.hasDefaultValue()) {
			parameterRewrite.remove(parameterInfo.getDefaultValueNode(), null);
		}
	}

	private void createNewNameInsertModification(ParameterInfo parameterInfo, ASTRewrite parameterRewrite) {
		if(parameterInfo.hasNewName()) {
			IASTNode insertNode = parameterInfo.getNewNameNode();
			IASTName replaceNode = parameterInfo.getNameNode();
			parameterRewrite.replace(replaceNode, insertNode, null);
		}
	}

	private InsertLocation findInsertLocation() throws CoreException {
		InsertLocation insertLocation = MethodDefinitionInsertLocationFinder.find(methodDeclaration.getFileLocation(), methodDeclaration.getParent(), file);

		if (!insertLocation.hasFile() || NodeHelper.isContainedInTemplateDeclaration(methodDeclaration)) {
			insertLocation.setInsertFile(file);
			insertLocation.setNodeToInsertAfter(NodeHelper.findTopLevelParent(methodDeclaration));
		}
		
		return insertLocation;
	}

	private void createFunctionDefinition(IASTTranslationUnit unit) throws CoreException {
		createFunctionDefinition(
				methodDeclaration.getDeclSpecifier().copy(), 
				(ICPPASTFunctionDeclarator) methodDeclaration.getDeclarators()[0], 
				methodDeclaration.getParent(), unit);
	}
	
	public IASTDeclaration createFunctionDefinition() throws CoreException {
		createFunctionDefinition(unit);
		return createdMethodDefinition;		
	}

	private void createFunctionDefinition(IASTDeclSpecifier declSpecifier, ICPPASTFunctionDeclarator functionDeclarator, 
			IASTNode declarationParent, IASTTranslationUnit unit) throws CoreException {
		
		IASTFunctionDefinition func = new CPPASTFunctionDefinition();
		func.setParent(unit);
		
		if(declSpecifier instanceof ICPPASTDeclSpecifier) {
			((ICPPASTDeclSpecifier) declSpecifier).setVirtual(false); 
		}
		
		String currentFileName = methodDeclaration.getNodeLocations()[0].asFileLocation().getFileName();
		if(Path.fromOSString(currentFileName).equals(insertLocation.getInsertFile().getLocation())) {
			declSpecifier.setInline(true);
		}
		
		if(declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_static) {
			declSpecifier.setStorageClass(IASTDeclSpecifier.sc_unspecified);
		}
		
		func.setDeclSpecifier(declSpecifier);
		
		ICPPASTQualifiedName qname = createQualifiedNameFor(functionDeclarator, declarationParent);
		
		createdMethodDeclarator = new CPPASTFunctionDeclarator();
		createdMethodDeclarator.setName(qname);
		createdMethodDeclarator.setConst(functionDeclarator.isConst());
		for(IASTPointerOperator pop : functionDeclarator.getPointerOperators()) {
			createdMethodDeclarator.addPointerOperator(pop);
		}
	
		func.setDeclarator(createdMethodDeclarator);
		func.setBody(new CPPASTCompoundStatement());
		
		if(NodeHelper.isContainedInTemplateDeclaration(declarationParent)) {
			CPPASTTemplateDeclaration templateDeclaration = new CPPASTTemplateDeclaration();
			templateDeclaration.setParent(unit);
			
			for(ICPPASTTemplateParameter templateParameter : ((ICPPASTTemplateDeclaration) declarationParent.getParent().getParent() ).getTemplateParameters()) {
				templateDeclaration.addTemplateParamter(templateParameter.copy());
			}
			
			templateDeclaration.setDeclaration(func);
			createdMethodDefinition = templateDeclaration;
			return;
		}
		createdMethodDefinition = func;
	}

	private ICPPASTQualifiedName createQualifiedNameFor(IASTFunctionDeclarator functionDeclarator, IASTNode declarationParent) 
		throws CoreException {
		int insertOffset = insertLocation.getInsertPosition();
		return NameHelper.createQualifiedNameFor(functionDeclarator.getName(), file, region.getOffset(), insertLocation.getInsertFile(), insertOffset);
	}

	public IASTSimpleDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}

	public ParameterHandler getParameterHandler() {
		return parameterHandler;
	}
}
