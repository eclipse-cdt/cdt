/*******************************************************************************
 * Copyright (c) 2014 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.internal.core.model.IRegisterGroupDescriptor;
import org.eclipse.cdt.debug.internal.ui.actions.RegisterGroupDialog;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.internal.ui.Messages;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters2;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

public abstract class AbstractDsfRegisterGroupActions extends AbstractHandler {
	private static final String BLANK_STRING = ""; //$NON-NLS-1$
	private static final String REG_GROUP_ACTION_FAILED = "Register Group Action failed\n"; //$NON-NLS-1$

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

	protected void addRegisterGroup(final IWorkbenchPart part, final IStructuredSelection selection) {
		try {
			final SelectionDMContext selectionContext = new SelectionDMContext(selection);
			selectionContext.fsession.getExecutor().execute(new DsfRunnable() {

				@Override
				public void run() {
					final IRegisters2 registersService;
					try {
						registersService = selectionContext.resolveService();
					} catch (CoreException e) {
						failed(e);
						return;
					}

					// continue to process
					processAddRegisterGroup(part.getSite().getShell(), selectionContext,
							resolveSelectedRegisters(selection), registersService);
				}
			});

		} catch (DebugException e) {
		}
	}

	protected boolean canAddRegisterGroup(IWorkbenchPart part, IStructuredSelection selection) {
		try {
			final SelectionDMContext selectionContext = new SelectionDMContext(selection);
			Query<Boolean> query = new Query<Boolean>() {
				@Override
				protected void execute(DataRequestMonitor<Boolean> rm) {
					IRegisters2 registersService;
					try {
						registersService = selectionContext.resolveService();
					} catch (DebugException e) {
						rm.setData(false);
						rm.done();
						return;
					}

					if (registersService != null) {
						registersService.canAddRegisterGroup(selectionContext.fcontext, rm);
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

	protected void editRegisterGroup(final IWorkbenchPart part, IStructuredSelection selection) {
		try {
			final SelectionDMContext selectionContext = new SelectionDMContext(selection);

			selectionContext.fsession.getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					// Create a services tracker
					final IRegisters2 registersService;
					try {
						registersService = selectionContext.resolveService();
					} catch (CoreException e) {
						failed(e);
						return;
					}

					processEditRegisterGroup(part.getSite().getShell(), selectionContext, registersService);
				}

			});
		} catch (DebugException e) {
		}
	}

	protected boolean canEditRegisterGroup(IWorkbenchPart part, IStructuredSelection selection) {
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
						registersService.canEditRegisterGroup((IRegisterGroupDMContext) context, rm);
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActionsTarget#removeRegisterGroups(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected void removeRegisterGroups(IWorkbenchPart part, IStructuredSelection selection) {
		try {
			final SelectionDMContext selectionContext = new SelectionDMContext(selection);

			final IRegisterGroupDMContext[] groups = resolveSelectedGroups(selection);
			selectionContext.fsession.getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					IRegisters2 registersService;
					try {
						registersService = selectionContext.resolveService();
					} catch (CoreException e) {
						failed(e);
						return;
					}

					registersService.removeRegisterGroups(groups,
							new RequestMonitor(registersService.getExecutor(), null) {
							});
				}
			});
		} catch (DebugException e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActionsTarget#canRemoveRegisterGroups(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean canRemoveRegisterGroups(IWorkbenchPart part, IStructuredSelection selection) {
		final SelectionDMContext selectionContext;
		try {
			selectionContext = new SelectionDMContext(selection);
		} catch (DebugException e) {
			// No DM context present or group registers service found in the selection
			return false;
		}

		//resolve the selected groups
		final IRegisterGroupDMContext[] groups = resolveSelectedGroups(selection);
		if (groups == null || groups.length < 1) {
			return false;
		}

		//Prepare to Query the service and check if the selected groups can be removed
		Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				IRegisters2 regService;
				try {
					regService = selectionContext.resolveService();
				} catch (DebugException e) {
					// Unable to resolve the registers service
					rm.setData(false);
					rm.done();
					return;
				}

				regService.canRemoveRegisterGroups(groups, rm);
			}
		};

		//Execute the query
		selectionContext.fsession.getExecutor().execute(query);

		try {
			// return the answer from the service
			return query.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		}

		// No positive answer from the service
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActionsTarget#restoreDefaultGroups(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected void restoreDefaultGroups(IWorkbenchPart part, IStructuredSelection selection) {
		if (!restoreConfirmed()) {
			return;
		}

		try {
			final SelectionDMContext selectionContext = new SelectionDMContext(selection);

			selectionContext.fsession.getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					IRegisters2 registersService;
					try {
						registersService = selectionContext.resolveService();
					} catch (CoreException e) {
						failed(e);
						return;
					}

					// no success handler needed
					registersService.restoreDefaultGroups(null,
							new RequestMonitor(registersService.getExecutor(), null));
				}
			});
		} catch (DebugException e) {
			failed(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.IRegisterGroupActionsTarget#canRestoreDefaultGroups(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean canRestoreDefaultGroups(IWorkbenchPart part, IStructuredSelection selection) {
		final SelectionDMContext selectionContext;
		try {
			selectionContext = new SelectionDMContext(selection);
		} catch (DebugException e) {
			// No DM context present or group registers service found in the selection
			return false;
		}

		//Prepare to Query the service
		Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				IRegisters2 regService;
				try {
					regService = selectionContext.resolveService();
				} catch (DebugException e) {
					// Unable to resolve the registers service
					rm.setData(false);
					rm.done();
					return;
				}

				regService.canRestoreDefaultGroups(selectionContext.fcontext, rm);
			}
		};

		//Execute the query
		selectionContext.fsession.getExecutor().execute(query);

		try {
			// return the answer from the service
			return query.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		}

		// No positive answer from the service
		return false;
	}

	@ConfinedToDsfExecutor("selectionContext.fsession.getExecutor()")
	private void processAddRegisterGroup(final Shell shell, final SelectionDMContext selectionContext,
			final IRegisterDMContext[] selectedRegisters, final IRegisters2 regServiceManager) {

		final DsfSession session = selectionContext.fsession;
		final DsfExecutor executor = session.getExecutor();

		final IContainerDMContext contDmc = DMContexts.getAncestorOfType(selectionContext.fcontext,
				IContainerDMContext.class);
		// Using the container context to get all existing registers from the target instead of a limited set of registers for a selected group
		regServiceManager.getRegisters(contDmc, new DataRequestMonitor<IRegisterDMContext[]>(executor, null) {

			@Override
			protected void handleSuccess() {
				// Get Register Contexts
				final IRegisterDMContext[] rootRegisters = getData();

				if (rootRegisters.length < 1) {
					//The target is expected to have registers, an error has happened !
					assert false;
					noRegisterGroupFoundErr("Add Register Group", this); //$NON-NLS-1$
					return;
				}

				//Find the root register group, containing all the registers associated to a target, from any of the root registers
				final IRegisterGroupDMContext rootGroupDmc = DMContexts.getAncestorOfType(rootRegisters[0],
						IRegisterGroupDMContext.class);

				// Get data for all available registers
				getRegistersData(rootRegisters, regServiceManager,
						new DataRequestMonitor<IRegisterDMData[]>(executor, null) {
							@Override
							protected void handleSuccess() {
								final IRegisterDMData[] rootRegistersData = getData();

								getRegistersData(selectedRegisters, regServiceManager,
										new DataRequestMonitor<IRegisterDMData[]>(executor, null) {
											@Override
											protected void handleSuccess() {
												// Get data for all selected registers i.e. selected for the new group
												final IRegisterDMData[] selectedRegistersData = getData();

												//Need the root group name to build register descriptors
												regServiceManager.getRegisterGroupData(rootGroupDmc,
														new DataRequestMonitor<IRegisterGroupDMData>(executor, null) {
															@Override
															protected void handleSuccess() {
																final IRegisterGroupDMData rootGroupData = getData();
																// request for the next unused group name to propose it to the user
																proposeGroupName(rootRegisters, regServiceManager,
																		new DataRequestMonitor<String>(executor, null) {
																			@Override
																			protected void handleSuccess() {
																				String proposedGroupName = getData();

																				String rootGroupName = (rootGroupData == null)
																						? BLANK_STRING
																						: rootGroupData.getName();
																				// Create the Register descriptors
																				DialogRegisterProvider descriptors = buildDescriptors(
																						rootGroupName, rootRegisters,
																						rootRegistersData,
																						selectedRegistersData);
																				// Create Dialog Resolve selection to DSF
																				// Registers
																				getDialogSelection(shell,
																						proposedGroupName,
																						descriptors.getAllRegisters(),
																						descriptors
																								.getcheckedRegisters(),
																						new DataRequestMonitor<IRegisterGroupDescriptor>(
																								executor, null) {
																							@Override
																							protected void handleSuccess() {
																								try {
																									addRegisterGroup(
																											regServiceManager,
																											getData(),
																											contDmc);
																								} catch (CoreException e) {
																									failed(e);
																								}
																							}
																						});
																			}
																		});

															}
														});
											}
										});
							}
						});
			}

		});
	}

	private void noRegisterGroupFoundErr(String msgOrigin, RequestMonitor rm) {
		String message = msgOrigin + ": Unable to resolve root Group"; //$NON-NLS-1$
		IStatus status = new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED,
				REG_GROUP_ACTION_FAILED + message, new Exception(message));
		DsfUIPlugin.log(status);
		rm.setStatus(status);
		rm.done();
	}

	private void proposeGroupName(IRegisterDMContext[] registers, final IRegisters2 regServiceManager,
			final DataRequestMonitor<String> rm) {
		assert (registers != null && registers.length > 0);

		final DsfExecutor executor = regServiceManager.getExecutor();
		if (registers != null && registers.length > 0) {
			//First get all register group contexts, any register context can be used to resolve the container context
			regServiceManager.getRegisterGroups(registers[0],
					new DataRequestMonitor<IRegisterGroupDMContext[]>(executor, null) {
						@Override
						protected void handleSuccess() {
							IRegisterGroupDMContext[] groupsCtx = getData();
							assert (groupsCtx != null);

							final IRegisterGroupDMData[] groupsData = new IRegisterGroupDMData[groupsCtx.length];

							final CountingRequestMonitor crm = new CountingRequestMonitor(executor, rm) {
								@Override
								protected void handleCompleted() {
									//GroupsData is resolved now
									//Select an unused name
									String unusedGroupName = Messages.ProposeGroupNameRoot
											+ (resolveGroupNameWaterMark(groupsData) + 1);
									rm.setData(unusedGroupName);
									rm.done();
								}
							};

							//Resolve all register group data
							for (int i = 0; i < groupsCtx.length; i++) {
								final int index = i;
								regServiceManager.getRegisterGroupData(groupsCtx[index],
										new DataRequestMonitor<IRegisterGroupDMData>(executor, crm) {
											@Override
											protected void handleSuccess() {
												groupsData[index] = getData();
												crm.done();
											}
										});
							}

							crm.setDoneCount(groupsCtx.length);
						}
					});
		} else {
			//Should not happen
			rm.setData(Messages.DefaultRegistersGroupName);
			rm.done();
		}
	}

	// Adjust water mark suffix used to suggest register group names
	private Integer resolveGroupNameWaterMark(IRegisterGroupDMData[] groupsData) {
		// check only for this name pattern
		Pattern pattern = Pattern.compile("^group_(\\d*)$"); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(""); //$NON-NLS-1$
		int water_mark = 0;
		for (IRegisterGroupDMData groupData : groupsData) {
			// Normalize the name to lower case comparison
			String name = groupData.getName().trim().toLowerCase();
			// tracking proposed group names e.d. Group_1, Group_2, etc..,
			// otherwise no need to update the water mark
			if (matcher.reset(name).matches()) {
				// Obtain the numerical suffix
				String number = matcher.replaceAll("$1"); //$NON-NLS-1$
				try {
					int nameSequence = Integer.valueOf(number).intValue();
					if (nameSequence > water_mark) {
						// The new value is bigger so lets move up the water mark
						water_mark = nameSequence;
					}
				} catch (NumberFormatException e) {
					// Quite unlikely and only causing a possibility to
					// propose a group name that already exists.
				}
			}
		}

		return Integer.valueOf(water_mark);
	}

	@ConfinedToDsfExecutor("selectionContext.fsession.getExecutor()")
	private void processEditRegisterGroup(final Shell shell, final SelectionDMContext selectionContext,
			final IRegisters2 regServiceManager) {

		final DsfSession session = selectionContext.fsession;
		final DsfExecutor executor = session.getExecutor();

		// Get a handle to the context of the group being edited
		final IRegisterGroupDMContext groupDmc = DMContexts.getAncestorOfType(selectionContext.fcontext,
				IRegisterGroupDMContext.class);

		// Getting the children of the selected group
		regServiceManager.getRegisters(selectionContext.fcontext,
				new DataRequestMonitor<IRegisterDMContext[]>(executor, null) {
					@Override
					protected void handleSuccess() {
						// Get children Register Contexts
						final IRegisterDMContext[] childRegisters = getData();

						final IContainerDMContext contDmc = DMContexts.getAncestorOfType(selectionContext.fcontext,
								IContainerDMContext.class);
						// Using the container context to get all existing registers from the target instead of a limited set of registers for a selected group
						// This is needed to populate the dialog with all available registers to pick from
						regServiceManager.getRegisters(contDmc,
								new DataRequestMonitor<IRegisterDMContext[]>(executor, null) {
									@Override
									protected void handleSuccess() {
										final IRegisterDMContext[] rootRegisters = getData();

										if (rootRegisters.length < 1) {
											//The target is expected to have a root register group and associated registers, an error has happened !
											assert false;
											noRegisterGroupFoundErr("Edit Register Group", this); //$NON-NLS-1$
											return;
										}

										// We need to resolve the names for all root registers
										getRegistersData(rootRegisters, regServiceManager,
												new DataRequestMonitor<IRegisterDMData[]>(executor, null) {
													@Override
													protected void handleSuccess() {
														final IRegisterDMData[] rootRegistersData = getData();

														getRegistersData(childRegisters, regServiceManager,
																new DataRequestMonitor<IRegisterDMData[]>(executor,
																		null) {
																	@Override
																	protected void handleSuccess() {
																		// Get register data for all selected registers i.e. selected for the new group
																		final IRegisterDMData[] childRegisterData = getData();

																		// Need to get the parent group name. Used on the register descriptors
																		final IRegisterGroupDMContext rootGroupDmc = DMContexts
																				.getAncestorOfType(rootRegisters[0],
																						IRegisterGroupDMContext.class);
																		regServiceManager.getRegisterGroupData(
																				rootGroupDmc,
																				new DataRequestMonitor<IRegisterGroupDMData>(
																						executor, null) {
																					@Override
																					protected void handleSuccess() {
																						IRegisterGroupDMData rootGroupData = getData();
																						final String rootGroupName = (rootGroupData == null)
																								? BLANK_STRING
																								: rootGroupData
																										.getName();

																						regServiceManager
																								.getRegisterGroupData(
																										groupDmc,
																										new DataRequestMonitor<IRegisterGroupDMData>(
																												executor,
																												null) {
																											@Override
																											protected void handleSuccess() {
																												// Resolve the name of the selected group being edited
																												String selGroupName = getData()
																														.getName();
																												// Create the Register descriptors to
																												// access all children registers
																												DialogRegisterProvider descriptors = buildDescriptors(
																														rootGroupName,
																														rootRegisters,
																														rootRegistersData,
																														childRegisterData);

																												// Create Dialog to Resolve new user
																												// selection of group name and registers
																												getDialogSelection(
																														shell,
																														selGroupName,
																														descriptors
																																.getAllRegisters(),
																														descriptors
																																.getcheckedRegisters(),
																														new DataRequestMonitor<IRegisterGroupDescriptor>(
																																executor,
																																null) {
																															@Override
																															protected void handleSuccess() {
																																try {
																																	editRegisterGroup(
																																			groupDmc,
																																			regServiceManager,
																																			getData());
																																} catch (CoreException e) {
																																	failed(e);
																																}
																															}
																														});
																											}
																										});
																					}

																				});
																	}
																});

													}
												});
									}
								});
					}
				});
	}

	private IRegisterGroupDMContext[] resolveSelectedGroups(IStructuredSelection selection) {
		IRegisterGroupDMContext[] selectedGroups = null;
		List<IRegisterGroupDMContext> groupList = new ArrayList<>();
		if (selection != null && !selection.isEmpty()) {
			for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
				Object element = iterator.next();
				if (element instanceof IDMVMContext) {
					IDMContext dmContext = ((IDMVMContext) element).getDMContext();
					// Make sure this selection is a group
					if (dmContext instanceof IRegisterGroupDMContext) {
						IRegisterGroupDMContext groupDmc = (IRegisterGroupDMContext) dmContext;
						groupList.add(groupDmc);
					}
				}
			}

		}

		selectedGroups = groupList.toArray(new IRegisterGroupDMContext[groupList.size()]);
		return selectedGroups;
	}

	@ConfinedToDsfExecutor("selectionContext.fsession.getExecutor()")
	private IRegisterDMContext[] resolveSelectedRegisters(IStructuredSelection selection) {
		List<IRegisterDMContext> selectedRegistersList = new ArrayList<>();
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

		IRegisterDMContext[] selectedRegisters = selectedRegistersList
				.toArray(new IRegisterDMContext[selectedRegistersList.size()]);
		return selectedRegisters;
	}

	@ConfinedToDsfExecutor("selectionContext.fsession.getExecutor()")
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

	@ConfinedToDsfExecutor("selectionContext.fsession.getExecutor()")
	private void getDialogSelection(Shell shell, String originalGroupName, IRegisterDescriptor[] allRegisters,
			IRegisterDescriptor[] checkedRegisters, DataRequestMonitor<IRegisterGroupDescriptor> rm) {
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
	@ConfinedToDsfExecutor("selectionContext.fsession.getExecutor()")
	private DialogRegisterProvider buildDescriptors(String groupName, IRegisterDMContext[] registers,
			IRegisterDMData[] registerData, IRegisterDMData[] checkedRegistersData) {
		assert (registers.length == registerData.length);

		List<RegisterDescriptor> checkedDescriptorsList = new ArrayList<>();

		final RegisterDescriptor[] regDescriptors = new RegisterDescriptor[registers.length];

		Map<String, RegisterDescriptor> mapNameToRegDescriptor = new HashMap<>();

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

	@ConfinedToDsfExecutor("selectionContext.fsession.getExecutor()")
	private void addRegisterGroup(IRegisters2 regServiceManager, IRegisterGroupDescriptor groupDescriptor,
			IContainerDMContext contDmc) throws CoreException {
		IRegisterDescriptor[] selectedRegisters = groupDescriptor.getChildren();
		if (selectedRegisters != null) {
			String groupName = groupDescriptor.getName();
			// Register the addition of the group and notify the change
			IRegisterDMContext[] registers = getRegisterContexts(selectedRegisters);
			regServiceManager.addRegisterGroup(contDmc, groupName, registers,
					new RequestMonitor(regServiceManager.getSession().getExecutor(), null) {
						@Override
						protected void handleCompleted() {
							if (getStatus() != null && getStatus().getCode() == IDsfStatusConstants.NOT_SUPPORTED) {
								// This user request is not supported, notify the user
								notifyUser(getStatus().getMessage());
							}

						}
					});
		}
	}

	@ConfinedToDsfExecutor("selectionContext.fsession.getExecutor()")
	private void editRegisterGroup(IRegisterGroupDMContext group, IRegisters2 regServiceManager,
			IRegisterGroupDescriptor groupDescriptor) throws CoreException {
		IRegisterDescriptor[] selectedRegisters = groupDescriptor.getChildren();
		if (selectedRegisters != null) {
			String groupName = groupDescriptor.getName();
			// Register the addition of the group and notify the change
			regServiceManager.editRegisterGroup(group, groupName, getRegisterContexts(selectedRegisters),
					new RequestMonitor(regServiceManager.getSession().getExecutor(), null) {
						@Override
						protected void handleCompleted() {
							if (getStatus() != null && getStatus().getCode() == IDsfStatusConstants.NOT_SUPPORTED) {
								// This user request is not supported, notify the user
								notifyUser(getStatus().getMessage());
							}

						}
					});
		}
	}

	private void failed(Throwable e) {
		IStatus status = new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED,
				REG_GROUP_ACTION_FAILED + e.getMessage(), e);
		DsfUIPlugin.log(status);
	}

	private void notifyUser(final String message) {

		Runnable runnable = () -> {
			Shell parent = DsfUIPlugin.getActiveWorkbenchShell();
			if (parent != null) {
				MessageDialog.openInformation(parent, Messages.Information,
						Messages.RegisterGroupInfo + ": " + message); //$NON-NLS-1$
			}
		};

		Display.getDefault().asyncExec(runnable);
	}

	/**
	 * @return true - OK to restore
	 */
	private boolean restoreConfirmed() {
		ConfirmRestoreDialog restoreDialog = new ConfirmRestoreDialog();
		Display.getDefault().syncExec(restoreDialog);

		return restoreDialog.fRestore;
	}

	private class ConfirmRestoreDialog implements Runnable {

		private Boolean fRestore = false;

		@Override
		public void run() {
			Shell parent = DsfUIPlugin.getActiveWorkbenchShell();
			if (parent != null) {

				String title = Messages.RegisterGroupConfirmRestoreTitle;
				String message = Messages.RegisterGroupConfirmRestoreMessage;
				String[] buttonLabels = new String[] { Messages.RegisterGroupRestore,
						Messages.RegisterGroupRestoreCancel, };
				MessageDialog dialog = new MessageDialog(parent, title, null, message, MessageDialog.QUESTION,
						buttonLabels, 0);
				int res = dialog.open();
				if (res == 0) { // RESTORE
					fRestore = true;
				} else if (res == 1) { // CANCEL
					fRestore = false;
				}
			}
		}

	}

	private void getRegistersData(IRegisterDMContext[] regDMCs, IRegisters2 regService,
			final DataRequestMonitor<IRegisterDMData[]> rm) {
		final IRegisterDMData[] regDataArray = new IRegisterDMData[regDMCs.length];
		final DsfExecutor executor = regService.getExecutor();

		final CountingRequestMonitor crm = new CountingRequestMonitor(executor, rm) {
			@Override
			protected void handleSuccess() {
				rm.setData(regDataArray);
				rm.done();
			}
		};

		for (int i = 0; i < regDMCs.length; i++) {
			final int index = i;
			regService.getRegisterData(regDMCs[index], new DataRequestMonitor<IRegisterDMData>(executor, crm) {
				@Override
				protected void handleSuccess() {
					regDataArray[index] = getData();
					crm.done();
				}
			});
		}

		crm.setDoneCount(regDMCs.length);
	}
}
