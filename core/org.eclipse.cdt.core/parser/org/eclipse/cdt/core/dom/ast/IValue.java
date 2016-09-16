/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;

/**
 * Models a value of a variable, enumerator or expression.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.1
 */
public interface IValue {
	/**
	 * Returns the value as a Long number, or {@code null} if it is not possible.
	 * @deprecated Use numberValue() instead. 
	 */
	@Deprecated
	Long numericalValue();

	/**
	 * Returns the value as a number, or {@code null} if it is not possible.
	 * @since 6.0
	 */
	Number numberValue();
	
	/**
	 * If this value consists of sub-values, returns the number of these sub-values. Otherwise returns 1.
	 * @since 6.0
	 */
	int numberOfSubValues();
	
	/**
	 * If this value consists of sub-values, returns the sub-value at the given index.
	 * Otherwise, returns this value (represented as an ICPPEvaluation) if the index 0 is passed.
	 * EvalFixed.INCOMPLETE is returned if the given index is out of bounds.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	ICPPEvaluation getSubValue(int index);
	
	/**
	 * If this value consists of sub-values, returns an array containing all of them.
	 * Otherwise, returns an array containing 1 element representing this value.
	 * Not all implementations implement this; some may return {@code null}. 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	ICPPEvaluation[] getAllSubValues();
	
	/**
	 * Returns the evaluation object if this value cannot be represented as a single numerical value, or
	 * {@code null} otherwise. This cam happen if the value is dependent, or it's a composite value.
	 * If {@link #numberValue()} returns {@code null}, {@link #getEvaluation()} returns
	 * not {@code null} and vice versa.
	 * @noreference This method is not intended to be referenced by clients. 
	 */
	ICPPEvaluation getEvaluation();

	/**
	 * Returns a signature uniquely identifying the value.  Two values with identical
	 * signatures are guaranteed to be equal.
	 */
	char[] getSignature();

	/**
	 * @deprecated Returns an empty character array.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	char[] getInternalExpression(); 

	/**
	 * @deprecated Returns an empty array.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	IBinding[] getUnknownBindings();

	/**
	 * If this value consists of sub-values, set the sub-value at the given position to the given new value.
	 * Otherwise, set this value to the given new value.
	 * Not all implementations implement this; for some, a call to this may have no effect. 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	void setSubValue(int position, ICPPEvaluation newValue);
	
	/**
	 * Make a deep copy of this value.
	 * @since 6.0
	 */
	IValue clone();
	
	/**
	 * Serialize this value to the given type marhsal buffer.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	void marshal(ITypeMarshalBuffer buffer) throws CoreException;
}
