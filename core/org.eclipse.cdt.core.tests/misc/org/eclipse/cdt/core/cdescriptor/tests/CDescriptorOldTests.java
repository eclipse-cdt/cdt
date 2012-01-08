/**********************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems Ltd - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     James Blackburn (Broadcom Corp)
 ***********************************************************************/

package org.eclipse.cdt.core.cdescriptor.tests;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CDescriptorEvent;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorListener;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class exists because the tests in CDescriptorTests
 * are not fixed.
 * This class corresponds to the version of 
 * CDescrptorTests before the changes made in cdt.core 5.1
 * (CVS version 1.12)
 */
public class CDescriptorOldTests extends TestCase {

	static String projectId = CTestPlugin.PLUGIN_ID + ".TestProject";
	static IProject fProject;
	static CDescriptorListener listener = new CDescriptorListener();
	static CDescriptorEvent fLastEvent;

	/**
	 * Constructor for CDescriptorTest.
	 * 
	 * @param name
	 */
	public CDescriptorOldTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CDescriptorOldTests.class.getName());

		suite.addTest(new CDescriptorOldTests("testDescriptorCreation"));
		suite.addTest(new CDescriptorOldTests("testDescriptorOwner"));
		suite.addTest(new CDescriptorOldTests("testExtensionCreation"));
		suite.addTest(new CDescriptorOldTests("testExtensionGet"));
		suite.addTest(new CDescriptorOldTests("testExtensionData"));
		suite.addTest(new CDescriptorOldTests("testExtensionRemove"));
		suite.addTest(new CDescriptorOldTests("testProjectDataCreate"));
		suite.addTest(new CDescriptorOldTests("testProjectDataDelete"));
		suite.addTest(new CDescriptorOldTests("testConcurrentDescriptorCreation"));
		suite.addTest(new CDescriptorOldTests("testConcurrentDescriptorCreation2"));
		suite.addTest(new CDescriptorOldTests("testDeadlockDuringProjectCreation"));
		suite.addTest(new CDescriptorOldTests("testProjectStorageDelete"));
		
		TestSetup wrapper = new TestSetup(suite) {

			@Override
			protected void setUp() throws Exception {
				oneTimeSetUp();
			}

			@Override
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}

		};
		return wrapper;
	}

	private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}

	static public class CDescriptorListener implements ICDescriptorListener {

		@Override
		public void descriptorChanged(CDescriptorEvent event) {
			fLastEvent = event;
		}
	}

	static void oneTimeSetUp() throws Exception {
		CTestPlugin.getWorkspace().run(new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspaceRoot root = CTestPlugin.getWorkspace().getRoot();
				IProject project = root.getProject("testDescriptorProject");
				if (!project.exists()) {
					project.create(null);
				} else {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				}
				if (!project.isOpen()) {
					project.open(null);
				}
				CCorePlugin.getDefault().getCDescriptorManager().addDescriptorListener(listener);
				if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
					addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
				}
				fProject = project;
			}
		}, null);
	}

	static void oneTimeTearDown() throws Exception {
		fProject.delete(true, true, null);
	}

	public void testDescriptorCreation() throws Exception {
		CTestPlugin.getWorkspace().run(new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				CCorePlugin.getDefault().mapCProjectOwner(fProject, projectId, false);
			}
		}, null);
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_ADDED);
		Assert.assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;

		Assert.assertEquals(fProject, desc.getProject());
		Assert.assertEquals("*", desc.getPlatform());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185930
	public void testConcurrentDescriptorCreation() throws Exception {
		fProject.close(null);
		fProject.open(null);
		Thread t= new Thread() {
			@Override
			public void run() {
				try {
					CCorePlugin.getDefault().getCProjectDescription(fProject, true);
				} catch (CoreException exc) {
				}
			}
		};
		t.start();
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		t.join();
		
		Element data = desc.getProjectData("testElement0");
		data.appendChild(data.getOwnerDocument().createElement("test"));
		desc.saveProjectData();
		fLastEvent = null;
 	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185930
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=193503
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=196118
	public void testConcurrentDescriptorCreation2() throws Exception {
		int lastLength = 0;
		for (int i=0; i<200; ++i) {
			final int indexi = i;
			PDOMManager pdomMgr= (PDOMManager)CCorePlugin.getIndexManager();
			pdomMgr.shutdown();
			fProject.close(null);
			fProject.open(null);
			pdomMgr.startup().schedule();
			ICDescriptor desc= CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			if (lastLength == 0)
				lastLength = countChildElements(desc.getProjectData("testElement"));
			final Throwable[] exception= new Throwable[10];
			Thread[] threads= new Thread[10];
			for (int j = 0; j < 10; j++) {
				final int indexj = j;
				Thread t= new Thread() {
					@Override
					public void run() {
						try {
							ICDescriptorOperation operation= new ICDescriptorOperation() {
								@Override
								public void execute(ICDescriptor descriptor, IProgressMonitor monitor) throws CoreException {
									assertFalse(descriptor.getConfigurationDescription().isReadOnly());
									Element data = descriptor.getProjectData("testElement");
									String test = "test"+(indexi*10 + indexj);
									data.appendChild(data.getOwnerDocument().createElement(test));
									assertFalse(descriptor.getConfigurationDescription().isReadOnly());
									// BUG196118 the model cached in memory doesn't reflect the contents of .cproject
									//
									// descriptor.saveProjectData() doesn't actually save despite what the API says
									// see CConfigBasedDescriptor.fApplyOnChange
//									((CConfigBasedDescriptor)descriptor).apply(false);
//									System.out.println("Saved " + test);
								}};
								CCorePlugin.getDefault().getCDescriptorManager().runDescriptorOperation(fProject, operation, null);
								ICDescriptor descriptor = CCorePlugin.getDefault().getCDescriptorManager().getDescriptor(fProject);
								// perform apply outside descriptor operation to avoid deadlock - http://bugs.eclipse.org/241288 
								descriptor.saveProjectData();
						} catch (Throwable exc) {
							exception[indexj]= exc;
							exc.printStackTrace();
						}
					}
				};
				t.start();
				threads[j] = t;
			}
			for (int j = 0; j < threads.length; j++) {
				if (threads[j] != null) {
					threads[j].join();
				}
				assertNull("Exception occurred: "+exception[j], exception[j]);
			}
			desc= CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			int lengthAfter = countChildElements(desc.getProjectData("testElement"));
			lastLength += threads.length; // Update last lengths to what we expect
			assertEquals("Iteration count: " + i, lastLength, lengthAfter);

			fLastEvent = null;
		}
	}

	/**
	 * Count the number of Node.ELEMENT_NODE elements which are a 
	 * direct descendent of the parent Element.
	 * Other nodes (e.g. Text) are ignored
	 * @param parent
	 * @return
	 */
	private int countChildElements(Element parent) {
		int numElements = 0;
		NodeList childNodes = parent.getChildNodes();
		for (int k = 0 ; k < childNodes.getLength() ; k++)
			if (childNodes.item(k).getNodeType() == Node.ELEMENT_NODE)
				numElements ++;
		return numElements;
	}

	public void testDeadlockDuringProjectCreation() throws Exception {
		for (int i=0; i < 10; ++i) {
			oneTimeTearDown();
			oneTimeSetUp();
			Thread t= new Thread() {
				@Override
				public void run() {
					try {
						ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
						Element data = desc.getProjectData("testElement0");
						data.appendChild(data.getOwnerDocument().createElement("test"));
						desc.saveProjectData();
					} catch (CoreException exc) {
					}
				}
			};
			t.start();

			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			Element data = desc.getProjectData("testElement0");
			data.appendChild(data.getOwnerDocument().createElement("test"));
			desc.saveProjectData();
			t.join();
			
			fLastEvent = null;
		}
 	}

	public void testDescriptorOwner() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICOwnerInfo owner = desc.getProjectOwner();
		Assert.assertEquals(projectId, owner.getID());
		Assert.assertEquals("*", owner.getPlatform());
		Assert.assertEquals("C/C++ Test Project", owner.getName());
	}

	public void testDescriptorConversion() {

	}

	public void testExtensionCreation() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICExtensionReference extRef = desc.create("org.eclipse.cdt.testextension", "org.eclipse.cdt.testextensionID");

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		Assert.assertEquals(fLastEvent.getFlags(), CDescriptorEvent.EXTENSION_CHANGED);
		fLastEvent = null;

		Assert.assertEquals("org.eclipse.cdt.testextension", extRef.getExtension());
		Assert.assertEquals("org.eclipse.cdt.testextensionID", extRef.getID());
	}

	public void testExtensionGet() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICExtensionReference extRef[] = desc.get("org.eclipse.cdt.testextension");

		Assert.assertEquals("org.eclipse.cdt.testextension", extRef[0].getExtension());
		Assert.assertEquals("org.eclipse.cdt.testextensionID", extRef[0].getID());
	}

	public void testExtensionData() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICExtensionReference extRef[] = desc.get("org.eclipse.cdt.testextension");
		extRef[0].setExtensionData("testKey", "testValue");

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		Assert.assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;

		Assert.assertEquals("testValue", extRef[0].getExtensionData("testKey"));
		extRef[0].setExtensionData("testKey", null);
		Assert.assertEquals(null, extRef[0].getExtensionData("testKey"));
	}

	public void testExtensionRemove() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICExtensionReference extRef[] = desc.get("org.eclipse.cdt.testextension");
		desc.remove(extRef[0]);

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		Assert.assertEquals(fLastEvent.getFlags(), CDescriptorEvent.EXTENSION_CHANGED);
		fLastEvent = null;

	}

	public void testProjectDataCreate() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		Element data = desc.getProjectData("testElement");
		data.appendChild(data.getOwnerDocument().createElement("test"));
		desc.saveProjectData();

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		Assert.assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;
	}

	public void testProjectDataDelete() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		Element data = desc.getProjectData("testElement");
		NodeList list = data.getElementsByTagName("test");
		Assert.assertEquals(1, list.getLength());
		data.removeChild(data.getFirstChild());
		desc.saveProjectData();

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		Assert.assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;
	}

	public void testProjectStorageDelete() throws Exception {
		// 1st Add an item
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		Element data = desc.getProjectData("testElement");
		data.appendChild(data.getOwnerDocument().createElement("test"));
		desc.saveProjectData();

		// 2nd remove the storage element containing it
		desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		data = desc.getProjectData("testElement");
		data.getParentNode().removeChild(data);
		desc.saveProjectData();

		// 3rd check the item no longer exists
		desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		data = desc.getProjectData("testElement");
		assertTrue(data.getChildNodes().getLength() == 0);		
		fLastEvent = null;
	}

}
