/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.IListAdapter;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.ListDialogField;
import org.eclipse.cdt.debug.mi.ui.IMILaunchConfigurationComponent;
import org.eclipse.cdt.debug.mi.ui.IPathProvider;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The UI component to access the shared libraries search path.
 */
public class SolibSearchPathBlock extends Observable implements IMILaunchConfigurationComponent {

	class AddDirectoryDialog extends Dialog {

		protected Text fText;
		
		private Button fBrowseButton;

		private String fValue;

		/** 
		 * Constructor for AddDirectoryDialog. 
		 */
		public AddDirectoryDialog( Shell parentShell ) {
			super( parentShell );
		}

		protected Control createDialogArea( Composite parent ) {
			Composite composite = (Composite)super.createDialogArea( parent );

			Composite subComp = ControlFactory.createCompositeEx( composite, 2, GridData.FILL_HORIZONTAL );
			((GridLayout)subComp.getLayout()).makeColumnsEqualWidth = false;
			GridData data = new GridData( GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER );
			data.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
			subComp.setLayoutData( data );
			subComp.setFont( parent.getFont() );

			fText = new Text( subComp, SWT.SINGLE | SWT.BORDER );
			fText.setLayoutData( new GridData( GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL ) );
			fText.addModifyListener( new ModifyListener() {

				public void modifyText( ModifyEvent e ) {
					updateOKButton();
				}
			} );

			fBrowseButton = ControlFactory.createPushButton( subComp, MIUIMessages.getString( "GDBServerDebuggerPage.7" ) ); //$NON-NLS-1$
			data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			data.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.BUTTON_WIDTH );
			fBrowseButton.setLayoutData( data );
			fBrowseButton.addSelectionListener( new SelectionAdapter() {

				public void widgetSelected( SelectionEvent evt ) {
					DirectoryDialog dialog = new DirectoryDialog( getShell() );
					dialog.setMessage( MIUIMessages.getString( "SolibSearchPathBlock.5" ) ); //$NON-NLS-1$
					String res = dialog.open();
					if ( res != null ) {
						fText.setText( res );
					}
				}
			} );

			applyDialogFont( composite );
			return composite;
		}

		protected void configureShell( Shell newShell ) {
			super.configureShell( newShell );
			newShell.setText( MIUIMessages.getString( "SolibSearchPathBlock.Add_Directory" ) ); //$NON-NLS-1$
		}

		public String getValue() {
			return fValue;
		}

		private void setValue( String value ) {
			fValue = value;
		}

		protected void buttonPressed( int buttonId ) {
			if ( buttonId == IDialogConstants.OK_ID ) {
				setValue( fText.getText() );
			}
			else {
				setValue( null );
			}
			super.buttonPressed( buttonId );
		}

		protected void updateOKButton() {
			Button okButton = getButton( IDialogConstants.OK_ID );
			String text = fText.getText();
			okButton.setEnabled( isValid( text ) );
		}

		protected boolean isValid( String text ) {
			return ( text.trim().length() > 0 );
		}

