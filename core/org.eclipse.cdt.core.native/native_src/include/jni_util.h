#ifndef JNI_UTIL_H
#define JNI_UTIL_H

static void ThrowByName(JNIEnv *env, const char *name, const char *msg) {
    jclass cls = (*env)->FindClass(env, name);

    if (cls) { /* Otherwise an exception has already been thrown */
        (*env)->ThrowNew(env, cls, msg);
    }

    /* It's a good practice to clean up the local references. */
    (*env)->DeleteLocalRef(env, cls);
}

#endif /* JNI_UTIL_H */
