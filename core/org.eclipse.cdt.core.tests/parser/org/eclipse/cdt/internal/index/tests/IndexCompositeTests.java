/*******************************************************************************
 * Copyright (c) 2007, 2013 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import junit.framework.Test;

/**
 * Tests the behavior of the IIndex API when dealing with multiple projects
 */
public class IndexCompositeTests extends BaseTestCase {
	Set<IProject> createdProjects = new HashSet<>();

	/*
	 * Convenience class for setting up projects.
	 */
	private class ProjectBuilder {
		private final String name;
		private final boolean cpp;
		private List<IProject> dependencies = new ArrayList<>();
		private Map<String, String> path2content = new HashMap<>();

		ProjectBuilder(String name, boolean cpp) {
			this.name = name;
			this.cpp = cpp;
		}

		ProjectBuilder addDependency(IProject project) {
			dependencies.add(project);
			return this;
		}

		ProjectBuilder addFile(String relativePath, CharSequence content) {
			path2content.put(relativePath, content.toString());
			return this;
		}

		ICProject create() throws Exception {
			ICProject result = cpp ?
					CProjectHelper.createCCProject(name, "bin", IPDOMManager.ID_NO_INDEXER) :
					CProjectHelper.createCProject(name, "bin", IPDOMManager.ID_NO_INDEXER);
			createdProjects.add(result.getProject());

			IFile lastFile= null;
			for (Map.Entry<String, String> entry : path2content.entrySet()) {
				lastFile= TestSourceReader.createFile(result.getProject(), new Path(entry.getKey()), entry.getValue());
			}

			IProjectDescription desc = result.getProject().getDescription();
			desc.setReferencedProjects(dependencies.toArray(new IProject[dependencies.size()]));
			result.getProject().setDescription(desc, new NullProgressMonitor());

			IIndexManager indexManager = CCorePlugin.getIndexManager();
			indexManager.setIndexerId(result, IPDOMManager.ID_FAST_INDEXER);
			if (lastFile != null) {
				// Call reindex explicitly since setting indexer ID doesn't trigger reindexing.
				indexManager.reindex(result);
				IIndex index= indexManager.getIndex(result);
				TestSourceReader.waitUntilFileIsIndexed(index, lastFile, INDEXER_TIMEOUT_SEC * 1000);
			}
			BaseTestCase.waitForIndexer(result);
			return result;
		}
	}

	public static Test suite() {
		return suite(IndexCompositeTests.class);
	}

	private static final int NONE = 0;
	private static final int REFS = IIndexManager.ADD_DEPENDENCIES;
	private static final int REFD = IIndexManager.ADD_DEPENDENT;
	private static final int BOTH = REFS | REFD;

	private static final IndexFilter FILTER= new IndexFilter() {
		@Override
		public boolean acceptBinding(IBinding binding) throws CoreException {
			if (binding instanceof ICPPMethod) {
				return !((ICPPMethod) binding).isImplicit();
			}
			return true;
		}
	};

	IIndex index;

