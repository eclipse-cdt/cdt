package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * Represents a function.
 */
public interface IFunction extends ICElement, ISourceReference, ISourceManipulation {

	/**
	 * Returns the exceptions this method throws, in the order declared in the source.
	 * or an empty array if this method throws no exceptions.
	 *
	 * <p>For example, a source method declaring <code>"void f(int a) throw (x2, x3);"</code>,
	 * would return the array <code>{"x2", "x3"}</code>.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 *
	 */
	public String[] getExceptions() throws CModelException;

	/**
	 * Returns the number of parameters of this method.
	 */
	public int getNumberOfParameters();

	/**
	 * Returns the initializer of parameters pos for this method.
	 * Returns an empty string if this argument has no initializer.
	 *
	 * <p>For example, a method declared as <code>void foo(String text, int length=9)</code>
	 * would return the array <code>{"9"}</code>.
	 *
	 * @exception CModelException if this argument does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	public String getParameterInitializer(int pos);

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
	public String[] getParameterTypes();

	/**
	 * Returns the type signature of the return value of this method.
	 * For constructors, this returns the signature for void.
	 *
	 * <p>For example, a source method declared as <code>int getName()</code>
	 * would return <code>"int"</code>.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 *
	 * @see Signature
	 */
	public String getReturnType() throws CModelException;

	/**
	 * Returns the access Control of the member.  The value can be
	 * can be examined using class <code>Flags</code>.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 * @see Flags
	 */
	public int getAccessControl() throws CModelException;
}
