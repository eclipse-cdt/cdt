/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial implementation (159833)
 *******************************************************************************/

package org.eclipse.cdt.debug.core.sourcelookup;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.internal.core.model.ExternalTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;

public class AbsolutePathSourceContainer extends AbstractSourceContainer {
	/**
	 * Unique identifier for the absolute source container type
	 * (value <code>org.eclipse.cdt.debug.core.containerType.absolutePath</code>).
	 */
	public static final String TYPE_ID = CDebugCorePlugin.getUniqueIdentifier() + ".containerType.absolutePath";	 //$NON-NLS-1$

	private Object[] findSourceElementByFile(File file) {
		IFile[] wfiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(file.getAbsolutePath()));
		if (wfiles.length > 0)
			return wfiles;

		try {
			// Check the canonical path as well to support case insensitive file
			// systems like Windows.
			wfiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(file.getCanonicalPath()));
			if (wfiles.length > 0)
				return wfiles;
			
			// The file is not already in the workspace so try to create an external translation unit for it.
			String projectName = getDirector().getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			ICProject project = CoreModel.getDefault().getCModel().getCProject(projectName);
			if (project != null)
			{
				IPath path = Path.fromOSString(file.getCanonicalPath());
				String id = CoreModel.getRegistedContentTypeId(project.getProject(), path.lastSegment());
				return new ExternalTranslationUnit[] { new ExternalTranslationUnit(project, new Path(file.getCanonicalPath()), id) };
			}
		} catch (IOException e) { // ignore if getCanonicalPath throws
		} catch (CoreException e) {
		}

		// If we can't create an ETU then fall back on LocalFileStorage.
		return new LocalFileStorage[] { new LocalFileStorage(file) };
	}

	public boolean isValidAbsoluteFilePath( String name )
	{
		return isValidAbsoluteFilePath( new File(name) );	
	}

	public boolean isValidAbsoluteFilePath( File file )
	{
		return file.isAbsolute() && file.exists() && file.isFile();	
	}
	
	public Object[] findSourceElements( String name ) throws CoreException {
		if ( name != null ) {
			File file = new File( name );
			if ( isValidAbsoluteFilePath(file) ) {
				return findSourceElementByFile( file );
			}
		}
		return new Object[0];
	}

	public String getName() {
		return SourceLookupMessages.getString( "AbsolutePathSourceContainer.0" );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType( TYPE_ID );
	}

}
