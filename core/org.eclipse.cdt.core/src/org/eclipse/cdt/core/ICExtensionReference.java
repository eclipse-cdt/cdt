/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public interface ICExtensionReference {

    /**
     * Return the extension point of this reference.
     * 
     * @return String
     */
    public String getExtension();

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
     * @return the ICDescriptor
     */
    public ICDescriptor getCDescriptor();
    
	/**
	 * Creates and returns a new instance of the cextension executable 
	 * identified by the &lt;run&gt; attribute of the cextension.
	 * <p>
	 * The ICExtension is instantiated using its 0-argument public 
	 * constructor. If the class implements the
	 * <code>org.eclipse.core.runtime.IExecutableExtension</code> interface, the method
	 * <code>setInitializationData</code> is called, passing to the object
	 * the configuration information that was used to create it. 
	 * </p>
	 * <p>
	 * Unlike other methods on this object, invoking this method may activate 
	 * the plug-in.
	 * </p>
	 *
	 * @return the executable ICExtension instance
	 * @exception CoreException if an instance of the executable extension
	 *   could not be created for any reason.
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData
	 */
    public ICExtension createExtension() throws CoreException;

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
    public IConfigurationElement[] getExtensionElements() throws CoreException;
}
