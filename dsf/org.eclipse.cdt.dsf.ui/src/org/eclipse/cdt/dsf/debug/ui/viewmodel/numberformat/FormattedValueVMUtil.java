/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import com.ibm.icu.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
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
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * 
 * @since 2.0
 */
public class FormattedValueVMUtil {

    /**
     * Common map of user-readable labels for format IDs.  UI components for 
     */
    private static Map<String, String> fFormatLabels = new HashMap<String, String>(8);
    
    static {
        setFormatLabel(IFormattedValues.NATURAL_FORMAT, MessagesForNumberFormat.FormattedValueVMUtil_Natural_format__label);
        setFormatLabel(IFormattedValues.HEX_FORMAT, MessagesForNumberFormat.FormattedValueVMUtil_Hex_format__label);
        setFormatLabel(IFormattedValues.DECIMAL_FORMAT, MessagesForNumberFormat.FormattedValueVMUtil_Decimal_format__label);
        setFormatLabel(IFormattedValues.OCTAL_FORMAT, MessagesForNumberFormat.FormattedValueVMUtil_Octal_format__label);
        setFormatLabel(IFormattedValues.BINARY_FORMAT, MessagesForNumberFormat.FormattedValueVMUtil_Binary_format__label);
        setFormatLabel(IFormattedValues.STRING_FORMAT, MessagesForNumberFormat.FormattedValueVMUtil_String_format__label);
    }
    
    public static void setFormatLabel(String formatId, String label) {
        fFormatLabels.put(formatId, label);
    }
    
    public static String getFormatLabel(String formatId) {
        String label = fFormatLabels.get(formatId);
        if (label != null) {
            return label;
        } else {
            return MessageFormat.format(
                MessagesForNumberFormat.FormattedValueVMUtil_Other_format__format_text, new Object[] { formatId });
        }
    }
    
    public static String getPropertyForFormatId(String formatId) {
        if (formatId == null) {
            return null;
        }
        return IDebugVMConstants.PROP_FORMATTED_VALUE_BASE + "." + formatId;  //$NON-NLS-1$ 
    }    

    public static String getFormatFromProperty(String property) {
        return property.substring(IDebugVMConstants.PROP_FORMATTED_VALUE_BASE.length() + 1); 
    }    

    
    public static String getPreferredFormat(IPresentationContext context) {
        Object prop = context.getProperty( IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE );
        if ( prop != null ) {
            return (String) prop;
        }
        return IFormattedValues.NATURAL_FORMAT;        
    }
    
