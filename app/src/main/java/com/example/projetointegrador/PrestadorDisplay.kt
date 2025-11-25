package com.example.projetointegrador

import com.example.projetointegrador.registro.cadastro.Prestador
import com.example.projetointegrador.registro.cadastro.User

data class PrestadorDisplay(
    val user: User,
    val prestador: Prestador,
    val servicos: Map<String, String>? = null,
    val dataCadastro: String? = null,
    val ultimoAcesso: String? = null
)
