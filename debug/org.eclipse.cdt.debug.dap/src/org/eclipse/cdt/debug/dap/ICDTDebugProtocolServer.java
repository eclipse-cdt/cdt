/*******************************************************************************
 * Copyright (c) 2019 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
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
