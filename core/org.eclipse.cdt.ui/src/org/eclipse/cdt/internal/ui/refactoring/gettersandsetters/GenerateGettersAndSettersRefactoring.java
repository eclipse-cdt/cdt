/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * 	   Institute for Software - initial API and implementation
 * 	   Sergey Prigogin (Google)
 * 	   Marc-Andre Laperle - do not search for definition insert location twice. 
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ContainerNode;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring2;
import org.eclipse.cdt.internal.ui.refactoring.AddDeclarationNodeToClassChange;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.internal.ui.refactoring.implementmethod.InsertLocation;
import org.eclipse.cdt.internal.ui.refactoring.implementmethod.MethodDefinitionInsertLocationFinder;
import org.eclipse.cdt.internal.ui.refactoring.utils.Checks;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * @author Thomas Corbat
 */
public class GenerateGettersAndSettersRefactoring extends CRefactoring2 {

	private final class CompositeTypeSpecFinder extends ASTVisitor {
		private final int start;
		private final Container<IASTCompositeTypeSpecifier> container;
		{
			shouldVisitDeclSpecifiers = true;
		}

		private CompositeTypeSpecFinder(int start, Container<IASTCompositeTypeSpecifier> container) {
			this.start = start;
			this.container = container;
		}

		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			if (declSpec instanceof IASTCompositeTypeSpecifier) {
				IASTFileLocation loc = declSpec.getFileLocation();
				if (start > loc.getNodeOffset() && start < loc.getNodeOffset() + loc.getNodeLength()) {
					container.setObject((IASTCompositeTypeSpecifier) declSpec);
					return ASTVisitor.PROCESS_ABORT;
				}
			}
			
