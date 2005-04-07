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

import java.util.StringTokenizer;

import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.domsourceindexer.IndexEncoderUtil;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants.EntryType;

/**
 * @author Bogdan Gheorghe
 */
public class CTagsConsoleParser implements IConsoleParser {

    final static String TAB_SEPARATOR = "\t"; //$NON-NLS-1$
	final static String PATTERN_SEPARATOR = ";\""; //$NON-NLS-1$
	final static String COLONCOLON = "::";  //$NON-NLS-1$
	final static String LANGUAGE = "language"; //$NON-NLS-1$
	final static String KIND = "kind"; //$NON-NLS-1$
	final static String LINE = "line"; //$NON-NLS-1$
	final static String FILE = "file"; //$NON-NLS-1$
	final static String INHERITS = "inherits"; //$NON-NLS-1$
	final static String ACCESS = "access"; //$NON-NLS-1$
	final static String IMPLEMENTATION = "implementation"; //$NON-NLS-1$
	
	final static String CLASS = "class"; //$NON-NLS-1$
    final static String MACRO = "macro"; //$NON-NLS-1$
    final static String ENUMERATOR = "enumerator"; //$NON-NLS-1$
    final static String FUNCTION = "function"; //$NON-NLS-1$
    final static String ENUM = "enum"; //$NON-NLS-1$
    final static String MEMBER = "member"; //$NON-NLS-1$
    final static String NAMESPACE = "namespace"; //$NON-NLS-1$
    final static String PROTOTYPE = "prototype"; //$NON-NLS-1$
    final static String STRUCT = "struct"; //$NON-NLS-1$
    final static String TYPEDEF = "typedef"; //$NON-NLS-1$
    final static String UNION = "union"; //$NON-NLS-1$
    final static String VARIABLE = "variable"; //$NON-NLS-1$
    final static String EXTERNALVAR = ""; //$NON-NLS-1$
   
    private CTagsIndexerRunner indexer;

    public CTagsConsoleParser(CTagsIndexerRunner indexer){
        this.indexer = indexer;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.ctagsindexer.IConsoleParser#processLine(java.lang.String)
     */
    public boolean processLine(String line) {
        CTagEntry tempTag = new CTagEntry(this, line);
        if (indexer != null)
            encodeTag(tempTag);
        
        return false;
    }
    
    public CTagEntry processLineReturnTag(String line){
        CTagEntry tempTag = new CTagEntry(this, line);
        return tempTag;
    }

    
    /**
     * @param tempTag
     */
    private void encodeTag(CTagEntry tempTag) {
        EntryType entryType = null;
    	String kind = (String)tempTag.tagExtensionField.get(KIND);
    
    	if (kind == null)
    	  return;
    	
    	char[][] fullName = getQualifiedName(tempTag);
    	ICSearchConstants.LimitTo type = ICSearchConstants.DECLARATIONS;
    	
    	if (kind.equals(CLASS)){
    	    entryType = IIndexEncodingConstants.CLASS;
    	} else if (kind.equals(MACRO)){
    	    entryType = IIndexEncodingConstants.MACRO;
    	} else if (kind.equals(ENUMERATOR)){
    	    entryType = IIndexEncodingConstants.ENUMERATOR;
    	} else if (kind.equals(FUNCTION)){
    	    entryType = IIndexEncodingConstants.FUNCTION;
    	} else if (kind.equals(ENUM)){
    	    entryType = IIndexEncodingConstants.ENUM;
    	} else if (kind.equals(MEMBER)){
    	    entryType = IIndexEncodingConstants.FIELD;
    	} else if (kind.equals(NAMESPACE)){
    	    entryType = IIndexEncodingConstants.NAMESPACE;
    	} else if (kind.equals(PROTOTYPE)){
    	    entryType = IIndexEncodingConstants.FUNCTION;
    	    //type = ICSearchConstants.DEFINITIONS;
    	} else if (kind.equals(STRUCT)){
    	    entryType = IIndexEncodingConstants.STRUCT;
    	} else if (kind.equals(TYPEDEF)){
    	    entryType = IIndexEncodingConstants.TYPEDEF;
    	} else if (kind.equals(UNION)){
    	    entryType = IIndexEncodingConstants.UNION;
    	} else if (kind.equals(VARIABLE)){
    	    entryType = IIndexEncodingConstants.VAR;
    	} else if (kind.equals(EXTERNALVAR)){
    	
    	}
    
    	String lineNumber = (String)tempTag.tagExtensionField.get(LINE);
    	
    	if (entryType != null)
    	    indexer.getOutput().addRef(getFileNumber(),IndexEncoderUtil.encodeEntry(fullName,entryType,type), Integer.parseInt(lineNumber), 1, ICIndexStorageConstants.LINE);
    }

    /**
     * @return
     */
    private int getFileNumber() {
        int fileNum = 0;
        IndexedFileEntry mainIndexFile = indexer.getOutput().getIndexedFile(
                indexer.getResourceFile().getFullPath().toString());
        if (mainIndexFile != null)
            fileNum = mainIndexFile.getFileID();
        
        return fileNum;
    }
    /**
     * @param tempTag
     * @return
     */
    public char[][] getQualifiedName(CTagEntry tempTag) {
        char[][] fullName = null;
        String name = null;
        String[] types = {NAMESPACE, CLASS, STRUCT, UNION, FUNCTION, ENUM};
       
        for (int i=0; i<types.length; i++){
            //look for name
            name = (String) tempTag.tagExtensionField.get(types[i]); 
            if (name != null)
                break;
        }
        
        if (name != null){
	        StringTokenizer st = new StringTokenizer(name, COLONCOLON);
			fullName = new char[st.countTokens() + 1][];
			int i=0;
			while (st.hasMoreTokens()){
			    fullName[i] = st.nextToken().toCharArray();
			    i++;
			}
			fullName[i] = tempTag.elementName.toCharArray();
        } else {
            fullName = new char[1][];
            fullName[0] = tempTag.elementName.toCharArray();
        }
        
        return fullName;
    }
 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.ctagsindexer.IConsoleParser#shutdown()
     */
    public void shutdown() {
        // TODO Auto-generated method stub

    }
    
    

}
