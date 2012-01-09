/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.text.spelling.engine.DefaultSpellChecker;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellCheckEngine;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellDictionary;
import org.eclipse.cdt.internal.ui.text.spelling.engine.LocaleSensitiveSpellDictionary;
import org.eclipse.cdt.internal.ui.text.spelling.engine.PersistentSpellDictionary;

/**
 * Spell check engine for C/C++ source spell checking.
 */
public class SpellCheckEngine implements ISpellCheckEngine, IPropertyChangeListener {
	/** The dictionary location */
	public static final String DICTIONARY_LOCATION= "dictionaries/"; //$NON-NLS-1$

	/** The singleton engine instance */
	private static ISpellCheckEngine fgEngine= null;
	
	/**
	 * Caches the locales of installed dictionaries.
	 */
	private static Set<Locale> fgLocalesWithInstalledDictionaries;

	/**
	 * Returns the locales for which this
	 * spell check engine has dictionaries.
	 *
	 * @return The available locales for this engine
	 */
	public static Set<Locale> getLocalesWithInstalledDictionaries() {
		if (fgLocalesWithInstalledDictionaries != null)
			return fgLocalesWithInstalledDictionaries;
		
		URL location;
		try {
			location= getDictionaryLocation();
			if (location == null)
				return fgLocalesWithInstalledDictionaries= Collections.emptySet();
		} catch (MalformedURLException ex) {
			CUIPlugin.log(ex);
			return fgLocalesWithInstalledDictionaries= Collections.emptySet();
		}
		
		String[] fileNames;
		try {
			URL url= FileLocator.toFileURL(location);
			File file= new File(url.getFile());
			if (!file.isDirectory())
				return fgLocalesWithInstalledDictionaries= Collections.emptySet();
			fileNames= file.list();
			if (fileNames == null)
				return fgLocalesWithInstalledDictionaries= Collections.emptySet();
		} catch (IOException ex) {
			CUIPlugin.log(ex);
			return fgLocalesWithInstalledDictionaries= Collections.emptySet();
		}
		
		fgLocalesWithInstalledDictionaries= new HashSet<Locale>();
		int fileNameCount= fileNames.length;
		for (int i= 0; i < fileNameCount; i++) {
			String fileName= fileNames[i];
			int localeEnd= fileName.indexOf(".dictionary"); //$NON-NLS-1$ 
			if (localeEnd > 1) {
				String localeName= fileName.substring(0, localeEnd);
				int languageEnd=localeName.indexOf('_');
				if (languageEnd == -1) {
					fgLocalesWithInstalledDictionaries.add(new Locale(localeName));
				} else if (languageEnd == 2 && localeName.length() == 5) {
					fgLocalesWithInstalledDictionaries.add(new Locale(localeName.substring(0, 2), localeName.substring(3)));
				} else if (localeName.length() > 6 && localeName.charAt(5) == '_') {
					fgLocalesWithInstalledDictionaries.add(new Locale(localeName.substring(0, 2), localeName.substring(3, 5), localeName.substring(6)));
				}
			}
		}

		return fgLocalesWithInstalledDictionaries;
	}

	/**
	 * Returns the default locale for this engine.
	 *
	 * @return The default locale
	 */
	public static Locale getDefaultLocale() {
		return Locale.getDefault();
	}
	
