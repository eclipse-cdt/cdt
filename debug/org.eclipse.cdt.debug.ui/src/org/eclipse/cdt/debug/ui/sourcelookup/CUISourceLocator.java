/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.ui.sourcelookup;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.model.IStackFrameInfo;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceMode;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CDirectorySourceLocation;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLocator;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceManager;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * A source locator that prompts the user to find source when source cannot
 * be found on the current source lookup path.
 * 
 * @since Sep 24, 2002
 */
public class CUISourceLocator implements IAdaptable
{
	/**
	 * Dialog that prompts for source lookup path.
	 */
	private static class SourceLookupDialog extends Dialog
	{
		public static final int ATTACH_ID = 1000;
		public static final int ATTACH = 1000;

		private String fFileName;
		private boolean fNotAskAgain;
		private Button fAskAgainCheckBox;

		public SourceLookupDialog( Shell shell,
								   String fileName )
		{
			super( shell );
			fFileName = fileName;
			fNotAskAgain = false;
			fAskAgainCheckBox = null;
		}

		public boolean isNotAskAgain()
		{
			return fNotAskAgain;
		}

		protected Control createDialogArea( Composite parent )
		{
			getShell().setText( "Debugger Source Lookup" );

			Composite composite = (Composite)super.createDialogArea( parent );
			composite.setLayout( new GridLayout() );
			Composite group = new Composite( composite, SWT.NONE );
			group.setLayout( new GridLayout() ); 
			createMessageControls( group );
			createControls( group );

			return composite;
		}

		protected void createMessageControls( Composite parent )
		{
			Label message = new Label( parent, SWT.LEFT + SWT.WRAP );
			message.setText( MessageFormat.format( "The source could not be shown as the file ''{0}'' was not found.", new String[] { fFileName }  ) );
			GridData data = new GridData();
			data.widthHint = convertWidthInCharsToPixels( message, 70 );
			message.setLayoutData( data );
		}

		protected void createControls( Composite parent )
		{
			Composite composite = new Composite( parent, SWT.NONE );
			Layout layout = new GridLayout();
			composite.setLayout( layout );
			GridData data = new GridData();
			data.horizontalAlignment = GridData.FILL;
 			data.grabExcessHorizontalSpace = true;
			composite.setLayoutData( data );
			fAskAgainCheckBox = new Button( composite, SWT.CHECK + SWT.WRAP );
			data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			fAskAgainCheckBox.setLayoutData( data );
 			data.grabExcessHorizontalSpace = true;
			fAskAgainCheckBox.setText( "Do &not ask again" );
			fAskAgainCheckBox.addSelectionListener( new SelectionAdapter()
														{
															/* (non-Javadoc)
															 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(SelectionEvent)
															 */
															public void widgetSelected( SelectionEvent e )
															{
																askAgainSelected();
															}
														} );
		}

		protected void askAgainSelected()
		{
			getButton( ATTACH_ID ).setEnabled( !fAskAgainCheckBox.getSelection() );
		}

		/**
		 * @see Dialog#convertWidthInCharsToPixels(FontMetrics, int)
		 */
		protected int convertWidthInCharsToPixels( Control control, int chars )
		{
			GC gc = new GC( control );
			gc.setFont( control.getFont() );
			FontMetrics fontMetrics = gc.getFontMetrics();
			gc.dispose();
			return Dialog.convertWidthInCharsToPixels( fontMetrics, chars );
		}

		protected void okPressed()
		{
			if ( fAskAgainCheckBox != null )
			{
				fNotAskAgain = fAskAgainCheckBox.getSelection();
			}
			super.okPressed();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(Composite)
		 */
		protected void createButtonsForButtonBar( Composite parent )
		{
			super.createButtonsForButtonBar( parent );
			Button button = createButton( parent, ATTACH_ID, "&Attach ...", false );
			button.setToolTipText( "Attach New Source Location" );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
		 */
		protected void buttonPressed( int buttonId )
		{
			super.buttonPressed( buttonId );
			if ( buttonId == ATTACH_ID )
				attachPressed();
		}

		protected void attachPressed()
		{
			setReturnCode( ATTACH );
			close();
		}
	}

	/**
	 * The project being debugged.
	 */
	private IProject fProject = null; 
	
	/**
	 * Underlying source locator.
	 */
	private CSourceManager fSourceLocator;
	
	/**
	 * Whether the user should be prompted for source.
	 * Initially true, until the user checks the 'do not
	 * ask again' box.
	 */
	protected boolean fAllowedToAsk;

	protected boolean fNewLocationAttached;

	/**
	 * Constructor for CUISourceLocator.
	 */
	public CUISourceLocator( IProject project )
	{
		fProject = project;
		fSourceLocator = new CSourceManager( new CSourceLocator( project ) );
		fAllowedToAsk = true;
		fNewLocationAttached = false;
	}

	public Object getSourceElement( IStackFrame stackFrame )
	{
		Object res = fSourceLocator.getSourceElement( stackFrame );
		if ( res == null && fAllowedToAsk )
		{
			IStackFrameInfo frameInfo = (IStackFrameInfo)stackFrame.getAdapter( IStackFrameInfo.class );
			if ( frameInfo != null )
			{
				showDebugSourcePage( frameInfo.getFile() );
				if ( fNewLocationAttached )
				{
					res = fSourceLocator.getSourceElement( stackFrame );
				}
			}
		}
		return res;
	}

	/**
	 * Prompts to locate the source of the given type. 
	 * 
	 * @param frameInfo the frame information for which source
	 *  could not be located
	 */
	private void showDebugSourcePage( final String fileName )
	{
		Runnable prompter = new Runnable()
								{
									public void run()
									{
										SourceLookupDialog dialog = new SourceLookupDialog( CDebugUIPlugin.getActiveWorkbenchShell(), fileName );
										if ( dialog.open() == SourceLookupDialog.ATTACH )
										{
											attachSourceLocation( fileName );
										}
										fAllowedToAsk = !dialog.isNotAskAgain();
									}
								};
		CDebugUIPlugin.getStandardDisplay().syncExec( prompter );
	}

	protected void attachSourceLocation( String fileName )
	{
		AttachSourceLocationDialog dialog = new AttachSourceLocationDialog( CDebugUIPlugin.getActiveWorkbenchShell() );
		Path path = new Path( fileName );
		if ( path.isAbsolute() )
		{
			dialog.setInitialPath( path.removeLastSegments( 1 ) );
		}
		if ( dialog.open() == Dialog.OK )
		{
			if ( dialog.getLocation() != null )
			{
				fSourceLocator.addSourceLocation( new CDirectorySourceLocation( dialog.getLocation(), dialog.getAssociation() ) );
				fNewLocationAttached = true;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( fSourceLocator != null )
		{
			if ( adapter.equals( ICSourceLocator.class ) )
				return fSourceLocator;
			if ( adapter.equals( ISourceMode.class ) )
				return fSourceLocator;
		}
		return null;
	}
}
