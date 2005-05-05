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

import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
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
    
    private IEntryResult result = null;
	private String [] fileMap = null;
	
	public String[] getFileMap() {
		return fileMap;
	}
    
    private IndexerNodeParent parent = null;
    
    public IndexerNodeLeaf(IEntryResult result, String[] fileMap) {
        this.result = result;
        this.fileMap = fileMap;
        if (result != null)
        	this.filtersType = IndexerView.getKey(result.getMetaKind(), result.getKind(), result.getRefKind());
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
            
                // get the reference descriptors
                int[] references = entryResult.getFileReferences();
                if (references != null) {
                    for (int j = 0; j < references.length; ++j) {
                        String file = fileMap[references[j]];
                        if (file != null) {
                            String id = REFERENCE_NUMBER_ + String.valueOf(j);
                            text = new TextPropertyDescriptor(new TextDescriptorId(id, PathUtil.getWorkspaceRelativePath(file).toOSString()), id);
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
                text = new TextPropertyDescriptor(new TextDescriptorId("MetaKind", entryResult.getStringMetaKind()), "MetaKind");
                text.setCategory(IENTRYRESULT);
                descriptors = (IPropertyDescriptor[])ArrayUtil.append(IPropertyDescriptor.class, descriptors, text);
				
		          // add a word descriptor
				if (entryResult.getMetaKind() == IIndex.TYPE) {
					text = new TextPropertyDescriptor(new TextDescriptorId("TypeKind", entryResult.getStringKind()), "TypeKind");
					text.setCategory(IENTRYRESULT);
					descriptors = (IPropertyDescriptor[])ArrayUtil.append(IPropertyDescriptor.class, descriptors, text);
				}
		          // add a word descriptor
                text = new TextPropertyDescriptor(new TextDescriptorId("ReferenceKind", entryResult.getStringRefKind()), "ReferenceKind");
                text.setCategory(IENTRYRESULT);
                descriptors = (IPropertyDescriptor[])ArrayUtil.append(IPropertyDescriptor.class, descriptors, text);

				
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
        if (!parent.isDisplayFullName())
            return getShortName();
        
        return getName();
    }
    
    public int getFiltersType() {
        return filtersType;
    }
    
    public String getName() {
        return result.getName();
    }
	
	public String getShortName() {
		return result.extractSimpleName();
	}
}
