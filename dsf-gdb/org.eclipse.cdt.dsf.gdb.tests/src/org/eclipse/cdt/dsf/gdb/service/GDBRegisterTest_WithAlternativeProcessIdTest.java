package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;

public class GDBRegisterTest_WithAlternativeProcessIdTest extends GDBRegisterTest {

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