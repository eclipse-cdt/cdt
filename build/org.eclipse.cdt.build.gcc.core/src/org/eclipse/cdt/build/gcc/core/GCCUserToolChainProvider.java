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
package org.eclipse.cdt.build.gcc.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.build.gcc.core.internal.Activator;
import org.eclipse.cdt.build.gcc.core.internal.Messages;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IUserToolChainProvider;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GCCUserToolChainProvider implements IUserToolChainProvider {

	public static final String PROVIDER_ID = "org.eclipse.cdt.build.gcc.core.provider.user"; //$NON-NLS-1$

	private static final String ARCH = "arch"; //$NON-NLS-1$
	private static final String DELIMITER = "delimiter"; //$NON-NLS-1$
	private static final String ENVIRONMENT = "environment"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String OPERATION = "operation"; //$NON-NLS-1$
	private static final String PATH = "path"; //$NON-NLS-1$
	private static final String PROPERTIES = "properties"; //$NON-NLS-1$
	private static final String TYPE = "type"; //$NON-NLS-1$
	private static final String VALUE = "value"; //$NON-NLS-1$

	private IToolChainManager manager;
	private JsonArray toolChains;

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	private File getJsonFile() {
		return Activator.getPlugin().getStateLocation().append("toolchains.json").toFile(); //$NON-NLS-1$
	}

	@Override
	public void init(IToolChainManager manager) throws CoreException {
		this.manager = manager;

		// Load up the magic JSON file which contains our toolchain defs
		try {
			File jsonFile = getJsonFile();
			if (jsonFile.exists()) {
				toolChains = new JsonParser().parse(new FileReader(jsonFile)).getAsJsonArray();
				for (JsonElement element : toolChains) {
					JsonObject tc = element.getAsJsonObject();
					String type;
					if (tc.has(TYPE)) {
						type = tc.get(TYPE).getAsString();
					} else {
						type = GCCToolChain.TYPE_ID;
					}
					String arch;
					if (tc.has(ARCH)) {
						arch = tc.get(ARCH).getAsString();
					} else {
						arch = null;
					}
					Path path = Paths.get(tc.get(PATH).getAsString());
					IEnvironmentVariable[] envvars = null;
					if (tc.has(ENVIRONMENT)) {
						List<IEnvironmentVariable> envlist = new ArrayList<>();
						for (JsonElement var : tc.get(ENVIRONMENT).getAsJsonArray()) {
							JsonObject varobj = var.getAsJsonObject();
							String name = varobj.get(NAME).getAsString();
							int operation = varobj.get(OPERATION).getAsInt();
							String value = null;
							if (varobj.has(VALUE)) {
								value = varobj.get(VALUE).getAsString();
							}
							String delimiter = null;
							if (varobj.has(DELIMITER)) {
								delimiter = varobj.get(DELIMITER).getAsString();
							}
							envlist.add(new EnvironmentVariable(name, value, operation, delimiter));
						}
						envvars = envlist.toArray(new IEnvironmentVariable[0]);
					}

					GCCToolChain gcc = null;
					switch (type) {
					case GCCToolChain.TYPE_ID:
						gcc = new GCCToolChain(this, path, arch, envvars);
						break;
					case ClangToolChain.TYPE_ID:
						gcc = new ClangToolChain(this, path, arch, envvars);
						break;
					}
					if (gcc != null) {
						if (tc.has(PROPERTIES)) {
							for (JsonElement prop : tc.get(PROPERTIES).getAsJsonArray()) {
								JsonObject propobj = prop.getAsJsonObject();
								gcc.setProperty(propobj.get(NAME).getAsString(), propobj.get(VALUE).getAsString());
							}
						}
						manager.addToolChain(gcc);
					}
				}
			}
		} catch (IOException | IllegalStateException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, Activator.getId(), Messages.GCCUserToolChainProvider_Loading, e));
		}
	}

	@Override
	public void addToolChain(IToolChain toolChain) throws CoreException {
		if (!(toolChain instanceof GCCToolChain)) {
			throw new CoreException(
					new Status(IStatus.ERROR, Activator.getId(), Messages.GCCUserToolChainProvider_NotOurs));
		}

		GCCToolChain gcc = (GCCToolChain) toolChain;

		// Persist the toolchain
		if (toolChains == null) {
			toolChains = new JsonArray();
		}

		JsonObject newtc = new JsonObject();
		toolChains.add(newtc);

		newtc.addProperty(ID, gcc.getId());
		newtc.addProperty(ARCH, gcc.getProperty(IToolChain.ATTR_ARCH));
		newtc.addProperty(PATH, gcc.getPath().toString());

		Map<String, String> properties = gcc.getProperties();
		if (properties != null && !properties.isEmpty()) {
			JsonArray props = new JsonArray();
			newtc.add(PROPERTIES, props);
			for (Entry<String, String> entry : gcc.getProperties().entrySet()) {
				JsonObject prop = new JsonObject();
				props.add(prop);
				prop.addProperty(NAME, entry.getKey());
				prop.addProperty(VALUE, entry.getValue());
			}
		}

		IEnvironmentVariable[] envvars = gcc.getVariables();
		if (envvars != null && envvars.length > 0) {
			JsonArray env = new JsonArray();
			newtc.add(ENVIRONMENT, env);
			for (IEnvironmentVariable var : gcc.getVariables()) {
				JsonObject envvar = new JsonObject();
				env.add(envvar);

				envvar.addProperty(NAME, var.getName());
				envvar.addProperty(OPERATION, var.getOperation());
				String value = var.getValue();
				if (value != null) {
					envvar.addProperty(VALUE, value);
				}

				String delimiter = var.getDelimiter();
				if (delimiter != null) {
					envvar.addProperty(DELIMITER, delimiter);
				}
			}
		}

		try {
			saveJsonFile();
		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, Activator.getId(), Messages.GCCUserToolChainProvider_Saving, e));
		}

		manager.addToolChain(toolChain);
	}

	@Override
	public void removeToolChain(IToolChain toolChain) throws CoreException {
		if (toolChains != null) {
			String id = toolChain.getId();
			JsonArray copy = new JsonArray();
			copy.addAll(toolChains);
			for (JsonElement element : copy) {
				JsonObject tc = element.getAsJsonObject();
				if (id.equals(tc.get(ID).getAsString())) {
					toolChains.remove(element);
				}
			}

			try {
				saveJsonFile();
			} catch (IOException e) {
				throw new CoreException(
						new Status(IStatus.ERROR, Activator.getId(), Messages.GCCUserToolChainProvider_Saving1, e));
			}
		}
		manager.removeToolChain(toolChain);
	}

	private void saveJsonFile() throws IOException {
		try (Writer writer = new FileWriter(getJsonFile())) {
			writer.write(new Gson().toJson(toolChains));
		}
	}
}