	protected StringBuilder[] getContentsForTest(int blocks) throws IOException {
		return TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), blocks);
	}

	// class A {};

	// class B {};
	public void testPairDisjointContent() throws Exception {
		CharSequence[] contents = getContentsForTest(2);
		List<ICProject> projects = new ArrayList<ICProject>();

		try {
			ProjectBuilder pb = new ProjectBuilder("projB_" + getName(), true);
			pb.addFile("h1.h", contents[0]);
			ICProject cprojB = pb.create();
			projects.add(cprojB);

			pb = new ProjectBuilder("projA_" + getName(), true);
			pb.addFile("h2.h", contents[1]).addDependency(cprojB.getProject());
			ICProject cprojA = pb.create();
			projects.add(cprojA);

			setIndex(cprojB, NONE);	assertBCount(1, 1);
			setIndex(cprojB, REFS);	assertBCount(1, 1);
			setIndex(cprojB, REFD);	assertBCount(2, 2);
			setIndex(cprojB, BOTH);	assertBCount(2, 2);

			setIndex(cprojA, NONE);	assertBCount(1, 1);
			setIndex(cprojA, REFS);	assertBCount(2, 2);
			setIndex(cprojA, REFD);	assertBCount(1, 1);
			setIndex(cprojA, BOTH);	assertBCount(2, 2);
		} finally {
			for (ICProject project : projects) {
				project.getProject().delete(true, true, new NullProgressMonitor());
			}
		}
	}

	// class C1 {public: int i;};
	// namespace X { class C2 {}; }
	// enum E {E1,E2};
	// void foo(C1 c) {}

	// #include "h3.h"
	// class B1 {};
	// namespace X { class B2 {}; }
	// C1 c1;
	// X::C2 c2;
	// void foo(B1 c) {}
	// void foo(X::C2 c) {}

	// #include "h2.h"
	// class A1 {};
	// void foo(X::B2 c) {}
	// namespace X { class A2 {}; B2 b; C2 c; }
	public void testTripleLinear() throws Exception {
		CharSequence[] contents = getContentsForTest(3);
		List<ICProject> projects = new ArrayList<ICProject>();

		try {
			ProjectBuilder pb = new ProjectBuilder("projC_" + getName(), true);
			pb.addFile("h3.h", contents[0]);
			ICProject cprojC = pb.create();
			projects.add(cprojC);

			pb = new ProjectBuilder("projB_" + getName(), true);
			pb.addFile("h2.h", contents[1]).addDependency(cprojC.getProject());
			ICProject cprojB = pb.create();
			projects.add(cprojB);

			pb = new ProjectBuilder("projA_" + getName(), true);
			pb.addFile("h1.h", contents[2]).addDependency(cprojB.getProject());
			ICProject cprojA = pb.create();
			projects.add(cprojA);

			/* Defines Global, Defines Namespace, References Global, References Namespace
			 * projC: 6, 2, 0, 0
			 * projB: 6, 1, 1, 1 + projC
			 * projA: 3, 3, 0, 2 + projB + projC
			 */

			final int gC= 6, aC= gC + 2;
			final int gB= 6, aB= gB + 1;
			final int gA= 3, aA= gA + 3;

			final int gBC= gB + gC - 1, aBC= aB + aC - 1;
			final int gABC= gA + gBC - 1, aABC= aA + aBC - 1;

			setIndex(cprojC, NONE);
			assertBCount(gC, aC); assertNamespaceXMemberCount(1);
			assertFieldCount("C1", 1);

			setIndex(cprojC, REFS);
			assertBCount(gC, aC);
			assertNamespaceXMemberCount(1);
			assertFieldCount("C1", 1);

			setIndex(cprojC, REFD);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(5);
			assertFieldCount("C1", 1);

			setIndex(cprojC, BOTH);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(5);
			assertFieldCount("C1", 1);

			setIndex(cprojB, NONE);
			assertBCount(gBC, aBC);
			assertNamespaceXMemberCount(2);
			assertFieldCount("C1", 1);

			setIndex(cprojB, REFS);
			assertBCount(gBC, aBC);
			assertNamespaceXMemberCount(2);
			assertFieldCount("C1", 1);

			setIndex(cprojB, REFD);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(5);
			assertFieldCount("C1", 1);

			setIndex(cprojB, BOTH);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(5);
			assertFieldCount("C1", 1);

			setIndex(cprojA, NONE);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(5);
			// binding C1 is not referenced by cprojA

			setIndex(cprojA, REFS);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(5);
			assertFieldCount("C1", 1);

			setIndex(cprojA, REFD);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(5);
			// binding C1 is not referenced by cprojA

			setIndex(cprojA, BOTH);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(5);
			assertFieldCount("C1", 1);
		} finally {
			for (ICProject project : projects) {
				project.getProject().delete(true, true, new NullProgressMonitor());
			}
		}
	}

	// class B1 {};
	// namespace X { class B2 {}; }
	// void foo(B1 c) {}
	// void foo(X::B2 c, B1 c) {}

	// #include "h2.h"
	// class A1 {};
	// void foo(X::B2 c) {}
	// namespace X { class A2 {}; }
	// B1 ab;

	// #include "h2.h"
	// class C1 {};
	// namespace X { class C2 {}; B1 b; }
	// enum E {E1,E2};
	// X::B2 cb;
	// void foo(C1 c) {}
	public void testTripleUpwardV() throws Exception {
		CharSequence[] contents = getContentsForTest(3);
		List<ICProject> projects = new ArrayList<ICProject>();

		try {
			ProjectBuilder pb = new ProjectBuilder("projB_" + getName(), true);
			pb.addFile("h2.h", contents[0]);
			ICProject cprojB = pb.create();
			projects.add(cprojB);

			pb = new ProjectBuilder("projA_" + getName(), true);
			pb.addFile("h1.h", contents[1]).addDependency(cprojB.getProject());
			ICProject cprojA = pb.create();
			projects.add(cprojA);

			pb = new ProjectBuilder("projC_" + getName(), true);
			pb.addFile("h3.h", contents[2]).addDependency(cprojB.getProject());
			ICProject cprojC = pb.create();
			projects.add(cprojC);

			/*  A   C    |
		     *   \ /     | Depends On / References
		     *    B      V
		     *
			 * Defines Global, Defines Namespace, Ext. References Global, Ext. References Namespace
			 * projC: 7, 2, 1, 1
			 * projB: 4, 1, 0, 0
			 * projA: 4, 1, 1, 1
			 */

			final int gC= 7, aC= gC + 2;
			final int gB= 4, aB= gB + 1;
			final int gA= 4, aA= gA + 1;

			final int gBC= gB + gC - 1, aBC= aB + aC - 1;
			final int gAB= gA + gB - 1, aAB= aA + aB - 1;
			final int gABC= gA + gBC - 1, aABC= aA + aBC - 1;

			setIndex(cprojC, NONE);
			assertBCount(gBC, aBC);
			assertNamespaceXMemberCount(3);
			setIndex(cprojC, REFS);
			assertBCount(gBC, aBC);
			assertNamespaceXMemberCount(3);
			setIndex(cprojC, REFD);
			assertBCount(gBC, aBC);
			assertNamespaceXMemberCount(3);
			setIndex(cprojC, BOTH);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(4);

			setIndex(cprojB, NONE);
			assertBCount(gB, aB);
			assertNamespaceXMemberCount(1);
			setIndex(cprojB, REFS);
			assertBCount(gB, aB);
			assertNamespaceXMemberCount(1);
			setIndex(cprojB, REFD);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(4);
			setIndex(cprojB, BOTH);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(4);

			setIndex(cprojA, NONE);
			assertBCount(gAB, aAB);
			assertNamespaceXMemberCount(2);
			setIndex(cprojA, REFS);
			assertBCount(gAB, aAB);
			assertNamespaceXMemberCount(2);
			setIndex(cprojA, REFD);
			assertBCount(gAB, aAB);
			assertNamespaceXMemberCount(2);
			setIndex(cprojA, BOTH);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(4);
		} finally {
			for (ICProject project : projects) {
				project.getProject().delete(true, true, new NullProgressMonitor());
			}
		}
	}

	// class C1 {};
	// namespace X { class C2 {}; }
	// enum E {E1,E2};
	// void foo(C1 c) {}

	// #include "h3.h"
	// #include "h1.h"
	// class B1 {};
	// namespace X { class B2 {}; C1 c; }
	// void foo(A1 c) {}
	// void foo(X::A2 c, B1 c) {}

	// class A1 {};
	// void foo(A1 a, A1 b) {}
	// namespace X { class A2 {}; }
	public void testTripleDownwardV() throws Exception {
		CharSequence[] contents = getContentsForTest(3);
		List<ICProject> projects = new ArrayList<ICProject>();

		try {
			ProjectBuilder pb = new ProjectBuilder("projC_" + getName(), true);
			pb.addFile("h3.h", contents[0]);
			ICProject cprojC = pb.create();
			projects.add(cprojC);

			pb = new ProjectBuilder("projA_" + getName(), true);
			pb.addFile("h1.h", contents[2]);
			ICProject cprojA = pb.create();
			projects.add(cprojA);

			pb = new ProjectBuilder("projB_" + getName(), true);
			pb.addFile("h2.h", contents[1]).addDependency(cprojC.getProject()).addDependency(cprojA.getProject());
			ICProject cprojB = pb.create();
			projects.add(cprojB);

			/*    B     |
		     *   / \    | Depends On / References
		     *  A   C   V
		     *
			 *  Defines Global, Defines Namespace, References Global, References Namespace
			 * projC: 6, 1, 0, 0
			 * projB: 4, 2, 2, 1
			 * projA: 3, 1, 0, 0
			 */

			final int gC= 6, aC= gC + 1;
			final int gB= 4, aB= gB + 2;
			final int gA= 3, aA= gA + 1;

			final int gBC= gB + gC - 1, aBC= aB + aC - 1;
			final int gAB= gA + gB - 1, aAB= aA + aB - 1;
			final int gABC= gA + gBC - 1, aABC= aA + aBC - 1;

			setIndex(cprojC, NONE);
			assertBCount(gC, aC);
			assertNamespaceXMemberCount(1);
			setIndex(cprojC, REFS);
			assertBCount(gC, aC);
			assertNamespaceXMemberCount(1);
			setIndex(cprojC, REFD);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(4);
			setIndex(cprojC, BOTH);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(4);

			setIndex(cprojB, NONE);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(4);
			setIndex(cprojB, REFS);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(4);
			setIndex(cprojB, REFD);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(4);
			setIndex(cprojB, BOTH);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(4);

			setIndex(cprojA, NONE);
			assertBCount(gA, aA);
			assertNamespaceXMemberCount(1);
			setIndex(cprojA, REFS);
			assertBCount(gA, aA);
			assertNamespaceXMemberCount(1);
			setIndex(cprojA, REFD);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(4);
			setIndex(cprojA, BOTH);
			assertBCount(gABC, aABC);
			assertNamespaceXMemberCount(4);
		} finally {
			for (ICProject project : projects) {
				project.getProject().delete(true, true, new NullProgressMonitor());
			}
		}
	}

	/**
	 * Asserts binding counts, and returns the index tested against
	 * @param global the number of bindings expected to be found at global scope
	 * @param all the number of bindings expected to be found at all scopes
	 * @return the index
	 * @throws CoreException
	 */
	private IIndex assertBCount(int global, int all) throws CoreException {
		IBinding[] bindings = index.findBindings(Pattern.compile(".*"), true, FILTER, new NullProgressMonitor());
		assertEquals(global, bindings.length);
		bindings = index.findBindings(Pattern.compile(".*"), false, FILTER, new NullProgressMonitor());
		assertEquals(all, bindings.length);
		return index;
	}

	private void assertNamespaceXMemberCount(int count) throws CoreException, DOMException {
		IBinding[] bindings = index.findBindings(Pattern.compile("X"), true, FILTER, new NullProgressMonitor());
		assertEquals(1, bindings.length);
		assertEquals(count, ((ICPPNamespace) bindings[0]).getMemberBindings().length);
	}

	private void assertFieldCount(String qnPattern, int count) throws CoreException, DOMException {
		IBinding[] bindings = index.findBindings(Pattern.compile(qnPattern), true, FILTER, new NullProgressMonitor());
		assertEquals(1, bindings.length);
		assertEquals(count, ((ICompositeType) bindings[0]).getFields().length);
	}

	private void setIndex(ICProject project, int options) throws CoreException, InterruptedException {
		if (index != null) {
			index.releaseReadLock();
		}
		index = CCorePlugin.getIndexManager().getIndex(project, options);
		index.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		if (index != null) {
			index.releaseReadLock();
			index = null;
		}
		for (IProject project : createdProjects) {
			project.delete(true, npm());
		}
		createdProjects.clear();
		super.tearDown();
	}
}
