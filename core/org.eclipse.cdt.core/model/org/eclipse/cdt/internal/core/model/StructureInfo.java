/**********************************************************************
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

import org.eclipse.cdt.core.model.ICElement;

public class StructureInfo extends SourceManipulationInfo {

	protected String typeStr;
	protected boolean isStatic;
	protected boolean isVolatile;
	protected boolean isConst;

	protected StructureInfo (CElement element) {
		super(element);		
	}

	protected String getTypeName(){
		return typeStr;
	}
	
	protected void setTypeName(String type){
		typeStr = type;
	}

	public boolean isUnion() {
		return element.getElementType() == ICElement.C_UNION;
	}

	public boolean isClass() {
		return element.getElementType() == ICElement.C_CLASS;
	}

	public boolean isStruct() {
		return element.getElementType() == ICElement.C_STRUCT;
	}

	/**
	 * Returns the isStatic.
	 * @return boolean
	 */
	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * Sets the isStatic.
	 * @param isStatic The isStatic to set
	 */
	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	/**
	 * Returns the isVolatile.
	 * @return boolean
	 */
	public boolean isVolatile() {
		return isVolatile;
	}

	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

	/**
	 * Returns the isConst.
	 * @return boolean
	 */
	public boolean isConst() {
		return isConst;
	}

	/**
	 * Sets the isConst.
	 * @param isConst The isConst to set
	 */
	public void setConst(boolean isConst) {
		this.isConst = isConst;
	}

}
