

# YOLO26n Android — Real-Time Object Detection

An Android application that integrates the lightweight **YOLO26n object detection model** for live camera-based object detection. This project demonstrates how computer vision and deep learning can be deployed in an Android APK using Kotlin and an on-device machine learning runtime.

**Developer:** Ankit Kushwaha  
**GitHub Repository:** https://github.com/Ankitkushwaha90/Android-Yolo-apk

## Overview

YOLO26n Android is a mobile computer vision project designed to run an object detection model directly on an Android device. The application uses a camera-based detection workflow to identify objects in live frames and display the detection results.

The goal is to bring AI-powered object detection from desktop environments to mobile devices, making computer vision accessible through a standalone Android application.

## YOLO26n Android Live Detection

The following screenshot demonstrates the Android application's live object detection interface.

![YOLO26n Android live object detection app screenshot](https://raw.githubusercontent.com/Ankitkushwaha90/Android-Yolo-apk/main/Screenshot%202026-10-29%20030140.png)
 
![YOLO26n Android object detection application](https://raw.githubusercontent.com/Ankitkushwaha90/Android-Yolo-apk/main/images.jpg)

*YOLO26n Android application — live object detection project.*

[View the project on GitHub](https://github.com/Ankitkushwaha90/Android-Yolo-apk)

*Figure: YOLO26n real-time object detection running in the Android application.*

### Project Repository

Explore the source code and Android APK on GitHub:

[View Android YOLO26n Project](https://github.com/Ankitkushwaha90/Android-Yolo-apk)


## Features

- **Real-Time Camera Detection:** Process live camera frames for object detection.
- **YOLO26n Model Integration:** Use a lightweight YOLO26n model for mobile computer vision.
- **Android APK:** Build and install the application on a compatible Android device.
- **Bounding Box Visualization:** Display detected objects and their locations when supported by the detection implementation.
- **On-Device Inference:** Run inference locally when the model and runtime are bundled with the application.
- **Android Studio Support:** Open, build, test, and customize the project using Android Studio.
- **Custom Model Potential:** Adapt the application to compatible custom-trained object detection models.

*Note: Actual features and inference capabilities depend on the model, application code, and runtime configuration included in the repository.*

## Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin / Android | Native Android application development |
| Android Studio | Development, debugging, and APK generation |
| YOLO26n | Object detection model |
| TensorFlow Lite / LiteRT | Model inference, if used by the implementation |
| CameraX / Android Camera API | Live camera input, depending on the implementation |
| Gradle | Dependency management and project builds |

The exact inference framework and camera library should match the dependencies configured in the project.

## How It Works

The general live object detection pipeline follows these steps:

1. **Camera input:** Capture frames from the Android device's camera.
2. **Preprocessing:** Resize and normalize each frame to match the model's input requirements.
3. **Model inference:** Pass the processed frame to the YOLO26n model.
4. **Output processing:** Interpret predicted classes, confidence scores, and bounding box coordinates.
5. **Visualization:** Map detection coordinates to the camera preview and render the results.
6. **Continuous detection:** Repeat the process as new camera frames arrive.

```text
Android Camera
      |
      v
Capture Live Frame
      |
      v
Image Preprocessing
      |
      v
YOLO26n Model
      |
      v
Detection Postprocessing
      |
      v
Bounding Boxes and Labels
      |
      v
Live Android Preview
```

## Repository Structure

The repository includes the Android application module, Gradle build configuration, and a debug APK.

```text
Android-Yolo-apk/
├── app/
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/ or kotlin/
│           ├── res/
│           └── assets/       # If the model is bundled here
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── app-debug.apk
└── README.md
```

The source files and model assets may differ from this illustrative structure. Check the actual `app/src/main/` directory for the model file, application entry point, and camera implementation.

## Requirements

- Android Studio
- A compatible JDK and Gradle version
- An Android device or emulator compatible with the project's configured SDK
- Camera permission for live detection
- A compatible YOLO26n model and inference runtime
- USB debugging enabled when deploying to a physical Android device

A physical Android device with a camera is recommended for testing live detection.

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Ankitkushwaha90/Android-Yolo-apk.git
cd Android-Yolo-apk
```

### 2. Open in Android Studio

1. Launch Android Studio.
2. Select **Open**.
3. Choose the cloned `Android-Yolo-apk` directory.
4. Allow Gradle synchronization to finish.
5. Resolve any missing SDK or dependency requirements.

### 3. Connect an Android device

Enable Developer Options and USB debugging on your Android phone, then connect it to your computer.

Verify the connection:

```bash
adb devices
```

Authorize the computer on your phone if prompted.

### 4. Build the APK

On Linux or macOS:

```bash
chmod +x gradlew
./gradlew assembleDebug
```

On Windows:

```powershell
gradlew.bat assembleDebug
```

If the build succeeds, the debug APK is normally generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 5. Install the APK

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Alternatively, open the APK on your Android device and install it, allowing installation from that source if Android asks.

### 6. Run live detection

1. Launch the installed application.
2. Grant camera permission when requested.
3. Open the live detection screen.
4. Point the camera at objects.
5. Observe the detection output provided by the application.

If the camera preview opens but objects are not detected, check the model loading process, input dimensions, output tensor format, label mapping, and inference logs.

## Model Integration

YOLO26n is the model variant targeted by this project. To use a compatible model, confirm the following:

- The model was exported for a runtime supported by the Android application.
- The input image size, color channel order, and normalization match the model.
- The model's output tensor shape is correctly interpreted.
- Class IDs map to the correct labels.
- Bounding box coordinates are converted correctly to the preview dimensions.
- Confidence filtering and non-maximum suppression, if required, match the model's output format.

For example, if the app uses a TensorFlow Lite model, its asset might be stored at:

```text
app/src/main/assets/model.tflite
```

The filename above is an example, not a guarantee of the model's current path in this repository. Use the actual filename referenced by the application code.

## Performance Optimization

Mobile inference performance depends on the model format, Android device, CPU/GPU/NPU support, input resolution, and camera pipeline.

Potential optimization techniques include:

- Use an appropriate input resolution.
- Consider a quantized model when supported and accuracy remains acceptable.
- Avoid running multiple inference jobs simultaneously.
- Keep heavy preprocessing and inference off the main UI thread.
- Reuse image buffers when practical.
- Release camera and interpreter resources when the application closes.
- Measure inference latency and end-to-end frame rate on the target device.

No specific FPS or latency is claimed without device-level benchmarking.

## Troubleshooting

| Problem | What to check |
|---|---|
| Camera does not open | Camera permission, manifest declarations, and camera lifecycle |
| Model fails to load | Asset path, model filename, and runtime compatibility |
| No objects detected | Input preprocessing, confidence threshold, output decoding, and labels |
| Incorrect bounding boxes | Image resizing, letterboxing, scaling, and coordinate conversion |
| Application crashes | Logcat output, memory usage, and interpreter initialization |
| Slow detection | Input size, inference threading, device capability, and runtime delegates |
| Gradle build fails | JDK version, SDK installation, Gradle configuration, and dependencies |

View application logs with:

```bash
adb logcat
```

## Use Cases

YOLO26n-based Android detection can be extended for:

- Educational computer vision demonstrations
- Real-time object detection experiments
- Smart camera applications
- Edge AI and offline inference prototypes
- Object detection in robotics projects
- Custom-trained object recognition applications
- Mobile AI research and development

The model's actual supported classes depend on the weights used. A custom-trained model may be required for specialized applications.

## Future Improvements

Possible future enhancements include:

- Detection confidence and FPS indicators
- Camera switching between front and rear cameras
- Image gallery detection
- Detection history and result export
- Custom model selection
- Quantized model support
- GPU or accelerator inference where supported
- Improved camera and inference pipeline performance
- A more detailed detection interface

## Contributing

Contributions and suggestions are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Implement and test your changes.
4. Submit a pull request describing the changes.

For bugs or feature requests, open an issue in the GitHub repository.

## Author

**Ankit Kushwaha**

- GitHub: https://github.com/Ankitkushwaha90
- LinkedIn: https://linkedin.com/in/ankitkushwaha90
- Hugging Face: https://huggingface.co/Ankitkushwaha90
- Website: https://tech3space.com

## License

Check the repository for a `LICENSE` file before reusing or redistributing the source code or model assets. The model's license and the application's license may have different terms.

## Acknowledgements

This project explores the practical deployment of YOLO-based computer vision on Android. Thanks to the developers and maintainers of the relevant Android, machine learning, and computer vision tools used in the implementation.

---

**If you find this project useful, consider starring the repository and sharing your feedback.**

Repository: https://github.com/Ankitkushwaha90/Android-Yolo-apk
