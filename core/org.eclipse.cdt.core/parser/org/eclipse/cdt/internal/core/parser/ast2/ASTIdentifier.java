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

import org.eclipse.cdt.core.parser.ast2.IASTIdentifier;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * @author Doug Schaefer
 */
public class ASTIdentifier extends ASTNode implements IASTIdentifier {

	private char[] name;
	
	public ASTIdentifier(char[] name) {
		this.name = name;
	}
	
	public ASTIdentifier(String name) {
		this.name = name.toCharArray();
	}

	public String toString() {
		return new String(name);
	}
	
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (!(obj instanceof ASTIdentifier))
			return false;
		ASTIdentifier other = (ASTIdentifier)obj;
		
		if (other.name == name)
			return true;
		
		return CharArrayUtils.equals(other.name, name);
	}
	
	public int hashCode() {
		return CharArrayUtils.hash(name);
	}

}
