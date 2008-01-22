/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action.c99;

import org.eclipse.cdt.core.dom.lrparser.action.c99.SymbolTableTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ActionTestSuite extends TestSuite {
	
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		
		suite.addTestSuite(SymbolTableTests.class);
		suite.addTestSuite(ResolverActionTests.class);
		
		return suite;
	}	
}
