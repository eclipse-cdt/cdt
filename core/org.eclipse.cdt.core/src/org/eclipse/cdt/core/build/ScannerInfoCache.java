/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.core.resources.IResource;

/**
 * Scanner info for a given build configuration.
 *
 * @since 6.1
 */
public class ScannerInfoCache {

	private static class Command {
		public List<String> command;
		public IExtendedScannerInfo info;
		public List<String> resourcePaths;
	}

	private List<Command> commands;

	private transient Map<List<String>, Command> commandMap = new HashMap<>();
	private transient Map<String, Command> resourceMap = new HashMap<>();

	/**
	 * Initialize the cache of scanner info. Call this after loading this info
	 * using Gson.
	 */
	public void initCache() {
		if (commands == null) {
			commands = new ArrayList<>();
		}

		for (Command command : commands) {
			commandMap.put(command.command, command);
			for (String resourcePath : command.resourcePaths) {
				resourceMap.put(resourcePath, command);
			}
		}
	}

	public IExtendedScannerInfo getScannerInfo(IResource resource) {
		String resourcePath = resource.getLocation().toOSString();
		Command command = resourceMap.get(resourcePath);
		return command != null ? command.info : null;
	}

	public IExtendedScannerInfo getScannerInfo(List<String> commandStrings) {
		Command command = commandMap.get(commandStrings);
		return command != null ? command.info : null;
	}

	public boolean hasCommand(List<String> commandStrings) {
		return commandMap.get(commandStrings) != null;
	}

	public void addScannerInfo(List<String> commandStrings, IExtendedScannerInfo info, IResource resource) {
		// Do I need to remove the resource from an existing command?
		String resourcePath = resource.getLocation().toOSString();
		Command oldCommand = resourceMap.get(resourcePath);
		if (oldCommand != null) {
			if (oldCommand.command.equals(commandStrings)) {
				// duplicate
				return;
			} else {
				oldCommand.resourcePaths.remove(resourcePath);
				if (oldCommand.resourcePaths.isEmpty()) {
					// unused, remove
					commandMap.remove(oldCommand.command);
					commands.remove(oldCommand);
					resourceMap.remove(resourcePath);
				}
			}
		}

		Command command = commandMap.get(commandStrings);
		if (command != null) {
			command.info = info;
			command.resourcePaths.add(resourcePath);
			resourceMap.put(resourcePath, command);
		} else {
			command = new Command();
			command.command = commandStrings;
			command.info = info;
			command.resourcePaths = new ArrayList<>();
			command.resourcePaths.add(resourcePath);
			commands.add(command);
			commandMap.put(commandStrings, command);
			resourceMap.put(resourcePath, command);
		}
	}

	/**
	 * @since 6.3
	 */
	public boolean hasResource(List<String> commandStrings, IResource resource) {
		String resourcePath = resource.getLocation().toOSString();
		Command command = commandMap.get(commandStrings);
		if (command == null) {
			return false;
		}
		return command.resourcePaths.contains(resourcePath);
	}

	public void addResource(List<String> commandStrings, IResource resource) {
		String resourcePath = resource.getLocation().toOSString();
		Command command = commandMap.get(commandStrings);
		Command current = resourceMap.get(resourcePath);
		if (current != null) {
			if (!current.equals(command)) {
				// remove from old command
				current.resourcePaths.remove(resourcePath);
				if (current.resourcePaths.isEmpty()) {
					commands.remove(current);
					commandMap.remove(current.command);
				}
			} else {
				// we're already there
				return;
			}
		}
		command.resourcePaths.add(resource.getLocation().toOSString());
		resourceMap.put(resourcePath, command);
	}

	/**
	 * @since 6.4
	 */
	public void removeResource(IResource resource) {
		String resourcePath = resource.getLocation().toOSString();
		Command command = resourceMap.get(resourcePath);
		if (command != null) {
			command.resourcePaths.remove(resourcePath);
			if (command.resourcePaths.isEmpty()) {
				commands.remove(command);
				commandMap.remove(command.command);
			}
			resourceMap.remove(resourcePath);
		}
	}

	/**
	 * @since 6.4
	 */
	public void removeCommand(List<String> commandStrings) {
		Command command = commandMap.remove(commandStrings);
		if (command != null) {
			commands.remove(command);
			for (String resourcePath : command.resourcePaths) {
				Command current = resourceMap.get(resourcePath);
				if (current.equals(command)) {
					resourceMap.remove(resourcePath);
				}
			}
		}
	}

}
