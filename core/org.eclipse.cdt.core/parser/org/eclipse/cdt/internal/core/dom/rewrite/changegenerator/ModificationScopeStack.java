/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationMap;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

public class ModificationScopeStack {
	private LinkedList<List<ASTModification>> scopeStack;
	private ASTModificationStore modStore;

	public ModificationScopeStack(ASTModificationStore modificationStore) {
		scopeStack = new LinkedList<List<ASTModification>>();
		modStore = modificationStore;
		ArrayList<ASTModification> nullModList = new ArrayList<ASTModification>();
		nullModList.add(null);
		scopeStack.addFirst(nullModList);
	}
	
	public void pushScope(IASTNode node) {
		List<ASTModification> newMods = new ArrayList<ASTModification>();
		for(ASTModification peekMod : scopeStack.peek()) {
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
		if (peek != null) {
			if (!peek.isEmpty() && peek.get(0).getTargetNode() == node) {
				scopeStack.removeFirst();
			}
		}
	}
	
	public Collection<IASTNode> getModifiedNodes() {
		List<ASTModification> aktModList = scopeStack.peek();
		if (aktModList == null) {
			return getNestedModifiedNodes();
		}
		Collection<IASTNode> nodes = new ArrayList<IASTNode>();
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
		if(rootModifications == null) {
			return Collections.emptyList();
		}
		return rootModifications.getModifiedNodes();
	}

	public List<ASTModification> getModificationsForNode(IASTNode node) {
		List<ASTModification> aktModList = scopeStack.peek();
		if (aktModList == null) {
			return getNestedModifikationsForNode(node);
		}
		List<ASTModification> modForNodeList = new ArrayList<ASTModification>();
		for (ASTModification modification : aktModList) {
			modForNodeList.addAll(modStore.getNestedModifications(modification).getModificationsForNode(node));
		}
		return Collections.unmodifiableList(modForNodeList);
	}

}
