/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;

/**
 * A location context representing a file.
 * @since 5.0
 */
class LocationCtxFile extends LocationCtxContainer {
	private final String fFilename;
	private final ASTInclusionStatement fASTInclude;
	private final boolean fIsSource;

	public LocationCtxFile(LocationCtxContainer parent, String filename, AbstractCharArray source,
			int parentOffset, int parentEndOffset, int sequenceNumber,
			ASTInclusionStatement inclusionStatement, boolean isSource) {
		super(parent, source, parentOffset, parentEndOffset, sequenceNumber);
		fFilename= new String(filename);
		fASTInclude= inclusionStatement;
		fIsSource= isSource;
	}
	
	@Override
	public final void addChildSequenceLength(int childLength) {
		super.addChildSequenceLength(childLength);
	}

	@Override
	public final String getFilePath() {
		return fFilename;
	}

	@Override
	public ASTFileLocation findMappedFileLocation(int sequenceNumber, int length) {
		// try to delegate to a child.
		final int testEnd= length > 1 ? sequenceNumber + length - 1 : sequenceNumber;
		final int sequenceEnd= sequenceNumber+length;
		final LocationCtx child1= findChildLessOrEqualThan(sequenceNumber, false);
		final LocationCtx child2= testEnd == sequenceNumber ?
				child1 : findChildLessOrEqualThan(testEnd, false);
	
		if (child1 == child2 && child1 != null &&
				child1.fSequenceNumber + child1.getSequenceLength() > testEnd) {
			return child1.findMappedFileLocation(sequenceNumber, length);
		}
		
		// handle here
		int startOffset;
		int endOffset;
		
		if (child1 == null) {
			startOffset= sequenceNumber-fSequenceNumber;
		} else {
			int childSequenceEnd= child1.fSequenceNumber + child1.getSequenceLength();
			if (sequenceNumber < childSequenceEnd) {
				startOffset= child1.fOffsetInParent;
			} else {	// start beyond child1
				startOffset= child1.fEndOffsetInParent + sequenceNumber - childSequenceEnd;
			}
		}
		if (child2 == null) {
			endOffset= sequenceEnd - fSequenceNumber;
		} else {
			int childSequenceEnd= child2.fSequenceNumber + child2.getSequenceLength();
			if (childSequenceEnd < sequenceEnd) { // beyond child2
				endOffset= child2.fEndOffsetInParent + sequenceEnd - childSequenceEnd;
			} else {
				endOffset= child2.fEndOffsetInParent;
			}
		}
		return new ASTFileLocation(this, startOffset, endOffset - startOffset);
	}
	
	@Override
	public ASTFileLocation createMappedFileLocation(int offset, int length) {
		return new ASTFileLocation(this, offset, length);
	}

	@Override
	public ASTInclusionStatement getInclusionStatement() {
		return fASTInclude;
	}

	@Override
	ASTFileLocation createFileLocation(int start, int length) {
		return new ASTFileLocation(this, start, length);
	}

	public boolean isThisFile(int sequenceNumber) {
		LocationCtx child= findChildLessOrEqualThan(sequenceNumber, false);
		if (!(child instanceof LocationCtxFile)) {
			return true;
		}
		return sequenceNumber >= child.fSequenceNumber + child.getSequenceLength();
	}

	public void collectMacroExpansions(int offset, int length,
			ArrayList<IASTPreprocessorMacroExpansion> list) {
		Collection<LocationCtx> children= getChildren();
		for (LocationCtx ctx : children) {
			// context must start before the end of the search range
			if (ctx.fOffsetInParent >= offset+length) {
				break;
			}
			if (ctx instanceof LocationCtxMacroExpansion) {
				// expansion must end after the search start
				if (ctx.fEndOffsetInParent > offset) {
					IASTNode macroExpansion =
							((LocationCtxMacroExpansion) ctx).getMacroReference().getParent();
					list.add((IASTPreprocessorMacroExpansion) macroExpansion);
				}
			}
		}
	}
	
	@Override
	public boolean isSourceFile() {
		return fIsSource;
	}
	
	@Override
	public String toString() {
		return fFilename;
	}
}