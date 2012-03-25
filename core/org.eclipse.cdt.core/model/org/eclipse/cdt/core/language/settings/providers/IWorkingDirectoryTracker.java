/*******************************************************************************
 * Copyright (c) 2012, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.language.settings.providers;

import java.net.URI;

import org.eclipse.core.resources.IFile;

/**
 * TODO
 * @since 5.4
 */
public interface IWorkingDirectoryTracker {
	// TODO - define the API

	public URI getWorkingDirectoryURI();
	public IFile findFileName(String partialLoc);

	/*
	public URI findFileURI(String partialLoc);
	public URI findFolderURI(String partialLoc);

	or
	public File findFile(String partialLoc); // best choice
	public Folder findFolder(String partialLoc); // best choice

	or
	public List<URI> findFileURI(String partialLoc); // all candidates
	public List<URI> findFolderURI(String partialLoc); // all candidates

	 */
}
