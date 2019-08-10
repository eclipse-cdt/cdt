/*******************************************************************************
 * Copyright (c) 2016 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 * *******************************************************************************/
package org.eclipse.cdt.debug.ui.memory.traditional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval;
import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval.EventType;
import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval.IAddressInfoUpdateListener;
import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval.IGetMemoryBlockAddressInfoReq;
import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval.IMemoryBlockAddressInfoItem;
import org.eclipse.cdt.debug.internal.core.CRequest;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * @since 1.4
 */
public class RenderingAddressInfo extends Rendering implements IDebugContextListener, IAddressInfoUpdateListener {

	private final TraditionalRendering fParent;
	/**
	 * Simple tracker of selected context, to reduce the number of asynchronous calls to resolve the
	 * information items related to a selected context
	 */
	private volatile Object fSelectedContext;
	private IMemoryBlockAddressInfoRetrieval fAddressInfoRetrieval = null;

	/**
	 * This maintains the full set of information items retrieved for the currently selected context. This is
	 * updated each time a context selection change is detected
	 */
	private volatile IMemoryBlockAddressInfoItem[] fAddressInfoItems;

	private final AddressInfoTypeMap fAddressInfoTypeStatusMap = new AddressInfoTypeMap();

	public RenderingAddressInfo(Composite parent, TraditionalRendering renderingParent) {
		super(parent, renderingParent);

		fParent = renderingParent;
		// Register as Debug context listener
		IWorkbenchPartSite site = fParent.getMemoryRenderingContainer().getMemoryRenderingSite().getSite();
		DebugUITools.addPartDebugContextListener(site, this);

		IDebugContextService contextService = DebugUITools.getDebugContextManager()
				.getContextService(site.getWorkbenchWindow());
		resolveAddressInfoForCurrentSelection(contextService);
	}

	/**
	 * Keeps a map from information type to its state and to a corresponding Action instance
	 * needed to update the actual state from UI interactions
	 */
	class AddressInfoTypeMap extends HashMap<String, Boolean> {
		private static final long serialVersionUID = 1L;

		private final Map<String, Action> fTypeToActionMap = new HashMap<>();

		public Action getAction(final String infoType) {
			if (!containsKey(infoType)) {
				if (fTypeToActionMap.containsKey(infoType)) {
					// The key status has been removed, clean the action map
					fTypeToActionMap.remove(infoType);
				}
				return null;
			}

			Action action = fTypeToActionMap.get(infoType);
			if (action != null) {
				return action;
			} else {
				action = new Action(infoType, IAction.AS_CHECK_BOX) {
					@Override
					public void run() {
						put(infoType, Boolean.valueOf(isChecked()));
						redrawPanes();
					}
				};
				action.setChecked(get(infoType));
				fTypeToActionMap.put(infoType, action);
			}

			return action;
		}

		@Override
		public void clear() {
			fTypeToActionMap.clear();
			super.clear();
		}
	}

	@Override
	void resolveAddressInfoForCurrentSelection() {
		IWorkbenchPartSite site = fParent.getMemoryRenderingContainer().getMemoryRenderingSite().getSite();
		IDebugContextService contextService = DebugUITools.getDebugContextManager()
				.getContextService(site.getWorkbenchWindow());
		resolveAddressInfoForCurrentSelection(contextService);
	}

	@Override
	public void dispose() {

		fSelectedContext = null;
		fMapStartAddrToInfoItems.clear();
		fMapAddrToInfoItems.clear();
		fAddressInfoTypeStatusMap.clear();
		fAddressInfoItems = null;

		IWorkbenchPartSite site = fParent.getMemoryRenderingContainer().getMemoryRenderingSite().getSite();
		DebugUITools.removePartDebugContextListener(site, this);

		if (fAddressInfoRetrieval != null) {
			fAddressInfoRetrieval.removeAddressInfoUpdateListener(this);
		}

		super.dispose();
	}

	private class GetMemoryBlockAddressInfoReq extends CRequest implements IGetMemoryBlockAddressInfoReq {
		private Map<String, IMemoryBlockAddressInfoItem[]> fInfoTypeToItems = Collections
				.synchronizedMap(new HashMap<String, IMemoryBlockAddressInfoItem[]>());
		private final Object fContext;

		GetMemoryBlockAddressInfoReq(Object context) {
			fContext = context;
		}

		@Override
		public IMemoryBlockAddressInfoItem[] getAddressInfoItems(String type) {
			return fInfoTypeToItems.get(type);
		}

		@Override
		public void setAddressInfoItems(String type, IMemoryBlockAddressInfoItem[] items) {
			fInfoTypeToItems.put(type, items);
		}

		public Object getContext() {
			return fContext;
		}

		@Override
		public String[] getAddressInfoItemTypes() {
			return fInfoTypeToItems.keySet().toArray(new String[fInfoTypeToItems.size()]);
		}

