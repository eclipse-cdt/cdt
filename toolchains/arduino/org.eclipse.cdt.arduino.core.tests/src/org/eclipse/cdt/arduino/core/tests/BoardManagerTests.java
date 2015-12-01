package org.eclipse.cdt.arduino.core.tests;

import static org.junit.Assert.assertNotEquals;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.junit.Test;

public class BoardManagerTests {

	@Test
	public void loadPackagesTest() throws Exception {
		assertNotEquals(0, Activator.getService(ArduinoManager.class).getPackageIndices().size());
	}

}
