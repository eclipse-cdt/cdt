/*******************************************************************************
 * Copyright (c) 2003, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

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

public class TerminalPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
    public static final String  PREF_LIMITOUTPUT               = "TerminalPrefLimitOutput"; //$NON-NLS-1$
    public static final String  PREF_BUFFERLINES                = "TerminalPrefBufferLines"; //$NON-NLS-1$
    public static final String  PREF_TIMEOUT_SERIAL            = "TerminalPrefTimeoutSerial"; //$NON-NLS-1$
    public static final String  PREF_TIMEOUT_NETWORK           = "TerminalPrefTimeoutNetwork"; //$NON-NLS-1$
    public static final String  PREF_INVERT_COLORS          = "TerminalPrefInvertColors"; //$NON-NLS-1$
    public static final boolean DEFAULT_LIMITOUTPUT            = true;
    public static final int     DEFAULT_BUFFERLINES            = 1000;
    public static final int     DEFAULT_TIMEOUT_SERIAL         = 5;
    public static final int     DEFAULT_TIMEOUT_NETWORK        = 5;
    public static final boolean DEFAULT_INVERT_COLORS            = false;


    protected TerminalBooleanFieldEditor fEditorLimitOutput;

    protected BooleanFieldEditor fInvertColors;

	protected IntegerFieldEditor fEditorBufferSize;

	protected IntegerFieldEditor fEditorSerialTimeout;

	protected IntegerFieldEditor fEditorNetworkTimeout;
	public TerminalPreferencePage() {
		super(GRID);
	}
	public void onLimitOutputSelected() {
		Button ctlButton;
		Text ctlText;
		Label ctlLabel;
		boolean bEnabled;


		ctlButton = fEditorLimitOutput.getChangeControl(getFieldEditorParent());
		ctlText = fEditorBufferSize.getTextControl(getFieldEditorParent());
		ctlLabel = fEditorBufferSize.getLabelControl(getFieldEditorParent());
		bEnabled = ctlButton.getSelection();

		ctlText.setEnabled(bEnabled);
		ctlLabel.setEnabled(bEnabled);
	}
	protected void createFieldEditors() {
		setupPage();
	}
	protected void initialize() {
		super.initialize();

		onLimitOutputSelected();
	}
	protected void performDefaults() {
		super.performDefaults();

		onLimitOutputSelected();
	}
	public void init(IWorkbench workbench) {
		// do nothing
	}
	protected void setupPage() {
		setupData();
		setupEditors();
		setupListeners();
	}
	protected void setupData() {
		TerminalViewPlugin plugin;
		IPreferenceStore preferenceStore;

		plugin = TerminalViewPlugin.getDefault();
		preferenceStore = plugin.getPreferenceStore();
		setPreferenceStore(preferenceStore);
	}
	protected void setupEditors() {
		fInvertColors = new BooleanFieldEditor(
				PREF_INVERT_COLORS, ViewMessages.INVERT_COLORS,
				getFieldEditorParent());
		fEditorLimitOutput = new TerminalBooleanFieldEditor(
				PREF_LIMITOUTPUT, ViewMessages.LIMITOUTPUT,
				getFieldEditorParent());
		fEditorBufferSize = new IntegerFieldEditor(PREF_BUFFERLINES,
				ViewMessages.BUFFERLINES, getFieldEditorParent());
		fEditorSerialTimeout = new IntegerFieldEditor(
				PREF_TIMEOUT_SERIAL, ViewMessages.SERIALTIMEOUT,
				getFieldEditorParent());
		fEditorNetworkTimeout = new IntegerFieldEditor(
				PREF_TIMEOUT_NETWORK, ViewMessages.NETWORKTIMEOUT,
				getFieldEditorParent());

		fEditorBufferSize.setValidRange(0, Integer.MAX_VALUE);
		fEditorSerialTimeout.setValidRange(0, Integer.MAX_VALUE);
		fEditorNetworkTimeout.setValidRange(0, Integer.MAX_VALUE);

		addField(fInvertColors);
		addField(fEditorLimitOutput);
		addField(fEditorBufferSize);
		addField(fEditorSerialTimeout);
		addField(fEditorNetworkTimeout);
	}
	protected void setupListeners() {
		TerminalSelectionHandler selectionHandler;
		Button ctlButton;

		selectionHandler = new TerminalSelectionHandler();
		ctlButton = fEditorLimitOutput.getChangeControl(getFieldEditorParent());
		ctlButton.addSelectionListener(selectionHandler);
	}
	public class TerminalBooleanFieldEditor extends BooleanFieldEditor {
		public TerminalBooleanFieldEditor(String strName, String strLabel,
				Composite ctlParent) {
			super(strName, strLabel, ctlParent);
		}
		public Button getChangeControl(Composite parent) {
			return super.getChangeControl(parent);
		}
	}
	protected class TerminalSelectionHandler extends SelectionAdapter {
		protected TerminalSelectionHandler() {
			super();
		}
		public void widgetSelected(SelectionEvent event) {
			Object source;
			Button ctlButton;

			source = event.getSource();
			ctlButton = fEditorLimitOutput
					.getChangeControl(getFieldEditorParent());

			if (source == ctlButton) {
				onLimitOutputSelected();
			}
		}

	}
}
