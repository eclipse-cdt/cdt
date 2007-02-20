/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.cdtvariables;

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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.XmlStorageElement;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationSpecSettings;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.cdt.internal.core.settings.model.IInternalCCfgInfo;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
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
public class UserDefinedVariableSupplier extends CoreMacroSupplierBase {
//	public static final String MACROS_ELEMENT_NAME = "macros"; //$NON-NLS-1$
	public static final String NODENAME = "macros";  //$NON-NLS-1$
	public static final String PREFNAME_WORKSPACE = "workspace";  //$NON-NLS-1$

	private static UserDefinedVariableSupplier fInstance;
	
	private StorableCdtVariables fWorkspaceMacros;
	
	private StorableCdtVariables getStorableMacros(int contextType, Object contextData){
		StorableCdtVariables macros = null;
		switch(contextType){
		case ICoreVariableContextInfo.CONTEXT_CONFIGURATION:
			if(contextData instanceof IInternalCCfgInfo){
				try {
					CConfigurationSpecSettings settings = ((IInternalCCfgInfo)contextData).getSpecSettings();
					macros = settings.getMacros();
				} catch (CoreException e) {
				}
			}
			break;
		case ICoreVariableContextInfo.CONTEXT_WORKSPACE:
			if(contextData == null || contextData instanceof IWorkspace){
				if(fWorkspaceMacros == null)
					fWorkspaceMacros = loadWorkspaceMacros();
				macros = fWorkspaceMacros;
			}
		}

		return macros;
	}
	
	private UserDefinedVariableSupplier(){
		
	}

