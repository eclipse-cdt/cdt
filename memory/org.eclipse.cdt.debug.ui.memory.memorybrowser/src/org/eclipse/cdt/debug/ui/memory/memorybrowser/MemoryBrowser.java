/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *     Ted R Williams (Mentor Graphics, Inc.) - address space enhancements
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.memorybrowser;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.provisional.IMemoryRenderingViewportProvider;
import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval;
import org.eclipse.cdt.debug.internal.core.CRequest;
import org.eclipse.cdt.debug.ui.provisional.IRepositionableMemoryRendering2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.memory.MemoryRenderingManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingSynchronizationService;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A lightweight rendering container.
 * 
 * Debug model requirements:
 * 		IMemoryBlockExtension (IMemoryBlock not supported)
 * 		IMemoryBlockRetrievalExtension
 * <p>
 * Rendering requirements:
 * 		IRepositionableMemoryRendering
 * 
 */

@SuppressWarnings("restriction")
public class MemoryBrowser extends ViewPart implements IDebugContextListener, IMemoryRenderingSite, IDebugEventSetListener
{
	public static final String ID = "org.eclipse.cdt.debug.ui.memory.memorybrowser.MemoryBrowser";  //$NON-NLS-1$
	
	protected StackLayout fStackLayout;
	private Composite fRenderingsComposite;
	private GoToAddressBarWidget fGotoAddressBar;
	private Control fGotoAddressBarControl;
	private Combo fGotoMemorySpaceControl;
	private Label fUnsupportedLabel;
	private Composite fMainComposite;
	private String defaultRenderingTypeId = null;
	private IMemoryRendering fActiveRendering;
	
	/**
	 * Every memory retrieval object is given its own tab folder. Typically all
	 * elements of a "process" (process, threads, frames) have the same
	 * retrieval object.
	 */
	private Map<IMemoryBlockRetrieval,CTabFolder> fContextFolders = new HashMap<IMemoryBlockRetrieval,CTabFolder> ();
	
	private List<IMemoryRenderingContainer> fCurrentContainers = new ArrayList<IMemoryRenderingContainer>();
	
	private final static String KEY_CONTEXT      = "CONTEXT";   //$NON-NLS-1$
	private final static String KEY_CONTAINER    = "CONTAINER"; //$NON-NLS-1$
	private final static String KEY_RENDERING_TYPE    = "RENDERING_TYPE"; //$NON-NLS-1$

	/**
	 * Property we attach to a CTabItem to track the retrieval object we use to
	 * create memory blocks on the tab's behalf. Value is an
	 * {@link IMemoryBlockRetrieval}
	 */
	private final static String KEY_RETRIEVAL    = "RETRIEVAL"; //$NON-NLS-1$

	/**
	 * Property we attach to a CTabItem to track the memory space it's
	 * associated with. Value is a memory space ID (String), or null if n/a
	 */
	private final static String KEY_MEMORY_SPACE = "MEMORY_SPACE"; //$NON-NLS-1$

	/**
	 * Property we attach to a CTabItem to track what renderings have been
	 * created on its behalf. There will be more than one rendering if the
	 * backend supports memory spaces, there is more than one such space, and
	 * the user has viewed memory in multiple memory spaces within that tab.
	 * The value is a map of memory-space-ID==>IMemoryRendering.  
	 */
	private final static String KEY_RENDERINGS    = "RENDERINGS"; //$NON-NLS-1$

	/**
	 * Property we attach to a CTabItem to track the active rendering in the
	 * tab. The value is an IMemoryRendering.
	 */
	private final static String KEY_RENDERING    = "RENDERING"; //$NON-NLS-1$

	/**
	 * Property we attach to a CTabItem to track what memory blocks have been
	 * created on its behalf. There can be multiple when dealing with memory
	 * spaces, for the same reasons there can be multiple renderings. There is a
	 * 1:1:1 association between rendering, block and memory space. The value is
	 * a list of IMemoryBlockExtension
	 */
	private final static String KEY_MEMORY_BLOCKS = "MEMORY_BLOCKS"; //$NON-NLS-1$
	

	public static final String PREF_DEFAULT_RENDERING = "org.eclipse.cdt.debug.ui.memory.memorybrowser.defaultRendering";  //$NON-NLS-1$
	
	/**
	 * The text we use in the combobox to represent no memory space specification
	 */
	private static final String NA_MEMORY_SPACE_ID = "   -----";

	public MemoryBrowser() {
	}
	
	public Control getControl() {
		return fMainComposite;
	}

