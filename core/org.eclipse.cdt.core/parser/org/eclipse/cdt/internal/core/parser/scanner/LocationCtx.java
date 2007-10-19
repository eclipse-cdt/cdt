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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

/**
 * Various location contexts which are suitable for interpreting local offsets. These offsets are
 * converted in a global sequence-number to make all ast nodes comparable with each other.
 * @since 5.0
 */
abstract class LocationCtx implements ILocationCtx {
	final LocationCtx fParent;
	final int fSequenceNumber;
	final int fParentOffset;
	final int fParentEndOffset;

	public LocationCtx(LocationCtx parent, int parentOffset, int parentEndOffset, int sequenceNumber) {
		fParent= parent;
		fParentOffset= parentOffset;
		fParentEndOffset= parentEndOffset;
		fSequenceNumber= sequenceNumber;
	}
	
	public String getFilename() {
		return fParent.getFilename();
	}
	
	final public LocationCtx getParent() {
		return fParent;
	}
	/**
	 * Returns the amount of sequence numbers occupied by this context including its children.
	 */
	public abstract int getSequenceLength();
	
	/**
	 * Converts an offset within this context to the sequence number. In case there are child-contexts
	 * behind the given offset, you need to set checkChildren to <code>true</code>.
	 */
	public int getSequenceNumberForOffset(int offset, boolean checkChildren) {
		return fSequenceNumber+offset;
	}

	/**
	 * When a child-context is finished it reports its total sequence length, such that offsets in this
	 * context can be converted to sequence numbers.
	 */
	public void addChildSequenceLength(int childLength) {
		assert false;
	}
	
	/**
	 * Returns the line number for an offset within this context. Not all contexts support line numbers,
	 * so this may return 0.
	 */
	public int getLineNumber(int offset) {
		return 0;
	}

	/**
	 * Returns the minimal context containing the specified range, assuming that it is contained in
	 * this context.
	 */
	public LocationCtx findContextForSequenceNumberRange(int sequenceNumber, int length) {
		return this;
	}

	/**
	 * Returns the minimal file location containing the specified sequence number range, assuming 
	 * that it is contained in this context.
	 */
	public IASTFileLocation getFileLocationForSequenceNumberRange(int sequenceNumber, int length) {
		return fParent.getFileLocationForOffsetRange(fParentOffset, fParentEndOffset-fParentOffset);
	}

	/**
	 * Returns the file location containing the specified offset range in this context.
	 */
	public IASTFileLocation getFileLocationForOffsetRange(int parentOffset, int length) {
		return fParent.getFileLocationForOffsetRange(fParentOffset, fParentEndOffset-fParentOffset);
	}

	/**
	 * Support for the dependency tree, add inclusion statements found in this context.
	 */
	public void getInclusions(ArrayList target) {
	}

	/**
	 * Support for the dependency tree, returns inclusion statement that created this context, or <code>null</code>.
	 */
	public ASTInclusionStatement getInclusionStatement() {
		return null;
	}
}

class ContainerLocationCtx extends LocationCtx {
	private int fChildSequenceLength;
	private ArrayList fChildren;
	private char[] fSource;
	
	public ContainerLocationCtx(LocationCtx parent, char[] source, int parentOffset, int parentEndOffset, int sequenceNumber) {
		super(parent, parentOffset, parentEndOffset, sequenceNumber);
		fSource= source;
	}
	
	public final int getSequenceLength() {
		return fSource.length + fChildSequenceLength;
	}
	public final int getSequenceNumberForOffset(int offset, boolean checkChildren) {
		int result= fSequenceNumber + fChildSequenceLength + offset;
		if (checkChildren && fChildren != null) {
			for (int i= fChildren.size()-1; i >= 0; i--) {
				final LocationCtx child= (LocationCtx) fChildren.get(i);
				if (child.fParentEndOffset > offset) {	// child was inserted behind the offset, adjust sequence number
					result-= child.getSequenceLength();
				}
				else {
					return result;
				}
			}
		}
		return result;
	}
	
	public void addChildSequenceLength(int childLength) {
		fChildSequenceLength+= childLength;
	}

	public final LocationCtx findContextForSequenceNumberRange(int sequenceNumber, int length) {
		final LocationCtx child= findChildLessOrEqualThan(sequenceNumber);
		if (child != null && child.fSequenceNumber+child.getSequenceLength() >= sequenceNumber+length) {
			return child;
		}
		return this;
	}
	
