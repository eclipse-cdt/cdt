/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:	John Camelon 
 * Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.parser.Name;

/**
 * @author jcamelon
 *
 */
public class Expression {

	private List tokens = new ArrayList(); 
	
	public void add( IToken t )
	{
		tokens.add( t ); 
	}

	public void add( Name t )
	{
		tokens.add( t ); 
	}
	
	public List elements()
	{
		return Collections.unmodifiableList( tokens );	
	}

}
