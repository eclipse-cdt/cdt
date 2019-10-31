package org.eclipse.cdt.debug.dap;

import static org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils.DEBUG;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.dap.CDTDebugProtocol.CDTDisassembleArguments;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AbstractDisassemblyBackend;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyUtils;
import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.ErrorPosition;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.lsp4e.debug.debugmodel.DSPDebugTarget;
import org.eclipse.lsp4e.debug.debugmodel.DSPStackFrame;
import org.eclipse.lsp4j.debug.DisassembleResponse;
import org.eclipse.lsp4j.debug.DisassembledInstruction;
import org.eclipse.lsp4j.debug.Source;

@SuppressWarnings("restriction")
public class DapDisassemblyBackend extends AbstractDisassemblyBackend {

	private DSPStackFrame dspStackFrame;

	@Override
	public boolean supportsDebugContext(IAdaptable context) {
		return context instanceof DSPStackFrame;
	}

	@Override
	public boolean hasDebugContext() {
		return dspStackFrame != null;
	}

	@Override
	public SetDebugContextResult setDebugContext(IAdaptable context) {
		assert context instanceof DSPStackFrame;
		SetDebugContextResult setDebugContextResult = new SetDebugContextResult();
		if (context instanceof DSPStackFrame) {

			DSPStackFrame newDspStackFrame = (DSPStackFrame) context;
			setDebugContextResult.contextChanged = !newDspStackFrame.equals(dspStackFrame);
			dspStackFrame = newDspStackFrame;
			// sessionId should have been boolean and been hasSessionId, only null/non-null is relevant
			setDebugContextResult.sessionId = ""; //$NON-NLS-1$
			if (!setDebugContextResult.contextChanged) {
				fCallback.gotoFrameIfActive(dspStackFrame.getDepth());
			}
		} else {
			setDebugContextResult.contextChanged = true;
			setDebugContextResult.sessionId = null;
		}

		return setDebugContextResult;
	}

	@Override
	public void clearDebugContext() {
		dspStackFrame = null;
	}

	@Override
	public void retrieveFrameAddress(int frame) {
		fCallback.setUpdatePending(false);
		fCallback.asyncExec(() -> {
			int addressBits = dspStackFrame.getFrameInstructionAddressBits();
			BigInteger address = dspStackFrame.getFrameInstructionAddress();
			if (addressBits != fCallback.getAddressSize()) {
				fCallback.addressSizeChanged(addressBits);
			}
			if (frame == 0) {
				fCallback.updatePC(address);
			} else {
				fCallback.gotoFrame(frame, address);
			}
		});
	}

	@Override
	public int getFrameLevel() {
		return dspStackFrame.getDepth();
	}

	@Override
	public boolean isSuspended() {
		return dspStackFrame.getDebugTarget().isSuspended();
	}

	@Override
	public boolean hasFrameContext() {

		return false;
	}

	@Override
	public String getFrameFile() {

		return null;
	}

	@Override
	public int getFrameLine() {

		return 0;
	}

	/**
	 * Retrieves disassembly based on either (a) start and end address range, or
	 * (b) file, line number, and line count. If the caller specifies both sets
	 * of information, the implementation should honor (b) and ignore (a).
	*/
	@Override
	public void retrieveDisassembly(BigInteger startAddress, BigInteger endAddress, String file, int lineNumber,
			int lines, boolean mixed, boolean showSymbols, boolean showDisassembly, int linesHint) {
		CDTDisassembleArguments args = new CDTDisassembleArguments();
		args.setMemoryReference("0x" + startAddress.toString(16)); //$NON-NLS-1$
		args.setInstructionCount((long) lines);
		args.setEndMemoryReference("1+0x" + endAddress.toString(16)); //$NON-NLS-1$
		CompletableFuture<DisassembleResponse> future = dspStackFrame.getDebugProtocolServer().disassemble(args);
		future.thenAcceptAsync(res -> {
			fCallback.asyncExec(() -> insertDisassembly(startAddress, endAddress, res, showSymbols, showDisassembly));
		});
	}

