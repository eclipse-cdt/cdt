package org.eclipse.cdt.utils.spawner;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;


public class ProcessFactory {

    static private ProcessFactory instance;
    private boolean hasSpawner;
    private Runtime runtime;
    
    private ProcessFactory() {
        hasSpawner = false;
        runtime = Runtime.getRuntime();
        try 
        {
            System.loadLibrary( "spawner" );
            hasSpawner = true;
        }
        catch( SecurityException e )
        {
        }
        catch( UnsatisfiedLinkError e )
        {
        }
    }

    public static ProcessFactory getFactory() {
        if( instance == null )
            instance = new ProcessFactory();
        return instance;
    }

    public Process exec( String cmd ) throws IOException {
        if( hasSpawner )
            return new Spawner( cmd );
        return runtime.exec( cmd );
    }

    public Process exec( String[] cmdarray ) throws IOException {
        if( hasSpawner )
            return new Spawner( cmdarray );
        return runtime.exec( cmdarray );
    }

    public Process exec( String[] cmdarray, String[] envp ) throws IOException {
        if( hasSpawner )
            return new Spawner( cmdarray, envp );
        return runtime.exec( cmdarray, envp );
    }

    public Process exec( String cmd, String[] envp ) throws IOException {
        if( hasSpawner )
            return new Spawner( cmd, envp );
        return runtime.exec( cmd, envp );
    }

    public Process exec( String cmd, String[] envp, File dir ) throws IOException {
        if( hasSpawner )
            return new Spawner( cmd, envp, dir );
        return runtime.exec( cmd, envp, dir );
    }

    public Process exec( String cmdarray[], String[] envp, File dir ) 
        throws IOException 
    {
        if( hasSpawner )
            return new Spawner( cmdarray, envp, dir );
        return runtime.exec( cmdarray, envp, dir );
    }
}

