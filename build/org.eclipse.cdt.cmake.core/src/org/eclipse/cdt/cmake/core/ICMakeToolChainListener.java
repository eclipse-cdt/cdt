package org.eclipse.cdt.cmake.core;

/**
 * Listener for toolchain events.
 */
public interface ICMakeToolChainListener {

	void handleCMakeToolChainEvent(CMakeToolChainEvent event);

}
