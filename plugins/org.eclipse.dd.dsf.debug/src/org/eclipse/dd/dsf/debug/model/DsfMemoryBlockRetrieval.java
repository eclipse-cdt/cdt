/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson Communication  - upgrade IF from IMemoryBlockRetrieval to IMemoryBlockRetrievalExtension
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.DsfDebugPlugin;
import org.eclipse.dd.dsf.debug.service.IMemory;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Implementation of memory access API of the Eclipse standard debug model.
 *
 * The IMemoryBlockRetrievalExtension is implemented for the reference
 * application. This will result in getExtendedMemoryBlock() being called
 * when a memory block is selected from the memory pane.
 * 
 * However, if the 'simpler' IMemoryBlockRetrieval is to be implemented, the
 * code will still be functional after trivial adjustments. In that case, the
 * platform will call getMemoryBlock() instead. Note that DsfMemoryBlock will
 * have to be 'downgraded' to implement IMemoryBlock (instead of IMemoryBlockExtension)   
 * 
 */
public class DsfMemoryBlockRetrieval extends PlatformObject implements IMemoryBlockRetrievalExtension 
{
    private final String fModelId; 
    private final DsfSession fSession; 
    private final DsfExecutor fExecutor; 
    private final IDMContext<?> fContext; 
    private final ServiceTracker fServiceTracker; 
    
    /**
     * Constructor 
     * 
     * @param modelId
     * @param dmc
     * @throws DebugException
     */
    public DsfMemoryBlockRetrieval(String modelId, IDMContext<?> dmc) throws DebugException {
        fModelId = modelId;
        fContext = dmc;
        fSession = DsfSession.getSession(fContext.getSessionId());
        if (fSession == null) {
            throw new IllegalArgumentException("Session for context " + fContext + " is not active"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        fExecutor = fSession.getExecutor();
        String memoryServiceFilter = 
            "(&" +  //$NON-NLS-1$
            "(OBJECTCLASS=" + IMemory.class.getName() + ")" +   //$NON-NLS-1$//$NON-NLS-2$
            "(" + IDsfService.PROP_SESSION_ID + "=" + dmc.getSessionId() + ")" +  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            ")"; //$NON-NLS-1$
        BundleContext bundle = DsfDebugPlugin.getBundleContext();
        try {
            fServiceTracker = new ServiceTracker(bundle, bundle.createFilter(memoryServiceFilter), null);
        } catch (InvalidSyntaxException e) {
            throw new DebugException(new Status(IStatus.ERROR, DsfDebugPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Error creating service filter.", e)); //$NON-NLS-1$
        }
        fServiceTracker.open();
    }

    // ////////////////////////////////////////////////////////////////////////
    // Accessors
    // ////////////////////////////////////////////////////////////////////////
    
	/**
	 * 
	 * @return
	 */
    public String getModelId() {
		return fModelId;
	}

	/**
	 * 
	 * @return
	 */
    public DsfSession getSession() {
		return fSession;
	}

	/**
	 * 
	 * @return
	 */
    public DsfExecutor getExecutor() {
		return fExecutor;
	}

	/**
	 * 
	 * @return
	 */
    public IDMContext<?> getContext() {
		return fContext;
	}

    /**
	 * 
	 * @return
	 */
    public ServiceTracker getServiceTracker() {
		return fServiceTracker;
	}

    // ////////////////////////////////////////////////////////////////////////
    // IMemoryBlockRetrieval - obsoleted by IMemoryBlockRetrievalExtension
    // ////////////////////////////////////////////////////////////////////////
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
     */
    public boolean supportsStorageRetrieval() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
     */
    public IMemoryBlock getMemoryBlock(final long startAddress, final long length) throws DebugException {
    	// The expression to display in the rendering tab
    	// Put here for the sake of completeness (not used with IMemoryBlock)
    	String expression = "0x" + Long.toHexString(startAddress); //$NON-NLS-1$
    	return new DsfMemoryBlock(DsfMemoryBlockRetrieval.this, fModelId, expression, startAddress, length);
    }

    // ////////////////////////////////////////////////////////////////////////
    // IMemoryBlockRetrievalExtension
    // ////////////////////////////////////////////////////////////////////////
    
    /*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension#getExtendedMemoryBlock(java.lang.String, java.lang.Object)
	 */
    public IMemoryBlockExtension getExtendedMemoryBlock(String expression, Object context) throws DebugException {

    	boolean validAddress = false;
    	long address = 0;

    	/* See if the expression is a simple numeric value; if it is, we can 
    	 * avoid some costly processing (calling the back-end to resolve the 
    	 * expression and obtain an address)
    	 */
    	try {
    		// First, assume a decimal address (not too realistic but bear with us :-)
    		int base = 10;
    		int offset = 0;

    		// Check for "hexadecimality"
    		if (expression.startsWith("0x") || expression.startsWith("0X")) {  //$NON-NLS-1$//$NON-NLS-2$
    			base = 16;
    			offset = 2;
    		}

    		// Now, try to parse the expression. If a NumberFormatException is thrown,
    		// then it wasn't a simple numerical expression and we go to plan B
    		address = Long.parseLong(expression.substring(offset), base);

    		// All right! We saved a trip to the back-end.
    		validAddress = true;

    	} catch (NumberFormatException nfexc) {
    		// OK, expression is not a simple, absolute numeric value; try to resolve as expression
    	}

    	// We have to ask the debugger to resolve the address for us
    	if (!validAddress) {
    		// [frch] Code removed until we properly hook the ExpressionService
    		// [frch] Code saved in lmcfrch-memory-070625.PATCH
    		return null;
    	}

    	/* At this point, we only know the requested address and we have no
    	 * idea of the memory block length. The renderer will provide this
    	 * information when it calls getBytesFromAddress() i.e. after the 
    	 * memory block holder has been instantiated...
    	 * 
    	 * We could try to out-smart the renderer and do some educated guess
    	 * about the start address it will select and the length it will
    	 * request.
    	 * 
    	 * This would 'work' for the standard debug renderers: start address
    	 * on a 16-byte boundary, length of 320 bytes (20 lines of 16 bytes).
    	 * However, this is not such a great idea: a given renderer might ask
    	 * for a block starting at any address adn length it sees fit. Therefore
    	 * we can't make any assumption.  
    	 * 
    	 * The only safe approach is to just instantiate the IMemoryBlockExtension
    	 * and postpone the actual memory fetch until the renderer explicitly
    	 * asks for it (i.e. calls getBytesFromAddress()). 
    	 * 
    	 * The down side is that every time we switch renderer, for the same block,
    	 * a trip to the target could result.
    	 * 
    	 * However, we have an ace in our sleeve: the memory request cache should
    	 * save us a trip to the back-end.
    	 */

     	return new DsfMemoryBlock(DsfMemoryBlockRetrieval.this, fModelId, expression, address, 0);
    }

    // ////////////////////////////////////////////////////////////////////////
    // Helper functions
    // ////////////////////////////////////////////////////////////////////////

	// [frch] Code removed until we properly hook the ExpressionService
	// [frch] Code saved in lmcfrch-memory-070625.PATCH

}
