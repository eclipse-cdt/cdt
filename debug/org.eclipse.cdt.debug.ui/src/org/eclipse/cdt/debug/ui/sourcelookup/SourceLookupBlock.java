/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.ui.sourcelookup;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
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
	private Composite fControl = null;
	private Shell fShell = null;
	private FontMetrics fFontMetrics = null;

	/**
	 * Constructor for SourceLookupBlock.
	 */
	public SourceLookupBlock()
	{
		super();
	}

	public void createControl( Composite parent )
	{
		fShell = parent.getShell();
		fControl = new Composite( parent, SWT.NONE );
		fControl.setLayout( new GridLayout( 2, false ) );
		fControl.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		fControl.setFont( JFaceResources.getDialogFont() );
		createListControl( fControl );
		createButtonBar( fControl );	
	}

	public Control getControl()
	{
		return fControl;
	}

	protected void initializeDialogUnits( Control control )
	{
		// Compute and store a font metric
		GC gc = new GC( control );
		gc.setFont( control.getFont() );
		fFontMetrics = gc.getFontMetrics();
		gc.dispose();
	}

	protected int convertVerticalDLUsToPixels( int dlus )
	{
		if ( fFontMetrics == null )
			return 0;
		return Dialog.convertVerticalDLUsToPixels( fFontMetrics, dlus );
	}

	protected int convertHorizontalDLUsToPixels( int dlus )
	{
		if ( fFontMetrics == null )
			return 0;
		return Dialog.convertHorizontalDLUsToPixels( fFontMetrics, dlus );
	}
	
	protected void createListControl( Composite parent )
	{
	}

	protected void createButtonBar( Composite parent )
	{
	}
}
