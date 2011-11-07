/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * 		Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.togglefunction;

import junit.framework.TestCase;

import org.junit.Test;

import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;

/**
 * @author egraf
 */
public class ToggleNodeHelperTest extends TestCase {
	/**
	 * Test method for {@link org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper#getFilenameWithoutExtension(java.lang.String)}.
	 */
	@Test
	public void testGetFilenameWithoutExtension1() {
		assertEquals("MyClass", ToggleNodeHelper.getFilenameWithoutExtension("MyClass.h"));
	}
	
	/**
	 * Test method for {@link org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper#getFilenameWithoutExtension(java.lang.String)}.
	 */
	@Test
	public void testGetFilenameWithoutExtension2() {
		assertEquals("My.Class", ToggleNodeHelper.getFilenameWithoutExtension("My.Class.h"));
	}
}
