package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * Represents a function
 */
public interface IFunctionDeclaration extends ICElement, ISourceReference, ISourceManipulation {

	/**
	 * Returns the type signatures of the exceptions this method throws,
	 * in the order declared in the source. Returns an empty array
	 * if this method throws no exceptions.
	 *
	 * <p>For example, a source method declaring <code>"void f(int a) throw (x1, x2);"</code>,
	 * would return the array <code>{"x1", "x2"}</code>.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 *
	 * @see Signature
	 */
	String[] getExceptions() throws CModelException;

	/**
	 * Returns the number of parameters of this method.
	 */
	int getNumberOfParameters();

	/**
	 * Returns the initializer of parameters position for this method.
	 * Returns an empty string if this argument has no initializer.
	 *
	 * <p>For example, a method declared as <code>public void foo(String text, int length=9)</code>
	 * would return the array <code>{"9"}</code>.
	 *
	 * @exception CModelException if this argument does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	String getParameterInitializer(int pos);

	/**
	 * Returns the type signatures for the parameters of this method.
	 * Returns an empty array if this method has no parameters.
	 * This is a handle-only method.
	 *
	 * <p>For example, a source method declared as <code>void foo(string text, int length)</code>
	 * would return the array <code>{"string","int"}</code>.
	 *
	 * @see Signature
	 */
	String[] getParameterTypes();

	/**
	 * Returns the type signature of the return value of this method.
	 * For constructors, this returns the signature for void.
	 *
	 * <p>For example, a source method declared as <code>public String getName()</code>
	 * would return <code>"String"</code>.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 *
	 * @see Signature
	 */
	String getReturnType() throws CModelException;

	/**
	 * Returns the access Control of the member. The access qualifier
	 * can be examine using the AccessControl class.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 * @see IAccessControl
	 */
	int getAccessControl() throws CModelException;
}
