package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

public class ViewMemoryActionDelegate implements IObjectActionDelegate {

	private ICVariable[] variables;

	public ViewMemoryActionDelegate() {
		// TODO Auto-generated constructor stub
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		
	}

	private IMemoryBlockRetrieval getMemoryBlockRetrieval(Object object)
	{
		IMemoryBlockRetrieval retrieval = null;

		if (object instanceof IAdaptable)
		{
			IAdaptable adaptable = (IAdaptable)object;
			retrieval = (IMemoryBlockRetrieval)adaptable.getAdapter(IMemoryBlockRetrieval.class);
		}
		
		if (retrieval == null && object instanceof IDebugElement)
		{
			IDebugElement de = (IDebugElement)object;
			retrieval = de.getDebugTarget();
		}
		
		return retrieval;
	}

	/**
	 * @param memoryBlock
	 * @param primaryType
	 * @throws CoreException
	 */
	private void createRenderingInContainer(IMemoryBlock memoryBlock, IMemoryRenderingType primaryType, String paneId) throws CoreException {

		IWorkbenchPage page = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart newView = page.showView(IDebugUIConstants.ID_MEMORY_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);
		
		IMemoryRenderingSite memSite = (IMemoryRenderingSite) newView.getSite();

		IMemoryRendering rendering = primaryType.createRendering();
		IMemoryRenderingContainer container = memSite.getContainer(paneId);
		rendering.init(container, memoryBlock);
		container.addMemoryRendering(rendering);
	}

	private void addDefaultRenderings(IMemoryBlock memoryBlock)
	{
		IMemoryRenderingType primaryType = DebugUITools.getMemoryRenderingManager().getPrimaryRenderingType(memoryBlock);
		IMemoryRenderingType renderingTypes[] = DebugUITools.getMemoryRenderingManager().getDefaultRenderingTypes(memoryBlock);
		
		// create primary rendering
		try {
			if (primaryType != null)
			{
				createRenderingInContainer(memoryBlock, primaryType, IDebugUIConstants.ID_RENDERING_VIEW_PANE_1);
			}
			else if (renderingTypes.length > 0)
			{
				primaryType = renderingTypes[0];
				createRenderingInContainer(memoryBlock, renderingTypes[0], IDebugUIConstants.ID_RENDERING_VIEW_PANE_1);
			}
		} catch (CoreException e1) {
			DebugUIPlugin.log(e1);	
		}
		
		for (int i = 0; i<renderingTypes.length; i++)
		{
			try {
				boolean create = true;
				if (primaryType != null)
				{
					if (primaryType.getId().equals(renderingTypes[i].getId()))
						create = false;
				}
				if (create)
					createRenderingInContainer(memoryBlock, renderingTypes[i], IDebugUIConstants.ID_RENDERING_VIEW_PANE_2);
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
	}

	private void addMemoryBlock() throws DebugException
	{					// if the debug session supports IMemoryBlockExtensionRetrieval

		Object elem = DebugUITools.getDebugContext();
		
		IMemoryBlockRetrieval retrieval = getMemoryBlockRetrieval(elem);
		
		if (retrieval == null)
			return;

		IMemoryBlockRetrievalExtension memRetrieval = (IMemoryBlockRetrievalExtension)retrieval;
		
		// get extended memory block with the expression entered
		IMemoryBlockExtension memBlock = memRetrieval.getExtendedMemoryBlock("0x12345678", elem);
		
		// add block to memory block manager
		if (memBlock != null)
		{
			IMemoryBlock[] memArray = new IMemoryBlock[]{memBlock};
			
			DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks(memArray);
			addDefaultRenderings(memBlock);
		}
		else
		{
			// open error if it failed to retrieve a memory block
			MemoryViewUtil.openError(DebugUIMessages.AddMemoryBlockAction_title, DebugUIMessages.AddMemoryBlockAction_noMemoryBlock, null);
		}
}
	
	public void run(IAction action) {
		ICVariable[] vars = getVariables();
		if ( vars != null && vars.length > 0 ) {
		
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if ( selection instanceof IStructuredSelection ) {
			List list = new ArrayList();
			IStructuredSelection ssel = (IStructuredSelection)selection;
			Iterator i = ssel.iterator();
			while( i.hasNext() ) {
				Object o = i.next();
				if ( o instanceof ICVariable ) {
					ICVariable var = (ICVariable)o;
					boolean enabled = true;
					action.setEnabled( enabled );
					if ( enabled ) {
						list.add( o );
					}
				}
			}
			setVariables( (ICVariable[])list.toArray( new ICVariable[list.size()] ) );
		}
		else {
			action.setChecked( false );
			action.setEnabled( false );
		}
	}

	protected ICVariable[] getVariables() {
		return variables;
	}

	private void setVariables( ICVariable[] variables ) {
		this.variables = variables;
	}

}
