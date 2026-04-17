package com.anna.chroniclog.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FdaReactionResponse(val results: List<EventResult>)
data class EventResult(val patient: PatientInfo)

data class PatientInfo(
    // a single event report can have multiple symptoms/reactions
    @SerializedName("reaction")
    val reactions: List<FdaReaction>
)

data class FdaReaction(
    @SerializedName("reactionmeddrapt")
    val name: String
) : Serializable {
    fun getDisplayName() = name
}