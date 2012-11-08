/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Nokia - https://bugs.eclipse.org/bugs/show_bug.cgi?id=145606
 * QNX Software Systems - Catchpoints support https://bugs.eclipse.org/bugs/show_bug.cgi?id=226689
 *  Scott Tepavich (WindRiver) -  Fixed bad reference to messages.properties string (Bug 393178)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.breakpoints;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemorySpaceManagement;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint2;
import org.eclipse.cdt.debug.internal.core.breakpoints.CEventBreakpoint;
import org.eclipse.cdt.debug.internal.ui.preferences.ComboFieldEditor;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.breakpoints.CBreakpointUIContributionFactory;
import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointContext;
import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContribution;
import org.eclipse.cdt.debug.ui.preferences.ReadOnlyFieldEditor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * The preference page used to present the properties of a breakpoint as preferences. A CBreakpointPreferenceStore is used to interface between this page and
 * the breakpoint.
 */
public class CBreakpointPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {

	private Composite fEventBPComposite;
	private Composite fEventArgsComposite;
	private List<FieldEditor> fEventArgsFEs = null;
	
	class BreakpointIntegerFieldEditor extends IntegerFieldEditor {

		public BreakpointIntegerFieldEditor( String name, String labelText, Composite parent ) {
			super( name, labelText, parent );
			setErrorMessage( BreakpointsMessages.getString( "CBreakpointPropertyPage.0" ) ); //$NON-NLS-1$
		}

		/**
		 * @see IntegerFieldEditor#checkState()
		 */
		@Override
		protected boolean checkState() {
			Text control = getTextControl();
			if ( !control.isEnabled() ) {
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}

		/**
		 * Overrode here to be package visible.
		 */
		@Override
		protected void refreshValidState() {
			super.refreshValidState();
		}

		/**
		 * Only store if the text control is enabled
		 * 
		 * @see FieldEditor#doStore()
		 */
		@Override
		protected void doStore() {
			Text text = getTextControl();
			if ( text.isEnabled() ) {
				super.doStore();
			}
		}

		/**
		 * Clears the error message from the message line if the error message is the error message from this field editor.
		 */
		@Override
		protected void clearErrorMessage() {
			if ( getPage() != null ) {
				String message = getPage().getErrorMessage();
				if ( message != null ) {
					if ( getErrorMessage().equals( message ) ) {
						super.clearErrorMessage();
					}
				}
				else {
					super.clearErrorMessage();
				}
			}
		}
	}

	class BreakpointStringFieldEditor extends StringFieldEditor {

		public BreakpointStringFieldEditor( String name, String labelText, Composite parent ) {
			super( name, labelText, parent );
		}

		/**
		 * @see StringFieldEditor#checkState()
		 */
		@Override
		protected boolean checkState() {
			Text control = getTextControl();
			if ( !control.isEnabled() ) {
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}

		@Override
		protected void doStore() {
			Text text = getTextControl();
			if ( text.isEnabled() ) {
				super.doStore();
			}
		}
		protected void doLoad()  {
			String value = getPreferenceStore().getString(getPreferenceName());
            setStringValue(value);
		}

		/**
		 * @see FieldEditor#refreshValidState()
		 */
		@Override
		protected void refreshValidState() {
			super.refreshValidState();
		}

		/**
		 * Clears the error message from the message line if the error message is the error message from this field editor.
		 */
		@Override
		protected void clearErrorMessage() {
			if ( getPage() != null ) {
				String message = getPage().getErrorMessage();
				if ( message != null ) {
					if ( getErrorMessage().equals( message ) ) {
						super.clearErrorMessage();
					}
				}
				else {
					super.clearErrorMessage();
				}
			}
		}
	}

    class WatchpointRangeFieldEditor extends IntegerFieldEditor {

        private static final String DISABLED_VALUE = "0"; //$NON-NLS-1$
        private Button fCheckbox;
        private boolean fWasSelected;
        
        public WatchpointRangeFieldEditor( String name, String labelText, Composite parent ) {
            super( name, labelText, parent );
        }
        
        @Override
        protected void doFillIntoGrid(Composite parent, int numColumns) {
            getCheckboxControl(parent);
            super.doFillIntoGrid(parent, numColumns);
        }

