/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ICastToArray;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Enter type comment.
 * 
 * @since Mar 10, 2003
 */
public class CastToArrayActionDelegate extends ActionDelegate implements IObjectActionDelegate
{
	protected class CastToArrayDialog extends Dialog
	{
		private String fType = "";
		private int fFirstIndex = 0;
		private int fLastIndex = 0;

		private Button fOkButton;
		private Label fErrorMessageLabel;

		private Text fTypeText;
		private Text fFirstIndexText;
		private Text fLastIndexText;

		public CastToArrayDialog( Shell parentShell, String initialType, int initialStart, int initialEnd )
		{
			super( parentShell );
			fType = ( initialType == null ) ? "" : initialType;
			fFirstIndex = initialStart;
			fLastIndex = initialEnd;
		}

		protected String getType()
		{
			return fType;
		}

		protected int getFirstIndex()
		{
			return fFirstIndex;
		}

		protected int getLastIndex()
		{
			return fLastIndex;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		protected void configureShell( Shell newShell )
		{
			super.configureShell( newShell );
			newShell.setText( "Display As Array" );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
		 */
		protected void createButtonsForButtonBar( Composite parent )
		{
			// create OK and Cancel buttons by default
			fOkButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
			createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

			//do this here because setting the text will set enablement on the ok button
			fTypeText.setFocus();
			if ( fType != null ) 
			{
				fTypeText.setText( fType );
				fTypeText.selectAll();
				fFirstIndexText.setText( String.valueOf( fFirstIndex ) );
				fLastIndexText.setText( String.valueOf( fLastIndex ) );
			}
		}

		protected Label getErrorMessageLabel()
		{
			return fErrorMessageLabel;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea( Composite parent )
		{
			Composite composite = (Composite)super.createDialogArea( parent );

			createDialogFields( composite );

			fErrorMessageLabel = new Label( composite, SWT.NONE );
			fErrorMessageLabel.setLayoutData( new GridData( GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL ) );
			fErrorMessageLabel.setFont(parent.getFont());
			return composite;
		}

		private void createDialogFields( Composite parent )
		{
			Composite composite = ControlFactory.createComposite( parent, 4 );
			((GridData)composite.getLayoutData()).widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
			((GridLayout)composite.getLayout()).makeColumnsEqualWidth = true;
			
			ControlFactory.createLabel( composite, "Type:" );
			
			fTypeText = ControlFactory.createTextField( composite );
			GridData data = new GridData( GridData.FILL_HORIZONTAL );
			data.horizontalSpan = 3;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			fTypeText.setLayoutData( data );
			fTypeText.addModifyListener( 
								new ModifyListener()
									{
										public void modifyText( ModifyEvent e )
										{
											validateInput();
										}
									} );

			Label label = ControlFactory.createLabel( composite, "First index:" );
			((GridData)label.getLayoutData()).horizontalSpan = 3;
			fFirstIndexText = ControlFactory.createTextField( composite );
			fFirstIndexText.addModifyListener(
								new ModifyListener()
									{
										public void modifyText( ModifyEvent e )
										{
											validateInput();
										}
									} );

			label = ControlFactory.createLabel( composite, "Last index:" );
			((GridData)label.getLayoutData()).horizontalSpan = 3;
			fLastIndexText = ControlFactory.createTextField( composite );
			fLastIndexText.addModifyListener(
								new ModifyListener()
									{
										public void modifyText( ModifyEvent e )
										{
											validateInput();
										}
									} );
		}

		protected void validateInput()
		{
			boolean enabled = true;
			String message = "";
			if ( fTypeText.getText().trim().length() == 0 )
			{
				message = "The 'Type' field must not be empty.";
				enabled = false;
			}
			else
			{
				String firstIndex = fFirstIndexText.getText().trim();
				if ( firstIndex.length() == 0 )
				{
					message = "The 'First index' field must not be empty.";
					enabled = false;
				}
				else
				{
					int first = -1;
					try
					{
						first = Integer.parseInt( firstIndex );
					}
					catch( NumberFormatException e )
					{
					}
					if ( first < 0 )
					{
						message = "Invalid first index.";
						enabled = false;
					}
					else
					{
						String lastIndex = fLastIndexText.getText().trim();
						if ( lastIndex.length() == 0 )
						{
							message = "The 'Last index' field must not be empty.";
							enabled = false;
						}
						else
						{
							int last = -1;
							try
							{
								last = Integer.parseInt( lastIndex );
							}
							catch( NumberFormatException e )
							{
							}
							if ( last < 0 )
							{
								message = "Invalid last index.";
								enabled = false;
							}
							else if ( last < first )
							{
								message = "The first index must not be greater than the last index.";
								enabled = false;
							}
						}
					}
				}
			}
			fOkButton.setEnabled( enabled );
			getErrorMessageLabel().setText( message );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
		 */
		protected void buttonPressed( int buttonId )
		{
			if ( buttonId == IDialogConstants.OK_ID )
			{
				fType = fTypeText.getText().trim();
				String firstIndex = fFirstIndexText.getText().trim();
				String lastIndex = fLastIndexText.getText().trim();
				try
				{
					fFirstIndex = Integer.parseInt( firstIndex );
					fLastIndex = Integer.parseInt( lastIndex );
				}
				catch( NumberFormatException e )
				{
					fFirstIndex = 0;
					fLastIndex = 0;
				}
			}
			else
			{
				fType = null;
			}
			super.buttonPressed( buttonId );
		}
	}

	private ICastToArray fCastToArray = null;
	private IStatus fStatus = null;

	public CastToArrayActionDelegate()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action )
	{
		if ( getCastToArray() == null )
			return;
		BusyIndicator.showWhile( Display.getCurrent(), 
								 new Runnable() 
									 {
										 public void run() 
										 {
											 try 
											 {
												 doAction( getCastToArray() );
												 setStatus( null );
											 } 
											 catch( DebugException e ) 
											 {
												setStatus( e.getStatus() );
											 }
										 }
									 } );
		if ( getStatus() != null && !getStatus().isOK() ) 
		{
			IWorkbenchWindow window= CDebugUIPlugin.getActiveWorkbenchWindow();
			if ( window != null ) 
			{
				CDebugUIPlugin.errorDialog( "Unable to display this variable as an array.", getStatus() );
			} 
			else 
			{
				CDebugUIPlugin.log( getStatus() );
			}
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		if ( selection instanceof IStructuredSelection )
		{
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if ( element instanceof ICastToArray )
			{
				boolean enabled = ((ICastToArray)element).supportsCastToArray();
				action.setEnabled( enabled );
				if ( enabled )
				{
					setCastToArray( (ICastToArray)element );
					return;
				}
			}
		}
		action.setEnabled( false );
		setCastToArray( null );
	}

	protected ICastToArray getCastToArray()
	{
		return fCastToArray;
	}

	protected void setCastToArray( ICastToArray castToArray )
	{
		fCastToArray = castToArray;
	}

	public IStatus getStatus()
	{
		return fStatus;
	}

	public void setStatus( IStatus status )
	{
		fStatus = status;
	}

	protected void doAction( ICastToArray castToArray ) throws DebugException
	{
		String currentType = castToArray.getCurrentType().trim();
		CastToArrayDialog dialog = new CastToArrayDialog( CDebugUIPlugin.getActiveWorkbenchShell(), currentType, 0, 0 );
		if ( dialog.open() == Window.OK )
		{
			String newType = dialog.getType().trim();
			int firstIndex = dialog.getFirstIndex();
			int lastIndex = dialog.getLastIndex();
			castToArray.castToArray( newType, firstIndex, lastIndex );
		}
	}
}
