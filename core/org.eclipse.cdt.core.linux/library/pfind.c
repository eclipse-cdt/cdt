/*
 * pfind.c - Search for a binary in $PATH.
 */

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>

#ifndef PATH_MAX
#define PATH_MAX 1024
#endif


char *pfind( const char *name )
{
    char *tok;
	char *sp;
    char *path = getenv( "PATH" );
	char FullPath[PATH_MAX+1];

    if( name == NULL )
    {
        fprintf( stderr, "pfind(): Null argument.\n" );
        return NULL;
    }

    if( path == NULL || strlen( path ) <= 0 )
    {
        fprintf( stderr, "Unable to get $PATH.\n" );
        return NULL;
    }

    // The value return by getenv() is readonly */
    path = strdup( path );

    tok = strtok_r( path, ":", &sp );
    while( tok != NULL )
    {
        //strcpy( FullPath, tok );
        //strcat( FullPath, "/" );
        //strcat( FullPath, name );
		snprintf(FullPath, sizeof(FullPath) - 1, "%s/%s", tok, name);

        if( access( FullPath, X_OK | R_OK ) == 0 )
        {
            free( path );
            return strdup(FullPath);
        }

        tok = strtok_r( NULL, ":", &sp );
    }

    free( path );
    return NULL;
}

#ifdef BUILD_WITH_MAIN
int main( int argc, char **argv )
{
   int i;
   char *fullpath;

   for( i=1; i<argc; i++ )
   {
      fullpath = pfind( argv[i] );
      if( fullpath == NULL )
        printf( "Unable to find %s in $PATH.\n", argv[i] );
      else 
        printf( "Found %s @ %s.\n", argv[i], fullpath );
   }
}
#endif