        private Button getCheckboxControl(Composite parent) {
            if (fCheckbox == null) {
                Composite inner= new Composite(parent, SWT.NULL);
                final GridLayout layout= new GridLayout(2, false);
                layout.marginWidth = 0;
                inner.setLayout(layout);
                fCheckbox= new Button(inner, SWT.CHECK);
                fCheckbox.setFont(parent.getFont());
                fCheckbox.setText(getLabelText());
                // create and hide label from base class
                Label label = getLabelControl(inner);
                label.setText(""); //$NON-NLS-1$
                label.setVisible(false);
                fCheckbox.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        boolean isSelected = fCheckbox.getSelection();
                        valueChanged(fWasSelected, isSelected);
                        fWasSelected = isSelected;
                    }
                });
            } else {
                checkParent(fCheckbox.getParent(), parent);
            }
            return fCheckbox;
        }

        @Override
        protected boolean checkState() {
            if (fCheckbox != null && !fCheckbox.getSelection()) {
                clearErrorMessage();
                return true;
            }
            return super.checkState();
        }
        
        @Override
        public Label getLabelControl(Composite parent) {
            final Label label= getLabelControl();
            if (label == null) {
                return super.getLabelControl(parent);
            } else {
                checkParent(label.getParent(), parent);
            }
            return label;
        }

        @Override
        protected void doLoad() {
            if (getTextControl() != null && fCheckbox != null && getLabelControl() != null) {
                oldValue = getPreferenceStore().getString(getPreferenceName());
                boolean enabled = !DISABLED_VALUE.equals(oldValue);
                getTextControl().setText(enabled ? oldValue : ""); //$NON-NLS-1$
                fCheckbox.setSelection(enabled);
                fWasSelected = enabled;
                getTextControl().setEnabled(enabled);
                getLabelControl().setEnabled(enabled);
            }
        }

        @Override
        protected void doStore() {
            if (fCheckbox != null && !fCheckbox.getSelection()) {
                getPreferenceStore().setValue(getPreferenceName(), DISABLED_VALUE);
            } else {
                Text text = getTextControl();
                if (text != null) {
                    getPreferenceStore().setValue(getPreferenceName(), text.getText().trim());
                }
            }
        }

        @Override
        public int getIntValue() throws NumberFormatException {
            if (fCheckbox != null && !fCheckbox.getSelection()) {
                return 0;
            } else {
                return super.getIntValue();
            }
        }
        
