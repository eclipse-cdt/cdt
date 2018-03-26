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
package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.DeclarationGenerator;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTEqualsInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.ASTHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectedExpressionFinder;
import org.eclipse.cdt.internal.ui.util.NameComposer;

/**
 * The main class for the Extract Local Variable refactoring. This refactoring
 * differs from the Extract Constant refactoring in that any valid expression
 * which can be used to initialize a local variable can be extracted.
 *
 * @author Tom Ball
 */
public class ExtractLocalVariableRefactoring extends CRefactoring {
	public static final String ID =
			"org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable.ExtractLocalVariableRefactoring"; //$NON-NLS-1$
	private IASTExpression target;
	private final ExtractLocalVariableInfo info;
	private List<IASTExpression> targets;

	public ExtractLocalVariableRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
		info = new ExtractLocalVariableInfo();
		name = Messages.ExtractLocalVariable;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		RefactoringStatus status = super.checkInitialConditions(sm.newChild(6));
		if (status.hasError()) {
			return status;
		}
		target = findSelectedExpression(pm);
		if (target == null) {
			initStatus.addFatalError(Messages.ExpressionMustBeSelected);
			return initStatus;
		}
		IASTFunctionDefinition funcDef = ASTQueries.findAncestorWithType(target, IASTFunctionDefinition.class);
		if (funcDef == null) {
			initStatus.addFatalError(Messages.ExpressionMustBeSelected);
		}

		sm.worked(1);
		if (isProgressMonitorCanceled(sm, initStatus))
			return initStatus;

		info.addNamesToUsedNames(findAllDeclaredNames(funcDef));
		sm.worked(1);

		NodeHelper.findMethodContext(target, refactoringContext, sm);
		sm.worked(1);

