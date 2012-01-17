/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DisassemblyPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public DisassemblyPreferencePage() {
        super( GRID );
        IPreferenceStore store = CDebugUIPlugin.getDefault().getPreferenceStore();
        setPreferenceStore( store );
        setDescription( "Disassembly Settings" ); 
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        Group group = ControlFactory.createGroup( getFieldEditorParent(), "Open disassembly options", 1 );
        Composite spacer = ControlFactory.createComposite( group, 1 );
        FieldEditor edit = new BooleanFieldEditor(
                ICDebugPreferenceConstants.PREF_DISASM_OPEN_NO_SOURCE_INFO,
                "Source information is not available",
                spacer );
            edit.fillIntoGrid( spacer, 2 );
            addField( edit );

            edit = new BooleanFieldEditor(
                ICDebugPreferenceConstants.PREF_DISASM_OPEN_SOURCE_NOT_FOUND,
                "Source file not found",
                spacer );
            edit.fillIntoGrid( spacer, 2 );
            addField( edit );


        group = ControlFactory.createGroup( getFieldEditorParent(), "Display settings", 1 );
        spacer = ControlFactory.createComposite( group, 1 );

        edit = new BooleanFieldEditor(
            ICDebugPreferenceConstants.PREF_DISASM_SHOW_INSTRUCTIONS,
            "Show instructions",
            spacer );
        edit.fillIntoGrid( spacer, 2 );
        addField( edit );

        edit = new BooleanFieldEditor(
            ICDebugPreferenceConstants.PREF_DISASM_SHOW_SOURCE,
            "Show source",
            spacer );
        edit.fillIntoGrid( spacer, 2 );
        addField( edit );
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
	public void init( IWorkbench workbench ) {
    }
}
