/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.wizards;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

/**
 * 
 * Enter type comment.
 * 
 * @since Dec 24, 2002
 */
public class SourceLocationSelectionPage extends WizardSelectionPage
										 implements ISelectionChangedListener, 
										 			IDoubleClickListener
{
	private static final String PAGE_NAME = "Add Source Location";
	private final static int SIZING_LISTS_HEIGHT = 200;
	private final static int SIZING_LISTS_WIDTH = 150;

	protected TableViewer fWizardSelectionViewer;
	
	protected Object[] fElements = null;
	
	/**
	 * Constructor for SourceLocationSelectionPage.
	 * @param pageName
	 */
	public SourceLocationSelectionPage( ICSourceLocation[] locations )
	{
		super( PAGE_NAME );
		setTitle( "Select" );
		setImageDescriptor( CDebugImages.DESC_WIZBAN_ADD_SOURCE_LOCATION );
		fElements = new Object[] { new AddProjectSourceLocationWizard( getProjectList( locations ) ),
								   new AddDirectorySourceLocationWizard() };
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
	 */
	public void createControl( Composite parent )
	{
		// create composite for page.
		Composite outerContainer = new Composite( parent, SWT.NONE );
		outerContainer.setLayout( new GridLayout() );
		outerContainer.setLayoutData( new GridData( GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL ) );

		new Label( outerContainer, SWT.NONE ).setText( "Select source location type:" );

		//Create a table for the list
		Table table = new Table( outerContainer, SWT.BORDER );
		GridData data = new GridData( GridData.FILL_HORIZONTAL );
		data.widthHint = SIZING_LISTS_WIDTH;
		data.heightHint = SIZING_LISTS_HEIGHT;
		table.setLayoutData( data );

		// the list viewer		
		fWizardSelectionViewer = new TableViewer( table );
		fWizardSelectionViewer.setContentProvider( new IStructuredContentProvider()
														{
															public Object[] getElements( Object inputElement )
															{
																return fElements;
															}
															
															public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
															{
															}

															public void dispose()
															{
															}
														} );
		fWizardSelectionViewer.setLabelProvider( new LabelProvider()
														{
															public String getText( Object element )
															{
																if ( element instanceof AddProjectSourceLocationWizard )
																{
																	return "Existing Project Into Workspace"; 
																}
																if ( element instanceof AddDirectorySourceLocationWizard )
																{
																	return "File System Directory"; 
																}
																return super.getText( element );
															}

															/**
															 * @see org.eclipse.jface.viewers.LabelProvider#getImage(Object)
															 */
															public Image getImage(Object element)
															{
																if ( element instanceof AddProjectSourceLocationWizard )
																{
																	return CDebugImages.get( CDebugImages.IMG_TOOLS_ADD_PRJ_SOURCE_LOCATION ); 
																}
																if ( element instanceof AddDirectorySourceLocationWizard )
																{
																	return CDebugImages.get( CDebugImages.IMG_TOOLS_ADD_DIR_SOURCE_LOCATION ); 
																}
																return super.getImage( element );
															}
														} );
		fWizardSelectionViewer.addSelectionChangedListener( this );
		fWizardSelectionViewer.addDoubleClickListener( this );

		fWizardSelectionViewer.setInput( fElements );

		fWizardSelectionViewer.setSelection( new StructuredSelection( fElements[0] ) );

		setControl( outerContainer );
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
	 */
	public void selectionChanged( SelectionChangedEvent event )
	{
		setErrorMessage( null );
		IStructuredSelection selection = (IStructuredSelection)event.getSelection();
		INewSourceLocationWizard currentWizardSelection = (INewSourceLocationWizard)selection.getFirstElement();
		if ( currentWizardSelection == null ) 
		{
			setMessage( null );
			setSelectedNode( null );
			return;
		}
	
		setSelectedNode( createWizardNode( currentWizardSelection ) );
		setMessage( currentWizardSelection.getDescription() );
	}

	/**
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(DoubleClickEvent)
	 */
	public void doubleClick( DoubleClickEvent event )
	{
		selectionChanged( new SelectionChangedEvent( fWizardSelectionViewer, fWizardSelectionViewer.getSelection() ) );
		getContainer().showPage( getNextPage() );
	}
	
	private IWizardNode createWizardNode( INewSourceLocationWizard wizard )
	{
		return new SourceLocationWizardNode( wizard );
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose()
	{
		if ( fElements != null )
		{
			for ( int i = 0; i < fElements.length; ++i )
			{
				((INewSourceLocationWizard)fElements[i]).dispose();
			}
			fElements = null;
		}
		super.dispose();
	}
	
	public ICSourceLocation getSourceLocation()
	{
		return ((INewSourceLocationWizard)getSelectedNode().getWizard()).getSourceLocation();
	}

	private IProject[] getProjectList( ICSourceLocation[] locations )
	{
		ArrayList projects = new ArrayList( locations.length );
		for ( int i = 0; i < locations.length; ++i )
		{
			if ( locations[i] instanceof IProjectSourceLocation )
			{
				projects.add( ((IProjectSourceLocation)locations[i]).getProject() );
			}
		}
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		ArrayList result = new ArrayList( allProjects.length );
		for ( int i = 0; i < allProjects.length; ++i )
		{
			if ( ( CoreModel.getDefault().hasCNature( allProjects[i] ) || CoreModel.getDefault().hasCNature( allProjects[i] ) ) && 
				 allProjects[i].isOpen() && 
				 !projects.contains( allProjects[i] ) )
			{
				result.add( allProjects[i] );
			}
		}
		return (IProject[])result.toArray( new IProject[result.size()] );
	}	
}
