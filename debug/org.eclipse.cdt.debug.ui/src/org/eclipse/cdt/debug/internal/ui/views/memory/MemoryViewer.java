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
package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.cdt.debug.core.model.IFormattedMemoryBlock;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.text.ITextOperationTarget;
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
 * The viewer of the Memory view.
 */
public class MemoryViewer extends ContentViewer {

	static final private int NUMBER_OF_TABS = 4;

	protected MemoryView fView = null;

	protected Composite fParent = null;

	protected CTabFolder fTabFolder = null;

	private Composite fControl = null;

	private MemoryControlArea[] fMemoryControlAreas = new MemoryControlArea[NUMBER_OF_TABS];

	/**
	 * Constructor for MemoryViewer.
	 */
	public MemoryViewer( Composite parent, MemoryView view ) {
		super();
		fParent = parent;
		fView = view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.Viewer#getControl()
	 */
	public Control getControl() {
		if ( fControl == null ) {
			fControl = new Composite( fParent, SWT.NONE );
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			fControl.setLayout( layout );
			fControl.setLayoutData( new GridData( GridData.FILL_BOTH ) );
			fTabFolder = new CTabFolder( fControl, SWT.TOP );
			fTabFolder.setLayoutData( new GridData( GridData.FILL_BOTH | GridData.GRAB_VERTICAL ) );
			for( int i = 0; i < NUMBER_OF_TABS; ++i ) {
				CTabItem tabItem = new CTabItem( fTabFolder, SWT.NONE );
				tabItem.setText( MemoryViewMessages.getString( "MemoryViewer.0" ) + ' ' + (i + 1) ); //$NON-NLS-1$
				fMemoryControlAreas[i] = new MemoryControlArea( fTabFolder, SWT.NONE, i, fView );
				tabItem.setControl( fMemoryControlAreas[i] );
			}
			fTabFolder.addSelectionListener( new SelectionListener() {

				public void widgetSelected( SelectionEvent e ) {
					fView.updateObjects();
				}

				public void widgetDefaultSelected( SelectionEvent e ) {
					fView.updateObjects();
				}
			} );
			fTabFolder.setSelection( 0 );
		}
		return fControl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.Viewer#refresh()
	 */
	public void refresh() {
		if ( fTabFolder != null ) {
			CTabItem[] tabItems = fTabFolder.getItems();
			for( int i = 0; i < tabItems.length; ++i )
				if ( tabItems[i].getControl() instanceof MemoryControlArea )
					((MemoryControlArea)tabItems[i].getControl()).refresh();
		}
	}

	public void refresh( Object element ) {
		if ( element instanceof IFormattedMemoryBlock ) {
			MemoryControlArea mca = getMemoryControlArea( (IFormattedMemoryBlock)element );
			if ( mca != null ) {
				mca.refresh();
			}
		}
	}

	public void remove( Object element ) {
		if ( element instanceof IFormattedMemoryBlock ) {
			MemoryControlArea mca = getMemoryControlArea( (IFormattedMemoryBlock)element );
			if ( mca != null ) {
				mca.clear();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.Viewer#setSelection(ISelection, boolean)
	 */
	public void setSelection( ISelection selection, boolean reveal ) {
	}

	public void propertyChange( PropertyChangeEvent event ) {
		if ( fTabFolder != null ) {
			CTabItem[] tabItems = fTabFolder.getItems();
			for( int i = 0; i < tabItems.length; ++i )
				if ( tabItems[i].getControl() instanceof MemoryControlArea )
					((MemoryControlArea)tabItems[i].getControl()).propertyChange( event );
		}
	}

	protected void inputChanged( Object input, Object oldInput ) {
		for( int i = 0; i < fMemoryControlAreas.length; ++i )
			fMemoryControlAreas[i].setInput( input );
	}

	protected CTabFolder getTabFolder() {
		return fTabFolder;
	}

	private MemoryControlArea getMemoryControlArea( IFormattedMemoryBlock block ) {
		CTabItem[] tabItems = fTabFolder.getItems();
		for( int i = 0; i < tabItems.length; ++i ) {
			if ( tabItems[i].getControl() instanceof MemoryControlArea && block != null && block.equals( ((MemoryControlArea)tabItems[i].getControl()).getMemoryBlock() ) ) {
				return (MemoryControlArea)tabItems[i].getControl();
			}
		}
		return null;
	}

	public boolean canChangeFormat( int format ) {
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		return (block != null && block.canChangeFormat( format ));
	}

	public boolean canUpdate() {
		return (((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock() != null);
	}

	public boolean canSave() {
		return (((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock() != null);
	}

	public boolean isFrozen() {
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		return (block != null) ? block.isFrozen() : true;
	}

	public void setFrozen( boolean frozen ) {
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		if ( block != null ) {
			block.setFrozen( frozen );
		}
	}

	public void clear() {
		((MemoryControlArea)fTabFolder.getSelection().getControl()).clear();
	}

	public void refreshMemoryBlock() {
		((MemoryControlArea)fTabFolder.getSelection().getControl()).refreshMemoryBlock();
	}

	public boolean showAscii() {
		return ((MemoryControlArea)fTabFolder.getSelection().getControl()).getPresentation().displayASCII();
	}

	public void setShowAscii( boolean show ) {
		((MemoryControlArea)fTabFolder.getSelection().getControl()).getPresentation().setDisplayAscii( show );
		((MemoryControlArea)fTabFolder.getSelection().getControl()).refresh();
	}

	public boolean canShowAscii() {
		return ((MemoryControlArea)fTabFolder.getSelection().getControl()).getPresentation().canDisplayAscii();
	}

	public int getCurrentFormat() {
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		return (block != null) ? block.getFormat() : 0;
	}

	public void setFormat( int format ) throws DebugException {
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		if ( block != null ) {
			block.reformat( format, block.getWordSize(), block.getNumberOfRows(), block.getNumberOfColumns() );
			((MemoryControlArea)fTabFolder.getSelection().getControl()).refresh();
		}
	}

	public int getCurrentWordSize() {
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		return (block != null) ? block.getWordSize() : 0;
	}

	public void setWordSize( int size ) throws DebugException {
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		if ( block != null ) {
			block.reformat( block.getFormat(), size, block.getNumberOfRows(), block.getNumberOfColumns() );
			((MemoryControlArea)fTabFolder.getSelection().getControl()).refresh();
		}
	}

	public int getCurrentNumberOfColumns() {
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		return (block != null) ? block.getNumberOfColumns() : 0;
	}

	public void setNumberOfColumns( int numberOfColumns ) throws DebugException {
		IFormattedMemoryBlock block = ((MemoryControlArea)fTabFolder.getSelection().getControl()).getMemoryBlock();
		if ( block != null ) {
			block.reformat( block.getFormat(), block.getWordSize(), block.getNumberOfRows(), numberOfColumns );
			((MemoryControlArea)fTabFolder.getSelection().getControl()).refresh();
		}
	}

	protected ITextOperationTarget getTextOperationTarget() {
		return (MemoryControlArea)fTabFolder.getSelection().getControl();
	}
}