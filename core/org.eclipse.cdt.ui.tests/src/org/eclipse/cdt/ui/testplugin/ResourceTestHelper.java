/*******************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.testplugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.junit.Assert;

/**
 * Copied from org.eclipse.jdt.text.tests.performance.
 *
 * @since 4.0
 */
public class ResourceTestHelper {
	public static final int FAIL_IF_EXISTS = 0;

	public static final int OVERWRITE_IF_EXISTS = 1;

	public static final int SKIP_IF_EXISTS = 2;

	private static final int DELETE_MAX_RETRY = 5;

	private static final long DELETE_RETRY_DELAY = 1000;

	public static void replicate(String src, String destPrefix, String destSuffix, int n, int ifExists)
			throws CoreException {
		for (int i = 0; i < n; i++) {
			copy(src, destPrefix + i + destSuffix, ifExists);
		}
	}

	public static void copy(String src, String dest) throws CoreException {
		copy(src, dest, FAIL_IF_EXISTS);
	}

	public static void copy(String src, String dest, int ifExists) throws CoreException {
		if (handleExisting(dest, ifExists))
			getFile(src).copy(new Path(dest), true, null);
	}

	private static boolean handleExisting(String dest, int ifExists) throws CoreException {
		IFile destFile = getFile(dest);
		switch (ifExists) {
		case FAIL_IF_EXISTS:
			if (destFile.exists())
				throw new IllegalArgumentException("Destination file exists: " + dest);
			return true;
		case OVERWRITE_IF_EXISTS:
			if (destFile.exists())
				delete(destFile);
			return true;
		case SKIP_IF_EXISTS:
			if (destFile.exists())
				return false;
			return true;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static IFile getFile(String path) {
		return getRoot().getFile(new Path(path));
	}

	public static void delete(String file) throws CoreException {
		delete(getFile(file));
	}

	private static void delete(IFile file) throws CoreException {
		CoreException x = null;
		for (int i = 0; i < DELETE_MAX_RETRY; i++) {
			try {
				file.delete(true, null);
				return;
			} catch (CoreException x0) {
				x = x0;
				try {
					Thread.sleep(DELETE_RETRY_DELAY);
				} catch (InterruptedException x1) {
					// should not happen
				}
			}
		}
		throw x;
	}

	public static void delete(String prefix, String suffix, int n) throws CoreException {
		for (int i = 0; i < n; i++)
			delete(prefix + i + suffix);
	}

	public static IFile findFile(String pathStr) {
		IFile file = getFile(pathStr);
		Assert.assertTrue(file != null && file.exists());
		return file;
	}

	public static IFile[] findFiles(String prefix, String suffix, int i, int n) {
		List<IFile> files = new ArrayList<>(n);
		for (int j = i; j < i + n; j++) {
			String path = prefix + j + suffix;
			files.add(findFile(path));
		}
		return files.toArray(new IFile[files.size()]);
	}

	public static StringBuffer read(String src) throws IOException, CoreException {
		return FileTool.read(new InputStreamReader(getFile(src).getContents()));
	}

	public static void write(String dest, final String content) throws IOException, CoreException {
		InputStream stream = new InputStream() {
			private Reader fReader = new StringReader(content);

			@Override
			public int read() throws IOException {
				return fReader.read();
			}
		};
		getFile(dest).create(stream, true, null);
	}

	public static void replicate(String src, String destPrefix, String destSuffix, int n, String srcName,
			String destNamePrefix, int ifExists) throws IOException, CoreException {
		StringBuffer s = read(src);
		List<Integer> positions = identifierPositions(s, srcName);
		for (int j = 0; j < n; j++) {
			String dest = destPrefix + j + destSuffix;
			if (handleExisting(dest, ifExists)) {
				StringBuffer c = new StringBuffer(s.toString());
				replacePositions(c, srcName.length(), destNamePrefix + j, positions);
				write(dest, c.toString());
			}
		}
	}

	public static void copy(String src, String dest, String srcName, String destName, int ifExists)
			throws IOException, CoreException {
		if (handleExisting(dest, ifExists)) {
			StringBuffer buf = read(src);
			List<Integer> positions = identifierPositions(buf, srcName);
			replacePositions(buf, srcName.length(), destName, positions);
			write(dest, buf.toString());
		}
	}

	private static void replacePositions(StringBuffer c, int origLength, String string, List<Integer> positions) {
		int offset = 0;
		for (Iterator<Integer> iter = positions.iterator(); iter.hasNext();) {
			int position = iter.next().intValue();
			c.replace(offset + position, offset + position + origLength, string);
			offset += string.length() - origLength;
		}
	}

	private static List<Integer> identifierPositions(StringBuffer buffer, String identifier) {
		List<Integer> positions = new ArrayList<>();
		int i = -1;
		while (true) {
			i = buffer.indexOf(identifier, i + 1);
			if (i == -1)
				break;
			if (i > 0 && Character.isJavaIdentifierPart(buffer.charAt(i - 1)))
				continue;
			if (i < buffer.length() - 1 && Character.isJavaIdentifierPart(buffer.charAt(i + identifier.length())))
				continue;
			positions.add(Integer.valueOf(i));
		}
		return positions;
	}

	private static IWorkspaceRoot getRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	public static void incrementalBuild() throws CoreException {
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
	}

	public static void fullBuild() throws CoreException {
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
	}

	public static boolean disableAutoBuilding() {
		return setAutoBuilding(false);
	}

	public static boolean enableAutoBuilding() {
		return setAutoBuilding(true);
	}

	public static boolean setAutoBuilding(boolean value) {
		Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
		boolean oldValue = preferences.getBoolean(ResourcesPlugin.PREF_AUTO_BUILDING);
		if (value != oldValue)
			preferences.setValue(ResourcesPlugin.PREF_AUTO_BUILDING, value);
		return oldValue;
	}

	public static IProject createExistingProject(String projectName) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		IProjectDescription description = workspace.newProjectDescription(projectName);
		description.setLocation(null);

		project.create(description, null);
		project.open(null);
		return project;
	}

	public static IProject createProjectFromZip(Plugin installationPlugin, String projectZip, String projectName)
			throws IOException, ZipException, CoreException {
		String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/";
		FileTool.unzip(new ZipFile(FileTool.getFileInPlugin(installationPlugin, new Path(projectZip))),
				new File(workspacePath));
		return createExistingProject(projectName);
	}

	public static IProject getProject(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		return workspace.getRoot().getProject(projectName);
	}

	public static boolean projectExists(String projectName) {
		return getProject(projectName).exists();
	}
}
