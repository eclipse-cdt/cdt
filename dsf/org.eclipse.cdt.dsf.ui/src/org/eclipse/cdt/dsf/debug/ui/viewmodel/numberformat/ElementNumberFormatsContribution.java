/*****************************************************************
 * Copyright (c) 2011 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.VMHandlerUtils;
import org.eclipse.cdt.dsf.ui.concurrent.SimpleDisplayExecutor;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Dynamic menu contribution that shows available number formats in the current
 * selection of the view.
 * 
 * @since 2.2
 */
public class ElementNumberFormatsContribution extends NumberFormatsContribution {

	static class SelectFormatAction extends Action {
		private final IElementFormatProvider fProvider;
		private final IPresentationContext fContext;
		private final IVMNode[] fNodes;
		private final Object fViewerInput;
		private final TreePath[] fElementPaths;
		private final String fFormatId;

		SelectFormatAction(IElementFormatProvider provider, IPresentationContext context, IVMNode[] nodes,
				Object viewerInput, TreePath[] elementPaths, String formatId) {
			super(formatId == null ? "Restore To Preference" : FormattedValueVMUtil.getFormatLabel(formatId), //$NON-NLS-1$
					formatId == null ? AS_PUSH_BUTTON : AS_RADIO_BUTTON);
			fProvider = provider;
			fContext = context;
			fNodes = nodes;
			fViewerInput = viewerInput;
			fElementPaths = elementPaths;
			fFormatId = formatId;
		}

		@Override
		public void run() {
			if (fFormatId == null) {
				fProvider.setActiveFormat(fContext, fNodes, fViewerInput, fElementPaths, fFormatId);
				return;
			}
			if (isChecked()) {
				fProvider.setActiveFormat(fContext, fNodes, fViewerInput, fElementPaths, fFormatId);
			}
		}
	}

	protected static IContributionItem[] NO_ITEMS = new IContributionItem[] {
		new ContributionItem() {
			@Override
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setEnabled(false);
				item.setText(MessagesForNumberFormat.NumberFormatContribution_EmptyFormatsList_label);
			}
	
			@Override
			public boolean isEnabled() {
				return false;
			}
		}
	};

	@Override
	protected IContributionItem[] getContributionItems() {
		ISelection selection = VMHandlerUtils.getSelection(fServiceLocator);
		if (selection == null || selection.isEmpty() || selection instanceof ITreeSelection == false) {
			return NO_ITEMS;
		}
		IVMProvider provider = VMHandlerUtils.getVMProviderForSelection(selection);
		if (FORMATS.size() == 0) {
			return NO_ITEMS;
		}
		IPresentationContext context = provider.getPresentationContext();
		TreePath[] elementPaths = ((ITreeSelection) selection).getPaths();
		IVMNode[] nodes = new IVMNode[elementPaths.length];
		final String[] formats = new String[elementPaths.length];
		Object viewerInput = null;
		if (context.getPart() instanceof AbstractDebugView) {
			Viewer viewer = ((AbstractDebugView)context.getPart()).getViewer();
			if (viewer != null) {
				viewerInput = viewer.getInput();
			}
		}
		// Here we keep using hard-coded formats, which are common formats.
		// We expect clients may add extra formats before and after these formats.
		// For details, please refer to 371012.
		// For now, we do not use vm provider's cache entry to get available formats
		// because it shows something extra than what we have been expecting. See 371012 comment #2.
		final List<SelectFormatAction> actions = new ArrayList<SelectFormatAction>(FORMATS.size());
		for (String formatId : FORMATS) {
			actions.add(new SelectFormatAction((IElementFormatProvider) provider,
					context, nodes, viewerInput, elementPaths, formatId));
		}
		CountingRequestMonitor crm = new CountingRequestMonitor(SimpleDisplayExecutor.getSimpleDisplayExecutor(Display.getDefault()), null) {
			@Override
			protected void handleCompleted() {
				String activeFormat = null;
				for (int i = 0; i < formats.length; i++) {
					if (i == 0) {
						activeFormat = formats[i];
					} else if (activeFormat != null
							&& activeFormat.equals(formats[i]) == false) {
						activeFormat = null;
						break;
					}
				}
				if (activeFormat != null) {
					for (int i = 0; i < actions.size(); i++) {
						if (activeFormat.equals(actions.get(i).fFormatId)) {
							actions.get(i).setChecked(true);
							break;
						}
					}
				}
			}
		};
		for (int i = 0; i < elementPaths.length; i++) {
			Object segment = elementPaths[i].getLastSegment();
			if (segment instanceof IVMContext) {
				nodes[i] = ((IVMContext) segment).getVMNode();
			} else {
				nodes[i] = null;
			}
			final int index = i;
			((IElementFormatProvider) provider).getActiveFormat(context, nodes[i], viewerInput, elementPaths[i],
					new DataRequestMonitor<String>(ImmediateExecutor.getInstance(), crm) {
				@Override
				protected void handleSuccess() {
					formats[index] = this.getData();
					super.handleSuccess();
				}
			});
		}
		crm.setDoneCount(elementPaths.length);
		int count = actions.size();
		IContributionItem[] items = new IContributionItem[count];
		for (int i = 0; i < actions.size(); i++) {
			items[i] = new ActionContributionItem(actions.get(i));
		}
		return items;
	}
}
