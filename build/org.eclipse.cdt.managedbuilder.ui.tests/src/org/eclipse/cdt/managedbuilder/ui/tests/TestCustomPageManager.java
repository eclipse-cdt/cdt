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

package org.eclipse.cdt.managedbuilder.ui.tests;

import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.managedbuilder.ui.wizards.*;
import org.eclipse.cdt.managedbuilder.ui.tests.util.TestToolchain;

/**
 *   This class is respnsible for testing the functionality of the custom page manager (MBSCustomPageManager)
 *   that manages custom wizard pages that are provided by ISVs to supplement the pages in the New Project wizards
 *   for Managed Build.
 *   
 *   The idea behind the test plan is pretty simple.
 *   
 *   There are seven custom wizard pages specified by this plugin.  One page has no restrictions placed upon it and
 *   thus should always be present.  There are two pages which are constrained by different natures respectively,
 *   two constrained by different toolchains, and two constrained by different project types.  The goal is to
 *   change around what options (i.e. nature, project type, toolchain) a hypothetical user would set, and then
 *   check to see if the proper pages are displayed in the proper order.
 */
public class TestCustomPageManager extends TestCase
{

	private static final String alwaysPresentPageName = "org.eclipse.cdt.managedbuilder.ui.tests.wizardPages.AlwaysPresentWizardPage";
	private static final String natureAPageName = "org.eclipse.cdt.managedbuilder.ui.tests.wizardPages.NatureAWizardPage";
	private static final String natureBPageName = "org.eclipse.cdt.managedbuilder.ui.tests.wizardPages.NatureBWizardPage";
	private static final String toolchainCPageName = "org.eclipse.cdt.managedbuilder.ui.tests.wizardPages.ToolchainCWizardPage";
	private static final String projectTypeDPageName = "org.eclipse.cdt.managedbuilder.ui.tests.wizardPages.ProjectTypeDWizardPage";
	private static final String projectTypeEPageName = "org.eclipse.cdt.managedbuilder.ui.tests.wizardPages.ProjectTypeEWizardPage";
	private static final String toolchainFPageName = "org.eclipse.cdt.managedbuilder.ui.tests.wizardPages.ToolchainFWizardPage";
	private static final String toolchainCv20PageName = "org.eclipse.cdt.managedbuilder.ui.tests.wizardPages.ToolchainCv20WizardPage";
	
	public static boolean testFlag = false;
	
	public void setUp() throws Exception
	{
		MBSCustomPageManager.init();
		
		MBSCustomPageManager.loadExtensions();
		
		testFlag = false;
	}
	
	/**
	 *  Test with a setup such that only an unconstrained page should show up.
	 */
	public void testOneVisiblePage()
	{
		// set the project type to be "X"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.PROJECT_TYPE, "X");

		// set the toolchain to "Y"
		Set toolchainSet = new LinkedHashSet();
		TestToolchain toolchain = new TestToolchain();
		toolchain.setID("Y");
		toolchainSet.add(toolchain);
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.TOOLCHAIN, toolchainSet);
		
		// set the nature to "Z"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.NATURE, "Z");
		
		// check each of the pages
		
		// this page should always be visible
		if(!MBSCustomPageManager.isPageVisible(alwaysPresentPageName))
		{
			fail("AlwaysPresentWizardPage should be visible");
		}
		
		// next page for this page should be null
		if(MBSCustomPageManager.getNextPage(alwaysPresentPageName) != null)
		{
			fail("AlwaysPresentWizardPage should not have a next page.");
		}
		
