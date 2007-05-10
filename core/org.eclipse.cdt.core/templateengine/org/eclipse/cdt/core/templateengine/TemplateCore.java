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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.TemplateProcessHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xml.sax.SAXException;


/**
 * TemplateCore class is responsible providing the non-UI part of template and 
 * initiating process part of Template Engine. This is created per TemplateDescriptor basis. 
 * Once The Template is created it creates a TemplateDescriptor for the XML file name given.
 * Template class extends this class with additional UI elements that are part of the template.
 * 
 * @since 4.0
 */
public class TemplateCore {

	private static final String DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String LABEL = "label"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String TYPE = "type"; //$NON-NLS-1$
	
	private static Map/*<TemplateInfo, Template>*/ templateCache = new HashMap/*<TemplateInfo, Template>*/();

	public static final Comparator/*<Template>*/ TEMPLATE_ID_CASE_INSENSITIVE_COMPARATOR = new Comparator/*<Template>*/() {
		public int compare(Object/*Template*/ t1, Object/*Template*/ t2) {
			return String.CASE_INSENSITIVE_ORDER.compare(((TemplateCore)t1).getTemplateId(), ((TemplateCore)t2).getTemplateId());
		}		
	};

	private TemplateDescriptor templateDescriptor;
	private Map/*<String, String>*/ valueStore;
	private TemplateInfo templateInfo;
	private Set/*<String>*/ allMacrosInProcesses;
	private TemplateProcessHandler processHandler;
	private String description;
	private String label;
	private String templateId;
	private String templateType;
	private boolean fireDirtyEvents;
	
	/**
	 * Constructor
	 * 
	 * @param templateInfo
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */

	protected TemplateCore(TemplateInfo templateInfo) throws IOException, SAXException, ParserConfigurationException {
		this.templateInfo = templateInfo;
		templateDescriptor = new TemplateDescriptor(TemplateEngineHelper.getTemplateResourceURL(templateInfo.getPluginId(), templateInfo.getTemplatePath()));
		valueStore = new ValueStore/*<String, String>*/(this);
		valueStore.putAll(templateDescriptor.getTemplateDefaults(templateDescriptor.getRootElement()));
		valueStore.putAll(TemplateEngine.getDefault().getSharedDefaults());
		valueStore.put("projectType", templateInfo.getProjectType()); //$NON-NLS-1$

		processHandler = new TemplateProcessHandler(this);
		allMacrosInProcesses = processHandler.getAllMacros();

		fireDirtyEvents = true;
	}

	/**
	 * Returns All Missing Macros In Processes.
	 * @return Set
	 */
	public Set/*<String>*/ getAllMissingMacrosInProcesses() {
		Set/*<String>*/ set = new TreeSet/*<String>*/(allMacrosInProcesses);
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			if (valueStore.get(iter.next()) != null) {
				iter.remove();
			}
		}
		return set;
	}

	/**
	 * return the ValueStore maintained by this Template.
	 * @return   ValueStore.
	 */
	public Map/*<String, String>*/ getValueStore() {
		return valueStore;
	}

	/**
	 * This is the List of IDs from TemplateDescriptor with "attribute" Persist
	 * as "true"
	 * 
	 * @return Vector of IDs.
	 */
	public List/*<String>*/ getPersistTrueIDs() {
		return templateDescriptor.getPersistTrueIDs();
	}

	/**
	 * return the TemplateInfo instance corresponding to this Template.
	 * @return   TemplateInfo.
	 */
	public TemplateInfo getTemplateInfo() {
		return templateInfo;
	}

	/**
	 * TemplateDescriptor for which, this Template is created.
	 */
	public TemplateDescriptor getTemplateDescriptor() {
		return templateDescriptor;
	}
	
	/**
	 * @return   String, which contains the description
	 */
	public String getDescription() {
        if (description == null) {
        	description = templateDescriptor.getRootElement().getAttribute(DESCRIPTION).trim();
        }
        return description;
	}

	/**
	 * @return   String, which contains the id of the template
	 */
	public String getTemplateId() {
        if (templateId == null) {
        	templateId = templateDescriptor.getRootElement().getAttribute(ID).trim();
        }
        return templateId;
	}
	
	/**
	 * @return   String, which contains the id of the template
	 */
	public String getTemplateType() {
        if (templateType == null) {
        	templateType = templateDescriptor.getRootElement().getAttribute(TYPE).trim();
        }
        return templateType;
	}
	
	/**
	 * @return   String, which contains the Label
	 */
	public String getLabel() {
        if (label == null) {
        	label = templateDescriptor.getRootElement().getAttribute(LABEL).trim();
        }
        return label;
	}
	
	/**
	 * TemplateDescriptor for which, this Template is created.
	 */
	public TemplateProcessHandler getProcessHandler() {
		return processHandler;
	}
	
	public String toString()
	{
		return getLabel();
	}
	/**
	 * sets Dirty
	 *
	 */
	public void setDirty() {
		if (fireDirtyEvents) {
			synchronized (templateCache) {
				templateCache.remove(templateInfo);
			}
		}
	}

	/**
	 * initializeProcessBlockList() will create the ProcessBlockList,
	 * processPorcessBlockList() will invoke each process execution by assigning
	 * resources to each process (Ref. ProcessResourceManager).
	 * @param monitor 
	 */
	public IStatus[] executeTemplateProcesses(IProgressMonitor monitor, final boolean showError) {
		setDirty();
		TemplateEngine.getDefault().updateSharedDefaults(this);
		final IStatus[][] result = new IStatus[1][];
		try {
			result[0] = getProcessHandler().processAll(monitor);
		} catch (ProcessFailureException e) {
			TemplateEngineUtil.log(e);
			result[0] = new IStatus[] {new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e)};
		}
		return result[0];
	}
	
	/**
	 * Gets the Template
	 * 
	 * @param templateInfo
	 * @throws IOException
	 * @throws ProcessFailureException
	 * @throws SAXException
	 * @throws ParserConfigurationException
     * 
     * @since 4.0
	 */
	public static TemplateCore getTemplate(TemplateInfo templateInfo) throws IOException, ProcessFailureException, SAXException, ParserConfigurationException {
		synchronized (templateCache) {
			TemplateCore template = (TemplateCore) templateCache.get(templateInfo);
			if (template == null) {
				template = new TemplateCore(templateInfo);
				templateCache.put(templateInfo, template);
			}
			return template;
		}
	}
	
	private static class ValueStore/*<K, V>*/ extends HashMap/*<K, V>*/ {
		private static final long serialVersionUID = -4523467333437879406L;
		private TemplateCore template;

		ValueStore(TemplateCore template) {
			this.template = template;
		}
		
		public Object/*V*/ put(Object/*K*/ key, Object/*V*/ value) {
			Object/*V*/ v = super.put(key, value);
			template.setDirty();
			return v;
		}

		public void putAll(Map/*<? extends K, ? extends V>*/ map) {
			super.putAll(map);
			template.setDirty();
		}

		public Object/*V*/ remove(Object key) {
			Object/*V*/ v = super.remove(key);
			template.setDirty();
			return v;
		}
		
	}
}
