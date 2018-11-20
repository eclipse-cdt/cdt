/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval.IMemoryBlockAddressInfoItem;
import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlock;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemorySpaces2;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMData;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.memory.IGdbMemoryAddressInfoTypeRetrieval;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IMemoryBlock;

public class GdbMemoryAddressInfoVariablesRetrieval implements IGdbMemoryAddressInfoTypeRetrieval {

	private final static String VARIABLES_INFO_TYPE = "Variables"; //$NON-NLS-1$
	private final static int LOCALS_COLOR = 0xB630D1;
	private final static int POINTER_COLOR = 0xFF0000;
	private final static String DEREF_CHAR = "*"; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private final DsfSession fSession;

	public GdbMemoryAddressInfoVariablesRetrieval(DsfSession session) {
		fSession = session;
	}

	private static class MemoryBlockAddressVariableItem extends MemoryBlockAddressInfoItem {
		public MemoryBlockAddressVariableItem(String name, String value) {
			super(name, value);
		}

		public MemoryBlockAddressVariableItem(String name, BigInteger addressValue, BigInteger dataTypeSize,
				int color) {
			super(name, addressValue, dataTypeSize, color);
		}

		@Override
		public String getInfoType() {
			return VARIABLES_INFO_TYPE;
		}
	}

	private final static class ExpressionBin {
		private final IExpressionDMContext fContext;
		private IExpressionDMAddress fAddress;
		private boolean fDereferenced;

		public ExpressionBin(IExpressionDMContext expDmc, boolean dereferenced) {
			fContext = expDmc;
			fDereferenced = dereferenced;
		}

		boolean isDereferenced() {
			return fDereferenced;
		}

		boolean isComplete() {
			if (fContext != null && fAddress != null) {
				return true;
			}
			return false;
		}
	}

	@Override
	public void itemsRequest(final IDMContext context, final IMemoryBlock memBlock,
			final DataRequestMonitor<IMemoryBlockAddressInfoItem[]> rm) {
		if (fSession == null || fSession.getExecutor() == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Initialization problem, invalid session")); //$NON-NLS-1$
			return;
		}

		// resolve handles to the current context
		final IFrameDMContext frameDmc = DMContexts.getAncestorOfType(context, IFrameDMContext.class);
		final IStack stackFrameService = resolveService(IStack.class);
		final IExpressions expressionService = resolveService(IExpressions.class);

		// validate context
		if (frameDmc == null || expressionService == null || stackFrameService == null) {
			rm.done(new Status(IStatus.INFO, GdbPlugin.PLUGIN_ID,
					"Unable to resolve Variables for the currently selected context")); //$NON-NLS-1$
			return;
		}

		// Call IStack.getLocals() to get an array of IVariableDMContext objects representing the local
		// variables in the stack frame represented by frameDmc.
		final DsfExecutor dsfExecutor = fSession.getExecutor();
		stackFrameService.getLocals(frameDmc, new DataRequestMonitor<IVariableDMContext[]>(dsfExecutor, rm) {
			@Override
			protected void handleSuccess() {
				// For each IVariableDMContext object returned by IStack.getLocals(), call
				// MIStackFrameService.getModelData() to get the IVariableDMData object. This requires
				// a MultiRequestMonitor object.

				// First, get the data model context objects for the local variables.

				IVariableDMContext[] localsDMCs = getData();

				if (localsDMCs == null || localsDMCs.length == 0) {
					// There are no locals so just complete the request
					rm.setData(new IMemoryBlockAddressInfoItem[0]);
					rm.done();
					return;
				}

				// Create a List in which we store the DM data objects for the local variables. This is
				// necessary because there is no MultiDataRequestMonitor. :)

				final List<IVariableDMData> localsDMData = new ArrayList<>();

				// Create the MultiRequestMonitor to handle completion of the set of getModelData() calls.

				final CountingRequestMonitor crm = new CountingRequestMonitor(dsfExecutor, rm) {
					@Override
					public void handleSuccess() {
						// Now that all the calls to getModelData() are complete, we create an
						// IExpressionDMContext object for each local variable name, saving them all
						// in an array.
						ExpressionBin[] expressionBins = new ExpressionBin[localsDMData.size() * 2];
						int i = 0;
						for (IVariableDMData localDMData : localsDMData) {

							expressionBins[i++] = createExpression(expressionService, frameDmc, localDMData.getName(),
									false);
							expressionBins[i++] = createExpression(expressionService, frameDmc,
									DEREF_CHAR + localDMData.getName(), true);
						}

						// Lastly, we fill the update from the array of view model context objects
						// that reference the ExpressionDMC objects for the local variables. This is
						// the last code to run for a given call to updateElementsInSessionThread().
						// We can now leave anonymous-inner-class hell.
						resolveItems(expressionService, expressionBins, frameDmc, memBlock, rm);
					}
				};

				int countRM = 0;
				// Perform a set of getModelData() calls, one for each local variable's data model
				// context object. In the handleCompleted() method of the DataRequestMonitor, add the
				// IVariableDMData object to the localsDMData List for later processing (see above).
				for (IVariableDMContext localDMC : localsDMCs) {
					stackFrameService.getVariableData(localDMC,
							new DataRequestMonitor<IVariableDMData>(dsfExecutor, crm) {
								@Override
								public void handleSuccess() {
									localsDMData.add(getData());
									crm.done();
								}
							});

					countRM++;
				}
				crm.setDoneCount(countRM);
			}
		});
	}

