#include <unistd.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <signal.h>
#include <errno.h>

extern pid_t exec0(const char *path, char *const argv[],
                   char *const envp[], const char *dirpath,
                   int channels[3] );

extern int wait0(pid_t pid);
