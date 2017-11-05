/*******************************************************************************
 * Copyright (c) 2017 Institute for Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.cdt.cmake.core.CMakeInstallation;
import org.eclipse.cdt.cmake.core.ICMakeInstallation;
import org.eclipse.cdt.cmake.core.ICMakeInstallation.Type;
import org.eclipse.cdt.cmake.core.ICMakeInstallationProvider;

public class CMakeSystemInstallationProvider implements ICMakeInstallationProvider {

	private List<ICMakeInstallation> installations = null;

	@Override
	public Collection<ICMakeInstallation> getInstallations() {
		return installations;
	}

	@Override
	public void init() {
		if(installations == null) {
			installations = new ArrayList<>();
			final Map<String, String> environment = System.getenv();
		
			List<Path> roots = environment.entrySet().stream()
				.filter(entry -> entry.getKey().equals("PATH") || entry.getKey().equals("Path"))
				.map(Map.Entry::getValue)
				.map(variable -> variable.split(File.pathSeparator))
				.flatMap(Arrays::stream)
				.map(Paths::get)
				.filter(path -> Files.exists(path.resolve(ICMakeInstallation.CMAKE_COMMAND)))
				.filter(Files::exists)
				.filter(Files::isExecutable)
				.collect(Collectors.toList());
			
			for(Path root : roots) {
				try {
					installations.add(new CMakeInstallation(root, Type.SYSTEM));
				} catch (IOException e) {
					Activator.log(e);
				}
			}
			}
	}

}
