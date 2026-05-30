package com.talal.techhub.data

object ProductCatalog {

    val categories = linkedMapOf(
        "PC Components" to listOf("cpu", "mobo", "ram", "cooler", "fans", "gpu", "psu", "casing", "storage"),
        "Peripherals" to listOf("monitor", "keyboard", "mouse", "headphones", "speakers", "mic"),
        "Mobile Phones" to listOf("mobile_phone"),
        "Accessories" to listOf("accessory")
    )

    fun displayName(type: String): String {
        return when (type) {
            "cpu" -> "CPU"
            "mobo" -> "Motherboard"
            "ram" -> "RAM"
            "cooler" -> "CPU Cooler"
            "fans" -> "Case Fans"
            "gpu" -> "GPU"
            "psu" -> "Power Supply"
            "casing" -> "Casing"
            "storage" -> "Storage"
            "monitor" -> "Monitor"
            "keyboard" -> "Keyboard"
            "mouse" -> "Mouse"
            "headphones" -> "Headphones"
            "speakers" -> "Speakers"
            "mic" -> "Microphone"
            "mobile_phone" -> "Mobile Phones"
            "accessory" -> "Accessories"
            else -> type
        }
    }

    fun filterKeysFor(type: String): List<String> {
        return when (type) {
            "cpu" -> listOf("socket", "cores", "threads", "generation", "tdp", "integratedGpu")
            "mobo" -> listOf("socket", "chipset", "ramType", "formFactor", "wifi", "bluetooth")
            "ram" -> listOf("ramType", "capacityGb", "speedMhz", "sticks", "rgb")
            "cooler" -> listOf("coolerType", "supportedSockets", "radiatorSize", "heightMm")
            "fans" -> listOf("sizeMm", "rpm", "rgb", "packSize")
            "gpu" -> listOf("chipset", "vramGb", "vramType", "recommendedPsu", "ports")
            "psu" -> listOf("wattage", "efficiency", "modular")
            "casing" -> listOf("formFactorSupport", "gpuClearanceMm", "coolerClearanceMm", "fanSupport")
            "storage" -> listOf("storageType", "capacityGb", "interface")

            "monitor" -> listOf("sizeInch", "resolution", "refreshRate", "panelType", "adaptiveSync")
            "keyboard" -> listOf("layout", "switchType", "connection", "rgb", "hotswap")
            "mouse" -> listOf("dpi", "sensor", "connection", "weightG")
            "headphones" -> listOf("type", "connection", "mic", "surround")
            "speakers" -> listOf("channels", "powerW", "connection")
            "mic" -> listOf("type", "connection", "pattern")

            "mobile_phone" -> listOf(
                "brand", "chipset", "ram", "internalStorage", "displayType",
                "refreshRate", "batteryMah", "charging", "mainCamera", "ptaApproved"
            )

            "accessory" -> listOf("accessoryType", "compatibility", "color", "warranty")
            else -> emptyList()
        }
    }

    fun specsFor(type: String): List<String> {
        return when (type) {
            "cpu" -> listOf("socket", "cores", "threads", "baseClock", "boostClock", "tdp", "integratedGpu", "generation")
            "mobo" -> listOf("socket", "chipset", "ramType", "formFactor", "m2Slots", "pcieVersion", "wifi", "bluetooth")
            "ram" -> listOf("ramType", "capacityGb", "speedMhz", "sticks", "latency", "rgb")
            "cooler" -> listOf("coolerType", "supportedSockets", "radiatorSize", "heightMm", "tdpSupport")
            "fans" -> listOf("sizeMm", "rpm", "airflowCfm", "rgb", "packSize")
            "gpu" -> listOf("chipset", "vramGb", "vramType", "tdp", "lengthMm", "recommendedPsu", "ports")
            "psu" -> listOf("wattage", "efficiency", "modular", "formFactor")
            "casing" -> listOf("formFactorSupport", "gpuClearanceMm", "coolerClearanceMm", "psuSupport", "fanSupport")
            "storage" -> listOf("storageType", "capacityGb", "interface", "readSpeed", "writeSpeed")

            "monitor" -> listOf("sizeInch", "resolution", "refreshRate", "panelType", "responseTime", "adaptiveSync")
            "keyboard" -> listOf("layout", "switchType", "connection", "rgb", "hotswap", "keycaps")
            "mouse" -> listOf("dpi", "sensor", "connection", "weightG", "buttons")
            "headphones" -> listOf("type", "connection", "mic", "surround", "impedance")
            "speakers" -> listOf("channels", "powerW", "connection", "subwoofer")
            "mic" -> listOf("type", "connection", "pattern", "standIncluded")

            "mobile_phone" -> listOf(
                "network", "launchDate", "body", "sim", "displayType",
                "displaySize", "resolution", "refreshRate", "os", "chipset", "cpu", "gpu",
                "cardSlot", "internalStorage", "ram", "mainCamera", "selfieCamera",
                "video", "loudspeaker", "audioJack", "wifi", "bluetooth", "gps", "nfc",
                "usb", "sensors", "batteryMah", "charging", "colors", "priceRange",
                "ptaApproved"
            )

            "accessory" -> listOf("accessoryType", "compatibility", "material", "color", "warranty")
            else -> emptyList()
        }
    }
}