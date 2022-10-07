package com.durini.solucionlab10.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import coil.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.durini.solucionlab10.R
import com.durini.solucionlab10.datasource.api.RetrofitInstance
import com.durini.solucionlab10.datasource.local_source.DataBase
import com.durini.solucionlab10.datasource.model.Character
import com.durini.solucionlab10.datasource.model.LoadCharacter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CharacterDetailsFragment : Fragment(R.layout.fragment_character_details) {

    private val args: CharacterDetailsFragmentArgs by navArgs()
    private lateinit var txtName: TextInputLayout
    private lateinit var txtSpecies: TextInputLayout
    private lateinit var txtGender: TextInputLayout
    private lateinit var txtStatus: TextInputLayout
    private lateinit var txtOrigin: TextInputLayout
    private lateinit var txtEpisodes: TextInputLayout
    private lateinit var imageCharacter: ImageView
    private lateinit var btnSave: Button
    private lateinit var toolbar: MaterialToolbar
    private lateinit var database: DataBase


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.apply {
            txtName = findViewById(R.id.nameEditText)
            txtSpecies = findViewById(R.id.speciesEditText)
            txtGender = findViewById(R.id.genderEditText)
            txtStatus = findViewById(R.id.statusEditText)
            txtOrigin = findViewById(R.id.originEditText)
            txtEpisodes = findViewById(R.id.episodesEditText)
            imageCharacter = findViewById(R.id.image_characterDetails)
            btnSave = findViewById(R.id.loadButton)
            toolbar = findViewById(R.id.toolbar_characterDetails)
            database = Room.databaseBuilder(
                requireContext(),
                DataBase::class.java,
                "dbName"
            ).build()

        }

        setToolbar()
        getCharacter()
        setListener()
    }

    private fun setListener() {
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId){
                R.id.updateChar_item -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val x = database.characterDao().getLoadCharacter(args.id)
                        database.characterDao().update(
                            LoadCharacter(
                                id = x.id,
                                name = x.name,
                                status = x.status,
                                species = x.species,
                                gender = x.gender,
                                image = x.image,
                                origin = x.origin,
                                episode = x.episode
                            )
                        )
                    }
                    true
                }
                R.id.deleteCharDB_item -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        database.characterDao().deleteCharacter(args.id)
                    }
                    true
                }
                else -> true
            }

        }
        btnSave.setOnClickListener {

        }
    }

    private fun setToolbar() {
        val navController = findNavController()
        val appbarConfig = AppBarConfiguration(navController.graph)

        toolbar.setupWithNavController(navController, appbarConfig)
    }

    private fun getCharacter() {
        RetrofitInstance.api.getCharacter(args.id).enqueue(object: Callback<Character> {
            override fun onResponse(call: Call<Character>, response: Response<Character>) {
                if (response.isSuccessful && response.body() != null) {
                    var miChar = response.body()!!
                    val miMeroChar = LoadCharacter(
                        id = miChar.id,
                        name = miChar.name,
                        status = miChar.status,
                        species = miChar.species,
                        gender = miChar.gender,
                        image = miChar.image,
                        origin = miChar.origin.name.toString(),
                        episode = miChar.episode.size
                    )
                    setData(miMeroChar)
                }
            }

            override fun onFailure(call: Call<Character>, t: Throwable) {
                Toast.makeText(requireContext(), getString(R.string.error_fetching), Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun setData(character: LoadCharacter) {
        character.apply {
            txtName.editText!!.setText(character.name)
            txtSpecies.editText!!.setText(character.species)
            txtStatus.editText!!.setText(character.status)
            txtGender.editText!!.setText(character.gender)
            txtOrigin.editText!!.setText(character.origin)
            txtEpisodes.editText!!.setText(character.episode.toString())
            imageCharacter.load(image) {
                placeholder(R.drawable.ic_downloading)
                transformations(CircleCropTransformation())
                error(R.drawable.ic_error)
                memoryCachePolicy(CachePolicy.ENABLED)
            }
        }
    }

}