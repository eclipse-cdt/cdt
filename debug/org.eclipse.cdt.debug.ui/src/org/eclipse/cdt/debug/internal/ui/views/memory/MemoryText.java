/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.views.memory;

import java.util.LinkedList;
import java.util.List;

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
		if ( event.length != 1 )
			return;
		int offset = fText.getCaretOffset() - 1;
		int size = fPresentation.getItemSize( offset );
		fPresentation.setItem( offset, fText.getText().substring( offset, offset + size ) );
		Point[] zones = fPresentation.getDirtyZones();
		refresh( zones, fPresentation.getText( zones ) );
	}
	
	public void refresh()
	{
		fText.setFont( new Font( fText.getDisplay(), getFontData() ) );
		fText.setBackground( getBackgroundColor() );
		fText.setForeground( getForegroundColor() );
		fText.setText( fPresentation.getText() );
		List list = new LinkedList();
		Point[] zones = fPresentation.getChangedZones();
		for ( int i = 0; i < zones.length; ++i )
			list.add( new StyleRange( zones[i].x,
									  zones[i].y - zones[i].x + 1,
									  getChangedColor(),
									  getBackgroundColor() ) );
		zones = fPresentation.getAddressZones();
		for ( int i = 0; i < zones.length; ++i )
			list.add( new StyleRange( zones[i].x,
									  zones[i].y - zones[i].x + 1,
									  getAddressColor(),
									  getBackgroundColor() ) );
		zones = fPresentation.getDirtyZones();
		for ( int i = 0; i < zones.length; ++i )
			list.add( new StyleRange( zones[i].x,
									  zones[i].y - zones[i].x + 1,
									  getDirtyColor(),
									  getBackgroundColor() ) );
		fText.setStyleRanges( (StyleRange[])list.toArray( new StyleRange[list.size()] ) );
		fText.redraw();
	}
	
	private void refresh( Point[] zones, String[] items )
	{
		int count = ( zones.length < items.length ) ? zones.length : items.length;
		for ( int i = 0; i < count; ++i )
		{
			fText.replaceTextRange( zones[i].x, 
							  		zones[i].y - zones[i].x + 1,
							  		items[i] );
			fText.setStyleRange( new StyleRange( zones[i].x, 
										   		 zones[i].y - zones[i].x + 1,
										   		 getDirtyColor(),
										   		 getBackgroundColor() ) );
			fText.redrawRange( zones[i].x, zones[i].y - zones[i].x + 1, false );
		}
	}

	protected void handleVerifyKey( VerifyEvent event ) 
	{
		if ( event.character == SWT.LF ||
			 event.character == SWT.CR || 
			 event.character == SWT.BS ||
			 event.character == SWT.DEL ||
			 !fPresentation.isAcceptable( event.character, fText.getCaretOffset() ) )
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
	
	private Color getDirtyColor()
	{
		return CDebugUIPlugin.getPreferenceColor( ICDebugPreferenceConstants.MEMORY_DIRTY_RGB );
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
		List list = new LinkedList();
		Point[] zones = fPresentation.getChangedZones();
		for ( int i = 0; i < zones.length; ++i )
			list.add( new StyleRange( zones[i].x,
									  zones[i].y - zones[i].x + 1,
									  getChangedColor(),
									  getBackgroundColor() ) );
		fText.setStyleRanges( (StyleRange[])list.toArray( new StyleRange[list.size()] ) );
	}
	
	public void setAddressColor()
	{
		List list = new LinkedList();
		Point[] zones = fPresentation.getAddressZones();
		for ( int i = 0; i < zones.length; ++i )
			list.add( new StyleRange( zones[i].x,
									  zones[i].y - zones[i].x + 1,
									  getAddressColor(),
									  getBackgroundColor() ) );
		fText.setStyleRanges( (StyleRange[])list.toArray( new StyleRange[list.size()] ) );
	}
	
	public void setDirtyColor()
	{
		List list = new LinkedList();
		Point[] zones = fPresentation.getDirtyZones();
		for ( int i = 0; i < zones.length; ++i )
			list.add( new StyleRange( zones[i].x,
									  zones[i].y - zones[i].x + 1,
									  getDirtyColor(),
									  getBackgroundColor() ) );
		fText.setStyleRanges( (StyleRange[])list.toArray( new StyleRange[list.size()] ) );
	}
}
