/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This supplier is used to obtain the user-defined macros
 * 
 * @since 3.0
 */
public class UserDefinedMacroSupplier implements IBuildMacroSupplier {
//	public static final String MACROS_ELEMENT_NAME = "macros"; //$NON-NLS-1$
	public static final String NODENAME = "macros";  //$NON-NLS-1$
	public static final String PREFNAME_WORKSPACE = "workspace";  //$NON-NLS-1$

	private static UserDefinedMacroSupplier fInstance;
	
	private StorableMacros fWorkspaceMacros;
	
	public boolean areMacrosExpanded(IConfiguration cfg){
		StorableMacros macros = getStorableMacros(IBuildMacroProvider.CONTEXT_CONFIGURATION, cfg);
		if(macros != null)
			return macros.isExpanded();
		return false;
	}

	public void setMacrosExpanded(IConfiguration cfg, boolean expanded){
		StorableMacros macros = getStorableMacros(IBuildMacroProvider.CONTEXT_CONFIGURATION, cfg);
		if(macros != null)
			macros.setExpanded(expanded);
	}

	private StorableMacros getStorableMacros(int contextType, Object contextData){
		StorableMacros macros = null;
		switch(contextType){
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			if(contextData instanceof IConfiguration){
				IToolChain toolChain = ((IConfiguration)contextData).getToolChain();
				if(toolChain instanceof ToolChain)
					macros = ((ToolChain)toolChain).getUserDefinedMacros();
			}
			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:
			if(contextData instanceof ManagedProject){
				macros = ((ManagedProject)contextData).getUserDefinedMacros();
			}
			break;
		case IBuildMacroProvider.CONTEXT_WORKSPACE:
			if(contextData instanceof IWorkspace){
				if(fWorkspaceMacros == null)
					fWorkspaceMacros = loadWorkspaceMacros();
				macros = fWorkspaceMacros;
			}
		}

		return macros;
	}
	
	private UserDefinedMacroSupplier(){
		
	}

