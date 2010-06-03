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

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.text.edits.TextEditGroup;

public class ASTModification {
	public enum ModificationKind {
		REPLACE,
		INSERT_BEFORE,
		APPEND_CHILD
	}
	
	private final ModificationKind fKind;
	private final IASTNode fTargetNode;
	private final IASTNode fNewNode;
	private final TextEditGroup fTextEditGroup;

	public ASTModification(ModificationKind kind, IASTNode targetNode, IASTNode newNode, TextEditGroup group) {
		fKind= kind;
		fTargetNode= targetNode;
		fNewNode= newNode;
		fTextEditGroup= group;
	}

	/**
	 * @return the kind of the modification
	 */
	public ModificationKind getKind() {
		return fKind;
	}

	/**
	 * Return the target node of this modification.
	 */
	public IASTNode getTargetNode() {
		return fTargetNode;
	}
	
	/**
	 * Return the new node of this modification, or <code>null</code>
	 */
	public IASTNode getNewNode() {
		return fNewNode;
	}
	
	/**
	 * Returns the edit group to collect the text edits of this modification.
	 * @return the edit group or <code>null</code>.
	 */
	public TextEditGroup getAssociatedEditGroup() {
		return fTextEditGroup;
	}
}
