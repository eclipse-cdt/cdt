/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qmldir;

import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.eclipse.cdt.internal.qt.core.Activator;

public class QMLDirectoryInfo {
	public static class Module {
		private final String name;
		private final String initialVersion;

		public Module(String name, String ver) {
			this.name = name;
			this.initialVersion = ver;
		}

		public String getName() {
			return name;
		}

		public String getInitialVersion() {
			return initialVersion;
		}
	}

	public static class Plugin {
		private final String name;
		private final Path path;

		private Plugin(String name, String path) {
			this.name = name;
			Path p = null;
			if (path != null) {
				try {
					p = Paths.get(path);
				} catch (InvalidPathException e) {
					Activator.log(e);
				}
			}
			this.path = p;
		}

		public String getName() {
			return name;
		}

		public Path getRelativePath() {
			return path;
		}
	}

	public static class ResourceFile {
		private final String name;
		private final boolean internal;
		private final boolean singleton;
		private final String initialVersion;

		private ResourceFile(String name, String ver, boolean internal, boolean singleton) {
			this.name = name;
			this.initialVersion = ver;
			this.internal = internal;
			this.singleton = singleton;
		}

		public String getName() {
			return name;
		}

		public String getInitialVersion() {
			return initialVersion;
		}

		public boolean isSingleton() {
			return singleton;
		}

		public boolean isInternal() {
			return internal;
		}
	}

	private String moduleIdentifier;
	private Plugin plugin;
	private String classname;
	private String typeInfo;
	private final Collection<Module> depends;
	private final Collection<ResourceFile> resources;
	private boolean designersupported;

	public QMLDirectoryInfo(InputStream input) {
		this.depends = new LinkedList<>();
		this.resources = new LinkedList<>();

		IQDirAST ast = new QMLDirectoryParser().parse(input);
		for (IQDirCommand c : ast.getCommands()) {
			if (c instanceof IQDirModuleCommand) {
				if (moduleIdentifier == null) {
					moduleIdentifier = ((IQDirModuleCommand) c).getModuleIdentifier().getText();
				}
			} else if (c instanceof IQDirPluginCommand) {
				if (plugin == null) {
					IQDirPluginCommand pc = (IQDirPluginCommand) c;
					plugin = new Plugin(pc.getName().getText(), pc.getPath() != null ? pc.getPath().getText() : null);
				}
			} else if (c instanceof IQDirTypeInfoCommand) {
				if (typeInfo == null) {
					typeInfo = ((IQDirTypeInfoCommand) c).getFile().getText();
				}
			} else if (c instanceof IQDirResourceCommand) {
				IQDirResourceCommand rc = (IQDirResourceCommand) c;
				resources.add(new ResourceFile(rc.getFile().getText(), rc.getInitialVersion().getVersionString(), false,
						false));
			} else if (c instanceof IQDirInternalCommand) {
				IQDirInternalCommand rc = (IQDirInternalCommand) c;
				resources.add(new ResourceFile(rc.getFile().getText(), null, true, false));
			} else if (c instanceof IQDirSingletonCommand) {
				IQDirSingletonCommand rc = (IQDirSingletonCommand) c;
				resources.add(new ResourceFile(rc.getFile().getText(), rc.getInitialVersion().getVersionString(), false,
						true));
			} else if (c instanceof IQDirDependsCommand) {
				IQDirDependsCommand dc = (IQDirDependsCommand) c;
				depends.add(new Module(dc.getModuleIdentifier().getText(), dc.getInitialVersion().getVersionString()));
			} else if (c instanceof IQDirClassnameCommand) {
				if (classname == null) {
					classname = ((IQDirClassnameCommand) c).getIdentifier().getText();
				}
			} else if (c instanceof IQDirDesignerSupportedCommand) {
				designersupported = true;
			}
		}
	}

	public String getModuleIdentifier() {
		return moduleIdentifier;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public String getClassname() {
		return classname;
	}

	public String getTypesFileName() {
		return typeInfo;
	}

	public Collection<Module> getDependentModules() {
		return Collections.unmodifiableCollection(depends);
	}

	public Collection<ResourceFile> getResources() {
		return Collections.unmodifiableCollection(resources);
	}

	public boolean isDesignersupported() {
		return designersupported;
	}
}
