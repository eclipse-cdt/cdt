/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
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
package org.eclipse.dd.dsf.debug.model;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.Query;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.DsfDebugPlugin;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.debug.service.IMemory;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMData;
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
 */
public class DsfMemoryBlockRetrieval extends PlatformObject implements IMemoryBlockRetrievalExtension
{
	private final String fModelId;
	private final DsfSession fSession;
	private final DsfExecutor fExecutor;
	private       IDMContext<?> fContext;
	private final ServiceTracker fMemoryServiceTracker;
	private final ServiceTracker fExpressionServiceTracker;

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
			throw new IllegalArgumentException(
					"Session for context " + fContext + " is not active"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		fExecutor = fSession.getExecutor();
 		BundleContext bundle = DsfDebugPlugin.getBundleContext();

 		// Here we chose to use 2 distinct service trackers instead of an
 		// amalgamated one because it is less error prone (and we are lazy).

 		// Create a tracker for the MemoryService
 		String memoryServiceFilter = "(&" + //$NON-NLS-1$
				"(OBJECTCLASS=" //$NON-NLS-1$
				+ IMemory.class.getName()
				+ ")" + //$NON-NLS-1$
				"(" + IDsfService.PROP_SESSION_ID //$NON-NLS-1$
				+ "=" + dmc.getSessionId() + ")" + //$NON-NLS-1$//$NON-NLS-2$
				")"; //$NON-NLS-1$

 		try {
			fMemoryServiceTracker = new ServiceTracker(
					bundle,	bundle.createFilter(memoryServiceFilter), null);
		} catch (InvalidSyntaxException e) {
			throw new DebugException(new Status(IStatus.ERROR,
					DsfDebugPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Error creating service filter.", e)); //$NON-NLS-1$
		}
		fMemoryServiceTracker.open();

		// Create a tracker for the ExpressionService
 		String expressionServiceFilter = "(&" + //$NON-NLS-1$
				"(OBJECTCLASS=" //$NON-NLS-1$
				+ IExpressions.class.getName()
				+ ")" + //$NON-NLS-1$
				"(" + IDsfService.PROP_SESSION_ID //$NON-NLS-1$
				+ "=" + dmc.getSessionId() + ")" + //$NON-NLS-1$//$NON-NLS-2$
				")"; //$NON-NLS-1$

		try {
			fExpressionServiceTracker = new ServiceTracker(
					bundle, bundle.createFilter(expressionServiceFilter), null);
		} catch (InvalidSyntaxException e) {
			throw new DebugException(new Status(IStatus.ERROR,
					DsfDebugPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Error creating service filter.", e)); //$NON-NLS-1$
		}
		fExpressionServiceTracker.open();
	}

	// ////////////////////////////////////////////////////////////////////////
	// Accessors
	// ////////////////////////////////////////////////////////////////////////

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
		return fMemoryServiceTracker;
	}

	// ////////////////////////////////////////////////////////////////////////
	// IMemoryBlockRetrieval - obsoleted by IMemoryBlockRetrievalExtension
	// ////////////////////////////////////////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	public boolean supportsStorageRetrieval() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long,
	 *      long)
	 */
	public IMemoryBlock getMemoryBlock(final long startAddress,	final long length) throws DebugException {
		// The expression to display in the rendering tab
		// Put here for the sake of completeness (not used with IMemoryBlockExtension)
		String expression = "0x" + Long.toHexString(startAddress); //$NON-NLS-1$
		return new DsfMemoryBlock(this, fModelId, expression, BigInteger.valueOf(startAddress), length);
	}

	// ////////////////////////////////////////////////////////////////////////
	// IMemoryBlockRetrievalExtension
	// ////////////////////////////////////////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension#getExtendedMemoryBlock(java.lang.String,
	 *      java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public IMemoryBlockExtension getExtendedMemoryBlock(String expression, Object context) throws DebugException {

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

			// Drill for the actual DMC
		    IDMContext<?> dmc = null;
			if (context instanceof IAdaptable) {
			    dmc = (IDMContext<?>)((IAdaptable)context).getAdapter(IDMContext.class);
			}

			if (dmc == null) {
                return null;
            }
		    // Update the DMC
			fContext = dmc;

			// Resolve the expression
			blockAddress = resolveMemoryAddress(fContext, expression);
			if (blockAddress == null)
				return null;
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

		return new DsfMemoryBlock(this, fModelId, expression, blockAddress, 0);
	}

	// ////////////////////////////////////////////////////////////////////////
	// Helper functions
	// ////////////////////////////////////////////////////////////////////////

	private BigInteger resolveMemoryAddress(final IDMContext<?> idmContext, final String expression) throws DebugException {

		// Use a Query to "synchronize" the inherently asynchronous downstream calls
		Query<BigInteger> query = new Query<BigInteger>() {
			@Override
			protected void execute(final DataRequestMonitor<BigInteger> rm) {
				// Lookup for the ExpressionService
				final IExpressions expressionService = (IExpressions) fExpressionServiceTracker.getService();
				if (expressionService != null) {
					// Create the expression
					final IExpressionDMContext expressionDMC = expressionService.createExpression(idmContext, expression);
					expressionService.getModelData(expressionDMC, new DataRequestMonitor<IExpressionDMData>(getExecutor(), rm) {
						@Override
						protected void handleOK() {
							// Evaluate the expression - request HEX since it works in every case 
							String formatId = IFormattedValues.HEX_FORMAT;
							FormattedValueDMContext valueDmc = expressionService.getFormattedValue(expressionDMC, formatId);
			                expressionService.getModelData(
			                	valueDmc, 
			                    new DataRequestMonitor<FormattedValueDMData>(getExecutor(), null) {
			            			@Override
			            			protected void handleOK() {
			            				// Store the result
			            				FormattedValueDMData data = getData();
			            				if (data.isValid()) {
			            					String value = data.getFormattedValue().substring(2);	// Strip the "0x"
			            					rm.setData(new BigInteger(value, 16));
			            				}
			            				rm.done();
			            			}
			                	}
			                );
						}
					});
				}

			}
		};
		fExecutor.execute(query);
		try {
			// The happy case
			return query.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		}

		// The error case
		return null;
	}

}
