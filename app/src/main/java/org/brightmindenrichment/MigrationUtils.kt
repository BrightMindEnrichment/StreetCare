package org.brightmindenrichment.street_care.ui.visit

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot


/**
 * Utility for migrating specific fields in Firestore documents.
 *
 * **Use with caution**: This can modify live data. Always test in a non-production environment.
 *
 * Example usage:
 *
 * ```
 * MigrationUtils.migrateSpecificDataForAllUsers<String, String>({
 *     when (it) {
 *         "Counselling and Mentoring" -> "Counseling and Mentoring"
 *         else -> it
 *     }
 * }, "helpType", "outreachEvents")
 * ```
 */
object MigrationUtils {
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    /**
     * Generic migration function to update specific fields in Firestore documents.
     *
     * @param mapToCorrectValue A lambda function that takes a value of type T and returns a value of type R.
     * @param fieldToUpdate The name of the field to update in the Firestore document.
     * @param collection The name of the Firestore collection.
     *
     * @param T The original type of the field value.
     * @param R The type of the transformed value after applying mapToCorrectValue.
     */
    fun <T, R> migrateSpecificDataForAllUsers(
        mapToCorrectValue: (T) -> R, fieldToUpdate: String, collection: String
    ) {
        firestore.collection(collection)
            .get()
            .addOnSuccessListener { snapshot: QuerySnapshot ->
                for (document in snapshot.documents) {
                    val field = document.get(fieldToUpdate)

                    val castedField: T? = try {
                        field as? T
                    } catch (e: ClassCastException) {
                        Log.w("Migration", "Field $fieldToUpdate in document ${document.id} is not casted correctly")
                        null
                    }

                    val newFieldValue = castedField?.let { mapToCorrectValue(it) }


                    newFieldValue?.let {
                        firestore.collection(collection)
                            .document(document.id)
                            .update(fieldToUpdate, it)
                            .addOnSuccessListener {
                                Log.d("Migration", "Updated field for document ${document.id}")
                            }
                            .addOnFailureListener { exception ->
                                Log.w("Migration", "Error updating document ${document.id}", exception)
                            }
                    }
                }
                Log.d("Migration", "Migration completed")
            }
            .addOnFailureListener { exception ->
                Log.w("Migration", "Error fetching documents for migration", exception)
            }
    }


}
