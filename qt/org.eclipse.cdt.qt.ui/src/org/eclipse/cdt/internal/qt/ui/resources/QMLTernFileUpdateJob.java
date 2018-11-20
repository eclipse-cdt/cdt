/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.script.ScriptException;

import org.eclipse.cdt.internal.qt.ui.Activator;
import org.eclipse.cdt.qt.core.IQMLAnalyzer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class QMLTernFileUpdateJob extends Job {

	private List<IResourceDelta> deltaList;
	private final IQMLAnalyzer analyzer = Activator.getService(IQMLAnalyzer.class);

	public QMLTernFileUpdateJob(List<IResourceDelta> deltas) {
		super("Add/Remove Files in Tern"); //$NON-NLS-1$
		this.deltaList = deltas;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		for (IResourceDelta delta : deltaList) {
			IResource resource = delta.getResource();
			String fileName = resource.getFullPath().toString().substring(1);

			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				try {
					if ((delta.getKind() & IResourceDelta.ADDED) > 0) {
						analyzer.addFile(fileName, readFileContents(file.getContents()));
					} else if ((delta.getKind() & IResourceDelta.REMOVED) > 0) {
						analyzer.deleteFile(fileName);
					}
				} catch (NoSuchMethodException e) {
					Activator.log(e);
				} catch (ScriptException e) {
					Activator.log(e);
				} catch (IOException e) {
					Activator.log(e);
				} catch (CoreException e) {
					Activator.log(e);
				}
			}
		}
		return Status.OK_STATUS;
	}

	private String readFileContents(InputStream stream) throws IOException {
		StringBuilder sb = new StringBuilder();
		int read;
		while ((read = stream.read()) != -1) {
			sb.append((char) read);
		}
		return sb.toString();
	}
}
