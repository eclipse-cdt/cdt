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
	 * Returns a canonical representation that is suitable for distinguishing 
	 * constant values for the purpose of template instantiation.
	 * The representation may not be used to display the value.
	 */
	String getCanonicalRepresentation(); 
}
