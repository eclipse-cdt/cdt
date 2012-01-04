/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - adapted for DSF
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputRequestor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ViewerInputService;
import org.eclipse.debug.internal.ui.views.variables.details.DefaultDetailPane;
import org.eclipse.debug.internal.ui.views.variables.details.DetailPaneProxy;
import org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Creates an information control to display an expression in a hover control.
 * <br/> This class is derivative work from JDT's <code>ExpressionInformationControlCreator</code>.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * 
 * @since 2.1
 */
@SuppressWarnings("restriction")
public class ExpressionInformationControlCreator implements IInformationControlCreator {

    /**
     * A presentation context for the expression hover control.
     * Implements equals and hashCode based on id comparison.
     */
    private static final class ExpressionHoverPresentationContext extends PresentationContext {

        private ExpressionHoverPresentationContext(String id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof ExpressionHoverPresentationContext) {
                if (getId().equals(((PresentationContext) obj).getId())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getId().hashCode();
        }
    }

	class ExpressionInformationControl extends AbstractInformationControl implements IInformationControlExtension2, IViewerInputRequestor {

		/**
		 * Dialog setting key for height
		 */
		private static final String HEIGHT = "HEIGHT"; //$NON-NLS-1$

		/**
		 * Dialog setting key for width. 
		 */
		private static final String WIDTH = "WIDTH"; //$NON-NLS-1$

		/**
		 * Dialog setting key for tree sash weight
		 */
		private static final String SASH_WEIGHT_TREE = "SashWeightTree"; //$NON-NLS-1$

		/**
		 * Dialog setting key for details sash weight
		 */
		private static final String SASH_WEIGHT_DETAILS = "SashWeightDetails"; //$NON-NLS-1$		

		/**
		 * Variable to display.
		 */
		private Object fVariable;

		private TreeModelViewer fViewer;
		private SashForm fSashForm;
		private Composite fDetailPaneComposite;
		private DetailPaneProxy fDetailPane;
		private Tree fTree;

		private ViewerInputService fInputService;

        private IInformationControlCreator fInformationControlCreator;

		/**
		 * Inner class implementing IDetailPaneContainer methods.  Handles changes to detail
		 * pane and provides limited access to the detail pane proxy.
		 */
		private class DetailPaneContainer implements IDetailPaneContainer {

			/*
			 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#getCurrentPaneID()
			 */
			@Override
			public String getCurrentPaneID() {
				return fDetailPane.getCurrentPaneID();
			}

			/*
			 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#getCurrentSelection()
			 */
			@Override
			public IStructuredSelection getCurrentSelection() {
				return (IStructuredSelection)fViewer.getSelection();
			}

			/*
			 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#refreshDetailPaneContents()
			 */
			@Override
			public void refreshDetailPaneContents() {		
				fDetailPane.display(getCurrentSelection());
			}

			/*
			 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#getParentComposite()
			 */
			@Override
			public Composite getParentComposite() {
				return fDetailPaneComposite;
			}

			/*
			 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#getWorkbenchPartSite()
			 */
			@Override
			public IWorkbenchPartSite getWorkbenchPartSite() {
				return null;
			}

			/*
			 * @see org.eclipse.debug.internal.ui.views.variables.details.IDetailPaneContainer#paneChanged(java.lang.String)
			 */
			@Override
			public void paneChanged(String newPaneID) {
				if (DefaultDetailPane.ID.equals(newPaneID)){
					fDetailPane.getCurrentControl().setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
				}
			}

		}

		/**
		 * Constructs a new control in the given shell.
		 * 
		 * @param parentShell shell
		 * @param resize whether resize is supported
		 */
		ExpressionInformationControl(Shell parentShell, boolean resize) {
			super(parentShell, resize);
			create();
		}

		@Override
		public Point computeSizeHint() {
			IDialogSettings settings = getDialogSettings(false);
			if (settings != null) {
				int x = getIntSetting(settings, WIDTH);
				if (x > 0) {
					int y = getIntSetting(settings, HEIGHT);
					if (y > 0) {
						return new Point(x,y);
					}
				}
			}
			return super.computeSizeHint();
		}

		@Override
		public void setSize(int width, int height) {
		    if (!isResizable() && fDetailPaneComposite != null) {
		        // add height of details pane
		        height += fDetailPaneComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		    }
		    super.setSize(width, height);
		}

