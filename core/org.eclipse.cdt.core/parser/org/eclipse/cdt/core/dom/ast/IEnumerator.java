/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Interface for enumerators.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEnumerator extends IBinding {
	/**
	 * Returns the type of this enumeration.  The type of an enumerator
	 * is the enumeration in which it is declared.
	 * 
	 * @return the type of the enumeration
	 */
	public IType getType();
	
	/**
	 * Returns the value assigned to this enumerator.
	 * @since 5.1
	 */
	public IValue getValue();
}
