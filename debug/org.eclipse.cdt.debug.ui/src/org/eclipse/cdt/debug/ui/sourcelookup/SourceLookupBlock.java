/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.ui.sourcelookup;

import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.DialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.IListAdapter;
import org.eclipse.cdt.debug.internal.ui.dialogfields.LayoutUtil;
import org.eclipse.cdt.debug.internal.ui.dialogfields.ListDialogField;
import org.eclipse.cdt.debug.internal.ui.wizards.AddSourceLocationWizard;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * Enter type comment.
 * 
 * @since Dec 18, 2002
 */
public class SourceLookupBlock
{
	private class SourceLookupAdapter implements IListAdapter
	{
		public void customButtonPressed( DialogField field, int index )
		{
			doButtonPressed( index );
		}

		public void selectionChanged( DialogField field )
		{
			doSelectionChanged();
		}
	}

	private static class SourceLookupLabelProvider extends LabelProvider
	{
		public String getText( Object element )
		{
			if ( element instanceof IProjectSourceLocation )
			{
				return ((IProjectSourceLocation)element).getProject().getName();
			}
			if ( element instanceof IDirectorySourceLocation )
			{
				return ((IDirectorySourceLocation)element).getDirectory().toOSString();
			}
			return null;
		}

		public Image getImage( Object element )
		{
			if ( element instanceof IProjectSourceLocation )
			{
				return CDebugImages.get( CDebugImages.IMG_OBJS_PROJECT );
			}
			if ( element instanceof IDirectorySourceLocation )
			{
				return CDebugImages.get( CDebugImages.IMG_OBJS_FOLDER );
			}
			return null;
		}
	}

	private Composite fControl = null;
	private Shell fShell = null;
	private ListDialogField fSourceListField;

	/**
	 * Constructor for SourceLookupBlock.
	 */
	public SourceLookupBlock()
	{
		String[] buttonLabels = new String[] 
		{
			/* 0 */ "Add...",
			/* 1 */ null,
			/* 2 */ "Up",
			/* 3 */ "Down",
			/* 4 */ null,
			/* 5 */ "Remove",
		};

		SourceLookupAdapter adapter = new SourceLookupAdapter();

		fSourceListField = new ListDialogField( adapter, buttonLabels, new SourceLookupLabelProvider() );
		fSourceListField.setLabelText( "Source Locations" );
		fSourceListField.setUpButtonIndex( 2 );
		fSourceListField.setDownButtonIndex( 3 );
		fSourceListField.setRemoveButtonIndex( 5 );
	}

	public void createControl( Composite parent )
	{
		fShell = parent.getShell();
		fControl = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		fControl.setLayout( layout );
		fControl.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		fControl.setFont( JFaceResources.getDialogFont() );
		
		PixelConverter converter = new PixelConverter( fControl );
		
		fSourceListField.doFillIntoGrid( fControl, 3 );
		LayoutUtil.setHorizontalSpan( fSourceListField.getLabelControl( null ), 2 );
		LayoutUtil.setWidthHint( fSourceListField.getLabelControl( null ), converter.convertWidthInCharsToPixels( 40 ) );
		LayoutUtil.setHorizontalGrabbing( fSourceListField.getListControl( null ) );
	}

	public Control getControl()
	{
		return fControl;
	}
	
	protected void initialize( ICSourceLocation[] locations )
	{
		fSourceListField.removeAllElements();
		for ( int i = 0; i < locations.length; ++i )
		{
			fSourceListField.addElement( locations[i] );
		}
	}

	protected void doButtonPressed( int index )
	{
		switch( index )
		{
			case 0:		// Add...
				addSourceLocation();
				break;
		}
	}
	
	protected void doSelectionChanged()
	{
	}
	
	public ICSourceLocation[] getSourceLocations()
	{
		return (ICSourceLocation[])fSourceListField.getElements().toArray( new ICSourceLocation[fSourceListField.getElements().size()] );
	}
	
	private void addSourceLocation()
	{
		AddSourceLocationWizard wizard = new AddSourceLocationWizard( getSourceLocations() );
		WizardDialog dialog = new WizardDialog( fControl.getShell(), wizard );
		if ( dialog.open() == Window.OK )
		{
			fSourceListField.addElement( wizard.getSourceLocation() );
		}
	}
}
