#include "Main.h"
#include <jni.h>
#include <iostream>

void JNICALL hook1(JNIEnv* env, jclass clazz, jboolean abs, jlong tm) {
    std::cerr << "hook!" << std::endl;
}

void JNICALL hook2(JNIEnv* env, jclass clazz, jboolean abs, jlong tm) {
    std::cerr << "not hook!" << std::endl;
}

void doHook(JNIEnv* env, jclass clazz, int i) {
    const JNINativeMethod park = {(char*)"park", (char*)"(ZJ)V", 
                                  (void*)(uintptr_t) (i == 1 ? hook1 : hook2)};
    jclass unsafe = env->FindClass("jdk/internal/misc/Unsafe");
    env->RegisterNatives(unsafe, &park, 1);
}

JNIEXPORT void JNICALL Java_Main_hookUnhook(JNIEnv * env, jclass clazz) {
    doHook(env, clazz, 1);
    doHook(env, clazz, 2);
}
