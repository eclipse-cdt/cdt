/*******************************************************************************
 * Copyright (c) 2009, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *     James Blackburn (Broadcom Corp.)
 *     Liviu Ionescu - bug 392416
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.TestCfgDataProvider;
import org.eclipse.cdt.internal.core.Cygwin;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.Assert;

/**
 * This class contains utility methods for creating resources
 * such as projects, files, folders etc. which are being used
 * in test fixture of unit tests.
 *
 * Some classes with similar idea worth to look at:
 * org.eclipse.core.filebuffers.tests.ResourceHelper,
 * org.eclipse.cdt.ui.tests.text.ResourceHelper.
 *
 * @since 6.0
 */
public class ResourceHelper {
	private final static IProgressMonitor NULL_MONITOR = new NullProgressMonitor();
	private static final int MAX_RETRY = 5;

	private final static Set<String> externalFilesCreated = new HashSet<>();
	private final static Set<IResource> resourcesCreated = new HashSet<>();

	/**
	 * Creates CDT project in a specific path in workspace and opens it.
	 *
	 * @param projectName - project name.
	 * @param pathInWorkspace - path relative to workspace root.
	 * @return - new {@link IProject}.
	 * @throws CoreException - if the project can't be created.
	 * @throws OperationCanceledException...
	 */
	public static IProject createCDTProject(String projectName, String pathInWorkspace)
			throws OperationCanceledException, CoreException {
		return createCDTProject(projectName, pathInWorkspace, null);
	}

	/**
	 * Creates CDT project in a specific path in workspace adding specified configurations and opens it.
	 *
	 * @param projectName - project name.
	 * @param pathInWorkspace - path relative to workspace root.
	 * @param configurationIds - array of configuration IDs.
	 * @return - new {@link IProject}.
	 * @throws CoreException - if the project can't be created.
	 * @throws OperationCanceledException...
	 */
	public static IProject createCDTProject(String projectName, String pathInWorkspace, String[] configurationIds)
			throws OperationCanceledException, CoreException {
		CCorePlugin cdtCorePlugin = CCorePlugin.getDefault();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		IProject project = root.getProject(projectName);
		IndexerPreferences.set(project, IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_NO_INDEXER);
		resourcesCreated.add(project);

		IProjectDescription prjDescription = workspace.newProjectDescription(projectName);
		if (pathInWorkspace != null) {
			IPath absoluteLocation = root.getLocation().append(pathInWorkspace);
			prjDescription.setLocation(absoluteLocation);
		}

		if (configurationIds != null && configurationIds.length > 0) {
			ICProjectDescriptionManager prjDescManager = cdtCorePlugin.getProjectDescriptionManager();

			project.create(NULL_MONITOR);
			project.open(NULL_MONITOR);

			ICProjectDescription icPrjDescription = prjDescManager.createProjectDescription(project, false);
			ICConfigurationDescription baseConfiguration = cdtCorePlugin
					.getPreferenceConfiguration(TestCfgDataProvider.PROVIDER_ID);

			for (String cfgId : configurationIds) {
				icPrjDescription.createConfiguration(cfgId, cfgId + " Name", baseConfiguration);
			}
			prjDescManager.setProjectDescription(project, icPrjDescription);
		}
		project = cdtCorePlugin.createCDTProject(prjDescription, project, NULL_MONITOR);
		waitForProjectRefreshToFinish();
		Assert.assertNotNull(project);

		project.open(null);
		Assert.assertTrue(project.isOpen());

		return project;
	}

	/**
	 * Creates CDT project in a specific location and opens it.
	 *
	 * @param projectName - project name.
	 * @param locationURI - location.
	 * @return - new {@link IProject}.
	 * @throws CoreException - if the project can't be created.
	 * @throws OperationCanceledException...
	 */
	public static IProject createCDTProject(String projectName, URI locationURI)
			throws OperationCanceledException, CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		IProject project = root.getProject(projectName);
		IndexerPreferences.set(project, IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_NO_INDEXER);
		resourcesCreated.add(project);

		IProjectDescription description = workspace.newProjectDescription(projectName);
		description.setLocationURI(locationURI);
		project = CCorePlugin.getDefault().createCDTProject(description, project, NULL_MONITOR);
		waitForProjectRefreshToFinish();
		Assert.assertNotNull(project);

