package com.imaxcorp.imaxc.ui.courier

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.imaxcorp.imaxc.R

class OrderFragment : Fragment() {
    private lateinit var toolBar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setToolbar()
    }
/*
    private fun setToolbar(){
        setHasOptionsMenu(true)
        toolBar = (context as CourierActivity).findViewById(R.id.toolbar)
        (context as CourierActivity).setSupportActionBar(toolBar)
        toolBar.inflateMenu(R.menu.collector_bar_menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.collector_bar_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menuConfig->{
                true
            }
            R.id.menuPorfile->{
                true
            }
            else->{
                super.onOptionsItemSelected(item)
            }
        }

    }

 */
}