/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;

/**
 * This substitutor resolves all macro references 
 * 
 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor
 * @since 3.0
 */
public class DefaultMacroSubstitutor implements IMacroSubstitutor {
	private static final Object UNDEFINED_MACRO_VALUE = new Object();
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private IMacroContextInfo fContextInfo;
	private String fInexistentMacroValue;
	private String fListDelimiter;
	private String fIncorrectlyReferencedMacroValue;
	private Map fDelimiterMap;
	
	protected class ResolvedMacro extends BuildMacro{
		private boolean fIsDefined;
		private boolean fIsList;
		
		public ResolvedMacro(String name){
			super(name, VALUE_TEXT, (String)null);
			fIsDefined = false;
		}

		public ResolvedMacro(String name, String value, boolean isDefined){
			super(name, VALUE_TEXT, value);
			fIsDefined = isDefined;
			fIsList = false;
		}

		public ResolvedMacro(String name, String value){
			super(name, VALUE_TEXT, value);
			fIsDefined = true;
			fIsList = false;
		}

		public ResolvedMacro(String name, String value[]){
			super(name, VALUE_TEXT_LIST, value);
			fIsDefined = true;
			fIsList = true;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getStringValue()
		 */
		public String getStringValue() throws BuildMacroException {
//			if(!fIsDefined)
//				throw new BuildMacroException(BuildMacroException.TYPE_MACROS_UNDEFINED,fName); 
			if(fIsList && fStringValue == null)
				fStringValue = stringListToString(fStringListValue);
			return fStringValue;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getStringListValue()
		 */
		public String[] getStringListValue() throws BuildMacroException {
//			if(!fIsDefined)
//				throw new BuildMacroException(BuildMacroException.TYPE_MACROS_UNDEFINED,fName);
			if(!fIsList && fStringListValue == null)
				fStringListValue = new String[]{fStringValue};
			return fStringListValue;
		}
		
		protected String getDelimiter(){
			if(fDelimiterMap != null){
				Object delimiter = fDelimiterMap.get(fName);
				if(delimiter instanceof String)
					return (String)delimiter;
			}
			return fListDelimiter;
		}
	
		protected String stringListToString(String values[]) throws BuildMacroException {
			String result = null;
			String delimiter;
			if(values == null)
				result = null;
			else if(values.length == 0)
				result = EMPTY_STRING;
			else if(values.length == 1)
				result = values[0];
			else if((delimiter = getDelimiter()) != null){
				StringBuffer buffer = new StringBuffer(); 
				for(int i = 0; i < values.length; i++){
					buffer.append(values[i]);
					if(i < values.length-1)
						buffer.append(delimiter);
				}
				result = buffer.toString();
			} else {
				throw new BuildMacroException(IBuildMacroStatus.TYPE_MACRO_NOT_STRING, 
						null, 
						null, 
						fName,
						fContextInfo != null ? fContextInfo.getContextType() : 0,
						fContextInfo != null ? fContextInfo.getContextData() : null);
			}
			
			return result;

		}
		
		public boolean isList(){
			return fIsList;
		}

		public boolean isDefined(){
			return fIsDefined;
		}
	}
	
	/*
	 * describes the macro and the context where the macro was found
	 */
	protected class MacroDescriptor {
		private String fName;
		private IMacroContextInfo fInfo;
		private IBuildMacro fMacro;
		private boolean fInitialized;
		private int fSupplierNum;
		private int fEnvSupplierNum;
		
		public MacroDescriptor(String name, IMacroContextInfo info){
			fName = name;
			fInfo = info;
		}

		public MacroDescriptor(String name, IMacroContextInfo info, int supplierNum){
			fName = name;
			fInfo = info;
			fSupplierNum = supplierNum;
		}

		public MacroDescriptor(IBuildMacro macro, IMacroContextInfo info, int supplierNum){
			fName = macro.getName();
			fInfo = info;
			fMacro = macro;
			fSupplierNum = supplierNum;
			fInitialized = true;
		}

		public MacroDescriptor getNext(){
			return new MacroDescriptor(fName,getInfo(),getSupplierNum()+1);
		}
		
		public int getSupplierNum(){
			init();
			return fSupplierNum;
		}
		
		private void init(){
			if(fInitialized)
				return;
			fInitialized = true;
			for(; fInfo != null; fInfo = fInfo.getNext()){
				IBuildMacroSupplier suppliers[] = fInfo.getSuppliers();
				if(suppliers != null){
					for(; fSupplierNum < suppliers.length; fSupplierNum++){
						if((fMacro = suppliers[fSupplierNum].getMacro(fName,fInfo.getContextType(),fInfo.getContextData())) != null){
							return;
						}
					}
				}
				fSupplierNum = 0;
			}
		}

		protected IMacroContextInfo getInfo(){
			init();
			return fInfo;
		}
		
		public IBuildMacro getMacro(){
			init();
			return fMacro;
		}

	}

	private Map fResolvedMacros = new HashMap();
	private HashSet fMacrosUnderResolution = new HashSet();
	private Stack fMacroDescriptors = new Stack();

	public DefaultMacroSubstitutor(int contextType, Object contextData, String inexistentMacroValue, String listDelimiter, Map delimiterMap, String incorrectlyReferencedMacroValue){
		this(new DefaultMacroContextInfo(contextType,contextData),inexistentMacroValue,listDelimiter, delimiterMap, incorrectlyReferencedMacroValue);
	}

	public DefaultMacroSubstitutor(int contextType, Object contextData, String inexistentMacroValue, String listDelimiter){
		this(new DefaultMacroContextInfo(contextType,contextData),inexistentMacroValue,listDelimiter);
	}

	public DefaultMacroSubstitutor(IMacroContextInfo contextInfo, String inexistentMacroValue, String listDelimiter){
		this(contextInfo, inexistentMacroValue, listDelimiter, null ,inexistentMacroValue);
	}

	public DefaultMacroSubstitutor(IMacroContextInfo contextInfo, String inexistentMacroValue, String listDelimiter, Map delimiterMap, String incorrectlyReferencedMacroValue){
		fContextInfo = contextInfo;
		fInexistentMacroValue = inexistentMacroValue;
		fListDelimiter = listDelimiter;
		fDelimiterMap = delimiterMap;
		fIncorrectlyReferencedMacroValue = incorrectlyReferencedMacroValue;
	}

	protected String resolveToString(MacroDescriptor des) throws BuildMacroException {
		String result = null;
		ResolvedMacro value = getResolvedMacro(des);
		result = value.getStringValue();
		
		return result;
	}

	protected String[] resolveToStringList(MacroDescriptor des) throws BuildMacroException {
		String result[] = null;
		ResolvedMacro value = getResolvedMacro(des);
		result = value.getStringListValue();

		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor#resolveToString(java.lang.String)
	 */
	public String resolveToString(String macroName) throws BuildMacroException {
		return resolveToString(new MacroDescriptor(macroName,fContextInfo));
	}
	
	public void setMacroContextInfo(IMacroContextInfo info)
			throws BuildMacroException{
		if(checkEqual(fContextInfo,info))
			return;

		reset();
		fContextInfo = info;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor#setMacroContextInfo(int, java.lang.Object)
	 */
	public void setMacroContextInfo(int contextType, Object contextData) throws BuildMacroException{
		setMacroContextInfo(getMacroContextInfo(contextType,contextData));
	}
	
	protected IMacroContextInfo getMacroContextInfo(int contextType, Object contextData){
		return ((BuildMacroProvider)ManagedBuildManager.getBuildMacroProvider()).getMacroContextInfo(contextType,contextData);
	}

	protected ResolvedMacro getResolvedMacro(MacroDescriptor des)
			throws BuildMacroException {
		ResolvedMacro value = checkResolvingMacro(des);
		
		if(value == null)
		{
			try{
				value = resolveMacro(des);
			}finally{
				if(value != null)
					addResolvedMacro(des,value);
				else {
					value = new ResolvedMacro(des.fName,fInexistentMacroValue,false);
					addResolvedMacro(des,value);
				}
			}
		}
		
		return value;
	}
	
	protected ResolvedMacro resolveMacro(MacroDescriptor des) throws BuildMacroException{
		return des.fMacro != null ? resolveMacro(des.fMacro) : resolveMacro(des.fName);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor#resolveToStringList(java.lang.String)
	 */
	public String[] resolveToStringList(String macroName)
			throws BuildMacroException {
		return resolveToStringList(new MacroDescriptor(macroName,fContextInfo));
	}
	
	protected ResolvedMacro resolveMacro(String macroName) throws BuildMacroException{
		return resolveMacro(BuildMacroProvider.getMacro(macroName,fContextInfo,true));
	}
	
	protected ResolvedMacro resolveParentMacro(MacroDescriptor macroDes) throws BuildMacroException{
		MacroDescriptor des = macroDes.getNext();

		ResolvedMacro macro = null;
		
		if(des != null){
			try{
				fMacroDescriptors.push(des);
				macro = resolveMacro(des.getMacro());
			} finally {
				fMacroDescriptors.pop();
			}
		}
		return macro;
	}
	
	protected ResolvedMacro resolveMacro(IBuildMacro macro) throws BuildMacroException{
		if(macro == null)
			return null;
		
		String macroName = macro.getName();
		IMacroSubstitutor substitutor = this;

		ResolvedMacro resolvedMacro = null;

		if(MacroResolver.isStringListMacro(macro.getMacroValueType())){
			String result[] = null;
			String unresolvedValues[] = macro.getStringListValue();
			if(unresolvedValues == null || unresolvedValues.length == 0)
				resolvedMacro = new ResolvedMacro(macroName,EMPTY_STRING);
			else{
				String resolvedValues[][] = new String[unresolvedValues.length][];
				
				for(int i = 0; i < unresolvedValues.length; i++){
					try{
						resolvedValues[i] = MacroResolver.resolveToStringList(unresolvedValues[i],substitutor);
					} catch (BuildMacroException e) {
						IBuildMacroStatus statuses[] = e.getMacroStatuses();
						if(statuses != null && statuses.length == 1){
							IBuildMacroStatus status = statuses[0];
							if(status.getMacroName() == null){
								e = new BuildMacroException(status.getCode(),
										macro.getName(),
										status.getExpression(),
										status.getReferencedMacroName(),
										fContextInfo != null ? fContextInfo.getContextType() : 0,
										fContextInfo != null ? fContextInfo.getContextData() : null);
							}
						}
						throw e;
					}
				}
				
				if(resolvedValues.length == 1)
					result = resolvedValues[0];
				else{
					List list = new ArrayList();
					for(int i = 0; i < resolvedValues.length; i++)
						list.addAll(Arrays.asList(resolvedValues[i]));
					
					result = (String[])list.toArray(new String[list.size()]);
				}
				resolvedMacro = new ResolvedMacro(macroName,result);
			}
		} else {
			try{
				resolvedMacro = new ResolvedMacro(macroName,MacroResolver.resolveToString(macro.getStringValue(),substitutor));
			} catch (BuildMacroException e) {
				IBuildMacroStatus statuses[] = e.getMacroStatuses();
				if(statuses != null && statuses.length == 1){
					IBuildMacroStatus status = statuses[0];
					if(status.getMacroName() == null){
						e = new BuildMacroException(status.getCode(),
								macro.getName(),
								status.getExpression(),
								status.getReferencedMacroName(),
								fContextInfo != null ? fContextInfo.getContextType() : 0,
								fContextInfo != null ? fContextInfo.getContextData() : null);
					}
				}
				throw e;
			}
		}
		return resolvedMacro;
	}
	
	private ResolvedMacro checkResolvingMacro(MacroDescriptor des)
				throws BuildMacroException{
		String name = des.fName;
		ResolvedMacro value = (ResolvedMacro)fResolvedMacros.get(name);
		if(value == null){
			if(fMacrosUnderResolution.add(name))
				fMacroDescriptors.push(des);
			else {
				// the macro of the specified name is referenced from the other macros that
				// are referenced by the given macro
				// e.g. ${macro1}="...${macro2}...", ${macro2}="...${macro1}..."
				// in this case when resolving the ${macro1} value, the ${macro2} resolution will be requested,
				// that in turn will call the ${macro1} resolution again.
				// In this case if the fIncorrectlyReferencedMacroValue is null, the BuildMacroException will be thrown
				// with the IBuildMacroStatus.TYPE_MACRO_REFERENCE_INCORRECT status
				// The special case is when the , macro references itself, e.g.
				// ${macro1} = "...${macro1}..."
				// In the above example the ${macro1} reference will be expanded to the value of the ${macro1} macro of the
				// parent context or to an empty string if there is no such macro defined in the parent contexts
				MacroDescriptor last = (MacroDescriptor)fMacroDescriptors.lastElement();
				if(last != null && last.fName.equals(name)){
					value = resolveParentMacro(last);
					if(value == null)
						value = new ResolvedMacro(name,EMPTY_STRING,false);
				}else if(fIncorrectlyReferencedMacroValue != null)
					value = new ResolvedMacro(name,fIncorrectlyReferencedMacroValue,false);
				else{
					throw new BuildMacroException(IBuildMacroStatus.TYPE_MACRO_REFERENCE_INCORRECT,
							(String)null,
							null,
							name,
							fContextInfo != null ? fContextInfo.getContextType() : 0,
							fContextInfo != null ? fContextInfo.getContextData() : null);
				}
			}
		}
			
		return value;
	}
	
	protected void addResolvedMacro(MacroDescriptor des, ResolvedMacro value){
		String name = des.fName;
		fMacrosUnderResolution.remove(name);
		fResolvedMacros.put(name,value);
		fMacroDescriptors.pop();
	}
	
	protected ResolvedMacro removeResolvedMacro(String name){
		return (ResolvedMacro)fResolvedMacros.remove(name);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor#getMacroContextInfo()
	 */
	public IMacroContextInfo getMacroContextInfo(){
		return fContextInfo;
	}
	
	public void reset() throws BuildMacroException{
		if(fMacrosUnderResolution.size() != 0)
			throw new BuildMacroException(IBuildMacroStatus.TYPE_ERROR,(String)null,null,null,0,null);
		
		fResolvedMacros.clear();
	}

	public Map getDelimiterMap() {
		return fDelimiterMap;
	}

	public void setDelimiterMap(Map delimiterMap) throws BuildMacroException {
		if(checkEqual(fDelimiterMap,delimiterMap))
			return;
		reset();
		fDelimiterMap = delimiterMap;
	}

	public String getIncorrectlyReferencedMacroValue() {
		return fIncorrectlyReferencedMacroValue;
	}

	protected boolean checkEqual(Object o1, Object o2){
		if(o1 != null){
			if(o1.equals(o2))
				return true;
		} else if (o2 == null)
			return true;
		return false;
	}

	public void setIncorrectlyReferencedMacroValue(
			String incorrectlyReferencedMacroValue) throws BuildMacroException {
		if(checkEqual(fIncorrectlyReferencedMacroValue,incorrectlyReferencedMacroValue))
			return;
		reset();
		fIncorrectlyReferencedMacroValue = incorrectlyReferencedMacroValue;
	}

	public String getInexistentMacroValue() {
		return fInexistentMacroValue;
	}

	public void setInexistentMacroValue(String inexistentMacroValue) throws BuildMacroException {
		if(checkEqual(fInexistentMacroValue,inexistentMacroValue))
			return;
		reset();
		fInexistentMacroValue = inexistentMacroValue;
	}

	public String getListDelimiter() {
		return fListDelimiter;
	}

	public void setListDelimiter(String listDelimiter) throws BuildMacroException {
		if(checkEqual(fListDelimiter,listDelimiter))
			return;
		reset();
		fListDelimiter = listDelimiter;
	}

}
