package rma.projekt.cookbook.ui

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import rma.projekt.cookbook.R
import rma.projekt.cookbook.databinding.FragmentHomeBinding
import rma.projekt.cookbook.ui.board.TableView
import rma.projekt.cookbook.ui.favorites.FavoritesFragment
import rma.projekt.cookbook.ui.gallery.GalleryFragment
import rma.projekt.cookbook.ui.posts.PostsFragment

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        replaceFragment(GalleryFragment()) // Here is the starting point of the app

        binding.button.setOnClickListener{
            replaceFragment(GalleryFragment())
        }
        binding.button2.setOnClickListener{
            replaceFragment(FavoritesFragment())
        }
        binding.button3.setOnClickListener{
            replaceFragment(PostsFragment())
        }
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_Home_to_recipeAdd)
        }


        return root
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        setTable()
        chooseContent()

    }
    private fun setTable() {
        val adapter = TableView(requireActivity())
//        binding.pager.adapter = adapter

        //adapter.addFragment(NewsFragment(), R.string.news)
        //adapter.addFragment(FriendsFragment(), R.string.friends)
        //adapter.addFragment(MyRecipesFragment(), R.string.recipes)


//        binding.pager.offscreenPageLimit = adapter.itemCount
//
//        TabLayoutMediator(binding.tabs, binding.pager) {
//            tab, position -> tab.text = getString(adapter.getTitle(position))
//       }.attach()
    }

    private fun chooseContent() {
        binding.ibLogout.setOnClickListener { showLogoutConfirmationDialog() }
    }


    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Jeste li sigurni da se želite odjaviti?")
            .setPositiveButton("Da") { _, _ ->
                logout()
            }
            .setNegativeButton("Ne", null)
            .show()
    }

    private fun logout() {
        auth.signOut()
        findNavController().navigate(R.id.action_homeFragment_to_authentication)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}