package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * Represent struct(ure), class or union.
 */
public interface IStructure extends IInheritance, IParent, IVariableDeclaration {
	//public String instantiatesTemplate();

	public IField getField(String name);
	public IField[] getFields();

	public IMethod getMethod(String name);
	public IMethod [] getMethods();

	public boolean isUnion();

	public boolean isClass();

	public boolean isStruct();

	public boolean isAbstract();
}
