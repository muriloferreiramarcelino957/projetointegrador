package com.example.projetointegrador

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.widget.Switch
import android.widget.TextView
import android.widget.ImageView

class ConfiguracoesActivity : AppCompatActivity() {

    private lateinit var switchTema: Switch
    private lateinit var txtTema: TextView
    private lateinit var iconeTema: ImageView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Usa layout da sua tela
        setContentView(R.layout.tela_configuracoes)

        // Inicializa SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Ligações com XML
        switchTema = findViewById(R.id.switchTema)


        // Carregar estado salvo
        val isDarkMode = sharedPreferences.getBoolean("DARK_MODE", false)
        switchTema.isChecked = isDarkMode


        // Listener do switch
        switchTema.setOnCheckedChangeListener { _, isChecked ->
            salvarPreferencia(isChecked)
            aplicarTema(isChecked)

        }
    }

    private fun salvarPreferencia(isDarkMode: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("DARK_MODE", isDarkMode)
        editor.apply()
    }

    private fun aplicarTema(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }




}

