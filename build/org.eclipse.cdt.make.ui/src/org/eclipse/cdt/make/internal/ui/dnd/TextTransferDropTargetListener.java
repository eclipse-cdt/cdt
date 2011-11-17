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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;

import com.ibm.icu.text.MessageFormat;

/**
 * {@code TextTransferDropTargetListener} handles dropping of selected text to
 * Make Target View. Each line of miltiline text passed is treated as separate
 * make target command. {@link TextTransfer} is used as the transfer agent.
 *
 * @see AbstractContainerAreaDropAdapter
 * @see org.eclipse.swt.dnd.DropTargetListener
 */
public class TextTransferDropTargetListener extends AbstractContainerAreaDropAdapter {

	Viewer fViewer;

	/**
	 * Constructor setting a viewer such as TreeViewer to pull selection from later on.
	 * @param viewer - the viewer providing shell for UI.
	 */
	public TextTransferDropTargetListener(Viewer viewer) {
		fViewer = viewer;
	}

	/**
	 * @return the {@link Transfer} type that this listener can accept a
	 * drop operation for.
	 */
	@Override
	public Transfer getTransfer() {
		return TextTransfer.getInstance();
	}

	/**
	 * Initial drag operation. Only {@link DND#DROP_COPY} is supported for
	 * dropping text to Make Target View, same as for {@code dragOverOperation}.
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
	 * supported for dropping text to Make Target View.
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
		if (dropObject instanceof String && ((String)dropObject).length()>0  && dropContainer != null) {
			createMultilineTargetsUI((String)dropObject, dropContainer, operation,
				fViewer.getControl().getShell());
		}
	}

	/**
	 * Convert multiline text to array of {@code IMakeTarget}s. Each line is
	 * interpreted as one separate make command.
	 *
	 * @param multilineText - input text.
	 * @param container - container where the targets will belong.
	 * @return resulting array of {@code IMakeTarget}s.
	 */
	private static IMakeTarget[] prepareMakeTargetsFromString(String multilineText, IContainer container) {
		if (container!=null) {
			String[] lines = multilineText.split("[\n\r]"); //$NON-NLS-1$
			List<IMakeTarget> makeTargets = new ArrayList<IMakeTarget>(lines.length);
			for (String command : lines) {
				command = command.trim();
				if (command.length() > 0) {
					String name = command;
					String buildCommand = command;
					String buildTarget = null;
					String defaultBuildCommand = MakeTargetDndUtil.getProjectBuildCommand(container.getProject());
					if (command.startsWith(defaultBuildCommand+" ")) { //$NON-NLS-1$
						buildCommand = defaultBuildCommand;
						buildTarget = command.substring(defaultBuildCommand.length()+1).trim();
						name = buildTarget;
					}
					try {
						makeTargets.add(MakeTargetDndUtil.createMakeTarget(name, buildTarget, buildCommand, container));
					} catch (CoreException e) {
						// Ignore failed targets
						MakeUIPlugin.log(e);
					}
				}
			}
			return makeTargets.toArray(new IMakeTarget[makeTargets.size()]);
		}
		return null;
	}

	/**
	 * Combined operation of creating make targets in Make Target View from
	 * multiline text. The method will ask a confirmation if user tries to drop
	 * more then 1 target to prevent easily made mistake of unintended copying
	 * of old contents of the clipboard.
	 *
	 * @param multilineText - input make target commands in textual form.
	 * @param dropContainer - container where add the targets.
	 * @param operation - operation such as copying or moving. Must be a
	 *        {@link org.eclipse.swt.dnd.DND} operation.
	 * @param shell - a shell to display progress of operation to user.
	 *
	 * @see DND#DROP_NONE
	 * @see DND#DROP_COPY
	 * @see DND#DROP_MOVE
	 * @see DND#DROP_LINK
	 */
	public static void createMultilineTargetsUI(String multilineText, IContainer dropContainer,
		int operation, Shell shell) {

		IMakeTarget[] makeTargets = prepareMakeTargetsFromString(multilineText, dropContainer);
		boolean confirmed = true;
		if (makeTargets.length > 1) {
			String title = MakeUIPlugin.getResourceString("MakeTargetDnD.title.createFromTextConfirm"); //$NON-NLS-1$
			String question = MessageFormat.format(MakeUIPlugin.getResourceString("MakeTargetDnD.message.createFromTextConfirm"), //$NON-NLS-1$
				new Object[] { new Integer(makeTargets.length) });

			String topTargets = ""; //$NON-NLS-1$
			for (int i=0;i<makeTargets.length;i++) {
				// limit dimensions of the confirm dialog
				final int HEIGHT_LIMIT = 20;
				final int LENGTH_LIMIT = 200;
				if (i > HEIGHT_LIMIT) {
					topTargets = topTargets + "..."; //$NON-NLS-1$
					break;
				}
				String name = makeTargets[i].getName();
				if (name.length() > LENGTH_LIMIT) {
					name = name.substring(0,LENGTH_LIMIT-3)+"..."; //$NON-NLS-1$
				}
				topTargets = topTargets + name + "\n"; //$NON-NLS-1$
			}

			confirmed = MessageDialog.openConfirm(shell, title, question + topTargets);
		}
		if (confirmed) {
			MakeTargetDndUtil.copyTargets(makeTargets, dropContainer, operation, shell);
		}
	}

}
