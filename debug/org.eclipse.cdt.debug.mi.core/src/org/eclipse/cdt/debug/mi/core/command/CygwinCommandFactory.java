/*
 *(c) Copyright Rational Software Corporation, 2002
 * All Rights Reserved.
 *
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * @author Doug Schaefer
 *
 * Cygwin Command Factory overrides the regular Command Factory to allow for
 * commands to take into account the cygwin environment.
 */
public class CygwinCommandFactory extends CommandFactory {

	public MIEnvironmentDirectory createMIEnvironmentDirectory(String[] pathdirs) {
		return new CygwinMIEnvironmentDirectory(pathdirs);
	}

}
