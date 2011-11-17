/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.cdtvariables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.core.cdtvariables.CdtVariable;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableStatus;

public class SupplierBasedCdtVariableSubstitutor implements IVariableSubstitutor {
//	private static final Object UNDEFINED_MACRO_VALUE = new Object();
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private IVariableContextInfo fContextInfo;
	private String fInexistentMacroValue;
	private String fListDelimiter;
	private String fIncorrectlyReferencedMacroValue;
	private Map<?, ?> fDelimiterMap;

	protected class ResolvedMacro extends CdtVariable{
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
		@Override
		public String getStringValue() throws CdtVariableException {
//			if(!fIsDefined)
//				throw new BuildMacroException(BuildMacroException.TYPE_MACROS_UNDEFINED,fName);
			if(fIsList && fStringValue == null)
				fStringValue = stringListToString(fStringListValue);
			return fStringValue;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getStringListValue()
		 */
		@Override
		public String[] getStringListValue() throws CdtVariableException {
//			if(!fIsDefined)
//				throw new BuildMacroException(BuildMacroException.TYPE_MACROS_UNDEFINED,fName);
			if(!fIsList && fStringListValue == null){
				if(fStringValue != null && fStringValue.length() != 0)
					fStringListValue = new String[]{fStringValue};
				else
					fStringListValue = new String[0];
			}
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

		protected String stringListToString(String values[]) throws CdtVariableException {
			String result = null;
			String delimiter;
			if (values != null) {
				if(values.length == 0)
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
					ICdtVariableStatus eStatus = new SupplierBasedCdtVariableStatus(ICdtVariableStatus.TYPE_MACRO_NOT_STRING,
							null,
							null,
							fName,
							fContextInfo);
					throw new CdtVariableException(eStatus);
				}
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
		private IVariableContextInfo fInfo;
		private ICdtVariable fMacro;
		private boolean fInitialized;
		private int fSupplierNum;
//		private int fEnvSupplierNum;

		public MacroDescriptor(String name, IVariableContextInfo info){
			fName = name;
			fInfo = info;
		}

		public MacroDescriptor(String name, IVariableContextInfo info, int supplierNum){
			fName = name;
			fInfo = info;
			fSupplierNum = supplierNum;
		}

		public MacroDescriptor(ICdtVariable macro, IVariableContextInfo info, int supplierNum){
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
				ICdtVariableSupplier suppliers[] = fInfo.getSuppliers();
				if(suppliers != null){
					for(; fSupplierNum < suppliers.length; fSupplierNum++){
						if((fMacro = suppliers[fSupplierNum].getVariable(fName,fInfo)) != null){
							return;
						}
					}
				}
				fSupplierNum = 0;
			}
		}

		protected IVariableContextInfo getInfo(){
			init();
			return fInfo;
		}

		public ICdtVariable getMacro(){
			init();
			return fMacro;
		}

	}

	private Map<String, ResolvedMacro> fResolvedMacros = new HashMap<String, ResolvedMacro>();
	private HashSet<String> fMacrosUnderResolution = new HashSet<String>();
	private Stack<MacroDescriptor> fMacroDescriptors = new Stack<MacroDescriptor>();

	public SupplierBasedCdtVariableSubstitutor(IVariableContextInfo contextInfo, String inexistentMacroValue, String listDelimiter){
		this(contextInfo, inexistentMacroValue, listDelimiter, null ,inexistentMacroValue);
	}

	public SupplierBasedCdtVariableSubstitutor(IVariableContextInfo contextInfo, String inexistentMacroValue, String listDelimiter, Map<?, ?> delimiterMap, String incorrectlyReferencedMacroValue){
		fContextInfo = contextInfo;
		fInexistentMacroValue = inexistentMacroValue;
		fListDelimiter = listDelimiter;
		fDelimiterMap = delimiterMap;
		fIncorrectlyReferencedMacroValue = incorrectlyReferencedMacroValue;
	}

	protected String resolveToString(MacroDescriptor des) throws CdtVariableException {
		String result = null;
		ResolvedMacro value = getResolvedMacro(des);
		result = value.getStringValue();

		return result;
	}

	protected String[] resolveToStringList(MacroDescriptor des) throws CdtVariableException {
		String result[] = null;
		ResolvedMacro value = getResolvedMacro(des);
		result = value.getStringListValue();

		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor#resolveToString(java.lang.String)
	 */
	@Override
	public String resolveToString(String macroName) throws CdtVariableException {
		return resolveToString(new MacroDescriptor(macroName,fContextInfo));
	}

	public void setMacroContextInfo(IVariableContextInfo info)
			throws CdtVariableException{
		if(checkEqual(fContextInfo,info))
			return;

		reset();
		fContextInfo = info;
	}

	protected ResolvedMacro getResolvedMacro(MacroDescriptor des)
			throws CdtVariableException {
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

	protected ResolvedMacro resolveMacro(MacroDescriptor des) throws CdtVariableException{
		return des.fMacro != null ? resolveMacro(des.fMacro) : resolveMacro(des.fName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor#resolveToStringList(java.lang.String)
	 */
	@Override
	public String[] resolveToStringList(String macroName)
			throws CdtVariableException {
		return resolveToStringList(new MacroDescriptor(macroName,fContextInfo));
	}

	protected ResolvedMacro resolveMacro(String macroName) throws CdtVariableException{
		return resolveMacro(SupplierBasedCdtVariableManager.getVariable(macroName,fContextInfo,true));
	}

	protected ResolvedMacro resolveParentMacro(MacroDescriptor macroDes) throws CdtVariableException{
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

	protected ResolvedMacro resolveMacro(ICdtVariable macro) throws CdtVariableException{
		if(macro == null)
			return null;

		String macroName = macro.getName();
		IVariableSubstitutor substitutor = this;

		ResolvedMacro resolvedMacro = null;

		if(CdtVariableResolver.isStringListVariable(macro.getValueType())){
			String result[] = null;
			String unresolvedValues[] = macro.getStringListValue();
			if(unresolvedValues == null || unresolvedValues.length == 0)
				resolvedMacro = new ResolvedMacro(macroName,EMPTY_STRING);
			else{
				String resolvedValues[][] = new String[unresolvedValues.length][];

				for(int i = 0; i < unresolvedValues.length; i++){
					try{
						resolvedValues[i] = CdtVariableResolver.resolveToStringList(unresolvedValues[i],substitutor);
					} catch (CdtVariableException e) {
						ICdtVariableStatus statuses[] = e.getVariableStatuses();
						if(statuses != null && statuses.length == 1){
							ICdtVariableStatus status = statuses[0];
							if(status.getVariableName() == null){
								ICdtVariableStatus eStatus = new SupplierBasedCdtVariableStatus(status.getCode(),
										macro.getName(),
										status.getExpression(),
										status.getReferencedMacroName(),
										fContextInfo);
								e = new CdtVariableException(eStatus);
							}
						}
						throw e;
					}
				}

				if(resolvedValues.length == 1)
					result = resolvedValues[0];
				else{
					List<String> list = new ArrayList<String>();
					for (String[] resolvedValue : resolvedValues)
						list.addAll(Arrays.asList(resolvedValue));

					result = list.toArray(new String[list.size()]);
				}
				resolvedMacro = new ResolvedMacro(macroName,result);
			}
		} else {
			try{
				resolvedMacro = new ResolvedMacro(macroName,CdtVariableResolver.resolveToString(macro.getStringValue(),substitutor));
			} catch (CdtVariableException e) {
				ICdtVariableStatus statuses[] = e.getVariableStatuses();
				if(statuses != null && statuses.length == 1){
					ICdtVariableStatus status = statuses[0];
					if(status.getVariableName() == null){
						ICdtVariableStatus eStatus = new SupplierBasedCdtVariableStatus(status.getCode(),
								macro.getName(),
								status.getExpression(),
								status.getReferencedMacroName(),
								fContextInfo);
						e = new CdtVariableException(eStatus);
					}
				}
				throw e;
			}
		}
		return resolvedMacro;
	}

	private ResolvedMacro checkResolvingMacro(MacroDescriptor des)
				throws CdtVariableException{
		String name = des.fName;
		ResolvedMacro value = fResolvedMacros.get(name);
		if(value == null){
			if(fMacrosUnderResolution.add(name)) {
				fMacroDescriptors.push(des);
			} else {
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
				MacroDescriptor last = fMacroDescriptors.lastElement();
				if(last != null && last.fName.equals(name)) {
					value = resolveParentMacro(last);
					if(value == null)
						value = new ResolvedMacro(name,EMPTY_STRING,false);
				} else if(fIncorrectlyReferencedMacroValue != null) {
					value = new ResolvedMacro(name,fIncorrectlyReferencedMacroValue,false);
				} else{
					ICdtVariableStatus status = new SupplierBasedCdtVariableStatus(ICdtVariableStatus.TYPE_MACRO_REFERENCE_INCORRECT,
							(String)null,
							null,
							name,
							fContextInfo);
					throw new CdtVariableException(status);
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
		return fResolvedMacros.remove(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor#getMacroContextInfo()
	 */
	public IVariableContextInfo getMacroContextInfo(){
		return fContextInfo;
	}

	public void reset() throws CdtVariableException{
		if(fMacrosUnderResolution.size() != 0)
			throw new CdtVariableException(ICdtVariableStatus.TYPE_ERROR,(String)null,null,null);

		fResolvedMacros.clear();
	}

	public Map<?, ?> getDelimiterMap() {
		return fDelimiterMap;
	}

	public void setDelimiterMap(Map<?, ?> delimiterMap) throws CdtVariableException {
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
			String incorrectlyReferencedMacroValue) throws CdtVariableException {
		if(checkEqual(fIncorrectlyReferencedMacroValue,incorrectlyReferencedMacroValue))
			return;
		reset();
		fIncorrectlyReferencedMacroValue = incorrectlyReferencedMacroValue;
	}

	public String getInexistentMacroValue() {
		return fInexistentMacroValue;
	}

	public void setInexistentMacroValue(String inexistentMacroValue) throws CdtVariableException {
		if(checkEqual(fInexistentMacroValue,inexistentMacroValue))
			return;
		reset();
		fInexistentMacroValue = inexistentMacroValue;
	}

	public String getListDelimiter() {
		return fListDelimiter;
	}

	public void setListDelimiter(String listDelimiter) throws CdtVariableException {
		if(checkEqual(fListDelimiter,listDelimiter))
			return;
		reset();
		fListDelimiter = listDelimiter;
	}
}
