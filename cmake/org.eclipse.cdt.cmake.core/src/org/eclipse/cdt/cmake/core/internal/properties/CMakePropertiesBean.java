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

import org.eclipse.cdt.cmake.core.CMakeBuildConfiguration;
import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.ICMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.ICMakeProperties;

/**
 * Holds project Properties for cmake. Follows the Java-Beans pattern to make (de-)serialization easier.
 *
 * @author Martin Weber
 */
public class CMakePropertiesBean implements ICMakeProperties {

	private String command = CMakeBuildConfiguration.CMAKE_BUILD_COMMAND_DEFAULT;
	private ICMakeGenerator generator = CMakeGenerator.getGenerator(CMakeBuildConfiguration.CMAKE_GENERATOR_DEFAULT);
	private boolean warnNoDev = false, debugTryCompile = false, debugOutput = false, trace = false,
			warnUninitialized = false, warnUnused = false;
	private String cacheFile = ""; //$NON-NLS-1$
	private boolean clearCache = false;
	private List<String> extraArguments = new ArrayList<>(0);
	private String buildType;
	private String allTarget = CMakeBuildConfiguration.CMAKE_ALL_TARGET_DEFAULT;
	private String cleanTarget = CMakeBuildConfiguration.CMAKE_CLEAN_TARGET_DEFAULT;

	/**
	 * Creates a new object, initialized with all default values.
	 */
	public CMakePropertiesBean() {
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
	public final ICMakeGenerator getGenerator() {
		return generator;
	}

	@Override
	public void setGenerator(ICMakeGenerator generator) {
		this.generator = Objects.requireNonNull(generator, "generator"); //$NON-NLS-1$
	}

	@Override
	public boolean isWarnNoDev() {
		return warnNoDev;
	}

	@Override
	public void setWarnNoDev(boolean warnNoDev) {
		this.warnNoDev = warnNoDev;
	}

	@Override
	public boolean isDebugTryCompile() {
		return debugTryCompile;
	}

	@Override
	public void setDebugTryCompile(boolean debugTryCompile) {
		this.debugTryCompile = debugTryCompile;
	}

	@Override
	public boolean isDebugOutput() {
		return debugOutput;
	}

	@Override
	public void setDebugOutput(boolean debugOutput) {
		this.debugOutput = debugOutput;
	}

	@Override
	public boolean isTrace() {
		return trace;
	}

	@Override
	public void setTrace(boolean trace) {
		this.trace = trace;
	}

	@Override
	public boolean isWarnUninitialized() {
		return warnUninitialized;
	}

	@Override
	public void setWarnUninitialized(boolean warnUninitialized) {
		this.warnUninitialized = warnUninitialized;
	}

	@Override
	public boolean isWarnUnusedVars() {
		return warnUnused;
	}

	@Override
	public void setWarnUnusedVars(boolean warnUnused) {
		this.warnUnused = warnUnused;
	}

	@Override
	public String getBuildType() {
		return buildType;
	}

	@Override
	public void setBuildType(String buildType) {
		this.buildType = buildType;
	}

	@Override
	public List<String> getExtraArguments() {
		return List.copyOf(extraArguments);
	}

	@Override
	public void setExtraArguments(List<String> extraArguments) {
		this.extraArguments = extraArguments;
	}

	@Override
	public String getCacheFile() {
		return cacheFile;
	}

	@Override
	public void setCacheFile(String cacheFile) {
		this.cacheFile = cacheFile;
	}

	@Override
	public boolean isClearCache() {
		return clearCache;
	}

	@Override
	public void setClearCache(boolean clearCache) {
		this.clearCache = clearCache;
	}

	@Override
	public String getCleanTarget() {
		return cleanTarget;
	}

	@Override
	public void setCleanTarget(String cleanTarget) {
		this.cleanTarget = cleanTarget;
	}

	@Override
	public String getAllTarget() {
		return allTarget;
	}

	@Override
	public void setAllTarget(String allTarget) {
		this.allTarget = allTarget;
	}
}
