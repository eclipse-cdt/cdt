/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on May 27, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.internal.core.search.indexing;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ParserMode;

/**
 * @author bgheorgh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IndexProblemHandler {

	
	public static boolean ruleOnProblem( IProblem p, ParserMode mode )
	{
			if( p == null ) return true; 
			if( p.checkCategory( IProblem.SCANNER_RELATED ) || p.checkCategory( IProblem.PREPROCESSOR_RELATED ))
			{
				switch( p.getID() )
				{
					case IProblem.PREPROCESSOR_POUND_ERROR:
					case IProblem.PREPROCESSOR_UNBALANCE_CONDITION:
					case IProblem.PREPROCESSOR_INVALID_MACRO_DEFN:
					case IProblem.PREPROCESSOR_MACRO_PASTING_ERROR:
					case IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR:
					case IProblem.SCANNER_UNEXPECTED_EOF:
						if( mode == ParserMode.COMPLETE_PARSE )
							return false;
						
					case IProblem.PREPROCESSOR_INVALID_MACRO_REDEFN:
					case IProblem.PREPROCESSOR_INVALID_DIRECTIVE: 
						return true;
					
					default:
						return true;
				}
			}
			return true; 
	}

	
}
