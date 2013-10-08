/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson) - First Implementation and API (Bug 235747)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.internal.core.model.IRegisterGroupDescriptor;
import org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActions;
import org.eclipse.cdt.debug.internal.ui.actions.RegisterGroupDialog;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters2;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

@SuppressWarnings("restriction")
public class DsfRegisterGroupActions implements IRegisterGroupActions {
	private static final String BLANK_STRING = ""; //$NON-NLS-1$
	private static final String ROOT_GROUP_NAME = "General Registers"; //$NON-NLS-1$

	private class RegisterGroupDialogRunnable implements Runnable {
		private String fGroupName = BLANK_STRING;
		private IRegisterDescriptor[] fSelectedRegisters = null;
		private final IRegisterDescriptor[] fallRegisters;
		private final Shell fShell;
		private final DataRequestMonitor<IRegisterGroupDescriptor> fMonitor;

		private RegisterGroupDialogRunnable(Shell shell, String groupName, IRegisterDescriptor[] allRegisters,
				IRegisterDescriptor[] selectedRegisters, DataRequestMonitor<IRegisterGroupDescriptor> rm) {
			fallRegisters = allRegisters;
			fSelectedRegisters = selectedRegisters;
			fShell = shell;
			fGroupName = groupName;
			fMonitor = rm;
		}

		@Override
		public void run() {
			RegisterGroupDialog dialog = new RegisterGroupDialog(fShell, fGroupName, fallRegisters, fSelectedRegisters);
			if (dialog.open() == Window.OK) {
				String groupName = dialog.getName();
				IRegisterDescriptor[] iSelectedRegisters = dialog.getDescriptors();
				IRegisterGroupDescriptor groupDescriptor = createGroupDescriptor(groupName, iSelectedRegisters);

				fMonitor.setData(groupDescriptor);
			} else {
				fMonitor.cancel();
				return;
			}

			fMonitor.done();
		}
	}

	private class RegisterDescriptor implements IRegisterDescriptor {
		private final IRegisterDMContext fRegContext;
		private String fOriginalGroupName = BLANK_STRING;
		private String fName = BLANK_STRING;

		private RegisterDescriptor(String groupName, IRegisterDMContext regContext, String name) {
			fRegContext = regContext;
			fName = name;
			// initial group Name
			fOriginalGroupName = groupName;
		}

		@Override
		public String getName() {
			return fName;
		}

		@Override
		public String getGroupName() {
			return fOriginalGroupName;
		}
	}

	private class SelectionDMContext {
		private final IDMContext fcontext;
		private final DsfSession fsession;

		private SelectionDMContext(IStructuredSelection selection) throws DebugException {
			if (!(selection.getFirstElement() instanceof IDMVMContext)) {
				abort("Unrecognized element from the provided register selection"); //$NON-NLS-1$
			}

			// Resolve the context
			IDMVMContext context = (IDMVMContext) selection.getFirstElement();
			fcontext = context.getDMContext();

			// Resolve the session
			String sessionId = fcontext.getSessionId();
			fsession = DsfSession.getSession(sessionId);

			if (fsession == null || !(fsession.isActive())) {
				abort("Sesssion inactive"); //$NON-NLS-1$
			}
		}

		/**
		 * This method has to be called under the executor's thread
		 */
		public IRegisters2 resolveService() throws DebugException {
			// Resolve the registers service
			DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), fsession.getId());
			IRegisters service = tracker.getService(IRegisters.class, null);

			tracker.dispose();

			if (!(service instanceof IRegisters2)) {
				abort("Unable to resolve IRegisters2 service"); //$NON-NLS-1$
			}

