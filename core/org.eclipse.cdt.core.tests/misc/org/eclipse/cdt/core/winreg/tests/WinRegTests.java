package org.eclipse.cdt.core.winreg.tests;

import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.Platform;

import junit.framework.TestCase;

public class WinRegTests extends TestCase {

	public void test1() {
		WindowsRegistry registry = WindowsRegistry.getRegistry();
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			assertNotNull(registry);
			String value = registry.getLocalMachineValue("SOFTWARE\\Microsoft\\Windows\\CurrentVersion", "ProgramFilesDir");
			// Not sure how you set this to anything else so it seems safe.
			assertEquals("C:\\Program Files", value);
		} else {
			// Should be null on non-Windows platforms
			assertNotNull(registry);
		}
	}
	
	public void test2() {
		WindowsRegistry registry = WindowsRegistry.getRegistry();
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			assertNotNull(registry);
			String value = registry.getLocalMachineValue("SOFTWARE\\Microsoft\\Windows\\CurrentVersion", "Nothing");
			// Not sure how you set this to anything else so it seems safe.
			assertNull(value);
		} else {
			// Should be null on non-Windows platforms
			assertNotNull(registry);
		}
	}
}
