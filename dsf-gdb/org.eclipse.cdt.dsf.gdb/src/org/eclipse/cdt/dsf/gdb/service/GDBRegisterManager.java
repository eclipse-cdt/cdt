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
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.internal.core.RegisterGroupsPersistance;
import org.eclipse.cdt.debug.internal.core.model.IRegisterGroupDescriptor;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.CompositeDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters2;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.MIRegisters;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.osgi.util.NLS;

/**
 * @since 4.3
 */
public class GDBRegisterManager extends MIRegisters implements IRegisters2 {
	private static final String PROPOSE_GROUP_NAME_ROOT = "Group_"; //$NON-NLS-1$

	/**
	 * Unique temporary id for a group. 0 is reserved for the root group
	 */
	private static int fGroupBookingCount = 1;

	/**
	 * Propose next unused group name suffix This water mark may be used across sessions i.e. different executors
	 */
	private static AtomicInteger fGroupNameWaterMark = new AtomicInteger(0);

	/**
	 * References to all groups related to a given context. Different programs may use different sets of registers e.g.
	 * 32/64 bits
	 */
	private final ContextToGroupsMap<IContainerDMContext, MIRegisterGroupDMC[]> fContextToGroupsMap = new ContextToGroupsMap<IContainerDMContext, MIRegisterGroupDMC[]>();

	/**
	 * Used to save base list of Registers associated to a group, these registers can not be used as is for
	 * "getRegisters" since the execution context may change e.g. The current selection points to a process or a running
	 * thread or a different frame, all information besides the execution context is valid.
	 */
	private final GroupRegistersMap<MIRegisterGroupDMC, MIRegisterDMC[]> fGroupToRegistersMap = new GroupRegistersMap<MIRegisterGroupDMC, MIRegisterDMC[]>();

	/**
	 * Saves the Group number to RegisterGroupDescriptor created from the serialized memento, The group number is used across contexts as the
	 * key:Integer uses a booking number incremented across container contexts
	 */
	@SuppressWarnings("restriction")
	private final Map<Integer, IRegisterGroupDescriptor> fGroupMementoDescriptorIndex = new HashMap<Integer, IRegisterGroupDescriptor>();

	public GDBRegisterManager(DsfSession session) {
		super(session);
	}

	private class ContextToGroupsMap<K, V> extends HashMap<IContainerDMContext, MIRegisterGroupDMC[]> {
		private static final long serialVersionUID = 1L;
		private final Map<IContainerDMContext, Map<String, MIRegisterGroupDMC>> fNameToGroupMap = new HashMap<IContainerDMContext, Map<String, MIRegisterGroupDMC>>();

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
			fGroupNameWaterMark.set(0);

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
				nameMap = new HashMap<String, MIRegisterGroupDMC>();
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
		private final Map<MIRegisterGroupDMC, Map<String, MIRegisterDMC>> fNameToRegisterMap = new HashMap<MIRegisterGroupDMC, Map<String, MIRegisterDMC>>();

		@Override
		public MIRegisterDMC[] put(MIRegisterGroupDMC key, MIRegisterDMC[] value) {
			// Make sure a previous entry of the key does not keep an out of
			// data cache
			fNameToRegisterMap.remove(key);
			// New entry added, check to see if the water mark to propose name suffixes needs to be bumped up
			updateGroupNameWaterMark(key.getName());
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

			Map<String, MIRegisterDMC> registerNameMap = new HashMap<String, MIRegisterDMC>();
			for (IRegisterDMContext register : registers) {
				MIRegisterDMC registerDmc = (MIRegisterDMC) register;
				registerNameMap.put(registerDmc.getName(), registerDmc);
			}

			return registerNameMap;
		}

