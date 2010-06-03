/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.rewrite;

import java.util.HashMap;

/**
 * Collects modifications to an AST in a hierarchical manner. The store gives access to
 * the root modifications and for each modification you can obtain the nested modifications here.
 * @since 5.0
 */
public class ASTModificationStore {
	private HashMap<ASTModification,ASTModificationMap> fNestedModMaps;
	
	public ASTModificationStore() {
		fNestedModMaps= new HashMap<ASTModification, ASTModificationMap>();
	}

	/**
	 * Adds a potentially nested modification to the store.
	 * @param parentMod the parent for a nested modification, or <code>null</code>
	 * @param mod a modification
	 */
	public void storeModification(ASTModification parentMod, ASTModification mod) {
		ASTModificationMap modMap = createNestedModificationMap(parentMod);
		modMap.addModification(mod);
	}

	private ASTModificationMap createNestedModificationMap(ASTModification parentMod) {
		ASTModificationMap modMap= fNestedModMaps.get(parentMod);
		if (modMap == null) {
			modMap= new ASTModificationMap();
			fNestedModMaps.put(parentMod, modMap);
		}
		return modMap;
	}
	
	/**
	 * Returns the modifications that are performed directly on the AST, or <code>null</code> if there
	 * are no modifications.
	 * @return the root modifications or <code>null</code>.
	 */
	public ASTModificationMap getRootModifications() {
		return fNestedModMaps.get(null);
	}
	
	/**
	 * Returns the modifications that are performed on the node that has been introduced by the
	 * given modification. If there are no nested modifications, <code>null</code> is returned.
	 * @return the nested modifications or <code>null</code>.
	 */
	public ASTModificationMap getNestedModifications(ASTModification mod) {
		return fNestedModMaps.get(mod);
	}
}
