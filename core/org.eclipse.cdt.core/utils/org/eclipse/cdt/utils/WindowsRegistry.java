/**
 * 
 */
package org.eclipse.cdt.utils;

import org.eclipse.core.runtime.Platform;

/**
 * @author DSchaefer
 *
 */
public class WindowsRegistry {

	private static boolean failed = false;
	private static WindowsRegistry registry;
	
	private WindowsRegistry() {
	}
	
	public static WindowsRegistry getRegistry() {
		if (registry == null && !failed) {
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				try {
					System.loadLibrary("winreg");
					registry = new WindowsRegistry();
				} catch (UnsatisfiedLinkError e) {
					failed = true;
					return null;
				}
			} else
				failed = true;
		}
		
		return registry;
	}
	
	/**
	 * Gets the registry value for the subkey of HKEY_LOCAL_MACHINE with the
	 * given name. If problems occur, like the name is not found, null is returned.
	 * 
	 * @param subkey subkey of HKEY_LOCAL_MACHINE
	 * @param name name of the registry value
	 * @return registry value or null if not found
	 */
	public native String getLocalMachineValue(String subkey, String name);
	
}
