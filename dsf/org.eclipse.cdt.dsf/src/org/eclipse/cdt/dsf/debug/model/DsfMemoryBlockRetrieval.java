/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson Communication - upgrade IF to IMemoryBlockRetrievalExtension
 *     Ericsson Communication - added Expression evaluation
 *     Ericsson Communication - added support for 64 bit processors
 *     Ericsson Communication - added support for event handling
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.service.DsfServices;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of memory access API of the Eclipse standard debug model.
 * 
 * The DsfMemoryBlockRetrieval is not an actual memory block but rather a
 * reference to the memory address space for an execution context (e.g. a 
 * process) within a debug session. From this debug 'context', memory blocks 
 * can then be read/written.
 * 
 * Note: For the reference application, The IMemoryBlockRetrievalExtension
 * is implemented. This will result in getExtendedMemoryBlock() being called
 * when a memory block is selected from the platform memory pane.
 * 
 * However, if the 'simpler' IMemoryBlockRetrieval is to be implemented, the
 * code will still be functional after some trivial adjustments.
 * 
 * @since 1.0
 */
public class DsfMemoryBlockRetrieval extends PlatformObject implements IMemoryBlockRetrievalExtension
{
	private final String           fModelId;
	private final DsfSession       fSession;
    private final DsfExecutor      fExecutor;
    private final String           fContextString;
    private final ServiceTracker   fMemoryServiceTracker;
    private final ServiceTracker   fExpressionServiceTracker;

    private final ILaunchConfiguration fLaunchConfig;
	private final ILaunch          fLaunch;
	private final IDebugTarget     fDebugTarget;
    private final boolean          fSupportsValueModification;
    private final boolean          fSupportBaseAddressModification;
    private final int              fAddressSize;
    private final int              fWordSize;  // Number of bytes per address
	
