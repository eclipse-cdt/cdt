/**********************************************************************
 * Created on Mar 25, 2003
 *
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.Token;

/**
 * @author jcamelon
 *
 */
public class EnumerationSpecifier extends TypeSpecifier implements IOffsetable {
	
	public EnumerationSpecifier(IOwner declaration) {
		super(declaration);
	}
	
	private Name name = null;
	private List enumeratorDefinitions = new ArrayList();
	private int startingOffset = 0, totalLength = 0;
	private Token startToken = null;
	
	public void addEnumeratorDefinition( EnumeratorDefinition def )
	{
		enumeratorDefinitions.add( def );
	}
	
	
	/**
	 * @return List
	 */
	public List getEnumeratorDefinitions() {
		return Collections.unmodifiableList( enumeratorDefinitions );
	}

	/**
	 * @return Name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}

	/**
	 * @return
	 */
	public int getStartingOffset() {
		return startingOffset;
	}

	/**
	 * @return
	 */
	public int getTotalLength() {
		return totalLength;
	}

	/**
	 * @param i
	 */
	public void setStartingOffset(int i) {
		startingOffset = i;
	}

	/**
	 * @param i
	 */
	public void setTotalLength(int i) {
		totalLength = i;
	}

	/**
	 * Returns the startToken.
	 * @return Token
	 */
	public Token getStartToken() {
		return startToken;
	}

	/**
	 * Sets the startToken.
	 * @param startToken The startToken to set
	 */
	public void setStartToken(Token startToken) {
		this.startToken = startToken;
	}

	private int topLine = 0, bottomLine = 0; 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#setTopLine(int)
	 */
	public void setTopLine(int lineNumber) {
		topLine = lineNumber;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#setBottomLine(int)
	 */
	public void setBottomLine(int lineNumber) {
		bottomLine = lineNumber;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#getTopLine()
	 */
	public int getTopLine() {
		return topLine;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#getBottomLine()
	 */
	public int getBottomLine() {
		return bottomLine;
	}

}
