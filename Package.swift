// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "MtwCapacitorUsbHid",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "MtwCapacitorUsbHid",
            targets: ["CapacitorUSBPluginPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "CapacitorUSBPluginPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/CapacitorUSBPluginPlugin"),
        .testTarget(
            name: "CapacitorUSBPluginPluginTests",
            dependencies: ["CapacitorUSBPluginPlugin"],
            path: "ios/Tests/CapacitorUSBPluginPluginTests")
    ]
)