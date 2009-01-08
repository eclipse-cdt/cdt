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

package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.datamodel.IDMService;

public interface IFormattedValues extends IDMService {
    
    /** Marker interface for a DMC that has a formatted value. */
    public interface IFormattedDataDMContext extends IDMContext {}

    /**
     * These strings represent the standard known formats for any bit stream
     * which needs to be formatted. These ID's as well as others which may be
     * specifically available from the backend are what is returned from the
     * getID() method.
     */
    public final static String HEX_FORMAT     = "HEX.Format"     ; //$NON-NLS-1$
    public final static String OCTAL_FORMAT   = "OCTAL.Format"   ; //$NON-NLS-1$
    public final static String NATURAL_FORMAT = "NATURAL.Format" ; //$NON-NLS-1$
    public final static String BINARY_FORMAT  = "BINARY.Format"  ; //$NON-NLS-1$
    public final static String DECIMAL_FORMAT = "DECIMAL.Format" ; //$NON-NLS-1$
    public final static String STRING_FORMAT = "STRING.Format" ; //$NON-NLS-1$
    
    /**
     * Retrieves the  formats that the given data is available in.  
     * This method is asynchronous because the service may need to retrieve     
     * information from the backend in order to determine what formats are 
     * available for the given data context.
     * 
     * @param dmc Context for which to retrieve available formats.
     * @param rm Completion monitor returns an array of support formatIds.  
     */
    public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm);
        
    /**
     * Creates a FormattedValueDMContext representing the given formatId.  
     * 
     * @param dmc Parent context for the context that is being created
     * @param formatId Defines format to be used for the returned context.
     */
    public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String formatId);
    
    /**
     * Retrieves the DM data associated with given formatted value context.
     * @param dmc Context to retrieve the value for.
     * @param rm Completion monitor returns the formatted value.
     */
    public void getFormattedExpressionValue(FormattedValueDMContext dmc, DataRequestMonitor<FormattedValueDMData> rm); 

    
    /**
     * DMC that represents a value with specific format.  The format ID can be
     * persisted and used for comparison. 
     */

    public static class FormattedValueDMContext extends AbstractDMContext 
    {
        private final String fFormatID;
        
        public FormattedValueDMContext(IDMService service, IDMContext parent, String formatId) {
            super(service, new IDMContext[] { parent });
            fFormatID = formatId;
        }

        public FormattedValueDMContext(String sessionId, IDMContext parent, String formatId) {
            super(sessionId, new IDMContext[] { parent });
            fFormatID = formatId;
        }
        
        public String getFormatID() {
            return fFormatID;
        }
        
        @Override
        public boolean equals(Object obj) {
            return baseEquals(obj) && ((FormattedValueDMContext)obj).getFormatID().equals(getFormatID());
        }

        @Override
        public int hashCode() {
            return baseHashCode() + getFormatID().hashCode();
        }
        
        @Override
        public String toString() {
            return baseToString() + ".format(" + getFormatID() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static class FormattedValueDMData implements IDMData {

        private final String fValue;
        
        public FormattedValueDMData(String value) {
            fValue = value;
        }
        
        public String getFormattedValue() {
            return fValue;
        }
        
        
    }
}