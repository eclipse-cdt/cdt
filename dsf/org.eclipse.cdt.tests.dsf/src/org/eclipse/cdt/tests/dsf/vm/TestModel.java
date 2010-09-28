/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import java.util.Arrays;
import java.util.Hashtable;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelCheckProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;
import org.osgi.framework.BundleContext;

/**
 * Test model for the use in unit tests.  This test model contains a set of 
 * elements in a tree structure.  It contains utility methods for modifying the 
 * model and for verifying that the viewer content matches the model.
 * 
 * @since 2.2
 */
public class TestModel extends AbstractDsfService implements IFormattedValues {
    
    public interface TestElementValidator {
        public void validate(TestElement modelElement, TestElement viewerElement, TreePath viewerPath);
    }
    
    public static class TestElement extends AbstractDMContext implements IFormattedDataDMContext {
        public static final IDMContext[] EMPTY_PARENTS_ARRAY = new IDMContext[0];
        
        private final TestModel fModel;
        private final String fID;
        TestElement[] fChildren;
        String fLabelAppendix = "";
        boolean fExpanded;
        boolean fChecked;
        boolean fGrayed;
        
        private TestElement[] fParents = new TestElement[1];
        
        public TestElement(TestModel model, String text, TestElement[] children) {
            this (model, text, false, false, children);
        }

        public TestElement(TestModel model, String text, boolean checked, boolean grayed, TestElement[] children) {
            super(model, EMPTY_PARENTS_ARRAY);
            fModel = model;
            fID = text;
            fChildren = children;
            for (TestElement child : children) {
                child.setParent(this);
            }
            fChecked = checked;
            fGrayed = grayed;
        }
        
        public void setParent(TestElement parent) {
            fParents[0] = parent;
        }

        public TestElement getParent() {
            return fParents[0];
        }
        
        @Override
        public IDMContext[] getParents() {
            return fParents;
        }

        public TestModel getModel() {
            return fModel;
        }
        
        @SuppressWarnings("rawtypes")
        @Override
        public Object getAdapter(Class adapter) {
            if (adapter.isInstance(fModel)) {
                return fModel;
            }
            return null;
        }
        
        public String getID() {
            return fID;
        }
        
        public void setLabelAppendix(String appendix) {
            fLabelAppendix = appendix;
        }
        
        public String getLabel() {
            return fID + fLabelAppendix;
        }
        
        public TestElement[] getChildren() {
            return fChildren;
        }
        
        public boolean isExpanded() {
            return fExpanded;
        }
        
        public boolean getGrayed() {
            return fGrayed;
        }
        
        public boolean getChecked() {
            return fChecked;
        }

        public void setChecked(boolean checked, boolean grayed) {
            fChecked = checked;
            fGrayed = grayed;
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof TestElement && fID.equals(((TestElement)obj).fID);
        }
        
        @Override
        public int hashCode() {
            return fID.hashCode();
        }
        
        @Override
        public String toString() {
            return getLabel();
        }
        
        public int indexOf(TestElement child) {
            return Arrays.asList(fChildren).indexOf(child);
        }
    }

    public static final class TestEvent {
        private final TestElement fElement;
        private final int fType;
        
        public TestEvent(TestElement element, int type) {
            fElement = element;
            fType = type;
        }
        
        public TestElement getElement() {
            return fElement;
        }
        
        /**
         * @see IModelDelta#getFlags()
         */
        public int getType() {
            return fType;
        }
    }
    
    private static final IFormattedValuesListener NULL_LISTENER = new IFormattedValuesListener() {
        public void formattedValueUpdated(FormattedValueDMContext formattedValueDmc) {}
    };
    
    private TestElement fRoot;
    private Object fInput = null;
    private TreePath fRootPath = TreePath.EMPTY;
    private IFormattedValuesListener fListener = NULL_LISTENER;
    
    /**
     * Constructor private.  Use static factory methods instead. 
     */
    public TestModel(DsfSession session) {
        super(session);
    }

    @Override
    protected BundleContext getBundleContext() {
        return DsfTestPlugin.getBundleContext();
    }
    
    @Override
    public void initialize(RequestMonitor rm) {
        super.initialize(new RequestMonitor(getExecutor(), rm)  {
            @Override
            protected void handleSuccess() {
                register(new String[0], new Hashtable<String, String>() );
                super.handleSuccess();
            }
        });
    }

