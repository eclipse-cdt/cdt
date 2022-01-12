/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *     Kirk Beitz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.internal.ui.dialogs.OptionalMessageDialog;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension2;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * A content assist processor that aggregates the proposals of the
 * {@link org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer}s contributed via
 * the {@code org.eclipse.cdt.ui.completionProposalComputer} extension point.
 * <p>
 * Subclasses may extend:
 * <ul>
 * <li>{@code createContext} to provide the context object passed to the computers</li>
 * <li>{@code createProgressMonitor} to change the way progress is reported</li>
 * <li>{@code filterAndSort} to add sorting and filtering</li>
 * <li>{@code getContextInformationValidator} to add context validation (needed if any
 * contexts are provided)</li>
 * <li>{@code getErrorMessage} to change error reporting</li>
 * </ul>
 *
 * @since 4.0
 */
public class ContentAssistProcessor implements IContentAssistProcessor {
	private static final boolean DEBUG = Boolean
			.parseBoolean(Platform.getDebugOption("org.eclipse.cdt.ui/debug/contentassist")); //$NON-NLS-1$

	/**
	 * Dialog settings key for the "all categories are disabled" warning dialog.
	 * See {@link OptionalMessageDialog}.
	 */
	private static final String PREF_WARN_ABOUT_EMPTY_ASSIST_CATEGORY = "EmptyDefaultAssistCategory"; //$NON-NLS-1$

	private static final Comparator<CompletionProposalCategory> ORDER_COMPARATOR = new Comparator<CompletionProposalCategory>() {
		@Override
		public int compare(CompletionProposalCategory d1, CompletionProposalCategory d2) {
			return d1.getSortOrder() - d2.getSortOrder();
		}
	};

	private static final ICompletionProposal[] NO_PROPOSALS = {};

	private final List<CompletionProposalCategory> fCategories;
	private final String fPartition;
	private final ContentAssistant fAssistant;

	private char[] fCompletionAutoActivationCharacters;

	/* cycling stuff */
	private int fRepetition = -1;
	private List<List<CompletionProposalCategory>> fCategoryIteration;
	private String fIterationGesture;
	private int fNumberOfComputedResults;
	private String fErrorMessage;
	private boolean fIsAutoActivated;