	public IASTFileLocation getFileLocationForSequenceNumberRange(int sequenceNumber, int length) {
		// try to delegate to a child.
		int useLength= length > 0 ? length-1 : 0;
		final LocationCtx child1= findChildLessOrEqualThan(sequenceNumber);
		final LocationCtx child2= findChildLessOrEqualThan(sequenceNumber+useLength);
		if (child1 == child2 && child1 != null) {
			return child1.getFileLocationForOffsetRange(sequenceNumber, length);
		}
		return super.getFileLocationForSequenceNumberRange(sequenceNumber, length);
	}

	final LocationCtx findChildLessOrEqualThan(final int sequenceNumber) {
		if (fChildren == null) {
			return null;
		}
		int upper= fChildren.size();
		if (upper < 10) {
			for (int i=upper-1; i>=0; i--) {
				LocationCtx child= (LocationCtx) fChildren.get(i);
				if (child.fSequenceNumber <= sequenceNumber) {
					return child;
				}
			}
			return null;
		}
		
		int lower= 0;
		while (upper > lower) {
			int middle= (upper+lower)/2;
			LocationCtx child= (LocationCtx) fChildren.get(middle);
			if (child.fSequenceNumber <= sequenceNumber) {
				lower= middle+1;
			}
			else {
				upper= middle;
			}
		}
		if (lower > 0) {
			return (LocationCtx) fChildren.get(lower-1);
		}
		return null;
	}

	public void getInclusions(ArrayList result) {
		for (Iterator iterator = fChildren.iterator(); iterator.hasNext();) {
			LocationCtx ctx= (LocationCtx) iterator.next();
			if (ctx.getInclusionStatement() != null) {
				result.add(new ASTInclusionNode(ctx));
			}
			else {
				ctx.getInclusions(result);
			}
		}
	}
}

class FileLocationCtx extends ContainerLocationCtx {
	private final String fFilename;
	private final ASTInclusionStatement fASTInclude;

	public FileLocationCtx(LocationCtx parent, String filename, char[] source, int parentOffset, int parentEndOffset, int sequenceNumber, ASTInclusionStatement inclusionStatement) {
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

	public final String getFilename() {
		return fFilename;
	}

	public IASTFileLocation getFileLocationForSequenceNumberRange(int sequenceNumber, int length) {
		// try to delegate to a child.
		final int sequenceEnd= sequenceNumber+length;
		final LocationCtx child1= findChildLessOrEqualThan(sequenceNumber);
		final LocationCtx child2= sequenceEnd == sequenceNumber ? child1 : findChildLessOrEqualThan(sequenceEnd-1);
		if (child1 == child2 && child1 != null) {
			return child1.getFileLocationForOffsetRange(sequenceNumber, length);
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
				startOffset= child1.fParentOffset;
			}
			else {	// start beyond child1
				startOffset= child1.fParentEndOffset + sequenceNumber-childSequenceEnd;
			}
		}
		if (child2 == null) {
			endOffset= sequenceEnd-fSequenceNumber;
		}
		else {
			int childSequenceEnd= child2.fSequenceNumber + child2.getSequenceLength();
			if (childSequenceEnd < sequenceEnd) { // beyond child2
				endOffset= child2.fParentEndOffset+sequenceEnd-childSequenceEnd;
			}
			else {
				endOffset= child2.fParentEndOffset;
			}
		}
		return new ASTFileLocation(fFilename, startOffset, endOffset-startOffset);
	}

	public int getLineNumber(int offset) {
		// mstodo Auto-generated method stub
		return super.getLineNumber(offset);
	}

	public ASTInclusionStatement getInclusionStatement() {
		return fASTInclude;
	}
}


class MacroExpansionCtx extends LocationCtx {
	private final int fLength;

	public MacroExpansionCtx(LocationCtx parent, int parentOffset, int parentEndOffset,
			int sequenceNumber, int length, ImageLocationInfo[] imageLocations,	ASTPreprocessorName expansion) {
		super(parent, parentOffset, parentEndOffset, sequenceNumber);
		fLength= length;
	}

	public int getSequenceLength() {
		return fLength;
	}
	
	// mstodo once image locations are supported we need to handle those in here
}