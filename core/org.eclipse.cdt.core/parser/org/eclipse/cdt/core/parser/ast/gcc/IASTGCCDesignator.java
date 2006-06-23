/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.parser.ast.gcc;

import org.eclipse.cdt.core.parser.ast.IASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 *
 */
public interface IASTGCCDesignator extends IASTDesignator {
	
	public static class DesignatorKind extends IASTDesignator.DesignatorKind
	{
		public static final DesignatorKind SUBSCRIPT_RANGE = new DesignatorKind(LAST_KIND + 1 );
		
		/**
		 * @param enumValue
		 */
		protected DesignatorKind(int enumValue) {
			super(enumValue);
		}
	}
	
	public static final String SECOND_EXRESSION = "SUBSCRIPT2 EXPRESSION"; //$NON-NLS-1$
	public IASTExpression arraySubscriptExpression2();
}
