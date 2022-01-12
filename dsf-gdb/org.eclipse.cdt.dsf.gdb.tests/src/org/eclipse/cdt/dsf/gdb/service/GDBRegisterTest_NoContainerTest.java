package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.MIRegisters.MIRegisterGroupDMC;

/**
 * Variant of {@link GDBRegisterTest} where register groups are saved without a container id.
 * This is the default behavior for register group persistence.
 */
public class GDBRegisterTest_NoContainerTest extends GDBRegisterTest {

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