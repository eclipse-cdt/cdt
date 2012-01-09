/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Kirk Beitz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.IColorManager;

import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;

import org.eclipse.cdt.internal.ui.text.CTextTools;



public class ContentAssistPreference {
	
//	/** Preference key for content assist auto activation (unused) */
//	public final static String AUTOACTIVATION=  "content_assist_autoactivation";
	/** Preference key for content assist auto activation delay */
	public final static String AUTOACTIVATION_DELAY=  "content_assist_autoactivation_delay"; //$NON-NLS-1$
	/** Preference key for content assist timeout delay (unused) */
	public final static String TIMEOUT_DELAY=  "content_assist_timeout_delay"; //$NON-NLS-1$
	/** Preference key for completion proposal timeout */
	public final static String PROPOSALS_TIMEOUT=  PreferenceConstants.CODEASSIST_PROPOSALS_TIMEOUT;
	/** Preference key for content assist proposal color */
	public final static String PROPOSALS_FOREGROUND=  PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND;
	/** Preference key for content assist proposal color */
	public final static String PROPOSALS_BACKGROUND=  PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND;
	/** Preference key for content assist parameters color */
	public final static String PARAMETERS_FOREGROUND=  PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND;
	/** Preference key for content assist parameters color */
	public final static String PARAMETERS_BACKGROUND=  PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND;
	/** Preference key for content assist auto insert */
	public final static String AUTOINSERT=  "content_assist_autoinsert"; //$NON-NLS-1$
	/** Preference key for content assist to insert the common prefix */
	public final static String PREFIX_COMPLETION= "content_assist_prefix_completion"; //$NON-NLS-1$

	/** Preference key for C/CPP content assist auto activation triggers */
	public final static String AUTOACTIVATION_TRIGGERS_DOT= "content_assist_autoactivation_trigger_dot"; //$NON-NLS-1$
	public final static String AUTOACTIVATION_TRIGGERS_ARROW= "content_assist_autoactivation_trigger_arrow"; //$NON-NLS-1$
	public final static String AUTOACTIVATION_TRIGGERS_DOUBLECOLON= "content_assist_autoactivation_trigger_doublecolon"; //$NON-NLS-1$
	public final static String AUTOACTIVATION_TRIGGERS_REPLACE_DOT_WITH_ARROW= "content_assist_autoactivation_trigger_replace_dot_with_arrow"; //$NON-NLS-1$
//	/** Preference key for visibility of proposals (unused) */
//	public final static String SHOW_DOCUMENTED_PROPOSALS= "content_assist_show_visible_proposals"; //$NON-NLS-1$
	/** Preference key for alphabetic ordering of proposals */
	public final static String ORDER_PROPOSALS= "content_assist_order_proposals"; //$NON-NLS-1$
//	/** Preference key for case sensitivity of propsals */
//	public final static String CASE_SENSITIVITY= "content_assist_case_sensitivity";
//	/** Preference key for adding includes on code assist (unused) */
//	public final static String ADD_INCLUDE= "content_assist_add_import";	 //$NON-NLS-1$
	/** Preference key for completion search scope (unused) */
	public final static String CURRENT_FILE_SEARCH_SCOPE= "content_assist_current_file_search_scope";	 //$NON-NLS-1$
	/** Preference key for completion search scope (unused) */
	public final static String PROJECT_SEARCH_SCOPE= "content_assist_project_search_scope";	 //$NON-NLS-1$
	/** Preference key for completion filtering */
	public final static String PROPOSALS_FILTER= "content_assist_proposal_filter"; //$NON-NLS-1$
	
	/** Key for boolean preference telling whether camel case/underscore matches are to be shown by content assist features or not*/
	public static final String SHOW_CAMEL_CASE_MATCHES = "contentAssist.showCamelCaseMatches"; //$NON-NLS-1$

	private static Color getColor(IPreferenceStore store, String key, IColorManager manager) {
		RGB rgb= PreferenceConverter.getColor(store, key);
		return manager.getColor(rgb);
	}
	
	private static Color getColor(IPreferenceStore store, String key) {
		CTextTools textTools= CUIPlugin.getDefault().getTextTools();
		return getColor(store, key, textTools.getColorManager());
	}
	
	private static CContentAssistProcessor getCProcessor(ContentAssistant assistant) {
		IContentAssistProcessor p= assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
		if (p instanceof CContentAssistProcessor)
			return  (CContentAssistProcessor) p;
		return null;
	}
	
