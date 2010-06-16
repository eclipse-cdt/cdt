/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.browser.opentype;

import java.util.Arrays;
import java.util.HashSet;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.IndexTypeInfo;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog;

import org.eclipse.cdt.internal.core.browser.IndexModelUtil;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;

/**
 * A dialog to select an element from a filterable list of elements.
 *
 * @since 4.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ElementSelectionDialog extends TypeSelectionDialog {

	/**
	 * Job to update the element list in the background.
	 */
	private class UpdateElementsJob extends Job {

		/**
		 * The last used prefix to query the index. <code>null</code> means
		 * the query result should be empty.
		 */
		private volatile char[] fCurrentPrefix = null;

		public UpdateElementsJob(String name) {
			super(name);
			setSystem(true);
			setUser(false);
			setPriority(Job.LONG);
		}

		public char[] getCurrentPrefix() {
			return fCurrentPrefix;
		}
		public void scheduleQuery(char[] prefix) {
			fCurrentPrefix= prefix;
			int delay = fCurrentPrefix == null ? 0 : (fCurrentPrefix.length < 5 ? 400 : 200);
			schedule(delay);
		}

		@Override
		public IStatus run(final IProgressMonitor monitor) {
			monitor.beginTask(OpenTypeMessages.ElementSelectionDialog_UpdateElementsJob_inProgress, IProgressMonitor.UNKNOWN);
			final ITypeInfo[] elements= getElementsByPrefix(fCurrentPrefix, monitor);
			if (elements != null && !monitor.isCanceled()) {
				final Shell shell= getShell();
				if (shell != null && !shell.isDisposed()) {
					Runnable update= new Runnable() {
						public void run() {
							if (!shell.isDisposed() && !monitor.isCanceled()) {
								setListElements(elements);
								updateOkState();
							}
						}};
					shell.getDisplay().asyncExec(update);
					monitor.done();
					return Status.OK_STATUS;
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

		@Override
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

		@Override
		public void running(final IJobChangeEvent event) {
			fDone= false;
			final Shell shell= getShell();
			if (shell != null && !shell.isDisposed()) {
				Runnable update= new Runnable() {
					public void run() {
						if (!shell.isDisposed() && !fDone) {
							fMonitor.beginTask(OpenTypeMessages.ElementSelectionDialog_UpdateElementsJob_inProgress, IProgressMonitor.UNKNOWN);
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

	private UpdateElementsJob fUpdateJob;
	private boolean fAllowEmptyPrefix= true;
	private boolean fAllowEmptyString= true;
	private ProgressMonitorPart fProgressMonitorPart;

	private String fHelpContextId;

	/**
	 * Constructs an instance of <code>OpenTypeDialog</code>.
	 * @param parent  the parent shell.
	 */
	public ElementSelectionDialog(Shell parent) {
		super(parent);
		setHelpContextId(ICHelpContextIds.OPEN_ELEMENT_DIALOG);
		setMatchEmptyString(false);
		fUpdateJob= new UpdateElementsJob(OpenTypeMessages.ElementSelectionDialog_UpdateElementsJob_name);
		fUpdateJob.setRule(SINGLE_INSTANCE_RULE);
	}
	
	/*
	 * @see org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog#create()
	 */
	@Override
	public void create() {
		super.create();
		// trigger initial query
		scheduleUpdate(getFilter());
	}

	/*
	 * @see org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog#close()
	 */
	@Override
	public boolean close() {
		fUpdateJob.cancel();
		return super.close();
	}

	/**
	 * Configure the help context id for this dialog.
	 * 
	 * @param helpContextId
	 */
	public void setHelpContextId(String helpContextId) {
		fHelpContextId= helpContextId;
		setHelpAvailable(fHelpContextId != null);
	}

	/*
	 * @see org.eclipse.ui.dialogs.AbstractElementListSelectionDialog#setMatchEmptyString(boolean)
	 */
	@Override
	public void setMatchEmptyString(boolean matchEmptyString) {
		super.setMatchEmptyString(matchEmptyString);
		fAllowEmptyString= matchEmptyString;
		if (matchEmptyString) {
			setAllowEmptyPrefix(true);
		}
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
	@Override
	protected boolean showLowLevelFilter() {
		// the low-level filter is useless for us
		return false;
	}
	
	/*
	 * @see org.eclipse.ui.dialogs.TwoPaneElementSelector#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createDialogArea(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, fHelpContextId);
		return super.createDialogArea(parent);
	}

	/*
	 * @see org.eclipse.ui.dialogs.TwoPaneElementSelector#createLowerList(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Table createLowerList(Composite parent) {
		Table table= super.createLowerList(parent);
		createProgressMonitorPart(parent);
		return table;
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
        
		Label separator= new Label(parent.getParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
		HashSet<IndexTypeInfo> types = new HashSet<IndexTypeInfo>();
		if(prefix != null) {
			final IndexFilter filter= new IndexFilter() {
				@Override
				public boolean acceptBinding(IBinding binding) throws CoreException {
					if (isVisibleType(IndexModelUtil.getElementType(binding))) {
						return IndexFilter.ALL_DECLARED.acceptBinding(binding);
					}
					return false;
				}
			};
			try {
				IIndex index = CCorePlugin.getIndexManager().getIndex(CoreModel.getDefault().getCModel().getCProjects());
				index.acquireReadLock();
				try {
					IIndexBinding[] bindings= index.findBindingsForPrefix(prefix, false, filter, monitor);
					for(int i=0; i<bindings.length; i++) {
						if (i % 0x1000 == 0 && monitor.isCanceled()) {
							return null;
						}
						final IndexTypeInfo typeinfo = IndexTypeInfo.create(index, bindings[i]);
						types.add(typeinfo);
					}
					
					if (isVisibleType(ICElement.C_MACRO)) {
						IIndexMacro[] macros= index.findMacrosForPrefix(prefix, IndexFilter.ALL_DECLARED, monitor);
						for(int i=0; i<macros.length; i++) {
							if (i % 0x1000 == 0 && monitor.isCanceled()) {
								return null;
							}
							final IndexTypeInfo typeinfo = IndexTypeInfo.create(index, macros[i]);
							types.add(typeinfo);
						}
					}
				} finally {
					index.releaseReadLock();
				}
			} catch(CoreException ce) {
				CUIPlugin.log(ce);
			} catch(InterruptedException ie) {
				CUIPlugin.log(ie);
			}
		}
		return types.toArray(new ITypeInfo[types.size()]);
	}
	
	@Override
	protected final void setListElements(Object[] elements) {
		super.setListElements(elements);
	}
	
	/**
	 * @deprecated Unsupported
	 */
	@Deprecated
	@Override
	public void setElements(Object[] elements) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void handleEmptyList() {
		updateOkState();
	}
	
	@Override
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
		final char[] currentPrefix= fUpdateJob.getCurrentPrefix();
		final boolean equivalentPrefix= isEquivalentPrefix(currentPrefix, newPrefix);
		boolean emptyQuery= newPrefix.length == 0 && !fAllowEmptyPrefix || filterText.length() == 0 && !fAllowEmptyString;
		final int jobState = fUpdateJob.getState();
		boolean needQuery= !equivalentPrefix || (currentPrefix.length < newPrefix.length && currentPrefix.length < 5 && jobState == Job.RUNNING);
		if (emptyQuery) {
			newPrefix= null;
			needQuery= needQuery || currentPrefix != null;
		}
		if(needQuery || jobState == Job.WAITING  || jobState == Job.SLEEPING) {
			fUpdateJob.cancel();
			fUpdateJob.scheduleQuery(newPrefix);
		}
	}

	private char[] toPrefix(String userFilter) {
		QualifiedTypeName qualifiedName= new QualifiedTypeName(userFilter);
		if (qualifiedName.segmentCount() > 1) {
			userFilter= qualifiedName.lastSegment();
		}
		if (userFilter.endsWith("<")) { //$NON-NLS-1$
			userFilter= userFilter.substring(0, userFilter.length() - 1);
		}
		int asterisk= userFilter.indexOf("*"); //$NON-NLS-1$
		int questionMark= userFilter.indexOf("?"); //$NON-NLS-1$
		int prefixEnd = asterisk < 0 ? questionMark
				: (questionMark < 0 ? asterisk : Math.min(asterisk, questionMark));
		return (prefixEnd==-1 ? userFilter : userFilter.substring(0, prefixEnd)).toCharArray();		
	}
	
	private boolean isEquivalentPrefix(char[] currentPrefix, char[] newPrefix) {
		if (currentPrefix == null || currentPrefix.length > newPrefix.length) {
			return false;
		} else if (newPrefix.length == currentPrefix.length) {
			return Arrays.equals(currentPrefix, newPrefix);
		}
		return new String(currentPrefix).equals(new String(newPrefix, 0, currentPrefix.length));
	}

}
