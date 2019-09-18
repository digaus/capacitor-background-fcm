import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/ios
 */
@objc(BackgroundFCM)
public class BackgroundFCM: CAPPlugin {
    
    @objc func setAdditionalData(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        let fileName = getDocumentsDirectory().appendingPathComponent("config.txt")
        do {
            try value.write(to: fileName, atomically: true, encoding: String.Encoding.utf8)
        } catch let err as NSError {
            call.error(err.localizedDescription)
        }
        call.success([
            "value": value
        ])
    }
    
    func getDocumentsDirectory() -> URL {
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        return paths[0]
    }
}

