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
public interface IASTTemplateParameter  extends IASTTemplateParameterList {

	public class ParameterKind extends Enum
	{
		public static final ParameterKind CLASS = new ParameterKind( 1 );
		public static final ParameterKind TYPENAME = new ParameterKind( 2 );
		public static final ParameterKind TEMPLATE_LIST = new ParameterKind( 3 );
		public static final ParameterKind PARAMETER = new ParameterKind( 4 );

        /**
         * @param enumValue
         */
        protected ParameterKind(int enumValue)
        {
            super(enumValue);
        }
	
	}
	
	public ParameterKind getTemplateParameterKind(); 
	public String        getIdentifier(); 
	public String		 getDefaultValueIdExpression();
}
