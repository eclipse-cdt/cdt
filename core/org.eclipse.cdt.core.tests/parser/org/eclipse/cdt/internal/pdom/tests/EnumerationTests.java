/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM;

/**
 * @author Doug Schaefer
 *
 */
public class EnumerationTests extends PDOMTestBase {

	protected ICProject project;
	
	protected void setUp() throws Exception {
		project = createProject("enumerationTests");
	}
	
	public void testC() throws Exception {
		// Check bindings
		PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(project);
		Pattern pattern = Pattern.compile("TestCEnum");
		IBinding[] bindings = pdom.findBindings(pattern);
		assertEquals(1, bindings.length);
		IEnumeration enumeration = (IEnumeration)bindings[0];
		assertEquals("TestCEnum", enumeration.getName());
		IEnumerator[] enumerators = enumeration.getEnumerators();
		assertEquals(3, enumerators.length);
		assertEquals("ca", enumerators[0].getName());
		assertEquals("cb", enumerators[1].getName());
		assertEquals("cc", enumerators[2].getName());
		
		// Declaration of TestEnum 
		IASTName[] enumDecls = pdom.getDeclarations(enumeration);
		assertEquals(1, enumDecls.length);
		IASTFileLocation loc = enumDecls[0].getFileLocation();
		assertEquals(5, loc.getNodeOffset());
		
		// Reference to TestEnum
		IASTName[] enumRefs = pdom.getReferences(enumeration);
		assertEquals(1, enumRefs.length);
		loc = enumRefs[0].getFileLocation();
		assertEquals(offset(46, 40), loc.getNodeOffset());
		
		// Reference to a
		IASTName[] aRefs = pdom.getReferences(enumerators[0]);
		assertEquals(1, aRefs.length);
		loc = aRefs[0].getFileLocation();
		assertEquals(offset(74, 67), loc.getNodeOffset());
	}

	public void testCPP() throws Exception {
		// Check bindings
		PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(project);
		Pattern pattern = Pattern.compile("TestCPPEnum");
		IBinding[] bindings = pdom.findBindings(pattern);
		assertEquals(1, bindings.length);
		IEnumeration enumeration = (IEnumeration)bindings[0];
		assertEquals("TestCPPEnum", enumeration.getName());
		IEnumerator[] enumerators = enumeration.getEnumerators();
		assertEquals(3, enumerators.length);
		assertEquals("cppa", enumerators[0].getName());
		assertEquals("cppb", enumerators[1].getName());
		assertEquals("cppc", enumerators[2].getName());
		
		// Declaration of TestEnum 
		IASTName[] enumDecls = pdom.getDeclarations(enumeration);
		assertEquals(1, enumDecls.length);
		IASTFileLocation loc = enumDecls[0].getFileLocation();
		assertEquals(5, loc.getNodeOffset());
		
		// Reference to TestEnum
		IASTName[] enumRefs = pdom.getReferences(enumeration);
		assertEquals(1, enumRefs.length);
		loc = enumRefs[0].getFileLocation();
		assertEquals(offset(49, 43), loc.getNodeOffset());
		
		// Reference to a
		IASTName[] aRefs = pdom.getReferences(enumerators[0]);
		assertEquals(1, aRefs.length);
		loc = aRefs[0].getFileLocation();
		assertEquals(offset(79, 72), loc.getNodeOffset());
	}
	
}