	public static UserDefinedVariableSupplier getInstance(){
		if(fInstance == null)
			fInstance = new UserDefinedVariableSupplier();
		return fInstance;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
	 */
	public ICdtVariable getMacro(String macroName, int contextType, Object contextData) {
		if(macroName == null || "".equals(macroName))  //$NON-NLS-1$
			return null;
		
		StorableCdtVariables macros = getStorableMacros(contextType,contextData);
		if(macros != null)
			return macros.getMacro(macroName);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacros(int, java.lang.Object)
	 */
	public ICdtVariable[] getMacros(int contextType, Object contextData) {
		StorableCdtVariables macros = getStorableMacros(contextType,contextData);
		if(macros != null)
			return macros.getMacros();
		return null;
	}
	
	public ICdtVariable createMacro(String macroName, 
					int type, 
					String value, 
					int contextType,
					Object contextData){
		if(macroName == null || "".equals(macroName))  //$NON-NLS-1$
			return null;
		StorableCdtVariables macros = getStorableMacros(contextType, contextData);
		if(macros == null)
			return null;
		
		ICdtVariable macro = macros.createMacro(macroName,type,value);
		if(macros.isChanged()){
			setRebuildStateForContext(contextType, contextData);
			macros.setChanged(false);
		}
		
		return macro;

	}

	public ICdtVariable createMacro(String macroName, 
				int type, 
				String value[], 
				int contextType,
				Object contextData){
		if(macroName == null || "".equals(macroName))  //$NON-NLS-1$
			return null;
		StorableCdtVariables macros = getStorableMacros(contextType, contextData);
		if(macros == null)
			return null;
		
		ICdtVariable macro = macros.createMacro(macroName,type,value);
		if(macros.isChanged()){
			setRebuildStateForContext(contextType, contextData);
			macros.setChanged(false);
		}
		
		return macro;

	}

	public ICdtVariable createMacro(ICdtVariable copy, int contextType, Object contextData){
		if(copy == null)
			return null;
		String macroName = copy.getName();
		if(macroName == null || "".equals(macroName))  //$NON-NLS-1$
			return null;
		StorableCdtVariables macros = getStorableMacros(contextType, contextData);
		if(macros == null)
			return null;
		
		ICdtVariable macro = macros.createMacro(copy);
		if(macros.isChanged()){
			setRebuildStateForContext(contextType, contextData);
			macros.setChanged(false);
		}
		
		return macro;
	}

	public ICdtVariable deleteMacro(String name, int contextType, Object contextData){
		StorableCdtVariables macros = getStorableMacros(contextType,contextData);
		if(macros == null)
			return null;
		ICdtVariable macro = macros.deleteMacro(name);
		if(macro != null)
			setRebuildStateForContext(contextType, contextData);
		
		return macro;
	}
	
	public void deleteAll(int contextType, Object contextData){
		StorableCdtVariables macros = getStorableMacros(contextType, contextData);
		if(macros == null)
			return;

		if(macros.deleteAll())
			setRebuildStateForContext(contextType, contextData);
	}
	
	public void setMacros(ICdtVariable m[], int contextType, Object contextData){
		StorableCdtVariables macros = getStorableMacros(contextType, contextData);
		if(macros == null)
			return;

		macros.setMacros(m);
		if(macros.isChanged()){
			setRebuildStateForContext(contextType, contextData);
			macros.setChanged(false);
		}
	}

	/*
	 * 
	 * methods used for loadding/storing workspace macros from properties 
	 * 
	 */
	public void storeWorkspaceVariables(boolean force){
		try{
			if(fWorkspaceMacros != null)
				storeWorkspaceMacros(fWorkspaceMacros,force);
		}catch(CoreException e){
		}
	}
	
	public StorableCdtVariables getWorkspaceVariablesCopy(){
		StorableCdtVariables vars = getStorableMacros(ICoreVariableContextInfo.CONTEXT_WORKSPACE, null);
		return new StorableCdtVariables(vars, false);
	}
	
	public void setWorkspaceVariables(StorableCdtVariables vars) throws CoreException{
		fWorkspaceMacros = new StorableCdtVariables(vars, false);
		
		storeWorkspaceVariables(true);
	}
	
	private Preferences getWorkspaceNode(){
		Preferences prefNode = new InstanceScope().getNode(CCorePlugin.PLUGIN_ID);
		if(prefNode == null)
			return null;
		
		return prefNode.node(NODENAME);
	}

	
	/*
	 * loads the stored workspace macros
	 */
	protected StorableCdtVariables loadWorkspaceMacros(){
		InputStream stream = loadInputStream(getWorkspaceNode(),PREFNAME_WORKSPACE);
		if(stream == null)
			return new StorableCdtVariables(false);
		return loadMacrosFromStream(stream);
	}
	
	/*
	 * stores the given macros 
	 */
	protected void storeWorkspaceMacros(StorableCdtVariables macros, boolean force) throws CoreException{
		if(!macros.isDirty() && !force)
			return;
		
		ByteArrayOutputStream stream = storeMacrosToStream(macros);
		if(stream == null)
			return;
		storeOutputStream(stream,getWorkspaceNode(),PREFNAME_WORKSPACE);
	}
	
	private StorableCdtVariables loadMacrosFromStream(InputStream stream){
		try{
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource inputSource = new InputSource(stream);
			Document document = parser.parse(inputSource);
			Element rootElement = document.getDocumentElement();
			
			if(!StorableCdtVariables.MACROS_ELEMENT_NAME.equals(rootElement.getNodeName()))
				return null;
			
			return new StorableCdtVariables(new XmlStorageElement(rootElement), true);
		}
		catch(ParserConfigurationException e){
			
		}
		catch(SAXException e){
			
		}
		catch(IOException e){
			
		}
		
		return null;
	}
	
	private ByteArrayOutputStream storeMacrosToStream(StorableCdtVariables macros) throws CoreException{
		try{
			DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
			DocumentBuilder builder= factory.newDocumentBuilder();		
			Document document= builder.newDocument();
			
			Element rootElement = document.createElement(StorableCdtVariables.MACROS_ELEMENT_NAME);
			document.appendChild(rootElement);
			ICStorageElement storageElement = new XmlStorageElement(rootElement);
			macros.serialize(storageElement);
			
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
			throw ExceptionFactory.createCoreException(e.getMessage(),
					e);
		}
		catch(TransformerConfigurationException e){
			throw ExceptionFactory.createCoreException(e.getMessage(),
					e);
		}
		catch(TransformerException e){
			throw ExceptionFactory.createCoreException(e.getMessage(),
					e);
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
			throw ExceptionFactory.createCoreException(new IllegalArgumentException());
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
			throw ExceptionFactory.createCoreException(e.getMessage(),
					e);
		}
	}
	
	protected void setRebuildStateForContext(int contextType, Object contextData){
/*		
		switch(contextType){
		case DefaultMacroContextInfo.CONTEXT_CONFIGURATION:
			if(contextData instanceof IConfiguration){
				((IConfiguration)contextData).setRebuildState(true);
			}
			break;
		case DefaultMacroContextInfo.CONTEXT_WORKSPACE:
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

*/		
	}
	
}
