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

import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;

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
            tempTag.addTagToIndexOutput(getFileNumber(), indexer.getOutput());
        
        return false;
    }
    
    public CTagEntry processLineReturnTag(String line){
        CTagEntry tempTag = new CTagEntry(this, line);
        return tempTag;
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
 
 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.ctagsindexer.IConsoleParser#shutdown()
     */
    public void shutdown() {
        // TODO Auto-generated method stub

    }
    
    

}
