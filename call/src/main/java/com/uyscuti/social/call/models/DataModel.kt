package com.uyscuti.social.call.models

enum class DataModelType{
    SignIn, StartStreaming,EndCall, Offer, Answer, IceCandidates, StartVideoCall, StartVoiceCall,ToggleCamera, ReceiverInCall,DeclineCall
}


data class DataModel(
    val type: DataModelType?=null,
    val username:String,
    val target:String?=null,
    val data:Any?=null
)
