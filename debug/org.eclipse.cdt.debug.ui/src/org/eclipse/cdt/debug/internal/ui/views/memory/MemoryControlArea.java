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

import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICMemoryManager;
import org.eclipse.cdt.debug.core.model.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The tab content in the memory view.
 */
public class MemoryControlArea extends Composite implements ITextOperationTarget {

	private MemoryView fMemoryView;

	private MemoryPresentation fPresentation;

	private int fIndex = 0;

	private ICMemoryManager fMemoryManager = null;

	private Text fAddressText;

	private Button fEvaluateButton;

	private MemoryText fMemoryText;

	private int fFormat = ICMemoryManager.MEMORY_FORMAT_HEX;

	private int fWordSize = ICMemoryManager.MEMORY_SIZE_BYTE;

	private int fNumberOfRows = 40;

	private int fNumberOfColumns = 16;

	private char fPaddingChar = '.';

	/**
	 * Constructor for MemoryControlArea.
	 * 
	 * @param parent
	 * @param style
	 */
	public MemoryControlArea( Composite parent, int style, int index, MemoryView view ) {
		super( parent, style );
		fMemoryView = view;
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		GridData gridData = new GridData( GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL );
		setLayout( layout );
		setLayoutData( gridData );
		setIndex( index );
		fPresentation = createPresentation();
		fAddressText = createAddressText( this );
		fMemoryText = createMemoryText( this, style, fPresentation );
		setDefaultPreferences();
		updateToolTipText();
	}

	private void setDefaultPreferences() {
		char[] paddingCharStr = CDebugUIPlugin.getDefault().getPreferenceStore().getString( ICDebugPreferenceConstants.PREF_MEMORY_PADDING_CHAR ).toCharArray();
		setPaddingChar( (paddingCharStr.length > 0) ? paddingCharStr[0] : '.' );
		fPresentation.setDisplayAscii( CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean( ICDebugPreferenceConstants.PREF_MEMORY_SHOW_ASCII ) );
	}

	private MemoryPresentation createPresentation() {
		return new MemoryPresentation();
	}

	public MemoryPresentation getPresentation() {
		return fPresentation;
	}

	private Text createAddressText( Composite parent ) {
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setLayout( new GridLayout( 3, false ) );
		composite.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		// create label
		Label label = new Label( composite, SWT.RIGHT );
		label.setText( MemoryViewMessages.getString( "MemoryControlArea.0" ) ); //$NON-NLS-1$
		label.pack();
		// create address text
		Text text = new Text( composite, SWT.BORDER );
		text.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		text.addTraverseListener( new TraverseListener() {

			public void keyTraversed( TraverseEvent e ) {
				if ( e.detail == SWT.TRAVERSE_RETURN && e.stateMask == 0 ) {
					e.doit = false;
					handleAddressEnter();
				}
			}
		} );
		text.addFocusListener( new FocusListener() {

			public void focusGained( FocusEvent e ) {
				getMemoryView().updateObjects();
			}

			public void focusLost( FocusEvent e ) {
				getMemoryView().updateObjects();
			}
		} );
		text.addModifyListener( new ModifyListener() {

			public void modifyText( ModifyEvent e ) {
				handleAddressModification();
			}
		} );
		text.addKeyListener( new KeyListener() {

			public void keyPressed( KeyEvent e ) {
				getMemoryView().updateObjects();
			}

			public void keyReleased( KeyEvent e ) {
				getMemoryView().updateObjects();
			}
		} );
		text.addMouseListener( new MouseListener() {

			public void mouseDoubleClick( MouseEvent e ) {
				getMemoryView().updateObjects();
			}

			public void mouseDown( MouseEvent e ) {
				getMemoryView().updateObjects();
			}

			public void mouseUp( MouseEvent e ) {
				getMemoryView().updateObjects();
			}
		} );
		fEvaluateButton = new Button( composite, SWT.PUSH );
		fEvaluateButton.setText( MemoryViewMessages.getString( "MemoryControlArea.1" ) ); //$NON-NLS-1$
		fEvaluateButton.setToolTipText( MemoryViewMessages.getString( "MemoryControlArea.2" ) ); //$NON-NLS-1$
		fEvaluateButton.addSelectionListener( new SelectionAdapter() {

			public void widgetSelected( SelectionEvent e ) {
				evaluateAddressExpression();
			}
		} );
		return text;
	}

	private MemoryText createMemoryText( Composite parent, int styles, MemoryPresentation presentation ) {
		return new MemoryText( parent, SWT.BORDER | SWT.HIDE_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL, presentation );
	}

