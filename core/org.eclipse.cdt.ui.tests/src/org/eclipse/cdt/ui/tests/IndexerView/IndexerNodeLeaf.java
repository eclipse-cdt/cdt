/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.IndexerView;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexInput;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
 
/**
 * @author dsteffle
 */
public class IndexerNodeLeaf implements IAdaptable {
    private static final char EMPTY_SPACE = ' ';
    private int filtersType=0;
    
    IEntryResult result = null;
    String name = null;
    File indexFile = null;
    char type = EMPTY_SPACE;
    
    private IndexerNodeParent parent = null;
    
    public IndexerNodeLeaf(IEntryResult result, File indexFile) {
        this.result = result;
        this.indexFile = indexFile;
        
        setNameAndFiltersFlag();
    }
    
    private void setNameAndFiltersFlag() {
        if (result == null) return;
        
        filtersType = IndexerView.getKey(result.getMetaKind(), result.getKind(), result.getRefKind());
        name = result.getName();       
    }
    
    public IndexerNodeParent getParent() {
        return parent;
    }
    
    public void setParent(IndexerNodeParent parent) {
        this.parent = parent;
    }
    
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class)
            return new IndexerPropertySource(result);
        
        return null;
    }
    
    private class IndexerPropertySource implements IPropertySource {

        private static final String IENTRYRESULT = "IEntryResult"; //$NON-NLS-1$
        private static final String IENTRYRESULT_GETWORD__ = "IEntryResult#getWord()"; //$NON-NLS-1$
        private static final String REFERENCES = "References"; //$NON-NLS-1$
        private static final String REFERENCE_NUMBER_ = "reference# "; //$NON-NLS-1$
        private static final String OFFSETS = "Offsets"; //$NON-NLS-1$
        private static final String OFFSETS_NUMBER = "offsets for #"; //$NON-NLS-1$
        private static final String OFFSETS_LINE = "Line "; //$NON-NLS-1$
        private static final String OFFSETS_OFFSET = "Offset "; //$NON-NLS-1$
        private static final int DEFAULT_DESCRIPTOR_SIZE = 4;
        IEntryResult entryResult = null;
        
        public IndexerPropertySource(IEntryResult result) {
            this.entryResult = result;
        }
        
        public Object getEditableValue() {
            return null;
        }

        public IPropertyDescriptor[] getPropertyDescriptors() {
            IPropertyDescriptor[] descriptors = new IPropertyDescriptor[DEFAULT_DESCRIPTOR_SIZE];
            
            TextPropertyDescriptor text = null;
            
            IndexInput input = new BlocksIndexInput(indexFile);
            try {
                input.open();

                // get the reference descriptors
                int[] references = entryResult.getFileReferences();
                if (references != null) {
                    for (int j = 0; j < references.length; ++j) {
                        IndexedFileEntry file = input.getIndexedFile(references[j]);
                        if (file != null && file.getPath() != null) {
                            String id = REFERENCE_NUMBER_ + String.valueOf(j);
                            text = new TextPropertyDescriptor(new TextDescriptorId(id, PathUtil.getWorkspaceRelativePath(file.getPath()).toOSString()), id);
                            text.setCategory(REFERENCES);
                            descriptors = (IPropertyDescriptor[])ArrayUtil.append(IPropertyDescriptor.class, descriptors, text);
                        }
                    }
                }
                
                //offsets
                int[][]offsets = entryResult.getOffsets();
				int[][]offsetLengths = entryResult.getOffsetLengths();
                if (offsets != null){
                    for (int j=0; j<offsets.length; j++){
                        String id = OFFSETS_NUMBER + j;
                        String offsetString = ""; //$NON-NLS-1$
                        for (int k=0; k<offsets[j].length; k++){
                            String rawOffset = String.valueOf(offsets[j][k]) ;
							String offsetLocation = String.valueOf(offsetLengths[j][k]);
                            switch(rawOffset.charAt(0)){
                             case '1':
                                 offsetString +=  OFFSETS_LINE + rawOffset.substring(1) + " "; //$NON-NLS-1$
                             break;
                             case '2':
                                 offsetString +=  OFFSETS_OFFSET + rawOffset.substring(1) + ":" + offsetLocation + " "; //$NON-NLS-1$ //$NON-NLS-2$
                             break;    
                            }
                       
                        }
                        text = new TextPropertyDescriptor(new TextDescriptorId(id, offsetString), id);
                        text.setCategory(OFFSETS);
                        descriptors = (IPropertyDescriptor[])ArrayUtil.append(IPropertyDescriptor.class, descriptors, text);
                    }
                }
                
                // add a word descriptor
                text = new TextPropertyDescriptor(new TextDescriptorId(IENTRYRESULT_GETWORD__, String.valueOf(entryResult.toString())), IENTRYRESULT_GETWORD__);
                text.setCategory(IENTRYRESULT);
                descriptors = (IPropertyDescriptor[])ArrayUtil.append(IPropertyDescriptor.class, descriptors, text);
                
            } catch (IOException e) {
            }
            
            return (IPropertyDescriptor[])ArrayUtil.trim(IPropertyDescriptor.class, descriptors);
        }
        
        private class TextDescriptorId {
            String id = null;
            String file = null;
            
            public TextDescriptorId(String id, String file) {
                this.id=id;
                this.file=file;
            }

            public String getFile() {
                return file;
            }
            
        }

        public Object getPropertyValue(Object id) {
            if (id instanceof TextDescriptorId) {
                return ((TextDescriptorId)id).getFile();
            }
            
            return null;
        }

        public boolean isPropertySet(Object id) {
            return false;
        }

        public void resetPropertyValue(Object id) { }

        public void setPropertyValue(Object id, Object value) { }
        
    }
    
    public IEntryResult getResult() {
        return result;
    }
    
    public void setResult(IEntryResult result) {
        this.result = result;
    }

    public String toString() {
        if (!parent.isDisplayFullName() && name.indexOf(ICIndexStorageConstants.SEPARATOR) > 0)
            return getShortName();
        
        return name;
    }
    
    public int getFiltersType() {
        return filtersType;
    }

    public char getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
	
	public String getShortName() {
		if (name.indexOf(ICIndexStorageConstants.SEPARATOR) > 0)
			return name.substring(0, name.indexOf(ICIndexStorageConstants.SEPARATOR));
		
		return name;
	}
}
