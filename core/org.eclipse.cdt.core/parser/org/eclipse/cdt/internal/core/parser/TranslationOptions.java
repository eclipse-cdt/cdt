/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.parser.ITranslationOptions;


public class TranslationOptions implements ITranslationOptions  {
	
	// tags used to recognize tasks in comments
	private char[][] taskTags = null;

	// priorities of tasks in comments
	private char[][] taskPriorities = null;
    
	public TranslationOptions(Map settings) {
        initialize(settings);
	}

	/** 
	 * Initializing the translation options with external settings
	 */
	public void initialize(Map settings){

		if (settings == null) return;
		
		// filter out related options
		Iterator entries = settings.entrySet().iterator();
		
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry)entries.next();
			Object oKey = entry.getKey();
			Object oValue = entry.getValue();
			if (!(oKey instanceof String)) continue;
			if (!(oValue instanceof String)) continue;
			
			String optionID = (String) oKey;
			String optionValue = (String) oValue;
			
			// Unpack task tags
			if (optionID.equals(OPTION_TaskTags)) {
				
				if (optionValue.length() == 0) {
					this.taskTags = null;
				} else {	
					StringTokenizer tokenizer = new StringTokenizer(optionValue, ",");
					this.taskTags = new char[tokenizer.countTokens()][];
					int i = 0;
					
					while (tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken().trim();
						this.taskTags[i] = token.toCharArray();
						i++;
					}
				}
				
				continue;
			} 

			//	Unpack task priorities
			if (optionID.equals(OPTION_TaskPriorities)){
				
				if (optionValue.length() == 0) {
					this.taskPriorities = null;
				} else {
					StringTokenizer tokenizer = new StringTokenizer(optionValue, ",");
					this.taskPriorities = new char[tokenizer.countTokens()][];
					int i = 0;
					
					while (tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken().trim();
						this.taskPriorities[i] = token.toCharArray();
						i++;
					}
				}
				
				continue;
			} 
		}
	}


    public void setTaskTags(char[][] taskTags) {
        this.taskTags = taskTags;
    }

    public char[][] getTaskTags() {
        return taskTags;
    }

    public void setTaskPriorities(char[][] taskPriorities) {
        this.taskPriorities = taskPriorities;
    }

    public char[][] getTaskPriorities() {
        return taskPriorities;
    }

    
    public String toString() {
    
        StringBuffer buf = new StringBuffer("TranslationOptions:"); //$NON-NLS-1$
        String result = "";

        if (this.taskTags != null) {
            for (int i=0; i<this.taskTags.length; i++) {
                result += this.taskTags.toString();
                if (i<this.taskTags.length-1) result += ",";
            }
        }
        buf.append("\n-task tags: " + result);  //$NON-NLS-1$
        
        result = "";
        if (this.taskPriorities != null) {
            for (int i=0; i<this.taskPriorities.length; i++) {
                result += this.taskPriorities.toString();
                if (i<this.taskPriorities.length-1) result += ",";
            }
        }
        buf.append("\n-task priorities : " + result); //$NON-NLS-1$
        
        return buf.toString();
    }
}
