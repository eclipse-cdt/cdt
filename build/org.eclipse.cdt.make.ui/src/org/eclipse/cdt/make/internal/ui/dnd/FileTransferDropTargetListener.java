/*******************************************************************************
 * Copyright (c) 2008, 2009 Andrew Gvozdev.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev (Quoin Inc.) - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.dnd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;

/**
 * {@code FileTransferDropTargetListener} handles dropping of files to Make
 * Target View. {@link FileTransfer} is used as the transfer agent. The files
 * passed are treated as make targets.
 *
 * @see AbstractContainerAreaDropAdapter
 * @see org.eclipse.swt.dnd.DropTargetListener
 */
public class FileTransferDropTargetListener extends AbstractContainerAreaDropAdapter {

	private final Viewer fViewer;

	/**
	 * Constructor setting a viewer such as TreeViewer to pull selection from later on.
	 * @param viewer - the viewer providing shell for UI.
	 */
	public FileTransferDropTargetListener(Viewer viewer) {
		fViewer = viewer;
	}

	/**
	 * @return the {@link Transfer} type that this listener can accept a
	 * drop operation for.
	 */
	@Override
	public Transfer getTransfer() {
		return FileTransfer.getInstance();
	}

	/**
	 * Initial drag operation. Only {@link DND#DROP_COPY} is supported for
	 * dropping files to Make Target View, same as for {@code dragOverOperation}.
	 *
	 * @param operation - incoming operation.
	 * @return changed operation.
	 */
	@Override
	public int dragEnterOperation(int operation) {
		return dragOverOperation(operation, null, null);
	}

	/**
	 * Operation of dragging over a drop target. Only {@link DND#DROP_COPY} is
	 * supported for dropping files to Make Target View.
	 *
	 * @param operation - incoming operation.
	 * @return changed operation.
	 */
	@Override
	public int dragOverOperation(int operation, IContainer dropContainer, Object dropTarget) {
		// This class is intended only for drag/drop between eclipse instances,
		// so DND_COPY always set and we don't bother checking if the target is the source
		if (operation!=DND.DROP_NONE) {
			return DND.DROP_COPY;
		}
		return operation;
	}

	/**
	 * Implementation of the actual drop of {@code dropObject} to {@code dropContainer}.
	 *
	 * @param dropObject - object to drop.
	 * @param dropContainer - container where to drop the object.
	 * @param operation - drop operation.
	 */
	@Override
	public void dropToContainer(Object dropObject, IContainer dropContainer, int operation) {
		if (dropObject instanceof String[] && ((String[])dropObject).length>0 && dropContainer!=null) {
			Shell shell = fViewer.getControl().getShell();
			createFileTargetsUI((String[])dropObject, dropContainer, operation, shell);
		}
	}

	/**
	 * Creates make targets array from filenames array. These will be loose
	 * targets not connected to global make target list managed by MakeTargetManager
	 *
	 * @param filenames - array of filenames. Each filename expected to be an
	 *        actual file otherwise a user gets a warning popup.
	 * @param dropContainer - a container where the targets are being created.
	 * @param shell - a shell to display warnings to user. If null, no warnings
	 *        are displayed.
	 * @return array of make targets.
	 */
	private static IMakeTarget[] prepareMakeTargetsFromFiles(String[] filenames,
			IContainer dropContainer, Shell shell) {
		List<IMakeTarget> makeTargetsList = new ArrayList<IMakeTarget>(filenames.length);

		int errorCount = 0;
		int nonFileCount = 0;
		for (String filepath : filenames) {
			IPath path = new Path(filepath);
			File file = path.toFile();
			if (file.isFile()) {
				String name = path.lastSegment();
				try {
					String buildCommand = MakeTargetDndUtil.getProjectBuildCommand(dropContainer.getProject());
					makeTargetsList.add(MakeTargetDndUtil.createMakeTarget(name, filepath, buildCommand, dropContainer));
				} catch (CoreException e) {
					errorCount++;
					MakeUIPlugin.log(e);
				}
			} else {
				nonFileCount++;
			}
		}

		if (shell != null) {
			if (errorCount > 0) {
				MessageDialog.openError(shell, MakeUIPlugin.getResourceString("MakeTargetDnD.title.createError"), //$NON-NLS-1$
					MakeUIPlugin.getResourceString("MakeTargetDnD.message.createError")); //$NON-NLS-1$
			}
			if (nonFileCount > 0) {
				MessageDialog.openInformation(shell, MakeUIPlugin.getResourceString("MakeTargetDnD.title.createInfo"), //$NON-NLS-1$
					MakeUIPlugin.getResourceString("MakeTargetDnD.message.createNonFileTargetAttempt")); //$NON-NLS-1$
			}
		}

		return makeTargetsList.toArray(new IMakeTarget[makeTargetsList.size()]);
	}

	/**
	 * Creates make targets from array of filenames in Make Target View. Each
	 * file will be a separate target in the view.
	 *
	 * @param filenames - array of filenames. Each filename expected to be an
	 *        actual file otherwise a user gets a warning popup.
	 * @param dropContainer - a container where the targets are being created.
	 * @param operation - drop/paste operation.
	 * @param shell - a shell to display warnings to the user.
	 */
	public static void createFileTargetsUI(String[] filenames, IContainer dropContainer, int operation, Shell shell) {
		IMakeTarget[] makeTargets = prepareMakeTargetsFromFiles(filenames, dropContainer, shell);
		MakeTargetDndUtil.copyTargets(makeTargets, dropContainer, operation, shell);
	}
}
