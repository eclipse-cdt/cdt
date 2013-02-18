/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints; 

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDynamicPrintf;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.ui.preferences.ReadOnlyFieldEditor;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;

/**
 * The preference page used to present the properties of a GDB DynamicPrintf as preferences. 
 * A DynamicPrintfPreferenceStore is used to interface between this page and the DynamicPrintf.
 */
public class GDBDynamicPrintfPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {

	class DynamicPrintfIntegerFieldEditor extends IntegerFieldEditor {

		public DynamicPrintfIntegerFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
			setErrorMessage(Messages.PropertyPage_integer_negative);
		}

		/**
		 * @see IntegerFieldEditor#checkState()
		 */
		@Override
		protected boolean checkState() {
			Text control = getTextControl();
			if (!control.isEnabled()) {
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}

		/**
		 * Only store if the text control is enabled
		 * 
		 * @see FieldEditor#doStore()
		 */
		@Override
		protected void doStore() {
			Text text = getTextControl();
			if (text.isEnabled()) {
				super.doStore();
			}
		}

		/**
		 * Clears the error message from the message line if the error message is the error message from this field editor.
		 */
		@Override
		protected void clearErrorMessage() {
			if (getPage() != null) {
				String message = getPage().getErrorMessage();
				if ( message != null ) {
					if (getErrorMessage().equals(message)) {
						super.clearErrorMessage();
					}
				}
				else {
					super.clearErrorMessage();
				}
			}
		}
	}

	class DynamicPrintfStringFieldEditor extends StringFieldEditor {

		public DynamicPrintfStringFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
		}

		/**
		 * @see StringFieldEditor#checkState()
		 */
		@Override
		protected boolean checkState() {
			Text control = getTextControl();
			if (!control.isEnabled()) {
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}

		@Override
		protected void doStore() {
			Text text = getTextControl();
			if (text.isEnabled()) {
				super.doStore();
			}
		}

		/**
		 * Clears the error message from the message line if the error message is the error message from this field editor.
		 */
		@Override
		protected void clearErrorMessage() {
			if (getPage() != null) {
				String message = getPage().getErrorMessage();
				if ( message != null ) {
					if (getErrorMessage().equals(message)) {
						super.clearErrorMessage();
					}
				}
				else {
					super.clearErrorMessage();
				}
			}
		}
	}

	class LabelFieldEditor extends ReadOnlyFieldEditor {
		private String fValue;

		public LabelFieldEditor(Composite parent, String title, String value) {
			super(title, title, parent);
			fValue = value;
		}

		@Override
		protected void doLoad() {
			if (textField != null) {
				textField.setText(fValue);
			}
		}
		@Override
		protected void doLoadDefault() {
			// nothing
		}

	}

	private BooleanFieldEditor fEnabled;

	private DynamicPrintfStringFieldEditor fCondition;

	private Text fIgnoreCountTextControl;
	private DynamicPrintfIntegerFieldEditor fIgnoreCount;
	
	private DynamicPrintfStringFieldEditor fMessage;

	private IAdaptable fElement;

	/**
	 * The "fake" preference store used to interface between
	 * the runtimePrint and the runtimePrint preference page.
	 */
	private DynamicPrintfPreferenceStore fDynamicPrintfPreferenceStore;

	public GDBDynamicPrintfPropertyPage() {
		super( GRID );
		noDefaultAndApplyButton();
		fDynamicPrintfPreferenceStore = new DynamicPrintfPreferenceStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		ICDynamicPrintf runtimePrint = getDynamicPrintf();
		createMainLabel(runtimePrint);
		createTypeSpecificLabelFieldEditors(runtimePrint);
		createEnabledField(getFieldEditorParent());
		IPreferenceStore store = getPreferenceStore();
		try {
			String condition = runtimePrint.getCondition();
			if ( condition == null ) {
				condition = ""; //$NON-NLS-1$
			}
			store.setValue(DynamicPrintfPreferenceStore.CONDITION, condition);
			createConditionEditor(getFieldEditorParent());
			store.setValue(DynamicPrintfPreferenceStore.ENABLED, runtimePrint.isEnabled());
			int ignoreCount = runtimePrint.getIgnoreCount();
			store.setValue(DynamicPrintfPreferenceStore.IGNORE_COUNT, (ignoreCount >= 0) ? ignoreCount : 0);
			createIgnoreCountEditor(getFieldEditorParent());
			String message = runtimePrint.getPrintfString();
			store.setValue(DynamicPrintfPreferenceStore.MESSAGE, message == null ? "" : message); //$NON-NLS-1$
			createMessageEditor(getFieldEditorParent());
		}
		catch( CoreException ce ) {
			GdbUIPlugin.log(ce);
		}
	}

