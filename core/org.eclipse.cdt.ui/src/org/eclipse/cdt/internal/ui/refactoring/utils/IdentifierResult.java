/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.refactoring.utils;


/**
 * Holds the result of a name validation, used by the IdentifierHelper.
 * 
 * @author Thomas Corbat
 *
 */
public class IdentifierResult {

	public static final int VALID = 0;
	public static final int EMPTY = 1;
	public static final int ILLEGAL_CHARACTER = 2;
	public static final int DIGIT_FIRST = 3;
	public static final int KEYWORD = 4;
	public static final int UNKNOWN = 5;
	
	
	private final int result;
	private final String message;
	
	public boolean isCorrect(){
		return result == VALID;
	}
	
	public int getResult(){
		return result;
	}
	
	public String getMessage(){
		return message;
	}

	public IdentifierResult(int result, String message) {
		this.result = result;
		this.message = message;
	}
}
