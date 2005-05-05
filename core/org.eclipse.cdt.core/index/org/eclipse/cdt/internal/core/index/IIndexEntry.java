package org.eclipse.cdt.internal.core.index;

public interface IIndexEntry {
	
	//DECLARATION, DEFINITION, or REFERENCE as defined in IIndex
	public void setEntryType(int type);
	public int getEntryType();
	
	public void serialize(IIndexerOutput output);
	
}
