/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;



/**
 * This can not be detected yet by gdb/mi.
 *
 */
public class MIRegisterChangedEvent extends MIChangedEvent {

		String regName;
		int regno;

		public MIRegisterChangedEvent(String name, int no) {
			regName = name;
			regno = no;
		}

		public String getName() {
			return regName;
		}

		public int getNumber() {
			return regno;
		}
}
