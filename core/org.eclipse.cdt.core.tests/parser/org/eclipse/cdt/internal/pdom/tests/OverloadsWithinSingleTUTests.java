/*******************************************************************************
 * Copyright (c) 2006, 2012 Symbian Software and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test overloaded symbols are correctly resolved when within a single translation
 * unit. This covers the case of adapting non-PDOM bindings to PDOM bindings by
 * searching for the equivalent binding within the PDOM.
 */
public class OverloadsWithinSingleTUTests extends PDOMTestBase {
	protected PDOM pdom;

	@BeforeEach
	protected void beforeEach() throws Exception {
		if (pdom == null) {
			ICProject project = createProject("overloadsWithinSingleTU");
			pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(project);
		}
		pdom.acquireReadLock();
	}

	@AfterEach
	protected void afterEach() throws Exception {
		pdom.releaseReadLock();
	}

	@Test
	public void testDistinctBindingsPresent() throws Exception {
		IBinding[] fooBs = pdom.findBindings(Pattern.compile("foo"), false, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(3, fooBs.length);

		IBinding[] barBs = pdom.findBindings(Pattern.compile("bar"), false, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(8, barBs.length);

		IBinding[] FooBs = pdom.findBindings(Pattern.compile("Foo"), false, IndexFilter.ALL_DECLARED,
				new NullProgressMonitor());
		assertEquals(4, FooBs.length);

		Pattern[] XBarAbsPath = makePatternArray("X", "bar");
		IBinding[] XBarBs = pdom.findBindings(XBarAbsPath, true, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(4, XBarBs.length);

		Pattern[] XFooPath = makePatternArray("X", "Foo");
		IBinding[] XFooPathBs = pdom.findBindings(XFooPath, true, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(1, XFooPathBs.length);
	}

	@Test
	public void testReferencesToGlobalBindings() throws Exception {
		IBinding[] BarBs = pdom.findBindings(Pattern.compile("bar"), true, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(4, BarBs.length);

		// bar()
		assertFunctionRefCount(new Class[] {}, BarBs, 4);

		// bar(int)
		assertFunctionRefCount(new Class[] { IBasicType.class }, BarBs, 3);

		// bar(int,int)
		assertFunctionRefCount(new Class[] { IBasicType.class, IBasicType.class }, BarBs, 2);

		// bar(Foo,int)
		assertFunctionRefCount(new Class[] { ICPPClassType.class, IBasicType.class }, BarBs, 1);
	}

	@Test
	public void testReferencesToNamespacedBindings() throws Exception {
		Pattern[] XBarAbsPath = makePatternArray("X", "bar");
		IBinding[] XBarBs = pdom.findBindings(XBarAbsPath, false, IndexFilter.ALL, new NullProgressMonitor());

		// X::bar()
		assertFunctionRefCount(new Class[] {}, XBarBs, 2);

		// X::bar(int)
		assertFunctionRefCount(new Class[] { IBasicType.class }, XBarBs, 3);

		// X::bar(int,int)
		assertFunctionRefCount(new Class[] { IBasicType.class, IBasicType.class }, XBarBs, 4);

		// X::bar(X::Foo,int)
		assertFunctionRefCount(new Class[] { ICPPClassType.class, IBasicType.class }, XBarBs, 5);
	}

	public void assertFunctionRefCount(Class[] args, IBinding[] bindingPool, int refCount) throws CoreException {
		assertFunctionRefCount(pdom, args, bindingPool, refCount);
	}
}
