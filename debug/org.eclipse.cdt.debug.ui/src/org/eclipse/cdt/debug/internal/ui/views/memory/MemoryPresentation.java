/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.views.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.debug.core.ICMemoryManager;
import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.core.IFormattedMemoryBlockRow;
import org.eclipse.cdt.debug.internal.core.CDebugUtils;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.debug.core.DebugException;
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

	private boolean fDisplayAscii = true;
		
	/**
	 * Constructor for MemoryPresentation.
	 */
	public MemoryPresentation()
	{		
		fAddressZones = new LinkedList();
		fChangedZones = new LinkedList();
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
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < rows.length; ++i )
		{
			int offset = sb.length();
			sb.append( getRowText( rows[i] ) );
			fAddressZones.add( new Point( offset, offset + getAddressLength() ) );
		}
		return sb.toString();
	}

	public String[] getText( Point[] zones )
	{
		return new String[0];
	}
	
	public boolean isAcceptable( char ch, int offset )
	{
		if ( isInAsciiArea( offset ) )
			return true;
		if ( isInDataArea( offset ) )
			return isValidValue( ch );
		return false;
	}
	
	public Point[] getAddressZones()
	{
		return (Point[])fAddressZones.toArray( new Point[fAddressZones.size()] );
	}
	
	public Point[] getChangedZones()
	{
		fChangedZones.clear();
		Long[] changedAddresses = getChangedAddresses();
		for ( int i = 0; i < changedAddresses.length; ++i )
		{
			int dataOffset = getDataItemOffsetByAddress( changedAddresses[i] );
			if ( dataOffset != -1 )
			{
				fChangedZones.add( new Point( dataOffset, dataOffset + getDataItemLength() - 1 ) );
			}
			if ( displayASCII() )
			{
				int asciiOffset = getAsciiOffsetByAddress( changedAddresses[i] );
				if ( asciiOffset != -1 )
				{
					fChangedZones.add( new Point( asciiOffset, asciiOffset ) );
				}
			}			
		}
		return (Point[])fChangedZones.toArray( new Point[fChangedZones.size()] );
	}
	
	public Point[] getDirtyZones()
	{
		ArrayList dirtyZones = new ArrayList();
		if ( fBlock != null )
		{
			IFormattedMemoryBlockRow[] rows = fBlock.getRows();
			for ( int i = 0; i < rows.length; ++i )
			{
				int rowOffset = i * getRowLength();
				Integer[] dirtyItems = rows[i].getDirtyItems();
				for ( int j = 0; j < dirtyItems.length; ++j )
				{
					int offset = rowOffset + 
								 getAddressLength() + INTERVAL_BETWEEN_ADDRESS_AND_DATA +
								 dirtyItems[j].intValue() * (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS);
					dirtyZones.add( new Point( offset, offset + getDataItemLength() ) );
					if ( displayASCII() )
					{
						offset = rowOffset + 
								 getAddressLength() + INTERVAL_BETWEEN_ADDRESS_AND_DATA +
								 getNumberOfDataItemsInRow() * (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS);
						dirtyZones.add( new Point( offset, offset + (getDataItemLength() / 2) ) );
					}
				}
			}
		}
		return (Point[])dirtyZones.toArray( new Point[dirtyZones.size()] );
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
		Arrays.fill( chars, ' ' );
		return new String( chars );
	}

	private String getAddressString( long address )
	{
		return CDebugUIUtils.toHexAddressString( address );
	}

	private String getRowText( IFormattedMemoryBlockRow row )
	{
		StringBuffer result = new StringBuffer( getRowLength() ); 
		result.append( getAddressString( row.getAddress() ) ); 
		result.append( getInterval( INTERVAL_BETWEEN_ADDRESS_AND_DATA ) );
		String[] items = row.getData();
		for ( int i = 0; i < items.length; ++i )
		{
			result.append( items[i] );
			result.append( getInterval( INTERVAL_BETWEEN_DATA_ITEMS ) );
		}
		if ( displayASCII() )
		{
			result.append( getInterval( INTERVAL_BETWEEN_DATA_AND_ASCII ) );
			result.append( row.getASCII() );
		}
		result.append( '\n' );
		return result.toString();
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

	private int getRowLength()
	{
		return getAddressLength() + 
			   INTERVAL_BETWEEN_ADDRESS_AND_DATA +
			   (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS) * getNumberOfDataItemsInRow() + 
			   ( ( displayASCII() ) ? INTERVAL_BETWEEN_DATA_AND_ASCII +
			   getDataBytesPerRow() : 0 ) + 1;
	}

	private int getAddressLength()
	{
		return 10;
	}
	
	private boolean isInAddressZone( int offset )
	{
		if ( getRowLength() != 0 )
		{
			int pos = offset % getRowLength();
			return ( pos >= 0 && pos < getAddressLength() ); 
		}
		return false;
	}

	private boolean isInAsciiArea( int offset )
	{
		if ( displayASCII() && getRowLength() != 0 )
		{
			int pos = offset % getRowLength();
			int asciiColumn = getAddressLength() + 
			   				  INTERVAL_BETWEEN_ADDRESS_AND_DATA +
			   				  (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS ) * getNumberOfDataItemsInRow() + 
			   				  INTERVAL_BETWEEN_DATA_AND_ASCII;
			return ( pos >=  asciiColumn && pos < getRowLength() - 1 ); 
		}
		return false;
	}

	private boolean isInDataArea( int offset )
	{
		if ( getRowLength() != 0 )
		{
			int pos = offset % getRowLength();
			int dataBegin = getAddressLength() + INTERVAL_BETWEEN_ADDRESS_AND_DATA;
			int dataEnd = dataBegin + ((getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS ) * getNumberOfDataItemsInRow());
			if ( pos >= dataBegin && pos < dataEnd )
				return isInDataItem( pos - dataBegin );
		}
		return false;
	}

	private boolean isInDataItem( int pos )
	{
		for ( int i = 0; i < getNumberOfDataItemsInRow(); ++i )
		{
			if ( pos < i * (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS) )
				return false;
			if ( pos >= i * (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS) &&
				 pos < (i * (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS)) + getDataItemLength() )
				return true;
		}
		return false;
	}

	private int getDataItemLength()
	{
		if ( getMemoryBlock() != null )
			return getMemoryBlock().getWordSize() * 2;
		return 0;
	}
	
	private int getNumberOfDataItemsInRow()
	{
		if ( getMemoryBlock() != null )
			return getMemoryBlock().getNumberOfColumns();
		return 0;
	}
	
	protected boolean displayASCII()
	{
		if ( canDisplayAscii() )
			return fDisplayAscii;
		return false;
	}
	
	protected void setDisplayAscii( boolean displayAscii )
	{
		fDisplayAscii = displayAscii;
	}
 
	private int getDataBytesPerRow()
	{
		if ( getMemoryBlock() != null )
			return getMemoryBlock().getNumberOfColumns() * getMemoryBlock().getWordSize();
		return 0;
	}

	private boolean isValidValue( char ch )
	{
		switch( getDataFormat() )
		{
			case ICMemoryManager.MEMORY_FORMAT_HEX:
				return isHexadecimal( ch );
			case ICMemoryManager.MEMORY_FORMAT_BINARY:
			case ICMemoryManager.MEMORY_FORMAT_OCTAL:
			case ICMemoryManager.MEMORY_FORMAT_SIGNED_DECIMAL:
			case ICMemoryManager.MEMORY_FORMAT_UNSIGNED_DECIMAL:
			case -1:
			default:
				return false;
		}
	}

	private boolean isHexadecimal( char ch )
	{
		return ( Character.isDigit( ch ) ||
				 ( ch >= 'a' && ch <= 'f' ) ||
				 ( ch >= 'A' && ch <= 'F' ) );
	}
	
	private int getDataFormat()
	{
		if ( getMemoryBlock() != null )
			return getMemoryBlock().getFormat();
		return -1;
	}
	
	private Long[] getChangedAddresses()
	{
		return ( getMemoryBlock() != null ) ? getMemoryBlock().getChangedAddresses() : new Long[0];
	}
	
	private int getDataItemOffsetByAddress( Long address )
	{
		if ( getMemoryBlock() != null )
		{
			IFormattedMemoryBlockRow[] rows = getMemoryBlock().getRows();
			for ( int i = 0; i < rows.length; ++i )
			{
				int wordSize = getMemoryBlock().getWordSize();
				int numberOfColumns = getMemoryBlock().getNumberOfColumns();
				if ( address.longValue() >= rows[i].getAddress() && 
					 address.longValue() < rows[i].getAddress() + (wordSize * numberOfColumns) )
				{
					for ( int j = 1; j < numberOfColumns; ++j )
					{
						if ( address.longValue() >= rows[i].getAddress() + ((j - 1) * wordSize) &&
							 address.longValue() < rows[i].getAddress() + (j * wordSize) )
						{
							return (i * getRowLength()) + ((j - 1) * (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS)) + getAddressLength() + INTERVAL_BETWEEN_ADDRESS_AND_DATA;
						}
					}
				}
			}
			
		}
		return -1;
	}
	
	private int getAsciiOffsetByAddress( Long address )
	{
		if ( getMemoryBlock() != null )
		{
			IFormattedMemoryBlockRow[] rows = getMemoryBlock().getRows();
			if ( rows.length > 0 )
			{
				IFormattedMemoryBlockRow firstRow = rows[0];
				IFormattedMemoryBlockRow lastRow = rows[rows.length - 1];
				if ( address.longValue() >= firstRow.getAddress() && address.longValue() <= lastRow.getAddress() )
				{
					int asciiOffset = (int)(address.longValue() - firstRow.getAddress());
					int asciiRowlength = getMemoryBlock().getWordSize() * getMemoryBlock().getNumberOfColumns();
					int numberOfRows = asciiOffset / asciiRowlength;
					int offsetInRow = asciiOffset % asciiRowlength;
					return (numberOfRows * getRowLength()) + 
						   getAddressLength() + INTERVAL_BETWEEN_ADDRESS_AND_DATA +
						   (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS) * getMemoryBlock().getNumberOfColumns() +
						   INTERVAL_BETWEEN_DATA_AND_ASCII + offsetInRow;
				}
			}
		}
		return -1;
	}
	
	protected boolean canDisplayAscii()
	{
		if ( getMemoryBlock() != null )
			return getMemoryBlock().displayASCII();
		return false;
	}
	
	protected void textChanged( int offset, char newChar, char[] replacedText )
	{
		if ( getMemoryBlock() != null )
		{
			int index = getDataItemIndex( offset );
			if ( index != -1 )
			{
				char[] chars = getDataItemChars( index );
				if ( isInDataArea( offset ) )
				{
					int charIndex = getOffsetInDataItem( offset, index );
					chars[charIndex] = newChar;
				}
				if ( isInAsciiArea( offset ) )
				{
					chars = CDebugUtils.getByteText( (byte)newChar );
				}
				try
				{
					getMemoryBlock().setItemValue( index, new String( chars ) );
				}
				catch( DebugException e )
				{
					// ignore
				}
			}
		}
	}

	private int getDataItemIndex( int offset )
	{
		int row = offset / getRowLength();
		int pos = offset % getRowLength() - getAddressLength() - INTERVAL_BETWEEN_ADDRESS_AND_DATA;
		for ( int i = 0; i < getNumberOfDataItemsInRow(); ++i )
		{
			if ( pos < i * (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS) )
				return -1;
			if ( pos >= i * (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS) &&
				 pos < (i * (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS)) + getDataItemLength() )
				return i + (row * getNumberOfDataItemsInRow());
		}
		if ( displayASCII() && pos >= getNumberOfDataItemsInRow() * (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS) + INTERVAL_BETWEEN_DATA_AND_ASCII )
		{
			return ((pos - ((getNumberOfDataItemsInRow() * (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS) + INTERVAL_BETWEEN_DATA_AND_ASCII))) * 2 / getDataItemLength()) + row * getNumberOfDataItemsInRow();
		}
		return -1;
	}

	private int getDataItemOffset( int index )
	{
		int row = index / getNumberOfDataItemsInRow();
		int pos = index % getNumberOfDataItemsInRow();
		return row * getRowLength() + 
			   getAddressLength() + INTERVAL_BETWEEN_ADDRESS_AND_DATA +
			   pos * (getDataItemLength() + INTERVAL_BETWEEN_DATA_ITEMS); 
	}

	private char[] getDataItemChars( int index )
	{
		if ( getMemoryBlock() != null )
		{
			int rowNumber = index / getMemoryBlock().getNumberOfColumns();
			int pos = index % getMemoryBlock().getNumberOfColumns();
			IFormattedMemoryBlockRow[] rows = getMemoryBlock().getRows();
			if ( rowNumber < rows.length )
			{
				String[] data = rows[rowNumber].getData();
				if ( pos < data.length )
				{
					return data[pos].toCharArray();
				}
			}
		}
		return new char[0];
	}
	
	private int getOffsetInDataItem( int offset, int index )
	{
		if ( isInDataArea( offset ) )
		{
			return offset - getDataItemOffset( index );
		}
		return -1;
	}
}
