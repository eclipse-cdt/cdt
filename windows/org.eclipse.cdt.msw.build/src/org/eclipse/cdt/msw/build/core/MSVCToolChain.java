/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.msw.build.core;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.osgi.framework.Version;

public class MSVCToolChain extends PlatformObject implements IToolChain {

	private final IToolChainProvider provider;
	private final Path path;
	private final Map<String, String> properties = new HashMap<>();
	private final String id;
	private final String arch;
	private final String version;
	private final IEnvironmentVariable pathVar;
	private final IEnvironmentVariable includeVar;
	private final IEnvironmentVariable libVar;
	private final String[] includeDirs;
	private final Map<String, String> symbols;

	public MSVCToolChain(IToolChainProvider provider, Path path) {
		this.provider = provider;
		this.path = path;

		// path = <version>/bin/<hostArch>/<targetArch>
		String targetArch = path.getFileName().toString();
		this.arch = targetArch.equalsIgnoreCase("x86") ? Platform.ARCH_X86 : Platform.ARCH_X86_64; //$NON-NLS-1$
		this.id = "msvc." + arch; //$NON-NLS-1$
		this.version = path.getParent().getParent().getParent().getFileName().toString();

		Path kitRoot = Paths.get("C:", "Program Files (x86)", "Windows Kits", "10"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		List<String> versions = Arrays.asList(kitRoot.resolve("lib").toFile().list()); //$NON-NLS-1$
		Collections.sort(versions, (v1, v2) -> {
			Version ver1;
			try {
				ver1 = new Version(v1);
			} catch (IllegalArgumentException e) {
				return 1;
			}

			Version ver2;
			try {
				ver2 = new Version(v2);
			} catch (IllegalArgumentException e) {
				return -1;
			}

			return ver2.compareTo(ver1);
		});
		String sdkVersion = versions.iterator().next();

		pathVar = new EnvironmentVariable("Path", String.join(File.pathSeparator, //$NON-NLS-1$
				path.toString(), kitRoot.resolve("bin").resolve(sdkVersion).resolve(targetArch).toString() //$NON-NLS-1$
		), IEnvironmentVariable.ENVVAR_PREPEND, File.pathSeparator);

		this.includeDirs = new String[] { path.getParent().getParent().getParent().resolve("include").toString(), //$NON-NLS-1$
				kitRoot.resolve("include").resolve(sdkVersion).resolve("ucrt").toString(), //$NON-NLS-1$ //$NON-NLS-2$
				kitRoot.resolve("include").resolve(sdkVersion).resolve("shared").toString(), //$NON-NLS-1$ //$NON-NLS-2$
				kitRoot.resolve("include").resolve(sdkVersion).resolve("um").toString(), //$NON-NLS-1$ //$NON-NLS-2$
				kitRoot.resolve("include").resolve(sdkVersion).resolve("winrt").toString() //$NON-NLS-1$ //$NON-NLS-2$
		};

		includeVar = new EnvironmentVariable("INCLUDE", String.join(File.pathSeparator, this.includeDirs), //$NON-NLS-1$
				IEnvironmentVariable.ENVVAR_REPLACE, File.pathSeparator);

		libVar = new EnvironmentVariable("LIB", String.join(File.pathSeparator, //$NON-NLS-1$
				path.getParent().getParent().getParent().resolve("lib").resolve(targetArch).toString(), //$NON-NLS-1$
				kitRoot.resolve("lib").resolve(sdkVersion).resolve("ucrt").resolve(targetArch).toString(), //$NON-NLS-1$ //$NON-NLS-2$
				kitRoot.resolve("lib").resolve(sdkVersion).resolve("um").resolve(targetArch).toString() //$NON-NLS-1$ //$NON-NLS-2$
		), IEnvironmentVariable.ENVVAR_REPLACE, File.pathSeparator);

		symbols = new HashMap<>();
		symbols.put("_WIN32", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		if (this.arch.equals(Platform.ARCH_X86)) {
			symbols.put("_M_IX86", "600"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			symbols.put("_WIN64", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			symbols.put("_M_X64", "100"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// TODO make this more dynamic to actual version
		symbols.put("_MSC_VER", "1900"); //$NON-NLS-1$ //$NON-NLS-2$

		// Microsoft specific modifiers that can be ignored
		symbols.put("__cdecl", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__fastcall", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__restrict", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__sptr", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__stdcall", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__unaligned", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__uptr", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__w64", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__clrcall", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__thiscall", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__vectorcall", ""); //$NON-NLS-1$ //$NON-NLS-2$

		// Redefine some things so that the CDT parser can handle them, until there is a VC specific parser
		symbols.put("__forceinline", "__inline"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__int8", "char"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__int16", "short"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__int32", "int"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__int64", "long long"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__pragma(X)", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__nullptr", "nullptr"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__debugbreak()", "0/0"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__LPREFIX(A)", "L\"##A\""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public IToolChainProvider getProvider() {
		return provider;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getName() {
		return "Visual C++";
	}

	@Override
	public String getProperty(String key) {
		String value = properties.get(key);
		if (value != null) {
			return value;
		}

		// By default, we're a local GCC
		switch (key) {
		case ATTR_OS:
			return Platform.OS_WIN32;
		case ATTR_ARCH:
			return arch;
		}

		return null;
	}

	@Override
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}

	@Override
	public IEnvironmentVariable[] getVariables() {
		return new IEnvironmentVariable[] { pathVar, includeVar, libVar };
	}

	@Override
	public IEnvironmentVariable getVariable(String name) {
		switch (name) {
		case "PATH": //$NON-NLS-1$
		case "Path": //$NON-NLS-1$
			return pathVar;
		default:
			return null;
		}
	}

	@Override
	public String[] getErrorParserIds() {
		return new String[] { CCorePlugin.PLUGIN_ID + ".VCErrorParser" //$NON-NLS-1$
		};
	}

	@Override
	public String getBinaryParserId() {
		return CCorePlugin.PLUGIN_ID + ".PE"; //$NON-NLS-1$
	}

	@Override
	public Path getCommandPath(Path command) {
		if (command.isAbsolute()) {
			return command;
		} else {
			return this.path.resolve(command);
		}
	}

	@Override
	public String[] getCompileCommands() {
		return new String[] { "cl", "cl.exe" //$NON-NLS-1$ //$NON-NLS-2$
		};
	}

	@Override
	public IExtendedScannerInfo getDefaultScannerInfo(IBuildConfiguration buildConfig,
			IExtendedScannerInfo baseScannerInfo, ILanguage language, URI buildDirectoryURI) {
		return new ExtendedScannerInfo(symbols, includeDirs);
	}

	@Override
	public IExtendedScannerInfo getScannerInfo(IBuildConfiguration buildConfig, List<String> command,
			IExtendedScannerInfo baseScannerInfo, IResource resource, URI buildDirectoryURI) {
		Map<String, String> symbols = new HashMap<>(this.symbols);
		List<String> includeDirs = new ArrayList<>(Arrays.asList(this.includeDirs));

		for (String arg : command) {
			if (arg.startsWith("-") || arg.startsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
				if (arg.charAt(1) == 'I') {
					includeDirs.add(arg.substring(2));
				} else if (arg.charAt(1) == 'D') {
					String[] define = arg.substring(2).split("="); //$NON-NLS-1$
					if (define.length == 1) {
						symbols.put(define[0], "1"); //$NON-NLS-1$
					} else {
						symbols.put(define[0], define[1]);
					}
				}
			}
		}

		return new ExtendedScannerInfo(symbols, includeDirs.toArray(new String[includeDirs.size()]));
	}

	@Override
	public IResource[] getResourcesFromCommand(List<String> command, URI buildDirectoryURI) {
		// Start at the back looking for arguments
		// TODO this was copied from the GCCToolChain, good candidate for the default implementation
		List<IResource> resources = new ArrayList<>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (int i = command.size() - 1; i >= 0; --i) {
			String arg = command.get(i);
			if (arg.startsWith("-") || arg.startsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
				// ran into an option, we're done.
				break;
			}
			Path srcPath = Paths.get(arg);
			URI uri;
			if (srcPath.isAbsolute()) {
				uri = srcPath.toUri();
			} else {
				try {
					uri = buildDirectoryURI.resolve(arg);
				} catch (IllegalArgumentException e) {
					// Bad URI
					continue;
				}
			}

			for (IFile resource : root.findFilesForLocationURI(uri)) {
				resources.add(resource);
			}
		}

		return resources.toArray(new IResource[resources.size()]);
	}
}
