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
public interface IASTDesignator extends ISourceElementCallbackDelegate
{
	public static class DesignatorKind extends Enum 
	{
		public static final DesignatorKind FIELD = new DesignatorKind( 0 );
		public static final DesignatorKind SUBSCRIPT = new DesignatorKind( 1 );
		protected static final int LAST_KIND = 1;
        /**
         * @param enumValue
         */
        protected DesignatorKind(int enumValue)
        {
            super(enumValue);
            // TODO Auto-generated constructor stub
        }
	}
	
	public DesignatorKind getKind(); 
	public IASTExpression arraySubscriptExpression();
	public String         fieldName();
    public int fieldOffset();
	
}
