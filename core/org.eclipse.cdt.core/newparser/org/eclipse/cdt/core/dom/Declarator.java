/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core.dom;

import java.util.LinkedList;
import java.util.List;

public class Declarator extends DOMNode {

	private List name = new LinkedList();
	
	/**
	 * Returns the name.
	 * @return List
	 */
	public List getName() {
		return name;
	}

	public void setParent(SimpleDeclaration declaration) {
		super.setParent(declaration);
		declaration.getDeclarators().add(this);
	}
		
}
