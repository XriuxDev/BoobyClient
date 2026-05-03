#!/bin/bash

# Booby Client Build Script

echo "Building Booby Client..."
echo "Java version:"
java -version

# Create build directory
mkdir -p build/classes
mkdir -p build/lib

# Download dependencies (using curl to fetch JAR files)
echo "Downloading dependencies..."

LIBS_DIR="build/lib"

# Function to download JAR
download_jar() {
    local url=$1
    local filename=$2
    if [ ! -f "$LIBS_DIR/$filename" ]; then
        echo "Downloading $filename..."
        wget -q -O "$LIBS_DIR/$filename" "$url" || curl -L -o "$LIBS_DIR/$filename" "$url"
    fi
}

# Download required libraries
download_jar "https://repo1.maven.org/maven2/org/openjfx/javafx-controls/21.0.2/javafx-controls-21.0.2.jar" "javafx-controls-21.0.2.jar"
download_jar "https://repo1.maven.org/maven2/org/openjfx/javafx-fxml/21.0.2/javafx-fxml-21.0.2.jar" "javafx-fxml-21.0.2.jar"
download_jar "https://repo1.maven.org/maven2/org/openjfx/javafx-graphics/21.0.2/javafx-graphics-21.0.2.jar" "javafx-graphics-21.0.2.jar"
download_jar "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" "gson-2.10.1.jar"
download_jar "https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.13.0/commons-lang3-3.13.0.jar" "commons-lang3-3.13.0.jar"
download_jar "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar" "slf4j-api-2.0.9.jar"
download_jar "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar" "slf4j-simple-2.0.9.jar"

# Build classpath
CLASSPATH="build/lib/*"

echo "Compiling source code..."
javac -d build/classes -cp "$CLASSPATH" src/main/java/com/boobyclient/**/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"

    # Copy resources
    cp -r src/main/resources/* build/classes/ 2>/dev/null

    # Create JAR
    echo "Creating JAR file..."
    cd build/classes
    jar cfe ../../booby-client.jar com.boobyclient.launcher.LauncherApp .
    cd ../../

    echo "Build complete! Run with:"
    echo "java -cp build/lib/*:build/classes com.boobyclient.launcher.LauncherApp"
else
    echo "Compilation failed!"
    exit 1
fi
