/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * For searching ast-nodes by offset and length, instances of this class can be used to 
 * determine whether a node matches or not.
 *
 * @since 5.0
 */
public class ASTNodeMatchKind {
	public enum Relation {FIRST_CONTAINED, EXACT, SURROUNDING}

	static final ASTNodeMatchKind MATCH_EXACT= new ASTNodeMatchKind(Relation.EXACT, false);
	static final ASTNodeMatchKind MATCH_EXACT_NAME= new ASTNodeMatchKind(Relation.EXACT, true);
	static final ASTNodeMatchKind MATCH_FIRST_CONTAINED= new ASTNodeMatchKind(Relation.FIRST_CONTAINED, false);
	static final ASTNodeMatchKind MATCH_FIRST_NAME_CONTAINED= new ASTNodeMatchKind(Relation.FIRST_CONTAINED, true);
	static final ASTNodeMatchKind MATCH_SURROUNDING= new ASTNodeMatchKind(Relation.SURROUNDING, false);
	static final ASTNodeMatchKind MATCH_SURROUNDING_NAME= new ASTNodeMatchKind(Relation.SURROUNDING, true);
	
	private boolean fNamesOnly;
	private Relation fRelation;
    
	private ASTNodeMatchKind(Relation relation, boolean namesOnly) {
		fRelation= relation;
		fNamesOnly= namesOnly;
	}
	
	public Relation getRelationToSelection() {
		return fRelation;
	}

	public boolean matchNamesOnly() {
		return fNamesOnly;
	}
	
	/**
	 * Returns whether the node matches the selection.
	 */
	public boolean matches(ASTNode node, int selOffset, int selLength) {
		if (fNamesOnly && node instanceof IASTName == false) {
			return false;
		}
		
		final int nodeOffset= node.getOffset();
		final int nodeLength= node.getLength();
		switch(fRelation) {
		case EXACT:
			return selOffset == nodeOffset && selLength == nodeLength;
		case FIRST_CONTAINED:
			return selOffset <= nodeOffset && nodeOffset+nodeLength <= selOffset+selLength;
		case SURROUNDING:
			return nodeOffset <= selOffset && selOffset+selLength <= nodeOffset+nodeLength;
		default:
			assert false;
		return false;
		}
	}

	/**
	 * Returns whether the node is a lower bound for matching the selection. A node is a lower
	 * bound when a node to the left of the given one cannot match the selection.
	 */
	public boolean isLowerBound(ASTNode node, int selOffset, int selLength) {
		final int nodeOffset= node.getOffset();
		switch(fRelation) {
		case SURROUNDING:
		case EXACT:
			return nodeOffset < selOffset+selLength;
		case FIRST_CONTAINED:
			return nodeOffset <= selOffset;
		default:
			assert false;
		return false;
		}
	}

	/**
	 * Returns whether cand1 is a better match than cand2, prefers cand1 in case of doubt.
	 */
	public boolean isBetterMatch(ASTNode cand1, ASTNode cand2) {
		if (cand1 == null) 
			return false;
		if (cand2 == null)
			return true;
		
		final int nodeOffset1= cand1.getOffset();
		final int nodeLength1= cand1.getLength();
		final int nodeOffset2= cand2.getOffset();
		final int nodeLength2= cand2.getLength();
		switch(fRelation) {
		case EXACT:
			return true;
		case FIRST_CONTAINED:
			return nodeOffset1 < nodeOffset2 || (nodeOffset1 == nodeOffset2 && nodeLength1 <= nodeLength2);
		case SURROUNDING:
			return nodeLength1 <= nodeLength2;
		default:
			assert false;
		return false;
		}
	}
}