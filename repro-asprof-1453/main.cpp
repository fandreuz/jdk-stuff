#include <cstdint>
#include "Main.h"
#include <jni.h>

void JNICALL hook1(JNIEnv* env, jclass clazz) {}
void JNICALL hook2(JNIEnv* env, jclass clazz) {}

void doHook(JNIEnv* env, jclass clazz, int i) {
    const JNINativeMethod method = {(char*)"bug", (char*)"()V",
                                    (void*)(uintptr_t) (i == 1 ? hook1 : hook2)};
    env->RegisterNatives(clazz, &method, 1);
}

JNIEXPORT void JNICALL Java_Main_hookUnhook(JNIEnv * env, jclass clazz) {
    doHook(env, clazz, 1);
    doHook(env, clazz, 2);
}
