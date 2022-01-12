package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;

public class GDBRegisterTest_WithContainerDMContextTest extends GDBRegisterTest {

	@Override
	protected GDBRegisters createGdbRegisters() {
		return new GDBRegisters(fSession) {
			@Override
			protected String getPersistenceIdForRegisterGroupContainer(IContainerDMContext contDmc) {
				IMIContainerDMContext contextDmc = DMContexts.getAncestorOfType(contDmc, IMIContainerDMContext.class);
				return contextDmc.getGroupId();
			}
		};
	}

}