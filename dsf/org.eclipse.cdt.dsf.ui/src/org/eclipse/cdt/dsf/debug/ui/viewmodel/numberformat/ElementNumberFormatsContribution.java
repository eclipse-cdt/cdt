/*****************************************************************
 * Copyright (c) 2011, 2014 Texas Instruments and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *     Marc Khouzam (Ericsson) - Base available formats on each element (Bug 439624)
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.VMHandlerUtils;
import org.eclipse.cdt.dsf.ui.concurrent.SimpleDisplayExecutor;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICacheEntry;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICachingVMProviderExtension2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
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
			super(formatId == null ? MessagesForNumberFormat.ElementNumberFormatContribution_RestoreToPreference_label
					: FormattedValueVMUtil.getFormatLabel(formatId),
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

	protected static IContributionItem[] NO_ITEMS = new IContributionItem[] { new ContributionItem() {
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
	} };

	@Override
	protected IContributionItem[] getContributionItems() {
		ISelection selection = VMHandlerUtils.getSelection(fServiceLocator);
		if (selection == null || selection.isEmpty() || selection instanceof ITreeSelection == false) {
			return NO_ITEMS;
		}

		IVMProvider provider = VMHandlerUtils.getVMProviderForSelection(selection);
		if (provider instanceof IElementFormatProvider == false) {
			return NO_ITEMS;
		}

		IPresentationContext context = provider.getPresentationContext();
		Object viewerInput = VMHandlerUtils.getViewerInput(context);
		TreePath[] elementPaths = ((ITreeSelection) selection).getPaths();
		List<String> availableFormats = getAvailableFormats(provider, viewerInput, elementPaths);
		if (availableFormats.size() == 0) {
			return NO_ITEMS;
		}

		IVMNode[] nodes = new IVMNode[elementPaths.length];
		final List<SelectFormatAction> actions = new ArrayList<>(availableFormats.size());
		for (String formatId : availableFormats) {
			actions.add(new SelectFormatAction((IElementFormatProvider) provider, context, nodes, viewerInput,
					elementPaths, formatId));
		}

		final String[] elementActiveFormats = new String[elementPaths.length];
		CountingRequestMonitor crm = new CountingRequestMonitor(
				SimpleDisplayExecutor.getSimpleDisplayExecutor(Display.getDefault()), null) {
			@Override
			protected void handleCompleted() {
				String activeFormat = null;
				for (int i = 0; i < elementActiveFormats.length; i++) {
					if (i == 0) {
						activeFormat = elementActiveFormats[i];
					} else if (activeFormat != null && activeFormat.equals(elementActiveFormats[i]) == false) {
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
							elementActiveFormats[index] = this.getData();
							super.handleSuccess();
						}
					});
		}
		crm.setDoneCount(elementPaths.length);
		int count = actions.size();
		IContributionItem[] items = new IContributionItem[count];
		for (int i = 0; i < items.length; i++) {
			items[i] = new ActionContributionItem(actions.get(i));
		}
		return items;
	}

	/**
	 * Get the available formats for all elements in the selection.  If all elements have the same
	 * available formats, return that list; if not, return the default list.
	 */
	private List<String> getAvailableFormats(IVMProvider provider, Object viewerInput, TreePath[] paths) {
		if (provider instanceof ICachingVMProviderExtension2) {
			ICachingVMProviderExtension2 cachingProvider = (ICachingVMProviderExtension2) provider;

			String[] formats = null;
			for (TreePath path : paths) {
				IVMNode node = VMHandlerUtils.getVMNode(viewerInput, path);
				if (node != null) {
					ICacheEntry cacheEntry = cachingProvider.getCacheEntry(node, viewerInput, path);
					if (cacheEntry != null && cacheEntry.getProperties() != null) {
						String[] entryFormats = (String[]) cacheEntry.getProperties()
								.get(IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS);
						if (entryFormats == null) {
							// At least one element has no formats.  Use the default ones.
							return FORMATS;
						}
						if (formats == null) {
							// First set of formats
							formats = entryFormats;
						} else {
							// Found another set of formats.  Make sure it is the same as the set we already have.
							// If not, return the default set of formats.
							if (!Arrays.equals(formats, entryFormats)) {
								return FORMATS;
							}
						}
					}
				}
			}
			if (formats != null) {
				return Arrays.asList(formats);
			}
		}
		return FORMATS;
	}
}