package com.android.vinhphuc.mapswhimtest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

/**
 * Created by VINH PHUC on 10/8/2019
 */

class PlaceAdapter(context: Context, places: ArrayList<POIPlace>): BaseAdapter() {
    private val mContext = context
    private val mPlaces = places

    override fun getCount(): Int {
        return mPlaces.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return "TEST"
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val layoutInflater = LayoutInflater.from(mContext)
        val rowmain = layoutInflater.inflate(R.layout.row_main, viewGroup, false)

        val nameTextView = rowmain.findViewById<TextView>(R.id.name)
        nameTextView.text = "${position + 1}. ${mPlaces.get(position).title}"

        val latTextView = rowmain.findViewById<TextView>(R.id.lattitude)
        latTextView.text = "Lattitude: ${mPlaces.get(position).lat}"

        val lonTextView = rowmain.findViewById<TextView>(R.id.longitude)
        lonTextView.text = "Longitude: ${mPlaces.get(position).lon}"

        return rowmain
    }
}