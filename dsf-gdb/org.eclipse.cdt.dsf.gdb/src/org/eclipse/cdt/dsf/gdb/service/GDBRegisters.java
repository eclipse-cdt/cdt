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
 *     Bruno Medeiros (Renesas) - Persistence of register groups per process (449104)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.internal.core.RegisterGroupsPersistance;
import org.eclipse.cdt.debug.internal.core.model.IRegisterGroupDescriptor;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.CompositeDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters2;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.MIRegisters;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;

/**
 * <p>An extension of MIRegisters to support management of Register Groups as per the IRegisters2 interface.</p>
 * <p>The managed registered groups are user-defined subsets of the complete list of Registers reported by GDB for a specific Target</p>
 * <p>This class also triggers the read/write (persistence) of the user-defined Register Groups during the start/shutdown process of a session respectively.
 * It optionally supports persistence of user-defined Register Groups per container/process,
 * see {@link #getPersistenceIdForRegisterGroupContainer(IContainerDMContext)}.</p>
 * @since 4.6
 */
public class GDBRegisters extends MIRegisters implements IRegisters2 {

	/**
	 * Unique temporary id for a group. 0 is reserved for the root group
	 */
	private static int fGroupBookingCount = 1;

	/**
	 * References to all groups related to a given context. Different programs may use different sets of registers e.g.
	 * 32/64 bits
	 */
	private final ContextToGroupsMap<IContainerDMContext, MIRegisterGroupDMC[]> fContextToGroupsMap = new ContextToGroupsMap<>();

	/**
	 * Used to save base list of Registers associated to a group, these registers can not be used as is for
	 * "getRegisters" since the execution context may change e.g. The current selection points to a process or a running
	 * thread or a different frame, all information besides the execution context is valid.
	 */
	private final GroupRegistersMap<MIRegisterGroupDMC, MIRegisterDMC[]> fGroupToRegistersMap = new GroupRegistersMap<>();

	/**
	 * Saves the Group number to RegisterGroupDescriptor created from the serialized memento, The group number is used across contexts as the
	 * key:Integer uses a booking number incremented across container contexts
	 */
	private final Map<Integer, IRegisterGroupDescriptor> fGroupMementoDescriptorIndex = new HashMap<>();

	public GDBRegisters(DsfSession session) {
		super(session);
	}

	private class ContextToGroupsMap<K, V> extends HashMap<IContainerDMContext, MIRegisterGroupDMC[]> {
		private static final long serialVersionUID = 1L;
		private final Map<IContainerDMContext, Map<String, MIRegisterGroupDMC>> fNameToGroupMap = new HashMap<>();

		@Override
		public MIRegisterGroupDMC[] put(IContainerDMContext key, MIRegisterGroupDMC[] value) {
			if (key == null || value == null) {
				return null;
			}

			// Contents are updated for the given context, reset this context
			// cache
			// So it can be rebuilt on the next get
			fNameToGroupMap.remove(key);
			return super.put(key, value);
		}

		@Override
		public void clear() {
			fNameToGroupMap.clear();
			fGroupMementoDescriptorIndex.clear();
			fGroupToRegistersMap.clear();
			super.clear();
		}

		@Override
		public MIRegisterGroupDMC[] remove(Object key) {
			fNameToGroupMap.remove(key);
			return super.remove(key);
		}

		public Map<String, MIRegisterGroupDMC> getGroupNameMap(IContainerDMContext key) {
			// validate input
			if (key == null) {
				return null;
			}

			Map<String, MIRegisterGroupDMC> nameMap = fNameToGroupMap.get(key);
			if (nameMap == null) {
				// cache not resolved, rebuild
				nameMap = new HashMap<>();
				MIRegisterGroupDMC[] groupsArr = super.get(key);
				// If the container context exist, build the name map
				if (groupsArr != null) {
					for (MIRegisterGroupDMC group : groupsArr) {
						nameMap.put(group.getName(), group);
					}

					// cache it !
					fNameToGroupMap.put(key, nameMap);
				}
			}

			return nameMap;
		}

		/**
		 * Needed when group name(s) change but the associated group objects remain the same
		 */
		public void resetGroupNameMap(IContainerDMContext key) {
			fNameToGroupMap.remove(key);
		}

		/**
		 * The result will reflect the reverse order of creation, i.e. last created first
		 */
		public MIRegisterGroupDMC[] getReversed(IDMContext key) {
			MIRegisterGroupDMC[] groups = get(key);
			MIRegisterGroupDMC[] reversedGroups = new MIRegisterGroupDMC[groups.length];
			int size = groups.length;
			for (int i = 0; i < size; i++) {
				reversedGroups[size - 1 - i] = groups[i];
			}

			return reversedGroups;
		}
	}

	/**
	 * Used to associate two dependent maps, Group to ordered Register[] and Group to indexed registers (Map<String,
	 * Register>)
	 */
	private class GroupRegistersMap<K, V> extends HashMap<MIRegisterGroupDMC, MIRegisterDMC[]> {
		private static final long serialVersionUID = 1L;
		private final Map<MIRegisterGroupDMC, Map<String, MIRegisterDMC>> fNameToRegisterMap = new HashMap<>();

