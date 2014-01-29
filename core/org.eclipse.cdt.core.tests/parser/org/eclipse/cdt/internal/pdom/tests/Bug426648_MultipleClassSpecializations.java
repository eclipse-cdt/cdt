/*
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.pdom.tests;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.index.tests.IndexBindingResolutionTestBase;
import org.eclipse.core.runtime.CoreException;

public class Bug426648_MultipleClassSpecializations extends IndexBindingResolutionTestBase {

	// In the original source code the multiple specializations were in different
	// translation units.  Each instance was doubling the number of base classes
	// in the corresponding PDOMNode.  This reproduces that case.
	//
	// This checks both the single file and multiple file cases because different
	// code is used during indexing.  In the multiple file case, the extra bases
	// are added to a PDOMNode, in the single file case they are added to an AST
	// node.

	private IIndex index;

	public Bug426648_MultipleClassSpecializations() {
		setStrategy(new SinglePDOMTestNamedFilesStrategy(true));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		index = CCorePlugin.getIndexManager().getIndex(strategy.getCProject());
		if (index != null)
			index.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		if (index != null)
			index.releaseReadLock();
		index = null;

		super.tearDown();
	}

	// impl.cpp
	// template <int I>   class A {};
	//                    class B {};
	// template <class T> class C : public B {};
	//
	// #include "header.h"
	// template <int I> class C<A<I> > : public B {};
	//
	// #include "header.h"
	// template <int I> class C<A<I> > : public B {};
	public void testOneFile() throws Exception {
		assertCorrect(index);
	}

	// header.h
	// template <int I>   class A {};
	//                    class B {};
	// template <class T> class C : public B {};

	// impl_1.cpp
	// #include "header.h"
	// template <int I> class C<A<I> > : public B {};

	// impl_2.cpp
	// #include "header.h"
	// template <int I> class C<A<I> > : public B {};
	public void testMultipleFiles() throws Exception {
		assertCorrect(index);
	}

	private static void assertCorrect(IIndex index) throws Exception {
		assertNotNull(index);

		IIndexBinding[] bindings = index.findBindings("C".toCharArray(), false, Filter, npm());
		assertNotNull(bindings);
		assertEquals(1, bindings.length);

		ICPPBase[] bases = ((ICPPClassTemplatePartialSpecialization) bindings[0]).getBases();
		assertNotNull(bases);
		assertEquals(1, bases.length);
	}

	private static final IndexFilter Filter = new IndexFilter() {
		@Override
		public boolean acceptLinkage(ILinkage linkage) {
			return linkage != null
				&& linkage.getLinkageID() == ILinkage.CPP_LINKAGE_ID;
		}

		@Override
		public boolean acceptBinding(IBinding binding) throws CoreException {
			return binding instanceof ICPPClassTemplatePartialSpecialization;
		}
	};
}
