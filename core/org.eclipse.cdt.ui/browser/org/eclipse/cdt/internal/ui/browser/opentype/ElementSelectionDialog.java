/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.browser.opentype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.IndexTypeInfo;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.ui.browser.typeinfo.TypeInfoMessages;
import org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog;

import org.eclipse.cdt.internal.core.browser.util.IndexModelUtil;

/**
 * A dialog to select a element from a filterable list of elements.
 *
 * @since 4.0
 */
public class ElementSelectionDialog extends TypeSelectionDialog {

	/**
	 * Job to update the element list in the background.
	 */
	private class UpdateElementsJob extends Job {

		public UpdateElementsJob(String name) {
			super(name);
			setSystem(true);
			setUser(false);
			setPriority(Job.LONG);
		}

		public IStatus run(final IProgressMonitor monitor) {
			monitor.beginTask(TypeInfoMessages.OpenSymbolDialog_UpdateSymbolsJob_inProgress, IProgressMonitor.UNKNOWN);
			final ITypeInfo[] elements= getElementsByPrefix(fCurrentPrefix, monitor);
			if (elements != null && !monitor.isCanceled()) {
				final Shell shell= getShell();
				if (shell != null && !shell.isDisposed()) {
					Runnable update= new Runnable() {
						public void run() {
							if (!shell.isDisposed() && !monitor.isCanceled()) {
								setListElements(elements);
								done(Status.OK_STATUS);
								updateOkState();
							}
						}};
					shell.getDisplay().asyncExec(update);
					monitor.done();
					return Job.ASYNC_FINISH;
				}
			}
			return Status.CANCEL_STATUS;
		}

	}

	/**
	 * A job listener for simple job status reporting.
	 */
	private final class UpdateJobListener extends JobChangeAdapter {

		boolean fDone;
		private IProgressMonitor fMonitor;

		private UpdateJobListener(IProgressMonitor monitor) {
			fMonitor= monitor;
		}

		public void done(IJobChangeEvent event) {
			fDone= true;
			final Shell shell= getShell();
			if (shell != null && !shell.isDisposed()) {
				Runnable update= new Runnable() {
					public void run() {
						if (!shell.isDisposed() && fDone) {
							fMonitor.done();
						}
					}};
				shell.getDisplay().asyncExec(update);
			}
		}

		public void running(final IJobChangeEvent event) {
			fDone= false;
			final Shell shell= getShell();
			if (shell != null && !shell.isDisposed()) {
				Runnable update= new Runnable() {
					public void run() {
						if (!shell.isDisposed() && !fDone) {
							fMonitor.beginTask(TypeInfoMessages.OpenSymbolDialog_UpdateSymbolsJob_inProgress, IProgressMonitor.UNKNOWN);
						}
					}};
				shell.getDisplay().asyncExec(update);
			}
		}
	}

