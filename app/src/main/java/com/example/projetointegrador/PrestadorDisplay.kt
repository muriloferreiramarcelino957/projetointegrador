package com.example.projetointegrador

import com.example.projetointegrador.registro.cadastro.TiposServico
import com.example.projetointegrador.registro.cadastro.User
import com.projetointegrador.app.ui.Prestador

data class PrestadorDisplay(
    val user: User,
    val prestador: Prestador,
    val tiposServico: TiposServico?
)

