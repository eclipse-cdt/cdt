/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.ast2.c;

import org.eclipse.cdt.core.parser.ast2.IASTType;

/**
 * This class supports C qualifiers on types
 * @author Doug Schaefer
 */
public interface ICASTModifiedType extends IASTType {

	/**
	 * @return the type being modified.
	 */
	public IASTType getType();
	
	/**
	 * @param type the type to be modified.
	 */
	public void setType(IASTType type);
	
	/**
	 * @return is the type a const type, e.g. const int
	 */
	public boolean isConst();
	
	/**
	 * Sets whether this type is const or not.
	 * 
	 * @param isConst
	 */
	public void setIsConst(boolean isConst);
	
}
