/*******************************************************************************
 * Copyright (c) 2013, 2022 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Mentor Graphics - Initial API and implementation
 * 		John Dallaway - Add methods to get the endianness and address size (Bug 225609)
 * 		Philippe Gil (AdaCore) - Switch to c language when getting sizeof(void *) when required (Bug 421541)
 *      Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *      John Dallaway - Enhance memory data initialization checks (#138)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.MIMemory;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIAddressableSizeInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIShowEndianInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBShowLanguageInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * @since 4.2
 */
public class GDBMemory extends MIMemory implements IGDBMemory2 {

	private IGDBControl fCommandControl;

	/**
	 * Cache of the address sizes for each memory context.
	 */
	private Map<IMemoryDMContext, Integer> fAddressSizes = new HashMap<>();

	/**
	 * Cache of the addressable sizes for each memory context.
	 */
	private Map<IMemoryDMContext, Integer> fAddressableSizes = new HashMap<>();

	/**
	 * Cache of the endianness for each memory context.
	 */
	private Map<IMemoryDMContext, Boolean> fIsBigEndian = new HashMap<>();

	public GDBMemory(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(final RequestMonitor requestMonitor) {
		fCommandControl = getServicesTracker().getService(IGDBControl.class);
		getSession().addServiceEventListener(this, null);
		register(new String[] { IMemory.class.getName(), MIMemory.class.getName(), IGDBMemory.class.getName(),
				IGDBMemory2.class.getName(), GDBMemory.class.getName(), }, new Hashtable<String, String>());
		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		getSession().removeServiceEventListener(this);
		fAddressableSizes.clear();
		fAddressSizes.clear();
		fIsBigEndian.clear();
		super.shutdown(requestMonitor);
	}

	@Override
	protected void readMemoryBlock(final IDMContext dmc, IAddress address, long offset, int word_size, int word_count,
			final DataRequestMonitor<MemoryByte[]> drm) {
		super.readMemoryBlock(dmc, address, offset, word_size, word_count,
				new DataRequestMonitor<MemoryByte[]>(ImmediateExecutor.getInstance(), drm) {
					@Override
					protected void handleSuccess() {
						IMemoryDMContext memDmc = DMContexts.getAncestorOfType(dmc, IMemoryDMContext.class);
						if (memDmc != null) {
							boolean bigEndian = isBigEndian(memDmc);
							for (MemoryByte b : getData()) {
								b.setBigEndian(bigEndian);
								b.setEndianessKnown(true);
							}
						}
						drm.setData(getData());
						drm.done();
					}
				});
	}

	@Override
	public void initializeMemoryData(final IMemoryDMContext memContext, RequestMonitor rm) {

		ImmediateExecutor.getInstance().execute(new Sequence(getExecutor(), rm) {

			private String originalLanguage = MIGDBShowLanguageInfo.AUTO;
			private boolean abortLanguageSteps = false;

			// Need a global here as getSteps() can be called more than once.
			private Step[] steps = null;

			private void determineSteps() {
				ArrayList<Step> stepsList = new ArrayList<>();

				if (fAddressSizes.get(memContext) == null) {
					stepsList.add(new Step() {
						// store original language
						@Override
						public void execute(final RequestMonitor requestMonitor) {
							fCommandControl.queueCommand(
									fCommandControl.getCommandFactory().createMIGDBShowLanguage(memContext),
									new ImmediateDataRequestMonitor<MIGDBShowLanguageInfo>(requestMonitor) {
										@Override
										protected void handleCompleted() {
											if (isSuccess()) {
												originalLanguage = getData().getLanguage();
											} else {
												abortLanguageSteps = true;
											}
											requestMonitor.done();
										}
									});
						}
					});
					stepsList.add(new Step() {
						// switch to c language
						@Override
						public void execute(final RequestMonitor requestMonitor) {
							if (abortLanguageSteps) {
								requestMonitor.done();
								return;
							}

							fCommandControl.queueCommand(
									fCommandControl.getCommandFactory().createMIGDBSetLanguage(memContext,
											MIGDBShowLanguageInfo.C),
									new ImmediateDataRequestMonitor<MIInfo>(requestMonitor) {
										@Override
										protected void handleCompleted() {
											if (!isSuccess()) {
												abortLanguageSteps = true;
											}
											// Accept failure
											requestMonitor.done();
										}
									});
						}
					});

					stepsList.add(new Step() {
						// Run this step even if the language commands where aborted, but accept failures.
						// Resolve Addressable and Address size
						@Override
						public void execute(final RequestMonitor requestMonitor) {
							//Read Minimum addressable memory size and actual address size
							readAddressableSize(memContext, new ImmediateDataRequestMonitor<Integer>(requestMonitor) {
								@Override
								protected void handleCompleted() {
									if (isSuccess()) {
										final Integer minAddressableInOctets = getData();
										//Preserve the addressable size per context
										fAddressableSizes.put(memContext, minAddressableInOctets);
									}

									readAddressSize(memContext,
											new ImmediateDataRequestMonitor<Integer>(requestMonitor) {
												@Override
												protected void handleCompleted() {
													if (isSuccess()) {
														//Preserve the address size per context
														fAddressSizes.put(memContext, getData());
													}

													// Accept failures
													requestMonitor.done();
												}
											});
								}
							});
						}
					});

					stepsList.add(new Step() {
						// restore original language
						@Override
						public void execute(final RequestMonitor requestMonitor) {
							if (abortLanguageSteps) {
								requestMonitor.done();
								return;
							}

							fCommandControl
									.queueCommand(
											fCommandControl.getCommandFactory().createMIGDBSetLanguage(memContext,
													originalLanguage),
											new ImmediateDataRequestMonitor<MIInfo>(requestMonitor) {
												@Override
												protected void handleCompleted() {
													if (!isSuccess()) {
														// If we are unable to set the original language back things could be bad.
														// Let's try setting it to "auto" as a fall back. Log the situation as info.
														GdbPlugin.log(getStatus());

														fCommandControl.queueCommand(
																fCommandControl.getCommandFactory()
																		.createMIGDBSetLanguage(memContext,
																				MIGDBShowLanguageInfo.AUTO),
																new ImmediateDataRequestMonitor<MIInfo>(
																		requestMonitor) {
																	@Override
																	protected void handleCompleted() {
																		if (!isSuccess()) {
																			// This error could be bad because we've changed the language to C
																			// but are unable to switch it back. Log the error.
																			// If the language happens to be C anyway, everything will
																			// continue to work, which is why we don't abort the sequence
																			// (which would cause the entire session to fail).
																			GdbPlugin.log(getStatus());
																		}
																		// Accept failure
																		requestMonitor.done();
																	}
																});
													} else {
														requestMonitor.done();
													}
												}
											});
						}
					});

				}

				if (fIsBigEndian.get(memContext) == null) {
					stepsList.add(new Step() {
						// read endianness
						@Override
						public void execute(final RequestMonitor requestMonitor) {
							readEndianness(memContext, new ImmediateDataRequestMonitor<Boolean>(requestMonitor) {
								@Override
								protected void handleCompleted() {
									if (isSuccess()) {
										fIsBigEndian.put(memContext, getData());
									}
									// Accept failure
									requestMonitor.done();
								}
							});
						}
					});
				}

				steps = stepsList.toArray(new Step[stepsList.size()]);
			}

			@Override
			public Step[] getSteps() {
				if (steps == null) {
					determineSteps();
				}

				return steps;
			}
		});
	}

	@DsfServiceEventHandler
	public void eventDispatched(IExitedDMEvent event) {
		if (event.getDMContext() instanceof IContainerDMContext) {
			IMemoryDMContext context = DMContexts.getAncestorOfType(event.getDMContext(), IMemoryDMContext.class);
			if (context != null) {
				fAddressSizes.remove(context);
				fAddressableSizes.remove(context);
			}
		}
	}

	@Override
	public int getAddressSize(IMemoryDMContext context) {
		Integer addressSize = fAddressSizes.get(context);
		assert addressSize != null;
		if (addressSize == null) {
			GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					"Address size was never initialized for " + context)); //$NON-NLS-1$
			return 8;
		}
		return addressSize.intValue();
	}

	/**
	 * @since 4.4
	 */
	@Override
	public int getAddressableSize(IMemoryDMContext context) {
		Integer addressableSize = fAddressableSizes.get(context);
		assert addressableSize != null;
		if (addressableSize == null) {
			GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					"Addressable size was never initialized for " + context)); //$NON-NLS-1$
			return super.getAddressableSize(context);
		}
		return addressableSize.intValue();
	}

	@Override
	public boolean isBigEndian(IMemoryDMContext context) {
		Boolean isBigEndian = fIsBigEndian.get(context);
		assert isBigEndian != null;
		if (isBigEndian == null) {
			GdbPlugin.log(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Endianness was never initialized for " + context)); //$NON-NLS-1$
			return false;
		}
		return isBigEndian.booleanValue();
	}

	/**
	 * Address size is determined by space, in octets, used to store an address value (e.g. a pointer) on a target system.
	 *
	 * <p>NOTE: Implementation requires addressable memory size to be known</p>
	 * @param memContext
	 * @param drm
	 *
	 * @see IGDBMemory#getAddressSize(org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext)
	 * @see GDBMemory#readAddressableSize(org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext, DataRequestMonitor)
	 */
	protected void readAddressSize(final IMemoryDMContext memContext, final DataRequestMonitor<Integer> drm) {
		IExpressions exprService = getServicesTracker().getService(IExpressions.class);
		IExpressionDMContext exprContext = exprService.createExpression(memContext, "sizeof (void*)"); //$NON-NLS-1$
		CommandFactory commandFactory = fCommandControl.getCommandFactory();
		fCommandControl.queueCommand(commandFactory.createMIDataEvaluateExpression(exprContext),
				new DataRequestMonitor<MIDataEvaluateExpressionInfo>(ImmediateExecutor.getInstance(), drm) {
					@Override
					protected void handleSuccess() {
						try {
							// 'sizeof' returns number of bytes (aka 'chars').
							// Multiply with byte size in octets to get storage required to hold a pointer.
							Integer ptrBytes = Integer.decode(getData().getValue());
							drm.setData(ptrBytes * getAddressableSize(memContext));
						} catch (NumberFormatException e) {
							drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
									String.format("Invalid address size: %s", getData().getValue()))); //$NON-NLS-1$
						}
						drm.done();
					}
				});
	}

	/**
	 * The minimum addressable size is determined by the space used to store a "char" on a target system
	 * This is then resolved by retrieving a hex representation of -1 casted to the size of a "char"
	 * e.g. from GDB command line
	 *    > p/x (char)-1
	 *    > $7 = 0xffff
	 *
	 * Since two hex characters are representing one octet, for the above example this method should return 2
	 * @since 4.4
	 *
	 */
	protected void readAddressableSize(IMemoryDMContext memContext, final DataRequestMonitor<Integer> drm) {
		//We use a CLI command here instead of the expression services, since the target may not be available
		//e.g. when using a remote launch.
		// Using MI directly is a possibility although there is no way to specify the required output format to hex.
		CommandFactory commandFactory = fCommandControl.getCommandFactory();
		fCommandControl.queueCommand(commandFactory.createCLIAddressableSize(memContext),
				new DataRequestMonitor<CLIAddressableSizeInfo>(ImmediateExecutor.getInstance(), drm) {
					@Override
					protected void handleSuccess() {
						drm.setData(Integer.valueOf(getData().getAddressableSize()));
						drm.done();
					}
				});
	}

	protected void readEndianness(IMemoryDMContext memContext, final DataRequestMonitor<Boolean> drm) {
		CommandFactory commandFactory = fCommandControl.getCommandFactory();
		fCommandControl.queueCommand(commandFactory.createCLIShowEndian(memContext),
				new DataRequestMonitor<CLIShowEndianInfo>(ImmediateExecutor.getInstance(), drm) {
					@Override
					protected void handleSuccess() {
						drm.setData(Boolean.valueOf(getData().isBigEndian()));
						drm.done();
					}
				});
	}
}
