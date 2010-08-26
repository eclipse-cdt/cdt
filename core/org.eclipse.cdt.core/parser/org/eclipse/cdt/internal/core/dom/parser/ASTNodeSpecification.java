/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;

/**
 * For searching ast-nodes by offset and length, instances of this class can be used to 
 * determine whether a node matches or not.
 *
 * @since 5.0
 */
public class ASTNodeSpecification<T extends IASTNode> {
	public enum Relation {FIRST_CONTAINED, EXACT_MATCH, ENCLOSING, STRICTLY_ENCLOSING}

	private final Class<T> fClass;
	private final Relation fRelation;
	private final int fFileOffset;
	private final int fFileEndOffset;
	private int fSeqNumber;
	private int fSeqEndNumber;
	private int fBestOffset;
	private int fBestEndOffset;
	private T fBestNode;
	private boolean fSearchInExpansion;
	private boolean fZeroToLeft= false;
	
	public ASTNodeSpecification(Relation relation, Class<T> clazz, int fileOffset, int fileLength) {
		fRelation= relation;
		fClass= clazz;
		fFileOffset= fileOffset;
		fFileEndOffset= fileOffset+fileLength;
	}

	public void setRangeInSequence(int offsetInSeq, int lengthInSeq) {
		fSeqNumber= offsetInSeq;
		fSeqEndNumber= offsetInSeq+lengthInSeq;
	}

	public void setRangeInSequence(int offsetInSeq, int lengthInSeq, boolean zeroRangeToLeft) {
		setRangeInSequence(offsetInSeq, lengthInSeq);
		fZeroToLeft= zeroRangeToLeft;
	}
	
	public void setSearchInExpansion(boolean searchInExpansion) {
		fSearchInExpansion= searchInExpansion;
	}

	public Relation getRelationToSelection() {
		return fRelation;
	}

	public int getSequenceStart() {
		return fSeqNumber;
	}

	public int getSequenceEnd() {
		return fSeqEndNumber;
	}

	public T getBestNode() {
		return fBestNode;
	}

	public boolean requiresClass(Class<? extends IASTNode> clazz) {
		return clazz.isAssignableFrom(fClass);
	}
		
	@SuppressWarnings("unchecked")
	public void visit(ASTNode astNode) {
		if (isAcceptableNode(astNode) && isMatchingRange(astNode.getOffset(), astNode.getLength(), fSeqNumber, fSeqEndNumber)) {
			IASTFileLocation loc= astNode.getFileLocation();
			if (loc != null) {
				storeIfBest(loc, (T) astNode);
			}
		}
	}
	@SuppressWarnings("unchecked")
	public void visit(ASTNode astNode, IASTImageLocation imageLocation) {
		if (isAcceptableNode(astNode) && imageLocation != null) {
			if (isMatchingRange(imageLocation.getNodeOffset(), imageLocation.getNodeLength(), fFileOffset, fFileEndOffset)) {
				storeIfBest(imageLocation, (T) astNode);
			}
		}
	}

	private boolean isMatchingRange(int offset, int length, int selOffset, int selEndOffset) {
		final int endOffset= offset+length;
		switch(fRelation) {
		case EXACT_MATCH:
			return selOffset == offset && selEndOffset == endOffset;
		case FIRST_CONTAINED:
			return selOffset <= offset && endOffset <= selEndOffset;
		case ENCLOSING:
			return offset <= selOffset && selEndOffset <= endOffset;
		case STRICTLY_ENCLOSING:
			if (offset <= selOffset && selEndOffset <= endOffset) {
				return offset != selOffset || selEndOffset != endOffset;
			}
			return false; 
		}
		assert false;
		return false;
	}

