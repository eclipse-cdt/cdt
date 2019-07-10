package org.eclipse.cdt.debug.dap;

import java.util.concurrent.CompletableFuture;

import org.eclipse.cdt.debug.dap.CDTDebugProtocol.MemoryContents;
import org.eclipse.cdt.debug.dap.CDTDebugProtocol.MemoryRequestArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

public interface ICDTDebugProtocolServer extends IDebugProtocolServer {

	/**
	 * Request a memory block
	 */
	@JsonRequest(value = "cdt-gdb-adapter/Memory")
	default CompletableFuture<MemoryContents> memory(MemoryRequestArguments args) {
		throw new UnsupportedOperationException();
	}

}