		// Adjust water mark suffix used to suggest register group names
		private void updateGroupNameWaterMark(String groupName) {
			// check only for this name pattern
			String regex = "^group_(\\d*)$"; //$NON-NLS-1$
			// Normalize the name first
			String name = groupName.trim().toLowerCase();
			// tracking proposed group names e.d. Group_1, Group_2, etc..,
			// otherwise no need to update the water mark
			if (name.matches(regex)) {
				// Obtain the numerical suffix
				String number = name.replaceAll(regex, "$1"); //$NON-NLS-1$
				try {
					Integer nameSequence = Integer.valueOf(number);
					if (nameSequence > fGroupNameWaterMark.get()) {
						// The new value is bigger so lets move up the water mark
						fGroupNameWaterMark.set(nameSequence.intValue());
					}
				} catch (NumberFormatException e) {
					// Quite unlikely and only causing a possibility to
					// propose a group name that already exists.
				}
			}
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

		@SuppressWarnings("restriction")
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
				@SuppressWarnings("restriction")
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

		// The register groups information need to be build from GDB and user defined groups i.e. de-serialized
		// from the launch configuration.
		super.getRegisterGroups(ctx, new DataRequestMonitor<IRegisterGroupDMContext[]>(getExecutor(), rm) {
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
			final MIRegisterGroupDMC groupDmc = (MIRegisterGroupDMC) regGroupDmc;

			rm.setData(new IRegisterGroupDMData() {
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

			});

			// Make sure this group is available in the groups to registers map,
			// as this map provides the input to save /serialize the groups
			// The associated registers will be resolved upon request.
			if (fGroupToRegistersMap.get(groupDmc) == null) {
				fGroupToRegistersMap.put(groupDmc, new MIRegisterDMC[0]);
			}
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
					"Unable to resolve Group Data, Invalid Register Group provided", null)); //$NON-NLS-1$
		}

		rm.done();
	}