		/**
		 * Returns the dialog settings for this hover or <code>null</code> if none
		 * 
		 * @param create whether to create the settings
		 */
		private IDialogSettings getDialogSettings(boolean create) {
			IDialogSettings settings = DsfUIPlugin.getDefault().getDialogSettings();
			IDialogSettings section = settings.getSection(this.getClass().getName());
			if (section == null & create) {
				section = settings.addNewSection(this.getClass().getName());
			}
			return section;
		}

		/**
		 * Returns an integer value in the given dialog settings or -1 if none.
		 * 
		 * @param settings dialog settings
		 * @param key key
		 * @return value or -1 if not present
		 */
		private int getIntSetting(IDialogSettings settings, String key) {
			try {
				return settings.getInt(key);
			} catch (NumberFormatException e) {
				return -1;
			}
		}

		@Override
		public void dispose() {
			persistSettings(getShell());
			super.dispose();
		}

		/**
		 * Persists dialog settings.
		 * 
		 * @param shell
		 */
		private void persistSettings(Shell shell) {
			if (shell != null && !shell.isDisposed()) {
				if (isResizable()) {
					IDialogSettings settings = getDialogSettings(true);
					Point size = shell.getSize();
					settings.put(WIDTH, size.x);
					settings.put(HEIGHT, size.y);
                    int[] weights = fSashForm.getWeights();
                    if (weights.length == 1) {
                        settings.put(SASH_WEIGHT_TREE, weights[0]);
                    }
                    else if (weights.length == 2) {
                        settings.put(SASH_WEIGHT_TREE,    weights[0]);
                        settings.put(SASH_WEIGHT_DETAILS, weights[1]);
                    }
				}
			}
		}

		@Override
		public void setVisible(boolean visible) {
			if (!visible) {		
				persistSettings(getShell());
			}
			super.setVisible(visible);
		}

