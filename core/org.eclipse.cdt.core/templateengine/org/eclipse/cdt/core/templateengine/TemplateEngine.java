/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * TemplateEngine is implemented as a Singleton. TemplateEngine is responsible for 
 * creating SharedDefaults and initializing the SharedDefaults. Template instances
 * are obtained from TemplateEngine.
 * 
 * @since 4.0
 */
public class TemplateEngine {

	public static String TEMPLATES_EXTENSION_ID = CCorePlugin.PLUGIN_ID + ".templates"; //$NON-NLS-1$
	public static String TEMPLATE_ASSOCIATIONS_EXTENSION_ID = CCorePlugin.PLUGIN_ID + ".templateAssociations"; //$NON-NLS-1$
	
	/**
	 * static reference to the Singleton TemplateEngine instance.
	 */
	private static TemplateEngine TEMPLATE_ENGINE;
	
	/**
	 * This is a Map <WizardID, TemplateInfo>.
	 */
	private Map/*<String, List<TemplateInfo>>*/ templateInfoMap;

	/**
	 * TemplateEngine constructor, create and initialize SharedDefaults.
	 */
	private TemplateEngine() {
		templateInfoMap = new HashMap/*<String, List<TemplateInfo>>*/();
		initializeTemplateInfoMap();
	}

	/**
	 * Returns all the TemplateCore objects, no filtering is done.
	 */
	public TemplateCore[] getTemplates() {
		TemplateInfo[] templateInfoArray = getTemplateInfos();
		List/*<TemplateCore>*/ tcores = new ArrayList/*<TemplateCore>*/();
		for (int i=0; i<templateInfoArray.length; i++) {
			TemplateInfo info = templateInfoArray[i];
			try {
				tcores.add(TemplateCore.getTemplate(info));
			} catch (TemplateInitializationException e) {
				CCorePlugin.log(e);
			}
		}
		return (TemplateCore[]) tcores.toArray(new TemplateCore[tcores.size()]);
	}

	/**
	 * Returns the TemplateCore for the first template defined for the specified parameters, or null
	 * if no such definition exists, or if there is an error initializing the template (the error will
	 * be logged). 
	 * @param projectType may not be null
	 * @param toolChain may be null to indicate no tool-chain filtering
	 * @param usageFilter a regex in java.util.regex.Pattern format, may be null to indicate no filtering
	 * @see java.util.regex.Pattern
	 * @return
	 */
	public TemplateCore getFirstTemplate(String projectType, String toolChain, String usageFilter) {
		TemplateInfo[] infos= getTemplateInfos(projectType, toolChain, usageFilter);
		if(infos.length>0) {
			try {
				return TemplateCore.getTemplate(infos[0]);
			} catch(TemplateInitializationException tie) {
				CCorePlugin.log(tie);
			}
		}
		return null;
	}
	
	/**
	 * Equivalent to calling the overloaded version of getFirstTemplate with null arguments for
	 * toolChain and usageFilter.
	 * @see TemplateEngine#getFirstTemplate(String, String, String)
	 * @return the first TemplateCore object registered, or null if this does not exist
	 */
	public TemplateCore getFirstTemplate(String projectType) {
		return getFirstTemplate(projectType, null, null);
	}

	/**
	 * This method will be called by Contianer UIs (Wizard, PropertyPage,
	 * PreferencePage). Create a Template instance, update the ValueStore, with
	 * SharedDefaults. This method calls the getTemplate(URL), after getting URL
	 * for the given String TemplateDescriptor.
	 */
	public TemplateCore[] getTemplates(String projectType, String toolChain, String usageFilter) {
		TemplateInfo[] templateInfoArray = getTemplateInfos(projectType, toolChain, usageFilter);
		List/*<Template>*/ templatesList = new ArrayList/*<Template>*/();
		for (int i=0; i<templateInfoArray.length; i++) {
			TemplateInfo info = templateInfoArray[i];
			try {
				templatesList.add(TemplateCore.getTemplate(info));
			} catch (TemplateInitializationException tie) {
				CCorePlugin.log(tie);
			}
		}
		return (TemplateCore[]) templatesList.toArray(new TemplateCore[templatesList.size()]);
	}
	
	public TemplateCore[] getTemplates(String projectType, String toolChain) {
		return getTemplates(projectType, toolChain, null);
	}

	public TemplateCore[] getTemplates(String projectType) {
		return getTemplates(projectType, null);
	}
	
	public TemplateCore getTemplateById(String templateId) {
		TemplateCore[] templates = getTemplates();
		
		for(int i=0; i<templates.length; i++) {
			TemplateCore template = templates[i];
			if (template.getTemplateId().equalsIgnoreCase(templateId)) {
				return template;
			}
		}
		return null;
	}
	
