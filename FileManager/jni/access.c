#include <jni.h>
#include <string.h>
#include <unistd.h>

jboolean Java_org_openintents_filemanager_util_FileUtils_access(JNIEnv * env, jclass clazz, jstring path, jint mode)
{
	jboolean isCopy;
	const char * szPath = (*env)->GetStringUTFChars(env, path, &isCopy);
	int result = access(szPath, mode);
	(*env)->ReleaseStringUTFChars(env, path, szPath);
	if(result == 0){
		return JNI_TRUE;
	} else {
		return JNI_FALSE;
	}
}
