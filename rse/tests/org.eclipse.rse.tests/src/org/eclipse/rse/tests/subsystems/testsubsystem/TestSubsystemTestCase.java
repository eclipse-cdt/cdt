/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tobias Schwarz (Wind River) - initial API and implementation.
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.testsubsystem;

import junit.framework.AssertionFailedError;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.ui.view.SystemView;
import org.eclipse.rse.internal.ui.view.SystemViewPart;
import org.eclipse.rse.tests.core.IRSEViews;
import org.eclipse.rse.tests.core.RSEWaitAndDispatchUtil;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;
import org.eclipse.rse.tests.testsubsystem.TestSubSystemContainerNode;
import org.eclipse.rse.tests.testsubsystem.TestSubSystemNode;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystem;
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

		// all view management must happen in the UI thread!
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
		//-test-author-:TobiasSchwarz
		if (isTestDisabled())
			return;
		// these test _must_ run in UI thread
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				internalTestAddAndDeleteNodes(true);
			}
		});
	}

	public void testAddAndDeleteFlatNodes() {
		//-test-author-:TobiasSchwarz
		if (isTestDisabled())
			return;
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
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
        registry.invalidateFiltersFor(testSubSystem);

		TestSubSystemContainerNode node = null;
		for (int i=0; i<100; i++) {
			if (node == null) {
				node = new TestSubSystemContainerNode("node "+i); //$NON-NLS-1$
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

		testSubSystem.removeAllChildNodes();
		//registry.invalidateFiltersFor(testSubSystem);

		//SystemPerspectiveHelpers.findRSEView().refresh(testSubSystem);
		registry.fireEvent(new SystemResourceChangeEvent(testSubSystem, ISystemResourceChangeEvents.EVENT_REFRESH, testSubSystem));
		RSEWaitAndDispatchUtil.waitAndDispatch(1000);
	}

	public void testBugzilla170728() {
		//-test-author-:TobiasSchwarz
		if (isTestDisabled())
			return;
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

		RSECorePlugin.getTheSystemRegistry().invalidateFiltersFor(testSubSystem);
		rseSystemView.refresh(testSubSystem);
		RSEWaitAndDispatchUtil.waitAndDispatch(1000);
		rseSystemView.expandToLevel(testSubSystem, AbstractTreeViewer.ALL_LEVELS);
		RSEWaitAndDispatchUtil.waitAndDispatch(1000);

		ISystemFilterPoolManager mgr = testSubSystem.getFilterPoolReferenceManager().getDefaultSystemFilterPoolManager();
		String[] strings = new String[] { "Node.*" }; //$NON-NLS-1$

		try {
			mgr.createSystemFilter(mgr.getFirstDefaultSystemFilterPool(), "Node*", strings, "Node*"); //$NON-NLS-1$ //$NON-NLS-2$

			TestSubSystemNode node = new TestSubSystemNode("Node 1"); //$NON-NLS-1$
			testSubSystem.addChildNode(node);
			testSubSystem.addChildNode(new TestSubSystemNode("Node 2")); //$NON-NLS-1$
			testSubSystem.addChildNode(new TestSubSystemNode("Node 3")); //$NON-NLS-1$
			testSubSystem.addChildNode(new TestSubSystemNode("Node 4")); //$NON-NLS-1$

			RSECorePlugin.getTheSystemRegistry().invalidateFiltersFor(testSubSystem);
			rseSystemView.refresh(testSubSystem);

			RSEWaitAndDispatchUtil.waitAndDispatch(1000);
			rseSystemView.expandToLevel(testSubSystem, AbstractTreeViewer.ALL_LEVELS);
			rseSystemView.refresh(testSubSystem);
			RSEWaitAndDispatchUtil.waitAndDispatch(1000);

			node.setName("Node 1 (changed)"); //$NON-NLS-1$

			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			registry.fireEvent(new SystemResourceChangeEvent(node, ISystemResourceChangeEvents.EVENT_REFRESH, node));

			RSEWaitAndDispatchUtil.waitAndDispatch(10000);
		}
		catch (Exception e) {
			//We cannot have the Exception forwarded to the test framework, because
			//it happens in the dispatch thread and our Runnable.run() method does
			//not allow checked exceptions. Therefore, convert the exception into
			//an Error that the test framework can handle, but make sure that the
			//cause of the Error (the original exception) is maintained by calling
			//initCause(). This will allow seeing it in the JUnit runner later on.
			Error err = new AssertionFailedError("Unhandled event loop exception");
			err.initCause(e);
			throw err;
		}
	}
}
