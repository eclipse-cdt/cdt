package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


public class SetConditionAction extends Action 
{
	class BreakpointConditionDialog
	{
		Shell fShell;
		String fOldCondition;
		String fCondition;
		String fOldIgnoreCount;
		String fIgnoreCount;
		Button fOkButton;
		Button fCancelButton;
		boolean fIsCanceled = true;
		String fTitle = "Breakpoint condition";
		public BreakpointConditionDialog( Shell parent, 
										  Point location, 
										  String condition,
										  int ignoreCount )
		{
			fOldCondition = condition;
			fCondition = condition;
			fOldIgnoreCount = String.valueOf( ignoreCount );
			fIgnoreCount = fOldIgnoreCount;
			fShell = new Shell( parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL );
			fShell.setText( fTitle );
			fShell.setLayout( new GridLayout() );
			fShell.setLocation( location );
		}


		private void createControlButtons() 
		{
			Composite composite = new Composite( fShell, SWT.NULL );
			composite.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_CENTER ) );
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			composite.setLayout( layout );
			
			fOkButton = new Button( composite, SWT.PUSH );
			fOkButton.setText( "OK" );
			fOkButton.addSelectionListener( 
							new SelectionAdapter() 
							{
								public void widgetSelected( SelectionEvent event ) 
								{
									try
									{
										Integer.parseInt( fIgnoreCount );
									}
									catch( NumberFormatException e )
									{
										MessageDialog.openError( fShell, fTitle, "Invalid integer value - " + e.getMessage() );
										return;
									}
									fIsCanceled = false;
									fShell.close();
								}
							} );
			
			fCancelButton = new Button( composite, SWT.PUSH );
			fCancelButton.setText( "Cancel" );
			fCancelButton.addSelectionListener( 
							new SelectionAdapter() 
							{
								public void widgetSelected( SelectionEvent e ) 
								{
									fCondition = fOldCondition;
									fIgnoreCount = fOldIgnoreCount;
									fShell.close();
								}
							} );
			
