/*******************************************************************************
 * Copyright (c) 2008, 2011 Google and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Google - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.DeclarationGenerator;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTEqualsInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescription;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.NameNVisibilityInformation;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
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
	private final NameNVisibilityInformation info;
	private NodeContainer container;

	public ExtractLocalVariableRefactoring(IFile file, ISelection selection, NameNVisibilityInformation info,
			ICProject project) {
		super(file, selection, null, project);
		this.info = info;
		name = Messages.ExtractLocalVariable;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 9);
		try {
			lockIndex();
			try {
				RefactoringStatus status = super.checkInitialConditions(sm.newChild(6));
				if (status.hasError()) {
					return status;
				}

				container = findAllExpressions();
				if (container.size() < 1) {
					initStatus.addFatalError(Messages.ExpressionMustBeSelected);
					return initStatus;
				}

				sm.worked(1);
				if (isProgressMonitorCanceld(sm, initStatus))
					return initStatus;

				boolean oneMarked = region != null && isOneMarked(container.getNodesToWrite(), region);
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

				sm.worked(1);

				info.addNamesToUsedNames(findAllDeclaredNames());
				sm.worked(1);

				NodeHelper.findMethodContext(container.getNodesToWrite().get(0), getIndex());
				sm.worked(1);

				info.setName(guessTempName());
				sm.done();
			} finally {
				unlockIndex();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return initStatus;
	}

	private ArrayList<String> findAllDeclaredNames() {
		ArrayList<String> names = new ArrayList<String>();
		IASTFunctionDefinition funcDef = NodeHelper.findFunctionDefinitionInAncestors(target);
		ICPPASTCompositeTypeSpecifier comTypeSpec = getCompositeTypeSpecifier(funcDef);
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

	private ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier(IASTFunctionDefinition funcDef) {
		if (funcDef != null) {
			IBinding binding = funcDef.getDeclarator().getName().resolveBinding();
			if (binding instanceof CPPFunction) {
				CPPFunction function = (CPPFunction) binding;
				IASTNode[] decls = function.getDeclarations();
				if (decls != null && decls.length > 0) {
					IASTNode spec = decls[0].getParent().getParent();
					if (spec instanceof ICPPASTCompositeTypeSpecifier) {
						return (ICPPASTCompositeTypeSpecifier) spec;
					}
				}
			}
		}
		return null;
	}

	private boolean isOneMarked(List<IASTNode> selectedNodes, Region textSelection) {
		boolean oneMarked = false;
		for (IASTNode node : selectedNodes) {
			if (node instanceof IASTExpression) {
				IASTExpression expression = (IASTExpression) node;
				boolean isInSameFileSelection =
						SelectionHelper.isInSameFileSelection(textSelection, expression, file);
				if (isInSameFileSelection && isExpressionInSelection(expression, textSelection)) {
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

	private boolean isExpressionInSelection(IASTExpression expression, Region selection) {
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

		ast.accept(new ASTVisitor() {
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
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		try {
			lockIndex();
			try {
				String variableName = info.getName();
				TextEditGroup editGroup = new TextEditGroup(Messages.CreateLocalVariable);

				// Define temporary variable declaration and insert it
				IASTStatement declInsertPoint = getParentStatement(target);
				IASTDeclarationStatement declaration = getVariableNodes(variableName);
				declaration.setParent(declInsertPoint.getParent());
				ASTRewrite rewriter = collector.rewriterForTranslationUnit(ast);
				rewriter.insertBefore(declInsertPoint.getParent(), declInsertPoint, declaration, editGroup);

				// Replace target with reference to temporary variable
				CPPASTIdExpression idExpression =
						new CPPASTIdExpression(new CPPASTName(variableName.toCharArray()));
				rewriter.replace(target, idExpression, editGroup);
			} finally {
				unlockIndex();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private IASTStatement getParentStatement(IASTNode node) {
		while (node != null) {
			if (node instanceof IASTStatement && !(node.getParent() instanceof IASTForStatement))
				return (IASTStatement) node;
			node = node.getParent();
		}
		return null;
	}

	private IASTDeclarationStatement getVariableNodes(String newName) {
		INodeFactory factory = this.ast.getASTNodeFactory();
		
		IASTSimpleDeclaration simple = factory.newSimpleDeclaration(null);

		DeclarationGenerator generator = DeclarationGenerator.create(factory);
		
		IASTDeclSpecifier declSpec = generator.createDeclSpecFromType(target.getExpressionType());
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
		IASTFunctionDefinition funcDef = NodeHelper.findFunctionDefinitionInAncestors(target);
		final IScope scope;
		if (funcDef != null && funcDef.getBody() instanceof IASTCompoundStatement) {
			IASTCompoundStatement body = (IASTCompoundStatement)funcDef.getBody();
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
					if (expression == target && expression instanceof ICPPASTFunctionCallExpression) {
						ICPPASTFunctionCallExpression functionCallExpression = (ICPPASTFunctionCallExpression) expression;
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
					
					if (expression instanceof CPPASTLiteralExpression) {
						CPPASTLiteralExpression literal = (CPPASTLiteralExpression) expression;
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
		return true; // no name references found
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

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		Map<String, String> arguments = getArgumentMap();
		RefactoringDescriptor desc = new ExtractLocalVariableRefactoringDescription(project.getProject().getName(),
				"Extract Local Variable Refactoring", "Extract " + target.getRawSignature(), arguments);  //$NON-NLS-1$//$NON-NLS-2$
		return desc;
	}

	private Map<String, String> getArgumentMap() {
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put(CRefactoringDescription.FILE_NAME, file.getLocationURI().toString());
		arguments.put(CRefactoringDescription.SELECTION, region.getOffset() + "," + region.getLength()); //$NON-NLS-1$
		arguments.put(ExtractLocalVariableRefactoringDescription.NAME, info.getName());
		return arguments;
	}
}
