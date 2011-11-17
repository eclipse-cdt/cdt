/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;

public class FunctionDeclaration extends SourceManipulation implements IFunctionDeclaration {
	protected String[] fParameterTypes;
	protected String returnType;

	public FunctionDeclaration(ICElement parent, String name) {
		super(parent, name, ICElement.C_FUNCTION_DECLARATION);
		fParameterTypes= fgEmptyStrings;
	}

	public FunctionDeclaration(ICElement parent, String name, int type) {
		super(parent, name, type);
		fParameterTypes= fgEmptyStrings;
	}

	@Override
	public String getReturnType(){
		if (returnType != null) {
			return returnType;
		}
		return ""; //$NON-NLS-1$
	}

	public void setReturnType(String type){
		returnType = type;
	}

	@Override
	public int getNumberOfParameters() {
		return fParameterTypes == null ? 0 : fParameterTypes.length;
	}

	@Override
	public String[] getParameterTypes() {
		return fParameterTypes;
	}

	public void setParameterTypes(String[] parameterTypes) {
		fParameterTypes = parameterTypes;
	}

	@Override
	public String getSignature() throws CModelException{
		return getSignature(this);
	}

	public static String getSignature(IFunctionDeclaration func) {
		StringBuffer sig = new StringBuffer(func.getElementName());
		sig.append(getParameterClause(func.getParameterTypes()));
		try {
			if(func.isConst())
				sig.append(" const"); //$NON-NLS-1$
			if(func.isVolatile()) {
				sig.append(" volatile"); //$NON-NLS-1$
			}
		} catch (CModelException e) {
		}
		return sig.toString();
	}

	public String getParameterClause() {
		return getParameterClause(getParameterTypes());
	}

	public static String getParameterClause(String[] paramTypes){
		StringBuffer sig = new StringBuffer();

		if(paramTypes.length > 0){
			sig.append("("); //$NON-NLS-1$
			int i = 0;
			sig.append(paramTypes[i++]);
			while (i < paramTypes.length){
				sig.append(", "); //$NON-NLS-1$
				sig.append(paramTypes[i++]);
			}
			sig.append(")"); //$NON-NLS-1$
		}
		else{
			sig.append("()"); //$NON-NLS-1$
		}
		return sig.toString();
	}

	@Override
	public String getParameterInitializer(int pos) {
		return ""; //$NON-NLS-1$
	}

	@Override
	public String[] getExceptions(){
		return new String[] {};
	}

	@Override
	protected CElementInfo createElementInfo () {
		return new FunctionInfo(this);
	}

	protected FunctionInfo getFunctionInfo() throws CModelException{
		return (FunctionInfo) getElementInfo();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof IFunctionDeclaration) {
			return equals(this, (IFunctionDeclaration) other);
		}
		return false;
	}

	public static boolean equals(IFunctionDeclaration lhs, IFunctionDeclaration rhs) {
		return CElement.equals(lhs, rhs) &&
			Util.equalArraysOrNull(lhs.getParameterTypes(), rhs.getParameterTypes()) &&
			lhs.getReturnType().equals(rhs.getReturnType());
	}

	/**
	 * FunctionDeclarations and Functions can not be constant
	 * @see org.eclipse.cdt.core.model.IDeclaration#isConst()
	 */
	@Override
	public boolean isConst() throws CModelException{
		return getFunctionInfo().isConst();
	}

	public void setConst(boolean isConst) throws CModelException{
		getFunctionInfo().setConst(isConst);
	}

	/**
	 * Returns the isStatic.
	 * @return boolean
	 */
	@Override
	public boolean isStatic() throws CModelException {
		return getFunctionInfo().isStatic();
	}

	/**
	 * Returns the isVolatile.
	 * @return boolean
	 */
	@Override
	public boolean isVolatile() throws CModelException {
		return getFunctionInfo().isVolatile();
	}

	/**
	 * Sets the isStatic.
	 * @param isStatic The isStatic to set
	 */
	public void setStatic(boolean isStatic) throws CModelException {
		getFunctionInfo().setStatic(isStatic);
	}

	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) throws CModelException {
		getFunctionInfo().setVolatile(isVolatile);
	}

	@Override
	public void getHandleMemento(StringBuilder buff) {
		super.getHandleMemento(buff);
		for (int i = 0; i < fParameterTypes.length; i++) {
			buff.append(CEM_PARAMETER);
			escapeMementoName(buff, fParameterTypes[i]);
		}
	}

}
