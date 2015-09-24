/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ReverseDebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ReverseDebugPreferencePage() {
        super( GRID );
        IPreferenceStore store = CDebugUIPlugin.getDefault().getPreferenceStore();
        setPreferenceStore( store );
        setDescription( "Reverse Debug Settings" );  //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        FieldEditor edit = new BooleanFieldEditor(
                ICDebugPreferenceConstants.PREF_SHOW_ERROR_REVERSE_TRACE_METHOD_NOT_AVAILABLE,
                "Show Error Dialog when Reverse Trace Method not available", //$NON-NLS-1$
                getFieldEditorParent() );
            edit.fillIntoGrid( getFieldEditorParent(), 2 );
            getPreferenceStore().setDefault(ICDebugPreferenceConstants.PREF_SHOW_ERROR_REVERSE_TRACE_METHOD_NOT_AVAILABLE, true);
            addField( edit );

           edit = new RadioGroupFieldEditor(
                   ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE
                   , "Select Hardware Tracing Method" //$NON-NLS-1$
                   ,1
                   , new String[][] {
                       {"Use GDB preference", "UseGdbTrace"},  //$NON-NLS-1$//$NON-NLS-2$
                       {"Use Branch Trace", "UseBranchTrace"}, //$NON-NLS-1$ //$NON-NLS-2$
                       {"Use Processor Trace", "UseProcessorTrace"}  //$NON-NLS-1$//$NON-NLS-2$
                   }
                   , getFieldEditorParent() );
           edit.fillIntoGrid( getFieldEditorParent(), 1 );
           getPreferenceStore().setDefault(ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE, "UseGdbTrace"); //$NON-NLS-1$
           addField( edit );
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init( IWorkbench workbench ) {
    }
}
