package org.eclipse.cdt.core.model;


/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * Represent struct(ure), class or union.
 */
public interface IStructure extends IInheritance, IParent, IVariableDeclaration {
	public IField getField(String name);
	
	/**
	 * 
	 * @return
	 * @throws CModelException
	 */
	public IField[] getFields() throws CModelException;

	public IMethodDeclaration getMethod(String name);
	
	/**
	 * 
	 * @return
	 * @throws CModelException
	 */
	public IMethodDeclaration [] getMethods() throws CModelException;

	public boolean isUnion();

	public boolean isClass();

	public boolean isStruct();

	/**
	 * 
	 * @return
	 * @throws CModelException
	 */
	public boolean isAbstract() throws CModelException;
	
}
