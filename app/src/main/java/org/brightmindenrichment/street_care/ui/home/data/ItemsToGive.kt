package org.brightmindenrichment.street_care.ui.home.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class ItemsToGive(@DrawableRes val imageResourceId : Int, @StringRes val stringResourceId : Int, @StringRes val stringResourceDetailsId : Int)