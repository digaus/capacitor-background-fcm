//
//  BackgroundFCMHandlerInterface.swift
//  Plugin
//
//  Created by Dirk Gausmann on 28.08.19.
//  Copyright © 2019 Max Lynch. All rights reserved.
//

import Foundation

protocol BackgroundFCMHandlerIntercace  {
    func setAdditionalData(additionalData: Dictionary<String, Any>) -> Void
}
	
