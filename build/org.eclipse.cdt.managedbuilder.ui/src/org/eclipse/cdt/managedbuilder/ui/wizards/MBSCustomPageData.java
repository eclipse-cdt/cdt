/*******************************************************************************
 * Copyright (c) 2005 Texas Instruments Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;

/**
 *   This class is responsible for storing all of the data associated with a given custom wizard page.
 */
public final class MBSCustomPageData
{

	private Set natureSet = null;

	private Set toolchainSet = null;

	private Set projectTypeSet = null;

	private IWizardPage wizardPage = null;

	private Runnable operation = null;

	private String id = null;

	private boolean isStock = false;

	/**
	 *  Stores data on a particular toolchain that a custom wizard page supports.
	 */
	public class ToolchainData
	{
		private String id = null;

		private String[] versionsSupported = null;

		/**
		 * @param id The id to set.
		 * @since 3.0
		 */
		public void setId(String id)
		{
			this.id = id;
		}

		/**
		 * @return Returns the id.
		 * @since 3.0
		 */
		public String getId()
		{
			return id;
		}

		/**
		 * @param versionsSupported The versionsSupported to set.
		 * @since 3.0
		 */
		public void setVersionsSupported(String[] versionsSupported)
		{
			this.versionsSupported = versionsSupported;
		}

		/**
		 * @return Returns the versionsSupported.
		 * @since 3.0
		 */
		public String[] getVersionsSupported()
		{
			return versionsSupported;
		}

	}

	/**
	 * Contstructs a custom page data record
	 * 
	 * @param id - Unique ID of the page
	 * @param wizardPage - the IWizardPage that is displayed in the wizard
	 * @param operation - the Runnable() that is executed during the wizard's DoRunEpilogue() method, or null if no operation is specified
	 * @param isStock - true if the page is a stock page provided by Managed Build, false otherwise.
	 * @since 3.0
	 */
	public MBSCustomPageData(String id, IWizardPage wizardPage,
			Runnable operation, boolean isStock)
	{

		this.id = id;
		this.wizardPage = wizardPage;
		this.operation = operation;
		this.isStock = isStock;
	}

	/**
	 * @return The unique ID by which this page is referred to.
	 * @since 3.0
	 */
	public String getID()
	{
		return id;
	}

	/**
	 * @return The IWizardPage corresponding to the actual page to be displayed in the wizard.
	 * @since 3.0
	 */
	public IWizardPage getWizardPage()
	{
		return wizardPage;
	}

	/**
	 * @return true if this page is a stock page provided by the Managed Build System, false otherwise.
	 * @since 3.0 
	 */
	public boolean isStockPage()
	{

		return isStock;
	}

	/**
	 * @param nature A fully qualified nature ID (org.eclipse.core.resources.IProjectNature).  Currently MBS
	 * 				 only supports creating projects with either one of two natures:
	 * 				 org.eclipse.cdt.core.cnature and org.eclipse.cdt.core.ccnature
	 * @return true if the page should be visible when the project has the given nature, false otherwise.
	 * @since 3.0
	 */
	public boolean shouldBeVisibleForNature(String nature)
	{
		if (natureSet == null)
			return true;

		return hasNature(nature);
	}

	private boolean hasNature(String nature)
	{
		return natureSet.contains(nature);

	}

	/**
	 * @return An array of nature IDs corresponding to the natures for which this page should appear.
	 * @since 3.0
	 */
	public String[] getNatures()
	{
		Object[] objArray = natureSet.toArray();

		String[] strArray = new String[objArray.length];
		for (int k = 0; k < objArray.length; k++)
		{
			strArray[k] = objArray[k].toString();

		}

		return strArray;
	}

