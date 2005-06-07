/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.index.ctagsindexer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.eclipse.cdt.internal.core.index.FunctionEntry;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.index.INamedEntry;
import org.eclipse.cdt.internal.core.index.NamedEntry;
import org.eclipse.cdt.internal.core.index.TypeEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
import org.eclipse.cdt.internal.core.search.matching.CSearchPattern;


class CTagEntry{
	private final CTagsConsoleParser parser;
    String elementName;
	String fileName;
	int lineNumber;

	/* Miscellaneous extension fields */
	HashMap tagExtensionField;

	String line;

	public CTagEntry(CTagsConsoleParser parser, String line) {
		this.line = line;
        this.parser = parser; 
		elementName = ""; //$NON-NLS-1$
		fileName =""; //$NON-NLS-1$
		lineNumber = 0;
		tagExtensionField = new HashMap();
		parse();
	}
	
	void parse () {
		String delim = CTagsConsoleParser.TAB_SEPARATOR;
		StringTokenizer st = new StringTokenizer(line, delim);
		for (int state = 0; st.hasMoreTokens(); state++) {
			String token = st.nextToken();
			
			switch (state) {
				case 0: // ELEMENT_NAME:
					elementName = token;
				break;

				case 1: // FILE_NAME:
					fileName = token;
				break;

				case 2: // LINE NUMBER;
					try {
						String sub = token.trim();
						int i = sub.indexOf(';');
						String num = sub.substring(0, i);
						if (Character.isDigit(num.charAt(0))) {
							lineNumber = Integer.parseInt(num);
						}
					} catch (NumberFormatException e) {
					} catch (IndexOutOfBoundsException e) {
					}
				break;

				default: // EXTENSION_FIELDS:
					int i = token.indexOf(':');
					if (i != -1) {
						String key = token.substring(0, i);
						String value = token.substring(i + 1);
						tagExtensionField.put(key, value);
					}
				break;
			}
		}
	}
	
	   /**
     * @param tempTag
     * @return
     */
    public char[][] getQualifiedName() {
        char[][] fullName = null;
        String name = null;
        String[] types = {CTagsConsoleParser.NAMESPACE, CTagsConsoleParser.CLASS, CTagsConsoleParser.STRUCT, CTagsConsoleParser.UNION, CTagsConsoleParser.FUNCTION, CTagsConsoleParser.ENUM};
       
        for (int i=0; i<types.length; i++){
            //look for name
            name = (String) tagExtensionField.get(types[i]); 
            if (name != null)
                break;
        }
        
        if (name != null){
	        StringTokenizer st = new StringTokenizer(name, CTagsConsoleParser.COLONCOLON);
			fullName = new char[st.countTokens() + 1][];
			int i=0;
			while (st.hasMoreTokens()){
			    fullName[i] = st.nextToken().toCharArray();
			    i++;
			}
			fullName[i] = elementName.toCharArray();
        } else {
            fullName = new char[1][];
            fullName[0] = elementName.toCharArray();
        }
        
        return fullName;
    }
	
