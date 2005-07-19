/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.cdt.debug.mi.core.command.Command;
import org.eclipse.cdt.debug.mi.core.command.MIExecContinue;
import org.eclipse.cdt.debug.mi.core.command.MIExecFinish;
import org.eclipse.cdt.debug.mi.core.command.MIExecNext;
import org.eclipse.cdt.debug.mi.core.command.MIExecNextInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecReturn;
import org.eclipse.cdt.debug.mi.core.command.MIExecStep;
import org.eclipse.cdt.debug.mi.core.command.MIExecStepInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecUntil;
import org.eclipse.cdt.debug.mi.core.command.MIInterpreterExecConsole;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointHitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIErrorEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIFunctionFinishedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorSignalExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MILocationReachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRunningEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibEvent;
import org.eclipse.cdt.debug.mi.core.event.MISignalEvent;
import org.eclipse.cdt.debug.mi.core.event.MISteppingRangeEvent;
import org.eclipse.cdt.debug.mi.core.event.MIStoppedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointScopeEvent;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointTriggerEvent;
import org.eclipse.cdt.debug.mi.core.output.MIAsyncRecord;
import org.eclipse.cdt.debug.mi.core.output.MIConsoleStreamOutput;
import org.eclipse.cdt.debug.mi.core.output.MIConst;
import org.eclipse.cdt.debug.mi.core.output.MIExecAsyncOutput;
import org.eclipse.cdt.debug.mi.core.output.MILogStreamOutput;
import org.eclipse.cdt.debug.mi.core.output.MINotifyAsyncOutput;
import org.eclipse.cdt.debug.mi.core.output.MIOOBRecord;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIParser;
import org.eclipse.cdt.debug.mi.core.output.MIResult;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;
import org.eclipse.cdt.debug.mi.core.output.MIStatusAsyncOutput;
import org.eclipse.cdt.debug.mi.core.output.MIStreamRecord;
import org.eclipse.cdt.debug.mi.core.output.MITargetStreamOutput;
import org.eclipse.cdt.debug.mi.core.output.MIValue;

/**
 * Receiving thread of gdb response output.
 */
public class RxThread extends Thread {

	final MISession session;
	List oobList;
	CLIProcessor cli;
	int prompt = 1; // 1 --> Primary prompt "(gdb)"; 2 --> Secondary Prompt ">"
	boolean fEnableConsole = true;

	public RxThread(MISession s) {
		super("MI RX Thread"); //$NON-NLS-1$
		session = s;
		cli = new CLIProcessor(session);
		oobList = new ArrayList();
	}

