/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
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
 * @since 3.0
 */
public class MITraceStatusInfo extends MIInfo {

	private int fFreeBufferSize = 0;
	private int fTotalBufferSize = 0;
	private int fNumberOfCollectedFrames = 0;
	private boolean fIsTracingActive = false;
	private boolean fIsTracingSupported = false;
	private STOP_REASON_ENUM fStopReason = null;
	private Integer fStoppingTracepoint = null;

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

	public int getTotalBufferSize() {
		return fTotalBufferSize;
	}

	public boolean isTracingActive() {
		return fIsTracingActive;
	}

	public boolean isTracingSupported() {
		return fIsTracingSupported;
	}
	
	public STOP_REASON_ENUM getStopReason() {
		return fStopReason;
	}
	
	public Integer getStopTracepoint() {
		return fStoppingTracepoint;
	}
	
	private void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("supported")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fIsTracingSupported = ((MIConst)val).getString().equals("0") ? false : true;;  //$NON-NLS-1$
						}
					} else if (var.equals("running")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fIsTracingActive = ((MIConst)val).getString().equals("0") ? false : true;;  //$NON-NLS-1$
						}
					} else if (var.equals("frames")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							try {
								fNumberOfCollectedFrames = Integer.parseInt(((MIConst)val).getString());
							} catch (NumberFormatException e) {}
						}
					} else if (var.equals("buffer-size")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							try {
								fTotalBufferSize = Integer.parseInt(((MIConst)val).getString());
							} catch (NumberFormatException e) {}
						}
					} else if (var.equals("buffer-free")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							try {
								fFreeBufferSize = Integer.parseInt(((MIConst)val).getString());
							} catch (NumberFormatException e) {}
						}
					} else if (var.equals("stop-reason")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							String reason = ((MIConst)val).getString().trim();
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
								fStoppingTracepoint = Integer.parseInt(((MIConst)val).getString());
							} catch (NumberFormatException e) {}
						}
					}  
				}
			}
		}
	}
}
