package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;

public class FunctionDeclaration extends SourceManipulation implements IFunctionDeclaration {
	/**
	 * An empty list of Strings
	 */
	protected static final String[] fgEmptyList= new String[] {};
	protected String[] fParameterTypes;
	protected String returnType;
	
	public FunctionDeclaration(ICElement parent, String name) {
		super(parent, name, CElement.C_FUNCTION_DECLARATION);
		fParameterTypes= fgEmptyList;
	}

	public FunctionDeclaration(ICElement parent, String name, int type) {
		super(parent, name, type);
		fParameterTypes= fgEmptyList;
	}

	public String getReturnType(){
		if (returnType != null)
			return returnType;
		else
			return "";
	}

	public void setReturnType(String type){
		returnType = type;
		getFunctionInfo().setReturnType(type);
	}

	public int getNumberOfParameters() {
		return fParameterTypes == null ? 0 : fParameterTypes.length;
	}

	public String[] getParameterTypes() {
		return fParameterTypes;
	}
	
	public void setParameterTypes(String[] parameterTypes) {
		fParameterTypes = parameterTypes;
	}		
		
	public String getSignature(){
		String sig = getReturnType();
		sig += " ";
		sig += getElementName();
		if(getNumberOfParameters() > 0){
			sig += "(";
			String[] paramTypes = getParameterTypes();
			int i = 0;
			sig += paramTypes[i++];
			while (i < paramTypes.length){
				sig += (", ");
				sig += paramTypes[i++];
			}
			sig += ")";
		}
		else{
			sig +=  "()";
		}
		return sig;
	}
		
	public String getParameterInitializer(int pos) {
		return "";
	}
	
	public int getAccessControl(){
		return getFunctionInfo().getAccessControl();
	}

	public String[] getExceptions(){
		return new String[] {};
	}

	protected CElementInfo createElementInfo () {
		return new FunctionInfo(this);
	}
	
	protected FunctionInfo getFunctionInfo(){
		return (FunctionInfo) getElementInfo();
	}
	
	public boolean equals(Object other) {
		return ( super.equals(other) 
		&& Util.equalArraysOrNull(fParameterTypes, ((FunctionDeclaration)other).fParameterTypes)
		&& getReturnType().equals(((FunctionDeclaration)other).getReturnType())
		);
	}
	
}
