package org.eclipse.cdt.arduino.core.tests;

import static org.junit.Assert.assertNotNull;

import org.eclipse.cdt.arduino.core.board.ArduinoBoardManager;
import org.junit.Test;

public class BoardManagerTests {

	@Test
	public void loadPackagesTest() throws Exception {
		assertNotNull(ArduinoBoardManager.instance.getPackageIndex());
	}

}
