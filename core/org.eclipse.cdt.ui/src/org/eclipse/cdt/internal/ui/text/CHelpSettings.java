/**********************************************************************
 * Copyright (c) 2004, 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpProvider;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class represents the Help settings for the current project
 * 
 * @since 2.1
 */
public class CHelpSettings {
	public static final String CONTRIBUTION_EXTENSION = "CHelpProvider"; //$NON-NLS-1$

	final private static String ELEMENT_PROJECT = "project"; //$NON-NLS-1$
	final private static String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$

	private IProject fProject;
	private static IConfigurationElement fConfigElements[] = null;
	private CHelpProviderDescriptor fProviderDescriptors[] = null;
	
	public CHelpSettings(IProject project){
		this(project,null);
	}
	
	public CHelpSettings(IProject project, Element parentElement){
		fProject = project;
		
		if(parentElement == null)
			return;
		
		Element projectElement = getProjectElement(parentElement);
		if(projectElement == null)
			return;
		
		getCHelpProviderDescriptors(projectElement);
	}
	
	private Element getProjectElement(Element parentElement){
		NodeList nodes = parentElement.getElementsByTagName(ELEMENT_PROJECT);
		for(int i = 0; i < nodes.getLength(); i++){
			Element curProject = (Element)nodes.item(i);
			if(getProject().getName().equals(curProject.getAttribute(ATTRIBUTE_NAME)))
				return curProject;
		}
		return null;
	}

	public IProject getProject(){
		return fProject;
	}
	
	public CHelpProviderDescriptor[] getCHelpProviderDescriptors(Element projectElement){
		if(fProviderDescriptors == null || projectElement != null){
			IConfigurationElement congifElements[] = getConfigElements();
			fProviderDescriptors = new CHelpProviderDescriptor[congifElements.length];
			for(int i = 0; i < congifElements.length; i++){
				fProviderDescriptors[i] = new CHelpProviderDescriptor(fProject,congifElements[i],projectElement);
			}
		}
		return fProviderDescriptors;
	}

	public CHelpProviderDescriptor[] getCHelpProviderDescriptors(){
		return getCHelpProviderDescriptors(null);
	}

	public CHelpBookDescriptor[] getCHelpBookDescriptors(){
		CHelpProviderDescriptor providerDescriptors[] = getCHelpProviderDescriptors();
		if(providerDescriptors.length == 0)
			return new CHelpBookDescriptor[0];
		
		List bookList = new ArrayList(); 	
		for(int i = 0; i < providerDescriptors.length; i++){
			CHelpBookDescriptor bookDescriptors[] = providerDescriptors[i].getCHelpBookDescriptors();
			if(bookDescriptors.length != 0)
				bookList.addAll(Arrays.asList(bookDescriptors));
		}
		return (CHelpBookDescriptor[])bookList.toArray(new CHelpBookDescriptor[bookList.size()]);
	}
	
	private static IConfigurationElement[] getConfigElements(){
		if(fConfigElements == null){
			fConfigElements= Platform.getExtensionRegistry().getConfigurationElementsFor(CUIPlugin.PLUGIN_ID, CONTRIBUTION_EXTENSION);
			if(fConfigElements == null)
				fConfigElements = new IConfigurationElement[0];
		}
		return fConfigElements;
	}
	
	public IFunctionSummary getFunctionInfo(ICHelpInvocationContext context, String name){
		CHelpProviderDescriptor providerDescriptors[] = getCHelpProviderDescriptors();
		for(int i = 0; i < providerDescriptors.length; i++){
			ICHelpBook books[] = providerDescriptors[i].getEnabledMatchedCHelpBooks(context);
			if(books != null && books.length != 0){
				ICHelpProvider provider = providerDescriptors[i].getCHelpProvider();
				if(provider != null){
					IFunctionSummary summary = provider.getFunctionInfo(context,books,name);
					if(summary != null)
						return summary;
				}
			}
		}
		return null;
	}
	
	public IFunctionSummary[] getMatchingFunctions(ICHelpInvocationContext context, String frag){
		CHelpProviderDescriptor providerDescriptors[] = getCHelpProviderDescriptors();
		List sumaryList = new ArrayList();
		for(int i = 0; i < providerDescriptors.length; i++){
			ICHelpBook books[] = providerDescriptors[i].getEnabledMatchedCHelpBooks(context);
			if(books != null && books.length != 0){
				ICHelpProvider provider = providerDescriptors[i].getCHelpProvider();
				if(provider != null){
					IFunctionSummary summaries[] = provider.getMatchingFunctions(context,books,frag);
					if(summaries != null && summaries.length != 0)
						sumaryList.addAll(Arrays.asList(summaries));
				}
			}
		}
		if(sumaryList.size() == 0)
			return null;
		
		return (IFunctionSummary[])sumaryList.toArray(new IFunctionSummary[sumaryList.size()]);
	}
	
	public ICHelpResourceDescriptor[] getHelpResources(ICHelpInvocationContext context, String name){
		CHelpProviderDescriptor providerDescriptors[] = getCHelpProviderDescriptors();
		List resourcesList = new ArrayList();
		for(int i = 0; i < providerDescriptors.length; i++){
			ICHelpBook books[] = providerDescriptors[i].getEnabledMatchedCHelpBooks(context);
			if(books != null && books.length != 0){
				ICHelpProvider provider = providerDescriptors[i].getCHelpProvider();
				if(provider != null){
					ICHelpResourceDescriptor resources[] = provider.getHelpResources(context,books,name);
					if(resources != null && resources.length != 0)
						resourcesList.addAll(Arrays.asList(resources));
				}
			}
		}
		if(resourcesList.size() == 0)
			return null;
		
		return (ICHelpResourceDescriptor[])resourcesList.toArray(new ICHelpResourceDescriptor[resourcesList.size()]);
	}
	
	public void serialize(Document doc, Element parentElement){
		CHelpProviderDescriptor providerDescriptors[] = getCHelpProviderDescriptors();
		Element oldProjectElement = getProjectElement(parentElement);

		Element projectElement = doc.createElement(ELEMENT_PROJECT);
		projectElement.setAttribute(ATTRIBUTE_NAME,getProject().getName());

		if(oldProjectElement != null)
			parentElement.replaceChild(projectElement,oldProjectElement);
		else
			parentElement.appendChild(projectElement);

		for(int i = 0; i < providerDescriptors.length; i++){
			providerDescriptors[i].serialize(doc,projectElement);
		}
	}
}
