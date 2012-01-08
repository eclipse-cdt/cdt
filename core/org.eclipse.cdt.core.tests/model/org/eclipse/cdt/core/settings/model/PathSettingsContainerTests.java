/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.settings.model;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 */
public class PathSettingsContainerTests extends BaseTestCase {

	public static TestSuite suite() {
		return suite(PathSettingsContainerTests.class, "_");
	}
	
	@Override
	protected void setUp() throws Exception {
	}
	
	@Override
	protected void tearDown() throws Exception {
	}

	public void testPathSettingsContainerCreate() {
		final PathSettingsContainer root= PathSettingsContainer.createRootContainer();
		assertNull(root.getValue());
		assertNull(root.getParentContainer());
		assertTrue(root.isRoot());
		assertTrue(root.isValid());
		assertEquals(0, root.getChildren(false).length);
		assertEquals(1, root.getChildren(true).length);
		
		final IPath level1= new Path("level1");
		final PathSettingsContainer child1= root.getChildContainer(level1, true, true);
		assertNotNull(child1);
		assertNull(child1.getValue());
		assertSame(root, child1.getParentContainer());
		assertFalse(child1.isRoot());
		assertTrue(child1.isValid());
		assertEquals(1, root.getChildren(false).length);
		assertEquals(0, child1.getChildren(false).length);
		assertEquals(1, child1.getChildren(true).length);
		final String value1= "child1";
		child1.setValue(value1);
		assertSame(value1, child1.getValue());
		
		final IPath level2= level1.append("level2");
		final PathSettingsContainer child2= root.getChildContainer(level2, true, true);
		assertNotNull(child2);
		assertNull(child2.getValue());
		assertSame(child1, child2.getParentContainer());
		assertFalse(child2.isRoot());
		assertTrue(child2.isValid());
		assertEquals(1, child1.getChildren(false).length);
		assertEquals(0, child2.getChildren(false).length);
		assertEquals(1, child2.getChildren(true).length);
		final String value2= "child2";
		child2.setValue(value2);
		assertSame(value2, child2.getValue());

		final IPath level3= level2.append("level3");
		final PathSettingsContainer child3= root.getChildContainer(level3, true, true);
		assertNotNull(child3);
		assertNull(child3.getValue());
		assertSame(child2, child3.getParentContainer());
		assertFalse(child3.isRoot());
		assertTrue(child3.isValid());
		assertEquals(1, child2.getChildren(false).length);
		assertEquals(0, child3.getChildren(false).length);
		assertEquals(1, child3.getChildren(true).length);
		final String value3= "child3";
		child3.setValue(value3);
		assertSame(value3, child3.getValue());
		
		assertSame(child1, root.getChildContainer(level1, true, true));
		assertSame(child2, root.getChildContainer(level2, true, true));
		assertSame(child3, root.getChildContainer(level3, true, true));
	}

	public void testPathSettingsContainerRemove() {
		final PathSettingsContainer root= PathSettingsContainer.createRootContainer();
		final IPath level1= new Path("level1");
		final PathSettingsContainer child1= root.getChildContainer(level1, true, true);
		final IPath level2= level1.append("level2");
		final PathSettingsContainer child2= root.getChildContainer(level2, true, true);
		final IPath level3= level2.append("level3");
		final PathSettingsContainer child3= root.getChildContainer(level3, true, true);
		final IPath level31= level2.append("level31");
		final PathSettingsContainer child31= root.getChildContainer(level31, true, true);
		
		child3.remove();
		assertEquals(1, child2.getChildren(false).length);
		assertFalse(child3.isValid());

		child2.remove();
		assertFalse(child2.isValid());

		child31.remove();
		assertEquals(0, child2.getChildren(false).length);
		assertFalse(child31.isValid());

	}

	public void testPathSettingsContainer_Bug208765() {
		final PathSettingsContainer root= PathSettingsContainer.createRootContainer();
		try {
			root.removeChildContainer(new Path(""));
		} catch (NullPointerException npe) {
			fail(npe.getMessage());
		}
	}

}
