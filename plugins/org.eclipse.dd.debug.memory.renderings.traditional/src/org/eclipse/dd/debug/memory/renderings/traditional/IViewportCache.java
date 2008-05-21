package org.eclipse.dd.debug.memory.renderings.traditional;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.MemoryByte;

public interface IViewportCache {

    public void dispose();
    public void refresh();
    public TraditionalMemoryByte[] getBytes(BigInteger address, int bytesRequested) throws DebugException;
    public void archiveDeltas();
    public void setEditedValue(BigInteger address, TraditionalMemoryByte[] bytes);
    public void clearEditBuffer();
    public void writeEditBuffer();
    public boolean containsEditedCell(BigInteger address);
    //    private void queueRequest(BigInteger startAddress, BigInteger endAddress);
}
