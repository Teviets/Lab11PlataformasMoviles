package com.durini.solucionlab10.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.durini.solucionlab10.R
import com.durini.solucionlab10.datasource.api.RetrofitInstance
import com.durini.solucionlab10.datasource.local_source.DataBase
import com.durini.solucionlab10.datasource.model.Character
import com.durini.solucionlab10.datasource.model.CharactersResponse
import com.durini.solucionlab10.datasource.model.LoadCharacter
import com.durini.solucionlab10.ui.KEY_EMAIL
import com.durini.solucionlab10.ui.PREFERENCES_NAME
import com.durini.solucionlab10.ui.adapters.CharacterAdapter
import com.durini.solucionlab10.ui.dataStore
import com.durini.solucionlab10.ui.removePreferencesValue
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CharacterListFragment : Fragment(R.layout.fragment_character_list), CharacterAdapter.RecyclerViewCharactersEvents {

    private lateinit var characters: MutableList<LoadCharacter>
    private lateinit var adapter: CharacterAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerCharacters: RecyclerView
    private lateinit var database:DataBase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerCharacters = view.findViewById(R.id.recycler_characters)
        toolbar = view.findViewById(R.id.toolbar_characterList)
        database = Room.databaseBuilder(
            requireContext(),
            DataBase::class.java,
            "dbName"
        ).build()

        setToolbar()
        setListeners()
        getCharacters()
    }

    private fun setToolbar() {
        val navController = findNavController()
        val appbarConfig = AppBarConfiguration(setOf(R.id.characterListFragment))

        toolbar.setupWithNavController(navController, appbarConfig)
    }

    private fun setListeners() {
        toolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.deleteDB_item -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        database.characterDao().deleteAll()
                    }
                    true
                }

                R.id.menu_item_asc -> {
                    characters.sortBy { character -> character.name }
                    adapter.notifyDataSetChanged()
                    true
                }

                R.id.menu_item_des -> {
                    characters.sortByDescending { character -> character.name }
                    adapter.notifyDataSetChanged()
                    true
                }

                R.id.menu_item_logout -> {
                    logout()
                    true
                }
                else -> true
            }
        }
    }

    private fun getCharacters() {
        CoroutineScope(Dispatchers.IO).launch {
            val characts = database.characterDao().getAllLoadCharacters()
            if (characts.size >= 1){
                CoroutineScope(Dispatchers.Main).launch {
                    for (element in characts){
                        val miNuevoChar = LoadCharacter(
                            id = element.id,
                            name = element.name,
                            status = element.status,
                            species = element.species,
                            gender = element.gender,
                            image = element.image,
                            origin = element.origin,
                            episode = element.episode

                        )
                    }
                    setupRecycler(characts.toMutableList())
                }
            }else{
                laApi()
            }
        }
    }

    private fun setupRecycler(characters: MutableList<LoadCharacter>) {

        this.characters = characters

        adapter = CharacterAdapter(this.characters, this)
        recyclerCharacters.layoutManager = LinearLayoutManager(requireContext())
        recyclerCharacters.setHasFixedSize(true)
        recyclerCharacters.adapter = adapter
    }

    private fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            requireContext().dataStore.removePreferencesValue(KEY_EMAIL)
            CoroutineScope(Dispatchers.Main).launch {
                requireView().findNavController().navigate(
                    CharacterListFragmentDirections.actionCharacterListFragmentToLoginFragment()
                )
            }
        }
    }

    /*override fun onItemClicked(character: Character) {
        val action = CharacterListFragmentDirections.actionCharacterListFragmentToCharacterDetailsFragment(
            character.id.toInt()
        )

        requireView().findNavController().navigate(action)
    }*/
    fun laApi(){
        RetrofitInstance.api.getCharacters().enqueue(object: Callback<CharactersResponse> {
            override fun onResponse(
                call: Call<CharactersResponse>,
                response: Response<CharactersResponse>
            ) {
                if (response.isSuccessful) {
                    val res = response.body()?.results
                    val misChar = mutableListOf<LoadCharacter>()
                    if (res != null){
                        for (elem in res){
                            val x = LoadCharacter(
                                id = elem.id,
                                name = elem.name,
                                status = elem.status,
                                species = elem.species,
                                gender = elem.gender,
                                image = elem.image,
                                origin = elem.origin.toString(),
                                episode = elem.episode.size
                            )
                            misChar.add(x)
                        }
                    }
                    setupRecycler(misChar ?: mutableListOf())
                }
            }

            override fun onFailure(call: Call<CharactersResponse>, t: Throwable) {
                Toast.makeText(requireContext(), getString(R.string.error_fetching), Toast.LENGTH_LONG).show()
            }

        })
    }

    override fun onItemClicked(charact: LoadCharacter) {
        requireView().findNavController().navigate(
            CharacterListFragmentDirections.actionCharacterListFragmentToCharacterDetailsFragment(
                id = charact.id.toInt() ?: -1
            )
        )
    }

}