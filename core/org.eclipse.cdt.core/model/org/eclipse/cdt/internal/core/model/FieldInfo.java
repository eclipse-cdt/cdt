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

	/**
	 * Tests info stored in element info only
	 * @param otherInfo
	 * @return boolean
	 */
	public boolean hasSameContentsAs( SourceManipulationInfo info){
		FieldInfo otherInfo = (FieldInfo) info;
		if( (typeStr.equals(otherInfo.getTypeName())) 
		&&  (isConst == otherInfo.isConst())
		&&  (isVolatile == otherInfo.isVolatile())
		&& 	(isMutable == otherInfo.isMutable())
		&& 	(visibility == otherInfo.getVisibility())
		)
			return true;
		else
			return false;
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

	protected void setIsConst(boolean isConst){
		this.isConst = isConst;
	}

	protected boolean isVolatile(){
		return isVolatile;
	}

	protected void setIsVolatile(boolean isVolatile){
		this.isVolatile = isVolatile;
	}

	protected boolean isMutable(){
		return isMutable;
	}

	protected void setIsMutable(boolean mutable){
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

}