	/**
	 * return the SharedDefaults.
	 * 
	 * @return
	 */
	public static Map/*<String, String>*/ getSharedDefaults() {
		return SharedDefaults.getInstance().getSharedDefaultsMap();
	}

	/**
	 * update The SharedDefaults Map. This method will be called by Container
	 * UIs. After collecting data from UIPages, the IDs with Persist attribute
	 * as true, has to be persisted in SharedDefaults XML. For the same this
	 * method is called by passing the ValueStore(updated with user entered
	 * values). Get the PersistTrueIDs from TemplateDescriptor. Persist the
	 * values of IDs in ValueStore, which are also present in PersistTrueIDs
	 * vector.
	 * @param template
	 * @param aSharedValue
	 */
	public void updateSharedDefaults(TemplateCore template) {
		Map/*<String, String>*/ tobePersisted = new HashMap/*<String, String>*/();
		Map/*<String, String>*/ valueStore = template.getValueStore();
		
		for (Iterator i = template.getPersistTrueIDs().iterator(); i.hasNext();) {
			String key = (String) i.next();
			tobePersisted.put(key, valueStore.get(key));
		}
		SharedDefaults.getInstance().updateShareDefaultsMap(tobePersisted);
	}

	/**
	 * create the singleton instance, check for null condition of
	 * TEMPLATE_ENGINE. If TEMPLATE_ENGINE is null create the TemplateEngine
	 * instance assign it to TEMPLATE_ENGINE. There is no need to have
	 * synchronized here(while creating TemplateEngine).
	 * 
	 * @return TEMPLATE_ENGINE, instance of TemplateEngine.
     * 
     * @since 4.0
	 */
	public static TemplateEngine getDefault() {
		if(TEMPLATE_ENGINE==null) {
			TEMPLATE_ENGINE = new TemplateEngine();
		}
		return TEMPLATE_ENGINE;
	}

	/**
	 * From the extension point take the class implementing the required
	 * functionality. Update the local HashMap of page-id and URL. This is for
	 * extension point "templates"
	 */
	private void initializeTemplateInfoMap() {
		String templateId = null;
		String location = null;
		String pluginId = null;
		String projectType = null;
		String filterPattern = null;
		String usage = null;
		boolean isCategory = false;
		String extraPagesProvider = null;

		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(TEMPLATES_EXTENSION_ID).getExtensions();
		for(int i=0; i<extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			pluginId = extension.getNamespaceIdentifier(); // Plugin-id of the extending plugin.
			for(int j=0; j<configElements.length; j++) {
				IConfigurationElement config = configElements[j];
				templateId = config.getAttribute(TemplateEngineHelper.ID);
				location = config.getAttribute(TemplateEngineHelper.LOCATION);
				projectType = config.getAttribute(TemplateEngineHelper.PROJECT_TYPE);
				filterPattern = config.getAttribute(TemplateEngineHelper.FILTER_PATTERN);
				usage = config.getAttribute(TemplateEngineHelper.USAGE_DESCRIPTION);
				isCategory = Boolean.valueOf(config.getAttribute(TemplateEngineHelper.IS_CATEGORY)).booleanValue();
				extraPagesProvider = config.getAttribute(TemplateEngineHelper.EXTRA_PAGES_PROVIDER);
				
				IConfigurationElement[] toolChainConfigs = config.getChildren(TemplateEngineHelper.TOOL_CHAIN);
				Set toolChainIdSet = new HashSet();
				for (int k=0; k < toolChainConfigs.length; k++) {
					toolChainIdSet.add(toolChainConfigs[k].getAttribute(TemplateEngineHelper.ID));
				}
				
				TemplateInfo templateInfo = new TemplateInfo(templateId, projectType, filterPattern, location, 
														pluginId, toolChainIdSet,
														usage, extraPagesProvider, isCategory);
				if (!templateInfoMap.containsKey(projectType)) {
					templateInfoMap.put(projectType, new ArrayList/*<TemplateInfo>*/());
				}
				((List/*<TemplateInfo>*/)templateInfoMap.get(projectType)).add(templateInfo);
			}
		}
		// Check for tool Chains added to the templates outside template info definition
		addToolChainsToTemplates();
	}

	private void addToolChainsToTemplates() {
		String templateId = null;
		TemplateCore[] templates = getTemplates();

		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(TEMPLATE_ASSOCIATIONS_EXTENSION_ID).getExtensions();
		for(int i=0; i<extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			for(int j=0; j<configElements.length; j++) {
				IConfigurationElement config = configElements[j];
				templateId = config.getAttribute(TemplateEngineHelper.ID);
				
				IConfigurationElement[] toolChainConfigs = config.getChildren(TemplateEngineHelper.TOOL_CHAIN);
				Set toolChainIdSet = new HashSet();
				for (int k=0; k < toolChainConfigs.length; k++) {
					toolChainIdSet.add(toolChainConfigs[k].getAttribute(TemplateEngineHelper.ID));
				}
				
				for (int k=0; k < templates.length; k++) {
					String id = templates[k].getTemplateInfo().getTemplateId();
					if (id == null) {
						id = templates[k].getTemplateId();
					}
					if (id != null && id.equals(templateId)) {
						toolChainIdSet.addAll(Arrays.asList(templates[k].getTemplateInfo().getToolChainIds()));
						templates[k].getTemplateInfo().setToolChainSet(toolChainIdSet);
					}
				}
			}
		}
	}
	
