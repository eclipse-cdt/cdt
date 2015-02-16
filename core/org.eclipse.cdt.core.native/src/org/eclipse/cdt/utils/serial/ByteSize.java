package org.eclipse.cdt.utils.serial;

public enum ByteSize {

	B5(5),
	B6(6),
	B7(7),
	B8(8);
	
	private final int size;
	
	private ByteSize(int size) {
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}
}
