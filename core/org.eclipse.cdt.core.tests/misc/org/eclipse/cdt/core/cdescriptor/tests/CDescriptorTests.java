/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems Ltd and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author David
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CDescriptorTests extends TestCase {

	static String projectId = CTestPlugin.PLUGIN_ID + ".TestProject";
	static IProject fProject;
	static CDescriptorListener listener = new CDescriptorListener();
	static CDescriptorEvent fLastEvent;

	/**
	 * Constructor for CDescriptorTest.
	 * 
	 * @param name
	 */
	public CDescriptorTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CDescriptorTests.class.getName());

		suite.addTest(new CDescriptorTests("testDescriptorCreation"));
		suite.addTest(new CDescriptorTests("testDescriptorOwner"));
		suite.addTest(new CDescriptorTests("testExtensionCreation"));
		suite.addTest(new CDescriptorTests("testExtensionGet"));
		suite.addTest(new CDescriptorTests("testExtensionData"));
		suite.addTest(new CDescriptorTests("testExtensionRemove"));
		suite.addTest(new CDescriptorTests("testProjectDataCreate"));
		suite.addTest(new CDescriptorTests("testProjectDataDelete"));

		TestSetup wrapper = new TestSetup(suite) {

			protected void setUp() throws Exception {
				oneTimeSetUp();
			}

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

		public void descriptorChanged(CDescriptorEvent event) {
			fLastEvent = event;
		}
	}

	static void oneTimeSetUp() throws Exception {
		CTestPlugin.getWorkspace().run(new IWorkspaceRunnable() {

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
}