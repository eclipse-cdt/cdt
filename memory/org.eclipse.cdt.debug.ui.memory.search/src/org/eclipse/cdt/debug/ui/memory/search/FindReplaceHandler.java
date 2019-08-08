/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.search;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public class FindReplaceHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		if (part instanceof IMemoryRenderingSite) {
			IMemoryRenderingSite fView = (IMemoryRenderingSite) part;
			ISelection selection = fView.getSite().getSelectionProvider().getSelection();

			if (selection instanceof IStructuredSelection) {
				IStructuredSelection strucSel = (IStructuredSelection) selection;

				if (!strucSel.isEmpty()) {
					IMemoryBlock memBlock = null;
					Object obj = strucSel.getFirstElement();

					if (obj instanceof IMemoryRendering) {
						memBlock = ((IMemoryRendering) obj).getMemoryBlock();
					} else if (obj instanceof IMemoryBlock) {
						memBlock = (IMemoryBlock) obj;
					}

					if (memBlock instanceof IMemoryBlockExtension) {
						FindReplaceDialog dialog = new FindReplaceDialog(
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								(IMemoryBlockExtension) memBlock, fView, FindAction.getProperties(), null);
						dialog.open();
					}
				}
			}
		}

		return null;
	}
}
