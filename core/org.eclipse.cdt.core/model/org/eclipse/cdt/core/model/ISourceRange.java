package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * A source range defines an element's source coordinates
 */
public interface ISourceRange {

	/**
	 * Returns the 0-based starting position of this element.
	 */
	public int getStartPos();

	/**
	 * Returns the number of characters of the source code for this element.
	 */
	public int getLength();
        
	/**
	 * Returns the Id starting position of this element.
	 */
	public int getIdStartPos();

	/**
	 * Returns the number of characters of the Id for this element.
	 */
	public int getIdLength();

	/**
	 * Returns the 1-based starting line of this element.
	 */
	public int getStartLine();

	/**
	 * Returns the 1-based ending line of this element.
	 */
	public int getEndLine();
}
