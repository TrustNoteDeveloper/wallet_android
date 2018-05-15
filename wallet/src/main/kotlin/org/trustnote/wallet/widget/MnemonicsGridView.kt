package org.trustnote.wallet.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.TextView
import org.trustnote.wallet.R


class MnemonicsGridView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var gridView: GridView
    var gridAdapter: MnemonicAdapter
    var err: TextView
    var onCheckResult = { isAllWordOK: Boolean -> }

    init {
        val view = View.inflate(context, R.layout.w_mnemonics_grid, null)
        addView(view)

        gridView = findViewById<GridView>(R.id.grid_view)
        err = findViewById<TextView>(R.id.err)

        val wordPlaceHolder = List(12) {
            ""
        }
        gridAdapter = MnemonicAdapter(context, wordPlaceHolder)
        gridView.adapter = gridAdapter

        gridAdapter.onCheckResult = {
            onCheckResult(it)
            err.visibility = if (it) INVISIBLE else View.VISIBLE
        }
    }

    fun setMnemonic(mnemonic: String, isVerify: Boolean) {

        gridAdapter.verifyEnabled = isVerify
        gridAdapter.mMnemonic = mnemonic.split(" ")

        gridAdapter.notifyDataSetInvalidated()

        err.visibility = GONE
    }

    fun setCheckMnemonic(mnemonic: String) {
        gridAdapter.mMnemonicCheck = mnemonic.split(" ")
    }

}


class MnemonicAdapter(private val context: Context, mnemonic: List<String>) : BaseAdapter() {
    var mMnemonic: List<String> = mnemonic
    var onCheckResult = { isAllWordOK: Boolean -> }
    val editTextCache = HashMap<Int, MnemonicAutoCompleteTextView>()
    var verifyEnabled = true
    var mMnemonicCheck: List<String> = mnemonic

    override fun getCount(): Int = mMnemonic.size

    override fun getItem(position: Int): String? = null

    override fun getItemId(position: Int): Long = 0L

    // create a new ImageView for each item referenced by the Adapter
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val editTextView: MnemonicAutoCompleteTextView
        //TODO: why warning.
        when (convertView == null) {
            true -> {
                editTextView = LayoutInflater.from(context).inflate(R.layout.item_mnemonic, parent, false) as MnemonicAutoCompleteTextView
            }

            false -> {
                editTextView = convertView as MnemonicAutoCompleteTextView
            }
        }

        editTextView.isEnabled = verifyEnabled

        if (mMnemonic[position].isNotEmpty()) {
            editTextView.setText(mMnemonic[position])
        }
        editTextCache[position] = editTextView

        //setup the focus for next/enter key event.
        val resourceId = context.resources.getIdentifier("mnemonic_$position", "id", context.packageName)
        var nextPosition = position + 1
        if (nextPosition == mMnemonic.size) {
            nextPosition = 0
        }
        val nextResourceId = context.resources.getIdentifier("mnemonic_$nextPosition", "id", context.packageName)
        editTextView.id = resourceId
        editTextView.nextFocusForwardId = nextResourceId

        if (verifyEnabled) {
            editTextView.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    checkAllWord()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        return editTextView
    }

    fun checkAllWord() {
        //TODO: Bug, the first cell cannot get latest text. strange?
        for (entry in editTextCache) {
            val oneWord = entry.value.text.toString()
            if (oneWord.length < 3 || oneWord != mMnemonicCheck[entry.key]) {
                onCheckResult(false)
                return
            }
        }
        onCheckResult(true)
    }
}