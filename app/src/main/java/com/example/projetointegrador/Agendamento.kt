package com.example.projetointegrador.model

data class Agendamento(
    val data: String = "",
    val hora: String = "",
    val tipoServico: String = "",
    val prestador: String = "",
    val usuarioId: String = "",
    val status: String = "",
)

