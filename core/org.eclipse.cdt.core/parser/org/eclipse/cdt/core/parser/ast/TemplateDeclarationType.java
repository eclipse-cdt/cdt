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

/**
 * @author jcamelon
 *
 */
public class TemplateDeclarationType {
	
	public static final TemplateDeclarationType t_class = new TemplateDeclarationType(1);
	public static final TemplateDeclarationType t_function = new TemplateDeclarationType( 2 );
	public static final TemplateDeclarationType t_memberClass = new TemplateDeclarationType( 3 );
	public static final TemplateDeclarationType t_method = new TemplateDeclarationType( 4 );
	public static final TemplateDeclarationType t_field = new TemplateDeclarationType( 5 ); 
	
	
	private final int type; 
	private TemplateDeclarationType( int t )
	{
		type = t; 
	}

}
