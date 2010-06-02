/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
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
import org.eclipse.cdt.core.model.ICProject;

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
 * @author Mirko Stocker, Lukas Felber, Emanuel Graf
 * 
 */
public class ImplementMethodRefactoring extends CRefactoring {

	private InsertLocation insertLocation;
	private CPPASTFunctionDeclarator createdMethodDeclarator;
	private ImplementMethodData data;
	
	public ImplementMethodRefactoring(IFile file, ISelection selection, ICElement element, ICProject project) {
		super(file, selection, element, project);
		data = new ImplementMethodData();
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		super.checkInitialConditions(sm.newChild(6));

		if(!initStatus.hasFatalError()) {

			data.setMethodDeclarations(findUnimplementedMethodDeclarations(unit));

			if(region.getLength()>0) {
				IASTSimpleDeclaration methodDeclaration = SelectionHelper.findFirstSelectedDeclaration(region, unit);
				if (NodeHelper.isMethodDeclaration(methodDeclaration)) {
					for (MethodToImplementConfig config : data.getMethodDeclarations()) {
						if(config.getDeclaration() == methodDeclaration) {
							config.setChecked(true);
						}
					}
				}
			}
		}
		sm.done();
		return initStatus;
	}


	private List<IASTSimpleDeclaration> findUnimplementedMethodDeclarations(
			IASTTranslationUnit unit) {
		final List<IASTSimpleDeclaration> list = new ArrayList<IASTSimpleDeclaration>();
		unit.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
					try {
						if(NodeHelper.isMethodDeclaration(simpleDeclaration) && DefinitionFinder.getDefinition(simpleDeclaration, file) == null) {
							list.add(simpleDeclaration);
						}
					} catch (CoreException e) {}
				}
				return ASTVisitor.PROCESS_CONTINUE;
			}
			
			
			
		});
		return list;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException,	OperationCanceledException {
		
		List<MethodToImplementConfig> methodsToImplement = data.getMethodsToImplement();
		SubMonitor sm = SubMonitor.convert(pm, 4*methodsToImplement.size());
		for(MethodToImplementConfig config : methodsToImplement) {
			createDefinition(collector, config, sm.newChild(4));
		}		
	}

	protected void createDefinition(ModificationCollector collector,
			MethodToImplementConfig config, IProgressMonitor subMonitor) throws CoreException {
		IASTSimpleDeclaration decl = config.getDeclaration();
		insertLocation = findInsertLocation(decl);
		subMonitor.worked(1);
		IASTTranslationUnit targetUnit = insertLocation.getTargetTranslationUnit();
		IASTNode parent = insertLocation.getPartenOfNodeToInsertBefore();
		ASTRewrite translationUnitRewrite = collector.rewriterForTranslationUnit(targetUnit);
		subMonitor.worked(1);

		IASTNode nodeToInsertBefore = insertLocation.getNodeToInsertBefore();
		IASTNode createdMethodDefinition = createFunctionDefinition(targetUnit, decl);
		subMonitor.worked(1);
		ASTRewrite methodRewrite = translationUnitRewrite.insertBefore(parent, nodeToInsertBefore, createdMethodDefinition , null);
		createParameterModifications(methodRewrite, config.getParaHandler());
		subMonitor.done();
	}
	
	private void createParameterModifications(ASTRewrite methodRewrite, ParameterHandler handler) {
		for(ParameterInfo actParameterInfo : handler.getParameterInfos()) {
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

	private InsertLocation findInsertLocation(IASTSimpleDeclaration methodDeclaration) throws CoreException {
		InsertLocation insertLocation = MethodDefinitionInsertLocationFinder.find(methodDeclaration.getFileLocation(), methodDeclaration.getParent(), file);

		if (!insertLocation.hasFile() || NodeHelper.isContainedInTemplateDeclaration(methodDeclaration)) {
			insertLocation.setInsertFile(file);
			insertLocation.setNodeToInsertAfter(NodeHelper.findTopLevelParent(methodDeclaration));
		}
		
		return insertLocation;
	}

	private IASTDeclaration createFunctionDefinition(IASTTranslationUnit unit, IASTSimpleDeclaration methodDeclaration) throws CoreException {
		return createFunctionDefinition(
				methodDeclaration.getDeclSpecifier().copy(), 
				(ICPPASTFunctionDeclarator) methodDeclaration.getDeclarators()[0], 
				methodDeclaration.getParent(), unit);
	}

	private IASTDeclaration createFunctionDefinition(IASTDeclSpecifier declSpecifier, ICPPASTFunctionDeclarator functionDeclarator, 
			IASTNode declarationParent, IASTTranslationUnit unit) throws CoreException {
		
		IASTFunctionDefinition func = new CPPASTFunctionDefinition();
		func.setParent(unit);
		
		if(declSpecifier instanceof ICPPASTDeclSpecifier) {
			((ICPPASTDeclSpecifier) declSpecifier).setVirtual(false); 
		}
		
		String currentFileName = declarationParent.getNodeLocations()[0].asFileLocation().getFileName();
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
			createdMethodDeclarator.addPointerOperator(pop.copy());
		}
	
		func.setDeclarator(createdMethodDeclarator);
		func.setBody(new CPPASTCompoundStatement());
		
		if(NodeHelper.isContainedInTemplateDeclaration(declarationParent)) {
			CPPASTTemplateDeclaration templateDeclaration = new CPPASTTemplateDeclaration();
			templateDeclaration.setParent(unit);
			
			for(ICPPASTTemplateParameter templateParameter : ((ICPPASTTemplateDeclaration) declarationParent.getParent().getParent() ).getTemplateParameters()) {
				templateDeclaration.addTemplateParameter(templateParameter.copy());
			}
			
			templateDeclaration.setDeclaration(func);
			return templateDeclaration;
		}
		return func;
	}

	private ICPPASTQualifiedName createQualifiedNameFor(IASTFunctionDeclarator functionDeclarator, IASTNode declarationParent) 
		throws CoreException {
		int insertOffset = insertLocation.getInsertPosition();
		return NameHelper.createQualifiedNameFor(functionDeclarator.getName(), file, functionDeclarator.getFileLocation().getNodeOffset(), insertLocation.getInsertFile(), insertOffset);
	}
	
	public ImplementMethodData getRefactoringData() {
		return data;
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		// TODO egraf add Descriptor
		return null;
	}
}
