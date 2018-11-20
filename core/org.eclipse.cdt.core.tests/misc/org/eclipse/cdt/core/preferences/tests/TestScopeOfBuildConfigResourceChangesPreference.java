/*******************************************************************************
 *  Copyright (c) 2017 Simeon Andreev and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.preferences.tests;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test case for Bug 529023: Cannot set build.proj.ref.configs.enabled via
 * plugin_customization.ini.
 *
 * @author Simeon Andreev
 */
public class TestScopeOfBuildConfigResourceChangesPreference extends TestCase {

	private static final String PREFERENCE_NAME = CCorePreferenceConstants.PREF_BUILD_CONFIGS_RESOURCE_CHANGES;

	private boolean oldInstanceScopeValue;
	private boolean oldDefaultScopeValue;

	public static Test suite() {
		return new TestSuite(TestScopeOfBuildConfigResourceChangesPreference.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		oldInstanceScopeValue = ACBuilder.buildConfigResourceChanges();
		IEclipsePreferences defaultScopePreferences = defaultScopePreferences();
		oldDefaultScopeValue = defaultScopePreferences.getBoolean(PREFERENCE_NAME, oldInstanceScopeValue);
	}

	@Override
	protected void tearDown() throws Exception {
		ACBuilder.setBuildConfigResourceChanges(oldInstanceScopeValue);
		IEclipsePreferences defaultScopePreferences = defaultScopePreferences();
		defaultScopePreferences.putBoolean(PREFERENCE_NAME, oldDefaultScopeValue);

		super.tearDown();
	}

	private static IEclipsePreferences defaultScopePreferences() {
		return DefaultScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
	}

	/**
	 * Validates that {@link ACBuilder#buildConfigResourceChanges()} also takes
	 * {@link DefaultScope} into account.
	 */
	public void testSettingPreferenceViaDefaultScope() throws Exception {
		IEclipsePreferences defaultScopePreferences = defaultScopePreferences();
		defaultScopePreferences.putBoolean(PREFERENCE_NAME, true);

		boolean buildConfigResourceChanges = ACBuilder.buildConfigResourceChanges();
		assertTrue("unable to set preference \"" + PREFERENCE_NAME + "\" via default scope",
				buildConfigResourceChanges);
	}
}
