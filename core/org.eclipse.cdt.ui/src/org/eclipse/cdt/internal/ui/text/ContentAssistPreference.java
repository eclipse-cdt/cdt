package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;



public class ContentAssistPreference {
	
	/** Preference key for content assist auto activation */
	public final static String AUTOACTIVATION=  "content_assist_autoactivation";
	/** Preference key for content assist auto activation delay */
	public final static String AUTOACTIVATION_DELAY=  "content_assist_autoactivation_delay";
	/** Preference key for content assist proposal color */
	public final static String PROPOSALS_FOREGROUND=  "content_assist_proposals_foreground";
	/** Preference key for content assist proposal color */
	public final static String PROPOSALS_BACKGROUND=  "content_assist_proposals_background";
	/** Preference key for content assist parameters color */
	public final static String PARAMETERS_FOREGROUND=  "content_assist_parameters_foreground";
	/** Preference key for content assist parameters color */
	public final static String PARAMETERS_BACKGROUND=  "content_assist_parameters_background";
	/** Preference key for content assist auto insert */
	public final static String AUTOINSERT=  "content_assist_autoinsert";
	
	/** Preference key for java content assist auto activation triggers */
	public final static String AUTOACTIVATION_TRIGGERS_C= "content_assist_autoactivation_triggers_java";
	
	/** Preference key for visibility of proposals */
	public final static String SHOW_DOCUMENTED_PROPOSALS= "content_assist_show_visible_proposals";
	/** Preference key for alphabetic ordering of proposals */
	public final static String ORDER_PROPOSALS= "content_assist_order_proposals";
	/** Preference key for case sensitivity of propsals */
	public final static String CASE_SENSITIVITY= "content_assist_case_sensitivity";
	/** Preference key for adding imports on code assist */
	public final static String ADD_INCLUDE= "content_assist_add_import";	
	
	private static Color getColor(IPreferenceStore store, String key, IColorManager manager) {
		RGB rgb= PreferenceConverter.getColor(store, key);
		return manager.getColor(rgb);
	}
	
	private static Color getColor(IPreferenceStore store, String key) {
		CTextTools textTools= CUIPlugin.getDefault().getTextTools();
		return getColor(store, key, textTools.getColorManager());
	}
	
	private static CCompletionProcessor getCProcessor(ContentAssistant assistant) {
		IContentAssistProcessor p= assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
		if (p instanceof CCompletionProcessor)
			return  (CCompletionProcessor) p;
		return null;
	}
	
	private static void configureCProcessor(ContentAssistant assistant, IPreferenceStore store) {
		CCompletionProcessor jcp= getCProcessor(assistant);
		if (jcp == null)
			return;
			
		String triggers= store.getString(AUTOACTIVATION_TRIGGERS_C);
		if (triggers != null)
			jcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
			
		boolean enabled= store.getBoolean(SHOW_DOCUMENTED_PROPOSALS);
		//jcp.restrictProposalsToVisibility(enabled);
		
		enabled= store.getBoolean(CASE_SENSITIVITY);
		jcp.restrictProposalsToMatchingCases(enabled);
		
		enabled= store.getBoolean(ORDER_PROPOSALS);
		jcp.orderProposalsAlphabetically(enabled);
		
		enabled= store.getBoolean(ADD_INCLUDE);
		jcp.allowAddingIncludes(enabled);		
	}

	
	/**
	 * Configure the given content assistant from the given store.
	 */
	public static void configure(ContentAssistant assistant, IPreferenceStore store) {
		
		CTextTools textTools= CUIPlugin.getDefault().getTextTools();
		IColorManager manager= textTools.getColorManager();		
		
		
		boolean enabled= store.getBoolean(AUTOACTIVATION);
		assistant.enableAutoActivation(enabled);
		
		int delay= store.getInt(AUTOACTIVATION_DELAY);
		assistant.setAutoActivationDelay(delay);
		
		//Color c= getColor(store, PROPOSALS_FOREGROUND, manager);
		//assistant.setProposalSelectorForeground(c);
		
		Color c= getColor(store, PROPOSALS_BACKGROUND, manager);
		assistant.setContextInformationPopupBackground(c);
		
		c= getColor(store, PARAMETERS_FOREGROUND, manager);
		//assistant.setContextInformationPopupForeground(c);
		//assistant.setContextSelectorForeground(c);
		
		c= getColor(store, PARAMETERS_BACKGROUND, manager);
		assistant.setContextInformationPopupBackground(c);
		//assistant.setContextSelectorBackground(c);
		
		enabled= store.getBoolean(AUTOINSERT);
		//assistant.enableAutoInsert(enabled);

		configureCProcessor(assistant, store);
	}
	
	
	private static void changeCProcessor(ContentAssistant assistant, IPreferenceStore store, String key) {
		CCompletionProcessor jcp= getCProcessor(assistant);
		if (jcp == null)
			return;
			
		if (AUTOACTIVATION_TRIGGERS_C.equals(key)) {
			String triggers= store.getString(AUTOACTIVATION_TRIGGERS_C);
			if (triggers != null)
				jcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
		} else if (SHOW_DOCUMENTED_PROPOSALS.equals(key)) {
			//boolean enabled= store.getBoolean(SHOW_DOCUMENTED_PROPOSALS);
			//jcp.restrictProposalsToVisibility(enabled);
		} else if (CASE_SENSITIVITY.equals(key)) {
			boolean enabled= store.getBoolean(CASE_SENSITIVITY);
			jcp.restrictProposalsToMatchingCases(enabled);
		} else if (ORDER_PROPOSALS.equals(key)) {
			boolean enable= store.getBoolean(ORDER_PROPOSALS);
			jcp.orderProposalsAlphabetically(enable);
		} else if (ADD_INCLUDE.equals(key)) {
			boolean enabled= store.getBoolean(ADD_INCLUDE);
			jcp.allowAddingIncludes(enabled);
		}
	}
	
	/**
	 * Changes the configuration of the given content assistant according to the given property
	 * change event and the given preference store.
	 */
	public static void changeConfiguration(ContentAssistant assistant, IPreferenceStore store, PropertyChangeEvent event) {
		
		String p= event.getProperty();
		
		if (AUTOACTIVATION.equals(p)) {
			boolean enabled= store.getBoolean(AUTOACTIVATION);
			assistant.enableAutoActivation(enabled);
		} else if (AUTOACTIVATION_DELAY.equals(p)) {
			int delay= store.getInt(AUTOACTIVATION_DELAY);
			assistant.setAutoActivationDelay(delay);
		} else if (PROPOSALS_FOREGROUND.equals(p)) {
			//Color c= getColor(store, PROPOSALS_FOREGROUND);
			//assistant.setProposalSelectorForeground(c);
		} else if (PROPOSALS_BACKGROUND.equals(p)) {
			//Color c= getColor(store, PROPOSALS_BACKGROUND);
			//assistant.setProposalSelectorBackground(c);
		} else if (PARAMETERS_FOREGROUND.equals(p)) {
			//Color c= getColor(store, PARAMETERS_FOREGROUND);
			//assistant.setContextInformationPopupForeground(c);
			//assistant.setContextSelectorForeground(c);
		} else if (PARAMETERS_BACKGROUND.equals(p)) {
			Color c= getColor(store, PARAMETERS_BACKGROUND);
			assistant.setContextInformationPopupBackground(c);
			//assistant.setContextSelectorBackground(c);
		} else if (AUTOINSERT.equals(p)) {
			//boolean enabled= store.getBoolean(AUTOINSERT);
			//assistant.enableAutoInsert(enabled);
		}
		
		changeCProcessor(assistant, store, p);
	}
}



