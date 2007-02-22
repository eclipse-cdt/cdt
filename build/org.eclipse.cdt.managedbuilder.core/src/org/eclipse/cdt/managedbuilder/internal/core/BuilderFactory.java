/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.newmake.internal.core.MakeMessages;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class BuilderFactory {

	private static final String PREFIX = "org.eclipse.cdt.make.core"; //$NON-NLS-1$
	private static final String PREFIX_WITH_DOT = PREFIX + '.'; //$NON-NLS-1$

	static final String BUILD_COMMAND = PREFIX + ".buildCommand"; //$NON-NLS-1$
	static final String BUILD_LOCATION = PREFIX + ".buildLocation"; //$NON-NLS-1$
	static final String STOP_ON_ERROR = PREFIX + ".stopOnError"; //$NON-NLS-1$
	static final String USE_DEFAULT_BUILD_CMD = PREFIX + ".useDefaultBuildCmd"; //$NON-NLS-1$
	static final String BUILD_TARGET_AUTO = PREFIX + ".autoBuildTarget"; //$NON-NLS-1$
	static final String BUILD_TARGET_INCREMENTAL = PREFIX + ".incrementalBuildTarget"; //$NON-NLS-1$
	static final String BUILD_TARGET_FULL = PREFIX + ".fullBuildTarget"; //$NON-NLS-1$
	static final String BUILD_TARGET_CLEAN = PREFIX + ".cleanBuildTarget"; //$NON-NLS-1$
	static final String BUILD_FULL_ENABLED = PREFIX + ".enableFullBuild"; //$NON-NLS-1$
	static final String BUILD_CLEAN_ENABLED = PREFIX + ".enableCleanBuild"; //$NON-NLS-1$
	static final String BUILD_INCREMENTAL_ENABLED = PREFIX + ".enabledIncrementalBuild"; //$NON-NLS-1$
	static final String BUILD_AUTO_ENABLED = PREFIX + ".enableAutoBuild"; //$NON-NLS-1$
	static final String BUILD_ARGUMENTS = PREFIX + ".buildArguments"; //$NON-NLS-1$
	static final String ENVIRONMENT = PREFIX + ".environment"; //$NON-NLS-1$
	static final String BUILD_APPEND_ENVIRONMENT = PREFIX + ".append_environment"; //$NON-NLS-1$ 

	static final String CONTENTS = PREFIX + ".contents"; //$NON-NLS-1$ 
	static final String CONTENTS_BUILDER = PREFIX + ".builder"; //$NON-NLS-1$ 
//	static final String CONTENTS_BUILDER_CUSTOMIZATION = PREFIX + ".builderCustomization"; //$NON-NLS-1$ 
	static final String CONTENTS_CONFIGURATION_IDS = PREFIX + ".configurationIds"; //$NON-NLS-1$ 

//	static final String IDS = PREFIX + ".ids"; //$NON-NLS-1$ 
	static final String CONFIGURATION_IDS = PREFIX + ".configurationIds"; //$NON-NLS-1$
	
	static final IBuilder[] EMPTY_BUILDERS_ARRAY = new IBuilder[0];
	static final String[] EMPTY_STRING_ARRAY = new String[0];
	static final IConfiguration[] EMPTY_CFG_ARAY = new IConfiguration[0];

	private static class BuildArgsStorageElement extends MapStorageElement{

		public BuildArgsStorageElement(Map map, MapStorageElement parent) {
			super(map, parent);
		}

		public BuildArgsStorageElement(String name, MapStorageElement parent) {
			super(name, parent);
		}

		public String getAttribute(String name) {
			String value = super.getAttribute(name);
			if(value == null){
				String keys[] = Builder.toBuildAttributes(name);
				for(int i = 0; i < keys.length; i++){
					value = super.getAttribute(keys[i]);
					if(value != null)
						break;
				}
			}
			return value;
		}

//		protected String getMapKey(String name) {
//			if(name.indexOf('.') == -1)
//				return PREFIX_WITH_DOT + name;
//			return super.getMapKey(name);
//		}

		protected MapStorageElement createChildElement(Map childMap) {
			return new BuildArgsStorageElement(childMap, this);
		}

		protected MapStorageElement createChildElement(String name) {
			return new BuildArgsStorageElement(name, this);
		}
	}

/*	public static IMakeBuilderInfo create(Preferences prefs, String builderID, boolean useDefaults) {
		return new BuildInfoPreference(prefs, builderID, useDefaults);
	}

	public static IMakeBuilderInfo create(IProject project, String builderID) throws CoreException {
		return new BuildInfoProject(project, builderID);
	}

	public static IMakeBuilderInfo create(Map args, String builderID) {
		return new BuildInfoMap(args, builderID);
	}
	*/
/*	
	private static IBuilder customizeBuilder(IBuilder builder, Map args){
		if(args.get(IBuilder.ID) == null){
			args = new HashMap(args);
			String id = builder.getSuperClass().getId();
			id = ManagedBuildManager.calculateChildId(id, null);
			args.put(IBuilder.ID, id);
		}
		MapStorageElement el = new MapStorageElement(args, null);
		
		Builder builder = new Builder(builder.getParent(), )
		
		
	}
*/
	
	public static Map createBuildArgs(IConfiguration cfgs[], IBuilder builder){
		Map map = builderToMap(builder);
		cfgsToMap(cfgs, map);
		map.put(CONTENTS, /*CONTENTS_BUILDER_CUSTOMIZATION*/CONTENTS_BUILDER);
		return map;
	}

	public static Map createBuildArgs(IConfiguration cfgs[]){
		Map map = new HashMap();
		cfgsToMap(cfgs, map);
		map.put(CONTENTS, CONTENTS_CONFIGURATION_IDS);
		return map;
	}
	
	private static Map cfgIdsToMap(String ids[], Map map){
		map.put(CONFIGURATION_IDS, MapStorageElement.encodeList(Arrays.asList(ids)));
		return map;
	}
	
	private static String[] cfgIdsFromMap(Map map){
		String idsString = (String)map.get(CONFIGURATION_IDS);
		if(idsString != null){
			List list = MapStorageElement.decodeList(idsString);
			return (String[])list.toArray(new String[list.size()]);
		}
		return EMPTY_STRING_ARRAY;
	}
	
	private static IConfiguration[] configsFromMap(Map map, IManagedBuildInfo info){
		String ids[] = cfgIdsFromMap(map);
		if(ids.length == 0){
			IConfiguration cfg = info.getDefaultConfiguration();
			if(cfg != null)
				return new IConfiguration[]{cfg};
			return EMPTY_CFG_ARAY;
		}
		IManagedProject mProj = info.getManagedProject();
		if(mProj != null)
			return idsToConfigurations(ids, mProj.getConfigurations());
		return EMPTY_CFG_ARAY;
	}
	
	private static IConfiguration[] idsToConfigurations(String ids[], IConfiguration allCfgs[]){
		List list = new ArrayList(ids.length);
		for(int i = 0; i < ids.length; i++){
			String id = ids[i];
			for(int j = 0; j < allCfgs.length; j++){
				if(allCfgs[j].getId().equals(id)){
					list.add(allCfgs[j]);
					break;
				}
			}
		}
		return (IConfiguration[])list.toArray(new IConfiguration[list.size()]);
	}

	private static Map cfgsToMap(IConfiguration cfgs[], Map map){
		String ids[] = getCfgIds(cfgs);
		return cfgIdsToMap(ids, map);
	}

	private static String[] getCfgIds(IConfiguration cfgs[]){
		String ids[] = new String[cfgs.length];
		for(int i = 0; i < cfgs.length; i++){
			ids[i] = cfgs[i].getId();
		}
		return ids;
	}
	
	private static Map builderToMap(IBuilder builder){
		MapStorageElement el = new MapStorageElement("", null);
		((Builder)builder).serialize(el, false);
		
		return el.toStringMap();
	}

	public static IBuilder createCustomBuilder(IConfiguration cfg, String builderId) throws CoreException{
		IBuilder builder = cfg.getBuilder();
		if(!builderId.equals(builder.getId())){
			builder = ManagedBuildManager.getExtensionBuilder(builderId);
		}
		
		if(builder != null)
			return createCustomBuilder(cfg, builder);
		throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
				"can not find builder with the specified id", null));
	}

	public static IBuilder createCustomBuilder(IConfiguration cfg, IBuilder base){
		String subId;
		String subName = base.getName();
		if (base.getSuperClass() != null) {
			subId =  ManagedBuildManager.calculateChildId(
						base.getSuperClass().getId(),
						null);
		} else {
			subId = ManagedBuildManager.calculateChildId(
					base.getId(),
					null);
		}

		return new Builder(cfg.getToolChain(), subId, subName, (Builder)base);
	}
	
	public static IBuilder createBuilderFromCommand(IConfiguration cfg, ICommand command){
		Map args = command.getArguments();
		if(!args.containsKey(IBuilder.ID)){
			args.put(IBuilder.ID, ManagedBuildManager.calculateChildId(command.getBuilderName(), null));
		}
		
		return createBuilder(cfg, args);
	}
	
	public static IBuilder createBuilderForEclipseBuilder(IConfiguration cfg, String eclipseBuilderID) throws CoreException {
		IProject project = cfg.getOwner().getProject();
		ICommand command = getBuildSpec(project.getDescription(), eclipseBuilderID);
		if (command == null) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					MakeMessages.getString("BuildInfoFactory.Missing_Builder") + eclipseBuilderID, null)); //$NON-NLS-1$
		}
		
		return createBuilderFromCommand(cfg, command);
