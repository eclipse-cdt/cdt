#include <unistd.h>
#include <malloc.h>
#include <spawn.h>
#include <signal.h>
#include <dlfcn.h>
#include <limits.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>

#include "Spawner.h"

typedef JNIEXPORT void * (JNICALL * JVM_GetThreadInterruptEvent)();
typedef JNIEXPORT char * (JNICALL * JVM_NativePath)(const char *);

void ThrowByName(JNIEnv *env, const char *name, const char *msg);
void * GetJVMProc(char * vmlib, char * procName);


static void * hVM = NULL; // Java Virtual Machine handler


JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_exec0
  (JNIEnv * env, jobject proc, jobjectArray cmdArray, jobjectArray envp, jstring dir, jintArray channels)
{
   int fd_map[3]; // File descriptors
   int fd_ret[3]; // File descriptors that we return to Java
   int fd[2];     // Pipe open structure
   int i;
   int nParms = 0;// Number of parameters
   int nEnvs  = 0;// Number of environment variables
   char ** pParms = NULL; // Parameters
   char ** pEnvs  = NULL; // Environment variables
   char * pCommand = NULL; // Command to execute
   char * pwd   = 0; // Process working directory
   char cwd[PATH_MAX + 1]; // Current working directory
   pid_t pid;       // Process ID
   struct inheritance inherit;
   
	
   
   if ((cmdArray == 0) || ((nParms = (*env) -> GetArrayLength(env, cmdArray)) == 0))
	ThrowByName(env, "java/lang/NullPointerException", "No command line specified");
   for(i = 0; i < 3; ++i) 
     {
     if(EOK != pipe(fd)) 
        ThrowByName(env, "java/io/IOException", "Cannot create pipe for spawner");
     if(0 ==i) 
        {
        fd_map[i] = fd[0];
     	fd_ret[i] = fd[1];
     	}
     else 
        {
        fd_map[i] = fd[1];
        fd_ret[i] = fd[0];
	    }
     }
   
   
   
   if(nParms > 0)
     {
     pParms = malloc(sizeof(char *) * (nParms + 1));
     for(i = 0; i < nParms; ++i)
	{
	jobject item = (*env) -> GetObjectArrayElement(env, cmdArray, i);
	const char *  str = (*env) -> GetStringUTFChars(env, item, 0);	
	if(i == 0)
	   pCommand = strdup(str);
	pParms[i] = strdup(str);
        (*env) -> ReleaseStringUTFChars(env, item, str);
	}
     pParms[i] = NULL;
     }
   nEnvs = (*env) -> GetArrayLength(env, envp);
   if(nEnvs > 0)
     {
     pEnvs = malloc(sizeof(char *) * (nEnvs + 1));
     for(i = 0; i < nEnvs; ++i)
	{
	jobject item = (*env) -> GetObjectArrayElement(env, envp, i);
	const char *  str = (*env) -> GetStringUTFChars(env, item, 0);	
	pEnvs[i] = strdup(str);
        (*env) -> ReleaseStringUTFChars(env, item, str);
	}
     pEnvs[i] = NULL;
     }

   if (dir != 0) 
     {
     char * item;
     pwd = strdup(item = (char *)(*env) -> GetStringUTFChars(env, dir, 0));
     getcwd(cwd, sizeof(cwd));
     chdir(pwd);
     (*env) -> ReleaseStringUTFChars(env, dir, item);	
     }
   
   // Nothing for now
   memset(&inherit, 0, sizeof(inherit));
   inherit.flags = SPAWN_SETGROUP;
   inherit.pgroup = SPAWN_NEWPGROUP;
   
   pid = spawnp(pCommand, 3, fd_map, &inherit, pParms, pEnvs);

   if(dir != 0) // Restore working directory
     chdir(cwd);
   
   for(i = 0; i < 3; ++i)
     {
     close(fd_map[i]);
     }
   
   if(-1 == pid) // Failed - close pipes
     {
     for(i = 0; i < 3; ++i)
       {
       close(fd_ret[i]);
       }
     }
   else // Success - return pipes to Java
     {
     (*env) -> SetIntArrayRegion(env, channels, 0, 3, fd_ret);
     }
   
   return pid;
   
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_exec1
  (JNIEnv * env, jobject proc, jobjectArray cmdArray, jobjectArray envp, jstring dir)
{
   int i;
   int nParms = 0;// Number of parameters
   int nEnvs  = 0;// Number of environment variables
   char ** pParms = NULL; // Parameters
   char ** pEnvs  = NULL; // Environment variables
   char * pCommand = NULL; // Command to execute
   char * pwd   = 0; // Process working directory
   char cwd[PATH_MAX + 1]; // Current working directory
   pid_t pid;       // Process ID
   struct inheritance inherit;
   
   
   if ((cmdArray == 0) || ((nParms = (*env) -> GetArrayLength(env, cmdArray)) == 0))
	ThrowByName(env, "java/lang/NullPointerException", "No command line specified");
   
   
   if(nParms > 0)
     {
     pParms = malloc(sizeof(char *) * (nParms + 1));
     for(i = 0; i < nParms; ++i)
	{
	jobject item = (*env) -> GetObjectArrayElement(env, cmdArray, i);
	const char *  str = (*env) -> GetStringUTFChars(env, item, 0);	
	if(i == 0)
	   pCommand = strdup(str);
	pParms[i] = strdup(str);
        (*env) -> ReleaseStringUTFChars(env, item, str);
	}
     pParms[i] = NULL;
     }
   nEnvs = (*env) -> GetArrayLength(env, envp);
   if(nEnvs > 0)
     {
     pEnvs = malloc(sizeof(char *) * (nEnvs + 1));
     for(i = 0; i < nEnvs; ++i)
	{
	jobject item = (*env) -> GetObjectArrayElement(env, envp, i);
	const char *  str = (*env) -> GetStringUTFChars(env, item, 0);	
	pEnvs[i] = strdup(str);
        (*env) -> ReleaseStringUTFChars(env, item, str);
	}
     pEnvs[i] = NULL;
     }

   if (dir != 0) 
     {
     char * item;
     pwd = strdup(item = (char *)(*env) -> GetStringUTFChars(env, dir, 0));
     getcwd(cwd, sizeof(cwd));
     chdir(pwd);
     (*env) -> ReleaseStringUTFChars(env, dir, item);	
     }
   
   // Nothing for now
   memset(&inherit, 0, sizeof(inherit));
   inherit.flags = SPAWN_SETGROUP;
   inherit.pgroup = SPAWN_NEWPGROUP;

   
   pid = spawnp(pCommand, 0, NULL, &inherit, pParms, pEnvs);

   
   if(dir != 0) // Restore working directory
     chdir(cwd);
   
   return pid;
   
}

/*
 * Class:     com_qnx_tools_utils_spawner_Spawner
 * Method:    raise
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_raise
  (JNIEnv * env, jobject proc, jint pid, jint sig)
{
   return kill(pid, sig);
}

/*
 * Class:     com_qnx_tools_utils_spawner_Spawner
 * Method:    waitFor
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_waitFor
  (JNIEnv * env, jobject proc, jint pid)
{
   int stat_loc;
   return (waitpid(pid, &stat_loc, WEXITED));
}


// Utilities

void ThrowByName(JNIEnv *env, const char *name, const char *msg)
{
    jclass cls = (*env)->FindClass(env, name);

    if (cls != 0) /* Otherwise an exception has already been thrown */
        (*env)->ThrowNew(env, cls, msg);

    /* It's a good practice to clean up the local references. */
    (*env)->DeleteLocalRef(env, cls);
}



void * GetJVMProc(char * vmlib, char * procName)
{
   if(NULL == vmlib)
     vmlib = "libj9vm14.so";
   if((NULL == hVM) || (NULL == procName))
     {
     if(NULL == (hVM = dlopen(vmlib, 0)))
	return NULL;
     }
   return dlsym(hVM, procName);
}