	public ContentAssistProcessor(ContentAssistant assistant, String partition) {
		Assert.isNotNull(partition);
		Assert.isNotNull(assistant);
		fPartition = partition;
		fCategories = CompletionProposalComputerRegistry.getDefault().getProposalCategories();
		fAssistant = assistant;
		fAssistant.addCompletionListener(new ICompletionListener() {
			@Override
			public void assistSessionStarted(ContentAssistEvent event) {
				if (event.processor != ContentAssistProcessor.this)
					return;

				fIsAutoActivated = event.isAutoActivated;
				fIterationGesture = getIterationGesture();
				KeySequence binding = getIterationBinding();

				// This may show the warning dialog if all categories are disabled.
				fCategoryIteration = getCategoryIteration();
				for (Object element : fCategories) {
					CompletionProposalCategory cat = (CompletionProposalCategory) element;
					cat.sessionStarted();
				}

				fRepetition = 0;
				if (event.assistant instanceof IContentAssistantExtension2) {
					IContentAssistantExtension2 extension = (IContentAssistantExtension2) event.assistant;

					if (fCategoryIteration.size() == 1) {
						extension.setRepeatedInvocationMode(false);
						extension.setShowEmptyList(false);
					} else {
						extension.setRepeatedInvocationMode(true);
						extension.setStatusLineVisible(true);
						extension.setStatusMessage(createIterationMessage());
						extension.setShowEmptyList(true);
						if (extension instanceof IContentAssistantExtension3) {
							IContentAssistantExtension3 ext3 = (IContentAssistantExtension3) extension;
							((ContentAssistant) ext3).setRepeatedInvocationTrigger(binding);
						}
					}

				}
			}

			@Override
			public void assistSessionEnded(ContentAssistEvent event) {
				if (event.processor != ContentAssistProcessor.this)
					return;

				for (Object element : fCategories) {
					CompletionProposalCategory cat = (CompletionProposalCategory) element;
					cat.sessionEnded();
				}

				fCategoryIteration = null;
				fRepetition = -1;
				fIterationGesture = null;
				if (event.assistant instanceof IContentAssistantExtension2) {
					IContentAssistantExtension2 extension = (IContentAssistantExtension2) event.assistant;
					extension.setShowEmptyList(false);
					extension.setRepeatedInvocationMode(false);
					extension.setStatusLineVisible(false);
					if (extension instanceof IContentAssistantExtension3) {
						IContentAssistantExtension3 ext3 = (IContentAssistantExtension3) extension;
						((ContentAssistant) ext3).setRepeatedInvocationTrigger(null);
					}
				}
			}

			@Override
			public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
			}

		});
	}

	@Override
	public final ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		long start = DEBUG ? System.currentTimeMillis() : 0;

		if (isAutoActivated() && !verifyAutoActivation(viewer, offset)) {
			return NO_PROPOSALS;
		}

		clearState();

		IProgressMonitor monitor = createProgressMonitor();
		monitor.beginTask(ContentAssistMessages.ContentAssistProcessor_computing_proposals, fCategories.size() + 1);

		ContentAssistInvocationContext context = createContext(viewer, offset, true);
		if (context == null)
			return null;

		try {
			long setup = DEBUG ? System.currentTimeMillis() : 0;

			monitor.subTask(ContentAssistMessages.ContentAssistProcessor_collecting_proposals);
			List<ICompletionProposal> proposals = collectProposals(viewer, offset, monitor, context);
			long collect = DEBUG ? System.currentTimeMillis() : 0;

			monitor.subTask(ContentAssistMessages.ContentAssistProcessor_sorting_proposals);
			List<ICompletionProposal> filtered = filterAndSortProposals(proposals, monitor, context);
			fNumberOfComputedResults = filtered.size();
			long filter = DEBUG ? System.currentTimeMillis() : 0;

			ICompletionProposal[] result = filtered.toArray(new ICompletionProposal[filtered.size()]);
			monitor.done();

			if (DEBUG) {
				System.out.println("Code Assist Stats (" + result.length + " proposals)"); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.println("Code Assist (setup):\t" + (setup - start)); //$NON-NLS-1$
				System.out.println("Code Assist (collect):\t" + (collect - setup)); //$NON-NLS-1$
				System.out.println("Code Assist (sort):\t" + (filter - collect)); //$NON-NLS-1$
			}

			return result;
		} finally {
			context.dispose();
		}
	}

	/**
	 * Verifies that auto activation is allowed.
	 * <p>
	 * The default implementation always returns {@code true}.
	 *
	 * @param viewer  the text viewer
	 * @param offset  the offset where content assist was invoked on
	 * @return {@code true} if auto activation is allowed
	 */
	protected boolean verifyAutoActivation(ITextViewer viewer, int offset) {
		return true;
	}

	private void clearState() {
		fErrorMessage = null;
		fNumberOfComputedResults = 0;
	}

	private List<ICompletionProposal> collectProposals(ITextViewer viewer, int offset, IProgressMonitor monitor,
			ContentAssistInvocationContext context) {
		List<ICompletionProposal> proposals = new ArrayList<>();
		List<CompletionProposalCategory> providers = getCategories();
		SubMonitor progress = SubMonitor.convert(monitor, providers.size());
		for (CompletionProposalCategory cat : providers) {
			List<ICompletionProposal> computed = cat.computeCompletionProposals(context, fPartition, progress.split(1));
			proposals.addAll(computed);
			if (fErrorMessage == null)
				fErrorMessage = cat.getErrorMessage();
		}

		return proposals;
	}

	/**
	 * Filters and sorts the proposals. The passed list may be modified
	 * and returned, or a new list may be created and returned.
	 *
	 * @param proposals the list of collected proposals (element type: {@link ICompletionProposal})
	 * @param monitor a progress monitor
	 * @param context TODO
	 * @return the list of filtered and sorted proposals, ready for
	 *         display (element type: {@link ICompletionProposal})
	 */
	protected List<ICompletionProposal> filterAndSortProposals(List<ICompletionProposal> proposals,
			IProgressMonitor monitor, ContentAssistInvocationContext context) {
		return proposals;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		clearState();

		IProgressMonitor monitor = createProgressMonitor();
		monitor.beginTask(ContentAssistMessages.ContentAssistProcessor_computing_contexts, fCategories.size() + 1);

		monitor.subTask(ContentAssistMessages.ContentAssistProcessor_collecting_contexts);
		List<IContextInformation> proposals = collectContextInformation(viewer, offset, monitor);

		monitor.subTask(ContentAssistMessages.ContentAssistProcessor_sorting_contexts);
		List<IContextInformation> filtered = filterAndSortContextInformation(proposals, monitor);
		fNumberOfComputedResults = filtered.size();

		IContextInformation[] result = filtered.toArray(new IContextInformation[filtered.size()]);
		monitor.done();
		return result;
	}

	private List<IContextInformation> collectContextInformation(ITextViewer viewer, int offset,
			IProgressMonitor monitor) {
		List<IContextInformation> proposals = new ArrayList<>();
		ContentAssistInvocationContext context = createContext(viewer, offset, false);

		try {
			List<CompletionProposalCategory> providers = getCategories();
			SubMonitor progress = SubMonitor.convert(monitor, providers.size());
			for (CompletionProposalCategory cat : providers) {
				List<IContextInformation> computed = cat.computeContextInformation(context, fPartition,
						progress.split(1));
				proposals.addAll(computed);
				if (fErrorMessage == null)
					fErrorMessage = cat.getErrorMessage();
			}

			return proposals;
		} finally {
			context.dispose();
		}
	}

	/**
	 * Filters and sorts the context information objects. The passed list may be modified
	 * and returned, or a new list may be created and returned.
	 *
	 * @param contexts the list of collected proposals (element type: {@link IContextInformation})
	 * @param monitor a progress monitor
	 * @return the list of filtered and sorted proposals, ready for
	 *         display (element type: {@link IContextInformation})
	 */
	protected List<IContextInformation> filterAndSortContextInformation(List<IContextInformation> contexts,
			IProgressMonitor monitor) {
		return contexts;
	}

	/**
	 * Sets this processor's set of characters triggering the activation of the
	 * completion proposal computation (including auto-correction auto-activation)
	 *
	 * @param activationSet the activation set
	 */
	public void setCompletionProposalAutoActivationCharacters(char[] activationSet) {
		fCompletionAutoActivationCharacters = activationSet;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return fCompletionAutoActivationCharacters;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		if (fNumberOfComputedResults > 0)
			return null;
		if (fErrorMessage != null)
			return fErrorMessage;
		return ContentAssistMessages.ContentAssistProcessor_no_completions;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/**
	 * Creates a progress monitor.
	 * <p>
	 * The default implementation creates a {@code NullProgressMonitor}.
	 *
	 * @return a progress monitor
	 */
	protected IProgressMonitor createProgressMonitor() {
		return new NullProgressMonitor();
	}

	/**
	 * Creates the context that is passed to the completion proposal computers.
	 *
	 * @param viewer the viewer that content assist is invoked on
	 * @param offset the content assist offset
	 * @return the context to be passed to the computers,
	 *     or {@code null} if no completion is possible
	 */
	protected ContentAssistInvocationContext createContext(ITextViewer viewer, int offset, boolean isCompletion) {
		return new ContentAssistInvocationContext(viewer, offset);
	}

	/**
	 * Test whether the current session was auto-activated.
	 *
	 * @return  {@code true} if the current session was auto-activated.
	 */
	protected boolean isAutoActivated() {
		return fIsAutoActivated;
	}

	private List<CompletionProposalCategory> getCategories() {
		if (fCategoryIteration == null)
			return fCategories;

		int iteration = fRepetition % fCategoryIteration.size();
		fAssistant.setStatusMessage(createIterationMessage());
		fAssistant.setEmptyMessage(createEmptyMessage());
		fRepetition++;

		//		fAssistant.setShowMessage(fRepetition % 2 != 0);
		//
		return fCategoryIteration.get(iteration);
	}

	private List<List<CompletionProposalCategory>> getCategoryIteration() {
		List<List<CompletionProposalCategory>> sequence = new ArrayList<>();
		sequence.add(getDefaultCategories());
		for (CompletionProposalCategory cat : getSeparateCategories()) {
			sequence.add(Collections.singletonList(cat));
		}
		return sequence;
	}

	private List<CompletionProposalCategory> getDefaultCategories() {
		// default mix - enable all included computers
		List<CompletionProposalCategory> included = getDefaultCategoriesUnchecked();

		if (IDocument.DEFAULT_CONTENT_TYPE.equals(fPartition) && included.isEmpty() && !fCategories.isEmpty())
			if (informUserAboutEmptyDefaultCategory())
				// preferences were restored - recompute the default categories
				included = getDefaultCategoriesUnchecked();

		return included;
	}

	private List<CompletionProposalCategory> getDefaultCategoriesUnchecked() {
		List<CompletionProposalCategory> included = new ArrayList<>();
		for (Object element : fCategories) {
			CompletionProposalCategory category = (CompletionProposalCategory) element;
			if (category.isIncluded() && category.hasComputers(fPartition))
				included.add(category);
		}
		return included;
	}

	/**
	 * Informs the user about the fact that there are no enabled categories in the default content
	 * assist set and shows a link to the preferences.
	 */
	private boolean informUserAboutEmptyDefaultCategory() {
		if (OptionalMessageDialog.isDialogEnabled(PREF_WARN_ABOUT_EMPTY_ASSIST_CATEGORY)) {
			final Shell shell = CUIPlugin.getActiveWorkbenchShell();
			String title = ContentAssistMessages.ContentAssistProcessor_all_disabled_title;
			String message = ContentAssistMessages.ContentAssistProcessor_all_disabled_message;
			// see PreferencePage#createControl for the 'defaults' label
			final String restoreButtonLabel = JFaceResources.getString("defaults"); //$NON-NLS-1$
			final String linkMessage = Messages.format(
					ContentAssistMessages.ContentAssistProcessor_all_disabled_preference_link,
					LegacyActionTools.removeMnemonics(restoreButtonLabel));
			final int restoreId = IDialogConstants.CLIENT_ID + 10;
			final int settingsId = IDialogConstants.CLIENT_ID + 11;
			final OptionalMessageDialog dialog = new OptionalMessageDialog(PREF_WARN_ABOUT_EMPTY_ASSIST_CATEGORY, shell,
					title, null /* default image */, message, MessageDialog.WARNING,
					new String[] { restoreButtonLabel, IDialogConstants.CLOSE_LABEL }, 1) {
				@Override
				protected Control createCustomArea(Composite composite) {
					// wrap link and checkbox in one composite without space
					Composite parent = new Composite(composite, SWT.NONE);
					GridLayout layout = new GridLayout();
					layout.marginHeight = 0;
					layout.marginWidth = 0;
					layout.verticalSpacing = 0;
					parent.setLayout(layout);

					Composite linkComposite = new Composite(parent, SWT.NONE);
					layout = new GridLayout();
					layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
					layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
					layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
					linkComposite.setLayout(layout);

					Link link = new Link(linkComposite, SWT.NONE);
					link.setText(linkMessage);
					link.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							setReturnCode(settingsId);
							close();
						}
					});
					GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
					gridData.widthHint = this.getMinimumMessageWidth();
					link.setLayoutData(gridData);

					// create checkbox and "don't show this message" prompt
					super.createCustomArea(parent);

					return parent;
				}

				@Override
				protected void createButtonsForButtonBar(Composite parent) {
					Button[] buttons = new Button[2];
					buttons[0] = createButton(parent, restoreId, restoreButtonLabel, false);
					buttons[1] = createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
					setButtons(buttons);
				}
			};
			int returnValue = dialog.open();
			if (restoreId == returnValue || settingsId == returnValue) {
				if (restoreId == returnValue) {
					IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
					store.setToDefault(PreferenceConstants.CODEASSIST_CATEGORY_ORDER);
					store.setToDefault(PreferenceConstants.CODEASSIST_EXCLUDED_CATEGORIES);
				}
				if (settingsId == returnValue)
					PreferencesUtil.createPreferenceDialogOn(shell,
							"org.eclipse.cdt.ui.preferences.CodeAssistPreferenceAdvanced", null, null).open(); //$NON-NLS-1$
				CompletionProposalComputerRegistry registry = CompletionProposalComputerRegistry.getDefault();
				registry.reload();
				return true;
			}
		}
		return false;
	}

	private List<CompletionProposalCategory> getSeparateCategories() {
		ArrayList<CompletionProposalCategory> sorted = new ArrayList<>();
		for (Object element : fCategories) {
			CompletionProposalCategory category = (CompletionProposalCategory) element;
			if (category.isSeparateCommand() && category.hasComputers(fPartition))
				sorted.add(category);
		}
		Collections.sort(sorted, ORDER_COMPARATOR);
		return sorted;
	}

	private String createEmptyMessage() {
		return Messages.format(ContentAssistMessages.ContentAssistProcessor_empty_message,
				getCategoryLabel(fRepetition));
	}

	private String createIterationMessage() {
		return Messages.format(ContentAssistMessages.ContentAssistProcessor_toggle_affordance_update_message,
				getCategoryLabel(fRepetition), fIterationGesture, getCategoryLabel(fRepetition + 1));
	}

	private String getCategoryLabel(int repetition) {
		int iteration = repetition % fCategoryIteration.size();
		if (iteration == 0)
			return ContentAssistMessages.ContentAssistProcessor_defaultProposalCategory;
		return toString(fCategoryIteration.get(iteration).get(0));
	}

	private String toString(CompletionProposalCategory category) {
		return category.getDisplayName();
	}

	private String getIterationGesture() {
		TriggerSequence binding = getIterationBinding();
		return binding != null
				? Messages.format(ContentAssistMessages.ContentAssistProcessor_toggle_affordance_press_gesture,
						new Object[] { binding.format() })
				: ContentAssistMessages.ContentAssistProcessor_toggle_affordance_click_gesture;
	}

	private KeySequence getIterationBinding() {
		final IBindingService bindingSvc = PlatformUI.getWorkbench().getAdapter(IBindingService.class);
		TriggerSequence binding = bindingSvc
				.getBestActiveBindingFor(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		if (binding instanceof KeySequence)
			return (KeySequence) binding;
		return null;
	}
}
