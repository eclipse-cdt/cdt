/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Marc-Andre Laperle
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring2;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.internal.ui.refactoring.utils.Checks;
import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;

/**
 * Main class of the ImplementMethodRefactoring (Source generator).
 * Checks conditions, finds insert location and generates the ImplementationNode.
 * 
 * @author Mirko Stocker, Lukas Felber, Emanuel Graf
 */
public class ImplementMethodRefactoring extends CRefactoring2 {
	private ICPPASTFunctionDeclarator createdMethodDeclarator;
	private ImplementMethodData data;
	private MethodDefinitionInsertLocationFinder methodDefinitionInsertLocationFinder;
	private Map<IASTSimpleDeclaration, InsertLocation> insertLocations;
	private static ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
	
	public ImplementMethodRefactoring(ICElement element, ISelection selection, ICProject project, RefactoringASTCache astCache) {
		super(element, selection, project, astCache);
		data = new ImplementMethodData();
		methodDefinitionInsertLocationFinder = new MethodDefinitionInsertLocationFinder();
		insertLocations = new HashMap<IASTSimpleDeclaration, InsertLocation>();
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		super.checkInitialConditions(sm.newChild(6));

		if (!initStatus.hasFatalError()) {
			List<IASTSimpleDeclaration> unimplementedMethodDeclarations = findUnimplementedMethodDeclarations(pm);
			if (unimplementedMethodDeclarations.isEmpty()) {
				initStatus.addFatalError(Messages.ImplementMethodRefactoring_NoMethodToImplement);
			}
			else {
				data.setMethodDeclarations(unimplementedMethodDeclarations);

				if (selectedRegion.getLength() > 0) {
					IASTSimpleDeclaration methodDeclaration = SelectionHelper.findFirstSelectedDeclaration(selectedRegion, astCache.getAST(tu, pm));
					if (NodeHelper.isMethodDeclaration(methodDeclaration)) {
						for (MethodToImplementConfig config : data.getMethodDeclarations()) {
							if (config.getDeclaration() == methodDeclaration) {
								config.setChecked(true);
							}
						}
					}
				}
			}
		}
		sm.done();
		return initStatus;
	}

