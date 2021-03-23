#include "study_acodexm_NativePanorama.h"
#include "opencv2/opencv.hpp"
#include <android/log.h>
#include "ImgStitcher.h"
#include "CroppImg.h"

#define  LOG_TAG    "NativePanorama"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  LOGP(...)  do{ __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__ ); FILE* f = fopen("/data/data/study.acodexm/files/jperformance.txt","a+"); fprintf(f, __VA_ARGS__); fprintf(f,"\r\n"); fclose(f); }while(0);

using namespace std;
using namespace cv;

int stitchImg(vector<Mat> &imagesArg, Mat &result, vector<string> params);

int cropp(Mat &result);
/*
 * This method uses the openCV Stitcher class to create panorama image from given pictures list
 * Additionally if the stitching was successful it crops the image to rectangular shape
 */
extern "C" JNIEXPORT void JNICALL
Java_study_acodexm_NativePanorama_processPanorama
        (JNIEnv *env, jclass clazz, jlongArray imageAddressArray, jlong outputAddress,
         jobjectArray stringArray) {

    bool isCropped = false;
    int size = env->GetArrayLength(stringArray);
    vector<string> params;
    bool useDefault = false;
    for (int i = 0; i < size; ++i) {
        auto args = (jstring) env->GetObjectArrayElement(stringArray, i);
        const char *value = env->GetStringUTFChars(args, nullptr);
        if (string(value) == "cropp") {
            isCropped = true;
        } else if (string(value) == "open_cv_default") {
            useDefault = true;
        } else {
            params.emplace_back(value);
        }
        env->ReleaseStringUTFChars(args, value);
        env->DeleteLocalRef(args);
    }

    // Get the length of the long array
    jsize a_len = env->GetArrayLength(imageAddressArray);
    // Convert the jlongArray to an array of jlong
    jlong *imgAddressArr = env->GetLongArrayElements(imageAddressArray, 0);
    // Create a vector to store all the image
    vector<Mat> imgVec;
    for (int k = 0; k < a_len; k++) {
        // Get the image
        Mat &curimage = *(Mat *) imgAddressArr[k];
        Mat newimage;
        // Convert to a 3 channel Mat
        cvtColor(curimage, newimage, CV_BGRA2RGB);
        curimage.release();
        imgVec.push_back(newimage);
        newimage.release();
    }
    if (useDefault) {
        int64 app_start_time = getTickCount();
        Mat &result = *(Mat *) outputAddress;
        Stitcher::Mode mode = Stitcher::PANORAMA;
        Ptr<Stitcher> stitcher = Stitcher::create(mode, false);
        Stitcher::Status status = stitcher->stitch(imgVec, result);
        LOGP("OpenCV Stitcher, total time: %f",
             ((getTickCount() - app_start_time) / getTickFrequency()));
        if (status != Stitcher::OK) {
            LOGE("Can't stitch images, error code = %d", int(status));
        } else {
            LOGD("Stitch SUCCESS");
            if (isCropped) {
                LOGD("cropping...");
                if (cropp(result) != 0) {
                    LOGE("cropping FAILED");
                } else {
                    LOGD("cropping SUCCESS");
                }
            }
        }
    } else {
        Mat &result = *(Mat *) outputAddress;
        int status = stitchImg(imgVec, result, params);
        if (status != 0) {
            LOGE("Can't stitch images, error code = %d", status);
        } else {
            LOGD("Stitch SUCCESS");
            if (isCropped) {
                LOGD("cropping...");
                if (cropp(result) != 0) {
                    LOGE("cropping FAILED");
                } else {
                    LOGD("cropping SUCCESS");
                }
            }
        }
    }
    // Release the jlong array
    env->ReleaseLongArrayElements(imageAddressArray, imgAddressArr, 0);
}

extern "C" JNIEXPORT jint JNICALL
Java_study_acodexm_NativePanorama_getProgress
        (JNIEnv *env, jclass clazz) {
    return getProgress();
}


extern "C" JNIEXPORT void JNICALL
Java_study_acodexm_NativePanorama_cropPanorama
        (JNIEnv *env, jclass clazz, jlong imageAddress, jlong outputAddress) {

    Mat &curimage = *(Mat *) imageAddress;
    Mat &result = *(Mat *) outputAddress;
    cvtColor(curimage, result, CV_RGB2BGRA);

    LOGD("cropping...");
    if (cropp(result) != 0) {
        LOGE("cropping FAILED");
    } else {
        LOGD("cropping SUCCESS");
    }
}