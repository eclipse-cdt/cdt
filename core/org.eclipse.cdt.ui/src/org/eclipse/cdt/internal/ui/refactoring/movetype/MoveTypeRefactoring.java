/*******************************************************************************
 * Copyright (c) 2008, 2016 Google and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Ball (Google) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.movetype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;
import org.eclipse.cdt.internal.ui.refactoring.ClassMemberInserter;
import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.cdt.internal.ui.refactoring.MethodContext.ContextType;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * The main class for the Move Type refactoring.
 *
 */
public class MoveTypeRefactoring extends CRefactoring {
	public static final String ID = "org.eclipse.cdt.internal.ui.refactoring.extracttype.ExtractTypeRefactoring"; //$NON-NLS-1$

	private final MoveTypeInformation info;
	private IASTNode target;
	private NodeContainer container;
	private INodeFactory nodeFactory;
	private IASTTranslationUnit ast;

	public MoveTypeRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
		info = new MoveTypeInformation();
		name = Messages.MoveType;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		RefactoringStatus status = super.checkInitialConditions(sm.newChild(8));
		if (status.hasError()) {
			return status;
		}

		ast = getAST(tu, sm.newChild(1));
		nodeFactory = ast.getASTNodeFactory();
		container = findAllNodes(pm);

		if (container.isEmpty()) {
			initStatus.addFatalError(Messages.DeclarationMustBeSelected);
			return initStatus;
		}

		if (container.size() > 1) {
			initStatus.addFatalError(Messages.TooManyDeclarationsSelected);
			return initStatus;
		}

		sm.worked(1);
		if (isProgressMonitorCanceled(sm, initStatus))
			return initStatus;

		target = container.getNodesToWrite().get(0);
		if (!isType(target)) {
			initStatus.addFatalError(Messages.NoTypeSelected);
			return initStatus;
		}

		if (!isInsideFunction(target)) {
			initStatus.addFatalError(Messages.NotInsideFunction);
			return initStatus;
		}

		if (isProgressMonitorCanceled(sm, initStatus))
			return initStatus;

		info.setMethodContext(NodeHelper.findMethodContext(target, refactoringContext, sm.split(1)));

