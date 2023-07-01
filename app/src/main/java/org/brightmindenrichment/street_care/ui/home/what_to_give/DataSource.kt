package org.brightmindenrichment.street_care.ui.home.what_to_give

import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.ui.home.data.ItemsToGive

class DataSource {
    fun loadWhatToGiveItems(): List<ItemsToGive> {
        return listOf(
            ItemsToGive(R.drawable.icon_snacks, R.string.healthy_snacks, R.string.healthy_snacks_details),
            ItemsToGive(R.drawable.icon_water, R.string.water, R.string.water_details),
            ItemsToGive(R.drawable.icon_bandages, R.string.first_aid, R.string.first_aid_details),
            ItemsToGive(R.drawable.icon_soap, R.string.personal_hygiene, R.string.personal_hygiene_details),
            ItemsToGive(R.drawable.ic_clothes, R.string.sock_clothing, R.string.sock_clothing_details),
            ItemsToGive(R.drawable.icon_feminine, R.string.feminine_hygiene, R.string.feminine_hygiene_details)

        )
    }
}