	public void addTagToIndexOutput(int fileNum, IIndexerOutput output){
		
		String kind = (String) tagExtensionField.get(CTagsConsoleParser.KIND);
	    
	    if (kind == null)
	    	  return;
		
		char[][] fullName = getQualifiedName();
	
		if (kind.equals(CTagsConsoleParser.CLASS)){
			TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_CLASS,IIndex.DECLARATION, fullName, getModifiers(), fileNum);
			typeEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
			typeEntry.setBaseTypes(getInherits());
			typeEntry.serialize(output);
    	} else if (kind.equals(CTagsConsoleParser.MACRO)){
			NamedEntry namedEntry = new NamedEntry(IIndex.MACRO, IIndex.DECLARATION,fullName,getModifiers(),fileNum);
			namedEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
			namedEntry.serialize(output);
    	} else if (kind.equals(CTagsConsoleParser.ENUMERATOR)){
			NamedEntry namedEntry = new NamedEntry(IIndex.ENUMTOR, IIndex.DECLARATION,fullName,getModifiers(),fileNum);
			namedEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
			namedEntry.serialize(output);
    	} else if (kind.equals(CTagsConsoleParser.FUNCTION)){
			//Both methods and functions are reported back as functions - methods can be distinguished
			//from functions by the presence of a class field in the type entry
			String isMethod = (String) tagExtensionField.get(CTagsConsoleParser.CLASS);
			if (isMethod != null){
				//method
				FunctionEntry funEntry = new FunctionEntry(IIndex.METHOD, IIndex.DEFINITION,fullName,getModifiers(),fileNum);
				funEntry.setSignature(getFunctionSignature());
				funEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
				funEntry.serialize(output);
			} else {
				//function
				FunctionEntry funEntry = new FunctionEntry(IIndex.FUNCTION, IIndex.DEFINITION,fullName,getModifiers(), fileNum);
				funEntry.setSignature(getFunctionSignature());
				funEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
				funEntry.serialize(output);
			}
    	} else if (kind.equals(CTagsConsoleParser.ENUM)){
			TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_ENUM ,IIndex.DECLARATION, fullName, getModifiers(), fileNum);
			typeEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
			typeEntry.serialize(output);
    	} else if (kind.equals(CTagsConsoleParser.MEMBER)){
			NamedEntry namedEntry = new NamedEntry(IIndex.FIELD, IIndex.DEFINITION,fullName,getModifiers(),fileNum);
			namedEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
			namedEntry.serialize(output);
    	} else if (kind.equals(CTagsConsoleParser.NAMESPACE)){
			NamedEntry namedEntry = new NamedEntry(IIndex.NAMESPACE, IIndex.DEFINITION,fullName,getModifiers(),fileNum);
			namedEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
			namedEntry.serialize(output);
    	} else if (kind.equals(CTagsConsoleParser.PROTOTYPE)){
			//Both methods and functions are reported back as functions - methods can be distinguished
			//from functions by the presence of a class field in the type entry
			String isMethod = (String) tagExtensionField.get(CTagsConsoleParser.CLASS);
			if (isMethod != null){
				//method
				FunctionEntry funEntry = new FunctionEntry(IIndex.METHOD, IIndex.DECLARATION,fullName,getModifiers(), fileNum);
				funEntry.setSignature(getFunctionSignature());
				funEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
				funEntry.serialize(output);
			} else {
				//function
				FunctionEntry funEntry = new FunctionEntry(IIndex.FUNCTION, IIndex.DECLARATION,fullName,getModifiers(),fileNum);
				funEntry.setSignature(getFunctionSignature());
				funEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
				funEntry.serialize(output);
			}
    	} else if (kind.equals(CTagsConsoleParser.STRUCT)){
			TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_STRUCT,IIndex.DECLARATION, fullName, getModifiers(), fileNum);
			typeEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
			typeEntry.setBaseTypes(getInherits());
			typeEntry.serialize(output);
    	} else if (kind.equals(CTagsConsoleParser.TYPEDEF)){
			TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_TYPEDEF,IIndex.DECLARATION, fullName, getModifiers(), fileNum);
			typeEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
			typeEntry.serialize(output);
    	} else if (kind.equals(CTagsConsoleParser.UNION)){
			TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_UNION,IIndex.DECLARATION, fullName, getModifiers(), fileNum);
			typeEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
			typeEntry.serialize(output);
    	} else if (kind.equals(CTagsConsoleParser.VARIABLE)){
			TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_VAR,IIndex.DECLARATION, fullName, getModifiers(), fileNum);
			typeEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
			typeEntry.serialize(output);
    	} else if (kind.equals(CTagsConsoleParser.EXTERNALVAR)){
			//Have to specifically set external bit flag in modifier;
			int modifiers = getModifiers();
			modifiers |= 1 << 6;
			TypeEntry typeEntry = new TypeEntry(IIndex.TYPE_VAR,IIndex.DECLARATION, fullName, modifiers, fileNum);
			typeEntry.setNameOffset(lineNumber, 1, IIndex.LINE);
			typeEntry.serialize(output);
    	}
	}

	private char[][] getFunctionSignature() {
		String signature =  (String) tagExtensionField.get(CTagsConsoleParser.SIGNATURE);
		
		LinkedList list = CSearchPattern.scanForParameters(signature);
		char [][] parameters = new char [0][];
		parameters = (char[][])list.toArray( parameters );
		
		return parameters;
	}

	private int getModifiers() {
		
		int modifier=0;  
		
		//Check access modifier
		String access = (String) tagExtensionField.get(CTagsConsoleParser.ACCESS);
		
		if (access != null){
			for (int i=0; i<ICIndexStorageConstants.allSpecifiers.length; i++){
				if (access.equals(ICIndexStorageConstants.allSpecifiers[i])){
					int tempNum = 1 << i;
					modifier |= tempNum;
					break;
				}
			}
		}
		
		//Check implementation modifier
		String implementation=(String) tagExtensionField.get(CTagsConsoleParser.IMPLEMENTATION);
		
		if (implementation != null){
			for (int i=0; i<ICIndexStorageConstants.allSpecifiers.length; i++){
				if (implementation.equals(ICIndexStorageConstants.allSpecifiers[i])){
					int tempNum = 1 << i;
					modifier |= tempNum;
				}
			}
		}
		
		return modifier;
	}
	
	private INamedEntry[] getInherits() {
		
		//Check inherits modifier
		String access = (String) tagExtensionField.get(CTagsConsoleParser.INHERITS);
		
		if (access != null){
			StringTokenizer tokenizer = new StringTokenizer(access, ","); //$NON-NLS-1$
			LinkedList list = new LinkedList();
			while (tokenizer.hasMoreTokens()){
				list.add(tokenizer.nextToken());
			}
		
			String[] inherits = new String[0];
			inherits = (String []) list.toArray(inherits);
			INamedEntry[] inherits2 = new INamedEntry[inherits.length];
			for (int i=0; i<inherits.length; i++){
				NamedEntry tempEntry = new NamedEntry(IIndex.FIELD, IIndex.REFERENCE, inherits[i], 1, 1);
				inherits2[i] = tempEntry;
			}
			return inherits2;
		}
		
		return null; 
	}
}