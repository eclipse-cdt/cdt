/*
 * IFormattedValuesService.java
 * Created on Apr 16, 2007
 *
 * Copyright 2007 Wind River Systems Inc. All rights reserved.
*/
package org.eclipse.dd.dsf.debug.service;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMService;

public interface IFormattedValues extends IDMService {

    /** Marker interface for a DMC that has a formatted value. */
    public interface IDataDMContext<V extends IDMData> extends IDMContext<V> {}

    /**
     * These strings represent the standard known formats  for any bit stream
     * which needs to be formatted. These ID's as well as others which may be
     * specifically available from the backend  are what is returned from the
     * getID() method.
     */
    public final static String HEX_FORMAT     = "HEX.Format" ; //$NON-NLS-1$
    public final static String OCTAL_FORMAT   = "OCTAL.Format" ; //$NON-NLS-1$
    public final static String BINARY_FORMAT  = "BINARY.Format" ; //$NON-NLS-1$
    public final static String NATURAL_FORMAT = "NATURAL.Format" ; //$NON-NLS-1$
    
    /**
     * DMC that represents a value with specific format.  The format ID can be
     * persisted and used for comparison. 
     */
    public interface IValueDMContext extends  IDMContext<IValueDMData>
    {
        public String getID();
    }

    /**
     * DM Data returned when a formatted value DMC is evaluated.  It contains 
     * only the properly formatted string.
     * 
     * @param includePrefix Should the resulting formatted string contain the
     * typical prefix ( if any exists for this format - e.g. 0x, 0b, 0 ).
     * @param leadingZeros Should the resulting formatted string contain leading 0's.
     */
    public interface IValueDMData extends IDMData {
        String getFormattedValue();
    }

    /**
     * Retrieves the available formats that the given data is available in.  
     * This method is asynchronous because the service may need to retrieve     
     * information from the back end in order to determine what formats are 
     * available for the given data context.
     * 
     * @param dmc Context for which to retrieve available formatted values.
     * @param formatIDs Currently supported format IDs.  
     */
    public void getAvailableFormattedValues(IDataDMContext<?> dmc, DataRequestMonitor<String[]> formatIDs);
        
    /**
     * Retrieves the available formats that the given data is available in.  
     * This method is asynchronous because the service may need to retrieve     
     * information from the back end in order to determine what formats are 
     * available for the given data context.
     * 
     * @param dmc Context for which to retrieve a IValueDMContext.
     * @param formatId Defines format to be supplied from the returned context.
     */
    public IValueDMContext getFormattedValue(IDataDMContext<?> dmc, String formatId);
}