//		Map args = command.getArguments();
//		if(!args.containsKey(IBuilder.ID)){
//			args.put(IBuilder.ID, ManagedBuildManager.calculateChildId(eclipseBuilderID, null));
//		}
//		
//		return createBuilder(cfg, args);
	}
	
	public static ICommand getBuildSpec(IProjectDescription description, String builderID) {
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				return commands[i];
			}
		}
		return null;
	}



	private static IBuilder createBuilder(IConfiguration cfg, Map args){
		IToolChain tCh = cfg.getToolChain();
		if(args.get(IBuilder.ID) == null)
			args.put(IBuilder.ID, ManagedBuildManager.calculateChildId(cfg.getId(), null));
		MapStorageElement el = new BuildArgsStorageElement(args, null);
		Builder builder = new Builder(tCh, el, ManagedBuildManager.getVersion().toString());
		return builder;
	}

	public static IBuilder[] createBuilders(IProject project, Map args){
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IBuilder[] builders = null;
		if(info != null){
			if(args == null){
				IConfiguration cfg = info.getDefaultConfiguration();
				IBuilder builder = cfg.getEditableBuilder();
				builders = new IBuilder[]{builder};
			} else {
				String type = (String)args.get(CONTENTS);
				if(type == null){
					IConfiguration cfg = info.getDefaultConfiguration();
					IBuilder builder = cfg.getEditableBuilder();
					builders = new IBuilder[]{builder};
					//TODO:
				} else if (CONTENTS_BUILDER.equals(type)){
					IConfiguration cfgs[] = configsFromMap(args, info);
					if(cfgs.length != 0){
						List list = new ArrayList(cfgs.length);
						for(int i = 0; i < cfgs.length; i++){
							IBuilder builder = createBuilder(cfgs[i], args);
							if(builder != null)
								list.add(builder);
						}
						builders = (IBuilder[])list.toArray(new IBuilder[list.size()]);
					}
				} else if (CONTENTS_CONFIGURATION_IDS.equals(type)){
					IConfiguration cfgs[] = configsFromMap(args, info);
					if(cfgs.length != 0){
						List list = new ArrayList(cfgs.length);
						for(int i = 0; i < cfgs.length; i++){
							list.add(cfgs[i].getEditableBuilder());
						}
						builders = (IBuilder[])list.toArray(new IBuilder[list.size()]);
					}
				} /*else if (CONTENTS_BUILDER_CUSTOMIZATION.equals(type)){
					String idsString = (String)args.get(CONFIGURATION_IDS);
					if(idsString != null){
						String[] ids = CDataUtil.stringToArray(idsString, SEPARATOR);
						if(ids.length != 0){
							IManagedProject mProj = info.getManagedProject();
							List list = new ArrayList(ids.length);
							for(int i = 0; i < ids.length; i++){
								IConfiguration cfg = mProj.getConfiguration(ids[i]);
								if(cfg != null){
									IBuilder builder = customizeBuilder(cfg.getEditableBuilder(), args);
									if(builder != null)
										list.add(builder);
								}
							}
							builders = (IBuilder[])list.toArray(new IBuilder[list.size()]);
						}
					}
				}*/
			}
		}
		
		if(builders != null)
			return builders;
		return EMPTY_BUILDERS_ARRAY;
	}
}