	@Override
	public String getInfoType() {
		return VARIABLES_INFO_TYPE;
	}

	private void resolveItems(final IExpressions expressionService, final ExpressionBin[] expressionsBins,
			final IFrameDMContext frameDmc, final IMemoryBlock memBlock,
			final DataRequestMonitor<IMemoryBlockAddressInfoItem[]> rm) {
		final DsfExecutor executor = expressionService.getExecutor();

		resolveAddressData(expressionService, expressionsBins, new RequestMonitor(executor, rm) {
			@Override
			protected void handleCompleted() {
				// resolve the default memory space id for the current context
				IMemorySpaces2 memSpaceService = resolveService(IMemorySpaces2.class);
				if (memSpaceService != null) {
					memSpaceService.getDefaultMemorySpace(frameDmc, new DataRequestMonitor<String>(executor, rm) {
						@Override
						protected void handleCompleted() {
							String defaultMemSpaceId = getData();
							rm.setData(createAddressInfoItems(expressionsBins, memBlock, defaultMemSpaceId));
							rm.done();
						}
					});
				} else {
					rm.setData(createAddressInfoItems(expressionsBins, memBlock, EMPTY_STRING));
					rm.done();
				}
			}
		});
	}

	private <V> V resolveService(Class<V> type) {
		V service = null;
		if (fSession != null) {
			DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getDefault().getBundle().getBundleContext(),
					fSession.getId());
			service = tracker.getService(type);
			tracker.dispose();
		}
		return service;
	}

	private ExpressionBin createExpression(IExpressions expressionService, final IDMContext dmc,
			final String expression, boolean dereferenced) {
		IExpressionDMContext exprDMC = expressionService.createExpression(dmc, expression);

		// if (fCastToTypeSupport != null) {
		// exprDMC = fCastToTypeSupport.replaceWithCastedExpression(exprDMC);
		// }
		return new ExpressionBin(exprDMC, dereferenced);
	}

	private void resolveAddressData(IExpressions expressionService, ExpressionBin[] expressionsBins,
			final RequestMonitor rm) {
		DsfExecutor executor = expressionService.getExecutor();
		final CountingRequestMonitor crm = new CountingRequestMonitor(executor, rm);
		for (final ExpressionBin expBin : expressionsBins) {
			expressionService.getExpressionAddressData(expBin.fContext,
					new DataRequestMonitor<IExpressionDMAddress>(executor, crm) {
						@Override
						protected void handleCompleted() {
							if (isSuccess()) {
								expBin.fAddress = getData();
							}
							crm.done();
						}
					});
		}

		crm.setDoneCount(expressionsBins.length);
	}

	private IMemoryBlockAddressInfoItem[] createAddressInfoItems(ExpressionBin[] contentsBins, IMemoryBlock memBlock,
			String ctxDefaultMemSpaceId) {

		int length = contentsBins.length;
		final List<IMemoryBlockAddressInfoItem> infoItems = new ArrayList<>();

		// Resolve the memory space id of the memory block
		String memBlockMemSpaceId = EMPTY_STRING;
		if (memBlock instanceof IMemorySpaceAwareMemoryBlock) {
			String tMemBlockMemSpace = ((IMemorySpaceAwareMemoryBlock) memBlock).getMemorySpaceID();
			memBlockMemSpaceId = tMemBlockMemSpace == null ? EMPTY_STRING : tMemBlockMemSpace;
		}

		for (int i = 0; i < length; i++) {
			ExpressionBin expBin = contentsBins[i];
			if (!expBin.isComplete()) {
				// invalid item
				continue;
			}

			IExpressionDMAddress dmAddress = expBin.fAddress;
			BigInteger addressValue = dmAddress.getAddress().getValue();
			// Skip addresses of zero, likely not yet initialized
			if (!addressValue.equals(BigInteger.ZERO)) {
				String name = expBin.fContext.getExpression();
				BigInteger exprSize = BigInteger.valueOf(dmAddress.getSize());

				final int color;
				if (expBin.isDereferenced()) {
					color = POINTER_COLOR;
				} else {
					color = LOCALS_COLOR;
				}

				// Resolve the memory space of the expression
				String exprMemSpaceId = EMPTY_STRING;
				String exprMemSpace = dmAddress.getMemorySpaceID();
				exprMemSpaceId = exprMemSpace != null ? exprMemSpace : EMPTY_STRING;

				// if the memory space of the block is valid and the memory space id of the expression is empty,
				// use the context default memory space id for the expression.
				if (memBlockMemSpaceId.length() > 0 && exprMemSpaceId.length() == 0) {
					exprMemSpaceId = ctxDefaultMemSpaceId;
				}

				// If the expression's address is in the same memory space as the memory block create a new item
				if (exprMemSpaceId.equals(memBlockMemSpaceId)) {
					infoItems.add(new MemoryBlockAddressVariableItem(name, addressValue, exprSize, color));
				}
			}
		}

		return infoItems.toArray(new IMemoryBlockAddressInfoItem[infoItems.size()]);
	}
}
