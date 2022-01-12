package org.eclipse.cdt.debug.dap;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.lsp4e.debug.debugmodel.DSPStackFrame;

public class DapDisassemblyBackendFactory implements IAdapterFactory {

	private static final Class<?>[] ADAPTERS = { IDisassemblyBackend.class };

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (IDisassemblyBackend.class.equals(adapterType)) {
			if (adaptableObject instanceof DSPStackFrame) {
				DSPStackFrame dspDebugElement = (DSPStackFrame) adaptableObject;
				if (dspDebugElement.getDebugTarget() instanceof DapDebugTarget) {
					return (T) new DapDisassemblyBackend();
				}
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTERS;
	}
}