	/**
	 * @param id - The unique ID of the toolchain
	 * @param version - The version of the toolchain, or <code>null</code> if versions are not to be checked. 
	 * @return true if the page should be present for the given toolchain and version, false otherwise.
	 * @since 3.0
	 */
	public boolean shouldBeVisibleForToolchain(String id, String version)
	{
		// if no toolchains specified then always return true
		if (toolchainSet.size() == 0)
			return true;

		Iterator iterator = toolchainSet.iterator();

		while (iterator.hasNext())
		{
			ToolchainData tcd = (ToolchainData) iterator.next();

			// look for toolchain with same id
			if (tcd.getId().equals(id))
			{
				// if we don't check versions then we're done
				if (version == null)
					return true;

				// does the toolchain have a compatible version?
				for (int k = 0; k < tcd.getVersionsSupported().length; k++)
				{
					// TODO:  implement version support - should this check for an exact match?
					//        An older version of a tool-chain could use a different custom page
					//        than a newer version.
					if (tcd.getVersionsSupported()[k].compareTo(version) >= 0)
					{
						return true;
					}
				}

			}
		}

		return false;
	}

	/**
	 * @return The set of toolchains supported by this page, or null if this page does not specify any
	 * 		   toolchain dependencies.
	 * @since 3.0
	 */
	public ToolchainData[] getToolchains()
	{
		if (toolchainSet == null)
			return null;

		ToolchainData[] tcd = new ToolchainData[toolchainSet.size()];

		Iterator iterator = toolchainSet.iterator();

		int k = 0;

		while (iterator.hasNext())
		{
			tcd[k++] = (ToolchainData) iterator.next();
		}
		if (tcd.length > 0)
			return tcd;
		else
			return null;
	}

	/**
	 * @return The set of project types supported by this page, or null if there are no such dependencies.
	 * @since 3.0
	 */
	public String[] getProjectTypes()
	{
		if (projectTypeSet == null || projectTypeSet.size() == 0)
			return null;

		Object[] objArray = projectTypeSet.toArray();

		String[] strArray = new String[objArray.length];
		for (int k = 0; k < objArray.length; k++)
		{
			strArray[k] = objArray[k].toString();

		}

		return strArray;
	}

	/**
	 * Adds a dependency to this page upon a given nature.  The page will be visible
	 * iff the given nature is selected by the user.
	 * 
	 * @param nature The unique ID of the nature.
	 * @since 3.0
	 */
	public void addNature(String nature)
	{
		if (nature == null)
			return;

		if (natureSet == null)
			natureSet = new TreeSet();

		natureSet.add(nature);
	}

	/**
	 * Adds a dependency to this page upon a given toolchain.  The page will be visible
	 * iff one or more of the selected project configurations utilizes the specified toolchain.
	 * If versions are specified, then the version of the toolchain must exactly match one of the specified versions.
	 * 
	 * @param toolchainID - The unique ID of the toolchain.
	 * @param versionsSupported - A comma separated list of supported versions, or null if no version checking is to be done.
	 * @since 3.0
	 */
	public void addToolchain(String toolchainID, String[] versionsSupported)
	{
		if(toolchainID == null)
			return;
		
		if (toolchainSet == null)
			toolchainSet = new TreeSet();

		ToolchainData toolchainData = new ToolchainData();
		toolchainData.setId(toolchainID);
		toolchainData.setVersionsSupported(versionsSupported);

		toolchainSet.add(toolchainData);
	}

	/**
	 * @param projectType The unique ID of the project type to check.
	 * @return true if this page should be visible if the given project type is selected, false otherwise.
	 * @since 3.0
	 */
	public boolean shouldBeVisibleForProjectType(String projectType)
	{
		if (projectTypeSet == null)
			return true;

		if (projectTypeSet.contains(projectType))
			return true;

		return false;
	}

	/**
	 * Adds a dependency to this page upon a given project type.  The page will be visible
	 * iff the given project type is selected by the user.
	 * 
	 * @param projectType - The unique ID of the project type.
	 * @since 3.0
	 */
	public void addProjectType(String projectType)
	{
		if(projectType == null)
			return;
			
		if (projectTypeSet == null)
			projectTypeSet = new TreeSet();

		projectTypeSet.add(projectType);
	}

	/**
	 * @return the java.lang.Runnable() operation associated with this page that should be run during
	 * the wizard's doRunEpilogue() method.  This operation should only be executed if in fact the page
	 * is visible.
	 * @since 3.0
	 */
	public Runnable getOperation()
	{
		return operation;
	}
}
