/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.builder.internal.CBuildConfigPoint;
import org.eclipse.cdt.core.builder.internal.CBuildVariablePoint;
import org.eclipse.cdt.core.builder.internal.CToolPoint;
import org.eclipse.cdt.core.builder.internal.CToolTypePoint;
import org.eclipse.cdt.core.builder.internal.CToolchainPoint;
import org.eclipse.cdt.core.builder.model.ICToolType;
import org.eclipse.cdt.core.builder.model.internal.CBuildConfigManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;

/**
 * Fragments aren't first class citizens in the Eclipse world.
 * This class lets me write code in the experimental builder
 * fragment as if there was a real plugin class that implemented
 * some of these methods.
 * <p>
 * Also - I'm not sure that some of these methods don't belong
 * elsewhere.  Suggestions are welcome.
 */
public class BuilderPlugin {

	// Pretend this is a real plugin.
	static private BuilderPlugin thisPlugin;

	// Pretend this is a real plugin.
	static {
		thisPlugin = new BuilderPlugin();
		thisPlugin.loadToolTypes();
		thisPlugin.loadToolProviders();
		thisPlugin.loadToolchainProviders();
		thisPlugin.loadConfigProviders();
		thisPlugin.loadBuildVarProviders();
		thisPlugin.dump();
	}

	// Pretend this is a real plugin.
	private BuilderPlugin() {
		fBuildConfigManager = new CBuildConfigManager();
	}

	// Pretend this is a real plugin.
	private void dump() {
		for (Iterator iter = fToolTypes.entrySet().iterator();
			iter.hasNext();
			) {
			ICToolType element =
				(ICToolType) ((Map.Entry) iter.next()).getValue();
			System.err.println(
				"Tool type ("
					+ element.getName()
					+ ", "
					+ element.getId()
					+ ")");
		}
		for (Iterator iter = fToolProviders.entrySet().iterator();
			iter.hasNext();
			) {
			ICToolPoint element =
				(ICToolPoint) ((Map.Entry) iter.next()).getValue();
			System.err.println(
				"Tool ("
					+ element.getName()
					+ ", "
					+ element.getId()
					+ ", "
					+ element.getProviderClassName()
					+ ")");
		}
		for (Iterator iter = fToolchainProviders.entrySet().iterator();
			iter.hasNext();
			) {
			ICToolchainPoint element =
				(ICToolchainPoint) ((Map.Entry) iter.next()).getValue();
			System.err.println(
				"Toolchain ("
					+ element.getId()
					+ ", "
					+ element.getProviderClassName()
					+ ")");
		}
		for (Iterator iter = fBuildConfigProviders.entrySet().iterator();
			iter.hasNext();
			) {
			ICBuildConfigPoint element =
				(ICBuildConfigPoint) ((Map.Entry) iter.next()).getValue();
			System.err.println(
				"BuildConfig ("
					+ element.getName()
					+ ", "
					+ element.getId()
					+ ", "
					+ element.getProviderClassName()
					+ ")");
		}
		for (Iterator iter = fBuildVarProviders.entrySet().iterator();
			iter.hasNext();
			) {
			ICBuildVariablePoint element =
				(ICBuildVariablePoint) ((Map.Entry) iter.next()).getValue();
			System.err.println(
				"BuildVar ("
					+ element.getId()
					+ ", "
					+ element.getProviderClassName()
					+ ")");
		}
	}

	// Pretend this is a real plugin.
	static public BuilderPlugin getDefault() {
		return thisPlugin;
	}

	// Pretend this is a real plugin.
	public IPluginDescriptor getDescriptor() {
		return CCorePlugin.getDefault().getDescriptor();
	}

	/*
	 * Data and methods to merge with CCorePlugin 
	 */

	private CBuildConfigManager fBuildConfigManager;
	private Map fToolTypes;
	private Map fToolProviders;
	private Map fToolchainProviders;
	private Map fBuildConfigProviders;
	private Map fBuildVarProviders;

	public CBuildConfigManager getBuildConfigurationManager() {
		return fBuildConfigManager;
	}

	public Map getToolTypes() {
		return fToolTypes;
	}

	public Map getToolProviders() {
		return fToolProviders;
	}

	public Map getToolchainProviders() {
		return fToolchainProviders;
	}

	public Map getBuildConfigurationProviders() {
		return fBuildConfigProviders;
	}

	public Map getBuildVariableProviders() {
		return fBuildVarProviders;
	}

	private void loadToolTypes() {
		IPluginDescriptor descriptor = getDefault().getDescriptor();
		IExtensionPoint extensionPoint =
			descriptor.getExtensionPoint("CToolType");
		IExtension[] exts = extensionPoint.getExtensions();
		IConfigurationElement[] infos =
			extensionPoint.getConfigurationElements();
		fToolTypes = new HashMap(infos.length);
		for (int i = 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			CToolTypePoint provider = new CToolTypePoint(configurationElement);
			fToolTypes.put(provider.getId(), provider);
		}
	}

	private void loadToolProviders() {
		IPluginDescriptor descriptor = getDefault().getDescriptor();
		IExtensionPoint extensionPoint = descriptor.getExtensionPoint("CTool");
		IConfigurationElement[] infos =
			extensionPoint.getConfigurationElements();
		fToolProviders = new HashMap(infos.length);
		for (int i = 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			CToolPoint provider = new CToolPoint(configurationElement);
			fToolProviders.put(provider.getId(), provider);
		}
	}

	private void loadToolchainProviders() {
		IPluginDescriptor descriptor = getDefault().getDescriptor();
		IExtensionPoint extensionPoint =
			descriptor.getExtensionPoint("CToolchain");
		IConfigurationElement[] infos =
			extensionPoint.getConfigurationElements();
		fToolchainProviders = new HashMap(infos.length);
		for (int i = 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			CToolchainPoint provider =
				new CToolchainPoint(configurationElement);
			fToolchainProviders.put(provider.getId(), provider);
		}
	}

	private void loadConfigProviders() {
		IPluginDescriptor descriptor = getDefault().getDescriptor();
		IExtensionPoint extensionPoint =
			descriptor.getExtensionPoint("CBuildConfig");
		IConfigurationElement[] infos =
			extensionPoint.getConfigurationElements();
		fBuildConfigProviders = new HashMap(infos.length);
		for (int i = 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			CBuildConfigPoint provider =
				new CBuildConfigPoint(configurationElement);
			fBuildConfigProviders.put(provider.getId(), provider);
		}
	}

	private void loadBuildVarProviders() {
		IPluginDescriptor descriptor = getDefault().getDescriptor();
		IExtensionPoint extensionPoint =
			descriptor.getExtensionPoint("CBuildVariable");
		IConfigurationElement[] infos =
			extensionPoint.getConfigurationElements();
		fBuildVarProviders = new HashMap(infos.length);
		for (int i = 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			CBuildVariablePoint provider =
				new CBuildVariablePoint(configurationElement);
			fBuildVarProviders.put(provider.getId(), provider);
		}
	}

}
