package com.arathort.localinteraction.domain.model

data class OrganizerShortInfo(
    val id: String,
    val fullName: String? = null,
    val avatarUrl: String? = null,
    val rating: Double = 0.0
)

data class Event(
    val id: String,
    val title: String,
    val description: String = "",
    val categoryId: String? = null,
    val categoryName: String = "",
    val status: EventStatus = EventStatus.OPEN,
    val moderationStatus: ModerationStatus = ModerationStatus.APPROVED,
    val eventDate: String,
    val location: GeoPoint,
    val volunteerQuota: Int,
    val approvedVolunteersCount: Int = 0,
    val progressText: String = "Підтверджено: $approvedVolunteersCount з $volunteerQuota",
    val photos: List<String> = emptyList(),
    val organizer: OrganizerShortInfo = OrganizerShortInfo(id = ""),
    val rejectionReason: String? = null,
    val createdAt: String = ""
)
