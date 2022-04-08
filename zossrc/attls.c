#include "attls.h"

#include <resolv.h>
#include <ezbztlsc.h>

// this pragma generates string value encoded in ASCII
#if defined(__IBMC__) || defined(__IBMCPP__)
#pragma convert(819)
#endif

const char *ILLEGAL_STATE_EXCEPTION = "java/lang/IllegalStateException";
const char *SECURED = "secured";
const char *USER_ID = "userId";
const char *SIGNATURE_STRING = "Ljava/lang/String;";
const char *SIGNATURE_BOOLEAN = "Z";

#if defined(__IBMC__) || defined(__IBMCPP__)
#pragma convert(0)
#endif

JNIEXPORT void JNICALL Java_com_mycompany_Attls_00024AttlsInfo_fetchData (JNIEnv *env, jobject obj, jint socketId)
{
   struct TTLS_IOCTL ioc = {0};

   // fulfill request
   ioc.TTLSi_Ver = TTLS_VERSION1;
   ioc.TTLSi_Req_Type = TTLS_QUERY_ONLY;

   // call ioctl
   int rc = ioctl(socketId, SIOCTTLSCTL, (char*) &ioc);

   // if ioctl returns an error throw exception
   if (rc < 0) {
       char message[256] = {0};
       snprintf(message, 255, "Cannot obtain AT-TLS data about socket ID=%. Return code: %d", socketId, rc);
       __etoa(message);
       jclass exception = (*env) -> FindClass(env, ILLEGAL_STATE_EXCEPTION);
       (*env) -> ThrowNew(env, exception, message);
       return;
   }

   // get class of AttlsInfo
   jclass clazz = (*env) -> GetObjectClass(env, obj);

   // check if connection is secured and store the flag in AttlsInfo
   // 1 = non-secure, 2 = handshake in progress, 3 = secure
   jfieldID secured = (*env) -> GetFieldID(env, clazz, SECURED, SIGNATURE_BOOLEAN);
   (*env) -> SetBooleanField(env, obj, secured, (jboolean) (ioc.TTLSi_Stat_Conn == 3));

   // get userId, convert to ASCII and set into AttlsInfo
   jfieldID userId = (*env) -> GetFieldID(env, clazz, USER_ID, SIGNATURE_STRING);
   __etoa(ioc.TTLSi_UserID);
   (*env) -> SetObjectField(env, obj, userId, (*env) -> NewStringUTF(env, ioc.TTLSi_UserID));
}
