package org.eclipse.cdt.internal.corext.template.c;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.DateFormat;

import org.eclipse.cdt.internal.corext.template.SimpleTemplateVariable;
import org.eclipse.cdt.internal.corext.template.TemplateContext;


/**
 * Global variables which are available in any context.
 */
public class GlobalVariables {

	/**
	 * The cursor variable determines the cursor placement after template edition.
	 */
	static class Cursor extends SimpleTemplateVariable {
		public Cursor() {
			super("cursor", TemplateMessages.getString("GlobalVariables.variable.description.cursor"));
			setEvaluationString("");
			setResolved(true);
		}
	}

	/**
	 * The dollar variable inserts an escaped dollar symbol.
	 */
	static class Dollar extends SimpleTemplateVariable {
		public Dollar() {
			super("dollar", TemplateMessages.getString("GlobalVariables.variable.description.dollar"));
			setEvaluationString("$");
			setResolved(true);
		}
	}

	/**
	 * The date variable evaluates to the current date.
	 */
	static class Date extends SimpleTemplateVariable {
		public Date() {
			super("date", TemplateMessages.getString("GlobalVariables.variable.description.date"));
			setResolved(true);
		}
		public String evaluate(TemplateContext context) {
			return DateFormat.getDateInstance().format(new java.util.Date());
		}
	}		

	/**
	 * The time variable evaluates to the current time.
	 */
	static class Time extends SimpleTemplateVariable {
		public Time() {
			super("time", TemplateMessages.getString("GlobalVariables.variable.description.time"));
			setResolved(true);
		}
		public String evaluate(TemplateContext context) {
			return DateFormat.getTimeInstance().format(new java.util.Date());
		}
	}

	/**
	 * The user variable evaluates to the current user.
	 */
	static class User extends SimpleTemplateVariable {
		public User() {
			super("user", TemplateMessages.getString("GlobalVariables.variable.description.user"));
			setResolved(true);
		}
		public String evaluate(TemplateContext context) {
			return System.getProperty("user.name");
		}	
	}
}


