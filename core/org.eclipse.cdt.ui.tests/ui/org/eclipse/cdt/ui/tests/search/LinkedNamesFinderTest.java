/*******************************************************************************
 * Copyright (c) 2009 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import junit.framework.AssertionFailedError;
import junit.framework.TestSuite;

import org.eclipse.jface.text.IRegion;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.parser.tests.ast2.AST2BaseTest;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

import org.eclipse.cdt.internal.core.parser.ParserException;

import org.eclipse.cdt.internal.ui.search.LinkedNamesFinder;

/**
 * Tests for LinkedNamesFinder class.
 */
public class LinkedNamesFinderTest extends AST2BaseTest {
	private static class RegionComparator implements Comparator<IRegion> {
		public int compare(IRegion r1, IRegion r2) {
			return r1.getOffset() - r2.getOffset();
		}
	}

	static final RegionComparator REGION_COMPARATOR = new RegionComparator();

	public LinkedNamesFinderTest() {
	}

	public LinkedNamesFinderTest(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(LinkedNamesFinderTest.class);
	}

	@Override
	protected StringBuffer[] getContents(int sections) throws IOException {
		CTestPlugin plugin = CTestPlugin.getDefault();
		if (plugin == null)
			throw new AssertionFailedError("This test must be run as a JUnit plugin test");
		return TestSourceReader.getContentsForTest(plugin.getBundle(), "ui", getClass(), getName(), sections);
	}

	private IRegion[] getLinkedRegions(String code, String name, int len, boolean isCpp) throws ParserException {
		BindingAssertionHelper ba= new BindingAssertionHelper(code, isCpp);
		IASTName astName = ba.findName(name, len);
		IRegion[] regions = LinkedNamesFinder.findByName(ba.getTranslationUnit(), astName);
		Arrays.sort(regions, REGION_COMPARATOR);
		name = name.substring(0, len);
		if (name.charAt(0) == '~') {
			name = name.substring(1);
		}
		for (IRegion region : regions) {
			assertEquals(name, code.substring(region.getOffset(), region.getOffset() + region.getLength()));
		}
		return regions;
	}

	private void assertContents(String code, int offset, String expected) {
		assertEquals(expected, code.substring(offset, offset + expected.length()));
	}

	//	class A {
	//	public:
	//	  void m(int x);
	//	  void m(int x, int y);
	//	};
	//  
	//	void A::m(int x) {}
	//	void A::m(int x, int y) {}
	public void testMethodParameter() throws Exception {
		String code = getAboveComment();
		IRegion[] regions = getLinkedRegions(code, "x);", 1, true);
		assertEquals(2, regions.length);
		assertContents(code, regions[0].getOffset(), "x)");
		assertContents(code, regions[1].getOffset(), "x) {}");
	}

	//	class A {
	//	public:
	//    A();
	//    A(int x);
	//    ~A();
	//	};
	//  
	//	A::A() {}
	//	A::A(int x) {}
	//	A::~A() {}
	public void testClass() throws Exception {
		String code = getAboveComment();
		IRegion[] regions = getLinkedRegions(code, "A {", 1, true);
		assertEquals(10, regions.length);
		assertContents(code, regions[0].getOffset(), "A {");
		assertContents(code, regions[1].getOffset(), "A();");
		assertContents(code, regions[2].getOffset(), "A(int x);");
		assertContents(code, regions[3].getOffset() - 1, "~A();");
		assertContents(code, regions[4].getOffset(), "A::A() {}");
		assertContents(code, regions[5].getOffset(), "A() {}");
		assertContents(code, regions[6].getOffset(), "A::A(int x) {}");
		assertContents(code, regions[7].getOffset(), "A(int x) {}");
		assertContents(code, regions[8].getOffset(), "A::~A() {}");
		assertContents(code, regions[9].getOffset() - 1, "~A() {}");
		IRegion[] regions2 = getLinkedRegions(code, "A(int x) {}", 1, true);
		assertTrue(Arrays.equals(regions2, regions));
		IRegion[] regions3 = getLinkedRegions(code, "~A();", 2, true);
		assertTrue(Arrays.equals(regions3, regions));
	}

	//	class A {
	//	public:
	//    virtual void m(int a);
	//    virtual void m();
	//	};
	//  
	//	class B : public A {
	//	public:
	//    void m();
	//	};
    //
	//	class C : public B {
	//	public:
	//    void m(int c);
	//	};
	public void testVirtualMethod() throws Exception {
		String code = getAboveComment();
		IRegion[] regions = getLinkedRegions(code, "m(int c)", 1, true);
		assertEquals(2, regions.length);
		assertContents(code, regions[0].getOffset(), "m(int a)");
		assertContents(code, regions[1].getOffset(), "m(int c)");
		regions = getLinkedRegions(code, "m(int a)", 1, true);
		assertEquals(2, regions.length);
		assertContents(code, regions[0].getOffset(), "m(int a)");
		assertContents(code, regions[1].getOffset(), "m(int c)");
	}

	//	#ifndef GUARD //1
	//	#define GUARD //2
	//	// This is a GUARD test
	//	#endif // GUARD
	public void testIncludeGuards() throws Exception {
		String code = getAboveComment();
		IRegion[] regions = getLinkedRegions(code, "GUARD //1", 5, true);
		assertEquals(3, regions.length);
		assertContents(code, regions[0].getOffset(), "GUARD //1");
		assertContents(code, regions[1].getOffset(), "GUARD //2");
		assertContents(code, regions[2].getOffset() - 3, "// GUARD");
	}
}
