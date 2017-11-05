/*******************************************************************************
 * Copyright (c) 2017 Institute for Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.cmake.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.cmake.core.internal.Activator;

public class CMakeInstallation implements ICMakeInstallation {

	protected Path path;
	protected String version;
	protected Type type;

	public CMakeInstallation(Path path, Type type) throws IOException {
		this.path = path.toRealPath();
		this.version = parseVersion();
		this.type = type;
	}
	
	@Override
	public Path getRoot() {
		return path;
	}
	
	@Override
	public Path getCMakeCommand() {
		return path.resolve(CMAKE_COMMAND);
	}
	
	@Override
	public Path getCMakeGUICommand() {
		return path.resolve(CMAKE_GUI_COMMAND);
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public int compareTo(ICMakeInstallation other) {
		if (!type.equals(other.getType())) {
			return type.compareTo(other.getType());
		} else if (!version.equals(other.getVersion())) {
			return version.compareTo(other.getVersion());
		}

		return path.compareTo(other.getCMakeCommand());
	}

	private String parseVersion() {
		ProcessBuilder processBuilder = new ProcessBuilder(getCMakeCommand().toString(), "--version");
		String versionLine = null;
		
		try {
			Process cmakeProcess = processBuilder.start();
			BufferedReader cmakeOutput = new BufferedReader(new InputStreamReader(cmakeProcess.getInputStream()));
			cmakeProcess.waitFor();
			versionLine = cmakeOutput.readLine();
		} catch(IOException | InterruptedException e) {
			Activator.log(e);
		}
		
		if (versionLine == null) {
			return null;
		}
		
		Pattern versionPattern = Pattern.compile(".*(\\d+\\.\\d+\\.\\d+).*");
		Matcher versionMatcher = versionPattern.matcher(versionLine);
		if(!versionMatcher.matches()) {
			return null;
		}
		
		return versionMatcher.group(1);
	}

}
