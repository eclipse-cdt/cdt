/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Tobias Schwarz (Wind River) - initial API and implementation.
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.testsubsystem;

import java.util.Vector;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.ui.view.SystemView;
import org.eclipse.rse.internal.ui.view.SystemViewPart;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemRegistry;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.core.IRSEViews;
import org.eclipse.rse.tests.core.RSEWaitAndDispatchUtil;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;
import org.eclipse.rse.tests.testsubsystem.TestSubSystemContainerNode;
import org.eclipse.rse.tests.testsubsystem.TestSubSystemNode;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystem;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

public class TestSubsystemTestCase extends RSEBaseConnectionTestCase {
	private ITestSubSystem testSubSystem;
	SystemView rseSystemView;
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		// all view managment must happen in the UI thread!
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				// For the test subsystem test case we need the RSE remote systems view
				IViewPart viewPart = showView(IRSEViews.RSE_REMOTE_SYSTEMS_VIEW_ID, IRSEViews.RSE_PERSPECTIVE_ID);
				assertNotNull("Failed to show required RSE remote systems view!", viewPart); //$NON-NLS-1$
				if (viewPart instanceof SystemViewPart) {
					rseSystemView = ((SystemViewPart)viewPart).getSystemView();
				}
			}
		});
		assertNotNull("Failed to get remote system viewer instance from RSE remote systems view!", rseSystemView); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		rseSystemView = null;
		testSubSystem = null;
		
		super.tearDown();
	}
	
	public void testAddAndDeleteDeepNodes() {
		if (!RSETestsPlugin.isTestCaseEnabled("TestSubsystemTestCase.testAddAndDeleteDeepNodes")) return; //$NON-NLS-1$
		// these test _must_ run in UI thread
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				internalTestAddAndDeleteNodes(true);
			}
		});
	}
		
	public void testAddAndDeleteFlatNodes() {
		if (!RSETestsPlugin.isTestCaseEnabled("TestSubsystemTestCase.testAddAndDeleteFlatNodes")) return; //$NON-NLS-1$
		// these test _must_ run in UI thread
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				internalTestAddAndDeleteNodes(false);
			}
		});
	}
			
	public void internalTestAddAndDeleteNodes(boolean deep) {
		IHost	connection = getLocalSystemConnection();
		assertNotNull("Failed to get local system connection", connection); //$NON-NLS-1$
		
		Exception exception = null;
		String cause = null;
		
		testSubSystem = null;
		try {
			testSubSystem = getConnectionManager().getTestSubSystem(connection);
		} catch(Exception e) {
			exception = e;
			cause = e.getLocalizedMessage();
		}
		assertNull("Failed to get test subsystem! Possible cause: " + cause, exception); //$NON-NLS-1$
		assertNotNull("No test subystem", testSubSystem); //$NON-NLS-1$

		testSubSystem.removeAllChildNodes();
		SystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
        registry.invalidateFiltersFor(testSubSystem);
        
		TestSubSystemContainerNode firstNode = null;
		TestSubSystemContainerNode node = null;
		for (int i=0; i<100; i++) {
			if (node == null) {
				node = new TestSubSystemContainerNode("node "+i); //$NON-NLS-1$
				firstNode = node;
				testSubSystem.addChildNode(node);
			}
			else {
				TestSubSystemContainerNode parentNode = node;
				node = new TestSubSystemContainerNode("node "+i); //$NON-NLS-1$
				if (deep) {
					parentNode.addChildNode(node);
				}
				else {
					testSubSystem.addChildNode(node);
				}
			}
		}
		rseSystemView.refresh(testSubSystem);
		rseSystemView.expandToLevel(testSubSystem, AbstractTreeViewer.ALL_LEVELS);
		RSEWaitAndDispatchUtil.waitAndDispatch(1000);
		
		rseSystemView.setSelection(new StructuredSelection(node));

		ISelection selection = rseSystemView.getSelection();
		assertTrue("missing selection", selection != null); //$NON-NLS-1$
		assertTrue("not a structured selection", selection instanceof IStructuredSelection); //$NON-NLS-1$
		IStructuredSelection structSel = (IStructuredSelection)selection;
		assertEquals("invalid number of selected items", 1, structSel.size()); //$NON-NLS-1$
		assertEquals("wrong item selected", node, structSel.getFirstElement()); //$NON-NLS-1$

		testSubSystem.removeAllChildNodes();
		//registry.invalidateFiltersFor(testSubSystem);
		 
		//SystemPerspectiveHelpers.findRSEView().refresh(testSubSystem);
		registry.fireEvent(new SystemResourceChangeEvent(testSubSystem, ISystemResourceChangeEvents.EVENT_REFRESH, testSubSystem));
		RSEWaitAndDispatchUtil.waitAndDispatch(1000);
		
		rseSystemView.setSelection(new StructuredSelection(firstNode));

		 selection = rseSystemView.getSelection();
		assertTrue("missing selection", selection != null); //$NON-NLS-1$
		assertTrue("not a structured selection", selection instanceof IStructuredSelection); //$NON-NLS-1$
		 structSel = (IStructuredSelection)selection;
		assertEquals("invalid number of selected items", 0, structSel.size()); //$NON-NLS-1$
	}
	
	public void testBugzilla170728() {
		if (!RSETestsPlugin.isTestCaseEnabled("TestSubsystemTestCase.testBugzilla170728")) return; //$NON-NLS-1$
		// these test _must_ run in UI thread
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				internalTestBugzilla170728();
			}
		});
	}

	public void internalTestBugzilla170728() {
		IHost	connection = getLocalSystemConnection();
		assertNotNull("Failed to get local system connection", connection); //$NON-NLS-1$
		
		Exception exception = null;
		String cause = null;
		
		testSubSystem = null;
		try {
			testSubSystem = getConnectionManager().getTestSubSystem(connection);
		} catch(Exception e) {
			exception = e;
			cause = e.getLocalizedMessage();
		}
		assertNull("Failed to get test subsystem! Possible cause: " + cause, exception); //$NON-NLS-1$
		assertNotNull("No test subystem", testSubSystem); //$NON-NLS-1$

		RSEUIPlugin.getTheSystemRegistry().invalidateFiltersFor(testSubSystem);
		rseSystemView.refresh(testSubSystem);
		RSEWaitAndDispatchUtil.waitAndDispatch(1000);
		rseSystemView.expandToLevel(testSubSystem, AbstractTreeViewer.ALL_LEVELS);
		RSEWaitAndDispatchUtil.waitAndDispatch(1000);

		ISystemFilterPoolManager mgr = testSubSystem.getFilterPoolReferenceManager().getDefaultSystemFilterPoolManager();
		Vector strings = new Vector();
		strings.add("Node.*"); //$NON-NLS-1$

		try {
			mgr.createSystemFilter(mgr.getFirstDefaultSystemFilterPool(), "Node*", strings, "Node*"); //$NON-NLS-1$ //$NON-NLS-2$
			
			TestSubSystemNode node = new TestSubSystemNode("Node 1"); //$NON-NLS-1$
			testSubSystem.addChildNode(node);
			testSubSystem.addChildNode(new TestSubSystemNode("Node 2")); //$NON-NLS-1$
			testSubSystem.addChildNode(new TestSubSystemNode("Node 3")); //$NON-NLS-1$
			testSubSystem.addChildNode(new TestSubSystemNode("Node 4")); //$NON-NLS-1$
			
			RSEUIPlugin.getTheSystemRegistry().invalidateFiltersFor(testSubSystem);
			rseSystemView.refresh(testSubSystem);
			
			RSEWaitAndDispatchUtil.waitAndDispatch(1000);
			rseSystemView.expandToLevel(testSubSystem, AbstractTreeViewer.ALL_LEVELS);
			rseSystemView.refresh(testSubSystem);
			RSEWaitAndDispatchUtil.waitAndDispatch(1000);
			
			node.setName("Node 1 (changed)"); //$NON-NLS-1$

			SystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			registry.fireEvent(new SystemResourceChangeEvent(node, ISystemResourceChangeEvents.EVENT_REFRESH, node));
			
			RSEWaitAndDispatchUtil.waitAndDispatch(10000);
		}
		catch (Exception e) {
			assertNull(e.getMessage(), e);
		}
	}
}
