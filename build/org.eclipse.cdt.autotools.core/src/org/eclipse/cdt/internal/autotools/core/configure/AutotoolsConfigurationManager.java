/*******************************************************************************
 * Copyright (c) 2009, 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.autotools.core.AutotoolsNewProjectNature;
import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.autotools.core.IAutotoolsOption;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfiguration.Option;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AutotoolsConfigurationManager implements IResourceChangeListener {

	public static final String CFG_FILE_NAME = ".autotools"; //$NON-NLS-1$
	private static final String CFG_CANT_SAVE = "Configure.Error.NoProjectToSave"; //$NON-NLS-1$

	/**
	 * @since 1.2
	 */
	public static final String INVALID_AUTOTOOLS_PROJECT = "CfgOptions.Invalid.Project"; //$NON-NLS-1$
	/**
	 * @since 1.2
	 */

	public static final String INVALID_AUTOTOOLS_CONFIG_ID = "CfgOptions.Invalid.Config"; //$NON-NLS-1$

	private static AutotoolsConfigurationManager instance;
	private static Random rand = new Random();

	private boolean isSyncing;

	private static Map<String, Map<String, IAConfiguration>> configs;
	private static Map<String, Map<String, IAConfiguration>> tmpConfigs;

	private AutotoolsConfigurationManager() {
		configs = new HashMap<>();
		tmpConfigs = new HashMap<>();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public static AutotoolsConfigurationManager getInstance() {
		if (instance == null) {
			instance = new AutotoolsConfigurationManager();
		}
		return instance;
	}

	public synchronized IAConfiguration createDefaultConfiguration(String id) {
		return new AutotoolsConfiguration(id);
	}

	public synchronized IAConfiguration createDefaultConfiguration(IProject project, String id) {
		return new AutotoolsConfiguration(project, id);
	}

	public synchronized IAConfiguration findCfg(IProject p, String id) {
		Map<String, IAConfiguration> cfgs = getConfigurations(p);
		return cfgs.get(id);
	}

	public synchronized IAConfiguration getConfiguration(IProject p, String cfgId) {
		return getConfiguration(p, cfgId, true);
	}

	public synchronized IAConfiguration getConfiguration(IProject p, String cfgId, boolean persist) {
		IAConfiguration cfg = findCfg(p, cfgId);
		if (cfg == null) {
			cfg = createDefaultConfiguration(p, cfgId);
			if (persist) {
				addConfiguration(p, cfg);
			}
		} else {
			if (!persist) {
				cfg = cfg.copy();
			}
		}
		return cfg;
	}

	public synchronized boolean isConfigurationAlreadySaved(IProject project, ICConfigurationDescription cfgd) {
		Map<String, IAConfiguration> cfgs = getSavedConfigs(project);
		if (cfgs != null)
			return cfgs.get(cfgd.getId()) != null;
		return false;
	}

	public synchronized void addConfiguration(IProject project, IAConfiguration cfg) {
		String projectName = project.getName();
		Map<String, IAConfiguration> cfgs = getSavedConfigs(project);
		if (cfgs == null) {
			cfgs = new HashMap<>();
			configs.put(projectName, cfgs);
		}
		cfgs.put(cfg.getId(), cfg);
		saveConfigs(project);
	}

	public synchronized boolean isSyncing() {
		return isSyncing;
	}

	private synchronized void setSyncing(boolean value) {
		isSyncing = value;
	}

	/**
	 * Synchronize the current set of configurations for the project with the
	 * Autotools saved configuration data.  This is required when configuration
	 * management occurs outside of the Autotools Configure Settings page in the
	 * Property menu.
	 *
	 * @param project to synchronize configurations for
	 *
	 */
	public synchronized void syncConfigurations(IProject project) {
		setSyncing(true);
		clearTmpConfigurations(project);
		ICProjectDescription pd = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] cfgs = pd.getConfigurations();
		Map<String, IAConfiguration> newCfgList = new HashMap<>();
		for (int i = 0; i < cfgs.length; ++i) {
			cfgs[i].getConfigurationData();
			IAConfiguration acfg = getTmpConfiguration(project, cfgs[i]);
			newCfgList.put(cfgs[i].getId(), acfg);
		}
		setSyncing(false);
		clearTmpConfigurations(project);
		replaceProjectConfigurations(project, newCfgList);
	}

	public synchronized void replaceProjectConfigurations(IProject project, Map<String, IAConfiguration> cfgs) {
		String projectName = project.getName();
		configs.put(projectName, cfgs);
		saveConfigs(project);
	}

	public synchronized void replaceProjectConfigurations(IProject project, Map<String, IAConfiguration> cfgs,
			ICConfigurationDescription[] cfgds) {
		String projectName = project.getName();
		configs.put(projectName, cfgs);
		saveConfigs(project, cfgds);
	}

	private Map<String, IAConfiguration> getSavedConfigs(IProject project) {
		String projectName = project.getName();
		Map<String, IAConfiguration> list = configs.get(projectName);
		if (list == null) {
			try {
				IPath fileLocation = project.getLocation().append(CFG_FILE_NAME);
				File dirFile = fileLocation.toFile();
				Map<String, IAConfiguration> cfgList = new HashMap<>();
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				if (dirFile.exists()) {
					Document d = db.parse(dirFile);
					Element e = d.getDocumentElement();
					// Get the stored configuration data
					NodeList cfgs = e.getElementsByTagName("configuration"); //$NON-NLS-1$
					for (int x = 0; x < cfgs.getLength(); ++x) {
						Node n = cfgs.item(x);
						NamedNodeMap attrs = n.getAttributes();
						// Originally we used the configuration name, but now we use
						// the ConfigurationDescription id which is unique.  Check for
						// id first, but fall back to name for older .autotools files.
						Node nameNode = attrs.getNamedItem("name"); //$NON-NLS-1$
						Node cfgIdNode = attrs.getNamedItem("id"); //$NON-NLS-1$
						String cfgId = null;
						if (cfgIdNode != null)
							cfgId = cfgIdNode.getNodeValue();
						else if (nameNode != null) {
							String cfgName = nameNode.getNodeValue();
							ICConfigurationDescription cfgd = CoreModel.getDefault().getProjectDescription(project)
									.getConfigurationByName(cfgName);
							if (cfgd != null)
								cfgId = cfgd.getId();
							else
								continue; // have to punt, this doesn't map to real cfg
						}
						IAConfiguration cfg = new AutotoolsConfiguration(project, cfgId);
						NodeList l = n.getChildNodes();
						for (int y = 0; y < l.getLength(); ++y) {
							Node child = l.item(y);
							if (child.getNodeName().equals("option")) { //$NON-NLS-1$
								NamedNodeMap optionAttrs = child.getAttributes();
								Node id = optionAttrs.getNamedItem("id"); //$NON-NLS-1$
								Node value = optionAttrs.getNamedItem("value"); //$NON-NLS-1$
								if (id != null && value != null)
									cfg.setOption(id.getNodeValue(), value.getNodeValue());
							} else if (child.getNodeName().equals("flag")) { //$NON-NLS-1$
								// read in flag values
								NamedNodeMap optionAttrs = child.getAttributes();
								Node id = optionAttrs.getNamedItem("id"); //$NON-NLS-1$
								String idValue = id.getNodeValue();
								IConfigureOption opt = cfg.getOption(idValue);
								if (opt instanceof FlagConfigureOption) {
									NodeList l2 = child.getChildNodes();
									for (int z = 0; z < l2.getLength(); ++z) {
										Node flagChild = l2.item(z);
										if (flagChild.getNodeName().equals("flagvalue")) { //$NON-NLS-1$
											NamedNodeMap optionAttrs2 = flagChild.getAttributes();
											Node id2 = optionAttrs2.getNamedItem("id"); //$NON-NLS-1$
											Node value = optionAttrs2.getNamedItem("value"); //$NON-NLS-1$
											cfg.setOption(id2.getNodeValue(), value.getNodeValue());
										}
									}
								}
							}
						}
						cfg.setDirty(false);
						cfgList.put(cfg.getId(), cfg);
					}
					if (cfgList.size() > 0) {
						configs.put(projectName, cfgList);
						list = cfgList;
					}
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public synchronized IAConfiguration getTmpConfiguration(IProject p, ICConfigurationDescription cfgd) {
		Map<String, IAConfiguration> list = getTmpConfigs(p);
		IAConfiguration acfg = list.get(cfgd.getId());
		if (acfg != null) {
			return acfg;
		}
		IAConfiguration oldCfg = getConfiguration(p, cfgd.getId(), false);
		list.put(cfgd.getId(), oldCfg);
		return oldCfg;
	}

	/**
	 * Clone a configuration and put it on the tmp list if it is not already a saved configuration
	 * and not already on the tmp list.
	 *
	 * @param p project
	 * @param oldId the id of the old configuration to clone
	 * @param cfgd the configuration descriptor for the clone
	 * @return true if the configuration is already saved, false otherwise
	 */
	public synchronized boolean cloneCfg(IProject p, String oldId, ICConfigurationDescription cfgd) {
		if (isConfigurationAlreadySaved(p, cfgd))
			return true;
		Map<String, IAConfiguration> tmpList = getTmpConfigs(p);
		String newId = cfgd.getId();
		// Don't bother if the new configuration is already on the tmp list
		IAConfiguration cfg = tmpList.get(newId);
		if (cfg != null)
			return false;
		// Otherwise, try and find the old id to copy the configuration from
		// or punt if not found
		IAConfiguration oldCfg = null;
		Map<String, IAConfiguration> savedList = getSavedConfigs(p);
		if (savedList != null)
			oldCfg = savedList.get(oldId);
		if (oldCfg != null) {
			IAConfiguration newCfg = oldCfg.copy(newId);
			tmpList.put(cfgd.getId(), newCfg);
			// Check to see if the new configuration is already stored as part of the project description.
			// If yes, it should already be saved.  This can occur if the configuration was added as part of
			// another CDT Property page and the Autotools Property page was never opened.
			if (CoreModel.getDefault().getProjectDescription(p).getConfigurationById(newId) != null) {
				addConfiguration(p, newCfg);
				return true;
			}
		}
		return false;
	}

	private Map<String, IAConfiguration> getTmpConfigs(IProject p) {
		Map<String, IAConfiguration> tmpList = tmpConfigs.get(p.getName());
		if (tmpList == null) {
			tmpList = new HashMap<>();
			tmpConfigs.put(p.getName(), tmpList);
		}
		return tmpList;
	}

	public synchronized void clearTmpConfigurations(IProject p) {
		tmpConfigs.remove(p.getName());
	}

	public synchronized void saveConfigs(IProject project) {
		synchronized (project) {
			ICConfigurationDescription[] cfgds = CoreModel.getDefault().getProjectDescription(project)
					.getConfigurations();
			saveConfigs(project, cfgds);
		}
	}

	private void syncNameField(ICConfigurationDescription cfgd) {
		IConfiguration icfg = ManagedBuildManager.getConfigurationForDescription(cfgd);
		String id = cfgd.getId();
		if (icfg != null) {
			IToolChain toolchain = icfg.getToolChain();
			ITool[] tools = toolchain.getTools();
			for (int j = 0; j < tools.length; ++j) {
				ITool tool = tools[j];
				if (tool.getName().equals("configure")) { //$NON-NLS-1$
					IOption option = tool
							.getOptionBySuperClassId("org.eclipse.linuxtools.cdt.autotools.core.option.configure.name"); //$NON-NLS-1$
					IHoldsOptions h = tool;
					try {
						IOption optionToSet = h.getOptionToSet(option, false);
						optionToSet.setValue(id);
					} catch (BuildException e) {
					}
				}
			}
		}
	}

	private String xmlEscape(String value) {
		value = value.replaceAll("\\&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		value = value.replaceAll("\\\"", "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$
		value = value.replaceAll("\\\'", "&apos;"); //$NON-NLS-1$ //$NON-NLS-2$
		value = value.replaceAll("\\<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		value = value.replaceAll("\\>", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		return value;
	}

	private void saveConfigs(IProject project, ICConfigurationDescription[] cfgds) {
		try {
			String projectName = project.getName();
			IPath output = project.getLocation().append(CFG_FILE_NAME);
			File f = output.toFile();
			if (!f.exists())
				f.createNewFile();
			if (f.exists()) {
				try (PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(f)))) {
					Map<String, IAConfiguration> cfgs = configs.get(projectName);
					p.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
					p.println("<configurations>"); //$NON-NLS-1$
					Option[] optionList = AutotoolsConfiguration.getOptionList();
					// Before saving, force any cloning to occur via the option
					// value handler.
					setSyncing(true);
					for (int i = 0; i < cfgds.length; ++i) {
						@SuppressWarnings("unused")
						CConfigurationData data = cfgds[i].getConfigurationData();
					}
					setSyncing(false);
					for (int i = 0; i < cfgds.length; ++i) {
						ICConfigurationDescription cfgd = cfgds[i];
						String id = cfgd.getId();
						IAConfiguration cfg = cfgs.get(id);
						if (cfg == null) {
							cfg = createDefaultConfiguration(project, id);
						}
						p.println("<configuration id=\"" + cfg.getId() + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
						for (int j = 0; j < optionList.length; ++j) {
							Option option = optionList[j];
							IConfigureOption opt = cfg.getOption(option.getName());
							if (opt.isFlag()) {
								p.println("<flag id=\"" + option.getName() + "\" value=\"" //$NON-NLS-1$ //$NON-NLS-2$
										+ xmlEscape(option.getDefaultValue()) + "\">"); //$NON-NLS-1$
								FlagConfigureOption fco = (FlagConfigureOption) opt;
								List<String> children = fco.getChildren();
								for (int k = 0; k < children.size(); ++k) {
									String childName = children.get(k);
									IConfigureOption childopt = cfg.getOption(childName);
									p.println("<flagvalue id=\"" + childopt.getName() + "\" value=\"" //$NON-NLS-1$ //$NON-NLS-2$
											+ xmlEscape(childopt.getValue()) + "\"/>"); //$NON-NLS-3$
								}
								p.println("</flag>"); //$NON-NLS-1$
							} else if (!opt.isCategory() && !opt.isFlagValue())
								p.println("<option id=\"" + option.getName() + "\" value=\"" + xmlEscape(opt.getValue()) //$NON-NLS-1$ //$NON-NLS-2$
										+ "\"/>"); //$NON-NLS-3$
						}
						p.println("</configuration>"); //$NON-NLS-1$
						// Sync name field as this configuration is now
						// officially saved
						syncNameField(cfgd);
					}
					p.println("</configurations>");
				}
			}
		} catch (IOException e) {
			AutotoolsPlugin.log(e);
		}
	}

	// Perform apply of configuration changes.  This rewrites out the current known list of configurations
	// with any changes currently that have been made to them.  If a configuration has been renamed, but this
	// has not yet been confirmed by the end-user, then only the changes to the configuration are made.  The
	// name currently remains the same in the output file.
	public synchronized void applyConfigs(String projectName, ICConfigurationDescription[] cfgds) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource res = root.findMember(projectName, false);
			if (res == null || res.getType() != IResource.PROJECT) {
				AutotoolsPlugin.logErrorMessage(
						ConfigureMessages.getFormattedString(CFG_CANT_SAVE, new String[] { projectName }));
				return;
			}
			IProject project = (IProject) res;
			IPath output = project.getLocation().append(CFG_FILE_NAME);
			File f = output.toFile();
			if (!f.exists())
				f.createNewFile();
			if (f.exists()) {
				try (PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(f)))) {
					Map<String, IAConfiguration> cfgs = getSavedConfigs(project);
					if (cfgs == null)
						return;
					p.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
					p.println("<configurations>"); //$NON-NLS-1$
					Option[] optionList = AutotoolsConfiguration.getOptionList();
					HashSet<String> savedIds = new HashSet<>();
					setSyncing(true);
					for (int x = 0; x < cfgds.length; ++x) {
						ICConfigurationDescription cfgd = cfgds[x];
						@SuppressWarnings("unused")
						CConfigurationData data = cfgd.getConfigurationData();
						String id = cfgd.getId();
						savedIds.add(id);
						IAConfiguration cfg = getTmpConfiguration(project, cfgd);
						cfgs.put(id, cfg); // add to list in case we have a new configuration not yet added to Project Description
						p.println("<configuration id=\"" + id + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
						for (int j = 0; j < optionList.length; ++j) {
							Option option = optionList[j];
							IConfigureOption opt = cfg.getOption(option.getName());
							if (!opt.isCategory())
								p.println("<option id=\"" + option.getName() + "\" value=\"" + opt.getValue() + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
						p.println("</configuration>"); //$NON-NLS-1$
						syncNameField(cfgd);
					}
					setSyncing(false);

					// Put all the remaining configurations already saved back into the file.
					// These represent deleted configurations, but confirmation has not occurred.
					for (Entry<String, IAConfiguration> i : cfgs.entrySet()) {
						String id = i.getKey();
						// A remaining id won't appear in our savedIds list.
						if (!savedIds.contains(id)) {
							IAConfiguration cfg = i.getValue();
							p.println("<configuration id=\"" + id + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
							for (int j = 0; j < optionList.length; ++j) {
								Option option = optionList[j];
								IConfigureOption opt = cfg.getOption(option.getName());
								if (!opt.isCategory())
									p.println("<option id=\"" + option.getName() + "\" value=\"" + opt.getValue() //$NON-NLS-1$//$NON-NLS-2$
											+ "\"/>"); //$NON-NLS-1$
							}
							p.println("</configuration>"); //$NON-NLS-1$
						}
					}
					p.println("</configurations>");
				}
			}
		} catch (IOException e) {
			AutotoolsPlugin.log(e);
		}
	}

	public synchronized Map<String, IAConfiguration> getConfigurations(IProject project) {
		Map<String, IAConfiguration> list = getSavedConfigs(project);
		if (list == null) {
			list = new HashMap<>();
			configs.put(project.getName(), list);
		}
		return list;
	}

	@Override
	public synchronized void resourceChanged(IResourceChangeEvent event) {
		IResource res = event.getResource();
		if (!(res instanceof IProject))
			return;
		String name = res.getName();
		IResourceDelta delta = event.getDelta();
		if (delta == null)
			return;
		int kind = delta.getKind();
		if (configs.containsKey(name)) {
			if (kind == IResourceDelta.REMOVED) {
				configs.remove(name);
				tmpConfigs.remove(name);
			} else if (kind == IResourceDelta.CHANGED) {
				int flags = delta.getFlags();
				if ((flags & IResourceDelta.MOVED_TO) != 0) {
					IPath path = delta.getMovedToPath();
					Map<String, IAConfiguration> cfgs = configs.get(name);
					String newName = path.lastSegment();
					configs.remove(name);
					configs.put(newName, cfgs);
					Map<String, IAConfiguration> tmpcfgs = tmpConfigs.get(name);
					tmpConfigs.remove(name);
					tmpConfigs.put(newName, tmpcfgs);
				}
			}
		}
	}

	private static class AutotoolsOption implements IAutotoolsOption {

		private IConfigureOption option;
		private final static String UNMODIFIABLE_CONFIG_OPTION = "CfgOptions.Unmodifiable.Option"; //$NON-NLS-1$

		public AutotoolsOption(IConfigureOption option) {
			this.option = option;
		}

		@Override
		public int getType() {
			return option.getType();
		}

		@Override
		public boolean canUpdate() {
			int type = getType();
			switch (type) {
			case STRING:
			case BIN:
			case TOOL:
			case FLAGVALUE:
			case MULTIARG:
			case INTERNAL:
			case ENVVAR:
				return true;
			}
			return false;
		}

		@Override
		public void setValue(String value) throws CoreException {
			if (!canUpdate()) {
				throw new CoreException(new Status(IStatus.ERROR, AutotoolsPlugin.PLUGIN_ID,
						ConfigureMessages.getString(UNMODIFIABLE_CONFIG_OPTION)));
			}
			synchronized (option) {
				option.setValue(value);
			}
		}

		@Override
		public String getValue() {
			synchronized (option) {
				return option.getValue();
			}
		}

	}

	private String createDummyId() {
		for (;;) {
			String id = "TEMP_" + rand.nextInt();
			if (tmpConfigs.get(id) == null)
				return id;
		}
	}

	/**
	 * @since 1.2
	 */
	public synchronized Map<String, IAutotoolsOption> getAutotoolsCfgOptions(IProject project, String cfgId)
			throws CoreException {

		// Verify project is valid Autotools project
		if (project == null || !project.hasNature(AutotoolsNewProjectNature.AUTOTOOLS_NATURE_ID)) {
			throw new CoreException(new Status(IStatus.ERROR, AutotoolsPlugin.PLUGIN_ID,
					ConfigureMessages.getString(INVALID_AUTOTOOLS_PROJECT)));
		}

		// Verify configuration id is valid
		ICConfigurationDescription cfgd = CoreModel.getDefault().getProjectDescription(project)
				.getConfigurationById(cfgId);
		IConfiguration icfg = ManagedBuildManager.getConfigurationForDescription(cfgd);
		if (icfg == null) {
			throw new CoreException(new Status(IStatus.ERROR, AutotoolsPlugin.PLUGIN_ID,
					ConfigureMessages.getString(INVALID_AUTOTOOLS_CONFIG_ID)));
		}

		IAConfiguration cfg = getConfiguration(project, cfgId);
		HashMap<String, IAutotoolsOption> options = new HashMap<>();

		// Get set of configuration options and convert to set of IAutotoolOptions
		Map<String, IConfigureOption> cfgOptions = cfg.getOptions();
		IAConfiguration dummyCfg = createDefaultConfiguration(project, createDummyId());
		for (Iterator<Entry<String, IConfigureOption>> i = cfgOptions.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, IConfigureOption> entry = i.next();
			String name = entry.getKey();
			IAutotoolsOption configOption = new AutotoolsOption(
					entry.getValue().copy((AutotoolsConfiguration) dummyCfg));
			options.put(name, configOption);
		}

		return options;
	}

	/**
	 * @since 1.2
	 */
	public synchronized void updateAutotoolCfgOptions(IProject project, String cfgId,
			Map<String, IAutotoolsOption> options) throws CoreException {

		// Verify project is valid Autotools project
		if (project == null || !project.hasNature(AutotoolsNewProjectNature.AUTOTOOLS_NATURE_ID)) {
			throw new CoreException(new Status(IStatus.ERROR, AutotoolsPlugin.PLUGIN_ID,
					ConfigureMessages.getString(INVALID_AUTOTOOLS_PROJECT)));
		}

		// Verify configuration id is valid
		IAConfiguration cfg = findCfg(project, cfgId);
		if (cfg == null) {
			throw new CoreException(new Status(IStatus.ERROR, AutotoolsPlugin.PLUGIN_ID,
					ConfigureMessages.getString(INVALID_AUTOTOOLS_CONFIG_ID)));
		}

		// Get set of configuration options and convert to set of IAutotoolOptions
		for (Iterator<Entry<String, IAutotoolsOption>> i = options.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, IAutotoolsOption> entry = i.next();
			String name = entry.getKey();
			IAutotoolsOption option = entry.getValue();
			IConfigureOption cfgOption = cfg.getOption(name);
			if (cfgOption != null) {
				cfgOption.setValue(option.getValue());
			}
		}

		// Save changes
		saveConfigs(project);
	}

}
