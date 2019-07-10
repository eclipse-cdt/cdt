package org.eclipse.cdt.debug.dap;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.UnaryOperator;

import org.eclipse.cdt.debug.dap.CDTDebugProtocol.MemoryContents;
import org.eclipse.cdt.debug.dap.CDTDebugProtocol.MemoryRequestArguments;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.lsp4e.debug.debugmodel.DSPDebugTarget;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.debug.DebugLauncher;

public class DapDebugTarget extends DSPDebugTarget implements IMemoryBlockRetrievalExtension, ICDTDebugProtocolClient {

	public DapDebugTarget(ILaunch launch, Runnable processCleanup, InputStream in, OutputStream out,
			Map<String, Object> dspParameters) {
		super(launch, processCleanup, in, out, dspParameters);
	}

	@Override
	protected Launcher<? extends IDebugProtocolServer> createLauncher(UnaryOperator<MessageConsumer> wrapper,
			InputStream in, OutputStream out, ExecutorService threadPool) {
		Launcher<ICDTDebugProtocolServer> debugProtocolLauncher = DebugLauncher.createLauncher(this,
				ICDTDebugProtocolServer.class, in, out, threadPool, wrapper);
		return debugProtocolLauncher;
	}

	@Override
	public ICDTDebugProtocolServer getDebugProtocolServer() {
		return (ICDTDebugProtocolServer) super.getDebugProtocolServer();
	}

	@Override
	public boolean supportsStorageRetrieval() {
		return true;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, DebugException.NOT_SUPPORTED,
				"getMemoryBlock() not supported, use getExtendedMemoryBlock()", null)); //$NON-NLS-1$
	}

	@Override
	public IMemoryBlockExtension getExtendedMemoryBlock(String expression, Object context) throws DebugException {
		BigInteger bigBaseAddress;
		/*
		 * See if the expression is a simple numeric value; if it is, we can
		 * avoid some costly processing (calling the back-end to resolve the
		 * expression and obtain an address)
		 */
		try {
			// Now, try to parse the expression. If a NumberFormatException is
			// thrown, then it wasn't a simple numerical expression and we give
			// up as debug adapter hasn't provided us what was needed
			bigBaseAddress = BigInteger.valueOf(Long.decode(expression));

		} catch (NumberFormatException nfexc) {
			MemoryRequestArguments memoryRequestArguments = new MemoryRequestArguments();
			memoryRequestArguments.setAddress(expression);
			memoryRequestArguments.setLength(1L);
			CompletableFuture<MemoryContents> memory = getDebugProtocolServer().memory(memoryRequestArguments);
			MemoryContents body = complete(memory);
			String address = body.getAddress();
			try {
				bigBaseAddress = BigInteger.valueOf(Long.decode(address));
			} catch (NumberFormatException e) {
				// still no resolvable address
				return null;
			}
		}
		return new MemoryBlock(this, expression, bigBaseAddress, context);
	}

}
