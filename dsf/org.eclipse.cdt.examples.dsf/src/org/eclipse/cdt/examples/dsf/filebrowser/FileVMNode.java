/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.filebrowser;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;


/**
 * File view model node which returns file elements that are found in the directory 
 * specified by the parent element.  The child nodes of this node are fixed to 
 * reference this element, and therefore this node will recursively populate 
 * the contents of the tree reflecting the underlying filesystem directories.
 * <br>
 * Note: this node does NOT sub-class the {@link org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMNode}
 */
@SuppressWarnings("restriction")
class FileVMNode 
    implements IElementLabelProvider, IVMNode
{
    /**
     * Reference to the viewer model provider.  It's mainly used to access the
     * viewer model adapter and its executor.
     */
    private final FileBrowserVMProvider fProvider;
    
    public FileVMNode(FileBrowserVMProvider provider) {
        fProvider = provider;
    }

    @Override
    public String toString() {
        return "FileVMNode";  
    }


    public void dispose() {
        // All resources garbage collected.
    }
    
    public void setChildNodes(IVMNode[] childNodes) {
        throw new UnsupportedOperationException("This node does not support children."); //$NON-NLS-1$
    }

    /** 
     * List of child nodes containing only a reference to this.
     */
    private final IVMNode[] fChildNodes = { this };

    public IVMNode[] getChildNodes() {
        return fChildNodes;
    }
    
    public void update(final IHasChildrenUpdate[] updates) {
        new Job("") { //$NON-NLS-1$
            {
                setSystem(true);
                setPriority(INTERACTIVE);
            }
                
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                for (IHasChildrenUpdate update : updates) {
                    /*
                     * Do not retrieve directory contents just to mark the plus
                     * sign in the tree.  If it's a directory, just assume that
                     * it has children.
                     */
                    FileVMContext vmc = (FileVMContext)update.getElement();
                    update.setHasChilren(vmc.getFile().isDirectory());
                    update.done();
                }
                
                return Status.OK_STATUS;
            }
        }.schedule();
    }
    
    public void update(final IChildrenCountUpdate[] updates) {
        new Job("") { //$NON-NLS-1$
            {
                setSystem(true);
                setPriority(INTERACTIVE);
            }
                
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                for (IChildrenCountUpdate update : updates) {
                    update.setChildCount(getFiles(update).length);
                    update.done();
                }                
                return Status.OK_STATUS;
            }
        }.schedule();
    }
    
    public void update(final IChildrenUpdate[] updates) {
        new Job("") { //$NON-NLS-1$
            {
                setSystem(true);
                setPriority(INTERACTIVE);
            }
                
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                for (IChildrenUpdate update : updates) {
                    File[] files = getFiles(update);
                    int offset = update.getOffset() != -1 ? update.getOffset() : 0;
                    int length = update.getLength() != -1 ? update.getLength() : files.length;
                    for (int i = offset; (i < files.length) && (i < (offset + length)); i++) {
                        update.setChild(new FileVMContext(FileVMNode.this, files[i]), i);
                    }
                    update.done();
                }                
                return Status.OK_STATUS;
            }
        }.schedule();
    }
    
    public void update(final ILabelUpdate[] updates) {
        new Job("") { //$NON-NLS-1$
            {
                setSystem(true);
                setPriority(INTERACTIVE);
            }
                
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                for (ILabelUpdate update : updates) {
                    update.setLabel(getLabel((FileVMContext)update.getElement()), 0);
                    update.done();
                }
                
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    private static final File[] EMPTY_FILE_LIST = new File[0];

    /**
     * Retrieves the list of files for this node.  The list of files is based 
     * on the parent element in the tree, which must be of type FileVMC. 
     * 
     * @param update Update object containing the path (and the parent element) 
     * in the tree viewer.
     * @return List of files contained in the directory specified in the 
     * update object.  An empty list if the parent element is not a directory. 
     * @throws ClassCastException If the parent element contained in the update 
     * is NOT of type FileVMC. 
     */
    private File[] getFiles(IViewerUpdate update) {
        FileVMContext vmc = (FileVMContext)update.getElement();
        File[] files =  vmc.getFile().listFiles();
        return files != null ? files : EMPTY_FILE_LIST;
    }

    /**
     * Returs the text label to show in the tree for given element.
     */
    private  String getLabel(FileVMContext vmc) {
        return vmc.getFile().getName();     
    }

    public void getContextsForEvent(VMDelta parentDelta, Object event, DataRequestMonitor<IVMContext[]> rm) {
        rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "", null)); //$NON-NLS-1$
        rm.done();
    }
    
    public int getDeltaFlags(Object e) {
        /*
         * @see buildDelta()
         */
        int retVal = IModelDelta.NO_CHANGE;
        if (e instanceof String) {
            retVal |= IModelDelta.SELECT | IModelDelta.EXPAND; 
        }
        
        return retVal;
    }

    public void buildDelta(final Object event, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
        /*
         * The FileLayoutNode is recursive, with itself as the only child.  In this 
         * method the delta is calculated for a full path VMContext elements, and the
         * implementation of this method is not recursive.
         */
        if (event instanceof String) {
            new Job("") { //$NON-NLS-1$
                {
                    setSystem(true);
                    setPriority(INTERACTIVE);
                }
                    
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    /*
                     * Requirements for a selection event to be issued is that the file exist, and
                     * that the parentDelta contain a FileVMC of a parent directory as its element.
                     *  
                     * The test for first the former requirement could be performed inside getDeltaFlags() 
                     * but getDeltaFlags() is synchronous, so it is better to perform this test here using 
                     * a background thread (job).
                     * 
                     *  The latter is requirement is needed because this node does not have the algorithm 
                     *  calculate the complete list of root nodes.  That algorithm is implemented inside the
                     *  {@link FileSystemRootsLayoutNode#updateElements} method.
                     */
                    
                    final File eventFile = new File((String)event);
                    File parentFile = null;
                    if (parentDelta.getElement() instanceof FileVMContext) {
                        parentFile = ((FileVMContext)parentDelta.getElement()).getFile();
                    }

                    // The file has to exist in order for us to be able to select 
                    // it in the tree. 
                    if (eventFile.exists() && parentFile != null) {
                        // Create a list containing all files in path
                        List<File> filePath = new LinkedList<File>();
                        for (File file = eventFile; file != null && !file.equals(parentFile); file = file.getParentFile()) {
                            filePath.add(0, file);
                        }

                        if (filePath.size() != 0) {
                            // Build the delta for all files in path.
                            ModelDelta delta = parentDelta;
                            File[] allFilesInDirectory = parentFile.listFiles();
                            for (File pathSegment : filePath) {
                                // All files in path should be directories, and should therefore
                                // have a valid list of elements.
                                assert allFilesInDirectory != null;
                                
                                File[] pathSegmentDirectoryFiles = pathSegment.listFiles();
                                delta = delta.addNode(
                                    new FileVMContext(FileVMNode.this, pathSegment), 
                                    nodeOffset + Arrays.asList(allFilesInDirectory).indexOf(pathSegment), 
                                    IModelDelta.NO_CHANGE, 
                                    pathSegmentDirectoryFiles != null ? pathSegmentDirectoryFiles.length : 0);
                                allFilesInDirectory = pathSegmentDirectoryFiles;
                            }
                            
                            // The last file in path gets the EXPAND | SELECT flags.
                            delta.setFlags(delta.getFlags() | IModelDelta.SELECT | IModelDelta.EXPAND);
                        }
                    }
                    
                    // Invoke the request monitor.
                    
                    requestMonitor.done();

                    return Status.OK_STATUS;
                }
            }.schedule();
        } else {
            requestMonitor.done();
        }            
    }
    
    /**
     * Override the behavior which checks for delta flags of all the child nodes, 
     * because we would get stuck in a recursive loop.  Instead call only the child 
     * nodes which are not us.
     */
    protected Map<IVMNode, Integer> getChildNodesWithDeltas(Object e) {
        Map<IVMNode, Integer> nodes = new HashMap<IVMNode, Integer>(); 
        for (final IVMNode childNode : getChildNodes()) {
            int delta = childNode.getDeltaFlags(e);
            if (delta != IModelDelta.NO_CHANGE) {
                nodes.put(childNode, delta);
            }
        }
        return nodes;
    }

    public IVMProvider getVMProvider() {
        return fProvider;
    }

    
}
