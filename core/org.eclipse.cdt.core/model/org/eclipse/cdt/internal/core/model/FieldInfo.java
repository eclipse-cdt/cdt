package org.eclipse.cdt.internal.core.model;

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

import org.eclipse.cdt.core.model.IMember;

public class FieldInfo extends SourceManipulationInfo {

	int flags = 0;
	String typeStr = "";
	boolean isConst = false;
	boolean isVolatile = false;
	boolean isMutable = false;
	boolean isStatic = false;
	int visibility;
	
	protected FieldInfo (CElement element) {
		super(element);
		flags = 0;
		visibility = IMember.V_PRIVATE;
	}

	protected int getAccessControl() {
		return flags;
	}

	protected String getTypeName(){
		return typeStr;
	}
	
	protected void setAccessControl(int flags) {
		this.flags = flags;
	}
	
	protected void setTypeName(String type){
		typeStr = type;
	}
	
	protected boolean isConst(){
		return isConst;
	}

	protected void setConst(boolean isConst){
		this.isConst = isConst;
	}

	protected boolean isVolatile(){
		return isVolatile;
	}

	protected void setVolatile(boolean isVolatile){
		this.isVolatile = isVolatile;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	protected boolean isMutable(){
		return isMutable;
	}

	protected void setMutable(boolean mutable){
		this.isMutable = mutable;
	}
	/**
	 * Returns the visibility.
	 * @return int
	 */
	public int getVisibility() {
		return visibility;
	}

	/**
	 * Sets the visibility.
	 * @param visibility The visibility to set
	 */
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.SourceManipulationInfo#hasSameContentsAs(SourceManipulationInfo)
	 */
	public boolean hasSameContentsAs( SourceManipulationInfo info){
		
		return( super.hasSameContentsAs(info)
		&&  (typeStr.equals(((FieldInfo)info).getTypeName())) 
		&&  (isConst == ((FieldInfo)info).isConst())
		&&  (isVolatile == ((FieldInfo)info).isVolatile())
		&& 	(isMutable == ((FieldInfo)info).isMutable())
		&& 	(visibility == ((FieldInfo)info).getVisibility())
		&& 	(isStatic == ((FieldInfo)info).isStatic())
		);
	}
	
}
