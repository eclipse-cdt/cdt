/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.Iterator;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValueModification;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 16, 2002
 */
public class ChangeRegisterValueAction extends SelectionProviderAction
{
	protected Tree fTree;
	protected TreeEditor fTreeEditor;
	protected Composite fComposite;
	protected Label fEditorLabel;
	protected Text fEditorText;
	protected IVariable fVariable;
	protected boolean fKeyReleased = false;

	/**
	 * Constructor for ChangeRegisterValueAction.
	 * @param provider
	 * @param text
	 */
	public ChangeRegisterValueAction( Viewer viewer )
	{
		super( viewer, "Change Register Value" );
		setDescription( "Change Register Value" );
		CDebugImages.setLocalImageDescriptors( this, CDebugImages.IMG_LCL_CHANGE_REGISTER_VALUE );
		fTree = ((TreeViewer)viewer).getTree();
		fTreeEditor = new TreeEditor( fTree );
		WorkbenchHelp.setHelp( this, ICDebugHelpContextIds.CHANGE_REGISTER_VALUE_ACTION );
	}

	/**
	 * Edit the variable value with an inline text editor.  
	 */
	protected void doActionPerformed( final IVariable variable )
	{
		IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
		if ( window == null )
		{
			return;
		}
		final Shell activeShell = window.getShell();

		// If a previous edit is still in progress, finish it
		if ( fEditorText != null )
		{
			saveChangesAndCleanup( fVariable, activeShell );
		}
		fVariable = variable;

		// Use a Composite containing a Label and a Text.  This allows us to edit just
		// the value, while still showing the variable name.
		fComposite = new Composite( fTree, SWT.NONE );
		fComposite.setBackground( fTree.getBackground() );
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		fComposite.setLayout( layout );

		fEditorLabel = new Label( fComposite, SWT.LEFT );
		fEditorLabel.setLayoutData( new GridData( GridData.FILL_VERTICAL ) );

		// Fix for bug 1766.  Border behavior on Windows & Linux for text
		// fields is different.  On Linux, you always get a border, on Windows,
		// you don't.  Specifying a border on Linux results in the characters
		// getting pushed down so that only there very tops are visible.  Thus,
		// we have to specify different style constants for the different platforms.
		int textStyles = SWT.SINGLE | SWT.LEFT;
		if ( SWT.getPlatform().equals( "win32" ) )
		{ //$NON-NLS-1$
			textStyles |= SWT.BORDER;
		}
		fEditorText = new Text( fComposite, textStyles );
		fEditorText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL ) );
		String valueString = ""; //$NON-NLS-1$
		try
		{
			valueString = fVariable.getValue().getValueString();
		}
		catch( DebugException de )
		{
			CDebugUIPlugin.errorDialog( "Setting the register value failed.", de );
		}
		TreeItem[] selectedItems = fTree.getSelection();
		fTreeEditor.horizontalAlignment = SWT.LEFT;
		fTreeEditor.grabHorizontal = true;
		fTreeEditor.setEditor( fComposite, selectedItems[0] );

		// There is no API on the model presentation to get just the variable name, 
		// so we have to make do with just calling IVariable.getName()
		String varName = ""; //$NON-NLS-1$
		try
		{
			varName = fVariable.getName();
		}
		catch (DebugException de)
		{
			CDebugUIPlugin.errorDialog( "Setting the register value failed.", de );
		}
		fEditorLabel.setText( varName + "=" ); //$NON-NLS-1$

		fEditorText.setText( valueString );
		fEditorText.selectAll();

		fComposite.layout( true );
		fComposite.setVisible( true );
		fEditorText.setFocus();

		// CR means commit the changes, ESC means abort changing the value
		fEditorText.addKeyListener( 
						new KeyAdapter()
							{
								public void keyReleased( KeyEvent event )
								{
									if ( event.character == SWT.CR )
									{
										if ( fKeyReleased )
										{
											saveChangesAndCleanup( fVariable, activeShell );
										}
										else
										{
											cleanup();
											return;
										}
									}
									if ( event.character == SWT.ESC )
									{
										cleanup();
										return;
									}
									fKeyReleased = true;
								}
							} );

		// If the focus is lost, then act as if user hit CR and commit changes
		fEditorText.addFocusListener(
						new FocusAdapter()
							{
								public void focusLost( FocusEvent fe )
								{
									if ( fKeyReleased )
									{
										saveChangesAndCleanup( fVariable, activeShell );
									}
									else
									{
										cleanup();
									}
								}
							} );
	}

	/** 
	 * If the new value validates, save it, and dispose the text widget, 
	 * otherwise sound the system bell and leave the user in the editor.
	 */
	protected void saveChangesAndCleanup( IVariable variable, Shell shell )
	{
		String newValue = fEditorText.getText();
		try
		{
			if ( !variable.verifyValue( newValue ) )
			{
				shell.getDisplay().beep();
				return;
			}
			variable.setValue( newValue );
		}
		catch( DebugException de )
		{
			cleanup();
			CDebugUIPlugin.errorDialog( "Setting the register value failed.", de );
			return;
		}
		cleanup();
	}

	/**
	 * Tidy up the widgets that were used
	 */
	protected void cleanup()
	{
		fKeyReleased = false;
		if ( fEditorText != null )
		{
			fEditorText.dispose();
			fEditorText = null;
			fVariable = null;
			fTreeEditor.setEditor( null, null );
			fComposite.setVisible( false );
		}
	}

	/**
	 * Updates the enabled state of this action based
	 * on the selection
	 */
	protected void update( IStructuredSelection sel )
	{
		Iterator iter = sel.iterator();
		if (iter.hasNext())
		{
			Object object = iter.next();
			if ( object instanceof IValueModification )
			{
				IValueModification varMod = (IValueModification)object;
				if ( !varMod.supportsValueModification() )
				{
					setEnabled( false );
					return;
				}
				setEnabled( !iter.hasNext() );
				return;
			}
		}
		setEnabled( false );
	}

	/**
	 * @see Action
	 */
	public void run()
	{
		Iterator iterator = getStructuredSelection().iterator();
		doActionPerformed( (IVariable)iterator.next() );
	}

	/**
	 * @see SelectionProviderAction
	 */
	public void selectionChanged( IStructuredSelection sel )
	{
		update( sel );
	}
}
