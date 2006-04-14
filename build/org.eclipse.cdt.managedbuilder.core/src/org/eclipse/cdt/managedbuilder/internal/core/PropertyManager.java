/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * This class allows specifying BuildObject-specific persisted properties
 * The properties are stored as project preferences for now
 *
 */
public class PropertyManager {
	private static final String PROPS_PROPERTY = "properties";	//$NON-NLS-1$
	private static final QualifiedName propsSessionProperty = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), PROPS_PROPERTY);

	private static final String NODE_NAME = "properties";	//$NON-NLS-1$
	
	private static PropertyManager fInstance;

	private PropertyManager(){
	}
	
	public static PropertyManager getInstance(){
		if(fInstance == null)
			fInstance = new PropertyManager();
		return fInstance;
	}

	protected void setProperty(IConfiguration cfg, IBuildObject bo, String prop, String value){
		Properties props = getProperties(cfg, bo);
		if(props != null)
			props.setProperty(prop, value);
	}

	protected String getProperty(IConfiguration cfg, IBuildObject bo, String prop){
		Properties props = getProperties(cfg, bo);
		if(props != null)
			return props.getProperty(prop);
		return null;
	}
	
	protected Properties getProperties(IConfiguration cfg, IBuildObject bo){
		return loadProperties(cfg, bo);
	}

	protected Map getLoaddedData(IConfiguration cfg){
		Map map = null;
		try {
			IProject proj = cfg.getOwner().getProject();
			map = (Map)proj.getSessionProperty(propsSessionProperty);
			if(map == null){
				map = new HashMap();
				proj.setSessionProperty(propsSessionProperty, map);
			}
			map = (Map)map.get(cfg.getId());
		} catch (CoreException e) {
		}
		return map;
	}

	protected void clearLoaddedData(IConfiguration cfg){
		IProject proj = cfg.getOwner().getProject();
		try {
			proj.setSessionProperty(propsSessionProperty, null);
		} catch (CoreException e) {
		}
	}

	protected Properties loadProperties(IConfiguration cfg, IBuildObject bo){
		Map map = getData(cfg);
		
		return getPropsFromData(map, bo);
	}
	
	protected Properties getPropsFromData(Map data, IBuildObject bo){
		Object oVal = data.get(bo.getId());
		Properties props = null;
		if(oVal instanceof String){
			props = stringToProps((String)oVal);
			data.put(bo.getId(), props);
		} else if (oVal instanceof Properties){
			props = (Properties)oVal;
		}
		
		if(props == null){
			props = new Properties();
			data.put(bo.getId(), props);
		} 

		return props;
	}


	protected void storeData(IConfiguration cfg){
		Map map = getLoaddedData(cfg);

		if(map != null)
			storeData(cfg, map);
	}

	protected Properties mapToProps(Map map){
		Properties props = null;
		if(map != null && map.size() > 0){
			props = new Properties();
			for(Iterator iter = map.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				String key = (String)entry.getKey();
				String value = null;
				Object oVal = entry.getValue();
				if(oVal instanceof Properties){
					value = propsToString((Properties)oVal);
				} else if (oVal instanceof String){
					value = (String)oVal;
				}
				
				if(key != null && value != null)
					props.setProperty(key, value);
			}
		}
		
		return props;
	}
	
	protected String propsToString(Properties props){
		if(props == null || props.size() == 0)
			return null;
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			props.store(stream, ""); //$NON-NLS-1$
		} catch (IOException e1) {
		}

		byte[] bytes= stream.toByteArray();
		
		String value = null;
		try {
			value= new String(bytes, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			value= new String(bytes);
		}
		return value;
	}
	
	protected Properties stringToProps(String str){
		Properties props = null;
		if(str != null){
			props = new Properties();
			byte[] bytes;
			try {
				bytes = str.getBytes("UTF-8"); //$NON-NLS-1$
			} catch (UnsupportedEncodingException e) {
				bytes = str.getBytes();
			}

			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			try {
				props.load(stream);
			} catch (IOException e) {
				props = null;
			}
		}
		return props;
	}
	
	protected void storeData(IConfiguration cfg, Map map){
		String str = null;
		Properties props = mapToProps(map);

		str = propsToString(props);

		storeString(cfg, str);
	}
	
	protected void storeString(IConfiguration cfg, String str){
		IProject proj = cfg.getOwner().getProject();
		
		Preferences prefs = getNode(proj);
		if(prefs != null){
			if(str != null)
				prefs.put(cfg.getId(), str);
			else
				prefs.remove(cfg.getId());
			try {
				prefs.flush();
			} catch (BackingStoreException e) {
			}
		}
	}

	protected String loadString(IConfiguration cfg){
		IProject proj = cfg.getOwner().getProject();
		
		if(proj == null || !proj.exists() || !proj.isOpen())
			return null;

		String str = null;
		Preferences prefs = getNode(proj);
		if(prefs != null)
			str = prefs.get(cfg.getId(), null);
		return str;	
	}
	
	protected Preferences getNode(IProject project){
		Preferences prefs = new ProjectScope(project).getNode(ManagedBuilderCorePlugin.getUniqueIdentifier());
		if(prefs != null)
			return prefs.node(NODE_NAME);
		return null;
	}

	
	protected Map getData(IConfiguration cfg){
		Map map = getLoaddedData(cfg);
		
		if(map == null){
			map = loadData(cfg);
			
			setLoaddedData(cfg, map);
		}
		
		return map;
	}
	
	protected Map loadData(IConfiguration cfg){
		Map map = null;
		String str = loadString(cfg);

		Properties props = stringToProps(str);
			
		map = propsToMap(props);

		if(map == null)
			map = new HashMap();
		
		return map;
	}
	
	protected Map propsToMap(Properties props){
		if(props != null)
			return new HashMap(props);
		return null;
	}

	protected void setLoaddedData(IConfiguration cfg, Map data){
		try {
			IProject proj = cfg.getOwner().getProject();
			Map map = (Map)proj.getSessionProperty(propsSessionProperty);
			if(map == null){
				map = new HashMap();
				proj.setSessionProperty(propsSessionProperty, map);
			}
			map.put(cfg.getId(), data);
		} catch (CoreException e) {
		}
	}

	public void setProperty(IConfiguration cfg, String key, String value){
		setProperty(cfg, cfg, key, value);
	}

	public void setProperty(IResourceConfiguration rcCfg, String key, String value){
		setProperty(rcCfg.getParent(), rcCfg, key, value);
	}

	public void setProperty(IToolChain tc, String key, String value){
		setProperty(tc.getParent(), tc, key, value);
	}

	public void setProperty(ITool tool, String key, String value){
		setProperty(getConfiguration(tool), tool, key, value);
	}
	
	public void setProperty(IBuilder builder, String key, String value){
		setProperty(getConfiguration(builder), builder, key, value);
	}

	public String getProperty(IConfiguration cfg, String key){
		return getProperty(cfg, cfg, key);
	}

	public String getProperty(IResourceConfiguration rcCfg, String key){
		return getProperty(rcCfg.getParent(), rcCfg, key);
	}

	public String getProperty(IToolChain tc, String key){
		return getProperty(tc.getParent(), tc, key);
	}

	public String getProperty(ITool tool, String key){
		return getProperty(getConfiguration(tool), tool, key);
	}
	
	public String getProperty(IBuilder builder, String key){
		return getProperty(getConfiguration(builder), builder, key);
	}
	
	public void clearProperties(IConfiguration cfg){
		clearLoaddedData(cfg);
		storeData(cfg, null);
	}

	private IConfiguration getConfiguration(IBuilder builder){
		IToolChain tc = builder.getParent();
		if(tc != null)
			return tc.getParent();
		return null;
	}

	private IConfiguration getConfiguration(ITool tool){
		IBuildObject p = tool.getParent();
		IConfiguration cfg = null;
		if(p instanceof IToolChain){
			cfg = ((IToolChain)p).getParent();
		} else if(p instanceof IResourceConfiguration){
			cfg = ((IResourceConfiguration)p).getParent();
		}
		return cfg;
	}

	public void serialize(IConfiguration cfg){
		if(cfg.isTemporary())
			return;
		
		storeData(cfg);
	}

	public void serialize(){
		IProject projects[] = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for(int i = 0; i < projects.length; i++){
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(projects[i], false);
			if(info != null && info.isValid() && info.getManagedProject() != null){
				IConfiguration cfgs[] = info.getManagedProject().getConfigurations();
				for(int j = 0; j < cfgs.length; j++){
					serialize(cfgs[j]);
				}
			}
		}
	}

}
