/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationSpecSettings;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.cdt.internal.core.settings.model.IInternalCCfgInfo;
import org.eclipse.cdt.internal.core.settings.model.xml.XmlStorageElement;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
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
	static final String OLD_VARIABLE_PREFIX = "pathEntryVariable."; //$NON-NLS-1$


	private static UserDefinedVariableSupplier fInstance;
	
	private StorableCdtVariables fWorkspaceMacros;
	private Set<ICdtVariableChangeListener> fListeners;
	
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
		fListeners = Collections.synchronizedSet(new HashSet<ICdtVariableChangeListener>());
	}

	public static UserDefinedVariableSupplier getInstance(){
		if(fInstance == null)
			fInstance = getInstanceSynch();
		return fInstance;
	}

	private static UserDefinedVariableSupplier getInstanceSynch(){
		if(fInstance == null)
			fInstance = new UserDefinedVariableSupplier();
		return fInstance;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String, int, java.lang.Object)
	 */
	@Override
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
	@Override
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
		
		ICdtVariable oldVar = macros.getMacro(macroName);
		
		ICdtVariable macro = macros.createMacro(macroName,type,value);
		if(macros.isChanged()){
			macros.setChanged(false);
		}
		
		if(macro != null){
			VariableChangeEvent event = createVariableChangeEvent(macro, oldVar);
			if(event != null){
//				updateProjectInfo(contextType, contextData);
				notifyListeners(event);
			}
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
		
		ICdtVariable oldVar = macros.getMacro(macroName);

		ICdtVariable macro = macros.createMacro(macroName,type,value);
		if(macros.isChanged()){
			macros.setChanged(false);
		}
		
		if(macro != null){
			VariableChangeEvent event = createVariableChangeEvent(macro, oldVar);
			if(event != null){
//				updateProjectInfo(contextType, contextData);
				notifyListeners(event);
			}
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
		
		ICdtVariable oldVar = macros.getMacro(macroName);
		
		ICdtVariable macro = macros.createMacro(copy);
		if(macros.isChanged()){
			macros.setChanged(false);
		}
		
		if(macro != null){
			VariableChangeEvent event = createVariableChangeEvent(macro, oldVar);
			if(event != null){
//				updateProjectInfo(contextType, contextData);
				notifyListeners(event);
			}
		}
		return macro;
	}

	public ICdtVariable deleteMacro(String name, int contextType, Object contextData){
		StorableCdtVariables macros = getStorableMacros(contextType,contextData);
		if(macros == null)
			return null;
		ICdtVariable macro = macros.deleteMacro(name);
		if(macro != null){
			
			VariableChangeEvent event = createVariableChangeEvent(null, macro);
			if(event != null){
//				updateProjectInfo(contextType, contextData);
				notifyListeners(event);
			}

		}
		
		return macro;
	}
	
	public void deleteAll(int contextType, Object contextData){
		StorableCdtVariables macros = getStorableMacros(contextType, contextData);
		if(macros == null)
			return;

		ICdtVariable[] oldVars = macros.getMacros();
		
		if(macros.deleteAll()){
//			setRebuildStateForContext(contextType, contextData);
		}
		
		VariableChangeEvent event = createVariableChangeEvent(null, oldVars);
		if(event != null){
//			updateProjectInfo(contextType, contextData);
			notifyListeners(event);
		}

	}
	
	public void setMacros(ICdtVariable m[], int contextType, Object contextData){
		StorableCdtVariables macros = getStorableMacros(contextType, contextData);
		if(macros == null)
			return;

		ICdtVariable[] oldVars = macros.getMacros();
		
		macros.setMacros(m);
		if(macros.isChanged()){
			macros.setChanged(false);
			
			VariableChangeEvent event = createVariableChangeEvent(m, oldVars);
			if(event != null){
//				updateProjectInfo(contextType, contextData);
				notifyListeners(event);
			}

		}
	}
	
	static class VarKey {
		private ICdtVariable fVar;
		private boolean fNameOnly;
		
		VarKey(ICdtVariable var, boolean nameOnly){
			fVar = var;
			fNameOnly = nameOnly;
		}
		
		public ICdtVariable getVariable(){
			return fVar; 
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			
			if(!(obj instanceof VarKey))
				return false;
			
			VarKey other = (VarKey)obj;
			
			ICdtVariable otherVar = other.fVar;
			
			if(fVar == otherVar)
				return true;
			
			if(!CDataUtil.objectsEqual(fVar.getName(), otherVar.getName()))
				return false;

			if(fNameOnly)
				return true;
			
			if(fVar.getValueType() != otherVar.getValueType())
				return false;

			if(CdtVariableResolver.isStringListVariable(fVar.getValueType())){
				try {
					if(!Arrays.equals(fVar.getStringListValue(), otherVar.getStringListValue()))
						return false;
				} catch (CdtVariableException e) {
					CCorePlugin.log(e);
				}
			} else {
				try {
					if(!CDataUtil.objectsEqual(fVar.getStringValue(), otherVar.getStringValue()))
						return false;
				} catch (CdtVariableException e) {
					CCorePlugin.log(e);
				}
			}
				
			return true;
		}

		@Override
		public int hashCode() {
			int code = 51;
			
			String name = fVar.getName();
			if(name != null)
				code += name.hashCode();

			if(fNameOnly)
				return code;
			
			code += fVar.getValueType();
			if(CdtVariableResolver.isStringListVariable(fVar.getValueType())){
				try {
					String[] value = fVar.getStringListValue();
					if(value != null){
						for (String element : value) {
							code += element.hashCode();
						}
					}
				} catch (CdtVariableException e) {
					CCorePlugin.log(e);
				}
			} else {
				try {
					String value =fVar.getStringValue();
					if(value != null){
						code += value.hashCode();
					}
				} catch (CdtVariableException e) {
					CCorePlugin.log(e);
				}
			}
			
			return code;
		}
		
	}

	static VariableChangeEvent createVariableChangeEvent(ICdtVariable newVar, ICdtVariable oldVar){
		ICdtVariable newVars[] = newVar != null ? new ICdtVariable[]{newVar} : null;
		ICdtVariable oldVars[] = oldVar != null ? new ICdtVariable[]{oldVar} : null;
		
		return createVariableChangeEvent(newVars, oldVars);
	}

	static ICdtVariable[] varsFromKeySet(Set<VarKey> set){
		ICdtVariable vars[] = new ICdtVariable[set.size()];
		int i = 0;
		for(VarKey key : set) {
			vars[i++] = key.getVariable();
		}
		return vars;
	}
	
	@SuppressWarnings("unchecked")
	static VariableChangeEvent createVariableChangeEvent(ICdtVariable[] newVars, ICdtVariable[] oldVars){
		ICdtVariable[] addedVars = null, removedVars = null, changedVars = null;
		
		if(oldVars == null || oldVars.length == 0){
			if(newVars != null && newVars.length != 0)
				addedVars = newVars.clone() ;
		} else if(newVars == null || newVars.length == 0){
			removedVars = oldVars.clone();
		} else {
			HashSet<VarKey> newSet = new HashSet<VarKey>(newVars.length);
			HashSet<VarKey> oldSet = new HashSet<VarKey>(oldVars.length);
			
			for (ICdtVariable newVar : newVars) {
				newSet.add(new VarKey(newVar, true));
			}
	
			for (ICdtVariable oldVar : oldVars) {
				oldSet.add(new VarKey(oldVar, true));
			}
	
			HashSet<VarKey> newSetCopy = (HashSet<VarKey>)newSet.clone();
	
			newSet.removeAll(oldSet);
			oldSet.removeAll(newSetCopy);
			
			if(newSet.size() != 0){
				addedVars = varsFromKeySet(newSet);
			}
			
			if(oldSet.size() != 0){
				removedVars = varsFromKeySet(oldSet);
			}
			
			newSetCopy.removeAll(newSet);
			
			HashSet<VarKey> modifiedSet = new HashSet<VarKey>(newSetCopy.size());
			for (Object element : newSetCopy) {
				VarKey key = (VarKey)element;
				modifiedSet.add(new VarKey(key.getVariable(), false));
			}
			
			for (ICdtVariable oldVar : oldVars) {
				modifiedSet.remove(new VarKey(oldVar, false));
			}
			
			if(modifiedSet.size() != 0)
				changedVars = varsFromKeySet(modifiedSet); 
		}
		
		if(addedVars != null || removedVars != null || changedVars != null)
			return new VariableChangeEvent(addedVars, removedVars, changedVars);
		return null;
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
	
	public boolean setWorkspaceVariables(StorableCdtVariables vars) throws CoreException{
		StorableCdtVariables old = getStorableMacros(ICoreVariableContextInfo.CONTEXT_WORKSPACE, null);
		ICdtVariable[] oldVars = null;
		if(old != null)
			oldVars = old.getMacros();
		
		ICdtVariable[] newVars = vars.getMacros();
		
		fWorkspaceMacros = new StorableCdtVariables(vars, false);
		
		VariableChangeEvent event = createVariableChangeEvent(newVars, oldVars);
		if(event != null){
//			updateProjectInfo(ICoreVariableContextInfo.CONTEXT_WORKSPACE, null);
			notifyListeners(event);
		}

		storeWorkspaceVariables(true);
		return event != null;
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
		StorableCdtVariables macros = loadNewStileWorkspaceMacros();
		
		//now load PathEntry Variables from preferences
		loadPathEntryVariables(macros);
		
		if(macros.isDirty()){
			try {
				storeWorkspaceMacros(macros, true);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return macros;
	}
	
	protected void loadPathEntryVariables(StorableCdtVariables vars){
		org.eclipse.core.runtime.Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
		String[] names = prefs.propertyNames();
		for (String name : names) {
			if (name.startsWith(OLD_VARIABLE_PREFIX)) {
				String value = prefs.getString(name);
				prefs.setToDefault(name);
				if(value.length() != 0){
					name = name.substring(OLD_VARIABLE_PREFIX.length());
					vars.createMacro(name, ICdtVariable.VALUE_PATH_ANY, value);
				}
			}
		}
	}
	
	protected StorableCdtVariables loadNewStileWorkspaceMacros(){
		InputStream stream = loadInputStream(getWorkspaceNode(),PREFNAME_WORKSPACE);
		if(stream == null)
			return new StorableCdtVariables(false);
		return loadMacrosFromStream(stream, false);
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
	
	private StorableCdtVariables loadMacrosFromStream(InputStream stream, boolean readOnly){
		try{
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource inputSource = new InputSource(stream);
			Document document = parser.parse(inputSource);
			Element rootElement = document.getDocumentElement();
			
			if(!StorableCdtVariables.MACROS_ELEMENT_NAME.equals(rootElement.getNodeName()))
				return null;
			
			return new StorableCdtVariables(new XmlStorageElement(rootElement), readOnly);
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
	
//	protected void updateProjectInfo(int type, Object context){
//	}
	
	public void addListener(ICdtVariableChangeListener listener){
		fListeners.add(listener);
	}
	
	public void removeListener(ICdtVariableChangeListener listener){
		fListeners.remove(listener);
	}
	
	private void notifyListeners(VariableChangeEvent event){
		ICdtVariableChangeListener[] listeners = fListeners.toArray(new ICdtVariableChangeListener[fListeners.size()]);
		for (ICdtVariableChangeListener listener : listeners) {
			listener.variablesChanged(event);
		}
	}
	
	public boolean containsVariable(int context, Object data, ICdtVariable var){
		ICdtVariable varContained = getMacro(var.getName(), context, data);
		if(varContained == null)
			return false;
		
		if(new VarKey(varContained, false).equals(new VarKey(var, false)))
			return true;
		
		return false;
	}
	
}
