/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The preference page used to present the properties of a breakpoint as preferences. A CBreakpointPreferenceStore is used to interface between this page and
 * the breakpoint.
 */
public class CBreakpointPreferencePage extends FieldEditorPreferencePage {

	class BreakpointIntegerFieldEditor extends IntegerFieldEditor {

		public BreakpointIntegerFieldEditor( String name, String labelText, Composite parent ) {
			super( name, labelText, parent );
			setErrorMessage( ActionMessages.getString( "CBreakpointPreferencePage.0" ) ); //$NON-NLS-1$
		}

		/**
		 * @see IntegerFieldEditor#checkState()
		 */
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
		protected void refreshValidState() {
			super.refreshValidState();
		}

		/**
		 * Only store if the text control is enabled
		 * 
		 * @see FieldEditor#doStore()
		 */
		protected void doStore() {
			Text text = getTextControl();
			if ( text.isEnabled() ) {
				super.doStore();
			}
		}

		/**
		 * Clears the error message from the message line if the error message is the error message from this field editor.
		 */
		protected void clearErrorMessage() {
			if ( getPreferencePage() != null ) {
				String message = getPreferencePage().getErrorMessage();
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
		protected boolean checkState() {
			Text control = getTextControl();
			if ( !control.isEnabled() ) {
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}

		protected void doStore() {
			Text text = getTextControl();
			if ( text.isEnabled() ) {
				super.doStore();
			}
		}

		/**
		 * @see FieldEditor#refreshValidState()
		 */
		protected void refreshValidState() {
			super.refreshValidState();
		}

		/**
		 * Clears the error message from the message line if the error message is the error message from this field editor.
		 */
		protected void clearErrorMessage() {
			if ( getPreferencePage() != null ) {
				String message = getPreferencePage().getErrorMessage();
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

	class LabelFieldEditor extends FieldEditor {

		private Label fTitleLabel;

		private Label fValueLabel;

		private Composite fBasicComposite;

		private String fValue;

		private String fTitle;

		public LabelFieldEditor( Composite parent, String title, String value ) {
			fValue = value;
			fTitle = title;
			this.createControl( parent );
		}

		protected void adjustForNumColumns( int numColumns ) {
			((GridData)fBasicComposite.getLayoutData()).horizontalSpan = numColumns;
		}

		protected void doFillIntoGrid( Composite parent, int numColumns ) {
			fBasicComposite = new Composite( parent, SWT.NULL );
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.numColumns = 2;
			fBasicComposite.setLayout( layout );
			GridData data = new GridData();
			data.verticalAlignment = GridData.FILL;
			data.horizontalAlignment = GridData.FILL;
			fBasicComposite.setLayoutData( data );
			fTitleLabel = new Label( fBasicComposite, SWT.NONE );
			fTitleLabel.setText( fTitle );
			GridData gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			fTitleLabel.setLayoutData( gd );
			fValueLabel = new Label( fBasicComposite, SWT.WRAP );
			fValueLabel.setText( fValue );
			gd = new GridData();
			fValueLabel.setLayoutData( gd );
		}

		public int getNumberOfControls() {
			return 1;
		}

		/**
		 * The label field editor is only used to present a text label on a preference page.
		 */
		protected void doLoad() {
		}

		protected void doLoadDefault() {
		}

		protected void doStore() {
		}
	}

	private BreakpointStringFieldEditor fCondition;

	private Text fIgnoreCountTextControl;

	private BreakpointIntegerFieldEditor fIgnoreCount;

	private ICBreakpoint fBreakpoint;

	/**
	 * Constructor for CBreakpointPreferencePage.
	 * 
	 * @param breakpoint
	 */
	public CBreakpointPreferencePage( ICBreakpoint breakpoint ) {
		super( GRID );
		setBreakpoint( breakpoint );
		noDefaultAndApplyButton();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		ICBreakpoint breakpoint = getBreakpoint();
		createTypeSpecificLabelFieldEditors( breakpoint );
		IPreferenceStore store = getPreferenceStore();
		try {
			String condition = breakpoint.getCondition();
			if ( condition == null ) {
				condition = ""; //$NON-NLS-1$
			}
			store.setValue( CBreakpointPreferenceStore.CONDITION, condition );
			createConditionEditor( getFieldEditorParent() );
			store.setValue( CBreakpointPreferenceStore.ENABLED, breakpoint.isEnabled() );
			int ignoreCount = breakpoint.getIgnoreCount();
			store.setValue( CBreakpointPreferenceStore.IGNORE_COUNT, (ignoreCount >= 0) ? ignoreCount : 0 );
			createIgnoreCountEditor( getFieldEditorParent() );
		}
		catch( CoreException ce ) {
			CDebugUIPlugin.log( ce );
		}
	}

	/**
	 * Method createTypeSpecificLabelFieldEditors.
	 * 
	 * @param breakpoint
	 */
	private void createTypeSpecificLabelFieldEditors( ICBreakpoint breakpoint ) {
		if ( breakpoint instanceof ICFunctionBreakpoint ) {
			ICFunctionBreakpoint fbrkpt = (ICFunctionBreakpoint)breakpoint;
			String function = ActionMessages.getString( "CBreakpointPreferencePage.1" ); //$NON-NLS-1$
			try {
				function = fbrkpt.getFunction();
			}
			catch( CoreException e ) {
			}
			catch( NumberFormatException e ) {
			}
			if ( function != null ) {
				addField( createLabelEditor( getFieldEditorParent(), ActionMessages.getString( "CBreakpointPreferencePage.2" ), function ) ); //$NON-NLS-1$
			}
			setTitle( ActionMessages.getString( "CBreakpointPreferencePage.3" ) ); //$NON-NLS-1$
		}
		else if ( breakpoint instanceof ICAddressBreakpoint ) {
			ICAddressBreakpoint abrkpt = (ICAddressBreakpoint)breakpoint;
			String address = ActionMessages.getString( "CBreakpointPreferencePage.4" ); //$NON-NLS-1$
			try {
				address = CDebugUtils.toHexAddressString( Long.parseLong( abrkpt.getAddress() ) );
			}
			catch( CoreException e ) {
			}
			catch( NumberFormatException e ) {
			}
			if ( address != null ) {
				addField( createLabelEditor( getFieldEditorParent(), ActionMessages.getString( "CBreakpointPreferencePage.5" ), address ) ); //$NON-NLS-1$
			}
			setTitle( ActionMessages.getString( "CBreakpointPreferencePage.6" ) ); //$NON-NLS-1$
		}
		else if ( breakpoint instanceof ILineBreakpoint ) {
			String fileName = breakpoint.getMarker().getResource().getLocation().toOSString();
			if ( fileName != null ) {
				addField( createLabelEditor( getFieldEditorParent(), ActionMessages.getString( "CBreakpointPreferencePage.7" ), fileName ) ); //$NON-NLS-1$
			}
			setTitle( ActionMessages.getString( "CBreakpointPreferencePage.8" ) ); //$NON-NLS-1$
			ILineBreakpoint lBreakpoint = (ILineBreakpoint)breakpoint;
			StringBuffer lineNumber = new StringBuffer( 4 );
			try {
				int lNumber = lBreakpoint.getLineNumber();
				if ( lNumber > 0 ) {
					lineNumber.append( lNumber );
				}
			}
			catch( CoreException ce ) {
				CDebugUIPlugin.log( ce );
			}
			if ( lineNumber.length() > 0 ) {
				addField( createLabelEditor( getFieldEditorParent(), ActionMessages.getString( "CBreakpointPreferencePage.9" ), lineNumber.toString() ) ); //$NON-NLS-1$
			}
		}
		else if ( breakpoint instanceof ICWatchpoint ) {
			String projectName = breakpoint.getMarker().getResource().getLocation().toOSString();
			if ( projectName != null ) {
				addField( createLabelEditor( getFieldEditorParent(), ActionMessages.getString( "CBreakpointPreferencePage.10" ), projectName ) ); //$NON-NLS-1$
			}
			ICWatchpoint watchpoint = (ICWatchpoint)breakpoint;
			String title = ""; //$NON-NLS-1$
			String expression = ""; //$NON-NLS-1$
			try {
				if ( watchpoint.isReadType() && !watchpoint.isWriteType() )
					title = ActionMessages.getString( "CBreakpointPreferencePage.11" ); //$NON-NLS-1$
				else if ( !watchpoint.isReadType() && watchpoint.isWriteType() )
					title = ActionMessages.getString( "CBreakpointPreferencePage.12" ); //$NON-NLS-1$
				else
					title = ActionMessages.getString( "CBreakpointPreferencePage.13" ); //$NON-NLS-1$
				expression = watchpoint.getExpression();
			}
			catch( CoreException ce ) {
				CDebugUIPlugin.log( ce );
			}
			setTitle( title );
			addField( createLabelEditor( getFieldEditorParent(), ActionMessages.getString( "CBreakpointPreferencePage.14" ), expression ) ); //$NON-NLS-1$
		}
	}

	protected void createConditionEditor( Composite parent ) {
		fCondition = new BreakpointStringFieldEditor( CBreakpointPreferenceStore.CONDITION, ActionMessages.getString( "CBreakpointPreferencePage.15" ), parent ); //$NON-NLS-1$
		fCondition.setEmptyStringAllowed( true );
		fCondition.setErrorMessage( ActionMessages.getString( "CBreakpointPreferencePage.16" ) ); //$NON-NLS-1$
		addField( fCondition );
	}

	protected void createIgnoreCountEditor( Composite parent ) {
		fIgnoreCount = new BreakpointIntegerFieldEditor( CBreakpointPreferenceStore.IGNORE_COUNT, ActionMessages.getString( "CBreakpointPreferencePage.17" ), parent ); //$NON-NLS-1$
		fIgnoreCount.setValidRange( 0, Integer.MAX_VALUE );
		fIgnoreCountTextControl = fIgnoreCount.getTextControl( parent );
		try {
			fIgnoreCountTextControl.setEnabled( getBreakpoint().getIgnoreCount() >= 0 );
		}
		catch( CoreException ce ) {
			CDebugUIPlugin.log( ce );
		}
		addField( fIgnoreCount );
	}

	protected FieldEditor createLabelEditor( Composite parent, String title, String value ) {
		return new LabelFieldEditor( parent, title, value );
	}

	protected ICBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint( ICBreakpoint breakpoint ) {
		fBreakpoint = breakpoint;
	}
}
