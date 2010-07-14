/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.tcmodification.extension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ObjectTypeBasedStorage;
import org.eclipse.core.runtime.IConfigurationElement;

public class MatchObjectElement {
	public static String ELEMENT_NAME = "matchObject"; //$NON-NLS-1$
	private static String ATTR_OBJECT_TYPE = "objectType"; //$NON-NLS-1$
	private static String ATTR_OBJECT_IDS = "objectIds"; //$NON-NLS-1$
	private static String ELEMENT_PATTERN = "pattern"; //$NON-NLS-1$
	private static String ATTR_PATTERN_TYPE_SEARCH_SCOPE = "searchScope"; //$NON-NLS-1$
	private static String ATTR_PATTERN_TYPE_ID_TYPE = "objectIdsType"; //$NON-NLS-1$

	private static String DELIMITER = ";"; //$NON-NLS-1$
	
	private int fObjectType;
	private PatternElement[] fPatterns;
	private int fHash;
	
	public static class TypeToStringAssociation {
		private int fType;
		private String fString;
		private static ObjectTypeBasedStorage fTypeAssociationStorage = new ObjectTypeBasedStorage();
		private static Map fStringAssociationStorage = new HashMap();
		
		public static TypeToStringAssociation TOOL = new TypeToStringAssociation(IRealBuildObjectAssociation.OBJECT_TOOL, "tool"); //$NON-NLS-1$
		public static TypeToStringAssociation TOOLCHAIN = new TypeToStringAssociation(IRealBuildObjectAssociation.OBJECT_TOOLCHAIN, "toolChain"); //$NON-NLS-1$
		public static TypeToStringAssociation CONFIGURATION = new TypeToStringAssociation(IRealBuildObjectAssociation.OBJECT_CONFIGURATION, "configuration"); //$NON-NLS-1$
		public static TypeToStringAssociation BUILDER = new TypeToStringAssociation(IRealBuildObjectAssociation.OBJECT_BUILDER, "builder"); //$NON-NLS-1$
		
		private TypeToStringAssociation(int type, String string){
			fType = type;
			fString = string;
			fTypeAssociationStorage.set(type, this);
			fStringAssociationStorage.put(fString, this);
		}
		
		public int getType(){
			return fType;
		}
		
		public String getString(){
			return fString;
		}
		
		public static TypeToStringAssociation getAssociation(String str){
			return (TypeToStringAssociation)fStringAssociationStorage.get(str);
		}
		
		public static TypeToStringAssociation getAssociation(int type){
			return (TypeToStringAssociation)fTypeAssociationStorage.get(type);
		}
	}

	private static class PatternTypeKey {
		private int fType;
		
		PatternTypeKey(PatternElement el){
			fType = el.getCompleteOredTypeValue();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			
			if(!(obj instanceof PatternTypeKey))
				return false;
			
			return fType == ((PatternTypeKey)obj).fType;
		}

		@Override
		public int hashCode() {
			return fType;
		}
	}
	
	public class PatternElement {
		private HashSet fIds;
		private int fHash;
		private int fType;
		
		private static final int SEARCH_TYPE_MASK = 0xff;
		private static final int SEARCH_TYPE_OFFSET = 0;
		public static final int TYPE_SEARCH_EXTENSION_OBJECT = 1 << SEARCH_TYPE_OFFSET;
		public static final int TYPE_SEARCH_ALL_EXTENSION_SUPERCLASSES = 1 << 1 + SEARCH_TYPE_OFFSET;
		private static final int DEFAULT_PATTERN_SEARCH_TYPE = TYPE_SEARCH_EXTENSION_OBJECT;

		private static final String EXTENSION_OBJECT = "EXTENSION_OBJECT"; //$NON-NLS-1$
		private static final String ALL_EXTENSION_SUPERCLASSES = "ALL_EXTENSION_SUPERCLASSES"; //$NON-NLS-1$

		private static final int ID_TYPE_MASK = 0xff00;
		private static final int ID_TYPE_OFFSET = 8;
		public static final int TYPE_ID_EXACT_MATCH = 1 << ID_TYPE_OFFSET;
		public static final int TYPE_ID_REGEXP = 1 << 1 + ID_TYPE_OFFSET;
		private static final int DEFAULT_PATTERN_ID_TYPE = TYPE_ID_EXACT_MATCH;

		private static final String EXACT_MATCH = "EXACT_MATCH"; //$NON-NLS-1$
		private static final String REGEGP = "REGEXP"; //$NON-NLS-1$
		
