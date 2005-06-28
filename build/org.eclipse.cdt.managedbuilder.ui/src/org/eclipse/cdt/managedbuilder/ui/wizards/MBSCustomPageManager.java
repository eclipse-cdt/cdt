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

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.IWizardPage;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Stack;
import org.eclipse.core.runtime.CoreException;

/**
 *  This class is responsible for managing the use of custom pages in the Managed Build System's
 *  New Project wizards.
 *  
 *  This class is a singleton.
 */
public final class MBSCustomPageManager
{

	/**
	 * ID attribute of nature element
	 */
	public static final String NATURE_ID = "natureID"; //$NON-NLS-1$

	/**
	 *  versions supported attribute of toolchain element
	 */
	public static final String VERSIONS_SUPPORTED = "versionsSupported"; //$NON-NLS-1$

	/**
	 * ID attribute of toolchain element
	 */
	public static final String TOOLCHAIN_ID = "toolchainID"; //$NON-NLS-1$

	/**
	 *  ID attribute of projectType element
	 */
	public static final String PROJECT_TYPE_ID = "projectTypeID"; //$NON-NLS-1$

	/**
	 *  nature element
	 */
	public static final String NATURE = "nature"; //$NON-NLS-1$

	/**
	 *  toolchain element
	 */
	public static final String TOOLCHAIN = "toolchain"; //$NON-NLS-1$

	/**
	 *  project type element
	 */
	public static final String PROJECT_TYPE = "projectType"; //$NON-NLS-1$

	/**
	 *   attribute for the class associated witha wizardPage element
	 */
	public static final String PAGE_CLASS = "pageClass"; //$NON-NLS-1$

	/**
	 *  attribute for the operation that is run for a wizardPage during the wizard's DoRunEpilogue() method
	 */
	public static final String OPERATION_CLASS = "operationClass"; //$NON-NLS-1$

	/**
	 *   ID attribute for wizardPage
	 */
	public static final String ID = "ID"; //$NON-NLS-1$

	/**
	 *   element for a custom wizard page
	 */
	public static final String WIZARD_PAGE = "wizardPage"; //$NON-NLS-1$

	/**
	 * Maps String IDs to IWizardPages
	 */
	private static Map idToPageDataMap = null;

	
	/**
	 *   The set of pages that this manager knows about.
	 */
	private static Set pageSet = null;

	/**
	 * Maps page IDs to the properties that page has set.
	 */
	private static java.util.Map pageIDtoPagePropertiesMap = null;

	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.managedbuilder.ui.newWizardPages"; //$NON-NLS-1$

	/**
	 * 
	 * Looks for contributions to the extension point org.eclipse.cdt.managedbuilder.ui.newWizardPages and adds all pages to the manager.
	 * @since 3.0
	 * 
	 */
	public static void loadExtensions() throws BuildException
	{
		loadExtensionsSynchronized();
	}

