package org.eclipse.cdt.arduino.ui.internal.project;

import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.remote.core.IRemoteConnection;

public class ArduinoPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IRemoteConnection) {
			IRemoteConnection remote = (IRemoteConnection) receiver;
			return remote.hasService(ArduinoRemoteConnection.class);
		} else {
			return false;
		}
	}

}