		@Override
		protected void createContent(Composite parent) {

			fSashForm = new SashForm(parent, parent.getStyle());
			fSashForm.setOrientation(SWT.VERTICAL);

			// update presentation context
			AbstractDebugView view = getViewToEmulate();
			IPresentationContext context = new ExpressionHoverPresentationContext(IDsfDebugUIConstants.ID_EXPRESSION_HOVER);
			if (view != null) {
				// copy over properties
				IPresentationContext copy = ((TreeModelViewer)view.getViewer()).getPresentationContext();
				try {
					String[] properties = copy.getProperties();
					for (int i = 0; i < properties.length; i++) {
						String key = properties[i];
						context.setProperty(key, copy.getProperty(key));
					}
				} catch (NoSuchMethodError e) {
					// ignore
				}
			}

			fViewer = new TreeModelViewer(fSashForm, SWT.MULTI | SWT.VIRTUAL | SWT.FULL_SELECTION, context);
			fViewer.setAutoExpandLevel(fExpansionLevel);
			
			if (view != null) {
				// copy over filters
				StructuredViewer structuredViewer = (StructuredViewer) view.getViewer();
				if (structuredViewer != null) {
					ViewerFilter[] filters = structuredViewer.getFilters();
					for (int i = 0; i < filters.length; i++) {
						fViewer.addFilter(filters[i]);
					}
				}
			}
			fInputService = new ViewerInputService(fViewer, this);
            fTree = fViewer.getTree();

            if (fShowDetailPane) {
    			fDetailPaneComposite = SWTFactory.createComposite(fSashForm, 1, 1, GridData.FILL_BOTH);
    			Layout layout = fDetailPaneComposite.getLayout();
    			if (layout instanceof GridLayout) {
    				GridLayout gl = (GridLayout) layout;
    				gl.marginHeight = 0;
    				gl.marginWidth = 0;
    			}
    
    			fDetailPane = new DetailPaneProxy(new DetailPaneContainer());
    			fDetailPane.display(null); // Bring up the default pane so the user doesn't see an empty composite
    
    			fTree.addSelectionListener(new SelectionListener() {
    				@Override
					public void widgetSelected(SelectionEvent e) {
    					fDetailPane.display((IStructuredSelection)fViewer.getSelection());
    				}
    				@Override
					public void widgetDefaultSelected(SelectionEvent e) {}
    			});
            }
            
			initSashWeights();

			// add update listener to auto-select and display details of root expression
			fViewer.addViewerUpdateListener(new IViewerUpdateListener() {
				@Override
				public void viewerUpdatesComplete() {
                    fViewer.getDisplay().timerExec(100, new Runnable() {
                        @Override
						public void run() {
                            TreeSelection selection = (TreeSelection) fViewer.getSelection();
                            if (selection.isEmpty()) {
                                selection = new TreeSelection(fViewer.getTopElementPath());
                            }
                            fViewer.setSelection(selection);
                            if (fDetailPane != null) {
                                fDetailPane.display(selection);
                            }
                        }});
				}
				@Override
				public void viewerUpdatesBegin() {
				}
				@Override
				public void updateStarted(IViewerUpdate update) {
				}
				@Override
				public void updateComplete(IViewerUpdate update) {
				}
			});

			setBackgroundColor(getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		}


		/**
		 * Attempts to find an appropriate view to emulate, this will either be the
		 * variables view or the expressions view.
		 * @return a view to emulate or <code>null</code>
		 */
		private AbstractDebugView getViewToEmulate() {
			IWorkbenchPage page = DsfUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			AbstractDebugView expressionsView = (AbstractDebugView) page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
			if (expressionsView != null && expressionsView.isVisible()) {
				return expressionsView;
			}
			AbstractDebugView variablesView = (AbstractDebugView) page.findView(IDebugUIConstants.ID_VARIABLE_VIEW);
			if (variablesView != null && variablesView.isVisible()) {
				return variablesView;
			}
			if (expressionsView != null) {
				return expressionsView;
			}
			return variablesView;
		}	

		/**
		 * Initializes the sash form weights from the preference store (using default values if 
		 * no sash weights were stored previously).
		 */
		protected void initSashWeights(){
			IDialogSettings settings = getDialogSettings(false);
			if (settings != null) {
				int tree = getIntSetting(settings, SASH_WEIGHT_TREE);
				if (tree > 0) {
                    if (fDetailPane != null) {
                        int details = getIntSetting(settings, SASH_WEIGHT_DETAILS);
                        if (details <= 0) {
                            details = tree / 2;
                        }
                        fSashForm.setWeights(new int[]{tree, details});
                    }
                    else {
                        fSashForm.setWeights(new int[]{tree});
                    }
				}
			}
		}

		@Override
		public void setBackgroundColor(Color background) {
			super.setBackgroundColor(background);
			if (fDetailPaneComposite != null) {
			    fDetailPaneComposite.setBackground(background);
			}
			fTree.setBackground(background);
		}

		@Override
		public void setFocus() {
			super.setFocus();
			fTree.setFocus();
		}

		/*
		 * @see org.eclipse.jface.text.IInformationControlExtension#hasContents()
		 */
		@Override
		public boolean hasContents() {
			return fVariable != null;
		}

		/*
		 * @see org.eclipse.jface.text.IInformationControlExtension2#setInput(java.lang.Object)
		 */
		@Override
		public void setInput(Object input) {
			if (input instanceof IExpressionDMContext) {
				fVariable = input;
				fInputService.resolveViewerInput(input);
			}
		}

		@Override
		public IInformationControlCreator getInformationPresenterControlCreator() {
		    if (fInformationControlCreator == null) {
    		    fInformationControlCreator = new ExpressionInformationControlCreator(fShowDetailPane, fExpansionLevel) {
    				@Override
    				public IInformationControl createInformationControl(Shell shell) {
    					return new ExpressionInformationControl(shell, true);
    				}
    			};
		    }
			return fInformationControlCreator;
		}

		@Override
		public void viewerInputComplete(IViewerInputUpdate update) {
			fViewer.setInput(fVariable = update.getInputElement());
		}

	}

    protected final boolean fShowDetailPane;
    protected final int fExpansionLevel;

    /**
     * Create default expression information control creator.
     * <p>
     * Same as {@code ExpressionInformationControlCreator(true, 1)}.
     * </p>
     */
    public ExpressionInformationControlCreator() {
        this(true, 1);
    }

	/**
	 * Create expression information control creator with customization options.
	 * 
     * @param showDetailPane  if <code>true</code> the detail pane will be shown
     * @param expansionLevel  tree level to which the expression should be expanded by default
     */
    public ExpressionInformationControlCreator(boolean showDetailPane, int expansionLevel) {
        fShowDetailPane = showDetailPane;
        fExpansionLevel = expansionLevel;
    }

    /*
	 * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	public IInformationControl createInformationControl(Shell parent) {
		return new ExpressionInformationControl(parent, false);
	}

}
