/*******************************************************************************
 * Copyright (c) 2005 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Innoopract - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.CProjectPlatformPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewManagedProjectWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Tests for the get/setSelectedProjectType() of CProjectPlatformPage.
 * @author Elias Volanakis
 */
public class TestCProjectPlatformPage extends TestCase {
	
	private NewManagedProjectWizard wizard;
	private TestPage page;
	
	protected void setUp() throws Exception {
		MBSCustomPageManager.init();
		MBSCustomPageManager.loadExtensions();
		wizard = new NewManagedProjectWizard();
		page = new TestPage(wizard);
		wizard.addPages();
	}
	
	protected void tearDown() throws Exception {
		page.dispose();
		page = null;
		wizard = null;
	}
	
	
	// testing methods
	//////////////////
	
	/* Test the new page, set selection, create page lifecycle. */
	public void testSelectedProjectType1() throws Exception {
		page.createControl(getShell());
		final IProjectType type2 = page.getSecondType();
		
		TestPage page2 = new TestPage(wizard);
		page2.setSelectedProjectType(type2);
		page2.createControl(getShell());
		assertEquals(type2, page2.getSelectedProjectType());
		page2.dispose();
	}
	
	/* Test the new page, create page, set selection lifecycle. */
	public void testSelectedProjectType2() throws Exception {
		// test get null
		assertNull(page.getSelectedProjectType());
		// test set null
		page.setSelectedProjectType(null);
		assertNull(page.getSelectedProjectType()); // null, since no UI created

		// create ui
		page.createControl(getShell());
		final IProjectType type1 = page.getFirstType();
		
		// default behavior if selection set to null -> select first item 
		assertEquals(type1, page.getSelectedProjectType());
		// set 2nd element from project types list
		final IProjectType type2 = page.getSecondType();
		assertNotNull(type2);
		page.setSelectedProjectType(type2);
		assertEquals(type2, page.getSelectedProjectType());
	}
	
	/* 
	 * Tests that setting the selection to a projectType thats not on the list,
	 * is handled correctly.
	 */
	public void testSelectedProjectType3() throws Exception {
		IProjectType testType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testgnu21.so");
		assertNotNull(testType);
		assertTrue(testType.isTestProjectType());
		page.setSelectedProjectType(testType);
		page.createControl(getShell());
		// no selection made
		assertNull(null,page.getSelectedProjectType());
		assertFalse(page.canFlipToNextPage());
	}
	
	
	// helping methods and classes
	//////////////////////////////
	
	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
	
	class TestPage extends CProjectPlatformPage {
		TestPage(NewManagedProjectWizard wizard) throws Exception {
			super(TestCProjectPlatformPage.class.getName(), wizard);
		}
		IProjectType getFirstType() {
			return (IProjectType) projectTypes.get(0);
		}
		IProjectType getSecondType() {
			return (IProjectType) projectTypes.get(1);
		}
	}
	
}
