/*******************************************************************************
 * Copyright (c) 2008 Google and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Google - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.NameNVisibilityInformation;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractExpression;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;

/**
 * The main class for the Extract Local Variable refactoring. This refactoring
 * differs from the Extract Constant refactoring in that any valid expression
 * which can be used to initialize a local variable can be extracted.
 * 
 * @author Tom Ball
 */
public class ExtractLocalVariableRefactoring extends CRefactoring {
	private IASTExpression target = null;
	private final NameNVisibilityInformation info;
	private NodeContainer container;

	public ExtractLocalVariableRefactoring(IFile file, ISelection selection,
			NameNVisibilityInformation info) {
		super(file, selection, null);
		this.info = info;
		name = Messages.ExtractLocalVariable;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 9);
		super.checkInitialConditions(sm.newChild(6));

		container = findAllExpressions();
		if (container.size() < 1) {
			initStatus.addFatalError(Messages.ExpressionMustBeSelected);
			return initStatus;
		}

		sm.worked(1);
		if (isProgressMonitorCanceld(sm, initStatus))
			return initStatus;

		boolean oneMarked = region != null
				&& isOneMarked(container.getNodesToWrite(), region);
		if (!oneMarked) {
			if (target == null) {
				initStatus.addFatalError(Messages.NoExpressionSelected);
			} else {
				initStatus.addFatalError(Messages.TooManyExpressionsSelected);
			}
			return initStatus;
		}
		sm.worked(1);

		if (isProgressMonitorCanceld(sm, initStatus))
			return initStatus;

		container.findAllNames();
		sm.worked(1);

		container.getAllAfterUsedNames();
		info.addNamesToUsedNames(findAllDeclaredNames());
		sm.worked(1);

		NodeHelper.findMethodContext(container.getNodesToWrite().get(0),
				getIndex());
		sm.worked(1);
		