        protected void valueChanged(boolean oldValue, boolean newValue) {
            if (oldValue != newValue) {
                valueChanged();
                fireStateChanged(VALUE, oldValue, newValue);
                getTextControl().setEnabled(newValue);
                getLabelControl().setEnabled(newValue);
            }
        }
        
    }

    class WatchpointMemorySpaceFieldEditor extends ComboFieldEditor {

        private static final String DISABLED_VALUE = ""; //$NON-NLS-1$
        private Button fCheckbox;
        private boolean fWasSelected;
        
        public WatchpointMemorySpaceFieldEditor( String name, String labelText, String[] memorySpaces, Composite parent ) {
            super( name, labelText, makeArray2D(memorySpaces), parent );
        }        
        
        @Override
        protected void doFillIntoGrid(Composite parent, int numColumns) {
            getCheckboxControl(parent);
            super.doFillIntoGrid(parent, numColumns);
        }

        private Button getCheckboxControl(Composite parent) {
            if (fCheckbox == null) {
                Composite inner= new Composite(parent, SWT.NULL);
                final GridLayout layout= new GridLayout(2, false);
                layout.marginWidth = 0;
                inner.setLayout(layout);
                fCheckbox= new Button(inner, SWT.CHECK);
                fCheckbox.setFont(parent.getFont());
                fCheckbox.setText(getLabelText());
                // create and hide label from base class
                Label label = getLabelControl(inner);
                label.setText(""); //$NON-NLS-1$
                label.setVisible(false);
                fCheckbox.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        boolean isSelected = fCheckbox.getSelection();
                        valueChanged(fWasSelected, isSelected);
                        fWasSelected = isSelected;
                    }
                });
            } else {
                checkParent(fCheckbox.getParent(), parent);
            }
            return fCheckbox;
        }

        @Override
        public Label getLabelControl(Composite parent) {
            final Label label= getLabelControl();
            if (label == null) {
                return super.getLabelControl(parent);
            } else {
                checkParent(label.getParent(), parent);
            }
            return label;
        }

        @Override
        protected void doLoad() {
            super.doLoad();
            if (fCheckbox != null && getLabelControl() != null) {
                String value = getPreferenceStore().getString(getPreferenceName());
                boolean enabled = !DISABLED_VALUE.equals(value);
                fCheckbox.setSelection(enabled);
                fWasSelected = enabled;
                getComboBoxControl().setEnabled(enabled);
                getLabelControl().setEnabled(enabled);
            }
        }

        @Override
        protected void doStore() {
            if (fCheckbox != null && !fCheckbox.getSelection()) {
                getPreferenceStore().setValue(getPreferenceName(), DISABLED_VALUE);
            } else {
                super.doStore();
            }
        }

        protected void valueChanged(boolean oldValue, boolean newValue) {
            if (oldValue != newValue) {
                fireStateChanged(VALUE, oldValue, newValue);
                getComboBoxControl().setEnabled(newValue);
                getLabelControl().setEnabled(newValue);
            }
        }
        
    }

    private String[][] makeArray2D(String[] array) {
        String[][] array2d = new String[array.length][];
        for (int i = 0; i < array.length; i++) {
            array2d[i] = new String[2];
            array2d[i][0] = array2d[i][1] =  array[i];
        }
        return array2d;
    }

    private String[][] joinToArray2D(String[] labels, String[] values) {
        String[][] array2d = new String[labels.length][];
        for (int i = 0; i < labels.length; i++) {
            array2d[i] = new String[2];
            array2d[i][0] = labels[i];
            array2d[i][1] = values[i];
        }
        return array2d;
    }
    
    private ICDIMemorySpaceManagement getMemorySpaceManagement(){
        Object debugViewElement = getDebugContext();
        ICDIMemorySpaceManagement memMgr = null;
        
        if ( debugViewElement != null ) {
            ICDebugTarget debugTarget = (ICDebugTarget)DebugPlugin.getAdapter(debugViewElement, ICDebugTarget.class);
            
            if ( debugTarget != null ){
                ICDITarget target = (ICDITarget)debugTarget.getAdapter(ICDITarget.class);
            
                if (target instanceof ICDIMemorySpaceManagement)
                    memMgr = (ICDIMemorySpaceManagement)target;
            }
        }
        
        return memMgr;
    }
    
	class LabelFieldEditor extends ReadOnlyFieldEditor {
		private String fValue;

		public LabelFieldEditor( Composite parent, String title, String value ) {
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

	private BreakpointStringFieldEditor fCondition;

	private Text fIgnoreCountTextControl;

	private BreakpointIntegerFieldEditor fIgnoreCount;

	private IAdaptable fElement;

	/**
	 * The preference store used to interface between the breakpoint and the 
	 * breakpoint preference page.  This preference store is initialized only
	 * when the preference store cannot be retrieved from the preference 
	 * dialog's element.
	 * @see #getPreferenceStore()
	 */
	private CBreakpointPreferenceStore fCBreakpointPreferenceStore;

	/**
	 * Constructor for CBreakpointPropertyPage.
	 * 
	 * @param breakpoint
	 */
	public CBreakpointPropertyPage() {
		super( GRID );
		noDefaultAndApplyButton();
//		Control control = getControl();
//		fCBreakpointPreferenceStore = new CBreakpointPreferenceStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		ICBreakpoint breakpoint = getBreakpoint();
		createMainLabel(breakpoint);
		createContributedFieldEditors(breakpoint, ICBreakpointsUIContribution.BREAKPOINT_LABELS);
		createTypeSpecificLabelFieldEditors( breakpoint );
		createEnabledField( getFieldEditorParent() );
		createConditionEditor( getFieldEditorParent() );
		createIgnoreCountEditor( getFieldEditorParent() );
        createContributedFieldEditors(breakpoint, ICBreakpointsUIContribution.BREAKPOINT_EDITORS);
	}

	private void createMainLabel(ICBreakpoint breakpoint) {
	    String label = getBreakpointMainLabel(breakpoint); 
		addField( createLabelEditor( 
		    getFieldEditorParent(), 
		    BreakpointsMessages.getString( "CBreakpointPropertyPage.breakpointType_label" ),  //$NON-NLS-1$
		    label) );
	}

	/**
	 * Method createTypeSpecificLabelFieldEditors.
	 * 
	 * @param breakpoint
	 */
	private void createTypeSpecificLabelFieldEditors( ICBreakpoint breakpoint ) {

		if ( breakpoint instanceof ICFunctionBreakpoint ) {
		    createFunctionEditor(getFieldEditorParent());
		}
		else if ( breakpoint instanceof ICAddressBreakpoint ) {
	        String title = BreakpointsMessages.getString( "CBreakpointPropertyPage.address_label" ); //$NON-NLS-1$
	        
            String address = getPreferenceStore().getString(ICLineBreakpoint.ADDRESS);
            if (address == null || address.trim().length() == 0) {
                address = BreakpointsMessages.getString( "CBreakpointPropertyPage.address_valueNotAvailable_label" ); //$NON-NLS-1$
            }
            addField( createLabelEditor( getFieldEditorParent(), title, address ) ); 
		}
		else if ( breakpoint instanceof ICWatchpoint ) {
			IResource resource = getResource();
			if (resource != null) {
    			IProject project = resource.getProject();
    			if ( project != null ) {
    				addField( createLabelEditor( getFieldEditorParent(), BreakpointsMessages.getString( "CBreakpointPropertyPage.project_label" ), project.getName() ) ); //$NON-NLS-1$
    			}
			} 
			String filename = getPreferenceStore().getString(ICBreakpoint.SOURCE_HANDLE);
			if (filename != null && !"".equals(filename)) { //$NON-NLS-1$
				addField( createLabelEditor( getFieldEditorParent(), BreakpointsMessages.getString( "CBreakpointPropertyPage.sourceHandle_label" ), filename ) ); //$NON-NLS-1$
			}
			createWatchExpressionEditor(getFieldEditorParent());
            createWatchMemorySpaceEditor(getFieldEditorParent());
			createWatchRangeEditor(getFieldEditorParent());
			createWatchTypeEditors(getFieldEditorParent());
			
		}
		else if ( breakpoint instanceof ILineBreakpoint ) {
		    String fileName = getPreferenceStore().getString(ICBreakpoint.SOURCE_HANDLE);
			if ( fileName != null ) {
				addField( createLabelEditor( getFieldEditorParent(), BreakpointsMessages.getString( "CBreakpointPropertyPage.sourceHandle_label" ), fileName ) ); //$NON-NLS-1$
			}
			int lNumber = getPreferenceStore().getInt(IMarker.LINE_NUMBER);
			if (lNumber > 0) {
				getPreferenceStore().setValue( IMarker.LINE_NUMBER, lNumber);
				createLineNumberEditor(getFieldEditorParent());
			}
		}
		else if ( breakpoint instanceof CEventBreakpoint ) {
			createEventBreakpointEditor( breakpoint, ICBreakpointsUIContribution.BREAKPOINT_LABELS);
		}
	}

	private String getBreakpointMainLabel(ICBreakpoint breakpoint) {
	    if (breakpoint instanceof ICWatchpoint  && breakpoint.getMarker() != null) {
	        // For an existing breakpoint, calculate watchpoint label based 
	        // on read/write type.
            boolean isReadType = getPreferenceStore().getBoolean(ICWatchpoint.READ);
            boolean isWriteType = getPreferenceStore().getBoolean(ICWatchpoint.WRITE);
            if (isReadType && !isWriteType) {
                return BreakpointsMessages.getString("CBreakpointPropertyPage.breakpointType_watchpoint_read_label"); //$NON-NLS-1$
            } else if (!isReadType && isWriteType) {
                return BreakpointsMessages.getString("CBreakpointPropertyPage.breakpointType_watchpoint_label"); //$NON-NLS-1$
            } else {
                return BreakpointsMessages.getString("CBreakpointPropertyPage.breakpointType_watchpoint_access_label"); //$NON-NLS-1$
            }
	    }
	    
	    IWorkbenchAdapter labelProvider = (IWorkbenchAdapter)getElement().getAdapter(IWorkbenchAdapter.class);
	    if (labelProvider != null) {
	        return labelProvider.getLabel(getElement());
	    }
        // default main label is the label of marker type for the breakpoint
        return CDIDebugModel.calculateMarkerType(breakpoint);
	}
	
    protected void createFunctionEditor( Composite parent ) {
            
        ICBreakpoint breakpoint = getBreakpoint();
        String title = BreakpointsMessages.getString("CBreakpointPropertyPage.function_label"); //$NON-NLS-1$
        if (breakpoint == null || breakpoint.getMarker() == null) {
            BreakpointStringFieldEditor expressionEditor = new BreakpointStringFieldEditor(
                ICLineBreakpoint.FUNCTION, title, parent);
            expressionEditor.setErrorMessage(BreakpointsMessages.getString("CBreakpointPropertyPage.function_value_errorMessage")); //$NON-NLS-1$
            expressionEditor.setEmptyStringAllowed(false);
            addField(expressionEditor);
        } else {
            String function = getPreferenceStore().getString(ICLineBreakpoint.FUNCTION); 
            if ( function == null ) { 
                function = BreakpointsMessages.getString( "CBreakpointPropertyPage.function_valueNotAvailable_label" ); //$NON-NLS-1$
            }
            addField( createLabelEditor( getFieldEditorParent(), BreakpointsMessages.getString( "CBreakpointPropertyPage.function_label" ), function ) ); //$NON-NLS-1$
        }
    }
	
	protected void createLineNumberEditor( Composite parent ) {
		 String title = BreakpointsMessages.getString( "CBreakpointPropertyPage.lineNumber_label" ); //$NON-NLS-1$
		 BreakpointIntegerFieldEditor labelFieldEditor =new BreakpointIntegerFieldEditor( IMarker.LINE_NUMBER ,title, parent);
		 labelFieldEditor.setValidRange( 1, Integer.MAX_VALUE );
		 addField( labelFieldEditor );
	}

    protected void createWatchExpressionEditor( Composite parent ) {
        ICBreakpoint breakpoint = getBreakpoint();
        if (breakpoint == null || breakpoint.getMarker() == null) {
            BreakpointStringFieldEditor expressionEditor =new BreakpointStringFieldEditor(
                 ICWatchpoint.EXPRESSION,
                 BreakpointsMessages.getString("CBreakpointPropertyPage.watchpoint_expression_label"), //$NON-NLS-1$
                 parent);
            expressionEditor.setErrorMessage(BreakpointsMessages.getString("CBreakpointPropertyPage.watchpoint_expression_errorMessage")); //$NON-NLS-1$
            expressionEditor.setEmptyStringAllowed(false);
            addField(expressionEditor);
        } else {
            addField(createLabelEditor(
                parent, 
                BreakpointsMessages.getString("CBreakpointPropertyPage.watchpoint_expression_label"), //$NON-NLS-1$
                getPreferenceStore().getString(ICWatchpoint.EXPRESSION) ));
        }
    }

    protected void createWatchMemorySpaceEditor( Composite parent ) {
        ICBreakpoint breakpoint = getBreakpoint();
        if (breakpoint == null || breakpoint.getMarker() == null) {
            ICDIMemorySpaceManagement memSpaceMgmt = getMemorySpaceManagement();
            if (memSpaceMgmt != null) {
                String[] memorySpaces = memSpaceMgmt.getMemorySpaces();
                if (memorySpaces != null && memorySpaces.length != 0) {
                    addField( new WatchpointMemorySpaceFieldEditor(
                         ICWatchpoint2.MEMORYSPACE,
                         BreakpointsMessages.getString("CBreakpointPropertyPage.watchpoint_memorySpace_label"), //$NON-NLS-1$
                         memorySpaces,
                         parent) ); 
                }
            }
        } else {
            String memorySpace = getPreferenceStore().getString(ICWatchpoint2.MEMORYSPACE);
            if (memorySpace != null && memorySpace.length() != 0) {
                addField(createLabelEditor(
                    parent, 
                    BreakpointsMessages.getString("CBreakpointPropertyPage.watchpoint_memorySpace_label"), //$NON-NLS-1$
                    getPreferenceStore().getString(ICWatchpoint2.MEMORYSPACE) ));
            }
        }
    }
    
    protected void createWatchRangeEditor( Composite parent ) {
        ICBreakpoint breakpoint = getBreakpoint();
        if (breakpoint == null || breakpoint.getMarker() == null) {
            addField( new WatchpointRangeFieldEditor(
                 ICWatchpoint2.RANGE,
                 BreakpointsMessages.getString("CBreakpointPropertyPage.watchpoint_range_label"), //$NON-NLS-1$
                 parent) ); 
        } else {
            addField(createLabelEditor(
                parent, 
                BreakpointsMessages.getString("CBreakpointPropertyPage.watchpoint_range_label"), //$NON-NLS-1$
                getPreferenceStore().getString(ICWatchpoint2.RANGE) ));
        }
    }

    protected void createWatchTypeEditors( Composite parent ) {
        // Edit read/write options only when creating the breakpoint.
        ICBreakpoint breakpoint = getBreakpoint();
        if (breakpoint != null && breakpoint.getMarker() == null) {
            addField( new BooleanFieldEditor(
                 ICWatchpoint.READ,
                 BreakpointsMessages.getString("CBreakpointPropertyPage.watchpointType_read_label"), //$NON-NLS-1$
                 parent) );
            addField( new BooleanFieldEditor(
                ICWatchpoint.WRITE,
                BreakpointsMessages.getString("CBreakpointPropertyPage.watchpointType_write_label"), //$NON-NLS-1$
                parent) );
        }
    }

	protected void createEnabledField( Composite parent ) {
		fEnabled = new BooleanFieldEditor( ICBreakpoint.ENABLED, BreakpointsMessages.getString( "CBreakpointPropertyPage.enabled_label" ), parent ); //$NON-NLS-1$
		addField( fEnabled );
	}

	protected void createConditionEditor( Composite parent ) {
		fCondition = new BreakpointStringFieldEditor( ICBreakpoint.CONDITION, BreakpointsMessages.getString( "CBreakpointPropertyPage.condition_label" ), parent ); //$NON-NLS-1$
		fCondition.setEmptyStringAllowed( true );
		fCondition.setErrorMessage( BreakpointsMessages.getString( "CBreakpointPropertyPage.condition_invalidValue_message" ) ); //$NON-NLS-1$
		addField( fCondition );
	}

	protected void createIgnoreCountEditor( Composite parent ) {
		fIgnoreCount = new BreakpointIntegerFieldEditor( ICBreakpoint.IGNORE_COUNT, BreakpointsMessages.getString( "CBreakpointPropertyPage.ignoreCount_label" ), parent ); //$NON-NLS-1$
		fIgnoreCount.setValidRange( 0, Integer.MAX_VALUE );
		fIgnoreCountTextControl = fIgnoreCount.getTextControl( parent );
		fIgnoreCountTextControl.setEnabled( getPreferenceStore().getInt(ICBreakpoint.IGNORE_COUNT) >= 0 );
		addField( fIgnoreCount );
	}

	protected FieldEditor createLabelEditor( Composite parent, String title, String value ) {
		return new LabelFieldEditor( parent, title, value );
	}

	protected ICBreakpoint getBreakpoint() {
		IAdaptable element = getElement();
		if (element instanceof ICBreakpoint) {
		    return (ICBreakpoint)element;
		} else if (element instanceof ICBreakpointContext) {
		    return ((ICBreakpointContext)element).getBreakpoint();
		} else {
		    return (ICBreakpoint)element.getAdapter(ICBreakpoint.class);
		}
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
        if (element instanceof ICBreakpoint) {
            IMarker marker = ((ICBreakpoint)element).getMarker();
            if (marker != null) {
                return marker.getResource();
            }
        } else if (element instanceof ICBreakpointContext) {
            return ((ICBreakpointContext)element).getResource();
        } 
        return null;
	}

	public IPreferenceStore getPreferenceStore() {
	    IAdaptable element = getElement();
	    if (element instanceof ICBreakpointContext) {
	        return ((ICBreakpointContext)element).getPreferenceStore();
	    }

	    if (fCBreakpointPreferenceStore == null) {
	        CBreakpointContext bpContext = element instanceof CBreakpointContext ? 
	            (CBreakpointContext)element : null;
	        fCBreakpointPreferenceStore = new CBreakpointPreferenceStore(bpContext, null);
	    }
	    return fCBreakpointPreferenceStore;
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
	public void setElement( IAdaptable element ) {
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
	
	private void createEventBreakpointEditor( ICBreakpoint breakpoint, String conMainElement) {
		boolean bAddEventType = true;
		Composite parent = getFieldEditorParent();		
		String[] debugModelIds = getDebugModelIds();
		try {
		    ICBreakpointsUIContribution[] cons;
		    CBreakpointUIContributionFactory factory = CBreakpointUIContributionFactory.getInstance();
		    IPreferenceStore prefStore = getPreferenceStore();
		    if (prefStore instanceof CBreakpointPreferenceStore) {
		        cons = factory.getBreakpointUIContributions(
		            debugModelIds, breakpoint, ((CBreakpointPreferenceStore) prefStore).getAttributes());
		    } else {
                cons = factory.getBreakpointUIContributions(breakpoint);
		    }
		    
		    setupEventTypeFieldEditor(cons, breakpoint, conMainElement, parent);
			for (ICBreakpointsUIContribution con : cons) {
			    if ( conMainElement.equals(con.getMainElement()) ) {
			    	FieldEditor fieldEditor = null;
			    	if (breakpoint.getMarker() == null && con.getId().equals(ICEventBreakpoint.EVENT_TYPE_ID)) {
			    		continue;
			    	}
			    	else if (con.getMarkerType().equalsIgnoreCase(ICEventBreakpoint.C_EVENT_BREAKPOINT_MARKER)) {
			    		if ( breakpoint.getMarker() == null ) {
			    			setupArgsComposite(parent);
			    			fieldEditor = con.getFieldEditor(con.getId(), con.getLabel() + ":", fEventArgsComposite); //$NON-NLS-1$			    			
			    			if ( fieldEditor != null ) {
			    				addEditorToComposite(fieldEditor);
			    				fEventArgsComposite.setVisible(true);
			    			}
			    			else { 
			    				fEventArgsComposite.setVisible(false);
			    			}
			    		}
			    		else {
			    			if (con.getId().equals(ICEventBreakpoint.EVENT_TYPE_ID)) {
			    				if (bAddEventType == true) bAddEventType = false;
			    				else continue;
			    			}
				    		fieldEditor = con.getFieldEditor(con.getId(), con.getLabel() + ":", parent); //$NON-NLS-1$
			    		}
			    	}

    				if (fieldEditor != null) {
    					addField(fieldEditor);
    				}
			    }
			}
		} catch (CoreException ce) {
			CDebugUIPlugin.log(ce);
		}		
	}
	/**
	 * Creates field editors contributed using breakpointUIContribution extension point
	 */
	private void createContributedFieldEditors(ICBreakpoint breakpoint, String conMainElement) {
		Composite parent = getFieldEditorParent();
		String[] debugModelIds = getDebugModelIds();
		try {
		    ICBreakpointsUIContribution[] cons;
		    CBreakpointUIContributionFactory factory = CBreakpointUIContributionFactory.getInstance();
		    IPreferenceStore prefStore = getPreferenceStore();
		    if (prefStore instanceof CBreakpointPreferenceStore) {
		        cons = factory.getBreakpointUIContributions(
		            debugModelIds, breakpoint, ((CBreakpointPreferenceStore) prefStore).getAttributes());
		    } else {
                cons = factory.getBreakpointUIContributions(breakpoint);
		    }
		    
			for (ICBreakpointsUIContribution con : cons) {
			    if ( conMainElement.equals(con.getMainElement()) ) {
			    	if (con.getMarkerType().equals(ICEventBreakpoint.C_EVENT_BREAKPOINT_MARKER)) {
			    		continue;
			    	}
			    	FieldEditor fieldEditor = con.getFieldEditor(con.getId(), con.getLabel() + ":", parent); //$NON-NLS-1$
    				if (fieldEditor != null) {
    					addField(fieldEditor);
    				}
			    }
			}
		} catch (CoreException ce) {
			CDebugUIPlugin.log(ce);
		}
	}
	
	private void setupEventTypeFieldEditor(ICBreakpointsUIContribution[] cons, ICBreakpoint breakpoint, String conMainElement, Composite parent) {
		String id = null;
		ArrayList<String> eventTypeValueList = new ArrayList<String>();
		ArrayList<String> eventTypeLabelList = new ArrayList<String>();		
		
		// The filter of the debugModelIds should already be done.
		for (ICBreakpointsUIContribution con : cons) {
		    if ( conMainElement.equals(con.getMainElement()) ) {
		    	if (breakpoint instanceof CEventBreakpoint && breakpoint.getMarker() == null &&
		    			con.getId().equals(ICEventBreakpoint.EVENT_TYPE_ID)) {
		    		id = con.getId();
		    		for (String value : con.getPossibleValues()) {
		    			eventTypeValueList.add(value);
		    			eventTypeLabelList.add(con.getLabelForValue(value));
		    		}
		    	}
		    }
		}
		if (eventTypeValueList.size() != 0) {
			EventTypeFieldEditor fieldEditor = new EventTypeFieldEditor(
					id, 
					BreakpointsMessages.getString("CBreakpointPropertyPage.eventType_label"), //$NON-NLS-1$
					eventTypeLabelList.toArray(new String[eventTypeLabelList.size()]),
					eventTypeValueList.toArray(new String[eventTypeValueList.size()]),
					parent, 
					breakpoint);
			addField(fieldEditor);
			setupArgsComposite(parent);
			fieldEditor.initializeComboBox(getPreferenceStore(), this);
		}
	}
	
	void addEditorToComposite(FieldEditor fieldEditor) {
		if (fEventArgsFEs == null) {
		    fEventArgsFEs = new ArrayList<FieldEditor>();
		}
		fEventArgsFEs.add(fieldEditor);
	}

	void cleanEditorsFromComposite() {
		if (fEventArgsFEs != null) {
			for (FieldEditor editor : fEventArgsFEs){
				editor.setPreferenceStore(null);
				editor.setPage(null);
			}
		}
	}

	void setupArgsComposite(Composite parent) {
		if (fEventArgsComposite != null) {
			cleanEditorsFromComposite();
			fEventArgsComposite.dispose();
			fEventArgsComposite = null;
		}
		if (fEventBPComposite == null || fEventBPComposite.isDisposed()) {
			fEventBPComposite = new Composite(parent,SWT.NONE);
			fEventBPComposite.setLayout(parent.getLayout());
			fEventBPComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
			GridDataFactory.defaultsFor(fEventBPComposite).grab(true, false).span(2, 1).applyTo(fEventBPComposite);
		}
		fEventArgsComposite = new Composite(fEventBPComposite,SWT.NONE);
		fEventArgsComposite.setLayout(fEventBPComposite.getLayout());
		GridDataFactory.defaultsFor(fEventArgsComposite).grab(true, false).span(2, 1).applyTo(fEventArgsComposite);
		GridData gridData = (GridData)fEventArgsComposite.getLayoutData();
		gridData.horizontalIndent = 10;
		fEventArgsComposite.setLayoutData(gridData);
		fEventArgsComposite.setVisible(false);
	}
	
	private void displayEventArgs(ICBreakpoint breakpoint, Composite parent) {
		boolean result = false;
		String[] debugModelIds = getDebugModelIds();

		try {
		    ICBreakpointsUIContribution[] cons;
		    CBreakpointUIContributionFactory factory = CBreakpointUIContributionFactory.getInstance();
		    IPreferenceStore prefStore = getPreferenceStore();
		    if (prefStore instanceof CBreakpointPreferenceStore) {
		        cons = factory.getBreakpointUIContributions(
		            debugModelIds, breakpoint, ((CBreakpointPreferenceStore) prefStore).getAttributes());
		    } else {
		        cons = factory.getBreakpointUIContributions(breakpoint);
		    }
		    for (ICBreakpointsUIContribution con : cons) {
		        if (con.getMarkerType().equalsIgnoreCase(ICEventBreakpoint.C_EVENT_BREAKPOINT_MARKER) &&
		            !con.getId().equals(ICEventBreakpoint.EVENT_TYPE_ID)) {
		            setupArgsComposite(parent);
		            FieldEditor fieldEditor = con.getFieldEditor(con.getId(), con.getLabel() + ":", fEventArgsComposite); //$NON-NLS-1$
		            if ( fieldEditor != null ) {
		                fieldEditor.setPreferenceStore(getPreferenceStore());
		                fieldEditor.setPage(this);
		                addEditorToComposite(fieldEditor);
		                addField(fieldEditor);			    			 
		                result = true;
		            }
		        }
		    }
		} catch (CoreException ce) {
		    CDebugUIPlugin.log(ce);
		}

		if (fEventArgsComposite != null && !fEventArgsComposite.isDisposed()) {
			fEventArgsComposite.setVisible(result);
			fEventArgsComposite.layout();
			fEventBPComposite.layout();
		}
	}

    class EventTypeFieldEditor extends ComboFieldEditor {

    	final Combo fCombo;
    	final Composite fParent;
    	final ICBreakpoint fBreakpoint;
    	
    	class EventTypeSelectionListener implements SelectionListener {
    		@Override
    		public void widgetSelected(SelectionEvent e) {
    			doStore();
    		    displayEventArgs(fBreakpoint, fParent);
    		    fParent.layout();
    		}
    		@Override
    		public void widgetDefaultSelected(SelectionEvent e) {
    		    widgetSelected(e);
    		}
    	}
    	
        public EventTypeFieldEditor( String name, String labelText, String[] eventTypesLabels, String[] eventTypesValues, Composite parent, ICBreakpoint breakpoint ) {
        	super( name, labelText, joinToArray2D(eventTypesLabels,eventTypesValues), parent);
            fBreakpoint = breakpoint;
            fParent = parent;
            fCombo = this.getComboBoxControl();
        	fCombo.select(0);        	            
            fCombo.addSelectionListener(new EventTypeSelectionListener());
        }
      
        public void initializeComboBox(IPreferenceStore prefStore, CBreakpointPropertyPage page) {
            if (getPage() == null) {
                setPage(page);
            }
        	if (getPreferenceStore() == null) {
        		setPreferenceStore(prefStore);
        	}
        	else
        		prefStore = getPreferenceStore();
        	
        	String value = getValueForName(fCombo.getText());
		    if (prefStore instanceof CBreakpointPreferenceStore) {
		    	prefStore.setValue(ICEventBreakpoint.EVENT_TYPE_ID, value);
		    }
		    displayEventArgs(fBreakpoint, fParent);
        }
    }
}