	/**
	 * Constructor
	 * 
	 * @param modelId
	 * @param dmc
	 * @throws DebugException
	 */
	public DsfMemoryBlockRetrieval(String modelId, ILaunchConfiguration config, DsfSession session) throws DebugException {

	    // DSF stuff
        fModelId = modelId;

        // FIXME: (Bug228573) Currently memory contexts are differentiated by
        // sessionID so there is no way to guarantee the memory blocks will be
        // reinstated in the correct memory space.
        // Need a way to create deterministically the context ID from a unique
        // target, ideally from the launch configuration (or derived from it).
        // For the time being, just put some constant. This will work until we
        // support multiple targets in the same launch.
        // fContextString = fContext.toString();
        fContextString = "Context string";  //$NON-NLS-1$

        fSession = session;
		if (fSession == null) {
			throw new IllegalArgumentException(
					"Session " + session + " is not active"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		fExecutor = fSession.getExecutor();
 		BundleContext bundle = DsfPlugin.getBundleContext();

 		// Here we chose to use 2 distinct service trackers instead of an
 		// amalgamated one because it is less error prone (and we are lazy).

 		// Create a tracker for the MemoryService
 		String memoryServiceFilter = DsfServices.createServiceFilter(IMemory.class, session.getId());

 		try {
			fMemoryServiceTracker = new ServiceTracker(
					bundle,	bundle.createFilter(memoryServiceFilter), null);
		} catch (InvalidSyntaxException e) {
			throw new DebugException(new Status(IStatus.ERROR,
					DsfPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Error creating service filter.", e)); //$NON-NLS-1$
		}
		fMemoryServiceTracker.open();

		// Create a tracker for the ExpressionService
 		String expressionServiceFilter = "(&" + //$NON-NLS-1$
				"(OBJECTCLASS=" //$NON-NLS-1$
				+ IExpressions.class.getName()
				+ ")" + //$NON-NLS-1$
				"(" + IDsfService.PROP_SESSION_ID //$NON-NLS-1$
				+ "=" + session.getId() + ")" + //$NON-NLS-1$//$NON-NLS-2$
				")"; //$NON-NLS-1$

		try {
			fExpressionServiceTracker = new ServiceTracker(
					bundle, bundle.createFilter(expressionServiceFilter), null);
		} catch (InvalidSyntaxException e) {
			throw new DebugException(new Status(IStatus.ERROR,
					DsfPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Error creating service filter.", e)); //$NON-NLS-1$
		}
		fExpressionServiceTracker.open();

        // Launch configuration information
        fLaunchConfig = config;
        fLaunch       = null;
        fDebugTarget  = null;
        fAddressSize  = 4;    // Get this from the launch configuration
        fWordSize     = 1;    // Get this from the launch configuration
        fSupportsValueModification = true;          // Get this from the launch configuration
        fSupportBaseAddressModification = false;    // Get this from the launch configuration
	}

	///////////////////////////////////////////////////////////////////////////
	// Memory monitors persistence
	///////////////////////////////////////////////////////////////////////////

	/*
	 * In the launch configuration file, the memory block entry is structured
	 *  as follows (note: this differs from CDI):
	 * 
	 *  <stringAttribute
	 *     key="org.eclipse.dsf.launch.MEMORY_BLOCKS" 
	 *     value="<?xml version="1.0" encoding="UTF-8" standalone="no"?>
	 *         <memoryBlockExpressionList context=[memory context ID]>
	 *             <memoryBlockExpression label=[monitor label] address=[base address]/>
	 *             <memoryBlockExpression ...>
	 *             ...
	 *         </memoryBlockExpressionList>
	 *         ...
     *         <memoryBlockExpressionList context=...>
     *             ...
     *         </memoryBlockExpressionList>"
	 *  />
	 */

	//-------------------------------------------------------------------------
	// Memory blocks memento tags
    //-------------------------------------------------------------------------

	// These 2 really belong in the DSF launch configuration class...
	private static final String DSF_LAUNCH_ID = "org.eclipse.dsf.launch"; //$NON-NLS-1$
	private static final String ATTR_DEBUGGER_MEMORY_BLOCKS = DSF_LAUNCH_ID + ".MEMORY_BLOCKS"; //$NON-NLS-1$
	
	private static final String MEMORY_BLOCK_EXPRESSION_LIST   = "memoryBlockExpressionList";   //$NON-NLS-1$
    private static final String ATTR_EXPRESSION_LIST_CONTEXT   = "context";                     //$NON-NLS-1$   
    private static final String MEMORY_BLOCK_EXPRESSION        = "memoryBlockExpression";       //$NON-NLS-1$
    private static final String ATTR_MEMORY_BLOCK_EXPR_LABEL   = "label";                       //$NON-NLS-1$
    private static final String ATTR_MEMORY_BLOCK_EXPR_ADDRESS = "address";                     //$NON-NLS-1$

    //-------------------------------------------------------------------------
    // Install persisted memory monitors
    //-------------------------------------------------------------------------

    /**
     * Restore the memory monitors from the memento in the launch configuration
     */
    public void initialize(final IMemoryDMContext memoryCtx) {
        try {
            final String memento = fLaunchConfig.getAttribute(ATTR_DEBUGGER_MEMORY_BLOCKS, ""); //$NON-NLS-1$
            if (memento != null && memento.trim().length() != 0) {
                // Submit the runnable to install the monitors on dispatch thread.
                getExecutor().submit(new Runnable() {
                    public void run() {
                        try {
                            createBlocksFromConfiguration(memoryCtx, memento);
                        } catch (CoreException e) {
                            DsfPlugin.getDefault().getLog().log(e.getStatus());
                        }
                    }
                });
            }
        } catch (CoreException e) {
            DsfPlugin.getDefault().getLog().log(e.getStatus());
        }
	}

	/**
	 * Create memory blocks based on the given memento (obtained from the launch
	 * configuration) and add them to the platform's IMemoryBlockManager. The
	 * memento was previously created by {@link #getMemento()}
	 * 
	 * @since 2.1
	 */
	protected void createBlocksFromConfiguration(IMemoryDMContext memoryCtx, String memento) throws CoreException {

	    // Parse the memento and validate its type
        Element root = DebugPlugin.parseDocument(memento);
		if (!root.getNodeName().equalsIgnoreCase(MEMORY_BLOCK_EXPRESSION_LIST)) {
	        IStatus status = new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, DebugPlugin.INTERNAL_ERROR,
	                "Memory monitor initialization: invalid memento", null);//$NON-NLS-1$
	        throw new CoreException(status);
		}
		    
        // Process the block list specific to this memory context
        // FIXME: (Bug228573) We only process the first entry...
	    if (root.getAttribute(ATTR_EXPRESSION_LIST_CONTEXT).equals(fContextString)) {
            List<IMemoryBlock> blocks = new ArrayList<IMemoryBlock>();
            NodeList expressionList = root.getChildNodes();
            int length = expressionList.getLength();
            for (int i = 0; i < length; ++i) {
                Node node = expressionList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element entry = (Element) node;
                    if (entry.getNodeName().equalsIgnoreCase(MEMORY_BLOCK_EXPRESSION)) {
                        String label   = entry.getAttribute(ATTR_MEMORY_BLOCK_EXPR_LABEL);
                        String address = entry.getAttribute(ATTR_MEMORY_BLOCK_EXPR_ADDRESS);
                        BigInteger blockAddress = new BigInteger(address);
                        DsfMemoryBlock block = new DsfMemoryBlock(this, memoryCtx, fModelId, label, blockAddress, fWordSize, 0);
                        blocks.add(block);
                    }
                }
            }
            DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks( blocks.toArray(new IMemoryBlock[blocks.size()]));
	    }
	}

    // FIXME: (Bug228573) Each retrieval overwrites the previous one :-(
	
    // In theory, we should make this a Job since we are writing to the file system.
	// However, this would cause the same racing condition as Bug228308. Finally, we
	// don't care too much about the UI responsiveness since we are in the process of
	// shutting down :-)
	public void saveMemoryBlocks() {
		try {
			ILaunchConfigurationWorkingCopy wc = fLaunchConfig.getWorkingCopy();
			wc.setAttribute(ATTR_DEBUGGER_MEMORY_BLOCKS, getMemento());
			wc.doSave();
		}
		catch( CoreException e ) {
            DsfPlugin.getDefault().getLog().log(e.getStatus());
		}
	}

	/**
	 * Create a memento to represent all active blocks created by this retrieval
	 * object (blocks currently registered with the platform's
	 * IMemoryBlockManager). We will be expected to recreate the blocks in
	 * {@link #createBlocksFromConfiguration(IMemoryDMContext, String)}.
	 * 
	 * @return a string memento
	 * @throws CoreException
	 */
	public String getMemento() throws CoreException {
		IMemoryBlock[] blocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(this);
		Document document = DebugPlugin.newDocument();
		Element expressionList = document.createElement(MEMORY_BLOCK_EXPRESSION_LIST);
		expressionList.setAttribute(ATTR_EXPRESSION_LIST_CONTEXT, fContextString);
		for (IMemoryBlock block : blocks) {
	          if (block instanceof IMemoryBlockExtension) {
	                IMemoryBlockExtension memoryBlock = (IMemoryBlockExtension) block;
	                Element expression = document.createElement(MEMORY_BLOCK_EXPRESSION);
	                expression.setAttribute(ATTR_MEMORY_BLOCK_EXPR_LABEL,   memoryBlock.getExpression());
                    expression.setAttribute(ATTR_MEMORY_BLOCK_EXPR_ADDRESS, memoryBlock.getBigBaseAddress().toString());
                    expressionList.appendChild(expression);
				}
		}
		document.appendChild(expressionList);
		return DebugPlugin.serializeDocument(document);
	}

	///////////////////////////////////////////////////////////////////////////
	// Accessors
	///////////////////////////////////////////////////////////////////////////

	public DsfSession getSession() {
	    return fSession;
	}
	
	public DsfExecutor getExecutor() {
		return fExecutor;
	}

	public ServiceTracker getServiceTracker() {
		return fMemoryServiceTracker;
	}

	///////////////////////////////////////////////////////////////////////////
	// Launch/Target specific information
	///////////////////////////////////////////////////////////////////////////

	public ILaunch getLaunch() {
		return fLaunch;
	}

	public IDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	public int getAddressSize() {
		return fAddressSize;
	}

	public int getAddressableSize() {
		return fWordSize;
	}

	public boolean supportsValueModification() {
		return fSupportsValueModification;
	}

	public boolean supportBaseAddressModification() {
		return fSupportBaseAddressModification;
	}

	///////////////////////////////////////////////////////////////////////////
	// IMemoryBlockRetrieval - obsoleted by IMemoryBlockRetrievalExtension
	///////////////////////////////////////////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	public boolean supportsStorageRetrieval() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
	 */
	public IMemoryBlock getMemoryBlock(final long startAddress,	final long length) throws DebugException {
	    throw new DebugException(new Status(
	        IStatus.ERROR, DsfPlugin.PLUGIN_ID, DebugException.NOT_SUPPORTED, 
	        "getMemoryBlock() not supported, use getExtendedMemoryBlock()", null)); //$NON-NLS-1$
	}

	///////////////////////////////////////////////////////////////////////////
	// IMemoryBlockRetrievalExtension
	///////////////////////////////////////////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension#getExtendedMemoryBlock(java.lang.String,
	 *      java.lang.Object)
	 */
	public IMemoryBlockExtension getExtendedMemoryBlock(String expression, Object context) throws DebugException {
        // Drill for the actual DMC
        IMemoryDMContext memoryDmc = null;
        IDMContext dmc = null;
        if (context instanceof IAdaptable) {
        	dmc = (IDMContext)((IAdaptable)context).getAdapter(IDMContext.class);
            if (dmc != null) {
                memoryDmc = DMContexts.getAncestorOfType(dmc, IMemoryDMContext.class);
            }
        }

        if (memoryDmc == null) {
            return null;
        }
        
		// The block start address (supports 64-bit processors)
		BigInteger blockAddress;

		/*
		 * See if the expression is a simple numeric value; if it is, we can
		 * avoid some costly processing (calling the back-end to resolve the
		 * expression and obtain an address)
		 */
		try {
			// First, assume a decimal address
			int base = 10;
			int offset = 0;

			// Check for "hexadecimality"
			if (expression.startsWith("0x") || expression.startsWith("0X")) { //$NON-NLS-1$//$NON-NLS-2$
				base = 16;
				offset = 2;
			}
			// Check for "binarity"
			else if (expression.startsWith("0b")) { //$NON-NLS-1$
				base = 2;
				offset = 2;
			}
			// Check for "octality"
			else if (expression.startsWith("0")) { //$NON-NLS-1$
				base = 8;
				offset = 1;
			}
			// Now, try to parse the expression. If a NumberFormatException is
			// thrown, then it wasn't a simple numerical expression and we go 
			// to plan B (attempt an expression evaluation)
			blockAddress = new BigInteger(expression.substring(offset), base);

		} catch (NumberFormatException nfexc) {
			// OK, expression is not a simple, absolute numeric value;
			// try to resolve as an expression.
			// In case of failure, simply return 'null'

			// Resolve the expression
			blockAddress = resolveMemoryAddress(dmc, expression);
			if (blockAddress == null) {
				return null;
			}
		}

		/*
		 * At this point, we only resolved the requested memory block
		 * start address and we have no idea of the block's length.
		 * 
		 * The renderer will provide this information when it calls
		 * getBytesFromAddress() i.e. after the memory block holder has
		 * been instantiated.
		 * 
		 * The down side is that every time we switch renderer, for the
		 * same memory block, a trip to the target could result. However,
		 * the memory request cache should save the day.
		 */

		 return new DsfMemoryBlock(this, memoryDmc, fModelId, expression, blockAddress, fWordSize, 0);
	}

	///////////////////////////////////////////////////////////////////////////
	// Helper functions
	///////////////////////////////////////////////////////////////////////////

	/**
	 * @since 2.1
	 */
	protected BigInteger resolveMemoryAddress(final IDMContext dmc, final String expression) throws DebugException {

		// Use a Query to "synchronize" the downstream calls
		Query<BigInteger> query = new Query<BigInteger>() {
			@Override
			protected void execute(final DataRequestMonitor<BigInteger> drm) {
				// Lookup for the ExpressionService
				final IExpressions expressionService = (IExpressions) fExpressionServiceTracker.getService();
				if (expressionService != null) {
					// Create the expression
					final IExpressionDMContext expressionDMC = expressionService.createExpression(dmc, expression);
					String formatId = IFormattedValues.HEX_FORMAT;
					FormattedValueDMContext valueDmc = expressionService.getFormattedValueContext(expressionDMC, formatId);
	                expressionService.getFormattedExpressionValue(
	                	valueDmc, 
	                    new DataRequestMonitor<FormattedValueDMData>(getExecutor(), drm) {
	            			@Override
	            			protected void handleSuccess() {
	            				// Store the result
	            				FormattedValueDMData data = getData();
            					String value = data.getFormattedValue().substring(2);	// Strip the "0x"
            					drm.setData(new BigInteger(value, 16));
	            				drm.done();
	            			}
	                	}
	                );
				}
			}
		};
		fExecutor.execute(query);

		try {
			// The happy case
			return query.get();
		} catch (InterruptedException e) {
			throw new DebugException(new Status(IStatus.ERROR,
					DsfPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Error evaluating memory address (InterruptedException).", e)); //$NON-NLS-1$

		} catch (ExecutionException e) {
			throw new DebugException(new Status(IStatus.ERROR,
					DsfPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Error evaluating memory address (ExecutionException).", e)); //$NON-NLS-1$
		}
	}
	
	
	/**
	 * Return the model ID specified at construction 
	 * 
	 * @since 2.1
	 */
	protected String getModelId() {
		return fModelId;
	}

}
