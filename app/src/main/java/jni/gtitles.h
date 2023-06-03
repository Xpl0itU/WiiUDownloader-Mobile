#include <stddef.h>
#include <stdint.h>
#include <jni.h>

typedef enum MCPRegion {
    MCP_REGION_JAPAN = 0x01,
    MCP_REGION_USA = 0x02,
    MCP_REGION_EUROPE = 0x04,
    MCP_REGION_CHINA = 0x10,
    MCP_REGION_KOREA = 0x20,
    MCP_REGION_TAIWAN = 0x40,
} MCPRegion;

typedef enum {
    TITLE_CATEGORY_GAME = 0,
    TITLE_CATEGORY_UPDATE = 1,
    TITLE_CATEGORY_DLC = 2,
    TITLE_CATEGORY_DEMO = 3,
    TITLE_CATEGORY_ALL = 4,
    TITLE_CATEGORY_DISC = 5,
} TITLE_CATEGORY;

typedef enum {
    TITLE_KEY_mypass = 0,
    TITLE_KEY_nintendo = 1,
    TITLE_KEY_test = 2,
    TITLE_KEY_1234567890 = 3,
    TITLE_KEY_Lucy131211 = 4,
    TITLE_KEY_fbf10 = 5,
    TITLE_KEY_5678 = 6,
    TITLE_KEY_1234 = 7,
    TITLE_KEY_ = 8,
    TITLE_KEY_MAGIC = 9,
} TITLE_KEY;

typedef struct
{
    const char *name;
    const uint64_t tid;
    const MCPRegion region;
    const TITLE_KEY key;
} TitleEntry;

const TitleEntry *getTitleEntries(TITLE_CATEGORY cat);
size_t getTitleEntriesSize(TITLE_CATEGORY cat);

JNIEXPORT jobjectArray JNICALL
Java_com_xpl0itu_wiiudownloader_1mobile_gtitlesWrapper_getTitleEntries(JNIEnv *env, jobject thiz,
                                                                       jint cat) {
    const TitleEntry* entries = getTitleEntries(cat);
    jclass titleEntryClass = (*env)->FindClass(env, "com/xpl0itu/wiiudownloader_mobile/gtitlesWrapper$TitleEntry");
    jmethodID titleEntryConstructor = (*env)->GetMethodID(env, titleEntryClass, "<init>",
                                                          "(Ljava/lang/String;JII)V");

    jobjectArray result = (*env)->NewObjectArray(env, getTitleEntriesSize(cat), titleEntryClass, NULL);

    for (int i = 0; i < getTitleEntriesSize(cat); i++) {
        jstring name = (*env)->NewStringUTF(env, entries[i].name);
        jlong tid = entries[i].tid;
        jint region = entries[i].region;
        jint key = entries[i].key;

        jobject titleEntryObject = (*env)->NewObject(env, titleEntryClass, titleEntryConstructor,
                                                     name, tid, region, key);

        (*env)->SetObjectArrayElement(env, result, i, titleEntryObject);

        (*env)->DeleteLocalRef(env, titleEntryObject);
        (*env)->DeleteLocalRef(env, name);
    }

    return result;
}

JNIEXPORT jlong JNICALL
Java_com_xpl0itu_wiiudownloader_1mobile_gtitlesWrapper_getTitleEntriesSize(JNIEnv *env,
                                                                           jobject thiz, jint cat) {
    return getTitleEntriesSize(cat);
}