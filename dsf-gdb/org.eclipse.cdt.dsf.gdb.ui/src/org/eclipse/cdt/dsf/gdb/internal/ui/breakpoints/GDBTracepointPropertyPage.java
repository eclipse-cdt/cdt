/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ericsson - Initial API and implementation
 * Marc Khouzam (Ericsson) - Updated to allow updating properties
 *                           before creating the tracepoint (Bug 376116)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints; 

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.cdt.debug.internal.ui.breakpoints.CBreakpointContext;
import org.eclipse.cdt.debug.internal.ui.breakpoints.CBreakpointPreferenceStore;
import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointContext;
import org.eclipse.cdt.debug.ui.preferences.ReadOnlyFieldEditor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.model.IWorkbenchAdapter;

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
		
		@Override
		protected void doLoad()  {
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
	 * The preference store used to interface between the tracepoint and the 
	 * tracepoint preference page.  This preference store is initialized only
	 * when the preference store cannot be retrieved from the preference 
	 * dialog's element.
	 * @see #getPreferenceStore()
	 */
	private CBreakpointPreferenceStore fTracepointPreferenceStore;

	/**
	 * Constructor for GDBTracepointPropertyPage.
	 * 
	 */
	public GDBTracepointPropertyPage() {
		super(GRID);
		noDefaultAndApplyButton();
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
		createConditionEditor(getFieldEditorParent());
		// GDB does not support ignore count right now
		//createIgnoreCountEditor(getFieldEditorParent());
		createPassCountEditor(getFieldEditorParent());
	}

	private void createMainLabel(ICTracepoint tracepoint) {
		addField(createLabelEditor(getFieldEditorParent(), 
				                   Messages.TracepointPropertyPage_Class,
				                   getTracepointMainLabel(tracepoint)));
	}

	private void createTypeSpecificLabelFieldEditors(ICTracepoint tracepoint) {
		if (tracepoint instanceof ICFunctionBreakpoint) {
		    createFunctionEditor(getFieldEditorParent());
		}
		else if (tracepoint instanceof ICAddressBreakpoint) {
			String address = getPreferenceStore().getString(ICLineBreakpoint.ADDRESS);
			if (address == null || address.trim().length() == 0) {
				address = Messages.TracepointPropertyPage_NotAvailable;
			}
			addField(createLabelEditor(getFieldEditorParent(), Messages.TracepointPropertyPage_Address, address));
		}
		else { // LineTracepoint
		    String fileName = getPreferenceStore().getString(ICBreakpoint.SOURCE_HANDLE);
			if (fileName != null) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.TracepointPropertyPage_File, fileName));
			}
			int lNumber = getPreferenceStore().getInt(IMarker.LINE_NUMBER);
			if (lNumber > 0) {
				createLineNumberEditor(getFieldEditorParent());
			}
		}
	}

	private String getTracepointMainLabel(ICTracepoint tracepoint) {
	    IWorkbenchAdapter labelProvider = (IWorkbenchAdapter)getElement().getAdapter(IWorkbenchAdapter.class);
	    if (labelProvider != null) {
	        return labelProvider.getLabel(getElement());
	    }
        // default main label is the label of marker type for the tracepoint
        return CDIDebugModel.calculateMarkerType(tracepoint);
	}
	
    protected void createFunctionEditor(Composite parent) {
    	ICTracepoint tracepoint = getTracepoint();
        if (tracepoint == null || tracepoint.getMarker() == null) {
            TracepointStringFieldEditor expressionEditor = new TracepointStringFieldEditor(
                ICLineBreakpoint.FUNCTION, Messages.TracepointPropertyPage_FunctionName, parent);
            expressionEditor.setErrorMessage(Messages.TracepointPropertyPage_function_value_errorMessage);
            expressionEditor.setEmptyStringAllowed(false);
            addField(expressionEditor);
        } else {
            String function = getPreferenceStore().getString(ICLineBreakpoint.FUNCTION); 
            if (function == null) { 
                function = Messages.TracepointPropertyPage_NotAvailable;
            }
            addField(createLabelEditor(getFieldEditorParent(), Messages.TracepointPropertyPage_FunctionName, function));
        }
    }
	protected void createLineNumberEditor(Composite parent) {
		 String title = Messages.TracepointPropertyPage_LineNumber;
		 TracepointIntegerFieldEditor labelFieldEditor = new TracepointIntegerFieldEditor(IMarker.LINE_NUMBER, title, parent);
		 labelFieldEditor.setValidRange(1, Integer.MAX_VALUE);
		 addField(labelFieldEditor);
	}
	
	protected void createEnabledField(Composite parent) {
		fEnabled = new BooleanFieldEditor(ICBreakpoint.ENABLED, Messages.TracepointPropertyPage_Enabled, parent);
		addField(fEnabled);
	}

	protected void createConditionEditor(Composite parent) {
		fCondition = new TracepointStringFieldEditor(ICBreakpoint.CONDITION, Messages.TracepointPropertyPage_Condition, parent);
		fCondition.setEmptyStringAllowed(true);
		fCondition.setErrorMessage(Messages.TracepointPropertyPage_InvalidCondition);
		addField(fCondition);
	}

	protected void createIgnoreCountEditor(Composite parent) {
		fIgnoreCount = new TracepointIntegerFieldEditor(ICBreakpoint.IGNORE_COUNT, Messages.TracepointPropertyPage_IgnoreCount, parent);
		fIgnoreCount.setValidRange(0, Integer.MAX_VALUE);
		fIgnoreCountTextControl = fIgnoreCount.getTextControl(parent);
		fIgnoreCountTextControl.setEnabled( getPreferenceStore().getInt(ICBreakpoint.IGNORE_COUNT) >= 0 );
		addField(fIgnoreCount);
	}

	protected void createPassCountEditor(Composite parent) {
		fPassCount = new TracepointIntegerFieldEditor(ICTracepoint.PASS_COUNT, Messages.TracepointPropertyPage_PassCount, parent);
		fPassCount.setValidRange(0, Integer.MAX_VALUE);
		fPassCountTextControl = fPassCount.getTextControl(parent);
		fPassCountTextControl.setEnabled(getPreferenceStore().getInt(ICTracepoint.PASS_COUNT) >= 0);
		addField(fPassCount);
	}

	protected FieldEditor createLabelEditor(Composite parent, String title, String value) {
		return new LabelFieldEditor(parent, title, value);
	}

	protected ICTracepoint getTracepoint() {
		IAdaptable element = getElement();
		if (element instanceof ICTracepoint) {
		    return (ICTracepoint)element;
		}
		
		if (element instanceof ICBreakpointContext) {
			ICBreakpoint breakpoint =((ICBreakpointContext)element).getBreakpoint();
			if (breakpoint instanceof ICTracepoint) {
			    return (ICTracepoint)breakpoint;
			}
			assert false : "Should always have a tracepoint"; //$NON-NLS-1$
		}

		return (ICTracepoint)element.getAdapter(ICTracepoint.class);
	}

	protected Object getDebugContext() {
        IDebugContextProvider provider = (IDebugContextProvider)getElement().getAdapter(IDebugContextProvider.class);
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
        if (element instanceof ICTracepoint) {
            IMarker marker = ((ICTracepoint)element).getMarker();
            if (marker != null) {
                return marker.getResource();
            }
        } else if (element instanceof ICBreakpointContext) {
            return ((ICBreakpointContext)element).getResource();
        } 
        return null;
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
	    IAdaptable element = getElement();
	    if (element instanceof ICBreakpointContext) {
	        return ((ICBreakpointContext)element).getPreferenceStore();
	    }

	    if (fTracepointPreferenceStore == null) {
	        CBreakpointContext bpContext = element instanceof CBreakpointContext ? 
	            (CBreakpointContext)element : null;
	        fTracepointPreferenceStore = new CBreakpointPreferenceStore(bpContext, null);
	    }
	    return fTracepointPreferenceStore;
	}

	@Override
	public boolean performCancel() {
	    IPreferenceStore store = getPreferenceStore();
	    if (store instanceof CBreakpointPreferenceStore) {
	        ((CBreakpointPreferenceStore)store).setCanceled(true);
	    }
	    return super.performCancel();
	}

	@Override
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        if (store instanceof CBreakpointPreferenceStore) {
            ((CBreakpointPreferenceStore)store).setCanceled(false);
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
		fElement = element;
	}

	protected String[] getDebugModelIds() {
        String[] debugModelIds = null;
        Object debugContext = getDebugContext();
        IDebugModelProvider debugModelProvider = (IDebugModelProvider)
            DebugPlugin.getAdapter(debugContext, IDebugModelProvider.class);
        if (debugModelProvider != null) {
            debugModelIds = debugModelProvider.getModelIdentifiers();
        } else if (debugContext instanceof IDebugElement) {
            debugModelIds = new String[] { ((IDebugElement)debugContext).getModelIdentifier() };
        }
        return debugModelIds;
	}
}