    @Override
    public void shutdown(RequestMonitor rm) {
        unregister();
        super.shutdown(rm);
    }
    
    public void setTestModelListener(IFormattedValuesListener listener) {
        if (listener != null) {
            fListener = listener;
        } else {
            fListener = NULL_LISTENER;
        }
    }
    
    public TestElement getRootElement() {
        return fRoot;
    }
    
    public ModelDelta getBaseDelta(ModelDelta rootDelta) {
        ModelDelta delta = rootDelta;
        for (int i = 0; i < fRootPath.getSegmentCount(); i++) {
        	ModelDelta subDelta = delta.getChildDelta(fRootPath.getSegment(i));
        	if (subDelta == null) {
        		subDelta = delta.addNode(fRootPath.getSegment(i), IModelDelta.NO_CHANGE);
        	}
        	delta = subDelta;
        }
        delta.setChildCount(getRootElement().getChildren().length);
        return delta;
    }

    public int getModelDepth() {
        return getDepth(getRootElement(), 0);
    }
    
    private int getDepth(TestElement element, int atDepth) {
        TestElement[] children = element.getChildren(); 
        if (children.length == 0) {
            return atDepth;
        }
        int depth = atDepth + 1;
        for (int i = 0; i < children.length; i++) {
            depth = Math.max(depth, getDepth(children[i], atDepth + 1));
        }

        return depth;
    }
    
    public final static String ELEMENT_MEMENTO_ID = "id";
    
    public void compareElements(IElementCompareRequest[] updates) {
        for (int i = 0; i < updates.length; i++) {
            String elementID = ((TestElement)updates[i].getElement()).getID();
            String mementoID = updates[i].getMemento().getString(ELEMENT_MEMENTO_ID);
            updates[i].setEqual( elementID.equals(mementoID) );
            updates[i].done();
        }        
        
    }
    
    public void encodeElements(IElementMementoRequest[] updates) {
        for (int i = 0; i < updates.length; i++) {
            String elementID = ((TestElement)updates[i].getElement()).getID();
            updates[i].getMemento().putString(ELEMENT_MEMENTO_ID, elementID);
            updates[i].done();
        }        
    }
    
    public void elementChecked(IPresentationContext context, Object viewerInput, TreePath path, boolean checked) {
        TestElement element = getElement(path); 
        Assert.assertFalse(element.getGrayed());
        element.setChecked(checked, false);
    }
    
    public TestElement getElement(TreePath path) {
        if (path.getSegmentCount() == 0) {
            return getRootElement();
        } else {
            if (path.getLastSegment() instanceof TestElement) {
                return (TestElement)path.getLastSegment();
            } else if (path.getLastSegment() instanceof TestElementVMContext) {
                return ((TestElementVMContext)path.getLastSegment()).getElement();
            }
            return null;
        }
    }
    
    public TestElement getElementFromViewer(ITreeModelContentProviderTarget viewer, TreePath parentPath, int index) {
        Object element = viewer.getChildElement(parentPath, index);
        if (element instanceof TestElementVMContext) {
            return ((TestElementVMContext)element).getElement();
        }
        return null;
    }

    public void setAllExpanded() {
        doSetExpanded(fRoot);
    }
    
    private void doSetExpanded(TestElement element) {
        element.fExpanded = true;
        for (int i = 0; i < element.fChildren.length; i++) {
            doSetExpanded(element.fChildren[i]);
        }
    }

    public void setAllAppendix(String appendix) {
        doSetAllAppendix(fRoot, appendix);
    }
    
    private void doSetAllAppendix(TestElement element, String appendix) {
        element.setLabelAppendix(appendix);
        for (int i = 0; i < element.fChildren.length; i++) {
            doSetAllAppendix(element.fChildren[i], appendix);
        }
    }

    public void validateData(ITreeModelViewer viewer, TreePath path) {
        
        validateData(viewer, path, null, false);
    }

    public void validateData(ITreeModelViewer viewer, TreePath path, TestElementValidator validator) {
        
        validateData(viewer, path, validator, false);
    }