		@Override
		public MIRegisterDMC[] put(MIRegisterGroupDMC key, MIRegisterDMC[] value) {
			// Make sure a previous entry of the key does not keep an out of
			// date cache
			fNameToRegisterMap.remove(key);
			return super.put(key, value);
		}

		public Map<String, MIRegisterDMC> getIndexedRegisters(MIRegisterGroupDMC key) {
			Map<String, MIRegisterDMC> nameToRegisterMap = fNameToRegisterMap.get(key);
			if (nameToRegisterMap == null && get(key) != null) {
				// Needs indexing
				nameToRegisterMap = indexRegisters(key);
				if (nameToRegisterMap != null) {
					fNameToRegisterMap.put(key, nameToRegisterMap);
				}
			}

			return nameToRegisterMap;
		}

		@Override
		public void clear() {
			fNameToRegisterMap.clear();
			super.clear();
		}

		@Override
		public MIRegisterDMC[] remove(Object key) {
			fNameToRegisterMap.remove(key);
			return super.remove(key);
		}

		private Map<String, MIRegisterDMC> indexRegisters(MIRegisterGroupDMC registerGroup) {
			MIRegisterDMC[] registers = super.get(registerGroup);
			if (registers == null || registers.length < 1) {
				return null;
			}

			Map<String, MIRegisterDMC> registerNameMap = new HashMap<>();
			for (IRegisterDMContext register : registers) {
				assert (register instanceof MIRegisterDMC);
				MIRegisterDMC registerDmc = (MIRegisterDMC) register;
				registerNameMap.put(registerDmc.getName(), registerDmc);
			}

			return registerNameMap;
		}
	}

	private class RegisterGroupDescriptor implements IRegisterGroupDescriptor {
		private final boolean fEnabled;
		private final MIRegisterGroupDMC fgroup;

		public RegisterGroupDescriptor(MIRegisterGroupDMC group, boolean enabled) {
			fgroup = group;
			fEnabled = enabled;
		}

		@Override
		public String getName() {
			return fgroup.getName();
		}

		@Override
		public boolean isEnabled() {
			return fEnabled;
		}

		@Override
		public String getContainerId() {
			IContainerDMContext parent = fgroup.getContainerParent();
			return getPersistenceIdForRegisterGroupContainer(parent);
		}

		@Override
		public IRegisterDescriptor[] getChildren() throws CoreException {
			IRegisterDescriptor[] regDescriptors = null;
			// Get a snap shot of the current registers
			MIRegisterDMC[] registers = fGroupToRegistersMap.get(fgroup);
			if (registers != null && registers.length > 0) {
				regDescriptors = new IRegisterDescriptor[registers.length];
				for (int i = 0; i < registers.length; i++) {
					regDescriptors[i] = new RegisterDescriptor(registers[i]);
				}
			} else {
				// The registers were probably never fetched, obtain the
				// original definitions from deserialized groups
				IRegisterGroupDescriptor groupMementoDescriptor = fGroupMementoDescriptorIndex.get(fgroup.getGroupNo());
				if (groupMementoDescriptor != null) {
					regDescriptors = groupMementoDescriptor.getChildren();
				}
			}

			return regDescriptors;
		}

	}

	private class RegisterDescriptor implements IRegisterDescriptor {
		private final MIRegisterDMC fRegister;
		private final static String ORIGINAL_GROUP_NAME = "Main"; //$NON-NLS-1$

		public RegisterDescriptor(MIRegisterDMC register) {
			fRegister = register;
		}

		@Override
		public String getName() {
			return fRegister.getName();
		}

		@Override
		public String getGroupName() {
			// Hard coded to keep compatibility with CDI's format
			return ORIGINAL_GROUP_NAME;
		}
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			public void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(final RequestMonitor requestMonitor) {
		register(new String[] { IRegisters.class.getName(), IRegisters2.class.getName(), MIRegisters.class.getName(),
				GDBRegisters.class.getName() }, new Hashtable<String, String>());
		requestMonitor.done();
	}

	@Override
	public void getRegisterGroups(final IDMContext ctx, final DataRequestMonitor<IRegisterGroupDMContext[]> rm) {
		final IContainerDMContext contDmc = DMContexts.getAncestorOfType(ctx, IContainerDMContext.class);
		if (contDmc == null) {
			IStatus status = new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"Container context not provided, unable to get Register Groups", null); //$NON-NLS-1$
			rm.setStatus(status);
			rm.done();
			return;
		}

		if (fContextToGroupsMap.containsKey(contDmc)) {
			// The groups information is already available and can be returned
			rm.setData(fContextToGroupsMap.getReversed(contDmc));
			rm.done();
			return;
		}

