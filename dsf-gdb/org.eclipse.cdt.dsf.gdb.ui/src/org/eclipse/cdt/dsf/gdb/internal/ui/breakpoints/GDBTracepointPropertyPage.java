/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
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
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
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
 * The preference page used to present the properties of a GDB tracepoint as preferences. 
 * A TracepointPreferenceStore is used to interface between this page and the tracepoint.
 */
public class GDBTracepointPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {

	class TracepointIntegerFieldEditor extends IntegerFieldEditor {

		public TracepointIntegerFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
			setErrorMessage(Messages.TracepointPropertyPage_integer_negative);
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

	class TracepointStringFieldEditor extends StringFieldEditor {

		public TracepointStringFieldEditor(String name, String labelText, Composite parent) {
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

	private TracepointStringFieldEditor fCondition;

	private Text fIgnoreCountTextControl;
	private TracepointIntegerFieldEditor fIgnoreCount;
	
	private Text fPassCountTextControl;
	private TracepointIntegerFieldEditor fPassCount;

	private IAdaptable fElement;

	/**
	 * The "fake" preference store used to interface between
	 * the tracepoint and the tracepoint preference page.
	 */
	private TracepointPreferenceStore fTracepointPreferenceStore;

	/**
	 * Constructor for GDBTracepointPropertyPage.
	 * 
	 */
	public GDBTracepointPropertyPage() {
		super( GRID );
		noDefaultAndApplyButton();
		fTracepointPreferenceStore = new TracepointPreferenceStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		ICTracepoint tracepoint = getTracepoint();
		createMainLabel(tracepoint);
		createTypeSpecificLabelFieldEditors(tracepoint);
		createEnabledField(getFieldEditorParent());
		IPreferenceStore store = getPreferenceStore();
		try {
			String condition = tracepoint.getCondition();
			if ( condition == null ) {
				condition = ""; //$NON-NLS-1$
			}
			store.setValue(TracepointPreferenceStore.CONDITION, condition);
			createConditionEditor(getFieldEditorParent());
			store.setValue(TracepointPreferenceStore.ENABLED, tracepoint.isEnabled());
			// GDB does not support ignore count right now
//			int ignoreCount = tracepoint.getIgnoreCount();
//			store.setValue(TracepointPreferenceStore.IGNORE_COUNT, (ignoreCount >= 0) ? ignoreCount : 0);
//			createIgnoreCountEditor(getFieldEditorParent());
			int passCount = tracepoint.getPassCount();
			store.setValue(TracepointPreferenceStore.PASS_COUNT, (passCount >= 0) ? passCount : 0);
			createPassCountEditor(getFieldEditorParent());
		}
		catch( CoreException ce ) {
			GdbUIPlugin.log(ce);
		}
	}

	private void createMainLabel(ICTracepoint tracepoint) {
		addField(createLabelEditor(getFieldEditorParent(), 
				                   Messages.TracepointPropertyPage_Class,
				                   getTracepointMainLabel(tracepoint)));
	}

	/**
	 * Method createTypeSpecificLabelFieldEditors.
	 * 
	 * @param tracepoint
	 */
	private void createTypeSpecificLabelFieldEditors(ICTracepoint tracepoint) {

		if (tracepoint instanceof ICFunctionBreakpoint) {
			ICFunctionBreakpoint ftrpt = (ICFunctionBreakpoint)tracepoint;
			String function = Messages.TracepointPropertyPage_NotAvailable;
			try {
				function = ftrpt.getFunction();
			}
			catch(CoreException e) {
				GdbUIPlugin.log(e);
			}
			if (function != null) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.TracepointPropertyPage_FunctionName, function));
			}
		}
		else if (tracepoint instanceof ICAddressBreakpoint) {
			ICAddressBreakpoint atrpt = (ICAddressBreakpoint)tracepoint;
			String address = Messages.TracepointPropertyPage_NotAvailable;
			try {
				address = atrpt.getAddress();
			}
			catch(CoreException e) {
				GdbUIPlugin.log(e);
			}
			if (address != null) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.TracepointPropertyPage_Address, address));
			}
		}
		else { // LineTracepoint
			String fileName = null;
			try {
				fileName = tracepoint.getSourceHandle();
			}
			catch(CoreException e) {
				GdbUIPlugin.log(e);
			}
			if (fileName != null) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.TracepointPropertyPage_File, fileName));
			}
			ILineBreakpoint ltrpt = tracepoint;

			int lNumber = 0;
			try {
				lNumber = ltrpt.getLineNumber();
			} catch (CoreException e) {
				GdbUIPlugin.log(e);
			}

			if (lNumber > 0) {
				getPreferenceStore().setValue(TracepointPreferenceStore.LINE, lNumber);
				createLineNumberEditor(getFieldEditorParent());
			}
		}
	}

	private String getTracepointMainLabel(ICTracepoint tracepoint) {
		if (tracepoint instanceof ICFunctionBreakpoint)
			return Messages.TracepointPropertyPage_FunctionTracepoint;
		if (tracepoint instanceof ICAddressBreakpoint)
			return Messages.TracepointPropertyPage_AddressTracepoint;

		return Messages.TracepointPropertyPage_LineTracepoint;
	}
	
	protected void createLineNumberEditor(Composite parent) {
		 String title = Messages.TracepointPropertyPage_LineNumber;
		 TracepointIntegerFieldEditor labelFieldEditor = new TracepointIntegerFieldEditor(TracepointPreferenceStore.LINE ,title, parent);
		 labelFieldEditor.setValidRange(1, Integer.MAX_VALUE);
		 addField(labelFieldEditor);
	}

	protected void createEnabledField(Composite parent) {
		fEnabled = new BooleanFieldEditor(TracepointPreferenceStore.ENABLED, Messages.TracepointPropertyPage_Enabled, parent);
		addField(fEnabled);
	}

	protected void createConditionEditor( Composite parent ) {
		fCondition = new TracepointStringFieldEditor(TracepointPreferenceStore.CONDITION, Messages.TracepointPropertyPage_Condition, parent);
		fCondition.setEmptyStringAllowed(true);
		fCondition.setErrorMessage(Messages.TracepointPropertyPage_InvalidCondition);
		addField(fCondition);
	}

	protected void createIgnoreCountEditor(Composite parent) {
		fIgnoreCount = new TracepointIntegerFieldEditor(TracepointPreferenceStore.IGNORE_COUNT, Messages.TracepointPropertyPage_IgnoreCount, parent);
		fIgnoreCount.setValidRange(0, Integer.MAX_VALUE);
		fIgnoreCountTextControl = fIgnoreCount.getTextControl(parent);
		try {
			fIgnoreCountTextControl.setEnabled(getTracepoint().getIgnoreCount() >= 0);
		}
		catch(CoreException ce) {
			GdbUIPlugin.log(ce);
		}
		addField(fIgnoreCount);
	}

	protected void createPassCountEditor(Composite parent) {
		fPassCount = new TracepointIntegerFieldEditor(TracepointPreferenceStore.PASS_COUNT, Messages.TracepointPropertyPage_PassCount, parent);
		fPassCount.setValidRange(0, Integer.MAX_VALUE);
		fPassCountTextControl = fPassCount.getTextControl(parent);
		try {
			fPassCountTextControl.setEnabled(getTracepoint().getPassCount() >= 0);
		}
		catch(CoreException ce) {
			GdbUIPlugin.log(ce);
		}
		addField(fPassCount);
	}

	protected FieldEditor createLabelEditor(Composite parent, String title, String value) {
		return new LabelFieldEditor(parent, title, value);
	}

	protected ICTracepoint getTracepoint() {
		IAdaptable element = getElement();
		return (element instanceof ICTracepoint) ? (ICTracepoint)element : (ICTracepoint)element.getAdapter(ICTracepoint.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
	 */
    @Override
	public IAdaptable getElement() {
		return fElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
    @Override
	public void setElement(IAdaptable element) {
		fElement = element;
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		return fTracepointPreferenceStore;
	}

	@Override
	public boolean performOk() {
		final List<String> changedProperties = new ArrayList<String>(5);
		getPreferenceStore().addPropertyChangeListener( new IPropertyChangeListener() {

			/**
			 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
			 */
            @Override
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

            @Override
			public void run( IProgressMonitor monitor ) throws CoreException {
				ICTracepoint tracepoint = getTracepoint();
				Iterator<String> changed = changedProperties.iterator();
				while(changed.hasNext()) {
					String property = changed.next();
					if (property.equals(TracepointPreferenceStore.ENABLED)) {
						tracepoint.setEnabled(getPreferenceStore().getBoolean(TracepointPreferenceStore.ENABLED));
					}
					else if (property.equals(TracepointPreferenceStore.IGNORE_COUNT)) {
						tracepoint.setIgnoreCount(getPreferenceStore().getInt(TracepointPreferenceStore.IGNORE_COUNT));
					}
					else if (property.equals(TracepointPreferenceStore.PASS_COUNT)) {
						tracepoint.setPassCount(getPreferenceStore().getInt(TracepointPreferenceStore.PASS_COUNT));
					}
					else if (property.equals(TracepointPreferenceStore.CONDITION)) {
						tracepoint.setCondition(getPreferenceStore().getString(TracepointPreferenceStore.CONDITION));
					}
					else if (property.equals(TracepointPreferenceStore.LINE)) {
						// already workspace runnable, setting markers are safe
						tracepoint.getMarker().setAttribute(IMarker.LINE_NUMBER, getPreferenceStore().getInt(TracepointPreferenceStore.LINE));
					} else {
					    // this allow set attributes contributed by other plugins
						String value = getPropertyAsString(property);
						tracepoint.getMarker().setAttribute(property, value);
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
