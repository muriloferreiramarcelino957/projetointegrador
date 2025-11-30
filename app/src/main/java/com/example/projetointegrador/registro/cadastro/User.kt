package com.example.projetointegrador.registro.cadastro

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class User(
    var nome: String = "",
    var email: String = "",
    var senha: String = "",
    var dataNascimento: String = "",
    var cpf: String = "",
    var cep: String = "",
    var logradouro: String = "",
    var numero: String = "",
    var bairro: String = "",
    var cidade: String = "",
    var estado: String = "",
    var prestador: Boolean = false
) : Parcelable
