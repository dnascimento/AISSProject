#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "protocol.h"

//char init(u32 mode);
JNIEXPORT jchar JNICALL Java_AesBox_init (JNIEnv * env, jobject obj, jint mode){
	printf("dario\n");
	u32 modo = (u32) mode;
	printf(modo);
	char str[10];
	char result = itoa(modo,str,10);

}

/*
 * Class:     AesBox
 * Method:    update
 * Signature: ([BI[BI)C
 */
//char  update(u8 * data_in, u32 size, u8 * data_out,u32 * size_out);
JNIEXPORT jchar JNICALL Java_AesBox_update (JNIEnv * env, jobject obj, jbyteArray  data_in, jint size, jbyteArray data_out, jint size_out){
	printf("dario\n");
}

/*
 * Class:     AesBox
 * Method:    doFinal
 * Signature: ([BI)C
 */
//char overloaded  doFinal(u8 * data_out,u32 *size_out);
JNIEXPORT jchar JNICALL Java_AesBox_doFinal___3BI (JNIEnv * env, jobject obj, jbyteArray data_out, jint size_out){
	printf("dario\n");
}

/*
 * Class:     AesBox
 * Method:    doFinal
 * Signature: ([BI[BI)C
 */
//char overloaded  doFinal(u8 * data_in, u32 size,u8 * data_out,u32 *size_out);
JNIEXPORT jchar JNICALL Java_AesBox_doFinal___3BI_3BI
  (JNIEnv * env, jobject obj, jbyteArray data_in, jint size, jbyteArray data_out, jint size_out){
	printf("dario\n");
}


