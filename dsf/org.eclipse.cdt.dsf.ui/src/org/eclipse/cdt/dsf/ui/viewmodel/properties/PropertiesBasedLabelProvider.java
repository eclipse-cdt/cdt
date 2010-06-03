/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
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
 * 
 * @since 2.0 - Renamed from PropertyBasedLabelProvider 
 */
@ThreadSafe
public class PropertiesBasedLabelProvider 
    implements IElementLabelProvider
{
    public static final String ID_COLUMN_NO_COLUMNS = "ID_COLUMN_NO_COLUMNS"; //$NON-NLS-1$
    
    /**
     * Attribute information for each column by column ID.
     */
    private Map<String, LabelColumnInfo> fColumnInfos = Collections.synchronizedMap(new HashMap<String,LabelColumnInfo>());
    
    /**
     * Standard constructor.  A property based label constructor does not 
     * initialize column attribute information {@link #setColumnInfo(String, LabelColumnInfo)} 
     * must be called to configure each column.
     */
    public PropertiesBasedLabelProvider() {
    }
    
    /**
     * Sets the given column info object for the given column ID.  This column
     * info will be used to generate the label when the given column is visibile.
     * 
     * @param columnId Column ID that the given column info is being registered for.
     * @param info Column 'info' object containing column attributes.
     * @return The previous column info object configured for this ID.
     */
    public LabelColumnInfo setColumnInfo(String columnId, LabelColumnInfo info) {
        LabelColumnInfo oldInfo = fColumnInfos.put(columnId, info);
        return oldInfo;
    }

    /**
     * Returns the given column info object for the given column ID.  
     * @param columnId Column ID to retrieve the column info for.
     * 
     * @param columnId Column ID that the given column info is being registered for.
     * @@return Column 'info' object containing column attributes.
     */
    public LabelColumnInfo getColumnInfo(String columnId) {
        return fColumnInfos.get(columnId);
    }

	/**
	 * In addition to guarantees on [labelUpdates] declared by
	 * {@link IElementLabelProvider}, we further require/assume that all the
	 * model elements referenced by [labelUpdates] adapt to the same
	 * {@link IElementPropertiesProvider}.
	 * 
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider#update(org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate[])
	 */
    public void update(ILabelUpdate[] labelUpdates) {
        IElementPropertiesProvider propertiesProvider = getElementPropertiesProvider(labelUpdates[0].getElement());
        if (propertiesProvider == null) {
            for (ILabelUpdate update : labelUpdates) {
                update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, "Properties-based label provider " + this + " failed to generate a label, no properties provider registered for element: " + labelUpdates[0].getElement()));  //$NON-NLS-1$ //$NON-NLS-2$
                update.done();
            }
            return;
        }
        
		// We are guaranteed that all the provided updates are for the same
		// presentation context. Thus we can safely assume they request the same
		// columns
        String[] columnIds = labelUpdates[0].getColumnIds();
        
        Set<String> propertyNames = calcPropertyNamesForColumns(columnIds);
        
        // Call the properties provider.  Create a request monitor for each label update.
        // We can use an immediate executor for the request monitor because the label provider
        // is thread safe.
        IPropertiesUpdate[] propertiesUpdates = new IPropertiesUpdate[labelUpdates.length];
        for (int i = 0; i < labelUpdates.length; i++) {
            final ILabelUpdate labelUpdate = labelUpdates[i];
            propertiesUpdates[i] = new VMPropertiesUpdate(
                propertyNames, labelUpdate,  
                new ViewerDataRequestMonitor<Map<String, Object>>(ImmediateExecutor.getInstance(), labelUpdate) {
                    @Override
                    protected void handleCompleted() {
                        updateLabel(labelUpdate, getStatus(), getData());
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
    private Set<String> calcPropertyNamesForColumns(String[] columnIds) {
        Set<String> propertyNames = new HashSet<String>();
        if (columnIds == null) {
            LabelColumnInfo columnInfo = getColumnInfo(ID_COLUMN_NO_COLUMNS);
            if (columnInfo != null) {
                for (String propertyName : columnInfo.getPropertyNames()) {
                    propertyNames.add(propertyName);
                }
            } 
        } else {
            for (String columnId : columnIds) {
                LabelColumnInfo info = getColumnInfo(columnId);
                if (info != null) {
                    String[] infoPropertyNames = info.getPropertyNames();
                    for (int i = 0; i < infoPropertyNames.length; i++) {
                        propertyNames.add(infoPropertyNames[i]);
                    }
                }
            }
        }
        return propertyNames;
    }
    
    /**
     * Updates the label information based on given map of properties.
     * 
     * @param update Label update to write to.
     * @param status Result of the properties update
     * @param properties Properties retrieved from the element properties provider.
     * 
     * @since 2.0
     */
    protected void updateLabel(ILabelUpdate update, IStatus status, Map<String, Object> properties) {
        if (update.getColumnIds() == null) {
            LabelColumnInfo info = getColumnInfo(ID_COLUMN_NO_COLUMNS);
            if (info != null) {
                info.updateColumn(update, 0, status, properties);
            }
        } else {
            String[] columnIds = update.getColumnIds();
            
            for (int i = 0; i < columnIds.length; i++) {
                LabelColumnInfo info = getColumnInfo(columnIds[i]);
                if (info != null) {
                    info.updateColumn(update, i, status, properties);
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
