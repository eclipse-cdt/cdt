/*******************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.regex.Pattern;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Test overloaded symbols are correctly resolved when in a common header. This
 * is of interested with the Fast Indexer, as binding resolution occurs purely on
 * AST information (as opposed to adapting a non-PDOM binding to a PDOM binding)
 */
public class OverloadsWithinCommonHeaderTests extends PDOMTestBase {
	protected PDOM pdom;

	public static TestSuite suite() {
		return suite(OverloadsWithinCommonHeaderTests.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		if (pdom == null) {
			ICProject project = createProject("overloadsWithinCommonHeader", true);
			pdom = (PDOM)CCoreInternals.getPDOMManager().getPDOM(project);
		}
		pdom.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}
	
	public void testOverloadedInCommonHeader_ClassScope() throws CoreException {
		Pattern[] ManyOverloadedQuxPath = makePatternArray(new String[] {"ManyOverloaded","qux"});
		IBinding[] ManyOverloadedQux = pdom.findBindings(ManyOverloadedQuxPath, true, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(5,ManyOverloadedQux.length);
		
		// ManyOverloaded.qux()
		assertFunctionRefCount(new Class[0], ManyOverloadedQux, 2);
		
		// ManyOverloaded.qux(int)
		assertFunctionRefCount(new Class[]{IBasicType.class}, ManyOverloadedQux, 4);
		
		// ManyOverloaded.qux(int,char)
		assertFunctionRefCount(new Class[]{IBasicType.class,IBasicType.class}, ManyOverloadedQux, 6);
		
		// ManyOverloaded.qux(ManyOverloaded*)
		assertFunctionRefCount(new Class[]{IPointerType.class}, ManyOverloadedQux, 8);
				
		// ManyOverloaded.qux(ManyOverloaded)
		assertFunctionRefCount(new Class[]{ICPPClassType.class}, ManyOverloadedQux, 10);
	}
	
	public void testOverloadedInCommonHeader_FileScope() throws CoreException {
		Pattern[] QuuxPath = makePatternArray(new String[] {"quux"});		
		IBinding[] Quux = pdom.findBindings(QuuxPath, true, IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), new NullProgressMonitor());
		
		assertEquals(5,Quux.length);
		
		// (file scope) quux()
		assertFunctionRefCount(new Class[0], Quux, 4);
		
		// (file scope) quux(int,char)
		assertFunctionRefCount(new Class[] {IBasicType.class}, Quux, 6);
		
		// (file scope) quux(int,char)
		assertFunctionRefCount(new Class[] {IBasicType.class, IBasicType.class}, Quux, 8);
		
		// (file scope) quux(ManyOverloaded*)
		assertFunctionRefCount(new Class[] {IPointerType.class}, Quux, 10);
		
		// (file scope) quux(ManyOverloaded)
		assertFunctionRefCount(new Class[] {ICPPClassType.class}, Quux, 12);
	}
	
	public void testOverloadedInCommonHeader_NamespaceScope() throws CoreException {
		Pattern[] GraultPath = makePatternArray(new String[] {"corge","grault"});
		IBinding[] Grault = pdom.findBindings(GraultPath, true, IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), new NullProgressMonitor());
		assertEquals(5,Grault.length);
		 
		// corge::grault()
		assertFunctionRefCount(new Class[0], Grault, 6);
		
		// corge::grault(int,char)
		assertFunctionRefCount(new Class[] {IBasicType.class}, Grault, 8);
		
		// corge::grault(int,char)
		assertFunctionRefCount(new Class[] {IBasicType.class, IBasicType.class}, Grault, 10);
		
		// corge::grault(ManyOverloaded*)
		assertFunctionRefCount(new Class[] {IPointerType.class}, Grault, 12);
		
		// corge::grault(ManyOverloaded)
		assertFunctionRefCount(new Class[] {ICPPClassType.class}, Grault, 14);
	}
	
	public void testOverloadedInCommonHeader_NamespaceScope_Collides_With_Filescope() throws CoreException {
		Pattern[] ns2Path = makePatternArray(new String[] {"ns2","quux"});
		IBinding[] ns2 = pdom.findBindings(ns2Path, true, IndexFilter.getFilter(ILinkage.CPP_LINKAGE_ID), new NullProgressMonitor());
		assertEquals(5,ns2.length);
		 
		// ns2::quux()
		assertFunctionRefCount(new Class[0], ns2, 16);
		
		// ns2::quux(int,char)
		assertFunctionRefCount(new Class[] {IBasicType.class}, ns2, 20);
		
		// ns2::quux(int,char)
		assertFunctionRefCount(new Class[] {IBasicType.class, IBasicType.class}, ns2, 24);
		
		// ns2::quux(ManyOverloaded*)
		assertFunctionRefCount(new Class[] {IPointerType.class}, ns2, 28);
		
		// ns2::quux(ManyOverloaded)
		assertFunctionRefCount(new Class[] {ICPPClassType.class}, ns2, 32);
	}
	
	protected void assertFunctionRefCount(Class[] args, IBinding[] bindingPool, int refCount) throws CoreException {
		assertFunctionRefCount(pdom, args, bindingPool, refCount);
	}
}
