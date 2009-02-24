/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class EndOfFileException extends Exception {
	private static final long serialVersionUID= 1607883323361197919L;
	
	private final boolean fEndsInactiveCode;

	public EndOfFileException() {
		fEndsInactiveCode= false;
	}
	
	/**
	 * @since 5.1
	 */
	public EndOfFileException(boolean endsInactiveCode) {
		fEndsInactiveCode= true;
	}
	
	/**
	 * @since 5.1
	 */
	public boolean endsInactiveCode() {
		return fEndsInactiveCode;
	}
}
