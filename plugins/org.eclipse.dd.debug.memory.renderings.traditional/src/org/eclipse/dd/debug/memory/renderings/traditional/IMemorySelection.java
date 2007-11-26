package org.eclipse.dd.debug.memory.renderings.traditional;

import java.math.BigInteger;

public interface IMemorySelection 
{
    public boolean hasSelection();
    
    public boolean isSelected(BigInteger address);
   
    public BigInteger getStart();

    public BigInteger getEnd();
    
    public void setStart(BigInteger high, BigInteger low);
    
    public void setEnd(BigInteger high, BigInteger low);
    
    public BigInteger getHigh();
    
    public BigInteger getLow();
    
    public void clear();
}
