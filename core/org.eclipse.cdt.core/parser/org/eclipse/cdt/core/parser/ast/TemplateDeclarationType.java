/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.ast;

import org.eclipse.cdt.core.parser.Enum;

/**
 * @author jcamelon
 *
 */
public class TemplateDeclarationType extends Enum {
	
	public static final TemplateDeclarationType CLASS = new TemplateDeclarationType(1);
	public static final TemplateDeclarationType FUNCTION = new TemplateDeclarationType( 2 );
	public static final TemplateDeclarationType MEMBERCLASS = new TemplateDeclarationType( 3 );
	public static final TemplateDeclarationType METHOD = new TemplateDeclarationType( 4 );
	public static final TemplateDeclarationType FIELD = new TemplateDeclarationType( 5 ); 
	
	private TemplateDeclarationType( int t )
	{
		super( t ); 
	}

}
