package org.eclipse.dd.dsf.ui.viewmodel;

import org.eclipse.dd.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;

/**
 * Implementation of basic view model context interface.  The main
 * purpose of the VMC wrapper is to re-direct adapter queries to the IVMAdapter
 * and the layout node that the given context was created by.
 * <p/> 
 * Note: Deriving classes must override the Object.equals/hashCode methods.
 * This is because the view model context objects are just wrappers that are 
 * created by the view model on demand, so the equals methods must use the 
 * object being wrapped to perform a meaningful comparison.    
 */
@SuppressWarnings("restriction")
abstract public class AbstractVMContext implements IVMContext {
    protected final IVMAdapter fVMAdapter;
    protected final IVMNode fNode;
    
    public AbstractVMContext(IVMAdapter adapter, IVMNode node) {
        fVMAdapter = adapter;
        fNode = node;
    }
    
    public IVMNode getVMNode() { return fNode; }

    /**
     * IAdapter implementation returns the {@link IVMAdapter} instance for 
     * the interfaces that are actually implemented by the VM Adapter.  
     * These should at least include {@link IElementContentProvider}, 
     * {@link IModelProxyFactory}, and {@link IColumnPresentationFactory}.  
     * It also returns the {@link IVMNode} instance for adapters implemented 
     * by the context's node.  The interfaces typically implemented by the 
     * node include {@link IElementLabelProvider} and {@link IElementPropertiesProvider}.
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isInstance(fVMAdapter)) {
            return fVMAdapter;
        } else if (adapter.isInstance(fNode)) {
            return fNode;
        }
        return null;
    }

    /** Deriving classes must override. */
    @Override
    abstract public boolean equals(Object obj);
    
    /** Deriving classes must override. */
    @Override
    abstract public int hashCode();
}