/**********************************************************************
 * Copyright (c) 2004, 2014 QNX Software Systems Ltd and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems Ltd - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     James Blackburn (Broadcom Corp.)
 *     Marc-Andre Laperle (Ericsson)
 ***********************************************************************/
package org.eclipse.cdt.core.cdescriptor.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(BaseTestCase5.FLAKY_TEST_TAG)
public class CDescriptorTests extends BaseTestCase5 {
	static String projectId = CTestPlugin.PLUGIN_ID + ".TestProject";
	static IProject fProject;
	static CDescriptorListener listener = new CDescriptorListener();
	static volatile CDescriptorEvent fLastEvent;

	@BeforeEach
	protected void setUpLocal() throws Exception {
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
				CTestPlugin.getWorkspace().run(new IWorkspaceRunnable() {
					@Override
					public void run(IProgressMonitor monitor) throws CoreException {
						CCorePlugin.getDefault().mapCProjectOwner(fProject, projectId, false);
					}
				}, null);
			}
		}, null);
	}

	@AfterEach
	protected void tearDownLocal() throws Exception {
		fProject.delete(true, true, null);
	}

	private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor)
			throws CoreException {
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

	@Test
	public void testDescriptorCreation() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);

		assertNotNull(fLastEvent);
		assertEquals(fLastEvent.getDescriptor(), desc);
		assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_ADDED);
		assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;

		assertEquals(fProject, desc.getProject());
		assertEquals("*", desc.getPlatform());
	}

	@Test
	public void testDescriptorOwner() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICOwnerInfo owner = desc.getProjectOwner();
		assertEquals(projectId, owner.getID());
		assertEquals("*", owner.getPlatform());
		assertEquals("C/C++ Test Project", owner.getName());
	}

	// Disabled this test because it fails every now and then and it tests deprecated API
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340123
	//	@Test
	public void _testConcurrentDescriptorCreation() throws Exception {
		for (int i = 0; i < 100; i++) {
			fProject.close(null);
			fProject.open(null);
			Thread t = new Thread() {
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
			try {
				t.join();
			} catch (InterruptedException e) {
			}

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
	@Test
	public void testConcurrentDescriptorModification() throws Exception {
		int lastLength = 0;
		for (int i = 0; i < 100; ++i) {
			final int indexi = i;
			PDOMManager pdomMgr = (PDOMManager) CCorePlugin.getIndexManager();
			pdomMgr.shutdown();
			fProject.close(null);
			fProject.open(null);
			pdomMgr.startup().schedule();
			final ICDescriptor fdesc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			if (lastLength == 0)
				lastLength = fdesc.getProjectStorageElement("testElement").getChildren().length;
			final Throwable[] exception = new Throwable[10];
			Thread[] threads = new Thread[10];
			for (int j = 0; j < 10; j++) {
				final int indexj = j;
				Thread t = new Thread() {
					@Override
					public void run() {
						try {
							ICDescriptorOperation operation = new ICDescriptorOperation() {
								@Override
								public void execute(ICDescriptor descriptor, IProgressMonitor monitor)
										throws CoreException {
									//									assertFalse(descriptor.getConfigurationDescription().isReadOnly());
									ICStorageElement data = fdesc.getProjectStorageElement("testElement");
									String test = "test" + (indexi * 10 + indexj);
									data.createChild(test);
									//									assertFalse(descriptor.getConfigurationDescription().isReadOnly());
									descriptor.saveProjectData();
									//									System.out.println("Saved " + test);
								}
							};
							CCorePlugin.getDefault().getCDescriptorManager().runDescriptorOperation(fProject, operation,
									null);
						} catch (Throwable exc) {
							exception[indexj] = exc;
							exc.printStackTrace();
						}
					}
				};
				t.start();
				threads[j] = t;
			}
			for (int j = 0; j < threads.length; j++) {
				if (threads[j] != null) {
					try {
						threads[j].join();
					} catch (InterruptedException e) {
					}
				}
				assertNull(exception[j], "Exception occurred: " + exception[j]);
			}
			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
			int lengthAfter = desc.getProjectStorageElement("testElement").getChildren().length;
			lastLength += threads.length; // Update last lengths to what we expect
			assertEquals(lastLength, lengthAfter, "Iteration count: " + i);

			fLastEvent = null;
		}
	}

	/*
	 * This test should pass as two threads, operating on the different storage elements
	 * (outside of an operation) should be safe.
	 */
	@Test
	public void testConcurrentDifferentStorageElementModification() throws Exception {
		for (int i = 0; i < 100; ++i) {
			Thread t = new Thread() {
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
			try {
				t.join();
			} catch (InterruptedException e) {
			}

			fLastEvent = null;
		}
		assertEquals(100, CCorePlugin.getDefault().getCProjectDescription(fProject, false)
				.getProjectStorageElement("testElement4").getChildren().length);
		assertEquals(100, CCorePlugin.getDefault().getCProjectDescription(fProject, false)
				.getProjectStorageElement("testElement5").getChildren().length);
	}

	/*
	 * Tests that (non-structural) changes to the storage element tree work as expected.
	 */
	@Test
	public void testConcurrentSameStorageElementModification() throws Exception {
		for (int i = 0; i < 100; ++i) {
			Thread t = new Thread() {
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
			try {
				t.join();
			} catch (InterruptedException e) {
			}

			fLastEvent = null;
		}
		assertEquals(200, CCorePlugin.getDefault().getCProjectDescription(fProject, false)
				.getProjectStorageElement("testElement6").getChildren().length);
	}

	/*
	 * Tests deadlock when accessing c project description concurrently from two threads
	 */
	@Test
	public void testDeadlockDuringProjectCreation() throws Exception {
		for (int i = 0; i < 10; ++i) {
			tearDownLocal();
			setUpLocal();
			Thread t = new Thread() {
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
			try {
				t.join();
			} catch (InterruptedException e) {
			}

			fLastEvent = null;
		}
	}

	@Test
	public void testDescriptorConversion() {
	}

	@Test
	public void testExtensionCreation() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICExtensionReference extRef = desc.create("org.eclipse.cdt.testextension", "org.eclipse.cdt.testextensionID");

		assertNotNull(fLastEvent);
		assertEquals(fLastEvent.getDescriptor(), desc);
		assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		assertEquals(fLastEvent.getFlags(), CDescriptorEvent.EXTENSION_CHANGED);
		fLastEvent = null;

		assertEquals("org.eclipse.cdt.testextension", extRef.getExtension());
		assertEquals("org.eclipse.cdt.testextensionID", extRef.getID());
	}

	@Test
	public void testExtensionGet() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		desc.create("org.eclipse.cdt.testextension", "org.eclipse.cdt.testextensionID");

		ICExtensionReference extRef[] = desc.get("org.eclipse.cdt.testextension");

		assertEquals("org.eclipse.cdt.testextension", extRef[0].getExtension());
		assertEquals("org.eclipse.cdt.testextensionID", extRef[0].getID());
	}

	@Test
	public void testExtensionData() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		desc.create("org.eclipse.cdt.testextension", "org.eclipse.cdt.testextensionID");

		ICExtensionReference extRef[] = desc.get("org.eclipse.cdt.testextension");
		extRef[0].setExtensionData("testKey", "testValue");

		assertNotNull(fLastEvent);
		assertEquals(fLastEvent.getDescriptor(), desc);
		assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;

		assertEquals("testValue", extRef[0].getExtensionData("testKey"));
		extRef[0].setExtensionData("testKey", null);
		assertEquals(null, extRef[0].getExtensionData("testKey"));
	}

	@Test
	public void testExtensionRemove() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		desc.create("org.eclipse.cdt.testextension", "org.eclipse.cdt.testextensionID");

		ICExtensionReference extRef[] = desc.get("org.eclipse.cdt.testextension");
		desc.remove(extRef[0]);

		assertNotNull(fLastEvent);
		assertEquals(fLastEvent.getDescriptor(), desc);
		assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		assertEquals(fLastEvent.getFlags(), CDescriptorEvent.EXTENSION_CHANGED);
		fLastEvent = null;
	}

	@Test
	public void testProjectDataCreate() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICStorageElement data = desc.getProjectStorageElement("testElement");
		data.createChild("test");
		desc.saveProjectData();

		assertNotNull(fLastEvent);
		assertEquals(fLastEvent.getDescriptor(), desc);
		assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;
	}

	@Test
	public void testProjectDataDelete() throws Exception {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(fProject, true);
		ICStorageElement data = desc.getProjectStorageElement("testElement");
		data.createChild("test");

		ICStorageElement[] list = data.getChildrenByName("test");
		assertEquals(1, list.length);
		data.removeChild(list[0]);
		desc.saveProjectData();

		assertNotNull(fLastEvent);
		assertEquals(fLastEvent.getDescriptor(), desc);
		assertEquals(fLastEvent.getType(), CDescriptorEvent.CDTPROJECT_CHANGED);
		assertEquals(fLastEvent.getFlags(), 0);
		fLastEvent = null;
	}

	@Test
	public void testCProjectDescriptionDescriptorInteraction() throws Exception {
		for (int i = 1; i < 100; i++) {
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

	@Test
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
		assertEquals(dotCProject1, dotCProject2, "Difference in .cproject file");
		assertTrue(mtime1 == mtime2, ".cproject file has been written");

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
		assertEquals(dotCProject2, dotCProject3, "Difference in .cproject file");
		assertTrue(mtime2 == mtime3, ".cproject file has been written");
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