			return (IRegisters2) service;
		}

		private void abort(String message) throws DebugException {
			// Interrupt on error
			IStatus status = new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IStatus.ERROR, message, null);
			throw new DebugException(status);
		}

	}

	private interface DialogRegisterProvider {
		public IRegisterDescriptor[] getAllRegisters();

		public IRegisterDescriptor[] getcheckedRegisters();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActions#
	 * addRegisterGroup(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.action.IAction,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void addRegisterGroup(final IWorkbenchPart part, final IStructuredSelection selection) throws DebugException {
		final SelectionDMContext selectionContext = new SelectionDMContext(selection);
		RequestMonitor asyncRequest = new RequestMonitor(selectionContext.fsession.getExecutor(), null) {
			@Override
			protected void handleSuccess() {
				final IRegisters2 registersService;
				try {
					registersService = selectionContext.resolveService();
				} catch (CoreException e) {
					failed(e);
					return;
				}

				// We need to resolve the root group in order to obtain the full list of registers provided by GDB
				registersService.findRegisterGroup(selectionContext.fcontext, ROOT_GROUP_NAME,
						new DataRequestMonitor<IRegisterGroupDMContext>(selectionContext.fsession.getExecutor(), null) {
							@Override
							protected void handleSuccess() {
								IRegisterGroupDMContext rootGroupDmc = getData();
								if (rootGroupDmc == null) {
									failed(new Exception("Add Register Group: Unable to resolve root Group")); //$NON-NLS-1$
									return;
								}

								// continue to process
								processAddRegisterGroup(part.getSite().getShell(), selectionContext, rootGroupDmc,
										resolveSelectedRegisters(selection), registersService);
							}
						});
			}
		};

		// Trigger async execution on the executor's thread
		asyncRequest.done();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActions#canAddRegisterGroup(org.eclipse.ui.IWorkbenchPart
	 * , org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public boolean canAddRegisterGroup(IWorkbenchPart part, IStructuredSelection selection) {
		try {
			final SelectionDMContext selectionContext = new SelectionDMContext(selection);
			Query<Boolean> query = new Query<Boolean>() {
				@Override
				protected void execute(final DataRequestMonitor<Boolean> rm) {
					IRegisters2 registersService;
					try {
						registersService = selectionContext.resolveService();
					} catch (DebugException e) {
						rm.setData(false);
						rm.done();
						return;
					}

					if (registersService != null) {
						registersService.canAddRegisterGroup(selectionContext.fcontext,
								new DataRequestMonitor<Boolean>(selectionContext.fsession.getExecutor(), null) {
									@Override
									protected void handleSuccess() {
										rm.setData(getData());
										rm.done();
									};
								});
					} else {
						rm.setData(false);
						rm.done();
					}
				}
			};
			selectionContext.fsession.getExecutor().execute(query);
			return query.get();
		} catch (RejectedExecutionException e) {
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (DebugException e1) {
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActions#
	 * editRegisterGroup(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void editRegisterGroup(final IWorkbenchPart part, IStructuredSelection selection) throws DebugException {
		final SelectionDMContext selectionContext = new SelectionDMContext(selection);
		RequestMonitor asyncRequest = new RequestMonitor(selectionContext.fsession.getExecutor(), null) {
			@Override
			protected void handleSuccess() {
				// Create a services tracker
				final IRegisters2 registersService;
				try {
					registersService = selectionContext.resolveService();
				} catch (CoreException e) {
					failed(e);
					return;
				}

				// We need to resolve the root group in order to obtain the full list of registers provided by GDB
				registersService.findRegisterGroup(selectionContext.fcontext, ROOT_GROUP_NAME,
						new DataRequestMonitor<IRegisterGroupDMContext>(selectionContext.fsession.getExecutor(), null) {
							@Override
							protected void handleSuccess() {
								final IRegisterGroupDMContext rootGroupDmc = getData();
								if (rootGroupDmc == null) {
									failed(new Exception("Edit Register Group: Unable to resolve root Group")); //$NON-NLS-1$
									return;
								}

								// continue to process
								processEditRegisterGroup(part.getSite().getShell(), selectionContext, rootGroupDmc,
										registersService);
							}
						});
			}
		};

		// Trigger async execution on the executor's thread
		asyncRequest.done();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActions#
	 * canEditRegisterGroup(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public boolean canEditRegisterGroup(IWorkbenchPart part, IStructuredSelection selection) {
		try {
			final SelectionDMContext selectionContext = new SelectionDMContext(selection);
			final IDMContext context = selectionContext.fcontext;

			// The group to be edited needs to be selected
			if (!(context instanceof IRegisterGroupDMContext)) {
				return false;
			}

			Query<Boolean> query = new Query<Boolean>() {
				@Override
				protected void execute(final DataRequestMonitor<Boolean> rm) {
					IRegisters2 registersService;
					try {
						registersService = selectionContext.resolveService();
					} catch (DebugException e) {
						rm.setData(false);
						rm.done();
						return;
					}

					if (registersService != null) {
						registersService.canEditRegisterGroup((IRegisterGroupDMContext) context,
								new DataRequestMonitor<Boolean>(selectionContext.fsession.getExecutor(), null) {
									@Override
									protected void handleSuccess() {
										rm.setData(getData());
										rm.done();
									};
								});
					} else {
						rm.setData(false);
						rm.done();
					}
				}
			};
			selectionContext.fsession.getExecutor().execute(query);
			return query.get();

		} catch (RejectedExecutionException e) {
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (DebugException e1) {
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActions#
	 * removeRegisterGroup(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void removeRegisterGroup(IWorkbenchPart part, IStructuredSelection selection) throws DebugException {
		final SelectionDMContext selectionContext = new SelectionDMContext(selection);
		final IRegisterGroupDMContext[] groups = resolveSelectedGroups(selection);
		RequestMonitor asyncRequest = new RequestMonitor(selectionContext.fsession.getExecutor(), null) {
			@Override
			protected void handleSuccess() {
				final IRegisters2 registersService;
				try {
					registersService = selectionContext.resolveService();
				} catch (CoreException e) {
					failed(e);
					return;
				}

				registersService.removeRegisterGroups(groups, new RequestMonitor(registersService.getExecutor(), null) {
				});
			}
		};

		// Trigger async execution on the executor's thread
		asyncRequest.done();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActions#
	 * canRemoveRegisterGroup(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public boolean canRemoveRegisterGroup(IWorkbenchPart part, IStructuredSelection selection) {
		// possible to remove if it's possible to edit a group
		return canEditRegisterGroup(part, selection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActions#
	 * restoreDefaultGroups(org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void restoreDefaultGroups(IWorkbenchPart part, IStructuredSelection selection) throws DebugException {
		final SelectionDMContext selectionContext = new SelectionDMContext(selection);
		final DsfExecutor executor = selectionContext.fsession.getExecutor();
		RequestMonitor asyncRequest = new RequestMonitor(executor, null) {
			@Override
			protected void handleSuccess() {
				final IRegisters2 registersService;
				try {
					registersService = selectionContext.resolveService();
				} catch (CoreException e) {
					failed(e);
					return;
				}

				// no success handler needed
				registersService.restoreDefaultGroups(new RequestMonitor(executor, null));
			}
		};

		// Trigger async execution on the executor's thread
		asyncRequest.done();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActions#
	 * canRestoreDefaultGroups(org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public boolean canRestoreDefaultGroups(IWorkbenchPart part, IStructuredSelection selection) {
		return canAddRegisterGroup(null, selection);
	}

	private void processAddRegisterGroup(final Shell shell, final SelectionDMContext selectionContext,
			final IRegisterGroupDMContext rootGroupDmc, final IRegisterDMContext[] selectedRegisters,
			final IRegisters2 regServiceManager) {

		final DsfSession session = selectionContext.fsession;
		final DsfExecutor executor = session.getExecutor();

		// Get all existing registers from the root group
		regServiceManager.getRegisters(rootGroupDmc, new DataRequestMonitor<IRegisterDMContext[]>(
				session.getExecutor(), null) {
			@Override
			@ConfinedToDsfExecutor("fExecutor")
			protected void handleSuccess() {
				// Get Register Contexts
				final IRegisterDMContext[] rootRegisters = getData();
				regServiceManager.getRegisterNames(rootRegisters, new DataRequestMonitor<IRegisterDMData[]>(executor,
						null) {
					@Override
					protected void handleSuccess() {
						final IRegisterDMData[] rootRegistersData = getData();
						// Get register data for all selected registers i.e. selected for the new group
						regServiceManager.getRegisterNames(selectedRegisters,
								new DataRequestMonitor<IRegisterDMData[]>(executor, null) {
									@Override
									protected void handleSuccess() {
										final IRegisterDMData[] selectedRegistersData = getData();
										//Need the root group name to build register descriptors 
										regServiceManager.getRegisterGroupData(rootGroupDmc,
												new DataRequestMonitor<IRegisterGroupDMData>(executor, null) {
													@Override
													@ConfinedToDsfExecutor("fExecutor")
													protected void handleSuccess() {
														final IRegisterGroupDMData rootGroupData = getData();
														// request for the next unused group name to propose it to the user
														regServiceManager
																.proposeGroupName(new DataRequestMonitor<String>(
																		executor, null) {
																	@Override
																	@ConfinedToDsfExecutor("fExecutor")
																	protected void handleSuccess() {
																		final String proposedGroupName = getData();

																		final String rootGroupName = (rootGroupData == null) ? BLANK_STRING
																				: rootGroupData.getName();
																		// Create the Register descriptors
																		final DialogRegisterProvider descriptors = buildDescriptors(
																				rootGroupName, rootRegisters,
																				rootRegistersData,
																				selectedRegistersData);
																		// Create Dialog Resolve selection to DSF
																		// Registers
																		getDialogSelection(
																				shell,
																				proposedGroupName,
																				descriptors.getAllRegisters(),
																				descriptors.getcheckedRegisters(),
																				new DataRequestMonitor<IRegisterGroupDescriptor>(
																						executor, null) {
																					@Override
																					protected void handleSuccess() {
																						try {
																							addRegisterGroup(
																									regServiceManager,
																									session, getData());
																						} catch (CoreException e) {
																							failed(e);
																						}
																					};
																				});
																	}
																});

													}
												});
									}

								});
					};
				});
			}
		});
	}

	private void processEditRegisterGroup(final Shell shell, final SelectionDMContext selectionContext,
			final IRegisterGroupDMContext rootGroupDmc, final IRegisters2 regServiceManager) {

		final DsfSession session = selectionContext.fsession;
		final DsfExecutor executor = session.getExecutor();

		// Get a handle to the context of the group being edited
		final IRegisterGroupDMContext groupDmc = DMContexts.getAncestorOfType(selectionContext.fcontext,
				IRegisterGroupDMContext.class);

		// Getting the children of the selected group
		regServiceManager.getRegisters(selectionContext.fcontext,
				new DataRequestMonitor<IRegisterDMContext[]>(executor, null) {
					@Override
					@ConfinedToDsfExecutor("fExecutor")
					protected void handleSuccess() {
						// Get children Register Contexts
						final IRegisterDMContext[] childRegisters = getData();
						// Getting all available gdb registers from the root
						// This is needed to populate the dialog with all available registers to pick from
						regServiceManager.getRegisters(rootGroupDmc, new DataRequestMonitor<IRegisterDMContext[]>(
								executor, null) {
							@Override
							@ConfinedToDsfExecutor("fExecutor")
							protected void handleSuccess() {
								final IRegisterDMContext[] rootRegisters = getData();
								// We need to resolve the names for all root registers
								regServiceManager.getRegisterNames(rootRegisters,
										new DataRequestMonitor<IRegisterDMData[]>(executor, null) {
											@Override
											protected void handleSuccess() {
												final IRegisterDMData[] rootRegisterData = getData();

												// Get Register names for the selected registers
												regServiceManager.getRegisterNames(childRegisters,
														new DataRequestMonitor<IRegisterDMData[]>(executor, null) {
															@Override
															protected void handleSuccess() {
																final IRegisterDMData[] childRegisterData = getData();

																//Need to get the group name. Used on the register descriptors
																regServiceManager.getRegisterGroupData(groupDmc,
																		new DataRequestMonitor<IRegisterGroupDMData>(
																				executor, null) {
																			@Override
																			@ConfinedToDsfExecutor("fExecutor")
																			protected void handleSuccess() {
																				IRegisterGroupDMData groupData = getData();
																				final String groupName = (groupData == null) ? BLANK_STRING
																						: groupData.getName();
																				// Create the Register descriptors to access
																				// all children registers
																				final DialogRegisterProvider descriptors = buildDescriptors(
																						groupName, rootRegisters,
																						rootRegisterData, childRegisterData);
																				// Create Dialog to Resolve new user
																				// selection of group name and registers
																				getDialogSelection(
																						shell,
																						groupName,
																						descriptors.getAllRegisters(),
																						descriptors
																								.getcheckedRegisters(),
																						new DataRequestMonitor<IRegisterGroupDescriptor>(
																								executor, null) {
																							@Override
																							protected void handleSuccess() {
																								try {
																									editRegisterGroup(
																											groupDmc,
																											regServiceManager,
																											session,
																											getData());
																								} catch (CoreException e) {
																									failed(e);
																								}
																							};
																						});
																			}
																		});

															};

														});
											};
										});
							}
						});

					}
				});
	}

	private IRegisterGroupDMContext[] resolveSelectedGroups(IStructuredSelection selection) {
		IRegisterGroupDMContext[] selectedGroups = null;
		List<IRegisterGroupDMContext> groupList = new ArrayList<IRegisterGroupDMContext>();
		if (selection != null && !selection.isEmpty()) {
			for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
				Object element = iterator.next();
				if (element instanceof IDMVMContext) {
					IDMContext dmContext = ((IDMVMContext) element).getDMContext();
					// Make sure this selection is a group
					if (dmContext instanceof IRegisterGroupDMContext) {
						final IRegisterGroupDMContext groupDmc = (IRegisterGroupDMContext) dmContext;
						groupList.add(groupDmc);
					}
				}
			}

		}

		selectedGroups = groupList.toArray(new IRegisterGroupDMContext[groupList.size()]);
		return selectedGroups;
	}

	private IRegisterDMContext[] resolveSelectedRegisters(final IStructuredSelection selection) {
		List<IRegisterDMContext> selectedRegistersList = new ArrayList<IRegisterDMContext>();
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			Object element = iterator.next();
			IDMVMContext regContext = null;
			if (element instanceof IDMVMContext) {
				regContext = (IDMVMContext) element;
				IRegisterDMContext registerDmc = DMContexts.getAncestorOfType(regContext.getDMContext(),
						IRegisterDMContext.class);
				if (registerDmc != null) {
					selectedRegistersList.add(registerDmc);
				}
			}
		}

		final IRegisterDMContext[] selectedRegisters = selectedRegistersList
				.toArray(new IRegisterDMContext[selectedRegistersList.size()]);
		return selectedRegisters;
	}

	private IRegisterDMContext[] getRegisterContexts(IRegisterDescriptor[] registerDescriptors) throws CoreException {
		IRegisterDMContext[] regContexts = new IRegisterDMContext[registerDescriptors.length];
		for (int i = 0; i < registerDescriptors.length; i++) {
			if (registerDescriptors[i] instanceof RegisterDescriptor) {
				regContexts[i] = ((RegisterDescriptor) registerDescriptors[i]).fRegContext;
			} else {
				// Interrupt on error
				IStatus status = new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IStatus.ERROR,
						"Unexpected IRegisterDescription instance type", null); //$NON-NLS-1$
				throw new CoreException(status);
			}
		}

		return regContexts;
	}

	private void getDialogSelection(final Shell shell, final String originalGroupName,
			final IRegisterDescriptor[] allRegisters, final IRegisterDescriptor[] checkedRegisters,
			DataRequestMonitor<IRegisterGroupDescriptor> rm) {
		RegisterGroupDialogRunnable dialog = new RegisterGroupDialogRunnable(shell, originalGroupName, allRegisters,
				checkedRegisters, rm);
		shell.getDisplay().asyncExec(dialog);
	}

	private IRegisterGroupDescriptor createGroupDescriptor(final String groupName,
			final IRegisterDescriptor[] iSelectedRegisters) {
		IRegisterGroupDescriptor groupDescriptor = new IRegisterGroupDescriptor() {

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public String getName() {
				return groupName;
			}

			@Override
			public IRegisterDescriptor[] getChildren() throws CoreException {
				return iSelectedRegisters;
			}
		};

		return groupDescriptor;
	}

	/**
	 * Build descriptor adapters to dialog interface both all registers as well as registers to be pre-selected on the
	 * dialog
	 * 
	 * @param checkedRegistersData
	 */
	private DialogRegisterProvider buildDescriptors(String groupName, IRegisterDMContext[] registers,
			IRegisterDMData[] registerData, IRegisterDMData[] checkedRegistersData) {
		assert (registers.length == registerData.length);

		List<RegisterDescriptor> checkedDescriptorsList = new ArrayList<RegisterDescriptor>();

		final RegisterDescriptor[] regDescriptors = new RegisterDescriptor[registers.length];

		final Map<String, RegisterDescriptor> mapNameToRegDescriptor = new HashMap<String, RegisterDescriptor>();

		for (int i = 0; i < registers.length; i++) {
			regDescriptors[i] = new RegisterDescriptor(groupName, registers[i], registerData[i].getName());
			mapNameToRegDescriptor.put(regDescriptors[i].getName(), regDescriptors[i]);
		}

		for (int i = 0; i < checkedRegistersData.length; i++) {
			// Resolve the descriptor by name
			RegisterDescriptor descriptor = mapNameToRegDescriptor.get(checkedRegistersData[i].getName());

			// All checked registers are expected to be part of the complete list
			assert (descriptor != null);

			// prevent duplicates or null values, duplicates are possible since the selected registers
			// may come from different groups
			if (descriptor != null && !checkedDescriptorsList.contains(descriptor)) {
				checkedDescriptorsList.add(descriptor);
			}
		}

		final RegisterDescriptor[] checkedRegDescriptors = checkedDescriptorsList
				.toArray(new RegisterDescriptor[checkedDescriptorsList.size()]);

		DialogRegisterProvider provider = new DialogRegisterProvider() {
			@Override
			public IRegisterDescriptor[] getcheckedRegisters() {
				return checkedRegDescriptors;
			}

			@Override
			public IRegisterDescriptor[] getAllRegisters() {
				return regDescriptors;
			}
		};

		return provider;
	}

	private void addRegisterGroup(final IRegisters2 regServiceManager, final DsfSession session,
			IRegisterGroupDescriptor groupDescriptor) throws CoreException {
		IRegisterDescriptor[] selectedRegisters = groupDescriptor.getChildren();
		if (selectedRegisters != null) {
			String groupName = groupDescriptor.getName();
			// Register the addition of the group and notify the change
			regServiceManager.addRegisterGroup(groupName, getRegisterContexts(selectedRegisters), new RequestMonitor(
					session.getExecutor(), null));
		}
	}

	private void editRegisterGroup(final IRegisterGroupDMContext group, final IRegisters2 regServiceManager,
			final DsfSession session, IRegisterGroupDescriptor groupDescriptor) throws CoreException {
		IRegisterDescriptor[] selectedRegisters = groupDescriptor.getChildren();
		if (selectedRegisters != null) {
			String groupName = groupDescriptor.getName();
			// Register the addition of the group and notify the change
			regServiceManager.editRegisterGroup(group, groupName, getRegisterContexts(selectedRegisters),
					new RequestMonitor(session.getExecutor(), null));
		}
	}

	private void failed(Throwable e) {
		MultiStatus ms = new MultiStatus(CDIDebugModel.getPluginIdentifier(), IDsfStatusConstants.REQUEST_FAILED,
				"Register Group Action failed", null); //$NON-NLS-1$
		ms.add(new Status(IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), IDsfStatusConstants.REQUEST_FAILED, e
				.getMessage(), e));
		DsfUIPlugin.log(ms);
	}

}
