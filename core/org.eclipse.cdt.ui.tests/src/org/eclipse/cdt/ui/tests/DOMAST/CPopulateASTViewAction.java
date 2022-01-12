/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author dsteffle
 */
public class CPopulateASTViewAction extends ASTVisitor implements IPopulateDOMASTAction {
	private static final int INITIAL_PROBLEM_SIZE = 4;

	{
		shouldVisitNames = true;
		shouldVisitDeclarations = true;
		shouldVisitInitializers = true;
		shouldVisitParameterDeclarations = true;
		shouldVisitDeclarators = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitDesignators = true;
		shouldVisitExpressions = true;
		shouldVisitStatements = true;
		shouldVisitTypeIds = true;
		shouldVisitEnumerators = true;
	}

	DOMASTNodeParent root;
	IProgressMonitor monitor;
	IASTProblem[] astProblems = new IASTProblem[INITIAL_PROBLEM_SIZE];

	public CPopulateASTViewAction(IASTTranslationUnit tu, IProgressMonitor monitor) {
		root = new DOMASTNodeParent(tu);
		this.monitor = monitor;
	}

	private class DOMASTNodeLeafContinue extends DOMASTNodeLeaf {
		public DOMASTNodeLeafContinue(IASTNode node) {
			super(node);
		}
	}

	/**
	 * Returns {@code null} if the algorithm should stop (monitor was cancelled).
	 * Returns DOMASTNodeLeafContinue if the algorithm should continue but no valid DOMASTNodeLeaf
	 * was added (i.e. node was {@code null}). Return the DOMASTNodeLeaf added to the DOM AST view's
	 * model otherwise
	 */
	private DOMASTNodeLeaf addRoot(IASTNode node) {
		if (monitor != null && monitor.isCanceled())
			return null;
		if (node == null)
			return new DOMASTNodeLeafContinue(null);

		// Only do length check for ASTNode (getNodeLocations on PreprocessorStatements is very expensive).
		if (node instanceof ASTNode && ((ASTNode) node).getLength() <= 0)
			return new DOMASTNodeLeafContinue(null);

		DOMASTNodeParent parent = null;

		// If it's a preprocessor statement being merged then do a special search for parent (no search).
		if (node instanceof IASTPreprocessorStatement) {
			parent = root;
		} else {
			IASTNode tempParent = node.getParent();
			if (tempParent instanceof IASTPreprocessorStatement) {
				parent = root.findTreeParentForMergedNode(node);
			} else {
				parent = root.findTreeParentForNode(node);
			}
		}

		if (parent == null)
			parent = root;

		return createNode(parent, node);
	}