	private synchronized static void loadExtensionsSynchronized()
			throws BuildException
	{
		// Get the extensions
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT_ID);
		if (extensionPoint != null)
		{
			IExtension[] extensions = extensionPoint.getExtensions();
			if (extensions != null)
			{

				for (int i = 0; i < extensions.length; ++i)
				{
					IExtension extension = extensions[i];

					// Get the "configuraton elements" defined in the plugin.xml file.
					// Note that these "configuration elements" are not related to the
					// managed build system "configurations".  
					// From the PDE Guide:
					//  A configuration element, with its attributes and children, directly 
					//  reflects the content and structure of the extension section within the 
					//  declaring plug-in's manifest (plugin.xml) file. 
					IConfigurationElement[] elements = extension.getConfigurationElements();

					// process the top level elements for this extension
					for (int k = 0; k < elements.length; k++)
					{
						IConfigurationElement element = elements[k];

						if (element.getName().equals(WIZARD_PAGE))
						{
							// load the data associated with the wizard page
							loadWizardPage(element);
						}

						else
						{
							// there are currently no other supported element types
							// so throw an exception
							throw new BuildException(ManagedBuilderUIMessages.getResourceString("MBSCustomPageManager.error0") //$NON-NLS-1$
									+ element.getName()
									+ ManagedBuilderUIMessages.getResourceString("MBSCustomPageManager.error1") //$NON-NLS-1$
									+ EXTENSION_POINT_ID);
						}
					}

				}

			}
		}
	}

	private static void loadWizardPage(IConfigurationElement element)
			throws BuildException
	{
		// get the ID and wizard page
		String id = element.getAttribute(ID);

		String operationClassName = element.getAttribute(OPERATION_CLASS); // optional element so may be null

		IWizardPage wizardPage = null;
		Runnable operation = null;
		// instantiate the classes specified in the manifest

		// first try to create the page class, which is required
		try
		{
			wizardPage = (IWizardPage) element.createExecutableExtension(PAGE_CLASS);

			// the operation is an optional element so it might not be present
			if (element.getAttribute(OPERATION_CLASS) != null)
				operation = (Runnable) element.createExecutableExtension(OPERATION_CLASS);
		}
		catch (CoreException e)
		{
			// convert to a build exception
			throw new BuildException(e.getMessage());
		}

		// create the page data and add it to ourselves
		MBSCustomPageData currentPageData = new MBSCustomPageData(id,
				wizardPage, operation, false);
		idToPageDataMap.put(id, currentPageData);
		pageSet.add(currentPageData);

		// load any child elements
		IConfigurationElement[] children = element.getChildren();

		for (int k = 0; k < children.length; k++)
		{
			IConfigurationElement childElement = children[k];

			if (childElement.getName().equals(PROJECT_TYPE))
			{
				loadProjectType(childElement, currentPageData);
			}

			else
			{
				if (childElement.getName().equals(TOOLCHAIN))
				{
					loadToolchain(childElement, currentPageData);
				}

				else
				{
					if (childElement.getName().equals(NATURE))
					{
						loadNature(childElement, currentPageData);
					}

					else
					{
						// no other types supported... throw an exception
						//						 there are currently no other supported element types
						// so throw an exception
						throw new BuildException(ManagedBuilderUIMessages.getResourceString("MBSCustomPageManager.error2") //$NON-NLS-1$
								+ element.getName()
								+ ManagedBuilderUIMessages.getResourceString("MBSCustomPageManager.error3") //$NON-NLS-1$
								+ EXTENSION_POINT_ID);
					}
				}
			}
		}
	}

	private static void loadProjectType(IConfigurationElement element,
			MBSCustomPageData currentPageData) throws BuildException
	{
		String projectType = element.getAttribute(PROJECT_TYPE_ID);

		if (projectType != null)
			currentPageData.addProjectType(projectType);
		else
			throw new BuildException(ManagedBuilderUIMessages.getResourceString("MBSCustomPageManager.error4")); //$NON-NLS-1$
	}

	private static void loadToolchain(IConfigurationElement element,
			MBSCustomPageData currentPageData) throws BuildException
	{
		String toolchainID = element.getAttribute(TOOLCHAIN_ID);

		if (toolchainID != null)
		{
			// get the supported versions
			String unparsedVersions = element.getAttribute(VERSIONS_SUPPORTED);
			String[] versionsSupported = null;

			if (unparsedVersions != null)
			{
				// parse out the individual versions - they are comma separated
				versionsSupported = unparsedVersions.split(","); //$NON-NLS-1$
			}

			// add toolchain data for the page
			currentPageData.addToolchain(toolchainID, versionsSupported);
		}
		else
			throw new BuildException(ManagedBuilderUIMessages.getResourceString("MBSCustomPageManager.error5")); //$NON-NLS-1$
	}

	private static void loadNature(IConfigurationElement element,
			MBSCustomPageData currentPageData) throws BuildException
	{
		String nature = element.getAttribute(NATURE_ID);

		if (nature != null)
			currentPageData.addNature(nature);
		else
			throw new BuildException(ManagedBuilderUIMessages.getResourceString("MBSCustomPageManager.error6")); //$NON-NLS-1$
	}

	/**
	 * 
	 * @param pageID - The unique ID of the page to search for.
	 * 
	 * @return - The MBSCustomPageData corresponding to the page, or null
	 * if not found.
	 * 
	 * @since 3.0
	 */
	public static MBSCustomPageData getPageData(String pageID)
	{
		return (MBSCustomPageData) idToPageDataMap.get(pageID);
	}

	/**
	 * @param pageID - The unique ID of the page to be tested.
	 * @return true if the page is visible given the currently selected project type, nature, and toolchain.  false otherwise.
	 * @since 3.0
	 */
	public static boolean isPageVisible(String pageID)
	{
		MBSCustomPageData page = getPageData(pageID);
		
		if(page == null)
			return false;
		
		
		//	first, find out what project type, toolchain, and nature have been set
		Map pagePropertiesMap = (Map) pageIDtoPagePropertiesMap.get(CProjectPlatformPage.PAGE_ID);

		String projectType = pagePropertiesMap.get(CProjectPlatformPage.PROJECT_TYPE).toString();

		Set toolchainSet = (Set) pagePropertiesMap.get(CProjectPlatformPage.TOOLCHAIN);

		String nature = pagePropertiesMap.get(CProjectPlatformPage.NATURE).toString();

		//		 does the page follow nature and project type constraints?
		if (page.shouldBeVisibleForNature(nature)
				&& page.shouldBeVisibleForProjectType(projectType))
		{

			MBSCustomPageData.ToolchainData[] toolchainData = page.getToolchains();

			// if no toolchains are specified then we're done
			if (toolchainData == null)
			{
				return true;
			}

			// otherwise, iterate through the toolchains to see if one matches
			for (int k = 0; k < toolchainData.length; k++)
			{

				// check all toolchains, see if there is one for which we should be present
				Iterator toolchainIterator = toolchainSet.iterator();

				while (toolchainIterator.hasNext())
				{
					IToolChain toolchain = (IToolChain) toolchainIterator.next();
					String id = ManagedBuildManager.getIdFromIdAndVersion(toolchain.getId());
					String version = ManagedBuildManager.getVersionFromIdAndVersion(toolchain.getId());

					// check the ID and version
					if (page.shouldBeVisibleForToolchain(id, version))
					{
						return true;
					}
				}

			}

		}

		return false;
	}

	/**
	 * 
	 * Publishes a piece of data associated with a wizard page.  Clients (e.g. other wizard pages) can retrieve
	 * the values of these pieces of data later given the proper page ID and key.
	 * 
	 * @param pageID - The unique ID of the page for which the data is being added.
	 * 
	 * @param key - A unique name by which the data is referred to.
	 * 
	 * @param data - The data to be stored.  No assumptions are made about the type of data stored.  It is up to the
	 * 				 contributor of a given page to establish their own contract as to what type of data is stored.
	 * 
	 * @since 3.0
	 * 
	 * @see org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager#getPageProperty(String, String)
	 */
	public static void addPageProperty(String pageID, String key, Object data)
	{
		Map propertiesMap = (Map) pageIDtoPagePropertiesMap.get(pageID);

		if (propertiesMap == null)
		{
			propertiesMap = new TreeMap();
			pageIDtoPagePropertiesMap.put(pageID, propertiesMap);
		}

		propertiesMap.put(key, data);
	}

	/**
	 * Retrieves a previously published piece of data associated with a wizard page.
	 * 
	 * 
	 * @param pageID - The unique ID of the page for which the
	 * data should be retrieved.
	 * 
	 * @param key - The unique name of the data to be retrieved.
	 * 
	 * @return  The data that was stored for the given key.  No assumptions are made about the type of data stored.  It is up to the
	 * 			contributor of a given page to establish their own contract as to what type of data is stored.
	 * 
	 * 			There are certain well known pieces of data published by the stock wizard pages provided by the Managed Build System.
	 * 			See org.eclipse.cdt.maangedbuilder.ui.wizards.CProjectPlatformPage.
	 * 
	 * @see org.eclipse.cdt.maangedbuilder.ui.wizards.CProjectPlatformPage
	 * @see org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager#addPageProperty(String, String, Object)
	 * 
	 * @since 3.0 
	 */
	public static Object getPageProperty(String pageID, String key)
	{
		Map propertiesMap = (Map) pageIDtoPagePropertiesMap.get(pageID);

		if (propertiesMap != null)
		{
			return propertiesMap.get(key);
		}

		else
			return null;
	}

	/**
	 * Gets the next page that should appear in the wizard.  This takes into account the selected
	 * project type, project nature, and toolchains.
	 * 
	 * @param currentPageID - The unique ID of the page the wizard is currently displaying.
	 * @return The next page that should be displayed in the wizard, or null if at the end of the wizard.
	 * @since 3.0
	 */
	public static IWizardPage getNextPage(String currentPageID)
	{
		// find the current page in the set of pages
		MBSCustomPageData pageData = getPageData(currentPageID);
		MBSCustomPageData currentData = null;

		Iterator iterator = pageSet.iterator();

		while (iterator.hasNext())
		{
			currentData = (MBSCustomPageData) iterator.next();

			if (currentData == pageData)
			{

				// we found the page we're looking for so stop looking
				break;
			}
		}

		if (currentData == pageData)
		{
			// we found our page
			// look for the next page that satisfies all project type, toolchain, and nature criteria

			// first, find out what project type, toolchain, and nature have been set
			Map pagePropertiesMap = (Map) pageIDtoPagePropertiesMap.get(CProjectPlatformPage.PAGE_ID);

			String projectType = pagePropertiesMap.get(CProjectPlatformPage.PROJECT_TYPE).toString();

			Set toolchainSet = (Set) pagePropertiesMap.get(CProjectPlatformPage.TOOLCHAIN);

			String nature = pagePropertiesMap.get(CProjectPlatformPage.NATURE).toString();

			IWizardPage nextPage = null;

			boolean pageFound = false;

			while (iterator.hasNext() && !pageFound)
			{
				MBSCustomPageData potentialPage = (MBSCustomPageData) iterator.next();

				if (isPageVisible(potentialPage.getID()))
				{
					pageFound = true;
					nextPage = potentialPage.getWizardPage();
				}
			}

			if (pageFound)
				return nextPage;

		}

		return null;
	}

	/**
	 * Adds an entry for a stock page into the manager.  This is used for pages provided by the Managed Build System that are not loaded via the
	 * extension point mechanism.
	 * 
	 * @param page - The IWizardPage to add.
	 * @param pageID - A unique ID to associate with this page.  This ID will be used to refer to the page by the rest of the system.
	 * @since 3.0 
	 */
	public static void addStockPage(IWizardPage page, String pageID)
	{
		MBSCustomPageData pageData = new MBSCustomPageData(pageID, page, null, true);
		idToPageDataMap.put(pageID, pageData);
		pageSet.add(pageData);
	}

	/**
	 * Gets the previous page that should appear in the wizard.  This takes into account the selected
	 * project type, project nature, and toolchains.  Stock pages can be returned by this method as well as
	 * custom pages.
	 * 
	 * @param currentPageID - The unique ID of the page currently being displayed in the wizard.
	 * @return - The IWizardPage that corresponds to the previous page to be displayed in the wizard, or null if at the start of the wizard.
	 * @since 3.0
	 */
	public static IWizardPage getPreviousPage(String currentPageID)
	{
		// find the current page in the set of pages
		MBSCustomPageData pageData = getPageData(currentPageID);
		MBSCustomPageData currentData = null;

		Iterator iterator = pageSet.iterator();

		// since Java has no concept of a reverse-iterator yet, we must keep a stack of the
		// pages we have visited, so that we can get them in reverse chronological order
		Stack pageDataStack = new Stack();

		while (iterator.hasNext())
		{
			currentData = (MBSCustomPageData) iterator.next();

			if (currentData == pageData)
			{

				// we found the page we're looking for so stop looking
				break;
			}

			else
			{
				pageDataStack.push(currentData);
			}
		}

		if (currentData == pageData)
		{
			// we found our page
			// look for the previous page that satisfies all project type, toolchain, and nature criteria

			// first, find out what project type, toolchain, and nature have been set
			Map pagePropertiesMap = (Map) pageIDtoPagePropertiesMap.get(CProjectPlatformPage.PAGE_ID);

			String projectType = pagePropertiesMap.get(CProjectPlatformPage.PROJECT_TYPE).toString();

			Set toolchainSet = (Set) pagePropertiesMap.get(CProjectPlatformPage.TOOLCHAIN);

			String nature = pagePropertiesMap.get(CProjectPlatformPage.NATURE).toString();

			IWizardPage prevPage = null;

			boolean pageFound = false;

			// use the stack to visit the previous pages
			while (pageDataStack.size() != 0 && !pageFound)
			{
				MBSCustomPageData potentialPage = (MBSCustomPageData) pageDataStack.pop();

				if (isPageVisible(potentialPage.getID()))
				{
					pageFound = true;
					prevPage = potentialPage.getWizardPage();
				}

			}

			if (pageFound)
				return prevPage;

		}

		return null;
	}

	/**
	 * Gets the pages that the page manager knows about.
	 * 
	 * @return An array of IWizardPage objects corresponding to all pages the manager knows about.
	 * Pages are returned in the order they appear in the wizard, and include both stock and custom pages.
	 * 
	 * @since 3.0
	 * 
	 * @see getCustomPages()
	 */
	public static IWizardPage[] getPages()
	{
		IWizardPage[] pages = new IWizardPage[pageSet.size()];

		Iterator iterator = pageSet.iterator();
		
		int k = 0;
		
		while(iterator.hasNext())
		{
			MBSCustomPageData page = (MBSCustomPageData) iterator.next();
			
			pages[k++] = page.getWizardPage();
		}

		return pages;
	}

	/**
	 * Gets all custom pages that the page manager knows about.
	 * 
	 * @return An array of IWizardPage objects corresponding to all custom pages the manager knows about.
	 * Pages are returned in the order they appear in the wizard.  Stock pages are not included.
	 * 
	 * @since 3.0
	 * 
	 * @see getPages()
	 */
	public static IWizardPage[] getCustomPages()
	{
		Set customPageSet = new LinkedHashSet();

		Iterator pageIterator = pageSet.iterator();
		
		while(pageIterator.hasNext())
		{
			MBSCustomPageData page = (MBSCustomPageData) pageIterator.next();
			
			if (!page.isStockPage())
			{
				customPageSet.add(page.getWizardPage());
			}

		}

		Iterator iterator = customPageSet.iterator();

		IWizardPage[] pages = new IWizardPage[customPageSet.size()];

		int k = 0;
		while (iterator.hasNext())
		{
			pages[k++] = (IWizardPage) iterator.next();
		}

		return pages;
	}

	/**
	 * Gets all operations that should be run during the wizard's DoRunEpilogue() method.  Only operations for visible pages are returned.
	 * 
	 * @return array of type Runnable[] corresponding to the operations.
	 * 
	 * @since 3.0
	 */
	public static Runnable[] getOperations()
	{
		Set operationSet = new LinkedHashSet();

		Iterator pageIterator = pageSet.iterator();
		
		while(pageIterator.hasNext())
		{
			MBSCustomPageData page = (MBSCustomPageData) pageIterator.next();
			
			if (!page.isStockPage()
					&& isPageVisible(page.getID()))
			{
				if (page.getOperation() != null)
				{
					operationSet.add(page.getOperation());
				}
			}

		}

		if(operationSet.size() == 0)
			return null;
		
		Iterator iterator = operationSet.iterator();

		Runnable[] operations = new Runnable[operationSet.size()];

		int k = 0;
		while (iterator.hasNext())
		{
			operations[k++] = (Runnable) iterator.next();
		}

		return operations;

	}

	/**
	 *  Initializes the manager.
	 *  
	 *  This method should be called before any other operations are performed using this class, and should
	 *  be called every time pages are added to the wizard.
	 *  
	 *  @since 3.0
	 */
	public static void init()
	{
		idToPageDataMap = new TreeMap();
		pageIDtoPagePropertiesMap = new TreeMap();
		pageSet = new LinkedHashSet();
	}

	// singleton class - do not use
	private MBSCustomPageManager()
	{

	}
}
