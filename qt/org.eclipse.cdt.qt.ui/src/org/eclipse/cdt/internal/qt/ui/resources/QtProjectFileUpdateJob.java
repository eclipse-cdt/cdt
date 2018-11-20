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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.qt.ui.Activator;
import org.eclipse.cdt.internal.qt.ui.editor.QtProjectFileKeyword;
import org.eclipse.cdt.internal.qt.ui.pro.parser.QtProjectFileModifier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * Job that calls the <code>QtProjectFileModifier</code> after changes to resources found in Qt Projects in order to update their
 * <code>SOURCES</code> variable.
 */
public class QtProjectFileUpdateJob extends Job {

	private List<IResourceDelta> deltaList;

	public QtProjectFileUpdateJob(List<IResourceDelta> deltas) {
		super("Update Qt Project File(s)"); //$NON-NLS-1$
		this.deltaList = deltas;
	}

	private IFile findQtProjectFile(IProject project) throws CoreException {
		for (IResource member : project.members()) {
			if (member.getType() == IResource.FILE && member.getFileExtension().equals("pro")) { //$NON-NLS-1$
				return (IFile) member;
			}
		}
		return null;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// Cache the project files so we don't continuously open them
		Map<IProject, QtProjectFileModifier> modifierMap = new HashMap<>();
		Map<IProject, IFile> projectFileMap = new HashMap<>();

		for (IResourceDelta delta : deltaList) {
			IResource resource = delta.getResource();
			IProject project = resource.getProject();
			QtProjectFileModifier modifier = modifierMap.get(project);

			if (modifier == null) {
				IFile proFile = null;
				try {
					proFile = findQtProjectFile(project);
				} catch (CoreException e) {
					Activator.log("Unable to find Qt Project File", e); //$NON-NLS-1$
				}

				// We can't update a project file if it doesn't exist
				if (proFile == null) {
					continue;
				}

				// Cache the project file under its containing project and read its contents into a Document.
				projectFileMap.put(project, proFile);
				StringBuilder sb = new StringBuilder();
				try (InputStream is = proFile.getContents()) {
					int read = -1;
					while ((read = is.read()) > 0) {
						sb.append((char) read);
					}
					IDocument document = new Document(sb.toString());
					modifier = new QtProjectFileModifier(document);
					modifierMap.put(project, modifier);
				} catch (IOException e) {
					Activator.log(e);
					break;
				} catch (CoreException e) {
					Activator.log(e);
					break;
				}
			}

			// Determine from the file extension where we should add this resource
			String variableKeyword = null;
			if ("cpp".equals(resource.getFileExtension())) { //$NON-NLS-1$
				variableKeyword = QtProjectFileKeyword.VAR_SOURCES.getKeyword();
			} else if ("h".equals(resource.getFileExtension())) { //$NON-NLS-1$
				variableKeyword = QtProjectFileKeyword.VAR_HEADERS.getKeyword();
			}

			if ((delta.getFlags() & IResourceDelta.MOVED_FROM) > 0) {
				// Resource was moved from another location.
				if (project.getFullPath().isPrefixOf(delta.getMovedFromPath())) {
					String oldValue = delta.getMovedFromPath().makeRelativeTo(project.getFullPath()).toString();
					String newValue = resource.getProjectRelativePath().toString();

					if (modifier.replaceVariableValue(variableKeyword, oldValue, newValue)) {
						// If we successfully replaced the variable, continue. If this line is not executed it means we failed to
						// replace and the file will be added in the subsequent code for the ADDED case.
						continue;
					}
				}
			} else if ((delta.getFlags() & IResourceDelta.MOVED_TO) > 0) {
				// Somewhat edge-case where a file from one Qt Project was moved to a different Qt Project.
				if (project.getFullPath().isPrefixOf(delta.getMovedToPath())) {
					// Getting here means that the replace was taken care of by the previous code. Otherwise, it will be removed in
					// the subsequent code for the REMOVED case.
					continue;
				}
			}

			if ((delta.getKind() & IResourceDelta.ADDED) > 0) {
				String value = resource.getProjectRelativePath().toString();
				if (value != null) {
					modifier.addVariableValue(variableKeyword, value);
				}
			} else if ((delta.getKind() & IResourceDelta.REMOVED) > 0) {
				String value = resource.getProjectRelativePath().toString();
				if (value != null) {
					modifier.removeVariableValue(variableKeyword, value);
				}
			}
		}

		// Write all documents to their respective files
		for (IProject project : projectFileMap.keySet()) {
			IFile file = projectFileMap.get(project);
			IDocument document = modifierMap.get(project).getDocument();

			try {
				file.setContents(new ByteArrayInputStream(document.get().getBytes()), 0, null);
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
		return Status.OK_STATUS;
	}
}
