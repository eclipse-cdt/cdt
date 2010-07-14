/*******************************************************************************
 * Copyright (c) 2005, 2007 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Innoopract - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.tests;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.cdt.ui.wizards.CDTProjectWizard;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Tests for the get/setSelectedProjectType() of CProjectPlatformPage.
 * @author Elias Volanakis
 */
public class TestCProjectPlatformPage extends TestCase implements IWizardItemsListListener {

	//TODO: migrate to the new UI functionality
	private CDTProjectWizard wizard;
	private TestPage page;
	private boolean currentState=false;
	
	@Override
	protected void setUp() throws Exception {
		MBSCustomPageManager.init();
		MBSCustomPageManager.loadExtensions();
		wizard = new CDTProjectWizard();
		page = new TestPage(wizard);
		wizard.addPages();
	}
	
	@Override
	protected void tearDown() throws Exception {
		page.dispose();
		page = null;
		wizard = null;
	}
	ArrayUtil x;
	
	// testing methods
	//////////////////
	
	/* Test the new page, set selection, create page lifecycle. */
	public void testHandler1() throws Exception {
		CWizardHandler h = new CWizardHandler(getShell(), "Head", "Name");
		assertNotNull(h);
		/*
		IProjectType pt = new TestProjectType();
		assertEquals(0, h.getToolChainsCount());
		IToolchain tc = new Toolchain(new TestFolderInfo());
		IToolChain xz;
		tc.setId("test1");
		h.addTc(tc);
		// Test toolchain cannot be added
		assertEquals(h.getToolChainsCount(), 1);
		tc = new TestToolchain();
		h.addTc(tc);
		assertEquals(h.getToolChainsCount(), 2);
		IToolChain[] tcs = h.getSelectedToolChains();
		assertEquals(tcs.length, 33);
		*/
	}
	
	/* Test the new page, create page, set selection lifecycle. */
	public void testProject() throws Exception {
		
		//IPath p = 
		ResourcesPlugin.getWorkspace().getRoot().getLocation();
		/*
		NewModelProjectWizard wiz = new CDTProjectWizard();
		/*
		String s = System.getenv("TEMP");
		
		System.out.println(s);
		assertNotNull(wiz);
		/*
		IProject pr1 = wiz.createIProject("test1", null);
		assertNotNull(pr1);
		
		IProject pr2 = wiz.createIProject("test2", p.append("test2"));
		assertNotNull(pr2);
		*/
	}
	
	/* 
	 * Tests that setting the selection to a projectType thats not on the list,
	 * is handled correctly.
	 */
	public void testSelectedProjectType3() throws Exception {
	}
	
	
	// helping methods and classes
	//////////////////////////////
	
	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
	
	class TestPage extends WizardPage implements IWizardItemsListListener{
		TestPage(CDTProjectWizard wizard) throws Exception {
			super(CDTMainWizardPage.class.getName());
		}
		IProjectType getFirstType() {
			return null; //(IProjectType) projectTypes.get(0);
		}
		IProjectType getSecondType() {
			return null; //(IProjectType) projectTypes.get(1);
		}
		public boolean isCurrent() {
			// TODO Auto-generated method stub
			return false;
		}
		public void toolChainListChanged(int count) {
			// TODO Auto-generated method stub
			
		}
		public void createControl(Composite parent) {
			// TODO Auto-generated method stub
			
		}
		public List filterItems(List items) {
			return items;
		}
	}

	// methods of IToolChainListListener
	public boolean isCurrent() { return currentState; }
	public void toolChainListChanged(int count) {}

	public List filterItems(List items) {
		return items;
	}
}
