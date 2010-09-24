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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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

import com.ibm.icu.text.MessageFormat;

/**
 * A helper class for View Model Node implementations that support elements 
 * to be formatted using different number formats.  The various static methods in 
 * this class handle populating the properties of an IPropertiesUpdate using data
 * retrieved from a DSF service implementing {@link IFormattedValues} interface.
 * 
 * @see org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider
 * @see org.eclipse.cdt.dsf.debug.service.IFormattedValues
 * 
 * @since 2.0
 */
public class FormattedValueVMUtil {

    /**
     * Cache to avoid creating many duplicate strings of formats and properties.
     */
    private static Map<String, Map<String, String>> fFormatProperties = 
        Collections.synchronizedMap(new TreeMap<String, Map<String, String>>());
    
    /**
     * Common map of user-readable labels for format IDs.  
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
    
    /**
     * Adds a user-readable label for a given format ID.  If a given view model has a custom format ID, it can 
     * add its label to the map of format IDs using this method. 
     * 
     * @param formatId Format ID to set the label for.
     * @param label User-readable label for a format.
     */
    public static void setFormatLabel(String formatId, String label) {
        fFormatLabels.put(formatId, label);
    }
    
    /**
     * Returns a user readable label for a given format ID.
     */
    public static String getFormatLabel(String formatId) {
        String label = fFormatLabels.get(formatId);
        if (label != null) {
            return label;
        } else {
            return MessageFormat.format(
                MessagesForNumberFormat.FormattedValueVMUtil_Other_format__format_text, new Object[] { formatId });
        }
    }
    
    /**
     * Returns an element property representing an element value in a given format.  

     * @deprecated Replaced by {@link #getPropertyForFormatId(String, String)}
     */
    public static String getPropertyForFormatId(String formatId) {
        return getPropertyForFormatId(formatId, ""); //$NON-NLS-1$
    }    

    /**
     * Returns an element property representing an element value in a given format.  
     * 
     * @param Format ID to create the property for.
     * @param prefix The prefix for the property that is used to distinguish 
     * it from other number format values in a given property map.   May be 
     * <code>null</code> or an empty string if no prefix is used.
     * @return The generated property name.
     * 
     * @since 2.2
     */
    public static String getPropertyForFormatId(String formatId, String prefix) {
        if (formatId == null) {
            return null;
        }
        if (prefix == null) {
            prefix = ""; //$NON-NLS-1$
        }
        synchronized(fFormatProperties) {
            Map<String, String> formatsMap = getFormatsMap(prefix);
            String property = formatsMap.get(formatId);
            if (property == null) {
                property = (prefix + IDebugVMConstants.PROP_FORMATTED_VALUE_BASE + "." + formatId).intern();  //$NON-NLS-1$
                formatsMap.put(formatId, property);
            }
            return property;
        }
    }    

    private static Map<String, String> getFormatsMap(String prefix) {
        synchronized(fFormatProperties) {
            Map<String, String> prefixMap = fFormatProperties.get(prefix);
            if (prefixMap == null) {
                prefixMap = new TreeMap<String, String>();
                fFormatProperties.put(prefix, prefixMap);
            }
            return prefixMap;
        }
    }
    
    /**
     * Returns a format ID based on the element property representing a 
     * formatted element value.
     * 
     * @deprecated Replaced by {@link #getFormatFromProperty(String, String)}
     */
    public static String getFormatFromProperty(String property) {
        return getFormatFromProperty(property, ""); //$NON-NLS-1$
    }    

