/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Dmitry Kozlov (Mentor Graphics) - Enhance trace status (Bug 390827)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.STOP_REASON_ENUM;

/**
 * -trace-status result.
 *
 * ^done,supported="1",running="1",frames="0",buffer-size="5242880",buffer-free="5242880"
 * ^done,supported="1",running="0",stop-reason="request",frames="0",buffer-size="5242880",buffer-free="5242880"
 * ^done,supported="1",running="0",stop-reason="passcount",stopping-tracepoint="7",frames="3",buffer-size="5242880",buffer-free="5242862"
 *
 * Field presence:
 *   With GDB 7.2:
 *        "supported"
 *        "running"
 *        "stop-reason"
 *        "stopping-tracepoint"
 *        "error-description"
 *        "frames"
 *        "frames-created"
 *        "buffer-size"
 *        "buffer-free"
 *        "disconnected"
 *        "circular"
 *   Added in GDB 7.4:
 *        "user-name"
 *        "notes"
 *        "start-time"
 *        "stop-time"
 *   Added in GDB 7.6:
 *        "trace-file"
 *
 * @since 3.0
 */
public class MITraceStatusInfo extends MIInfo {

	private int fFreeBufferSize = 0;
	private int fTotalBufferSize = 0;
	private int fNumberOfCollectedFrames = 0;
	private int fNumberOfCreatedFrames = 0;
	private boolean fIsTracingActive = false;
	private boolean fIsTracingSupported = false;
	private STOP_REASON_ENUM fStopReason = null;
	private String fStopErrorDesc = ""; //$NON-NLS-1$
	private Integer fStoppingTracepoint = null;
	private boolean fIsTracingFromFile = false;
	private String fTraceFile = ""; //$NON-NLS-1$
	private boolean fIsDisconnectedTracingEnabled = false;
	private String fUserName = ""; //$NON-NLS-1$
	private String fNotes = ""; //$NON-NLS-1$
	private String fStartTime = ""; //$NON-NLS-1$
	private String fStopTime = ""; //$NON-NLS-1$
	private boolean fIsCircularBuffer = false;

	public MITraceStatusInfo(MIOutput out) {
		super(out);
		parse();
	}

	public int getFreeBufferSize() {
		return fFreeBufferSize;
	}

	public int getNumberOfCollectedFrame() {
		return fNumberOfCollectedFrames;
	}

	/** @since 4.4 */
	public int getNumberOfCreatedFrames() {
		return fNumberOfCreatedFrames;
	}

	public int getTotalBufferSize() {
		return fTotalBufferSize;
	}

	public boolean isTracingActive() {
		return fIsTracingActive;
	}

	public boolean isTracingSupported() {
		return fIsTracingSupported;
	}

	/** @since 4.4 */
	public boolean isTracingFromFile() {
		return fIsTracingFromFile;
	}

	/** @since 4.4 */
	public boolean isDisconnectedTracingEnabled() {
		return fIsDisconnectedTracingEnabled;
	}

	public STOP_REASON_ENUM getStopReason() {
		return fStopReason;
	}

	/** @since 4.4 */
	public String getStopErrorDescription() {
		return fStopErrorDesc;
	}

	public Integer getStopTracepoint() {
		return fStoppingTracepoint;
	}

	/** @since 4.4 */
	public String getUserName() {
		return fUserName;
	}

	/** @since 4.4 */
	public String getNotes() {
		return fNotes;
	}

	/** @since 4.4 */
	public String getStartTime() {
		return fStartTime;
	}

	/** @since 4.4 */
	public String getStopTime() {
		return fStopTime;
	}

	/** @since 4.4 */
	public String getTraceFile() {
		return isTracingFromFile() ? fTraceFile : null;
	}

	/** @since 4.4 */
	public boolean isCircularBuffer() {
		return fIsCircularBuffer;
	}

	private void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("supported")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fIsTracingSupported = ((MIConst) val).getString().equals("0") ? false : true; //$NON-NLS-1$
							fIsTracingFromFile = ((MIConst) val).getString().equals("file"); //$NON-NLS-1$
						}
					} else if (var.equals("trace-file")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fTraceFile = ((MIConst) val).getString().trim();
						}
					} else if (var.equals("running")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fIsTracingActive = ((MIConst) val).getString().equals("0") ? false : true; //$NON-NLS-1$
						}
					} else if (var.equals("stop-reason")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							String reason = ((MIConst) val).getString().trim();
							if (reason.equalsIgnoreCase("request")) { //$NON-NLS-1$
								fStopReason = STOP_REASON_ENUM.REQUEST;
							} else if (reason.equalsIgnoreCase("overflow")) { //$NON-NLS-1$
								fStopReason = STOP_REASON_ENUM.OVERFLOW;
							} else if (reason.equalsIgnoreCase("disconnection")) { //$NON-NLS-1$
								fStopReason = STOP_REASON_ENUM.DISCONNECTION;
							} else if (reason.equalsIgnoreCase("passcount")) { //$NON-NLS-1$
								fStopReason = STOP_REASON_ENUM.PASSCOUNT;
							} else if (reason.equalsIgnoreCase("error")) { //$NON-NLS-1$
								fStopReason = STOP_REASON_ENUM.ERROR;
							} else {
								fStopReason = STOP_REASON_ENUM.UNKNOWN;
							}
						}
					} else if (var.equals("stopping-tracepoint")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							try {
								fStoppingTracepoint = Integer.parseInt(((MIConst) val).getString().trim());
							} catch (NumberFormatException e) {
							}
						}
					} else if (var.equals("error-description")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fStopErrorDesc = ((MIConst) val).getString().trim();
						}
					} else if (var.equals("frames")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							try {
								fNumberOfCollectedFrames = Integer.parseInt(((MIConst) val).getString().trim());
							} catch (NumberFormatException e) {
							}
						}
					} else if (var.equals("frames-created")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							try {
								fNumberOfCreatedFrames = Integer.parseInt(((MIConst) val).getString().trim());
							} catch (NumberFormatException e) {
							}
						}
					} else if (var.equals("buffer-size")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							try {
								fTotalBufferSize = Integer.parseInt(((MIConst) val).getString().trim());
							} catch (NumberFormatException e) {
							}
						}
					} else if (var.equals("buffer-free")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							try {
								fFreeBufferSize = Integer.parseInt(((MIConst) val).getString().trim());
							} catch (NumberFormatException e) {
							}
						}
					} else if (var.equals("disconnected")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fIsDisconnectedTracingEnabled = ((MIConst) val).getString().equals("0") ? false : true; //$NON-NLS-1$
						}
					} else if (var.equals("circular")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fIsCircularBuffer = ((MIConst) val).getString().trim().equals("0") ? false : true; //$NON-NLS-1$
						}
					} else if (var.equals("user-name")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fUserName = ((MIConst) val).getString().trim();
						}
					} else if (var.equals("notes")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fNotes = ((MIConst) val).getString();
						}
					} else if (var.equals("start-time")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fStartTime = ((MIConst) val).getString().trim();
						}
					} else if (var.equals("stop-time")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fStopTime = ((MIConst) val).getString().trim();
						}
					}
				}
			}
		}
	}
}
