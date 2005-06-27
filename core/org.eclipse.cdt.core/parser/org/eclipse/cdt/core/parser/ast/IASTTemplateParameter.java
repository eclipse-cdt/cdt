/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.ast;

import org.eclipse.cdt.core.parser.Enum;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;

/**
 * @author jcamelon
 *
 */
public interface IASTTemplateParameter  extends IASTTemplateParameterList, IASTTypeSpecifier, ISourceElementCallbackDelegate {

	public class ParamKind extends Enum
	{
		public static final ParamKind CLASS = new ParamKind( 1 );
		public static final ParamKind TYPENAME = new ParamKind( 2 );
		public static final ParamKind TEMPLATE_LIST = new ParamKind( 3 );
		public static final ParamKind PARAMETER = new ParamKind( 4 );

        /**
         * @param enumValue
         */
        protected ParamKind(int enumValue)
        {
            super(enumValue);
        }
	
	}
	
	public ParamKind getTemplateParameterKind(); 
	public String        getIdentifier(); 
	public String		 getDefaultValueIdExpression();
	public IASTParameterDeclaration getParameterDeclaration();
}