	/*
	 * Get the response, parse the output, dispatch for OOB
	 * search for the corresponding token in rxQueue for the ResultRecord.
	 */
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(session.getChannelInputStream()));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				// TRACING: print the output.
				if (MIPlugin.getDefault().isDebugging()) {
					MIPlugin.getDefault().debugLog(line);
				}
				setPrompt(line);
				processMIOutput(line + "\n"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
		// This code should be executed when gdb been abruptly
		// or unxepectedly killed.  This is detected by checking
		// if the channelInputStream is not null.  In normal case
		// session.terminate() will set the channelInputStream to null.
		if (session.getChannelInputStream() != null) {
			Runnable cleanup = new Runnable() {
				public void run() {
					// Change the state of the inferior.
					session.getMIInferior().setTerminated();
					session.terminate();
				}
			};
			Thread clean = new Thread(cleanup, "GDB Died"); //$NON-NLS-1$
			clean.setDaemon(true);
			clean.start();
		}
		// Clear the queue and notify any command waiting, we are going down.
		CommandQueue rxQueue = session.getRxQueue();
		if (rxQueue != null) {
			Command[] cmds = rxQueue.clearCommands();
			for (int i = 0; i < cmds.length; i++) {
				synchronized (cmds[i]) {
					cmds[i].notifyAll();
				}
			}
		}
	}

	void setPrompt(String line) {
		line = line.trim();
		MIParser parser = session.getMIParser();
		if (line.equals(parser.primaryPrompt)) {
			prompt = 1;
		} else if (line.equals(parser.secondaryPrompt)) {
			prompt = 2;
		} else {
			prompt = 0;
		}
	}

	public boolean inPrimaryPrompt() {
		return prompt == 1;
	}

	public boolean inSecondaryPrompt() {
		return prompt == 2;
	}

	public void setEnableConsole(boolean enable) {
		fEnableConsole = enable;
	}

	public boolean isEnableConsole() {
		return fEnableConsole;
	}

	/**
	 * Search for the command in the RxQueue, set the MIOutput
	 * and notify() the other end.
	 * Any OOBs are consider like event and dipatch to the
	 * listeners/observers in different thread.
	 */
	void processMIOutput(String buffer) {
		MIOutput response = session.parse(buffer);
		if (response != null) {
			List list = new ArrayList();
			CommandQueue rxQueue = session.getRxQueue();

			MIResultRecord rr = response.getMIResultRecord();
			if (rr != null) {
				int id = rr.getToken();
				Command cmd = rxQueue.removeCommand(id);

				// Clear the accumulate oobList on each new Result Command
				// response.
				MIOOBRecord[] oobRecords = new MIOOBRecord[oobList.size()];
				oobList.toArray(oobRecords);

				// Check if the state changed.
				String state = rr.getResultClass();
				if ("running".equals(state)) { //$NON-NLS-1$
					int type = 0;
					// Check the type of command
					// if it was a step instruction set state stepping
					if (cmd instanceof MIExecNext) {
						type = MIRunningEvent.NEXT;
					} else if (cmd instanceof MIExecNextInstruction) {
						type = MIRunningEvent.NEXTI;
					} else if (cmd instanceof MIExecStep) {
						type = MIRunningEvent.STEP;
					} else if (cmd instanceof MIExecStepInstruction) {
						type = MIRunningEvent.STEPI;
					} else if (cmd instanceof MIExecUntil) {
						type = MIRunningEvent.UNTIL;
					} else if (cmd instanceof MIExecFinish) {
						type = MIRunningEvent.FINISH;
					} else if (cmd instanceof MIExecReturn) {
						type = MIRunningEvent.RETURN;
					} else if (cmd instanceof MIExecContinue) {
						type = MIRunningEvent.CONTINUE;
					} else {
						type = MIRunningEvent.CONTINUE;
					}
					session.getMIInferior().setRunning();
					MIEvent event = new MIRunningEvent(session, id, type);
					list.add(event);
				} else if ("exit".equals(state)) { //$NON-NLS-1$
					// No need to do anything, terminate() will.
					session.getMIInferior().setTerminated();
				} else if ("connected".equals(state)) { //$NON-NLS-1$
					session.getMIInferior().setConnected();
				} else if ("error".equals(state)) { //$NON-NLS-1$
					if (session.getMIInferior().isRunning()) {
						session.getMIInferior().setSuspended();
						MIEvent event = new MIErrorEvent(session, rr, oobRecords);
						list.add(event);
					}
				} else if ("done".equals(state) && cmd instanceof CLICommand) { //$NON-NLS-1$
					// "done" usually mean that gdb returns after some CLI command
					// The result record may contains informaton specific to oob.
					// This will happen when CLI-Command is use, for example
					// doing "run" will block and return a breakpointhit
					processMIOOBRecord(rr, list);
				}

				// Notify the waiting command.
				// Notify any command waiting for a ResultRecord.
				if (cmd != null) {
					// Process the Command line to recognise patterns we may need to fire event.
					if (cmd instanceof CLICommand) {
						cli.processSettingChanges((CLICommand)cmd);
					} else if (cmd instanceof MIInterpreterExecConsole) {
						cli.processSettingChanges((MIInterpreterExecConsole)cmd);
					}

					synchronized (cmd) {
						// Set the accumulate console Stream
						response.setMIOOBRecords(oobRecords);
						cmd.setMIOutput(response);
						cmd.notifyAll();
					}
				}

				// Clear the accumulate oobList on each new Result Command response.
				oobList.clear();

			} else {

				// Process OOBs
				MIOOBRecord[] oobs = response.getMIOOBRecords();
				for (int i = 0; i < oobs.length; i++) {
					processMIOOBRecord(oobs[i], list);
				}
			}

			MIEvent[] events = (MIEvent[]) list.toArray(new MIEvent[list.size()]);
			session.fireEvents(events);
		} // if response != null
	}

	/**
	 * Dispatch a thread to deal with the listeners.
	 */
	void processMIOOBRecord(MIOOBRecord oob, List list) {
		if (oob instanceof MIAsyncRecord) {
			processMIOOBRecord((MIAsyncRecord) oob, list);
			oobList.clear();
		} else if (oob instanceof MIStreamRecord) {
			processMIOOBRecord((MIStreamRecord) oob);
		}
	}

	void processMIOOBRecord(MIAsyncRecord async, List list) {
		if (async instanceof MIExecAsyncOutput) {
			MIExecAsyncOutput exec = (MIExecAsyncOutput) async;
			// Change of state.
			String state = exec.getAsyncClass();
			if ("stopped".equals(state)) { //$NON-NLS-1$
				MIResult[] results = exec.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					MIValue val = results[i].getMIValue();
					if (var.equals("reason")) { //$NON-NLS-1$
						if (val instanceof MIConst) {
							String reason = ((MIConst) val).getString();
							MIEvent e = createEvent(reason, exec);
							if (e != null) {
								list.add(e);
							}
						}
					}
				}

				// GDB does not have reason when stopping on shared, hopefully
				// this will be fix in newer version meanwhile, we will use a hack
				// to cope.  On most platform we can detect by looking at the
				// console stream for phrase:
				// 	~"Stopped due to shared library event\n"
				//
				// Althought it is a _real_ bad idea to do this, we do not have
				// any other alternatives.
				if (list.isEmpty()) {
					String[] logs = getStreamRecords();
					for (int i = 0; i < logs.length; i++) {
						if (logs[i].equalsIgnoreCase("Stopped due to shared library event")) { //$NON-NLS-1$
							session.getMIInferior().setSuspended();
							MIEvent e = new MISharedLibEvent(session, exec);
							list.add(e);
						}
					}
				}

				// We were stopped for some unknown reason, for example
				// GDB for temporary breakpoints will not send the
				// "reason" ??? still fire a stopped event.
				if (list.isEmpty()) {
					session.getMIInferior().setSuspended();
					MIEvent e = new MIStoppedEvent(session, exec);
					list.add(e);
				}
			}
		} else if (async instanceof MIStatusAsyncOutput) {
			// Nothing done .. but what about +download??
		} else if (async instanceof MINotifyAsyncOutput) {
			// Nothing
		}
	}

	void processMIOOBRecord(MIStreamRecord stream) {
		if (stream instanceof MIConsoleStreamOutput) {
			OutputStream console = session.getConsolePipe();
			if (console != null) {
				MIConsoleStreamOutput out = (MIConsoleStreamOutput) stream;
				String str = out.getString();
				// Process the console stream too.
				setPrompt(str);
				if (str != null && isEnableConsole()) {
					try {
						console.write(str.getBytes());
						console.flush();
					} catch (IOException e) {
					}
				}
			}
			// Accumulate the Console Stream Output response for parsing.
			// Some commands will put valuable info  in the Console Stream.
			oobList.add(stream);
		} else if (stream instanceof MITargetStreamOutput) {
			OutputStream target = session.getMIInferior().getPipedOutputStream();
			if (target != null) {
				MITargetStreamOutput out = (MITargetStreamOutput) stream;
				String str = out.getString();
				if (str != null) {
					try {
						target.write(str.getBytes());
						target.flush();
					} catch (IOException e) {
					}
				}
			}
		} else if (stream instanceof MILogStreamOutput) {
			// This is meant for the gdb console.
			OutputStream log = session.getLogPipe();
			if (log != null) {
				MILogStreamOutput out = (MILogStreamOutput) stream;
				String str = out.getString();
				if (str != null && isEnableConsole()) {
					try {
						log.write(str.getBytes());
						log.flush();
					} catch (IOException e) {
					}
				}
			}
			// Accumulate the Log Stream Output response for parsing.
			// Some commands will put valuable info  in the Log Stream.
			oobList.add(stream);
		}
	}

	/**
	 * Check for any info that we can gather form the console.
	 */
	void processMIOOBRecord(MIResultRecord rr, List list) {
		MIResult[] results = rr.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			if (var.equals("reason")) { //$NON-NLS-1$
				MIValue value = results[i].getMIValue();
				if (value instanceof MIConst) {
					String reason = ((MIConst) value).getString();
					MIEvent event = createEvent(reason, rr);
					if (event != null) {
						list.add(event);
					}
				}
			}
		}
		// GDB does not have reason when stopping on shared, hopefully
		// this will be fix in newer version meanwhile, we will use a hack
		// to cope.  On most platform we can detect this state by looking at the
		// console stream for the phrase:
		// 	~"Stopped due to shared library event\n"
		//
		// Althought it is a _real_ bad idea to do this, we do not have
		// any other alternatives.
		if (list.isEmpty()) {
			String[] logs = getStreamRecords();
			for (int i = 0; i < logs.length; i++) {
				if (logs[i].equalsIgnoreCase("Stopped due to shared library event")) { //$NON-NLS-1$
					session.getMIInferior().setSuspended();
					MIEvent e = new MISharedLibEvent(session, rr);
					list.add(e);
				}
			}
		}
		// We were stopped for some unknown reason, for example
		// GDB for temporary breakpoints will not send the
		// "reason" ??? still fire a stopped event.
		if (list.isEmpty()) {
			if (session.getMIInferior().isRunning()) {
				session.getMIInferior().setSuspended();
				MIEvent event = new MIStoppedEvent(session, rr);
				session.fireEvent(event);
			}
		}
	}

	MIEvent createEvent(String reason, MIExecAsyncOutput exec) {
		return createEvent(reason, null, exec);
	}

	MIEvent createEvent(String reason, MIResultRecord rr) {
		return createEvent(reason, rr, null);
	}

	MIEvent createEvent(String reason, MIResultRecord rr, MIExecAsyncOutput exec) {
		MIEvent event = null;
		if ("breakpoint-hit".equals(reason)) { //$NON-NLS-1$
			if (exec != null) {
				event = new MIBreakpointHitEvent(session, exec);
			} else if (rr != null) {
				event = new MIBreakpointHitEvent(session, rr);
			}
			session.getMIInferior().setSuspended();
		} else if (
			"watchpoint-trigger".equals(reason) //$NON-NLS-1$
				|| "read-watchpoint-trigger".equals(reason) //$NON-NLS-1$
				|| "access-watchpoint-trigger".equals(reason)) { //$NON-NLS-1$
			if (exec != null) {
				event = new MIWatchpointTriggerEvent(session, exec);
			} else if (rr != null) {
				event = new MIWatchpointTriggerEvent(session, rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("watchpoint-scope".equals(reason)) { //$NON-NLS-1$
			if (exec != null) {
				event = new MIWatchpointScopeEvent(session, exec);
			} else if (rr != null) {
				event = new MIWatchpointScopeEvent(session, rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("end-stepping-range".equals(reason)) { //$NON-NLS-1$
			if (exec != null) {
				event = new MISteppingRangeEvent(session, exec);
			} else if (rr != null) {
				event = new MISteppingRangeEvent(session, rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("signal-received".equals(reason)) { //$NON-NLS-1$
			if (exec != null) {
				event = new MISignalEvent(session, exec);
			} else if (rr != null) {
				event = new MISignalEvent(session, rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("location-reached".equals(reason)) { //$NON-NLS-1$
			if (exec != null) {
				event = new MILocationReachedEvent(session, exec);
			} else if (rr != null) {
				event = new MILocationReachedEvent(session, rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("function-finished".equals(reason)) { //$NON-NLS-1$
			if (exec != null) {
				event = new MIFunctionFinishedEvent(session, exec);
			} else if (rr != null) {
				event = new MIFunctionFinishedEvent(session, rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("exited-normally".equals(reason) || "exited".equals(reason)) { //$NON-NLS-1$ //$NON-NLS-2$
			if (exec != null) {
				event = new MIInferiorExitEvent(session, exec);
			} else if (rr != null) {
				event = new MIInferiorExitEvent(session, rr);
			}
			session.getMIInferior().setTerminated();
		} else if ("exited-signalled".equals(reason)) { //$NON-NLS-1$
			if (exec != null) {
				event = new MIInferiorSignalExitEvent(session, exec);
			} else if (rr != null) {
				event = new MIInferiorSignalExitEvent(session, rr);
			}
			session.getMIInferior().setTerminated();
		}
		return event;
	}

	String[] getStreamRecords() {
		List streamRecords = new ArrayList();
		MIOOBRecord[] oobRecords = (MIOOBRecord[]) oobList.toArray(new MIOOBRecord[0]);
		for (int i = 0; i < oobRecords.length; i++) {
			if (oobRecords[i] instanceof MIStreamRecord) {
				String s = ((MIStreamRecord) oobRecords[i]).getString().trim();
				if (s != null && s.length() > 0) {
					streamRecords.add(s);
				}
			}
		}
		return (String[]) streamRecords.toArray(new String[0]);
	}

}
