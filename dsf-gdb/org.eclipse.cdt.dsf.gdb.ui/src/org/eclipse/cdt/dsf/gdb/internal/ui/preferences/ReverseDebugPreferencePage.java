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
package org.eclipse.cdt.dsf.gdb.internal.ui.preferences;

import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

@SuppressWarnings("restriction")
public class ReverseDebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ReverseDebugPreferencePage() {
        super( GRID );
        IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
        setPreferenceStore( store );
        setDescription( MessagesForPreferences.ReverseDebugPreferencePage_1 );  
    }

    @Override
    protected void createFieldEditors() {
        FieldEditor edit = new BooleanFieldEditor(
                ICDebugPreferenceConstants.PREF_SHOW_ERROR_REVERSE_TRACE_METHOD_NOT_AVAILABLE,
                MessagesForPreferences.ReverseDebugPreferencePage_2, 
                getFieldEditorParent() ) {
            /** We are swapping the preference store since PREF_SHOW_ERROR_REVERSE_TRACE_METHOD_NOT_AVAILABLE is 
             * available with CDebugUIPlugin  */

        	@Override
        	protected void doStore() {
                IPreferenceStore store = CDebugUIPlugin.getDefault().getPreferenceStore();
                setPreferenceStore( store );
        		super.doStore();
        		store = GdbUIPlugin.getDefault().getPreferenceStore();
                setPreferenceStore( store );
        	}

        	@Override
        	protected void doLoadDefault() {
                IPreferenceStore store = CDebugUIPlugin.getDefault().getPreferenceStore();
                setPreferenceStore( store );
        		super.doLoadDefault();
        		store = GdbUIPlugin.getDefault().getPreferenceStore();
                setPreferenceStore( store );
        	}

        	@Override
        	protected void doLoad() {
                IPreferenceStore store = CDebugUIPlugin.getDefault().getPreferenceStore();
                setPreferenceStore( store );
        		super.doLoad();
        		store = GdbUIPlugin.getDefault().getPreferenceStore();
                setPreferenceStore( store );
        	}
        };
           IPreferenceStore store = CDebugUIPlugin.getDefault().getPreferenceStore();
           setPreferenceStore( store );
           edit.fillIntoGrid( getFieldEditorParent(), 2 );
           getPreferenceStore().setDefault(ICDebugPreferenceConstants.PREF_SHOW_ERROR_REVERSE_TRACE_METHOD_NOT_AVAILABLE, true);
           addField( edit );

           store = GdbUIPlugin.getDefault().getPreferenceStore();
           setPreferenceStore( store );
           edit = new RadioGroupFieldEditor(
        		   IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE
                   , MessagesForPreferences.ReverseDebugPreferencePage_3 
                   ,1
                   , new String[][] {
                       {MessagesForPreferences.ReverseDebugPreferencePage_4, IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE},
                       {MessagesForPreferences.ReverseDebugPreferencePage_6, IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_BRANCH_TRACE},
                       {MessagesForPreferences.ReverseDebugPreferencePage_8, IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_PROCESSOR_TRACE}
                   }
                   , getFieldEditorParent() );
           edit.fillIntoGrid( getFieldEditorParent(), 1 );
           getPreferenceStore().setDefault(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE, IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE);
           addField( edit );
    }

    @Override
    public void init( IWorkbench workbench ) {
    }
}
