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

import java.util.Objects;

import org.eclipse.cdt.cmake.core.properties.ICMakeProperties;

/**
 * Holds project Properties for cmake. Follows the Java-Beans pattern to make (de-)serialization easier.
 *
 * @author Martin Weber
 */
public class CMakePropertiesBean implements ICMakeProperties {

	private boolean warnNoDev, debugTryCompile, debugOutput, trace, warnUnitialized, warnUnused;
	private String cacheFile;
	private boolean clearCache;
	private String extraArguments = ""; //$NON-NLS-1$
	private LinuxOverrides linuxOverrides = new LinuxOverrides();
	private WindowsOverrides windowsOverrides = new WindowsOverrides();
	private String buildType;

	/**
	 * Creates a new object, initialized with all default values.
	 */
	public CMakePropertiesBean() {
		reset(true);
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
	public boolean isWarnUnitialized() {
		return warnUnitialized;
	}

	@Override
	public void setWarnUnitialized(boolean warnUnitialized) {
		this.warnUnitialized = warnUnitialized;
	}

	@Override
	public boolean isWarnUnused() {
		return warnUnused;
	}

	@Override
	public void setWarnUnused(boolean warnUnused) {
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
	public String getExtraArguments() {
		return extraArguments;
	}

	@Override
	public void setExtraArguments(String extraArguments) {
		this.extraArguments = Objects.requireNonNull(extraArguments);
	}

	@Override
	public LinuxOverrides getLinuxOverrides() {
		return linuxOverrides;
	}

	public void setLinuxOverrides(LinuxOverrides linuxOverrides) {
		this.linuxOverrides = linuxOverrides;
	}

	@Override
	public WindowsOverrides getWindowsOverrides() {
		return windowsOverrides;
	}

	public void setWindowsOverrides(WindowsOverrides windowsOverrides) {
		this.windowsOverrides = windowsOverrides;
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
	public void reset(boolean resetOsOverrides) {
		buildType = null;
		clearCache = false;
		debugOutput = false;
		debugTryCompile = false;
		trace = false;
		warnNoDev = false;
		warnUnitialized = false;
		warnUnused = false;
		extraArguments = ""; //$NON-NLS-1$
		cacheFile = null;

		if (resetOsOverrides) {
			linuxOverrides.reset();
			windowsOverrides.reset();
		}
	}
}
