
/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser2.c;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class CFunctionScope implements IScope {
	private final CFunction function;
	
	public CFunctionScope( CFunction function ){
		this.function = function;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
	 */
	public IScope getParent() {
		return function.getScope();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public List find(String name) {
		return null;
	}

}
