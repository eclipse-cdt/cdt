/*******************************************************************************
 * Copyright (c) 2007 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.utils.Addr64;

public class MIBreakpointDMData implements IBreakpointDMData {

    /**
     * MI-specific breakpoint attributes markers.
     */
	public static final String DSFMIBREAKPOINT = "org.eclipse.cdt.dsf.debug.breakpoint.mi"; //$NON-NLS-1$
	public static final String NUMBER       = DSFMIBREAKPOINT + ".number";      //$NON-NLS-1$
	public static final String TYPE         = DSFMIBREAKPOINT + ".type";        //$NON-NLS-1$
	public static final String THREAD_ID    = DSFMIBREAKPOINT + ".threadId";    //$NON-NLS-1$
	public static final String FULL_NAME    = DSFMIBREAKPOINT + ".fullName";    //$NON-NLS-1$
	public static final String HITS         = DSFMIBREAKPOINT + ".hits";        //$NON-NLS-1$
	public static final String IS_TEMPORARY = DSFMIBREAKPOINT + ".isTemporary"; //$NON-NLS-1$
	public static final String IS_HARDWARE  = DSFMIBREAKPOINT + ".isHardware";  //$NON-NLS-1$
	public static final String LOCATION     = DSFMIBREAKPOINT + ".location";    //$NON-NLS-1$

	// Back-end breakpoint object
	private final MIBreakpoint fBreakpoint;
	private final Map<String, Object> fProperties;

	// Breakpoint types
	public static enum MIBreakpointNature { UNKNOWN, BREAKPOINT, WATCHPOINT, CATCHPOINT, 
		                                    /** @since 2.1*/ TRACEPOINT };
	private final MIBreakpointNature fNature;


	///////////////////////////////////////////////////////////////////////////
	// Constructors
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Copy constructor
	 *  
	 * @param other
	 */
	public MIBreakpointDMData(MIBreakpointDMData other) {

		fBreakpoint = new MIBreakpoint(other.fBreakpoint);
		fProperties = new HashMap<String, Object>(other.fProperties);
		fNature = other.fNature;
	}

	/**
	 * Constructs a DsfMIBreakpoint from a back-end object
	 *  
	 * @param dsfMIBreakpoint   back-end breakpoint
	 */
	public MIBreakpointDMData(MIBreakpoint dsfMIBreakpoint) {

		// No support for catchpoints yet
		fBreakpoint = dsfMIBreakpoint;
		if (dsfMIBreakpoint.isTracepoint()) {
			fNature = MIBreakpointNature.TRACEPOINT;
		} else if (dsfMIBreakpoint.isWatchpoint()) {
			fNature = MIBreakpointNature.WATCHPOINT;
		} else {
			fNature = MIBreakpointNature.BREAKPOINT;
		}

		fProperties = new HashMap<String,Object>();
		switch (fNature) {
		
			case BREAKPOINT:
			{
				// Generic breakpoint attributes
				fProperties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.BREAKPOINT);
				fProperties.put(MIBreakpoints.FILE_NAME,       dsfMIBreakpoint.getFile());
				fProperties.put(MIBreakpoints.LINE_NUMBER,     dsfMIBreakpoint.getLine());
				fProperties.put(MIBreakpoints.FUNCTION,        dsfMIBreakpoint.getFunction());
				fProperties.put(MIBreakpoints.ADDRESS,         dsfMIBreakpoint.getAddress());
				fProperties.put(MIBreakpoints.CONDITION,       dsfMIBreakpoint.getCondition());
				fProperties.put(MIBreakpoints.IGNORE_COUNT,    dsfMIBreakpoint.getIgnoreCount());
				fProperties.put(MIBreakpoints.IS_ENABLED,      new Boolean(dsfMIBreakpoint.isEnabled()));
	
				// MI-specific breakpoint attributes
				fProperties.put(NUMBER,       dsfMIBreakpoint.getNumber());
				fProperties.put(TYPE,         dsfMIBreakpoint.getType());
				fProperties.put(THREAD_ID,    dsfMIBreakpoint.getThreadId());
				fProperties.put(FULL_NAME,    dsfMIBreakpoint.getFullName());
				fProperties.put(HITS,         dsfMIBreakpoint.getTimes());
				fProperties.put(IS_TEMPORARY, new Boolean(dsfMIBreakpoint.isTemporary()));
				fProperties.put(IS_HARDWARE,  new Boolean(dsfMIBreakpoint.isHardware()));
				fProperties.put(LOCATION,     formatLocation());
				break;
			}

			case WATCHPOINT:
			{
				// Generic breakpoint attributes
				fProperties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.WATCHPOINT);
				fProperties.put(MIBreakpoints.EXPRESSION, dsfMIBreakpoint.getExpression());
				fProperties.put(MIBreakpoints.READ,       dsfMIBreakpoint.isAccessWatchpoint() || dsfMIBreakpoint.isReadWatchpoint());
				fProperties.put(MIBreakpoints.WRITE,      dsfMIBreakpoint.isAccessWatchpoint() || dsfMIBreakpoint.isWriteWatchpoint());

				// MI-specific breakpoint attributes
				fProperties.put(NUMBER,     dsfMIBreakpoint.getNumber());
				break;
			}
			
			case TRACEPOINT:
			{
				// Generic breakpoint attributes
				fProperties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.TRACEPOINT);
				fProperties.put(MIBreakpoints.FILE_NAME,       dsfMIBreakpoint.getFile());
				fProperties.put(MIBreakpoints.LINE_NUMBER,     dsfMIBreakpoint.getLine());
				fProperties.put(MIBreakpoints.FUNCTION,        dsfMIBreakpoint.getFunction());
				fProperties.put(MIBreakpoints.ADDRESS,         dsfMIBreakpoint.getAddress());
				fProperties.put(MIBreakpoints.CONDITION,       dsfMIBreakpoint.getCondition());
				fProperties.put(MIBreakpoints.IGNORE_COUNT,    dsfMIBreakpoint.getPassCount());
				fProperties.put(MIBreakpoints.IS_ENABLED,      new Boolean(dsfMIBreakpoint.isEnabled()));
	
				// MI-specific breakpoint attributes
				fProperties.put(NUMBER,       dsfMIBreakpoint.getNumber());
				fProperties.put(TYPE,         dsfMIBreakpoint.getType());
				fProperties.put(THREAD_ID,    dsfMIBreakpoint.getThreadId());
				fProperties.put(FULL_NAME,    dsfMIBreakpoint.getFullName());
				fProperties.put(HITS,         dsfMIBreakpoint.getTimes());
				fProperties.put(IS_TEMPORARY, new Boolean(dsfMIBreakpoint.isTemporary()));
				fProperties.put(IS_HARDWARE,  new Boolean(dsfMIBreakpoint.isHardware()));
				fProperties.put(LOCATION,     formatLocation());
				break;
			}
			
			// Not reachable
			default:
			{
				fProperties.put(MIBreakpoints.BREAKPOINT_TYPE, null);
				break;
			}
		}
	}

	/**
	 * Formats the LOCATION synthetic property from the existing fields
	 *  
	 * @return The location string 
	 */
	private String formatLocation() {

		// Unlikely default location
		String location = fBreakpoint.getAddress();

		// Get the relevant parameters
		String  fileName   = fBreakpoint.getFile();
		Integer lineNumber = fBreakpoint.getLine();
		String  function   = fBreakpoint.getFunction();

		if (!fileName.equals("")) { //$NON-NLS-1$
			if (lineNumber != -1) {
				location = fileName + ":" + lineNumber; //$NON-NLS-1$
			} else {
				location = fileName + ":" + function;   //$NON-NLS-1$
			}
		}

		return location;
	}

	/**
	 * Checks for equality
	 * 
	 * @param other
	 * @return
	 */
	public boolean equals(MIBreakpointDMData other) {
		return (fNature == other.fNature) && (fProperties.equals(other.fProperties));
	}
	
	///////////////////////////////////////////////////////////////////////////
	// IBreakpointDMData
	///////////////////////////////////////////////////////////////////////////

	public String getBreakpointType() {
		return (String) fProperties.get(MIBreakpoints.BREAKPOINT_TYPE);
	}

	public int getReference() {
		return fBreakpoint.getNumber();
	}

	public IAddress[] getAddresses() {
		IAddress[] addresses = new IAddress[1];
		addresses[0] = new Addr64(fBreakpoint.getAddress());
		return addresses;
	}

	public String getCondition() {
		return fBreakpoint.getCondition();
	}

	public String getExpression() {
		return fBreakpoint.getExpression();
	}

	public String getFileName() {
		return fBreakpoint.getFile();
	}

	public String getFunctionName() {
		return fBreakpoint.getFunction();
	}

	public int getIgnoreCount() {
		return fBreakpoint.getIgnoreCount();
	}

	public int getLineNumber() {
		return fBreakpoint.getLine();
	}

	public boolean isEnabled() {
		return fBreakpoint.isEnabled();
	}

	/**
	 * @since 2.1
	 */
	public int getPassCount() {
		return fBreakpoint.getPassCount();
	}
	
	/**
	 * @since 2.1
	 */
	public String getCommands() {
		return fBreakpoint.getCommands();
	}

	///////////////////////////////////////////////////////////////////////////
	// MIBreakpointDMData
	///////////////////////////////////////////////////////////////////////////

	public int getNumber() {
		return fBreakpoint.getNumber();
	}

	public String getThreadId() {
		return fBreakpoint.getThreadId();
	}

	public boolean isTemporary() {
		return fBreakpoint.isTemporary();
	}

	public boolean isHardware() {
		return fBreakpoint.isHardware();
	}

	public String getLocation() {
		return (String) fProperties.get(LOCATION);
	}

	public int getHits() {
		return fBreakpoint.getTimes();
	}

	public String getFullName() {
		return fBreakpoint.getFullName();
	}

	public String getType() {
		return fBreakpoint.getType();
	}

	public void setCondition(String condition) {
		fBreakpoint.setCondition(condition);
		fProperties.put(MIBreakpoints.CONDITION, condition);
	}

	public void setIgnoreCount(int ignoreCount) {
		fBreakpoint.setIgnoreCount(ignoreCount);
		fProperties.put(MIBreakpoints.IGNORE_COUNT, ignoreCount);
	}

	public void setEnabled(boolean isEnabled) {
		fBreakpoint.setEnabled(isEnabled);
		fProperties.put(MIBreakpoints.IS_ENABLED, isEnabled);
	}
	
	/**
	 * @since 2.1
	 */
	public void setPassCount(int count) {
		fBreakpoint.setPassCount(count);
		fProperties.put(MIBreakpoints.IGNORE_COUNT, count);
	}

	public boolean isReadWatchpoint() {
		return fBreakpoint.isReadWatchpoint();
	}

	public boolean isWriteWatchpoint() {
		return fBreakpoint.isWriteWatchpoint();
	}

	public boolean isAccessWatchpoint() {
		return fBreakpoint.isAccessWatchpoint();
	}

}
