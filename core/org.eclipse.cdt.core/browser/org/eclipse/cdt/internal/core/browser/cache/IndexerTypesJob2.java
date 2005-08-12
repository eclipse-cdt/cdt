/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.cache;

import java.io.IOException;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.browser.TypeInfo;
import org.eclipse.cdt.core.browser.TypeReference;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexInput;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class IndexerTypesJob2 extends IndexerJob2 {
	
	private ITypeCache fTypeCache;

	public IndexerTypesJob2(IndexManager indexManager, ITypeCache typeCache, ITypeSearchScope scope) {
		super(indexManager, typeCache.getProject());
		fTypeCache = typeCache;
	}

	protected boolean processIndex(IProgressMonitor progressMonitor) throws InterruptedException {
		IndexInput input = null;
		try {
			input = new BlocksIndexInput( fProjectIndex.getIndexFile() );	
			input.open();
			updateNamespaces(input, progressMonitor);
			updateTypes(input, progressMonitor);
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
	}

	private void updateNamespaces(IndexInput input, IProgressMonitor monitor)
		throws InterruptedException, IOException {
		if (monitor.isCanceled())
			throw new InterruptedException();

		IEntryResult[] namespaceEntries = fProjectIndex.getEntries( IIndex.NAMESPACE, IIndex.ANY, IIndex.DEFINITION );
        IQueryResult[] namespacePaths = fProjectIndex.getPrefix(IIndex.NAMESPACE, IIndex.ANY, IIndex.DEFINITION );
//            input.queryEntriesPrefixedBy(Index.encodeEntry(IIndex.NAMESPACE, IIndex.ANY, IIndex.DECLARATION));
		if (namespaceEntries != null) {
			//TODO subprogress monitor
			for (int i = 0; i < namespaceEntries.length; ++i) {
				if (monitor.isCanceled())
					throw new InterruptedException();
				
				IEntryResult entry = namespaceEntries[i];
				String name = entry.getName();
				if (name.length() != 0) {
					String[] enclosingNames = entry.getEnclosingNames();
					addType(input, entry, namespacePaths[i].getPath(), ICElement.C_NAMESPACE, name, enclosingNames, monitor);
				}
			}
		}
	}
	
	private void updateTypes(IndexInput input, IProgressMonitor monitor)
		throws InterruptedException, IOException {
		if (monitor.isCanceled())
			throw new InterruptedException();

        for( int counter = 0; counter < 2; ++counter )
        {
    		IEntryResult[] typeEntries = fProjectIndex.getEntries( IIndex.TYPE, IIndex.ANY, ( counter == 0 ) ? IIndex.DECLARATION : IIndex.DEFINITION ); 
           
    		if (typeEntries != null) {
    			//TODO subprogress monitor
    			for (int i = 0; i < typeEntries.length; ++i) {
    				if (monitor.isCanceled())
    					throw new InterruptedException();
    				
    				IEntryResult entry = typeEntries[i];
    				
    				String name = entry.extractSimpleName();
    				switch (entry.getKind() ) {
    				case IIndex.TYPE_CLASS :
    				case IIndex.TYPE_STRUCT :
    				case IIndex.TYPE_TYPEDEF :
    				case IIndex.TYPE_ENUM :
    				case IIndex.TYPE_UNION :			
    					if (counter != 0 && name.length() != 0) {  // skip anonymous structs
    						addType(input, entry, null, index2ICElement( entry.getKind() ), name, entry.getEnclosingNames(), monitor);
    					}
    					break;
    				default:
    					break;
    				}
    			}
    		}
        }
        
        IEntryResult[] typeEntries = fProjectIndex.getEntries( IIndex.TYPE, IIndex.TYPE_DERIVED, IIndex.ANY );
        if (typeEntries != null){
	        for( int j  = 0; j < typeEntries.length; ++j )
	        {
	            if (monitor.isCanceled())
	                throw new InterruptedException();
	            
	            IEntryResult entry = typeEntries[j];
	            String name = entry.extractSimpleName();
	            switch( entry.getKind() )
	            {
	              case IIndex.TYPE_DERIVED :
	                  if (name.length() != 0) {  // skip anonymous structs
	                      addSuperTypeReference(input, entry, name, entry.getEnclosingNames(), monitor);
	                  }
	                  break;
	              default:
	                  break;
	            }
	        }
        }
	}

    	private void addType(IndexInput input, IEntryResult entry, String pth, int type, String name, String[] enclosingNames, IProgressMonitor monitor)
    		throws InterruptedException, IOException {
		QualifiedTypeName qualifiedName = new QualifiedTypeName(name, enclosingNames);
		ITypeInfo info = fTypeCache.getType(type, qualifiedName);
		if (info == null || info.isUndefinedType()) {
			int[] references = entry.getFileReferences();
			if (references != null && references.length > 0) {
				// add new type to cache
				if (info != null) {
					info.setCElementType(type);
				} else {
					info = new TypeInfo(type, qualifiedName);
					fTypeCache.insert(info);
				}

//				for (int i = 0; i < references.length; ++i) {
//					if (monitor.isCanceled())
//						throw new InterruptedException();
//
//					IndexedFile file = input.getIndexedFile(references[i]);
//					if (file != null && file.getPath() != null) {
//						IPath path = PathUtil.getWorkspaceRelativePath(file.getPath());
//						info.addReference(new TypeReference(path, project));
//					}
//				}				
				if (pth == null) {
		            pth = input.getIndexedFile( references[0] ).getPath();
				}
				
				final IPath workspaceRelativePath = PathUtil.getWorkspaceRelativePath(pth);
                int offset = entry.getOffsets()[0][0];
//                int offsetType = Integer.valueOf(String.valueOf(offsets[i][j]).substring(0,1)).intValue();
                int offsetType = offset;
                int m = 1;
                while (offsetType >= 10) {
                    offsetType = offsetType / 10;
                    m *= 10;
                }
                int value = offset - ( offsetType * m );
//                int value = Integer.valueOf(String.valueOf(offset).substring(1)).intValue();
                
                TypeReference typeReference = null;
                if (offsetType==IIndex.LINE){
                    typeReference = new TypeReference(workspaceRelativePath, fProject, value, 0 );
                    typeReference.offsetIsLineNumber = true;
                }else if (offsetType==IIndex.OFFSET){
                    typeReference = new TypeReference(workspaceRelativePath, fProject, value, entry.getOffsetLengths()[0][0] );
                }
                if( typeReference != null )
                    info.addReference(typeReference);
			}
		}
	}

	private void addSuperTypeReference(IndexInput input, IEntryResult entry, String name, String[] enclosingNames, IProgressMonitor monitor) throws InterruptedException, IOException {
		QualifiedTypeName qualifiedName = new QualifiedTypeName(name, enclosingNames);
		ITypeInfo info = fTypeCache.getType(ICElement.C_CLASS, qualifiedName);
		if (info == null)
			info = fTypeCache.getType(ICElement.C_STRUCT, qualifiedName);
		if (info == null) {
			// add dummy type to cache
			info = new TypeInfo(0, qualifiedName);
			fTypeCache.insert(info);
		}
		int[] references = entry.getFileReferences();
		if (references != null && references.length > 0) {
			for (int i = 0; i < references.length; ++i) {
				if (monitor.isCanceled())
					throw new InterruptedException();
				
				String pth = input.getIndexedFile( references[i] ).getPath();
				IPath path = PathUtil.getWorkspaceRelativePath(pth);

                info.addDerivedReference(new TypeReference(path, fProject));
//
//					// get absolute path
//					IPath path = new Path(file.getPath());
//					IPath projectPath = project.getFullPath();
//					if (projectPath.isPrefixOf(path)) {
//						path = project.getLocation().append(path.removeFirstSegments(projectPath.segmentCount()));
//					}
//					info.addDerivedReference(new TypeReference(path, project));

			}
		}
	}
}
