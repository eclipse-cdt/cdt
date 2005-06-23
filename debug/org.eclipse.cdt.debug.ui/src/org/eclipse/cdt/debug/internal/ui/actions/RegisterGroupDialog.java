/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions; 

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.CheckedListDialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IListAdapter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.debug.internal.ui.dialogfields.Separator;
import org.eclipse.cdt.debug.internal.ui.dialogfields.StringDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
 
/**
 * This dialog is used to add/edit user-defined register groups.
 */
public class RegisterGroupDialog extends TitleAreaDialog {

	public class RegisterLabelProvider extends LabelProvider {

		public Image getImage( Object element ) {
			if ( element instanceof IRegisterDescriptor ) {
				return CDebugImages.get( CDebugImages.IMG_OBJS_REGISTER );
			}
			return super.getImage( element );
		}

		public String getText( Object element ) {
			if ( element instanceof IRegisterDescriptor ) {
				IRegisterDescriptor rd = (IRegisterDescriptor)element;
				return MessageFormat.format( "{0} - {1}", new String[] { rd.getName(), rd.getGroupName() } ); //$NON-NLS-1$
			}
			return super.getText( element );
		}
	}

	private StringDialogField fNameField;
	private CheckedListDialogField fListField;
	private String fName;
	private IRegisterDescriptor[] fDescriptors;

	public RegisterGroupDialog( Shell parentShell, IRegisterDescriptor[] allRegisters ) {
		this( parentShell, ActionMessages.getString( "RegisterGroupDialog.0" ), allRegisters, new IRegisterDescriptor[0] ); //$NON-NLS-1$
	}

	public RegisterGroupDialog( Shell parentShell, String groupName, IRegisterDescriptor[] allRegisters, IRegisterDescriptor[] groupRegisters ) {
		super( parentShell );
		fName = groupName;
		fDescriptors = groupRegisters;
		String[] buttonLabels = new String[] { ActionMessages.getString( "RegisterGroupDialog.1" ), ActionMessages.getString( "RegisterGroupDialog.2" ) }; //$NON-NLS-1$ //$NON-NLS-2$
 		fNameField = new StringDialogField();
		fNameField.setLabelText( ActionMessages.getString( "RegisterGroupDialog.3" ) ); //$NON-NLS-1$
		fNameField.setTextWithoutUpdate( groupName );
		fNameField.setDialogFieldListener( new IDialogFieldListener() {
			public void dialogFieldChanged( DialogField field ) {
				update();
			}
		} );
		fListField = new CheckedListDialogField( new IListAdapter() {
			
			public void customButtonPressed( DialogField field, int index ) {
				// TODO Auto-generated method stub
				
			}

			public void selectionChanged( DialogField field ) {
				// TODO Auto-generated method stub
				
			}
		}, buttonLabels, new RegisterLabelProvider() );
		fListField.setLabelText( ActionMessages.getString( "RegisterGroupDialog.4" ) ); //$NON-NLS-1$
		fListField.setCheckAllButtonIndex( 0 );
		fListField.setUncheckAllButtonIndex( 1 );
		fListField.setElements( Arrays.asList( allRegisters ) );
		fListField.setCheckedElements( Arrays.asList( groupRegisters ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea( Composite parent ) {
		getShell().setText( ActionMessages.getString( "RegisterGroupDialog.5" ) ); //$NON-NLS-1$
		setTitle( ActionMessages.getString( "RegisterGroupDialog.6" ) ); //$NON-NLS-1$
	//	setTitleImage( CDebugImages.get( CDebugImages.IMG_WIZBAN_REGISTER_GROUP ) );
		PlatformUI.getWorkbench().getHelpSystem().setHelp( getShell(), ICDebugHelpContextIds.REGISTER_GROUP );
        Composite composite = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout();
		layout.numColumns = Math.max( fNameField.getNumberOfControls(), fListField.getNumberOfControls() );
		layout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
		layout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
		composite.setLayout( layout );
		composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		Dialog.applyDialogFont( composite );
		PixelConverter converter = new PixelConverter( composite );
		new Separator().doFillIntoGrid( composite, layout.numColumns, converter.convertHeightInCharsToPixels( 1 ) );
		fNameField.doFillIntoGrid( composite, layout.numColumns );
		fNameField.getTextControl( null ).selectAll();
		new Separator().doFillIntoGrid( composite, layout.numColumns, converter.convertHeightInCharsToPixels( 1 ) );
		fListField.doFillIntoGrid( composite, layout.numColumns + 1 );
		LayoutUtil.setHorizontalSpan( fListField.getLabelControl( null ), layout.numColumns );
		LayoutUtil.setHeigthHint( fListField.getListControl( null ), convertWidthInCharsToPixels( 30 ) );
		LayoutUtil.setHorizontalGrabbing( fListField.getListControl( null ) );
		setMessage( null );
		return composite;
	}

	protected void update() {
		setErrorMessage( null );
		String name = fNameField.getText().trim();
		if ( name.length() == 0 ) {
			setErrorMessage( ActionMessages.getString( "RegisterGroupDialog.7" ) ); //$NON-NLS-1$
		}
		getButton( IDialogConstants.OK_ID ).setEnabled( name.length() > 0 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		super.okPressed();
		fName = fNameField.getText().trim();
		List elements = fListField.getCheckedElements();
		fDescriptors = (IRegisterDescriptor[])elements.toArray( new IRegisterDescriptor[elements.size()] );
	}

	public String getName() {
		return fName;
	}

	public IRegisterDescriptor[] getDescriptors() {
		return fDescriptors;
	}
}