	@Override
	public void createPartControl(Composite parent) {
		// set default rendering type. use the traditional rendering if available. fallback on first registered type.
		// this should eventually be configurable via a preference page.
		boolean isDefaultRenderingAvailable = false;
		IPreferenceStore store = MemoryBrowserPlugin.getDefault().getPreferenceStore();
		String defaultRendering = store.getString(PREF_DEFAULT_RENDERING);
		if(defaultRendering == null || defaultRendering.trim().length() == 0)
		{
			defaultRendering = "org.eclipse.cdt.debug.ui.memory.traditional.TraditionalRendering"; //$NON-NLS-1$
		}

		IMemoryRenderingType[] types = getRenderingTypes();
		for(final IMemoryRenderingType type : types)
		{
			if(type.getId().equals(defaultRendering))
			{
				isDefaultRenderingAvailable = true;
				break;
			}
		}
		if(isDefaultRenderingAvailable)
			defaultRenderingTypeId = defaultRendering;
		else if(types.length > 0)
			defaultRenderingTypeId = types[0].getId();
		
		getSite().setSelectionProvider(new SelectionProviderAdapter());
		
		fMainComposite = new Composite(parent, SWT.NONE);
		
		FormLayout layout = new FormLayout();
		layout.spacing = 0;
		fMainComposite.setLayout(layout);
		
		fGotoMemorySpaceControl = new Combo(fMainComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		fGotoAddressBar = new GoToAddressBarWidget();
		fGotoAddressBarControl = fGotoAddressBar.createControl(fMainComposite);
		
		fGotoAddressBar.getButton(IDialogConstants.OK_ID).addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				performGo(false);
			}
		});
		
		fGotoAddressBar.getButton(GoToAddressBarWidget.ID_GO_NEW_TAB).addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				performGo(true);
			}
		});
		
		fGotoAddressBar.getExpressionWidget().addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {}
			public void widgetDefaultSelected(SelectionEvent e) {
				performGo(false);
			}
		});
		
		
		FormData data = new FormData();
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(fGotoMemorySpaceControl);
		data.right = new FormAttachment(100);
		fGotoAddressBarControl.setLayoutData(data);
		
		fRenderingsComposite = new Composite(fMainComposite, SWT.NONE);
		data = new FormData();
		data.top = new FormAttachment(fGotoAddressBarControl);
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.bottom = new FormAttachment(100);
		fRenderingsComposite.setLayoutData(data);
		
		fStackLayout = new StackLayout();
	
		fRenderingsComposite.setLayout(fStackLayout);
		
		fUnsupportedLabel = new Label(fRenderingsComposite, SWT.NONE);
		fUnsupportedLabel.setText("");  //$NON-NLS-1$
		
		handleUnsupportedSelection();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(fMainComposite, MemoryBrowserPlugin.PLUGIN_ID);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		
		Object selection = null;
        IDebugContextService contextService = 
            DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()); 
		if (isBug145635Patched()) {
		    String presentationContextId = getPresentationContextId();
            contextService.addDebugContextListener(this, presentationContextId); 
            selection = contextService.getActiveContext(presentationContextId);
		} else {
		    contextService.addDebugContextListener(this); 
            selection = contextService.getActiveContext();
		}
		
		DebugPlugin.getDefault().addDebugEventListener(this);
		
		if(selection instanceof StructuredSelection)
			handleDebugContextChanged(((StructuredSelection) selection).getFirstElement());
	}

    private boolean isBug145635Patched() {
        Type[] managerTypes = DebugUITools.getDebugContextManager().getClass().getGenericInterfaces();
        for (int i = 0; i < managerTypes.length; i++) {
            if (managerTypes[i] instanceof Class<?>) {
                Class<?> clazz = (Class<?>)managerTypes[i];
                if ("org.eclipse.debug.ui.contexts.IBug145635Marker".equals(clazz.getName()) ) { //$NON-NLS-1$
                    return true;
                }
            }
        }
        return false;
    }
	
	/**
     * Returns the presentation context id for this view.  Used to support the 
     * pin and clone feature patch from bug 145635. 
     * 
     * @return context id
     */
    private String getPresentationContextId() {
        IViewSite site = (IViewSite)getSite(); 
        return site.getId() + (site.getSecondaryId() != null ? (":" + site.getSecondaryId()) : ""); //$NON-NLS-1$ //$NON-NLS-2$
    }
	
	@Override
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
        IDebugContextService contextService = 
            DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()); 
        if (isBug145635Patched()) {
            String presentationContextId = getPresentationContextId();
            contextService.removeDebugContextListener(this, presentationContextId); 
        } else {
            contextService.removeDebugContextListener(this); 
        }
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent event: events) {
			Object source = event.getSource();
			if (event.getKind() == DebugEvent.TERMINATE && source instanceof IMemoryBlockRetrieval) {
				releaseTabFolder((IMemoryBlockRetrieval)source);
			}
		}
	}

	public IMemoryRenderingContainer getContainer(String id) {
		return null;
	}

	public IMemoryRenderingContainer[] getMemoryRenderingContainers() {
		IMemoryRenderingContainer[] containerList = new IMemoryRenderingContainer[fCurrentContainers.size()];
		for ( int idx = 0 ; idx < fCurrentContainers.size() ; idx ++ ) {
			containerList[ idx ] = fCurrentContainers.get( idx );
		}
		return containerList;
	}
	
	public IMemoryRenderingSynchronizationService getSynchronizationService() {
		return null;
	}
	
	private void handleUnsupportedSelection() {
		fStackLayout.topControl = fUnsupportedLabel;
		fGotoAddressBarControl.setVisible(false);
		fGotoMemorySpaceControl.setVisible(false);
	}
	
	private void performGo(boolean inNewTab) {
		// Index zero is the 'auto' (n/a) memory space entry, unless the backend
		// said we need to force a memory space selection
		String memorySpace = null;
		if (fGotoMemorySpaceControl.isVisible()) {
			memorySpace = fGotoMemorySpaceControl.getText();
			if (memorySpace.equals(NA_MEMORY_SPACE_ID)) {
				memorySpace = null;
			}
			assert (memorySpace == null) || (memorySpace.length() > 0);
		}
		
		String expression = fGotoAddressBar.getExpressionText();
		if (expression.length() > 0) {
			performGo(inNewTab, fGotoAddressBar.getExpressionText(), memorySpace);	
		}
	}
	
	public void performGo(boolean inNewTab, final String expression, final String memorySpaceId) {
		final CTabFolder activeFolder = (CTabFolder) fStackLayout.topControl;
		if (activeFolder != null) {	
			final IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval) activeFolder.getData(KEY_RETRIEVAL);
			final Object context = activeFolder.getData(KEY_CONTEXT);
			IMemoryRendering rendering = null;
			
			CTabItem item = activeFolder.getSelection();
			if (inNewTab || item == null)
			{
				try {
					item = createTab(activeFolder, activeFolder.getSelectionIndex() + 1);
					rendering = populateTabWithRendering(item, retrieval, context, memorySpaceId, expression);
					fContextFolders.put(retrieval, activeFolder);
					activeFolder.setSelection(item);
					getSite().getSelectionProvider().setSelection(new StructuredSelection(item.getData(KEY_RENDERING)));
					handleTabActivated(item);
				} catch (DebugException e1) {
					fGotoAddressBar.handleExpressionStatus(new Status(Status.ERROR, MemoryBrowserPlugin.PLUGIN_ID, 
							Messages.getString("MemoryBrowser.FailedToGoToAddressTitle"), e1));
					item.dispose();
					return;
				} 
			} else {
				// Tab is already in place. However, the user may have selected
				// a different memory space. If so, that requires us to switch
				// out the rendering in the tab with either a new one or an
				// existing one already associated with that memory space.
				String oldMemorySpaceId = (String)activeFolder.getSelection().getData(KEY_MEMORY_SPACE);
				assert oldMemorySpaceId == null || !oldMemorySpaceId.equals(NA_MEMORY_SPACE_ID) : "should be null reference or an explicit, valid memory space ID (not including '----')";
				if ((oldMemorySpaceId != null && !oldMemorySpaceId.equals(memorySpaceId)) 
						|| (oldMemorySpaceId == null && memorySpaceId != null)) {
					try {
						rendering = populateTabWithRendering(item, retrieval, context, memorySpaceId, expression);
						activeFolder.setSelection(item);
						getSite().getSelectionProvider().setSelection(new StructuredSelection(item.getData(KEY_RENDERING)));
						handleTabActivated(item);
					} catch (DebugException e) {
						fGotoAddressBar.handleExpressionStatus(new Status(Status.ERROR, MemoryBrowserPlugin.PLUGIN_ID, 
								Messages.getString("MemoryBrowser.FailedToGoToAddressTitle"), e));
						return;
					}
				}
			}
			
			if (rendering == null) {
				rendering = (IRepositionableMemoryRendering) activeFolder.getSelection().getData(KEY_RENDERING);
			}
			
			if (retrieval instanceof IMemoryBlockRetrievalExtension && 
					rendering instanceof IRepositionableMemoryRendering) {
				final IRepositionableMemoryRendering renderingFinal = (IRepositionableMemoryRendering) rendering;
				new Thread() {
					@Override
					public void run() {
						try {
							BigInteger newBase = getExpressionAddress(retrieval, expression, context, memorySpaceId);
							IMemoryBlockExtension block = (IMemoryBlockExtension) renderingFinal.getMemoryBlock();
							if (block.supportBaseAddressModification()) {
								block.setBaseAddress(newBase);
							}
							if(renderingFinal instanceof IRepositionableMemoryRendering2) {
								((IRepositionableMemoryRendering2)renderingFinal).goToAddress(newBase, expression);
							}
							else {
								renderingFinal.goToAddress(newBase);
							}
							fGotoAddressBar.handleExpressionStatus(Status.OK_STATUS);
							runOnUIThread(new Runnable(){
								public void run() {
									updateLabel(activeFolder.getSelection(), renderingFinal);
								}
							});
						} catch (final DebugException e1) {
							// widgets update require Display 
							runOnUIThread(new Runnable(){
								public void run() {
									fGotoAddressBar.handleExpressionStatus(new Status(Status.ERROR, MemoryBrowserPlugin.PLUGIN_ID, 
											Messages.getString("MemoryBrowser.FailedToGoToAddressTitle"), e1));
								}
							});
						}
					}
				}.start();
			}
		}
	}
	
	private void updateLabel(CTabItem tab, IMemoryRendering rendering) {
		// The default is to use the label provided by the base rendering
		// interface.
		String label = rendering.getLabel();
		
		// If the rendering provides access to its viewport address (the first
		// address shown in the rendering, subject to scrolling), display that
		// in the tab rather than the expression that was used when the tab was
		// first created.
		if (rendering instanceof IMemoryRenderingViewportProvider) { 
			BigInteger viewportAddress = ((IMemoryRenderingViewportProvider)rendering).getViewportAddress();
			
			// The base label generation puts the rendering type name in "<>" and
			// appends it to the label. Fish that out
			String renderingType = null;
			int i = label.indexOf('<');
			if (i >= 0) {
				renderingType = label.substring(i);
			}

			label = null;

			// If a memory space is involved, we want to include its ID in the label 
			String memorySpaceID = (String)tab.getData(KEY_MEMORY_SPACE);
			if (memorySpaceID != null) {
				IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval) tab.getParent().getData(KEY_RETRIEVAL);
				if (retrieval instanceof IMemorySpaceAwareMemoryBlockRetrieval) {
					label = ((IMemorySpaceAwareMemoryBlockRetrieval)retrieval).encodeAddress("0x" + viewportAddress.toString(16), memorySpaceID) + ' ' + renderingType;
				}
			}
			if (label == null) {
				label = "0x" + viewportAddress.toString(16) + ' ' + renderingType;
			}
			
			// Allow the memory block to customize the label. The platform's
			// Memory view support this (it was done in the call to
			// rendering.getLabel() above)
			IMemoryBlock block = rendering.getMemoryBlock();
			ILabelDecorator labelDec = (ILabelDecorator)block.getAdapter(ILabelDecorator.class);
			if (labelDec != null) {
				String newLabel = labelDec.decorateText(label, rendering);
				if (newLabel != null) {
					label = newLabel;
				}
			}
		}
			
		tab.setText(label);
	}
	
	private CTabFolder createTabFolder(Composite parent)
	{
		final CTabFolder folder = new CTabFolder(parent, SWT.NO_REDRAW_RESIZE | SWT.NO_TRIM | SWT.FLAT);
		
		ColorRegistry reg = JFaceResources.getColorRegistry();
		Color c1 = reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_START"), //$NON-NLS-1$
			  c2 = reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_END"); //$NON-NLS-1$
		folder.setSelectionBackground(new Color[] {c1, c2},	new int[] {100}, true);
		folder.setSelectionForeground(reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_TEXT_COLOR")); //$NON-NLS-1$
		folder.setSimple(PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
		folder.setBorderVisible(true);
		
		// listener to dispose rendering resources for each closed tab
		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			@Override
			public void close(CTabFolderEvent event) {
				event.doit = true;
				CTabItem item = (CTabItem) event.item;
				disposeTab(item);
			}
		});
		
		// listener to dispose rendering resources for all tab items when view part is closed 
		folder.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				for(CTabItem tab : folder.getItems()) {
					disposeTab(tab);
				}
				folder.removeDisposeListener(this);
			}
		});
		return folder;
	}
	
	// these utility methods allow us restrict the scope of the unavoidable @SuppressWarnings
	
	@SuppressWarnings("unchecked")
	private static Map<String, IMemoryRendering> getRenderings(CTabItem tabItem) {
		return (Map<String, IMemoryRendering>)tabItem.getData(KEY_RENDERINGS); 
	}

	@SuppressWarnings("unchecked")
	private static List<IMemoryBlockExtension> getMemoryBlocks(CTabItem tabItem) {
		return (List<IMemoryBlockExtension>)tabItem.getData(KEY_MEMORY_BLOCKS); 
	}


	/**
	 * dispose rendering resources associated with the tab item
	 * @param item
	 */
	private void disposeTab(CTabItem item )  {
		if (item.isDisposed())
			return;
		
		IMemoryRenderingContainer container = (IMemoryRenderingContainer) item.getData(KEY_CONTAINER);
		fCurrentContainers.remove( container );
		Map<String, IMemoryRendering> map = getRenderings(item);
		Collection<IMemoryRendering> renderings = map.values();
		for (IMemoryRendering rendering : renderings) {
			// always deactivate rendering before disposing it.
			rendering.deactivated();
			rendering.dispose();
			if (rendering == fActiveRendering) {
				fActiveRendering = null;
			}
		}
		map.clear();
		
		List<IMemoryBlockExtension> blocks = getMemoryBlocks(item);
		for (IMemoryBlockExtension block : blocks) {
			try {
				block.dispose();
			} catch (DebugException e) {
				MemoryBrowserPlugin.getDefault().getLog().log(new Status(Status.ERROR, MemoryBrowserPlugin.PLUGIN_ID, "Could not dispose memory block", e)); //$NON-NLS-1$
			}
		}		
		blocks.clear();
	}
	
	private CTabItem createTab(CTabFolder tabFolder, int index) {
		int swtStyle = SWT.CLOSE;
		CTabItem tab = new CTabItem(tabFolder, swtStyle, index);
		tab.setData(KEY_RENDERINGS, new HashMap<String, IMemoryRendering>());
		tab.setData(KEY_MEMORY_BLOCKS, new ArrayList<IMemoryBlock>());
		return tab;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MemoryBrowser.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(getControl());
		getControl().setMenu(menu);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		
		
		MenuManager sub = new MenuManager(Messages.getString("MemoryBrowser.DefaultRendering")); //$NON-NLS-1$
		
		for(final IMemoryRenderingType type : getRenderingTypes())
		{
			final Action action = new Action(
				type.getLabel(), IAction.AS_RADIO_BUTTON)
	        {
	            @Override
				public void run()
	            {
	            	setDefaultRenderingTypeId(type.getId());
	            }
	        };
	        action.setChecked(type.getId().equals(getDefaultRenderingTypeId()));
	        sub.add(action);
		}
		
	    manager.add(sub);
	    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void makeActions() {

	}
	
	private IMemoryRenderingType[] getRenderingTypes()
	{
		 return MemoryRenderingManager.getDefault().getRenderingTypes(new IMemoryBlockExtension(){
			public void connect(Object client) {}
			public void disconnect(Object client) {}
			public void dispose() throws DebugException {}
			public int getAddressSize() throws DebugException { return 0; }
			public int getAddressableSize() throws DebugException { return 0; }
			public BigInteger getBigBaseAddress() throws DebugException { return null; }
			public BigInteger getBigLength() throws DebugException { return null; }
			public MemoryByte[] getBytesFromAddress(BigInteger address, long units) throws DebugException { return null; }
			public MemoryByte[] getBytesFromOffset(BigInteger unitOffset, long addressableUnits) throws DebugException { return null; }
			public Object[] getConnections() { return null; }
			public String getExpression() { return null; }
			public BigInteger getMemoryBlockEndAddress() throws DebugException { return null; }
			public IMemoryBlockRetrieval getMemoryBlockRetrieval() { return null; }
			public BigInteger getMemoryBlockStartAddress() throws DebugException { return null; }
			public void setBaseAddress(BigInteger address) throws DebugException {}
			public void setValue(BigInteger offset, byte[] bytes) throws DebugException {}
			public boolean supportBaseAddressModification() throws DebugException { return false; }
			public boolean supportsChangeManagement() { return false; }
			public byte[] getBytes() throws DebugException { return null; }
			public long getLength() { return 0; }
			public long getStartAddress() { return 0; }
			public void setValue(long offset, byte[] bytes) throws DebugException {}
			public boolean supportsValueModification() { return false; }
			public IDebugTarget getDebugTarget() { return null; }
			public ILaunch getLaunch() { return null; }
			public String getModelIdentifier() { return null; }
			@SuppressWarnings("rawtypes")
			public Object getAdapter(Class adapter) { return null; }
		});
	}

	@Override
	public void setFocus() {
		getControl().setFocus();
	}
	
	public void debugContextChanged(DebugContextEvent event) {
		handleDebugContextChanged(((StructuredSelection) event.getContext()).getFirstElement());
	}

	private final class MemoryBrowserRenderingContainer implements
			IMemoryRenderingContainer {
		private final List<IMemoryRendering> renderings = new ArrayList<IMemoryRendering>();

		private MemoryBrowserRenderingContainer() {
		}

		public void addMemoryRendering(IMemoryRendering rendering) {
			// do not allow duplicated objects
			if (!renderings.contains(rendering)) {
				renderings.add(rendering);
			}
		}

		public IMemoryRendering getActiveRendering() {
			return renderings.isEmpty() ? null : renderings.get(renderings.size() -1);
		}

		public String getId() {
			return "???"; //$NON-NLS-1$
		}

		public String getLabel() {
			IMemoryRendering rendering = getActiveRendering();
			return rendering != null ? rendering.getLabel() : null;
		}

		public IMemoryRenderingSite getMemoryRenderingSite() {
			return MemoryBrowser.this;
		}

		public IMemoryRendering[] getRenderings() {
			return renderings.toArray(new IMemoryRendering[renderings.size()]);
		}

		public void removeMemoryRendering(IMemoryRendering rendering) {
			renderings.remove(rendering);			
		}
	}

	private final class RenderingPropertyChangeListener implements
			IPropertyChangeListener {
		private final CTabItem tab;
		private final IMemoryRendering newRendering;

		private RenderingPropertyChangeListener(CTabItem tab,
				IMemoryRendering newRendering) {
			this.tab = tab;
			this.newRendering = newRendering;
		}

		public void propertyChange(final PropertyChangeEvent event) {
			WorkbenchJob job = new WorkbenchJob("MemoryBrowser PropertyChanged") { //$NON-NLS-1$
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if(tab.isDisposed())
						return Status.OK_STATUS;
						
					if (event.getProperty().equals(IBasicPropertyConstants.P_TEXT))
						updateLabel(tab, newRendering);
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
	}

	private class GetMemorySpacesRequest extends CRequest implements IMemorySpaceAwareMemoryBlockRetrieval.GetMemorySpacesRequest  {
		String [] fMemorySpaces;
		public String[] getMemorySpaces() {
			return fMemorySpaces;
		}
		public void setMemorySpaces(String[] memorySpaceIds) {
			fMemorySpaces = memorySpaceIds;
		}
	}
	
	public void handleDebugContextChanged(final Object context) {
		if(defaultRenderingTypeId == null)
			return;
	
        IAdaptable adaptable = null;
        IMemoryBlockRetrieval retrieval = null;
        ILaunch launch  = null;

		if(context instanceof IAdaptable)
		{
			adaptable = (IAdaptable) context;
			retrieval = ((IMemoryBlockRetrieval) adaptable.getAdapter(IMemoryBlockRetrieval.class));
			launch  = ((ILaunch) adaptable.getAdapter(ILaunch.class));
		}
		
		if(retrieval != null && launch != null && !launch.isTerminated()) {
			if (retrieval instanceof IMemorySpaceAwareMemoryBlockRetrieval) {
			    final IMemoryBlockRetrieval _retrieval = retrieval;
				((IMemorySpaceAwareMemoryBlockRetrieval)retrieval).getMemorySpaces(context, new GetMemorySpacesRequest(){
					@Override
					public void done() {
						updateTab(_retrieval, context, isSuccess() ? getMemorySpaces() : new String[0]);
					}
				}); 
			}
			else {
				updateTab(retrieval, context, new String[0]);
			}
		}
		else {
			handleUnsupportedSelection();
		}

		fGotoMemorySpaceControl.pack(true);
		fStackLayout.topControl.getParent().layout(true);
	}

	/**
	 * Called to update the tab once the asynchronous query for memory spaces
	 * has returned a result.
	 * 
	 * @param retrieval
	 *            the retrieval object associated with the newly active debug
	 *            context
	 * @param context
	 *            the newly active context
	 * @param memorySpaces
	 *            the memory spaces, if applicable. Otherwise an empty array.
	 */
	private void updateTab(final IMemoryBlockRetrieval retrieval, final Object context, final String[] memorySpaces) {
		// GUI activity must be on the main thread
		runOnUIThread(new Runnable(){
			public void run() {
				if (fGotoAddressBarControl.isDisposed()) {
					return;
				}
				
				fGotoAddressBarControl.setVisible(true);
				
				// If we've already created a tab folder for this retrieval
				// object, bring it to the forefront. Otherwise create the
				// folder.
				CTabFolder tabFolder = fContextFolders.get(retrieval);
				if(tabFolder != null) {
					fStackLayout.topControl = tabFolder;
					handleTabActivated(tabFolder.getSelection());
				}
				else {
					tabFolder = createTabFolder(fRenderingsComposite);
					tabFolder.addSelectionListener(new SelectionListener() {
						public void widgetDefaultSelected(SelectionEvent e) {}
						public void widgetSelected(SelectionEvent e) {
							CTabItem tabItem = (CTabItem)e.item;
							updateMemorySpaceControlSelection(tabItem);
							getSite().getSelectionProvider().setSelection(new StructuredSelection(tabItem.getData(KEY_RENDERING)));
							handleTabActivated(tabItem);
						}
					});
					
					tabFolder.setData(KEY_RETRIEVAL, retrieval);
					
					fContextFolders.put(retrieval, tabFolder);
					fStackLayout.topControl = tabFolder;
				}
				// update debug context to the new selection
				tabFolder.setData(KEY_CONTEXT, context);
				
				
				final CTabFolder activeFolder = tabFolder;
				if (!activeFolder.equals(tabFolder)) {
					return;
				}
				
				// Don't expose the memory spaces widget if there are none or
				// only one memory space involved.
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=309032#c50
				if (memorySpaces.length >= 2) {
					fGotoMemorySpaceControl.setItems(memorySpaces);
					
					// Add the '----' (N/A) entry unless the retrieval object
					// says it requires a memory space ID in all cases
					boolean addNA = true;	
					if (retrieval instanceof IMemorySpaceAwareMemoryBlockRetrieval) {
						addNA = !((IMemorySpaceAwareMemoryBlockRetrieval)retrieval).creatingBlockRequiresMemorySpaceID();
					}
					if (addNA) {
						fGotoMemorySpaceControl.add(NA_MEMORY_SPACE_ID, 0); 
					}
					setMemorySpaceControlVisible(true);
				}
				else {
					fGotoMemorySpaceControl.setItems(new String[0]);
					setMemorySpaceControlVisible(false);					
				}

				updateMemorySpaceControlSelection(activeFolder.getSelection());
				
				fStackLayout.topControl.getParent().layout(true);
			}
		});
	}
	
	protected final void handleTabActivated(CTabItem item) {
		if (item != null) {
			updateActiveRendering((IMemoryRendering) item.getData(KEY_RENDERING));
		}
	}
	
	private void updateActiveRendering(IMemoryRendering rendering) {
		if (fActiveRendering == rendering) {
			return;
		}
		if (fActiveRendering != null) {
			fActiveRendering.deactivated();
			fActiveRendering.becomesHidden();
		}
		if (rendering != null) {
			rendering.activated();
			rendering.becomesVisible();
		}
		fActiveRendering = rendering;
	}

	private void setMemorySpaceControlVisible(boolean visible) {
		FormData data = (FormData)fGotoAddressBarControl.getLayoutData();
		if (visible) {
			data.left = new FormAttachment(fGotoMemorySpaceControl);
		}
		else {
			data.left = new FormAttachment(0);
		}
		fGotoMemorySpaceControl.setVisible(visible);

	}

	/**
	 * Update the selection in the memory space combobox to reflect the memory
	 * space being shown in the given tab
	 * 
	 * @param item
	 *            the active tab; may be null if in a "fresh" memory browser instance
	 */
	private void updateMemorySpaceControlSelection(CTabItem item) {
		String[] memorySpaces = fGotoMemorySpaceControl.getItems();
		if (memorySpaces.length > 0 ) {
			// Don't assume that the memory space previously set in the tab
			// is one of the ones now available. If it isn't, then select
			// the first available one and update the tab data 
			boolean foundIt = false;
			if (item != null) {
				String currentMemorySpace = (String) item.getData(KEY_MEMORY_SPACE);
				if (currentMemorySpace != null)			{
					assert currentMemorySpace.length() > 0;
					for (String memorySpace : memorySpaces) {
						if (memorySpace.equals(currentMemorySpace)) {
							foundIt = true;
							fGotoMemorySpaceControl.setText(currentMemorySpace);
							break;
						}
					}
				}
			}
			if (!foundIt) {
				fGotoMemorySpaceControl.select(0);
				if (item != null) {
					item.setData(KEY_MEMORY_SPACE, null);
				}
			}
			fGotoMemorySpaceControl.setVisible(true);
		}
		else {
			fGotoMemorySpaceControl.setVisible(false);
		}
		fGotoMemorySpaceControl.getParent().layout(true);
		
	}
	
	private String getDefaultRenderingTypeId()
	{
		return defaultRenderingTypeId;
	}
	
	public void setDefaultRenderingTypeId(String id)
	{
		defaultRenderingTypeId = id;
		IPreferenceStore store = MemoryBrowserPlugin.getDefault().getPreferenceStore();
		store.setValue(PREF_DEFAULT_RENDERING, defaultRenderingTypeId);
	}
	
	/**
	 * Populate given tab with a rendering positioned at specified expression and memory space.
	 * Will create a new rendering if one does not exist for the given memory space
	 * 
	 * @param tab item to populate  
	 * @param retrieval memory service to retrieve memory block from
	 * @param context memory block would be retrieved
	 * @param memorySpaceId of the expression or null if not supported
	 * @param expression from where to retrieve the memory block
	 * @return return the memory rendering or null if could not be created
	 * 
	 * @throws DebugException if could not retrieve memory block (e.g. invalid expression)
	 */
	private IMemoryRendering populateTabWithRendering(final CTabItem tab, final IMemoryBlockRetrieval retrieval, Object context, String memorySpaceId, String expression) throws DebugException {
		IMemoryRenderingType type = DebugUITools.getMemoryRenderingManager().getRenderingType(getDefaultRenderingTypeId());
		IMemoryRenderingContainer container = (IMemoryRenderingContainer)tab.getData(KEY_CONTAINER);
		if (container == null) {
			container = new MemoryBrowserRenderingContainer();
			fCurrentContainers.add(container);
		}
		
		IMemoryRendering rendering = getRenderings(tab).get(memorySpaceId);
		if (rendering == null) {
			// No rendering yet. Create rendering and associated memory block.
			// createMemoryBlock will throw if expression cannot be resolved 
			IMemoryBlockExtension block = createMemoryBlock(retrieval, expression, context, memorySpaceId); //$NON-NLS-1$
			try {
				rendering = type.createRendering();
			} catch (CoreException e) {
				MemoryBrowserPlugin.getDefault().getLog().log(new Status(Status.ERROR, MemoryBrowserPlugin.PLUGIN_ID, "", e)); //$NON-NLS-1$
				return null;
			}
			
			rendering.init(container, block);
			container.addMemoryRendering(rendering);
			rendering.createControl(tab.getParent());
			getRenderings(tab).put(memorySpaceId, rendering);
			getMemoryBlocks(tab).add(block);
			rendering.addPropertyChangeListener(new RenderingPropertyChangeListener(tab, rendering));
		}
		
		tab.setControl(rendering.getControl());
		tab.getParent().setSelection(0);
		tab.setData(KEY_RENDERING, rendering);
		tab.setData(KEY_MEMORY_SPACE, memorySpaceId);
		tab.setData(KEY_CONTAINER, container);
		tab.setData(KEY_RENDERING_TYPE, type);
		getSite().getSelectionProvider().setSelection(new StructuredSelection(tab.getData(KEY_RENDERING)));
		updateLabel(tab, rendering);
		
		return rendering;
	}
	

	private void releaseTabFolder(final IMemoryBlockRetrieval retrieval)
	{
		final CTabFolder folder = fContextFolders.get(retrieval);
		if(folder != null)
		{
			Runnable run = new Runnable() {
				public void run() {
						for(CTabItem tab : folder.getItems()) {
							disposeTab(tab);
						}
						fContextFolders.remove(retrieval);
						folder.dispose();
				
						if (fStackLayout.topControl.equals(folder)) {
							handleUnsupportedSelection();
						}
					}
				};
			runOnUIThread(run);
		}
	}
	
	class SelectionProviderAdapter implements ISelectionProvider {

	    List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	    ISelection theSelection = StructuredSelection.EMPTY;

	    public void addSelectionChangedListener(ISelectionChangedListener listener) {
	        listeners.add(listener);
	    }

	    public ISelection getSelection() {
	        return theSelection;
	    }

	    public void removeSelectionChangedListener(
	            ISelectionChangedListener listener) {
	        listeners.remove(listener);
	    }

	    public void setSelection(ISelection selection) {
	        theSelection = selection;
	        final SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
	        Object[] listenersArray = listeners.toArray();
	        
	        for (int i = 0; i < listenersArray.length; i++) {
	            final ISelectionChangedListener l = (ISelectionChangedListener) listenersArray[i];
	            SafeRunner.run(new SafeRunnable() {
	                public void run() {
	                    l.selectionChanged(e);
	                }
	            });
			}
	    }
	}
	
	/**
	 * create a memory block 
	 * @param retrieval memory block retrieval.
	 * @param expression expression to be evaluated to an addressL
	 * @param context context for evaluating the expression.  This is typically
	 *  a debug element.
	 * @param memorySpaceID a memory space identifier, or null if n/a
	 * @return a memory block based on the given expression and context
	 * @throws DebugException if unable to retrieve the specified memory
	 */
	private IMemoryBlockExtension createMemoryBlock(IMemoryBlockRetrieval retrieval, String expression, Object context, String memorySpaceID) throws DebugException {
		IMemoryBlockExtension block = null;
		if(retrieval instanceof IAdaptable) {
			IMemoryBlockRetrievalExtension retrievalExtension = (IMemoryBlockRetrievalExtension)((IAdaptable) retrieval).getAdapter(IMemoryBlockRetrievalExtension.class);
			if (retrievalExtension != null) {
				if (retrievalExtension instanceof IMemorySpaceAwareMemoryBlockRetrieval) {
					block = ((IMemorySpaceAwareMemoryBlockRetrieval)retrievalExtension).getMemoryBlock(expression, context, memorySpaceID);
				}
				else {
					block = retrievalExtension.getExtendedMemoryBlock(expression, context);
				}
			}
		}
		if ( block == null ) {
			throw new DebugException(new Status(Status.ERROR, MemoryBrowserPlugin.PLUGIN_ID, "Extended Memory Block could not be obtained")); //$NON-NLS-1$
		}
		return block;
	}
	
	/**
	 * Get a memory address for an expression in a given context.    
	 * @param retrieval
	 * @param expression
	 * @param context
	 * @return BigInteger address of the expression
	 * @throws DebugException
	 */
	private BigInteger getExpressionAddress(IMemoryBlockRetrieval retrieval, String expression, Object context, String memorySpaceId) throws DebugException {
		// Until 257842 issue is solved this is done via IMemoryBlockRetrievalExtension API.
		IMemoryBlockExtension newBlock = createMemoryBlock(retrieval, expression, context, memorySpaceId);
		BigInteger address = newBlock.getBigBaseAddress();
		newBlock.dispose();
		return address;
	}

	/**
	 * Execute runnable on UI thread if the current thread is not an UI thread.
	 * Otherwise execute it directly.
	 * 
	 * @param runnable
	 *            the runnable to execute
	 */
	private void runOnUIThread(final Runnable runnable)	{
		if (Display.getCurrent() != null) {
			runnable.run();
		}
		else {
			UIJob job = new UIJob("Memory Browser UI Job"){ //$NON-NLS-1$
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					runnable.run();
					return Status.OK_STATUS;
				}};
			job.setSystem(true);
			job.schedule();
		}
	}
}
