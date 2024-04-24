package com.example.mapsapp.models

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Repository {
    private val db = FirebaseFirestore.getInstance()

    private val markersCollection = "myMarkers"


    fun saveMarker(newMarker: MyMarker) {
        db.collection(markersCollection)
            .add(
                hashMapOf(
                    "userId" to newMarker.userId,
                    "markerLatitude" to newMarker.position.latitude,
                    "markerLongitude" to newMarker.position.longitude,
                    "markerTitle" to newMarker.title,
                    "markerSnippet" to newMarker.snippet,
                    "markerColor" to newMarker.color,
                    "markerPhoto" to newMarker.photo
                )
            )
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener {e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }

    fun editMarker(editedMarker: MyMarker) {
        db.collection(markersCollection).document(editedMarker.markerId!!)
            .set(hashMapOf(
                "markerLatitude" to editedMarker.position.latitude,
                "markerLongitude" to editedMarker.position.longitude,
                "markerTitle" to editedMarker.title,
                "markerSnippet" to editedMarker.snippet,
                "markerColor" to editedMarker.color,
                "markerPhoto" to editedMarker.photo,
                "userId" to editedMarker.userId
            )
        )
    }

    fun deleteMarker(editedMarker: MyMarker) {
        db.collection(markersCollection).document(editedMarker.markerId!!)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }

    fun getMarkers(): CollectionReference {
        return db.collection(markersCollection)
    }
}