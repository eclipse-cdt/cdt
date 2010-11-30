/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServices;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesUpdateStatus;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICacheEntry;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICachingVMProviderExtension2;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IVMUpdatePolicyExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A helper class for View Model Node implementations that support elements 
 * to be formatted using different number formats.  This object can be 
 * instantiated by a VM node to retrieve formatted values from a given service
 * using given DMC type.  
 * <p>
 * Note: This class is a replacement for the {@link FormattedValueVMUtil#updateFormattedValues(IPropertiesUpdate[], IFormattedValues, Class, RequestMonitor)}
 * static method.  This new implementation retrieves cached values if they are
 * available in the VM Cache. 
 * </p>
 * 
 * @see FormattedValueVMUtil
 * @see org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider
 * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues
 * 
 * @since 2.0
 */
public class FormattedValueRetriever {

    private final IVMNode fNode;
    private final ICachingVMProviderExtension2 fCache;
    private final ServiceTracker fServiceTracker;
    private final Class<? extends IFormattedDataDMContext> fDmcType;
    private final String fPropertyPrefix;
    
    private final String PROP_AVAILABLE_FORMATS;
    private final String PROP_ACTIVE_FORMAT;
    private final String PROP_ACTIVE_FORMAT_VALUE;
    private final String PROP_BASE;

    public FormattedValueRetriever(IVMNode node, DsfSession session, Class<?> serviceClass, Class<? extends IFormattedDataDMContext> dmcType) {
        this(node, createFilter(session, serviceClass), dmcType, null);
    }
    
    public FormattedValueRetriever(IVMNode node, DsfSession session, Class<?> serviceClass, Class<? extends IFormattedDataDMContext> dmcType, String propertyPrefix) {
        this(node, createFilter(session, serviceClass), dmcType, propertyPrefix);
    }

    public FormattedValueRetriever(IVMNode node, Filter filter, Class<? extends IFormattedDataDMContext> dmcType, String propertyPrefix) {
        fNode = node;
        fCache = (ICachingVMProviderExtension2)node.getVMProvider();
        fServiceTracker = new ServiceTracker(DsfUIPlugin.getBundleContext(), filter, null);
        fServiceTracker.open();
        fDmcType = dmcType;
        if (propertyPrefix == null) {
            propertyPrefix = ""; //$NON-NLS-1$
        }
        fPropertyPrefix = propertyPrefix;
        PROP_AVAILABLE_FORMATS = (fPropertyPrefix + IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS).intern();
        PROP_ACTIVE_FORMAT = (fPropertyPrefix + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT).intern();
        PROP_ACTIVE_FORMAT_VALUE = (fPropertyPrefix + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE).intern();
        PROP_BASE = (fPropertyPrefix + IDebugVMConstants.PROP_FORMATTED_VALUE_BASE).intern();
    }
    
    /**
     * Creates an OSGI service filter for the given service type in a given 
     * DSF session.
     */
    private static Filter createFilter(DsfSession session, Class<?> serviceClass) {
        try {
            return DsfUIPlugin.getBundleContext().createFilter( DsfServices.createServiceFilter(serviceClass, session.getId()) );
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Unable to create service filter for " + serviceClass, e); //$NON-NLS-1$
        }        
    }
    
    public void dispose() {
        fServiceTracker.close();
    }
    
    /**
     * This method fills in the formatted value properties in the given array 
     * of property update objects using data retrieved from the given 
     * formatted values service.  
     * <p>
     * Note: The node parameter must return a <code>ICachingVMProviderExtension2</code>
     * through its {@link IVMNode#getVMProvider()} method.
     * 
     * @param node This method also takes an <code>IVMNode</code> parameter 
     * which allows for retrieving the format value data from the View Model 
     * cache.  If the needed value property is cached already, the cached 
     * value will be used otherwise the properties will be retrieved from the 
     * service.   
     * 
     * @param updates The array of updates to fill in information to.  This  
     * update is used to retrieve the data model context and to write the 
     * properties into. Implementation will not directly mark these updates 
     * complete, but contribute towards that end by marking [monitor] complete.
     * 
     * @param service The service to be used to retrieve the values from.
     * 
     * @param dmcType The class type of the data model context.  Some updates
     * can contain multiple formatted data data model contexts, and this
     * method assures that there is no ambiguity in which context should be 
     * used.
     * 
     * @param rm Request monitor used to signal completion of work
     * 
     * @since 2.2
     */
    @ConfinedToDsfExecutor("node.getExecutor()")
    public void update(final IPropertiesUpdate updates[], final RequestMonitor rm) 
    {
        final Map<IPropertiesUpdate, String[]> cachedAvailableFormatsMap = calcCachedAvailableFormatsMap(updates);
        if (cachedAvailableFormatsMap != null && cachedAvailableFormatsMap.size() == updates.length) {
            // All updates were satisfied by the cache.
            doUpdateWithAvailableFormats(updates, cachedAvailableFormatsMap, rm);
        } else {
            final IFormattedValues service = (IFormattedValues)fServiceTracker.getService();
            if (service == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Service not available " + fServiceTracker, null)); //$NON-NLS-1$
                rm.done();
                return;
            }
            
            try {
                service.getExecutor().execute(new DsfRunnable() {
                    public void run() {
                        retrieveAvailableFormats(
                            calcOutstandingAvailableFormatsUpdates(updates, cachedAvailableFormatsMap), 
                            new DataRequestMonitor<Map<IPropertiesUpdate, String[]>>(fNode.getVMProvider().getExecutor(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    Map<IPropertiesUpdate, String[]> availableFormatsMap;
                                    if (cachedAvailableFormatsMap != null) {
                                        availableFormatsMap = cachedAvailableFormatsMap;
                                        availableFormatsMap.putAll(getData());
                                    } else {
                                        availableFormatsMap = getData();
                                    }
                                    // Retrieve the formatted values now that we have the available formats (where needed).
                                    // Note that we are passing off responsibility of our parent monitor  
                                    doUpdateWithAvailableFormats(updates, availableFormatsMap, rm); 
                                }
                            });
                    }
                });
            } catch (RejectedExecutionException e) {
                rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Service executor shut down " + service.getExecutor(), e)); //$NON-NLS-1$
                rm.done();
            }
        }
    }
    
    /**
     * Retrieves the <code>PROP_FORMATTED_VALUE_AVAILABLE_FORMATS</code> 
     * property for each update and returns it in a map.  The returned 
     * map may be <code>null</code> if no cache data is available. 
     * 
     * @since 2.2
     */
    private Map<IPropertiesUpdate, String[]> calcCachedAvailableFormatsMap(IPropertiesUpdate updates[]) {
        Map<IPropertiesUpdate, String[]> cachedAvailableFormatsMap = null; // delay creating map till needed
        for (IPropertiesUpdate update : updates) {
            ICacheEntry cacheEntry = fCache.getCacheEntry(fNode, update.getViewerInput(), update.getElementPath());
            if (cacheEntry != null && cacheEntry.getProperties() != null) {
                String[] availableFormats = (String[])
                    cacheEntry.getProperties().get(PROP_AVAILABLE_FORMATS);
                // Add the cached entry to the cached map even if its null.  This will help keep track
                // of whether we need to call the service for data.
                if (availableFormats != null || !isAvailableFormatsPropertyNeeded(update)) {
                    if (cachedAvailableFormatsMap == null) {  
                        cachedAvailableFormatsMap = new HashMap<IPropertiesUpdate, String[]>(updates.length * 4/3);
                    }
                    cachedAvailableFormatsMap.put(update, availableFormats);
                    continue;
                }
            }
        }
        return cachedAvailableFormatsMap;
    }
    
    /**
     * Generates a list of updates which still need the 
     * <code>PROP_FORMATTED_VALUE_AVAILABLE_FORMATS</code> property.
     * 
     * @since 2.2
     */
    private List<IPropertiesUpdate> calcOutstandingAvailableFormatsUpdates(IPropertiesUpdate[] updates, Map<IPropertiesUpdate, String[]> cachedAvailableFormatsMap) {
        if (cachedAvailableFormatsMap != null) {
            List<IPropertiesUpdate> outstandingUpdates = new ArrayList<IPropertiesUpdate>(updates.length - cachedAvailableFormatsMap.size());
            for (IPropertiesUpdate update : updates) {
                if (!cachedAvailableFormatsMap.containsKey(update)) {
                    outstandingUpdates.add(update);
                }
            }
            return outstandingUpdates;
        } else {
            return Arrays.asList(updates);
        }
    }
    
    /**
     * Method to retrieve available formats for each update's element (if
     * needed). The result is returned in a map and in the
     * update object (if requested). 
     * <p>
     * Note that we use a synchronized map because it's updated by a request 
     * monitor with an ImmediateExecutor.
     * 
     * @since 2.2
     */
    @ConfinedToDsfExecutor("service.getExecutor()")
    private void retrieveAvailableFormats(
        final List<IPropertiesUpdate> updates, 
        final DataRequestMonitor<Map<IPropertiesUpdate, String[]>> rm) 
    {
        IFormattedValues service = (IFormattedValues)fServiceTracker.getService();
        assert service.getExecutor().isInExecutorThread();

        final Map<IPropertiesUpdate, String[]> availableFormats = Collections.synchronizedMap(new HashMap<IPropertiesUpdate, String[]>(updates.size() * 4/3));
        rm.setData(availableFormats);
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(service.getExecutor(), rm); 
        int count = 0;
        
        for (final IPropertiesUpdate update : updates) {

            if (!isAvailableFormatsPropertyNeeded(update)) {
                continue;
            }
            
            IFormattedDataDMContext dmc = getFormattedDataDMContext(update);
            if (dmc == null) {
                continue;
            } 
            
            service.getAvailableFormats(
                dmc, 
                new ViewerDataRequestMonitor<String[]>(ImmediateExecutor.getInstance(), update) {
                    /**
                     * Note we don't mark the update object done, and we
                     * avoid calling our base implementation so that it
                     * doesn't either. The completion of this request is
                     * just a step in servicing the update.
                     */
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            // Set the result (available formats) into the update object if it was requested 
                            if (update.getProperties().contains(PROP_AVAILABLE_FORMATS)) {
                                update.setProperty(PROP_AVAILABLE_FORMATS, getData());
                            }
                            
                            if (getData().length != 0) { 
                                // also add it to the map; we'll need to access it when querying the element's value.
                                availableFormats.put(update, getData());
                            } else { 
                                update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "No number formats available for " + update.getElement(), null)); //$NON-NLS-1$
                            }           
                        } else {
                            update.setStatus(getStatus()); 
                        }
                        countingRm.done();
                    }
                });
            count++;
        }
        countingRm.setDoneCount(count);
        
    }
    
    /**
     * This method continues retrieving formatted value properties.  It is 
     * called once the available formats are calculated for each requested 
     * update.
     * 
     * @param availableFormatsMap Prior to calling this method, the caller 
     * queries (where necessary) the formats supported by the element in each
     * update, and it puts that information in this map. If an entry in 
     * [updates] does not appear in this map, it means that its view-model 
     * element doesn't support any formats (very unlikely), or that the 
     * available formats aren't necessary to service the properties specified 
     * in the update
     * 
     * @since 2.2
     */
    @ConfinedToDsfExecutor("fNode.getExecutor()")
    private void doUpdateWithAvailableFormats(
        IPropertiesUpdate updates[],
        final Map<IPropertiesUpdate, String[]> availableFormatsMap,
        final RequestMonitor rm)
    {
        final List<IPropertiesUpdate> outstandingUpdates = new ArrayList<IPropertiesUpdate>(updates.length); 
        final Map<IPropertiesUpdate, List<String>> requestedFormatsMap = new HashMap<IPropertiesUpdate, List<String>>(updates.length * 4 / 3);
        final Map<IPropertiesUpdate, String> activeFormatsMap = new HashMap<IPropertiesUpdate, String>(updates.length * 4 / 3);
        
        for (final IPropertiesUpdate update : updates) {
            String preferredFormat = FormattedValueVMUtil.getPreferredFormat(update.getPresentationContext());
            if (update.getProperties().contains(IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE)) {
                update.setProperty(IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE, preferredFormat);
            }

            final String activeFormat = calcActiveFormat(update, preferredFormat, availableFormatsMap);
            
            if (update.getProperties().contains(PROP_ACTIVE_FORMAT)) {
                assert activeFormat != null : "Our caller should have provided the available formats if this property was specified; given available formats, an 'active' nomination is guaranteed."; //$NON-NLS-1$
                update.setProperty(PROP_ACTIVE_FORMAT, activeFormat);
            }
            List<String> requestedFormats = calcRequestedFormats(update, activeFormat, availableFormatsMap.get(update));

            ICacheEntry cacheEntry = fCache.getCacheEntry(fNode, update.getViewerInput(), update.getElementPath());
            if (cacheEntry != null && cacheEntry.getProperties() != null) {
                IVMUpdatePolicyExtension updatePolicy = getVMUpdatePolicyExtension();
                Iterator<String> itr = requestedFormats.iterator();
                while (itr.hasNext()) {
                    String format = itr.next();
                    String formatProperty = FormattedValueVMUtil.getPropertyForFormatId(format, fPropertyPrefix);
                    Object value = cacheEntry.getProperties().get(formatProperty);
                    if (value != null || !canUpdateProperty(cacheEntry, updatePolicy, formatProperty)) {
                        itr.remove();
                        setUpdateFormatProperty(update, activeFormat, format, value);
                    }
                }
            }
            
            if (!requestedFormats.isEmpty()) {
                outstandingUpdates.add(update);
                requestedFormatsMap.put(update, requestedFormats);
                activeFormatsMap.put(update, activeFormat);
            } 
        }
        final IFormattedValues service = (IFormattedValues)fServiceTracker.getService();
        if (service == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Service not available " + fServiceTracker, null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        try {
            service.getExecutor().execute(new DsfRunnable() {
                public void run() {
                    doUpdateWithRequestedFormats(outstandingUpdates, requestedFormatsMap, activeFormatsMap, rm);
                }
            });
        } catch (RejectedExecutionException e) {
            rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Service executor shut down " + service.getExecutor(), e)); //$NON-NLS-1$
            rm.done();
        }
    }

    private IVMUpdatePolicyExtension getVMUpdatePolicyExtension() {
        if( fCache.getActiveUpdatePolicy() instanceof IVMUpdatePolicyExtension) {
            return (IVMUpdatePolicyExtension)fCache.getActiveUpdatePolicy();
        }
        return null;
    }
    
    private static boolean canUpdateProperty(ICacheEntry entry, IVMUpdatePolicyExtension updatePolicy, String property) {
        return !entry.isDirty() || (updatePolicy != null && updatePolicy.canUpdateDirtyProperty(entry, property));
    }
    
    /**
     * Retrieves the specified formatted values from the service. 
     * 
     * @param requestedFormatsMap Map containing the formats to be retrieved 
     * and filled in for each given update.
     * @param activeFormatsMap Map containing the active format for each given
     * update.  The active format value needs to be set in the update using the 
     * special property <code>PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE</code>.
     * 
     * @since 2.2
     */
    @ConfinedToDsfExecutor("service.getExecutor()")
    private void doUpdateWithRequestedFormats(
        List<IPropertiesUpdate> updates,
        final Map<IPropertiesUpdate, List<String>> requestedFormatsMap,
        final Map<IPropertiesUpdate, String> activeFormatsMap,
        final RequestMonitor monitor)
    { 
        IFormattedValues service = (IFormattedValues)fServiceTracker.getService();
        assert service.getExecutor().isInExecutorThread();

        // Use a single counting RM for all the requested formats for each update.
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(ImmediateExecutor.getInstance(), monitor);
        int count = 0;
        
        for (final IPropertiesUpdate update : updates) {
            IFormattedDataDMContext dmc = getFormattedDataDMContext(update);
            if (dmc == null) {
                continue;
            } 

            List<String> requestedFormats = requestedFormatsMap.get(update);
            for (String requestedFormat : requestedFormats) { 
                final FormattedValueDMContext formattedValueDmc = service.getFormattedValueContext(dmc, requestedFormat);
                service.getFormattedExpressionValue(
                    formattedValueDmc, 
                    // Here also use the ViewerDataRequestMonitor in order to propagate the update's cancel request. 
                    // However, when operation is complete, call the counting RM's done().
                    // Use an immediate executor to avoid the possibility of a rejected execution exception.
                    new ViewerDataRequestMonitor<FormattedValueDMData>(ImmediateExecutor.getInstance(), update) {
                        @Override
                        protected void handleCompleted() {
                            if (isSuccess()) {
                                setUpdateFormatProperty(
                                    update, 
                                    activeFormatsMap.get(update), 
                                    formattedValueDmc.getFormatID(), 
                                    getData().getFormattedValue()); 
                            } else {
                                update.setStatus(getStatus());
                            }
                            // Note: we must not call the update's done method, instead call counting RM done.
                            countingRm.done();
                            
                        };
                    });
                count++;
            }
        }
        countingRm.setDoneCount(count);
    }

    /**
     * Determine the 'active' value format. It is the view preference if
     * and only if the element supports it. Otherwise it is the first
     * format supported by the element.  
     * <p>
     * Note: If the availableFormatsMap doesn't contain the available formats  
     * for the given update, it means the update doesn't request any properties 
     * which requires the active format to be calculated.
     * 
     * @param update Properties update to calculate the active format for.  
     * @param availableFormatsMap The map of available formats.
     * @return The active format, or null if active format not requested in 
     * update.
     */
    private String calcActiveFormat(IPropertiesUpdate update, String preferredFormat, Map<IPropertiesUpdate, String[]> availableFormatsMap) {
        String[] availableFormats = availableFormatsMap.get(update);
        if (availableFormats != null && availableFormats.length != 0) {
            if (isFormatAvailable(preferredFormat, availableFormats)) {
                return preferredFormat;
            } else {
                return availableFormats[0];
            }
        }
        return null;  // null means we don't need to know what the active format is
    }

    /**
     * Returns <code>true</code> if the given availableFormats array contains 
     * the given format. 
     */
    private boolean isFormatAvailable(String format, String[] availableFormats) {
        for (String availableFormat : availableFormats) {
            if (availableFormat.equals(format)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Service the properties that ask for the value in a specific
     * format. If the update request contains the property
     * PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE, and the active format
     * has not been explicitly requested, then we need an additional
     * iteration to provide it.
     */ 
    private List<String> calcRequestedFormats(IPropertiesUpdate update, String activeFormat, String[] availableFormats) {
        List<String> requestedFormats = new ArrayList<String>(10); 
        
        boolean activeFormatValueHandled = false;   // have we come across a specific format request that is the active format?

        for (Iterator<String> itr = update.getProperties().iterator(); itr.hasNext() || (activeFormat != null && !activeFormatValueHandled);) {
            String nextFormat;
            if (itr.hasNext()) {
                String propertyName = itr.next();
                if (propertyName.startsWith(PROP_BASE)) {
                    nextFormat = FormattedValueVMUtil.getFormatFromProperty(propertyName, fPropertyPrefix);
                    if (nextFormat.equals(activeFormat)) {
                        activeFormatValueHandled = true;
                    }
                    // if we know the supported formats (we may not), then no-op if this format is unsupported
                    if (availableFormats != null && !isFormatAvailable(nextFormat, availableFormats)) {
                        continue;
                    }
                }
                else {
                    continue;
                }
            } else {
                // the additional iteration to handle the active format 
                nextFormat = activeFormat;
                activeFormatValueHandled = true;
            }
            requestedFormats.add(nextFormat);
        }
        return requestedFormats;
    }
    
    /**
     * Writes the given formatted property value into the update.  It also 
     * writes the active format property if needed.
     * <p>
     * If the given property value is null, this method writes an error status 
     * instead.
     */
    private void setUpdateFormatProperty(IPropertiesUpdate update, String activeFormat, String format, Object value) {
        String formatProperty = FormattedValueVMUtil.getPropertyForFormatId(format, fPropertyPrefix); 
        if (value != null) {
            update.setProperty(formatProperty, value); 
            if (update.getProperties().contains(PROP_ACTIVE_FORMAT_VALUE) &&
                format.equals(activeFormat)) 
            {
                update.setProperty(PROP_ACTIVE_FORMAT_VALUE, value);
            }
        } else {
            IStatus staleDataStatus = DsfUIPlugin.newErrorStatus(IDsfStatusConstants.INVALID_STATE, "Cache contains stale data. Refresh view.", null );//$NON-NLS-1$
            if (update.getProperties().contains(PROP_ACTIVE_FORMAT_VALUE) &&
                format.equals(activeFormat)) 
            {
                PropertiesUpdateStatus.getPropertiesStatus(update).setStatus(
                    new String[] { PROP_ACTIVE_FORMAT_VALUE, formatProperty },
                    staleDataStatus);
            } else {
                PropertiesUpdateStatus.getPropertiesStatus(update).setStatus(formatProperty, staleDataStatus);                
            }
        }
    }

    /**
     * For each update, query the formats available for the update's
     * element...but only if necessary. The available formats are necessary
     * only if the update explicitly requests that information, or if the
     * update is asking what the active format is or is asking for the value
     * of the element in that format. The reason we need them in the last
     * two cases is that we can't establish the 'active' format for an
     * element without knowing its available formats. See
     * updateFormattedValuesWithAvailableFormats(), as that's where we make
     * that determination.
     * @param update
     * @return
     */
    private boolean isAvailableFormatsPropertyNeeded(IPropertiesUpdate update) {
        return update.getProperties().contains(PROP_AVAILABLE_FORMATS) || 
               update.getProperties().contains(PROP_ACTIVE_FORMAT) ||
               update.getProperties().contains(PROP_ACTIVE_FORMAT_VALUE);
    }
    
    /**
     * Extracts the formatted data DMC from the update.  If update doesn't 
     * contain DMC-based elemtn, it writes an error to the update and returns 
     * <code>null</code>. 
     */
    private IFormattedDataDMContext getFormattedDataDMContext(IPropertiesUpdate update) 
    {
        IFormattedDataDMContext dmc = null;
        if (update.getElement() instanceof IDMVMContext) {
            dmc = DMContexts.getAncestorOfType(((IDMVMContext)update.getElement()).getDMContext(), fDmcType);
        }
        
        if (dmc == null) {
            update.setStatus(DsfUIPlugin.newErrorStatus(IDsfStatusConstants.INVALID_HANDLE, "Update element did not contain a valid context: " + fDmcType, null)); //$NON-NLS-1$
        } 
        return dmc;
    }
}
