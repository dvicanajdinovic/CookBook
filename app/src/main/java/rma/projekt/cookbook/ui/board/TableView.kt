package rma.projekt.cookbook.ui.board

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TableView(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragmentList: MutableList<Fragment> = ArrayList()

    private val titleList: MutableList<Int> = ArrayList()

    fun getTitle(position: Int): Int {
        return titleList[position]
    }

    fun addFragment(fragment: Fragment, title: Int) {
        fragmentList.add(fragment)
        titleList.add(title)
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }
}