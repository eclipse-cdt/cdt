/*******************************************************************************
 * Copyright (c) 2000, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - Adapted from org.eclipse.rse.tests / FileServiceTest
 *                   - Portions adapted from org.eclipse.core.tests.resources / ResourceTest
 *                   - Portions adapted from org.eclipse.core.tests.harness / CoreTest
 *******************************************************************************/
package org.eclipse.rse.tests.synchronize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

/**
 * Base class for RSE Synchronization Tests. Contains utility methods copied
 * from org.eclipse.core.resources.tests
 */
public class SynchronizeTestBase extends RSEBaseConnectionTestCase {

	private String fPropertiesFileName;
	// For testing the test: verify methods on Local
	public static String fDefaultPropertiesFile = "localConnection.properties";

	protected IFileServiceSubSystem fss;
	protected IFileService fs;
	private IRemoteFile fHomeDirectory;
	protected IRemoteFile remoteTempDir;
	private String tempDirPath;

	/**
	 * Constructor with specific test name.
	 *
	 * @param name test to execute
	 */
	public SynchronizeTestBase(String name) {
		this(name, fDefaultPropertiesFile);
	}

	/**
	 * Constructor with connection type and specific test name.
	 *
	 * @param name test to execute
	 * @param propertiesFileName file with connection properties to use
	 */
	public SynchronizeTestBase(String name, String propertiesFileName) {
		super(name);
		fPropertiesFileName = propertiesFileName;
		if (propertiesFileName != null) {
			int idx = propertiesFileName.indexOf("Connection.properties");
			String targetName = propertiesFileName.substring(0, idx);
			setTargetName(targetName);
		}
	}

	public void setUp() throws Exception {
		super.setUp();
		IHost host = getHost(fPropertiesFileName);
		fss = (IFileServiceSubSystem) RemoteFileUtility.getFileSubSystem(host);
		fs = fss.getFileService();
		fss.checkIsConnected(getDefaultProgressMonitor());
		fHomeDirectory = fss.getRemoteFileObject(".", getDefaultProgressMonitor());
		remoteTempDir = fss.getRemoteFileObject(fHomeDirectory, "rsetest" + System.currentTimeMillis(), getDefaultProgressMonitor());
		fss.createFolder(remoteTempDir, getDefaultProgressMonitor());
		tempDirPath = remoteTempDir.getAbsolutePath();
	}

	public void tearDown() throws Exception {
		fss.delete(remoteTempDir, getDefaultProgressMonitor());
		super.tearDown();
	}

	public boolean isWindows() {
		return fss.getHost().getSystemType().isWindows();
	}

	public IProgressMonitor getMonitor() {
		return getDefaultProgressMonitor();
	}

	public String getTempDirPath() {
		return tempDirPath;
	}

	// <Copied from org.eclipse.core.tests.harness / CoreTest>
	/**
	 * Fails the test due to the given throwable.
	 */
	public static void fail(String message, Throwable e) {
		// If the exception is a CoreException with a multistatus
		// then print out the multistatus so we can see all the info.
		if (e instanceof CoreException) {
			IStatus status = ((CoreException) e).getStatus();
			//if the status does not have an exception, print the stack for this one
			if (status.getException() == null)
				e.printStackTrace();
			write(status, 0);
		} else
			e.printStackTrace();
		fail(message + ": " + e);
	}
	private static void write(IStatus status, int indent) {
		PrintStream output = System.out;
		indent(output, indent);
		output.println("Severity: " + status.getSeverity());

		indent(output, indent);
		output.println("Plugin ID: " + status.getPlugin());

		indent(output, indent);
		output.println("Code: " + status.getCode());

		indent(output, indent);
		output.println("Message: " + status.getMessage());

		if (status.getException() != null) {
			indent(output, indent);
			output.print("Exception: ");
			status.getException().printStackTrace(output);
		}

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++)
				write(children[i], indent + 1);
		}
	}
	private static void indent(OutputStream output, int indent) {
		for (int i = 0; i < indent; i++)
			try {
				output.write("\t".getBytes());
			} catch (IOException e) {
				// ignore
			}
	}
	// </Copied from org.eclipse.core.tests.harness / CoreTest (Copyright (c) IBM)>

	// <Copied from org.eclipse.core.tests.resources / ResourceTest (Copyright (c) IBM)>
	public String getUniqueString() {
		return new UniversalUniqueIdentifier().toString();
	}
	protected void create(final IResource resource, boolean local) throws CoreException {
		if (resource == null || resource.exists())
			return;
		if (!resource.getParent().exists())
			create(resource.getParent(), local);
		switch (resource.getType()) {
			case IResource.FILE :
				((IFile) resource).create(local ? new ByteArrayInputStream(new byte[0]) : null, true, getMonitor());
				break;
			case IResource.FOLDER :
				((IFolder) resource).create(true, local, getMonitor());
				break;
			case IResource.PROJECT :
				((IProject) resource).create(getMonitor());
				((IProject) resource).open(getMonitor());
				break;
		}
	}
	/**
	 * Create each element of the resource array in the workspace resource
	 * info tree.
	 */
	public void ensureExistsInWorkspace(final IResource[] resources, final boolean local) {
		IWorkspaceRunnable body = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < resources.length; i++)
					create(resources[i], local);
			}
		};
		try {
			getWorkspace().run(body, null);
		} catch (CoreException e) {
			fail("#ensureExistsInWorkspace(IResource[])", e);
		}
	}
	/**
	 * Delete the given resource from the workspace resource tree.
	 */
	public void ensureDoesNotExistInWorkspace(IResource resource) {
		try {
			if (resource.exists())
				resource.delete(true, null);
		} catch (CoreException e) {
			fail("#ensureDoesNotExistInWorkspace(IResource): " + resource.getFullPath(), e);
		}
	}
	// </Copied from org.eclipse.core.tests.resources / ResourceTest>

	// <Copied from org.eclipse.core.tests.resources / ProjectPreferencesTest (Copyright (c) IBM)>
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	protected static IProject getProject(String name) {
		return getWorkspace().getRoot().getProject(name);
	}
	// </Copied from org.eclipse.core.tests.resources / ProjectPreferencesTest (Copyright (c) IBM)>

}
