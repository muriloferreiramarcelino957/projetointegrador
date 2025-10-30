package com.example.projetointegrador.registro.cadastro

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var nomeUsuario: String,
    var email: String,
    var dataNascimento: String,
    var cpf: String,
    var cep: String,
    var tipoLogradouro: String,
    var descLogradouro: String,
    var numero: Int,
    var bairro: String,
    var cidade: String,
    var estado: String
) : Parcelable
