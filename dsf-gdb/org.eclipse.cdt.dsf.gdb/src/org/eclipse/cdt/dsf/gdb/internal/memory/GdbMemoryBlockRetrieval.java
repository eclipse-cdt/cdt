/*******************************************************************************
 * Copyright (c) 2010, Texas Instruments, Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Texas Instruments, Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlock;
import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlock;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlockRetrieval;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemorySpaces;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.service.DsfServices;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A specialization of the DSF memory block retrieval implementation supporting
 * memory spaces. The memory space support is provisional, thus this class is
 * internal.
 * 
 * @author Alain Lee and John Cortell
 */
public class GdbMemoryBlockRetrieval extends DsfMemoryBlockRetrieval implements
		IMemorySpaceAwareMemoryBlockRetrieval {

    private final ServiceTracker  fMemorySpaceServiceTracker;

	// No need to use the constants in our base class. Serializing and
	// recreating the blocks is done entirely by us
	private static final String MEMORY_BLOCK_EXPRESSION_LIST   = "memoryBlockExpressionList";   //$NON-NLS-1$
    private static final String ATTR_EXPRESSION_LIST_CONTEXT   = "context";                     //$NON-NLS-1$   
    private static final String MEMORY_BLOCK_EXPRESSION        = "gdbmemoryBlockExpression";       //$NON-NLS-1$
    private static final String ATTR_MEMORY_BLOCK_EXPR_LABEL   = "label";                       //$NON-NLS-1$
    private static final String ATTR_MEMORY_BLOCK_EXPR_ADDRESS = "address";                     //$NON-NLS-1$
    private static final String ATTR_MEMORY_BLOCK_MEMORY_SPACE_ID = "memorySpaceID"; 			//$NON-NLS-1$
    
    /** see comment in base class */ 
    private static final String CONTEXT_RESERVED = "reserved-for-future-use"; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	public GdbMemoryBlockRetrieval(String modelId, ILaunchConfiguration config,
			DsfSession session) throws DebugException {
		super(modelId, config, session);
		

 		BundleContext bundle = GdbPlugin.getBundleContext();
 		
		// Create a tracker for the MemoryPageService
 		String memoryPageServiceFilter = DsfServices.createServiceFilter(IMemorySpaces.class, session.getId());

 		try {
			fMemorySpaceServiceTracker = new ServiceTracker(
					bundle,	bundle.createFilter(memoryPageServiceFilter), null);
		} catch (InvalidSyntaxException e) {
			throw new DebugException(new Status(IStatus.ERROR,
					GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Error creating service filter.", e)); //$NON-NLS-1$
		}
		fMemorySpaceServiceTracker.open();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.model.DsfMemoryBlockRetrieval#getExtendedMemoryBlock(java.lang.String, java.lang.Object)
	 */
	@Override
	public IMemoryBlockExtension getExtendedMemoryBlock(String expression, Object context) throws DebugException {
		// Technically, we don't need to override this method. Letting our base
		// class create a DsfMemoryBlock would work just fine. But, for the
		// sake of consistency, lets have this retrieval class always return a
		// GdbMemoryBlock.
		return getMemoryBlock(expression, context, null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval#getExtendedMemoryBlock(java.lang.String, java.lang.Object, java.lang.String)
	 */
	public IMemorySpaceAwareMemoryBlock getMemoryBlock(String expression, Object context, String memorySpaceID) throws DebugException {
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
		return new GdbMemoryBlock(this, memoryDmc, getModelId(), expression, blockAddress, getAddressableSize(), 0, memorySpaceID);
	}

	/*
	 * implementation of
	 *    @see org.eclipse.cdt.debug.internal.core.model.provisional.IMemorySpaceManagement#getMemorySpaces(Object context)
	 */
	public void getMemorySpaces(final Object context, final GetMemorySpacesRequest request) {
		Query<String[]> query = new Query<String[]>() {
			@Override
			protected void execute(final DataRequestMonitor<String[]> drm) {
		        IDMContext dmc = null;
		        if (context instanceof IAdaptable) {
		        	dmc = (IDMContext)((IAdaptable)context).getAdapter(IDMContext.class);
		            if (dmc != null) {
		        		IMemorySpaces memoryPageService = (IMemorySpaces)fMemorySpaceServiceTracker.getService();
		                if (memoryPageService != null) {
		        			memoryPageService.getMemorySpaces(
		        				dmc, 
		        				new DataRequestMonitor<String[]>(getExecutor(), drm) {
			            			@Override
			            			protected void handleCompleted() {
			            				// Store the result
			            				if (isSuccess()) {
			            					request.setMemorySpaces(getData());
			            				}
			            				else {
			            					request.setStatus(getStatus());
			            				}
		            					request.done();
			            				drm.done(); // don't bother with status; we don't check it below
			            			}
		        				});
		                }
		                else {
		                	request.setStatus(new Status(IStatus.ERROR,	GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, Messages.Err_MemoryServiceNotAvailable, null));
		                	request.done();
            				drm.done();		                	
		                }
		            }
		        }
			}
		};
		getExecutor().execute(query);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval#encodeAddress(java.lang.String, java.lang.String)
	 */
	public String encodeAddress(String expression, String memorySpaceID) {
		String result = null;
    	IMemorySpaces service = (IMemorySpaces)fMemorySpaceServiceTracker.getService();
        if (service != null) {
        	// the service can tell us to use our default encoding by returning null
			result = service.encodeAddress(expression, memorySpaceID);
        }
        if (result == null) {
        	// default encoding
        	result = memorySpaceID + ':' + expression;
        }
		return result; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval#decodeAddress(java.lang.String)
	 */
	public DecodeResult decodeAddress(String str) throws CoreException {
    	IMemorySpaces memoryPageService = (IMemorySpaces)fMemorySpaceServiceTracker.getService();
    	if (memoryPageService != null) {
    		final IMemorySpaces.DecodeResult result = memoryPageService.decodeAddress(str);
    		if (result != null) {	// service can return null to tell use to use default decoding 
	    		return new DecodeResult() {
					public String getMemorySpaceId() { return result.getMemorySpaceId(); }
					public String getExpression() { return result.getExpression(); }
				};
    		}
    	}
    	
    	// default decoding
		int index = str.indexOf(':');
		if (index == -1) {
			throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, Messages.Err_InvalidEncodedAddress + ": " + str , null)); //$NON-NLS-1$
		}

		final String memorySpaceID = str.substring(0, index);
		final String expression = (index < str.length()-1) ? str.substring(index+1) : ""; //$NON-NLS-1$
		return new DecodeResult() {
			public String getMemorySpaceId() { return memorySpaceID; }
			public String getExpression() { return expression; }
		};
 
	}

	ServiceTracker getMemorySpaceServiceTracker() {
		return fMemorySpaceServiceTracker;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.model.DsfMemoryBlockRetrieval#getMemento()
	 */
	@Override
	public String getMemento() throws CoreException {
		IMemoryBlock[] blocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(this);
		Document document = DebugPlugin.newDocument();
		Element expressionList = document.createElement(MEMORY_BLOCK_EXPRESSION_LIST);
		expressionList.setAttribute(ATTR_EXPRESSION_LIST_CONTEXT, CONTEXT_RESERVED); 
		for (IMemoryBlock block : blocks) {
			if (block instanceof IMemoryBlockExtension) {
				IMemoryBlockExtension memoryBlock = (IMemoryBlockExtension) block;
				Element expression = document.createElement(MEMORY_BLOCK_EXPRESSION);
				expression.setAttribute(ATTR_MEMORY_BLOCK_EXPR_ADDRESS, memoryBlock.getBigBaseAddress().toString());
				if (block instanceof IMemorySpaceAwareMemoryBlock) {
					String memorySpaceID = ((IMemorySpaceAwareMemoryBlock)memoryBlock).getMemorySpaceID();
					if (memorySpaceID != null) {
						expression.setAttribute(ATTR_MEMORY_BLOCK_MEMORY_SPACE_ID, memorySpaceID);
						
						// What we return from GdbMemoryBlock#getExpression()
						// is the encoded representation. We need to decode it
						// to get the original expression used to create the block
						DecodeResult result = ((IMemorySpaceAwareMemoryBlockRetrieval)memoryBlock.getMemoryBlockRetrieval()).decodeAddress(memoryBlock.getExpression());
						expression.setAttribute(ATTR_MEMORY_BLOCK_EXPR_LABEL, result.getExpression());
					}
					else {
						expression.setAttribute(ATTR_MEMORY_BLOCK_EXPR_LABEL, memoryBlock.getExpression());
					}
				}
				else {
					assert false; // should never happen (see getExtendedMemoryBlock()), but we can handle it. 
					expression.setAttribute(ATTR_MEMORY_BLOCK_EXPR_LABEL, memoryBlock.getExpression());
				}
				expressionList.appendChild(expression);
			}
		}
		document.appendChild(expressionList);
		return DebugPlugin.serializeDocument(document);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.model.DsfMemoryBlockRetrieval#createBlocksFromConfiguration(org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext, java.lang.String)
	 */
	@Override
	protected void createBlocksFromConfiguration(IMemoryDMContext memoryCtx, String memento) throws CoreException {

	    // Parse the memento and validate its type
        Element root = DebugPlugin.parseDocument(memento);
		if (!root.getNodeName().equalsIgnoreCase(MEMORY_BLOCK_EXPRESSION_LIST)) {
	        IStatus status = new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugPlugin.INTERNAL_ERROR,
	                "Memory monitor initialization: invalid memento", null);//$NON-NLS-1$
	        throw new CoreException(status);
		}
		    
        // Process the block list specific to this memory context
        // FIXME: (Bug228573) We only process the first entry...
	    if (root.getAttribute(ATTR_EXPRESSION_LIST_CONTEXT).equals(CONTEXT_RESERVED)) {
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

                        String memorySpaceID = null;
                        if (entry.hasAttribute(ATTR_MEMORY_BLOCK_MEMORY_SPACE_ID)) {
                        	memorySpaceID = entry.getAttribute(ATTR_MEMORY_BLOCK_MEMORY_SPACE_ID);
                        	if (memorySpaceID.length() == 0) {
                        		memorySpaceID = null; 
                        		assert false : "should have either no memory space or a valid (non-empty) ID"; //$NON-NLS-1$	
                        	}
                        }

                        BigInteger blockAddress = new BigInteger(address);
                        DsfMemoryBlock block = new GdbMemoryBlock(this, memoryCtx, getModelId(), label, blockAddress, getAddressableSize(), 0, memorySpaceID);
                        blocks.add(block);
                    }
                }
            }
            DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks( blocks.toArray(new IMemoryBlock[blocks.size()]));
	    }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval#creatingBlockRequiresMemorySpaceID()
	 */
	public boolean creatingBlockRequiresMemorySpaceID() {
		IMemorySpaces memoryPageService = (IMemorySpaces)fMemorySpaceServiceTracker.getService();
        if (memoryPageService != null) {
        	return memoryPageService.creatingBlockRequiresMemorySpaceID();
        }
		return false;
	}
}
