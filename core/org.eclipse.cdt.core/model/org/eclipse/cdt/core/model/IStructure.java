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

	/**
	 * 
	 * @return
	 * @throws CModelException
	 */
	public boolean isUnion() throws CModelException;

	/**
	 * 
	 * @return
	 * @throws CModelException
	 */
	public boolean isClass() throws CModelException;

	/**
	 * 
	 * @return
	 * @throws CModelException
	 */
	public boolean isStruct() throws CModelException;

	/**
	 * 
	 * @return
	 * @throws CModelException
	 */
	public boolean isAbstract() throws CModelException;
}
