/*******************************************************************************
 * Copyright (c) 2007, 2016 Ericsson and others.
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
 *     Marc Khouzam (Ericsson) - Support for dynamic printf (400628)
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
	public static final String NUMBER = DSFMIBREAKPOINT + ".number"; //$NON-NLS-1$
	public static final String TYPE = DSFMIBREAKPOINT + ".type"; //$NON-NLS-1$
	public static final String THREAD_ID = DSFMIBREAKPOINT + ".threadId"; //$NON-NLS-1$
	public static final String FULL_NAME = DSFMIBREAKPOINT + ".fullName"; //$NON-NLS-1$
	public static final String HITS = DSFMIBREAKPOINT + ".hits"; //$NON-NLS-1$
	public static final String IS_TEMPORARY = DSFMIBREAKPOINT + ".isTemporary"; //$NON-NLS-1$
	public static final String IS_HARDWARE = DSFMIBREAKPOINT + ".isHardware"; //$NON-NLS-1$
	public static final String LOCATION = DSFMIBREAKPOINT + ".location"; //$NON-NLS-1$

	// Back-end breakpoint object
	private final MIBreakpoint fBreakpoint;
	private final Map<String, Object> fProperties;

	/**
	 * Breakpoint types
	 *
	 * @deprecated This enum is not extensible, so has been deprecated to allow extenders to have their own breakpoint
	 *             types. Within CDT there was no access to this enum outside of this class. The replacement is to use {@link MIBreakpointDMData#getBreakpointType()}
	 */
	@Deprecated
	public static enum MIBreakpointNature {
		UNKNOWN, BREAKPOINT, WATCHPOINT, CATCHPOINT,
		/** @since 3.0 */
		TRACEPOINT,
		/** @since 4.4 */
		DYNAMICPRINTF
	}

	///////////////////////////////////////////////////////////////////////////
	// Constructors
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Copy constructor
	 *
	 * @param other
	 * @deprecated Call {@link #copy()} on other instead to allow subclasses to be copied properly.
	 */
	@Deprecated
	public MIBreakpointDMData(MIBreakpointDMData other) {

		fBreakpoint = new MIBreakpoint(other.fBreakpoint);
		fProperties = new HashMap<>(other.fProperties);
	}

	/**
	 * Perform a copy.
	 *
	 * @return the copy
	 * @since 5.3
	 */
	public MIBreakpointDMData copy() {
		return new MIBreakpointDMData(new MIBreakpoint(fBreakpoint), new HashMap<>(fProperties));
	}

	/**
	 * Create a MIBreakpointDMData from a breakpoint and a potentially populated properties map.
	 *
	 * @param dsfMIBreakpoint
	 *            MI Breakpoint to represent
	 * @param properties
	 *            if {@code null}, calculate properties, otherwise use properties received
	 * @since 5.3
	 */
	protected MIBreakpointDMData(MIBreakpoint dsfMIBreakpoint, HashMap<String, Object> properties) {
		fBreakpoint = dsfMIBreakpoint;
		if (properties != null) {
			fProperties = properties;
		} else {
			fProperties = new HashMap<>();

			if (dsfMIBreakpoint.isTracepoint()) {
				// Generic breakpoint attributes
				fProperties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.TRACEPOINT);
				fProperties.put(MIBreakpoints.FILE_NAME, dsfMIBreakpoint.getFile());
				fProperties.put(MIBreakpoints.LINE_NUMBER, dsfMIBreakpoint.getLine());
				fProperties.put(MIBreakpoints.FUNCTION, dsfMIBreakpoint.getFunction());
				fProperties.put(MIBreakpoints.ADDRESS, dsfMIBreakpoint.getAddress());
				fProperties.put(MIBreakpoints.CONDITION, dsfMIBreakpoint.getCondition());
				fProperties.put(MIBreakpoints.PASS_COUNT, dsfMIBreakpoint.getPassCount());
				fProperties.put(MIBreakpoints.IS_ENABLED, Boolean.valueOf(dsfMIBreakpoint.isEnabled()));
				fProperties.put(MIBreakpoints.COMMANDS, dsfMIBreakpoint.getCommands());

				// MI-specific breakpoint attributes
				fProperties.put(NUMBER, dsfMIBreakpoint.getNumber());
				fProperties.put(TYPE, dsfMIBreakpoint.getType());
				fProperties.put(THREAD_ID, dsfMIBreakpoint.getThreadId());
				fProperties.put(FULL_NAME, dsfMIBreakpoint.getFullName());
				fProperties.put(HITS, dsfMIBreakpoint.getTimes());
				fProperties.put(IS_TEMPORARY, Boolean.valueOf(dsfMIBreakpoint.isTemporary()));
				fProperties.put(IS_HARDWARE, Boolean.valueOf(dsfMIBreakpoint.isHardware()));
				fProperties.put(LOCATION, formatLocation());

			} else if (dsfMIBreakpoint.isDynamicPrintf()) {
				// Generic breakpoint attributes
				fProperties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.DYNAMICPRINTF);
				fProperties.put(MIBreakpoints.FILE_NAME, dsfMIBreakpoint.getFile());
				fProperties.put(MIBreakpoints.LINE_NUMBER, dsfMIBreakpoint.getLine());
				fProperties.put(MIBreakpoints.FUNCTION, dsfMIBreakpoint.getFunction());
				fProperties.put(MIBreakpoints.ADDRESS, dsfMIBreakpoint.getAddress());
				fProperties.put(MIBreakpoints.CONDITION, dsfMIBreakpoint.getCondition());
				fProperties.put(MIBreakpoints.PRINTF_STRING, dsfMIBreakpoint.getPrintfString());
				fProperties.put(MIBreakpoints.IS_ENABLED, Boolean.valueOf(dsfMIBreakpoint.isEnabled()));
				fProperties.put(MIBreakpoints.COMMANDS, dsfMIBreakpoint.getCommands());

				// MI-specific breakpoint attributes
				fProperties.put(NUMBER, dsfMIBreakpoint.getNumber());
				fProperties.put(TYPE, dsfMIBreakpoint.getType());
				fProperties.put(THREAD_ID, dsfMIBreakpoint.getThreadId());
				fProperties.put(FULL_NAME, dsfMIBreakpoint.getFullName());
				fProperties.put(HITS, dsfMIBreakpoint.getTimes());
				fProperties.put(IS_TEMPORARY, Boolean.valueOf(dsfMIBreakpoint.isTemporary()));
				fProperties.put(IS_HARDWARE, Boolean.valueOf(dsfMIBreakpoint.isHardware()));
				fProperties.put(LOCATION, formatLocation());

			} else if (dsfMIBreakpoint.isWatchpoint()) {
				// Generic breakpoint attributes
				fProperties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.WATCHPOINT);
				fProperties.put(MIBreakpoints.EXPRESSION, dsfMIBreakpoint.getExpression());
				fProperties.put(MIBreakpoints.READ,
						dsfMIBreakpoint.isAccessWatchpoint() || dsfMIBreakpoint.isReadWatchpoint());
				fProperties.put(MIBreakpoints.WRITE,
						dsfMIBreakpoint.isAccessWatchpoint() || dsfMIBreakpoint.isWriteWatchpoint());

				// MI-specific breakpoint attributes
				fProperties.put(NUMBER, dsfMIBreakpoint.getNumber());

			} else if (dsfMIBreakpoint.isCatchpoint()) {
				// Because gdb doesn't support catchpoints in mi, we end up using
				// CLI to set the catchpoint. The sort of MIBreakpoint we create
				// at that time is minimal as the only information we get back from
				// gdb is the breakpoint number and type of the catchpoint we just
				// set. See MIBreakpoint(String)
				//
				// The only type of MIBreakpoint that will be of this CATCHPOINT type
				// is the instance we create from the response of the CLI command we
				// use to set the catchpoint. If we later query gdb for the breakpoint
				// list, we'll unfortunately end up creating an MIBreakpoint of type
				// BREAKPOINT. Maybe one day gdb will treats catchpoints like first
				// class citizens and this messy situation will go away.

				fProperties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.CATCHPOINT);
				fProperties.put(MIBreakpoints.CATCHPOINT_TYPE, dsfMIBreakpoint.getCatchpointType());
				fProperties.put(NUMBER, dsfMIBreakpoint.getNumber());

			} else {
				// For all other breakpoint types, use MIBreakpoints.BREAKPOINT.

				// Note that this may in fact be a catchpoint. See comment above in
				// isCatchpoint case

				// Generic breakpoint attributes
				fProperties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.BREAKPOINT);
				fProperties.put(MIBreakpoints.FILE_NAME, dsfMIBreakpoint.getFile());
				fProperties.put(MIBreakpoints.LINE_NUMBER, dsfMIBreakpoint.getLine());
				fProperties.put(MIBreakpoints.FUNCTION, dsfMIBreakpoint.getFunction());
				fProperties.put(MIBreakpoints.ADDRESS, dsfMIBreakpoint.getAddress());
				fProperties.put(MIBreakpoints.CONDITION, dsfMIBreakpoint.getCondition());
				fProperties.put(MIBreakpoints.IGNORE_COUNT, dsfMIBreakpoint.getIgnoreCount());
				fProperties.put(MIBreakpoints.IS_ENABLED, Boolean.valueOf(dsfMIBreakpoint.isEnabled()));
				fProperties.put(MIBreakpoints.COMMANDS, dsfMIBreakpoint.getCommands());

				// MI-specific breakpoint attributes
				fProperties.put(NUMBER, dsfMIBreakpoint.getNumber());
				fProperties.put(TYPE, dsfMIBreakpoint.getType());
				fProperties.put(THREAD_ID, dsfMIBreakpoint.getThreadId());
				fProperties.put(FULL_NAME, dsfMIBreakpoint.getFullName());
				fProperties.put(HITS, dsfMIBreakpoint.getTimes());
				fProperties.put(IS_TEMPORARY, Boolean.valueOf(dsfMIBreakpoint.isTemporary()));
				fProperties.put(IS_HARDWARE, Boolean.valueOf(dsfMIBreakpoint.isHardware()));
				fProperties.put(LOCATION, formatLocation());

			}
		}
	}

	/**
	 * Constructs a DsfMIBreakpoint from a back-end object. Create the object by calling
	 * {@link MIBreakpoints#createMIBreakpointDMData(MIBreakpoint)} to ensure correct version is called.
	 *
	 * @param dsfMIBreakpoint
	 *            back-end breakpoint
	 * @deprecated Call {@link MIBreakpoints#createMIBreakpointDMData(MIBreakpoint)} instead
	 */
	@Deprecated
	public MIBreakpointDMData(MIBreakpoint dsfMIBreakpoint) {
		this(dsfMIBreakpoint, null);
	}

	/**
	 * Obtain the properties map. Method only intended to be called by sub-classes.
	 *
	 * @return properties map
	 * @since 5.3
	 */
	protected Map<String, Object> getProperties() {
		return fProperties;
	}

	/**
	 * Obtain the MI Breakpoint. Method only intended to be called by sub-classes.
	 *
	 * @return breakpoint
	 * @since 5.3
	 */
	protected MIBreakpoint getBreakpoint() {
		return fBreakpoint;
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
		String fileName = fBreakpoint.getFile();
		Integer lineNumber = fBreakpoint.getLine();
		String function = fBreakpoint.getFunction();

		if (!fileName.isEmpty()) {
			if (lineNumber != -1) {
				location = fileName + ":" + lineNumber; //$NON-NLS-1$
			} else {
				location = fileName + ":" + function; //$NON-NLS-1$
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
		return fProperties.equals(other.fProperties);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof MIBreakpointDMData))
			return false;
		return equals((MIBreakpointDMData) other);
	}

	@Override
	public int hashCode() {
		return fProperties.hashCode();
	}

	///////////////////////////////////////////////////////////////////////////
	// IBreakpointDMData
	///////////////////////////////////////////////////////////////////////////

	@Override
	public String getBreakpointType() {
		return (String) fProperties.get(MIBreakpoints.BREAKPOINT_TYPE);
	}

	/** @since 5.0 */
	public String getReference() {
		return fBreakpoint.getNumber();
	}

	@Override
	public IAddress[] getAddresses() {
		IAddress[] addresses = new IAddress[1];
		addresses[0] = new Addr64(fBreakpoint.getAddress());
		return addresses;
	}

	@Override
	public String getCondition() {
		return fBreakpoint.getCondition();
	}

	@Override
	public String getExpression() {
		return fBreakpoint.getExpression();
	}

	@Override
	public String getFileName() {
		return fBreakpoint.getFile();
	}

	@Override
	public String getFunctionName() {
		return fBreakpoint.getFunction();
	}

	@Override
	public int getIgnoreCount() {
		return fBreakpoint.getIgnoreCount();
	}

	@Override
	public int getLineNumber() {
		return fBreakpoint.getLine();
	}

	@Override
	public boolean isEnabled() {
		return fBreakpoint.isEnabled();
	}

	///////////////////////////////////////////////////////////////////////////
	// MIBreakpointDMData
	///////////////////////////////////////////////////////////////////////////

	/**
	 * @since 3.0
	 */
	public int getPassCount() {
		return fBreakpoint.getPassCount();
	}

	/**
	 * @since 4.4
	 */
	public String getPrintfString() {
		return fBreakpoint.getPrintfString();
	}

	/**
	 * @since 3.0
	 */
	public String getCommands() {
		return fBreakpoint.getCommands();
	}

	/** @since 5.0 */
	public String getNumber() {
		return fBreakpoint.getNumber();
	}

	public String getThreadId() {
		return fBreakpoint.getThreadId();
	}

	/** @since 4.2 */
	public String[] getGroupIds() {
		return fBreakpoint.getGroupIds();
	}

	/** @since 4.2 */
	public void setGroupIds(String[] groups) {
		fBreakpoint.setGroupIds(groups);
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
	 * @since 3.0
	 */
	public void setPassCount(int count) {
		fBreakpoint.setPassCount(count);
		fProperties.put(MIBreakpoints.PASS_COUNT, count);
	}

	/**
	 * @since 3.0
	 */
	public void setCommands(String commands) {
		fBreakpoint.setCommands(commands);
		fProperties.put(MIBreakpoints.COMMANDS, commands);
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

	/**
	 * @since 4.0
	 */
	public boolean isPending() {
		return fBreakpoint.isPending();
	}
}
