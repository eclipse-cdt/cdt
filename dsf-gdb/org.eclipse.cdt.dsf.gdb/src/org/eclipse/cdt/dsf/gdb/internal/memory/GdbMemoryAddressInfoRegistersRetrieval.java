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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval.IMemoryBlockAddressInfoItem;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters2;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.memory.IGdbMemoryAddressInfoTypeRetrieval;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IMemoryBlock;

public class GdbMemoryAddressInfoRegistersRetrieval implements IGdbMemoryAddressInfoTypeRetrieval {

	private final static String REGISTERS_INFO_TYPE = "Registers"; //$NON-NLS-1$
	private final DsfSession fSession;

	public GdbMemoryAddressInfoRegistersRetrieval(DsfSession session) {
		fSession = session;
	}

	@Override
	public void itemsRequest(final IDMContext context, final IMemoryBlock memBlock,
			final DataRequestMonitor<IMemoryBlockAddressInfoItem[]> rm) {
		if (fSession == null || fSession.getExecutor() == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Initialization problem, invalid session")); //$NON-NLS-1$
			return;
		}

		final DsfExecutor executor = fSession.getExecutor();
		IFrameDMContext frameCtx = DMContexts.getAncestorOfType(context, IFrameDMContext.class);
		final IRegisters2 service = resolveService(IRegisters2.class);

		if (frameCtx != null && service != null) {
			// Getting all available register contexts
			service.getRegisters(frameCtx, new DataRequestMonitor<IRegisterDMContext[]>(executor, rm) {
				@Override
				protected void handleSuccess() {
					final IRegisterDMContext[] registers = getData();
					if (registers != null && registers.length != 0) {
						// Resolve register data
						getRegistersData(service, registers,
								new DataRequestMonitor<IRegisterDMData[]>(fSession.getExecutor(), rm) {
									@Override
									protected void handleSuccess() {
										final IRegisterDMData[] regBaseData = getData();
										// Resolve register values
										getRegisterValues(service, registers,
												new DataRequestMonitor<FormattedValueDMData[]>(fSession.getExecutor(),
														rm) {
													@Override
													protected void handleSuccess() {
														// Extract the register information needed to build the requested data items
														FormattedValueDMData[] regFormattedData = getData();
														String[] regNames = extractRegNames(regBaseData);
														String[] regValues = extractValues(regFormattedData);

														// Consolidate the Register information in a container class
														IMemoryBlockAddressInfoItem[] regDataContainers = createRegisterDataContainers(
																registers, regNames, regValues);

														// Filter or arrange data as needed
														regDataContainers = normalizeRegisterData(regDataContainers,
																memBlock);

														rm.setData(regDataContainers);
														rm.done();
													}
												});

									}
								});
					} else {
						// no valid registers
						rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
								"Successful request of register contexts but no registers received")); //$NON-NLS-1$
					}
				}
			});
		} else {
			rm.done(new Status(IStatus.INFO, GdbPlugin.PLUGIN_ID,
					"Unable to resolve registers for the currently selected context")); //$NON-NLS-1$
		}
	}

	protected IMemoryBlockAddressInfoItem[] normalizeRegisterData(IMemoryBlockAddressInfoItem[] regDataContainers,
			IMemoryBlock memBlock) {
		//		// The information provided may be applicable per memory space
		//		String memSpaceId = ""; //$NON-NLS-1$
		//		if (memBlock instanceof IMemorySpaceAwareMemoryBlock) {
		//			memSpaceId = ((IMemorySpaceAwareMemoryBlock) memBlock).getMemorySpaceID();
		//		}

		List<IMemoryBlockAddressInfoItem> items = new ArrayList<>();
		// Remove all items with value zero
		for (IMemoryBlockAddressInfoItem item : regDataContainers) {
			if (item.getAddress().intValue() != 0) {
				items.add(item);
			}
		}

		// Add any other filter, value adjustments etc.

		return items.toArray(new IMemoryBlockAddressInfoItem[items.size()]);
	}

	@Override
	public String getInfoType() {
		return REGISTERS_INFO_TYPE;
	}

	private class MemoryBlockAddressRegisterItem extends MemoryBlockAddressInfoItem {
		public MemoryBlockAddressRegisterItem(String name, String value) {
			super(name, value);
		}

		@Override
		public String getInfoType() {
			return REGISTERS_INFO_TYPE;
		}
	}

	private IMemoryBlockAddressInfoItem[] createRegisterDataContainers(IRegisterDMContext[] regContext, String[] names,
			String[] values) {
		// Expecting the three building arrays to be of the same length
		assert (regContext.length > 0 && regContext.length == names.length && regContext.length == values.length);
		IMemoryBlockAddressInfoItem[] regContainers = new IMemoryBlockAddressInfoItem[regContext.length];
		for (int i = 0; i < regContext.length; i++) {
			regContainers[i] = new MemoryBlockAddressRegisterItem(names[i], values[i]);
		}

		return regContainers;
	}

	private String[] extractRegNames(IRegisterDMData[] regData) {
		String[] names = new String[regData.length];
		for (int i = 0; i < regData.length; i++) {
			names[i] = regData[i].getName();
		}
		return names;
	}

	private String[] extractValues(FormattedValueDMData[] regValues) {
		String[] values = new String[regValues.length];
		for (int i = 0; i < regValues.length; i++) {
			values[i] = regValues[i].getEditableValue();
		}
		return values;
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

	private void getRegistersData(final IRegisters2 service, final IRegisterDMContext[] regDMCs,
			final DataRequestMonitor<IRegisterDMData[]> rm) {
		final IRegisterDMData[] datas = new IRegisterDMData[regDMCs.length];
		rm.setData(datas);
		final CountingRequestMonitor countingRm = new CountingRequestMonitor(fSession.getExecutor(), rm);
		for (int i = 0; i < regDMCs.length; i++) {
			final int index = i;
			service.getRegisterData(regDMCs[index],
					new DataRequestMonitor<IRegisterDMData>(fSession.getExecutor(), countingRm) {
						@Override
						protected void handleCompleted() {
							datas[index] = getData();
							if (!isSuccess()) {
								countingRm.setStatus(getStatus());
							}
							countingRm.done();
						}
					});
		}

		countingRm.setDoneCount(regDMCs.length);
	}

	private FormattedValueDMContext[] getFormatContexts(IRegisters2 service, IRegisterDMContext[] regDMCs) {
		FormattedValueDMContext[] datas = new FormattedValueDMContext[regDMCs.length];
		for (int i = 0; i < regDMCs.length; i++) {
			datas[i] = service.getFormattedValueContext(regDMCs[i], IFormattedValues.HEX_FORMAT);
		}

		return datas;
	}

	private void getRegisterValues(final IRegisters2 service, final IRegisterDMContext[] regDMCs,
			final DataRequestMonitor<FormattedValueDMData[]> rm) {
		FormattedValueDMContext[] fmtContexts = getFormatContexts(service, regDMCs);

		final FormattedValueDMData[] datas = new FormattedValueDMData[regDMCs.length];
		rm.setData(datas);
		final CountingRequestMonitor countingRm = new CountingRequestMonitor(fSession.getExecutor(), rm);
		for (int i = 0; i < regDMCs.length; i++) {
			final int index = i;
			service.getFormattedExpressionValue(fmtContexts[index],
					new DataRequestMonitor<FormattedValueDMData>(fSession.getExecutor(), countingRm) {
						@Override
						protected void handleCompleted() {
							datas[index] = getData();
							if (!isSuccess()) {
								countingRm.setStatus(getStatus());
							}
							countingRm.done();
						}
					});
		}

		countingRm.setDoneCount(regDMCs.length);
	}
}
