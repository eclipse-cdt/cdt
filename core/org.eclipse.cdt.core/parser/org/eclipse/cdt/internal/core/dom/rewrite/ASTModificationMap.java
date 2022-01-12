/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;

/**
 * Represents a list of modifications to an AST node. If there are nested modifications
 * to nodes introduced by insertions or replacements, these modifications are collected
 * in separate modification maps. I.e. a modification map represents one level of modifications.
 * @see ASTModificationStore
 * @since 5.0
 */
public class ASTModificationMap {
	private final Map<IASTNode, List<ASTModification>> fModifications = new IdentityHashMap<>();

	/**
	 * Adds a modification to this modification map.
	 */
	public void addModification(ASTModification mod) {
		IASTNode targetNode = mod.getTargetNode();
		List<ASTModification> mods = fModifications.get(targetNode);
		if (mods == null || mods.isEmpty()) {
			mods = new ArrayList<>();
			mods.add(mod);
			fModifications.put(targetNode, mods);
		} else {
			switch (mod.getKind()) {
			case REPLACE:
				if (mods.get(mods.size() - 1).getKind() != ModificationKind.INSERT_BEFORE) {
					throw new IllegalArgumentException("Attempt to replace a node that has been modified"); //$NON-NLS-1$
				}
				mods.add(mod);
				break;
			case APPEND_CHILD:
				if (mods.get(mods.size() - 1).getKind() == ModificationKind.REPLACE) {
					throw new IllegalArgumentException("Attempt to modify a node that has been replaced"); //$NON-NLS-1$
				}
				mods.add(mod);
				break;
			case INSERT_BEFORE:
				int i;
				for (i = mods.size(); --i >= 0;) {
					if (mods.get(i).getKind() == ModificationKind.INSERT_BEFORE) {
						break;
					}
				}
				mods.add(i + 1, mod);
				break;
			}
		}
	}

	/**
	 * Returns the list of modifications for a given node. The list can contain different
	 * modifications. It is guaranteed that INSERT_BEFORE modifications appear first. Furthermore,
	 * if there is a REPLACE modification the list will not contain any other REPLACE or
	 * APPEND_CHILD modifications.
	 * @return the modification list, which may be empty.
	 */
	public List<ASTModification> getModificationsForNode(IASTNode node) {
		List<ASTModification> modList = fModifications.get(node);
		if (modList == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(modList);
	}

	/**
	 * Returns the collection of nodes that are modified by this modification map.
	 */
	public Collection<IASTNode> getModifiedNodes() {
		return Collections.unmodifiableCollection(fModifications.keySet());
	}
}
