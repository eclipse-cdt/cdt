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

import org.eclipse.cdt.internal.core.parser.util.Name;

/**
 * @author jcamelon
 *
 */
public class EnumerationSpecifier extends TypeSpecifier implements IOffsettable {
	
	public EnumerationSpecifier(SimpleDeclaration declaration) {
		super(declaration);
	}
	
	private Name name = null;
	private List enumeratorDefinitions = new ArrayList();
	private int startingOffset = 0, totalLength = 0;
	
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

}
