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
public class MIRegisterCreatedEvent extends MICreatedEvent {

	String regName;
	int regno;

	public MIRegisterCreatedEvent(String name, int number) {
		this(0, name, number);
	}

	public MIRegisterCreatedEvent(int token, String name, int number) {
		super(token);
		regName = name;
		regno = number;
	}

	public String getName() {
		return regName;
	}

	public int getNumber() {
		return regno;
	}

}
