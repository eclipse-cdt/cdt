/**********************************************************************
 * Created on Mar 26, 2003
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * @author jcamelon
 *
 */
public class ExceptionSpecifier {

	private List typeNames = new LinkedList();
	private boolean throwsException = false;
	
	/**
	 * @return List
	 */
	public List getTypeNames() {
		return Collections.unmodifiableList( typeNames );
	}

	public void addTypeName( String name )
	{
		typeNames.add( name );
	}

	/**
	 * Sets the throwsException.
	 * @param throwsException The throwsException to set
	 */
	public void setThrowsException(boolean throwsException) {
		this.throwsException = throwsException;
	}

	/**
	 * @return boolean
	 */
	public boolean throwsException() {
		return throwsException;
	}

}
