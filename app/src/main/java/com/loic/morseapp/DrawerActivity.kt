package com.loic.morseapp

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.loic.morseapp.ui.settings.SettingsFragment
import com.loic.morseapp.ui.translate.TranslateFragment
import kotlinx.android.synthetic.main.activity_drawer.*
import kotlinx.android.synthetic.main.drawer_app_bar.*

/**
 * This activity manage different views (fragments) of this app with a drawer navigation.
 * Initiate the drawer menu.
 * Handle, create and add fragments of different views of the app.
 */
class DrawerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)
        title = ""

        // Add navigation drawer
        setSupportActionBar(drawer_toolbar)
        toggle = ActionBarDrawerToggle(this, drawer_layout, drawer_toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.setCheckedItem(R.id.drawer_menu_translate)

        // Init with the first view
        if (savedInstanceState == null) {
            replaceFragment(TranslateFragment.newInstance(), TranslateFragment.TAG)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (nav_view.checkedItem?.itemId != item.itemId) {
            when (item.itemId) {
                R.id.drawer_menu_translate -> replaceFragment(TranslateFragment.newInstance(), TranslateFragment.TAG)
                R.id.drawer_menu_settings -> replaceFragment(SettingsFragment.newInstance(), SettingsFragment.TAG)
            }
        }
        drawer_layout.closeDrawers()
        return true
    }

    /**
     * @param fragment Replace current fragment by this fragment.
     */
    private fun replaceFragment(fragment: Fragment, fragmentName: String? = null) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.drawer_fragment_container, fragment, fragmentName)
                .commit()
    }

    /**
     * @param fragment add this fragment on top of the current one.
     */
    private fun addFragment(fragment: Fragment, fragmentName: String? = null) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.drawer_fragment_container, fragment, fragmentName)
                .addToBackStack(null)
                .commit()
    }
}
