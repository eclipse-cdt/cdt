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
import java.util.Observable;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.IListAdapter;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.ListDialogField;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Enter type comment.
 * 
 * @since Sep 4, 2003
 */
public class SolibSearchPathBlock extends Observable
{
	public class SolibSearchPathListDialogField extends ListDialogField
	{
		public SolibSearchPathListDialogField( IListAdapter adapter, String[] buttonLabels, ILabelProvider lprovider )
		{
			super( adapter, buttonLabels, lprovider );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.internal.ui.dialogfields.ListDialogField#managedButtonPressed(int)
		 */
		protected boolean managedButtonPressed( int index )
		{
			boolean result = super.managedButtonPressed( index );
			if ( result )
				buttonPressed( index );
			return result;
		}

	}

	private Shell fShell;
	private SolibSearchPathListDialogField fDirList;
	
	public SolibSearchPathBlock()
	{
		super();

		String[] buttonLabels = new String[] 
		{
			/* 0 */ MIUIMessages.getString( "SolibSearchPathBlock.0" ), //$NON-NLS-1$
			/* 1 */ null,
			/* 2 */ MIUIMessages.getString( "SolibSearchPathBlock.1" ), //$NON-NLS-1$
			/* 3 */ MIUIMessages.getString( "SolibSearchPathBlock.2" ), //$NON-NLS-1$
			/* 4 */ null,
			/* 5 */ MIUIMessages.getString( "SolibSearchPathBlock.3" ), //$NON-NLS-1$
		};

		IListAdapter listAdapter = new IListAdapter()
										{
											public void customButtonPressed( DialogField field, int index )
											{
												buttonPressed( index );
											}

											public void selectionChanged( DialogField field )
											{
											}
										};

		fDirList = new SolibSearchPathListDialogField( listAdapter, buttonLabels, new LabelProvider() );
		fDirList.setLabelText( MIUIMessages.getString( "SolibSearchPathBlock.4" ) ); //$NON-NLS-1$
		fDirList.setUpButtonIndex( 2 );
		fDirList.setDownButtonIndex( 3 );
		fDirList.setRemoveButtonIndex( 5 );
	}

	public void createBlock( Composite parent )
	{
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
	}

	public void initializeFrom( ILaunchConfiguration configuration )
	{
		if ( fDirList != null )
		{
			try
			{
				fDirList.addElements( configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, Collections.EMPTY_LIST ) );
			}
			catch( CoreException e )
			{
			}
		}
	}

	public void setDefaults( ILaunchConfigurationWorkingCopy configuration )
	{
		configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, Collections.EMPTY_LIST );
	}

	public void performApply( ILaunchConfigurationWorkingCopy configuration )
	{
		if ( fDirList != null )
		{
			configuration.setAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, fDirList.getElements() );
		}
	}

	protected void buttonPressed( int index )
	{
		if ( index == 0 )
			addDirectory();
		setChanged();
		notifyObservers();
	}

	protected Shell getShell()
	{
		return fShell;
	}

	private void addDirectory()
	{
		DirectoryDialog dialog = new DirectoryDialog( getShell() );
		dialog.setMessage( MIUIMessages.getString( "SolibSearchPathBlock.5" ) ); //$NON-NLS-1$
		String res = dialog.open();
		if ( res != null ) 
			fDirList.addElement( res );
	}

	public void dispose()
	{
		deleteObservers();
	}
}

