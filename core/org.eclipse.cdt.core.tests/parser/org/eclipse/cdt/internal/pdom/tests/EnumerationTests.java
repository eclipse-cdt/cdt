/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Doug Schaefer
 */
public class EnumerationTests extends PDOMTestBase {

	protected PDOM pdom;

	public static Test suite() {
		return suite(EnumerationTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		if (pdom == null) {
			ICProject project = createProject("enumerationTests");
			pdom = (PDOM)CCoreInternals.getPDOMManager().getPDOM(project);
		}
		pdom.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}
	
	public void testC() throws Exception {
		// Check bindings
		Pattern pattern = Pattern.compile("TestCEnum");
		IBinding[] bindings = pdom.findBindings(pattern, false, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(1, bindings.length);
		IEnumeration enumeration = (IEnumeration)bindings[0];
		assertEquals("TestCEnum", enumeration.getName());
		IEnumerator[] enumerators = enumeration.getEnumerators();
		assertEquals(3, enumerators.length);
		// Enumerators are returned in arbitrary order. Sort them to make checking easier.
		Arrays.sort(enumerators, new Comparator<IEnumerator>() {
			@Override
			public int compare(IEnumerator o1, IEnumerator o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		assertEquals("ca", enumerators[0].getName());
		assertEquals("cb", enumerators[1].getName());
		assertEquals("cc", enumerators[2].getName());
		
		// Declaration of TestEnum 
		IName[] enumDecls = pdom.findNames(enumeration, IIndex.FIND_DECLARATIONS_DEFINITIONS);
		assertEquals(1, enumDecls.length);
		IASTFileLocation loc = enumDecls[0].getFileLocation();
		assertEquals(5, loc.getNodeOffset());
		
		// Reference to TestEnum
		IName[] enumRefs = pdom.findNames(enumeration, IIndex.FIND_REFERENCES);
		assertEquals(1, enumRefs.length);
		loc = enumRefs[0].getFileLocation();
		assertEquals(offset("enumTest.c", "TestCEnum test"), loc.getNodeOffset());
		
		// Reference to a
		IName[] aRefs = pdom.findNames(enumerators[0], IIndex.FIND_REFERENCES);
		assertEquals(1, aRefs.length);
		loc = aRefs[0].getFileLocation();
		assertEquals(offset("enumTest.c", "ca;"), loc.getNodeOffset());
	}

	public void testCPP() throws Exception {
		// Check bindings
		Pattern pattern = Pattern.compile("TestCPPEnum");
		IBinding[] bindings = pdom.findBindings(pattern, false, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(1, bindings.length);
		IEnumeration enumeration = (IEnumeration)bindings[0];
		assertEquals("TestCPPEnum", enumeration.getName());
		IEnumerator[] enumerators = enumeration.getEnumerators();
		assertEquals(3, enumerators.length);
		// Enumerators are returned in arbitrary order. Sort them to make checking easier.
		Arrays.sort(enumerators, new Comparator<IEnumerator>() {
			@Override
			public int compare(IEnumerator o1, IEnumerator o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		assertEquals("cppa", enumerators[0].getName());
		assertEquals("cppb", enumerators[1].getName());
		assertEquals("cppc", enumerators[2].getName());
		
		// Declaration of TestEnum 
		IName[] enumDecls = pdom.findNames(enumeration, IIndex.FIND_DECLARATIONS_DEFINITIONS);
		assertEquals(1, enumDecls.length);
		IASTFileLocation loc = enumDecls[0].getFileLocation();
		assertEquals(5, loc.getNodeOffset());
		
		// Reference to TestEnum
		IName[] enumRefs = pdom.findNames(enumeration, IIndex.FIND_REFERENCES);
		assertEquals(1, enumRefs.length);
		loc = enumRefs[0].getFileLocation();
		assertEquals(offset("enumTest.cpp", "TestCPPEnum test"), loc.getNodeOffset());
		
		// Reference to a
		IName[] aRefs = pdom.findNames(enumerators[0], IIndex.FIND_REFERENCES);
		assertEquals(1, aRefs.length);
		loc = aRefs[0].getFileLocation();
		assertEquals(offset("enumTest.cpp", "cppa;"), loc.getNodeOffset());
	}
}
