/*******************************************************************************
 * Copyright (c) 2008, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Base class for {@link CVisitor} and {@link CPPVisitor}
 */
public class ASTQueries {
	private static class NameSearch extends ASTVisitor {
		private boolean fFound;

		NameSearch() {
			super(false);
			shouldVisitAmbiguousNodes = true;
			shouldVisitNames = true;
		}

		public void reset() {
			fFound = false;
		}

		public boolean foundName() {
			return fFound;
		}

		@Override
		public int visit(IASTName name) {
			fFound = true;
			return PROCESS_ABORT;
		}

		@Override
		public int visit(ASTAmbiguousNode node) {
			IASTNode[] alternatives = node.getNodes();
			for (IASTNode alt : alternatives) {
				if (!alt.accept(this))
					return PROCESS_ABORT;
			}
			return PROCESS_CONTINUE;
		}
	}

	private static NameSearch NAME_SEARCH = new NameSearch();

	/**
	 * Tests whether the given node can contain ast-names, suitable to be used before ambiguity
	 * resolution.
	 */
	public static boolean canContainName(IASTNode node) {
		if (node == null)
			return false;

		NAME_SEARCH.reset();
		node.accept(NAME_SEARCH);
		return NAME_SEARCH.foundName();
	}

	/**
	 * Returns the outermost declarator the given {@code declarator} nests within, or
	 * the given {@code declarator} itself.
	 */
	public static IASTDeclarator findOutermostDeclarator(IASTDeclarator declarator) {
		IASTDeclarator outermost = null;
		IASTNode candidate = declarator;
		while (candidate instanceof IASTDeclarator) {
			outermost = (IASTDeclarator) candidate;
			candidate = outermost.getParent();
		}
		return outermost;
	}

	/**
	 * Returns the innermost declarator nested within the given {@code declarator}, or
	 * the given {@code declarator} itself.
	 */
	public static IASTDeclarator findInnermostDeclarator(IASTDeclarator declarator) {
		IASTDeclarator innermost = null;
		while (declarator != null) {
			innermost = declarator;
			declarator = declarator.getNestedDeclarator();
		}
		return innermost;
	}

	/**
	 * Searches for the innermost declarator that contributes the the type declared.
	 */
	public static IASTDeclarator findTypeRelevantDeclarator(IASTDeclarator declarator) {
		if (declarator == null)
			return null;

		IASTDeclarator result = findInnermostDeclarator(declarator);
		while (result.getPointerOperators().length == 0 && !(result instanceof IASTFieldDeclarator)
				&& !(result instanceof IASTFunctionDeclarator) && !(result instanceof IASTArrayDeclarator)) {
			final IASTNode parent = result.getParent();
			if (parent instanceof IASTDeclarator) {
				result = (IASTDeclarator) parent;
			} else {
				return result;
			}
		}
		return result;
	}

	/**
	 * Traverses parent chain of the given node and returns the first node of the given type.
	 * @param node the start node
	 * @param type the type to look for
	 * @return the node itself or its closest ancestor that has the given type, or {@code null}
	 *     if no such node is found.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends IASTNode> T findAncestorWithType(IASTNode node, Class<T> type) {
		while (node != null && !type.isInstance(node)) {
			node = node.getParent();
		}
		return (T) node;
	}

	/**
	 * Searches for the function enclosing the given node. May return {@code null}.
	 */
	public static IBinding findEnclosingFunction(IASTNode node) {
		IASTFunctionDefinition functionDefinition = findAncestorWithType(node, IASTFunctionDefinition.class);
		if (functionDefinition == null)
			return null;

		IASTDeclarator dtor = findInnermostDeclarator(functionDefinition.getDeclarator());
		if (dtor != null) {
			IASTName name = dtor.getName();
			if (name != null) {
				return name.resolveBinding();
			}
		}
		return null;
	}

	/**
	 * Extracts the active declarations from an array of declarations.
	 */
	public static IASTDeclaration[] extractActiveDeclarations(IASTDeclaration[] allDeclarations, int size) {
		IASTDeclaration[] active;
		if (size == 0) {
			active = IASTDeclaration.EMPTY_DECLARATION_ARRAY;
		} else {
			active = new IASTDeclaration[size];
			int j = 0;
			for (int i = 0; i < size; i++) {
				IASTDeclaration d = allDeclarations[i];
				if (d.isActive()) {
					active[j++] = d;
				}
			}
			active = ArrayUtil.trim(active, j);
		}
		return active;
	}

	public static boolean isSameType(IType type1, IType type2) {
		if (type1 == type2)
			return true;
		if (type1 == null || type2 == null)
			return false;
		return type1.isSameType(type2);
	}

	protected static boolean areArraysOfTheSameElementType(IType t1, IType t2) {
		if (t1 instanceof IArrayType && t2 instanceof IArrayType) {
			IArrayType a1 = (IArrayType) t1;
			IArrayType a2 = (IArrayType) t2;
			return isSameType(a1.getType(), a2.getType());
		}
		return false;
	}

	/**
	 * Check whether 'ancestor' is an ancestor of 'descendant' in the AST.
	 */
	public static boolean isAncestorOf(IASTNode ancestor, IASTNode descendant) {
		do {
			if (descendant == ancestor)
				return true;
			descendant = descendant.getParent();
		} while (descendant != null);
		return false;
	}

	protected static boolean isLabelReference(IASTNode node) {
		boolean labelReference = false;
		IASTNode parent = node.getParent();

		if (parent instanceof IASTUnaryExpression) {
			int operator = ((IASTUnaryExpression) parent).getOperator();
			labelReference = operator == IASTUnaryExpression.op_labelReference;
		}

		return labelReference;
	}

	private static class FindLabelsAction extends ASTVisitor {
		public IASTLabelStatement[] labels = IASTLabelStatement.EMPTY_ARRAY;

		public FindLabelsAction() {
			shouldVisitStatements = true;
		}

		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTLabelStatement) {
				labels = ArrayUtil.append(labels, (IASTLabelStatement) statement);
			}
			return PROCESS_CONTINUE;
		}
	}

	protected static ILabel[] getLabels(IASTFunctionDefinition functionDefinition) {
		FindLabelsAction action = new FindLabelsAction();

		functionDefinition.accept(action);

		ILabel[] result = ILabel.EMPTY_ARRAY;
		if (action.labels != null) {
			for (int i = 0; i < action.labels.length && action.labels[i] != null; i++) {
				IASTLabelStatement labelStatement = action.labels[i];
				IBinding binding = labelStatement.getName().resolveBinding();
				if (binding != null)
					result = ArrayUtil.append(result, (ILabel) binding);
			}
		}
		return ArrayUtil.trim(result);
	}

	protected static IBinding resolveLabel(IASTName labelReference) {
		char[] labelName = labelReference.toCharArray();
		IASTFunctionDefinition functionDefinition = findAncestorWithType(labelReference, IASTFunctionDefinition.class);
		if (functionDefinition != null) {
			for (ILabel label : getLabels(functionDefinition)) {
				if (CharArrayUtils.equals(label.getNameCharArray(), labelName)) {
					return label;
				}
			}
		}
		// Label not found.
		return new ProblemBinding(labelReference, IProblemBinding.SEMANTIC_LABEL_STATEMENT_NOT_FOUND, labelName);
	}
}
