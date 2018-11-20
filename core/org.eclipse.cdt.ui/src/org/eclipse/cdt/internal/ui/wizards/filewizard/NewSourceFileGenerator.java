/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.filewizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.cdt.internal.ui.util.NameComposer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.ui.dialogs.ContainerGenerator;

public class NewSourceFileGenerator {
	/**
	 * Creates a header file name from the given class name. This is the file name
	 * to be used when the class is created. eg. "MyClass" -> "MyClass.h"
	 *
	 * @param className the class name
	 * @return the header file name for the given class
	 */
	public static String generateHeaderFileNameFromClass(String className) {
		IPreferencesService preferences = Platform.getPreferencesService();
		int capitalization = preferences.getInt(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_CPP_HEADER_CAPITALIZATION,
				PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL, null);
		String wordDelimiter = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_CPP_HEADER_WORD_DELIMITER, "", null); //$NON-NLS-1$
		String prefix = preferences.getString(CUIPlugin.PLUGIN_ID, PreferenceConstants.NAME_STYLE_CPP_HEADER_PREFIX, "", //$NON-NLS-1$
				null);
		String suffix = preferences.getString(CUIPlugin.PLUGIN_ID, PreferenceConstants.NAME_STYLE_CPP_HEADER_SUFFIX,
				".h", null); //$NON-NLS-1$
		NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
		return composer.compose(className);
	}

	/**
	 * Creates a source file name from the given class name. This is the file name
	 * to be used when the class is created. e.g. "MyClass" -> "MyClass.cpp"
	 *
	 * @param className the class name
	 * @return the source file name for the given class
	 */
	public static String generateSourceFileNameFromClass(String className) {
		IPreferencesService preferences = Platform.getPreferencesService();
		int capitalization = preferences.getInt(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_CPP_SOURCE_CAPITALIZATION,
				PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL, null);
		String wordDelimiter = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_CPP_SOURCE_WORD_DELIMITER, "", null); //$NON-NLS-1$
		String prefix = preferences.getString(CUIPlugin.PLUGIN_ID, PreferenceConstants.NAME_STYLE_CPP_SOURCE_PREFIX, "", //$NON-NLS-1$
				null);
		String suffix = preferences.getString(CUIPlugin.PLUGIN_ID, PreferenceConstants.NAME_STYLE_CPP_SOURCE_SUFFIX,
				".cpp", null); //$NON-NLS-1$
		NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
		return composer.compose(className);
	}

	/**
	 * Creates a file name for the unit test from the given class name. This is the file name
	 * to be used when the test is created. e.g. "MyClass" -> "MyClass_test.cpp"
	 *
	 * @param className the class name
	 * @return the test file name for the given class
	 */
	public static String generateTestFileNameFromClass(String className) {
		IPreferencesService preferences = Platform.getPreferencesService();
		int capitalization = preferences.getInt(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_CPP_TEST_CAPITALIZATION,
				PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL, null);
		String wordDelimiter = preferences.getString(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.NAME_STYLE_CPP_TEST_WORD_DELIMITER, "", null); //$NON-NLS-1$
		String prefix = preferences.getString(CUIPlugin.PLUGIN_ID, PreferenceConstants.NAME_STYLE_CPP_TEST_PREFIX, "", //$NON-NLS-1$
				null);
		String suffix = preferences.getString(CUIPlugin.PLUGIN_ID, PreferenceConstants.NAME_STYLE_CPP_TEST_SUFFIX,
				"_test.cpp", null); //$NON-NLS-1$
		NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
		return composer.compose(className);
	}

	public static IFile createHeaderFile(IPath filePath, boolean force, IProgressMonitor monitor) throws CoreException {
		return createEmptyFile(filePath, force, monitor);
	}

	public static IFile createSourceFile(IPath filePath, boolean force, IProgressMonitor monitor) throws CoreException {
		return createEmptyFile(filePath, force, monitor);
	}

	public static IFile createTestFile(IPath filePath, boolean force, IProgressMonitor monitor) throws CoreException {
		return createEmptyFile(filePath, force, monitor);
	}

	public static IFile createEmptyFile(IPath filePath, boolean force, IProgressMonitor monitor) throws CoreException {
		ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
		return createNewFile(filePath, stream, force, monitor);
	}

	private static IFile createNewFile(IPath newFilePath, InputStream contents, boolean force, IProgressMonitor monitor)
			throws CoreException {
		int totalWork = 100;
		int createFileWork = totalWork;

		monitor.beginTask(NewFileWizardMessages.NewSourceFileGenerator_createFile_task, totalWork);

		IWorkspaceRoot root = CUIPlugin.getWorkspace().getRoot();
		IFile newFile = root.getFileForLocation(newFilePath);
		if (newFile == null)
			newFile = root.getFile(newFilePath);
		if (newFile.exists()) {
			monitor.done();
			return newFile;
		}

		if (newFilePath.segmentCount() > 1) {
			IPath containerPath = newFilePath.removeLastSegments(1);
			if (root.getContainerForLocation(containerPath) == null) {
				int containerWork = totalWork / 2;
				createFileWork = totalWork / 2;
				ContainerGenerator generator = new ContainerGenerator(containerPath);
				generator.generateContainer(new SubProgressMonitor(monitor, containerWork));
			}
		}

		createFile(newFile, contents, force, new SubProgressMonitor(monitor, createFileWork));
		monitor.done();

		return newFile;
	}

	private static void createFile(IFile fileHandle, InputStream contents, boolean force, IProgressMonitor monitor)
			throws CoreException {
		if (contents == null)
			contents = new ByteArrayInputStream(new byte[0]);

		try {
			fileHandle.create(contents, force, monitor);
		} catch (CoreException e) {
			// If the file already existed locally, just refresh to get contents
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
				fileHandle.refreshLocal(IResource.DEPTH_ZERO, null);
			else
				throw e;
		}

		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}
}
