package org.eclipse.cdt.arduino.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IResource;

/**
 * Responsible for collecting scanner info on Arduino Projects.
 */
public class ArduinoScannerInfoProvider implements IScannerInfoProvider {

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		Map<String, String> symbols = new HashMap<>();
		String[] includePath = { "/Users/dschaefer/.arduinocdt/hardware/arduino/avr/1.6.7/cores/arduino" };
		ExtendedScannerInfo scannerInfo = new ExtendedScannerInfo(symbols, includePath);
		return scannerInfo;
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		// TODO Auto-generated method stub

	}

}
