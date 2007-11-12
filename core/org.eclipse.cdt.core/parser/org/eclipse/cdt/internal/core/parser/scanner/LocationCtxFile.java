/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

/**
 * A location context representing a file.
 * @since 5.0
 */
class LocationCtxFile extends LocationCtxContainer {
	private final String fFilename;
	private final ASTInclusionStatement fASTInclude;

	public LocationCtxFile(LocationCtxContainer parent, String filename, char[] source, int parentOffset, int parentEndOffset, int sequenceNumber, ASTInclusionStatement inclusionStatement) {
		super(parent, source, parentOffset, parentEndOffset, sequenceNumber);
		fFilename= new String(filename);
		fASTInclude= inclusionStatement;
	}
	
	public final void addChildSequenceLength(int childLength) {
		super.addChildSequenceLength(childLength);
		if (fASTInclude != null) {
			fASTInclude.setLength(fASTInclude.getLength()+childLength);
		}
	}

	public final String getFilePath() {
		return fFilename;
	}

	public IASTFileLocation findMappedFileLocation(int sequenceNumber, int length) {
		// try to delegate to a child.
		final int testEnd= length > 1 ? sequenceNumber+length-1 : sequenceNumber;
		final int sequenceEnd= sequenceNumber+length;
		final LocationCtx child1= findChildLessOrEqualThan(sequenceNumber, false);
		final LocationCtx child2= testEnd == sequenceNumber ? child1 : findChildLessOrEqualThan(testEnd, false);
	
		if (child1 == child2 && child1 != null && child1.fSequenceNumber + child1.getSequenceLength() > testEnd) {
			return child1.findMappedFileLocation(sequenceNumber, length);
		}
		
		// handle here
		int startOffset;
		int endOffset;
		
		if (child1 == null) {
			startOffset= sequenceNumber-fSequenceNumber;
		}
		else {
			int childSequenceEnd= child1.fSequenceNumber + child1.getSequenceLength();
			if (sequenceNumber < childSequenceEnd) {
				startOffset= child1.fOffsetInParent;
			}
			else {	// start beyond child1
				startOffset= child1.fEndOffsetInParent + sequenceNumber-childSequenceEnd;
			}
		}
		if (child2 == null) {
			endOffset= sequenceEnd-fSequenceNumber;
		}
		else {
			int childSequenceEnd= child2.fSequenceNumber + child2.getSequenceLength();
			if (childSequenceEnd < sequenceEnd) { // beyond child2
				endOffset= child2.fEndOffsetInParent+sequenceEnd-childSequenceEnd;
			}
			else {
				endOffset= child2.fEndOffsetInParent;
			}
		}
		return new ASTFileLocation(this, startOffset, endOffset-startOffset);
	}
	
	public IASTFileLocation createMappedFileLocation(int offset, int length) {
		return new ASTFileLocation(this, offset, length);
	}

	public ASTInclusionStatement getInclusionStatement() {
		return fASTInclude;
	}

	ASTFileLocation createFileLocation(int start, int length) {
		return new ASTFileLocation(this, start, length);
	}
}