/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.cdt.debug.core.ICMemoryManager;
import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
 
 	protected MemoryView fView = null;
	protected Composite fParent = null;
	protected CTabFolder fTabFolder = null;
	private Composite fControl = null;
	private MemoryControlArea[] fMemoryControlAreas = new MemoryControlArea[NUMBER_OF_TABS];

	/**
	 * Constructor for MemoryViewer.
	 */
	public MemoryViewer( Composite parent, MemoryView view )
	{
		super();
		fParent = parent;
		fView = view;
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
				fMemoryControlAreas[i] = new MemoryControlArea( fTabFolder, SWT.NONE, i, fView );
				tabItem.setControl( fMemoryControlAreas[i] );			
			}
			fTabFolder.addSelectionListener( new SelectionListener()
			 									 {
													public void widgetSelected( SelectionEvent e )
													{
														fView.updateObjects();
													}
													
													public void widgetDefaultSelected( SelectionEvent e )
													{
														fView.updateObjects();
													}
			 									 } );
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
		if ( fTabFolder != null )
		{
			CTabItem[] tabItems = fTabFolder.getItems();
			for ( int i = 0; i < tabItems.length; ++i )
				if ( tabItems[i].getControl() instanceof MemoryControlArea )
					((MemoryControlArea)tabItems[i].getControl()).refresh();
		}
	}

	public void refresh( Object element )
	{		
		if ( element instanceof IFormattedMemoryBlock )
		{
			MemoryControlArea mca = getMemoryControlArea( (IFormattedMemoryBlock)element );
			if ( mca != null )
			{
				mca.refresh();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#setSelection(ISelection, boolean)
	 */
	public void setSelection( ISelection selection, boolean reveal )
	{
	}

	public void propertyChange( PropertyChangeEvent event )
	{
		if ( fTabFolder != null )
		{
			CTabItem[] tabItems = fTabFolder.getItems();
			for ( int i = 0; i < tabItems.length; ++i )
				if ( tabItems[i].getControl() instanceof MemoryControlArea )
					((MemoryControlArea)tabItems[i].getControl()).propertyChange( event );
		}
	}
	
	protected void inputChanged( Object input, Object oldInput )
	{
		for ( int i = 0; i < fMemoryControlAreas.length; ++i )
			fMemoryControlAreas[i].setInput( (ICMemoryManager)input );
	}
	
	protected CTabFolder getTabFolder()
	{
		return fTabFolder;
	}
	
	private MemoryControlArea getMemoryControlArea( int index )
	{
		CTabItem item = fTabFolder.getItem( index );
		return ( item != null ) ? (MemoryControlArea)item.getControl() : null;
	}
	
	private MemoryControlArea getMemoryControlArea( IFormattedMemoryBlock block )
	{
		CTabItem[] tabItems = fTabFolder.getItems();
		for ( int i = 0; i < tabItems.length; ++i )
		{
			if ( tabItems[i].getControl() instanceof MemoryControlArea && 
				 block != null &&
				 block.equals( ((MemoryControlArea)tabItems[i].getControl()).getMemoryBlock() ) )
				{
					return (MemoryControlArea)tabItems[i].getControl();
				}
		}
		return null;
	}
	
	public boolean canUpdate()
	{
		return ( ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock() != null );
	}
	
	public boolean isFrozen()
	{
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		return ( block != null ) ? block.isFrozen() : true;
	}
	
	public void setFrozen( boolean frozen )
	{
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		if ( block != null )
		{
			block.setFrozen( frozen );
		}
	}
	
	public void clear()
	{
		((MemoryControlArea)fTabFolder.getSelection().getControl()).clear();
	}
	
	public boolean showAscii()
	{
		return ((MemoryControlArea)fTabFolder.getSelection().getControl()).getPresentation().displayASCII();
	}
	
	public void setShowAscii( boolean show )
	{
		((MemoryControlArea)fTabFolder.getSelection().getControl()).getPresentation().setDisplayAscii( show );
		((MemoryControlArea)fTabFolder.getSelection().getControl()).refresh();
	}
	
	public boolean canShowAscii()
	{
		return ((MemoryControlArea)fTabFolder.getSelection().getControl()).getPresentation().canDisplayAscii();
	}
	
	public int getCurrentWordSize()
	{
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		return ( block != null ) ? block.getWordSize() : 0;
	}
	
	public void setWordSize( int size ) throws DebugException
	{
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		if ( block != null )
		{ 
			block.reformat( block.getFormat(), size, block.getNumberOfRows(), block.getNumberOfColumns() );
			((MemoryControlArea)fTabFolder.getSelection().getControl()).refresh();
		}
	}
	
	public int getCurrentNumberOfColumns()
	{
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		return ( block != null ) ? block.getNumberOfColumns() : 0;
	}
	
	public void setNumberOfColumns( int numberOfColumns ) throws DebugException
	{
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		if ( block != null )
		{ 
			block.reformat( block.getFormat(), block.getWordSize(), block.getNumberOfRows(), numberOfColumns );
			((MemoryControlArea)fTabFolder.getSelection().getControl()).refresh();
		}
	}
}
