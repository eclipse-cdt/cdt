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
import java.util.StringTokenizer;

import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;


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
    		output.addClassDecl(fileNum, fullName, lineNumber, 1, IIndex.LINE);
    	} else if (kind.equals(CTagsConsoleParser.MACRO)){
    		output.addMacroDecl(fileNum, fullName, lineNumber, 1, IIndex.LINE);
    	} else if (kind.equals(CTagsConsoleParser.ENUMERATOR)){
    		output.addEnumtorDecl(fileNum, fullName, lineNumber, 1, IIndex.LINE);
    	} else if (kind.equals(CTagsConsoleParser.FUNCTION)){
    		output.addFunctionDefn(fileNum, fullName, lineNumber, 1, IIndex.LINE);
    	} else if (kind.equals(CTagsConsoleParser.ENUM)){
    		output.addEnumDecl(fileNum, fullName, lineNumber, 1, IIndex.LINE);
    	} else if (kind.equals(CTagsConsoleParser.MEMBER)){
    		output.addFieldDecl(fileNum, fullName, lineNumber, 1, IIndex.LINE);
    	} else if (kind.equals(CTagsConsoleParser.NAMESPACE)){
    		output.addNamespaceDecl(fileNum, fullName, lineNumber, 1, IIndex.LINE);
    	} else if (kind.equals(CTagsConsoleParser.PROTOTYPE)){
    		output.addFunctionDecl(fileNum, fullName, lineNumber, 1, IIndex.LINE);
    	} else if (kind.equals(CTagsConsoleParser.STRUCT)){
    		output.addStructDecl(fileNum, fullName, lineNumber, 1, IIndex.LINE);
    	} else if (kind.equals(CTagsConsoleParser.TYPEDEF)){
    		output.addTypedefDecl(fileNum, fullName, lineNumber, 1, IIndex.LINE);
    	} else if (kind.equals(CTagsConsoleParser.UNION)){
    		output.addUnionDecl(fileNum, fullName, lineNumber, 1, IIndex.LINE);
    	} else if (kind.equals(CTagsConsoleParser.VARIABLE)){
    		output.addVariableDecl(fileNum, fullName, lineNumber, 1, IIndex.LINE);
    	} else if (kind.equals(CTagsConsoleParser.EXTERNALVAR)){
    	
    	}
	}
}