			fShell.setDefaultButton( fOkButton );
		}


		private void createTextWidgets() 
		{
			Composite composite = new Composite( fShell, SWT.NULL );
			composite.setLayoutData( new GridData( GridData.FILL_VERTICAL ) );
			GridLayout layout= new GridLayout();
			layout.numColumns = 1;
			composite.setLayout( layout );
			
			Label label = new Label( composite, SWT.RIGHT );
			label.setText( "Enter condition to be evaluated:" );	
			Text textCondition = new Text( composite, SWT.BORDER );
			label = new Label( composite, SWT.RIGHT );
			label.setText( "Enter the number of times to skip before stopping:" );	
			Text textIgnoreCount = new Text( composite, SWT.BORDER );
			GridData condGridData = new GridData();
			condGridData.widthHint = 300;
			textCondition.setLayoutData( condGridData );
			textCondition.setText( fCondition );
			addTextListener( textCondition, true );	
			GridData ignoreGridData = new GridData();
			ignoreGridData.widthHint = 50;
			textIgnoreCount.setLayoutData( ignoreGridData );
			textIgnoreCount.setText( fIgnoreCount );
			addTextListener( textIgnoreCount, false );	
		}


		private void addTextListener( final Text text, final boolean isCondition ) 
		{
			text.addModifyListener( 
							new ModifyListener() 
								{
									public void modifyText( ModifyEvent e )
									{
										if ( isCondition )
											fCondition = text.getText();
										else
										{
											fIgnoreCount = text.getText();
											fOkButton.setEnabled( fIgnoreCount.length() > 0 );
										}
									}
								} );
		}


		public String getCondition()
		{
			return fCondition;
		}
		
		public int getIgnoreCount()
		{
			int ret = 0;
			try
			{
				ret = Integer.parseInt( fIgnoreCount );
			}
			catch( NumberFormatException e )
			{
			}
			
			return ret;
		}


		public boolean open() 
		{
			createTextWidgets();
			createControlButtons();
			fShell.pack();
			fShell.open();
			Display display = fShell.getDisplay();
			while( !fShell.isDisposed() )
			{
				if( !display.readAndDispatch() )
					display.sleep();
			}
			
			return !fIsCanceled;
		}
	}


	private IVerticalRuler fRuler;
	private ITextEditor fTextEditor;


	public SetConditionAction( IVerticalRuler ruler, ITextEditor editor )
	{
		super( "Set condition ..." );
		fRuler = ruler;
		fTextEditor = editor;
	}


	public boolean isBreakpointSet()
	{
		List markers = getMarkers();
		return ( markers != null && markers.size() > 0 );
	}


	protected IResource getResource() 
	{
		IEditorInput input = fTextEditor.getEditorInput();
		
		IResource resource = (IResource)input.getAdapter( IFile.class );
		
		if ( resource == null )
			resource = (IResource)input.getAdapter( IResource.class );
			
		return resource;
	}


	protected ITextEditor getTextEditor() 
	{
		return fTextEditor;
	}


	protected IVerticalRuler getVerticalRuler() 
	{
		return fRuler;
	}


	protected IDocument getDocument() 
	{
		IDocumentProvider provider = fTextEditor.getDocumentProvider();
		return provider.getDocument( fTextEditor.getEditorInput() );
	}


	protected AbstractMarkerAnnotationModel getAnnotationModel() 
	{
		IDocumentProvider provider = fTextEditor.getDocumentProvider();
		IAnnotationModel model = provider.getAnnotationModel( fTextEditor.getEditorInput() );
		if ( model instanceof AbstractMarkerAnnotationModel )
			return (AbstractMarkerAnnotationModel)model;
		return null;
	}


	/**
	 * Returns all breakpoint markers which include the ruler's line of activity.
	 *
	 * @returns all breakpoint markers which include the ruler's line of activity
	 */
	protected List getMarkers() 
	{
		List markers = new ArrayList();


		IResource resource = getResource();
		IDocument document = getDocument();
		AbstractMarkerAnnotationModel model = getAnnotationModel();


		if ( resource != null && model != null ) 
		{
			try 
			{
				IMarker[] allMarkers = resource.findMarkers( IBreakpoint.BREAKPOINT_MARKER, 
															 true, 
															 IResource.DEPTH_ZERO );
				if ( allMarkers != null ) 
				{
					for ( int i = 0; i < allMarkers.length; i++ ) 
					{
						if ( includesRulerLine( model.getMarkerPosition( allMarkers[i] ), document ) ) 
						{
							markers.add( allMarkers[i] );
						}
					}
				}
			} 
			catch( CoreException x ) 
			{
				handleCoreException( x, "SetConditionAction:getMarkers" );
			}
		}


		return markers;
	}


	protected boolean includesRulerLine( Position position, IDocument document ) 
	{
		if ( position != null ) 
		{
			try 
			{
				int markerLine = document.getLineOfOffset( position.getOffset() );
				int line = fRuler.getLineOfLastMouseButtonActivity();
				if ( line == markerLine )
					return true;
				return ( markerLine <= line && line <= document.getLineOfOffset( position.getOffset() + position.getLength() ) );
			} 
			catch( BadLocationException x ) 
			{
			}
		}
		return false;
	}


	protected void handleCoreException( CoreException exception, String message ) 
	{
		ILog log = Platform.getPlugin( PlatformUI.PLUGIN_ID ).getLog();
		
		if ( message != null )
			log.log( new Status( IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, null ) );
		
		log.log( exception.getStatus() );
/*		
		Shell shell = getTextEditor().getSite().getShell();
		String title = getString( fBundle, fPrefix + "error.dialog.title", fPrefix + "error.dialog.title" );
		String msg = getString( fBundle, fPrefix + "error.dialog.message", fPrefix + "error.dialog.message" );
		
		ErrorDialog.openError( shell, title, msg, exception.getStatus() );
*/
	}


	protected Shell getShell() 
	{
		IWorkbench workbench = PlatformUI.getWorkbench();
		if ( workbench != null ) 
		{
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if ( window != null ) 
			{
				Shell shell = window.getShell();
				if ( !shell.isDisposed() ) 
				{
					return shell;
				}
			}
		}
		return null;
	}


	/**
	 * @see Action#run()
	 */
	public void run() 
	{
		try
		{
			Object[] markers = getMarkers().toArray();
			if ( markers.length == 0 )
				return;
			IMarker marker = (IMarker)markers[0];
			Shell shell = getShell();
			if ( shell == null )
				return;
			Point location = shell.getDisplay().getCursorLocation();
			String condition = marker.getAttribute( "condition", "" );
			int ignoreCount = marker.getAttribute( "ignoreCount", 0 );
			BreakpointConditionDialog dialog = 
					new BreakpointConditionDialog( shell, location, condition, ignoreCount );
			if ( dialog.open() )
			{
				condition = dialog.getCondition();
				marker.setAttribute( "condition", condition );
				ignoreCount = dialog.getIgnoreCount();
				marker.setAttribute( "ignoreCount", ignoreCount );
				String message = "";
				if ( condition.length() > 0 )
					message += "if " + condition + " ";
				if ( ignoreCount > 0 )
					message += "ignore = " + ignoreCount;
				marker.setAttribute( IMarker.MESSAGE, message );
			}
		}
		catch( CoreException e )
		{
		}
	}
}


