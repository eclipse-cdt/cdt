/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;


/**
 * Represents a function
 */
public interface IFunctionDeclaration extends IDeclaration {

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
	String[] getExceptions();

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
	 * Returns the return value of this method. 
	 */
	String getReturnType();
	
	/**
	 * Returns the signature of the method. 
	 */	
	String getSignature() throws CModelException;
}