	private static void configureCProcessor(ContentAssistant assistant, IPreferenceStore store) {
		CContentAssistProcessor ccp= getCProcessor(assistant);
		if (ccp == null)
			return;
		configureActivationCharacters(store, ccp);

//		boolean enabled;
//		enabled= store.getBoolean(SHOW_DOCUMENTED_PROPOSALS);
//		ccp.restrictProposalsToVisibility(enabled);
//
//		enabled= store.getBoolean(CASE_SENSITIVITY);
//		ccp.restrictProposalsToMatchingCases(enabled);
		
//		enabled= store.getBoolean(ORDER_PROPOSALS);
//		ccp.orderProposalsAlphabetically(enabled);
		
//		enabled= store.getBoolean(ADD_INCLUDE);
//		ccp.allowAddingIncludes(enabled);
	}

	private static void configureActivationCharacters(IPreferenceStore store, CContentAssistProcessor ccp) {
		String triggers = ""; //$NON-NLS-1$
		boolean useDotAsTrigger = store.getBoolean(AUTOACTIVATION_TRIGGERS_DOT);
		boolean useArrowAsTrigger = store.getBoolean(AUTOACTIVATION_TRIGGERS_ARROW);
		boolean useDoubleColonAsTrigger = store.getBoolean(AUTOACTIVATION_TRIGGERS_DOUBLECOLON);

		if (useDotAsTrigger)
			triggers = ".";	//$NON-NLS-1$
		if (useArrowAsTrigger)
			triggers += ">"; //$NON-NLS-1$
		if (useDoubleColonAsTrigger)
			triggers += ":"; //$NON-NLS-1$
		ccp.setCContentAutoActivationCharacters(triggers);

		boolean dotTriggersAutoReplace = store.getBoolean(AUTOACTIVATION_TRIGGERS_REPLACE_DOT_WITH_ARROW);

		// quick and dirty, since we only have one thing to replace
		// if other replacement auto-activate triggers are added,
		// triggers will have to be cleared and characters that share
		// such as "." will have to be ||ed together.
		if (!useDotAsTrigger && dotTriggersAutoReplace)
			triggers += "."; //$NON-NLS-1$
		ccp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());

