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

import org.eclipse.cdt.debug.core.model.ICastToArray;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
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
 * The delegate of the "Display As Array" action.
 */
public class CastToArrayActionDelegate extends ActionDelegate implements IObjectActionDelegate {

	protected class CastToArrayDialog extends Dialog {

		private String fType = ""; //$NON-NLS-1$

		private int fFirstIndex = 0;

		private int fLength = 0;

		private Button fOkButton;

		private Label fErrorMessageLabel;

		private Text fFirstIndexText;

		private Text fLengthText;

		public CastToArrayDialog( Shell parentShell, String initialType, int initialStart, int initialLength ) {
			super( parentShell );
			fType = (initialType == null) ? "" : initialType; //$NON-NLS-1$
			fFirstIndex = initialStart;
			fLength = initialLength;
		}

		protected String getType() {
			return fType;
		}

		protected int getFirstIndex() {
			return fFirstIndex;
		}

		protected int getLength() {
			return fLength;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		protected void configureShell( Shell newShell ) {
			super.configureShell( newShell );
			newShell.setText( ActionMessages.getString( "CastToArrayActionDelegate.0" ) ); //$NON-NLS-1$
			newShell.setImage( CDebugImages.get( CDebugImages.IMG_LCL_DISPLAY_AS_ARRAY ) );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
		 */
		protected void createButtonsForButtonBar( Composite parent ) {
			// create OK and Cancel buttons by default
			fOkButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
			createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
			//do this here because setting the text will set enablement on the ok button
			/*
			 * fTypeText.setFocus(); if ( fType != null ) { fTypeText.setText( fType ); fTypeText.selectAll(); fFirstIndexText.setText( String.valueOf(
			 * fFirstIndex ) ); fLengthText.setText( String.valueOf( fLength ) ); }
			 */
			fFirstIndexText.setText( String.valueOf( fFirstIndex ) );
			fLengthText.setText( String.valueOf( fLength ) );
		}

		protected Label getErrorMessageLabel() {
			return fErrorMessageLabel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea( Composite parent ) {
			Composite composite = (Composite)super.createDialogArea( parent );
			createDialogFields( composite );
			fErrorMessageLabel = new Label( composite, SWT.NONE );
			fErrorMessageLabel.setLayoutData( new GridData( GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL ) );
			fErrorMessageLabel.setFont( parent.getFont() );
			return composite;
		}

		private void createDialogFields( Composite parent ) {
			Composite composite = ControlFactory.createComposite( parent, 4 );
			((GridData)composite.getLayoutData()).widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
			((GridLayout)composite.getLayout()).makeColumnsEqualWidth = true;
			Label label = ControlFactory.createLabel( composite, ActionMessages.getString( "CastToArrayActionDelegate.1" ) ); //$NON-NLS-1$
			((GridData)label.getLayoutData()).horizontalSpan = 3;
			fFirstIndexText = ControlFactory.createTextField( composite );
			fFirstIndexText.addModifyListener( new ModifyListener() {

				public void modifyText( ModifyEvent e ) {
					validateInput();
				}
			} );
			label = ControlFactory.createLabel( composite, ActionMessages.getString( "CastToArrayActionDelegate.2" ) ); //$NON-NLS-1$
			((GridData)label.getLayoutData()).horizontalSpan = 3;
			fLengthText = ControlFactory.createTextField( composite );
			fLengthText.addModifyListener( new ModifyListener() {

				public void modifyText( ModifyEvent e ) {
					validateInput();
				}
			} );
		}

		protected void validateInput() {
			boolean enabled = true;
			String message = ""; //$NON-NLS-1$
			String firstIndex = fFirstIndexText.getText().trim();
			if ( firstIndex.length() == 0 ) {
				message = ActionMessages.getString( "CastToArrayActionDelegate.3" ); //$NON-NLS-1$
				enabled = false;
			}
			else {
				try {
					Integer.parseInt( firstIndex );
				}
				catch( NumberFormatException e ) {
					message = ActionMessages.getString( "CastToArrayActionDelegate.4" ); //$NON-NLS-1$
					enabled = false;
				}
				if ( enabled ) {
					String lengthText = fLengthText.getText().trim();
					if ( lengthText.length() == 0 ) {
						message = ActionMessages.getString( "CastToArrayActionDelegate.5" ); //$NON-NLS-1$
						enabled = false;
					}
					else {
						int length = -1;
						try {
							length = Integer.parseInt( lengthText );
						}
						catch( NumberFormatException e ) {
							message = ActionMessages.getString( "CastToArrayActionDelegate.6" ); //$NON-NLS-1$
							enabled = false;
						}
						if ( enabled && length < 1 ) {
							message = ActionMessages.getString( "CastToArrayActionDelegate.7" ); //$NON-NLS-1$
							enabled = false;
						}
					}
				}
			}
			fOkButton.setEnabled( enabled );
			getErrorMessageLabel().setText( message );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
		 */
		protected void buttonPressed( int buttonId ) {
			if ( buttonId == IDialogConstants.OK_ID ) {
				String firstIndex = fFirstIndexText.getText().trim();
				String lengthText = fLengthText.getText().trim();
				try {
					fFirstIndex = Integer.parseInt( firstIndex );
					fLength = Integer.parseInt( lengthText );
				}
				catch( NumberFormatException e ) {
					fFirstIndex = 0;
					fLength = 0;
				}
			}
			else {
				fType = null;
			}
			super.buttonPressed( buttonId );
		}
	}

	private ICastToArray fCastToArray = null;

	private IStatus fStatus = null;

	private IWorkbenchPart fTargetPart = null;

	public CastToArrayActionDelegate() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
		fTargetPart = targetPart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		if ( getCastToArray() == null )
			return;
		BusyIndicator.showWhile( Display.getCurrent(), new Runnable() {

			public void run() {
				try {
					doAction( getCastToArray() );
					setStatus( null );
				}
				catch( DebugException e ) {
					setStatus( e.getStatus() );
				}
			}
		} );
		if ( getStatus() != null && !getStatus().isOK() ) {
			IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
			if ( window != null ) {
				CDebugUIPlugin.errorDialog( ActionMessages.getString( "CastToArrayActionDelegate.8" ), getStatus() ); //$NON-NLS-1$
			}
			else {
				CDebugUIPlugin.log( getStatus() );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		if ( selection instanceof IStructuredSelection ) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if ( element instanceof ICastToArray ) {
				boolean enabled = ((ICastToArray)element).supportsCastToArray();
				action.setEnabled( enabled );
				if ( enabled ) {
					setCastToArray( (ICastToArray)element );
					return;
				}
			}
		}
		action.setEnabled( false );
		setCastToArray( null );
	}

	protected ICastToArray getCastToArray() {
		return fCastToArray;
	}

	protected void setCastToArray( ICastToArray castToArray ) {
		fCastToArray = castToArray;
	}

	public IStatus getStatus() {
		return fStatus;
	}

	public void setStatus( IStatus status ) {
		fStatus = status;
	}

	protected void doAction( ICastToArray castToArray ) throws DebugException {
		String currentType = castToArray.getCurrentType().trim();
		CastToArrayDialog dialog = new CastToArrayDialog( CDebugUIPlugin.getActiveWorkbenchShell(), currentType, 0, 1 );
		if ( dialog.open() == Window.OK ) {
			int firstIndex = dialog.getFirstIndex();
			int lastIndex = dialog.getLength();
			castToArray.castToArray( firstIndex, lastIndex );
			if ( getSelectionProvider() != null )
				getSelectionProvider().setSelection( new StructuredSelection( castToArray ) );
		}
	}

	private ISelectionProvider getSelectionProvider() {
		return (fTargetPart instanceof IDebugView) ? ((IDebugView)fTargetPart).getViewer() : null;
	}
}