	private List<IASTSimpleDeclaration> findUnimplementedMethodDeclarations(IProgressMonitor pm) throws OperationCanceledException, CoreException {
		IASTTranslationUnit ast = astCache.getAST(tu, pm);
		final List<IASTSimpleDeclaration> list = new ArrayList<IASTSimpleDeclaration>();
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
					if (NodeHelper.isMethodDeclaration(simpleDeclaration)) {
						IASTDeclarator[] declarators = simpleDeclaration.getDeclarators();
						IBinding binding = declarators[0].getName().resolveBinding();
						if (isUnimplementedMethodBinding(binding)) {
							list.add(simpleDeclaration);
							return ASTVisitor.PROCESS_SKIP;	
						}
					}
				}
				return ASTVisitor.PROCESS_CONTINUE;
			}
		});
		return list;
	}
	
	private boolean isUnimplementedMethodBinding(IBinding binding) {
		if (binding instanceof ICPPFunction) {
			if (binding instanceof ICPPMethod) {
				ICPPMethod methodBinding = (ICPPMethod) binding;
				if (methodBinding.isPureVirtual()) {
					return false; // Pure virtual not handled for now, see bug 303870
				}
			}
			
			try {
				IIndexName[] indexNames = astCache.getIndex().findNames(binding, IIndex.FIND_DEFINITIONS
								| IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
				if (indexNames.length == 0) {
					return true;
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
		
		return false;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException,	OperationCanceledException {
		List<MethodToImplementConfig> methodsToImplement = data.getMethodsToImplement();
		SubMonitor sm = SubMonitor.convert(pm, 4 * methodsToImplement.size());
		for (MethodToImplementConfig config : methodsToImplement) {
			createDefinition(collector, config, sm.newChild(4));
		}
	}

	protected void createDefinition(ModificationCollector collector,
			MethodToImplementConfig config, IProgressMonitor subMonitor) throws CoreException, OperationCanceledException {
		if (subMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		IASTSimpleDeclaration decl = config.getDeclaration();
		InsertLocation insertLocation = findInsertLocation(decl, subMonitor);
		if (subMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		
		subMonitor.worked(1);
		IASTNode parent = insertLocation.getParentOfNodeToInsertBefore();
		IASTTranslationUnit ast = parent.getTranslationUnit();
		ASTRewrite translationUnitRewrite = collector.rewriterForTranslationUnit(ast);
		subMonitor.worked(1);
		if (subMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		IASTNode nodeToInsertBefore = insertLocation.getNodeToInsertBefore();
		IASTNode createdMethodDefinition = createFunctionDefinition(ast, decl, insertLocation);
		subMonitor.worked(1);
		ASTRewrite methodRewrite = translationUnitRewrite.insertBefore(parent, nodeToInsertBefore, createdMethodDefinition , null);
		createParameterModifications(methodRewrite, config.getParaHandler());
		subMonitor.done();
	}
	
	private void createParameterModifications(ASTRewrite methodRewrite, ParameterHandler handler) {
		for (ParameterInfo actParameterInfo : handler.getParameterInfos()) {
			ASTRewrite parameterRewrite = methodRewrite.insertBefore(createdMethodDeclarator, null, actParameterInfo.getParameter(), null);
			createNewNameInsertModification(actParameterInfo, parameterRewrite);
			createRemoveDefaultValueModification(actParameterInfo, parameterRewrite);
		}
	}

	private void createRemoveDefaultValueModification(ParameterInfo parameterInfo, ASTRewrite parameterRewrite) {
		if (parameterInfo.hasDefaultValue()) {
			parameterRewrite.remove(parameterInfo.getDefaultValueNode(), null);
		}
	}

	private void createNewNameInsertModification(ParameterInfo parameterInfo, ASTRewrite parameterRewrite) {
		if (parameterInfo.hasNewName()) {
			IASTNode insertNode = parameterInfo.getNewNameNode();
			IASTName replaceNode = parameterInfo.getNameNode();
			parameterRewrite.replace(replaceNode, insertNode, null);
		}
	}

	private InsertLocation findInsertLocation(IASTSimpleDeclaration methodDeclaration, IProgressMonitor subMonitor) throws CoreException {
		if (insertLocations.containsKey(methodDeclaration)) {
			return insertLocations.get(methodDeclaration);
		}
		InsertLocation insertLocation = methodDefinitionInsertLocationFinder.find(tu, methodDeclaration.getFileLocation(), methodDeclaration.getParent(), astCache, subMonitor);
		
		if (insertLocation.getTranslationUnit() == null || NodeHelper.isContainedInTemplateDeclaration(methodDeclaration)) {
			insertLocation.setNodeToInsertAfter(NodeHelper.findTopLevelParent(methodDeclaration), tu);
		}
		insertLocations.put(methodDeclaration, insertLocation);
		return insertLocation;
	}

	private IASTDeclaration createFunctionDefinition(IASTTranslationUnit unit, IASTSimpleDeclaration methodDeclaration, InsertLocation insertLocation) throws CoreException {
		IASTDeclSpecifier declSpecifier = methodDeclaration.getDeclSpecifier().copy(CopyStyle.withLocations);
		ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) methodDeclaration.getDeclarators()[0];
		IASTNode declarationParent = methodDeclaration.getParent();
		
		if (declSpecifier instanceof ICPPASTDeclSpecifier) {
			((ICPPASTDeclSpecifier) declSpecifier).setVirtual(false);
			((ICPPASTDeclSpecifier) declSpecifier).setExplicit(false);
		}
		
		String currentFileName = declarationParent.getNodeLocations()[0].asFileLocation().getFileName();
		if (Path.fromOSString(currentFileName).equals(insertLocation.getFile().getLocation())) {
			declSpecifier.setInline(true);
		}
		
		if (declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_static) {
			declSpecifier.setStorageClass(IASTDeclSpecifier.sc_unspecified);
		}
		
		ICPPASTQualifiedName qName = createQualifiedNameFor(functionDeclarator, declarationParent, insertLocation);
		
		createdMethodDeclarator = nodeFactory.newFunctionDeclarator(qName);
		createdMethodDeclarator.setConst(functionDeclarator.isConst());
		for (IASTPointerOperator pop : functionDeclarator.getPointerOperators()) {
			createdMethodDeclarator.addPointerOperator(pop.copy(CopyStyle.withLocations));
		}
		
		IASTFunctionDefinition functionDefinition = nodeFactory.newFunctionDefinition(declSpecifier, createdMethodDeclarator, nodeFactory.newCompoundStatement());
		functionDefinition.setParent(unit);
		
		ICPPASTTemplateDeclaration templateDeclaration = NodeHelper.findContainedTemplateDecalaration(declarationParent);
		if (templateDeclaration != null) {
			ICPPASTTemplateDeclaration newTemplateDeclaration = nodeFactory.newTemplateDeclaration(functionDefinition);
			newTemplateDeclaration.setParent(unit);
			
			for (ICPPASTTemplateParameter templateParameter : templateDeclaration.getTemplateParameters()) {
				newTemplateDeclaration.addTemplateParameter(templateParameter.copy(CopyStyle.withLocations));
			}
			
			return newTemplateDeclaration;
		}
		return functionDefinition;
	}

	private ICPPASTQualifiedName createQualifiedNameFor(IASTFunctionDeclarator functionDeclarator,
			IASTNode declarationParent, InsertLocation insertLocation)	throws CoreException {
		int insertOffset = insertLocation.getInsertPosition();
		return NameHelper.createQualifiedNameFor(
				functionDeclarator.getName(), tu, functionDeclarator.getFileLocation().getNodeOffset(),
				insertLocation.getTranslationUnit(), insertOffset, astCache);
	}
	
	public ImplementMethodData getRefactoringData() {
		return data;
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		// TODO egraf add Descriptor
		return null;
	}
	
	private IFile[] getAllFilesToModify() {
		List<IFile> files = new ArrayList<IFile>(2);
		IFile file = (IFile) tu.getResource();
		if (file != null) {
			files.add(file);
		}
		
		for (InsertLocation insertLocation : insertLocations.values()) {
			if (insertLocation != null) {
				file = insertLocation.getFile();
				if (file != null) {
					files.add(file);
				}
			}			
		}

		return files.toArray(new IFile[files.size()]);
	}

	@Override
	protected RefactoringStatus checkFinalConditions(IProgressMonitor subProgressMonitor,
			CheckConditionsContext checkContext) throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		if (isOneOrMoreImplementationInHeader(subProgressMonitor)) {
			result.addInfo(Messages.ImplementMethodRefactoring_NoImplFile);
		}
		Checks.addModifiedFilesToChecker(getAllFilesToModify(), checkContext);
		return result;
	}
	
	private boolean isOneOrMoreImplementationInHeader(IProgressMonitor subProgressMonitor) throws CoreException {
		for (MethodToImplementConfig config : data.getMethodsToImplement()) {
			IASTSimpleDeclaration decl = config.getDeclaration();
			findInsertLocation(decl, subProgressMonitor);	
		}
		
		if (insertLocations.isEmpty()) {
			return true;
		}
		
		for (InsertLocation insertLocation : insertLocations.values()) {
			if (insertLocation != null && tu.equals(insertLocation.getTranslationUnit())) {
				return true;
			}
		}
		return false;
	}
}
