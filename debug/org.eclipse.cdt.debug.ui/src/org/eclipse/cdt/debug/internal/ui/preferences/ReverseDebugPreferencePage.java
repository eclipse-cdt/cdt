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
        setDescription( Messages.ReverseDebugPreferencePage_1 );  
    }

    @Override
    protected void createFieldEditors() {
        FieldEditor edit = new BooleanFieldEditor(
                ICDebugPreferenceConstants.PREF_SHOW_ERROR_REVERSE_TRACE_METHOD_NOT_AVAILABLE,
                Messages.ReverseDebugPreferencePage_2, 
                getFieldEditorParent() );
            edit.fillIntoGrid( getFieldEditorParent(), 2 );
            getPreferenceStore().setDefault(ICDebugPreferenceConstants.PREF_SHOW_ERROR_REVERSE_TRACE_METHOD_NOT_AVAILABLE, true);
            addField( edit );

           edit = new RadioGroupFieldEditor(
                   ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE
                   , Messages.ReverseDebugPreferencePage_3 
                   ,1
                   , new String[][] {
                       {Messages.ReverseDebugPreferencePage_4, ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE},
                       {Messages.ReverseDebugPreferencePage_6, ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_BRANCH_TRACE},
                       {Messages.ReverseDebugPreferencePage_8, ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_PROCESSOR_TRACE}
                   }
                   , getFieldEditorParent() );
           edit.fillIntoGrid( getFieldEditorParent(), 1 );
           getPreferenceStore().setDefault(ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE, ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE);
           addField( edit );
    }

    @Override
    public void init( IWorkbench workbench ) {
    }
}
