/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.cdt.debug.internal.core.sourcelookup.CDirectorySourceLocation;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CProjectSourceLocation;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLocator;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Enter type comment.
 * 
 * @since Jul 14, 2003
 */
public class SourceLookupFactory
{
	public static IProjectSourceLocation createProjectSourceLocation( IProject project )
	{
		return new CProjectSourceLocation( project );
	}

	public static IProjectSourceLocation createProjectSourceLocation( IProject project, boolean generated )
	{
		return new CProjectSourceLocation( project, generated );
	}

	public static IDirectorySourceLocation createDirectorySourceLocation( IPath directory, IPath association, boolean searchSubfolders )
	{
		return new CDirectorySourceLocation( directory, association, searchSubfolders );
	}

	public static ICSourceLocator createSourceLocator( IProject project )
	{
		return new CSourceManager( new CSourceLocator( project ) );
	}
}
