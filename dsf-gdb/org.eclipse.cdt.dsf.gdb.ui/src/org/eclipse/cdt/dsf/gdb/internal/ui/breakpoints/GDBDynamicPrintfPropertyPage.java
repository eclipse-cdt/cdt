/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDynamicPrintf;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.internal.ui.breakpoints.BreakpointsMessages;
import org.eclipse.cdt.debug.internal.ui.breakpoints.CBreakpointContext;
import org.eclipse.cdt.debug.internal.ui.breakpoints.CBreakpointPreferenceStore;
import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointContext;
import org.eclipse.cdt.debug.ui.preferences.ReadOnlyFieldEditor;
import org.eclipse.cdt.dsf.gdb.breakpoints.GDBDynamicPrintfUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * The preference page used to present the properties of a GDB dynamic printf as preferences.
 */
public class GDBDynamicPrintfPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {

	private class DynamicPrintfIntegerFieldEditor extends IntegerFieldEditor {

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
				if (message != null) {
					if (getErrorMessage().equals(message)) {
						super.clearErrorMessage();
					}
				} else {
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

		@Override
		protected void doLoad() {
			String value = getPreferenceStore().getString(getPreferenceName());
			setStringValue(value);
		}

		/**
		 * Clears the error message from the message line if the error message is the error message from this field editor.
		 */
		@Override
		protected void clearErrorMessage() {
			if (getPage() != null) {
				String message = getPage().getErrorMessage();
				if (message != null) {
					if (getErrorMessage().equals(message)) {
						super.clearErrorMessage();
					}
				} else {
					super.clearErrorMessage();
				}
			}
		}
	}

	private class LabelFieldEditor extends ReadOnlyFieldEditor {
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
	private DynamicPrintfIntegerFieldEditor fLineEditor;
	private DynamicPrintfIntegerFieldEditor fIgnoreCount;

	/**
	 * Indicates if the page currently aims to create
	 * a breakpoint that already exits.
	 */
	private boolean fDuplicateBreakpoint;

	private DynamicPrintfStringFieldEditor fPrintString;

	private IAdaptable fElement;

	/**
	 * The preference store used to interface between the dynamic printf and the
	 * dynamic printf preference page.  This preference store is initialized only
	 * when the preference store cannot be retrieved from the preference
	 * dialog's element.
	 * @see #getPreferenceStore()
	 */
	private CBreakpointPreferenceStore fDynamicPrintfPreferenceStore;

	public GDBDynamicPrintfPropertyPage() {
		super(GRID);
		noDefaultAndApplyButton();
	}

	@Override
	protected void createFieldEditors() {
		ICDynamicPrintf dprintf = getDprintf();
		createMainLabel(dprintf);
		createTypeSpecificLabelFieldEditors(dprintf);
		createEnabledField(getFieldEditorParent());
		createConditionEditor(getFieldEditorParent());
		createIgnoreCountEditor(getFieldEditorParent());
		createPrintStringEditor(getFieldEditorParent());
	}

	private void createMainLabel(ICDynamicPrintf dprintf) {
		addField(createLabelEditor(getFieldEditorParent(), Messages.PropertyPage_Class,
				getDynamicPrintfMainLabel(dprintf)));
	}

