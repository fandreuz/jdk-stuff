#include <cstdint>
#include "Main.h"
#include <jni.h>

void JNICALL hook0(JNIEnv* env, jclass clazz) {}
void JNICALL hook1(JNIEnv* env, jclass clazz) {}

JNIEXPORT void JNICALL Java_Main_callRegisterNatives(JNIEnv * env, jclass clazz, jint idx) {
    const JNINativeMethod method = {(char*)"method", (char*)"()V",
                                    (void*)(uintptr_t) (idx == 1 ? hook0 : hook1)};
    env->RegisterNatives(clazz, &method, 1);
}