	/**
	 * Gets an array of template info objects matching the criteria passed as params.
	 * @param projectType may not be null
	 * @param toolChain may be null to indicate no tool-chain
	 * @param usageFilter a usage string which is matched against the filter from the template, may be null
	 * to indicate no usage filtering
	 * @return an array of template infos (never null)
	 */
	public TemplateInfo[] getTemplateInfos(String projectType, String toolChain, String usageFilter) {
		List/*<TemplateInfo>*/ templateInfoList = (List/*<TemplateInfo*/) templateInfoMap.get(projectType.trim());
		List/*<TemplateInfo>*/ matchedTemplateInfoList = new ArrayList/*<TemplateInfo>*/();
		
		if (templateInfoList != null) {
			for (Iterator i = templateInfoList.iterator(); i.hasNext(); ) {
				TemplateInfo templateInfo = (TemplateInfo) i.next();
				String filterPattern = templateInfo.getFilterPattern();
				String[] toolChains = templateInfo.getToolChainIds();

				if (toolChain != null) {
					for (int j=0; j < toolChains.length; j++) {
						if (toolChains[j].equals(toolChain)) {
							if (usageFilter != null && filterPattern.matches(usageFilter)) {
								matchedTemplateInfoList.add(templateInfo);
							} else if (usageFilter == null) {
								matchedTemplateInfoList.add(templateInfo);
							}
							continue;
						}
					}
				} else {
					if (usageFilter != null && filterPattern.matches(usageFilter)) {
						matchedTemplateInfoList.add(templateInfo);
					} else if (usageFilter == null) {
						matchedTemplateInfoList.add(templateInfo);
					}
				}
			}
		}
		return (TemplateInfo[]) matchedTemplateInfoList.toArray(new TemplateInfo[matchedTemplateInfoList.size()]);
	}

	public TemplateInfo[] getTemplateInfos(String projectType, String toolChain) {
		return getTemplateInfos(projectType, toolChain, null);
	}

	public TemplateInfo[] getTemplateInfos(String projectType) {
		return getTemplateInfos(projectType, null, null);
	}

	/**
	 * Returns all TemplateInfo objects known to the TemplateEngine
	 * @return
	 */
	public TemplateInfo[] getTemplateInfos() {
		List/*<TemplateInfo>*/ infoList = new ArrayList/*<TemplateInfo>*/();
		for (Iterator i = templateInfoMap.values().iterator(); i.hasNext();) {
			infoList.addAll((List/*<TemplateInfo>*/)i.next());
		}

		return (TemplateInfo[]) infoList.toArray(new TemplateInfo[infoList.size()]);
	}


	/**
	 * Getter for templateInfoMap
	 * 
	 * @return
	 */
	public Map/*<String, List<TemplateInfo>>*/ getTemplateInfoMap() {
		return templateInfoMap;
	}

	/**
	 * Returns the Template Schema URL
	 * 
	 * @return URL of the Template Schema.
	 * @throws IOException
	 */
	public URL getTemplateSchemaURL() throws IOException {
		return FileLocator.toFileURL(Platform.getBundle(CCorePlugin.PLUGIN_ID).getEntry("schema/TemplateDescriptorSchema.xsd")); //$NON-NLS-1$
	}
	
	/**
	 * Returns the Children of the Element.
	 * @param element
	 * @return List of the child elelments
     * 
     * @since 4.0
	 */
	public static List/*<Element>*/ getChildrenOfElement(Element element) {
		List/*<Element>*/ list = new ArrayList/*<Element>*/();
		NodeList children = element.getChildNodes();
		for (int i = 0, l = children.getLength(); i < l; i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				list.add((Element) child);
			}
		}
		return list;
	}
	
	/**
	 * Returns the child elements by Tag
	 * 
	 * @param element
	 * @param tag
	 * @return List of child elements
     * 
     * @since 4.0
	 */
	public static List/*<Element>*/ getChildrenOfElementByTag(Element element, String tag) {
		List/*<Element>*/ list = new ArrayList/*<Element>*/();
		NodeList children = element.getChildNodes();
		for (int i = 0, l = children.getLength(); i < l; i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(tag)) {
				list.add((Element) child);
			}
		}
		return list;
	}
}
