/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/

package org.eclipse.tm.terminal;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class TerminalPreferencePage extends FieldEditorPreferencePage 
    implements  IWorkbenchPreferencePage,
                TerminalTarget,
                TerminalConsts
{
    /**
     * 
     */
    protected TerminalBooleanFieldEditor    m_editorLimitOutput;
    protected IntegerFieldEditor            m_editorBufferSize;
    protected IntegerFieldEditor            m_editorSerialTimeout;
    protected IntegerFieldEditor            m_editorNetworkTimeout;

    /**
     * 
     */
    public TerminalPreferencePage()
    {
        super(GRID);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TerminalTarget interface
    //

    /**
     *
     */
    public void execute(String strMsg,Object data)
    {
        if (strMsg.equals(ON_LIMITOUTPUT_SELECTED))
        {
            onLimitOutputSelected(data);
        }
        else
        {
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Message handlers
    //
    
    /**
     * 
     */
    protected void onLimitOutputSelected(Object data)
    {
        Button  ctlButton;
        Text    ctlText;
        Label   ctlLabel;
        boolean bEnabled;
        
        ctlButton   = m_editorLimitOutput.getChangeControl(getFieldEditorParent());
        ctlText     = m_editorBufferSize.getTextControl(getFieldEditorParent());
        ctlLabel    = m_editorBufferSize.getLabelControl(getFieldEditorParent());
        bEnabled    = ctlButton.getSelection();

        ctlText.setEnabled(bEnabled);
        ctlLabel.setEnabled(bEnabled);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FieldEditorPreferencePage interface
    //

    /**
     * 
     */
    protected void createFieldEditors()
    {
        setupPage();
    }

    /**
     * 
     */
    protected void initialize() 
    {
        super.initialize();
        
        execute(ON_LIMITOUTPUT_SELECTED,null);
    }

    /**
     * 
     */
    protected void performDefaults() 
    {
        super.performDefaults();
        
        execute(ON_LIMITOUTPUT_SELECTED,null);
    }

    /**
     * 
     */
    public void init(IWorkbench workbench)
    {
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Operations
    //
    
    /**
     * 
     */
    protected void setupPage()
    {
        setupData();
        setupEditors();
        setupListeners();
    }
    
    /**
     * 
     */
    protected void setupData()
    {
        TerminalPlugin      plugin;
        IPreferenceStore    preferenceStore;
        
        plugin          = TerminalPlugin.getDefault();
        preferenceStore = plugin.getPreferenceStore();
        setPreferenceStore(preferenceStore);
    }

    /**
     * 
     */
    protected void setupEditors()
    {
        m_editorLimitOutput     = new TerminalBooleanFieldEditor(TERMINAL_PREF_LIMITOUTPUT,
                                                                 TERMINAL_TEXT_LIMITOUTPUT,
                                                                 getFieldEditorParent());
        m_editorBufferSize      = new IntegerFieldEditor(TERMINAL_PREF_BUFFERLINES,
                                                         TERMINAL_TEXT_BUFFERLINES,
                                                         getFieldEditorParent());
        m_editorSerialTimeout   = new IntegerFieldEditor(TERMINAL_PREF_TIMEOUT_SERIAL,
                                                         TERMINAL_TEXT_SERIALTIMEOUT,
                                                         getFieldEditorParent());
        m_editorNetworkTimeout  = new IntegerFieldEditor(TERMINAL_PREF_TIMEOUT_NETWORK,
                                                         TERMINAL_TEXT_NETWORKTIMEOUT,
                                                         getFieldEditorParent());
                      
        m_editorBufferSize.setValidRange(0,Integer.MAX_VALUE);
        m_editorSerialTimeout.setValidRange(0,Integer.MAX_VALUE);
        m_editorNetworkTimeout.setValidRange(0,Integer.MAX_VALUE);
        
        addField(m_editorLimitOutput);
        addField(m_editorBufferSize);
        addField(m_editorSerialTimeout);                                            
        addField(m_editorNetworkTimeout);
    }

    /**
     * 
     */
    protected void setupListeners()
    {
        TerminalSelectionHandler    selectionHandler;
        Button                      ctlButton;
            
        selectionHandler    = new TerminalSelectionHandler();
        ctlButton           = m_editorLimitOutput.getChangeControl(getFieldEditorParent());
        ctlButton.addSelectionListener(selectionHandler);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner classes
    //
    
    /**
     * 
     */
    public class TerminalBooleanFieldEditor extends BooleanFieldEditor 
    {
        /**
         * 
         */
        public TerminalBooleanFieldEditor(String    strName,
                                          String    strLabel,
                                          Composite ctlParent) 
        {
            super(strName,strLabel,ctlParent);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // BooleanFieldEditor interface
        //
    
        /**
         * 
         */
        public Button getChangeControl(Composite parent) 
        {
            return super.getChangeControl(parent);
        }
    }


    /**
     * 
     */
    protected class TerminalSelectionHandler extends SelectionAdapter
    {
        /**
         * 
         */
        protected TerminalSelectionHandler()
        {
            super();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // SelectionAdapter interface
        //

        /**
         * 
         */
        public void widgetSelected(SelectionEvent event) 
        {
            Object  source;
            Button  ctlButton;
            
            source      = event.getSource();
            ctlButton   = m_editorLimitOutput.getChangeControl(getFieldEditorParent());
            
            if (source == ctlButton)
            {
                execute(ON_LIMITOUTPUT_SELECTED,null);
            }
        }
    
    }
}
