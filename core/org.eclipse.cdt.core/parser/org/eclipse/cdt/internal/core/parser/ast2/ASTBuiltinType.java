/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast2;

/**
 * @author Doug Schaefer
 */
public class ASTBuiltinType extends ASTType {

	private char[] name;
	
	public char[] getName() {
		return name;
	}
	
	public void setName(char[] name) {
		this.name = name;
	}
	
}
