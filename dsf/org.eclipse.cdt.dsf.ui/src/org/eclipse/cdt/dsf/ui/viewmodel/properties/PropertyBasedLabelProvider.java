/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.VMViewerUpdate;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;

/**
 * A configurable label provider which uses element's property label provider 
 * to set element's label attributes.
 * <p>
 * When this provider is registered for an element it calculates the properties
 * that need to be retrieved based on view's active columns, and then it calls the
 * element's property provider to retrieve those properties.  After the property
 * values are retrieved, they are processed in order to produce correct label text, 
 * images, fonts, and colors, for the given element.
 */
@SuppressWarnings("restriction")
@ThreadSafe
public class PropertyBasedLabelProvider 
    implements IElementLabelProvider, ILabelAttributeChangedListener  
{
    private static final String[] EMPTY_PROPERTY_NAMES_ARRAY = new String[0];
    
    /**
     * Properties update used as to collect property data from the provider. 
     */
    private class PropertiesUpdate extends VMViewerUpdate implements IPropertiesUpdate {

        private final String[] fProperties;
        private final Map<String, Object> fValues;
        
        public PropertiesUpdate(String[] properties, ILabelUpdate labelUpdate, DataRequestMonitor<Map<String,Object>> rm) {
            super(labelUpdate, rm);
            fProperties = properties;
            fValues = fProperties != null 
                ? new HashMap<String, Object>(properties.length * 4 / 3, 0.75f) 
                : new HashMap<String, Object>();
        }
            
        public String[] getProperties() {
            return fProperties;
        }

        public void setProperty(String property, Object value) {
            fValues.put(property, value);
        }
        
        /**
         * Overrides the standard done in order to store the retrieved values 
         * in the client's request monitor. 
         */
        @Override
        public void done() {
            @SuppressWarnings("unchecked")
            DataRequestMonitor<Map<String,Object>> rm = (DataRequestMonitor<Map<String,Object>>)getRequestMonitor();
            if (fProperties == null || fValues.size() >= fProperties.length) {
                rm.setData(fValues);
            } else {
                rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Incomplete properties updated", null)); //$NON-NLS-1$
            }
            super.done();
        }
    }

    /**
     * Attribute information for each column by column ID.
     */
    private Map<String, LabelColumnInfo> fColumnInfos = Collections.synchronizedMap(new HashMap<String,LabelColumnInfo>());
    
    private ListenerList fListeners = new ListenerList(); 
    
    /**
     * Standard constructor.  A property based label constructor does not 
     * initialize column attribute information {@link #setColumnInfo(String, LabelColumnInfo)} 
     * must be called to configure each column.
     */
    public PropertyBasedLabelProvider() {
    }
    
    /**
     * Disposes this label provider and its configured column info objects.
     */
    public void dispose() {
        LabelColumnInfo[] infos = null;
        synchronized (fColumnInfos) {
            infos = fColumnInfos.values().toArray(new LabelColumnInfo[fColumnInfos.size()]);
            fColumnInfos.clear();
        }
        for (LabelColumnInfo info : infos) {
            info.dispose();
        }
    }
    
    /**
     * Sets the given column info object for the given column ID.  This column
     * info will be used to generate the label when the given column is visibile.
     * @param columnId Column ID that the given column info is being registered for.
     * @param info Column 'info' object containing column attributes.
     * @return The previous column info object configured for this ID.
     */
    public LabelColumnInfo setColumnInfo(String columnId, LabelColumnInfo info) {
        LabelColumnInfo oldInfo = fColumnInfos.put(columnId, info);
        info.addChangedListener(this);
        if (oldInfo != null) {
            info.removeChangedListener(this);
        }
        return oldInfo;
    }

    /**
     * Returns the given column info object for the given column ID.  
     * @param columnId Column ID to retrieve the column info for.
     * @@return Column 'info' object containing column attributes.
     */
    public LabelColumnInfo getColumnInfo(String column) {
        return fColumnInfos.get(column);
    }
       
    /**
     * Registers the given listener for changes in the attributes of this 
     * label provider.  A change in the attributes of a label should cause
     * a view to repaint.
     * @param listener Listener to register.
     */
    public void addChangedListener(ILabelAttributeChangedListener listener) {
        fListeners.add(listener);
    }
    
    /**
     * Unregisters the given listener.
     * @param listener Listener to unregister.
     */
    public void removeChangedListener(ILabelAttributeChangedListener listener) {
        fListeners.remove(listener);
    }

    /** 
     * Listener method called by label provider's column info objects.
     * @see ILabelAttributeChangedListener
     */
    public void attributesChanged() {
        Object[] listeners = fListeners.getListeners();
        for (Object listener : listeners) {
            ((ILabelAttributeChangedListener)listener).attributesChanged();
        }
    }

    public void update(ILabelUpdate[] labelUpdates) {
        IElementPropertiesProvider propertiesProvider = getElementPropertiesProvider(labelUpdates[0].getElement());
        if (propertiesProvider == null) {
            for (ILabelUpdate update : labelUpdates) {
                update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, "Properties-based label provider " + this + " failed to generate a label, no properties provider registered for element: " + labelUpdates[0].getElement()));  //$NON-NLS-1$ //$NON-NLS-2$
                update.done();
            }
            return;
        }
        
        String[] columnIds = labelUpdates[0].getColumnIds();
        String[] propertyNames = calcPropertyNamesForColumns(columnIds);
        
        // Call the properties provider.  Create a request monitor for each label update.
        // We can use an immediate executor for the request monitor because the label provider
        // is thread safe.
        IPropertiesUpdate[] propertiesUpdates = new IPropertiesUpdate[labelUpdates.length];
        for (int i = 0; i < labelUpdates.length; i++) {
            final ILabelUpdate labelUpdate = labelUpdates[i];
            propertiesUpdates[i] = new PropertiesUpdate(
                propertyNames, labelUpdates[i],  
                new ViewerDataRequestMonitor<Map<String, Object>>(ImmediateExecutor.getInstance(), labelUpdates[i]) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            updateLabel(labelUpdate, getData());
                        }
                        labelUpdate.done();
                    }
                });
        }
        propertiesProvider.update(propertiesUpdates);        
    }

    /**
     * Calculates the names of properties that have to be retrieved from the property
     * provider to generate the labels for given columns.
     * @param columnIds Column IDs to check.
     * @return Array of property names.
     */
    private String[] calcPropertyNamesForColumns(String[] columnIds) {
        if (columnIds == null) {
            LabelColumnInfo columnInfo = getColumnInfo(null);
            if (columnInfo != null) {
                return columnInfo.getPropertyNames();
            } else {
                return EMPTY_PROPERTY_NAMES_ARRAY;
            }
        } else {
            List<String> properties = new LinkedList<String>();
            for (String columnId : columnIds) {
                LabelColumnInfo info = getColumnInfo(columnId);
                if (info != null) {
                    String[] infoPropertyNames = info.getPropertyNames();
                    for (int i = 0; i < infoPropertyNames.length; i++) {
                        properties.add(infoPropertyNames[i]);
                    }
                }
            }
            return properties.toArray(new String[properties.size()]);  
        }
    }
    
    /**
     * Updates the label information based on given map of properties.
     * @param update Label update to write to.
     * @param properties Properties retrieved from the element properties provider.
     */
    protected void updateLabel(ILabelUpdate update, Map<String,Object> properties) {
        if (update.getColumnIds() == null) {
            LabelColumnInfo info = getColumnInfo(null);
            if (info != null) {
                info.updateColumn(update, 0, properties);
            }
        } else {
            String[] columnIds = update.getColumnIds();
            
            for (int i = 0; i < columnIds.length; i++) {
                LabelColumnInfo info = getColumnInfo(columnIds[i]);
                if (info != null) {
                    info.updateColumn(update, i, properties);
                }
            }       
        }
        
        update.done();
    }

    private IElementPropertiesProvider getElementPropertiesProvider(Object element) {
        if (element instanceof IAdaptable) {
            return (IElementPropertiesProvider)((IAdaptable)element).getAdapter(IElementPropertiesProvider.class);
        }
        return null;
    }
}