		// the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureAPageName))
		{
			fail("NatureAWizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureBPageName))
		{
			fail("NatureBWizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCPageName))
		{
			fail("ToolChainCWizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCv20PageName))
		{
			fail("ToolChainCv20WizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeDPageName))
		{
			fail("ProjectTypeDWizardPage should not be visible");
		}

//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeEPageName))
		{
			fail("ProjectTypeEWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainFPageName))
		{
			fail("ToolchainFWizardPage should not be visible");
		}
	}
	
	
	/**
	 *   Set the nature to "A".  Only pages with no contraints or natures set to "A" should show up.
	 */
	public void testNatureA()
	{
//		 set the project type to be "X"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.PROJECT_TYPE, "X");

		// set the toolchain to "Y"
		Set toolchainSet = new LinkedHashSet();
		TestToolchain toolchain = new TestToolchain();
		toolchain.setID("Y");
		toolchainSet.add(toolchain);
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.TOOLCHAIN, toolchainSet);
		
		// set the nature to "A"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.NATURE, "A");
		
		// check each of the pages
		
		// this page should always be visible
		if(!MBSCustomPageManager.isPageVisible(alwaysPresentPageName))
		{
			fail("AlwaysPresentWizardPage should be visible");
		}
		
		// next page for this page should be the page for Nature A
		if(MBSCustomPageManager.getNextPage(alwaysPresentPageName) != MBSCustomPageManager.getPageData(natureAPageName).getWizardPage())
		{
			fail("AlwaysPresentWizardPage's next page should be NatureAWizardPage");
		}
		
		// Nature A page's previous page should be the always present page
		if(MBSCustomPageManager.getPreviousPage(natureAPageName) != MBSCustomPageManager.getPageData(alwaysPresentPageName).getWizardPage())
		{
			fail("NatureAWizardPage's next page should be AlwaysPresentWizardPage");
		}
	
		// Nature A page should be visible
		if(!MBSCustomPageManager.isPageVisible(natureAPageName))
		{
			fail("NatureAWizardPage should be visible");
		}
		
		// Nature A page's next page should be null
		if(MBSCustomPageManager.getNextPage(natureAPageName) != null)
		{
			fail("NatureAWizardPage should not have a next page.");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureBPageName))
		{
			fail("NatureBWizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCPageName))
		{
			fail("ToolChainCWizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCv20PageName))
		{
			fail("ToolChainCv20WizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeDPageName))
		{
			fail("ProjectTypeDWizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeEPageName))
		{
			fail("ProjectTypeEWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainFPageName))
		{
			fail("ToolchainFWizardPage should not be visible");
		}
	}
	
	/**
	 *   Set the nature to "B".  Only pages with no contraints or natures set to "B" should show up.
	 */
	public void testNatureB()
	{
//		 set the project type to be "X"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.PROJECT_TYPE, "X");

		// set the toolchain to "Y"
		Set toolchainSet = new LinkedHashSet();
		TestToolchain toolchain = new TestToolchain();
		toolchain.setID("Y");
		toolchainSet.add(toolchain);
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.TOOLCHAIN, toolchainSet);
		
		// set the nature to "B"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.NATURE, "B");
		
		// check each of the pages
		
		// this page should always be visible
		if(!MBSCustomPageManager.isPageVisible(alwaysPresentPageName))
		{
			fail("AlwaysPresentWizardPage should be visible");
		}
		
		// next page for this page should be the page for Nature B
		if(MBSCustomPageManager.getNextPage(alwaysPresentPageName) != MBSCustomPageManager.getPageData(natureBPageName).getWizardPage())
		{
			fail("AlwaysPresentWizardPage's next page should be NatureBWizardPage");
		}
		
		// Nature B page's previous page should be the always present page
		if(MBSCustomPageManager.getPreviousPage(natureBPageName) != MBSCustomPageManager.getPageData(alwaysPresentPageName).getWizardPage())
		{
			fail("NatureBWizardPage's next page should be AlwaysPresentWizardPage");
		}
		
		// Nature B page should be visible
		if(!MBSCustomPageManager.isPageVisible(natureBPageName))
		{
			fail("NatureBWizardPage should be visible");
		}
		
		// Nature B page's next page should be null
		if(MBSCustomPageManager.getNextPage(natureBPageName) != null)
		{
			fail("NatureBWizardPage should not have a next page.");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureAPageName))
		{
			fail("NatureAWizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCPageName))
		{
			fail("ToolChainCWizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCv20PageName))
		{
			fail("ToolChainCv20WizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeDPageName))
		{
			fail("ProjectTypeDWizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeEPageName))
		{
			fail("ProjectTypeEWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainFPageName))
		{
			fail("ToolchainFWizardPage should not be visible");
		}
	}
	
	/**
	 *   Set the toolchain to "C".  Only pages with no contraints or toolchains set to "C" should show up.
	 */
	public void testToolchainC()
	{
//		 set the project type to be "X"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.PROJECT_TYPE, "X");

		// set the toolchain to "C"
		Set toolchainSet = new LinkedHashSet();
		TestToolchain toolchain = new TestToolchain();
		toolchain.setID("C");
		toolchainSet.add(toolchain);
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.TOOLCHAIN, toolchainSet);
		
		// set the nature to "Z"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.NATURE, "Z");
		
		// check each of the pages
		
		// this page should always be visible
		if(!MBSCustomPageManager.isPageVisible(alwaysPresentPageName))
		{
			fail("AlwaysPresentWizardPage should be visible");
		}
		
		// next page for this page should be the page for toolchain C
		if(MBSCustomPageManager.getNextPage(alwaysPresentPageName) != MBSCustomPageManager.getPageData(toolchainCPageName).getWizardPage())
		{
			fail("AlwaysPresentWizardPage's next page should be ToolchainCWizardPage");
		}
		
		// toolchain C page's previous page should be the always present page
		if(MBSCustomPageManager.getPreviousPage(toolchainCPageName) != MBSCustomPageManager.getPageData(alwaysPresentPageName).getWizardPage())
		{
			fail("ToolchainCWizardPage's previous page should be AlwaysPresentWizardPage");
		}
		
		// Toolchain C page should be visible
		if(!MBSCustomPageManager.isPageVisible(toolchainCPageName))
		{
			fail("ToolchainCWizardPage should be visible");
		}
		
		// Toolchain C page's next page should be null
		if(MBSCustomPageManager.getNextPage(toolchainCPageName) != null)
		{
			fail("ToolchainCWizardPage should not have a next page.");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCv20PageName))
		{
			fail("ToolChainCv20WizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureAPageName))
		{
			fail("NatureAWizardPage should not be visible");
		}
		
		//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureBPageName))
		{
			fail("NatureBWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeDPageName))
		{
			fail("ProjectTypeDWizardPage should not be visible");
		}
		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeEPageName))
		{
			fail("ProjectTypeEWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainFPageName))
		{
			fail("ToolchainFWizardPage should not be visible");
		}
	}
	
	/**
	 *   Set the toolchain to "C_2.0.0".  Only pages with no contraints, or toolchains set to "C", or toolchains set to "C" version 2.0.0 should show up.
	 */
	public void testToolchainCv20()
	{
//		 set the project type to be "X"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.PROJECT_TYPE, "X");

		// set the toolchain to "C"
		Set toolchainSet = new LinkedHashSet();
		TestToolchain toolchain = new TestToolchain();
		toolchain.setID("C_2.0.0");
		toolchainSet.add(toolchain);
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.TOOLCHAIN, toolchainSet);
		
		// set the nature to "Z"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.NATURE, "Z");
		
		// check each of the pages
		
		// this page should always be visible
		if(!MBSCustomPageManager.isPageVisible(alwaysPresentPageName))
		{
			fail("AlwaysPresentWizardPage should be visible");
		}
		
		// next page for this page should be the page for toolchain C
		if(MBSCustomPageManager.getNextPage(alwaysPresentPageName) != MBSCustomPageManager.getPageData(toolchainCPageName).getWizardPage())
		{
			fail("AlwaysPresentWizardPage's next page should be ToolchainCWizardPage");
		}
		
		// toolchain C page's previous page should be the always present page
		if(MBSCustomPageManager.getPreviousPage(toolchainCPageName) != MBSCustomPageManager.getPageData(alwaysPresentPageName).getWizardPage())
		{
			fail("ToolchainCWizardPage's previous page should be AlwaysPresentWizardPage");
		}
		
		// Toolchain C page should be visible
		if(!MBSCustomPageManager.isPageVisible(toolchainCPageName))
		{
			fail("ToolchainCWizardPage should be visible");
		}
		
		// Toolchain C page's next page should be the page for C 2.0
		if(MBSCustomPageManager.getNextPage(toolchainCPageName) != MBSCustomPageManager.getPageData(toolchainCv20PageName).getWizardPage())
		{
			fail("ToolchainCWizardPage's next page should be ToolchainCv20WizardPage.");
		}
		
		// toolchain C v 2.0.0 page's previous page should be the toolchain C page
		if(MBSCustomPageManager.getPreviousPage(toolchainCv20PageName) != MBSCustomPageManager.getPageData(toolchainCPageName).getWizardPage())
		{
			fail("ToolchainCv20WizardPage's previous page should be ToolchainCWizardPage");
		}
		
		// Toolchain C v 2.0.0 page should be visible
		if(!MBSCustomPageManager.isPageVisible(toolchainCv20PageName))
		{
			fail("ToolchainCWizardPage should be visible");
		}
		
		// Toolchain C v 2.0.0 page's next page should be null
		if(MBSCustomPageManager.getNextPage(toolchainCv20PageName) != null)
		{
			fail("ToolchainCv20WizardPage should not have a next page.");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureAPageName))
		{
			fail("NatureAWizardPage should not be visible");
		}
		
		//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureBPageName))
		{
			fail("NatureBWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeDPageName))
		{
			fail("ProjectTypeDWizardPage should not be visible");
		}
		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeEPageName))
		{
			fail("ProjectTypeEWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainFPageName))
		{
			fail("ToolchainFWizardPage should not be visible");
		}
	}
	
	
	
	/**
	 *   Set the project type to "D".  Only pages with no contraints or project types set to "D" should show up.
	 */
	public void testProjectTypeD()
	{
//		 set the project type to be "D"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.PROJECT_TYPE, "D");

		// set the toolchain to "Y"
		Set toolchainSet = new LinkedHashSet();
		TestToolchain toolchain = new TestToolchain();
		toolchain.setID("Y");
		toolchainSet.add(toolchain);
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.TOOLCHAIN, toolchainSet);
		
		// set the nature to "Z"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.NATURE, "Z");
		
		// check each of the pages
		
		// this page should always be visible
		if(!MBSCustomPageManager.isPageVisible(alwaysPresentPageName))
		{
			fail("AlwaysPresentWizardPage should be visible");
		}
		
		// next page for this page should be the page for project type D
		if(MBSCustomPageManager.getNextPage(alwaysPresentPageName) != MBSCustomPageManager.getPageData(projectTypeDPageName).getWizardPage())
		{
			fail("AlwaysPresentWizardPage's next page should be ProjectTypeDWizardPage");
		}
		
		// Project type D page's previous page should be the always present page
		if(MBSCustomPageManager.getPreviousPage(projectTypeDPageName) != MBSCustomPageManager.getPageData(alwaysPresentPageName).getWizardPage())
		{
			fail("ProjectTypeDWizardPage's next page should be AlwaysPresentWizardPage");
		}
		
		// Project type D page should be visible
		if(!MBSCustomPageManager.isPageVisible(projectTypeDPageName))
		{
			fail("ProjectTypeDWizardPage should be visible");
		}
		
		// Project type D page's next page should be null
		if(MBSCustomPageManager.getNextPage(projectTypeDPageName) != null)
		{
			fail("ProjectTypeDWizardPage should not have a next page.");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureAPageName))
		{
			fail("NatureAWizardPage should not be visible");
		}
		
		//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureBPageName))
		{
			fail("NatureBWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCPageName))
		{
			fail("ToolChainCWizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCv20PageName))
		{
			fail("ToolChainCv20WizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeEPageName))
		{
			fail("ProjectTypeEWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainFPageName))
		{
			fail("ToolchainFWizardPage should not be visible");
		}
		
	}
	

	
	/**
	 *   Set the project type to "E".  Only pages with no contraints or project types set to "E" should show up.
	 */
	public void testProjectTypeE()
	{
//		 set the project type to be "E"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.PROJECT_TYPE, "E");

		// set the toolchain to "Y"
		Set toolchainSet = new LinkedHashSet();
		TestToolchain toolchain = new TestToolchain();
		toolchain.setID("Y");
		toolchainSet.add(toolchain);
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.TOOLCHAIN, toolchainSet);
		
		// set the nature to "Z"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.NATURE, "Z");
		
		// check each of the pages
		
		// this page should always be visible
		if(!MBSCustomPageManager.isPageVisible(alwaysPresentPageName))
		{
			fail("AlwaysPresentWizardPage should be visible");
		}
		
		// next page for this page should be the page for project type D
		if(MBSCustomPageManager.getNextPage(alwaysPresentPageName) != MBSCustomPageManager.getPageData(projectTypeEPageName).getWizardPage())
		{
			fail("AlwaysPresentWizardPage's next page should be ProjectTypeEWizardPage");
		}
		
		// Project type E page's previous page should be the always present page
		if(MBSCustomPageManager.getPreviousPage(projectTypeEPageName) != MBSCustomPageManager.getPageData(alwaysPresentPageName).getWizardPage())
		{
			fail("ProjectTypeDWizardPage's next page should be AlwaysPresentWizardPage");
		}
		
		// Project type E page should be visible
		if(!MBSCustomPageManager.isPageVisible(projectTypeEPageName))
		{
			fail("ProjectTypeDWizardPage should be visible");
		}
		
		// Project type D page's next page should be null
		if(MBSCustomPageManager.getNextPage(projectTypeEPageName) != null)
		{
			fail("ProjectTypeEWizardPage should not have a next page.");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureAPageName))
		{
			fail("NatureAWizardPage should not be visible");
		}
		
		//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureBPageName))
		{
			fail("NatureBWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCPageName))
		{
			fail("ToolChainCWizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCv20PageName))
		{
			fail("ToolChainCv20WizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeDPageName))
		{
			fail("ProjectTypeDWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainFPageName))
		{
			fail("ToolchainFWizardPage should not be visible");
		}
		
	}
	
	/**
	 *   Set the toolchain to "F".  Only pages with no contraints or toolchains set to "F" should show up.
	 */
	public void testToolchainF()
	{
//		 set the project type to be "X"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.PROJECT_TYPE, "X");

		// set the toolchain to "F"
		Set toolchainSet = new LinkedHashSet();
		TestToolchain toolchain = new TestToolchain();
		toolchain.setID("F");
		toolchainSet.add(toolchain);
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.TOOLCHAIN, toolchainSet);
		
		// set the nature to "Z"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.NATURE, "Z");
		
		// check each of the pages
		
		// this page should always be visible
		if(!MBSCustomPageManager.isPageVisible(alwaysPresentPageName))
		{
			fail("AlwaysPresentWizardPage should be visible");
		}
		
		// next page for this page should be the page for toolchain F
		if(MBSCustomPageManager.getNextPage(alwaysPresentPageName) != MBSCustomPageManager.getPageData(toolchainFPageName).getWizardPage())
		{
			fail("AlwaysPresentWizardPage's next page should be ToolchainFWizardPage");
		}
		
		// toolchain F page's previous page should be the always present page
		if(MBSCustomPageManager.getPreviousPage(toolchainFPageName) != MBSCustomPageManager.getPageData(alwaysPresentPageName).getWizardPage())
		{
			fail("ToolchainFWizardPage's previous page should be AlwaysPresentWizardPage");
		}
		
		// Toolchain C page should be visible
		if(!MBSCustomPageManager.isPageVisible(toolchainFPageName))
		{
			fail("ToolchainFWizardPage should be visible");
		}
		
		// Toolchain F page's next page should be null
		if(MBSCustomPageManager.getNextPage(toolchainFPageName) != null)
		{
			fail("ToolchainCWizardPage should not have a next page.");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureAPageName))
		{
			fail("NatureAWizardPage should not be visible");
		}
		
		//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureBPageName))
		{
			fail("NatureBWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCv20PageName))
		{
			fail("ToolChainCv20WizardPage should not be visible");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeDPageName))
		{
			fail("ProjectTypeDWizardPage should not be visible");
		}
		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeEPageName))
		{
			fail("ProjectTypeEWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainCPageName))
		{
			fail("ToolchainCWizardPage should not be visible");
		}
	}
	
	/**
	 *  Set nature to A, toolchain to C, project type to D.  We should have several pages with different constraints appearing.
	 */
	public void testMultiplePages()
	{
//		 set the project type to be "D"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.PROJECT_TYPE, "D");

		// set the toolchain to "C"
		Set toolchainSet = new LinkedHashSet();
		TestToolchain toolchain = new TestToolchain();
		toolchain.setID("C");
		toolchainSet.add(toolchain);
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.TOOLCHAIN, toolchainSet);
		
		// set the nature to "A"
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.NATURE, "A");
		
		// check each of the pages
		
		// this page should always be visible
		if(!MBSCustomPageManager.isPageVisible(alwaysPresentPageName))
		{
			fail("AlwaysPresentWizardPage should be visible");
		}
		
		// next page for this page should be the page for Nature A
		if(MBSCustomPageManager.getNextPage(alwaysPresentPageName) != MBSCustomPageManager.getPageData(natureAPageName).getWizardPage())
		{
			fail("AlwaysPresentWizardPage's next page should be NatureAWizardPage");
		}
		
		// Nature A page's previous page should be the always present page
//		 next page for this page should be the page for Nature A
		if(MBSCustomPageManager.getPreviousPage(natureAPageName) != MBSCustomPageManager.getPageData(alwaysPresentPageName).getWizardPage())
		{
			fail("NatureAWizardPage's next page should be AlwaysPresentWizardPage");
		}
	
		// Nature A page should be visible
		if(!MBSCustomPageManager.isPageVisible(natureAPageName))
		{
			fail("NatureAWizardPage should be visible");
		}
		
		// next page for this page should be the page for toolchain C
		if(MBSCustomPageManager.getNextPage(natureAPageName) != MBSCustomPageManager.getPageData(toolchainCPageName).getWizardPage())
		{
			fail("NatureAWizardPage's next page should be ToolchainCWizardPage");
		}
		
		// Toolchain C page's previous page should be NatureAWizardPage
		if(MBSCustomPageManager.getPreviousPage(toolchainCPageName) != MBSCustomPageManager.getPageData(natureAPageName).getWizardPage())
		{
			fail("ToolchainCWizardPage's previous page should be NatureAWizardPage");
		}
		
		// Toolchain C page should be visible
		if(!MBSCustomPageManager.isPageVisible(toolchainCPageName))
		{
			fail("ToolchainCWizardPage should be visible");
		}
		
		// next page for this page should be the page for project type D
		if(MBSCustomPageManager.getNextPage(toolchainCPageName) != MBSCustomPageManager.getPageData(projectTypeDPageName).getWizardPage())
		{
			fail("ToolchainCWizardPage's next page should be ProjectTypeDWizardPage");
		}
		
		// Project type D page's previous page should be the toolchain C page
		if(MBSCustomPageManager.getPreviousPage(projectTypeDPageName) != MBSCustomPageManager.getPageData(toolchainCPageName).getWizardPage())
		{
			fail("ProjectTypeDWizardPage's previous page should be toolchainCPageName");
		}
		
		// Project type D page should be visible
		if(!MBSCustomPageManager.isPageVisible(projectTypeDPageName))
		{
			fail("ProjectTypeDWizardPage should be visible");
		}
		
		// Project type D page's next page should be null
		if(MBSCustomPageManager.getNextPage(projectTypeDPageName) != null)
		{
			fail("ProjectTypeDWizardPage should not have a next page.");
		}
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(natureBPageName))
		{
			fail("NatureBWizardPage should not be visible");
		}
		
		//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(projectTypeEPageName))
		{
			fail("ProjectTypeEWizardPage should not be visible");
		}		
		
//		 the rest of the pages should be invisible
		if(MBSCustomPageManager.isPageVisible(toolchainFPageName))
		{
			fail("ToolchainFWizardPage should not be visible");
		}
	}
	
	
	public void testOperation()
	{
		MBSCustomPageManager.getPageData(alwaysPresentPageName).getOperation().run();
		
		if(testFlag != true)
		{
			fail("Running operation associated with AlwaysPresentWizardPage failed.");
		}
	}
	
	public TestCustomPageManager(String name)
	{
		
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(TestCustomPageManager.class.getName());
		
		suite.addTest(new TestCustomPageManager("testOneVisiblePage"));
		suite.addTest(new TestCustomPageManager("testNatureA"));
		suite.addTest(new TestCustomPageManager("testNatureB"));
		suite.addTest(new TestCustomPageManager("testToolchainC"));
		suite.addTest(new TestCustomPageManager("testToolchainCv20"));
		suite.addTest(new TestCustomPageManager("testProjectTypeD"));
		suite.addTest(new TestCustomPageManager("testProjectTypeE"));
		suite.addTest(new TestCustomPageManager("testToolchainF"));
		suite.addTest(new TestCustomPageManager("testMultiplePages"));
		suite.addTest(new TestCustomPageManager("testOperation"));
		return suite;
	}
	
}
