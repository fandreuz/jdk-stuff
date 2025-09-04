#include "Main.h"
#include <jni.h>
#include <iostream>

//#define INCBIN_SECTION ".section \".rodata\", \"a\""
//#define INCBIN_SYMBOL
//
//#define INCBIN(NAME, FILE) \
//    extern "C" const char NAME[];\
//    extern "C" const char NAME##_END[];\
//    asm(INCBIN_SECTION "\n"\
//        ".globl " INCBIN_SYMBOL #NAME "\n"\
//        INCBIN_SYMBOL #NAME ":\n"\
//        ".incbin \"" FILE "\"\n"\
//        ".globl " INCBIN_SYMBOL #NAME "_END\n"\
//        INCBIN_SYMBOL #NAME "_END:\n"\
//        ".byte 0x00\n"\
//        ".previous\n"\
//    );
//
//#define INCBIN_SIZEOF(NAME) (NAME##_END - NAME)
//
//#define INCLUDE_HELPER_CLASS(NAME_VAR, DATA_VAR, NAME) \
//    static const char* const NAME_VAR = NAME;\
//    INCBIN(DATA_VAR, NAME ".class")
//
//INCLUDE_HELPER_CLASS(LOCK_TRACER_NAME, LOCK_TRACER_CLASS, "LockTracer")

//static jclass _LockTracer;
static jclass _Main;
//static jmethodID _setEntry;

void setEntry0(JNIEnv* env, jclass cls, jlong entry) {
    const JNINativeMethod doStuff = {(char*)"doStuff", (char*)"()V", (void*)(uintptr_t)entry};
    env->RegisterNatives(_Main, &doStuff, 1);
}

void JNICALL hook(JNIEnv* env, jclass clazz) {
    std::cerr << "hook!" << std::endl;
}

void JNICALL notHook(JNIEnv* env, jclass clazz) {
    std::cerr << "not hook!" << std::endl;
}

JNIEXPORT void JNICALL Java_Main_init(JNIEnv* env, jclass clazz) {
    _Main = clazz;
    
    //jclass cls = env->DefineClass(LOCK_TRACER_NAME, NULL, (const jbyte*)LOCK_TRACER_CLASS, INCBIN_SIZEOF(LOCK_TRACER_CLASS));
    //_LockTracer = (jclass)env->NewGlobalRef(cls);
    //const JNINativeMethod method = {(char*)"setEntry0", (char*)"(J)V", (void*)setEntry0};
    //if (env->RegisterNatives(cls, &method, 1) != 0) {
    //    std::cerr << "RegisterNatives failed" << std::endl;
    //}
    //_setEntry = env->GetStaticMethodID(_LockTracer, "setEntry", "(J)V");
}

//void JNICALL Java_Main_doHook(JNIEnv* env, jclass clazz) {
//    env->CallStaticVoidMethod(_LockTracer, _setEntry, (jlong)(uintptr_t)hook);
//}
//
//void JNICALL Java_Main_doUnhook(JNIEnv* env, jclass clazz) {
//    env->CallStaticVoidMethod(_LockTracer, _setEntry, (jlong)(uintptr_t)notHook);
//}

void JNICALL Java_Main_doHookSimple(JNIEnv* env, jclass clazz) {
    const JNINativeMethod doStuff = {(char*)"doStuff", (char*)"()V", (void*)(uintptr_t)hook};
    env->RegisterNatives(_Main, &doStuff, 1);
}

void JNICALL Java_Main_doUnhookSimple(JNIEnv* env, jclass clazz) {
    const JNINativeMethod doStuff = {(char*)"doStuff", (char*)"()V", (void*)(uintptr_t)notHook};
    env->RegisterNatives(_Main, &doStuff, 1);
}
