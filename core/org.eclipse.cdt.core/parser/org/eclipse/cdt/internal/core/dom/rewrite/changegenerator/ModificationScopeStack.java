/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationMap;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class ModificationScopeStack {
	private final Deque<List<ASTModification>> scopeStack;
	private final ASTModificationStore modStore;

	public ModificationScopeStack(ASTModificationStore modificationStore) {
		scopeStack = new ArrayDeque<>();
		modStore = modificationStore;
		ArrayList<ASTModification> nullModList = new ArrayList<>();
		nullModList.add(null);
		scopeStack.addFirst(nullModList);
	}

	public void pushScope(IASTNode node) {
		List<ASTModification> newMods = new ArrayList<>();
		for (ASTModification peekMod : scopeStack.peek()) {
			ASTModificationMap nestedMods = modStore.getNestedModifications(peekMod);
			if (nestedMods != null) {
				newMods.addAll(nestedMods.getModificationsForNode(node));
			}
		}

		if (!newMods.isEmpty()) {
			scopeStack.addFirst(newMods);
		}
	}

	private List<ASTModification> getNestedModifikationsForNode(IASTNode node) {
		ASTModificationMap rootModifications = modStore.getRootModifications();
		if (rootModifications == null) {
			return Collections.emptyList();
		}
		return rootModifications.getModificationsForNode(node);
	}

	public void popScope(IASTNode node) {
		List<ASTModification> peek = scopeStack.peek();
		if (peek != null && !peek.isEmpty()) {
			ASTModification modification = peek.get(0);
			if (modification != null) {
				if (modification.getKind() == ModificationKind.REPLACE) {
					if (modification.getTargetNode() == node)
						scopeStack.removeFirst();
				} else if (modification.getNewNode() == node) {
					scopeStack.removeFirst();
				}
			}
		}
	}

	public Collection<IASTNode> getModifiedNodes() {
		List<ASTModification> aktModList = scopeStack.peek();
		if (aktModList == null) {
			return getNestedModifiedNodes();
		}
		Collection<IASTNode> nodes = new ArrayList<>();
		for (ASTModification modification : aktModList) {
			ASTModificationMap nestedModifications = modStore.getNestedModifications(modification);
			if (nestedModifications != null) {
				nodes.addAll(nestedModifications.getModifiedNodes());
			}
		}
		return Collections.unmodifiableCollection(nodes);
	}

	private Collection<IASTNode> getNestedModifiedNodes() {
		ASTModificationMap rootModifications = modStore.getRootModifications();
		if (rootModifications == null) {
			return Collections.emptyList();
		}
		return rootModifications.getModifiedNodes();
	}

	public List<ASTModification> getModificationsForNode(IASTNode node) {
		List<ASTModification> aktModList = scopeStack.peek();
		if (aktModList == null) {
			return getNestedModifikationsForNode(node);
		}
		List<ASTModification> modForNodeList = new ArrayList<>();
		for (ASTModification modification : aktModList) {
			ASTModificationMap nestedModifications = modStore.getNestedModifications(modification);
			if (nestedModifications != null) {
				modForNodeList.addAll(nestedModifications.getModificationsForNode(node));
			}
		}
		return Collections.unmodifiableList(modForNodeList);
	}

	public void clean(IASTNode actualNode) {
		while (scopeStack.size() > 1) {
			for (IASTNode currentModifiedNode : getModifiedNodes()) {
				for (ASTModification currentMod : getModificationsForNode(currentModifiedNode)) {
					if (currentMod.getNewNode() == actualNode) {
						return;
					}
				}
			}
			if (!nodeIsChildOfModifications(actualNode, scopeStack.getFirst())) {
				if (scopeStack.getFirst().get(0).getTargetNode().getTranslationUnit() == actualNode
						.getTranslationUnit()) {
					scopeStack.removeFirst();
				} else {
					return;
				}
			} else {
				return;
			}
		}
	}

	private boolean nodeIsChildOfModifications(IASTNode actualNode, List<ASTModification> modifications) {
		for (ASTModification currentModification : modifications) {
			if (currentModification != null && nodeIsChildOfModification(currentModification, actualNode)) {
				return true;
			}
		}
		return false;
	}

	private boolean nodeIsChildOfModification(ASTModification modification, IASTNode actualNode) {
		IASTNode nodeToTest = actualNode;
		while (nodeToTest != null) {
			if (modification.getNewNode() == nodeToTest) {
				return true;
			} else {
				nodeToTest = nodeToTest.getParent();
			}
		}
		return false;
	}
}
