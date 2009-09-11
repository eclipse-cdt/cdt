/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IElementUpdateTester;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IElementUpdateTesterExtension;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ManualUpdatePolicy;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreePath;

/**
 * Manual update policy with extensions specific for the debugger views.  It 
 * properly handles the changes in active number format values in debug view. 
 * 
 * @since 2.1
 */
public class DebugManualUpdatePolicy extends ManualUpdatePolicy {

    public static String DEBUG_MANUAL_UPDATE_POLICY_ID = "org.eclipse.cdt.dsf.debug.ui.viewmodel.update.debugManualUpdatePolicy";  //$NON-NLS-1$

    private static final List<String> ACTIVE_NUMBER_FORMAT_PROPERTIES = new ArrayList<String>(1);
    
    static {
        ACTIVE_NUMBER_FORMAT_PROPERTIES.add(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT);
    }

    /**
     * This specialized element update tester flushes the active number format 
     * property of the elemetn under consideration.  The partial property flush
     * is performed only if the cache entry is not yet dirty. 
     */
    private static IElementUpdateTester fgNumberFormatPropertyEventUpdateTester = new IElementUpdateTesterExtension() {
        
        public int getUpdateFlags(Object viewerInput, TreePath path) {
            return FLUSH_PARTIAL_PROPERTIES; 
        }  
        
        public Collection<String> getPropertiesToFlush(Object viewerInput, TreePath path, boolean isDirty) {
            if (!isDirty) {
                return ACTIVE_NUMBER_FORMAT_PROPERTIES;
            }
            return null;
        }
        
        public boolean includes(IElementUpdateTester tester) {
            return tester.equals(this);
        }
        
        @Override
        public String toString() {
            return "Manual (refresh = false) update tester for an event that did not originate from the data model"; //$NON-NLS-1$
        }
    };

    @Override
    public String getID() {
        return DEBUG_MANUAL_UPDATE_POLICY_ID;
    }

    @Override
    public IElementUpdateTester getElementUpdateTester(Object event) {
        if ((event instanceof PropertyChangeEvent && 
            ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE)) 
        {
            return fgNumberFormatPropertyEventUpdateTester;
        }
        return super.getElementUpdateTester(event);
    }
}
