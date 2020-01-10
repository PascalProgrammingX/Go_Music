package com.pca.gomusic.Fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.pca.gomusic.ModelClass.Streaming
import com.pca.gomusic.R


class StreamingFragment : Fragment() {

    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var documentReference: DocumentReference
    private lateinit var streamingList: ArrayList<Streaming>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_streaming, container, false)



        return view
    }


}

