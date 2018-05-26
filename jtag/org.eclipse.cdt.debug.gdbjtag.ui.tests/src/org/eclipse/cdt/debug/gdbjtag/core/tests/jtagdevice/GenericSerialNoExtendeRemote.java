package org.eclipse.cdt.debug.gdbjtag.core.tests.jtagdevice;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection2;

public class GenericSerialNoExtendeRemote extends GenericSerialNoExtendedRemoteInfo implements IGDBJtagConnection2 {
	@Override
	public boolean getSupportsExtendedRemote() {
		return false;
	}
}
