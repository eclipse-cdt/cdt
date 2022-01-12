/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.launcher;

import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProvider;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProviderInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Provides access to the information about the Tests Runner provider plug-in
 * (id, name, description, supported features set). Also provides the access to
 * <code>ITestsRunnerProvider</code> interface (the main interface of Tests Runner
 * provider plug-in).
 */
public class TestsRunnerProviderInfo implements ITestsRunnerProviderInfo {

	private static final String TESTS_RUNNER_FEATURES_ELEMENT = "features"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_FEATURE_MULTIPLE_TEST_FILTER_ATTRIBUTE = "multipleTestFilter"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_FEATURE_TESTING_TIME_MEASUREMENT_ATTRIBUTE = "testingTimeMeasurement"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_FEATURE_DATA_STREAM_ATTRIBUTE = "dataStream"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_DESCRIPTION_ATTRIBUTE = "description"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_ERROR_STREAM_VALUE = "error"; //$NON-NLS-1$

	/** Configuration element of the Tests Runner provider plug-ins extension point. */
	private IConfigurationElement element;

	/**
	 * The constructor.
	 *
	 * @param element configuration element of the Tests Runner provider plug-ins extension point
	 */
	public TestsRunnerProviderInfo(IConfigurationElement element) {
		this.element = element;
	}

	@Override
	public String getName() {
		return element.getAttribute(TESTS_RUNNER_NAME_ATTRIBUTE);
	}

	@Override
	public String getId() {
		return element.getAttribute(TESTS_RUNNER_ID_ATTRIBUTE);
	}

	@Override
	public String getDescription() {
		String result = element.getAttribute(TESTS_RUNNER_DESCRIPTION_ATTRIBUTE);
		return result == null ? "" : result; //$NON-NLS-1$
	}

	/**
	 * Instantiates Tests Runner provider plug-in and return its main interface.
	 *
	 * @note Instantiated Tests Runner provider plug-in objects are not cached
	 * and are instantiated as much times as
	 * <code>instantiateTestsRunnerProvider()</code> is called.
	 *
	 * @return Tests Runner provider plug-in main interface
	 */
	public ITestsRunnerProvider instantiateTestsRunnerProvider() {
		try {
			Object object = element.createExecutableExtension(TESTS_RUNNER_CLASS_ATTRIBUTE);
			if (object instanceof ITestsRunnerProvider) {
				return (ITestsRunnerProvider) object;
			}
		} catch (CoreException e) {
			TestsRunnerPlugin.log(e);
		}
		return null;
	}

	/**
	 * Provides an access to the 'features' node of Tests Runner provider
	 * plug-in configuration element.
	 *
	 * @return 'features' configuration element
	 */
	private IConfigurationElement getFeatures() {
		IConfigurationElement[] featuresElements = element.getChildren(TESTS_RUNNER_FEATURES_ELEMENT);
		if (featuresElements.length == 1) {
			return featuresElements[0];
		}
		return null;
	}

	/**
	 * Provides an access to the value of the feature with specified name.
	 *
	 * @param featureName
	 * @return feature value or null if there is no features described or there
	 * is no feature with such name
	 */
	private String getFeatureAttributeValue(String featureName) {
		IConfigurationElement features = getFeatures();
		if (features != null) {
			return features.getAttribute(featureName);
		}
		return null;
	}

	/**
	 * Provides an access to the boolean value of the feature with the specified
	 * name. If the feature with such name cannot be accessed or it contains
	 * invalid boolean value, the default value will be returned.
	 *
	 * @param featureName
	 * @param defaultValue
	 * @return feature value or null if there is no features described or there
	 * is no feature with such name
	 */
	private boolean getBooleanFeatureValue(String featureName, boolean defaultValue) {
		String attrValue = getFeatureAttributeValue(featureName);
		if (attrValue != null) {
			return Boolean.parseBoolean(attrValue);
		}
		return defaultValue;
	}

	@Override
	public boolean isAllowedMultipleTestFilter() {
		return getBooleanFeatureValue(TESTS_RUNNER_FEATURE_MULTIPLE_TEST_FILTER_ATTRIBUTE, false);
	}

	/**
	 * Returns whether test execution time measurement should be performed by
	 * the Tests Runner Plug-in.
	 *
	 * @note It should be used only if testing framework does not provide
	 * execution time measurement, cause measurement by Tests Runner Plug-in is
	 * not very precise (it also takes into account output processing & internal
	 * model building time).
	 *
	 * @return whether testing time measurement should be done by Tests Runner
	 * Plug-in
	 */
	public boolean isAllowedTestingTimeMeasurement() {
		return getBooleanFeatureValue(TESTS_RUNNER_FEATURE_TESTING_TIME_MEASUREMENT_ATTRIBUTE, false);
	}

	@Override
	public boolean isOutputStreamRequired() {
		return !isErrorStreamRequired();
	}

	@Override
	public boolean isErrorStreamRequired() {
		String attrValue = getFeatureAttributeValue(TESTS_RUNNER_FEATURE_DATA_STREAM_ATTRIBUTE);
		if (attrValue != null) {
			return attrValue.equals(TESTS_RUNNER_ERROR_STREAM_VALUE);
		}
		return false;
	}
}
