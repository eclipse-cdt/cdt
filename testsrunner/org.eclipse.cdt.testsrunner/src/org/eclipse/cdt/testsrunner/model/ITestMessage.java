/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.model;

/**
 * Describes the message that was produced during the testing process.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITestMessage {

	/**
	 * The level of the test message.
	 */
	public enum Level {
		Info(ModelMessages.MessageLevel_info), Message(ModelMessages.MessageLevel_message),
		Warning(ModelMessages.MessageLevel_warning), Error(ModelMessages.MessageLevel_error),
		FatalError(ModelMessages.MessageLevel_fatal_error), Exception(ModelMessages.MessageLevel_exception);

		String stringRepr;

		Level(String stringRepr) {
			this.stringRepr = stringRepr;
		}

		@Override
		public String toString() {
			return stringRepr;
		}
	}

	/**
	 * Returns the location of the test message.
	 *
	 * @return message location
	 */
	public ITestLocation getLocation();

	/**
	 * Returns the level of the test message.
	 *
	 * @return message level
	 */
	public Level getLevel();

	/**
	 * Returns the text of the test message.
	 *
	 * @return message text
	 */
	public String getText();

	/**
	 * Visitor pattern support for the tests hierarchy.
	 *
	 * @param visitor - any object that supports visitor interface
	 */
	public void visit(IModelVisitor visitor);

}