		info.setName(guessTempName());
		targets = findExpressionsToExtract(target);
		sm.done();
		return initStatus;
	}

	private Collection<String> findAllDeclaredNames(IASTFunctionDefinition funcDef) {
		Set<String> names = new HashSet<String>();
		List<IASTName> nameNodes = new NameVisitor().findNames(funcDef);
		for (IASTName name : nameNodes) {
			if (name.resolveBinding() instanceof IVariable) {
				names.add(name.toString());
			}
		}
		return names;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		String variableName = info.getName();
		TextEditGroup editGroup = new TextEditGroup(Messages.CreateLocalVariable);

		if (!info.isReplaceAllOccurrences() && targets.size() > 1) {
			targets.clear();
			targets.add(target);
		}

		// Define temporary variable declaration and insert it
		IASTTranslationUnit ast = getAST(tu, pm);
		IASTDeclarationStatement declaration = getVariableNodes(ast, variableName);

		ASTRewrite rewriter = collector.rewriterForTranslationUnit(ast);
		IASTNode insertPoint = getInsertionPoint(targets);
		rewriter.insertBefore(insertPoint.getParent(), insertPoint, declaration, editGroup);

		// Replace target with reference to temporary variable
		CPPASTIdExpression idExpression =
				new CPPASTIdExpression(new CPPASTName(variableName.toCharArray()));

		for (IASTExpression expression : targets) {
			rewriter.replace(expression, idExpression, editGroup);
		}
	}

	private IASTNode getInsertionPoint(List<IASTExpression>  expressions) {
		IASTNode insertPoint = expressions.get(0);
		boolean validInsertPoint = false;
		while (!validInsertPoint && insertPoint.getParent() != null) {
			insertPoint = insertPoint.getParent();
			IASTNode parent = insertPoint.getParent();
			validInsertPoint = true;
			for (IASTExpression expression : expressions) {
				if (!parent.contains(expression)) {
					validInsertPoint = false;
				}
			}
		}
		return getParentStatement(insertPoint);
	}

	private IASTExpression findSelectedExpression(IProgressMonitor monitor)
			throws OperationCanceledException, CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 5);

		IASTTranslationUnit ast = getAST(tu, subMonitor.split(4));
		subMonitor.split(1);
		SelectedExpressionFinder expressionFinder = new SelectedExpressionFinder(selectedRegion) {
			@Override
			protected boolean isSearchedType(IASTExpression expression) {
				return expression instanceof IASTLiteralExpression || expression instanceof IASTIdExpression;
			}
		};
		return expressionFinder.findSelectedExpression(ast);
	}

	private List<IASTExpression> findExpressionsToExtract(IASTExpression target) {
		List<IASTExpression> targets = new ArrayList<IASTExpression>();
		IASTFunctionDefinition functionDef = ASTQueries.findAncestorWithType(target,
				IASTFunctionDefinition.class);
		functionDef.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}
			boolean hasExpression = false;
			boolean isModifying = false;
			@Override
			public int visit(IASTExpression expression) {
				if (ASTHelper.isSameExpressionTree(target, expression)) {
					if (isSameScope(target, expression)) {
						if (isModifying) {
							targets.clear();
							targets.add(target);
							return PROCESS_ABORT;
						}
						targets.add(expression);
						hasExpression = true;
					}
					return PROCESS_SKIP;
				}
				if (hasExpression && isModifyingVariable(target,expression)) {
					isModifying = true;
				}
				return PROCESS_CONTINUE;
			}
		});
		return targets;
	}

	private boolean isModifyingVariable(IASTExpression target, IASTExpression expr) {
		if (target == expr) {
			return false;
		}
		List<IASTName> names1 = new NameVisitor().findNames(target);
		List<IASTName> names2 = new NameVisitor().findNames(expr);
		for (IASTName name1 : names1) {
			for (IASTName name2 : names2) {
				if (name1.resolveBinding() == name2.resolveBinding()) {
					if (isModifyingExpression(expr)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isSameScope(IASTExpression expr1, IASTExpression expr2) {
		List<IASTName> names1 = new NameVisitor().findNames(expr1);
		List<IASTName> names2 = new NameVisitor().findNames(expr2);
		if (names1.size() != names2.size()) {
			return false;
		}
		for (int i = 0; i < names1.size(); i++) {
			IBinding binding1, binding2;
			binding1 = names1.get(i).resolveBinding();
			binding2 = names2.get(i).resolveBinding();
			if (binding1 != binding2) {
				return false;
			}
		}
		return true;
	}

	private boolean isModifyingExpression(IASTExpression expr) {
		//TODO: check if modified in a function
		return NodeHelper.isAssignment(expr) || NodeHelper.isIncrementOrDecrement(expr);
	}

	private IASTStatement getParentStatement(IASTNode node) {
		while (node != null) {
			if (node instanceof IASTStatement && !(node.getParent() instanceof IASTForStatement)) {
				if (node instanceof IASTIfStatement && node.getParent() instanceof IASTIfStatement) {
					IASTIfStatement ifStatement = (IASTIfStatement) node.getParent();
					if (!ifStatement.getElseClause().contains(node)){
						return (IASTStatement) node;
					}
				} else {
					return (IASTStatement) node;
				}
			}
			node = node.getParent();
		}
		return null;
	}

	private IASTDeclarationStatement getVariableNodes(IASTTranslationUnit ast, String newName) {
		INodeFactory factory = ast.getASTNodeFactory();
		IASTSimpleDeclaration simple = factory.newSimpleDeclaration(null);
		DeclarationGenerator generator = DeclarationGenerator.create(factory);
		IASTDeclSpecifier declSpec = null;
		IType targetType = target.getExpressionType();
		declSpec = generator.createDeclSpecFromType(targetType);
		declSpec.setStorageClass(IASTDeclSpecifier.sc_unspecified);
		simple.setDeclSpecifier(declSpec);
		IASTDeclarator decl = generator.createDeclaratorFromType(target.getExpressionType(),
				newName.toCharArray());
		IASTEqualsInitializer init = new CPPASTEqualsInitializer();
		init.setInitializerClause(deblock(target.copy(CopyStyle.withLocations)));
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
		if (proposals.length == 0) {
			return info.getName();
		} else {
			String name = proposals[proposals.length - 1];
			return name;
		}
	}

	private String[] getPrefixes() {
		// In Future we could use user preferences to define the prefixes
		String[] prefixes = { "get", "is" }; //$NON-NLS-1$//$NON-NLS-2$
		return prefixes;
	}

	/**
	 * @return proposed variable names (may be empty, but not null). The first
	 *         proposal should be used as "best guess" (if it exists).
	 */
	public String[] guessTempNames() {
		final List<String> guessedTempNames = new ArrayList<String>();
		final List<String> usedNames = new ArrayList<String>();
		IASTFunctionDefinition funcDef = ASTQueries.findAncestorWithType(target, IASTFunctionDefinition.class);
		final IScope scope;
		if (funcDef != null && funcDef.getBody() instanceof IASTCompoundStatement) {
			IASTCompoundStatement body = (IASTCompoundStatement) funcDef.getBody();
			scope = body.getScope();
		} else {
			scope = null;
		}

		if (target != null) {
			target.accept(new ASTVisitor() {
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
					// If the expression starts with a function call with a name, we should only
					// need to guess this name
					if (expression == target && expression instanceof IASTFunctionCallExpression) {
						IASTFunctionCallExpression functionCallExpression = (IASTFunctionCallExpression) expression;
						IASTExpression functionNameExpression = functionCallExpression.getFunctionNameExpression();
						if (functionNameExpression instanceof IASTIdExpression) {
							IASTIdExpression idExpression = (IASTIdExpression) functionNameExpression;
							if (idExpression.getName() != null) {
								addTempName(idExpression.getName().getLastName().toString());
								if (guessedTempNames.size() > 0) {
									return PROCESS_ABORT;
								}
							}
						}
					}

					if (expression instanceof IASTLiteralExpression) {
						IASTLiteralExpression literal = (IASTLiteralExpression) expression;
						String name = null;
						switch (literal.getKind()) {
				          case IASTLiteralExpression.lk_char_constant:
				              name = "c"; //$NON-NLS-1$
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
					name = trimPrefixes(name);

			    	IPreferencesService preferences = Platform.getPreferencesService();
			    	int capitalization = preferences.getInt(CUIPlugin.PLUGIN_ID,
			    			PreferenceConstants.NAME_STYLE_VARIABLE_CAPITALIZATION,
			    			PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CAMEL_CASE, null);
			    	String wordDelimiter = preferences.getString(CUIPlugin.PLUGIN_ID,
			    			PreferenceConstants.NAME_STYLE_VARIABLE_WORD_DELIMITER, "", null); //$NON-NLS-1$
			    	String prefix = preferences.getString(CUIPlugin.PLUGIN_ID,
									PreferenceConstants.NAME_STYLE_VARIABLE_PREFIX, "", null); //$NON-NLS-1$
			    	String suffix = preferences.getString(CUIPlugin.PLUGIN_ID,
			    			PreferenceConstants.NAME_STYLE_VARIABLE_SUFFIX, "", null); //$NON-NLS-1$
			    	NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
			    	name = composer.compose(name);

					if (name.length() > 0) {
						if (nameAvailable(name, guessedTempNames, scope)) {
							guessedTempNames.add(name);
						} else {
							usedNames.add(name);
						}
					}
				}
			});
		}
		if (guessedTempNames.isEmpty()) {
			String name = makeTempName(usedNames, scope);
			if (name != null) {
				guessedTempNames.add(name);
			}
		}
		return guessedTempNames.toArray(new String[guessedTempNames.size()]);
	}

	private String trimPrefixes(String name) {
		String lower = name.toLowerCase();
		int start = 0;
		for (String prefix : getPrefixes()) {
			if (lower.startsWith(prefix)) {
				if (name.length() > prefix.length()) {
					start = prefix.length();
				}
			}
			prefix = prefix + "_"; //$NON-NLS-1$
			if (lower.startsWith(prefix)) {
				if (name.length() > prefix.length()) {
					start = prefix.length();
				}
			}
		}

		if (start > 0) {
			String nameWithoutPrefix = name.substring(start);
			if (Character.isUpperCase(nameWithoutPrefix.charAt(0))) {
				nameWithoutPrefix = nameWithoutPrefix.substring(0, 1).toLowerCase()
						+ nameWithoutPrefix.substring(1);
			}

			if (!Character.isJavaIdentifierStart(nameWithoutPrefix.charAt(0))) {
				nameWithoutPrefix = "_" + nameWithoutPrefix; //$NON-NLS-1$
			}
			return nameWithoutPrefix;
		} else {
			return name;
		}
	}

	private boolean nameAvailable(String name, List<String> guessedNames, IScope scope) {
		if (guessedNames.contains(name) || info.getUsedNames().contains(name)) {
			return false;
		}
		if (scope != null) {
			IBinding[] bindings = scope.find(name);
			return bindings == null || bindings.length == 0;
		}
		return true; // No name references found
	}

	private String makeTempName(List<String> usedNames, IScope scope) {
		List<String> noNames = new ArrayList<String>();
		for (int i = 0; i < 10; i++) {
			for (String used : usedNames) {
				String name = used + i;   // such as "i2"
				if (nameAvailable(name, noNames, scope)) {
					return name;
				}
			}
		}
		return null;
	}

	public int getOccurrences() {
		if (targets == null) {
			return 0;
		}
		return targets.size();
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		Map<String, String> arguments = getArgumentMap();
		RefactoringDescriptor desc = new ExtractLocalVariableRefactoringDescriptor(project.getProject().getName(),
				"Extract Local Variable Refactoring", "Extract " + target.getRawSignature(), arguments);  //$NON-NLS-1$//$NON-NLS-2$
		return desc;
	}

	private Map<String, String> getArgumentMap() {
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put(CRefactoringDescriptor.FILE_NAME, tu.getLocationURI().toString());
		arguments.put(CRefactoringDescriptor.SELECTION, selectedRegion.getOffset() + "," + selectedRegion.getLength()); //$NON-NLS-1$
		arguments.put(ExtractLocalVariableRefactoringDescriptor.NAME, info.getName());
		return arguments;
	}

	public ExtractLocalVariableInfo getRefactoringInfo() {
		return info;
	}
}
