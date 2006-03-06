/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.preferences; 

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.CheckedListDialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IListAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The "Debugger Types" preference page.
 */
public class DebuggerTypesPage extends PreferencePage implements IWorkbenchPreferencePage {

	protected static String[] fgButtonLabels = new String[] { PreferenceMessages.getString( "DebuggerTypesPage.0" ), PreferenceMessages.getString( "DebuggerTypesPage.1" ), PreferenceMessages.getString( "DebuggerTypesPage.2" ) }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * Comment for DebuggerTypesPage.
	 */
	class DebuggerTypesDialogField extends CheckedListDialogField {

		public DebuggerTypesDialogField() {
			super( new IListAdapter() {
				public void customButtonPressed( DialogField field, int index ) {
				}
				
				public void selectionChanged( DialogField field ) {
				}
			}, fgButtonLabels, new DebuggerTypeLabelProvider() );
		}

		public Control[] doFillIntoGrid( Composite parent, int nColumns ) {
			PixelConverter converter = new PixelConverter( parent );
			assertEnoughColumns( nColumns );
			Control list = getListControl( parent );
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.verticalAlignment = GridData.FILL;
			gd.grabExcessVerticalSpace = true;
			gd.horizontalSpan = nColumns - 2;
			gd.widthHint = converter.convertWidthInCharsToPixels( 50 );
			gd.heightHint = converter.convertHeightInCharsToPixels( 6 );
			list.setLayoutData( gd );
			Composite buttons = getButtonBox( parent );
			gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = false;
			gd.verticalAlignment = GridData.FILL;
			gd.grabExcessVerticalSpace = true;
			gd.horizontalSpan = 1;
			buttons.setLayoutData( gd );
			return new Control[]{ list, buttons };
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.internal.ui.dialogfields.CheckedListDialogField#getManagedButtonState(org.eclipse.jface.viewers.ISelection, int)
		 */
		protected boolean getManagedButtonState( ISelection sel, int index ) {
			// Enable/disable the "Default" button
			if ( index == 2 && sel instanceof IStructuredSelection ) {
				Object o = ((IStructuredSelection)sel).getFirstElement();
				return o != null && isChecked( o );
			}
			return super.getManagedButtonState( sel, index );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.internal.ui.dialogfields.CheckedListDialogField#managedButtonPressed(int)
		 */
		protected boolean managedButtonPressed( int index ) {
			if ( index == 2 ) {
				List list = getSelectedElements();
				if ( !list.isEmpty() )
					setDefault( ((ICDebugConfiguration)list.get( 0 )).getID() );
				else
					setDefault( null );
				refresh();
			}
			return super.managedButtonPressed( index );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.internal.ui.dialogfields.ListDialogField#getListStyle()
		 */
		protected int getListStyle() {
			return SWT.BORDER + SWT.SINGLE + SWT.H_SCROLL + SWT.V_SCROLL;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.internal.ui.dialogfields.CheckedListDialogField#doCheckStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
		 */
		protected void doCheckStateChanged( CheckStateChangedEvent e ) {
			super.doCheckStateChanged( e );
			ICDebugConfiguration dc = (ICDebugConfiguration)e.getElement();
			if ( dc.getID().equals( getDefault() ) && !e.getChecked() ) {
				List list = getCheckedElements();
				setDefault( ( list.size() > 0 ) ? ((ICDebugConfiguration)list.get( 0 )).getID() : null );
				refresh();
			}
			else if ( e.getChecked() && getDefault() == null ) {
				setDefault( ((ICDebugConfiguration)e.getElement()).getID() );
				refresh();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.internal.ui.dialogfields.CheckedListDialogField#checkAll(boolean)
		 */
		public void checkAll( boolean state ) {
			super.checkAll( state );
			List list = getCheckedElements();
			setDefault( ( list.size() > 0 ) ? ((ICDebugConfiguration)list.get( 0 )).getID() : null );
			refresh();
		}
	}

	/**
	 * Comment for DebuggerTypesPage.
	 */
	class DebuggerTypeLabelProvider extends LabelProvider {

		public String getText( Object element ) {
			if ( element instanceof ICDebugConfiguration ) {
				ICDebugConfiguration dc = (ICDebugConfiguration)element;
				String label = dc.getName();
				if ( dc.getID().equals( getDefault() ) )
					label += MessageFormat.format( " ({0})", new String[] { PreferenceMessages.getString( "DebuggerTypesPage.3" ) } ); //$NON-NLS-1$ //$NON-NLS-2$
				return label;
			}
			return super.getText( element );
		}
	}

	private DebuggerTypesDialogField fListField;
	private IWorkbench fWorkbench;
	private String fDefault;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents( Composite parent ) {
		Font font = parent.getFont();
		Composite comp = new Composite( parent, SWT.NONE );
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 3;
		comp.setLayout( topLayout );
		GridData gd = new GridData( GridData.FILL_BOTH );
		comp.setLayoutData( gd );
		comp.setFont( font );
		Label viewerLabel = new Label( comp, SWT.LEFT );
		viewerLabel.setText( PreferenceMessages.getString( "DebuggerTypesPage.4" ) ); //$NON-NLS-1$
		gd = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
		gd.horizontalSpan = 3;
		viewerLabel.setLayoutData( gd );
		viewerLabel.setFont( font );
		fListField = new DebuggerTypesDialogField();
		fListField.setCheckAllButtonIndex( 0 );
		fListField.setUncheckAllButtonIndex( 1 );
		Dialog.applyDialogFont( comp );
		fListField.doFillIntoGrid( comp, 3 );
		initialize();
		getWorkbench().getHelpSystem().setHelp( comp, ICDebugHelpContextIds.DEBUGGER_TYPES_PAGE );
		return comp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init( IWorkbench workbench ) {
		fWorkbench = workbench;
	}

	private IWorkbench getWorkbench() {
		return fWorkbench;
	}

	private void initialize() {
		ICDebugConfiguration dc = CDebugCorePlugin.getDefault().getDefaultDebugConfiguration();
		setDefault( ( dc != null ) ? dc.getID() : null );
		fListField.addElements( Arrays.asList( CDebugCorePlugin.getDefault().getDebugConfigurations() ) );
		fListField.setCheckedElements( Arrays.asList( CDebugCorePlugin.getDefault().getActiveDebugConfigurations() ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		CDebugCorePlugin.getDefault().saveDefaultDebugConfiguration( getDefault() );
		List elements = fListField.getElements();
		elements.removeAll( fListField.getCheckedElements() );
		CDebugCorePlugin.getDefault().saveFilteredDebugConfigurations( (ICDebugConfiguration[])elements.toArray( new ICDebugConfiguration[elements.size()] ) );
		return super.performOk();
	}

	protected String getDefault() {
		return fDefault;
	}

	protected void setDefault( String defaultConfiguration ) {
		fDefault = defaultConfiguration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fListField.setCheckedElements( Arrays.asList( CDebugCorePlugin.getDefault().getDefaultActiveDebugConfigurations() ) );
		ICDebugConfiguration defaultConfiguration = CDebugCorePlugin.getDefault().getDefaultDefaultDebugConfiguration();
		if ( defaultConfiguration != null ) {
			setDefault( defaultConfiguration.getID() );
		}
		else {
			List list = fListField.getCheckedElements();
			if ( !list.isEmpty() ) {
				setDefault( ((ICDebugConfiguration)list.get( 0 )).getID() );
			}
		}
		fListField.refresh();
		super.performDefaults();
	}
}
