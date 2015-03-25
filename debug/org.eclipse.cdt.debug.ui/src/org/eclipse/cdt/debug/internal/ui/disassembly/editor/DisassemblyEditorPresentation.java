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

package org.eclipse.cdt.debug.internal.ui.disassembly.editor;

import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;

/**
 * org.eclipse.cdt.debug.internal.ui.disassembly.viewer.DisassemblyEditorPresentation: 
 */
public class DisassemblyEditorPresentation extends PresentationContext implements IDocumentPresentation {

    public static final String PROPERTY_SHOW_INSTRUCTIONS = "PROPERTY_SHOW_INSTRUCTIONS"; //$NON-NLS-1$
    public static final String PROPERTY_SHOW_SOURCE = "PROPERTY_SHOW_SOURCE"; //$NON-NLS-1$
    public static final String PROPERTY_SHOW_ADDRESSES = "PROPERTY_SHOW_ADDRESSES"; //$NON-NLS-1$
    public static final String PROPERTY_SHOW_LINE_NUMBERS = "PROPERTY_SHOW_LINE_NUMBERS"; //$NON-NLS-1$

    private boolean fShowAddresses = true;
    private boolean fShowLineNumbers = true;

    public DisassemblyEditorPresentation() {
        super( ICDebugUIConstants.ID_DEFAULT_DISASSEMBLY_EDITOR );
        setProperty( PROPERTY_SHOW_INSTRUCTIONS, Boolean.valueOf( CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean( ICDebugPreferenceConstants.PREF_DISASM_SHOW_INSTRUCTIONS ) ) );
        setProperty( PROPERTY_SHOW_SOURCE, Boolean.valueOf( CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean( ICDebugPreferenceConstants.PREF_DISASM_SHOW_SOURCE ) ) );
    }

    public boolean showIntstructions() {
        return ((Boolean)getProperty( PROPERTY_SHOW_INSTRUCTIONS )).booleanValue();
    }

    public void setShowIntstructions( boolean showIntstructions ) {
        setProperty( PROPERTY_SHOW_INSTRUCTIONS, Boolean.valueOf( showIntstructions ) );
    }

    public boolean showSource() {
        return ((Boolean)getProperty( PROPERTY_SHOW_SOURCE )).booleanValue();
    }

    public void setShowSource( boolean showSource ) {
        setProperty( PROPERTY_SHOW_SOURCE, Boolean.valueOf( showSource ) );
    }

    public boolean showAddresses() {
        return fShowAddresses;
    }

    public void setShowAddresses( boolean showAddresses ) {
        fShowAddresses = showAddresses;
    }

    public boolean showLineNumbers() {
        return fShowLineNumbers;
    }

    public void setShowLineNumbers( boolean showLineNumbers ) {
        fShowLineNumbers = showLineNumbers;
    }
}
