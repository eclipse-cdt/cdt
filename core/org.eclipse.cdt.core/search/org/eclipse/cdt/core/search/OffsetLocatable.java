package org.eclipse.cdt.core.search;

public class OffsetLocatable implements IOffsetLocatable {
		
	int nameStartOffset;
	int nameEndOffset;
	int elementStartOffset;
	int elementEndOffset;
	
	/**
	 * Used to create an OffsetLocatable that contains just the name offsets
	 * @param nameStartOffset - the starting offset of the name
	 * @param nameEndOffset - the ending offset of the name
	 */
	public OffsetLocatable(int nameStartOffset, int nameEndOffset){
		this(nameStartOffset, nameEndOffset, 0, 0);
	}
		
	/**
	 * Create an OffsetLocatable that contains both the offsets of the name and the offsets
	 * of the element itself
	 * @param nameStartOffset - the starting offset of the name
	 * @param nameEndOffset - the ending offset of the name
	 * @param elementStartOffset - the starting offset of the element
	 * @param elementEndOffset - the ending offset of the element
	 */
	public OffsetLocatable(int nameStartOffset, int nameEndOffset, int elementStartOffset, int elementEndOffset){
		this.nameStartOffset = nameStartOffset;
		this.nameEndOffset = nameEndOffset;
		this.elementStartOffset=elementStartOffset;
		this.elementEndOffset=elementEndOffset;
	}
	
	public int getNameStartOffset() {
		return nameStartOffset;
	}

	public int getNameEndOffset() {
		return nameEndOffset;
	}

	public int getElementStartOffset(){
		return elementStartOffset;
	}
	
	public int getElementEndOffset(){
		return elementEndOffset;
	}
}
