package org.eclipse.cdt.internal.core.dom;

/**
 */
public class Declaration implements IOffsetable {
	
	public Declaration( IScope scope )
	{
		ownerScope = scope;
	}
	
	private final IScope ownerScope;

	/**
	 * @return
	 */
	public IScope getOwnerScope() {
		return ownerScope;
	}


	private int startingOffset, endingOffset; 
	private int startingLine, endingLine;
	private int totalLength; 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#getStartingOffset()
	 */
	public int getStartingOffset()
	{
		return startingOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#getTotalLength()
	 */
	public int getTotalLength()
	{
		return totalLength;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#setStartingOffset(int)
	 */
	public void setStartingOffset(int i)
	{
		startingOffset = i; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#setTotalLength(int)
	 */
	public void setTotalLength(int i)
	{
		totalLength = i;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#setTopLine(int)
	 */
	public void setTopLine(int lineNumber)
	{
		startingLine = lineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#setBottomLine(int)
	 */
	public void setBottomLine(int lineNumber)
	{
		endingLine = lineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#getTopLine()
	 */
	public int getTopLine()
	{
		return startingLine; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsetable#getBottomLine()
	 */
	public int getBottomLine()
	{
		return endingLine;
	}

}
