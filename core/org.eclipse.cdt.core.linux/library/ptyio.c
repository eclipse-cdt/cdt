#include <jni.h>
#include <stdio.h>
#include <PTYInputStream.h>
#include <PTYOutputStream.h>
#include <unistd.h>

/* Header for class _org_eclipse_cdt_utils_pty_PTYInputStream */
/* Header for class _org_eclipse_cdt_utils_pty_PTYOutputStream */

/*
 * Class:     org_eclipse_cdt_utils_pty_PTYInputStream
 * Method:    read0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_org_eclipse_cdt_utils_pty_PTYInputStream_read0(JNIEnv * env,
                                                          jobject jobj,
                                                          jint jfd,
                                                          jbyteArray buf,
                                                          jint buf_len)
{
    int fd;
    int status;
    jbyte *data;
    int data_len;

    data = (*env)->GetByteArrayElements(env, buf, 0);
    data_len = buf_len;
    fd = jfd;

    status = read( fd, data, data_len );
    (*env)->ReleaseByteArrayElements(env, buf, data, 0);

    if (status == 0) {
        /* EOF. */
        status = -1;
    } else if (status == -1) {
        /* Error, toss an exception */
	/* Ignore the error for now, the debugger will attempt
	 * to close this multiple time.  */
#if 0
        jclass exception = (*env)->FindClass(env, "java/io/IOException");
        if (exception == NULL) {
            /* Give up.  */
            return -1;
        }
        (*env)->ThrowNew(env, exception, "read error");
#endif
    }

    return status;
}


/*
 * Class:     org_eclipse_cdt_utils_pty_PTYInputStream
 * Method:    close0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_org_eclipse_cdt_utils_pty_PTYInputStream_close0(JNIEnv * env,
                                                           jobject jobj,
                                                           jint fd)
{
    return close(fd);
}

/*
 * Class:     org_eclipse_cdt_utils_pty_PTYOutputStream
 * Method:    write0
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL
Java_org_eclipse_cdt_utils_pty_PTYOutputStream_write0(JNIEnv * env,
                                                            jobject jobj,
                                                            jint jfd,
                                                            jbyteArray buf,
                                                            jint buf_len)
{
    int status;
    int fd;
    jbyte *data;
    int data_len;

    data = (*env)->GetByteArrayElements(env, buf, 0);
    data_len = buf_len;
    fd = jfd;

    status = write(fd, data, data_len);
    (*env)->ReleaseByteArrayElements(env, buf, data, 0);

    return status;
}


/*
 * Class:     org_eclipse_cdt_utils_pty_PTYOutputStream
 * Method:    close0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_org_eclipse_cdt_utils_pty_PTYOutputStream_close0(JNIEnv * env,
                                                            jobject jobj,
                                                            jint fd)
{
    return close(fd);
}
