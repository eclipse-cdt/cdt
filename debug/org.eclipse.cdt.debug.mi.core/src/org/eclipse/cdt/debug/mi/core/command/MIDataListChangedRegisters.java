/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -data-list-changed-registers
 *
 *   Display a list of the registers that have changed.
 *
 */
public class MIDataListChangedRegisters extends MICommand 
{
	public MIDataListChangedRegisters() {
		super("-data-list-changed-registers" );
	}
}
