/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.framework;

import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * A delegating holder is the abstract superclass of all suite holders that are 
 * defined in extension points.
 */
public abstract class DelegatingTestSuiteHolder extends AbstractTestSuiteHolder {
	
	private static final String SUITE_EXTENSION_POINT_NAME = "org.eclipse.rse.tests.framework.suites"; //$NON-NLS-1$ 
	
	// elements
	private static final String TYPE_ELEMENT = "type"; //$NON-NLS-1$ 
	private static final String SUITE_ELEMENT = "suite"; //$NON-NLS-1$ 
	private static final String ARGUMENT_ELEMENT = "arg"; //$NON-NLS-1$
	
	// attributes
	private static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$ 
	private static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$ 
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static final String VALUE_ATTRIBUTE = "value"; //$NON-NLS-1$ 
	
	/**
	 * Returns a newly minted suite holder of the given type.
	 * @param wantedTypeName the type name of the suite holder
	 * @return a new suite holder of that type or null if that type is not defined
	 */
	private static DelegatingTestSuiteHolder getSuiteHolder(String wantedTypeName) {
		DelegatingTestSuiteHolder holder = null;
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] configs = registry.getConfigurationElementsFor(SUITE_EXTENSION_POINT_NAME);
		for (int i = 0; i < configs.length; i++) {
			IConfigurationElement config = configs[i];
			String elementName = config.getName();
			if (elementName.equals(TYPE_ELEMENT)) {
				String candidateTypeName = config.getAttribute(NAME_ATTRIBUTE);
				if (candidateTypeName.equals(wantedTypeName)) {
					try {
						holder = (DelegatingTestSuiteHolder) config.createExecutableExtension(CLASS_ATTRIBUTE);
					} catch (CoreException e) {
					}
				}
			}
		}
		return holder;
	}
	
	/**
	 * Retrieve all suite holders defined by extension points.
	 * @return an array of suite holders.
	 * @throws IllegalArgumentException if any suite holder is ill-defined
	 */
	public static DelegatingTestSuiteHolder[] getHolders() {
		List holders = new Vector();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] configs = registry.getConfigurationElementsFor(SUITE_EXTENSION_POINT_NAME);
		for (int i = 0; i < configs.length; i++) {
			IConfigurationElement config = configs[i];
			String elementName = config.getName();
			if (elementName.equals(SUITE_ELEMENT)) {
				String suiteType = config.getAttribute(TYPE_ATTRIBUTE);
				String suiteName = config.getAttribute(NAME_ATTRIBUTE);
				DelegatingTestSuiteHolder holder = getSuiteHolder(suiteType);
				if (holder != null) {
					holder.setConfiguration(config);
					holder.setName(suiteName);
					holders.add(holder);
				}
			}
		}
		DelegatingTestSuiteHolder[] result = new DelegatingTestSuiteHolder[holders.size()];
		holders.toArray(result);
		return result;
	}
	
	private String name;
	private IConfigurationElement config;
	
	/**
	 * Retrieves the argument configuration element for an argument of a given name
	 * @param name the name of the argument
	 * @return the configuration element of that name, or null if not found.
	 */
	protected IConfigurationElement getArgumentElement(String name) {
		IConfigurationElement result = null;
		IConfigurationElement[] argumentElements = config.getChildren(ARGUMENT_ELEMENT);
		for (int i = 0; i < argumentElements.length && result == null; i++) {
			IConfigurationElement argumentElement = argumentElements[i];
			String argumentName = argumentElement.getAttribute(NAME_ATTRIBUTE);
			if (argumentName.equals(name)) {
				result = argumentElement;
			}
		}
		return result;
	}
	
	protected String getStringValue(String name) {
		String result = null;
		IConfigurationElement element = getArgumentElement(name);
		if (element != null) {
			result = element.getAttribute(VALUE_ATTRIBUTE);
		}
		return result;
	}

	protected Object getObjectValue(String name) {
		Object result = null;
		IConfigurationElement element = getArgumentElement(name);
		if (element != null) {
			try {
				result = element.createExecutableExtension("value"); //$NON-NLS-1$
			} catch (CoreException e) {
			}
		}
		return result;
	}
	
	protected Bundle getBundle() {
		IContributor contributor = config.getContributor();
		String bundleName = contributor.getName();
		Bundle bundle = Platform.getBundle(bundleName);
		return bundle;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.AbstractTestSuiteHolder#getName()
	 */
	public String getName() {
		return name;
	}
	
	private void setName(String name) {
		this.name = name;
	}
	
	private void setConfiguration(IConfigurationElement config) {
		this.config = config;
	}
	
}


