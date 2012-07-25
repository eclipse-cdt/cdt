/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;

/**
 * Models a value of a variable, enumerator or expression.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.1
 */
public interface IValue {
	/**
	 * Returns the value as a number, or {@code null} if it is not possible.
	 */
	Long numericalValue();

	/**
	 * Returns the evaluation object if this value is dependent, or {@code null} otherwise.
	 * If {@link #numericalValue()} returns {@code null}, {@link #getEvaluation()} returns
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
	 */
	@Deprecated
	char[] getInternalExpression(); 

	/**
	 * @deprecated Returns an empty array.
	 */
	@Deprecated
	IBinding[] getUnknownBindings();
}
