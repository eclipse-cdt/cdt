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
/*        
        addField( new RadioGroupFieldEditor( 
                        ICDebugPreferenceConstants.PREF_OPEN_DISASSEMBLY_MODE,
                        "Open disassembly if source is not available", 
                        3, 
                        new String[][] {
                            { "Always", MessageDialogWithToggle.ALWAYS },
                            { "Never", MessageDialogWithToggle.NEVER },
                            { "Prompt", MessageDialogWithToggle.PROMPT }, 
                        }, 
                        getFieldEditorParent(), 
                        true ) );
*/      
        Group group = ControlFactory.createGroup( getFieldEditorParent(), "Display settings", 1 );
        Composite spacer = ControlFactory.createComposite( group, 1 );

        FieldEditor edit = new BooleanFieldEditor(
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
    public void init( IWorkbench workbench ) {
    }
}
