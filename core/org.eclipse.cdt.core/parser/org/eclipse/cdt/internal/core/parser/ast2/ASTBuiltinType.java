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

import org.eclipse.cdt.core.parser.ast2.IASTBuiltinType;
import org.eclipse.cdt.core.parser.ast2.IASTIdentifier;

/**
 * @author Doug Schaefer
 */
public class ASTBuiltinType extends ASTType implements IASTBuiltinType {

	private IASTIdentifier name;
	
	public ASTBuiltinType(String name) {
		this.name = new ASTIdentifier(name);
	}

	public ASTBuiltinType(IASTIdentifier name) {
		this.name = name;
	}

	public IASTIdentifier getName() {
		return name;
	}
	
}
