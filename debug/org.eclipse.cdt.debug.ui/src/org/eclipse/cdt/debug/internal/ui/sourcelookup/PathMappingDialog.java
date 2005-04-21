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
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import java.io.File;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A dialog for editing a path mapping source container.
 */
public class PathMappingDialog extends TitleAreaDialog {

	class MapEntryDialog extends TitleAreaDialog {

		private MapEntrySourceContainer fEntry;

		protected Text fBackendPathText;
		protected Text fLocalPathText;

		/**
		 * Constructor for MapEntryDialog.
		 */
		public MapEntryDialog( Shell parentShell ) {
			super( parentShell );
			fEntry = null;
		}

		/**
		 * Constructor for MapEntryDialog.
		 */
		public MapEntryDialog( Shell parentShell, MapEntrySourceContainer entry ) {
			super( parentShell );
			fEntry = entry;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea( Composite parent ) {
			setTitle( SourceLookupUIMessages.getString( "PathMappingDialog.0" ) ); //$NON-NLS-1$

			Font font = parent.getFont();
			Composite composite = new Composite( parent, SWT.NONE );
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
			layout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
			layout.verticalSpacing = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_SPACING );
			layout.horizontalSpacing = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_SPACING );
			composite.setLayout( layout );
			GridData data = new GridData( GridData.FILL_BOTH );
			composite.setLayoutData( data );
			composite.setFont( font );

			Dialog.applyDialogFont( composite );
			PlatformUI.getWorkbench().getHelpSystem().setHelp( getShell(), ICDebugHelpContextIds.SOURCE_PATH_MAP_ENTRY_DIALOG );

			setMessage( null );

			Label label = new Label( composite, SWT.LEFT );
			label.setText( SourceLookupUIMessages.getString( "PathMappingDialog.1" ) ); //$NON-NLS-1$
			data = new GridData( GridData.FILL_HORIZONTAL );
			data.horizontalSpan = 2;
			label.setLayoutData( data );
			label.setFont( font );
			
			fBackendPathText = new Text( composite, SWT.SINGLE | SWT.BORDER );
			data = new GridData( GridData.FILL_HORIZONTAL );
			data.horizontalSpan = 2;
			fBackendPathText.setLayoutData( data );
			fBackendPathText.setFont( font );
			fBackendPathText.addModifyListener( new ModifyListener() {
				public void modifyText( ModifyEvent e ) {
					update();
				}
			} );

			label = new Label( composite, SWT.LEFT );
			label.setText( SourceLookupUIMessages.getString( "PathMappingDialog.2" ) ); //$NON-NLS-1$
			data = new GridData( GridData.FILL_HORIZONTAL );
			data.horizontalSpan = 2;
			label.setLayoutData( data );
			label.setFont( font );
			
			fLocalPathText = new Text( composite, SWT.SINGLE | SWT.BORDER );
			data = new GridData( GridData.FILL_HORIZONTAL );
			fLocalPathText.setLayoutData( data );
			fLocalPathText.setFont( font );
			fLocalPathText.addModifyListener( new ModifyListener() {
				public void modifyText( ModifyEvent e ) {
					update();
				}
			} );

			Button button = new Button( composite, SWT.PUSH );
			button.setFont( font );
			button.setText( SourceLookupUIMessages.getString( "PathMappingDialog.3" ) ); //$NON-NLS-1$
			button.addSelectionListener( new SelectionListener() { 

				public void widgetSelected( SelectionEvent e ) {
					DirectoryDialog dialog = new DirectoryDialog( MapEntryDialog.this.getShell() );
					String path = dialog.open();
					if ( path != null ) {
						fLocalPathText.setText( path );
					}
				}

				public void widgetDefaultSelected( SelectionEvent e ) {
				}
			} );

			return composite;
		}

		protected Control createContents( Composite parent ) {
			Control control = super.createContents( parent );
			initialize();
			update();
			return control;
		}

		protected void configureShell( Shell newShell ) {
			newShell.setText( SourceLookupUIMessages.getString( "PathMappingDialog.4" ) ); //$NON-NLS-1$
			super.configureShell( newShell );
		}

		private void initialize() {
			if ( fEntry != null ) {
				fBackendPathText.setText( fEntry.getBackendPath().toOSString() );
				fLocalPathText.setText( fEntry.getLocalPath().toOSString() );
			}
		}

		protected void update() {
			boolean isOk = updateErrorMessage();
			Button ok = getButton( IDialogConstants.OK_ID );
			if ( ok != null )
				ok.setEnabled( isOk );
		}

