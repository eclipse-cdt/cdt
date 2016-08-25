package org.eclipse.cdt.cmake.core;

import java.nio.file.Path;

public interface ICMakeToolChainFile {

	Path getPath();

	String getProperty(String key);

	void setProperty(String key, String value);

}
