/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.wizards;

import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 * Enter type comment.
 * 
 * @since Dec 23, 2002
 */
public class AddProjectSourceLocationWizard extends Wizard implements INewSourceLocationWizard
{
	protected static final String PAGE_NAME = "AddProjectSourceLocationWizardPage";

	protected IProject[] fProjects = null;

	protected IProjectSourceLocation fSourceLocation = null;

	/**
	 * 
	 * Enter type comment.
	 * 
	 * @since Dec 27, 2002
	 */
	public class AddProjectSourceLocationWizardPage extends WizardPage 
													implements ISelectionChangedListener, IDoubleClickListener
	{
		private AddProjectSourceLocationBlock fBlock;

		/**
		 * Constructor for AddProjectSourceLocationWizardPage.
		 * @param pageName
		 */
		public AddProjectSourceLocationWizardPage( AddProjectSourceLocationWizard wizard )
		{
			super( PAGE_NAME, "Select Project", CDebugImages.DESC_WIZBAN_ADD_PRJ_SOURCE_LOCATION );
			setWindowTitle( "Add Project Source Location" );
			setMessage( "Add an existing workspace project to the source locations list." );
			setWizard( wizard );
			fBlock = new AddProjectSourceLocationBlock( fProjects );
			setPageComplete( false );
		}

		/**
		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
		 */
		public void createControl( Composite parent )
		{
			Composite composite = new Composite( parent, SWT.NULL );
			composite.setLayout( new GridLayout() );
			composite.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
			
			fBlock.createControl( composite );
			fBlock.addDoubleClickListener( this );
			fBlock.addSelectionChangedListener( this );

			setControl( composite );
		}

		/**
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
		 */
		public void selectionChanged( SelectionChangedEvent event )
		{
			setPageComplete( !event.getSelection().isEmpty() );
		}

		/**
		 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(DoubleClickEvent)
		 */
		public void doubleClick( DoubleClickEvent event )
		{
			
		}

		protected boolean finish()
		{
			if ( fBlock != null )
			{
				fSourceLocation = fBlock.getSourceLocation();
			}
			return ( fSourceLocation != null );
		}
	}
	/**
	 * Constructor for AddProjectSourceLocationWizard.
	 */
	public AddProjectSourceLocationWizard( IProject[] projects )
	{
		super();
		fProjects = projects;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish()
	{
		AddProjectSourceLocationWizardPage page = (AddProjectSourceLocationWizardPage)getStartingPage();
		if ( page != null )
		{
			return page.finish();
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard#getDescription()
	 */
	public String getDescription()
	{
		return "Add an existing project to the source locations list.";
	}

	/**
	 * @see org.eclipse.cdt.debug.ui.sourcelookup.INewSourceLocationWizard#getSourceLocation()
	 */
	public ICSourceLocation getSourceLocation()
	{
		return fSourceLocation;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages()
	{
		addPage( new AddProjectSourceLocationWizardPage( this ) );
	}
}
