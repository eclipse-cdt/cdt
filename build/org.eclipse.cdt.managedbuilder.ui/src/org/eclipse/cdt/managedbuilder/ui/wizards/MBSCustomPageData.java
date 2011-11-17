/*******************************************************************************
 * Copyright (c) 2005, 2010 Texas Instruments Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;

/**
 *   This class is responsible for storing all of the data associated with a given custom wizard page.
 */
public final class MBSCustomPageData
{

	private Set natureSet = null;

	private Set toolchainSet = null;

	private Set projectTypeSet = null;

	private IWizardPage wizardPage = null;

	private IRunnableWithProgress operation = null;

	private String id = null;

	private boolean isStock = false;

	/**
	 *  Stores data on a particular toolchain that a custom wizard page supports.
	 */
	public class ToolchainData implements Comparable
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

		/**
		 *
		 */
		@Override
		public int compareTo(Object arg0) {
			if (arg0 == null || !(arg0 instanceof ToolchainData))
				return 0;
			ToolchainData other = (ToolchainData)arg0;
			return this.id.compareTo(other.id);
		}

       /* (non-Javadoc)
        * @see java.lang.Object#equals(java.lang.Object)
        */
        @Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof ToolchainData))
				return false;
           if (this == obj) {
              return true;
           }
           return this.id.equals(((ToolchainData)obj).id);
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
		this(id, wizardPage, convertRunnable(operation), isStock);
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
			IRunnableWithProgress operation, boolean isStock)
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
	 *
	 * Nature can be either string (old mode) or Set (new mode) or null.
	 * New mode allows to take into account several natures per project.
	 * Accepting null allows to process projects w/o nature
	 * @since 4.0
	 */
	public boolean shouldBeVisibleForNature(Object nature)
	{
		if (natureSet == null) return true;
		if (nature == null) return false;

		if (nature instanceof String) // old style
			return hasNature((String)nature);
		else if (nature instanceof Set) {
			Iterator it = ((Set)nature).iterator();
			while (it.hasNext()) {
				String s = it.next().toString();
				if (hasNature(s)) return true;
			}
		}
		return false; // no one nature fits or bad data
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
		if (natureSet == null || natureSet.size() == 0)
			return null;

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
			// look for toolchain with same id.  The id in the tool-chain data should never
			// contain a version suffix.
			if (tcd.getId().equals(id))
			{
				// if we don't check versions then we're done
				if (tcd.getVersionsSupported() == null)
					return true;
				// is the toolchain of one of the specified versions?
				for (int k = 0; k < tcd.getVersionsSupported().length; k++) {
					if (tcd.getVersionsSupported()[k].equals(version))
						return true;
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
	 *
	 * Type can be either string (old mode) or Set (new mode) or null.
	 * New mode allows to take into account several types per project,
	 * or types absence in some cases
	 * @since 4.0
	 */
	public boolean shouldBeVisibleForProjectType(Object projectType)
	{
		if (projectTypeSet == null)	return true;
		if (projectType == null) return false;

		if (projectType instanceof String)
			return projectTypeSet.contains(projectType);
		else if (projectType instanceof Set) {
			Iterator it = ((Set)projectType).iterator();
			while (it.hasNext()) {
				String s = it.next().toString();
				if (projectTypeSet.contains(s))
					return true;
			}
		}
		return false; // no one type fits
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
	 * @return the {@link IRunnableWithProgress} operation associated with this page that should be run during
	 * the wizard's doRunEpilogue() method.  This operation should only be executed if in fact the page
	 * is visible.
	 * @since 3.0
	 */
	public IRunnableWithProgress getOperation()
	{
		return operation;
	}

	private static IRunnableWithProgress convertRunnable(final Runnable runnable) {
		if (runnable == null) {
			return null;
		}
		return new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				runnable.run();
			}
		};
	}
}
