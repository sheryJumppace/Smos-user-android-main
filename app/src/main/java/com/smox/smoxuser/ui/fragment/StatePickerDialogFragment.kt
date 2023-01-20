package com.smox.smoxuser.ui.fragment

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import com.smox.smoxuser.R;
import com.smox.smoxuser.manager.Constants


/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    PhotoPickerDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 *
 * You activity (or fragment) needs to implement [StatePickerDialogFragment.Listener].
 */
class StatePickerDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private var mListener: Listener? = null
    private lateinit var picker: NumberPicker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v =  inflater.inflate(R.layout.fragment_state_picker_dialog, container, false)
        v.findViewById<View>(R.id.btnSelect).setOnClickListener(this)
        picker = v.findViewById<View>(R.id.picker) as NumberPicker
        picker.displayedValues = Constants.KUSAStates
        picker.minValue = 0
        picker.maxValue = Constants.KUSAStates.size-1
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parent = parentFragment

        if(mListener == null){
            mListener = if (parent != null) {
                parent as Listener
            } else {
                context as Listener
            }
        }
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnSelect -> mListener?.onSelectState(Constants.KUSAStates[picker.value])
        }
        dismiss()
    }

    interface Listener {
        fun onSelectState(state:String)
    }

}
