/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine;



/**
 * This Exception is thrown when, we execute getNextChild, addToChildList and
 * getChildCount on an InputUIElement.
 * 
 * @since 4.0
 */

public class SimpleElementException extends Exception {

	private static final long serialVersionUID = 0000000000L;

	/**
	 * The description of the exception.
	 */
	String expDefinition;

	private static final String EXCEPTION_STRING = Messages.getString("SimpleElementException.0"); //$NON-NLS-1$

	/**
	 * Constructor receives description of this instance of event as parameter.
	 * The same is assigned to iExpDefinition.
	 */
	public SimpleElementException() {
		super(EXCEPTION_STRING);
		expDefinition = EXCEPTION_STRING;
	}

	/**
	 * Constructor receives description of this instance of event as parameter.
	 * The same is assigned to iExpDefinition.
	 * 
	 * @param def
	 */
	public SimpleElementException(String def) {
		super(def);
		expDefinition = def;
	}

	/**
	 * The description of the SimpleElementException is returned.
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		return expDefinition;
	}

}
