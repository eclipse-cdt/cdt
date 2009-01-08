/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 *  Provides default implementation of preference storage.
 */
@SuppressWarnings("restriction")
public class FormattedValuePreferenceStore implements IFormattedValuePreferenceStore {

    private static IFormattedValuePreferenceStore fgSingletonReference;
    
    public static IFormattedValuePreferenceStore getDefault() {
        if (fgSingletonReference == null) {
            fgSingletonReference = new FormattedValuePreferenceStore();
        }
        return fgSingletonReference;
    }
    
    public String getCurrentNumericFormat( IPresentationContext context ) {
        
        Object prop = context.getProperty( IDebugVMConstants.CURRENT_FORMAT_STORAGE );

        if ( prop != null ) {
            return (String) prop;
        }
        return IFormattedValues.NATURAL_FORMAT;
    }
}