	public static UserDefinedMacroSupplier getInstance(){
		if(fInstance == null)
			fInstance = new UserDefinedMacroSupplier();
		return fInstance;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
	 */
	public IBuildMacro getMacro(String macroName, int contextType,
			Object contextData) {
		if(macroName == null || "".equals(macroName))  //$NON-NLS-1$
			return null;
		
		StorableMacros macros = getStorableMacros(contextType,contextData);
		if(macros != null)
			return macros.getMacro(macroName);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacros(int, java.lang.Object)
	 */
	public IBuildMacro[] getMacros(int contextType, Object contextData) {
		StorableMacros macros = getStorableMacros(contextType,contextData);
		if(macros != null)
			return macros.getMacros();
		return null;
	}
	
	public IBuildMacro createMacro(String macroName, 
					int type, 
					String value, 
					int contextType,
					Object contextData){
		if(macroName == null || "".equals(macroName))  //$NON-NLS-1$
			return null;
		StorableMacros macros = getStorableMacros(contextType, contextData);
		if(macros == null)
			return null;
		
		IBuildMacro macro = macros.createMacro(macroName,type,value);
		if(macros.isChanged())
			setRebuildStateForContext(contextType, contextData);
		
		return macro;

	}

	public IBuildMacro createMacro(String macroName, 
				int type, 
				String value[], 
				int contextType,
				Object contextData){
		if(macroName == null || "".equals(macroName))  //$NON-NLS-1$
			return null;
		StorableMacros macros = getStorableMacros(contextType, contextData);
		if(macros == null)
			return null;
		
		IBuildMacro macro = macros.createMacro(macroName,type,value);
		if(macros.isChanged())
			setRebuildStateForContext(contextType, contextData);
		
		return macro;

	}

	public IBuildMacro createMacro(IBuildMacro copy, int contextType, Object contextData){
		if(copy == null)
			return null;
		String macroName = copy.getName();
		if(macroName == null || "".equals(macroName))  //$NON-NLS-1$
			return null;
		StorableMacros macros = getStorableMacros(contextType, contextData);
		if(macros == null)
			return null;
		
		IBuildMacro macro = macros.createMacro(copy);
		if(macros.isChanged())
			setRebuildStateForContext(contextType, contextData);
		
		return macro;
	}

	public IBuildMacro deleteMacro(String name, int contextType, Object contextData){
		StorableMacros macros = getStorableMacros(contextType,contextData);
		if(macros == null)
			return null;
		IBuildMacro macro = macros.deleteMacro(name);
		if(macro != null)
			setRebuildStateForContext(contextType, contextData);
		
		return macro;
	}
	
	public void deleteAll(int contextType, Object contextData){
		StorableMacros macros = getStorableMacros(contextType, contextData);
		if(macros == null)
			return;

		if(macros.deleteAll())
			setRebuildStateForContext(contextType, contextData);
	}
	
	public void setMacros(IBuildMacro m[], int contextType, Object contextData){
		StorableMacros macros = getStorableMacros(contextType, contextData);
		if(macros == null)
			return;

		macros.setMacros(m);
		if(macros.isChanged())
			setRebuildStateForContext(contextType, contextData);
	}

	/*
	 * 
	 * methods used for loadding/storing workspace macros from properties 
	 * 
	 */
	
	public void serialize(boolean force){
		try{
			if(fWorkspaceMacros != null)
				storeWorkspaceMacros(fWorkspaceMacros,force);
		}catch(CoreException e){
		}
	}
	
	private Preferences getWorkspaceNode(){
		Preferences prefNode = new InstanceScope().getNode(ManagedBuilderCorePlugin.getUniqueIdentifier());
		if(prefNode == null)
			return null;
		
		return prefNode.node(NODENAME);
	}

	
	/*
	 * loads the stored workspace macros
	 */
	protected StorableMacros loadWorkspaceMacros(){
		InputStream stream = loadInputStream(getWorkspaceNode(),PREFNAME_WORKSPACE);
		if(stream == null)
			return new StorableMacros();
		return loadMacrosFromStream(stream);
	}
	
	/*
	 * stores the given macros 
	 */
	protected void storeWorkspaceMacros(StorableMacros macros, boolean force) throws CoreException{
		if(!macros.isDirty() && !force)
			return;
		
		ByteArrayOutputStream stream = storeMacrosToStream(macros);
		if(stream == null)
			return;
		storeOutputStream(stream,getWorkspaceNode(),PREFNAME_WORKSPACE);
	}
	
	private StorableMacros loadMacrosFromStream(InputStream stream){
		try{
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource inputSource = new InputSource(stream);
			Document document = parser.parse(inputSource);
			Element rootElement = document.getDocumentElement();
			
			if(!StorableMacros.MACROS_ELEMENT_NAME.equals(rootElement.getNodeName()))
				return null;
			
			return new StorableMacros(rootElement);
		}
		catch(ParserConfigurationException e){
			
		}
		catch(SAXException e){
			
		}
		catch(IOException e){
			
		}
		
		return null;
	}
	
	private ByteArrayOutputStream storeMacrosToStream(StorableMacros macros) throws CoreException{
		try{
			DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
			DocumentBuilder builder= factory.newDocumentBuilder();		
			Document document= builder.newDocument();
			
			Element rootElement = document.createElement(StorableMacros.MACROS_ELEMENT_NAME);
			document.appendChild(rootElement);
			macros.serialize(document,rootElement);
			
			Transformer transformer=TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			DOMSource source = new DOMSource(document);
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(stream);
	
			transformer.transform(source, result);
			return stream;
		}
		catch(ParserConfigurationException e){
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					-1,
					e.getMessage(),
					e));
		}
		catch(TransformerConfigurationException e){
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					-1,
					e.getMessage(),
					e));
		}
		catch(TransformerException e){
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					-1,
					e.getMessage(),
					e));
		}
	}
	
	private InputStream loadInputStream(Preferences node, String key){
		if(node == null || key == null)
			return null;
		
		String value = node.get(key,null);
		if(value == null || value.length() == 0)
			return null;
		
		byte[] bytes;
		try {
			bytes = value.getBytes("UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			bytes = value.getBytes();
		}
		
		return new ByteArrayInputStream(bytes);
	}
	
	private void storeOutputStream(ByteArrayOutputStream stream, Preferences node, String key) throws CoreException{
		if(stream == null || node == null || key == null)
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					-1,
					ManagedMakeMessages.getResourceString("UserDefinedMacroSupplier.storeOutputStream.wrong.arguments"), //$NON-NLS-1$
					null));
		byte[] bytes= stream.toByteArray();
		
		String val = null;
		try {
			val= new String(bytes, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			val= new String(bytes);
		}
		
		node.put(key,val);
		
		try{
			node.flush();
		}
		catch(BackingStoreException e){
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					-1,
					e.getMessage(),
					e));
		}
	}
	
	protected void setRebuildStateForContext(int contextType, Object contextData){
		
		switch(contextType){
		case IBuildMacroProvider.CONTEXT_CONFIGURATION:
			if(contextData instanceof IConfiguration){
				((IConfiguration)contextData).setRebuildState(true);
			}
			break;
		case IBuildMacroProvider.CONTEXT_PROJECT:
			if(contextData instanceof IManagedProject){
				IConfiguration cfgs[] = ((IManagedProject)contextData).getConfigurations();
				for(int i = 0; i < cfgs.length; i++){
					cfgs[i].setRebuildState(true);
				}
			}
			break;
		case IBuildMacroProvider.CONTEXT_WORKSPACE:
			if(contextData instanceof IWorkspace){
				IProject projects[] = ((IWorkspace)contextData).getRoot().getProjects();
				for(int i = 0; i < projects.length; i++){
					if(ManagedBuildManager.manages(projects[i])){
						IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(projects[i]);
						if(info != null){
							IConfiguration cfgs[] = info.getManagedProject().getConfigurations();
							for(int j = 0; j < cfgs.length; j++){
								cfgs[j].setRebuildState(true);
							}
						}
					}
				}
			}
		}

		
	}
	
}
