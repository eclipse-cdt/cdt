/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.code.flow;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IRegion;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class Selection {
	/** Flag indicating that the AST node somehow intersects with the selection. */
	public static final int INTERSECTS= 0;

	/** Flag that indicates that an AST node appears before the selected nodes. */
	public static final int BEFORE= 1;

	/** Flag indicating that an AST node is covered by the selection. */
	public static final int SELECTED= 2;

	/** Flag indicating that an AST nodes appears after the selected nodes. */
	public static final int AFTER= 3;

	private int fStart;
	private int fLength;
	private int fEnd;

	protected Selection() {
	}

	/**
	 * Creates a new selection from the given start and length.
	 *
	 * @param start the start offset of the selection (inclusive)
	 * @param length the length of the selection
	 * @return the created selection object
	 */
	public static Selection createFromStartLength(int start, int length) {
		Assert.isTrue(start >= 0 && length >= 0);
		Selection result= new Selection();
		result.fStart= start;
		result.fLength= length;
		result.fEnd= start + length;
		return result;
	}

	/**
	 * Creates a new selection from the given start and end.
	 *
	 * @param start the start offset of the selection (inclusive)
	 * @param end the end offset of the selection (exclusive)
	 * @return the created selection object
	 */
	public static Selection createFromStartEnd(int start, int end) {
		Assert.isTrue(start >= 0 && end >= start);
		Selection result= new Selection();
		result.fStart= start;
		result.fLength= end - start;
		result.fEnd= result.fStart + result.fLength;
		return result;
	}

	public int getOffset() {
		return fStart;
	}

	public int getLength() {
		return fLength;
	}

	public int getEnd() {
		return fEnd;
	}

	/**
	 * Returns the selection mode of the given AST node regarding this selection. Possible
	 * values are <code>INTERSECTS</code>, <code>BEFORE</code>, <code>SELECTED</code>, and
	 * <code>AFTER</code>.
	 *
	 * @param node the node to return the visit mode for
	 *
	 * @return the selection mode of the given AST node regarding this selection
	 * @see #INTERSECTS
	 * @see #BEFORE
	 * @see #SELECTED
	 * @see #AFTER
	 */
	public int getVisitSelectionMode(IASTNode node) {
		IASTFileLocation location = node.getFileLocation();
		int nodeStart= location.getNodeOffset();
		int nodeEnd= nodeStart + location.getNodeLength();
		if (nodeEnd <= fStart) {
			return BEFORE;
		} else if (covers(node)) {
			return SELECTED;
		} else if (fEnd <= nodeStart) {
			return AFTER;
		}
		return INTERSECTS;
	}

	public int getEndVisitSelectionMode(IASTNode node) {
		IASTFileLocation location = node.getFileLocation();
		int nodeStart= location.getNodeOffset();
		int nodeEnd= nodeStart + location.getNodeLength();
		if (nodeEnd <= fStart) {
			return BEFORE;
		} else if (covers(node)) {
			return SELECTED;
		} else if (nodeEnd >= fEnd) {
			return AFTER;
		}
		return INTERSECTS;
	}

	// cover* methods do a closed interval check.

	public boolean covers(int position) {
		return fStart <= position && position < fStart + fLength;
	}

	public boolean covers(IASTNode node) {
		IASTFileLocation location = node.getFileLocation();
		int nodeStart= location.getNodeOffset();
		return fStart <= nodeStart && nodeStart + location.getNodeLength() <= fEnd;
	}

	public boolean coveredBy(IASTNode node) {
		IASTFileLocation location = node.getFileLocation();
		int nodeStart= location.getNodeOffset();
		return nodeStart <= fStart && fEnd <= nodeStart + location.getNodeLength();
	}

	public boolean coveredBy(IRegion region) {
		int regionStart= region.getOffset();
		return regionStart <= fStart && fEnd <= regionStart + region.getLength();
	}

	public boolean endsIn(IASTNode node) {
		IASTFileLocation location = node.getFileLocation();
		int nodeStart= location.getNodeOffset();
		return nodeStart < fEnd && fEnd < nodeStart + location.getNodeLength();
	}

	public boolean liesOutside(IASTNode node) {
		IASTFileLocation location = node.getFileLocation();
		int nodeStart= location.getNodeOffset();
		return fEnd < nodeStart || nodeStart + location.getNodeLength() < fStart; 
	}

	@Override
	public String toString() {
		return "<start == " + fStart + ", length == " + fLength + "/>";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}