		PatternElement(IConfigurationElement el, int defaultSearchType, int defaultIdType){
			String tmp = el.getAttribute(ATTR_OBJECT_IDS);
			fIds = new HashSet(Arrays.asList(CDataUtil.stringToArray(tmp, DELIMITER)));
			
			int type = 0;
			tmp = el.getAttribute(ATTR_PATTERN_TYPE_SEARCH_SCOPE);
			if(tmp == null) {
				type = defaultSearchType;
			} else {
				if(EXTENSION_OBJECT.equals(tmp)){
					type = TYPE_SEARCH_EXTENSION_OBJECT;
				} else if (ALL_EXTENSION_SUPERCLASSES.equals(tmp)){
					type = TYPE_SEARCH_ALL_EXTENSION_SUPERCLASSES;
				} else {
					throw new IllegalArgumentException();
				}
			}

			tmp = el.getAttribute(ATTR_PATTERN_TYPE_ID_TYPE);
			if(tmp == null) {
				type |= defaultIdType;
			} else {
				if(EXACT_MATCH.equals(tmp)){
					type |= TYPE_ID_EXACT_MATCH;
				} else if (REGEGP.equals(tmp)){
					type |= TYPE_ID_REGEXP;
				} else {
					throw new IllegalArgumentException();
				}
			}

			fType = type;
		}

		private PatternElement(HashSet ids, int type){
			fIds = ids;
			fType = type;
		}

		public String[] getIds(){
			return (String[])fIds.toArray(new String[fIds.size()]);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			
			if(!(obj instanceof PatternElement))
				return false;
			
			PatternElement other = (PatternElement)obj;
			if(other.fIds.size() != fIds.size())
				return false;
			
			return other.fIds.containsAll(fIds);
		}

		@Override
		public int hashCode() {
			if(fHash == 0){
				fHash = fIds.hashCode();
			}
			return fHash;
		}
		
		public PatternElement merge(PatternElement el) throws IllegalArgumentException {
			if(el.fType != fType)
				throw new IllegalArgumentException();
			
			HashSet set = new HashSet();
			set.addAll(fIds);
			set.addAll(el.fIds);
			return new PatternElement(set, fType);
		}
		
		public int getSearchType(){
			return fType & SEARCH_TYPE_MASK;
		}
		
		public int getIdType(){
			return fType & ID_TYPE_MASK;
		}

		public int getCompleteOredTypeValue(){
			return fType;
		}
	}

	public MatchObjectElement(IConfigurationElement element) throws IllegalArgumentException {
		TypeToStringAssociation assoc = TypeToStringAssociation.getAssociation(element.getAttribute(ATTR_OBJECT_TYPE));
		if(assoc == null)
			throw new IllegalArgumentException();
		
		fObjectType = assoc.getType();
		
		Map patternMap = new HashMap();
		int defaultSearchType = PatternElement.DEFAULT_PATTERN_SEARCH_TYPE;
		int defaultIdType = PatternElement.DEFAULT_PATTERN_ID_TYPE;
		
		if(element.getAttribute(ATTR_OBJECT_IDS) != null){
			PatternElement el = new PatternElement(element, defaultSearchType, defaultIdType);
			patternMap.put(new PatternTypeKey(el), el);
			defaultSearchType = el.getSearchType();
			defaultIdType = el.getIdType();
		}
		
		IConfigurationElement patternsChildren[] = element.getChildren(ELEMENT_PATTERN);
		if(patternsChildren.length != 0){
			for(int i = 0; i < patternsChildren.length; i++){
				PatternElement el = new PatternElement(patternsChildren[i], defaultSearchType, defaultIdType);
				PatternTypeKey key = new PatternTypeKey(el);
				PatternElement cur = (PatternElement)patternMap.get(key);
				if(cur != null){
					patternMap.put(key, cur.merge(el));
				} else {
					patternMap.put(key, el);
				}
			}
		} 
		
		if(patternMap.size() == 0) {
			throw new IllegalArgumentException();
		}
		
		fPatterns = (PatternElement[])patternMap.values().toArray(new PatternElement[patternMap.size()]);
	}
	
	public int getObjectType(){
		return fObjectType;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		
		if(!(obj instanceof MatchObjectElement))
			return false;
		
		MatchObjectElement other = (MatchObjectElement)obj;
		if(fObjectType != other.fObjectType)
			return false;
		
		return Arrays.equals(other.fPatterns, fPatterns);
	}

	@Override
	public int hashCode() {
		if(fHash == 0){
			int hash = fObjectType;
			for(int i = 0; i < fPatterns.length; i++){
				hash += fPatterns[i].hashCode();
			}
			fHash = hash;
		}
		return fHash;
	}
	
	public PatternElement[] getPatterns(){
		return fPatterns.clone();
	}
}