    public void validateData(ITreeModelViewer _viewer, TreePath path, TestElementValidator validator, boolean expandedElementsOnly) {
        
        ITreeModelContentProviderTarget viewer = (ITreeModelContentProviderTarget)_viewer;
        TestElement element = getElement(path);
        if ( Boolean.TRUE.equals(_viewer.getPresentationContext().getProperty(ICheckUpdate.PROP_CHECK)) ) {
            ITreeModelCheckProviderTarget checkTarget = (ITreeModelCheckProviderTarget)_viewer;  
            Assert.assertEquals(element.getChecked(), checkTarget.getElementChecked(path));
            Assert.assertEquals(element.getGrayed(), checkTarget.getElementGrayed(path));
        }
        
        if (!expandedElementsOnly || path.getSegmentCount() == 0 || viewer.getExpandedState(path) ) {
            TestElement[] children = element.getChildren();
            Assert.assertEquals(children.length, viewer.getChildCount(path));

            for (int i = 0; i < children.length; i++) {
                Object viewerObject = viewer.getChildElement(path, i);
                if (viewerObject instanceof TestElementVMContext) {
                    TreePath childPath = path.createChildPath(viewerObject);
                    TestElement viewerElement = ((TestElementVMContext)viewerObject).getElement();
                    Assert.assertEquals(children[i], viewerElement);
                    if (validator != null) {
                        validator.validate(children[i], viewerElement, childPath);
                    }
                    
                    validateData(viewer, childPath, validator, expandedElementsOnly);
                }
            }
        } else if (!viewer.getExpandedState(path)) {
            // If element not expanded, verify the plus sign.
            Assert.assertEquals(viewer.getHasChildren(path), element.getChildren().length > 0);
        }
    }

    public void setRoot(TestElement root) {
        fRoot = root;
    }
    
    /** Create or retrieve delta for given path
     * @param combine if then new deltas for the given path are created. If false existing ones are reused.
     */
    public ModelDelta getElementDelta(ModelDelta baseDelta, TreePath path, boolean combine) {
        TestElement element = getRootElement();
        ModelDelta delta = baseDelta;
        
        for (int i = 0; i < path.getSegmentCount(); i++) {
            TestElement[] children = element.getChildren(); 
            delta.setChildCount(children.length);
            Object segment = path.getSegment(i);
            int j;
            for (j = 0; j < children.length; j++) {
                if (segment.equals(children[j])) {
                    element = children[j];
                    ModelDelta nextDelta = null;
                    if (combine) {
                    	nextDelta = delta.getChildDelta(element);
                    }
                    if (nextDelta == null) {
                    	nextDelta = delta.addNode(element, j, IModelDelta.NO_CHANGE, element.getChildren().length);
                    }
                    delta = nextDelta;
                    break;
                }
            }
            if (j == children.length) {
                throw new IllegalArgumentException("Invalid path");
            }
        }
        return delta;
        
    }
    
    private TreePath getRelativePath(TreePath path) {
        Object[] segments = new Object[path.getSegmentCount() - fRootPath.getSegmentCount()];
        for (int i = fRootPath.getSegmentCount(), _i = 0; i < path.getSegmentCount(); i++, _i++) {
            segments[_i] = path.getSegment(i);
        }
        return new TreePath(segments);
    }
    
    public ModelDelta appendElementLabel(TreePath path, String labelAppendix) {
        Assert.assertTrue(path.startsWith(fRootPath, null));
        ModelDelta rootDelta = new ModelDelta(fInput, IModelDelta.NO_CHANGE);
        ModelDelta baseDelta = getBaseDelta(rootDelta);
        TreePath relativePath = getRelativePath(path);
        TestElement element = getElement(relativePath);
        ModelDelta delta = getElementDelta(baseDelta, relativePath, false);
        element.setLabelAppendix(labelAppendix);
        delta.setFlags(delta.getFlags() | IModelDelta.STATE);

        return rootDelta;
    }

    public ModelDelta setElementChecked(TreePath path, boolean checked, boolean grayed) {
        Assert.assertTrue(path.startsWith(fRootPath, null));
        ModelDelta rootDelta = new ModelDelta(fInput, IModelDelta.NO_CHANGE);
        ModelDelta baseDelta = getBaseDelta(rootDelta);
        TreePath relativePath = getRelativePath(path);
        TestElement element = getElement(relativePath);
        ModelDelta delta = getElementDelta(baseDelta, relativePath, false);
        element.setChecked(checked, grayed);
        delta.setFlags(delta.getFlags() | IModelDelta.STATE);

        return rootDelta;
    }

