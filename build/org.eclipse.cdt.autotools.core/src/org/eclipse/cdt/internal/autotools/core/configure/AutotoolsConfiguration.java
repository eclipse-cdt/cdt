/*******************************************************************************
 * Copyright (c) 2009, 2011 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.autotools.core.AutotoolsOptionConstants;


public class AutotoolsConfiguration implements IAConfiguration {

	public static class Option {
		private String name;
		private String transformedName;
		private int type;
		private String defaultValue;
		
		public Option(String name, int type) {
			this(name, name, type);
		}
		
		public Option(String name, String transformedName, int type) {
			this.name = name;
			this.transformedName = transformedName;
			this.type = type;
		}
		
		public Option(String name, String transformedName, String defaultValue, int type) {
			this.name = name;
			this.transformedName = transformedName;
			this.type = type;
			this.defaultValue = defaultValue;
		}
		
		public String getName() {
			return name;
		}
		
		public int getType() {
			return type;
		}
		
		public String getDefaultValue() {
			return defaultValue;
		}
		
		public String getDescription() {
			return ConfigureMessages.getConfigureDescription(transformedName);
		}
		
		public String getToolTip() {
			return ConfigureMessages.getConfigureTip(transformedName);
		}
	}
	
	// Configure options and categories.  List below is ordered.
	// All options following a category are children of that category
	// in a tree view, up to the next category.
	private static Option[] configOpts = new Option[] {
		new Option(AutotoolsOptionConstants.TOOL_CONFIGURE, IConfigureOption.TOOL),
		new Option(AutotoolsOptionConstants.CATEGORY_GENERAL, IConfigureOption.CATEGORY),
		new Option(AutotoolsOptionConstants.OPT_CONFIGDIR, IConfigureOption.INTERNAL),
		new Option(AutotoolsOptionConstants.OPT_CACHE_FILE, "cache_file", IConfigureOption.STRING), // $NON-NLS-1$
		new Option(AutotoolsOptionConstants.OPT_HELP, IConfigureOption.BIN),
		new Option(AutotoolsOptionConstants.OPT_NO_CREATE, "no_create", IConfigureOption.BIN), // $NON-NLS-1$
		new Option(AutotoolsOptionConstants.OPT_QUIET, IConfigureOption.BIN),
		new Option(AutotoolsOptionConstants.OPT_VERSION, IConfigureOption.BIN),
		new Option(AutotoolsOptionConstants.CATEGORY_PLATFORM, IConfigureOption.CATEGORY),
		new Option(AutotoolsOptionConstants.OPT_HOST, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_BUILD, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_TARGET, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.CATEGORY_DIRECTORIES, IConfigureOption.CATEGORY),
		new Option(AutotoolsOptionConstants.OPT_PREFIX, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_EXEC_PREFIX, "exec_prefix", IConfigureOption.STRING), // $NON-NLS-1$
		new Option(AutotoolsOptionConstants.OPT_LIBDIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_BINDIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_SBINDIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_INCLUDEDIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_DATADIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_SYSCONFDIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_INFODIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_MANDIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_SRCDIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_LOCALSTATEDIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_SHAREDSTATEDIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_LIBEXECDIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.OPT_OLDINCLUDEDIR, IConfigureOption.STRING),
		new Option(AutotoolsOptionConstants.CATEGORY_FILENAMES, IConfigureOption.CATEGORY),
		new Option(AutotoolsOptionConstants.OPT_PROGRAM_PREFIX, "program_prefix", IConfigureOption.STRING), // $NON-NLS-1$
		new Option(AutotoolsOptionConstants.OPT_PROGRAM_SUFFIX, "program_suffix", IConfigureOption.STRING), // $NON-NLS-1$
		new Option(AutotoolsOptionConstants.OPT_PROGRAM_TRANSFORM_NAME, "program_transform_name", IConfigureOption.STRING), // $NON-NLS-1$
		new Option(AutotoolsOptionConstants.CATEGORY_FEATURES, IConfigureOption.CATEGORY),
		new Option(AutotoolsOptionConstants.OPT_ENABLE_MAINTAINER_MODE, "enable_maintainer_mode", IConfigureOption.BIN), // $NON-NLS-1$
		new Option(AutotoolsOptionConstants.FLAG_CFLAGS, "cflags", AutotoolsOptionConstants.FLAG_CFLAGS_FLAGS, IConfigureOption.FLAG), // $NON-NLS-1$
		new Option(AutotoolsOptionConstants.OPT_CFLAGS_DEBUG, "cflags_debug", IConfigureOption.FLAGVALUE), // $NON-NLS-1$ // $NON-NLS-2$
		new Option(AutotoolsOptionConstants.OPT_CFLAGS_GPROF, "cflags_gprof", IConfigureOption.FLAGVALUE), // $NON-NLS-1$ // $NON-NLS-2$ 
		new Option(AutotoolsOptionConstants.OPT_CFLAGS_GCOV, "cflags_gcov", IConfigureOption.FLAGVALUE), // $NON-NLS-1$ // $NON-NLS-2$ 
		new Option(AutotoolsOptionConstants.VAR_CC, IConfigureOption.VARIABLE), // $NON-NLS-1$
		new Option(AutotoolsOptionConstants.OPT_USER, IConfigureOption.MULTIARG),
		new Option(AutotoolsOptionConstants.TOOL_AUTOGEN, "autogen", "autogen.sh", IConfigureOption.TOOL), // $NON-NLS-1$
		new Option(AutotoolsOptionConstants.CATEGORY_OPTIONS, IConfigureOption.CATEGORY),
		new Option(AutotoolsOptionConstants.OPT_AUTOGENOPTS, IConfigureOption.MULTIARG),
	};
	
	private static Option[] toolList;
	
	private String id;
	private boolean isDirty;
	private boolean isParmsDirty;
	private Map<String, IConfigureOption> configOptions;
	private ArrayList<String> configParms = new ArrayList<String>();

	public AutotoolsConfiguration(String name) {
		this(name, true);
	}
		
	private AutotoolsConfiguration(String name, boolean initialize) {
		this.id = name;
		configOptions = new HashMap<String, IConfigureOption>();
		if (initialize)
			initConfigOptions();
		isParmsDirty = true;
	}
	
	private void initConfigOptions() {
		// Put configure options in hash map.  Ignore categories.
		ArrayList<Option> tools = new ArrayList<Option>();
		FlagConfigureOption lastFlag = null;
		for (int i = 0; i < configOpts.length; ++i) {
			Option opt = configOpts[i];
			String defaultValue = opt.getDefaultValue();
			int type = opt.type;
			switch (type) {
			case IConfigureOption.BIN:
				BinConfigureOption b = new BinConfigureOption(opt.name, opt.transformedName, this);
				if (defaultValue != null)
					b.setValue(defaultValue);
				configOptions.put(opt.name, b);
				break;
			case IConfigureOption.STRING:
				StringConfigureOption s = new StringConfigureOption(opt.name, opt.transformedName, this);
				if (defaultValue != null)
					s.setValue(defaultValue);
				configOptions.put(opt.name, s);
				break;
			case IConfigureOption.INTERNAL:
				InternalConfigureOption io = new InternalConfigureOption(opt.name, opt.transformedName, this);
				if (defaultValue != null)
					io.setValue(defaultValue);
				configOptions.put(opt.name, io);
				break;
			case IConfigureOption.MULTIARG:
				MultiArgConfigureOption m = new MultiArgConfigureOption(opt.name, opt.transformedName, this);
				if (defaultValue != null)
					m.setValue(defaultValue);
				configOptions.put(opt.name, m);
				break;
			case IConfigureOption.TOOL:
				tools.add(opt);
				ConfigureTool t = new ConfigureTool(opt.name, opt.transformedName, this);
				if (defaultValue != null)
					t.setValue(defaultValue);
				configOptions.put(opt.name, t);
				break;
			case IConfigureOption.CATEGORY:
				configOptions.put(opt.name, new ConfigureOptionCategory(opt.name));
				break;
			case IConfigureOption.FLAG:
				FlagConfigureOption f = new FlagConfigureOption(opt.name, opt.transformedName, this);
				if (defaultValue != null)
					f.setValue(defaultValue);
				lastFlag = f;
				configOptions.put(opt.name, f);
				break;
			case IConfigureOption.FLAGVALUE:
				FlagValueConfigureOption fv 
					= new FlagValueConfigureOption(opt.name, opt.transformedName, 
							this, ConfigureMessages.getParameter(opt.transformedName));
				if (defaultValue != null)
					fv.setValue(defaultValue);
				lastFlag.addChild(opt.name);
				configOptions.put(opt.name, fv);
				break;
			case IConfigureOption.VARIABLE:
				VariableConfigureOption v = new VariableConfigureOption(opt.name, opt.transformedName, this);
				if (defaultValue != null)
					v.setValue(defaultValue);
				configOptions.put(opt.name, v);
				break;
			}
		}
		toolList = tools.toArray(new Option[tools.size()]);
	}
	
	public static Option[] getOptionList() {
		return configOpts.clone();
	}

	public static Option[] getChildOptions(String name) {
		ArrayList<Option> options = new ArrayList<Option>();
		for (int i = 0; i < configOpts.length; ++i) {
			Option opt = configOpts[i];
			if (opt.getName().equals(name)) {
				if (opt.getType() == IConfigureOption.CATEGORY) {
					for (int j = i + 1; j < configOpts.length; ++j) {
						Option o = configOpts[j];
						int type = o.getType();
						if (type != IConfigureOption.CATEGORY &&
								type != IConfigureOption.TOOL)
							options.add(o);
						else
							return options.toArray(new Option[options.size()]);
					}
				} else if (opt.getType() == IConfigureOption.TOOL) {
					for (int j = i + 1; j < configOpts.length; ++j) {
						Option o = configOpts[j];
						int type = o.getType();
						if (type == IConfigureOption.CATEGORY)
							options.add(o);
						else if (type == IConfigureOption.TOOL)
							return options.toArray(new Option[options.size()]);
					}	
				}
			}
		}
		return options.toArray(new Option[options.size()]);
	}
	
	public static Option[] getTools() {
		return toolList.clone();
	}
	
	public IConfigureOption getOption(String name) {
		return configOptions.get(name);
	}

	public IAConfiguration copy() {
		return copy(id);
	}
	
	public IAConfiguration copy(String newId) {
		AutotoolsConfiguration cfg = new AutotoolsConfiguration(newId, false);
		Collection<IConfigureOption> oldValues = configOptions.values();
		for (Iterator<IConfigureOption> i = oldValues.iterator(); i.hasNext();) {
			IConfigureOption opt = i.next();
			cfg.configOptions.put(opt.getName(), opt.copy(cfg));
		}
		if (getId().equals(newId))
			cfg.setDirty(isDirty()); // copying with same id, do not change dirty attribute
		else
			cfg.setDirty(true); // we are cloning with a new id, treat it as never built/dirty
		return cfg;
	}
	
	public String getId() {
		return id;
	}
	
	public boolean isDirty() {
		return isDirty;
	}
	
	public void setDirty(boolean value) {
		isDirty = value;
		if (isDirty)
			isParmsDirty = true;
	}
	
	public Map<String, IConfigureOption> getOptions() {
		return configOptions;
	}
	
	public String getToolParameters(String name) {
		StringBuffer buf = new StringBuffer();
		Option[] options = getChildOptions(name);
		for (int i = 0; i < options.length; ++i) {
			IConfigureOption option = getOption(options[i].getName());
			if (option.getType() == IConfigureOption.CATEGORY) {
				Option[] childOptions = getChildOptions(option.getName());
				for (int j = 0; j < childOptions.length; ++j) {
					IConfigureOption childOption = getOption(childOptions[j].getName());
					String parameter = childOption.getParameter();
					if (!parameter.equals(""))
						buf.append(" " + parameter);
				}
			} else {
				String parameter = option.getParameter();
				if (!parameter.equals(""))
					buf.append(" " + parameter);
			}
		}
		return buf.toString();
	}

	public ArrayList<String> getToolArgs(String name) {
		if (isParmsDirty) {
			configParms = new ArrayList<String>();
			Option[] options = getChildOptions(name);
			for (int i = 0; i < options.length; ++i) {
				IConfigureOption option = getOption(options[i].getName());
				if (option.getType() == IConfigureOption.CATEGORY) {
					Option[] childOptions = getChildOptions(option.getName());
					for (int j = 0; j < childOptions.length; ++j) {
						IConfigureOption childOption = getOption(childOptions[j].getName());
						ArrayList<String> parameters = childOption.getParameters();
						configParms.addAll(parameters);
					}
				} else {
					ArrayList<String> parameters = option.getParameters();
					configParms.addAll(parameters);
				}
			}
		}
		return configParms;
	}

	public void setOption(String name, String value) {
		IConfigureOption option = configOptions.get(name);
		if (option != null) {
			if (!option.getValue().equals(value)) {
				option.setValue(value);
				setDirty(true);
			}
		}
	}
	
	public void setConfigToolDirectory(String configToolDirectory) {
		setOption("configdir", configToolDirectory);
	}

	public String getConfigToolDirectory() {
		IConfigureOption option = configOptions.get("configdir");
		return option.getValue();
	}

	public void setDefaultOptions() {
		initConfigOptions();
	}
}
