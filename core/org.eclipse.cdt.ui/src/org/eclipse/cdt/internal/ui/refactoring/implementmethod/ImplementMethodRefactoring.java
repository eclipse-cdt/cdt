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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateDeclaration;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.DefinitionFinder;
import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.PseudoNameGenerator;
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
	private final Set<String> generatedNames = new HashSet<String>();
	
	public ImplementMethodRefactoring(IFile file, ISelection selection, ICElement element) {
		super(file, selection, element);
	}
	
	public boolean needsAdditionalArgumentNames() {
		for (IASTParameterDeclaration parameterDeclaration : getParameters()) {
			if(parameterDeclaration.getDeclarator().getName().toCharArray().length < 1) {
				return true;
			}
		}
		return false;
	}

	public IASTParameterDeclaration[] getParameters() {
		if(methodDeclaration.getDeclarators().length < 1) {
			return null;
		}
		return ((ICPPASTFunctionDeclarator) methodDeclaration.getDeclarators()[0]).getParameters();
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 8);
		super.checkInitialConditions(sm.newChild(6));

		methodDeclaration = SelectionHelper.findFirstSelectedDeclaration(region, unit);

		if (methodDeclaration == null) {
			initStatus.addFatalError("No method selected"); //$NON-NLS-1$
			return initStatus;
		}
		
		if(isProgressMonitorCanceld(sm, initStatus))return initStatus;
		sm.worked(1);
		
		if (DefinitionFinder.getDefinition(methodDeclaration, file) != null) {
			initStatus.addFatalError("This method already has an implementation."); //$NON-NLS-1$
			return initStatus;
		}
		if(isProgressMonitorCanceld(sm, initStatus))return initStatus;
		sm.worked(1);
		sm.done();
		return initStatus;
	}


	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException,	OperationCanceledException {
		findInsertLocation();
 		IASTTranslationUnit targetUnit = insertLocation.getTargetTranslationUnit();
 		IASTNode parent = insertLocation.getPartenOfNodeToInsertBefore();
		IASTNode insertNode = createFunctionDefinition(targetUnit);
		IASTNode nodeToInsertBefore = insertLocation.getNodeToInsertBefore();
		ASTRewrite rewrite = collector.rewriterForTranslationUnit(targetUnit);
		rewrite.insertBefore(parent, nodeToInsertBefore, insertNode, null);
	}

	private void findInsertLocation() throws CoreException {
		insertLocation = MethodDefinitionInsertLocationFinder.find(
				methodDeclaration.getFileLocation(), methodDeclaration
						.getParent(), file);

		if (!insertLocation.hasFile()) {
			insertLocation.setInsertFile(file);
			insertLocation.setNodeToInsertAfter(NodeHelper
					.getTopLevelParent(methodDeclaration));
		}
	}

	private IASTNode createFunctionDefinition(IASTTranslationUnit unit) {
		return createFunctionDefinition(
				methodDeclaration.getDeclSpecifier(), 
				(ICPPASTFunctionDeclarator) methodDeclaration.getDeclarators()[0], 
				methodDeclaration.getParent(), unit);
	}
	
	public IASTNode createFunctionDefinition() {
		return createFunctionDefinition(unit);
	}

	private IASTNode createFunctionDefinition(IASTDeclSpecifier declSpecifier, ICPPASTFunctionDeclarator functionDeclarator, IASTNode declarationParent, IASTTranslationUnit unit) {
		
		IASTFunctionDefinition func = new CPPASTFunctionDefinition();
		func.setParent(unit);
		
		if(declSpecifier instanceof ICPPASTDeclSpecifier) {
			((ICPPASTDeclSpecifier) declSpecifier).setVirtual(false); 
		}
		
		if(Path.fromOSString(methodDeclaration.getNodeLocations()[0].asFileLocation().getFileName()).equals(insertLocation.getInsertFile().getLocation())) {
			declSpecifier.setInline(true);
		}
		
		if(declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_static) {
			declSpecifier.setStorageClass(IASTDeclSpecifier.sc_unspecified);
		}
		
		func.setDeclSpecifier(declSpecifier);
		
		ICPPASTQualifiedName qname = createQualifiedNameFor(functionDeclarator, declarationParent);
		
		CPPASTFunctionDeclarator newFunctionDeclarator = new CPPASTFunctionDeclarator();
		newFunctionDeclarator.setName(qname);
		newFunctionDeclarator.setConst(functionDeclarator.isConst());
		
		PseudoNameGenerator pseudoNameGenerator = new PseudoNameGenerator();

			for(IASTParameterDeclaration parameter : getParameters()) {
				if(parameter.getDeclarator().getName().toString().length() > 0) {
					pseudoNameGenerator.addExistingName(parameter.getDeclarator().getName().toString());
				}
			}
		
		for(IASTParameterDeclaration parameter : getParameters()) {
			if(parameter.getDeclarator().getName().toString().length() < 1) {
				
				IASTDeclSpecifier parameterDeclSpecifier = parameter.getDeclSpecifier();
				String typeName;
				if(parameterDeclSpecifier instanceof ICPPASTNamedTypeSpecifier) {
					typeName = ((ICPPASTNamedTypeSpecifier) parameterDeclSpecifier).getName().getRawSignature();
				} else {
					typeName = parameterDeclSpecifier.getRawSignature();
				}
				
				String generateNewName = pseudoNameGenerator.generateNewName(typeName);
				generatedNames.add(generateNewName);
				parameter.getDeclarator().setName(new CPPASTName(generateNewName.toCharArray()));
			}
			newFunctionDeclarator.addParameterDeclaration(parameter);
		}
		
		removeAllDefaultParameters(newFunctionDeclarator);
		
		func.setDeclarator(newFunctionDeclarator);
		func.setBody(new CPPASTCompoundStatement());
		
		if(classHasTemplates(declarationParent)) {
			CPPASTTemplateDeclaration templateDeclaration = new CPPASTTemplateDeclaration();
			templateDeclaration.setParent(unit);
			
			for(ICPPASTTemplateParameter templateParameter : ((ICPPASTTemplateDeclaration) declarationParent.getParent().getParent() ).getTemplateParameters()) {
				templateDeclaration.addTemplateParamter(templateParameter);
			}
			
			templateDeclaration.setDeclaration(func);
			return templateDeclaration;
		}
		
		return func;
	}

	private void removeAllDefaultParameters(ICPPASTFunctionDeclarator functionDeclarator) {
		for (IASTParameterDeclaration parameterDeclaration : functionDeclarator.getParameters()) {
			parameterDeclaration.getDeclarator().setInitializer(null);
		}
	}

	private ICPPASTQualifiedName createQualifiedNameFor(IASTFunctionDeclarator functionDeclarator, IASTNode declarationParent) {
		
		int insertOffset;
		if(insertLocation.getNodeToInsertBefore() == null) {
			insertOffset = 0;
		} else {
			insertOffset = insertLocation.getPartenOfNodeToInsertBefore().getFileLocation().getNodeOffset();
		}
		return NameHelper.createQualifiedNameFor(functionDeclarator.getName(), file, region.getOffset(), insertLocation.getInsertFile(), insertOffset);
	}

	private boolean classHasTemplates(IASTNode declarationParent) {
		return declarationParent.getParent() != null && declarationParent.getParent().getParent() instanceof ICPPASTTemplateDeclaration;
	}

	public IASTSimpleDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}

	public Set<String> getGeneratedNames() {
		return generatedNames;
	}
}
