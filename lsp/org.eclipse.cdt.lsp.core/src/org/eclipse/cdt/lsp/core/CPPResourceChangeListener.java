/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.core;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;

/**
 * A resource listener used to generate FileEvents, as part of the LSP. This
 * only listens to Added, Changed, Removed event on a specific project that as a
 * C/C++ language server started.
 */
@SuppressWarnings("restriction")
final class CPPResourceChangeListener implements IResourceChangeListener {
	private final IProject fProject;

	CPPResourceChangeListener(IProject project) {
		fProject = project;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		LanguageServerDefinition definition = LanguageServersRegistry.getInstance()
				.getDefinition(CPPStreamConnectionProvider.ID);
		LanguageServerWrapper wrapper = getLanguageSeverWrapper(definition);
		if (event.getType() != IResourceChangeEvent.POST_CHANGE || !isRelevantDelta(event.getDelta())
				|| wrapper == null) {
			return;
		}

		sendFileEvents(wrapper, createFileEventsFromResourceEvent(event));
	}

	private static void sendFileEvents(LanguageServerWrapper wrapper, List<FileEvent> fileEvents) {
		if (!fileEvents.isEmpty()) {
			DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(fileEvents);
			wrapper.getServer().getWorkspaceService().didChangeWatchedFiles(params);
		}
	}

	private static List<FileEvent> createFileEventsFromResourceEvent(IResourceChangeEvent event) {
		List<FileEvent> fileEvents = new ArrayList<>();
		try {
			event.getDelta().accept((delta) -> {
				if (delta.getResource() instanceof IFile && isRelevantDelta(delta)) {
					FileEvent fileEvent = createFileEventFromDelta(delta);
					if (fileEvent != null) {
						fileEvents.add(fileEvent);
					}
				}
				return true;
			}, false);
		} catch (CoreException e) {
			// Do nothing
		}
		return fileEvents;
	}

	private LanguageServerWrapper getLanguageSeverWrapper(LanguageServerDefinition definition) {
		try {
			return LanguageServiceAccessor.getLSWrapperForConnection(fProject, definition);
		} catch (IOException e) {
			// Do nothing
			return null;
		}
	}

	private static boolean isRelevantDelta(IResourceDelta delta) {
		int kind = delta.getKind();
		int flags = delta.getFlags();
		if (delta.getResource() instanceof IFile && kind == IResourceDelta.CHANGED) {
			return (flags & IResourceDelta.CONTENT) != 0;
		}

		return kind == IResourceDelta.ADDED || kind == IResourceDelta.CHANGED || kind == IResourceDelta.REMOVED;
	}

	private static FileEvent createFileEventFromDelta(IResourceDelta delta) {
		URI locationURI = delta.getResource().getLocationURI();
		if (locationURI == null) {
			return null;
		}

		FileChangeType changeType = null;
		if (delta.getKind() == IResourceDelta.ADDED) {
			changeType = FileChangeType.Created;
		} else if (delta.getKind() == IResourceDelta.CHANGED) {
			changeType = FileChangeType.Changed;
		} else if (delta.getKind() == IResourceDelta.REMOVED) {
			changeType = FileChangeType.Deleted;
		} else {
			throw new IllegalStateException("Unsupported resource delta kind: " + delta.getKind()); //$NON-NLS-1$
		}

		return new FileEvent(locationURI.toString(), changeType);
	}
}