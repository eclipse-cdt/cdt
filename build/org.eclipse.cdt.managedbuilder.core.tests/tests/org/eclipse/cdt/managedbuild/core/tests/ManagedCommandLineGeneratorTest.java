/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuild.core.tests;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator;

public class ManagedCommandLineGeneratorTest extends TestCase {

    private static String[] testCommandLinePatterns = {null, "${COMMAND}", "${COMMAND} ${FLAGS}", "${COMMAND} ${FLAGS} ${OUTPUT_FLAG}",
            "${COMMAND} ${FLAGS} ${OUTPUT_FLAG}${OUTPUT_PREFIX}", "${COMMAND} ${FLAGS} ${OUTPUT_FLAG}${OUTPUT_PREFIX}${OUTPUT}",
            "${COMMAND} ${FLAGS} ${OUTPUT_FLAG}${OUTPUT_PREFIX}${OUTPUT} ${INPUTS}", 
            "${command} ${flags} ${output_flag}${output_prefix}${output} ${WRONG_VAR_NAME}"};
    private static String COMMAND_VAL = "[command]";
    private static String FLAGS_VAL = "[flags]";
    private static String[] FLAGS_ARRAY_VAL = FLAGS_VAL.split( "\\s" );
    private static String OUTPUT_FLAG_VAL = "[outputFlag]";
    private static String OUTPUT_PREFIX_VAL = "[outputPrefix]";
    private static String OUTPUT_VAL = "[output]";
    private static String INPUTS_VAL = "[inputs]";
    private static String[] INPUTS_ARRAY_VAL = INPUTS_VAL.split( "\\s" );
    private static String[] commandLineEtalonesForPatterns = {
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + OUTPUT_VAL + " " + INPUTS_VAL,
            COMMAND_VAL, COMMAND_VAL + " " + FLAGS_VAL, COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL, 
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL,
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + OUTPUT_VAL,
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + OUTPUT_VAL + " " + INPUTS_VAL,
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + OUTPUT_VAL + " " + "${WRONG_VAR_NAME}" };
    private static String[] commandLineEtalonesForParameters = {
            FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + OUTPUT_VAL + " " + INPUTS_VAL,
            COMMAND_VAL + "  " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + OUTPUT_VAL + " " + INPUTS_VAL,
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_PREFIX_VAL + OUTPUT_VAL + " " + INPUTS_VAL,
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_VAL + " " + INPUTS_VAL,
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + " " + INPUTS_VAL,
            COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + OUTPUT_VAL + " ",
             };
    
    public ManagedCommandLineGeneratorTest( String name ) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite( ManagedCommandLineGeneratorTest.class.getName() );
        suite.addTest( new ManagedCommandLineGeneratorTest( "testGetCommandLineGenerator" ) );
        suite.addTest( new ManagedCommandLineGeneratorTest( "testGenerateCommandLineInfoPatterns" ) );
        //  TODO:  The parameters set to NULL in these tests are not currently allowed to be null
        //suite.addTest( new ManagedCommandLineGeneratorTest( "testGenerateCommandLineInfoParameters" ) );
        return suite;
    }
    
    public final void testGetCommandLineGenerator() {
        IManagedCommandLineGenerator gen = ManagedCommandLineGenerator.getCommandLineGenerator();
        assertNotNull( gen );
    }

    public final void testGenerateCommandLineInfoPatterns() {
        IManagedCommandLineGenerator gen = ManagedCommandLineGenerator.getCommandLineGenerator();
        IManagedCommandLineInfo info = null;
        for( int i = 0; i < testCommandLinePatterns.length; i++ ) {
            info = gen.generateCommandLineInfo( null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL, OUTPUT_VAL, INPUTS_ARRAY_VAL, 
                    testCommandLinePatterns[i] );
            assertNotNull( info );
            if( i < commandLineEtalonesForPatterns.length ) {
                assertEquals( info.getCommandLine(), commandLineEtalonesForPatterns[i] );
            }
        }
    }
    
    public final void testGenerateCommandLineInfoParameters() {
        IManagedCommandLineGenerator gen = ManagedCommandLineGenerator.getCommandLineGenerator();
        IManagedCommandLineInfo info = gen.generateCommandLineInfo( null, null, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL, OUTPUT_VAL, INPUTS_ARRAY_VAL, null );
        assertNotNull( info );
        assertEquals( info.getCommandLine(), commandLineEtalonesForParameters[0] );
        info = gen.generateCommandLineInfo( null, COMMAND_VAL, null, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL, OUTPUT_VAL, INPUTS_ARRAY_VAL, null );
        assertNotNull( info );
        assertEquals( info.getCommandLine(), commandLineEtalonesForParameters[1] );
        info = gen.generateCommandLineInfo( null, COMMAND_VAL, FLAGS_ARRAY_VAL, null, OUTPUT_PREFIX_VAL, OUTPUT_VAL, INPUTS_ARRAY_VAL, null );
        assertNotNull( info );
        assertEquals( info.getCommandLine(), commandLineEtalonesForParameters[2] );
        info = gen.generateCommandLineInfo( null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, null, OUTPUT_VAL, INPUTS_ARRAY_VAL, null );
        assertNotNull( info );
        assertEquals( info.getCommandLine(), commandLineEtalonesForParameters[3] );
        info = gen.generateCommandLineInfo( null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL, null, INPUTS_ARRAY_VAL, null );
        assertNotNull( info );
        assertEquals( info.getCommandLine(), commandLineEtalonesForParameters[4] );
        info = gen.generateCommandLineInfo( null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL, OUTPUT_VAL, null, null );
        assertNotNull( info );
        assertEquals( info.getCommandLine().trim(), commandLineEtalonesForParameters[5].trim() );
    }

}
