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
package org.eclipse.cdt.internal.ui.refactoring.inlinelocalvariable;

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
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTInitializerList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleTypeConstructorExpression;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.cdt.internal.ui.refactoring.VariableNameInformation;
import org.eclipse.cdt.internal.ui.refactoring.utils.ASTHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectedExpressionFinder;

/**
 * The main class for the InlineLocalVariable refactoring.
 * @since 6.5
 */
public class InlineLocalVariableRefactoring extends CRefactoring {
	public static final String ID = "org.eclipse.cdt.internal.ui.refactoring.inline.InlineLocalVariableRefactoring"; //$NON-NLS-1$

	private NameInformation target;
	private IASTExpression targetExpression;
	private IASTDeclaration targetDeclaration;
	private final VariableNameInformation info;
	IASTFunctionDefinition functionDef;
	private List<IASTName> names;

	public InlineLocalVariableRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
		info = new VariableNameInformation();
		name = Messages.InlineLocalVariable;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);

		RefactoringStatus status = super.checkInitialConditions(sm.newChild(6));
		if (status.hasError()) {
			return status;
		}

		NodeContainer nodes = findSelectedExpression(pm);

		if (nodes.isEmpty()) {
			initStatus.addFatalError(Messages.NoExpressionSelected);
			return initStatus;
		} else if (!(nodes.getNodesToWrite().get(0) instanceof IASTIdExpression)
				|| nodes.getNames().isEmpty()) {
			initStatus.addFatalError(Messages.NoIdExpressionSelected);
			return initStatus;
		} else if (nodes.getNodesToWrite().get(0).getParent() instanceof IASTFieldReference) {
			initStatus.addFatalError(Messages.FieldReference);
			return initStatus;
		}

		target = nodes.getNames().get(0);
		functionDef = ASTQueries.findAncestorWithType(nodes.getNodesToWrite().get(0),
				IASTFunctionDefinition.class);

		targetDeclaration = ASTQueries.findAncestorWithType(target.getDeclarator(), IASTDeclaration.class);
		setExpression(targetDeclaration);
		if (targetExpression == null) {
			initStatus.addFatalError(Messages.Uninitialized);
			return initStatus;
		}

		names = getAllNamesOfSelectedName(functionDef, target.getDeclarationName());
		if (!isValidInline(names)) {
			initStatus.addFatalError(Messages.VariableUsedOutside);
			return initStatus;
		}

		sm.done();
		return initStatus;
	}

	private boolean isValidInline(List<IASTName> names) {
		for (IASTName name : names) {
			if ((name.getParent() instanceof IASTIdExpression)) {
				IASTIdExpression idExpression = (IASTIdExpression) name.getParent();
				if (isIncrementOrDecrement(idExpression) || isAssignment(idExpression)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isAssignment(IASTIdExpression idExpression) {
		if (!(idExpression.getParent() instanceof IASTBinaryExpression)) {
			return false;
		}
		IASTBinaryExpression binaryExpression = (IASTBinaryExpression) idExpression.getParent();
		if (binaryExpression.getOperand1() != idExpression) {
			return false;
		}
		return NodeHelper.isAssignment(binaryExpression);
	}

	private boolean isIncrementOrDecrement(IASTIdExpression idExpression) {
		return NodeHelper.isIncrementOrDecrement(idExpression.getParent());
	}

	private NodeContainer findSelectedExpression(IProgressMonitor monitor)
			throws OperationCanceledException, CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 5);
		NodeContainer nodes = new NodeContainer();
		IASTTranslationUnit ast = getAST(tu, subMonitor.split(4));
		subMonitor.split(1);
		SelectedExpressionFinder expressionFinder = new SelectedExpressionFinder(selectedRegion) {
			@Override
			protected boolean isSearchedType(IASTExpression expression) {
				return expression instanceof IASTIdExpression;
			}
		};
		nodes.add(expressionFinder.findSelectedExpression(ast));
		return nodes;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {

		IASTTranslationUnit ast = getAST(tu, pm);
		ASTRewrite rewrite = collector.rewriterForTranslationUnit(ast);

		rewrite.remove(targetDeclaration, null);

		for (IASTName name : names) {
			rewrite.replace(name, targetExpression, null);
		}
	}

	private void setExpression(IASTDeclaration declaration) throws OperationCanceledException, CoreException {
		declaration.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				targetExpression = expression;
				return PROCESS_ABORT;
			}
		});
		IASTDeclarator declarator = ASTHelper.getDeclaratorForNode(declaration);
		IASTInitializer initializer = declarator.getInitializer();
		if (initializer != null && initializer instanceof CPPASTInitializerList) {
			CPPASTInitializerList initializerList = (CPPASTInitializerList) initializer;
			IASTDeclSpecifier declSpec = ASTHelper.getDeclarationSpecifier(declaration).copy();
			declSpec.setConst(false);
			if (initializerList.getClauses().length == 1) {
				IType typeInInitializer = initializerList.getClauses()[0].getEvaluation().getType();
				if (!(declSpec.toString().equals(typeInInitializer.toString()))) {
					targetExpression = createSimpleTypeCtorExpr(initializerList, declSpec);
				}
			} else {
				targetExpression = createSimpleTypeCtorExpr(initializerList, declSpec);
			}
		}
	}

	private CPPASTSimpleTypeConstructorExpression createSimpleTypeCtorExpr(
			CPPASTInitializerList initializerList, IASTDeclSpecifier declSpec) {
		CPPASTSimpleTypeConstructorExpression simpleTypeCtorExpr = new CPPASTSimpleTypeConstructorExpression();
		simpleTypeCtorExpr.setDeclSpecifier((ICPPASTDeclSpecifier) declSpec.copy());
		simpleTypeCtorExpr.setInitializer(initializerList.copy());
		return simpleTypeCtorExpr;
	}

	private List<IASTName> getAllNamesOfSelectedName(IASTNode location, IASTName searchName) {
		List<IASTName> names = new ArrayList<IASTName>();
		IBinding bindingSearch = searchName.getBinding();
		location.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if (name.toString().equals(searchName.toString())) {
					IBinding bindingFound = name.getBinding();
					if (bindingSearch.equals(bindingFound)) {
						names.add(name);
					}
				}
				return PROCESS_CONTINUE;
			}
		});
		return names;
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		Map<String, String> arguments = getArgumentMap();
		RefactoringDescriptor desc = new InlineLocalVariableRefactoringDescriptor(
				project.getProject().getName(), "Inline Local Variable Refactoring", //$NON-NLS-1$
				"Inline " + targetExpression.getRawSignature(), arguments); //$NON-NLS-1$
		return desc;
	}

	private Map<String, String> getArgumentMap() {
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put(CRefactoringDescriptor.FILE_NAME, tu.getLocationURI().toString());
		arguments.put(CRefactoringDescriptor.SELECTION,
				selectedRegion.getOffset() + "," + selectedRegion.getLength()); //$NON-NLS-1$
		arguments.put(InlineLocalVariableRefactoringDescriptor.NAME, info.getName());
		return arguments;
	}

	public VariableNameInformation getRefactoringInfo() {
		return info;
	}
}
