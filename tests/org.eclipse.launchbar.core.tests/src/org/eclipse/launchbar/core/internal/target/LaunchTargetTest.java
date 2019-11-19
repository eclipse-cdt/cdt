/*******************************************************************************
 * Copyright (c) 2016, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.core.internal.target;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.service.prefs.Preferences;

public class LaunchTargetTest {
	private org.osgi.service.prefs.Preferences pref;

	@Before
	public void setUp() {
		pref = Mockito.mock(Preferences.class);
	}

	@Test
	public void testRemoveLaunchTarget() {
		ILaunchTargetManager manager = Activator.getLaunchTargetManager();
		// Account for pre-populated targets
		int baseSize = manager.getLaunchTargets().length;
		ILaunchTarget target1 = manager.addLaunchTarget("mytype", "target1");
		ILaunchTarget target2 = manager.addLaunchTarget("mytype", "target2");
		Set<ILaunchTarget> targetSet = new HashSet<>(Arrays.asList(manager.getLaunchTargets()));
		assertEquals(baseSize + 2, targetSet.size());
		assertTrue(targetSet.contains(target1));
		assertTrue(targetSet.contains(target2));
		manager.removeLaunchTarget(target2);
		targetSet = new HashSet<>(Arrays.asList(manager.getLaunchTargets()));
		assertEquals(baseSize + 1, targetSet.size());
		assertTrue(targetSet.contains(target1));
		assertFalse(targetSet.contains(target2));
		manager.removeLaunchTarget(target1);
		targetSet = new HashSet<>(Arrays.asList(manager.getLaunchTargets()));
		assertEquals(baseSize, targetSet.size());
		assertFalse(targetSet.contains(target1));
		assertFalse(targetSet.contains(target2));
	}

	@Test
	public void testEquals() {
		LaunchTarget t1 = new LaunchTarget("a", "b", pref);
		LaunchTarget t2 = new LaunchTarget("a", "b", pref);
		assertEquals(t1, t2);
		LaunchTarget t3 = new LaunchTarget("a", "a", pref);
		assertNotEquals(t1, t3);
		LaunchTarget t4 = new LaunchTarget("b", "a", pref);
		assertNotEquals(t4, t3);
		assertNotEquals(t4, null);
	}

	@Test
	public void testEqualsHashode() {
		LaunchTarget t1 = new LaunchTarget("a", "b", pref);
		LaunchTarget t2 = new LaunchTarget("a", "b", pref);
		assertEquals(t1.hashCode(), t2.hashCode());
	}

	@Test
	public void testBasic() {
		LaunchTarget t1 = new LaunchTarget("a", "b", pref);
		ILaunchTarget save = t1.getWorkingCopy().save();
		assertEquals(t1, save);
	}

	@Test
	public void testNullTarget() {
		ILaunchTarget nt = ILaunchTarget.NULL_TARGET;
		assertEquals("b", nt.getAttribute("a", "b"));
	}

	@Test(expected = NullPointerException.class)
	public void testNPEInConstrPref() {
		new LaunchTarget("a", "b", null);
	}

	@Test(expected = NullPointerException.class)
	public void testNPEInConstrType() {
		new LaunchTarget(null, "b", pref);
	}

	@Test(expected = NullPointerException.class)
	public void testNPEInConstrId() {
		new LaunchTarget("type", null, pref);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testWConNULL() {
		ILaunchTarget nt = ILaunchTarget.NULL_TARGET;
		nt.getWorkingCopy();
	}
}
