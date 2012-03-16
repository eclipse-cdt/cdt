/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Nokia - https://bugs.eclipse.org/bugs/show_bug.cgi?id=145606
 * QNX Software Systems - Catchpoints support https://bugs.eclipse.org/bugs/show_bug.cgi?id=226689
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.breakpoints;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
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
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * The preference page used to present the properties of a breakpoint as preferences. A CBreakpointPreferenceStore is used to interface between this page and
 * the breakpoint.
 */
public class CBreakpointPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {

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
		createContributedFieldEditors(breakpoint);
		createTypeSpecificLabelFieldEditors( breakpoint );
		createEnabledField( getFieldEditorParent() );
		createConditionEditor( getFieldEditorParent() );
		createIgnoreCountEditor( getFieldEditorParent() );
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
                return BreakpointsMessages.getString("CBreakpointPropertyPage.watchpointType_breakpointType_watchpoint_access_label"); //$NON-NLS-1$
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

	/**
	 * Creates field editors contributed using breakpointUIContribution extension point
	 * @param breakpoint
	 */
	private void createContributedFieldEditors(ICBreakpoint breakpoint) {
		Composite parent = getFieldEditorParent();
		try {
		    ICBreakpointsUIContribution[] cons;
		    CBreakpointUIContributionFactory factory = CBreakpointUIContributionFactory.getInstance();
		    IPreferenceStore prefStore = getPreferenceStore();
		    if (prefStore instanceof CBreakpointPreferenceStore) {
		        cons = factory.getBreakpointUIContributions(
		            breakpoint, ((CBreakpointPreferenceStore) prefStore).getAttributes());
		    } else {
                cons = factory.getBreakpointUIContributions(breakpoint);
		    }
		    
			for (ICBreakpointsUIContribution con : cons) {
				FieldEditor fieldEditor = con.getFieldEditor(con.getId(), con.getLabel()+":", parent); //$NON-NLS-1$
				if (fieldEditor != null) {
					addField(fieldEditor);
				}
			}
		} catch (CoreException ce) {
			CDebugUIPlugin.log(ce);
		}

	}

}