	protected void handleAddressEnter() {
		if ( getMemoryManager() != null ) {
			String address = fAddressText.getText().trim();
			try {
				removeBlock();
				if ( address.length() > 0 ) {
					createBlock( address );
				}
			}
			catch( DebugException e ) {
				CDebugUIPlugin.errorDialog( MemoryViewMessages.getString( "MemoryControlArea.3" ), e.getStatus() ); //$NON-NLS-1$
			}
			refresh();
			getMemoryView().updateObjects();
		}
	}

	public void propertyChange( PropertyChangeEvent event ) {
		if ( event.getProperty().equals( ICDebugPreferenceConstants.MEMORY_BACKGROUND_RGB ) ) {
			fMemoryText.setBackgroundColor();
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.MEMORY_FOREGROUND_RGB ) ) {
			fMemoryText.setForegroundColor();
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.MEMORY_FONT ) ) {
			fMemoryText.changeFont();
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.MEMORY_ADDRESS_RGB ) ) {
			fMemoryText.setAddressColor();
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.MEMORY_CHANGED_RGB ) ) {
			fMemoryText.setChangedColor();
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.MEMORY_DIRTY_RGB ) ) {
			fMemoryText.setDirtyColor();
		}
		else if ( event.getProperty().equals( ICDebugPreferenceConstants.PREF_MEMORY_PADDING_CHAR ) ) {
			String paddingCharString = (String)event.getNewValue();
			setPaddingChar( (paddingCharString.length() > 0) ? paddingCharString.charAt( 0 ) : '.' );
			refresh();
		}
	}

	public void setInput( Object input ) {
		setMemoryManager( (input instanceof ICMemoryManager) ? (ICMemoryManager)input : null );
		getPresentation().setMemoryBlock( getMemoryBlock() );
		setState();
		refresh();
	}

	protected void refresh() {
		fAddressText.setText( (getPresentation() != null) ? getPresentation().getAddressExpression() : "" ); //$NON-NLS-1$
		fMemoryText.refresh();
		getMemoryView().updateObjects();
		updateToolTipText();
	}

	protected void setMemoryManager( ICMemoryManager mm ) {
		fMemoryManager = mm;
	}

	protected ICMemoryManager getMemoryManager() {
		return fMemoryManager;
	}

	protected IFormattedMemoryBlock getMemoryBlock() {
		return (getMemoryManager() != null) ? getMemoryManager().getBlock( getIndex() ) : null;
	}

	protected int getIndex() {
		return fIndex;
	}

	protected void setIndex( int index ) {
		fIndex = index;
	}

	private void createBlock( String address ) throws DebugException {
		if ( getMemoryManager() != null ) {
			getMemoryManager().setBlockAt( getIndex(), CDebugModel.createFormattedMemoryBlock( (IDebugTarget)getMemoryManager().getAdapter( IDebugTarget.class ), address, getFormat(), getWordSize(), getNumberOfRows(), getNumberOfColumns(), getPaddingChar() ) );
			getMemoryBlock().setFrozen( !CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean( ICDebugPreferenceConstants.PREF_MEMORY_AUTO_REFRESH ) );
			getPresentation().setMemoryBlock( getMemoryBlock() );
		}
		setMemoryTextState();
		updateToolTipText();
	}

	private void removeBlock() throws DebugException {
		if ( getMemoryManager() != null ) {
			getMemoryManager().removeBlock( getIndex() );
			getPresentation().setMemoryBlock( null );
		}
		setMemoryTextState();
		updateToolTipText();
	}

	public int getFormat() {
		return fFormat;
	}

	public int getNumberOfColumns() {
		return fNumberOfColumns;
	}

	public int getNumberOfRows() {
		return fNumberOfRows;
	}

	public char getPaddingChar() {
		return fPaddingChar;
	}

	public int getWordSize() {
		return fWordSize;
	}

	public void setFormat( int format ) {
		fFormat = format;
	}

	public void setNumberOfColumns( int numberOfColumns ) {
		fNumberOfColumns = numberOfColumns;
	}

	public void setNumberOfRows( int numberOfRows ) {
		fNumberOfRows = numberOfRows;
	}

	public void setPaddingChar( char paddingChar ) {
		fPaddingChar = paddingChar;
		if ( getMemoryBlock() != null ) {
			try {
				getMemoryBlock().reformat( getMemoryBlock().getFormat(), getMemoryBlock().getWordSize(), getMemoryBlock().getNumberOfRows(), getMemoryBlock().getNumberOfColumns(), fPaddingChar );
			}
			catch( DebugException e ) {
				// ignore
			}
		}
	}

	public void setWordSize( int wordSize ) {
		fWordSize = wordSize;
	}

	private void enableAddressText( boolean enable ) {
		fAddressText.setEnabled( enable );
	}

	protected void setState() {
		enableAddressText( getMemoryManager() != null );
		setMemoryTextState();
	}

	private void setMemoryTextState() {
		fMemoryText.setEditable( getMemoryManager() != null && getMemoryBlock() != null );
	}

	protected MemoryText getMemoryText() {
		return fMemoryText;
	}

	protected void clear() {
		fAddressText.setText( "" ); //$NON-NLS-1$
		handleAddressEnter();
		updateToolTipText();
	}

	/**
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() {
		if ( getPresentation() != null ) {
			getPresentation().dispose();
		}
		super.dispose();
	}

	protected String getTitle() {
		if ( getParent() instanceof CTabFolder ) {
			CTabItem[] tabItems = ((CTabFolder)getParent()).getItems();
			return tabItems[fIndex].getText();
		}
		return ""; //$NON-NLS-1$
	}

	protected void setTitle( String title ) {
		if ( getParent() instanceof CTabFolder ) {
			CTabItem[] tabItems = ((CTabFolder)getParent()).getItems();
			tabItems[fIndex].setText( title );
		}
	}

	protected void setTabItemToolTipText( String text ) {
		String newText = replaceMnemonicCharacters( text );
		if ( getParent() instanceof CTabFolder ) {
			CTabItem[] tabItems = ((CTabFolder)getParent()).getItems();
			tabItems[fIndex].setToolTipText( MemoryViewMessages.getString( "MemoryControlArea.4" ) + (fIndex + 1) + ((newText.length() > 0) ? (": " + newText) : "") ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	protected void refreshMemoryBlock() {
		if ( getMemoryBlock() != null ) {
			try {
				getMemoryBlock().refresh();
			}
			catch( DebugException e ) {
				CDebugUIPlugin.errorDialog( MemoryViewMessages.getString( "MemoryControlArea.7" ), e.getStatus() ); //$NON-NLS-1$
			}
		}
	}

	private void updateToolTipText() {
		setTabItemToolTipText( fAddressText.getText().trim() );
	}

	private String replaceMnemonicCharacters( String text ) {
		StringBuffer sb = new StringBuffer( text.length() );
		for( int i = 0; i < text.length(); ++i ) {
			char ch = text.charAt( i );
			sb.append( ch );
			if ( ch == '&' ) {
				sb.append( ch );
			}
		}
		return sb.toString();
	}

	protected void handleAddressModification() {
		fEvaluateButton.setEnabled( fAddressText.getText().trim().length() > 0 );
	}

	protected void evaluateAddressExpression() {
		if ( getMemoryManager() != null ) {
			if ( getMemoryBlock() == null ) {
				String expression = fAddressText.getText().trim();
				try {
					removeBlock();
					if ( expression.length() > 0 ) {
						createBlock( expression );
					}
				}
				catch( DebugException e ) {
					CDebugUIPlugin.errorDialog( MemoryViewMessages.getString( "MemoryControlArea.8" ), e.getStatus() ); //$NON-NLS-1$
				}
			}
			if ( getMemoryBlock() != null ) {
				fAddressText.setText( CDebugUIUtils.toHexAddressString( getMemoryBlock().getStartAddress() ) );
				handleAddressEnter();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.ITextOperationTarget#canDoOperation(int)
	 */
	public boolean canDoOperation( int operation ) {
		switch( operation ) {
			case CUT:
			case COPY:
				return (fAddressText != null && fAddressText.isFocusControl() && fAddressText.isEnabled() && fAddressText.getSelectionCount() > 0);
			case PASTE:
			case SELECT_ALL:
				return (fAddressText != null && fAddressText.isFocusControl() && fAddressText.isEnabled());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.ITextOperationTarget#doOperation(int)
	 */
	public void doOperation( int operation ) {
		switch( operation ) {
			case CUT:
				fAddressText.cut();
				break;
			case COPY:
				fAddressText.copy();
				break;
			case PASTE:
				fAddressText.paste();
				break;
			case SELECT_ALL:
				fAddressText.selectAll();
				break;
		}
	}

	protected MemoryView getMemoryView() {
		return fMemoryView;
	}
}
