/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.cdt.debug.core.IFormattedMemoryRetrieval;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 * Memory viewer.
 * 
 * @since Jul 24, 2002
 */
public class MemoryViewer extends ContentViewer
{
	static final private int NUMBER_OF_TABS = 4;
 
	protected Composite fParent = null;
	protected CTabFolder fTabFolder = null;
	private Composite fControl = null;
	private MemoryControlArea[] fMemoryControlAreas = new MemoryControlArea[NUMBER_OF_TABS];

	/**
	 * Constructor for MemoryViewer.
	 */
	public MemoryViewer( Composite parent )
	{
		super();
		fParent = parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#getControl()
	 */
	public Control getControl()
	{
		if ( fControl == null )
		{
			fControl = new Composite( fParent, SWT.NONE );
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			fControl.setLayout( layout );
			fControl.setLayoutData( new GridData( GridData.FILL_BOTH ) );
			fTabFolder = new CTabFolder( fControl, SWT.TOP );
			fTabFolder.setLayoutData( new GridData( GridData.FILL_BOTH | GridData.GRAB_VERTICAL ) );
			for ( int i = 0; i < NUMBER_OF_TABS; ++i )
			{
				CTabItem tabItem = new CTabItem( fTabFolder, SWT.NONE );
				tabItem.setText( "Memory " + (i + 1) );
				fMemoryControlAreas[i] = new MemoryControlArea( fTabFolder, SWT.NONE, i );
				tabItem.setControl( fMemoryControlAreas[i] );			
			}
			fTabFolder.setSelection( 0 );				
		}		
		return fControl;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#refresh()
	 */
	public void refresh()
	{		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#setSelection(ISelection, boolean)
	 */
	public void setSelection(ISelection selection, boolean reveal)
	{
	}

	public void propertyChange( PropertyChangeEvent event )
	{
		CTabItem[] tabItems = fTabFolder.getItems();
		for ( int i = 0; i < tabItems.length; ++i )
			if ( tabItems[i].getControl() instanceof MemoryControlArea )
				((MemoryControlArea)tabItems[i].getControl()).propertyChange( event );
	}
	
	protected void inputChanged( Object input, Object oldInput )
	{
		if ( input instanceof IFormattedMemoryRetrieval )
		{
			for ( int i = 0; i < fMemoryControlAreas.length; ++i )
				fMemoryControlAreas[i].setInput( (IFormattedMemoryRetrieval)input );
		}
	}
}