	@Override
	public void getRegisters(final IDMContext ctx, final DataRequestMonitor<IRegisterDMContext[]> rm) {
		final MIRegisterGroupDMC groupDmc = DMContexts.getAncestorOfType(ctx, MIRegisterGroupDMC.class);

		if (groupDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
					"Unable to get Registers, Invalid Register Group provided", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		// check if base registers have been loaded already
		MIRegisterDMC[] baseRegisters = fGroupToRegistersMap.get(groupDmc);
		if (baseRegisters != null && baseRegisters.length > 0) {
			// use baseRegisters to build registers associated to the given context
			buildGroupRegisters(ctx, baseRegisters, rm);
			return;
		}

		// RESOLVE THE ROOT GROUP FOR THE GIVEN CONTEXT

		// Validate group context initialization
		final IContainerDMContext containerDmc = DMContexts.getAncestorOfType(ctx, IContainerDMContext.class);
		final MIRegisterGroupDMC[] groupContexts = fContextToGroupsMap.get(containerDmc);
		if (groupContexts == null || groupContexts.length < 1) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
					"Register Group initialization problem, no groups are present", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		// Check if the General Register base from GDB is available
		// Get the General Group (root) at index 0
		final MIRegisterGroupDMC rootGroupContext = groupContexts[0];
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
			super.getRegisters(compCtx, new DataRequestMonitor<IRegisterDMContext[]>(getExecutor(), rm) {
				@Override
				@ConfinedToDsfExecutor("fExecutor")
				protected void handleSuccess() {
					IRegisterDMContext[] iregisters = getData();
					MIRegisterDMC[] registers = Arrays.copyOf(iregisters, iregisters.length, MIRegisterDMC[].class);

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

	@Override
	public void getRegisterNames(IRegisterDMContext[] regDmcArray, final DataRequestMonitor<IRegisterDMData[]> rm) {
		assert(regDmcArray != null);
		
		if (regDmcArray == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
					"Unable to get Registers Names, Illegal IRegisterDMContext[] provided", null)); //$NON-NLS-1$
			rm.done();
			return;
		}
		
		IRegisterDMData[] registerDataArray = new IRegisterDMData[regDmcArray.length];

		for (int i = 0; i < regDmcArray.length; i++) {
			IRegisterDMContext regDmc = regDmcArray[i];
			String regName = BLANK_STRING;
			
			assert(regDmc instanceof MIRegisterDMC);
			
			if (regDmc instanceof MIRegisterDMC) {
				regName = ((MIRegisterDMC) regDmc).getName();
			}

			IFrameDMContext frameCtx = DMContexts.getAncestorOfType(regDmc, IFrameDMContext.class);
			registerDataArray[i] = new RegisterData(frameCtx, regName, BLANK_STRING, false);
		}

		rm.setData(registerDataArray);
		rm.done();
	}

	@Override
	public void canAddRegisterGroup(IDMContext selectionContext, DataRequestMonitor<Boolean> rm) {
		// Not relevant checks at this point
		rm.setData(true);
		rm.done();
	}

	@Override
	public void addRegisterGroup(final String name, final IRegisterDMContext[] registers, RequestMonitor rm) {
		if (registers == null || registers.length < 1) {
			notifyUser(Messages.RegisterGroup_invalid_number_of_registers);
			rm.done();
			return;
		}

		if (name.trim().toLowerCase().equals(ROOT_GROUP_NAME.toLowerCase())) {
			notifyUser(NLS.bind(Messages.RegisterGroup_name_reserved, ROOT_GROUP_NAME));
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
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"Unable to add Register group, Invalid Container", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		// must be a child of an existing container, at least the root group must be present
		assert (fContextToGroupsMap.containsKey(contDmc));

		//create the new group
		MIRegisterGroupDMC group = new MIRegisterGroupDMC(this, contDmc, fGroupBookingCount, name);
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
		generateGroupsChangedEvent(group);
		rm.done();
	}

	@Override
	public void canEditRegisterGroup(IRegisterGroupDMContext group, DataRequestMonitor<Boolean> rm) {
		rm.setData(canEditRegisterGroup(group));
		rm.done();
	}

	@Override
	public void editRegisterGroup(IRegisterGroupDMContext group, String groupName, IRegisterDMContext[] iRegisters,
			RequestMonitor rm) {
		assert (groupName != null);

		if (iRegisters == null || iRegisters.length < 1) {
			notifyUser(Messages.RegisterGroup_invalid_number_of_registers);
			rm.done();
			return;
		}

		if (!canEditRegisterGroup(group)) {
			// Shall not happen as canEdit is expected to be called before edit
			rm.done();
			return;
		}

		MIRegisterGroupDMC miGroup = ((MIRegisterGroupDMC) group);
		// Make sure the new group name is not the reserved root group name
		if (groupName.trim().toLowerCase().equals(ROOT_GROUP_NAME.toLowerCase())) {
			notifyUser(NLS.bind(Messages.RegisterGroup_name_reserved, ROOT_GROUP_NAME));
			rm.done();
			return;
		}
		
		miGroup.setName(groupName);

		// transform to MIRegistersDMC[]
		MIRegisterDMC[] registers = arrangeRegisters(iRegisters);

		// preserve registers in a general format not associated to a specific frame
		registers = toBareRegisters(registers);
		fGroupToRegistersMap.put(miGroup, registers);
		// Create event notification
		DsfSession session = DsfSession.getSession(group.getSessionId());
		generateRegistersChangedEvent(miGroup, session);

		rm.done();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.dsf.debug.service.IRegisters2#removeRegisterGroups(org.eclipse.cdt.dsf.debug.service.IRegisters
	 * .IRegisterGroupDMContext[], org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void removeRegisterGroups(IRegisterGroupDMContext[] groups, RequestMonitor rm) {
		removeRegisterGroups(groups, false, rm);
	}

	@Override
	public void restoreDefaultGroups(final RequestMonitor rm) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.dsf.mi.service.MIRegisters#findRegisterGroup(org.eclipse.cdt.dsf.datamodel.IDMContext,
	 * java.lang.String, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	@Override
	public void findRegisterGroup(IDMContext ctx, String name, DataRequestMonitor<IRegisterGroupDMContext> rm) {
		IContainerDMContext contDmc = DMContexts.getAncestorOfType(ctx, IContainerDMContext.class);
		if (contDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"Container context not found", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

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

	@Override
	public void proposeGroupName(final DataRequestMonitor<String> rm) {
		// propose the next available group name
		rm.setData(PROPOSE_GROUP_NAME_ROOT + (fGroupNameWaterMark.get() + 1));
		rm.done();
	}

	@SuppressWarnings("restriction")
	public void save() {
		IRegisterGroupDescriptor[] groups = buildDescriptors();
		ILaunchConfiguration launchConfig = getLaunchConfig();
		if (launchConfig != null) {
			RegisterGroupsPersistance serializer = new RegisterGroupsPersistance(launchConfig);
			try {
				serializer.saveGroups(groups);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Cast to MI and sort them ascending order by register index
	 */
	private MIRegisterDMC[] arrangeRegisters(IRegisterDMContext[] iRegisters) {
		TreeMap<Integer, MIRegisterDMC> sortedRegisters = new TreeMap<Integer, MIRegisterDMC>();
		for (int i = 0; i < iRegisters.length; i++) {
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
			for (IRegisterGroupDMContext group : groups) {
				MIRegisterGroupDMC miGroup = (MIRegisterGroupDMC) group;
				if ((!removeRoot) && miGroup.getName().equals(ROOT_GROUP_NAME)) {
					// Skip removal of a root group, except when restoring to default groups
					continue;
				}

				final IContainerDMContext containerDmc = DMContexts.getAncestorOfType(group, IContainerDMContext.class);

				// All given groups are expected to be part of the same Container, however it's safer to create a new list
				// per context to cover the unsual case
				// This could be revisited in case there is performance concerns which does not seem an issue at this
				// point.
				MIRegisterGroupDMC[] groupsCtx = fContextToGroupsMap.get(containerDmc);
				List<MIRegisterGroupDMC> groupsList = new ArrayList<MIRegisterGroupDMC>(Arrays.asList(groupsCtx));

				// Removing a single group
				groupsList.remove(group);

				// Back to context map without the given group
				fContextToGroupsMap.put(containerDmc, groupsList.toArray(new MIRegisterGroupDMC[groupsList.size()]));
				// Now remove the group from the groups to registers map
				if (fGroupToRegistersMap.remove(group) != null) {
					// Create event notification
					generateGroupsChangedEvent((MIRegisterGroupDMC) group);
				}
			}
		}

		rm.done();
	}

	private void removeRegisterGroups(IDMContext containerDmc) {
		String sessionId = containerDmc.getSessionId();
		DsfSession session = DsfSession.getSession(sessionId);

		MIRegisterGroupDMC[] groups = fContextToGroupsMap.get(containerDmc);
		if (groups != null) {
			removeRegisterGroups(groups, true, new RequestMonitor(session.getExecutor(), null) {
			});
		}
	}

	private void notifyUser(String message) {
		IStatus status = new Status(IStatus.INFO, GdbPlugin.PLUGIN_ID, IGdbDebugConstants.STATUS_HANDLER_CODE,
				Messages.RegisterGroupInfo + ": " + message, null); //$NON-NLS-1$
		IStatusHandler statusHandler = DebugPlugin.getDefault().getStatusHandler(status);
		if (statusHandler != null) {
			try {
				statusHandler.handleStatus(status, null);
			} catch (CoreException ex) {
				GdbPlugin.getDefault().getLog().log(ex.getStatus());
			}
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

	@SuppressWarnings("restriction")
	private IRegisterGroupDescriptor[] buildDescriptors() {
		// use a tree map to sort the entries by group number
		TreeMap<Integer, MIRegisterGroupDMC> sortedGroups = new TreeMap<Integer, MIRegisterGroupDMC>();

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
		for (Iterator<Entry<Integer, MIRegisterGroupDMC>> iterator = groupSet.iterator(); iterator.hasNext();) {
			Entry<Integer, MIRegisterGroupDMC> entry = iterator.next();
			descriptors[i] = new RegisterGroupDescriptor(entry.getValue(), true);
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

	@SuppressWarnings("restriction")
	private MIRegisterGroupDMC[] readGroupsFromMemento(final IContainerDMContext contDmc) {
		RegisterGroupsPersistance deserializer = new RegisterGroupsPersistance(getLaunchConfig());
		IRegisterGroupDescriptor[] groupDescriptions = deserializer.parseGroups();

		List<MIRegisterGroupDMC> groups = new ArrayList<MIRegisterGroupDMC>();
		for (IRegisterGroupDescriptor group : groupDescriptions) {
			fGroupMementoDescriptorIndex.put(fGroupBookingCount, group);
			groups.add(new MIRegisterGroupDMC(this, contDmc, fGroupBookingCount, group.getName()));
			fGroupBookingCount++;
		}

		return groups.toArray(new MIRegisterGroupDMC[groups.size()]);
	}

	private void getUserGroupRegisters(IDMContext ctx, final DataRequestMonitor<IRegisterDMContext[]> rm) {
		final MIRegisterGroupDMC groupDmc = DMContexts.getAncestorOfType(ctx, MIRegisterGroupDMC.class);

		// Need to build the corresponding register[] from the memento descriptors
		@SuppressWarnings("restriction")
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
	@SuppressWarnings("restriction")
	private MIRegisterDMC[] resolveRegisters(IRegisterGroupDescriptor grpDescriptor, IDMContext ctx)
			throws CoreException {
		final List<MIRegisterDMC> registerContexts = new ArrayList<MIRegisterDMC>();
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
	
	private void generateGroupsChangedEvent(MIRegisterGroupDMC group) {
		// Create event notification
		DsfSession session = DsfSession.getSession(group.getSessionId());
		session.dispatchEvent(new GroupsChangedDMEvent(group), null);
	}

	@Override
	protected void generateRegisterChangedEvent(final IRegisterDMContext dmc ) {
		//notify the register value change
        getSession().dispatchEvent(new IRegisterChangedDMEvent() {
			
			@Override
			public IRegisterDMContext getDMContext() {
				return dmc;
			}
		}, getProperties());
        
        //Propagate notification to all groups.
        //A change of a single register needs to be propagated to all groups within the same Container/Process
        //I.e. Some registers are dependent on the value of others and these dependent registers could be 
        //associated to different groups.
    	IContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IContainerDMContext.class);
    	DsfSession session = DsfSession.getSession(dmc.getSessionId());
    	generateRegistersChangedEvent(containerDmc, session);
	}
	
	private void generateRegistersChangedEvent(IContainerDMContext containerDmc, final DsfSession session) {
		//resolve the groups for the current container (process) context 
		final MIRegisterGroupDMC[] groups = fContextToGroupsMap.get(containerDmc);
		
		//trigger notification to all groups in the container
		for (int i = 0; i < groups.length; i++) {
			//We need final locals variables from the loop. Use a method call for this
			generateRegistersChangedEvent(groups[i], session);
		}
	}

	private void generateRegistersChangedEvent(final MIRegisterGroupDMC groupDmc, final DsfSession session) {
		IRegistersChangedDMEvent event = new IRegistersChangedDMEvent() {
			@Override
			public IRegisterGroupDMContext getDMContext() {
				return groupDmc;
			}
		};
		
		session.dispatchEvent(event, getProperties());
	}


	/**
	 * Create Registers from specific execution context to a generic register context, e.g. not associated to a specific
	 * execution context.
	 */
	private MIRegisterDMC[] toBareRegisters(MIRegisterDMC[] registers) {
	
		MIRegisterDMC[] bareRegisters = new MIRegisterDMC[registers.length];
		for (int i = 0; i < registers.length; i++) {
			//only one parent i.e. group context
			MIRegisterGroupDMC groupDmc = DMContexts.getAncestorOfType(registers[i], MIRegisterGroupDMC.class);
			assert (groupDmc != null);
			bareRegisters[i] = new MIRegisterDMC(this, groupDmc, registers[i].getRegNo(), registers[i].getName());
		}
	
		return bareRegisters;
	}

	private void buildGroupRegisters(final IDMContext ctx, final MIRegisterDMC[] baseRegisters,
			final DataRequestMonitor<IRegisterDMContext[]> rm) {
		final MIRegisterGroupDMC groupDmc = DMContexts.getAncestorOfType(ctx, MIRegisterGroupDMC.class);
		
		assert(groupDmc != null);
		
		final IFrameDMContext frameDmc = DMContexts.getAncestorOfType(ctx, IFrameDMContext.class);
		if (frameDmc == null) {
			// The selection does not provide a specific frame, then resolve the top frame on the current thread
			// if the execution frame is not available proceed with no frame context i.e. will not be able to resolve values.
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
			//build to valid stack frame context
			for (int i = 0; i < registers.length; i++) {
				registers[i] = new MIRegisterDMC(this, groupDmc, frameDmc, baseRegisters[i].getRegNo(),
						baseRegisters[i].getName());
			}
		} else {
			//build with no execution context, normal case if a selection is pointing to 
			//e.g. a running thread, a process..  i.e. not able to associate register values. 
			for (int i = 0; i < registers.length; i++) {
				registers[i] = new MIRegisterDMC(this, groupDmc, baseRegisters[i].getRegNo(),
						baseRegisters[i].getName());
			}
		}
	
		// return the registers
		rm.setData(registers);
		rm.done();
	}

}
