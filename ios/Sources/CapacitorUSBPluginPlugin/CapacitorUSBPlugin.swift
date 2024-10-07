import Foundation

@objc public class CapacitorUSBPlugin: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