		// The register groups information needs to be built from GDB and user-defined groups i.e. de-serialized
		// from the launch configuration.
		super.getRegisterGroups(ctx, new ImmediateDataRequestMonitor<IRegisterGroupDMContext[]>(rm) {
			@Override
			@ConfinedToDsfExecutor("fExecutor")
			protected void handleSuccess() {
				final IRegisterGroupDMContext[] regGroups = getData();
				// only one group from MI is expected at the moment
				assert (regGroups.length == 1);
				assert (regGroups[0] instanceof MIRegisterGroupDMC);

				final MIRegisterGroupDMC miGroup = (MIRegisterGroupDMC) regGroups[0];

				// read serialized groups
				MIRegisterGroupDMC[] mementoGroups = readGroupsFromMemento(contDmc);

				// Track the groups associated to this context
				// The root group (mi) is placed and expected at index 0 followed
				// by the user groups read from the memento
				MIRegisterGroupDMC[] regGroupsCtx = concatenateArr(new MIRegisterGroupDMC[] { miGroup }, mementoGroups);

				// Have the information ready for subsequent request or group operations.
				fContextToGroupsMap.put(contDmc, regGroupsCtx);

				// Reverse the order i.e. latest on top and get back to parent monitor
				rm.setData(fContextToGroupsMap.getReversed(contDmc));
				rm.done();
			}
		});
	}

