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

package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 * A widget that displays the textual content of the memory block.
 * 
 * @since Jul 25, 2002
 */
public class MemoryText
{	
	private StyledText fText = null;
	private MemoryPresentation fPresentation = null;
	private boolean fUpdating = false;

	/**
	 * Constructor for MemoryText.
	 * 
	 * @param parent
	 * @param style
	 */
	public MemoryText( Composite parent, 
					   int style, 
					   MemoryPresentation presentation )
	{
		fText = new StyledText( parent, style );
		fText.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		fPresentation = presentation;
		initialize();
	}

	private void initialize()
	{
		// Switch to overwrite mode
		fText.invokeAction( SWT.INSERT );
		// Unmap INSERT key action to preserve the overwrite mode
		fText.setKeyBinding( SWT.INSERT, SWT.NULL );

		fText.addExtendedModifyListener( 
					new ExtendedModifyListener() 
						{
							public void modifyText( ExtendedModifyEvent e ) 
							{
								handleExtendedModify( e );
							}
						} );

		fText.addVerifyKeyListener( 
					new VerifyKeyListener()
						{
							public void verifyKey( VerifyEvent e ) 
							{
								handleVerifyKey( e );
							}
						} );
		refresh();
	}

	protected void handleExtendedModify( ExtendedModifyEvent event )
	{
		if ( fUpdating )
			return;
		if ( event.length != 1 )
			return;
		int caretOffset = fText.getCaretOffset();
		fText.getCaret().setVisible( false );
		char ch = fText.getText().charAt( event.start );
		restoreText( event.start, event.length, event.replacedText );
		fPresentation.setItemValue( event.start, ch );
		fText.setCaretOffset( caretOffset );
		fText.getCaret().setVisible( true );
	}
	
	public void refresh()
	{
		int offset = fText.getCaretOffset();
		fText.getCaret().setVisible( false );
		fText.setFont( new Font( fText.getDisplay(), getFontData() ) );
		fText.setBackground( getBackgroundColor() );
		fText.setForeground( getForegroundColor() );
		fText.setText( fPresentation.getText() );
		Point[] zones = fPresentation.getChangedZones();
		for ( int i = 0; i < zones.length; ++i )
		{
			fText.setStyleRange( new StyleRange( zones[i].x,
												 zones[i].y - zones[i].x + 1,
												 getChangedColor(),
												 getBackgroundColor() ) );

		}
		zones = fPresentation.getAddressZones();
		boolean isStartAddressChanged = fPresentation.isStartAddressChanged();
		for ( int i = 0; i < zones.length; ++i )
		{
			fText.setStyleRange( new StyleRange( zones[i].x,
												 zones[i].y - zones[i].x + 1,
												 ( isStartAddressChanged ) ? getChangedColor() : getAddressColor(),
												 getBackgroundColor() ) );
		}
		fText.redraw();
		fText.setCaretOffset( offset );
		fText.getCaret().setVisible( true );
	}
	
	protected void handleVerifyKey( VerifyEvent event ) 
	{
		if ( event.character == SWT.LF ||
			 event.character == SWT.CR || 
			 event.character == SWT.BS ||
			 event.character == SWT.DEL )
		{
			event.doit = false;
			return;
		}
		if ( Character.isISOControl( event.character ) )
			return;
		if ( getSelectionCount() != 0 )
		{
			event.doit = false;
			return;
		}
		if ( !fPresentation.isAcceptable( event.character, fText.getCaretOffset() ) )
			event.doit = false;
	}
	
	private FontData getFontData()
	{
		IPreferenceStore pstore = CDebugUIPlugin.getDefault().getPreferenceStore();
		FontData fontData = PreferenceConverter.getFontData( pstore, ICDebugPreferenceConstants.MEMORY_FONT );
		return fontData;
	}
	
	private Color getForegroundColor()
	{
		return CDebugUIPlugin.getPreferenceColor( ICDebugPreferenceConstants.MEMORY_FOREGROUND_RGB );
	}
	
