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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethodDeclaration;

public class MethodDeclaration extends FunctionDeclaration implements IMethodDeclaration{
	

	public MethodDeclaration(ICElement parent, String name){
		super(parent, name, CElement.C_METHOD_DECLARATION);
	}


	public MethodDeclaration(ICElement parent, String name, int type){
		super(parent, name, type);
	}
	
	/**
	 * @see IMethod
	 */
	public boolean isConstructor(){
		return getElementName().equals(getParent().getElementName());
	}

	/**
	 * @see IMethod
	 */
	public boolean isDestructor() {
		return getElementName().startsWith("~");
	}

	/**
	 * @see IMethod
	 */
	public boolean isOperator(){
		return getElementName().startsWith("operator");
	}

	public boolean isAbstract(){
		return getMethodInfo().isAbstract();
	}

	public void setIsAbstract(boolean isAbstract){
		getMethodInfo().setIsAbstract(isAbstract);
	}

	public boolean isStatic(){
		return getMethodInfo().isStatic();
	}

	public void setIsStatic(boolean isStatic){
		getMethodInfo().setIsStatic(isStatic);
	}

	public boolean isInline(){
		return getMethodInfo().isInline();
	}

	public void setIsInline(boolean isInline){
		getMethodInfo().setIsInline(isInline);
	}

	public boolean isVirtual(){
		return getMethodInfo().isVirtual();
	}

	public void setIsVirtual(boolean isVirtual){
		getMethodInfo().setIsVirtual(isVirtual);
	}

	public boolean isFriend(){
		return getMethodInfo().isFriend();
	}

	public void setIsFriend(boolean isFriend){
		getMethodInfo().setIsFriend(isFriend);
	}

	public boolean isConst(){
		return getMethodInfo().isConst();
	}

	public void setIsConst(boolean isConst){
		getMethodInfo().setIsConst(isConst);
	}

	public boolean isVolatile(){
		return getMethodInfo().isVolatile();
	}

	public void setIsVolatile(boolean isVolatile){
		getMethodInfo().setIsVolatile(isVolatile);
	}

	public int getVisibility(){
		return getMethodInfo().getVisibility();
	}
	
	public void setVisibility(int visibility){
		getMethodInfo().setVisibility(visibility);
	}
	// do we need this one or not?
	// can we get this info from the parser or not?
	public boolean hasClassScope(){
		return false;
	}
	
	protected CElementInfo createElementInfo () {
		return new MethodInfo(this);
	}
	
	private MethodInfo getMethodInfo(){
		return (MethodInfo) getElementInfo();
	}
	
	/*
	 * See if we need anything else to put in equals here
	 */
	public boolean equals(Object other) {
		return ( super.equals(other) );
	}
		
}
