/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.core.IFormattedMemoryBlockRow;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.debug.core.DebugException;

/**
 * Enter type comment.
 * 
 * @since: Oct 15, 2002
 */
public class CFormattedMemoryBlock extends CDebugElement implements IFormattedMemoryBlock
{
	private ICDIMemoryBlock fCDIMemoryBlock;
	private int fFormat;
	private int fWordSize;
	private int fNumberOfRows;
	private int fNumberOfColumns;
	private boolean fDisplayAscii = true;
	private char fPaddingChar = 0;

	/**
	 * Constructor for CFormattedMemoryBlock.
	 * @param target
	 */
	public CFormattedMemoryBlock( CDebugTarget target,
								  ICDIMemoryBlock cdiMemoryBlock,
								  int format,
						  		  int wordSize,
						  		  int numberOfRows,
						  		  int numberOfColumns )
	{
		super( target );
		fCDIMemoryBlock = cdiMemoryBlock;
		fFormat = format;
		fWordSize = wordSize;
		fNumberOfRows = numberOfRows;
		fNumberOfColumns = numberOfColumns;
		fDisplayAscii = false;
		fPaddingChar = 0;
	}

	/**
	 * Constructor for CFormattedMemoryBlock.
	 * @param target
	 */
	public CFormattedMemoryBlock( CDebugTarget target,
								  ICDIMemoryBlock cdiMemoryBlock,
								  int format,
						  		  int wordSize,
						  		  int numberOfRows,
						  		  int numberOfColumns,
						  		  char paddingChar )
	{
		super( target );
		fCDIMemoryBlock = cdiMemoryBlock;
		fFormat = format;
		fWordSize = wordSize;
		fNumberOfRows = numberOfRows;
		fNumberOfColumns = numberOfColumns;
		fDisplayAscii = true;
		fPaddingChar = paddingChar;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#getFormat()
	 */
	public int getFormat()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#getWordSize()
	 */
	public int getWordSize()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#getNumberOfRows()
	 */
	public int getNumberOfRows()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#getNumberOfColumns()
	 */
	public int getNumberOfColumns()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#displayASCII()
	 */
	public boolean displayASCII()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IFormattedMemoryBlock#getRows()
	 */
	public IFormattedMemoryBlockRow[] getRows()
	{
		return null;
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getStartAddress()
	 */
	public long getStartAddress()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getLength()
	 */
	public long getLength()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getBytes()
	 */
	public byte[] getBytes() throws DebugException
	{
		return null;
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
		return 0;
	}

}
