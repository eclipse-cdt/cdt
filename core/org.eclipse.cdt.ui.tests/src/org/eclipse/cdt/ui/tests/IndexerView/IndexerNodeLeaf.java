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
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexInput;
import org.eclipse.cdt.internal.core.search.indexing.IIndexConstants;
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
        
        String word = String.valueOf(result.getWord());
        String stringBeforeName = null;

        // set the filtersFlag
        if (word.startsWith(FilterIndexerViewDialog.ENTRY_REF_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_REF_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_REF;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_REF_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_REF_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_REF;
//        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_DECL_STRING)) {
//            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_DECL_STRING;
//            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_DECL;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_FUNCTION_REF_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_FUNCTION_REF_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_FUNCTION_REF;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_FUNCTION_DECL_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_FUNCTION_DECL_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_FUNCTION_DECL;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_CONSTRUCTOR_REF_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_CONSTRUCTOR_REF_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_CONSTRUCTOR_REF;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_CONSTRUCTOR_DECL_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_CONSTRUCTOR_DECL_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_CONSTRUCTOR_DECL;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_NAMESPACE_REF_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_NAMESPACE_REF_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_NAMESPACE_REF;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_NAMESPACE_DECL_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_NAMESPACE_DECL_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_NAMESPACE_DECL;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_FIELD_REF_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_FIELD_REF_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_FIELD_REF;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_FIELD_DECL_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_FIELD_DECL_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_FIELD_DECL;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_ENUMTOR_REF_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_ENUMTOR_REF_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_ENUMTOR_REF;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_ENUMTOR_DECL_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_ENUMTOR_DECL_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_ENUMTOR_DECL;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_METHOD_REF_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_METHOD_REF_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_METHOD_REF;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_METHOD_DECL_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_METHOD_DECL_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_METHOD_DECL;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_MACRO_DECL_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_MACRO_DECL_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_MACRO_DECL;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_INCLUDE_REF_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_INCLUDE_REF_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_INCLUDE_REF;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_SUPER_REF_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_SUPER_REF_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_SUPER_REF;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_DECL_T_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_DECL_T_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_DECL_T;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_DECL_C_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_DECL_C_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_DECL_C;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_DECL_V_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_DECL_V_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_DECL_V;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_DECL_S_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_DECL_S_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_DECL_S;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_DECL_E_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_DECL_E_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_DECL_E;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_DECL_U_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_DECL_U_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_DECL_U;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_DECL_D_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_DECL_D_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_DECL_D;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_DECL_F_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_DECL_F_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_DECL_F;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_DECL_G_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_DECL_G_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_DECL_G;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_DECL_H_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_DECL_H_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_DECL_H;
        } else if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_DECL_I_STRING)) {
            stringBeforeName = FilterIndexerViewDialog.ENTRY_TYPE_DECL_I_STRING;
            filtersType = FilterIndexerViewDialog.ENTRY_TYPE_DECL_I;
        }

        // set the name
        if (word.startsWith(FilterIndexerViewDialog.ENTRY_TYPE_REF_STRING)) { // if the name is after an additional field then reset stringBeforeName
            int start = word.indexOf(stringBeforeName) + stringBeforeName.length();
            stringBeforeName = stringBeforeName + word.substring(start, start + 2);
        }
		if (stringBeforeName == null) {
			name = word;
			return;
		}

        name = word.substring(word.indexOf(stringBeforeName) + stringBeforeName.length());
        
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
                text = new TextPropertyDescriptor(new TextDescriptorId(IENTRYRESULT_GETWORD__, String.valueOf(entryResult.getWord())), IENTRYRESULT_GETWORD__);
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
        if (!parent.isDisplayFullName() && name.indexOf(IIndexConstants.SEPARATOR) > 0)
            return name.substring(0, name.indexOf(IIndexConstants.SEPARATOR));
        
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
}
