package com.example.projetointegrador

import com.example.projetointegrador.registro.cadastro.Prestador
import com.example.projetointegrador.registro.cadastro.User

data class PrestadorDisplay(
    val uid: String = "",
    val user: User = User(),
    val prestador: Prestador = Prestador(),
    val servicos: Map<String, String> = emptyMap(),
    val dataCadastro: String = "",
    val ultimoAcesso: String = "",
)
