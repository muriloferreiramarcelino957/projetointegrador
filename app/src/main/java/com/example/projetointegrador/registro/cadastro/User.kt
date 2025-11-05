package com.example.projetointegrador.registro.cadastro

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var nomeUsuario: String = "",
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
