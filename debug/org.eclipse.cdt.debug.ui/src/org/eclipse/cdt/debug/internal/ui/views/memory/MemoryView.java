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

import org.eclipse.cdt.debug.core.ICMemoryManager;
import org.eclipse.cdt.debug.core.model.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.actions.AutoRefreshMemoryAction;
import org.eclipse.cdt.debug.internal.ui.actions.ClearMemoryAction;
import org.eclipse.cdt.debug.internal.ui.actions.MemoryActionSelectionGroup;
import org.eclipse.cdt.debug.internal.ui.actions.MemoryFormatAction;
import org.eclipse.cdt.debug.internal.ui.actions.MemoryNumberOfColumnAction;
import org.eclipse.cdt.debug.internal.ui.actions.MemorySizeAction;
import org.eclipse.cdt.debug.internal.ui.actions.RefreshMemoryAction;
import org.eclipse.cdt.debug.internal.ui.actions.ShowAsciiAction;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * This view shows the content of the memory blocks associated with the selected debug target.
 */
public class MemoryView extends AbstractDebugEventHandlerView implements ISelectionListener, IPropertyChangeListener, IDebugExceptionHandler {

	private IDebugModelPresentation fModelPresentation = null;

	private MemoryActionSelectionGroup fMemoryFormatGroup = null;

	private MemoryActionSelectionGroup fMemorySizeGroup = null;