    public ModelDelta setElementChildren(TreePath path, TestElement[] children) {
        Assert.assertTrue(path.startsWith(fRootPath, null));
        ModelDelta rootDelta = new ModelDelta(fInput, IModelDelta.NO_CHANGE);
        ModelDelta baseDelta = getBaseDelta(rootDelta);
        TreePath relativePath = getRelativePath(path);

        // Find the parent element and generate the delta node for it.
        TestElement element = getElement(relativePath);
        ModelDelta delta = getElementDelta(baseDelta, relativePath, false);
        
        // Set the new children array
        element.fChildren = children;
        
        // Add the delta flag and update the child count in the parent delta.        
        delta.setFlags(delta.getFlags() | IModelDelta.CONTENT);
        delta.setChildCount(children.length);
        
        return rootDelta;
    }
    
    public ModelDelta replaceElementChild(TreePath parentPath, int index, TestElement child) {
        ModelDelta rootDelta = new ModelDelta(fInput, IModelDelta.NO_CHANGE);
        ModelDelta baseDelta = getBaseDelta(rootDelta);
        TreePath relativePath = getRelativePath(parentPath);
        
        TestElement element = getElement(relativePath);
        ModelDelta delta= getElementDelta(baseDelta, relativePath, false);
        TestElement oldChild = element.fChildren[index]; 
        element.fChildren[index] = child;
        delta.addNode(oldChild, child, IModelDelta.REPLACED);
        // TODO: set replacement index!?!
        
        return rootDelta;
    }    

    public ModelDelta addElementChild(TreePath parentPath, int index, TestElement newChild) {
        ModelDelta rootDelta = new ModelDelta(fInput, IModelDelta.NO_CHANGE);
        ModelDelta baseDelta = getBaseDelta(rootDelta);
        TreePath relativePath = getRelativePath(parentPath);

        // Find the parent element and generate the delta node for it.
        TestElement element = getElement(relativePath);
        ModelDelta delta= getElementDelta(baseDelta, relativePath, false);

        // Add the new element
        element.fChildren = doInsertElementInArray(element.fChildren, index, newChild);
        
        // Add the delta flag and update the child count in the parent delta.
        delta.setChildCount(element.getChildren().length);
        delta.addNode(newChild, IModelDelta.ADDED);
        
        return rootDelta;
    }    

    public ModelDelta insertElementChild(TreePath parentPath, int index, TestElement newChild) {
        return insertElementChild(null, parentPath, index, newChild);
    }    

    public ModelDelta insertElementChild(ModelDelta rootDelta, TreePath parentPath, int index, TestElement newChild) {
        if (rootDelta == null) {
        	rootDelta = new ModelDelta(fInput, IModelDelta.NO_CHANGE);
        }
        ModelDelta baseDelta = getBaseDelta(rootDelta);
        TreePath relativePath = getRelativePath(parentPath);

        // Find the parent element and generate the delta node for it.
        TestElement element = getElement(relativePath);
        ModelDelta delta= getElementDelta(baseDelta, relativePath, false);
        
        // Add the new element
        element.fChildren = doInsertElementInArray(element.fChildren, index, newChild);
        
        // Add the delta flag and update the child count in the parent delta.
        delta.setChildCount(element.getChildren().length);
        delta.addNode(newChild, index, IModelDelta.INSERTED);
        
        return rootDelta;
    }    
    
    private TestElement[] doInsertElementInArray(TestElement[] children, int index, TestElement newChild) {
        // Create the new children array add the element to it and set it to 
        // the parent.
        TestElement[] newChildren = new TestElement[children.length + 1];
        System.arraycopy(children, 0, newChildren, 0, index);
        newChildren[index] = newChild;
        System.arraycopy(children, index, newChildren, index + 1, children.length - index);
        return newChildren;
    }
    
    public ModelDelta removeElementChild(TreePath parentPath, int index) {
        ModelDelta rootDelta = new ModelDelta(fInput, IModelDelta.NO_CHANGE);
        ModelDelta baseDelta = getBaseDelta(rootDelta);

        // Find the parent element and generate the delta node for it.
        TestElement element = getElement(parentPath);
        ModelDelta delta= getElementDelta(baseDelta, parentPath, false);
        
        // Create a new child array with the element removed
        TestElement[] children = element.getChildren();
        TestElement childToRemove = children[index];
        TestElement[] newChildren = new TestElement[children.length - 1];
        System.arraycopy(children, 0, newChildren, 0, index);
        System.arraycopy(children, index + 1, newChildren, index, children.length - index - 1);
        element.fChildren = newChildren;

        // Add the delta flag and update the child count in the parent delta.
        delta.setChildCount(element.getChildren().length);
        delta.addNode(childToRemove, index, IModelDelta.REMOVED);
        
        return rootDelta;
    }        

