package com.anna.chroniclog.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.anna.chroniclog.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // do nothing when settings is clicked
                return false
            }
        }, viewLifecycleOwner)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMenu()

        val user = FirebaseAuth.getInstance().currentUser
        binding.tvUsername.text = if (user?.displayName.isNullOrEmpty()) "No Username" else user.displayName
        binding.tvEmail.text = user?.email

        binding.btnLogOut.setOnClickListener {
            // signout of firebase
            FirebaseAuth.getInstance().signOut()

            // start loginactivity
            val intent = Intent(requireContext(), LoginActivity::class.java)

            // clear backstack so user can't back into app
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}