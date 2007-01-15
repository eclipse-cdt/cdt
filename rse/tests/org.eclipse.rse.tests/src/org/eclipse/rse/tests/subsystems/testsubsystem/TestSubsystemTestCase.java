/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Tobias Schwarz (Wind River) - initial API and implementation.
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.testsubsystem;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.core.SystemPerspectiveHelpers;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.core.RSEWaitAndDispatchUtil;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;
import org.eclipse.rse.tests.testsubsystem.TestSubSystemContainerNode;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystem;

public class TestSubsystemTestCase extends RSEBaseConnectionTestCase {
	private ITestSubSystem testSubSystem;
	
	public void testAddAndDeleteDeepNodes() {
		if (!RSETestsPlugin.isTestCaseEnabled("TestSubsystemTestCase.testAddAndDeleteDeepNodes")) return; //$NON-NLS-1$
		testAddAndDeleteNodes(true);
	}
		
	public void testAddAndDeleteFlatNodes() {
		if (!RSETestsPlugin.isTestCaseEnabled("TestSubsystemTestCase.testAddAndDeleteFlatNodes")) return; //$NON-NLS-1$
		testAddAndDeleteNodes(false);
	}
			
	public void testAddAndDeleteNodes(boolean deep) {
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
		SystemPerspectiveHelpers.findRSEView().refresh(testSubSystem);
		SystemPerspectiveHelpers.findRSEView().expandToLevel(testSubSystem, AbstractTreeViewer.ALL_LEVELS);
		RSEWaitAndDispatchUtil.waitAndDispatch(1000);
		
		SystemPerspectiveHelpers.findRSEView().setSelection(new StructuredSelection(node));

		ISelection selection = SystemPerspectiveHelpers.findRSEView().getSelection();
		assertTrue("missing selection", selection != null); //$NON-NLS-1$
		assertTrue("not a structured selection", selection instanceof IStructuredSelection); //$NON-NLS-1$
		IStructuredSelection structSel = (IStructuredSelection)selection;
		assertEquals("invalid number of selected items", 1, structSel.size()); //$NON-NLS-1$
		assertEquals("wrong item selected", node, structSel.getFirstElement()); //$NON-NLS-1$

		testSubSystem.removeAllChildNodes();

		SystemPerspectiveHelpers.findRSEView().refresh(testSubSystem);
		RSEWaitAndDispatchUtil.waitAndDispatch(1000);
		
		SystemPerspectiveHelpers.findRSEView().setSelection(new StructuredSelection(firstNode));

		 selection = SystemPerspectiveHelpers.findRSEView().getSelection();
		assertTrue("missing selection", selection != null); //$NON-NLS-1$
		assertTrue("not a structured selection", selection instanceof IStructuredSelection); //$NON-NLS-1$
		 structSel = (IStructuredSelection)selection;
		assertEquals("invalid number of selected items", 0, structSel.size()); //$NON-NLS-1$
	}
}
