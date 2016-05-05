/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.junit.Test;

@SuppressWarnings("nls")
public class LaunchTargetTest {

	@Test
	public void testRemoveLaunchTarget() throws CoreException {
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

}