			return super.visit(declSpec);
		}
	}

	private static final String MEMBER_DECLARATION = "MEMBER_DECLARATION"; //$NON-NLS-1$
	private final GetterSetterContext context;
	private InsertLocation definitionInsertLocation;	
	
	public GenerateGettersAndSettersRefactoring(ICElement element, ISelection selection,
			ICProject project, RefactoringASTCache astCache) {
		super(element, selection, project, astCache);
		context = new GetterSetterContext();
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		RefactoringStatus status = super.checkInitialConditions(sm.newChild(6));
		if (status.hasError()) {
			return status;
		}

		if (!initStatus.hasFatalError()) {
			initRefactoring(pm);		
			if (context.existingFields.isEmpty()) {
				initStatus.addFatalError(Messages.GenerateGettersAndSettersRefactoring_NoFields);
			}
		}		
		return initStatus;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,
			CheckConditionsContext checkContext) throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		if (context.isDefinitionSeparate()) {
			findDefinitionInsertLocation(pm);
			if (definitionInsertLocation == null ||
					definitionInsertLocation.getTranslationUnit() == null) {
				result.addInfo(Messages.GenerateGettersAndSettersRefactoring_NoImplFile);
			}
		}
		Checks.addModifiedFilesToChecker(getAllFilesToModify(), checkContext);
		return result;
	}

	private IFile[] getAllFilesToModify() {
		List<IFile> files = new ArrayList<IFile>(2);
		IFile file = (IFile) tu.getResource();
		if (file != null) {
			files.add(file);
		}
		if (definitionInsertLocation != null) {
			file = definitionInsertLocation.getFile();
			if (file != null) {
				files.add(file);
			}
		}
		return files.toArray(new IFile[files.size()]);
	}

	private void initRefactoring(IProgressMonitor pm) throws OperationCanceledException, CoreException {
		IASTTranslationUnit ast = astCache.getAST(tu, null);
		context.selectedName = getSelectedName(ast);
		IASTCompositeTypeSpecifier compositeTypeSpecifier = null;
		if (context.selectedName != null) {
			compositeTypeSpecifier = getCompositeTypeSpecifier(context.selectedName);
		} else {
			compositeTypeSpecifier = findCurrentCompositeTypeSpecifier(ast);
		}
		if (compositeTypeSpecifier != null) {
			findDeclarations(compositeTypeSpecifier);
		} else {
			initStatus.addFatalError(Messages.GenerateGettersAndSettersRefactoring_NoCassDefFound);
		}
	}
	
	private IASTCompositeTypeSpecifier findCurrentCompositeTypeSpecifier(IASTTranslationUnit ast)
			throws OperationCanceledException, CoreException {
		final int start = selectedRegion.getOffset();
		Container<IASTCompositeTypeSpecifier> container = new Container<IASTCompositeTypeSpecifier>();
		ast.accept(new CompositeTypeSpecFinder(start, container));
		return container.getObject();
	}

	private IASTCompositeTypeSpecifier getCompositeTypeSpecifier(IASTName selectedName) {
		IASTNode node = selectedName;
		while(node != null && !(node instanceof IASTCompositeTypeSpecifier)) {
			node = node.getParent();
		}
		return (IASTCompositeTypeSpecifier) node;
	}

	private IASTName getSelectedName(IASTTranslationUnit ast) {
		List<IASTName> names = findAllMarkedNames(ast);
		if (names.size() < 1) {
			return null;
		}
		return names.get(names.size() - 1);
	}

	protected void findDeclarations(IASTCompositeTypeSpecifier compositeTypeSpecifier) {
		compositeTypeSpecifier.accept(new ASTVisitor() {
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
							IASTDeclarator innermostDeclarator = declarators[0];
							while (innermostDeclarator.getNestedDeclarator() != null) {
								innermostDeclarator = innermostDeclarator.getNestedDeclarator();
							}
							if ((innermostDeclarator instanceof IASTFunctionDeclarator)) {
								context.existingFunctionDeclarations.add(fieldDeclaration);
							} else if (fieldDeclaration.isPartOfTranslationUnitFile()) {
								context.existingFields.add(fieldDeclaration);
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
	protected void collectModifications(IProgressMonitor pm,ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		List<IASTNode> getterAndSetters = new ArrayList<IASTNode>();
		List<IASTFunctionDefinition> definitions = new ArrayList<IASTFunctionDefinition>();
		for (GetterSetterInsertEditProvider currentProvider : context.selectedFunctions) {
			if (context.isDefinitionSeparate()) {
				getterAndSetters.add(currentProvider.getFunctionDeclaration());
				IASTFunctionDefinition functionDefinition = currentProvider.getFunctionDefinition(true);
				// Standalone definitions in a header file have to be declared inline. 
				if (definitionInsertLocation.getTranslationUnit().isHeaderUnit()) {
					functionDefinition.getDeclSpecifier().setInline(true);
				}
				definitions.add(functionDefinition);
			} else {
				getterAndSetters.add(currentProvider.getFunctionDefinition(false));
			}
		}
		if (context.isDefinitionSeparate()) {
			addDefinition(collector, definitions, pm);
		}
		ICPPASTCompositeTypeSpecifier classDefinition =
				(ICPPASTCompositeTypeSpecifier) context.existingFields.get(context.existingFields.size() - 1).getParent();

		AddDeclarationNodeToClassChange.createChange(classDefinition, VisibilityEnum.v_public,
				getterAndSetters, false, collector);
	}

	private void addDefinition(ModificationCollector collector, List<IASTFunctionDefinition> definitions, IProgressMonitor pm)
			throws CoreException {
		findDefinitionInsertLocation(pm);
		IASTNode parent = definitionInsertLocation.getParentOfNodeToInsertBefore();
		IASTTranslationUnit ast = parent.getTranslationUnit();
		ASTRewrite rewrite = collector.rewriterForTranslationUnit(ast);
		IASTNode nodeToInsertBefore = definitionInsertLocation.getNodeToInsertBefore();
		ContainerNode cont = new ContainerNode();
		for (IASTFunctionDefinition functionDefinition : definitions) {
			cont.addNode(functionDefinition);
		}
		rewrite = rewrite.insertBefore(parent, nodeToInsertBefore, cont, null);
	}

	public GetterSetterContext getContext() {
		return context;
	}
	
	private void findDefinitionInsertLocation(IProgressMonitor pm) throws CoreException {
		if (definitionInsertLocation != null) {
			return;
		}
		
		IASTSimpleDeclaration decl = context.existingFields.get(0);
		MethodDefinitionInsertLocationFinder locationFinder = new MethodDefinitionInsertLocationFinder();
		InsertLocation location = locationFinder.find(tu, decl.getFileLocation(), decl.getParent(),
				astCache, pm);

		if (location.getFile() == null || NodeHelper.isContainedInTemplateDeclaration(decl)) {
			location.setNodeToInsertAfter(NodeHelper.findTopLevelParent(decl), tu);
		}
		
		definitionInsertLocation = location;
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		// TODO egraf add Descriptor
		return null;
	}	
}
