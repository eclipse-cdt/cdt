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
public interface IASTCompletionNode {

	public static class CompletionKind extends Enum
	{
		public static final CompletionKind TYPE = new CompletionKind( 0 );
		public static final CompletionKind DOT_MEMBER = new CompletionKind( 1 );
		public static final CompletionKind ARROW_MEMBER = new CompletionKind( 2 );
		public static final CompletionKind QUALIFIEDNAME_MEMBER = new CompletionKind( 3 );
		//TODO MORE TO COME
		/**
		 * @param enumValue
		 */
		protected CompletionKind(int enumValue) {
			super(enumValue);
		}
		
	}


	public CompletionKind		getCompletionKind(); 
	public IASTNode               getCompletionContext(); 
	public String                   	getCompletionPrefix(); 
}
