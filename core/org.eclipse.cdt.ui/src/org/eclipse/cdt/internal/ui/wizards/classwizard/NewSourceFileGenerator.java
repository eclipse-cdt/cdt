/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

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

    private static final String HEADER_EXT = ".h"; //$NON-NLS-1$
    private static final String SOURCE_EXT = ".cpp"; //$NON-NLS-1$
    private static boolean fUseIncludeGuard = true;
    private static final String fLineDelimiter = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

    public static String getLineDelimiter() {
        //TODO line delimiter part of code template prefs?
        return fLineDelimiter;
    }
    
    public static String generateHeaderFileNameFromClass(String className) {
        //TODO eventually make this a prefs option - filename pattern
        return className + HEADER_EXT;
    }

    public static String generateSourceFileNameFromClass(String className) {
        //TODO eventually make this a prefs option - filename pattern
        return className + SOURCE_EXT;
    }

    public static IFile createHeaderFile(IPath filePath, boolean force, IProgressMonitor monitor) throws CoreException {
        //TODO should use code templates
        ByteArrayInputStream stream;
        if (fUseIncludeGuard) {
            String includeGuardSymbol = generateIncludeGuardSymbol(filePath);
            StringBuffer buf = new StringBuffer();
            buf.append("#ifndef "); //$NON-NLS-1$
            buf.append(includeGuardSymbol);
            buf.append(fLineDelimiter);
            buf.append("#define "); //$NON-NLS-1$
            buf.append(includeGuardSymbol);
            buf.append(fLineDelimiter);
            buf.append(fLineDelimiter);
            buf.append("#endif //"); //$NON-NLS-1$
            buf.append(includeGuardSymbol);
            buf.append(fLineDelimiter);
            stream = new ByteArrayInputStream(buf.toString().getBytes());
        } else {
            stream = new ByteArrayInputStream(new byte[0]);
        }
		return createNewFile(filePath, stream, force, monitor);
    }
    
    private static String generateIncludeGuardSymbol(IPath headerPath) {
        //TODO eventually make this a prefs option - filename pattern or
        // unique id/incremental value
        String name = headerPath.lastSegment();
        if (name != null) {
            //convert to upper case and remove invalid characters
            //eg convert foo.h --> _FOO_H_
            StringBuffer buf = new StringBuffer();
            buf.append('_');
            for (int i = 0; i < name.length(); ++i) {
                char ch = name.charAt(i);
                if (Character.isLetterOrDigit(ch)) {
                    buf.append(Character.toUpperCase(ch));
                } else if (ch == '.' || ch == '_') {
                    buf.append('_');
                }
            }
            buf.append('_');
            return buf.toString();
        }
        return null;
    }

    public static IFile createSourceFile(IPath filePath, boolean useIncludeGuard, boolean force, IProgressMonitor monitor) throws CoreException {
        //TODO should use code templates
        ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
		return createNewFile(filePath, stream, force, monitor);
    }

    private static IFile createNewFile(IPath newFilePath, InputStream contents, boolean force, IProgressMonitor monitor) throws CoreException {
        int totalWork = 100;
        int createFileWork = totalWork;

        monitor.beginTask(NewClassWizardMessages.getString("NewClassCodeGeneration.createFile.task"), totalWork); //$NON-NLS-1$

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