	private MemoryActionSelectionGroup fMemoryNumberOfColumnsGroup = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer( Composite parent ) {
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
		final MemoryViewer viewer = new MemoryViewer( parent, this );
		viewer.setContentProvider( createContentProvider() );
		viewer.setLabelProvider( getModelPresentation() );
		getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		setEventHandler( createEventHandler( viewer ) );
		return viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
		IAction action = null;
		action = new MemoryViewAction( this, ITextOperationTarget.CUT );
		String cutStr = MemoryViewMessages.getString( "MemoryView.0" ); //$NON-NLS-1$
		action.setText( cutStr );
		action.setToolTipText( cutStr );
		action.setDescription( cutStr );
		setGlobalAction( ITextEditorActionConstants.CUT, (MemoryViewAction)action );
		action = new MemoryViewAction( this, ITextOperationTarget.COPY );
		String copyStr = MemoryViewMessages.getString( "MemoryView.1" ); //$NON-NLS-1$
		action.setText( copyStr );
		action.setToolTipText( copyStr );
		action.setDescription( copyStr );
		setGlobalAction( ITextEditorActionConstants.COPY, (MemoryViewAction)action );
		action = new MemoryViewAction( this, ITextOperationTarget.PASTE );
		String pasteStr = MemoryViewMessages.getString( "MemoryView.2" ); //$NON-NLS-1$
		action.setText( pasteStr );
		action.setToolTipText( pasteStr );
		action.setDescription( pasteStr );
		setGlobalAction( ITextEditorActionConstants.PASTE, (MemoryViewAction)action );
		action = new MemoryViewAction( this, ITextOperationTarget.SELECT_ALL );
		String selectAllStr = MemoryViewMessages.getString( "MemoryView.3" ); //$NON-NLS-1$
		action.setText( selectAllStr );
		action.setToolTipText( selectAllStr );
		action.setDescription( selectAllStr );
		setGlobalAction( ITextEditorActionConstants.SELECT_ALL, (MemoryViewAction)action );
		action = new RefreshMemoryAction( (MemoryViewer)getViewer() );
		action.setEnabled( false );
		setAction( "RefreshMemory", action ); //$NON-NLS-1$
		add( (RefreshMemoryAction)action );
		action = new AutoRefreshMemoryAction( (MemoryViewer)getViewer() );
		action.setEnabled( false );
		action.setChecked( CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean( ICDebugPreferenceConstants.PREF_MEMORY_AUTO_REFRESH ) );
		setAction( "AutoRefreshMemory", action ); //$NON-NLS-1$
		add( (AutoRefreshMemoryAction)action );
		action = new ClearMemoryAction( (MemoryViewer)getViewer() );
		action.setEnabled( false );
		setAction( "ClearMemory", action ); //$NON-NLS-1$
		add( (ClearMemoryAction)action );
		action = new ShowAsciiAction( (MemoryViewer)getViewer() );
		action.setEnabled( false );
		action.setChecked( CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean( ICDebugPreferenceConstants.PREF_MEMORY_SHOW_ASCII ) );
		setAction( "ShowAscii", action ); //$NON-NLS-1$
		add( (ShowAsciiAction)action );
		fMemoryFormatGroup = new MemoryActionSelectionGroup();
		createFormatActionGroup( fMemoryFormatGroup );
		fMemorySizeGroup = new MemoryActionSelectionGroup();
		createSizeActionGroup( fMemorySizeGroup );
		fMemoryNumberOfColumnsGroup = new MemoryActionSelectionGroup();
		createNumberOfColumnsActionGroup( fMemoryNumberOfColumnsGroup );
		// set initial content here, as viewer has to be set
		setInitialContent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return ICDebugHelpContextIds.MEMORY_VIEW;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(IMenuManager)
	 */
	protected void fillContextMenu( IMenuManager menu ) {
		menu.add( new Separator( ICDebugUIConstants.EMPTY_MEMORY_GROUP ) );
		menu.add( new Separator( ICDebugUIConstants.MEMORY_GROUP ) );
		menu.add( new Separator( ICDebugUIConstants.EMPTY_FORMAT_GROUP ) );
		menu.add( new Separator( ICDebugUIConstants.FORMAT_GROUP ) );
		menu.add( new Separator( IDebugUIConstants.EMPTY_RENDER_GROUP ) );
		menu.add( new Separator( IDebugUIConstants.RENDER_GROUP ) );
		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
		menu.appendToGroup( ICDebugUIConstants.MEMORY_GROUP, getAction( "AutoRefreshMemory" ) ); //$NON-NLS-1$
		menu.appendToGroup( ICDebugUIConstants.MEMORY_GROUP, getAction( "RefreshMemory" ) ); //$NON-NLS-1$
		menu.appendToGroup( ICDebugUIConstants.MEMORY_GROUP, getAction( "ClearMemory" ) ); //$NON-NLS-1$
		MenuManager subMenu = new MenuManager( MemoryViewMessages.getString( "MemoryView.4" ) ); //$NON-NLS-1$
		{
			IAction[] actions = fMemoryFormatGroup.getActions();
			for( int i = 0; i < actions.length; ++i ) {
				subMenu.add( actions[i] );
			}
		}
		menu.appendToGroup( ICDebugUIConstants.FORMAT_GROUP, subMenu );
		subMenu = new MenuManager( MemoryViewMessages.getString( "MemoryView.5" ) ); //$NON-NLS-1$
		{
			IAction[] actions = fMemorySizeGroup.getActions();
			for( int i = 0; i < actions.length; ++i ) {
				subMenu.add( actions[i] );
			}
		}
		menu.appendToGroup( ICDebugUIConstants.FORMAT_GROUP, subMenu );
		subMenu = new MenuManager( MemoryViewMessages.getString( "MemoryView.6" ) ); //$NON-NLS-1$
		{
			IAction[] actions = fMemoryNumberOfColumnsGroup.getActions();
			for( int i = 0; i < actions.length; ++i ) {
				subMenu.add( actions[i] );
			}
		}
		menu.appendToGroup( ICDebugUIConstants.FORMAT_GROUP, subMenu );
		menu.appendToGroup( IDebugUIConstants.RENDER_GROUP, getAction( "ShowAscii" ) ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar( IToolBarManager tbm ) {
		tbm.add( new Separator( this.getClass().getName() ) );
		tbm.add( new Separator( ICDebugUIConstants.MEMORY_GROUP ) );
		tbm.add( getAction( "AutoRefreshMemory" ) ); //$NON-NLS-1$
		tbm.add( getAction( "RefreshMemory" ) ); //$NON-NLS-1$
		tbm.add( getAction( "ClearMemory" ) ); //$NON-NLS-1$
		tbm.add( new Separator( IDebugUIConstants.RENDER_GROUP ) );
		tbm.add( getAction( "ShowAscii" ) ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
		if ( selection instanceof IStructuredSelection ) {
			setViewerInput( (IStructuredSelection)selection );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent event ) {
		((MemoryViewer)getViewer()).propertyChange( event );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler#handleException(DebugException)
	 */
	public void handleException( DebugException e ) {
	}

	/**
	 * Remove myself as a selection listener and preference change listener.
	 * 
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		removeActionGroup( fMemoryFormatGroup );
		fMemoryFormatGroup.dispose();
		removeActionGroup( fMemorySizeGroup );
		fMemorySizeGroup.dispose();
		removeActionGroup( fMemoryNumberOfColumnsGroup );
		fMemoryNumberOfColumnsGroup.dispose();
		remove( (ShowAsciiAction)getAction( "ShowAscii" ) ); //$NON-NLS-1$
		remove( (ClearMemoryAction)getAction( "ClearMemory" ) ); //$NON-NLS-1$
		remove( (RefreshMemoryAction)getAction( "RefreshMemory" ) ); //$NON-NLS-1$
		remove( (AutoRefreshMemoryAction)getAction( "AutoRefreshMemory" ) ); //$NON-NLS-1$
		getSite().getPage().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		CDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener( this );
		super.dispose();
	}

	protected void setViewerInput( IStructuredSelection ssel ) {
		ICMemoryManager mm = null;
		if ( ssel != null && ssel.size() == 1 ) {
			Object input = ssel.getFirstElement();
			if ( input instanceof IDebugElement ) {
				mm = (ICMemoryManager)((IDebugElement)input).getDebugTarget().getAdapter( ICMemoryManager.class );
			}
		}
		Object current = getViewer().getInput();
		if ( current != null && current.equals( mm ) ) {
			return;
		}
		showViewer();
		getViewer().setInput( mm );
		updateObjects();
	}

	private IContentProvider createContentProvider() {
		return new MemoryViewContentProvider();
	}

	private IDebugModelPresentation getModelPresentation() {
		if ( fModelPresentation == null ) {
			fModelPresentation = CDebugUIPlugin.getDebugModelPresentation();
		}
		return fModelPresentation;
	}

	/**
	 * Creates this view's event handler.
	 * 
	 * @param viewer
	 *            the viewer associated with this view
	 * @return an event handler
	 */
	protected AbstractDebugEventHandler createEventHandler( Viewer viewer ) {
		return new MemoryViewEventHandler( this );
	}

	/**
	 * Initializes the viewer input on creation
	 */
	protected void setInitialContent() {
		ISelection selection = getSite().getPage().getSelection( IDebugUIConstants.ID_DEBUG_VIEW );
		if ( selection instanceof IStructuredSelection && !selection.isEmpty() ) {
			setViewerInput( (IStructuredSelection)selection );
		}
		else {
			setViewerInput( null );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#createContextMenu(Control)
	 */
	protected void createContextMenu( Control menuControl ) {
		CTabItem[] items = ((MemoryViewer)getViewer()).getTabFolder().getItems();
		for( int i = 0; i < items.length; ++i ) {
			super.createContextMenu( ((MemoryControlArea)items[i].getControl()).getMemoryText().getControl() );
		}
	}

	private void createFormatActionGroup( MemoryActionSelectionGroup group ) {
		int[] formats = new int[]{ IFormattedMemoryBlock.MEMORY_FORMAT_HEX, IFormattedMemoryBlock.MEMORY_FORMAT_SIGNED_DECIMAL, IFormattedMemoryBlock.MEMORY_FORMAT_UNSIGNED_DECIMAL };
		for( int i = 0; i < formats.length; ++i ) {
			MemoryFormatAction action = new MemoryFormatAction( group, (MemoryViewer)getViewer(), formats[i] );
			action.setEnabled( false );
			setAction( action.getActionId(), action ); //$NON-NLS-1$
			add( action );
			group.addAction( action );
		}
	}

	private void createSizeActionGroup( MemoryActionSelectionGroup group ) {
		int[] ids = new int[]{ IFormattedMemoryBlock.MEMORY_SIZE_BYTE, IFormattedMemoryBlock.MEMORY_SIZE_HALF_WORD, IFormattedMemoryBlock.MEMORY_SIZE_WORD, IFormattedMemoryBlock.MEMORY_SIZE_DOUBLE_WORD };
		for( int i = 0; i < ids.length; ++i ) {
			MemorySizeAction action = new MemorySizeAction( group, (MemoryViewer)getViewer(), ids[i] );
			action.setEnabled( false );
			setAction( action.getActionId(), action ); //$NON-NLS-1$
			add( action );
			group.addAction( action );
		}
	}

	private void createNumberOfColumnsActionGroup( MemoryActionSelectionGroup group ) {
		int[] nocs = new int[]{ IFormattedMemoryBlock.MEMORY_NUMBER_OF_COLUMNS_1, IFormattedMemoryBlock.MEMORY_NUMBER_OF_COLUMNS_2, IFormattedMemoryBlock.MEMORY_NUMBER_OF_COLUMNS_4, IFormattedMemoryBlock.MEMORY_NUMBER_OF_COLUMNS_8, IFormattedMemoryBlock.MEMORY_NUMBER_OF_COLUMNS_16 };
		for( int i = 0; i < nocs.length; ++i ) {
			MemoryNumberOfColumnAction action = new MemoryNumberOfColumnAction( group, (MemoryViewer)getViewer(), nocs[i] );
			action.setEnabled( false );
			setAction( action.getActionId(), action ); //$NON-NLS-1$
			add( action );
			group.addAction( action );
		}
	}

	private void removeActionGroup( MemoryActionSelectionGroup group ) {
		IAction[] actions = group.getActions();
		for( int i = 0; i < actions.length; ++i ) {
			remove( (IUpdate)actions[i] );
		}
	}

	private void setGlobalAction( String actionId, MemoryViewAction action ) {
		add( action );
		getViewSite().getActionBars().setGlobalActionHandler( actionId, action );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( ITextOperationTarget.class.equals( adapter ) ) {
			return ((MemoryViewer)getViewer()).getTextOperationTarget();
		}
		return super.getAdapter( adapter );
	}
}