		sm.done();
		return initStatus;
	}

	private boolean isInsideFunction(IASTNode node) {
		IASTNode ancestor = ASTQueries.findAncestorWithType(node, IASTFunctionDefinition.class);
		return !(ancestor == null);

	}

	private boolean isType(IASTNode node) {
		if (!(node instanceof IASTSimpleDeclaration)) {
			return false;
		}
		IASTSimpleDeclaration declStmnt = (IASTSimpleDeclaration) node;
		for (IASTNode child : declStmnt.getChildren()) {
			if (child instanceof IASTCompositeTypeSpecifier || child instanceof IASTEnumerationSpecifier) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		ASTRewrite rewrite = collector.rewriterForTranslationUnit(ast);

		IASTNode insertBeforeNode = ASTQueries.findAncestorWithType(target, IASTFunctionDefinition.class);

		IASTName typeNameNode = getTypeName(target);
		ICPPASTCompositeTypeSpecifier classDeclaration = null;
		MethodContext context = info.getMethodContext();
		String name = null;
		if (context.getType() == ContextType.METHOD && !context.isInline()) {
			classDeclaration = (ICPPASTCompositeTypeSpecifier) context.getMethodDeclaration().getParent();
			name = findFreeName(classDeclaration.getMembers()[0], target, typeNameNode.toString());
		} else {
			name = findFreeName(insertBeforeNode, target, typeNameNode.toString());
		}
		IASTName newTypeNameNode = nodeFactory.newName(name);

		replaceNames(rewrite, insertBeforeNode, typeNameNode, newTypeNameNode);
		IASTSimpleDeclaration typeDecl = createSimpleDeclaration(target);

		List<IASTDeclarator> declarators = getDeclarators(target);
		if (declarators.isEmpty()) {
			rewrite.remove(target, null);
		} else {
			IASTNamedTypeSpecifier namedTypeSpec = nodeFactory.newTypedefNameSpecifier(newTypeNameNode);
			IASTSimpleDeclaration simpleDecl = nodeFactory.newSimpleDeclaration(namedTypeSpec);
			for (IASTDeclarator declarator : declarators) {
				simpleDecl.addDeclarator(declarator);
			}
			rewrite.replace(target, simpleDecl, null);
		}

		if (classDeclaration != null) {
			ASTRewrite rewriteHeader = ClassMemberInserter.createChange(classDeclaration, VisibilityEnum.v_private,
					typeDecl, true, collector);
			replaceNames(rewriteHeader, typeDecl, typeNameNode, newTypeNameNode);
		} else {
			rewrite = rewrite.insertBefore(insertBeforeNode.getParent(), insertBeforeNode, typeDecl, null);
			replaceNames(rewrite, typeDecl, typeNameNode, newTypeNameNode);
		}
	}

	private void replaceNames(ASTRewrite rewrite, IASTNode node, IASTName oldName, IASTName newName) {
		List<IASTName> names = findNamesToBeReplaced(node, oldName);
		for (IASTName name : names) {
			rewrite.replace(name, newName, null);
		}
	}

	private List<IASTName> findNamesToBeReplaced(IASTNode node, IASTName oldName) {
		List<IASTName> names = new ArrayList<IASTName>();
		node.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if (name.toString().equals(oldName.toString())) {
						names.add(name);
				}
				return PROCESS_CONTINUE;
			}
		});
		return names;
	}

	private String findFreeName(IASTNode defPoint, IASTNode usePoint, String typeName) {
		if (defPoint == null) {
			return typeName;
		}
		IScope scopeDef = CPPVisitor.getContainingScope(defPoint);
		IBinding bindingsDef[] = scopeDef.find(typeName, defPoint.getTranslationUnit());
		if (bindingsDef.length == 0) {
			return typeName;
		}

		IScope scopeUse = CPPVisitor.getContainingScope(usePoint);

		int counter = 0;
		IBinding bindingsUse[];
		String newTypeName;
		do {
			newTypeName = typeName + "_" + counter++; //$NON-NLS-1$
			bindingsDef = scopeDef.find(newTypeName, defPoint.getTranslationUnit());
			bindingsUse = scopeUse.find(newTypeName, usePoint.getTranslationUnit());
		} while (bindingsUse.length > 0 || bindingsDef.length > 0);
		return newTypeName;
	}

	private List<IASTDeclarator> getDeclarators(IASTNode node) {
		List<IASTDeclarator> declarators = new ArrayList<IASTDeclarator>();
		IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) node;
		for (IASTNode child : simpleDeclaration.getChildren()) {
			if (child instanceof IASTDeclarator) {
				IASTDeclarator declarator = (IASTDeclarator) child.copy(CopyStyle.withLocations);
				declarators.add(declarator);
			}
		}
		return declarators;
	}

	private IASTName getTypeName(IASTNode node) {
		IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) node;
		IASTName name = null;
		for (IASTNode child : simpleDeclaration.getChildren()) {
			if (child instanceof IASTCompositeTypeSpecifier) {
				IASTCompositeTypeSpecifier specifier = (IASTCompositeTypeSpecifier) child;
				name = specifier.getName();
			} else if (child instanceof IASTEnumerationSpecifier) {
				IASTEnumerationSpecifier specifier = (IASTEnumerationSpecifier) child;
				name = specifier.getName();
			}
		}
		return name;
	}

	private IASTSimpleDeclaration createSimpleDeclaration(IASTNode node) {
		IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) node;
		for (IASTNode child : simpleDeclaration.getChildren()) {
			if (child instanceof IASTCompositeTypeSpecifier) {
				IASTCompositeTypeSpecifier specifier = (IASTCompositeTypeSpecifier) child;
				return nodeFactory.newSimpleDeclaration(specifier.copy(CopyStyle.withLocations));
			} else if (child instanceof IASTEnumerationSpecifier) {
				IASTEnumerationSpecifier specifier = (IASTEnumerationSpecifier) child;
				return nodeFactory.newSimpleDeclaration(specifier.copy(CopyStyle.withLocations));
			}
		}
		return null;
	}

	private NodeContainer findAllNodes(IProgressMonitor pm) throws OperationCanceledException, CoreException {
		final NodeContainer container = new NodeContainer();
		IASTTranslationUnit ast = getAST(tu, pm);

		if (ast != null) {
			ast.accept(new ASTVisitor() {
				{
					shouldVisitDeclarations = true;
				}

				@Override
				public int visit(IASTDeclaration declaration) {
					if (isNodeInsideSelection(declaration)) {
						container.add(declaration);
						return PROCESS_SKIP;
					}
					return super.visit(declaration);
				}
			});
		}

		return container;
	}

	private boolean isNodeInsideSelection(IASTNode node) {
		return node.isPartOfTranslationUnitFile() && SelectionHelper.isNodeInsideRegion(node, selectedRegion);
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		Map<String, String> arguments = getArgumentMap();
		RefactoringDescriptor desc = new MoveTypeRefactoringDescriptor(project.getProject().getName(),
				"Move Type", "Move "  + target.getRawSignature() , arguments); //$NON-NLS-1$//$NON-NLS-2$
		return desc;
	}

	private Map<String, String> getArgumentMap() {
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put(CRefactoringDescriptor.FILE_NAME, tu.getLocationURI().toString());
		arguments.put(CRefactoringDescriptor.SELECTION,
				selectedRegion.getOffset() + "," + selectedRegion.getLength()); //$NON-NLS-1$
		arguments.put(MoveTypeRefactoringDescriptor.NAME, info.getName());
		return arguments;
	}

	public MoveTypeInformation getRefactoringInfo() {
		return info;
	}
}
