/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.mi.core.command.Command;
import org.eclipse.cdt.debug.mi.core.command.MIExecFinish;
import org.eclipse.cdt.debug.mi.core.command.MIExecNext;
import org.eclipse.cdt.debug.mi.core.command.MIExecNextInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecStep;
import org.eclipse.cdt.debug.mi.core.command.MIExecStepInstruction;
import org.eclipse.cdt.debug.mi.core.command.MIExecUntil;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIFunctionFinishedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MILocationReachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRunningEvent;
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

	public RxThread(MISession s) {
		super("MI RX Thread");
		session = s;
		oobList = new ArrayList();
	}

	/*
	 * Get the response, parse the output, dispatch for OOB
	 * search for the corresponding token in rxQueue for the ResultRecord.
	 */
	public void run () {
		BufferedReader reader =
			new BufferedReader(new InputStreamReader(session.getChannelInputStream()));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				// TRACING: print the output.
				MIPlugin.getDefault().debugLog(line);
				processMIOutput(line + "\n");
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
			Thread clean = new Thread(cleanup, "GDB Died");
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

			// Notify any command waiting for a ResultRecord.
			MIResultRecord rr = response.getMIResultRecord();
			if (rr != null) {
				int id = rr.getToken();
				Command cmd = rxQueue.removeCommand(id);

				// Check if the state changed.
				String state = rr.getResultClass();
				if ("running".equals(state)) {
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
					} else {
						type = MIRunningEvent.CONTINUE;
					}
					session.getMIInferior().setRunning();
					MIEvent event = new MIRunningEvent(id, type);
					session.fireEvent(event);
				} else if ("exit".equals(state)) {
					// No need to do anything, terminate() will.
					session.getMIInferior().setTerminated();
				} else if ("connected".equals(state)) {
					session.getMIInferior().setConnected();
				} else if ("error".equals(state)) {
					if (session.getMIInferior().isRunning()) {
						session.getMIInferior().setSuspended();
						MIEvent event = new MIStoppedEvent(id);
						session.fireEvent(event);
					}
				}

				// Clear the accumulate oobList on each new Result Command
				// response.
				MIOOBRecord [] oobRecords =
					(MIOOBRecord[])oobList.toArray(new MIOOBRecord[0]); 
				oobList.clear();
				
				// Notify the waiting command.
				if (cmd != null) {
					synchronized (cmd) {
						// Set the accumulate console Stream
						response.setMIOOBRecords(oobRecords);
						cmd.setMIOutput(response);
						cmd.notifyAll();
					}
				} 
				// Some result record contains informaton specific to oob.
				// This will happen when CLI-Command is use, for example
				// doing "run" will block and return a breakpointhit
				processMIOOBRecord(rr, list);

			} else {

				// Process OOBs
				MIOOBRecord[] oobs = response.getMIOOBRecords();
				for (int i = 0; i < oobs.length; i++) {
					processMIOOBRecord(oobs[i], list);
				}
			}

			MIEvent[] events = (MIEvent[])list.toArray(new MIEvent[list.size()]);
			session.fireEvents(events);
		} // if response != null
	}

	/**
	 * Dispatch a thread to deal with the listeners.
	 */
	void processMIOOBRecord(MIOOBRecord oob, List list) {
		if (oob instanceof MIAsyncRecord) {
			processMIOOBRecord((MIAsyncRecord)oob, list);
		} else if (oob instanceof MIStreamRecord) {
			processMIOOBRecord((MIStreamRecord)oob);
		}
	}

	void processMIOOBRecord(MIAsyncRecord async, List list) {
		if (async instanceof MIExecAsyncOutput) {
			MIExecAsyncOutput exec = (MIExecAsyncOutput)async;
			// Change of state.
			String state = exec.getAsyncClass();
			if ("stopped".equals(state)) {
				MIEvent e = null;
				MIResult[] results = exec.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					MIValue val = results[i].getMIValue();
					if (var.equals("reason")) {
						if (val instanceof MIConst) {
							String reason =((MIConst)val).getString();
							e = createEvent(reason, exec);
							if (e != null) {
								list.add(e);
							}
						}
					}
				}
			
				// We were stopped for some unknown reason, for example
				// GDB for temporary breakpoints will not send the
				// "reason" ??? still fire a stopped event.
				if (e == null) {
					session.getMIInferior().setSuspended();
					e = new MIStoppedEvent(exec.getToken());
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
				MIConsoleStreamOutput out = (MIConsoleStreamOutput)stream;
				String str = out.getString();
				if (str != null) {
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
				MITargetStreamOutput out = (MITargetStreamOutput)stream;
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
				MILogStreamOutput out = (MILogStreamOutput)stream;
				String str = out.getString();
				if (str != null) {
					try {
						log.write(str.getBytes());
						log.flush();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	/**
	 * Dispatch a thread to deal with the listeners.
	 */
	void processMIOOBRecord(MIResultRecord rr, List list) {
		MIResult[] results =  rr.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			if (var.equals("reason")) {
				MIValue value = results[i].getMIValue();
				if (value instanceof MIConst) {
					String reason = ((MIConst)value).getString();
					MIEvent event = createEvent(reason, rr);
					if (event != null) {
						list.add(event);
					}
				}
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
		if ("breakpoint-hit".equals(reason)) {
			if (exec != null) {
				event = new MIBreakpointEvent(exec);
			} else if (rr != null) {
				event = new MIBreakpointEvent(rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("watchpoint-trigger".equals(reason)) {
			if (exec != null) {
				event = new MIWatchpointTriggerEvent(exec);
			} else if (rr != null) {
				event = new MIWatchpointTriggerEvent(rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("watchpoint-scope".equals(reason)) {
			if (exec != null) {
				event = new MIWatchpointScopeEvent(exec);
			} else if (rr != null) {
				event = new MIWatchpointScopeEvent(rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("end-stepping-range".equals(reason)) {
			if (exec != null) {
				event = new MISteppingRangeEvent(exec);
			} else if (rr != null) {
				event = new MISteppingRangeEvent(rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("signal-received".equals(reason)) {
			if (exec != null) {
				event = new MISignalEvent(exec);
			} else if (rr != null) {
				event = new MISignalEvent(rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("location-reached".equals(reason)) {
			if (exec != null) {
				event = new MILocationReachedEvent(exec);
			} else if (rr != null) {
				event = new MILocationReachedEvent(rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("function-finished".equals(reason)) {
			if (exec != null) {
				event = new MIFunctionFinishedEvent(exec);
			} else if (rr != null) {
				event = new MIFunctionFinishedEvent(rr);
			}
			session.getMIInferior().setSuspended();
		} else if ("exited-normally".equals(reason) ||
			"exited-signalled".equals(reason) ||
			"exited-signalled".equals(reason) ||
			"exited".equals(reason)) {
			if (exec != null) {
				event = new MIInferiorExitEvent(exec);
			} else if (rr != null) {
				event = new MIInferiorExitEvent(rr);
			}
			session.getMIInferior().setTerminated();
		}
		return event;
	}

}