		triggers = "";	//$NON-NLS-1$
		if (dotTriggersAutoReplace)
			triggers = ".";	//$NON-NLS-1$
		ccp.setReplacementAutoActivationCharacters(triggers);
	}

	
	/**
	 * Configure the given content assistant from the given store.
	 */
	public static void configure(ContentAssistant assistant, IPreferenceStore store) {
			
		CTextTools textTools= CUIPlugin.getDefault().getTextTools();
		IColorManager manager= textTools.getColorManager();
		
		boolean enabledDot= store.getBoolean(AUTOACTIVATION_TRIGGERS_DOT);
		boolean enabledArrow= store.getBoolean(AUTOACTIVATION_TRIGGERS_ARROW);
		boolean enabledDoubleColon= store.getBoolean(AUTOACTIVATION_TRIGGERS_DOUBLECOLON);
		boolean enabledReplaceDotWithArrow
			= store.getBoolean(AUTOACTIVATION_TRIGGERS_REPLACE_DOT_WITH_ARROW);

		boolean enabled
			=  (enabledDot || enabledArrow || enabledDoubleColon
					|| enabledReplaceDotWithArrow);
		assistant.enableAutoActivation(enabled);
		
		int delay= store.getInt(AUTOACTIVATION_DELAY);
		assistant.setAutoActivationDelay(delay);
		
		Color c1= getColor(store, PROPOSALS_FOREGROUND, manager);
		assistant.setProposalSelectorForeground(c1);
		
		Color c2= getColor(store, PROPOSALS_BACKGROUND, manager);
		assistant.setProposalSelectorBackground(c2);
		
		Color c3= getColor(store, PARAMETERS_FOREGROUND, manager);
		assistant.setContextInformationPopupForeground(c3);
		assistant.setContextSelectorForeground(c3);
		
		Color c4= getColor(store, PARAMETERS_BACKGROUND, manager);
		assistant.setContextInformationPopupBackground(c4);
		assistant.setContextSelectorBackground(c4);
		
		enabled= store.getBoolean(AUTOINSERT);
		assistant.enableAutoInsert(enabled);

		enabled = store.getBoolean(PREFIX_COMPLETION);
		assistant.enablePrefixCompletion(enabled);

		configureCProcessor(assistant, store);
	}
	
	
	private static void changeCProcessor(ContentAssistant assistant, IPreferenceStore store, String key) {
		CContentAssistProcessor ccp= getCProcessor(assistant);
		if (ccp == null)
			return;
			
		if ( (AUTOACTIVATION_TRIGGERS_DOT.equals(key))
		     || (AUTOACTIVATION_TRIGGERS_ARROW.equals(key))
			 || (AUTOACTIVATION_TRIGGERS_DOUBLECOLON.equals(key)) 
			 || (AUTOACTIVATION_TRIGGERS_REPLACE_DOT_WITH_ARROW.equals(key))){
			configureActivationCharacters(store, ccp);
		}
//		else if (SHOW_DOCUMENTED_PROPOSALS.equals(key)) {
//			boolean enabled= store.getBoolean(SHOW_DOCUMENTED_PROPOSALS);
//			ccp.restrictProposalsToVisibility(enabled);
//		}
//		else if (CASE_SENSITIVITY.equals(key)) {
//			boolean enabled= store.getBoolean(CASE_SENSITIVITY);
//			ccp.restrictProposalsToMatchingCases(enabled);
//		}
//		else if (ORDER_PROPOSALS.equals(key)) {
//			boolean enable= store.getBoolean(ORDER_PROPOSALS);
//			ccp.orderProposalsAlphabetically(enable);
//		} else if (ADD_INCLUDE.equals(key)) {
//			boolean enabled= store.getBoolean(ADD_INCLUDE);
//			ccp.allowAddingIncludes(enabled);
//		}
	}
	
	/**
	 * Changes the configuration of the given content assistant according to the given property
	 * change event and the given preference store.
	 */
	public static void changeConfiguration(ContentAssistant assistant, IPreferenceStore store, PropertyChangeEvent event) {
		
		String p= event.getProperty();
		
		if ((AUTOACTIVATION_TRIGGERS_DOT.equals(p))
			|| (AUTOACTIVATION_TRIGGERS_ARROW.equals(p))
			|| (AUTOACTIVATION_TRIGGERS_DOUBLECOLON.equals(p))
			|| (AUTOACTIVATION_TRIGGERS_REPLACE_DOT_WITH_ARROW.equals(p))){
			boolean enabledDot= store.getBoolean(AUTOACTIVATION_TRIGGERS_DOT);
			boolean enabledArrow= store.getBoolean(AUTOACTIVATION_TRIGGERS_ARROW);
			boolean enabledDoubleColon= store.getBoolean(AUTOACTIVATION_TRIGGERS_DOUBLECOLON);
			boolean enabledReplaceDotWithArrow= store.getBoolean(AUTOACTIVATION_TRIGGERS_REPLACE_DOT_WITH_ARROW);
			boolean enabled =  ((enabledDot) || ( enabledArrow ) || (enabledDoubleColon ) || (enabledReplaceDotWithArrow ));
			assistant.enableAutoActivation(enabled);

		} else if (AUTOACTIVATION_DELAY.equals(p)) {
			int delay= store.getInt(AUTOACTIVATION_DELAY);
			assistant.setAutoActivationDelay(delay);
		} else if (PROPOSALS_FOREGROUND.equals(p)) {
			Color c= getColor(store, PROPOSALS_FOREGROUND);
			assistant.setProposalSelectorForeground(c);
		} else if (PROPOSALS_BACKGROUND.equals(p)) {
			Color c= getColor(store, PROPOSALS_BACKGROUND);
			assistant.setProposalSelectorBackground(c);
		} else if (PARAMETERS_FOREGROUND.equals(p)) {
			Color c= getColor(store, PARAMETERS_FOREGROUND);
			assistant.setContextInformationPopupForeground(c);
			assistant.setContextSelectorForeground(c);
		} else if (PARAMETERS_BACKGROUND.equals(p)) {
			Color c= getColor(store, PARAMETERS_BACKGROUND);
			assistant.setContextInformationPopupBackground(c);
			assistant.setContextSelectorBackground(c);
		} else if (AUTOINSERT.equals(p)) {
			boolean enabled= store.getBoolean(AUTOINSERT);
			assistant.enableAutoInsert(enabled);
		} else if (PREFIX_COMPLETION.equals(p)) {
			boolean enabled= store.getBoolean(PREFIX_COMPLETION);
			assistant.enablePrefixCompletion(enabled);
		}
		
		changeCProcessor(assistant, store, p);
	}
	
	private static ContentAssistPreference instance = null;
	
	private final IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
			
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			String prop = event.getProperty();
			if (prop.equals(ContentAssistPreference.SHOW_CAMEL_CASE_MATCHES)) {
				updateOnPreferences();
			}
			
		}
	};
	
	private ContentAssistPreference() {
		getPreferences().addPropertyChangeListener(
				propertyListener);
		updateOnPreferences();
	}
	
	public static synchronized ContentAssistPreference getInstance() {
		if (instance == null) {
			instance = new ContentAssistPreference();
		}
		
		return instance;
	}
	
	private static IPreferenceStore getPreferences() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}
	
	private synchronized void updateOnPreferences() {
		boolean showCamelCaseMatches = getPreferences().getBoolean(ContentAssistPreference.SHOW_CAMEL_CASE_MATCHES);
		ContentAssistMatcherFactory.getInstance().setShowCamelCaseMatches(showCamelCaseMatches);
	}
	
	private void shutdownInternal() {
		getPreferences().removePropertyChangeListener(propertyListener);
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static synchronized void shutdown() {
		if (instance != null) {
			instance.shutdownInternal();
		}
	}
}
