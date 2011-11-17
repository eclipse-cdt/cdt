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

import org.eclipse.cdt.make.core.IMakeCommonBuildInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

/**
 * {@code TextTransferDragSourceListener} supports dragging of selected make
 * targets from Make Target View. The targets converted to make commands in
 * order to be presented as plain text. Each command will be one line of text.
 * {@link TextTransfer} is used as the transfer agent.
 *
 * @see AbstractSelectionDragAdapter
 * @see org.eclipse.swt.dnd.DragSourceListener
 */
public class TextTransferDragSourceListener extends AbstractSelectionDragAdapter {

	/**
	 * Constructor setting selection provider.
	 * @param provider - selection provider.
	 */
	public TextTransferDragSourceListener(ISelectionProvider provider) {
		super(provider);
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
	 * Checks if the elements contained in the given selection can be dragged.
	 *
	 * @param selection - the selected elements to be dragged.
	 * @return {@code true} if the selection can be dragged.
	 */
	@Override
	protected boolean isDragable(ISelection selection) {
		return MakeTargetDndUtil.isDragable(selection);
	}

	/**
	 * A custom action executed during drag initialization.
	 *
	 * @param selection - the selected elements to be dragged.
	 */
	@Override
	protected void dragInit(ISelection selection) {
		// no special action is required
	}

	/**
	 * Prepare the selection to be passed via drag and drop actions.
	 *
	 * @param selection - the selected elements to be dragged.
	 * @return data to be passed. The data is a multiline string each line
	 *         containing make target command.
	 */
	@Override
	protected Object prepareDataForTransfer(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			return convertSelectedMakeTargetsToText((IStructuredSelection) selection);
		}
		return null;
	}

	/**
	 * A custom action to finish the drag.
	 */
	@Override
	protected void dragDone() {
		// no special action is required
	}

	/**
	 * Convert selected make targets to textual representation. Each make command
	 * is presented as a line in the resulting multiline text.
	 *
	 * @param selection - selected make targets in Make Target View.
	 * @return make targets as miltiline text.
	 */
	public static String convertSelectedMakeTargetsToText(IStructuredSelection selection) {
		String targetsText=""; //$NON-NLS-1$
		for (Object selectionItem : selection.toList()) {
			if (selectionItem instanceof IMakeTarget) {
				IMakeTarget makeTarget = (IMakeTarget)selectionItem;
				String buildCommand;
				if (makeTarget.isDefaultBuildCmd()) {
					buildCommand = MakeTargetDndUtil.getProjectBuildCommand(makeTarget.getProject());
				} else {
					buildCommand =makeTarget.getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND,
						MakeTargetDndUtil.DEFAULT_BUILD_COMMAND);
				}
				String buildCommandArguments = makeTarget.getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, "").trim(); //$NON-NLS-1$
				if (buildCommandArguments.length()>0) {
					buildCommandArguments = ' ' + buildCommandArguments;
				}
				String buildTarget = makeTarget.getBuildAttribute(IMakeTarget.BUILD_TARGET, "").trim(); //$NON-NLS-1$
				if (buildTarget.length()>0) {
					buildTarget = ' ' + buildTarget;
				}
				targetsText = targetsText + buildCommand + buildCommandArguments + buildTarget
					+ System.getProperty("line.separator"); //$NON-NLS-1$
			}
		}
		return targetsText;
	}

}