    /**
     * Returns a format ID based on the element property representing a 
     * formatted element value.  This method has an additional prefix parameter
     * which is used when multiple number formats are stored in a single 
     * property map. 
     * 
     * @param property The property to extract the format from.
     * @param prefix The prefix for the property that is used to distinguish 
     * it from other number format values in a given property map.   May be 
     * <code>null</code> or an empty string if no prefix is used.
     * @return The format ID.
     * 
     * @throws IllegalArgumentException if the property is not a formatted value
     * property.
     * 
     * @since 2.2
     */
    public static String getFormatFromProperty(String property, String prefix) {
        if (prefix == null) {
            prefix = ""; //$NON-NLS-1$
        }
        
        synchronized(fFormatProperties) {
            Map<String, String> formatsMap = getFormatsMap(prefix);
            for (Map.Entry<String, String> entry : formatsMap.entrySet()) {
                if (entry.getValue().equals(property)) {
                    return entry.getKey();
                }
            }
            if ( !property.startsWith(prefix) || 
                !property.startsWith(IDebugVMConstants.PROP_FORMATTED_VALUE_BASE, prefix.length()) )
            {
               throw new IllegalArgumentException("Property " + property + " is not a valid formatted value format property.");  //$NON-NLS-1$//$NON-NLS-2$
            }
            String formatId = property.substring(
                prefix.length() + IDebugVMConstants.PROP_FORMATTED_VALUE_BASE.length() + 1).intern();
            formatsMap.put(formatId, property);
            return formatId;
        }
    }    


