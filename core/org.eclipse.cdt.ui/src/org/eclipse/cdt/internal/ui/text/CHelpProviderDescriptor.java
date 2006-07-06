/**********************************************************************
 * Copyright (c) 2004, 2006 Intel Corporation and others.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpProvider;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;

/**
 * This class represents the CHelpProvider settings
 * 
 * @since 2.1
 */
public class CHelpProviderDescriptor {
	private static final String CLASS= "class"; //$NON-NLS-1$

	final private static String ELEMENT_PROVIDER = "provider"; //$NON-NLS-1$
	final private static String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	private static Map fProvidersMap = null;

	private ICHelpProvider fHelpProvider = null;
	private IConfigurationElement fConfigElement;
	private CHelpBookDescriptor fHelpBookDescriptors[] = null;
	private IProject fProject;

	public CHelpProviderDescriptor(IProject project, IConfigurationElement element){
		this(project,element,null);
	}
	
	public CHelpProviderDescriptor(IProject project, IConfigurationElement configElement, Element parentElement){
		fConfigElement = configElement;
		fProject = project;
		
		if(parentElement == null)
			return;

		Element projectElement = getDescriptorElement(parentElement);
			
		if(projectElement == null)
			return;
		
		getCHelpBookDescriptors(projectElement);
	}
	
	private Element getDescriptorElement(Element parentElement){
		String id = getConfigurationElement().getAttribute(ATTRIBUTE_ID);
		if(id == null || "".equals(id)) //$NON-NLS-1$
			return null;

		NodeList nodes = parentElement.getElementsByTagName(ELEMENT_PROVIDER);
		for(int i = 0; i < nodes.getLength(); i++){
			Element descriptorEl = (Element)nodes.item(i);
			if(id.equals(descriptorEl.getAttribute(ATTRIBUTE_ID))){
				return descriptorEl;
			}
		}
		return null;
	}

	private static Map getProvidersMap(){
		if(fProvidersMap == null){
			fProvidersMap = new HashMap();
		}
		return fProvidersMap;
	}
	
	private static ICHelpProvider getCHelpProvider(IConfigurationElement element){
		String id = element.getAttribute(ATTRIBUTE_ID);
		if(id == null || "".equals(id)) //$NON-NLS-1$
			return null;

		Map providersMap = getProvidersMap();
		try{
			ICHelpProvider provider = (ICHelpProvider)providersMap.get(id);
			if(provider == null){
				provider = (ICHelpProvider)element.createExecutableExtension(CLASS);
				providersMap.put(id,provider);

			final ICHelpProvider c = provider;
			// Run the initialiser the class
			ISafeRunnable runnable = new ISafeRunnable() {
				public void run() throws Exception {
					// Initialize
					c.initialize();
				}
				public void handleException(Throwable exception) {
				}
			};
			SafeRunner.run(runnable);
			}
			return provider;
		}
		catch(Exception e){
			return null;
		}
	}
	
	public IConfigurationElement getConfigurationElement(){
		return fConfigElement;
	}

	public ICHelpProvider getCHelpProvider(){
		if(fHelpProvider == null)
			fHelpProvider = getCHelpProvider(fConfigElement);
		return fHelpProvider;
	}
	
	public CHelpBookDescriptor [] getCHelpBookDescriptors(Element projectElement){
		if (fHelpBookDescriptors == null || projectElement != null){
			ICHelpProvider provider = getCHelpProvider();
			if (provider != null && fProject != null) {
				ICHelpBook books[] = provider.getCHelpBooks();
				if(books != null){
					List descriptorList = new ArrayList();
					for(int i = 0; i < books.length; i++){
						CHelpBookDescriptor des = new CHelpBookDescriptor(books[i],projectElement);
						if(des.matches(fProject))
							descriptorList.add(des);
					}
					fHelpBookDescriptors = (CHelpBookDescriptor[])descriptorList.toArray(new CHelpBookDescriptor[descriptorList.size()]);
				}
			}
			if(fHelpBookDescriptors == null)
				fHelpBookDescriptors = new CHelpBookDescriptor[0];
		}
		return fHelpBookDescriptors;
	}

	public CHelpBookDescriptor [] getCHelpBookDescriptors(){
		return getCHelpBookDescriptors(null);
	}

	ICHelpBook[] getEnabledMatchedCHelpBooks(ICHelpInvocationContext context){
		CHelpBookDescriptor bookDescriptors[] = getCHelpBookDescriptors();
		if(bookDescriptors.length == 0)
			return null;
		List bookList = new ArrayList();
		for(int i = 0; i < bookDescriptors.length; i++){
			if(bookDescriptors[i].isEnabled() && bookDescriptors[i].matches(context))
				bookList.add(bookDescriptors[i].getCHelpBook());
		}
		return (ICHelpBook[])bookList.toArray(new ICHelpBook[bookList.size()]);
	}
	
	public void serialize(Document doc, Element parentElement){
		String id = getConfigurationElement().getAttribute(ATTRIBUTE_ID);
		if(id == null || "".equals(id)) //$NON-NLS-1$
			return;

		CHelpBookDescriptor bookDescriptors[] = getCHelpBookDescriptors();
		Element providerElement = doc.createElement(ELEMENT_PROVIDER);
		providerElement.setAttribute(ATTRIBUTE_ID,id);
		parentElement.appendChild(providerElement);
		
		for(int i = 0; i < bookDescriptors.length; i++){
			bookDescriptors[i].serialize(doc,providerElement);
		}
	}
}