		project.open(null);
		Assert.assertTrue(project.isOpen());

		return project;
	}

	/**
	 * Creates a project in the workspace and opens it.
	 *
	 * @param projectName - project name.
	 * @return - new {@link IProject}.
	 * @throws CoreException - if the project can't be created.
	 * @throws OperationCanceledException...
	 */
	public static IProject createCDTProject(String projectName) throws OperationCanceledException, CoreException {
		return createCDTProject(projectName, null, null);
	}

	/**
	 * Creates a project with 1 test configuration and opens it.
	 *
	 * @param projectName - project name.
	 * @return - new {@link IProject}.
	 * @throws CoreException - if the project can't be created.
	 * @throws OperationCanceledException...
	 */
	public static IProject createCDTProjectWithConfig(String projectName) throws Exception {
		IProject project = createCDTProject(projectName, null,
				new String[] { "org.eclipse.cdt.core.tests.configuration" });
		resourcesCreated.add(project);
		return project;
	}

	/**
	 * Creates a plain Eclipse project.
	 *
	 * @param projectName
	 * @return  the project handle
	 * @throws CoreException  if project could not be created
	 */
	public static IProject createProject(String projectName) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		if (!project.exists()) {
			project.create(NULL_MONITOR);
		} else {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}

		if (!project.isOpen())
			project.open(NULL_MONITOR);

		resourcesCreated.add(project);
		return project;
	}

	/**
	 * Deletes project by name.
	 *
	 * @param projectName
	 * @throws CoreException
	 */
	public static void deleteProject(String projectName) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		if (project.exists())
			delete(project);
	}

	/**
	 * Deletes given project with content.
	 *
	 * @param project
	 * @throws CoreException
	 */
	public static void delete(final IProject project) throws CoreException {
		delete(project, true);
	}

	/**
	 * Deletes project.
	 *
	 * @param project
	 * @param deleteContent  whether to delete project content
	 * @throws CoreException
	 */
	public static void delete(final IProject project, boolean deleteContent) throws CoreException {
		for (int i = 0; i < MAX_RETRY; i++) {
			try {
				project.delete(deleteContent, true, NULL_MONITOR);
				i = MAX_RETRY;
			} catch (CoreException x) {
				if (i == MAX_RETRY - 1) {
					CTestPlugin.getDefault().getLog().log(x.getStatus());
				}
				try {
					Thread.sleep(1000); // sleep a second
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * Creates a file with specified content.
	 *
	 * @param file - file name.
	 * @param contents - contents of the file.
	 * @return file handle.
	 * @throws CoreException - if the file can't be created.
	 */
	public static IFile createFile(IFile file, String contents) throws CoreException {
		if (contents == null) {
			contents = "";
		}

		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
		file.create(inputStream, true, NULL_MONITOR);
		resourcesCreated.add(file);
		return file;
	}

	/**
	 * Creates new file from project root with empty content. The filename
	 * can include relative path as a part of the name but the the path
	 * has to be present on disk.
	 *
	 * @param project - project where to create the file.
	 * @param name - filename.
	 * @return file handle.
	 * @throws CoreException if something goes wrong.
	 */
	public static IFile createFile(IProject project, String name) throws CoreException {
		if (new Path(name).segmentCount() > 1)
			createFolder(project, new Path(name).removeLastSegments(1).toString());
		return createFile(project.getFile(name), null);
	}

	/**
	 * Creates new file from workspace root with empty content. The filename
	 * can include relative path as a part of the name but the the path
	 * has to be present on disk.
	 * The intention of the method is to create files which do not belong to any project.
	 *
	 * @param name - filename.
	 * @return full path of the created file.
	 *
	 * @throws CoreException...
	 * @throws IOException...
	 */
	public static IPath createWorkspaceFile(String name) throws CoreException, IOException {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath fullPath = workspaceRoot.getLocation().append(name);
		java.io.File file = new java.io.File(fullPath.toOSString());
		if (!file.exists()) {
			boolean result = file.createNewFile();
			Assert.assertTrue(result);
		}
		Assert.assertTrue(file.exists());

		externalFilesCreated.add(fullPath.toOSString());
		workspaceRoot.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		return fullPath;
	}

	/**
	 * Creates new folder from project root. The folder name
	 * can include relative path as a part of the name.
	 * Nonexistent parent directories are being created.
	 *
	 * @param project - project where to create the folder.
	 * @param name - folder name.
	 * @return folder handle.
	 * @throws CoreException if something goes wrong.
	 */
	public static IFolder createFolder(IProject project, String name) throws CoreException {
		final IPath p = new Path(name);
		IContainer folder = project;
		for (String seg : p.segments()) {
			folder = folder.getFolder(new Path(seg));
			if (!folder.exists())
				((IFolder) folder).create(true, true, NULL_MONITOR);
		}
		resourcesCreated.add(folder);
		return (IFolder) folder;
	}

	/**
	 * Creates new folder from workspace root. The folder name
	 * can include relative path as a part of the name.
	 * Nonexistent parent directories are being created as per {@link File#mkdirs()}.
	 * The intention of the method is to create folders which do not belong to any project.
	 *
	 * @param name - folder name.
	 * @return absolute location of the folder on the file system.
	 * @throws IOException if something goes wrong.
	 */
	public static IPath createWorkspaceFolder(String name) throws CoreException, IOException {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath fullPath = workspaceRoot.getLocation().append(name);
		java.io.File folder = new java.io.File(fullPath.toOSString());
		if (!folder.exists()) {
			boolean result = folder.mkdirs();
			Assert.assertTrue(result);
		}
		Assert.assertTrue(folder.exists());

		externalFilesCreated.add(fullPath.toOSString());
		workspaceRoot.refreshLocal(IResource.DEPTH_INFINITE, NULL_MONITOR);
		return fullPath;
	}

	/**
	 * Creates new temporary folder with generated name from workspace root.
	 *
	 * @return absolute location of the folder on the file system.
	 * @throws IOException if something goes wrong.
	 */
	public static IPath createTemporaryFolder() throws CoreException, IOException {
		return ResourceHelper.createWorkspaceFolder("tmp/" + System.currentTimeMillis() + '.' + UUID.randomUUID());
	}

	/**
	 * Creates new eclipse file-link from project root to file system file. The filename
	 * can include relative path as a part of the name but the the path
	 * has to be present on disk.
	 *
	 * @param project - project where to create the file.
	 * @param fileLink - filename of the link being created.
	 * @param realFile - file on the file system, the target of the link.
	 * @return file handle.
	 * @throws CoreException if something goes wrong.
	 */
	public static IFile createLinkedFile(IProject project, String fileLink, IPath realFile) throws CoreException {
		IFile file = project.getFile(fileLink);
		file.createLink(realFile, IResource.REPLACE, null);
		Assert.assertTrue(file.exists());
		resourcesCreated.add(file);
		return file;
	}

	/**
	 * Creates new eclipse file-link from project root to file system file. The filename
	 * can include relative path as a part of the name but the the path
	 * has to be present on disk.
	 *
	 * @param project - project where to create the file.
	 * @param fileLink - filename of the link being created.
	 * @param realFile - file on the file system, the target of the link.
	 * @return file handle.
	 * @throws CoreException if something goes wrong.
	 */
	public static IFile createLinkedFile(IProject project, String fileLink, String realFile) throws CoreException {
		return createLinkedFile(project, fileLink, new Path(realFile));
	}

	/**
	 * Creates new eclipse file-link from project root to EFS file.
	 *
	 * @param project - project where to create the file.
	 * @param fileLink - filename of the link being created.
	 * @param realFile - file on the EFS file system, the target of the link.
	 * @return file handle.
	 * @throws CoreException if something goes wrong.
	 */
	public static IFile createEfsFile(IProject project, String fileLink, URI realFile) throws CoreException {
		IFile file = project.getFile(fileLink);
		file.createLink(realFile, IResource.ALLOW_MISSING_LOCAL, NULL_MONITOR);
		resourcesCreated.add(file);
		return file;
	}

	/**
	 * Creates new eclipse file-link from project root to EFS file.
	 *
	 * @param project - project where to create the file.
	 * @param fileLink - filename of the link being created.
	 * @param realFile - file on the EFS file system, the target of the link.
	 * @return file handle.
	 * @throws CoreException if something goes wrong.
	 * @throws URISyntaxException if wrong URI syntax
	 */
	public static IFile createEfsFile(IProject project, String fileLink, String realFile)
			throws CoreException, URISyntaxException {
		return createEfsFile(project, fileLink, new URI(realFile));
	}

	/**
	 * Creates new eclipse folder-link from project root to file system folder. The folder name
	 * can include relative path as a part of the name but the the path
	 * has to be present on disk.
	 *
	 * @param project - project where to create the file.
	 * @param folderLink - name of the link being created.
	 * @param realFolder - folder on the file system, the target of the link.
	 * @return file handle.
	 * @throws CoreException if something goes wrong.
	 */
	public static IFolder createLinkedFolder(IProject project, String folderLink, IPath realFolder)
			throws CoreException {
		IFolder folder = project.getFolder(folderLink);
		folder.createLink(realFolder, IResource.REPLACE | IResource.ALLOW_MISSING_LOCAL, null);
		Assert.assertTrue(folder.exists());
		resourcesCreated.add(folder);
		return folder;
	}

	/**
	 * Creates new eclipse folder-link from project root to file system folder. The folder name
	 * can include relative path as a part of the name but the the path
	 * has to be present on disk.
	 *
	 * @param project - project where to create the file.
	 * @param folderLink - name of the link being created.
	 * @param realFolder - folder on the file system, the target of the link.
	 * @return file handle.
	 * @throws CoreException if something goes wrong.
	 */
	public static IFolder createLinkedFolder(IProject project, String folderLink, String realFolder)
			throws CoreException {
		return createLinkedFolder(project, folderLink, new Path(realFolder));
	}

	/**
	 * Creates new eclipse folder-link from project root to EFS folder.
	 *
	 * @param project - project where to create the folder.
	 * @param folderLink - folder name of the link being created.
	 * @param realFolder - folder on the EFS file system, the target of the link.
	 * @return folder handle.
	 * @throws CoreException if something goes wrong.
	 */
	public static IFolder createEfsFolder(IProject project, String folderLink, URI realFolder) throws CoreException {
		IFolder folder = project.getFolder(folderLink);
		if (folder.exists()) {
			Assert.assertEquals("Folder with the same name but different location already exists", realFolder,
					folder.getLocationURI());
			return folder;
		}

		folder.createLink(realFolder, IResource.ALLOW_MISSING_LOCAL, new NullProgressMonitor());
		resourcesCreated.add(folder);
		return folder;
	}

	/**
	 * Creates new eclipse folder-link from project root to EFS folder.
	 *
	 * @param project - project where to create the folder.
	 * @param folderLink - folder name of the link being created.
	 * @param realFolder - folder on the EFS file system, the target of the link.
	 * @return folder handle.
	 * @throws CoreException if something goes wrong.
	 * @throws URISyntaxException if wrong URI syntax
	 */
	public static IFolder createEfsFolder(IProject project, String folderLink, String realFolder)
			throws CoreException, URISyntaxException {
		return createEfsFolder(project, folderLink, new URI(realFolder));
	}

	/**
	 * Checks if symbolic links are supported on the system.
	 * Used in particular by method {@link #createSymbolicLink(IPath, IPath)}
	 * and other flavors to create symbolic links.
	 *
	 * Note that Windows links .lnk are not supported here.
	 * @return {@code true} if symbolic links are suppoted, {@code false} otherwise.
	 */
	public static boolean isSymbolicLinkSupported() {
		return !Platform.getOS().equals(Platform.OS_WIN32);
	}

	/**
	 * Creates new symbolic file system link from file or folder on project root
	 * to another file system file. The filename can include relative path
	 * as a part of the name but the the path has to be present on disk.
	 *
	 * @param project - project where to create the file.
	 * @param linkName - name of the link being created.
	 * @param realPath - file or folder on the file system, the target of the link.
	 * @return file handle.
	 *
	 * @throws UnsupportedOperationException on Windows where links are not supported.
	 * @throws IOException...
	 * @throws CoreException...
	 */
	public static IResource createSymbolicLink(IProject project, String linkName, IPath realPath)
			throws IOException, CoreException, UnsupportedOperationException {
		if (!isSymbolicLinkSupported()) {
			throw new UnsupportedOperationException("Windows links .lnk are not supported.");
		}

		Assert.assertTrue("Path for symbolic link does not exist: [" + realPath.toOSString() + "]",
				new File(realPath.toOSString()).exists());

		IPath linkedPath = project.getLocation().append(linkName);
		createSymbolicLink(linkedPath, realPath);

		IResource resource = project.getFile(linkName);
		resource.refreshLocal(IResource.DEPTH_ZERO, null);

		if (!resource.exists()) {
			resource = project.getFolder(linkName);
			resource.refreshLocal(IResource.DEPTH_ZERO, null);
		}
		Assert.assertTrue("Failed to create resource form symbolic link", resource.exists());

		externalFilesCreated.add(linkedPath.toOSString());
		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, NULL_MONITOR);
		return resource;
	}

	/**
	 * Creates new symbolic file system link from file or folder to another filesystem file.
	 * The target path has to be present on disk.
	 *
	 * @param linkPath - filesystem path of the link being created.
	 * @param realPath - file or folder on the file system, the target of the link.
	 *
	 * @throws UnsupportedOperationException on Windows where links are not supported.
	 * @throws IOException if execution of the command fails.
	 */
	public static void createSymbolicLink(IPath linkPath, IPath realPath) throws IOException {
		if (!isSymbolicLinkSupported()) {
			throw new UnsupportedOperationException("Windows links .lnk are not supported.");
		}

		String command[] = { "ln", "-s", realPath.toOSString(), linkPath.toOSString() };
		Process process = Runtime.getRuntime().exec(command);

		// Wait for up to 2.5s...
		for (int i = 0; i < 5; i++) {
			try {
				Assert.assertTrue("ln process exited with non-zero status", process.waitFor() == 0);
				// If exitValue succeeded, then the process has exited successfully.
				break;
			} catch (InterruptedException e) {
				// Clear interrupted state, see Java bug http://bugs.sun.com/view_bug.do?bug_id=6420270
				Thread.interrupted();
			}
			// Wait for a 500ms before checking again.
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				/*don't care*/}
		}
		Assert.assertTrue("Symbolic link not created, command=[" + command + "]", linkPath.toFile().exists());
	}

	/**
	 * Creates new symbolic file system link from file or folder on project root
	 * to another file system file. The filename can include relative path
	 * as a part of the name but the the path has to be present on disk.
	 *
	 * @param project - project where to create the file.
	 * @param linkName - name of the link being created.
	 * @param realPath - file or folder on the file system, the target of the link.
	 * @return file handle.
	 *
	 * @throws UnsupportedOperationException on Windows where links are not supported.
	 * @throws IOException...
	 * @throws CoreException...
	 */
	public static IResource createSymbolicLink(IProject project, String linkName, String realPath)
			throws IOException, CoreException, UnsupportedOperationException {
		return createSymbolicLink(project, linkName, new Path(realPath));
	}

	/**
	 * Conversion from Windows path to Cygwin path.
	 *
	 * @param windowsPath - Windows path.
	 * @return Cygwin style converted path.
	 * @throws UnsupportedOperationException if Cygwin is unavailable.
	 * @throws IOException on IO problem.
	 */
	public static String windowsToCygwinPath(String windowsPath) throws IOException, UnsupportedOperationException {
		return Cygwin.windowsToCygwinPath(windowsPath);
	}

	/**
	 * Conversion from Cygwin path to Windows path.
	 *
	 * @param cygwinPath - Cygwin path.
	 * @return Windows style converted path.
	 * @throws UnsupportedOperationException if Cygwin is unavailable.
	 * @throws IOException on IO problem.
	 */
	public static String cygwinToWindowsPath(String cygwinPath) throws IOException, UnsupportedOperationException {
		return Cygwin.cygwinToWindowsPath(cygwinPath);
	}

	/**
	 * Get contents of file on file-system.
	 *
	 * @param fullPath - full path to the file on the file-system.
	 * @return contents of the file.
	 * @throws IOException on IO problem.
	 */
	public static String getContents(IPath fullPath) throws IOException {
		FileInputStream stream = new FileInputStream(fullPath.toFile());
		try {
			// Avoid using java.nio.channels.FileChannel,
			// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4715154
			Reader reader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()));
			StringBuilder builder = new StringBuilder();
			char[] buffer = new char[8192];
			int read;
			while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
				builder.append(buffer, 0, read);
			}
			return builder.toString();
		} finally {
			stream.close();
		}
	}

	/**
	 * Get contents of file on file-system.
	 *
	 * @param fullPath - full path to the file on the file-system.
	 * @return contents of the file.
	 * @throws IOException on IO problem.
	 */
	public static String getContents(String fullPath) throws IOException {
		return getContents(new Path(fullPath));
	}

	/**
	 * Clean-up any files created as part of a unit test.
	 * This method removes *all* Workspace IResources and any external
	 * files / folders created with the #createWorkspaceFile #createWorkspaceFolder
	 * methods in this class
	 *
	 * @deprecated Use {@link #cleanUp(String)} instead so test name can be printed in diagnostics
	 */
	@Deprecated
	public static void cleanUp() throws CoreException, IOException {
		cleanUp("<unknown>");
	}

	/**
	 * Clean-up any files created as part of a unit test.
	 * This method removes *all* Workspace IResources and any external
	 * files / folders created with the #createWorkspaceFile #createWorkspaceFolder
	 * methods in this class
	 */
	public static void cleanUp(String testName) throws CoreException, IOException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		root.refreshLocal(IResource.DEPTH_INFINITE, NULL_MONITOR);

		joinIndexerBeforeCleanup(testName);

		// Delete all external files & folders created using ResourceHelper
		for (String loc : externalFilesCreated) {
			File f = new File(loc);
			if (f.exists())
				deleteRecursive(f);
		}
		externalFilesCreated.clear();

		// Remove IResources created by this helper
		for (IResource r : resourcesCreated) {
			if (r.exists()) {
				try {
					r.delete(true, NULL_MONITOR);
				} catch (CoreException e) {
					// Ignore
				}
			}
		}
		resourcesCreated.clear();
	}

	public static void joinIndexerBeforeCleanup(String testName) {
		// Bug 499471: there is a race condition in the indexer when projects
		// are created and deleted quickly. Therefore, wait for the indexer
		// to be idle before deleting projects.
		if (!CCorePlugin.getIndexManager().isIndexerIdle()) {
			// the 2 second wait is very long in practice, when the race condition is
			// happening the total join time is just a few ms for most tests, and
			// up to 75 ms for a couple of tests on the HIPP
			boolean joinSuccess = CCorePlugin.getIndexManager().joinIndexer(2000, new NullProgressMonitor());
			if (!joinSuccess) {
				System.err.println(
						"Indexer did not stop runing, possible deadlock about to happen. Running test " + testName);
			}
		}
	}

	private static void waitForProjectRefreshToFinish() {
		try {
			// CDT opens the Project with BACKGROUND_REFRESH enabled which causes the
			// refresh manager to refresh the project 200ms later.  This Job interferes
			// with the resource change handler firing see: bug 271264
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);
		} catch (Exception e) {
			// Ignore
		}
	}

	/**
	 * Recursively delete a directory / file
	 *
	 * For safety this method only deletes files created under the workspace
	 *
	 * @param file
	 */
	private static final void deleteRecursive(File f) throws IllegalArgumentException {
		// Ensure that the file being deleted is a child of the workspace
		// root to prevent anything nasty happening
		if (!f.getAbsolutePath()
				.startsWith(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsolutePath())) {
			throw new IllegalArgumentException("File must exist within the workspace!");
		}

		if (f.isDirectory()) {
			for (File f1 : f.listFiles()) {
				deleteRecursive(f1);
			}
		}
		f.delete();
	}

	/**
	 * Notify {@link ResourceHelper} that given resource should be removed in
	 * {@link #cleanUp(String)}.
	 */
	public static void addResourceCreated(IResource resource) {
		if (resource != null) {
			resourcesCreated.add(resource);
		}
	}
}