		@Override
		public IMemoryBlockAddressInfoItem[] getAllAddressInfoItems() {
			// concatenate the different type of items received into a single list
			List<IMemoryBlockAddressInfoItem> allItemsList = new ArrayList<>();
			// For each set of items
			for (IMemoryBlockAddressInfoItem[] partialItems : fInfoTypeToItems.values()) {
				if (partialItems != null && partialItems.length > 0) {
					allItemsList.addAll(Arrays.asList(partialItems));
				}
			}
			return allItemsList.toArray(new IMemoryBlockAddressInfoItem[allItemsList.size()]);
		}
	}

	/**
	 * @since 1.4
	 */
	@Override
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			// Resolve selection
			ISelection selection = event.getContext();
			if (!(selection instanceof IStructuredSelection)) {
				return;
			}

			Object elem = ((IStructuredSelection) selection).getFirstElement();
			handleDebugContextChanged(elem);
		}

	}

	private void resolveAddressInfoForCurrentSelection(IDebugContextService contextService) {
		IWorkbenchPartSite site = fParent.getMemoryRenderingContainer().getMemoryRenderingSite().getSite();
		// Check current selection
		ISelection selection = contextService.getActiveContext(site.getId(), ((IViewSite) site).getSecondaryId());
		if (selection instanceof StructuredSelection) {
			handleDebugContextChanged(((StructuredSelection) selection).getFirstElement());
		}
	}

	private void handleDebugContextChanged(final Object context) {
		if (isDisposed() || context == null || !isShowCrossReferenceInfo()) {
			// Invalid context or user has chosen not to see additional address information
			return;
		}

		if (context instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) context;

			final IMemoryBlockAddressInfoRetrieval addrInfo = (adaptable
					.getAdapter(IMemoryBlockAddressInfoRetrieval.class));

			if (addrInfo == null) {
				// No information retrieval available
				return;
			}

			// Save the selected context to later help us determine if the selection has really changed
			fSelectedContext = context;

			final Display display = getDisplay();
			addrInfo.getMemoryBlockAddressInfo(context, getMemoryBlock(), new GetMemoryBlockAddressInfoReq(context) {
				@Override
				public void done() {
					// If the context is still valid
					if (getContext().equals(fSelectedContext)) {
						final IMemoryBlockAddressInfoItem[] addressInfoItems = getAllAddressInfoItems();

						if (isShowCrossReferenceInfo()) {
							final String[] types = getAddressInfoItemTypes();

							if (!display.isDisposed()) {
								display.asyncExec(() -> {
									for (String type : types) {
										if (!fAddressInfoTypeStatusMap.containsKey(type)) {
											fAddressInfoTypeStatusMap.put(type, Boolean.TRUE);
										}
									}

									// The selection has changed, so our Address information may no longer be valid
									fAddressInfoItems = addressInfoItems;
									fMapStartAddrToInfoItems.clear();
									fMapAddrToInfoItems.clear();

									if (fBinaryPane.isVisible()) {
										redrawPanes();
									}

									refreshUpdateListener(addrInfo);
								});
							}
						}
					}
				}

				private void refreshUpdateListener(final IMemoryBlockAddressInfoRetrieval addrInfo) {
					if (fAddressInfoRetrieval == null) {
						// One retrieval per session,
						// Register this rendering to listen for info updates
						fAddressInfoRetrieval = addrInfo;
						addrInfo.addAddressInfoUpdateListener(RenderingAddressInfo.this);
					}
				}
			});
		}
	}

	/**
	 * @return Return the view port end address, if the DataPane displays information with a single height per
	 *         row i.e. single height is used when no additional address information is available for any of
	 *         the addresses in the view port
	 */
	private BigInteger getViewportEndAddressSingleHeight() {
		int cellHeight = fBinaryPane.getCellTextHeight() + (getCellPadding() * 2);
		int rowCount = getBounds().height / cellHeight;
		BigInteger endAddress = fViewportAddress
				.add(BigInteger.valueOf(this.getBytesPerRow() * rowCount / getAddressableSize()));

		return endAddress;
	}

	private boolean isWithinRange(BigInteger item, BigInteger start, BigInteger end) {
		if (item.compareTo(start) > -1 && item.compareTo(end) < 1) {
			return true;
		}
		return false;
	}

	private String[] orderTypesAscending(Set<String> items) {
		List<String> collection = new ArrayList<>(items);
		Collections.sort(collection);
		return collection.toArray(new String[collection.size()]);
	}

	@Override
	protected void redrawPanes() {
		if (!isDisposed() && this.isVisible()) {
			// Refresh address information visible in the current viewport
			getVisibleValueToAddressInfoItems();
		}

		super.redrawPanes();
	}

	@Override
	public void handleAddressInfoUpdate(EventType type, Object update) {
		fAddressInfoItems = null;
		resolveAddressInfoForCurrentSelection();
	}

	@Override
	Map<BigInteger, List<IMemoryBlockAddressInfoItem>> getVisibleValueToAddressInfoItems() {
		IMemoryBlockAddressInfoItem[] items = fAddressInfoItems;
		if (items == null || !isShowCrossReferenceInfo()) {
			fMapStartAddrToInfoItems.clear();
			fMapAddrToInfoItems.clear();
			return fMapStartAddrToInfoItems;
		}

		if (getRadix() != RADIX_HEX && getRadix() != RADIX_BINARY) {
			// If not using Hex or Binary radix, we can not accurately determine the location of cross
			// reference information
			// unless the cell size matches the addressable size of the target system
			if (fParent.getAddressableSize() != getBytesPerColumn()) {
				fMapStartAddrToInfoItems.clear();
				fMapAddrToInfoItems.clear();
				return fMapStartAddrToInfoItems;
			}
		}

		Map<BigInteger, List<IMemoryBlockAddressInfoItem>> allValuesMap = new HashMap<>(items.length);

		// This local variable will hold the same values as the instance variable fMapAddressToInfoItems, and
		// be used as
		// return value. The reason for the duplication is to prevent concurrent access exceptions
		Map<BigInteger, List<IMemoryBlockAddressInfoItem>> filteredValuesMap = new HashMap<>(items.length);

		synchronized (fMapStartAddrToInfoItems) {
			// Refreshing the Address to InfoItem data map
			fMapStartAddrToInfoItems.clear();
			fMapAddrToInfoItems.clear();
			BigInteger startAddress = getViewportStartAddress();
			// Get the endAddress considering a page that uses single height,
			// Note: The UI may some times present rows with double height even if the user does not see items
			// with additional info, the reason is that the second part of a view port page may contain all
			// the items with info.
			//   if we were to use and endAddress for a page that uses double height, but end up not having
			// items with additional information, then it would need to switch to single height to compact the
			// information in the view but since an endAddress for double height was used it will not consider
			// half of the items for additional information, so marking info. would not be shown.
			BigInteger endAddress = getViewportEndAddressSingleHeight();

			for (IMemoryBlockAddressInfoItem item : items) {
				// Skip information types not wanted by the user
				String itemType = item.getInfoType();
				if (!fAddressInfoTypeStatusMap.containsKey(itemType)
						|| fAddressInfoTypeStatusMap.get(itemType).equals(Boolean.FALSE)) {
					continue;
				}

				List<IMemoryBlockAddressInfoItem> containers = allValuesMap.get(item.getAddress());
				if (containers == null) {
					containers = new ArrayList<>();
					allValuesMap.put(item.getAddress(), containers);
				}
				containers.add(item);

				// If any address within the item width is within the visible range we want it in the filtered
				// result
				BigInteger itemStart = item.getAddress();
				BigInteger itemEnd = item.getAddress().add(item.getRangeInAddressableUnits()).subtract(BigInteger.ONE);
				boolean itemStartIsInRange = isWithinRange(itemStart, startAddress, endAddress);
				boolean itemEndIsInRange = isWithinRange(itemEnd, startAddress, endAddress);
				boolean itemSpansOverVisibleRange = isWithinRange(startAddress, itemStart, itemEnd)
						&& isWithinRange(endAddress, itemStart, itemEnd);

				if (itemStartIsInRange || itemEndIsInRange || itemSpansOverVisibleRange) {
					fMapStartAddrToInfoItems.put(item.getAddress(), allValuesMap.get(item.getAddress()));
					filteredValuesMap.put(item.getAddress(), allValuesMap.get(item.getAddress()));
					// Add information items for each address within the range
					// But establish the limits to only add information to visible items (i.e. limiting the processing)
					BigInteger firstItemVisibleAddress = itemStartIsInRange ? item.getAddress() : startAddress;
					BigInteger lastItemVisibleAddress = itemEndIsInRange
							? item.getAddress().add(item.getRangeInAddressableUnits().subtract(BigInteger.ONE))
							: endAddress;

					for (BigInteger candidateAddress = firstItemVisibleAddress; candidateAddress.compareTo(
							lastItemVisibleAddress) <= 0; candidateAddress = candidateAddress.add(BigInteger.ONE)) {
						List<IMemoryBlockAddressInfoItem> allItemsAtBase = allValuesMap.get(item.getAddress());
						List<IMemoryBlockAddressInfoItem> newInfoItems = filterToItemsValidForAddress(allItemsAtBase,
								item.getAddress(), candidateAddress);

						// Add new valid items to the map, associating it to candidate address
						List<IMemoryBlockAddressInfoItem> existingItems = fMapAddrToInfoItems.get(candidateAddress);
						if (existingItems == null) {
							// Brand new list of items
							fMapAddrToInfoItems.put(candidateAddress, newInfoItems);
						} else {
							// Appending new items to the existing list
							for (IMemoryBlockAddressInfoItem newItem : newInfoItems) {
								if (!existingItems.contains(newItem)) {
									existingItems.add(newItem);
								}
							}
						}
					}
				}
			}
		}

		return filteredValuesMap;
	}

	/**
	 * @param allBaseItems - Set of items sharing the same starting address
	 * @param baseAddress  - The starting address
	 * @param candidateAddress - An address higher than base address
	 * @return - The set of items that are still overlapping at the incremented address
	 */
	private List<IMemoryBlockAddressInfoItem> filterToItemsValidForAddress(
			List<IMemoryBlockAddressInfoItem> allBaseItems, BigInteger baseAddress, BigInteger candidateAddress) {

		List<IMemoryBlockAddressInfoItem> items = new ArrayList<>();

		// Keep info items applicable for the given address
		BigInteger range = candidateAddress.subtract(baseAddress);

		// sanity check - should not happen
		if (range.compareTo(BigInteger.ZERO) < 0) {
			// return empty list
			return items;
		} else if (range.compareTo(BigInteger.ZERO) == 0) {
			// Since all items share the same start address,
			// all items must be valid for a single address span
			return allBaseItems;
		}

		// Aggregate elements having a length equal or higher than the span between base address and current address
		for (IMemoryBlockAddressInfoItem item : allBaseItems) {
			if (item.getRangeInAddressableUnits().compareTo(range) >= 0) {
				items.add(item);
			}
		}

		return items;
	}

	@Override
	String buildAddressInfoString(BigInteger cellAddress, String separator, boolean addTypeHeaders) {
		List<IMemoryBlockAddressInfoItem> infoItems;
		if (addTypeHeaders) {
			// Tooltip information
			infoItems = fMapAddrToInfoItems.get(cellAddress);
		} else {
			// element information
			infoItems = fMapStartAddrToInfoItems.get(cellAddress);
		}

		return buildAddressInfoString(cellAddress, separator, addTypeHeaders, infoItems);
	}

	private String buildAddressInfoString(BigInteger cellAddress, String separator, boolean addTypeHeaders,
			List<IMemoryBlockAddressInfoItem> infoItems) {
		if (infoItems == null || infoItems.size() < 1) {
			// No information to display
			return "";
		}

		// The container string builder for all types
		StringBuilder sb = new StringBuilder();
		Map<String, StringBuilder> infoTypeToStringBuilder = new HashMap<>();

		for (int i = 0; i < infoItems.size(); i++) {
			String infoType = infoItems.get(i).getInfoType();

			// Resolve string builder for this info type
			StringBuilder typeBuilder = infoTypeToStringBuilder.get(infoType);
			if (typeBuilder == null) {
				// Create a String builder per information type
				if (addTypeHeaders) {
					typeBuilder = new StringBuilder(infoType).append(":").append(separator);
				} else {
					typeBuilder = new StringBuilder();
				}
				infoTypeToStringBuilder.put(infoType, typeBuilder);
			}

			// append the new item information to the string builder associated to its type
			typeBuilder.append(infoItems.get(i).getLabel()).append(separator);
		}

		// Present the group of items sorted by type name
		String[] sortedTypes = orderTypesAscending(infoTypeToStringBuilder.keySet());

		// Consolidate the String builders per type into a single one
		int i = 0;
		for (String type : sortedTypes) {
			StringBuilder builder = infoTypeToStringBuilder.get(type);
			String text = builder.toString();
			text = text.substring(0, text.length() - 1);
			sb.append(text);
			if (i < infoTypeToStringBuilder.keySet().size() - 1) {
				sb.append(separator);
			}
			i++;
		}

		return sb.toString();
	}

	@Override
	boolean hasAddressInfo(BigInteger cellAddress) {
		return fMapAddrToInfoItems.keySet().contains(cellAddress);
	}

	/**
	 * Indicates if additional address information is available to display in the current visible range
	 */
	@Override
	boolean hasVisibleRangeInfo() {
		return (fBinaryPane.fPaneVisible && fMapStartAddrToInfoItems.size() > 0);
	}

	@Override
	public Action[] getDynamicActions() {
		List<Action> actionList = new ArrayList<>(fAddressInfoTypeStatusMap.size());
		if (getPaneVisible(Rendering.PANE_BINARY)) {
			for (final String infoType : fAddressInfoTypeStatusMap.keySet()) {
				Action action = fAddressInfoTypeStatusMap.getAction(infoType);
				if (action != null) {
					actionList.add(action);
				}
			}
		}

		return actionList.toArray(new Action[actionList.size()]);
	}
}
