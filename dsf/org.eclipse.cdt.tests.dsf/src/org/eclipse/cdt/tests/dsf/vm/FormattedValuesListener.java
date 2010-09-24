/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdateListener;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElement;
import org.junit.Assert;

/**
 * 
 */
public class FormattedValuesListener implements IFormattedValuesListener, IPropertiesUpdateListener {

    private static final String ANY_FORMAT = "ANY_FORMAT";
    
    private final TestModel fModel;
    
    private List<IPropertiesUpdate> fPropertiesUpdates = new ArrayList<IPropertiesUpdate>();
    private List<List<FormattedValueDMContext>> fFormattedValuesInPending = new ArrayList<List<FormattedValueDMContext>>();
    private List<FormattedValueDMContext> fFormattedValuesInProgress = new LinkedList<FormattedValueDMContext>();
    private List<FormattedValueDMContext> fFormattedValuesCompleted = new ArrayList<FormattedValueDMContext>();
    
    private DsfRunnable fProcessUpdatedFormattedValuesRunnable = null;
    
    private Set<String> fCachedFormats = new HashSet<String>();
    
    public FormattedValuesListener(TestModel model) {
        fModel = model;
    }
    
    public void setCachedFormats(String[] cachedFormats) {
        fCachedFormats.clear();
        fCachedFormats.addAll(Arrays.asList(cachedFormats));
    }
    
    public void propertiesUpdatesStarted(IPropertiesUpdate[] updates) {
        fPropertiesUpdates.addAll(Arrays.asList(updates));
        List<FormattedValueDMContext> pending = new ArrayList<FormattedValueDMContext>(updates.length);
        for (IPropertiesUpdate update : updates) {
            List<String> formatIds = getRequestedFormatIDs(update);
            for (String formatId : formatIds) {
                TestElement te = getPropertyUpdateTestElement(update);
                pending.add(new FormattedValueDMContext(fModel, te, formatId));
            }
        }
        if (!pending.isEmpty()) {
            fFormattedValuesInPending.add(pending);
        }
    }

    private List<String> getRequestedFormatIDs(IPropertiesUpdate update) {
        List<String> formatIds = new ArrayList<String>(1);
        for (String property : update.getProperties()) {
            if (property.equals(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE)) {
                formatIds.add(ANY_FORMAT);
            }
            if (property.startsWith(IDebugVMConstants.PROP_FORMATTED_VALUE_BASE)) {
                formatIds.add(FormattedValueVMUtil.getFormatFromProperty(property, null));
            }
        }
        return formatIds;
    }

    public void reset() {
        reset(new String[0]);
    }

    public void reset(String[] cachedFormats) {
        fPropertiesUpdates.clear();
        fFormattedValuesInPending.clear();
        fFormattedValuesInProgress.clear();
        fFormattedValuesCompleted.clear();
        setCachedFormats(cachedFormats);
    }
    
    public List<FormattedValueDMContext> getFormattedValuesCompleted() {
        return fFormattedValuesCompleted;
    }

    public List<IPropertiesUpdate> getPropertiesUpdates() {
        return fPropertiesUpdates;
    }

    public boolean isFinished() {
        if ( !fFormattedValuesInProgress.isEmpty() ) {
            return false;
        }
        
        if (!fFormattedValuesInPending.isEmpty() && fCachedFormats.isEmpty()) {
            return false;
        }
        
        for (List<FormattedValueDMContext> pendingList : fFormattedValuesInPending) {
            for (FormattedValueDMContext pending : pendingList) {
                String pendingFormat = pending.getFormatID();
                if (!pendingFormat.equals(ANY_FORMAT) && !fCachedFormats.contains(pendingFormat)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public void propertiesUpdateCompleted(IPropertiesUpdate update) {}

    public void formattedValueUpdated(FormattedValueDMContext formattedValueDmc) {
        Assert.assertFalse("Expected values with formats " + fCachedFormats + " to be cached.", 
                           fCachedFormats.contains(formattedValueDmc.getFormatID()));
        
        if (fProcessUpdatedFormattedValuesRunnable == null) {
            fProcessUpdatedFormattedValuesRunnable = new DsfRunnable() {
                public void run() {
                    fProcessUpdatedFormattedValuesRunnable = null;
                    processFormattedValuesInProgress();
                }
            };
            fModel.getExecutor().execute(fProcessUpdatedFormattedValuesRunnable);
        }
        fFormattedValuesInProgress.add(formattedValueDmc);
    }

    private void processFormattedValuesInProgress() {
        while (!fFormattedValuesInProgress.isEmpty()) {
            List<FormattedValueDMContext> pendingList = findPendingList(fFormattedValuesInProgress.get(0));
            
            for (FormattedValueDMContext pending : pendingList) {
                int progressIdx = indexOfFormattedValueDMContext(fFormattedValuesInProgress, pending);
                
                if (progressIdx != -1) {
                    // The pending DMC may contain the ANY_FORMAT format ID.
                    // The progress DMC must contain the exact format retrieved. 
                    // To have a more accurate record, add the progress DMC to 
                    // the completed updates list.  
                    FormattedValueDMContext progress = fFormattedValuesInProgress.remove(progressIdx);
                    fFormattedValuesCompleted.add(progress);
                } else {
                    Assert.fail("Pending Updates not processed in bulk \n    " + pendingList);
                } 
            }
        }
    }
    
    private List<FormattedValueDMContext> findPendingList(FormattedValueDMContext dmc) {
        for (Iterator<List<FormattedValueDMContext>> itr = fFormattedValuesInPending.iterator(); itr.hasNext();) {
            List<FormattedValueDMContext> pendingList = itr.next();
            int pendingIdx = indexOfFormattedValueDMContext(pendingList, dmc);
            if (pendingIdx != -1) {
                itr.remove();
                return pendingList;
            }
        }
        throw new RuntimeException("Pending update not found for element: " + dmc);
    }
    
    private int indexOfFormattedValueDMContext(List<FormattedValueDMContext> list, FormattedValueDMContext dmc) {
        for (int i = 0; i < list.size(); i++) {
            if (dmc.getParentValueDMContext().equals(list.get(i).getParentValueDMContext())) {
                if ( ANY_FORMAT.equals(dmc.getFormatID()) ||
                     ANY_FORMAT.equals(list.get(i).getFormatID()) || 
                     dmc.getFormatID().equals(list.get(i).getFormatID()) ) 
                {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private TestElement getPropertyUpdateTestElement(IPropertiesUpdate update) {
        Object element = update.getElement();
        if (element instanceof TestElement) {
            return (TestElement)element; 
        } else if (element instanceof TestElementVMContext) {
            return ((TestElementVMContext)element).getElement();
        }
        throw new RuntimeException("Invalid element in properties update: " + update.getElement());
    }
    
}
