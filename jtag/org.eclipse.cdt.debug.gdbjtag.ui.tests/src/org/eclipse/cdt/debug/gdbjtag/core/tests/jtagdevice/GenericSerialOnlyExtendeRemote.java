package org.eclipse.cdt.debug.gdbjtag.core.tests.jtagdevice;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection2;

public class GenericSerialOnlyExtendeRemote extends GenericSerialNoExtendedRemoteInfo
		implements IGDBJtagConnection2 {
	@Override
	public boolean getSupportsOnlyExtendedRemote() {
		return true;
	}
}
