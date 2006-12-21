/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.core;

/**
 * Interface declaring public known and usable core test case properties.
 */
public interface IRSECoreTestCaseProperties {
	static final String PROP_BASE_KEY = "org.eclipse.rse.tests.core"; //$NON-NLS-1$

	/**
	 * Boolean property controling if or if not the Remote Systems View will be
	 * expanded before the test case is starting. The original view maximized
	 * state will be restored after the test case finished.
	 * <p>
	 * Default value is <b><code>false</code></b>.
	 */
	public static final String PROP_MAXIMIZE_REMOTE_SYSTEMS_VIEW = PROP_BASE_KEY + ".maximizeRemoteSystemsView"; //$NON-NLS-1$

	/**
	 * String property specifying the perspective id to switch to before the
	 * test case is starting. The original perspective will be restored after
	 * the test case finished.
	 * <p>
	 * Default value is <b><code>org.eclipse.rse.ui.view.SystemPerspective</code></b>.
	 */
	public static final String PROP_SWITCH_TO_PERSPECTIVE = PROP_BASE_KEY + ".switchToPerspective"; //$NON-NLS-1$

	/**
	 * Boolean property controling if the test execution should be forced into a non
	 * display thread (if not already running in a non display thread anyway).
	 * <p>
	 * Default value is <b><code>false</code></b>.
	 */
	public static final String PROP_FORCE_BACKGROUND_EXECUTION = PROP_BASE_KEY + ".forceBackgroundExecution"; //$NON-NLS-1$
	
	/**
	 * Boolean property controling if the printed test start, stop and delay information
	 * includes the time consumed from calling <code>setUp</code> and <code>tearDown</code>.
	 * <p>
	 * Default value is <b><code>false</code></b>.
	 */
	public static final String PROP_PERFORMANCE_TIMING_INCLUDE_SETUP_TEARDOWN = PROP_BASE_KEY + ".timingsIncludeSetupAndTearDown"; //$NON-NLS-1$
}
