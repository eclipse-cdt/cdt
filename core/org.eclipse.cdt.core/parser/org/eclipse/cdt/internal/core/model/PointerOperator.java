/**********************************************************************
 * Created on Mar 31, 2003
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
package org.eclipse.cdt.internal.core.model;

/**
 * @author jcamelon
 *
 */
public class PointerOperator {
	
	private final Declarator ownerDeclarator;
	
	public PointerOperator( Declarator decl )
	{
		ownerDeclarator = decl;
	} 

	/**
	 * @return Declarator
	 */
	public Declarator getOwnerDeclarator() {
		return ownerDeclarator;
	} 
	public static final int k_pointer = 1; 
	public static final int k_reference = 2; 
	
	private boolean isConst = false;
	private boolean isVolatile = false; 
	private int kind; 

	/**
	 * @return boolean
	 */
	public boolean isConst() {
		return isConst;
	}

	/**
	 * @return boolean
	 */
	public boolean isVolatile() {
		return isVolatile;
	}

	/**
	 * @return int
	 */
	public int getKind() {
		return kind;
	}

	/**
	 * Sets the isConst.
	 * @param isConst The isConst to set
	 */
	public void setConst(boolean isConst) {
		this.isConst = isConst;
	}

	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

	/**
	 * Sets the kind.
	 * @param kind The kind to set
	 */
	public void setKind(int kind) {
		this.kind = kind;
	}

}
