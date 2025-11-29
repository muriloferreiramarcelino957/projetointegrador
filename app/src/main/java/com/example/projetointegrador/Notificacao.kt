package com.example.projetointegrador.model

data class Notificacao(
    val tipo: String = "",
    val agendamento_id: String = "",
    val mensagem: String = "",
    val data_hora: Long = 0,
    val lido: Boolean = false
)
