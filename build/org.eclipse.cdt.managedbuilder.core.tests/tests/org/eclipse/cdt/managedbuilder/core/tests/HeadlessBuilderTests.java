/*******************************************************************************
 * Copyright (c) 2023, Simeon Andreev.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Simeon Andreev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.managedbuilder.internal.core.HeadlessBuilder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import junit.framework.TestCase;

public class HeadlessBuilderTests extends TestCase {

	private static final NullProgressMonitor MONITOR = new NullProgressMonitor();

	private List<IProject> createdProjects;

	public HeadlessBuilderTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createdProjects = new ArrayList<>();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			for (IProject project : createdProjects) {
				project.delete(true, MONITOR);
			}
		} finally {
			super.tearDown();
		}
	}

	/**
	 * Project structure:
	 * <pre>
	 * A -> B -> C
	 * </pre>
	 * Expect sorted sequence:
	 * <pre>
	 * C -> B -> A
	 * </pre>
	 */
	public void testHeadlessBuilderSorting1() throws Exception {
		IProject a = createProject("A");
		IProject b = createProject("B");
		IProject c = createProject("C");
		setReferencedProjects(a, b, c);
		setReferencedProjects(b, c);
		List<IProject> actualSortedProjects = HeadlessBuilder.sortProjects(Arrays.asList(b, c, a));
		List<IProject> expectedSortedProjects = Arrays.asList(c, b, a);
		assertEquals("Wrong results after sorting projects by referenced projects", expectedSortedProjects,
				actualSortedProjects);
	}

	/**
	 * Project structure:
	 * <pre>
	 * A -> B -> C
	 * D
	 * </pre>
	 * Expect sorted sequence:
	 * <pre>
	 * C, B, A, D
	 * </pre>
	 */
	public void testHeadlessBuilderSorting2() throws Exception {
		IProject a = createProject("A");
		IProject b = createProject("B");
		IProject c = createProject("C");
		IProject d = createProject("D");
		setReferencedProjects(a, b);
		setReferencedProjects(b, c);
		List<IProject> actualSortedProjects = HeadlessBuilder.sortProjects(Arrays.asList(d, a, c, b));
		List<IProject> expectedSortedProjects = Arrays.asList(c, b, a, d);
		assertEquals("Wrong results after sorting projects by referenced projects", expectedSortedProjects,
				actualSortedProjects);
	}

	/**
	 * Test cycle handling. Project structure:
	 * <pre>
	 * A -> B -> C -> A
	 * D -> E -> D
	 * </pre>
	 * Expect sorted sequence:
	 * <pre>
	 * A, B, C, D, E
	 * </pre>
	 */
	public void testHeadlessBuilderSorting3() throws Exception {
		IProject a = createProject("A");
		IProject b = createProject("B");
		IProject c = createProject("C");
		IProject d = createProject("D");
		IProject e = createProject("E");
		setReferencedProjects(a, b);
		setReferencedProjects(b, c);
		setReferencedProjects(c, a);
		setReferencedProjects(d, e);
		setReferencedProjects(e, d);
		List<IProject> actualSortedProjects = HeadlessBuilder.sortProjects(Arrays.asList(d, c, b, e, a));
		List<IProject> expectedSortedProjects = Arrays.asList(a, b, c, d, e);
		assertEquals("Wrong results after sorting projects by referenced projects", expectedSortedProjects,
				actualSortedProjects);
	}

	/**
	 * Test cycle handling. Project structure:
	 * <pre>
	 * A -> B -> C -> A
	 * D -> E
	 * F
	 * </pre>
	 * Expect sorted sequence:
	 * <pre>
	 * A, B, C, E, D, F
	 * </pre>
	 */
	public void testHeadlessBuilderSorting4() throws Exception {
		IProject a = createProject("A");
		IProject b = createProject("B");
		IProject c = createProject("C");
		IProject d = createProject("D");
		IProject e = createProject("E");
		IProject f = createProject("F");
		setReferencedProjects(a, b);
		setReferencedProjects(b, c);
		setReferencedProjects(c, a);
		setReferencedProjects(d, e);
		List<IProject> actualSortedProjects = HeadlessBuilder.sortProjects(Arrays.asList(a, b, c, d, e, f));
		List<IProject> expectedSortedProjects = Arrays.asList(a, b, c, e, d, f);
		assertEquals("Wrong results after sorting projects by referenced projects", expectedSortedProjects,
				actualSortedProjects);
	}

	/**
	 * Test cycle handling. Project structure:
	 * <pre>
	 * A -> B -> C, D
	 * C -> F
	 * D -> A
	 * D -> E
	 * </pre>
	 * Expect sorted sequence:
	 * <pre>
	 * E, A, B, C, D, F
	 * </pre>
	 */
	public void testHeadlessBuilderSorting5() throws Exception {
		IProject a = createProject("A");
		IProject b = createProject("B");
		IProject c = createProject("C");
		IProject d = createProject("D");
		IProject e = createProject("E");
		IProject f = createProject("F");
		setReferencedProjects(a, b);
		setReferencedProjects(b, c, d);
		setReferencedProjects(c, f);
		setReferencedProjects(d, a);
		setReferencedProjects(d, e);
		List<IProject> actualSortedProjects = HeadlessBuilder.sortProjects(Arrays.asList(f, b, c, a, e, d));
		List<IProject> expectedSortedProjects = Arrays.asList(f, c, e, d, b, a);
		assertEquals("Wrong results after sorting projects by referenced projects", expectedSortedProjects,
				actualSortedProjects);
	}

	/**
	 * Project structure:
	 * <pre>
	 * A -> B -> C, D
	 * E
	 * </pre>
	 * Expect sorted sequence:
	 * <pre>
	 *  C, D, A, B, E
	 * </pre>
	 */
	public void testHeadlessBuilderSorting6() throws Exception {
		IProject a = createProject("A");
		IProject b = createProject("B");
		IProject c = createProject("C");
		IProject d = createProject("D");
		IProject e = createProject("E");
		setReferencedProjects(a, b);
		setReferencedProjects(b, c, d);
		List<IProject> actualSortedProjects = HeadlessBuilder.sortProjects(Arrays.asList(d, a, e, c, b));
		List<IProject> expectedSortedProjects = Arrays.asList(c, d, b, a, e);
		assertEquals("Wrong results after sorting projects by referenced projects", expectedSortedProjects,
				actualSortedProjects);
	}

	private IProject createProject(String name) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(name);
		project.create(MONITOR);
		project.open(MONITOR);
		createdProjects.add(project);
		return project;
	}

	private static void setReferencedProjects(IProject t1, IProject... referencedProjects) throws CoreException {
		IProjectDescription d1 = t1.getDescription();
		d1.setReferencedProjects(referencedProjects);
		t1.setDescription(d1, MONITOR);
	}

} // end class
