/**********************************************************************
 * Copyright (c) 2004 IBM Canada Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner2;

import org.eclipse.cdt.core.parser.IGCCToken;

/**
 * @author jcamelon
 *
 */
public class GCCScannerExtensionsTest extends BaseScanner2Test {

	/**
	 * @param x
	 */
	public GCCScannerExtensionsTest(String x) {
		super(x);
	}
	
    public void testBug39698() throws Exception
	{
    	initializeScanner( "<? >?"); //$NON-NLS-1$
    	validateToken( IGCCToken.tMIN );
    	validateToken( IGCCToken.tMAX );
    	validateEOF();
	}

}
