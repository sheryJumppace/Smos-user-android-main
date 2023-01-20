package com.smox.smoxuser.ui.fragment

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smox.smoxuser.R;


/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    PhotoPickerDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 *
 * You activity (or fragment) needs to implement [PhotoPickerDialogFragment.Listener].
 */
class PhotoPickerDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private var mListener: Listener? = null
    fun setOnListener(listener: Listener){
        mListener = listener
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v =  inflater.inflate(R.layout.fragment_photo_picker_dialog, container, false)
        v.findViewById<View>(R.id.btnPhoto).setOnClickListener(this)
        v.findViewById<View>(R.id.btnGallery).setOnClickListener(this)
        v.findViewById<View>(R.id.btnCancel).setOnClickListener(this)
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
            R.id.btnPhoto -> mListener?.onPhotoClicked()
            R.id.btnGallery -> mListener?.onGalleryClicked()
            R.id.btnCancel -> mListener?.onCancel()
        }
        dismiss()
    }

    interface Listener {
        fun onPhotoClicked()
        fun onGalleryClicked()
        fun onCancel()
    }

}