	/**
	 * Returns the dictionary closest to the given locale.
	 *
	 * @param locale the locale
	 * @return the dictionary or <code>null</code> if none is suitable
	 */
	public ISpellDictionary findDictionary(Locale locale) {
		ISpellDictionary dictionary= fLocaleDictionaries.get(locale);
		if (dictionary != null)
			return dictionary;
		
		// Try same language
		String language= locale.getLanguage();
		for (Entry<Locale, ISpellDictionary> entry : fLocaleDictionaries.entrySet()) {
			Locale dictLocale= entry.getKey();
			if (dictLocale.getLanguage().equals(language))
				return entry.getValue();
		}
		
		return null;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellCheckEngine#findDictionary(java.util.Locale)
	 */
	public static Locale findClosestLocale(Locale locale) {
		if (locale == null || locale.toString().length() == 0)
			return locale;
		
		if (getLocalesWithInstalledDictionaries().contains(locale))
			return locale;

		// Try same language
		String language= locale.getLanguage();
		for (Locale dictLocale : getLocalesWithInstalledDictionaries()) {
			if (dictLocale.getLanguage().equals(language))
				return dictLocale;
		}
		
		// Try whether American English is present
		Locale defaultLocale= Locale.US;
		if (getLocalesWithInstalledDictionaries().contains(defaultLocale))
			return defaultLocale;
		
		return null;
	}

	/**
	 * Returns the URL for the dictionary location where
	 * the Platform dictionaries are located.
	 * <p>
	 * This is in <code>org.eclipse.cdt.ui/dictionaries/</code>
	 * which can also be populated via fragments.
	 * </p>
	 *
	 * @throws MalformedURLException if the URL could not be created
	 * @return The dictionary location, or <code>null</code> iff the location is not known
	 */
	public static URL getDictionaryLocation() throws MalformedURLException {
		// Unfortunately, dictionaries used by JDT are not accessible,
		// so we have to provide our own copies of the same files.
		final CUIPlugin plugin= CUIPlugin.getDefault();
		if (plugin != null)
			return plugin.getBundle().getEntry("/" + DICTIONARY_LOCATION); //$NON-NLS-1$

		return null;
	}

	/**
	 * Returns the singleton instance of the spell check engine.
	 *
	 * @return The singleton instance of the spell check engine
	 */
	public static final synchronized ISpellCheckEngine getInstance() {
		if (fgEngine == null)
			fgEngine= new SpellCheckEngine();

		return fgEngine;
	}
	
	/**
	 * Shuts down the singleton instance of the spell check engine.
	 */
	public static final synchronized void shutdownInstance() {
		if (fgEngine != null) {
			fgEngine.shutdown();
			fgEngine= null;
		}
	}

	/** The registered locale insensitive dictionaries */
	private Set<ISpellDictionary> fGlobalDictionaries= new HashSet<ISpellDictionary>();

	/** The spell checker for fLocale */
	private ISpellChecker fChecker= null;

	/** The registered locale sensitive dictionaries */
	private Map<Locale, ISpellDictionary> fLocaleDictionaries= new HashMap<Locale, ISpellDictionary>();

	/** The user dictionary */
	private ISpellDictionary fUserDictionary= null;

	/**
	 * Creates a new spell check manager.
	 */
	private SpellCheckEngine() {
		fGlobalDictionaries.add(new TaskTagDictionary());
		fGlobalDictionaries.add(new HtmlTagDictionary());

		try {
			final URL location= getDictionaryLocation();

			for (Locale locale : getLocalesWithInstalledDictionaries()) {
				fLocaleDictionaries.put(locale, new LocaleSensitiveSpellDictionary(locale, location));
			}
		} catch (MalformedURLException exception) {
			// Do nothing
		}
		
		SpellingPreferences.addPropertyChangeListener(this);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellCheckEngine#getSpellChecker()
	 */
	@Override
	public final synchronized ISpellChecker getSpellChecker() throws IllegalStateException {
		if (fGlobalDictionaries == null)
			throw new IllegalStateException("spell checker has been shut down"); //$NON-NLS-1$
		
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		Locale locale= getCurrentLocale(store);
		if (fUserDictionary == null && "".equals(locale.toString())) //$NON-NLS-1$
			return null;
		
		if (fChecker != null && fChecker.getLocale().equals(locale))
			return fChecker;
		
		resetSpellChecker();
		
		fChecker= new DefaultSpellChecker(store, locale);
		resetUserDictionary();
		
		for (ISpellDictionary dictionary : fGlobalDictionaries) {
			fChecker.addDictionary(dictionary);
		}

		ISpellDictionary dictionary= findDictionary(fChecker.getLocale());
		if (dictionary != null)
			fChecker.addDictionary(dictionary);

		return fChecker;
	}

	/**
	 * Returns the current locale of the spelling preferences.
	 *
	 * @param store the preference store
	 * @return The current locale of the spelling preferences
	 */
	private Locale getCurrentLocale(IPreferenceStore store) {
		return convertToLocale(SpellingPreferences.getSpellingLocale());
	}
	
	public static Locale convertToLocale(String locale) {
		Locale defaultLocale= SpellCheckEngine.getDefaultLocale();
		if (locale.equals(defaultLocale.toString()))
			return defaultLocale;
		
		if (locale.length() >= 5)
			return new Locale(locale.substring(0, 2), locale.substring(3, 5));
		
		return new Locale(""); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.cdt.ui.text.spelling.engine.ISpellCheckEngine#getLocale()
	 */
	@Override
	public synchronized final Locale getLocale() {
		if (fChecker == null)
			return null;
		
		return fChecker.getLocale();
	}

	/*
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public final void propertyChange(final PropertyChangeEvent event) {
		if (event.getProperty().equals(SpellingPreferences.SPELLING_LOCALE)) {
			resetSpellChecker();
			return;
		}
		
		if (event.getProperty().equals(SpellingPreferences.SPELLING_USER_DICTIONARY)) {
			resetUserDictionary();
			return;
		}
		
		if (event.getProperty().equals(SpellingPreferences.SPELLING_USER_DICTIONARY_ENCODING)) {
			resetUserDictionary();
			return;
		}
	}

	/**
	 * Resets the current checker's user dictionary.
	 */
	private synchronized void resetUserDictionary() {
		if (fChecker == null)
			return;
		
		// Update user dictionary
		if (fUserDictionary != null) {
			fChecker.removeDictionary(fUserDictionary);
			fUserDictionary.unload();
			fUserDictionary= null;
		}

		final String filePath= SpellingPreferences.getSpellingUserDictionary();
		if (filePath.length() > 0) {
			try {
				File file= new File(filePath);
				if (!file.exists() && !file.createNewFile())
					return;
				
				final URL url= new URL("file", null, filePath); //$NON-NLS-1$
				InputStream stream= url.openStream();
				if (stream != null) {
					try {
						fUserDictionary= new PersistentSpellDictionary(url);
						fChecker.addDictionary(fUserDictionary);
					} finally {
						stream.close();
					}
				}
			} catch (MalformedURLException exception) {
				// Do nothing
			} catch (IOException exception) {
				// Do nothing
			}
		}
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellCheckEngine#registerDictionary(org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellDictionary)
	 */
	@Override
	public synchronized final void registerGlobalDictionary(final ISpellDictionary dictionary) {
		fGlobalDictionaries.add(dictionary);
		resetSpellChecker();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellCheckEngine#registerDictionary(java.util.Locale, org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellDictionary)
	 */
	@Override
	public synchronized final void registerDictionary(final Locale locale, final ISpellDictionary dictionary) {
		fLocaleDictionaries.put(locale, dictionary);
		resetSpellChecker();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellCheckEngine#unload()
	 */
	@Override
	public synchronized final void shutdown() {
		SpellingPreferences.removePropertyChangeListener(this);

		for (ISpellDictionary dictionary : fGlobalDictionaries) {
			dictionary.unload();
		}
		fGlobalDictionaries= null;

		for (ISpellDictionary dictionary : fLocaleDictionaries.values()) {
			dictionary.unload();
		}
		fLocaleDictionaries= null;

		fUserDictionary= null;
		fChecker= null;
	}
	
	private synchronized void resetSpellChecker() {
		if (fChecker != null) {
			ISpellDictionary dictionary= fLocaleDictionaries.get(fChecker.getLocale());
			if (dictionary != null)
				dictionary.unload();
		}
		fChecker= null;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.spelling.engine.ISpellCheckEngine#unregisterDictionary(org.eclipse.cdt.ui.text.spelling.engine.ISpellDictionary)
	 */
	@Override
	public synchronized final void unregisterDictionary(final ISpellDictionary dictionary) {
		fGlobalDictionaries.remove(dictionary);
		fLocaleDictionaries.values().remove(dictionary);
		dictionary.unload();
		resetSpellChecker();
	}
}
