/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.IExecFileInfo;
import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.core.IFormattedMemoryBlockRow;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIMemoryChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.internal.core.CDebugUtils;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;

/**
 * Enter type comment.
 * 
 * @since: Oct 15, 2002
 */
public class CFormattedMemoryBlock extends CDebugElement 
								   implements IFormattedMemoryBlock,
											  ICDIEventListener
{
	protected class ByteRange
	{
		private int fStart;
		private byte[] fRangeBytes;

		/**
		 * Constructor for Range.
		 */
		public ByteRange( int start, byte[] bytes )
		{
			fStart = start;
			fRangeBytes = bytes;
		}

		public int getStart()
		{
			return fStart;
		}

		public int getLength()
		{
			if ( fRangeBytes != null )
			{
				return fRangeBytes.length;
			}
			return 0;
		}
		
		public byte[] getBytes()
		{
			return fRangeBytes;
		}
	}

	protected class DirtyBytes
	{
		private boolean[] fDirtyBytes = null;
		
		/**
		 * Constructor for DirtyBytes.
		 */
		public DirtyBytes( int size )
		{
			fDirtyBytes = new boolean[size];
			Arrays.fill( fDirtyBytes, false );
		}
		
		public void reset()
		{
			Arrays.fill( fDirtyBytes, false );
		}
		
		public void set( int start, int length, boolean value )
		{
			Arrays.fill( fDirtyBytes, start, start + length, value );
		}

		public void set( ByteRange range, boolean value )
		{
			Arrays.fill( fDirtyBytes, range.getStart(), range.getStart() + range.getLength(), value );
		}
		
		public ByteRange[] getDirtyRanges( byte[] bytes )
		{
			ArrayList list = new ArrayList();
			int startIndex = -1;
			for ( int i = 0; i < fDirtyBytes.length; ++i )
			{
				if ( fDirtyBytes[i] )
				{
					if ( startIndex == -1 )
					{
						startIndex = i;
					}
				}
				else
				{
					if ( startIndex != -1 )
					{
						byte[] rangeBytes = new byte[i - startIndex];
						System.arraycopy( bytes, startIndex, rangeBytes, 0, i - startIndex );
						list.add( new ByteRange( startIndex, rangeBytes ) );
						startIndex = -1;
					}
				}
			}
			return (ByteRange[])list.toArray( new ByteRange[list.size()] );
		}

		public boolean isDirty()
		{
			for ( int i = 0; i < fDirtyBytes.length; ++i )
			{
				if ( fDirtyBytes[i] )
					return true;
			}
			return false;
		}
	}

	class CFormattedMemoryBlockRow implements IFormattedMemoryBlockRow
	{
		private long fAddress;
		private String[] fData;
		private String fAscii;

		/**
		 * Constructor for CFormattedMemoryBlockRow.
		 */
		public CFormattedMemoryBlockRow( long address, String[] data, String ascii )
		{
			fAddress = address;
			fData = data;
			fAscii = ascii;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlockRow#getAddress()
		 */
		public long getAddress()
		{
			return fAddress;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlockRow#getASCII()
		 */
		public String getASCII()
		{
			return fAscii;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlockRow#getData()
		 */
		public String[] getData()
		{
			return fData;
		}
	}

//	private String fAddressExpression;
	private ICDIExpression fAddressExpression;
	private ICDIMemoryBlock fCDIMemoryBlock;
	private byte[] fBytes = null;
	private int fFormat;
	private int fWordSize;
	private int fNumberOfRows;
	private int fNumberOfColumns;
	private boolean fDisplayAscii = true;
	private char fPaddingChar = '.';
	private List fRows = null;
	private Long[] fChangedAddresses = new Long[0];
	private DirtyBytes fDirtyBytes = null;
	private boolean fStartAddressChanged = false;

	/**
	 * Constructor for CFormattedMemoryBlock.
	 * @param target
	 */
	public CFormattedMemoryBlock( CDebugTarget target,
								  ICDIMemoryBlock cdiMemoryBlock,
								  ICDIExpression addressExpression,
								  int format,
						  		  int wordSize,
						  		  int numberOfRows,
						  		  int numberOfColumns )
	{
		this( target, cdiMemoryBlock, addressExpression, format, wordSize, numberOfRows, numberOfColumns, '\0' );
	}

	/**
	 * Constructor for CFormattedMemoryBlock.
	 * @param target
	 */
	public CFormattedMemoryBlock( CDebugTarget target,
								  ICDIMemoryBlock cdiMemoryBlock,
								  ICDIExpression addressExpression,
								  int format,
						  		  int wordSize,
						  		  int numberOfRows,
						  		  int numberOfColumns,
						  		  char paddingChar )
	{
		super( target );
		fCDIMemoryBlock = cdiMemoryBlock;
		fAddressExpression = addressExpression;
		fFormat = format;
		fWordSize = wordSize;
		fNumberOfRows = numberOfRows;
		fNumberOfColumns = numberOfColumns;
		fDisplayAscii = true;
		fPaddingChar = paddingChar;	
		getCDISession().getEventManager().addEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#getFormat()
	 */
	public int getFormat()
	{
		return fFormat;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#getWordSize()
	 */
	public int getWordSize()
	{
		return fWordSize;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#getNumberOfRows()
	 */
	public int getNumberOfRows()
	{
		return fNumberOfRows;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#getNumberOfColumns()
	 */
	public int getNumberOfColumns()
	{
		return fNumberOfColumns;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#displayASCII()
	 */
	public boolean displayASCII()
	{
		return ( getWordSize() == IFormattedMemoryBlock.MEMORY_SIZE_BYTE && 
				 /*getFormat() == IFormattedMemoryBlock.MEMORY_FORMAT_HEX &&*/ 
				 fDisplayAscii );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#getRows()
	 */
	public IFormattedMemoryBlockRow[] getRows()
	{
		if ( fRows == null )
		{
			fRows = new ArrayList();
			try
			{
				int offset = 0;
				byte[] bytes = getBytes();
				while( bytes != null && offset < bytes.length )
				{
					int length = Math.min( fWordSize * fNumberOfColumns, bytes.length - offset );
					fRows.add( new CFormattedMemoryBlockRow( getRowAddress( offset ), 
															 createData( bytes, offset, length ),
															 createAscii( bytes, offset, length ) ) );
					offset += length;
				}
				
			}
			catch( DebugException e )
			{
			}
		}
		return (IFormattedMemoryBlockRow[])fRows.toArray( new IFormattedMemoryBlockRow[fRows.size()] );
	}

	private synchronized void resetBytes()
	{
		fBytes = null;
		if ( fDirtyBytes != null )
		{
			fDirtyBytes.reset();
		}
	}

	private void resetRows()
	{
		fRows = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#nextRowAddress()
	 */
	public long nextRowAddress()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#previousRowAddress()
	 */
	public long previousRowAddress()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#nextPageAddress()
	 */
	public long nextPageAddress()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#previousPageAddress()
	 */
	public long previousPageAddress()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#reformat(long, int, int, int, int)
	 */
	public void reformat( int format,
						  int wordSize,
						  int numberOfRows,
						  int numberOfColumns ) throws DebugException
	{
		resetRows();
		fFormat = format; 
		fWordSize = wordSize;
		fNumberOfRows = numberOfRows;
		fNumberOfColumns = numberOfColumns;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#reformat(long, int, int, int, int, char)
	 */
	public void reformat( int format,
						  int wordSize,
						  int numberOfRows,
						  int numberOfColumns,
						  char paddingChar ) throws DebugException
	{
		resetRows();
		fFormat = format; 
		fWordSize = wordSize;
		fNumberOfRows = numberOfRows;
		fNumberOfColumns = numberOfColumns;
		fPaddingChar = paddingChar;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getStartAddress()
	 */
	public long getStartAddress()
	{
		if ( fCDIMemoryBlock != null )
		{
			return fCDIMemoryBlock.getStartAddress();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getLength()
	 */
	public long getLength()
	{
		if ( fCDIMemoryBlock != null )
		{
			return fCDIMemoryBlock.getLength();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getBytes()
	 */
	public synchronized byte[] getBytes() throws DebugException
	{
		if ( fBytes == null )
		{ 
			if ( fCDIMemoryBlock != null )
			{
				try
				{
					fBytes = fCDIMemoryBlock.getBytes();
					if ( fDirtyBytes == null )
					{
						fDirtyBytes = createDirtyBytes( fBytes.length );
					}
				}
				catch( CDIException e )
				{
					targetRequestFailed( e.getMessage(), null );
				}
			}
		}
		byte[] copyBytes = null;
		if ( fBytes != null )
		{
			copyBytes = new byte[fBytes.length];
			System.arraycopy( fBytes, 0, copyBytes, 0, fBytes.length );
		}
		return copyBytes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#supportsValueModification()
	 */
	public boolean supportsValueModification()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#setValue(long, byte[])
	 */
	public void setValue( long offset, byte[] bytes ) throws DebugException
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#getPaddingCharacter()
	 */
	public char getPaddingCharacter()
	{
		return fPaddingChar;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#dispose()
	 */
	public void dispose()
	{
		if ( fCDIMemoryBlock != null )
		{
			try
			{
				((CDebugTarget)getDebugTarget()).getCDISession().getMemoryManager().removeBlock( fCDIMemoryBlock );
			}
			catch( CDIException e )
			{
				CDebugCorePlugin.log( e );
			}
			fCDIMemoryBlock = null;
		}
		if ( fAddressExpression != null )
		{
			try
			{
				((CDebugTarget)getDebugTarget()).getCDISession().getExpressionManager().removeExpression( fAddressExpression );
			}
			catch( CDIException e )
			{
				CDebugCorePlugin.log( e );
			}
			fAddressExpression = null;
		}
		getCDISession().getEventManager().removeEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#getAddressExpression()
	 */
	public String getAddressExpression()
	{
		try
		{
			return fAddressExpression.getName();
		}
		catch( CDIException e )
		{
			// ignore
		}
		return "";
	}
	
	private String[] createData( byte[] bytes, int offset, int length )
	{
		List data = new ArrayList( length / getWordSize() );
		for ( int i = offset; i < offset + length; i += getWordSize() )
		{
			data.add( createDataItem( bytes, i, Math.min( length + offset - i, getWordSize() ) ) );
		}
		return (String[])data.toArray( new String[data.size()] );
	}

	private String createDataItem( byte[] bytes, int offset, int length )
	{
		StringBuffer sb = new StringBuffer( length * 2 );
		for ( int i = offset; i < length + offset; ++i )
		{
			sb.append( CDebugUtils.getByteText( bytes[i] ) );
		}
		return sb.toString();
	}
	
	private String createAscii( byte[] bytes, int offset, int length )
	{
		StringBuffer sb = new StringBuffer( length );
		for ( int i = offset; i < offset + length; ++i )
		{
			sb.append( ( Character.isISOControl( (char)bytes[i] ) || bytes[i] < 0 ) ? getPaddingCharacter() : (char)bytes[i] );
		}
		return sb.toString();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvent(ICDIEvent)
	 */
	public void handleDebugEvent( ICDIEvent event )
	{
		ICDIObject source = event.getSource();
		if (source == null)
			return;

		if ( source.getTarget().equals( getCDITarget() ) )
		{
			if ( event instanceof ICDIResumedEvent )
			{
				if ( source instanceof ICDITarget )
				{
					handleResumedEvent( (ICDIResumedEvent)event );
				}
			}
			else if ( event instanceof ICDIMemoryChangedEvent )
			{
				if ( source instanceof ICDIMemoryBlock && source.equals( getCDIMemoryBlock() ) )
				{
					handleChangedEvent( (ICDIMemoryChangedEvent)event );
				}
			}
			else if ( event instanceof ICDIChangedEvent )
			{
				if ( source instanceof ICDIExpression && source.equals( fAddressExpression ) )
				{
					handleAddressChangedEvent( (ICDIChangedEvent)event );
				}
			}
			else if ( event instanceof ICDIDestroyedEvent )
			{
				if ( source instanceof ICDIExpression && source.equals( fAddressExpression ) )
				{
					handleDestroyedEvent( (ICDIDestroyedEvent)event );
				}
			}
		}
	}

	protected ICDIMemoryBlock getCDIMemoryBlock()
	{
		return fCDIMemoryBlock;
	}

	protected void setCDIMemoryBlock( ICDIMemoryBlock cdiMemoryBlock )
	{
		fCDIMemoryBlock = cdiMemoryBlock;
	}
	
	private void handleResumedEvent( ICDIResumedEvent event )
	{
		resetChangedAddresses();
		fStartAddressChanged = false;
		fireChangeEvent( DebugEvent.CONTENT );
	}
	
	private void handleChangedEvent( ICDIMemoryChangedEvent event )
	{
		resetBytes();
		resetRows();
		setChangedAddresses( event.getAddresses() );
		fireChangeEvent( DebugEvent.CONTENT );
	}
	
	private void handleAddressChangedEvent( ICDIChangedEvent event )
	{
		resetBytes();
		resetRows();
		fStartAddressChanged = true;
		fireChangeEvent( DebugEvent.CONTENT );
	}
	
	private void handleDestroyedEvent( ICDIDestroyedEvent event )
	{
		fireTerminateEvent();
	}
	
	public Long[] getChangedAddresses()
	{
		return fChangedAddresses;
	}

	protected void setChangedAddresses( Long[] changedAddresses )
	{
		fChangedAddresses = changedAddresses;
	}
	
	protected void resetChangedAddresses()
	{
		fChangedAddresses = new Long[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#isFrozen()
	 */
	public boolean isFrozen()
	{
		return getCDIMemoryBlock().isFrozen();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#setFrozen(boolean)
	 */
	public void setFrozen( boolean frozen )
	{
		getCDIMemoryBlock().setFrozen( frozen );
	}

	/**
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#setItemValue(int, String)
	 */
	public void setItemValue( int index, String newValue ) throws DebugException
	{
		byte[] bytes = itemToBytes( newValue.toCharArray() );
		setBytes( index * getWordSize(), bytes );
		fDirtyBytes.set( index * getWordSize(), bytes.length, true );
		resetRows();
	}
	
	private void setBytes( int index, byte[] newBytes )
	{
		if ( fBytes != null && fDirtyBytes != null )
		{
			for ( int i = index; i < index + newBytes.length; ++i )
			{
				fBytes[i] = newBytes[i - index];
				fDirtyBytes.set( index, newBytes.length, true );
			}
		}
	}
	
	private byte[] itemToBytes( char[] chars )
	{
		switch( getFormat() )
		{
			case IFormattedMemoryBlock.MEMORY_FORMAT_HEX:
				return hexItemToBytes( chars );
		}
		return new byte[0];
	}
	
	private byte[] hexItemToBytes( char[] chars )
	{
		byte[] result = new byte[chars.length / 2];
		for ( int i = 0; i < result.length; ++i )
		{ 
			result[i] = CDebugUtils.textToByte( new char[] { chars[2 * i], chars[2 * i + 1] } );
		}
		return result;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#isDirty()
	 */
	public boolean isDirty()
	{
		if ( fDirtyBytes != null )
		{
			return fDirtyBytes.isDirty();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#saveChanges()
	 */
	public void saveChanges() throws DebugException
	{
		byte[] bytes = getBytes();
		if ( bytes != null && fDirtyBytes != null && getCDIMemoryBlock() != null )
		{
			ByteRange[] ranges = fDirtyBytes.getDirtyRanges( getBytes() );
			for ( int i = 0; i < ranges.length; ++i )
			{
				try
				{
					getCDIMemoryBlock().setValue( ranges[i].getStart(), ranges[i].getBytes() );
				}
				catch( CDIException e )
				{
					targetRequestFailed( e.getMessage(), null );
				}
			}
			fDirtyBytes.reset();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#refresh()
	 */
	public void refresh() throws DebugException
	{
		if ( getCDIMemoryBlock() != null )
		{
			try
			{
				getCDIMemoryBlock().refresh();
			}
			catch( CDIException e )
			{
				targetRequestFailed( e.getMessage(), null );
			}
		}
	}

	private DirtyBytes createDirtyBytes( int size )
	{
		return new DirtyBytes( size );
	}

	/**
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#canChangeFormat(int)
	 */
	public boolean canChangeFormat( int format )
	{
		switch( format )
		{
			case IFormattedMemoryBlock.MEMORY_FORMAT_HEX:
				return true;
			case IFormattedMemoryBlock.MEMORY_FORMAT_SIGNED_DECIMAL:
				return ( /*getWordSize() != IFormattedMemoryBlock.MEMORY_SIZE_BYTE &&*/
						 getWordSize() != IFormattedMemoryBlock.MEMORY_SIZE_DOUBLE_WORD );
			case IFormattedMemoryBlock.MEMORY_FORMAT_UNSIGNED_DECIMAL:
				return ( /*getWordSize() != IFormattedMemoryBlock.MEMORY_SIZE_BYTE &&*/
						 getWordSize() != IFormattedMemoryBlock.MEMORY_SIZE_DOUBLE_WORD );
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#isLittleEndian()
	 */
	public boolean isLittleEndian()
	{
		IExecFileInfo info = (IExecFileInfo)getDebugTarget().getAdapter( IExecFileInfo.class );
		if ( info != null )
		{
			return info.isLittleEndian();
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#isStartAddressChanged()
	 */
	public boolean isStartAddressChanged()
	{
		return fStartAddressChanged;
	}
	
	private long getRowAddress( int offset )
	{
		long result = getStartAddress() + offset;		
		if ( result > 0xFFFFFFFFL )
		{
			result -= 0xFFFFFFFFL;
		}
		return result;
	}
}
