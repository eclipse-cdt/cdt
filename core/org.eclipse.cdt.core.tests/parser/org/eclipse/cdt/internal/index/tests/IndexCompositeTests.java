/*******************************************************************************
 * Copyright (c) 2006 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Tests the behaviour of the IIndex API when dealing with multiple projects
 */
public class IndexCompositeTests  extends BaseTestCase {

	public static Test suite() {
		return suite(IndexCompositeTests.class);
	}

	private static final int NONE = 0, REFS = IIndexManager.ADD_DEPENDENCIES;
	private static final int REFD = IIndexManager.ADD_DEPENDENT, BOTH = REFS | REFD;

	protected StringBuffer[] getContentsForTest(int blocks) throws IOException {
		return TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), blocks);
	}

	// class A {};

	// class B {};
	public void testPairDisjointContent() throws Exception {
		StringBuffer[] contents = getContentsForTest(2);
		List projects = new ArrayList();

		try {
			ProjectBuilder pb = new ProjectBuilder("projB"+System.currentTimeMillis(), true);
			pb.addFile("h1.h", contents[0]);
			ICProject cprojB = pb.create();
			projects.add(cprojB);

			pb = new ProjectBuilder("projA"+System.currentTimeMillis(), true);
			pb.addFile("h2.h", contents[1]).addDependency(cprojB.getProject());
			ICProject cprojA = pb.create();
			projects.add(cprojA);

			assertBCount(cprojB, NONE, 1, 1);
			assertBCount(cprojB, REFS, 1, 1);
			assertBCount(cprojB, REFD, 2, 2);
			assertBCount(cprojB, BOTH, 2, 2);

			assertBCount(cprojA, NONE, 1, 1);
			assertBCount(cprojA, REFS, 2, 2);
			assertBCount(cprojA, REFD, 1, 1);
			assertBCount(cprojA, BOTH, 2, 2);
		} finally {
			for(Iterator i = projects.iterator(); i.hasNext(); )
				((ICProject)i.next()).getProject().delete(true, true, new NullProgressMonitor());
		}
	}

	// class C1 {};
	// namespace X { class C2 {}; }
	// enum E {E1,E2};
	// void foo(C1 c) {}

	// class B1 {};
	// namespace X { class B2 {}; }
	// C1 c1;
	// X::C2 c2;
	// void foo(B1 c) {}
	// void foo(X::C2 c) {}

	// class A1 {};
	// void foo(X::B2 c) {}
	// namespace X { class A2 {}; B2 b; C2 c; }
	public void _testTripleLinear() throws Exception {
		StringBuffer[] contents = getContentsForTest(3);
		List projects = new ArrayList();

		try {
			ProjectBuilder pb = new ProjectBuilder("projC"+System.currentTimeMillis(), true);
			pb.addFile("h3.h", contents[0]);
			ICProject cprojC = pb.create();
			projects.add(cprojC);

			pb = new ProjectBuilder("projB"+System.currentTimeMillis(), true);
			pb.addFile("h2.h", contents[1]).addDependency(cprojC.getProject());
			ICProject cprojB = pb.create();
			projects.add(cprojB);

			pb = new ProjectBuilder("projB"+System.currentTimeMillis(), true);
			pb.addFile("h1.h", contents[2]).addDependency(cprojB.getProject());
			ICProject cprojA = pb.create();
			projects.add(cprojA);

			/* Defines Global, Defines Namespace, References Global, References Namespace
			 * projC: 6, 1, 0, 0
			 * projB: 6, 1, 1, 1
			 * projA: 3, 3, 0, 2
			 */

			assertBCount(cprojC, NONE, 6, 7);
			assertBCount(cprojC, REFS, 6, 7);
			assertBCount(cprojC, REFD, (6+(6-1)+(3-1)), (6+1)+(6+1-1)+(3+3-1));
			assertBCount(cprojC, BOTH, (6+(6-1)+(3-1)), (6+1)+(6+1-1)+(3+3-1));

			assertBCount(cprojB, NONE, 6+1, 6+1+1+1);
			assertBCount(cprojB, REFS, 6+1+6-1-1, (6+1+1+1)-1-1 + (6+1) -1);
			assertBCount(cprojB, REFD, 6+1+3-1, (6+1+1+1) + (3+3) -1);
			assertBCount(cprojB, BOTH, (6+1)-1+3+6 -2,  (6+1+1+1)-1-1 + (3+3+2)-2 + (6+1) -2);

			assertBCount(cprojA, NONE, 3, 8);
			assertBCount(cprojA, REFS, (6+1)-1+3+6 -2, (6+1+1+1)-1-1 + (3+3+2)-2 + (6+1) -2);
			assertBCount(cprojA, REFD, 3, 8);
			assertBCount(cprojA, BOTH, (6+1)-1+3+6 -2, (6+1+1+1)-1-1 + (3+3+2)-2 + (6+1) -2);
		} finally {
			for(Iterator i = projects.iterator(); i.hasNext(); )
				((ICProject)i.next()).getProject().delete(true, true, new NullProgressMonitor());
		}
	}

	// class C1 {};
	// namespace X { class C2 {}; B1 b; }
	// enum E {E1,E2};
	// X::B2 cb;
	// void foo(C1 c) {}

	// class B1 {};
	// namespace X { class B2 {}; }
	// void foo(B1 c) {}
	// void foo(X::B2 c, B1 c) {}

	// class A1 {};
	// void foo(X::B2 c) {}
	// namespace X { class A2 {}; }
	// B1 ab;
	public void _testTripleUpwardV() throws Exception {
		StringBuffer[] contents = getContentsForTest(3);
		List projects = new ArrayList();

		try {
			ProjectBuilder pb = new ProjectBuilder("projB"+System.currentTimeMillis(), true);
			pb.addFile("h2.h", contents[1]);
			ICProject cprojB = pb.create();
			projects.add(cprojB);

			pb = new ProjectBuilder("projC"+System.currentTimeMillis(), true);
			pb.addFile("h3.h", contents[0]).addDependency(cprojB.getProject());
			ICProject cprojC = pb.create();
			projects.add(cprojC);

			pb = new ProjectBuilder("projB"+System.currentTimeMillis(), true);
			pb.addFile("h1.h", contents[2]).addDependency(cprojB.getProject());
			ICProject cprojA = pb.create();
			projects.add(cprojA);

			/* Defines Global, Defines Namespace, Ext. References Global, Ext. References Namespace
			 * projC: 7, 2, 1, 1
			 * projB: 4, 1, 0, 0
			 * projA: 4, 1, 1, 1
			 */

			assertBCount(cprojC, NONE, 7+1, 7+2+1+1);
			assertBCount(cprojC, REFS, 7+1+4-1-1, 7+1+1+2+4+1-1-2);
			assertBCount(cprojC, REFD, 7+1, 7+1+1+2);
			assertBCount(cprojC, BOTH, 7+4+4-2, 7+4+4-2 +2+1+1);

			assertBCount(cprojB, NONE, 4, 4+1);
			assertBCount(cprojB, REFS, 4, 4+1);
			assertBCount(cprojB, REFD, 7+4+4-2, 7+4+4-2 +2+1+1);
			assertBCount(cprojB, BOTH, 7+4+4-2, 7+4+4-2 +2+1+1);

			assertBCount(cprojA, NONE, 4+1, 4+1+1+1);
			assertBCount(cprojA, REFS, 4+1+4-1-1, 4+1+4-1-1 +1+1);
			assertBCount(cprojA, REFD, 4+1, 4+1+1+1);
			assertBCount(cprojA, BOTH, 7+4+4-2, 7+4+4-2 +2+1+1);
		} finally {
			for(Iterator i = projects.iterator(); i.hasNext(); )
				((ICProject)i.next()).getProject().delete(true, true, new NullProgressMonitor());
		}
	}

	// class C1 {};
	// namespace X { class C2 {}; }
	// enum E {E1,E2};
	// void foo(C1 c) {}

	// class B1 {};
	// namespace X { class B2 {}; C1 c; }
	// void foo(A1 c) {}
	// void foo(X::A2 c, B1 c) {}

	// class A1 {};
	// void foo(A1 a, A1 b) {}
	// namespace X { class A2 {}; }
	public void _testTripleDownwardV() throws Exception {
		StringBuffer[] contents = getContentsForTest(3);
		List projects = new ArrayList();

		try {
			ProjectBuilder pb = new ProjectBuilder("projC"+System.currentTimeMillis(), true);
			pb.addFile("h3.h", contents[0]);
			ICProject cprojC = pb.create();
			projects.add(cprojC);

			pb = new ProjectBuilder("projB"+System.currentTimeMillis(), true);
			pb.addFile("h1.h", contents[2]);
			ICProject cprojA = pb.create();
			projects.add(cprojA);

			pb = new ProjectBuilder("projB"+System.currentTimeMillis(), true);
			pb.addFile("h2.h", contents[1]).addDependency(cprojC.getProject()).addDependency(cprojA.getProject());
			ICProject cprojB = pb.create();
			projects.add(cprojB);

			/* Defines Global, Defines Namespace, References Global, References Namespace
			 * projC: 6, 1, 0, 0
			 * projB: 4, 2, 2, 1
			 * projA: 3, 1, 0, 0
			 */

			assertBCount(cprojC, NONE, 6, 6+1);
			assertBCount(cprojC, REFS, 6, 6+1);
			assertBCount(cprojC, REFD, 6+4+1-1, 6+4+1-1 +1+1+1+1 );
			assertBCount(cprojC, BOTH, 6+4+3-2, 6+4+3-2 +1+2+1);

			assertBCount(cprojB, NONE, 4+2, 4+2 +2+1);
			assertBCount(cprojB, REFS, 6+4+3-2, 6+4+3-2 +1+2+1);
			assertBCount(cprojB, REFD, 4+2, 4+2 +2+1);
			assertBCount(cprojB, BOTH, 6+4+3-2, 6+4+3-2 +1+2+1);

			assertBCount(cprojA, NONE, 3, 3 +1);
			assertBCount(cprojA, REFS, 3, 3 +1);
			assertBCount(cprojA, REFD, 4+2+3-1-1, 4+2+3-1-1 +2+1 );
			assertBCount(cprojA, BOTH, 6+4+3-2, 6+4+3-2 +1+2+1);
		} finally {
			for(Iterator i = projects.iterator(); i.hasNext(); )
				((ICProject)i.next()).getProject().delete(true, true, new NullProgressMonitor());
		}
	}

	/**
	 * Asserts binding counts, and returns the index tested against
	 * @param cprojA the project to obtain the index for
	 * @param options the options to obtain the index for
	 * @param global the number of bindings expected to be found at global scope
	 * @param all the number of bindings expected to be found at all scopes
	 * @return
	 * @throws CoreException
	 */
	private IIndex assertBCount(ICProject cprojA, int options, int global, int all) throws CoreException {
		IIndex index = CCorePlugin.getIndexManager().getIndex(cprojA, options);
		IBinding[] bindings = index.findBindings(Pattern.compile(".*"), true, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(global, bindings.length);
		bindings = index.findBindings(Pattern.compile(".*"), false, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(all, bindings.length);
		return index;
	}
}

/*
 * Convenience class for setting up projects.
 */
class ProjectBuilder {
	private String name;
	private List dependencies = new ArrayList();
	private Map path2content = new HashMap();
	private boolean cpp;

	ProjectBuilder(String name, boolean cpp) {
		this.name = name;
		this.cpp = cpp;
	}

	ProjectBuilder addDependency(IProject project) {
		dependencies.add(project);
		return this;
	}

	ProjectBuilder addFile(String relativePath, StringBuffer content) {
		path2content.put(relativePath, content.toString());
		return this;
	}

	ICProject create() throws CoreException {
		ICProject result = cpp ? CProjectHelper.createCCProject(name, "bin", IPDOMManager.ID_FAST_INDEXER)
				: CProjectHelper.createCCProject(name, "bin", IPDOMManager.ID_FAST_INDEXER);

		for(Iterator i = path2content.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry entry = (Map.Entry) i.next();
			TestSourceReader.createFile(result.getProject(), new Path((String)entry.getKey()), (String) entry.getValue());
		}

		IProjectDescription desc = result.getProject().getDescription();
		desc.setReferencedProjects( (IProject[]) dependencies.toArray(new IProject[dependencies.size()]) );
		result.getProject().setDescription(desc, new NullProgressMonitor());

		CCoreInternals.getPDOMManager().reindex(result);
		CCorePlugin.getIndexManager().joinIndexer(4000, new NullProgressMonitor());
		return result;
	}
}