	private static final ISchedulingRule SINGLE_INSTANCE_RULE = new ISchedulingRule() {
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}};

	private char[] fCurrentPrefix= {};
	private Job fUpdateJob;
	private boolean fAllowEmptyPrefix;
	private ProgressMonitorPart fProgressMonitorPart;

	/**
	 * Constructs an instance of <code>OpenTypeDialog</code>.
	 * @param parent  the parent shell.
	 */
	public ElementSelectionDialog(Shell parent) {
		super(parent);
		fUpdateJob= new UpdateElementsJob(TypeInfoMessages.OpenSymbolDialog_UpdateSymbolsJob_name);
		fUpdateJob.setRule(SINGLE_INSTANCE_RULE);
	}
	
	/*
	 * @see org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog#create()
	 */
	public void create() {
		super.create();
		// trigger initial query
		scheduleUpdate(getFilter());
	}

	/*
	 * @see org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog#close()
	 */
	public boolean close() {
		fUpdateJob.cancel();
		return super.close();
	}

	/*
	 * @see org.eclipse.ui.dialogs.AbstractElementListSelectionDialog#setMatchEmptyString(boolean)
	 */
	public void setMatchEmptyString(boolean matchEmptyString) {
		super.setMatchEmptyString(matchEmptyString);
		setAllowEmptyPrefix(matchEmptyString);
	}
	/**
	 * Set whether an empty prefix should be allowed for queries.
	 * 
	 * @param allowEmptyPrefix
	 */
	public void setAllowEmptyPrefix(boolean allowEmptyPrefix) {
		fAllowEmptyPrefix = allowEmptyPrefix;
	}

	/*
	 * @see org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog#showLowLevelFilter()
	 */
	protected boolean showLowLevelFilter() {
		return false;
	}
	
	/*
	 * @see org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog#createFilteredList(org.eclipse.swt.widgets.Composite)
	 */
	protected FilteredList createFilteredList(Composite parent) {
		FilteredList list= super.createFilteredList(parent);
		createProgressMonitorPart(parent);
		return list;
	}

	/**
	 * Create the control for progress reporting.
	 * @param parent
	 */
	private void createProgressMonitorPart(Composite parent) {
		fProgressMonitorPart= new ProgressMonitorPart(parent, new GridLayout(2, false));
		GridData gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent= 0;
        gridData.verticalAlignment= GridData.BEGINNING;
        fProgressMonitorPart.setLayoutData(gridData);
		fUpdateJob.addJobChangeListener(new UpdateJobListener(fProgressMonitorPart));
	}

	/**
	 * Query the elements for the given prefix.
	 * 
	 * @param prefix
	 * @param monitor
	 */
	protected ITypeInfo[] getElementsByPrefix(char[] prefix, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return null;
		}
		List types = new ArrayList();
		if(prefix.length > 0 || fAllowEmptyPrefix) {
			try {
				IIndex index = CCorePlugin.getIndexManager().getIndex(CoreModel.getDefault().getCModel().getCProjects());
				try {
					index.acquireReadLock();
					IBinding[] bindings= index.findBindingsForPrefix(prefix, false, IndexFilter.ALL);
//					IBinding[] bindings= index.findBindingsForPrefix(prefix, false, IndexFilter.ALL, monitor);
					for(int i=0; i<bindings.length; i++) {
						if ((i % 100) == 0 && monitor.isCanceled()) {
							return null;
						}
						IBinding binding = bindings[i];
						try {
							String[] fqn;

							if(binding instanceof ICPPBinding) {
								fqn= ((ICPPBinding)binding).getQualifiedName();
							} else {
								fqn = new String[] {binding.getName()};
							}
							final int elementType = IndexModelUtil.getElementType(binding);
							if (isVisibleType(elementType)) {
								if (binding instanceof IFunction) {
									final IFunction function = (IFunction)binding;
									final String[] paramTypes = IndexModelUtil.extractParameterTypes(function);
									final String returnType= IndexModelUtil.extractReturnType(function);
									types.add(new IndexTypeInfo(fqn, elementType, paramTypes, returnType, index));
								} else {
									types.add(new IndexTypeInfo(fqn, elementType, index));
								}
							}
						} catch(DOMException de) {

						}
					}
				} finally {
					index.releaseReadLock();
				}
			} catch(CoreException ce) {
				CCorePlugin.log(ce);
			} catch(InterruptedException ie) {
				CCorePlugin.log(ie);
			}
		}
		return (ITypeInfo[])types.toArray(new ITypeInfo[types.size()]);
	}
	
	protected final void setListElements(Object[] elements) {
		super.setListElements(elements);
	}
	
	/**
	 * @deprecated Unsupported
	 */
	public void setElements(Object[] elements) {
		throw new UnsupportedOperationException();
	}
	
	protected void handleEmptyList() {
		updateOkState();
	}
	
	protected Text createFilterText(Composite parent) {
		final Text result = super.createFilterText(parent);
		Listener listener = new Listener() {
            public void handleEvent(Event e) {
                scheduleUpdate(result.getText());
            }
        };
        result.addListener(SWT.Modify, listener);
        return result;
	}

	protected void scheduleUpdate(String filterText) {
		char[] newPrefix= toPrefix(filterText);
		if (!isEquivalentPrefix(fCurrentPrefix, newPrefix)) {
			fUpdateJob.cancel();
			fCurrentPrefix= newPrefix;
			fUpdateJob.schedule(200);
		}
	}

	private char[] toPrefix(String userFilter) {
		QualifiedTypeName qualifiedName= new QualifiedTypeName(userFilter);
		if (qualifiedName.segmentCount() > 1) {
			userFilter= qualifiedName.lastSegment();
		}
		userFilter= userFilter.trim().replaceAll("^(\\*)*", "");  //$NON-NLS-1$//$NON-NLS-2$
		int asterix= userFilter.indexOf("*"); //$NON-NLS-1$
		return (asterix==-1 ? userFilter : userFilter.substring(0, asterix)).toCharArray();		
	}
	
	private boolean isEquivalentPrefix(char[] currentPrefix, char[] newPrefix) {
		if (currentPrefix.length == 0 || currentPrefix.length > newPrefix.length) {
			return false;
		} else if (newPrefix.length == currentPrefix.length) {
			return Arrays.equals(currentPrefix, newPrefix);
		}
		return new String(currentPrefix).equals(new String(newPrefix, 0, currentPrefix.length));
	}
}
