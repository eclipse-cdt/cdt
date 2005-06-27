/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog.IMacroDefinition;

/**
 * @author jcamelon
 *
 */
public abstract class DynamicStyleMacro implements IMacro{

	public abstract char [] execute();
	
	public DynamicStyleMacro( char [] n )
	{
		name = n;
	}
	public final char [] name;
    public IMacroDefinition attachment; 

	public char[] getSignature()
	{
	    return name;
	}
	public char[] getName()
	{
	    return name;
	}
}
