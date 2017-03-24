package org.eclipse.cdt.cmake.core.server;

import java.util.List;

/**
 * CMake server listener which attempts to mimic the behaviour of command line
 * cmake with respect to output logging.
 */
public class StdoutCMakeServerListener implements ICMakeServerListener {

	@Override
	public void onFileChange(String path, List<String> properties) {
	}

	@Override
	public void onMessage(String title, String message) {
		System.out.println("-- " + message);
	}

	@Override
	public void onProgress(CMakeProgress progress) {
	}

	@Override
	public void onSignal(String name) {
	}
}
