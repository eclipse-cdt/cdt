/*******************************************************************************
 * Copyright (c) 2007-2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.search;

import java.lang.reflect.Method;
import java.math.BigInteger;

import org.eclipse.cdt.debug.ui.memory.search.FindReplaceDialog.IMemorySearchQuery;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.part.Page;

public class MemorySearchResultsPage extends Page implements ISearchResultPage, IQueryListener {

	private TreeViewer fTreeViewer;
	private Composite fViewerContainer;
	private IQueryListener fQueryListener;

	private ISearchResultViewPart fPart;

	@Override
	public void queryAdded(ISearchQuery query) {
	}

	@Override
	public void queryFinished(ISearchQuery query) {
	}

	@Override
	public void queryRemoved(ISearchQuery query) {
	}

	@Override
	public void queryStarting(ISearchQuery query) {
	}

	@Override
	public String getID() {

		return MemorySearchPlugin.getUniqueIdentifier();
	}

	@Override
	public String getLabel() {
		if (fQuery == null)
			return Messages.getString("MemorySearchResultsPage.LabelMemorySearch"); //$NON-NLS-1$
		else
			return fQuery.getLabel();
	}

	@Override
	public Object getUIState() {

		return fTreeViewer.getSelection();
	}

	@Override
	public void restoreState(IMemento memento) {
	}

	@Override
	public void saveState(IMemento memento) {
	}

	@Override
	public void setID(String id) {
	}

	@Override
	public void setInput(ISearchResult search, Object uiState) {
		if (search instanceof MemorySearchResult) {
			((MemorySearchResult) search).addListener(new ISearchResultListener() {
				@Override
				public void searchResultChanged(SearchResultEvent e) {
					Display.getDefault().asyncExec(() -> fTreeViewer.refresh());
				}
			});
		}
	}

	@Override
	public void setViewPart(ISearchResultViewPart part) {
		fPart = part;
	}

	@Override
	public void createControl(Composite parent) {
		fViewerContainer = new Composite(parent, SWT.NULL);
		fViewerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewerContainer.setSize(100, 100);
		fViewerContainer.setLayout(new FillLayout());

		fTreeViewer = new TreeViewer(fViewerContainer, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		fTreeViewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				viewer.refresh();
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return new Object[0];
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public boolean hasChildren(Object element) {
				return false;
			}

			@Override
			public Object[] getElements(Object inputElement) {

				if (fQuery == null)
					return new Object[0];
				else {
					return ((MemorySearchResult) fQuery.getSearchResult()).getMatches();
				}
			}
		});

		fTreeViewer.setInput(new Object());
		fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				if (event.getSelection() instanceof StructuredSelection) {
					IMemoryRenderingContainer containers[] = ((IMemorySearchQuery) fQuery).getMemoryView()
							.getMemoryRenderingContainers();
					MemoryMatch match = (MemoryMatch) ((StructuredSelection) event.getSelection()).getFirstElement();
					if (match != null) {
						for (int i = 0; i < containers.length; i++) {
							IMemoryRendering rendering = containers[i].getActiveRendering();
							if (rendering instanceof IRepositionableMemoryRendering) {
								try {
									((IRepositionableMemoryRendering) rendering).goToAddress(match.getStartAddress());
								} catch (DebugException e) {
									MemorySearchPlugin.logError(
											Messages.getString("MemorySearchResultsPage.RepositioningMemoryViewFailed"), //$NON-NLS-1$
											e);
								}
							}

							if (rendering != null) {
								// Temporary, until platform accepts/adds new interface for setting the selection
								try {
									Method m = rendering.getClass().getMethod("setSelection", //$NON-NLS-1$
											new Class[] { BigInteger.class, BigInteger.class });
									if (m != null)
										m.invoke(rendering, match.getStartAddress(), match.getEndAddress());
								} catch (Exception e) {
									// do nothing
								}
							}
						}
					}
				}
			}
		});

		fTreeViewer.setLabelProvider(new ILabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof MemoryMatch)
					return "0x" + ((MemoryMatch) element).getStartAddress().toString(16); //$NON-NLS-1$

				return element.toString();
			}

			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public void addListener(ILabelProviderListener listener) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			@Override
			public void removeListener(ILabelProviderListener listener) {
			}
		});

		fQueryListener = createQueryListener();

		NewSearchUI.addQueryListener(fQueryListener);
	}

	private ISearchQuery fQuery;

	private IQueryListener createQueryListener() {
		return new IQueryListener() {
			@Override
			public void queryAdded(ISearchQuery query) {
			}

			@Override
			public void queryRemoved(ISearchQuery query) {
				queryStarting(query);
			}

			@Override
			public void queryStarting(final ISearchQuery query) {
				fQuery = query;

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						fPart.updateLabel();

						if (!fTreeViewer.getControl().isDisposed())
							fTreeViewer.refresh();
					}
				});
			}

			@Override
			public void queryFinished(final ISearchQuery query) {
			}
		};
	}

	@Override
	public void dispose() {
		fTreeViewer.getControl().dispose();
		fViewerContainer.dispose();
	}

	@Override
	public Control getControl() {
		return fViewerContainer;
	}

	@Override
	public void setActionBars(IActionBars actionBars) {
	}

	@Override
	public void setFocus() {
	}
}
