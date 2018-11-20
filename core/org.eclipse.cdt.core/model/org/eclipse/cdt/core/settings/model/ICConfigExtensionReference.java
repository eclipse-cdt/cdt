/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Kirk Beitz (Nokia) - [323094] documentation warnings
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.runtime.CoreException;

/**
 * Holds executable extension information in the
 * project configuration.  Like ICExtensionReference
 * but has knowledge of its ICConfigurationDescription
 *
 * deprecated-@see ICExtensionReference
 */
public interface ICConfigExtensionReference {
	/**
	 * Return the extension point of this reference.
	 *
	 * @return String
	 */
	public String getExtensionPoint();

	/**
	 * Return the extension ID of this reference.
	 *
	 * @return String
	 */
	public String getID();

	/**
	 * Sets a name/value data pair on this reference in the .cdtproject file
	 */
	public void setExtensionData(String key, String value) throws CoreException;

	/**
	 * Gets a value of the key from the .cdtproject file set by
	 * setExtensionData()
	 */
	public String getExtensionData(String key);

	/**
	 * Returns the project descriptor which this extension reference belongs to.
	 * @return the {@link ICConfigurationDescription}
	 */
	public ICConfigurationDescription getConfiguration();

	/**
	 * Returns all configuration elements that are children of the
	 * cextension element. Returns an empty array if this configuration
	 * element has no children.
	 * <p>
	 * Each child corresponds to a nested XML element in the configuration
	 * markup. For example, the configuration markup
	 *
	 * <pre>
	 *  &lt;view&gt;
	 *  &amp;nbsp&amp;nbsp&amp;nbsp&amp;nbsp&lt;verticalHint&gt;top&lt;/verticalHint&gt;
	 *  &amp;nbsp&amp;nbsp&amp;nbsp&amp;nbsp&lt;horizontalHint&gt;left&lt;/horizontalHint&gt;
	 *  &lt;/view&gt;
	 * </pre>
	 *
	 * corresponds to a configuration element, named <code>"view"</code>,
	 * with two children.
	 * </p>
	 *
	 * @return the child configuration elements
	 */
	//    public IConfigurationElement[] getExtensionElements() throws CoreException;

}