	private void createTypeSpecificLabelFieldEditors(ICDynamicPrintf dprintf) {
		if (dprintf instanceof ICFunctionBreakpoint) {
			createFunctionEditor(getFieldEditorParent());
		} else if (dprintf instanceof ICAddressBreakpoint) {
			String address = getPreferenceStore().getString(ICLineBreakpoint.ADDRESS);
			if (address == null || address.trim().length() == 0) {
				address = Messages.PropertyPage_NotAvailable;
			}
			addField(createLabelEditor(getFieldEditorParent(), Messages.PropertyPage_Address, address));
		} else { // LineDprintf
			String fileName = getPreferenceStore().getString(ICBreakpoint.SOURCE_HANDLE);
			if (fileName != null) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.PropertyPage_File, fileName));
			}
			int lNumber = getPreferenceStore().getInt(IMarker.LINE_NUMBER);
			if (lNumber > 0) {
				createLineNumberEditor(getFieldEditorParent());
			}
		}
	}

	private String getDynamicPrintfMainLabel(ICDynamicPrintf dprintf) {
		IWorkbenchAdapter labelProvider = getElement().getAdapter(IWorkbenchAdapter.class);
		if (labelProvider != null) {
			return labelProvider.getLabel(getElement());
		}
		// default main label is the label of marker type for the dynamic printf
		return CDIDebugModel.calculateMarkerType(dprintf);
	}

	protected void createFunctionEditor(Composite parent) {
		ICDynamicPrintf dprintf = getDprintf();
		if (dprintf == null || dprintf.getMarker() == null) {
			DynamicPrintfStringFieldEditor expressionEditor = new DynamicPrintfStringFieldEditor(
					ICLineBreakpoint.FUNCTION, Messages.PropertyPage_FunctionName, parent);
			expressionEditor.setErrorMessage(Messages.PropertyPage_function_value_errorMessage);
			expressionEditor.setEmptyStringAllowed(false);
			addField(expressionEditor);
		} else {
			String function = getPreferenceStore().getString(ICLineBreakpoint.FUNCTION);
			if (function == null) {
				function = Messages.PropertyPage_NotAvailable;
			}
			addField(createLabelEditor(getFieldEditorParent(), Messages.PropertyPage_FunctionName, function));
		}
	}

	protected void createLineNumberEditor(Composite parent) {
		String title = Messages.PropertyPage_LineNumber;
		fLineEditor = new DynamicPrintfIntegerFieldEditor(IMarker.LINE_NUMBER, title, parent);
		fLineEditor.setValidRange(1, Integer.MAX_VALUE);
		fLineEditor.setErrorMessage(Messages.PropertyPage_lineNumber_errorMessage);
		addField(fLineEditor);
	}

	protected void createEnabledField(Composite parent) {
		fEnabled = new BooleanFieldEditor(ICBreakpoint.ENABLED, Messages.PropertyPage_Enabled, parent);
		addField(fEnabled);
	}

	protected void createConditionEditor(Composite parent) {
		fCondition = new DynamicPrintfStringFieldEditor(ICBreakpoint.CONDITION, Messages.PropertyPage_Condition,
				parent);
		fCondition.setEmptyStringAllowed(true);
		fCondition.setErrorMessage(Messages.PropertyPage_InvalidCondition);
		addField(fCondition);
	}

	protected void createIgnoreCountEditor(Composite parent) {
		fIgnoreCount = new DynamicPrintfIntegerFieldEditor(ICBreakpoint.IGNORE_COUNT, Messages.PropertyPage_IgnoreCount,
				parent);
		fIgnoreCount.setValidRange(0, Integer.MAX_VALUE);
		fIgnoreCountTextControl = fIgnoreCount.getTextControl(parent);
		fIgnoreCountTextControl.setEnabled(getPreferenceStore().getInt(ICBreakpoint.IGNORE_COUNT) >= 0);
		addField(fIgnoreCount);
	}

	protected void createPrintStringEditor(Composite parent) {
		fPrintString = new DynamicPrintfStringFieldEditor(ICDynamicPrintf.PRINTF_STRING,
				Messages.DynamicPrintfPropertyPage_PrintString, parent) {
			@Override
			protected boolean doCheckState() {
				GDBDynamicPrintfUtils.GDBDynamicPrintfString parsedStr = new GDBDynamicPrintfUtils.GDBDynamicPrintfString(
						getTextControl().getText());

				boolean valid = parsedStr.isValid();
				if (!valid) {
					setErrorMessage(parsedStr.getErrorMessage());
				}

				return valid;
			}
		};
		addField(fPrintString);
	}

	@Override
	public boolean isValid() {
		// Don't allow to create a duplicate breakpoint
		return super.isValid() && !fDuplicateBreakpoint;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);

		ICBreakpoint currentBp = getDprintf();
		if (!(currentBp instanceof ICFunctionBreakpoint) && !(currentBp instanceof ICAddressBreakpoint)) {
			// Check for duplication of line dprintf
			if (event.getProperty().equals(FieldEditor.VALUE)) {
				if (super.isValid()) {
					// For every change, if all the fields are valid
					// we then check if we are dealing with a duplicate
					// breakpoint.
					boolean oldValue = fDuplicateBreakpoint;
					fDuplicateBreakpoint = isDuplicateBreakpoint();
					if (oldValue != fDuplicateBreakpoint) {
						if (fDuplicateBreakpoint) {
							setErrorMessage(BreakpointsMessages
									.getString("CBreakpointPropertyPage.breakpoint_already_exists_errorMessage")); //$NON-NLS-1$
						} else {
							setErrorMessage(null);
						}
						// update container state
						if (getContainer() != null) {
							getContainer().updateButtons();
						}
						// update page state
						updateApplyButton();
					}
				}
			}
		}
	}

	private boolean isDuplicateBreakpoint() {
		String source = getPreferenceStore().getString(ICBreakpoint.SOURCE_HANDLE);
		int line = fLineEditor.getIntValue();

		// Look for any breakpoint (base bp class) that has the same source file and line number as what
		// is currently being inputed.  Careful not to compare with the current dprintf
		// in the case of modifying the properties of an existing dprintf; in
		// that case we of course have this particular dprintf at this file and line.
		ICBreakpoint currentBp = getDprintf();
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
		for (IBreakpoint bp : breakpoints) {
			if (!bp.equals(currentBp) && bp instanceof ICBreakpoint) {
				IMarker marker = bp.getMarker();
				if (marker != null) {
					String markerFile = marker.getAttribute(ICBreakpoint.SOURCE_HANDLE, ""); //$NON-NLS-1$
					int markerLine = marker.getAttribute(IMarker.LINE_NUMBER, -1);
					if (source.equals(markerFile) && line == markerLine) {
						// Woops, we already have another breakpoint at this file:line
						return true;
					}
				}
			}
		}
		return false;
	}

	protected FieldEditor createLabelEditor(Composite parent, String title, String value) {
		return new LabelFieldEditor(parent, title, value);
	}

	protected ICDynamicPrintf getDprintf() {
		IAdaptable element = getElement();
		if (element instanceof ICDynamicPrintf) {
			return (ICDynamicPrintf) element;
		}

		if (element instanceof ICBreakpointContext) {
			ICBreakpoint breakpoint = ((ICBreakpointContext) element).getBreakpoint();
			if (breakpoint instanceof ICDynamicPrintf) {
				return (ICDynamicPrintf) breakpoint;
			}
			assert false : "Should always have a dprintf"; //$NON-NLS-1$
		}

		return element.getAdapter(ICDynamicPrintf.class);
	}

	protected Object getDebugContext() {
		IDebugContextProvider provider = getElement().getAdapter(IDebugContextProvider.class);
		if (provider != null) {
			ISelection selection = provider.getActiveContext();
			if (selection instanceof IStructuredSelection) {
				return ((IStructuredSelection) selection).getFirstElement();
			}
			return null;
		}
		return DebugUITools.getDebugContext();
	}

	protected IResource getResource() {
		IAdaptable element = getElement();
		if (element instanceof ICDynamicPrintf) {
			IMarker marker = ((ICDynamicPrintf) element).getMarker();
			if (marker != null) {
				return marker.getResource();
			}
		} else if (element instanceof ICBreakpointContext) {
			return ((ICBreakpointContext) element).getResource();
		}
		return null;
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		IAdaptable element = getElement();
		if (element instanceof ICBreakpointContext) {
			return ((ICBreakpointContext) element).getPreferenceStore();
		}

		if (fDynamicPrintfPreferenceStore == null) {
			CBreakpointContext bpContext = element instanceof CBreakpointContext ? (CBreakpointContext) element : null;
			fDynamicPrintfPreferenceStore = new CBreakpointPreferenceStore(bpContext, null);
		}
		return fDynamicPrintfPreferenceStore;
	}

	@Override
	public boolean performCancel() {
		IPreferenceStore store = getPreferenceStore();
		if (store instanceof CBreakpointPreferenceStore) {
			((CBreakpointPreferenceStore) store).setCanceled(true);
		}
		return super.performCancel();
	}

	@Override
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		if (store instanceof CBreakpointPreferenceStore) {
			((CBreakpointPreferenceStore) store).setCanceled(false);
		}
		return super.performOk();
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
		if (element instanceof ICBreakpoint) {
			fElement = new CBreakpointContext((ICBreakpoint) element, null);
		} else {
			fElement = element;
		}
	}

	protected String[] getDebugModelIds() {
		String[] debugModelIds = null;
		Object debugContext = getDebugContext();
		IDebugModelProvider debugModelProvider = (IDebugModelProvider) DebugPlugin.getAdapter(debugContext,
				IDebugModelProvider.class);
		if (debugModelProvider != null) {
			debugModelIds = debugModelProvider.getModelIdentifiers();
		} else if (debugContext instanceof IDebugElement) {
			debugModelIds = new String[] { ((IDebugElement) debugContext).getModelIdentifier() };
		}
		return debugModelIds;
	}
}
