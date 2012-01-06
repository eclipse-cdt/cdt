/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.Separator;

public class ScalabilityPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	// Files with this number of lines will trigger scalability mode
	private IntegerFieldEditor fLinesToTrigger;
	
	private Button fEnableAll;
	
	private Button fReconciler;
	
	private Button fSyntaxColor;
	
	private Button fSemanticHighlighting;
	
	private Button fContentAssist;
	
	private Button fContentAssistAutoActivation;
	
	private final Map<Object, String> fCheckBoxes= new HashMap<Object, String>();
	
	/**
	 * List of master/slave listeners when there's a dependency.
	 * 
	 * @see #createDependency(Button, String, Control)
	 */
	private final ArrayList<Object> fMasterSlaveListeners= new ArrayList<Object>();
	
	public ScalabilityPreferencePage() {
		setPreferenceStore(PreferenceConstants.getPreferenceStore());
		setDescription(PreferencesMessages.ScalabilityPreferencePage_description);
	}
	
	/**
	 * Creates a button with the given label and sets the default configuration data.
	 */
	private Button createCheckButton( Composite parent, String label, String key ) {
		Button button = new Button( parent, SWT.CHECK | SWT.LEFT );
		button.setText( label );
		// FieldEditor GridData
		GridData data = new GridData();
		button.setLayoutData( data );
		fCheckBoxes.put(button, key);
		return button;
	}
	
	private void initFields() {
		IPreferenceStore prefs=getPreferenceStore();

		Iterator<Object> iter= fCheckBoxes.keySet().iterator();
		while (iter.hasNext()) {
			Button b= (Button) iter.next();
			String key= fCheckBoxes.get(b);
			b.setSelection(prefs.getBoolean(key));
		}
		
        // Update slaves
        iter= fMasterSlaveListeners.iterator();
        while (iter.hasNext()) {
            SelectionListener listener= (SelectionListener)iter.next();
            listener.widgetSelected(null);
        }
        fLinesToTrigger.setStringValue(Integer.toString(prefs.getInt(PreferenceConstants.SCALABILITY_NUMBER_OF_LINES)));
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.SCALABILITY_PREFERENCE_PAGE);
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		int nColumns= 1;
				
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= 0;
		layout.numColumns= nColumns;
		composite.setLayout(layout);
		
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData( data );
		
		createDetectionSettings(composite);

		new Separator().doFillIntoGrid(composite, nColumns);
		
		createScalabilityModeSettings(composite);

		new Separator().doFillIntoGrid(composite, nColumns);
		
		String noteTitle= PreferencesMessages.ScalabilityPreferencePage_note;
		String noteMessage= PreferencesMessages.ScalabilityPreferencePage_preferenceOnlyForNewEditors;
		Composite noteControl= createNoteComposite(JFaceResources.getDialogFont(), composite, noteTitle, noteMessage);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		noteControl.setLayoutData(gd);
		
		initFields();
		
		Dialog.applyDialogFont(composite);
		return composite;
	}
	
	/**
	 * Creates composite group and sets the default layout data.
	 * 
	 * @param parent
	 *            the parent of the new composite
	 * @param numColumns
	 *            the number of columns for the new composite
	 * @param labelText
	 *            the text label of the new composite
	 * @return the newly-created composite
	 */
	private Composite createGroupComposite( Composite parent, int numColumns, String labelText ) {
		return ControlFactory.createGroup( parent, labelText, numColumns );
	}
	
	/**
	 * Create the view setting preferences composite widget
	 */
	private void createDetectionSettings( Composite parent ) {
		Composite group = createGroupComposite( parent, 1, PreferencesMessages.ScalabilityPreferencePage_detection_group_label );
		createCheckButton(group, PreferencesMessages.ScalabilityPreferencePage_detection_label,PreferenceConstants.SCALABILITY_ALERT);

		Composite comp= new Composite(group, SWT.NONE);
		fLinesToTrigger = new IntegerFieldEditor( PreferenceConstants.SCALABILITY_NUMBER_OF_LINES, PreferencesMessages.ScalabilityPreferencePage_trigger_lines_label, comp);
		GridData data = (GridData)fLinesToTrigger.getTextControl( comp ).getLayoutData();
		data.horizontalAlignment = GridData.BEGINNING;
		data.widthHint = convertWidthInCharsToPixels( 11 );
		fLinesToTrigger.setPage( this );
		fLinesToTrigger.setValidateStrategy( StringFieldEditor.VALIDATE_ON_KEY_STROKE );
		fLinesToTrigger.setValidRange( 1, Integer.MAX_VALUE );
		String minValue = Integer.toString( 1 );
		String maxValue = Integer.toString( Integer.MAX_VALUE );
		fLinesToTrigger.setErrorMessage( MessageFormat.format(PreferencesMessages.ScalabilityPreferencePage_error, new Object[] {minValue, maxValue}) );
		fLinesToTrigger.load();
		fLinesToTrigger.setPropertyChangeListener( new IPropertyChangeListener() {

			@Override
			public void propertyChange( PropertyChangeEvent event ) {
				if ( event.getProperty().equals( FieldEditor.IS_VALID ) )
					setValid( fLinesToTrigger.isValid() );
			}
		} );
		
	}
	
	/**
	 * Create the view setting preferences composite widget
	 */
	private void createScalabilityModeSettings( Composite parent ) {
		Composite group = createGroupComposite( parent, 1, PreferencesMessages.ScalabilityPreferencePage_scalabilityMode_group_label );
		
		fEnableAll = createCheckButton(group, PreferencesMessages.ScalabilityPreferencePage_scalabilityMode_label, PreferenceConstants.SCALABILITY_ENABLE_ALL);
		fReconciler = createCheckButton(group, PreferencesMessages.ScalabilityPreferencePage_reconciler_label, PreferenceConstants.SCALABILITY_RECONCILER);
		createDependency(fEnableAll, PreferenceConstants.SCALABILITY_ENABLE_ALL, fReconciler, true);
		
		fSemanticHighlighting = createCheckButton(group, PreferencesMessages.ScalabilityPreferencePage_semanticHighlighting_label, PreferenceConstants.SCALABILITY_SEMANTIC_HIGHLIGHT);
		createDependency(fEnableAll, PreferenceConstants.SCALABILITY_ENABLE_ALL, fSemanticHighlighting, true);
		
		fSyntaxColor = createCheckButton(group, PreferencesMessages.ScalabilityPreferencePage_syntaxColor_label, PreferenceConstants.SCALABILITY_SYNTAX_COLOR);
		createDependency(fEnableAll, PreferenceConstants.SCALABILITY_ENABLE_ALL, fSyntaxColor, true);
		
		fContentAssist = createCheckButton(group, PreferencesMessages.ScalabilityPreferencePage_contentAssist_label, PreferenceConstants.SCALABILITY_PARSER_BASED_CONTENT_ASSIST);
		createDependency(fEnableAll, PreferenceConstants.SCALABILITY_ENABLE_ALL, fContentAssist, true);
		
		fContentAssistAutoActivation = createCheckButton(group, PreferencesMessages.ScalabilityPreferencePage_contentAssist_autoActivation, PreferenceConstants.SCALABILITY_CONTENT_ASSIST_AUTO_ACTIVATION);
		createDependency(fContentAssist, PreferenceConstants.SCALABILITY_PARSER_BASED_CONTENT_ASSIST, fContentAssistAutoActivation, true);
		createDependency(fEnableAll, PreferenceConstants.SCALABILITY_ENABLE_ALL, fContentAssistAutoActivation, false);
	}
	
	private static void indent(Control control, GridData masterLayoutData) {
		GridData gridData= new GridData();
		gridData.horizontalIndent= masterLayoutData.horizontalIndent + 20;
		control.setLayoutData(gridData);
	}
	
	private void createDependency(final Button master, String masterKey, final Control slave, boolean indent) {
		if (indent) {
			indent(slave, (GridData)master.getLayoutData());
		}
		boolean masterState= getPreferenceStore().getBoolean(masterKey);
		slave.setEnabled(!masterState);
		
		if (masterState) {
			((Button)slave).setSelection(masterState);
		}
		
		SelectionListener listener= new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				slave.setEnabled(!master.getSelection());
				if (master.getSelection()) {
					((Button)slave).setSelection(master.getSelection());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		};
		master.addSelectionListener(listener);
		fMasterSlaveListeners.add(listener);
	}
	
	/*
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}
	
	/*
	 * @see IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		IPreferenceStore prefs= getPreferenceStore();
		Iterator<Object> iter= fCheckBoxes.keySet().iterator();
		while (iter.hasNext()) {
			Button b= (Button) iter.next();
			String key= fCheckBoxes.get(b);
			prefs.setValue(key, b.getSelection());
		}
		prefs.setValue(PreferenceConstants.SCALABILITY_NUMBER_OF_LINES, fLinesToTrigger.getIntValue());
		return super.performOk();
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		IPreferenceStore prefs= getPreferenceStore();

		Iterator<Object> iter= fCheckBoxes.keySet().iterator();
		while (iter.hasNext()) {
			Button b= (Button) iter.next();
			String key= fCheckBoxes.get(b);
			b.setSelection(prefs.getDefaultBoolean(key));
		}
		
        // Update slaves
        iter= fMasterSlaveListeners.iterator();
        while (iter.hasNext()) {
            SelectionListener listener= (SelectionListener)iter.next();
            listener.widgetSelected(null);
        }
        fLinesToTrigger.setStringValue(Integer.toString(prefs.getDefaultInt(PreferenceConstants.SCALABILITY_NUMBER_OF_LINES)));
	}
}