	private Color getBackgroundColor()
	{
		return CDebugUIPlugin.getPreferenceColor( ICDebugPreferenceConstants.MEMORY_BACKGROUND_RGB );
	}
	
	private Color getAddressColor()
	{
		return CDebugUIPlugin.getPreferenceColor( ICDebugPreferenceConstants.MEMORY_ADDRESS_RGB );
	}
	
	private Color getChangedColor()
	{
		return CDebugUIPlugin.getPreferenceColor( ICDebugPreferenceConstants.MEMORY_CHANGED_RGB );
	}
	
	public void changeFont()
	{
		Font oldFont = fText.getFont();
		fText.setFont( new Font( fText.getDisplay(), getFontData() ) );
		oldFont.dispose();
	}
	
	public void setForegroundColor()
	{
		fText.setForeground( CDebugUIPlugin.getPreferenceColor( ICDebugPreferenceConstants.MEMORY_FOREGROUND_RGB ) );
	}
	
	public void setBackgroundColor()
	{
		fText.setBackground( CDebugUIPlugin.getPreferenceColor( ICDebugPreferenceConstants.MEMORY_BACKGROUND_RGB ) );
	}
	
	public void setChangedColor()
	{
		Point[] zones = fPresentation.getChangedZones();
		for ( int i = 0; i < zones.length; ++i )
		{
			fText.setStyleRange( new StyleRange( zones[i].x,
												 zones[i].y - zones[i].x + 1,
												 getChangedColor(),
												 getBackgroundColor() ) );
		}
	}
	
	public void setAddressColor()
	{
		Point[] zones = fPresentation.getAddressZones();
		for ( int i = 0; i < zones.length; ++i )
		{
			fText.setStyleRange( new StyleRange( zones[i].x,
												 zones[i].y - zones[i].x + 1,
												 getAddressColor(),
												 getBackgroundColor() ) );
		}
	}
	
	public void setDirtyColor()
	{
/*
		Point[] zones = fPresentation.getDirtyZones();
		for ( int i = 0; i < zones.length; ++i )
		{
			fText.setStyleRange( new StyleRange( zones[i].x,
												 zones[i].y - zones[i].x + 1,
												 getDirtyColor(),
												 getBackgroundColor() ) );
		}
*/
	}
	
	protected void setEditable( boolean editable )
	{
		fText.setEditable( editable );
	}
	
	protected int getSelectionCount()
	{
		return fText.getSelectionCount();
	}
	
	protected Control getControl()
	{
		return fText;
	}
/*	
	protected void update( TextReplacement[] trs )
	{
		fUpdating = true;
		for ( int i = 0; i < trs.length; ++i )
		{
			fText.replaceTextRange( trs[i].getStart(), 
							  		trs[i].getText().length(),
							  		trs[i].getText() );
			fText.setStyleRange( new StyleRange( trs[i].getStart(), 
							  					 trs[i].getText().length(),
										   		 getDirtyColor(),
										   		 getBackgroundColor() ) );
			fText.redrawRange( trs[i].getStart(), trs[i].getText().length(), false );
		}
		saveChanges();
		fUpdating = false;
		updateTitle();
	}
*/
	private void restoreText( int start, int length, String text )
	{
		fUpdating = true;
		fText.replaceTextRange( start, length, text );
		fUpdating = false;
	}
/*
	private void updateTitle()
	{
		if ( fText.getParent() instanceof MemoryControlArea )
		{
			String title = ((MemoryControlArea)fText.getParent()).getTitle();
			if ( title.charAt( 0 ) == '*' )
			{
				title = title.substring( 1 );
			}
			if ( fPresentation.isDirty() )
			{
				title = '*' + title;
			}
			((MemoryControlArea)fText.getParent()).setTitle( title );
		}
	}
	
	private void saveChanges()
	{
		if ( fText.getParent() instanceof MemoryControlArea )
		{
			((MemoryControlArea)fText.getParent()).saveChanges();
		}
	}
*/
}
