package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.riversongai.R

class PlaceholderFragment : Fragment(R.layout.fragment_placeholder) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Title pulled from the nav label set in the nav graph
        val title = activity?.title?.toString()?.takeIf { it.isNotBlank() } ?: "Feature"
        view.findViewById<TextView>(R.id.placeholderTitle).text = title
    }
}
