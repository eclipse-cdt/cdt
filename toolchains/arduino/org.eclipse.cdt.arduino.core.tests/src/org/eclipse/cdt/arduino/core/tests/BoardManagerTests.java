package org.eclipse.cdt.arduino.core.tests;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.Semaphore;

import org.eclipse.cdt.arduino.core.IArduinoBoardManager.Handler;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoardManager;
import org.eclipse.cdt.arduino.core.internal.board.PackageIndex;
import org.junit.Test;

public class BoardManagerTests {

	@Test
	public void loadPackagesTest() throws Exception {
		Semaphore semaphore = new Semaphore(0);
		new ArduinoBoardManager().getPackageIndex(new Handler<PackageIndex>() {
			@Override
			public void handle(PackageIndex result) {
				assertNotNull(result);
				semaphore.release();
			}
		});
		semaphore.acquire();
	}

}
