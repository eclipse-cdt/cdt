package org.eclipse.cdt.internal.core.index;

public interface ITypeEntry extends IIndexEntry {
	
	public void setKind(int meta_kind);
    public int getKind();
	
	public void setReturnType();
	public void getReturnType();
	
	public void isStatic();
	
	public void setName(String name);
}
