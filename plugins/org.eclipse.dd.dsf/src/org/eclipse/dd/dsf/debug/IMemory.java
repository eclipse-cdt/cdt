package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.service.IDsfService;

/**
 * Service for accessing memory.  Memory contexts are not meant to be 
 * represented in tree or table views, so it doesn't need to implement
 * IDataModelService interface. 
 */
public interface IMemory extends IDsfService {
    
    /**
     * A context for memory is still needed, for debuggers that can debug 
     * multiple processes/targets at the same time.
     */
    public interface IMemoryContext {}

    /**
     * I was told that BigInteger is also too restrictive to represent an 
     * address, but I'm not really sure what is appropriate for this interface.
     */
    public interface IAddress {
        /** Returns the memory context that this address belongs to. */
        public IMemoryContext getContext();
    }

    /**  Writes the given value to the given memory location. */
    public void setMemory(IMemoryContext memCtx, IAddress addr, 
                          int word_size, byte[] buf, int offs, int size, int mode, Done done);

    /** Reads memory at the given location */
    public void getMemory(IMemoryContext memCtx, IAddress addr, 
                          int word_size, byte[] buf, int offs, int size, int mode, Done done);

    /**
     * Fill target memory with given pattern.
     * 'size' is number of bytes to fill.
     * Parameter 0 of sequent 'done' is assigned with Throwable if
     * there was an error.
     */
    public void fillMemory(IMemoryContext memCtx, IAddress addr,
                           int word_size, byte[] value, int size, int mode, Done done);
    
}