	/**
	 * @param startAddress
	 *            an address the caller is hoping will be covered by this
	 *            insertion. I.e., [mixedInstructions] may or may not contain
	 *            that address; the caller wants to know if it does, and so we
	 *            indicate that via our return value. Can be null to indicate n/a,
	 *            in which case we return true as long as any instruction was inserted
	 *            as long as any instruction was inserted
	 * @param endAddress
	 *            cut-off address. Any elements in [mixedInstructions] that
	 *            extend beyond this address are ignored.
	 * @param mixedInstructions
	 * @param showSymbols
	 * @param showDisassembly
	 * @return whether [startAddress] was inserted
	 */
	private void insertDisassembly(BigInteger startAddress, BigInteger endAddress, DisassembleResponse response,
			boolean showSymbols, boolean showDisassembly) {
		if (!fCallback.hasViewer() || dspStackFrame == null) {
			if (DEBUG) {
				System.out.println(
						MessageFormat.format("insertDisassembly ignored at {0} : missing context: [dspStackFrame={1}]", //$NON-NLS-1$
								DisassemblyUtils.getAddressText(startAddress), dspStackFrame));
			}
			if (dspStackFrame == null) {
				fCallback.setUpdatePending(false);
			}
			return;
		}
		if (DEBUG)
			System.out.println("insertDisassembly " + DisassemblyUtils.getAddressText(startAddress)); //$NON-NLS-1$
		boolean updatePending = fCallback.getUpdatePending();
		assert updatePending;
		if (!updatePending) {
			// safe-guard in case something weird is going on
			return;
		}

		boolean insertedAnyAddress = false;
		try {
			fCallback.lockScroller();

			AddressRangePosition p = null;
			Source location = null;
			DisassembledInstruction[] instructions = response.getInstructions();
			for (int i = 0; i < instructions.length; ++i) {
				DisassembledInstruction instruction = instructions[i];
				if (instruction.getLocation() != null) {
					location = instruction.getLocation();
				}
				assert location != null;
				String file = null;
				if (location != null) {
					file = location.getPath();
				}
				Long line = instruction.getLine();
				int lineNumber = (line == null ? 0 : line.intValue()) - 1;
				BigInteger address = getAddress(instruction);
				if (startAddress == null) {
					startAddress = address;
					fCallback.setGotoAddressPending(address);
				}
				if (p == null || !p.containsAddress(address)) {
					p = fCallback.getPositionOfAddress(address);
				}
				if (p instanceof ErrorPosition && p.fValid) {
					p.fValid = false;
					fCallback.getDocument().addInvalidAddressRange(p);
				} else if (p == null || address.compareTo(endAddress) > 0) {
					if (DEBUG)
						System.out.println("Excess disassembly lines at " + DisassemblyUtils.getAddressText(address)); //$NON-NLS-1$
					return;
				} else if (p.fValid) {
					if (DEBUG)
						System.out.println("Excess disassembly lines at " + DisassemblyUtils.getAddressText(address)); //$NON-NLS-1$
					if (!p.fAddressOffset.equals(address)) {
						// override probably unaligned disassembly
						p.fValid = false;
						fCallback.getDocument().addInvalidAddressRange(p);
					} else {
						continue;
					}
				}
				boolean hasSource = false;
				if (file != null && lineNumber >= 0) {
					p = fCallback.insertSource(p, address, file, lineNumber);
					hasSource = fCallback.getStorageForFile(file) != null;
				}
				// insert symbol label
				String functionName;
				int offset;
				String symbol = instruction.getSymbol();
				if (symbol != null) {
					String[] split = symbol.split("\\+", 2); //$NON-NLS-1$
					functionName = split[0];
					if (split.length > 1) {
						try {
							offset = Integer.parseInt(split[1]);
						} catch (NumberFormatException e) {
							offset = 0;
						}
					} else {
						offset = 0;
					}
				} else {
					functionName = null;
					offset = 0;
				}
				if (functionName != null && !functionName.isEmpty() && offset == 0) {
					p = fCallback.getDocument().insertLabel(p, address, functionName,
							showSymbols && (!hasSource || showDisassembly));
				}
				// determine instruction byte length
				BigInteger instrLength = null;
				if (i < instructions.length - 1) {
					instrLength = getAddress(instructions[i + 1]).subtract(address).abs();
				} else {
					// cannot determine length of last instruction
					break;
				}
				String funcOffset = instruction.getSymbol();
				if (funcOffset == null) {
					funcOffset = ""; //$NON-NLS-1$
				}

				BigInteger opCodes = null;
				if (instruction.getInstructionBytes() != null) {
					opCodes = new BigInteger(instruction.getInstructionBytes().replace(" ", ""), 16); //$NON-NLS-1$//$NON-NLS-2$
				}

				p = fCallback.getDocument().insertDisassemblyLine(p, address, instrLength.intValue(), funcOffset,
						opCodes, instruction.getInstruction(), file, lineNumber);
				if (p == null) {
					break;
				}
				insertedAnyAddress = true;

			}

		} catch (BadLocationException e) {
			// should not happen
			DisassemblyUtils.internalError(e);
		} finally {
			fCallback.setUpdatePending(false);
			if (insertedAnyAddress) {
				fCallback.updateInvalidSource();
				fCallback.unlockScroller();
				fCallback.doPending();
				fCallback.updateVisibleArea();
			} else {
				fCallback.unlockScroller();
			}
		}
	}