		protected boolean updateErrorMessage() {
			setErrorMessage( null );
			String backendText = fBackendPathText.getText().trim();
			if ( backendText.length() == 0 ) {
				setErrorMessage( SourceLookupUIMessages.getString( "PathMappingDialog.5" ) ); //$NON-NLS-1$
				return false;
			}
			if ( !new Path( backendText ).isValidPath( backendText ) ) {
				setErrorMessage( SourceLookupUIMessages.getString( "PathMappingDialog.6" ) ); //$NON-NLS-1$
				return false;
			}
			String localText = fLocalPathText.getText().trim();
			if ( localText.length() == 0 ) {
				setErrorMessage( SourceLookupUIMessages.getString( "PathMappingDialog.7" ) ); //$NON-NLS-1$
				return false;
			}
			File localPath = new File( localText );
			if ( !localPath.exists() ) {
				setErrorMessage( SourceLookupUIMessages.getString( "PathMappingDialog.8" ) ); //$NON-NLS-1$
				return false;
			}
			if ( !localPath.isDirectory() ) {
				setErrorMessage( SourceLookupUIMessages.getString( "PathMappingDialog.9" ) ); //$NON-NLS-1$
				return false;
			}
			if ( !localPath.isAbsolute() ) {
				setErrorMessage( SourceLookupUIMessages.getString( "PathMappingDialog.10" ) ); //$NON-NLS-1$
				return false;
			}
			return true;
		}

		protected IPath getBackendPath() {
			return new Path( fBackendPathText.getText().trim() );
		}

		protected IPath getLocalPath() {
			return new Path( fLocalPathText.getText().trim() );
		}

