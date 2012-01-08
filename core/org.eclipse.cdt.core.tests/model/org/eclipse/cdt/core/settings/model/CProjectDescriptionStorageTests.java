/*******************************************************************************
 * Copyright (c) 2008, 2011 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	James Blackburn (Broadcom Corp.) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.settings.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 * Testsuite for the project description storage. This
 * currently tests some of the features of the built-in
 * XmlProjectDescriptionStorage(2)
 */
public class CProjectDescriptionStorageTests extends BaseTestCase {

	/** CProject on which these tests are based */
	ICProject cProj;

	public static TestSuite suite() {
		return suite(CProjectDescriptionStorageTests.class, "_");
	}

	// resource change listener that will listen for file changes interesting to the tests
	OurResourceChangeListener resListener;

	@Override
	protected void setUp() throws Exception {
		cProj = CProjectHelper.createNewStileCProject("CProjDescStorage", IPDOMManager.ID_FAST_INDEXER);
		resListener = new OurResourceChangeListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resListener);
	}

	@Override
	protected void tearDown() throws Exception {
		// Remover our resource change listener
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resListener);
		// Make the project files writable so they can be deleted...
		cProj.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		cProj.getProject().getFile(".cproject").setReadOnly(false);
		if (cProj.getProject().getFolder(".csettings").exists()) {
			cProj.getProject().getFolder(".csettings").setReadOnly(false);
			for (IResource child : cProj.getProject().getFolder(".csettings").members())
				child.setReadOnly(false);
		}
		// Wait for a few seconds for the indexer to get going so we avoid deadlock
		Thread.sleep(2000);
		// Delete the project
		CProjectHelper.delete(cProj);
	}

	/**
	 * Tests that external modifications to the CProjectDescription file are picked up
	 * @throws Exception
	 */
	public void testExternalCProjDescModification() throws Exception {
		// Create auto-refresh Thread
		Job refreshJob = new Job("Auto-Refresh") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				try {
					ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					fail("Error during refresh: " + e.getMessage());
				}
				schedule(500);
				return Status.OK_STATUS;
			}
		};
		refreshJob.schedule();

		// Backup the CProjectFile
		final String initial = "initial";
		final String testingStorage = "testingStorage";
		final String testChildInStorage = "testChildInStorage";

		// Backup the original storage file
		backUpCProjectFile(initial);

		IProject project = cProj.getProject();
		ICProjectDescription projDesc = CoreModel.getDefault().getProjectDescription(project, true);
		projDesc.getDefaultSettingConfiguration().getStorage(testingStorage, true).createChild(testChildInStorage);
		CoreModel.getDefault().setProjectDescription(project, projDesc);
		// backup this project_desc
		backUpCProjectFile(testingStorage);

		// Close and open project
		project.close(null);
		project.open(null);

		// verify changes are in read-only description
		projDesc = CoreModel.getDefault().getProjectDescription(project, false);
		assertNotNull(projDesc.getDefaultSettingConfiguration().getStorage(testingStorage, false));
		project.refreshLocal(IResource.DEPTH_INFINITE, null);

		// Restore from backup
		resListener.reset();
		resListener.addFileToWatch(cProj.getProject().getFile(".cproject").getFullPath());
		restoreCProjectFile(initial);
		resListener.waitForChange();

		// Fetch what should be the initial project description
		projDesc = CoreModel.getDefault().getProjectDescription(project, false);
		assertNull(projDesc.getDefaultSettingConfiguration().getStorage(testingStorage, false));

		// Test XmlProjectDescriptionStorage2:

		// Test that updating the contents of a storage module leads to a reload of the project description
		// (In XmlProjectDescriptionStorage2 configuration storage modules are stored in different files...)
		restoreCProjectFile(testingStorage);
		project.close(null);
		project.open(null);

		// create testChildInStorage
		projDesc = CoreModel.getDefault().getProjectDescription(project, true);
		ICStorageElement[] children = projDesc.getDefaultSettingConfiguration().getStorage(testingStorage, false).getChildrenByName(testChildInStorage);
		assertTrue(children.length == 1);
		projDesc.getDefaultSettingConfiguration().getStorage(testingStorage, false).removeChild(children[0]);
		CoreModel.getDefault().setProjectDescription(project, projDesc);
		projDesc = CoreModel.getDefault().getProjectDescription(project, false);
		assertTrue(projDesc.getDefaultSettingConfiguration().getStorage(testingStorage, false).getChildrenByName(testChildInStorage).length == 0);

		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		// Restore from backup
		resListener.reset();
		resListener.addFileToWatch(cProj.getProject().getFolder(".csettings").getFullPath());
		restoreCProjectFile(testingStorage);
		resListener.waitForChange();

		// Check that the project description no longer contains the testChildInStorage
		projDesc = CoreModel.getDefault().getProjectDescription(project, false);
		assertTrue(projDesc.getDefaultSettingConfiguration().getStorage(testingStorage, false).getChildrenByName(testChildInStorage).length == 1);

		refreshJob.cancel();
	}

	/**
	 * Tests that external create and replace of CProjectDescription is picked up
	 * (Bug 311189)
	 * @throws Exception
	 */
	public void testExternalCProjDescRemoveAndReplace() throws Exception {
		// Create auto-refresh Thread
		Job refreshJob = new Job("Auto-Refresh") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				try {
					ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					fail("Error during refresh: " + e.getMessage());
				}
				schedule(500);
				return Status.OK_STATUS;
			}
		};
		refreshJob.schedule();

		// Backup the CProjectFile
		final String initial = "initial";
		final String testingStorage = "testingStorage";
		final String testChildInStorage = "testChildInStorage";

		// Backup the original storage file
		backUpCProjectFile(initial);

		IProject project = cProj.getProject();
		ICProjectDescription projDesc = CoreModel.getDefault().getProjectDescription(project, true);
		projDesc.getDefaultSettingConfiguration().getStorage(testingStorage, true).createChild(testChildInStorage);
		CoreModel.getDefault().setProjectDescription(project, projDesc);
		// backup this project_desc
		backUpCProjectFile(testingStorage);

		// Close and open project
		project.close(null);
		project.open(null);

		// verify changes are in read-only description
		projDesc = CoreModel.getDefault().getProjectDescription(project, false);
		assertNotNull(projDesc.getDefaultSettingConfiguration().getStorage(testingStorage, false));
		project.refreshLocal(IResource.DEPTH_INFINITE, null);

		try {
			// Lock the workspace
			Job.getJobManager().beginRule(ResourcesPlugin.getWorkspace().getRoot(), null);

			// Restore from backup
			resListener.reset();
			resListener.addFileToWatch(cProj.getProject().getFile(".cproject").getFullPath());
			restoreCProjectUsingIResource(initial);
		} finally {
			Job.getJobManager().endRule(ResourcesPlugin.getWorkspace().getRoot());
		}

		resListener.waitForChange();
		// Fetch what should be the initial project description
		projDesc = CoreModel.getDefault().getProjectDescription(project, false);
		assertNull(projDesc.getDefaultSettingConfiguration().getStorage(testingStorage, false));

		refreshJob.cancel();
	}


	/**
	 * Tests that a read-only project description file is picked up
	 * @throws Exception
	 */
	public void testReadOnlyProjectDescription() throws Exception {
		enableSetWritableWhenHeadless(true);
		try {
			makeDescriptionReadOnly();
			IProject project = cProj.getProject();
			ICProjectDescription projDesc = CoreModel.getDefault().getProjectDescription(project, true);
			projDesc.getDefaultSettingConfiguration().getStorage("Temp_testing_storage", true);
			CoreModel.getDefault().setProjectDescription(project, projDesc);

			project.close(null);
			project.open(null);

			projDesc = CoreModel.getDefault().getProjectDescription(project, false);
			assertNotNull(projDesc.getDefaultSettingConfiguration().getStorage("Temp_testing_storage", false));
			projDesc = CoreModel.getDefault().getProjectDescription(project, true);
			makeDescriptionReadOnly();
			projDesc.getDefaultSettingConfiguration().removeStorage("Temp_testing_storage");
			CoreModel.getDefault().setProjectDescription(project, projDesc);

			project.close(null);
			project.open(null);

			projDesc = CoreModel.getDefault().getProjectDescription(project, false);
			assertNull(projDesc.getDefaultSettingConfiguration().getStorage("Temp_testing_storage", false));
		} finally {
			enableSetWritableWhenHeadless(false);
		}
	}

	/*
	 *
	 * Helper methods for external modifications
	 *
	 */

	/**
	 * Enables/disables team UI preference whether validateEdit should
	 * set files writable if no UI context has been provided.
	 */
	private void enableSetWritableWhenHeadless(boolean enable) {
		InstanceScope.INSTANCE.getNode("org.eclipse.team.ui").putBoolean("org.eclipse.team.ui.validate_edit_with_no_context", enable);
	}

	/**
	 * makes the project description (as stored by the XmlProjectDescriptionStorage &
	 * XmlProjectDescriptionStorage2) read-only.  Does this using java.io.File deliberately.
	 */
	private void makeDescriptionReadOnly() throws Exception {
		File cproj = cProj.getProject().getFile(".cproject").getLocation().toFile();
		if (!cproj.exists())
			throw new FileNotFoundException();
		cproj.setReadOnly();
		// XmlProjectDescription2 stores settings in a .csettings directory, look for it
		File csettDir = cProj.getProject().getFile(".csettings").getLocation().toFile();
		if (csettDir.exists()) {
			for (File child : csettDir.listFiles())
				child.setReadOnly();
			csettDir.setReadOnly();
		}
	}

	/**
	 * Restore the file from the backup. Only does so if the contents of the file
	 * have changed (to prevent updating the modification stamp on the file...)
	 * @param uniqueKey
	 */
	private void restoreCProjectFile(String uniqueKey) {
		File cproj = cProj.getProject().getFile(".cproject").getLocation().toFile();
		File cprojback = cProj.getProject().getFile(".cproject_" + uniqueKey).getLocation().toFile();
		if (diff(cprojback, cproj))
			copyFile(cprojback, cproj);
		File csettings = cProj.getProject().getFile(".csettings").getLocation().toFile();
		File csettingsback = cProj.getProject().getFile(".csettings_" + uniqueKey).getLocation().toFile();
		if (csettingsback.exists()) {
			for (File f : csettingsback.listFiles()) {
				File orig = new File(csettings, f.getName());
				if (diff(f, orig))
					copyFile(f, orig);
			}
		}
	}

	/**
	 * Use IResource API to remove and replace the .cproject file (rather than just modifying
	 * it atomically exteranlly)
	 * This tests the team provider remove and replace behaviour when holding a higher-leve resource lock
	 */
	private void restoreCProjectUsingIResource(String uniqueKey) throws CoreException {
		// delete the .cproject
		IFile cproject = cProj.getProject().getFile(".cproject");
		IFile cproject_back = cProj.getProject().getFile(".cproject_" + uniqueKey);
		cproject.delete(true, null);
		cproject.create(cproject_back.getContents(true), true, null);

		final IFolder csettings = cProj.getProject().getFolder(".csettings");
		IFolder csettings_back = cProj.getProject().getFolder(".csettings_" + uniqueKey);
		csettings.refreshLocal(IResource.DEPTH_INFINITE, null);
		csettings_back.refreshLocal(IResource.DEPTH_INFINITE, null);

		// Nothing to do if these directories don't exist
		if (!csettings.exists() && !csettings_back.exists())
			return;

		csettings.delete(false, null);
		csettings.create(true, false, null);

		csettings_back.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				assertTrue(resource instanceof IFile);
				csettings.getFile(resource.getName()).create(((IFile)resource).getContents(), false, null);
				return false;
			}
		});
	}


	private void backUpCProjectFile(String uniqueKey) {
		File cproj = cProj.getProject().getFile(".cproject").getLocation().toFile();
		File cprojback = cProj.getProject().getFile(".cproject_" + uniqueKey).getLocation().toFile();
		copyFile(cproj, cprojback);
		// backup .csettings as well
		File csettings = cProj.getProject().getFile(".csettings").getLocation().toFile();
		if (csettings.exists() && csettings.isDirectory()) {
			File csettingsback = cProj.getProject().getFile(".csettings_" + uniqueKey).getLocation().toFile();
			if (!csettingsback.exists())
				csettingsback.mkdir();
			for (File f : csettings.listFiles())
				copyFile(f, new File(csettingsback, f.getName()));
		}
	}

	/**
	 * Return boolean indicating whether two files are different
	 * @param src1
	 * @param src2
	 * @return
	 */
	private boolean diff(File src1, File src2) {
		if (!src1.exists() || !src2.exists())
			return true;
		FileInputStream in1 = null;
		FileInputStream in2 = null;
		try {
			in1 = new FileInputStream(src1);
			in2 = new FileInputStream(src2);
			while (true) {
				int byteRead1 = in1.read();
				int byteRead2 = in2.read();
				if (byteRead1 == -1 && byteRead2 == -1)
					return false;
				if (byteRead1 != byteRead2)
					return true;
			}
		} catch (Exception e) {
			fail("Exception diffingFiles: " + src1.getAbsolutePath() + " ; " + src2.getAbsolutePath());
			return true;
		} finally {
			if (in1 != null)
				try {in1.close();} catch (Exception e) {/*Don't care*/}
			if (in2 != null)
				try {in2.close();} catch (Exception e) {/*Don't care*/}
		}
	}

	private void copyFile(File src, File dst) {
		long initModificationTime = dst.lastModified();
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dst);
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1)
				out.write(buffer, 0, bytesRead);
		} catch (Exception e) {
			fail("Exception copyingFile: " + src.getAbsolutePath() + " -> " + dst.getAbsolutePath());
		} finally {
			if (in != null)
				try {in.close();} catch (Exception e) {/*Don't care*/}
			if (out != null)
				try {out.close();} catch (Exception e) {/*Don't care*/}
		}

		while (dst.lastModified() - initModificationTime == 0) {
			// Unix stat doesn't return granularity < 1000ms :(
			// If we don't sleep here, and the unit test goes too quickly, we're scuppered.
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Don't care
			}
			dst.setLastModified(System.currentTimeMillis());
		}
	}

	/**
	 * Our resource change listener which notified us when a file has been detected as changed
	 * Users add files to the files to watch. Reset the listener and block waiting for the change to
	 * be noticed.
	 */
	private static class OurResourceChangeListener implements IResourceChangeListener {
		boolean changeDetected;
		private Set<IPath> filesToWatch = new HashSet<IPath>();
		@Override
		public synchronized void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			if (delta == null)
				return;
			for (IPath f : filesToWatch)
				if (delta.findMember(f) != null) {
					changeDetected = true;
					notifyAll();
					break;
				}
		}
		public synchronized void addFileToWatch(IPath file) {
			filesToWatch.add(file);
		}
		public synchronized void reset() {
			changeDetected = false;
		}
		public synchronized void waitForChange() {
			try {
				if (!changeDetected)
					wait(20000);
				if (!changeDetected)
					CCorePlugin.log("No Change detected in 20s!");
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
