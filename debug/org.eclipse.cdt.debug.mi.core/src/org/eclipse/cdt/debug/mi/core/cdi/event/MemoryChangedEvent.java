/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.event.ICDIMemoryChangedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.cdi.CSession;
import org.eclipse.cdt.debug.mi.core.cdi.model.MemoryBlock;
import org.eclipse.cdt.debug.mi.core.event.MIMemoryChangedEvent;

/**
 */
public class MemoryChangedEvent implements ICDIMemoryChangedEvent {

	CSession session;
	MemoryBlock source;
	MIMemoryChangedEvent miMem;

	public MemoryChangedEvent(CSession s, MemoryBlock block, MIMemoryChangedEvent mem) {
		session = s;
		source = block;
		miMem = mem;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getAddresses()
	 */
	public Long[] getAddresses() {
	 	/* But only returns the address that are in the block.  */
		Long[] longs = miMem.getAddresses();
		List aList = new ArrayList(longs.length);
		for (int i = 0; i < longs.length; i++) {
			if (source.contains(longs[i])) {
				aList.add(longs[i]);
			}
		}
		return (Long[])aList.toArray(new Long[0]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return source;
	}

}
