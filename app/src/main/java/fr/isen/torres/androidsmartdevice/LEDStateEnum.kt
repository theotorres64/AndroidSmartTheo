package fr.isen.torres.androidsmartdevice

enum class LEDStateEnum(val hex: ByteArray){
    LED_1(byteArrayOf(0x01)),
    LED_2(byteArrayOf(0x02)),
    LED_3(byteArrayOf(0x03)),
    NONE(byteArrayOf(0x00))
}