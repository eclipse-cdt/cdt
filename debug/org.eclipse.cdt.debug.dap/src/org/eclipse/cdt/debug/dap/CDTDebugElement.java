package org.eclipse.cdt.debug.dap;

import org.eclipse.lsp4e.debug.debugmodel.DSPDebugElement;
import org.eclipse.lsp4e.debug.debugmodel.DSPDebugTarget;

public class CDTDebugElement extends DSPDebugElement {
	public CDTDebugElement(DSPDebugTarget target) {
		super(target);
	}

	@Override
	public ICDTDebugProtocolServer getDebugProtocolServer() {
		// TODO Auto-generated method stub
		return (ICDTDebugProtocolServer) super.getDebugProtocolServer();
	}
}
