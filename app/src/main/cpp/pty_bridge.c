#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_vscodemobile_RunManager_nativeTest(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "PTY bridge loaded");
}