	@Override
	public void getRegisterGroupData(final IRegisterGroupDMContext regGroupDmc,
			final DataRequestMonitor<IRegisterGroupDMData> rm) {

		assert (regGroupDmc instanceof MIRegisterGroupDMC);

		if (regGroupDmc instanceof MIRegisterGroupDMC) {
			MIRegisterGroupDMC groupDmc = (MIRegisterGroupDMC) regGroupDmc;
			rm.setData(createRegisterGroupData(groupDmc));
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
					"Unable to resolve Group Data, Invalid Register Group provided", null)); //$NON-NLS-1$
		}

		rm.done();
	}

	private IRegisterGroupDMData createRegisterGroupData(final MIRegisterGroupDMC groupDmc) {

		IRegisterGroupDMData groupData = new IRegisterGroupDMData() {
			@Override
			public String getName() {
				return groupDmc.getName();
			}

			@Override
			public String getDescription() {
				if (groupDmc.getName().equals(ROOT_GROUP_NAME)) {
					return ROOT_GROUP_DESCRIPTION;
				}

				return BLANK_STRING;
			}

		};

		// Make sure this group is available in the groups to registers map,
		// as this map provides the input to save /serialize the groups
		// The associated registers will be resolved upon request.
		if (fGroupToRegistersMap.get(groupDmc) == null) {
			fGroupToRegistersMap.put(groupDmc, new MIRegisterDMC[0]);
		}

		return groupData;
	}

	@Override
	public void getRegisters(final IDMContext aCtx, final DataRequestMonitor<IRegisterDMContext[]> rm) {
		findRegisterGroup(aCtx, ROOT_GROUP_NAME, new ImmediateDataRequestMonitor<IRegisterGroupDMContext>() {
			@Override
			protected void handleSuccess() {
				//Get the root group, needed as a possible default group and to resolve target registers
				IRegisterGroupDMContext rootGroup = getData();
				assert (rootGroup instanceof MIRegisterGroupDMC);
				final MIRegisterGroupDMC rootGroupContext = (MIRegisterGroupDMC) rootGroup;

				//if the received context does not contain a register group i.e.is null, the default group to resolve registers is the root group
				MIRegisterGroupDMC tGroupDmc = DMContexts.getAncestorOfType(aCtx, MIRegisterGroupDMC.class);

				IDMContext tCtx = aCtx;
				if (tGroupDmc == null) {
					tGroupDmc = rootGroupContext;
					//We need a register group as part of the context to resolve registers
					tCtx = new CompositeDMContext(new IDMContext[] { aCtx, tGroupDmc });
				}

				final IDMContext ctx = tCtx;

				final MIRegisterGroupDMC groupDmc = tGroupDmc;
				// check if base registers have been loaded already
				MIRegisterDMC[] baseRegisters = fGroupToRegistersMap.get(groupDmc);
				if (baseRegisters != null && baseRegisters.length > 0) {
					// use baseRegisters to build registers associated to the given context
					buildGroupRegisters(ctx, baseRegisters, rm);
					return;
				}

				IContainerDMContext rootGroupContainer = DMContexts.getAncestorOfType(rootGroupContext,
						IContainerDMContext.class);
				MIRegisterDMC[] registerBase = fGroupToRegistersMap.get(rootGroupContainer);
				if (registerBase == null || registerBase.length < 1) {
					// Prepare to fetch the register information from GDB (root group)
					// Include the frame/execution context whenever available
					IDMContext miExecDmc = DMContexts.getAncestorOfType(ctx, IFrameDMContext.class);
					if (miExecDmc == null) {
						miExecDmc = DMContexts.getAncestorOfType(ctx, IMIExecutionDMContext.class);
					}

					// if Execution context is not available return shallow registers i.e. no execution context
					final CompositeDMContext compCtx;
					if (miExecDmc != null) {
						compCtx = new CompositeDMContext(new IDMContext[] { rootGroupContext, miExecDmc });
					} else {
						compCtx = new CompositeDMContext(new IDMContext[] { rootGroupContext });
					}

					// Fetch the register base from GDB
					GDBRegisters.super.getRegisters(compCtx,
							new DataRequestMonitor<IRegisterDMContext[]>(getExecutor(), rm) {
								@Override
								@ConfinedToDsfExecutor("fExecutor")
								protected void handleSuccess() {
									IRegisterDMContext[] iregisters = getData();
									MIRegisterDMC[] registers = Arrays.copyOf(iregisters, iregisters.length,
											MIRegisterDMC[].class);

									// associate group to bare registers i.e. not associated to a specific execution context
									fGroupToRegistersMap.put(rootGroupContext, toBareRegisters(registers));
									if (groupDmc.getName().equals(ROOT_GROUP_NAME)) {
										buildGroupRegisters(ctx, registers, rm);
										return;
									}

									// Now proceed to resolve the requested user group registers
									getUserGroupRegisters(ctx, rm);
								}
							});
				} else {
					if (groupDmc.getName().equals(ROOT_GROUP_NAME)) {
						buildGroupRegisters(ctx, registerBase, rm);
					} else {
						// resolve user group registers
						getUserGroupRegisters(ctx, rm);
					}
				}

			}
		});
	}

	@Override
	public void canAddRegisterGroup(IDMContext selectionContext, DataRequestMonitor<Boolean> rm) {
		// Not relevant checks at this point
		rm.setData(true);
		rm.done();
	}

	@Override
	public void addRegisterGroup(final IDMContext containerContext, final String groupName,
			final IRegisterDMContext[] registers, RequestMonitor rm) {
		if (registers == null || registers.length < 1) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
					Messages.RegisterGroup_invalid_number_of_registers, null));
			rm.done();
			return;
		}

		if (groupName.trim().toLowerCase().equals(ROOT_GROUP_NAME.toLowerCase())) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
					NLS.bind(Messages.RegisterGroup_name_reserved, ROOT_GROUP_NAME), null));
			rm.done();
			return;
		}

		if (!(registers[0] instanceof MIRegisterDMC)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"Unexpected IRegisterDMContext input instance type", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		IContainerDMContext contDmc = DMContexts.getAncestorOfType(registers[0], IContainerDMContext.class);
		if (contDmc == null) {
			contDmc = DMContexts.getAncestorOfType(containerContext, IContainerDMContext.class);
			if (contDmc == null) {
				rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
						"Unable to add Register group, Invalid Container", null)); //$NON-NLS-1$
				rm.done();
				return;
			}
		}

		// Make sure the name is not currently in use
		if (fContextToGroupsMap.getGroupNameMap(contDmc).get(groupName) != null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
					NLS.bind(Messages.RegisterGroup_name_used, groupName), null));
			rm.done();
			return;
		}

		//create the new group
		MIRegisterGroupDMC group = new MIRegisterGroupDMC(this, contDmc, fGroupBookingCount, groupName);
		fGroupBookingCount++;

		// Update the context to groups map including the new group
		fContextToGroupsMap.put(contDmc,
				concatenateArr(fContextToGroupsMap.get(contDmc), new MIRegisterGroupDMC[] { group }));

		//type adjustment
		MIRegisterDMC[] miRegisters = Arrays.copyOf(registers, registers.length, MIRegisterDMC[].class);

		// associate group to bare registers i.e. not associated to a specific execution context
		MIRegisterDMC[] bareRegisters = toBareRegisters(miRegisters);
		fGroupToRegistersMap.put(group, bareRegisters);

		// Create event notification, to trigger the UI refresh
		getSession().dispatchEvent(new GroupsChangedDMEvent(contDmc), null);
		rm.done();
	}

	@Override
	public void canEditRegisterGroup(IRegisterGroupDMContext group, DataRequestMonitor<Boolean> rm) {
		rm.setData(canEditRegisterGroup(group));
		rm.done();
	}

	@Override
	public void editRegisterGroup(IRegisterGroupDMContext group, String newGroupName, IRegisterDMContext[] iRegisters,
			RequestMonitor rm) {

		if (iRegisters != null && iRegisters.length == 0) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
					Messages.RegisterGroup_invalid_number_of_registers, null));
			return;
		}

		if (!(group instanceof MIRegisterGroupDMC)) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown DMC type", null)); //$NON-NLS-1$
			return;
		}

		IContainerDMContext contDmc = DMContexts.getAncestorOfType(group, IContainerDMContext.class);
		if (contDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"Unable to edit Register group, Invalid Container", null)); //$NON-NLS-1$
			rm.done();
		}

		MIRegisterGroupDMC miGroup = ((MIRegisterGroupDMC) group);

		if (!canEditRegisterGroup(group)) {
			// Should not happen as canEdit is expected to be called before edit
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
					"Cannot currently edit register groups", null)); //$NON-NLS-1$
			return;
		}

		if (newGroupName != null && !newGroupName.isEmpty()) {
			// Make sure the new group name is not the reserved root group name
			if (newGroupName.trim().toLowerCase().equals(ROOT_GROUP_NAME.toLowerCase())) {
				rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
						NLS.bind(Messages.RegisterGroup_name_reserved, ROOT_GROUP_NAME), null));
				rm.done();
				return;
			}

			// Make sure the name is not currently in use
			if (!miGroup.getName().equals(newGroupName)) {
				// we are updating the name, lets make sure this new name is not in use
				if (fContextToGroupsMap.getGroupNameMap(contDmc).get(newGroupName) != null) {
					rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
							NLS.bind(Messages.RegisterGroup_name_used, newGroupName), null));
					rm.done();
					return;
				}
			}

			miGroup.setName(newGroupName);

			//make sure we update the group name cache
			fContextToGroupsMap.resetGroupNameMap(contDmc);

			generateRegisterGroupChangedEvent(miGroup);
		} else {
			// Request to keep name the same
		}

		if (iRegisters != null) {
			assert (iRegisters.length > 0);

			// transform to MIRegistersDMC[]
			MIRegisterDMC[] registers = arrangeRegisters(iRegisters);

			// preserve registers in a general format not associated to a specific frame
			registers = toBareRegisters(registers);
			fGroupToRegistersMap.put(miGroup, registers);
			// Notify of Registers changed
			generateRegistersChangedEvent(miGroup);
		} else {
			// Request to keep register list the same
		}

		rm.done();
	}

	@Override
	public void removeRegisterGroups(IRegisterGroupDMContext[] groups, RequestMonitor rm) {
		removeRegisterGroups(groups, false, rm);
	}

	@Override
	public void restoreDefaultGroups(final IDMContext selectionContext, final RequestMonitor rm) {
		for (IDMContext context : fContextToGroupsMap.keySet()) {
			removeRegisterGroups(context);
		}

		// clean the serialized registers group information
		save();

		// Clear all global references to the contexts and groups
		fContextToGroupsMap.clear();
		rm.done();
	}

	/**
	 * Reset this class i.e. does not impact saved groups within launch configuration
	 *
	 * @param rm
	 */
	public void reset(final RequestMonitor rm) {
		for (IDMContext context : fContextToGroupsMap.keySet()) {
			removeRegisterGroups(context);
		}

		// Clear all global references to the contexts and groups
		fContextToGroupsMap.clear();
		rm.done();
	}

	@Override
	public void findRegisterGroup(final IDMContext ctx, final String name,
			final DataRequestMonitor<IRegisterGroupDMContext> rm) {
		final IContainerDMContext contDmc = DMContexts.getAncestorOfType(ctx, IContainerDMContext.class);
		if (contDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Container context not found", //$NON-NLS-1$
					null));
			rm.done();
			return;
		}

		if (fContextToGroupsMap.get(ctx) == null) {
			// Need to build the list of register groups including the one from target
			getRegisterGroups(contDmc, new DataRequestMonitor<IRegisterGroupDMContext[]>(getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					// Using the list of groups indirectly to find the one with the given name from it
					findRegisterGroup(contDmc, name, rm);
				}
			});
		} else {
			// The context to groups map has been initialized and can be used
			findRegisterGroup(contDmc, name, rm);
		}
	}

	/**
	 * Call it only after getRegisterGroups has been called at least once, so the context to groups map is not empty
	 */
	private void findRegisterGroup(IContainerDMContext contDmc, String name,
			DataRequestMonitor<IRegisterGroupDMContext> rm) {
		Map<String, MIRegisterGroupDMC> nameToGroup = fContextToGroupsMap.getGroupNameMap(contDmc);
		if (nameToGroup != null) {
			rm.setData(nameToGroup.get(name));
		} else {
			rm.setData(null);
		}

		rm.done();
	}

	@Override
	public void shutdown(RequestMonitor rm) {
		save();
		super.shutdown(rm);
	}

	/**
	 * Save the register group settings into the underlying launch configuration.
	 */
	public void save() {
		IRegisterGroupDescriptor[] groups = buildDescriptors();
		ILaunchConfiguration launchConfig = getLaunchConfig();
		if (launchConfig != null) {
			RegisterGroupsPersistance serializer = new RegisterGroupsPersistance(launchConfig);
			try {
				serializer.saveGroups(groups);
			} catch (CoreException e) {
				GdbPlugin.log(e);
			}
		}

	}

	/**
	 * Cast to MI and sort them ascending order by register index
	 */
	private MIRegisterDMC[] arrangeRegisters(IRegisterDMContext[] iRegisters) {
		TreeMap<Integer, MIRegisterDMC> sortedRegisters = new TreeMap<>();
		for (int i = 0; i < iRegisters.length; i++) {
			assert (iRegisters[i] instanceof MIRegisterDMC);
			MIRegisterDMC register = (MIRegisterDMC) iRegisters[i];
			sortedRegisters.put(register.getRegNo(), register);
		}

		return sortedRegisters.values().toArray(new MIRegisterDMC[sortedRegisters.size()]);
	}

	/**
	 * @param groups
	 *            - The groups to be removed
	 * @param removeRoot
	 *            - indicates if the root group needs to be removed e.g. during restore to defaults
	 * @param rm
	 */
	private void removeRegisterGroups(IRegisterGroupDMContext[] groups, boolean removeRoot, RequestMonitor rm) {
		if (groups != null) {
			// Save a list of updated containers to only send an update event for each of them
			final Set<IContainerDMContext> updatedContainers = new HashSet<>();
			for (IRegisterGroupDMContext group : groups) {

				if (!removeRoot) {
					// Prevent removal of the Root Group
					if (!(group instanceof MIRegisterGroupDMC)) {
						// All groups are expected to be instances of MIREgisterGroupDMC
						assert (false);
						continue;
					}

					if (((MIRegisterGroupDMC) group).getName().equals(ROOT_GROUP_NAME)) {
						// Skip removal of a root group, except when restoring to default groups
						continue;
					}
				}

				final IContainerDMContext containerDmc = DMContexts.getAncestorOfType(group, IContainerDMContext.class);

				// All given groups are expected to be part of the same Container, however it's safer to create a new list
				// per context to cover the unsual case
				// This could be revisited in case there is performance concerns which does not seem an issue at this
				// point.
				MIRegisterGroupDMC[] groupsCtx = fContextToGroupsMap.get(containerDmc);
				assert (groupsCtx != null);

				if (groupsCtx != null) {
					List<MIRegisterGroupDMC> groupsList = new ArrayList<>(Arrays.asList(groupsCtx));

					// Removing a single group
					groupsList.remove(group);

					// Back to context map without the given group
					fContextToGroupsMap.put(containerDmc,
							groupsList.toArray(new MIRegisterGroupDMC[groupsList.size()]));
					// Now remove the group from the groups to registers map
					if (fGroupToRegistersMap.remove(group) != null) {
						updatedContainers.add(containerDmc);
					}
				} else {
					rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
							"Unable to remove Register group, Invalid Container", null)); //$NON-NLS-1$
					rm.done();
					return;
				}
			}

			// Sending only one update per container
			for (IContainerDMContext container : updatedContainers) {
				getSession().dispatchEvent(new GroupsChangedDMEvent(container), null);
			}
		}

		rm.done();
	}

	private void removeRegisterGroups(IDMContext containerDmc) {
		MIRegisterGroupDMC[] groups = fContextToGroupsMap.get(containerDmc);
		if (groups != null) {
			removeRegisterGroups(groups, true, new RequestMonitor(getExecutor(), null) {
			});
		}
	}

	private boolean canEditRegisterGroup(IRegisterGroupDMContext group) {
		if (group instanceof MIRegisterGroupDMC) {
			MIRegisterGroupDMC miGroup = ((MIRegisterGroupDMC) group);
			// Prevent changes to the root group
			if (miGroup.getName().trim().toLowerCase().equals(ROOT_GROUP_NAME.toLowerCase())) {
				return false;
			}

			// Expected to be on the existing groups map
			if (fGroupToRegistersMap.containsKey(group)) {
				return true;
			}
		}

		return false;
	}

	private ILaunchConfiguration getLaunchConfig() {
		ILaunch launch = (ILaunch) getSession().getModelAdapter(ILaunch.class);
		if (launch == null) {
			// The launch is no longer active
			return null;
		}

		ILaunchConfiguration config = launch.getLaunchConfiguration();
		return config;
	}

	IRegisterGroupDescriptor[] buildDescriptors() {
		// use a tree map to sort the entries by group number
		TreeMap<Integer, MIRegisterGroupDMC> sortedGroups = new TreeMap<>();

		for (MIRegisterGroupDMC group : fGroupToRegistersMap.keySet()) {
			sortedGroups.put(Integer.valueOf(group.getGroupNo()), group);
		}

		// Not serializing the root group which is dynamically created from GDB
		sortedGroups.remove(Integer.valueOf(0));

		Set<Entry<Integer, MIRegisterGroupDMC>> groupSet = sortedGroups.entrySet();
		IRegisterGroupDescriptor[] descriptors = new IRegisterGroupDescriptor[groupSet.size()];

		// load group descriptors sorted in ascending order to their group
		// number into the result array
		int i = 0;
		for (Entry<Integer, MIRegisterGroupDMC> groupEntry : groupSet) {
			descriptors[i] = new RegisterGroupDescriptor(groupEntry.getValue(), true);
			i++;
		}

		return descriptors;
	}

	private <T> T[] concatenateArr(T[] origArr, T[] deltaArr) {
		if (origArr == null) {
			return deltaArr;
		}

		if (deltaArr == null) {
			return origArr;
		}

		T[] newArr = Arrays.copyOf(origArr, origArr.length + deltaArr.length);
		System.arraycopy(deltaArr, 0, newArr, origArr.length, deltaArr.length);
		return newArr;
	}

	MIRegisterGroupDMC[] readGroupsFromMemento(final IContainerDMContext contDmc) {
		String containerId = getPersistenceIdForRegisterGroupContainer(contDmc);

		RegisterGroupsPersistance deserializer = new RegisterGroupsPersistance(getLaunchConfig());
		IRegisterGroupDescriptor[] groupDescriptions;
		try {
			groupDescriptions = deserializer.parseGroups(containerId);
		} catch (CoreException e) {
			return new MIRegisterGroupDMC[0];
		}

		List<MIRegisterGroupDMC> groups = new ArrayList<>();
		for (IRegisterGroupDescriptor group : groupDescriptions) {
			fGroupMementoDescriptorIndex.put(fGroupBookingCount, group);
			groups.add(new MIRegisterGroupDMC(this, contDmc, fGroupBookingCount, group.getName()));
			fGroupBookingCount++;
		}

		return groups.toArray(new MIRegisterGroupDMC[groups.size()]);
	}

	/**
	 * @return the persistence id for the register group in the given contDmc container context,
	 * or null to associate the Register Groups in the given container to the launch itself (this is the default behavior).
	 *
	 * Subclasses may override. Alternatively simply use override {@link #useProcessIdAsRegisterGroupPersistanceId()} to return true to have
	 * the process id be used as the persistence id.
	 *
	 * Note that for this to work correctly the id returned for a given container must be same across launch sessions,
	 * otherwise persistence info will be lost. (Hence why null is the default behavior)
	 *
	 * @since 5.3
	 */
	protected String getPersistenceIdForRegisterGroupContainer(final IContainerDMContext contDmc) {
		if (useProcessIdAsRegisterGroupPersistanceId()) {
			IMIProcessDMContext processDmc = DMContexts.getAncestorOfType(contDmc, IMIProcessDMContext.class);
			return processDmc == null ? null : processDmc.getProcId();
		} else {
			return null;
		}
	}

	/**
	 * @return whether the process id should be used as a container id for the persistence
	 * of the register groups. Subclasses may override.
	 *
	 * @see #getPersistenceIdForRegisterGroupContainer(IContainerDMContext)
	 *
	 * @since 5.3
	 */
	protected boolean useProcessIdAsRegisterGroupPersistanceId() {
		return false;
	}

	private void getUserGroupRegisters(IDMContext ctx, final DataRequestMonitor<IRegisterDMContext[]> rm) {
		final MIRegisterGroupDMC groupDmc = DMContexts.getAncestorOfType(ctx, MIRegisterGroupDMC.class);

		// Need to build the corresponding register[] from the memento descriptors
		IRegisterGroupDescriptor grpDescriptor = fGroupMementoDescriptorIndex.get(groupDmc.getGroupNo());

		if (grpDescriptor == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
					"The Register Group Descriptor does not exist for group: " + groupDmc.getName(), null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		MIRegisterDMC[] registers;
		try {
			// Resolve bare registers from the memento descriptors
			registers = resolveRegisters(grpDescriptor, ctx);
		} catch (CoreException e) {
			rm.setStatus(e.getStatus());
			rm.done();
			return;
		}

		// update internal data
		fGroupToRegistersMap.put(groupDmc, registers);

		// now resolve to context specific registers
		buildGroupRegisters(ctx, registers, rm);
	}

	/**
	 * Resolve register dmcs from de-serialized memento descriptors
	 */
	private MIRegisterDMC[] resolveRegisters(IRegisterGroupDescriptor grpDescriptor, IDMContext ctx)
			throws CoreException {
		final List<MIRegisterDMC> registerContexts = new ArrayList<>();
		final IContainerDMContext containerDmc = DMContexts.getAncestorOfType(ctx, IContainerDMContext.class);
		final MIRegisterGroupDMC groupDmc = DMContexts.getAncestorOfType(ctx, MIRegisterGroupDMC.class);

		IRegisterDescriptor[] registerDescriptions = grpDescriptor.getChildren();
		MIRegisterGroupDMC[] groupContexts = fContextToGroupsMap.get(containerDmc);
		if (groupContexts != null && groupContexts.length > 0) {
			// Get the General Group (base) at index 0,
			// Registers map indexed by name
			Map<String, MIRegisterDMC> indexedRegisterBase = fGroupToRegistersMap.getIndexedRegisters(groupContexts[0]);

			// For each descriptors find its corresponding MIRegisterDMC
			for (IRegisterDescriptor registerDescription : registerDescriptions) {
				MIRegisterDMC registerDmc = indexedRegisterBase.get(registerDescription.getName());
				if (registerDmc == null) {
					// The Register is not present from the base received from GDB
					// Create a register DMC with no execution dmc and invalid
					// register number e.g. not mapped to a gdb register.
					registerDmc = new MIRegisterDMC(this, groupDmc, -1, registerDescription.getName());
				}

				registerContexts.add(registerDmc);
			}
		}

		return registerContexts.toArray(new MIRegisterDMC[registerContexts.size()]);
	}

	@Override
	protected void generateRegisterChangedEvent(final IRegisterDMContext dmc) {
		// notify the register value change
		getSession().dispatchEvent(new RegisterChangedDMEvent(dmc), getProperties());

		// Propagate notification to all groups.
		// A change of a single register needs to be propagated to all groups within the same Container/Process
		// I.e. Some registers are dependent on the value of others and these dependent registers could be
		// associated to different groups.
		IContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IContainerDMContext.class);
		generateRegistersChangedEvent(containerDmc);
	}

	private void generateRegistersChangedEvent(IContainerDMContext containerDmc) {
		//resolve the groups for the current container (process) context
		final MIRegisterGroupDMC[] groups = fContextToGroupsMap.get(containerDmc);

		//trigger notification to all groups in the container
		for (int i = 0; i < groups.length; i++) {
			//We need final locals variables from the loop. Use a method call for this
			generateRegistersChangedEvent(groups[i]);
		}
	}

	private void generateRegistersChangedEvent(final MIRegisterGroupDMC groupDmc) {
		IRegistersChangedDMEvent event = new IRegistersChangedDMEvent() {
			@Override
			public IRegisterGroupDMContext getDMContext() {
				return groupDmc;
			}
		};

		getSession().dispatchEvent(event, getProperties());
	}

	private void generateRegisterGroupChangedEvent(final MIRegisterGroupDMC groupDmc) {
		IGroupChangedDMEvent event = new IGroupChangedDMEvent() {
			@Override
			public IRegisterGroupDMContext getDMContext() {
				return groupDmc;
			}
		};

		getSession().dispatchEvent(event, getProperties());
	}

	/**
	 * Create Registers from specific execution context to a generic register context, e.g. not associated to a specific
	 * execution context.
	 */
	private MIRegisterDMC[] toBareRegisters(MIRegisterDMC[] registers) {

		MIRegisterDMC[] bareRegisters = new MIRegisterDMC[registers.length];
		for (int i = 0; i < registers.length; i++) {
			// only one parent i.e. group context
			MIRegisterGroupDMC groupDmc = DMContexts.getAncestorOfType(registers[i], MIRegisterGroupDMC.class);
			assert (groupDmc != null);
			bareRegisters[i] = new MIRegisterDMC(this, groupDmc, registers[i].getRegNo(), registers[i].getName());
		}

		return bareRegisters;
	}

	private void buildGroupRegisters(final IDMContext ctx, final MIRegisterDMC[] baseRegisters,
			final DataRequestMonitor<IRegisterDMContext[]> rm) {
		final MIRegisterGroupDMC groupDmc = DMContexts.getAncestorOfType(ctx, MIRegisterGroupDMC.class);

		assert (groupDmc != null);

		final IFrameDMContext frameDmc = DMContexts.getAncestorOfType(ctx, IFrameDMContext.class);
		if (frameDmc == null) {
			// The selection does not provide a specific frame, then resolve the top frame on the current thread
			// if the execution frame is not available proceed with no frame context i.e. will not be able to resolve
			// values.
			IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(ctx, IMIExecutionDMContext.class);
			if (execDmc != null) {
				IStack stackService = getServicesTracker().getService(IStack.class);
				if (stackService != null) {
					stackService.getTopFrame(execDmc, new ImmediateDataRequestMonitor<IStack.IFrameDMContext>(rm) {
						@Override
						protected void handleSuccess() {
							cloneRegistersToContext(groupDmc, getData(), baseRegisters, rm);
						}

						@Override
						protected void handleFailure() {
							// Unable to resolve top frame on current thread.
							// The thread could e.g. be in running state,
							// we return register instances with no associated execution context
							// i.e. unable to resolve its associated value.
							cloneRegistersToContext(groupDmc, null, baseRegisters, rm);
						}
					});

					return;
				}
			}
		}

		cloneRegistersToContext(groupDmc, frameDmc, baseRegisters, rm);
	}

	/**
	 * Create a new array of register instances with the given context
	 */
	private void cloneRegistersToContext(MIRegisterGroupDMC groupDmc, IFrameDMContext frameDmc,
			MIRegisterDMC[] baseRegisters, DataRequestMonitor<IRegisterDMContext[]> rm) {
		MIRegisterDMC[] registers = new MIRegisterDMC[baseRegisters.length];
		if (frameDmc != null) {
			// build to valid stack frame context
			for (int i = 0; i < registers.length; i++) {
				registers[i] = new MIRegisterDMC(this, groupDmc, frameDmc, baseRegisters[i].getRegNo(),
						baseRegisters[i].getName());
			}
		} else {
			// build with no execution context, normal case if a selection is pointing to
			// e.g. a running thread, a process.. i.e. not able to associate register values.
			for (int i = 0; i < registers.length; i++) {
				registers[i] = new MIRegisterDMC(this, groupDmc, baseRegisters[i].getRegNo(),
						baseRegisters[i].getName());
			}
		}

		// return the registers
		rm.setData(registers);
		rm.done();
	}

	@Override
	public void canRemoveRegisterGroups(IRegisterGroupDMContext[] groups, DataRequestMonitor<Boolean> rm) {
		if (groups == null || groups.length < 1) {
			rm.setData(false);
			rm.done();
			return;
		}

		for (IRegisterGroupDMContext group : groups) {
			assert (group instanceof MIRegisterGroupDMC);
			MIRegisterGroupDMC miGroup = (MIRegisterGroupDMC) group;
			if (miGroup.getName().equals(ROOT_GROUP_NAME)) {
				// Not allowed to remove the root group
				rm.setData(false);
				rm.done();
				return;
			}
		}

		rm.setData(true);
		rm.done();

	}

	@Override
	public void canRestoreDefaultGroups(IDMContext selectionContext, DataRequestMonitor<Boolean> rm) {
		// Not relevant checks at this point
		rm.setData(true);
		rm.done();
	}

}
