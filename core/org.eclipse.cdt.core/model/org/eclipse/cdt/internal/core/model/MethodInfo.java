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

public class MethodInfo extends FunctionInfo {

	boolean isAbstract = false;
	boolean isStatic = false;
	boolean isInline = false;
	boolean isVirtual = false;
	boolean isFriend = false;
	boolean isConst = false;
	boolean isVolatile = false;
	int visibility;
		
	MethodInfo(CElement element) {
		super(element);
		visibility = IMember.V_PRIVATE;
	}
	
	public boolean isAbstract(){
		return isAbstract;
	}

	public void setIsAbstract(boolean isAbstract){
		this.isAbstract = isAbstract;
	}

	public boolean isStatic(){
		return isStatic;
	}

	public void setIsStatic(boolean isStatic){
		this.isStatic = isStatic;
	}

	public boolean isInline(){
		return isInline;
	}

	public void setIsInline(boolean isInline){
		this.isInline = isInline;
	}

	public boolean isVirtual(){
		return isVirtual;
	}

	public void setIsVirtual(boolean isVirtual){
		this.isVirtual = isVirtual;
	}

	public boolean isFriend(){
		return isFriend;
	}

	public void setIsFriend(boolean isFriend){
		this.isFriend = isFriend;
	}

	public boolean isConst(){
		return isConst;
	}

	public void setIsConst(boolean isConst){
		this.isConst = isConst;
	}

	public boolean isVolatile(){
		return isVolatile;
	}

	public void setIsVolatile(boolean isVolatile){
		this.isVolatile = isVolatile;
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
