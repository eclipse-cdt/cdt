/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * CCommentScanner.java
 */
public class CCommentScanner extends AbstractCScanner
{
    private static class TaskTagDetector implements IWordDetector {

        public boolean isWordStart(char c) {
            return Character.isLetter(c);
        }

        public boolean isWordPart(char c) {
            return Character.isLetter(c);
        }
    }

    private class TaskTagRule extends WordRule {

        private IToken fToken;

        public TaskTagRule(IToken token) {
            super(new TaskTagDetector(), Token.UNDEFINED);
            fToken= token;
        }
    
        public void clearTaskTags() {
            fWords.clear();
        }
    
        public void addTaskTags(String value) {
            String[] tasks= split(value, ","); //$NON-NLS-1$
            for (int i= 0; i < tasks.length; i++) {
                if (tasks[i].length() > 0) {
                    addWord(tasks[i], fToken);
                }
            }
        }
        
        private String[] split(String value, String delimiters) {
            StringTokenizer tokenizer= new StringTokenizer(value, delimiters);
            int size= tokenizer.countTokens();
            String[] tokens= new String[size];
            int i= 0;
            while (i < size)
                tokens[i++]= tokenizer.nextToken();
            return tokens;
        }
    }
    
    private static final String TRANSLATION_TASK_TAGS= CCorePreferenceConstants.TRANSLATION_TASK_TAGS;    
    protected static final String TASK_TAG= ICColorConstants.TASK_TAG;

    private TaskTagRule fTaskTagRule;
    private Preferences fCorePreferenceStore;
    private String fDefaultTokenProperty;
    private String[] fTokenProperties;

    public CCommentScanner(IColorManager manager, IPreferenceStore store, Preferences coreStore, String defaultTokenProperty) {
        this(manager, store, coreStore, defaultTokenProperty, new String[] { defaultTokenProperty, TASK_TAG });
    }
    
    public CCommentScanner(IColorManager manager, IPreferenceStore store, Preferences coreStore, String defaultTokenProperty, String[] tokenProperties) {
        super(manager, store);
        
        fCorePreferenceStore= coreStore;
        fDefaultTokenProperty= defaultTokenProperty;
        fTokenProperties= tokenProperties;

        initialize();
    }

    /*
     * @see AbstractCScanner#createRules()
     */
    protected List createRules() {
        List list= new ArrayList();
        
        if (fCorePreferenceStore != null) {
            // Add rule for Task Tags.
            fTaskTagRule= new TaskTagRule(getToken(TASK_TAG));
            String tasks= fCorePreferenceStore.getString(TRANSLATION_TASK_TAGS);
            fTaskTagRule.addTaskTags(tasks);
            list.add(fTaskTagRule);
        }

        setDefaultReturnToken(getToken(fDefaultTokenProperty));

        return list;
    }

    /*
     * @see org.eclipse.cdt.internal.ui.text.AbstractJavaScanner#affectsBehavior(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public boolean affectsBehavior(PropertyChangeEvent event) {
        return event.getProperty().equals(TRANSLATION_TASK_TAGS) || super.affectsBehavior(event);
    }

    /*
     * @see org.eclipse.cdt.internal.ui.text.AbstractJavaScanner#adaptToPreferenceChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void adaptToPreferenceChange(PropertyChangeEvent event) {
        if (fTaskTagRule != null && event.getProperty().equals(TRANSLATION_TASK_TAGS)) {
            Object value= event.getNewValue();

            if (value instanceof String) {
                fTaskTagRule.clearTaskTags();
                fTaskTagRule.addTaskTags((String) value);
            }
            
        } else if (super.affectsBehavior(event)) {
            super.adaptToPreferenceChange(event);
        }
    }

    /*
     * @see org.eclipse.cdt.internal.ui.text.AbstractJavaScanner#getTokenProperties()
     */
    protected String[] getTokenProperties() {
        return fTokenProperties;
    }

}
