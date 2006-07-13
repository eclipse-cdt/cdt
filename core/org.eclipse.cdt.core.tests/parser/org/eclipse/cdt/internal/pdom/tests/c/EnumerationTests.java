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
package org.eclipse.cdt.internal.pdom.tests.c;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.pdom.tests.PDOMTestBase;

/**
 * @author Doug Schaefer
 *
 */
public class EnumerationTests extends PDOMTestBase {

	public void test1() throws Exception {
		ICProject project = createProject("enumerationTests");
		
		// Check bindings
		PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(project);
		Pattern pattern = Pattern.compile("TestEnum");
		IBinding[] bindings = pdom.findBindings(pattern);
		assertEquals(1, bindings.length);
		IEnumeration enumeration = (IEnumeration)bindings[0];
		assertEquals("TestEnum", enumeration.getName());
		IEnumerator[] enumerators = enumeration.getEnumerators();
		assertEquals(3, enumerators.length);
		assertEquals("a", enumerators[0].getName());
		assertEquals("b", enumerators[1].getName());
		assertEquals("c", enumerators[2].getName());
		
		// Declaration of TestEnum 
		IASTName[] enumDecls = pdom.getDeclarations(enumeration);
		assertEquals(1, enumDecls.length);
		IASTFileLocation loc = enumDecls[0].getFileLocation();
		assertEquals(5, loc.getNodeOffset());
		
		// Reference to TestEnum
		IASTName[] enumRefs = pdom.getReferences(enumeration);
		assertEquals(1, enumRefs.length);
		loc = enumRefs[0].getFileLocation();
		assertEquals(offset(44, 38), loc.getNodeOffset());
		
		// Reference to a
		IASTName[] aRefs = pdom.getReferences(enumerators[0]);
		assertEquals(1, aRefs.length);
		loc = aRefs[0].getFileLocation();
		assertEquals(offset(71, 64), loc.getNodeOffset());
	}

}
