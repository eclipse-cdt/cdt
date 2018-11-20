/*******************************************************************************
 * Copyright (c) 2017 Renesas Electronics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bruno Medeiros (Renesas) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.cdt.debug.internal.core.model.IRegisterGroupDescriptor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIRegisters.MIRegisterDMC;
import org.eclipse.cdt.dsf.mi.service.MIRegisters.MIRegisterGroupDMC;
import org.eclipse.cdt.dsf.mi.service.command.MIControlDMContext;
import org.eclipse.core.runtime.Assert;
import org.junit.Test;

public class GDBRegisterTest extends CommonDsfTest {

	public static final String PROCESS_A = "processA";
	public static final String PROCESS_B = "processB";

	protected MIProcesses miProcesses;
	protected GDBRegisters gdbRegisters;

	@Override
	protected void doSetupSession() {
		super.doSetupSession();

		miProcesses = new MIProcesses(fSession);
		gdbRegisters = createGdbRegisters();
	}

	protected GDBRegisters createGdbRegisters() {
		return new GDBRegisters(fSession) {
			@Override
			protected boolean useProcessIdAsRegisterGroupPersistanceId() {
				return true;
			}
		};
	}

	@Test
	public void testRegisterPersistence() throws Exception {
		testRegisterPersistence$();
	}

	public void testRegisterPersistence$() throws Exception {

		MIControlDMContext controlDmc = new MIControlDMContext(fSession.getId(), "TestControl");

		IProcessDMContext processDmcA = miProcesses.createProcessContext(controlDmc, PROCESS_A);
		IProcessDMContext processDmcB = miProcesses.createProcessContext(controlDmc, PROCESS_B);
		IMIContainerDMContext containerA = miProcesses.createContainerContext(processDmcA, "containerA");
		IMIContainerDMContext containerB = miProcesses.createContainerContext(processDmcB, "containerB");

		MIRegisterGroupDMC[] initialRegisterGroups = gdbRegisters.readGroupsFromMemento(containerA);
		Assert.isTrue(initialRegisterGroups.length == 0);

		MIRegisterGroupDMC registerGroupA = addRegisterGroup(containerA, "RegGroupA", "register_foo");
		// check build descriptors
		IRegisterGroupDescriptor[] buildDescriptors = gdbRegisters.buildDescriptors();
		org.junit.Assert.assertEquals(buildDescriptors[0].getContainerId(),
				gdbRegisters.getPersistenceIdForRegisterGroupContainer(containerA));

		// Save then check persistence
		gdbRegisters.save();
		checkAfterAdding_GroupA(containerA, containerB, registerGroupA);

		// Now add a second register group to a different process context
		MIRegisterGroupDMC registerGroupB = addRegisterGroup(containerB, "RegGroupB", "register_bar");
		gdbRegisters.save();
		checkAfterAdding_GroupB(containerA, containerB, registerGroupA, registerGroupB);
	}

	protected void checkAfterAdding_GroupA(IMIContainerDMContext containerA, IMIContainerDMContext containerB,
			MIRegisterGroupDMC registerGroupA) {
		checkRegisterGroupMemento(containerA, registerGroupA);
		checkRegisterGroupsMemento(containerB, array());
	}

	protected void checkAfterAdding_GroupB(IMIContainerDMContext containerA, IMIContainerDMContext containerB,
			MIRegisterGroupDMC registerGroupA, MIRegisterGroupDMC registerGroupB) {
		checkRegisterGroupMemento(containerA, registerGroupA);
		checkRegisterGroupMemento(containerB, registerGroupB);
	}

	protected MIRegisterGroupDMC addRegisterGroup(IMIContainerDMContext container, String groupName,
			String registerName) {
		MIRegisterGroupDMC registerGroup = new MIRegisterGroupDMC(gdbRegisters, container, 1, groupName);
		MIRegisterDMC rgFoo = new MIRegisterDMC(gdbRegisters, registerGroup, 1, registerName);

		gdbRegisters.addRegisterGroup(container, registerGroup.getName(), array(rgFoo), newRequestMonitor());
		return registerGroup;
	}

	protected void checkRegisterGroupMemento(IMIContainerDMContext container, MIRegisterGroupDMC registerGroup) {
		checkRegisterGroupsMemento(container, array(registerGroup));
	}

	protected void checkRegisterGroupsMemento(IMIContainerDMContext container,
			MIRegisterGroupDMC[] expectedRegisterGroups) {
		MIRegisterGroupDMC[] savedRegisterGroups = gdbRegisters.readGroupsFromMemento(container);
		if (expectedRegisterGroups == null) {
			assertTrue(savedRegisterGroups == null);
			return;
		}

		assertTrue(expectedRegisterGroups.length == savedRegisterGroups.length);

		for (int ix = 0; ix < expectedRegisterGroups.length; ix++) {
			MIRegisterGroupDMC expectedRG = expectedRegisterGroups[ix];
			int groupNo = savedRegisterGroups[ix].getGroupNo(); // Don't check group number, so set expected to obtained value
			expectedRegisterGroups[ix] = new MIRegisterGroupDMC(gdbRegisters, container, groupNo, expectedRG.getName());
		}

		assertTrue(Arrays.equals(expectedRegisterGroups, savedRegisterGroups));
	}

	/* -----------------  ----------------- */

	/**
	 * Variant of {@link GDBRegisterTest} where register groups are saved without a container id.
	 * This is the default behavior for register group persistence.
	 */
	public static class GDBRegisterTest_NoContainerTest extends GDBRegisterTest {

		@Override
		protected GDBRegisters createGdbRegisters() {
			return new GDBRegisters(fSession) {
				@Override
				protected boolean useProcessIdAsRegisterGroupPersistanceId() {
					return false;
				}
			};
		}

		@Override
		protected void checkAfterAdding_GroupA(IMIContainerDMContext containerA, IMIContainerDMContext containerB,
				MIRegisterGroupDMC registerGroupA) {
			checkRegisterGroupMemento(containerA, registerGroupA);
			checkRegisterGroupMemento(containerB, registerGroupA);
		}

		@Override
		protected void checkAfterAdding_GroupB(IMIContainerDMContext containerA, IMIContainerDMContext containerB,
				MIRegisterGroupDMC registerGroupA, MIRegisterGroupDMC registerGroupB) {
			checkRegisterGroupsMemento(containerA, array(registerGroupA, registerGroupB));
			checkRegisterGroupsMemento(containerB, array(registerGroupA, registerGroupB));
		}
	}

	public static class GDBRegisterTest_WithAlternativeProcessIdTest extends GDBRegisterTest {

		@Override
		protected GDBRegisters createGdbRegisters() {
			return new GDBRegisters(fSession) {
				@Override
				protected boolean useProcessIdAsRegisterGroupPersistanceId() {
					return true;
				}

				@Override
				protected String getPersistenceIdForRegisterGroupContainer(IContainerDMContext contDmc) {
					return super.getPersistenceIdForRegisterGroupContainer(contDmc) + "XXX";
				}
			};
		}

	}

	public static class GDBRegisterTest_WithContainerDMContextTest extends GDBRegisterTest {

		@Override
		protected GDBRegisters createGdbRegisters() {
			return new GDBRegisters(fSession) {
				@Override
				protected String getPersistenceIdForRegisterGroupContainer(IContainerDMContext contDmc) {
					IMIContainerDMContext contextDmc = DMContexts.getAncestorOfType(contDmc,
							IMIContainerDMContext.class);
					return contextDmc.getGroupId();
				}
			};
		}

	}
}