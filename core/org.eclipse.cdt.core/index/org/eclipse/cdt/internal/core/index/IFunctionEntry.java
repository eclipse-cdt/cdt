package org.eclipse.cdt.internal.core.index;


public interface IFunctionEntry extends IIndexEntry {
	
	public void setSignature();
    public void getSignature();
	
	public void setReturnType();
	public void getReturnType();
	
	public void setStatic(boolean staticVar);
	public boolean isStatic();

}
