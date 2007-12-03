/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.filewizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.dialogs.ContainerGenerator;

public class NewSourceFileGenerator {

    //TODO these should all be configurable in prefs
    private static final String HEADER_EXT = ".h"; //$NON-NLS-1$
    private static final String SOURCE_EXT = ".cpp"; //$NON-NLS-1$

    public static String generateHeaderFileNameFromClass(String className) {
        //TODO eventually make this a prefs option - filename pattern
        return className + HEADER_EXT;
    }

    public static String generateSourceFileNameFromClass(String className) {
        //TODO eventually make this a prefs option - filename pattern
        return className + SOURCE_EXT;
    }

    public static IFile createHeaderFile(IPath filePath, boolean force, IProgressMonitor monitor) throws CoreException {
		return createEmptyFile(filePath, force, monitor);
    }
    
    public static IFile createSourceFile(IPath filePath, boolean force, IProgressMonitor monitor) throws CoreException {
		return createEmptyFile(filePath, force, monitor);
    }

    public static IFile createEmptyFile(IPath filePath, boolean force, IProgressMonitor monitor) throws CoreException {
        ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
		return createNewFile(filePath, stream, force, monitor);
    }

    private static IFile createNewFile(IPath newFilePath, InputStream contents, boolean force, IProgressMonitor monitor) throws CoreException {
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

    private static void createFile(IFile fileHandle, InputStream contents, boolean force, IProgressMonitor monitor) throws CoreException {
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