	private DOMASTNodeLeaf createNode(DOMASTNodeParent parent, IASTNode node) {
		DOMASTNodeParent tree = new DOMASTNodeParent(node);
		parent.addChild(tree);

		// set filter flags
		if (node instanceof IASTProblemHolder || node instanceof IASTProblem) {
			tree.setFiltersFlag(DOMASTNodeLeaf.FLAG_PROBLEM);

			if (node instanceof IASTProblemHolder) {
				astProblems = ArrayUtil.append(astProblems, ((IASTProblemHolder) node).getProblem());
			} else {
				astProblems = ArrayUtil.append(astProblems, (IASTProblem) node);
			}
		}
		if (node instanceof IASTPreprocessorStatement)
			tree.setFiltersFlag(DOMASTNodeLeaf.FLAG_PREPROCESSOR);
		if (node instanceof IASTPreprocessorIncludeStatement)
			tree.setFiltersFlag(DOMASTNodeLeaf.FLAG_INCLUDE_STATEMENTS);

		return tree;
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		DOMASTNodeLeaf temp = addRoot(declaration);
		if (temp == null)
			return PROCESS_ABORT;
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		DOMASTNodeLeaf temp = addRoot(declarator);

		IASTPointerOperator[] ops = declarator.getPointerOperators();
		for (IASTPointerOperator op : ops) {
			addRoot(op);
		}

		if (declarator instanceof IASTArrayDeclarator) {
			IASTArrayModifier[] mods = ((IASTArrayDeclarator) declarator).getArrayModifiers();
			for (IASTArrayModifier mod : mods) {
				addRoot(mod);
			}
		}

		if (temp == null)
			return PROCESS_ABORT;
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(ICASTDesignator designator) {
		DOMASTNodeLeaf temp = addRoot(designator);
		if (temp == null)
			return PROCESS_ABORT;
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		DOMASTNodeLeaf temp = addRoot(declSpec);
		if (temp == null)
			return PROCESS_ABORT;
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTEnumerator enumerator) {
		DOMASTNodeLeaf temp = addRoot(enumerator);
		if (temp == null)
			return PROCESS_ABORT;
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTExpression expression) {
		DOMASTNodeLeaf temp = addRoot(expression);
		if (temp == null)
			return PROCESS_ABORT;
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTInitializer initializer) {
		DOMASTNodeLeaf temp = addRoot(initializer);
		if (temp == null)
			return PROCESS_ABORT;
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTName name) {
		DOMASTNodeLeaf temp = null;
		if (name.toString() == null)
			return PROCESS_CONTINUE;
		temp = addRoot(name);
		if (temp == null)
			return PROCESS_ABORT;
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		DOMASTNodeLeaf temp = addRoot(parameterDeclaration);
		if (temp == null)
			return PROCESS_ABORT;
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTStatement statement) {
		DOMASTNodeLeaf temp = addRoot(statement);
		if (temp == null)
			return PROCESS_ABORT;
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTTypeId typeId) {
		DOMASTNodeLeaf temp = addRoot(typeId);
		if (temp == null)
			return PROCESS_ABORT;
		return PROCESS_CONTINUE;
	}

	private DOMASTNodeLeaf mergeNode(ASTNode node) {
		DOMASTNodeLeaf temp = addRoot(node);

		if (node instanceof IASTPreprocessorMacroDefinition)
			addRoot(((IASTPreprocessorMacroDefinition) node).getName());

		return temp;
	}

	@Override
	public DOMASTNodeLeaf[] mergePreprocessorStatements(IASTPreprocessorStatement[] statements) {
		DOMASTNodeLeaf[] leaves = new DOMASTNodeLeaf[statements.length];
		for (int i = 0; i < statements.length; i++) {
			if (monitor != null && monitor.isCanceled())
				return leaves;

			if (statements[i] instanceof ASTNode)
				leaves[i] = mergeNode((ASTNode) statements[i]);
		}

		return leaves;
	}

	@Override
	public void mergePreprocessorProblems(IASTProblem[] problems) {
		for (IASTProblem problem : problems) {
			if (monitor != null && monitor.isCanceled())
				return;

			if (problem instanceof ASTNode)
				mergeNode((ASTNode) problem);
		}
	}

	@Override
	public DOMASTNodeParent getTree() {
		return root;
	}

	@Override
	public void groupIncludes(DOMASTNodeLeaf[] treeIncludes) {
		// Loop through the includes and make sure that all of the nodes
		// that are children of the TU are in the proper include (based on offset)
		for (int i = treeIncludes.length; --i >= 0;) {
			final DOMASTNodeLeaf nodeLeaf = treeIncludes[i];
			if (nodeLeaf == null || !(nodeLeaf.getNode() instanceof IASTPreprocessorIncludeStatement))
				continue;

			final String path = ((IASTPreprocessorIncludeStatement) nodeLeaf.getNode()).getPath();
			final DOMASTNodeLeaf[] children = root.getChildren(false);
			for (DOMASTNodeLeaf child : children) {
				if (child != null && child != nodeLeaf && child.getNode().getContainingFilename().equals(path)) {
					root.removeChild(child);
					((DOMASTNodeParent) nodeLeaf).addChild(child);
				}
			}
		}
	}

	public IASTProblem[] getASTProblems() {
		return astProblems;
	}
}
