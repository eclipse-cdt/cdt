/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;
import org.eclipse.osgi.util.NLS;

/**
 * A Change for creating a new file with the given name, content and encoding at the specified path.
 *
 * @author Emanuel Graf
 */
public class CreateFileChange extends ResourceChange {
	private String name;
	private final IPath path;
	private final String source;
	private final String encoding;

	public CreateFileChange(String name, IPath path, String source, String encoding) {
		super();
		this.name = name;
		this.path = path;
		this.source = source;
		this.encoding = encoding;
	}

	public CreateFileChange(IPath path, String source, String encoding) {
		this(null, path, source, encoding);
	}

	@Override
	public IResource getModifiedResource() {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	}

	@Override
	public String getName() {
		if (name == null) {
			return NLS.bind(Messages.CreateFileChange_CreateFile, path.toOSString());
		}
		return name;
	}

	@Override
	public void initializeValidationData(IProgressMonitor pm) {
	}

	@Override
	public RefactoringStatus isValid(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result= new RefactoringStatus();
		IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(path);

		URI location= file.getLocationURI();
		if (location == null) {
			result.addFatalError(NLS.bind(Messages.CreateFileChange_UnknownLoc, file.getFullPath().toString()));
			return result;
		}

		if (file.exists()) {
			result.addFatalError(NLS.bind(Messages.CreateFileChange_FileExists, file.getFullPath().toString()));
			return result;
		}
		return result;
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		InputStream is = new ByteArrayInputStream(source.getBytes());
		file.create(is, false, new SubProgressMonitor(pm, 1));
		if (encoding != null) {
			file.setCharset(encoding, new SubProgressMonitor(pm,1));
		}
		return new DeleteFileChange(file.getFullPath());
	}

	public String getSource() {
		return source;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}
}
