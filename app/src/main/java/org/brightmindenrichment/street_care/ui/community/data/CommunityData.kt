package org.brightmindenrichment.street_care.ui.community.data

class CommunityData(var event: Event?, var eventYear: EventYear?, var layoutType: Int) {

    constructor(event: Event,layoutType: Int): this(event, null, layoutType) {
        this.event = event
        this.layoutType = layoutType
    }

    constructor(eventYear: EventYear,layoutType: Int): this(null, eventYear, layoutType) {
        this.eventYear = eventYear
        this.layoutType = layoutType
    }
}