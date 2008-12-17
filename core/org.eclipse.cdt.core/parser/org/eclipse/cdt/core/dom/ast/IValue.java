/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast;

/**
 * Models a value of a variable, enumerator or expression.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.1
 */
public interface IValue {
	/**
	 * Returns the value as a number, or <code>null</code> if this is not possible.
	 */
	Long numericalValue();
	
	/**
	 * Returns an internal representation of the expression that builds up the
	 * value. It is suitable for instantiating dependent values but may not be
	 * used for the purpose of displaying values.
	 */
	char[] getInternalExpression(); 
	
	/**
	 * A value may be dependent on template parameters, in which case a list
	 * of unknown bindings is maintained for later instantiation.
	 */
	IBinding[] getUnknownBindings();
	
	/**
	 * Returns a signature containing both the internal representation and the 
	 * unknown bindings. The representation is sufficient to distinguish values
	 * for the purpose of instantiation, it may not be used to display the value.
	 */
	char[] getSignature();
}