		protected void okPressed() {
			if ( fEntry == null ) {
				fEntry = new MapEntrySourceContainer();
				fMapping.addMapEntry( fEntry );
			}
			fEntry.setBackendPath( getBackendPath() );
			fEntry.setLocalPath( getLocalPath() );
			super.okPressed();
		}
	}

	class PathMappingLabelProvider extends LabelProvider {

		private ILabelProvider fLabelProvider = null;

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage( Object element ) {
			Image image = getWorkbenchLabelProvider().getImage( element );
			if ( image != null ) {
				return image;
			}
			return super.getImage( element );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText( Object element ) {
			String label = getWorkbenchLabelProvider().getText( element );
			if ( label == null || label.length() == 0 ) {
				if ( element instanceof ISourceContainer ) {
					return ((ISourceContainer)element).getName();
				}
			}
			else {
				return label;
			}
			return super.getText( element );
		}

		private ILabelProvider getWorkbenchLabelProvider() {
			if ( fLabelProvider == null ) {
				fLabelProvider = new WorkbenchLabelProvider();
			}
			return fLabelProvider;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			super.dispose();
			if ( fLabelProvider != null ) {
				fLabelProvider.dispose();
			}
		}
	}

	class ContentProvider implements IStructuredContentProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements( Object input ) {
			if ( input instanceof MappingSourceContainer ) {
				try {
					return ((MappingSourceContainer)input).getSourceContainers();
				}
				catch( CoreException e ) {
					setErrorMessage( e.getMessage() );
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
		}
	}

	private MappingSourceContainer fOriginalMapping;

	protected MappingSourceContainer fMapping;

	private TableViewer fViewer;

	private Text fNameText;
	private Button fAddButton;
	private Button fEditButton;
	private Button fRemoveButton;

	public PathMappingDialog( Shell parentShell, MappingSourceContainer mapping ) {
		super( parentShell );
		fOriginalMapping = mapping;
		fMapping = fOriginalMapping.copy();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents( Composite parent ) {
		Control control = super.createContents( parent );
		updateButtons();
		return control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea( Composite parent ) {
		setTitle( SourceLookupUIMessages.getString( "PathMappingDialog.11" ) ); //$NON-NLS-1$
		//TODO Add image
		
		Font font = parent.getFont();
		Composite composite = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
		layout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
		layout.verticalSpacing = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_SPACING );
		layout.horizontalSpacing = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_SPACING );
		composite.setLayout( layout );
		GridData data = new GridData( GridData.FILL_BOTH );
		composite.setLayoutData( data );
		composite.setFont( font );

		Dialog.applyDialogFont( composite );
		PlatformUI.getWorkbench().getHelpSystem().setHelp( getShell(), ICDebugHelpContextIds.SOURCE_PATH_MAPPING_DIALOG );

		Composite nameComp = new Composite( composite, SWT.NONE );
		layout = new GridLayout();
		layout.numColumns = 2;
		nameComp.setLayout( layout );
		data = new GridData( GridData.FILL_HORIZONTAL );
		data.horizontalSpan = 2;
		nameComp.setLayoutData( data );
		nameComp.setFont( font );

		Label label = new Label( nameComp, SWT.LEFT );
		data = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
		label.setLayoutData( data );
		label.setFont( font );
		label.setText( SourceLookupUIMessages.getString( "PathMappingDialog.12" ) ); //$NON-NLS-1$
		fNameText = new Text( nameComp, SWT.SINGLE | SWT.BORDER );
		data = new GridData( GridData.FILL_HORIZONTAL );
		fNameText.setLayoutData( data );
		fNameText.setFont( font );
		fNameText.setText( getMapping().getName() );
		fNameText.addModifyListener( new ModifyListener() {
			public void modifyText( ModifyEvent e ) {
			}
		} );
		
		fViewer = createViewer( composite );
		data = new GridData( GridData.FILL_BOTH );
		fViewer.getControl().setLayoutData( data );
		fViewer.getControl().setFont( font );
		fViewer.addSelectionChangedListener( new ISelectionChangedListener() { 
			public void selectionChanged( SelectionChangedEvent event ) {
				updateButtons();
			}
		} );

		Composite buttonComp = new Composite( composite, SWT.NONE );
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonComp.setLayout( buttonLayout );
		data = new GridData( GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL );
		buttonComp.setLayoutData( data );
		buttonComp.setFont( font );

		GC gc = new GC( parent );
		gc.setFont( parent.getFont() );
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		fAddButton = createPushButton( buttonComp, SourceLookupUIMessages.getString( "PathMappingDialog.13" ), fontMetrics ); //$NON-NLS-1$
		fAddButton.addSelectionListener( new SelectionAdapter() {
				public void widgetSelected( SelectionEvent evt ) {
					MapEntryDialog dialog = new MapEntryDialog( getShell() );
					if ( dialog.open() == Window.OK ) {
						getViewer().refresh();
					}
				}
			} );

		fEditButton = createPushButton( buttonComp, SourceLookupUIMessages.getString( "PathMappingDialog.14" ), fontMetrics ); //$NON-NLS-1$
		fEditButton.addSelectionListener( new SelectionAdapter() {
				public void widgetSelected( SelectionEvent evt ) {
					MapEntrySourceContainer[] entries = getSelection();
					if ( entries.length > 0 ) {
						MapEntryDialog dialog = new MapEntryDialog( getShell(), entries[0] );
						if ( dialog.open() == Window.OK ) {
							getViewer().refresh();
						}
					}
				}
			} );

		fRemoveButton = createPushButton( buttonComp, SourceLookupUIMessages.getString( "PathMappingDialog.15" ), fontMetrics ); //$NON-NLS-1$
		fRemoveButton.addSelectionListener( new SelectionAdapter() {
				public void widgetSelected( SelectionEvent evt ) {
					MapEntrySourceContainer[] entries = getSelection();
						for ( int i = 0; i < entries.length; ++i ) {
							fMapping.removeMapEntry( entries[i] );
						}
						getViewer().refresh();
				}
			} );

		setMessage( null );

		fViewer.setInput( fMapping );

		return composite;
	}

	private TableViewer createViewer( Composite parent ) {
		TableViewer viewer = new TableViewer( parent );
		viewer.setContentProvider( new ContentProvider() );
		viewer.setLabelProvider( new PathMappingLabelProvider() );
		return viewer;
	}

	protected MappingSourceContainer getMapping() {
		return fOriginalMapping;
	}

	protected Button createPushButton( Composite parent, String label, FontMetrics fontMetrics ) {
		Button button = new Button( parent, SWT.PUSH );
		button.setFont( parent.getFont() );
		button.setText( label );
		GridData gd = getButtonGridData( button, fontMetrics );
		button.setLayoutData( gd );
		return button;
	}

	private GridData getButtonGridData( Button button, FontMetrics fontMetrics ) {
		GridData gd = new GridData( GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING );
		int widthHint = Dialog.convertHorizontalDLUsToPixels( fontMetrics, IDialogConstants.BUTTON_WIDTH );
		gd.widthHint = Math.max( widthHint, button.computeSize( SWT.DEFAULT, SWT.DEFAULT, true ).x );
		return gd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell( Shell newShell ) {
		newShell.setText( SourceLookupUIMessages.getString( "PathMappingDialog.16" ) ); //$NON-NLS-1$
		super.configureShell( newShell );
	}

	protected Viewer getViewer() {
		return fViewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		fOriginalMapping.clear();
		fOriginalMapping.setName( fNameText.getText().trim() );
		try {
			fOriginalMapping.addMapEntries( (MapEntrySourceContainer[])fMapping.getSourceContainers() );
		}
		catch( CoreException e ) {
		}
		fMapping.dispose();
		super.okPressed();
	}	

	protected MapEntrySourceContainer[] getSelection() {
		MapEntrySourceContainer[] result = new MapEntrySourceContainer[0];
		ISelection s = getViewer().getSelection();
		if ( s instanceof IStructuredSelection ) {
			int size = ((IStructuredSelection)s).size();
			result = (MapEntrySourceContainer[])((IStructuredSelection)s).toList().toArray( new MapEntrySourceContainer[size] );
		}
		return result; 
	}

	protected void updateButtons() {
		MapEntrySourceContainer[] entries = getSelection();
		if ( fEditButton != null ) {
			fEditButton.setEnabled( entries.length == 1 );
		}
		if ( fRemoveButton != null ) {
			fRemoveButton.setEnabled( entries.length > 0 );
		}
	}
}
