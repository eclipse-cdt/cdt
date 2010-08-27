/*******************************************************************************
 * Copyright (c) 2008, 2010 Google and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Google - initial API and implementation
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
import org.eclipse.core.runtime.SubMonitor;
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
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.DeclarationGenerator;
import org.eclipse.cdt.core.model.ICProject;

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

/**
 * The main class for the Extract Local Variable refactoring. This refactoring
 * differs from the Extract Constant refactoring in that any valid expression
 * which can be used to initialize a local variable can be extracted.
 *
 * @author Tom Ball
 */
public class ExtractLocalVariableRefactoring extends CRefactoring {
	
	public static final String ID = "org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable.ExtractLocalVariableRefactoring"; //$NON-NLS-1$
	
	private IASTExpression target = null;
	private final NameNVisibilityInformation info;
	private NodeContainer container;

	public ExtractLocalVariableRefactoring(IFile file, ISelection selection,
			NameNVisibilityInformation info, ICProject project) {
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
				if(status.hasError()) {
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
			}finally {
				unlockIndex();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
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
			if (binding instanceof CPPFunction) {

				CPPFunction function = (CPPFunction) binding;
				IASTNode[] decls = function.getDeclarations();
				if (decls != null && decls.length > 0) {
					IASTNode spec = decls[0].getParent().getParent();
					if (spec instanceof ICPPASTCompositeTypeSpecifier) {
						ICPPASTCompositeTypeSpecifier compTypeSpec =
							(ICPPASTCompositeTypeSpecifier) spec;
						return compTypeSpec;
					}
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

		unit.accept(new ASTVisitor() {
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
		try {
			lockIndex();
			try {
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
			}finally {
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
		
		INodeFactory factory = this.unit.getASTNodeFactory();
		
		IASTSimpleDeclaration simple = factory.newSimpleDeclaration(null);

		DeclarationGenerator generator = DeclarationGenerator.create(factory);
		
		IASTDeclSpecifier declSpec = generator.createDeclSpecFromType(target.getExpressionType());
		declSpec.setStorageClass(IASTDeclSpecifier.sc_unspecified);
		simple.setDeclSpecifier(declSpec);

		IASTDeclarator decl = generator.createDeclaratorFromType(target.getExpressionType(), newName.toCharArray());

		IASTEqualsInitializer init = new CPPASTEqualsInitializer();
		init.setInitializerClause(deblock(target.copy()));
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
		final List<String> usedNames = new ArrayList<String>();
		IASTFunctionDefinition funcDef = NodeHelper
			.findFunctionDefinitionInAncestors(target);
		final IScope scope;
		if (funcDef != null &&
				funcDef.getBody() instanceof IASTCompoundStatement) {
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
		return guessedTempNames.toArray(new String[0]);
	}

	private boolean nameAvailable(String name, List<String> guessedNames, IScope scope) {
		if (guessedNames.contains(name) ||
				info.getUsedNames().contains(name)) {
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
		RefactoringDescriptor desc = new ExtractLocalVariableRefactoringDescription(project.getProject().getName(), "Extract Local Variable Refactoring", "Extract " + target.getRawSignature(), arguments);  //$NON-NLS-1$//$NON-NLS-2$
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