		protected Control createButtonBar( Composite parent ) {
			Control control = super.createButtonBar( parent );
			updateOKButton();
			return control;
		}
	}

	private Composite fControl;

	public class SolibSearchPathListDialogField extends ListDialogField {

		public SolibSearchPathListDialogField( IListAdapter adapter, String[] buttonLabels, ILabelProvider lprovider ) {
			super( adapter, buttonLabels, lprovider );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.internal.ui.dialogfields.ListDialogField#managedButtonPressed(int)
		 */
		protected boolean managedButtonPressed( int index ) {
			boolean result = super.managedButtonPressed( index );
			if ( result )
				buttonPressed( index );
			return result;
		}
	}

	private Shell fShell;

	private SolibSearchPathListDialogField fDirList;
	
	private IPathProvider fPathProvider;

	public SolibSearchPathBlock( IPathProvider pathProvider ) {
		super();
		setPathProvider( pathProvider );
		int length = ( getPathProvider() != null ) ? 6 : 4;
		String[] buttonLabels = new String[length];
		buttonLabels[0] = MIUIMessages.getString( "SolibSearchPathBlock.0" ); //$NON-NLS-1$
		buttonLabels[1] = MIUIMessages.getString( "SolibSearchPathBlock.1" ); //$NON-NLS-1$
		buttonLabels[2] = MIUIMessages.getString( "SolibSearchPathBlock.2" ); //$NON-NLS-1$
		buttonLabels[3] = MIUIMessages.getString( "SolibSearchPathBlock.3" ); //$NON-NLS-1$
		if ( buttonLabels.length == 6 ) {
			buttonLabels[4] = null;
			buttonLabels[5] = MIUIMessages.getString( "SolibSearchPathBlock.Auto" ); //$NON-NLS-1$
		}
		IListAdapter listAdapter = new IListAdapter() {

			public void customButtonPressed( DialogField field, int index ) {
				buttonPressed( index );
			}

			public void selectionChanged( DialogField field ) {
			}
		};
		fDirList = new SolibSearchPathListDialogField( listAdapter, buttonLabels, new LabelProvider() );
		fDirList.setLabelText( MIUIMessages.getString( "SolibSearchPathBlock.4" ) ); //$NON-NLS-1$
		fDirList.setUpButtonIndex( 1 );
		fDirList.setDownButtonIndex( 2 );
		fDirList.setRemoveButtonIndex( 3 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl( Composite parent ) {
		fShell = parent.getShell();
		Composite comp = ControlFactory.createCompositeEx( parent, 2, GridData.FILL_BOTH );
		((GridLayout)comp.getLayout()).makeColumnsEqualWidth = false;
		((GridLayout)comp.getLayout()).marginHeight = 0;
		((GridLayout)comp.getLayout()).marginWidth = 0;
		comp.setFont( JFaceResources.getDialogFont() );
		PixelConverter converter = new PixelConverter( comp );
		fDirList.doFillIntoGrid( comp, 3 );
		LayoutUtil.setHorizontalSpan( fDirList.getLabelControl( null ), 2 );
		LayoutUtil.setWidthHint( fDirList.getLabelControl( null ), converter.convertWidthInCharsToPixels( 30 ) );
		LayoutUtil.setHorizontalGrabbing( fDirList.getListControl( null ) );
		fControl = comp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom( ILaunchConfiguration configuration ) {
		if ( fDirList != null ) {
			try {
				fDirList.addElements( configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, Collections.EMPTY_LIST ) );
			}
			catch( CoreException e ) {
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults( ILaunchConfigurationWorkingCopy configuration ) {
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, Collections.EMPTY_LIST );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply( ILaunchConfigurationWorkingCopy configuration ) {
		if ( fDirList != null ) {
			configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, fDirList.getElements() );
		}
	}

	protected void buttonPressed( int index ) {
		switch( index ) {
			case 0:
				addDirectory();
				break;
			case 5:
				generatePaths();
				break;
		}
		setChanged();
		notifyObservers();
	}

	protected Shell getShell() {
		return fShell;
	}

	private void addDirectory() {
		AddDirectoryDialog dialog = new AddDirectoryDialog( getShell() );
		dialog.open();
		String result = dialog.getValue();
		if ( result != null && !contains( result ) ) {
			fDirList.addElement( result.trim() );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#dispose()
	 */
	public void dispose() {
		deleteObservers();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#getControl()
	 */
	public Control getControl() {
		return fControl;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.IMILaunchConfigurationComponent#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid( ILaunchConfiguration launchConfig ) {
		// TODO Auto-generated method stub
		return false;
	}

	private void generatePaths() {
		IPathProvider pp = getPathProvider();
		if ( pp != null ) {
			IPath[] dirs = pp.getPaths();
			for ( int i = 0; i < dirs.length; ++i )
				if ( !contains( dirs[i] ) )
					fDirList.addElement( dirs[i].toOSString() );
		}
	}

	private IPathProvider getPathProvider() {
		return fPathProvider;
	}

	private void setPathProvider( IPathProvider pathProvider ) {
		fPathProvider = pathProvider;
	}

	private boolean contains( IPath path ) {
		List list = fDirList.getElements();
		Iterator it = list.iterator();
		while( it.hasNext() ) {
			IPath p = new Path( (String)it.next() );
			if ( p.toFile().compareTo( path.toFile() ) == 0 )
				return true;
		}
		return false;
	}

	private boolean contains( String dir ) {
		IPath path = new Path( dir );
		List list = fDirList.getElements();
		Iterator it = list.iterator();
		while( it.hasNext() ) {
			IPath p = new Path( (String)it.next() );
			if ( p.toFile().compareTo( path.toFile() ) == 0 )
				return true;
		}
		return false;
	}
}