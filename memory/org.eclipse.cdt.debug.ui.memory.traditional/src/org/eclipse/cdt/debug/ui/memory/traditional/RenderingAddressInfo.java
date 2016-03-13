/*******************************************************************************
 * Copyright (c) 2016 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Initial API and implementation
 *******************************************************************************/
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
public class RenderingAddressInfo extends Rendering
        implements IDebugContextListener, IAddressInfoUpdateListener {

    private final TraditionalRendering fParent;

    // Additional Address information variables

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

    public void dispose() {

        fSelectedContext = null;
        fMapStartAddrToInfoItems.clear();
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
        ISelection selection = contextService.getActiveContext(site.getId(),
                ((IViewSite) site).getSecondaryId());
        if (selection instanceof StructuredSelection) {
            handleDebugContextChanged(((StructuredSelection) selection).getFirstElement());
        }
    }

    private void handleDebugContextChanged(final Object context) {
        if (isDisposed() || context == null) {
            // Invalid context or Data pane is not visible
            return;
        }

        if (context instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) context;

            final IMemoryBlockAddressInfoRetrieval addrInfo = ((IMemoryBlockAddressInfoRetrieval) adaptable
                    .getAdapter(IMemoryBlockAddressInfoRetrieval.class));

            if (addrInfo == null) {
                // No information retrieval available
                return;
            }

            // Save the selected context to later help us determine if the selection has really changed
            fSelectedContext = context;

            final Display display = getDisplay();
            addrInfo.getMemoryBlockAddressInfo(context, getMemoryBlock(),
                    new GetMemoryBlockAddressInfoReq(context) {
                        @Override
                        public void done() {
                            // If the context is still valid
                            if (getContext().equals(fSelectedContext)) {
                                final IMemoryBlockAddressInfoItem[] addressInfoItems = getAllAddressInfoItems();
                                if (!display.isDisposed()) {
                                    display.asyncExec(new Runnable() {
                                        @Override
                                        public void run() {
                                            // The selection has changed, so our Address information may no longer be valid
                                            fAddressInfoItems = addressInfoItems;
                                            fMapStartAddrToInfoItems.clear();

                                            if (fBinaryPane.isVisible()) {
                                                redrawPanes();
                                            }

                                            refreshUpdateListener(addrInfo);
                                        }
                                    });
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
        List<String> collection = new ArrayList<String>(items);
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
        IWorkbenchPartSite site = fParent.getMemoryRenderingContainer().getMemoryRenderingSite().getSite();
        IDebugContextService contextService = DebugUITools.getDebugContextManager()
                .getContextService(site.getWorkbenchWindow());
        resolveAddressInfoForCurrentSelection(contextService);
    }

    @Override
    Map<BigInteger, List<IMemoryBlockAddressInfoItem>> getVisibleValueToAddressInfoItems() {
        IMemoryBlockAddressInfoItem[] items = fAddressInfoItems;
        if (items == null) {
            fMapStartAddrToInfoItems.clear();
            return fMapStartAddrToInfoItems;
        }

        if (getRadix() != RADIX_HEX && getRadix() != RADIX_BINARY) {
            // If not using Hex or Binary radix, we can not accurately determine the location of cross
            // reference information
            // unless the cell size matches the addressable size of the target system
            if (fParent.getAddressableSize() != getBytesPerColumn()) {
                fMapStartAddrToInfoItems.clear();
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
            BigInteger startAddress = getViewportStartAddress();
            // Get the endAddress considering single height so we don't miss marking items with information
            // Note: The UI may some times present rows with double height even if the user does not see items
            // with
            // additional info, the reason is that the second part of a view port page may contain all the
            // items with
            // info. However restricting the set of visible items to consider a double height may not consider
            // additional
            // info items present when using single height and display them with no markings and additional
            // information
            BigInteger endAddress = getViewportEndAddressSingleHeight();

            for (IMemoryBlockAddressInfoItem item : items) {
                List<IMemoryBlockAddressInfoItem> containers = allValuesMap.get(item.getAddress());
                if (containers == null) {
                    containers = new ArrayList<>();
                    allValuesMap.put(item.getAddress(), containers);
                }
                containers.add(item);

                // If any address within the item width is within the visible range we want it in the filtered
                // result
                BigInteger itemStart = item.getAddress();
                BigInteger itemEnd = item.getAddress().add(item.getRangeInAddressableUnits());
                boolean itemStartIsInRange = isWithinRange(itemStart, startAddress, endAddress);
                boolean itemEndIsInRange = isWithinRange(itemEnd, startAddress, endAddress);
                boolean itemSpansOverVisibleRange = isWithinRange(startAddress, itemStart, itemEnd)
                        && isWithinRange(endAddress, itemStart, itemEnd);

                if (itemStartIsInRange || itemEndIsInRange || itemSpansOverVisibleRange) {
                    fMapStartAddrToInfoItems.put(item.getAddress(), allValuesMap.get(item.getAddress()));
                    filteredValuesMap.put(item.getAddress(), allValuesMap.get(item.getAddress()));
                }
            }
        }

        return filteredValuesMap;
    }

    @Override
    String buildAddressInfoString(BigInteger cellAddress, String separator, boolean addTypeHeaders) {
        List<IMemoryBlockAddressInfoItem> infoItems = fMapStartAddrToInfoItems.get(cellAddress);

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
        return fMapStartAddrToInfoItems.keySet().contains(cellAddress);
    }

    /**
     * Indicates if additional address information is available to display in the current visible range
     */
    @Override
    boolean hasVisibleRangeInfo() {
        return (fBinaryPane.fPaneVisible && fMapStartAddrToInfoItems.size() > 0);
    }

}
