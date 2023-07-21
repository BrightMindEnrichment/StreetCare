package org.brightmindenrichment.street_care.ui.home.data

  data class BeforePageData (var beforeIntro : String){
     // To keep map of contents
     private val beforeContents = mutableMapOf<String, String>()

     // Function to add contents
     fun addBeforePageContent(beforeContentKey : String, beforeContentValue : String){
         beforeContents[beforeContentKey] = beforeContentValue
     }

     // Function to get contents
     fun getBeforePageContent(beforeContentKey : String): String? {
         return beforeContents[beforeContentKey]
     }
 }