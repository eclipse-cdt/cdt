/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

/**
 * @since 5.1
 */
public interface ICDTCommonProjectWizard extends IWizard {
	/**
	 * First stage of creating the project but no progress displayed. Identical to
	 * calling createIProject(name, location, new NullProgressMonitor())
	 *
	 * @param name name of the project
	 * @param location location URI for the project
	 * @return the project
	 * @throws CoreException if project creation fails for any reason
	 */
	public IProject createIProject(final String name, final URI location) throws CoreException;

	/**
	 * First stage of creating the project. Only used internally.
	 *
	 * @param name name of the project
	 * @param location location URI for the project
	 * @param monitor progress monitor
	 * @return the project
	 * @throws CoreException if project creation fails for any reason
	 */
	public IProject createIProject(final String name, final URI location, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Get the file content types supported by this project
	 *
	 * @return array of file content types
	 */
	public String[] getContentTypeIDs();

	/**
	 * Get the file extension specifications for each content type.
	 *
	 * @return array of file extension specifications
	 */
	public String[] getExtensions();

	/**
	 * Get the languages supported by each content type
	 *
	 * @return array of languages
	 */
	public String[] getLanguageIDs();

	/**
	 * Return the last project created by the call to getProject().
	 *
	 * @return the last project created
	 */
	public IProject getLastProject();

	/**
	 * Get the project natures provided by this project.
	 *
	 * @return array of project natures
	 */
	public String[] getNatures();

	/**
	 * Create and return the project specified by the wizard. Identical to calling
	 * getProject(defaults, true)
	 *
	 * @param defaults true if called from the first wizard page
	 * @return the newly created project
	 */
	public IProject getProject(boolean defaults);

	/**
	 * Create and return the project specified by the wizard.
	 *
	 * @param defaults true if called from the first wizard page
	 * @param onFinish true if the method is called when finish is pressed, false
	 * otherwise. If onFinish is false, the project is temporary and can be removed
	 * if cancel is pressed.
	 * @return the newly created project
	 */
	public IProject getProject(boolean defaults, boolean onFinish);

	/**
	 * Can be used to pass a configuration element to update the perspective based
	 * on the current settings in the Workbench/Perspectives preference page via
	 * {@link BasicNewProjectResourceWizard#updatePerspective(IConfigurationElement)}
	 *
	 * @param config the configuration element
	 * @param propertyName not used
	 * @param data not used
	 * @throws CoreException
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException;

}