    public ModelDelta makeElementDelta(TreePath path, int flags) {
        ModelDelta rootDelta = new ModelDelta(fInput, IModelDelta.NO_CHANGE);
        ModelDelta baseDelta = getBaseDelta(rootDelta);

        // Find the element and generate the delta node for it.
        ModelDelta delta= getElementDelta(baseDelta, path, false);
        
        delta.setFlags(flags);
        return rootDelta;
    }        

    public TreePath findElement(String label) {
        return findElement(TreePath.EMPTY, label);
    }

    public TreePath findElement(TreePath startPath, String label) {
        TestElement element = getElement(startPath);
        for (int i = 0; i < element.getChildren().length; i++) {
            TestElement child = element.getChildren()[i];
            TreePath path = startPath.createChildPath(child);
            if ( label.equals(child.getLabel()) ) {
                return path;
            } else {
                TreePath subPath = findElement(path, label);
                if (subPath != null) {
                    return subPath;
                }
            }
        }
        return null;
    }
    
    public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
        rm.setData(new String[] { HEX_FORMAT, DECIMAL_FORMAT, OCTAL_FORMAT, BINARY_FORMAT, NATURAL_FORMAT });
        rm.done();
    }
    
    public void getFormattedExpressionValue(FormattedValueDMContext dmc, DataRequestMonitor<FormattedValueDMData> rm) {
        TestElement te = DMContexts.getAncestorOfType(dmc, TestElement.class);
        rm.setData(new FormattedValueDMData( getFormattedValueText(te, dmc.getFormatID())));
        rm.done();
        fListener.formattedValueUpdated(dmc);
    }
    
    public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String formatId) {
        // Creates a context that can be used to retrieve a formatted value.
        return new FormattedValueDMContext(this, dmc, formatId);
    }

    public String getFormattedValueText(TestElement te, String formatId) {
        return te.getLabel() + " (" + formatId + ")";
    }
    
    @Override
    public String toString() {
        return getElementString(fRoot, "");
    }
    
    public String getElementString(TestElement element, String indent) {
        StringBuffer builder = new StringBuffer();
        builder.append(indent); 
        builder.append(element.toString());
        builder.append('\n');
        TestElement[] children = element.getChildren();
        for (int i = 0; i < children.length; i++) {
            builder.append(getElementString(children[i], indent + "  "));
        }
        return builder.toString();
    }
    
    public static void simpleSingleLevel(TestModel model) {
        model.setRoot( new TestElement(model, "root", new TestElement[] {
            new TestElement(model, "1", true, true, new TestElement[0]),
            new TestElement(model, "2", true, false, new TestElement[0]),
            new TestElement(model, "3", false, true, new TestElement[0]),
            new TestElement(model, "4", false, false, new TestElement[0]),
            new TestElement(model, "5", new TestElement[0]),
            new TestElement(model, "6", new TestElement[0])
        }) );
    }
    
    public static void simpleMultiLevel(TestModel model) {
        model.setRoot( new TestElement(model, "root", new TestElement[] {
            new TestElement(model, "1", new TestElement[0]),
            new TestElement(model, "2", true, false, new TestElement[] {
                new TestElement(model, "2.1", true, true, new TestElement[0]),
                new TestElement(model, "2.2", false, true, new TestElement[0]),
                new TestElement(model, "2.3", true, false, new TestElement[0]),
            }),
            new TestElement(model, "3", new TestElement[] {
                new TestElement(model, "3.1", new TestElement[] {
                    new TestElement(model, "3.1.1", new TestElement[0]),
                    new TestElement(model, "3.1.2", new TestElement[0]),
                    new TestElement(model, "3.1.3", new TestElement[0]),
                }),
                new TestElement(model, "3.2", new TestElement[] {
                    new TestElement(model, "3.2.1", new TestElement[0]),
                    new TestElement(model, "3.2.2", new TestElement[0]),
                    new TestElement(model, "3.2.3", new TestElement[0]),
                }),
                new TestElement(model, "3.3", new TestElement[] {
                    new TestElement(model, "3.3.1", new TestElement[0]),
                    new TestElement(model, "3.3.2", new TestElement[0]),
                    new TestElement(model, "3.3.3", new TestElement[0]),
                }),
            })
        }) );
    }
    
}
