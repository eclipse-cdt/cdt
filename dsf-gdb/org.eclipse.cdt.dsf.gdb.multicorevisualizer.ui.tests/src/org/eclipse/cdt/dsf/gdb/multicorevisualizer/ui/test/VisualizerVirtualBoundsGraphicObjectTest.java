
/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation
 *     Marc Khouzam (Ericsson) - Move to o.e.cdt.dsf.gdb.tests (bug 455237)
 *     Marc Dumais (Ericsson) - Move VisualizerVirtualBoundsGraphicObjectTest
 *                              to o.e.cdt.dsf.gdb.multicoreVisualizer.ui.tests
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.ui.test;

import org.eclipse.cdt.visualizer.ui.canvas.VirtualBoundsGraphicObject;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Test;

public class VisualizerVirtualBoundsGraphicObjectTest {

	// testcases

	/**
	 * Test that VirtualBoundsGraphicObject scale correctly.
	 * To do that create a hierarchy of graphical object using virtual coordinates
	 * and sizes (bounds). Then set the real, pixel-bounds for the outer-most object.
	 * Verify that child objects end-up with expected pixel bounds.
	 *
	 * Also test retrieval of child objects.
	 */
	@Test
	public void testVirtualBoundsGraphicObjectRelativeSizingAndRetrieval() throws Exception {

		// we create the graphical objects with no margin, so as to make the sizes
		// easier to predict / understand,  for this test
		VirtualBoundsGraphicObject containerA = new VirtualBoundsGraphicObject(true, 0);
		VirtualBoundsGraphicObject containerB = new VirtualBoundsGraphicObject(true, 0);
		VirtualBoundsGraphicObject containerC = new VirtualBoundsGraphicObject(true, 0);
		VirtualBoundsGraphicObject containerD = new VirtualBoundsGraphicObject(true, 0);
		VirtualBoundsGraphicObject containerE = new VirtualBoundsGraphicObject(true, 0);
		VirtualBoundsGraphicObject containerF = new VirtualBoundsGraphicObject(true, 0);

		// Add B within A
		containerA.addChildObject("B", containerB);

		// A - The outer-most container - is a virtual square of size 10x10
		containerA.setVirtualBounds(new Rectangle(0, 0, 10, 10));
		// Relative to A, B starts at (1,1) and is of size 8x8
		containerB.setVirtualBounds(new Rectangle(1, 1, 8, 8));

		// Add C, D and E within B
		containerB.addChildObject("C", containerC);
		containerB.addChildObject("D", containerD);
		containerB.addChildObject("E", containerE);

		// Relative to B, C starts at (3,2) and is of size 2x2
		containerC.setVirtualBounds(new Rectangle(3, 2, 2, 2));
		// Relative to B, D starts at (3,5) and is of size 2x2
		containerD.setVirtualBounds(new Rectangle(3, 5, 2, 2));
		// Relative to B, E starts at (6,1) and is of size 1x6
		containerE.setVirtualBounds(new Rectangle(6, 1, 1, 6));

		// Add F within C
		containerC.addChildObject("F", containerF);

		// Relative to C, F starts at (1,1) and is of size 1x1
		containerF.setVirtualBounds(new Rectangle(1, 1, 1, 1));

		// Define the real pixel bounds of the outer-most container -
		// the pixel coordinates of all children objects will be
		// computed recursively
		containerA.setBounds(100, 50, 100, 100);

		// check computed bounds for containerA
		org.junit.Assert.assertEquals(100, containerA.getBounds().x);
		org.junit.Assert.assertEquals(50, containerA.getBounds().y);
		org.junit.Assert.assertEquals(100, containerA.getBounds().width);
		org.junit.Assert.assertEquals(100, containerA.getBounds().height);

		// check computed bounds for containerB
		org.junit.Assert.assertEquals(110, containerB.getBounds().x);
		org.junit.Assert.assertEquals(60, containerB.getBounds().y);
		org.junit.Assert.assertEquals(80, containerB.getBounds().width);
		org.junit.Assert.assertEquals(80, containerB.getBounds().height);

		// check computed bounds for containerC
		org.junit.Assert.assertEquals(140, containerC.getBounds().x);
		org.junit.Assert.assertEquals(80, containerC.getBounds().y);
		org.junit.Assert.assertEquals(20, containerC.getBounds().width);
		org.junit.Assert.assertEquals(20, containerC.getBounds().height);

		// check computed bounds for containerD
		org.junit.Assert.assertEquals(140, containerD.getBounds().x);
		org.junit.Assert.assertEquals(110, containerD.getBounds().y);
		org.junit.Assert.assertEquals(20, containerD.getBounds().width);
		org.junit.Assert.assertEquals(20, containerD.getBounds().height);

		// check computed bounds for containerE
		org.junit.Assert.assertEquals(170, containerE.getBounds().x);
		org.junit.Assert.assertEquals(70, containerE.getBounds().y);
		org.junit.Assert.assertEquals(10, containerE.getBounds().width);
		org.junit.Assert.assertEquals(60, containerE.getBounds().height);

		// check computed bounds for containerF
		org.junit.Assert.assertEquals(150, containerF.getBounds().x);
		org.junit.Assert.assertEquals(90, containerF.getBounds().y);
		org.junit.Assert.assertEquals(10, containerF.getBounds().width);
		org.junit.Assert.assertEquals(10, containerF.getBounds().height);

		// check recursive object retrieval returns expected number of child objects, for A
		org.junit.Assert.assertEquals(5, containerA.getAllObjects(true).size());

		// check recursive object retrieval returns expected number of child objects, for B
		org.junit.Assert.assertEquals(4, containerB.getAllObjects(true).size());

		// check non-recursive object retrieval returns expected number of (1st level) child objects, for A
		org.junit.Assert.assertEquals(1, containerA.getAllObjects(false).size());

		// check object retrieval returns expected number of "selectable" child objects, for C
		org.junit.Assert.assertEquals(1, containerC.getSelectableObjects().size());

		// check specific children-object retrieval
		VirtualBoundsGraphicObject obj = containerA.getObject("D");
		org.junit.Assert.assertEquals(containerD, obj);

		// check another specific object retrieval
		obj = containerC.getObject("F");
		org.junit.Assert.assertEquals(containerF, obj);

		// Check the retrieval of a non-existing object
		obj = containerC.getObject("blablabla");
		org.junit.Assert.assertEquals(null, obj);
	}

}