    /**
     * Returns the user-selected number format that is saved in the given 
     * presentation context.
     */
    public static String getPreferredFormat(IPresentationContext context) {
        Object prop = context.getProperty( IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE );
        if ( prop != null ) {
            return (String) prop;
        }
        return IFormattedValues.NATURAL_FORMAT;        
    }

    
    /**
     * This method fills in the formatted value properties in the given array 
     * of property update objects using data retrieved from the given 
     * formatted values service.     
     * 
     * @param updates The array of updates to fill in information to.  This  
     * update is used to retrieve the data model context and to write the 
     * properties into. Implementation will not directly mark these updates 
     * complete, but contribute towards that end by marking [monitor] complete.
     * @param service The service to be used to retrieve the values from.
     * @param dmcType The class type of the data model context.  Some updates
     * can contain multiple formatted data data model contexts, and this
     * method assures that there is no ambiguity in which context should be 
     * used.
     * @param monitor Request monitor used to signal completion of work
     * 
     * @deprecated This method has been replaced by the {@link FormattedValueRetriever}
     * utility.
     */
    @ConfinedToDsfExecutor("service.getExecutor()")
    public static void updateFormattedValues(
        final IPropertiesUpdate updates[],
        final IFormattedValues service,
        final Class<? extends IFormattedDataDMContext> dmcType,
        final RequestMonitor monitor)
    { 
		// First retrieve the available formats for each update's element (if
		// needed). Store the result in a map (for internal use) and in the
		// update object (if requested). After that's done, call another method
		// to retrieve the formatted values. Note that we use a synchronized map
		// because it's updated by a request monitor with an ImmediateExecutor.
        final Map<IPropertiesUpdate, String[]> availableFormats = Collections.synchronizedMap(new HashMap<IPropertiesUpdate, String[]>(updates.length * 4/3));
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(
            service.getExecutor(), monitor) { 
                @Override
                protected void handleCompleted() {
                	// Retrieve the formatted values now that we have the available formats (where needed).
                	// Note that we are passing off responsibility of our parent monitor  
                    updateFormattedValuesWithAvailableFormats(updates, service, dmcType, availableFormats, monitor); 
                    
                    // Note: we must not call the update's done method
                }
        	};
        int count = 0;
        
		// For each update, query the formats available for the update's
		// element...but only if necessary. The available formats are necessary
		// only if the update explicitly requests that information, or if the
		// update is asking what the active format is or is asking for the value
		// of the element in that format. The reason we need them in the last
		// two cases is that we can't establish the 'active' format for an
		// element without knowing its available formats. See
		// updateFormattedValuesWithAvailableFormats(), as that's where we make
		// that determination.
        for (final IPropertiesUpdate update : updates) {
            if ((!update.getProperties().contains(IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS) && 
                !update.getProperties().contains(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT) &&
                !update.getProperties().contains(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE)))
            {
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
                new ViewerDataRequestMonitor<String[]>(ImmediateExecutor.getInstance(), update) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                        	// Set the result (available formats) into the update object if it was requested 
                        	if (update.getProperties().contains(IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS)) {
                        		update.setProperty(IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS, getData());
                        	}
                        	
                        	// also add it to the map; we'll need to access it when querying the element's value.
                            availableFormats.put(update, getData());
                        } else {
                            update.setStatus(getStatus()); 
                        }
                        countingRm.done();

						// Note we don't mark the update object done, and we
						// avoid calling our base implementation so that it
						// doesn't either. The completion of this request is
						// just a step in servicing the update.
                    }
                });
            count++;
        }
        countingRm.setDoneCount(count);
    }

	/**
	 * @param updates
	 *            the update objects to act on. Implementation will not directly
	 *            mark these complete, but contribute towards that end by
	 *            marking [monitor] complete.
	 * @param availableFormatsMap
	 *            prior to calling this method, the caller queries (where
	 *            necessary) the formats supported by the element in each
	 *            update, and it puts that information in this map. If an entry
	 *            in [updates] does not appear in this map, it means that its
	 *            view-model element doesn't support any formats (very
	 *            unlikely), or that the available formats aren't necessary to
	 *            service the properties specified in the update
	 * @param monitor
	 *            Request monitor used to signal completion of work
	 */
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
            
			// Determine the 'active' value format. It is the view preference if
			// and only if the element supports it. Otherwise it is the first
			// format supported by the element. If our caller didn't provide the
			// available formats for an update (element), then it means the
			// update doesn't contain any properties that requires us to
			// determine the active format.
            String[] availableFormats = availableFormatsMap.get(update);
            String _activeFormat = null;
            if (availableFormats != null && availableFormats.length != 0) {
                _activeFormat = getPreferredFormat(update.getPresentationContext());
                update.setProperty(IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE, _activeFormat);
                if (!isFormatAvailable(_activeFormat, availableFormats)) {
                    _activeFormat = availableFormats[0];
                }
            }
            final String activeFormat = _activeFormat;	// null means we don't need to know what the active format is
            
            if (update.getProperties().contains(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT)) {
            	assert activeFormat != null : "Our caller should have provided the available formats if this property was specified; given available formats, an 'active' nomination is guaranteed."; //$NON-NLS-1$
            	update.setProperty(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT, activeFormat);
            }
            
			// Service the properties that ask for the value in a specific
			// format. If the update request contains the property
			// PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE, and the active format
			// has not been explicitly requested, then we need an additional
			// iteration to provide it. 
            boolean activeFormatValueRequested = false;	// does the update object ask for PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE?
            boolean activeFormatValueHandled = false;	// have we come across a specific format request that is the active format?
            if (update.getProperties().contains(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE)) {
            	assert activeFormat != null : "Our caller should have provided the available formats if this property was specified; given available formats, an 'active' nomination is guaranteed."; //$NON-NLS-1$
            	activeFormatValueRequested = true; // we may end up making an additional run
            }

            for (Iterator<String> itr = update.getProperties().iterator(); itr.hasNext() || (activeFormatValueRequested && !activeFormatValueHandled);) {
                String nextFormat;
                if (itr.hasNext()) {
                    String propertyName = itr.next();
                    if (propertyName.startsWith(IDebugVMConstants.PROP_FORMATTED_VALUE_BASE)) {
                        nextFormat = FormattedValueVMUtil.getFormatFromProperty(propertyName);
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

                final boolean _activeFormatValueRequested = activeFormatValueRequested; 
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
                                if (_activeFormatValueRequested && format.equals(activeFormat)) {
                                    update.setProperty(
                                        IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE, 
                                        getData().getFormattedValue());
                                }
                            } else {
                                update.setStatus(getStatus());
                            }
                            countingRm.done();
                            
                            // Note: we must not call the update's done method
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
