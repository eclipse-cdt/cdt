/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.views.memory;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.core.IFormattedMemoryBlockRow;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.swt.graphics.Point;

/**
 * 
 * Provides rendering methods to the MemoryText widget.
 * 
 * @since Jul 25, 2002
 */
public class MemoryPresentation
{
	static final private char NOT_AVAILABLE_CHAR = '?';

	private static final int INTERVAL_BETWEEN_ADDRESS_AND_DATA = 2;
	private static final int INTERVAL_BETWEEN_DATA_ITEMS = 1;
	private static final int INTERVAL_BETWEEN_DATA_AND_ASCII = 1;
	
	private IFormattedMemoryBlock fBlock;
	
	private List fAddressZones;
	private List fChangedZones;
	private List fDirtyZones;
		
	/**
	 * Constructor for MemoryPresentation.
	 */
	public MemoryPresentation()
	{		
		fAddressZones = new LinkedList();
		fChangedZones = new LinkedList();
		fDirtyZones = new LinkedList();
	}

	public IFormattedMemoryBlock getMemoryBlock()
	{
		return fBlock;
	}

	public void setMemoryBlock( IFormattedMemoryBlock block )
	{
		fBlock = block;
	}
	
	/**
	 * Returns the string that contains the textual representation of 
	 * the memory according to this presentation.
	 * 
	 * @return the string that contains the textual representation of the memory
	 */
	public String getText()
	{
		fAddressZones.clear();
		IFormattedMemoryBlockRow[] rows = ( getMemoryBlock() != null ) ? getMemoryBlock().getRows() : new IFormattedMemoryBlockRow[0];
		String text = new String();
		for ( int i = 0; i < rows.length; ++i )
		{
			int offset = text.length();
			text += getRowText( rows[i] );
			fAddressZones.add( new Point( offset, offset + getAddressLength() ) );
		}
		return text;
	}

	public int getItemSize( int offset )
	{
		return -1;
	}
	
	public void setItem( int offset, String item )
	{
	}
	
	public String[] getText( Point[] zones )
	{
		return new String[0];
	}
	
	public boolean isAcceptable( char ch, int offset )
	{
		return true;
	}
	
	public Point[] getAddressZones()
	{
		return (Point[])fAddressZones.toArray( new Point[0] );
	}
	
	public Point[] getChangedZones()
	{
		return (Point[])fChangedZones.toArray( new Point[0] );
	}
	
	public Point[] getDirtyZones()
	{
		return (Point[])fDirtyZones.toArray( new Point[0] );
	}
	
	public String getStartAddress()
	{
		return ( fBlock != null ) ? getAddressString( fBlock.getStartAddress() ) : ""; 
	}

	public String getAddressExpression()
	{
		return ( fBlock != null ) ? fBlock.getAddressExpression() : "";
	}

	private String getInterval( int length )
	{
		char[] chars = new char[length];
		for ( int i = 0; i < chars.length; ++i )
			chars[i] = ' ';
		return new String( chars );
	}

	private String getAddressString( long address )
	{
		return CDebugUIUtils.toHexAddressString( address );
	}

	private String getRowText( IFormattedMemoryBlockRow row )
	{
		String result = getAddressString( row.getAddress() ) + 
						getInterval( INTERVAL_BETWEEN_ADDRESS_AND_DATA );
		String[] items = row.getData();
		for ( int i = 0; i < items.length; ++i )
			result += items[i] + getInterval( INTERVAL_BETWEEN_DATA_ITEMS );
		result += getInterval( INTERVAL_BETWEEN_DATA_AND_ASCII ) + row.getASCII() + '\n';
		return result;
	}

/*	
	private String getItemString( int offset )
	{
		byte[] data = getDataBytes();
		String item = new String( data, offset, getSize() );
		String result = "";
		switch( fFormat )
		{
			case ICDebugUIInternalConstants.MEMORY_FORMAT_HEX:
				for ( int i = 0; i < getSize(); ++i )
					result += new String( charToBytes( item.charAt( i ) ) );
				break;
			case ICDebugUIInternalConstants.MEMORY_FORMAT_BINARY:
				for ( int i = 0; i < getSize(); ++i )
					result += prepend( Integer.toBinaryString( data[offset + i] ), '0', 8 );
				break;
			case ICDebugUIInternalConstants.MEMORY_FORMAT_OCTAL:
				for ( int i = 0; i < getSize(); ++i )
					result += Integer.toOctalString( data[offset + i] );
				break;
		}
		return result;
	}
	
	private String getASCIIString( int offset )
	{
		byte[] data = getDataBytes();
		char[] ascii = new char[getBytesPerRow()];
		for ( int i = 0; i < ascii.length; ++i )
		{
			ascii[i] = ( data.length > offset + i ) ? getChar( data[offset + i] ) : fNotAvailableChar;			
		}
		return new String( ascii );
	}
	
	private char getChar( byte charByte )
	{
		char ch = (char)charByte;
		if ( ch == fNotAvailableChar )
			return fNotAvailableChar;
		return ( Character.isISOControl( ch ) ) ? fPaddingChar : ch;
	}

	private char[] charToBytes( char ch )
	{
		return new char[]{ charFromByte( (char)(ch >>> 4) ),
						   charFromByte( (char)(ch & 0x0f) ) };
	}

	private char charFromByte( char value )
	{
		if ( value >= 0x0 && value <= 0x9 )
			return (char)(value + '0');
		if ( value >= 0xa && value <= 0xf )
			return (char)(value - 0xa + 'a');
		return '0';
	}
	
	private byte[] getDataBytes()
	{
		return new byte[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
/*
		byte[] result =  new byte[0];
		if ( fBlock != null )
		{
			try
			{
				result = fBlock.getBytes();
			}
			catch( DebugException e )
			{
				// ignore
			}
		}
		return result;
*/
/*
	}
*/	
	private String resize( String item, char ch, int size )
	{
		char[] chars = new char[size - item.length()];
		for ( int i = 0; i < chars.length; ++i )
			chars[i] = ch;
		return String.valueOf( chars ).concat( item );
	}
/*	
	private int getRowLength()
	{
		return getAddressLength() + 
			   INTERVAL_BETWEEN_ADDRESS_AND_DATA +
			   (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS ) * getNumberOfDataItems() + 
			   ( ( displayASCII() ) ? INTERVAL_BETWEEN_DATA_AND_ASCII +
			   getBytesPerRow() : 0 ) + 1;
	}
*/	
	private int getAddressLength()
	{
		return 10;
	}
/*	
	private int getDataItemLength()
	{
		int result = 0;
		switch( getFormat() )
		{
			case ICDebugUIInternalConstants.MEMORY_FORMAT_HEX:
				result = 2 * getSize();
				break;
			case ICDebugUIInternalConstants.MEMORY_FORMAT_BINARY:
				result = 8 * getSize();
				break;
		}
		return result;
	}
	
	private int getNumberOfDataItems()
	{
		return getBytesPerRow() / getSize();
	}
*/
}
