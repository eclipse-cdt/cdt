/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.wizards;

import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.SourceLookupFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * 
 * Enter type comment.
 * 
 * @since Dec 27, 2002
 */
public class AddProjectSourceLocationBlock
{
	private Composite fControl = null;
	private TableViewer fViewer;

	protected IProject[] fProjects = null;

	/**
	 * Constructor for AddProjectSourceLocationBlock.
	 */
	public AddProjectSourceLocationBlock( IProject[] projects )
	{
		fProjects = projects;
	}

	public void createControl( Composite parent )
	{
		fControl = new Composite( parent, SWT.NONE );
		fControl.setLayout( new GridLayout() );
		fControl.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		fControl.setFont( JFaceResources.getDialogFont() );	

		//Create a table for the list
		Table table = new Table( fControl, SWT.BORDER | SWT.SINGLE );
		GridData data = new GridData( GridData.FILL_BOTH );
		table.setLayoutData( data );

		// the list viewer		
		fViewer = new TableViewer( table );
		fViewer.setContentProvider( new IStructuredContentProvider()
										{
											public Object[] getElements( Object inputElement )
											{
												return fProjects;
											}
											
											public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
											{
											}

											public void dispose()
											{
											}
										} );
		fViewer.setLabelProvider( new WorkbenchLabelProvider() );

		fViewer.setInput( fProjects );
	}

	public Control getControl()
	{
		return fControl;
	}

	public IProjectSourceLocation getSourceLocation()
	{
		if ( fViewer != null )
		{
			if ( !((IStructuredSelection)fViewer.getSelection()).isEmpty() )
			{
				return SourceLookupFactory.createProjectSourceLocation( (IProject)((IStructuredSelection)fViewer.getSelection()).getFirstElement(), false );
			}
		}
		return null;
	}

	public void addSelectionChangedListener( ISelectionChangedListener listener )
	{
		if ( fViewer != null )
		{
			fViewer.addSelectionChangedListener( listener );
		}
	}

	public void removeSelectionChangedListener( ISelectionChangedListener listener )
	{
		if ( fViewer != null )
		{
			fViewer.removeSelectionChangedListener( listener );
		}
	}

	public void addDoubleClickListener( IDoubleClickListener listener )
	{
		if ( fViewer != null )
		{
			fViewer.addDoubleClickListener( listener );
		}
	}

	public void removeDoubleClickListener( IDoubleClickListener listener )
	{
		if ( fViewer != null )
		{
			fViewer.removeDoubleClickListener( listener );
		}
	}
}