		info.setName(guessTempName());
		sm.done();
		return initStatus;
	}

	private ArrayList<String> findAllDeclaredNames() {
		ArrayList<String> names = new ArrayList<String>();
		IASTFunctionDefinition funcDef = NodeHelper
				.findFunctionDefinitionInAncestors(target);
		ICPPASTCompositeTypeSpecifier comTypeSpec = 
			getCompositeTypeSpecifier(funcDef);
		if (comTypeSpec != null) {
			for (IASTDeclaration dec : comTypeSpec.getMembers()) {
				if (dec instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpDec = (IASTSimpleDeclaration) dec;
					for (IASTDeclarator decor : simpDec.getDeclarators()) {
						names.add(decor.getName().getRawSignature());
					}
				}
			}
		}
		return names;
	}

	private ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier(
			IASTFunctionDefinition funcDef) {
		if (funcDef != null) {
			IBinding binding = funcDef.getDeclarator().getName()
					.resolveBinding();
			if (binding instanceof CPPMethod) {

				CPPMethod method = (CPPMethod) binding;
				IASTNode decl = method.getDeclarations()[0];

				IASTNode spec = decl.getParent().getParent();
				if (spec instanceof ICPPASTCompositeTypeSpecifier) {
					ICPPASTCompositeTypeSpecifier compTypeSpec = 
						(ICPPASTCompositeTypeSpecifier) spec;
					return compTypeSpec;
				}
			}
		}
		return null;
	}

	private boolean isOneMarked(List<IASTNode> selectedNodes,
			Region textSelection) {
		boolean oneMarked = false;
		for (IASTNode node : selectedNodes) {
			if (node instanceof IASTExpression) {
				IASTExpression expression = (IASTExpression) node;
				boolean isInSameFileSelection = SelectionHelper
						.isInSameFileSelection(textSelection, expression, file);
				if (isInSameFileSelection
						&& isExpressionInSelection(expression, textSelection)) {
					if (target == null) {
						target = expression;
						oneMarked = true;
					} else if (!isTargetChild(expression)) {
						oneMarked = false;
					}
				}
			}
		}
		return oneMarked;
	}

	private boolean isExpressionInSelection(IASTExpression expression,
			Region selection) {
		IASTFileLocation location = expression.getFileLocation();
		int e1 = location.getNodeOffset();
		int e2 = location.getNodeOffset() + location.getNodeLength();
		int s1 = selection.getOffset();
		int s2 = selection.getOffset() + selection.getLength();
		return e1 >= s1 && e2 <= s2;
	}

	private boolean isTargetChild(IASTExpression child) {
		if (target == null) {
			return false;
		}
		IASTNode node = child;
		while (node != null) {
			if (node.getParent() == target)
				return true;
			node = node.getParent();
		}
		return false;
	}

	private NodeContainer findAllExpressions() {
		final NodeContainer container = new NodeContainer();

		unit.accept(new CPPASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (SelectionHelper.isSelectedFile(region, expression, file)) {
					container.add(expression);
					return PROCESS_SKIP;
				}
				return super.visit(expression);
			}
		});

		return container;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm,
			ModificationCollector collector) throws CoreException,
			OperationCanceledException {
		String variableName = info.getName();
		TextEditGroup editGroup = new TextEditGroup(
				Messages.CreateLocalVariable);

		// Define temporary variable declaration and insert it
		IASTStatement declInsertPoint = getParentStatement(target);
		IASTDeclarationStatement declaration = getVariableNodes(variableName);
		declaration.setParent(declInsertPoint.getParent());
		ASTRewrite rewriter = collector.rewriterForTranslationUnit(unit);
		rewriter.insertBefore(declInsertPoint.getParent(), declInsertPoint,
				declaration, editGroup);

		// Replace target with reference to temporary variable
		CPPASTIdExpression idExpression = new CPPASTIdExpression(
				new CPPASTName(variableName.toCharArray()));
		rewriter.replace(target, idExpression, editGroup);
	}

	private IASTStatement getParentStatement(IASTNode node) {
		while (node != null) {
			if (node instanceof IASTStatement)
				return (IASTStatement) node;
			node = node.getParent();
		}
		return null;
	}

	private IASTDeclarationStatement getVariableNodes(String newName) {
		IASTSimpleDeclaration simple = new CPPASTSimpleDeclaration();

		IASTDeclSpecifier declSpec = new ExtractExpression()
				.determineReturnType(deblock(target), null);
		declSpec.setStorageClass(IASTDeclSpecifier.sc_unspecified);
		simple.setDeclSpecifier(declSpec);

		IASTDeclarator decl = new CPPASTDeclarator();
		IASTName name = new CPPASTName(newName.toCharArray());
		decl.setName(name);

		IASTInitializerExpression init = new CPPASTInitializerExpression();
		init.setExpression(deblock(target));
		decl.setInitializer(init);
		simple.addDeclarator(decl);

		return new CPPASTDeclarationStatement(simple);
	}
	
	/**
	 * Removes surrounding parentheses from an expression.  If the expression
	 * does not have surrounding parentheses, the original expression is returned. 
	 */
	private static IASTExpression deblock(IASTExpression expression) {
		if (expression instanceof IASTUnaryExpression) {
			IASTUnaryExpression unary = (IASTUnaryExpression)expression;
			if (unary.getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
				return deblock(unary.getOperand());
			}
		}
		return expression;
	}

	public String guessTempName() {
		String[] proposals= guessTempNames();
		if (proposals.length == 0)
			return info.getName();
		else
			return proposals[0];
	}

	/**
	 * @return proposed variable names (may be empty, but not null). The first 
	 *         proposal should be used as "best guess" (if it exists).
	 */
	public String[] guessTempNames() {
		final List<String> guessedTempNames = new ArrayList<String>();
		if (target != null) {
			target.accept(new CPPASTVisitor() {
				{
					shouldVisitNames = true;
					shouldVisitExpressions = true;
				}
				
				@Override
				public int visit(IASTName name) {
					addTempName(name.getLastName().toString());
					return super.visit(name);
				}
				
				@Override
				public int visit(IASTExpression expression) {
					if (expression instanceof CPPASTLiteralExpression) {
						CPPASTLiteralExpression literal = (CPPASTLiteralExpression)expression;
						String name = null;
						char[] value = literal.getValue();
						switch (literal.getKind()) {
				          case IASTLiteralExpression.lk_char_constant:
				              name = Character.toString(value[0]);
				              break;
				          case IASTLiteralExpression.lk_float_constant:
				              name = "f"; //$NON-NLS-1$
				              break;
				          case IASTLiteralExpression.lk_integer_constant:
				              name = "i"; //$NON-NLS-1$
				              break;
				          case IASTLiteralExpression.lk_string_literal:
				              name = literal.toString();
				              break;
				          case IASTLiteralExpression.lk_false: 
				          case IASTLiteralExpression.lk_true:
				              name = "b"; //$NON-NLS-1$
				              break;
						}
						if (name != null) {
							addTempName(name);
						}
					}
					return super.visit(expression);
				}
				
				private void addTempName(String name) {
					char[] tmpName = new char[name.length()];
					int len = 0;
					for (int i = 0; i < name.length(); i++) {
						char c = name.charAt(i);
						if (len == 0 && Character.isJavaIdentifierStart(c)) {
							tmpName[len++] = Character.toLowerCase(c);
						} else if (Character.isJavaIdentifierPart(c)) {
							tmpName[len++] = c;
						}
					}
					name = new String(tmpName, 0, len);
					if (name.length() > 0 && !guessedTempNames.contains(name) && 
							!info.getUsedNames().contains(name)) {
							guessedTempNames.add(name);
					}
				}
			});
		}
		return guessedTempNames.toArray(new String[0]);
	}
}
