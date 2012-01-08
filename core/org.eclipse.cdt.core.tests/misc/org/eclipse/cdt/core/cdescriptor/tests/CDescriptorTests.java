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
 *     James Blackburn (Broadcom Corp.)
 ***********************************************************************/

package org.eclipse.cdt.core.cdescriptor.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CDescriptorEvent;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorListener;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author David
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CDescriptorTests extends BaseTestCase {

	static String projectId = CTestPlugin.PLUGIN_ID + ".TestProject";
	static IProject fProject;
	static CDescriptorListener listener = new CDescriptorListener();
	static volatile CDescriptorEvent fLastEvent;

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

		// Add all the tests in this class
		for (Method m : CDescriptorTests.class.getMethods())
			if (m.getName().startsWith("test"))
				suite.addTest(new CDescriptorTests(m.getName()));

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
	
	@Override
	protected void setUp() throws Exception {
		fProject.open(new NullProgressMonitor());
	}

	@Override
	protected void tearDown() throws Exception {
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

	public void testDescriptorOwner() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICOwnerInfo owner = desc.getProjectOwner();
		Assert.assertEquals(projectId, owner.getID());
		Assert.assertEquals("*", owner.getPlatform());
		Assert.assertEquals("C/C++ Test Project", owner.getName());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185930
	public void testConcurrentDescriptorCreation() throws Exception {
		for (int i = 0; i < 100 ; i++) {
			fProject.close(null);
			fProject.open(null);
			Thread t= new Thread() {
				@Override
				public void run() {
					try {
						CCorePlugin.getDefault().getCProjectDescription(fProject, true);
					} catch (CoreException exc) {
						fail();
					}
				}
			};
			t.start();
			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			t.join();
			
			ICStorageElement data = desc.getProjectStorageElement("testElement0");
			data.createChild("test");
			desc.saveProjectData();
			fLastEvent = null;
		}
 	}

	/*
	 * This tests concurrent descriptor modification inside of a ICDescriptor operation run
	 * with 
	 * CConfigBasedDescriptorManager.runDescriptorOperation(IProject project, ICDescriptorOperation op, IProgressMonitor monitor)
	 */
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=185930
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=193503
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=196118
	public void testConcurrentDescriptorModification() throws Exception {
		int lastLength = 0;
		for (int i=0; i<100; ++i) {
			final int indexi = i;
			PDOMManager pdomMgr= (PDOMManager)CCorePlugin.getIndexManager();
			pdomMgr.shutdown();
			fProject.close(null);
			fProject.open(null);
			pdomMgr.startup().schedule();
			final ICDescriptor fdesc= CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			if (lastLength == 0)
				lastLength = fdesc.getProjectStorageElement("testElement").getChildren().length;
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
//									assertFalse(descriptor.getConfigurationDescription().isReadOnly());
									ICStorageElement data = fdesc.getProjectStorageElement("testElement");
									String test = "test"+(indexi*10 + indexj);
									data.createChild(test);
//									assertFalse(descriptor.getConfigurationDescription().isReadOnly());
									descriptor.saveProjectData();
//									System.out.println("Saved " + test);
								}};
								CCorePlugin.getDefault().getCDescriptorManager().runDescriptorOperation(fProject, operation, null);
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
			ICDescriptor desc= CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			int lengthAfter = desc.getProjectStorageElement("testElement").getChildren().length;
			lastLength += threads.length; // Update last lengths to what we expect
			assertEquals("Iteration count: " + i, lastLength, lengthAfter);

			fLastEvent = null;
		}
	}

	/*
	 * This test should pass as two threads, operating on the 
	 * different storage elements  (outside of an operation) should be safe
	 */
	public void testConcurrentDifferentStorageElementModification() throws Exception {
		for (int i=0; i < 100; ++i) {
			Thread t= new Thread() {
				@Override
				public void run() {
					try {
						ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
						ICStorageElement data = desc.getProjectStorageElement("testElement4");
						data.createChild("test");
						desc.saveProjectData();
					} catch (CoreException exc) {
						fail(exc.getMessage());
					}
				}
			};
			t.start();

			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			ICStorageElement data = desc.getProjectStorageElement("testElement5");
			data.createChild("test");
			desc.saveProjectData();
			t.join();

			fLastEvent = null;
		}
		Assert.assertEquals(100, CCorePlugin.getDefault().getCProjectDescription(fProject, false).getProjectStorageElement("testElement4").getChildren().length);
		Assert.assertEquals(100, CCorePlugin.getDefault().getCProjectDescription(fProject, false).getProjectStorageElement("testElement5").getChildren().length);
 	}

	/*
	 * Test that (non-structural) changes to the storage element tree
	 * work as expected.
	 */
	public void testConcurrentSameStorageElementModification() throws Exception {
		for (int i=0; i < 100; ++i) {
			Thread t= new Thread() {
				@Override
				public void run() {
					try {
						ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
						ICStorageElement data = desc.getProjectStorageElement("testElement6");
						data.createChild("test");
						desc.saveProjectData();
					} catch (CoreException exc) {
						fail(exc.getMessage());
					}
				}
			};
			t.start();

			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			ICStorageElement data = desc.getProjectStorageElement("testElement6");
			data.createChild("test");
			desc.saveProjectData();
			t.join();
			
			fLastEvent = null;
		}
		Assert.assertEquals(200, CCorePlugin.getDefault().getCProjectDescription(fProject, false).getProjectStorageElement("testElement6").getChildren().length);
 	}

	/*
	 * Tests deadlock when accessing c project description concurrently from two threads
	 */
	public void testDeadlockDuringProjectCreation() throws Exception {
		for (int i=0; i < 10; ++i) {
			oneTimeTearDown();
			oneTimeSetUp();
			Thread t= new Thread() {
				@Override
				public void run() {
					try {
						ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
						ICStorageElement data = desc.getProjectStorageElement("testElement0");
						data.createChild("test");
						desc.saveProjectData();
					} catch (Exception exc) {
					}
				}
			};
			t.start();

			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			ICStorageElement data = desc.getProjectStorageElement("testElement0");
			data.createChild("test");
			desc.saveProjectData();
			t.join();
			
			fLastEvent = null;
		}
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
		ICStorageElement data = desc.getProjectStorageElement("testElement");
		data.createChild("test");
		desc.saveProjectData();

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		Assert.assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;
	}

	public void testProjectDataDelete() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICStorageElement data = desc.getProjectStorageElement("testElement");
		ICStorageElement[] list = data.getChildrenByName("test");
		Assert.assertEquals(1, list.length);
		data.removeChild(list[0]);
		desc.saveProjectData();

		Assert.assertNotNull(fLastEvent);
		Assert.assertEquals(fLastEvent.getDescriptor(), desc);
		Assert.assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		Assert.assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;
	}

	public void testCProjectDescriptionDescriptorInteraction() throws Exception {
		for (int i = 1; i < 100 ; i++) {
			// Create a descriptor with some test data
			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			ICStorageElement data = desc.getProjectStorageElement("descDescInteraction");
			data.createChild("dataItem1");

			// Get the CProjectDescription
			ICProjectDescription projDesc = CCorePlugin.getDefault().getProjectDescription(fProject);
			data = desc.getProjectStorageElement("descDescInteraction");
			data.createChild("dataItem2");
			data = desc.getProjectStorageElement("descDescInteraction2");
			data.createChild("dataItem3");

			// save the descriptor
			desc.saveProjectData();
			// save the project description
			CCorePlugin.getDefault().setProjectDescription(fProject, projDesc);

			fProject.close(null);
			assertTrue(CCorePlugin.getDefault().getCProjectDescription(fProject, false) == null);
			fProject.open(null);

			// Check that the descriptor added data is still there
			desc = CCorePlugin.getDefault().getCProjectDescription(fProject, false);
			data = desc.getProjectStorageElement("descDescInteraction");
			assertEquals(2 * i, data.getChildren().length);
			data = desc.getProjectStorageElement("descDescInteraction2");
			assertEquals(1 * i, data.getChildren().length);
		}
	}

	public void testAccumulatingBlankLinesInProjectData() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICStorageElement data = desc.getProjectStorageElement("testElement");
		data.createChild("test");
		desc.saveProjectData();

		fProject.close(null);
		fProject.open(null);

		String dotCProject1 = readDotCProjectFile(fProject);
		long mtime1 = fProject.getFile(".cproject").getLocalTimeStamp();
		
		desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		data = desc.getProjectStorageElement("testElement");
		for (ICStorageElement child : data.getChildren()) {
			data.removeChild(child);
		}
		data.createChild("test");
		desc.saveProjectData();

		String dotCProject2 = readDotCProjectFile(fProject);
		long mtime2 = fProject.getFile(".cproject").getLocalTimeStamp();
		assertEquals("Difference in .cproject file", dotCProject1, dotCProject2);
		assertTrue(".cproject file has been written", mtime1 == mtime2);

		// do it a second time - just to be sure
		fProject.close(null);
		fProject.open(null);

		desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		data = desc.getProjectStorageElement("testElement");
		for (ICStorageElement child : data.getChildren()) {
			data.removeChild(child);
		}
		data.createChild("test");
		desc.saveProjectData();

		String dotCProject3 = readDotCProjectFile(fProject);
		long mtime3 = fProject.getFile(".cproject").getLocalTimeStamp();
		assertEquals("Difference in .cproject file", dotCProject2, dotCProject3);
		assertTrue(".cproject file has been written", mtime2 == mtime3);
	}

	/**
	 * Read .cproject file.
	 * 
	 * @param project
	 * @return content of .cproject file
	 * @throws CoreException 
	 * @throws IOException 
	 */
	private static String readDotCProjectFile(IProject project) throws CoreException, IOException {
		IFile cProjectFile = project.getFile(".cproject");
		InputStream in = cProjectFile.getContents();
		try {
			Reader reader = new InputStreamReader(in, "UTF-8");
			StringBuilder sb = new StringBuilder();
			char[] b = new char[4096];
			int n;
			while ((n = reader.read(b)) > 0) {
				sb.append(b, 0, n);
			}
			return sb.toString();
		} finally {
			in.close();
		}
	}
}