    @ConfinedToDsfExecutor("service.getExecutor()")
    public static void updateFormattedValues(
        final IPropertiesUpdate updates[],
        final IFormattedValues service,
        final Class<? extends IFormattedDataDMContext> dmcType,
        final RequestMonitor monitor)
    { 
        // First retrieve the available formats for all the updates, and store it in a map (as well as the updates).
        // After that is completed call another method to retrieve the formatted values.
        final Map<IPropertiesUpdate, String[]> availableFormats = new HashMap<IPropertiesUpdate, String[]>(updates.length * 4/3);
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(
            service.getExecutor(), 
            new RequestMonitor(service.getExecutor(), monitor) {
                @Override
                protected void handleCompleted() {
                    updateFormattedValuesWithAvailableFormats(updates, service, dmcType, availableFormats, monitor); 
                }
            });
        int count = 0;
        
        for (final IPropertiesUpdate update : updates) {
            if (!update.getProperties().contains(IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS) && 
                !update.getProperties().contains(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT) &&
                !update.getProperties().contains(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE))
            {
                // Update is not requesting any formatted value information, so just skip it.  If specitic formats were
                // requested for this update, they will still be retrieved by updateFormattedValuesWithAvailableFormats.
                continue;
            }
            
            IFormattedDataDMContext dmc = null;
            if (update.getElement() instanceof IDMVMContext) {
                dmc = DMContexts.getAncestorOfType(((IDMVMContext)update.getElement()).getDMContext(), dmcType);
            }
            
            if (dmc == null) {
                update.setStatus(DsfUIPlugin.newErrorStatus(IDsfStatusConstants.INVALID_HANDLE, "Update element did not contain a valid context: " + dmcType, null)); //$NON-NLS-1$
                continue;
            } 
            
            service.getAvailableFormats(
                dmc, 
                // Use the ViewerDataRequestMonitor in order to propagate the update's cancel request.  But this means that 
                // we have to override the handleRequestedExecutionException() to guarantee that the caller's RM gets 
                // completed even when the service session has been shut down.
                new ViewerDataRequestMonitor<String[]>(service.getExecutor(), update) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            update.setProperty(IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS, getData());
                            availableFormats.put(update, getData());
                        } else {
                            update.setStatus(getStatus()); 
                        }
                        countingRm.done();
                    }
                    
                    @Override
                    protected void handleRejectedExecutionException() {
                        countingRm.setStatus(DsfUIPlugin.newErrorStatus(IDsfStatusConstants.INVALID_STATE, "Request for monitor: '" + toString() + "' resulted in a rejected execution exception.", null));  //$NON-NLS-1$//$NON-NLS-2$
                        countingRm.done();
                    }
                });
            count++;
        }
        countingRm.setDoneCount(count);
    }
    
    @ConfinedToDsfExecutor("service.getExecutor()")
    private static void updateFormattedValuesWithAvailableFormats(
        IPropertiesUpdate updates[],
        IFormattedValues service,
        Class<? extends IFormattedDataDMContext> dmcType,
        Map<IPropertiesUpdate, String[]> availableFormatsMap,
        final RequestMonitor monitor)
    { 
        // Use a single counting RM for all the requested formats for each update.
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(ImmediateExecutor.getInstance(), monitor);
        int count = 0;
        
        for (final IPropertiesUpdate update : updates) {
            IFormattedDataDMContext dmc = null;
            if (update.getElement() instanceof IDMVMContext) {
                dmc = DMContexts.getAncestorOfType(((IDMVMContext)update.getElement()).getDMContext(), dmcType);
            }
            if (dmc == null) {
                // The error status should already be set by the calling method.
                continue;
            } 
            
            // Calculate the active value format based on view preference and available formats.
            String[] availableFormats = availableFormatsMap.get(update);
            String _activeFormat = null;
            if (availableFormats != null && availableFormats.length != 0) {
                _activeFormat = getPreferredFormat(update.getPresentationContext());
                update.setProperty(IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE, _activeFormat);
                if (!isFormatAvailable(_activeFormat, availableFormats)) {
                    _activeFormat = availableFormats[0];
                }
            }
            final String activeFormat = _activeFormat;
            
            update.setProperty(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT, activeFormat);
            
            // Loop through all the requested properties and check if any of them are for the formatted value.  In the 
            // same loop, try to see if the active format is already requested and if it's not request it at the end.
            boolean activeFormatRequested = activeFormat == null;
            for (Iterator<String> itr = update.getProperties().iterator(); itr.hasNext() || !activeFormatRequested;) {
                String nextFormat;
                if (itr.hasNext()) {
                    String propertyName = itr.next();
                    if (propertyName.startsWith(IDebugVMConstants.PROP_FORMATTED_VALUE_BASE)) {
                        nextFormat = FormattedValueVMUtil.getFormatFromProperty(propertyName);
                        if (nextFormat.equals(activeFormat)) {
                            activeFormatRequested = true;
                        }
                        if (availableFormats != null && !isFormatAvailable(nextFormat, availableFormats)) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                } else {
                    nextFormat = activeFormat;
                    activeFormatRequested = true;
                }

                final FormattedValueDMContext formattedValueDmc = service.getFormattedValueContext(dmc, nextFormat);
                service.getFormattedExpressionValue(
                    formattedValueDmc, 
                    // Here also use the ViewerDataRequestMonitor in order to propagate the update's cancel request. 
                    // Use an immediate executor to avoid the possibility of a rejected execution exception.
                    new ViewerDataRequestMonitor<FormattedValueDMData>(ImmediateExecutor.getInstance(), update) {
                        @Override
                        protected void handleCompleted() {
                            if (isSuccess()) {
                                String format = formattedValueDmc.getFormatID();
                                update.setProperty(
                                    FormattedValueVMUtil.getPropertyForFormatId(format), 
                                    getData().getFormattedValue()); 
                                if (format.equals(activeFormat)) {
                                    update.setProperty(
                                        IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE, 
                                        getData().getFormattedValue());
                                }
                            } else {
                                update.setStatus(getStatus());
                            }
                            countingRm.done();
                        };
                    });
                count++;
            }
        }
        countingRm.setDoneCount(count);
    }
        
    private static boolean isFormatAvailable(String format, String[] availableFormats) {
        for (String availableFormat : availableFormats) {
            if (availableFormat.equals(format)) {
                return true;
            }
        }
        return false;
    }

}