	public boolean isAcceptableNode(IASTNode astNode) {
		if (astNode == null || !fClass.isAssignableFrom(astNode.getClass())) 
			return false;
		
		if (fSearchInExpansion) {
			IASTNode check= astNode instanceof IASTName ? astNode.getParent() : astNode;
			if (check instanceof IASTPreprocessorMacroExpansion) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns whether the node can contain matches in its range. 
	 */
	public boolean canContainMatches(ASTNode node) {
		final int offset= node.getOffset();
		final int endOffset= offset+node.getLength();
		switch(fRelation) {
		case EXACT_MATCH:
		case ENCLOSING:
			return offset <= fSeqNumber && fSeqEndNumber <= endOffset;
		case STRICTLY_ENCLOSING:
			if (offset <= fSeqNumber && fSeqEndNumber <= endOffset) {
				return offset != fSeqNumber || fSeqEndNumber != endOffset;
			}
			return false; 
		case FIRST_CONTAINED:
			return offset <= fSeqEndNumber && fSeqNumber <= endOffset;
		}
		assert false;
		return false;
	}

	private void storeIfBest(IASTFileLocation loc, T astNode) {
		if (loc != null) {
			final int offset = loc.getNodeOffset();
			final int length = loc.getNodeLength();
			if (isBetterMatch(offset, length, astNode)) {
				fBestNode= astNode;
				fBestOffset= offset;
				fBestEndOffset= offset+length;
			}
		}
	}

	/**
	 * Assuming that the given range matches, this method returns whether the match is better 
	 * than the best match stored.
	 */
	private boolean isBetterMatch(int offset, int length, IASTNode cand) {
		if (fBestNode == null) {
			return true;
		}
		
		final int endOffset= offset+length;
		switch(fRelation) {
		case EXACT_MATCH:
			return isParent(fBestNode, cand);
		case FIRST_CONTAINED:
			if (offset < fBestOffset) {
				return true;
			}
			if (offset == fBestOffset) {
				if (endOffset < fBestEndOffset) {
					return true;
				}
				return endOffset == fBestEndOffset && isParent(fBestNode, cand);
			}
			return false;
		case ENCLOSING: 
		case STRICTLY_ENCLOSING: 
			final int bestLength= fBestEndOffset-fBestOffset;
			if (length < bestLength) {
				return true;
			}
			return length == bestLength && isParent(fBestNode, cand);
		default:
			assert false;
			return false;
		}
	}

	private boolean isParent(IASTNode cand1, IASTNode cand2) {
		while(cand2 != null) {
			if (cand2 == cand1) {
				return true;
			}
			cand2= cand2.getParent();
		}
		return false;
	}

	public IASTPreprocessorMacroExpansion findLeadingMacroExpansion(ASTNodeSelector nodeSelector) {
		IASTPreprocessorMacroExpansion exp= nodeSelector.findEnclosingMacroExpansion(fZeroToLeft ? fFileOffset-1 : fFileOffset, 1);
		if (fRelation == Relation.ENCLOSING || fRelation == Relation.STRICTLY_ENCLOSING)
			return exp;
		
		if (exp != null) {
			IASTFileLocation loc= exp.getFileLocation();
			if (loc != null) {
				final int offset= loc.getNodeOffset();
				final int endOffset= offset+loc.getNodeLength();
				if (offset == fFileOffset && endOffset <= fFileEndOffset)
					return exp;
			}
		}
		return null;
	}

	public IASTPreprocessorMacroExpansion findTrailingMacroExpansion(ASTNodeSelector nodeSelector) {
		IASTPreprocessorMacroExpansion exp= nodeSelector.findEnclosingMacroExpansion(fFileEndOffset==fFileOffset && !fZeroToLeft ? fFileEndOffset : fFileEndOffset-1, 1);
		if (fRelation == Relation.ENCLOSING || fRelation == Relation.STRICTLY_ENCLOSING)
			return exp;
		
		if (exp != null) {
			IASTFileLocation loc= exp.getFileLocation();
			if (loc != null) {
				final int offset= loc.getNodeOffset();
				final int endOffset= offset+loc.getNodeLength();
				if (endOffset == fFileEndOffset && offset >= fFileOffset)
					return exp;
			}
		}
		return null;
	}
}