	private void createMainLabel(ICDynamicPrintf runtimePrint) {
		addField(createLabelEditor(getFieldEditorParent(), 
				                   Messages.PropertyPage_Class,
				                   getDynamicPrintfMainLabel(runtimePrint)));
	}

	/**
	 * Method createTypeSpecificLabelFieldEditors.
	 * 
	 * @param runtimePrint
	 */
	private void createTypeSpecificLabelFieldEditors(ICDynamicPrintf runtimePrint) {

		if (runtimePrint instanceof ICFunctionBreakpoint) {
			ICFunctionBreakpoint ftrpt = (ICFunctionBreakpoint)runtimePrint;
			String function = Messages.PropertyPage_NotAvailable;
			try {
				function = ftrpt.getFunction();
			}
			catch(CoreException e) {
				GdbUIPlugin.log(e);
			}
			if (function != null) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.PropertyPage_FunctionName, function));
			}
		}
		else if (runtimePrint instanceof ICAddressBreakpoint) {
			ICAddressBreakpoint atrpt = (ICAddressBreakpoint)runtimePrint;
			String address = Messages.PropertyPage_NotAvailable;
			try {
				address = atrpt.getAddress();
			}
			catch(CoreException e) {
				GdbUIPlugin.log(e);
			}
			if (address != null) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.PropertyPage_Address, address));
			}
		}
		else { // LineDynamicPrintf
			String fileName = null;
			try {
				fileName = runtimePrint.getSourceHandle();
			}
			catch(CoreException e) {
				GdbUIPlugin.log(e);
			}
			if (fileName != null) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.PropertyPage_File, fileName));
			}
			ILineBreakpoint ltrpt = runtimePrint;

			int lNumber = 0;
			try {
				lNumber = ltrpt.getLineNumber();
			} catch (CoreException e) {
				GdbUIPlugin.log(e);
			}

			if (lNumber > 0) {
				getPreferenceStore().setValue(DynamicPrintfPreferenceStore.LINE, lNumber);
				createLineNumberEditor(getFieldEditorParent());
			}
		}
	}

	private String getDynamicPrintfMainLabel(ICDynamicPrintf runtimePrint) {
		if (runtimePrint instanceof ICFunctionBreakpoint)
			return Messages.DynamicPrintfPropertyPage_FunctionDynamicPrintf;
		if (runtimePrint instanceof ICAddressBreakpoint)
			return Messages.DynamicPrintfPropertyPage_AddressDynamicPrintf;

		return Messages.DynamicPrintfPropertyPage_LineDynamicPrintf;
	}
	
	protected void createLineNumberEditor(Composite parent) {
		 String title = Messages.PropertyPage_LineNumber;
		 DynamicPrintfIntegerFieldEditor labelFieldEditor = new DynamicPrintfIntegerFieldEditor(DynamicPrintfPreferenceStore.LINE ,title, parent);
		 labelFieldEditor.setValidRange(1, Integer.MAX_VALUE);
		 addField(labelFieldEditor);
	}

	protected void createEnabledField(Composite parent) {
		fEnabled = new BooleanFieldEditor(DynamicPrintfPreferenceStore.ENABLED, Messages.PropertyPage_Enabled, parent);
		addField(fEnabled);
	}

	protected void createConditionEditor( Composite parent ) {
		fCondition = new DynamicPrintfStringFieldEditor(DynamicPrintfPreferenceStore.CONDITION, Messages.PropertyPage_Condition, parent);
		fCondition.setEmptyStringAllowed(true);
		fCondition.setErrorMessage(Messages.PropertyPage_InvalidCondition);
		addField(fCondition);
	}

	protected void createIgnoreCountEditor(Composite parent) {
		fIgnoreCount = new DynamicPrintfIntegerFieldEditor(DynamicPrintfPreferenceStore.IGNORE_COUNT, Messages.PropertyPage_IgnoreCount, parent);
		fIgnoreCount.setValidRange(0, Integer.MAX_VALUE);
		fIgnoreCountTextControl = fIgnoreCount.getTextControl(parent);
		try {
			fIgnoreCountTextControl.setEnabled(getDynamicPrintf().getIgnoreCount() >= 0);
		}
		catch(CoreException ce) {
			GdbUIPlugin.log(ce);
		}
		addField(fIgnoreCount);
	}

	protected void createMessageEditor(Composite parent) {
		fMessage = new DynamicPrintfStringFieldEditor(DynamicPrintfPreferenceStore.MESSAGE, Messages.DynamicPrintfPropertyPage_Message, parent);
		fMessage.setEmptyStringAllowed(false);
		fMessage.setErrorMessage(Messages.DynamicPrintfPropertyPage_InvalidMessage);
		addField(fMessage);
	}

	protected FieldEditor createLabelEditor(Composite parent, String title, String value) {
		return new LabelFieldEditor(parent, title, value);
	}

	protected ICDynamicPrintf getDynamicPrintf() {
		IAdaptable element = getElement();
		return (element instanceof ICDynamicPrintf) ? (ICDynamicPrintf)element : (ICDynamicPrintf)element.getAdapter(ICDynamicPrintf.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
	 */
	public IAdaptable getElement() {
		return fElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
	public void setElement(IAdaptable element) {
		fElement = element;
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		return fDynamicPrintfPreferenceStore;
	}

	@Override
	public boolean performOk() {
		final List<String> changedProperties = new ArrayList<String>(5);
		getPreferenceStore().addPropertyChangeListener( new IPropertyChangeListener() {

			/**
			 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				changedProperties.add(event.getProperty());
			}
		} );
		boolean result = super.performOk();
		setBreakpointProperties(changedProperties);
		return result;
	}

	protected void setBreakpointProperties(final List<String> changedProperties) {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {

			public void run( IProgressMonitor monitor ) throws CoreException {
				ICDynamicPrintf runtimePrint = getDynamicPrintf();
				Iterator<String> changed = changedProperties.iterator();
				while(changed.hasNext()) {
					String property = changed.next();
					if (property.equals(DynamicPrintfPreferenceStore.ENABLED)) {
						runtimePrint.setEnabled(getPreferenceStore().getBoolean(DynamicPrintfPreferenceStore.ENABLED));
					}
					else if (property.equals(DynamicPrintfPreferenceStore.IGNORE_COUNT)) {
						runtimePrint.setIgnoreCount(getPreferenceStore().getInt(DynamicPrintfPreferenceStore.IGNORE_COUNT));
					}
					else if (property.equals(DynamicPrintfPreferenceStore.MESSAGE)) {
						runtimePrint.setPrintfString(getPreferenceStore().getString(DynamicPrintfPreferenceStore.MESSAGE));
					}
					else if (property.equals(DynamicPrintfPreferenceStore.CONDITION)) {
						runtimePrint.setCondition(getPreferenceStore().getString(DynamicPrintfPreferenceStore.CONDITION));
					}
					else if (property.equals(DynamicPrintfPreferenceStore.LINE)) {
						// already workspace runnable, setting markers are safe
						runtimePrint.getMarker().setAttribute(IMarker.LINE_NUMBER, getPreferenceStore().getInt(DynamicPrintfPreferenceStore.LINE));
					} else {
					    // this allow set attributes contributed by other plugins
						String value = getPropertyAsString(property);
						runtimePrint.getMarker().setAttribute(property, value);
					}
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		}
		catch(CoreException ce) {
			GdbUIPlugin.log(ce);
		}
	}

	/**
	 * Return string value of given property or null.
	 */
	protected String getPropertyAsString(String property) {
		// currently only supports String and Integer
		IPreferenceStore store = getPreferenceStore();
		
		if (store.contains(property)) {
			String value = store.getString(property);
			return value;
		} else return null;
	}
}