	private BigInteger getAddress(DisassembledInstruction instruction) {
		if (instruction.getAddress().startsWith("0x")) { //$NON-NLS-1$
			return new BigInteger(instruction.getAddress().substring(2), 16);
		} else {
			return new BigInteger(instruction.getAddress(), 10);
		}
	}

	@Override
	public Object insertSource(Position pos, BigInteger address, String file, int lineNumber) {
		ISourceLookupDirector lookupDirector = getSourceLookupDirector();
		return lookupDirector.getSourceElement(file);
	}

	private ISourceLookupDirector getSourceLookupDirector() {
		if (dspStackFrame == null) {
			return null;
		}
		DSPDebugTarget debugTarget = dspStackFrame.getDebugTarget();
		if (debugTarget == null) {
			return null;
		}
		ILaunch launch = debugTarget.getLaunch();
		if (launch == null) {
			return null;
		}
		ISourceLocator sourceLocator = launch.getSourceLocator();
		if (sourceLocator instanceof ISourceLookupDirector) {
			ISourceLookupDirector lookupDirector = (ISourceLookupDirector) sourceLocator;
			return lookupDirector;
		}
		return null;
	}

	@Override
	public void gotoSymbol(String symbol) {
		String.class.getClass();

	}

	@Override
	public void retrieveDisassembly(String file, int lines, BigInteger endAddress, boolean mixed, boolean showSymbols,
			boolean showDisassembly) {
		String.class.getClass();

	}

	@Override
	public String evaluateExpression(String expression) {
		CompletableFuture<IVariable> evaluate = dspStackFrame.evaluate(expression);
		try {
			IVariable iVariable = evaluate.get();
			return iVariable.getValue().getValueString();
		} catch (InterruptedException e) {
			return null;
		} catch (ExecutionException | DebugException | NumberFormatException e) {
			return null;
		}
	}

	@Override
	public void dispose() {
		String.class.getClass();

	}

	@Override
	protected void handleError(IStatus status) {
		Activator.log(status);
	}

	@Override
	public BigInteger evaluateAddressExpression(String expression, boolean suppressError) {
		CompletableFuture<IVariable> evaluate = dspStackFrame.evaluate(expression);
		try {
			IVariable variable = evaluate.get();
			return DisassemblyUtils.decodeAddress(variable.getValue().getValueString());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} catch (ExecutionException | DebugException | NumberFormatException e) {
			if (!suppressError) {
				DapDisassemblyBackend.this.handleError(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
						"Expression does not evaluate to an address (" //$NON-NLS-1$
								+ e.getMessage() + ")", //$NON-NLS-1$
						null));
			}
			return null;
		}

	}
}
