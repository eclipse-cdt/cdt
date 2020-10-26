/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.internal.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.IOsOverrides;

/**
 * Preferences that override/augment the generic properties when running under a
 * specific OS.
 *
 * @author Martin Weber
 */
public abstract class AbstractOsOverrides implements IOsOverrides {

	private String command;
	private boolean useDefaultCommand;
	private CMakeGenerator generator;
	private List<String> extraArguments = new ArrayList<>(0);

	/**
	 * Creates a new object, initialized with all default values.
	 */
	public AbstractOsOverrides() {
		reset();
	}

	/**
	 * Sets each value to its default.
	 */
	public void reset() {
		setCommand("cmake"); //$NON-NLS-1$
		useDefaultCommand = true;
		setGenerator(CMakeGenerator.UnixMakefiles);
		extraArguments.clear();
	}

	@Override
	public final boolean getUseDefaultCommand() {
		return useDefaultCommand;
	}

	@Override
	public void setUseDefaultCommand(boolean useDefaultCommand) {
		this.useDefaultCommand = useDefaultCommand;
	}

	@Override
	public final String getCommand() {
		return command;
	}

	@Override
	public void setCommand(String command) {
		this.command = Objects.requireNonNull(command, "command"); //$NON-NLS-1$
	}

	@Override
	public final CMakeGenerator getGenerator() {
		return generator;
	}

	@Override
	public void setGenerator(CMakeGenerator generator) {
		this.generator = Objects.requireNonNull(generator, "generator"); //$NON-NLS-1$
	}

	@Override
	public final List<String> getExtraArguments() {
		return List.copyOf(extraArguments);
	}

	@Override
	public void setExtraArguments(List<String> extraArguments) {
		this.extraArguments = extraArguments;
	}

}