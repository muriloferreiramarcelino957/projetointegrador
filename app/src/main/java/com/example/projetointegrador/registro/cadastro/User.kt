package com.example.projetointegrador.registro.cadastro

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val nomeUsuario: String,
    val email: String,
    val dataNascimento: String,
    val cpf: String,
    val cep: String,
    val tipoLogradouro: String,
    val descLogradouro: String,
    val numero: Int,
    val bairro: String,
    val cidade: String,
    val estado: String
) : Parcelable
