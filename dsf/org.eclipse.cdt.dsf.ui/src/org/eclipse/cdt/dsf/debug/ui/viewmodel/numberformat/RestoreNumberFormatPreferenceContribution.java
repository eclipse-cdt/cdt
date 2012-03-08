/*****************************************************************
 * Copyright (c) 2012 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *     Winnie Lai (Texas Instruments) - Allow contributions around number format menu (Bug 371012)
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.VMHandlerUtils;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;

/**
 * Dynamic menu contribution that restores the element number format in the current
 * selection of the view to view's preference.
 * 
 * We pull 'restore to preference' menu item out from ElementNumberFormatsContribution
 * so that clients can add extra contribution before or after it. See 371012.
 * @since 2.3
 */
public class RestoreNumberFormatPreferenceContribution extends ElementNumberFormatsContribution {
	@Override
	protected IContributionItem[] getContributionItems() {
		ISelection selection = VMHandlerUtils.getSelection(fServiceLocator);
		if (selection == null || selection.isEmpty() || selection instanceof ITreeSelection == false) {
			return new IContributionItem[0];
		}
		IVMProvider provider = VMHandlerUtils.getVMProviderForSelection(selection);
		IPresentationContext context = provider.getPresentationContext();
		TreePath[] elementPaths = ((ITreeSelection) selection).getPaths();
		IVMNode[] nodes = new IVMNode[elementPaths.length];
		Object viewerInput = null;
		if (context.getPart() instanceof AbstractDebugView) {
			Viewer viewer = ((AbstractDebugView)context.getPart()).getViewer();
			if (viewer != null) {
				viewerInput = viewer.getInput();
			}
		} 
		for (int i = 0; i < elementPaths.length; i++) {
			Object segment = elementPaths[i].getLastSegment();
			if (segment instanceof IVMContext) {
				nodes[i] = ((IVMContext) segment).getVMNode();
			} else {
				nodes[i] = null;
			}
		}
		IContributionItem[] items = new IContributionItem[1];
		items[0] = new ActionContributionItem(new SelectFormatAction(
				(IElementFormatProvider) provider, context, nodes, viewerInput, elementPaths, null));
		return items;
	}
}